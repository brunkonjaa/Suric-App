package ie.suric.suricapp

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ie.suric.suricapp.auth.*
import ie.suric.suricapp.chat.*
import ie.suric.suricapp.local.PersonalEntry
import ie.suric.suricapp.local.PersonalEntryStore
import ie.suric.suricapp.local.SharedEntryCodec
import ie.suric.suricapp.security.TokenVault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Base64

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = getSharedPreferences("suric_preferences", MODE_PRIVATE)
        setContent {
            var darkTheme by remember { mutableStateOf(preferences.getBoolean("dark_theme", false)) }
            SuricTheme(darkTheme) {
                val context = LocalContext.current
                SideEffect {
                    (context as? ComponentActivity)?.window?.let { window ->
                        val systemBar = if (darkTheme) android.graphics.Color.rgb(23, 27, 25) else android.graphics.Color.rgb(247, 243, 235)
                        window.statusBarColor = systemBar
                        window.navigationBarColor = systemBar
                        WindowInsetsControllerCompat(window, window.decorView).apply {
                            isAppearanceLightStatusBars = !darkTheme
                            isAppearanceLightNavigationBars = !darkTheme
                        }
                    }
                }
                val vm: ConnectionViewModel = viewModel(factory = ConnectionViewModel.Factory(application))
                val sharedText = remember { intent?.takeIf { it.action == Intent.ACTION_SEND }?.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString() }
                val deepLinkArticleId = remember {
                    intent?.data?.takeIf { it.scheme == "suric" && it.host == "library" }
                        ?.lastPathSegment?.takeIf { it.matches(Regex("[a-z0-9-]{1,100}")) }
                }
                SuricApp(vm, darkTheme, sharedText, deepLinkArticleId, onThemeChange = { enabled ->
                    darkTheme = enabled
                    preferences.edit().putBoolean("dark_theme", enabled).apply()
                })
            }
        }
    }
}

class ConnectionViewModel(private val app: android.app.Application) : ViewModel() {
    private val vault = TokenVault(app)
    private val oauth = OpenAiOAuth(app)
    private val api = ChatApi()
    private val _auth = MutableStateFlow<AuthState>(AuthState.SignedOut)
    val auth = _auth.asStateFlow()
    private val _conversation = MutableStateFlow(ConversationState())
    val conversation = _conversation.asStateFlow()
    private val conversationStore = EncryptedConversationStore(app)
    private val conversationPersistenceMutex = Mutex()
    private var conversationSaveJob: Job? = null
    private var conversationReadFailed = false
    private var tokenLoadError: String? = null
    private var tokens: Tokens? = runCatching { vault.load() }.getOrElse {
        tokenLoadError = "The saved ChatGPT connection could not be read. Reconnect to continue."
        null
    }
    private var model: String? = null
    private var chatJob: Job? = null
    private var prepareJob: Job? = null
    private var credentialClearJob: Job? = null
    private var credentialClearError: String? = null
    private var signInJob: Job? = null
    private val localEntries = PersonalEntryStore(app)
    private val _personalEntries = MutableStateFlow<List<PersonalEntry>>(emptyList())
    val personalEntries = _personalEntries.asStateFlow()
    private val _localDataError = MutableStateFlow<String?>(null)
    val localDataError = _localDataError.asStateFlow()
    private val localEntriesMutex = Mutex()
    private val credentialMutex = Mutex()

    private fun transitionAuth(event: AuthStateMachine.Event) {
        _auth.value = AuthStateMachine.transition(_auth.value, event)
    }

    init {
        runCatching { conversationStore.load() }
            .onSuccess { saved -> saved?.let { _conversation.value = ConversationState(messages = it.messages, draft = it.draft) } }
            .onFailure {
                conversationReadFailed = true
                _conversation.value = ConversationState(historyNotice = "Saved chat history could not be read and has not been overwritten.")
            }
        if (tokenLoadError != null) _auth.value = AuthState.Failed(tokenLoadError!!)
        else if (tokens != null) _auth.value = AuthState.SignedIn(tokens?.email)
        viewModelScope.launch { reloadEntries() }
    }

    fun signIn() {
        if (_auth.value == AuthState.SigningIn) return
        transitionAuth(AuthStateMachine.Event.Begin)
        signInJob = viewModelScope.launch {
            try {
                credentialClearJob?.join()
                credentialClearError?.let { error(it) }
                val result = oauth.signIn(tokens, vault.issuedClientId())
                credentialMutex.withLock {
                    if (signInJob?.isActive != true) throw kotlinx.coroutines.CancellationException()
                    withContext(Dispatchers.IO) { vault.save(result) }
                }
                tokens = result
                tokenLoadError = null
                transitionAuth(AuthStateMachine.Event.Success(result.email))
                prepareAssistant()
            } catch (_: kotlinx.coroutines.CancellationException) {
                transitionAuth(AuthStateMachine.Event.Cancel)
            } catch (e: Exception) {
                val message = e.message ?: "Sign-in failed. Please try again."
                if (message.contains("cancelled", true)) transitionAuth(AuthStateMachine.Event.Cancel)
                else transitionAuth(AuthStateMachine.Event.Error(message))
            }
        }
    }

    fun cancelSignIn() {
        oauth.cancel()
        signInJob?.cancel()
        signInJob = null
        transitionAuth(AuthStateMachine.Event.Cancel)
    }

    fun disconnect() {
        oauth.cancel()
        signInJob?.cancel()
        signInJob = null
        tokens = null
        model = null
        chatJob?.cancel()
        chatJob = null
        prepareJob?.cancel()
        prepareJob = null
        api.cancelActiveRequest()
        transitionAuth(AuthStateMachine.Event.Disconnect)
        if (_conversation.value.isLoading) {
            _conversation.value = _conversation.value.copy(isLoading = false, error = "The request stopped when you disconnected ChatGPT.")
            scheduleConversationSave()
        }
        credentialClearError = null
        credentialClearJob = viewModelScope.launch(Dispatchers.IO) {
            runCatching { credentialMutex.withLock { vault.clear() } }
                .onFailure { credentialClearError = it.message ?: "Couldn’t clear the saved ChatGPT connection." }
        }
    }

    suspend fun reloadEntries() = localEntriesMutex.withLock {
        runCatching { withContext(Dispatchers.IO) { localEntries.allEntries() } }
            .onSuccess { _personalEntries.value = it; _localDataError.value = null }
            .onFailure { _localDataError.value = it.message ?: "Suric could not read its saved notes. They have not been changed." }
    }
    suspend fun addEntry(section: String, title: String, body: String, sourceUrl: String? = null, sourceCheckedAt: String? = null): PersonalEntry = localEntriesMutex.withLock {
        withContext(Dispatchers.IO) { localEntries.add(section, title, body, sourceUrl, sourceCheckedAt).also { _personalEntries.value = localEntries.allEntries() } }
            .also { _localDataError.value = null }
    }
    suspend fun addShoppingIngredients(recipeTitle: String, ingredients: List<String>) = localEntriesMutex.withLock {
        withContext(Dispatchers.IO) { localEntries.addShoppingIngredients(recipeTitle, ingredients).also { _personalEntries.value = localEntries.allEntries() } }
            .also { _localDataError.value = null }
    }
    suspend fun deleteEntry(id: String) = localEntriesMutex.withLock {
        withContext(Dispatchers.IO) { localEntries.delete(id); _personalEntries.value = localEntries.allEntries() }
        _localDataError.value = null
    }
    suspend fun updateEntry(entry: PersonalEntry) = localEntriesMutex.withLock {
        withContext(Dispatchers.IO) { localEntries.update(entry); _personalEntries.value = localEntries.allEntries() }
        _localDataError.value = null
    }
    suspend fun restoreEntry(entry: PersonalEntry) = localEntriesMutex.withLock {
        withContext(Dispatchers.IO) { localEntries.restore(entry); _personalEntries.value = localEntries.allEntries() }
        _localDataError.value = null
    }
    suspend fun importSharedText(text: String): String {
        val cleaned = text.trim()
        require(cleaned.isNotEmpty()) { "The shared content is empty." }
        val sharedCopy = SharedEntryCodec.decodeOrNull(cleaned)
        val section = sharedCopy?.section ?: "Notes"
        val title = sharedCopy?.title?.ifBlank { "Shared copy" } ?: "Shared note"
        val body = sharedCopy?.body ?: cleaned
        runCatching { addEntry(section, title, body) }
            .onFailure { _localDataError.value = it.message ?: "Couldn’t import this shared note." }
            .getOrThrow()
        return section
    }

    fun updateDraft(value: String) {
        _conversation.value = _conversation.value.copy(draft = value)
        scheduleConversationSave()
    }
    fun send(text: String = _conversation.value.draft, image: Uri? = null) {
        val cleaned = text.trim()
        if ((cleaned.isEmpty() && image == null) || _conversation.value.isLoading) return
        val displayText = cleaned.ifEmpty { "What is in this picture?" }
        val before = _conversation.value
        _conversation.value = ConversationReducer.appendUser(before, displayText, image?.toString())
        scheduleConversationSave()
        chatJob = viewModelScope.launch {
            try {
                val active = validAccessToken()
                val selectedModel = model ?: loadModel(active)
                val dataUrl = image?.let { uri -> withContext(Dispatchers.IO) { uriToDataUrl(uri) } }
                val history = _conversation.value.messages
                withContext(Dispatchers.IO) {
                    api.stream(active, selectedModel, history, dataUrl) { delta ->
                        _conversation.value = ConversationReducer.appendDelta(_conversation.value, delta)
                        scheduleConversationSave()
                    }
                }
                _conversation.value = ConversationReducer.completed(_conversation.value)
                scheduleConversationSave()
            } catch (e: Exception) {
                if (chatJob?.isActive != true) return@launch
                if (e is ApiException && e.statusCode == 401) {
                    _conversation.value = ConversationReducer.failed(_conversation.value, "Your ChatGPT sign-in expired. Reconnect, then retry your question.")
                    scheduleConversationSave()
                    transitionAuth(AuthStateMachine.Event.Expired)
                } else {
                    _conversation.value = ConversationReducer.failed(_conversation.value, e.message ?: "The request failed. Try again.")
                    scheduleConversationSave()
                }
            }
        }
    }

    fun retry() {
        val lastUser = _conversation.value.messages.lastOrNull { it.role == "user" } ?: return
        val messages = _conversation.value.messages.toMutableList()
        while (messages.lastOrNull()?.role == "assistant") messages.removeAt(messages.lastIndex)
        if (messages.lastOrNull()?.role == "user") messages.removeAt(messages.lastIndex)
        _conversation.value = _conversation.value.copy(messages = messages, isLoading = false)
        scheduleConversationSave()
        send(lastUser.text, lastUser.imageUri?.let { Uri.parse(it) })
    }

    private suspend fun validAccessToken(): String {
        val current = tokens ?: error("Connect your ChatGPT account first.")
        if (!TokenExpiryPolicy.shouldRefresh(current.expiresAtEpochSeconds, System.currentTimeMillis() / 1000)) return current.accessToken
        return try {
            val refreshed = oauth.refresh(current)
            credentialMutex.withLock {
                if (_auth.value !is AuthState.SignedIn) throw kotlinx.coroutines.CancellationException()
                withContext(Dispatchers.IO) { vault.save(refreshed) }
            }
            tokens = refreshed
            refreshed.accessToken
        } catch (e: Exception) {
            throw ApiException(401, "Your ChatGPT sign-in expired. Reconnect your account and retry.")
        }
    }

    private suspend fun loadModel(access: String): String = withContext(Dispatchers.IO) {
        api.listModels(access).firstOrNull()?.also { model = it.slug; _conversation.value = _conversation.value.copy(modelLabel = it.displayName) }
            ?.slug ?: error("No models are currently available for this ChatGPT account.")
    }

    private fun scheduleConversationSave() {
        if (conversationReadFailed) return
        conversationSaveJob?.cancel()
        conversationSaveJob = viewModelScope.launch {
            delay(300)
            runCatching {
                conversationPersistenceMutex.withLock {
                    val latest = _conversation.value
                    withContext(Dispatchers.IO) { conversationStore.save(latest.messages, latest.draft) }
                }
            }.onSuccess {
                if (_conversation.value.historyNotice?.startsWith("Chat history could not be saved") == true) {
                    _conversation.value = _conversation.value.copy(historyNotice = null)
                }
            }.onFailure {
                _conversation.value = _conversation.value.copy(historyNotice = "Chat history could not be saved securely on this device.")
            }
        }
    }

    private fun prepareAssistant() {
        prepareJob?.cancel()
        prepareJob = viewModelScope.launch {
            try {
                val access = validAccessToken()
                loadModel(access)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                if (_auth.value is AuthState.SignedIn) {
                    _conversation.value = ConversationReducer.failed(_conversation.value, e.message ?: "Unable to prepare the ChatGPT connection.")
                }
            }
        }
    }

    private fun uriToDataUrl(uri: Uri): String {
        val mime = app.contentResolver.getType(uri)?.takeIf { it.startsWith("image/") } ?: error("The selected file is not an image.")
        val bytes = app.contentResolver.openInputStream(uri)?.use { input ->
            val out = ByteArrayOutputStream(); val buffer = ByteArray(8192); var count: Int
            while (input.read(buffer).also { count = it } >= 0) { out.write(buffer, 0, count); require(out.size() <= 10_000_000) { "Choose an image smaller than 10 MB." } }
            out.toByteArray()
        } ?: error("Unable to read the selected image.")
        return "data:$mime;base64," + Base64.getEncoder().encodeToString(bytes)
    }

    fun keepImageAccess(uri: Uri) {
        runCatching {
            app.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    class Factory(private val app: android.app.Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = ConnectionViewModel(app) as T
    }
}
