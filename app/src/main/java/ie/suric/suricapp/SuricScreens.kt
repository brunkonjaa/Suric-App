package ie.suric.suricapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.SheetValue
import ie.suric.suricapp.auth.AuthState
import ie.suric.suricapp.local.CalendarEvents
import ie.suric.suricapp.local.LocalCalendarEvent
import ie.suric.suricapp.local.PersonalEntry
import ie.suric.suricapp.local.PersonalEntryStore
import ie.suric.suricapp.local.OfflineArticle
import ie.suric.suricapp.local.OfflineLibrary
import ie.suric.suricapp.local.OfflineRecipe
import ie.suric.suricapp.local.OfflineRecipeCatalogue
import ie.suric.suricapp.local.OfflineReadingState
import ie.suric.suricapp.local.RecipeCatalogue
import ie.suric.suricapp.local.SharedEntryCodec
import ie.suric.suricapp.local.ShoppingResearchRules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date

internal data class PersonalArea(
    val name: String,
    val summary: String,
    val prompt: String,
    val image: Int
) {
    val displayName: String get() = if (name == "Shopping") "Shopping Assistant" else name
}

private val personalAreas = listOf(
    PersonalArea("Garden", "Plants, seasonal jobs and garden photos", "Help me with my garden in Kilkenny. Keep advice practical for Irish weather.", R.drawable.suric_garden),
    PersonalArea("Pottery", "Clay projects and studio notes", "Help me with pottery projects, clay, tools and firing notes. Ask when details matter.", R.drawable.suric_pottery),
    PersonalArea("Shopping", "Compare online options using links and details you provide", "Help me compare online-shopping options using the links and details I provide. Do not claim to open links or verify current prices, stock or delivery. Ask about my priorities when they would change the comparison.", R.drawable.suric_shopping),
    PersonalArea("Bills", "Monthly bills and due dates", "Help me organise household bills and due dates. Never assume a payment has been made.", R.drawable.suric_bills),
    PersonalArea("DIY & Home", "Repairs, projects and measurements", "Help me plan a safe, practical household DIY task. Ask about tools and measurements if needed.", R.drawable.suric_diy),
    PersonalArea("Cooking", "Meals, recipes and kitchen notes", "Help me cook with ingredients I already have and give clear, practical steps.", R.drawable.suric_cooking),
    PersonalArea("Cleaning", "Simple routines and household tasks", "Help me break cleaning into manageable steps and keep plans realistic.", R.drawable.suric_cleaning),
    PersonalArea("Retro & Android", "Consoles, phones and setup notes", "Help me with retro consoles and Android devices. Keep exact device and firmware details in mind.", R.drawable.suric_retro),
    PersonalArea("Music", "Play audio files you choose from this device", "Help me organise local music and listening notes.", R.drawable.suric_retro),
    PersonalArea("Notes", "A private place for useful details", "Use my selected personal notes only as context for the question I ask.", R.drawable.suric_home)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SuricApp(vm: ConnectionViewModel, darkTheme: Boolean, sharedText: String?, deepLinkArticleId: String?, onThemeChange: (Boolean) -> Unit) {
    val appContext = LocalContext.current
    val auth by vm.auth.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var route by rememberSaveable { mutableStateOf("home") }
    var selectedSection by rememberSaveable { mutableStateOf<String?>(null) }
    var showIncomingShare by rememberSaveable { mutableStateOf(false) }
    var incomingShareError by rememberSaveable { mutableStateOf<String?>(null) }
    var savingIncomingShare by rememberSaveable { mutableStateOf(false) }
    var deepLinkHandled by rememberSaveable { mutableStateOf(false) }
    var deepLinkSection by rememberSaveable { mutableStateOf<String?>(null) }
    var deepLinkMessage by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(auth, deepLinkArticleId) {
        if (auth is AuthState.SignedIn && !deepLinkHandled && !deepLinkArticleId.isNullOrBlank()) {
            val article = runCatching { withContext(Dispatchers.IO) { OfflineLibrary.load(appContext).firstOrNull { it.id == deepLinkArticleId } } }.getOrNull()
            if (article != null) {
                selectedSection = article.section
                deepLinkSection = article.section
                route = "home"
            } else deepLinkMessage = "This article is not included in this version of Suric's offline library."
            deepLinkHandled = true
        }
    }
    LaunchedEffect(auth, sharedText) {
        if (auth is AuthState.SignedIn && !sharedText.isNullOrBlank()) showIncomingShare = true
    }

    if (auth !is AuthState.SignedIn) {
        ConnectionScreen(auth, vm)
        return
    }
    val drawerIsVisible = drawerState.currentValue == DrawerValue.Open || drawerState.targetValue == DrawerValue.Open
    BackHandler(enabled = drawerIsVisible) {
        scope.launch { drawerState.close() }
    }
    BackHandler(enabled = !drawerIsVisible && (selectedSection != null || route != "home")) {
        selectedSection = null
        route = "home"
    }
    if (showIncomingShare && !sharedText.isNullOrBlank()) {
        val decoded = remember(sharedText) { runCatching { SharedEntryCodec.decodeOrNull(sharedText) } }
        val copy = decoded.getOrNull()
        AlertDialog(
            onDismissRequest = { if (!savingIncomingShare) showIncomingShare = false },
            title = { Text(if (copy != null) "Review shared copy" else "Review shared text") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (copy != null) "This will save a separate local copy in ${copy.section}." else "This text will be saved as a private note on this device.")
                    copy?.let { Text(it.title, fontWeight = FontWeight.SemiBold) }
                    Text(copy?.body ?: sharedText.take(1200), maxLines = 10, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Review it before saving. Nothing is added until you choose Save.", style = MaterialTheme.typography.bodySmall)
                    decoded.exceptionOrNull()?.let { Text(it.message ?: "This shared copy could not be read.", color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !savingIncomingShare && decoded.isSuccess,
                    onClick = {
                        savingIncomingShare = true
                        scope.launch {
                            runCatching { vm.importSharedText(sharedText) }
                                .onSuccess { section -> selectedSection = section; route = "home"; showIncomingShare = false }
                                .onFailure { incomingShareError = it.message ?: "Couldn’t save the shared copy." }
                            savingIncomingShare = false
                        }
                    }
                ) { Text(if (savingIncomingShare) "Saving…" else "Save copy") }
            },
            dismissButton = { TextButton(onClick = { showIncomingShare = false }, enabled = !savingIncomingShare) { Text("Cancel") } }
        )
    }
    incomingShareError?.let { message ->
        AlertDialog(onDismissRequest = { incomingShareError = null }, title = { Text("Couldn’t save") }, text = { Text(message) }, confirmButton = { TextButton(onClick = { incomingShareError = null }) { Text("OK") } })
    }
    deepLinkMessage?.let { message ->
        AlertDialog(onDismissRequest = { deepLinkMessage = null }, title = { Text("Article unavailable") }, text = { Text(message) }, confirmButton = { TextButton(onClick = { deepLinkMessage = null }) { Text("OK") } })
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 12.dp)) {
                    item { DrawerHeader() }
                    item { HorizontalDivider() }
                    item {
                        NavigationDrawerItem(
                            label = { Text("Home", maxLines = 1) },
                            selected = selectedSection == null && route == "home",
                            onClick = { selectedSection = null; route = "home"; scope.launch { drawerState.close() } },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
                        )
                    }
                    item {
                        NavigationDrawerItem(
                            label = { Text("Ask Suric", maxLines = 1) },
                            selected = route == "assistant",
                            onClick = { selectedSection = null; route = "assistant"; scope.launch { drawerState.close() } },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
                        )
                    }
                    item { Text("YOUR EVERYDAY", Modifier.padding(start = 28.dp, top = 14.dp, bottom = 6.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp) }
                    items(personalAreas, key = { it.name }) { area ->
                        NavigationDrawerItem(
                            label = { Text(area.displayName, maxLines = 1) },
                            selected = selectedSection == area.name,
                            onClick = { selectedSection = area.name; route = "home"; scope.launch { drawerState.close() } },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )
                    }
                    item { HorizontalDivider(Modifier.padding(top = 8.dp)) }
                    item {
                        Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text("Dark appearance", style = MaterialTheme.typography.bodyLarge)
                                Text("Saved on this device", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = darkTheme, onCheckedChange = onThemeChange, modifier = Modifier.semantics { contentDescription = "Toggle dark appearance" })
                        }
                    }
                    item {
                        TextButton(onClick = { vm.disconnect(); selectedSection = null; route = "home"; scope.launch { drawerState.close() } }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                            Icon(Icons.Default.Logout, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Disconnect ChatGPT")
                        }
                    }
                }
            }
        }
    ) {
        when {
            route == "assistant" -> ConversationScreen(
                email = (auth as? AuthState.SignedIn)?.email,
                vm = vm,
                onMenu = { scope.launch { drawerState.open() } }
            )
            selectedSection != null -> PersonalSectionScreen(
                section = selectedSection!!,
                initialArticleId = deepLinkArticleId.takeIf { selectedSection == deepLinkSection },
                vm = vm,
                onMenu = { scope.launch { drawerState.open() } },
                onAssistant = { context -> selectedSection = null; route = "assistant"; vm.send(context) }
            )
            else -> SuricHome(
                email = (auth as? AuthState.SignedIn)?.email,
                onMenu = { scope.launch { drawerState.open() } },
                onAssistant = { route = "assistant" },
                onSection = { selectedSection = it }
            )
        }
    }
}

@Composable
private fun DrawerHeader() {
    Box(Modifier.fillMaxWidth().height(184.dp)) {
        Image(painterResource(R.drawable.suric_home), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xD9000000)))))
        Column(Modifier.align(Alignment.BottomStart).padding(start = 24.dp, bottom = 20.dp)) {
            Text("Suric", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Your everyday, a little easier.", style = MaterialTheme.typography.bodyMedium, color = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuricHome(email: String?, onMenu: () -> Unit, onAssistant: () -> Unit, onSection: (String) -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Suric", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onMenu, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Menu, contentDescription = "Open sections") } },
                actions = { ConnectionPill() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            HomeHero(onAssistant)
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 10.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("Your spaces", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Text("Choose what you need right now.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("${personalAreas.size} areas", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                gridItems(personalAreas, key = { it.name }) { area -> AreaTile(area, onClick = { onSection(area.name) }) }
            }
            email?.let { Text("Connected privately", Modifier.align(Alignment.CenterHorizontally).padding(bottom = 6.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall) }
        }
    }
}

@Composable
private fun HomeHero(onAssistant: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp).height(184.dp)
            .clip(RoundedCornerShape(26.dp)).clickable(onClick = onAssistant)
    ) {
        Image(painterResource(R.drawable.suric_home), contentDescription = "A sunny garden patio with rosemary and a basket", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xE6000000)))))
        Column(Modifier.align(Alignment.BottomStart).padding(start = 20.dp, end = 20.dp, bottom = 18.dp)) {
            Text("A little help for everyday life", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1)
            Spacer(Modifier.height(5.dp))
            Text("Ask Suric", style = MaterialTheme.typography.labelLarge, color = Color.White)
        }
    }
}

@Composable
private fun AreaTile(area: PersonalArea, onClick: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        Modifier.fillMaxWidth().height(142.dp).shadow(5.dp, shape, ambientColor = Color(0x18000000), spotColor = Color(0x24000000))
            .clip(shape).clickable(onClick = onClick)
    ) {
        Image(painterResource(area.image), contentDescription = "${area.displayName} section", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xD9000000)))))
        Text(area.displayName, Modifier.align(Alignment.BottomStart).padding(start = 14.dp, end = 12.dp, bottom = 13.dp), maxLines = 1,
            style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ConnectionPill() {
    Row(Modifier.padding(end = 16.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 11.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
        Spacer(Modifier.width(7.dp))
        Text("Connected", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, maxLines = 1)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalSectionScreen(section: String, initialArticleId: String?, vm: ConnectionViewModel, onMenu: () -> Unit, onAssistant: (String) -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val readingState = remember(context) { OfflineReadingState(context) }
    var favouriteIds by remember { mutableStateOf(readingState.favourites()) }
    var recentIds by remember { mutableStateOf(readingState.recentlyViewed()) }
    var readingFilter by rememberSaveable(section) { mutableStateOf("all") }
    val area = personalAreas.first { it.name == section }
    val allSavedEntries by vm.personalEntries.collectAsState()
    val savedEntries = allSavedEntries.filter { it.section == section }
    val localDataError by vm.localDataError.collectAsState()
    var editingId by rememberSaveable(section) { mutableStateOf<String?>(null) }
    val editing = savedEntries.firstOrNull { it.id == editingId }
    var showEditor by rememberSaveable(section) { mutableStateOf(false) }
    var shareEntryId by rememberSaveable(section) { mutableStateOf<String?>(null) }
    var calendarEvents by remember { mutableStateOf<List<LocalCalendarEvent>>(emptyList()) }
    var calendarMessage by remember { mutableStateOf<String?>(null) }
    var calendarLoading by remember { mutableStateOf(false) }
    var addingCalendarEvents by remember { mutableStateOf(emptySet<String>()) }
    var savedCalendarEvents by remember { mutableStateOf(emptySet<String>()) }
    val snackbar = remember { SnackbarHostState() }
    val sectionListState = rememberLazyListState()
    var offlineArticles by remember(section) { mutableStateOf<List<OfflineArticle>>(emptyList()) }
    var recipeCatalogue by remember(section) { mutableStateOf<RecipeCatalogue?>(null) }
    var selectedRecipeId by rememberSaveable(section) { mutableStateOf<String?>(null) }
    var selectedRecipeCollectionId by rememberSaveable(section) { mutableStateOf("cuisine:ireland") }
    var recipeCollectionMenuExpanded by remember { mutableStateOf(false) }
    var offlineLibraryError by remember(section) { mutableStateOf<String?>(null) }
    var libraryQuery by rememberSaveable(section) { mutableStateOf("") }
    var selectedArticleId by rememberSaveable(section) { mutableStateOf(initialArticleId) }
    val selectedArticle = offlineArticles.firstOrNull { it.id == selectedArticleId }
    val selectedRecipe = recipeCatalogue?.recipes?.firstOrNull { it.id == selectedRecipeId }

    LaunchedEffect(selectedArticleId, selectedRecipeId) {
        val openedId = selectedArticleId?.let { "article:$it" } ?: selectedRecipeId?.let { "recipe:$it" }
        if (openedId != null) {
            readingState.markViewed(openedId)
            recentIds = readingState.recentlyViewed()
        }
    }

    fun setFavourite(id: String, saved: Boolean) {
        readingState.setFavourite(id, saved)
        favouriteIds = readingState.favourites()
    }

    if (section == "Music") {
        MusicSectionScreen(vm = vm, onMenu = onMenu)
        return
    }

    LaunchedEffect(section) {
        runCatching {
            withContext(Dispatchers.IO) {
                val articles = OfflineLibrary.load(context)
                articles to if (section == "Cooking") OfflineRecipeCatalogue.load(context, articles) else null
            }
        }
            .onSuccess { (articles, recipes) ->
                offlineArticles = articles
                recipeCatalogue = recipes
                if (recipes?.collections?.none { it.id == selectedRecipeCollectionId } != false) {
                    selectedRecipeCollectionId = recipes?.collections?.firstOrNull { it.id == "cuisine:ireland" }?.id
                        ?: recipes?.collections?.firstOrNull()?.id.orEmpty()
                }
                offlineLibraryError = null
            }
            .onFailure { offlineLibraryError = "Offline guides could not be opened. Your saved items are unchanged." }
    }
    BackHandler(enabled = selectedArticle != null || selectedRecipe != null) {
        if (selectedRecipe != null) selectedRecipeId = null else selectedArticleId = null
    }
    if (selectedRecipe != null) {
        OfflineRecipeDetail(
            recipe = selectedRecipe!!,
            isFavourite = "recipe:${selectedRecipe!!.id}" in favouriteIds,
            onFavourite = { saved -> setFavourite("recipe:${selectedRecipe!!.id}", saved) },
            onBack = { selectedRecipeId = null },
            onAsk = { onAssistant("Help me cook this recipe and adapt it to what I have. Do not invent ingredients or change the dish's origin.\n\n${selectedRecipe!!.name} (${selectedRecipe!!.country})\n${selectedRecipe!!.ingredients.joinToString("\n")}\n\n${selectedRecipe!!.instructions.joinToString("\n")}") },
            onCopy = {
                scope.launch {
                    val recipeBody = buildString {
                        appendLine("Origin: ${selectedRecipe!!.country}${selectedRecipe!!.region?.let { " • $it" }.orEmpty()}")
                        appendLine("Prep: ${selectedRecipe!!.prepMinutes} min | Cook: ${selectedRecipe!!.cookMinutes} min | Serves: ${selectedRecipe!!.servings}")
                        appendLine("\nINGREDIENTS")
                        selectedRecipe!!.ingredients.forEach { appendLine("• $it") }
                        appendLine("\nMETHOD")
                        selectedRecipe!!.instructions.forEachIndexed { index, step -> appendLine("${index + 1}. $step") }
                        appendLine("\nServe with: ${selectedRecipe!!.sides}")
                        appendLine("\nSubstitutions: ${selectedRecipe!!.substitutions}")
                        appendLine("\nStorage: ${selectedRecipe!!.storage}")
                    }
                    runCatching { vm.addEntry("Cooking", selectedRecipe!!.name, recipeBody.trim()) }
                        .onSuccess { snackbar.showSnackbar("Saved an independent recipe copy in Cooking.") }
                        .onFailure { snackbar.showSnackbar(it.message ?: "Couldn’t save a recipe copy.") }
                }
            },
            onShare = {
                val text = "${selectedRecipe!!.name} — ${selectedRecipe!!.country}\n${selectedRecipe!!.description}\n\nIngredients:\n${selectedRecipe!!.ingredients.joinToString("\n")}\n\nMethod:\n${selectedRecipe!!.instructions.mapIndexed { index, step -> "${index + 1}. $step" }.joinToString("\n")}\n\nServe with: ${selectedRecipe!!.sides}"
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                runCatching { context.startActivity(Intent.createChooser(sendIntent, "Share recipe")) }
                    .onFailure { scope.launch { snackbar.showSnackbar("Couldn’t open the Android share sheet.") } }
            }
        )
        return
    }
    if (selectedArticle != null) {
        OfflineArticleDetail(
            article = selectedArticle!!,
            isFavourite = "article:${selectedArticle!!.id}" in favouriteIds,
            onFavourite = { saved -> setFavourite("article:${selectedArticle!!.id}", saved) },
            onBack = { selectedArticleId = null },
            onShare = {
                val link = Uri.Builder().scheme("suric").authority("library").appendPath(selectedArticle!!.id).build()
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "${selectedArticle!!.title}\n${selectedArticle!!.summary}\n$link")
                }
                runCatching { context.startActivity(Intent.createChooser(sendIntent, "Share library article")) }
                    .onFailure { scope.launch { snackbar.showSnackbar("Couldn’t open the Android share sheet.") } }
            },
            onCopy = {
                scope.launch {
                    runCatching { vm.addEntry(selectedArticle!!.section, selectedArticle!!.title, selectedArticle!!.body) }
                        .onSuccess { snackbar.showSnackbar("Saved an independent copy in ${selectedArticle!!.section}.") }
                        .onFailure { snackbar.showSnackbar(it.message ?: "Couldn’t save a copy.") }
                }
            }
        )
        return
    }

    fun loadCalendar() {
        scope.launch {
            calendarLoading = true
            val result = runCatching { withContext(Dispatchers.IO) { CalendarEvents.upcoming(context.contentResolver) } }
            result.onSuccess { calendarEvents = it; calendarMessage = if (it.isEmpty()) "No upcoming events in the next 90 days." else null }
                .onFailure { calendarMessage = "Couldn’t read calendar events. Your saved items are unchanged." }
            calendarLoading = false
        }
    }
    val calendarPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) loadCalendar() else calendarMessage = "Calendar access was declined. You can add a date yourself."
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(area.displayName, maxLines = 1, fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onMenu, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Menu, contentDescription = "Open sections") } },
                actions = { IconButton(onClick = { onAssistant(area.prompt) }, modifier = Modifier.size(48.dp)) { Text("AI", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold) } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { editingId = null; showEditor = true }, icon = { Icon(Icons.Default.Add, contentDescription = null) }, text = { Text(if (section == "Shopping") "Add research" else "Add") })
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            state = sectionListState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (localDataError != null) item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(localDataError.orEmpty(), color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = { scope.launch { vm.reloadEntries() } }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Try again") }
                    }
                }
            }
            item { SectionHero(area) }
            item {
                Text(area.summary, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (section == "Cooking") item {
                OutlinedTextField(
                    value = libraryQuery,
                    onValueChange = { libraryQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    placeholder = { Text("Search recipes, ingredients and guides") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            if (section == "Cooking") item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("all" to "All", "favourites" to "Favourites", "recent" to "Recently viewed").forEach { (value, label) ->
                        FilterChip(selected = readingFilter == value, onClick = { readingFilter = value }, label = { Text(label, maxLines = 1) })
                    }
                }
            }
            if (section == "Cooking") {
                val activeRecipeCollection = recipeCatalogue?.collections?.firstOrNull { it.id == selectedRecipeCollectionId }
                    ?: recipeCatalogue?.collections?.firstOrNull()
                val featuredRecipes = activeRecipeCollection?.let { collection -> recipeCatalogue?.recipesIn(collection).orEmpty() }.orEmpty()
                val searchedRecipes = (if (libraryQuery.isBlank() && readingFilter == "all") featuredRecipes else recipeCatalogue?.let { catalogue ->
                    catalogue.sortedRecipes(catalogue.recipes.filter { it.searchableText.contains(libraryQuery.trim(), ignoreCase = true) })
                }.orEmpty()).filter { recipe ->
                    when (readingFilter) {
                        "favourites" -> "recipe:${recipe.id}" in favouriteIds
                        "recent" -> "recipe:${recipe.id}" in recentIds
                        else -> true
                    }
                }.let { recipes -> if (readingFilter == "recent") recipes.sortedBy { recentIds.indexOf("recipe:${it.id}").let { index -> if (index < 0) Int.MAX_VALUE else index } } else recipes }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Box {
                                OutlinedButton(onClick = { recipeCollectionMenuExpanded = true }, enabled = recipeCatalogue?.collections?.isNotEmpty() == true) {
                                    Text(if (libraryQuery.isBlank()) activeRecipeCollection?.title ?: "Recipe collections" else "Recipe search results", maxLines = 1)
                                }
                                DropdownMenu(expanded = recipeCollectionMenuExpanded, onDismissRequest = { recipeCollectionMenuExpanded = false }) {
                                    recipeCatalogue?.collections?.forEach { collection ->
                                        DropdownMenuItem(
                                            text = { Text(collection.title) },
                                            onClick = { selectedRecipeCollectionId = collection.id; libraryQuery = ""; recipeCollectionMenuExpanded = false }
                                        )
                                    }
                                }
                            }
                            Text("${searchedRecipes.size} recipes", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("Offline recipes with ingredients, timings and country of origin.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (activeRecipeCollection == null && offlineLibraryError == null) item {
                    Text("The recipe collection could not be opened.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                if (searchedRecipes.isEmpty() && libraryQuery.isNotBlank()) item {
                    Text("No bundled recipes match that search.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                items(searchedRecipes, key = { "recipe:${it.id}" }) { recipe ->
                    OutlinedCard(onClick = { selectedRecipeId = recipe.id }, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(recipe.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(listOfNotNull(recipe.country, recipe.region).joinToString(" • "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(recipe.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${recipe.prepMinutes} min prep • ${recipe.cookMinutes} min cook • Serves ${recipe.servings}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            if (section == "Shopping") item {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Use details you have checked", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(Modifier.height(4.dp))
                        Text("Save options with their links, details and the date you checked them. Tapping Compare sends these saved options to ChatGPT for analysis. Suric does not open retailer sites or verify live prices, stock or delivery. Check the retailer for current information.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            if (section == "Bills") {
                item {
                    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            Text("Calendar dates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text("Nothing is imported until you choose an event.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) loadCalendar()
                                    else calendarPermission.launch(Manifest.permission.READ_CALENDAR)
                                }, enabled = !calendarLoading, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                            ) {
                                if (calendarLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                else Text("Review upcoming dates", maxLines = 1)
                            }
                            calendarMessage?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
                if (calendarEvents.isNotEmpty()) item { Text("Choose a date to save", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                items(calendarEvents) { event ->
                    val eventKey = "${event.startsAtMillis}:${event.title}"
                    val adding = eventKey in addingCalendarEvents
                    val alreadySaved = eventKey in savedCalendarEvents
                    CalendarEventCard(event, adding, alreadySaved) {
                        if (adding || alreadySaved) return@CalendarEventCard
                        addingCalendarEvents = addingCalendarEvents + eventKey
                        val date = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(event.startsAtMillis))
                        scope.launch {
                            runCatching { vm.addEntry("Bills", event.title, "Calendar date: $date\n${event.notes.orEmpty()}".trim()) }
                                .onSuccess {
                                    savedCalendarEvents = savedCalendarEvents + eventKey
                                    calendarMessage = "Saved “${event.title}” on this device."
                                }
                                .onFailure { calendarMessage = it.message ?: "Couldn’t save that event." }
                            addingCalendarEvents = addingCalendarEvents - eventKey
                        }
                    }
                }
            }
            item {
                if (section == "Cooking") {
                    Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Offline cooking guides", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Text("${offlineArticles.count { it.section == section }} guides", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Offline library", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                            Text("${offlineArticles.size} guides", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("Bundled reading opens without internet. Search checks every section.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = libraryQuery,
                            onValueChange = { libraryQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            placeholder = { Text("Search offline guides and recipes") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }
            if (section != "Cooking") item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("all" to "All", "favourites" to "Favourites", "recent" to "Recently viewed").forEach { (value, label) ->
                        FilterChip(selected = readingFilter == value, onClick = { readingFilter = value }, label = { Text(label, maxLines = 1) })
                    }
                }
            }
            if (offlineLibraryError != null) item {
                Text(offlineLibraryError.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            val recipeArticleIds = recipeCatalogue?.collections?.firstOrNull { it.id == selectedRecipeCollectionId }
                ?.recipeIds?.toSet()?.let { collectionIds -> recipeCatalogue?.recipes?.filter { it.id in collectionIds }?.mapNotNull { it.articleId }?.toSet() }
                .orEmpty()
            val matchingArticles = offlineArticles.filter { article ->
                if (section == "Cooking" && article.id in recipeArticleIds) return@filter false
                if (libraryQuery.isBlank()) article.section == section
                else article.searchableText.contains(libraryQuery.trim(), ignoreCase = true)
            }.filter { article ->
                when (readingFilter) {
                    "favourites" -> "article:${article.id}" in favouriteIds
                    "recent" -> "article:${article.id}" in recentIds
                    else -> true
                }
            }.let { articles -> if (readingFilter == "recent") articles.sortedBy { recentIds.indexOf("article:${it.id}").let { index -> if (index < 0) Int.MAX_VALUE else index } } else articles }
            if (matchingArticles.isEmpty() && offlineLibraryError == null) item {
                Text(if (libraryQuery.isBlank()) "No bundled articles in this section yet." else "No offline content matches that search.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(matchingArticles, key = { "offline:${it.id}" }) { article ->
                OutlinedCard(onClick = { selectedArticleId = article.id }, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(article.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        if (libraryQuery.isNotBlank()) Text(article.section, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(article.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Offline • revision ${article.revision}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (section == "Shopping") "Saved options" else "Saved here", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text("${savedEntries.size}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (savedEntries.isEmpty() && localDataError == null) item { EmptyState(section) }
            items(savedEntries, key = { it.id }) { entry ->
                EntryCard(
                    entry = entry,
                    showCheck = section in setOf("Garden", "Cleaning", "DIY & Home", "Cooking", "Retro & Android", "Pottery", "Bills"),
                    onToggle = { changed -> scope.launch { runCatching { vm.updateEntry(changed) }.onFailure { snackbar.showSnackbar(it.message ?: "Couldn’t update this item.") } } },
                    onEdit = { editingId = entry.id; showEditor = true },
                    onShare = { shareEntryId = entry.id },
                    onOpenSource = { url -> scope.launch { runCatching { uriHandler.openUri(url) }.onFailure { snackbar.showSnackbar("Couldn’t open that link.") } } },
                    onDelete = {
                        scope.launch {
                            runCatching { vm.deleteEntry(entry.id) }
                                .onSuccess {
                                    val result = snackbar.showSnackbar("Removed ${entry.title.ifBlank { "item" }}", actionLabel = "Undo", withDismissAction = true)
                                    if (result == SnackbarResult.ActionPerformed) {
                                        runCatching { vm.restoreEntry(entry) }.onFailure { snackbar.showSnackbar(it.message ?: "Couldn’t restore this item.") }
                                    }
                                }
                                .onFailure { snackbar.showSnackbar(it.message ?: "Couldn’t delete this item.") }
                        }
                    }
                )
            }
            if (savedEntries.isNotEmpty()) item {
                OutlinedButton(onClick = {
                    if (section == "Shopping") onAssistant(ShoppingResearchRules.comparisonPrompt(savedEntries))
                    else {
                        val noteContext = savedEntries.joinToString("\n\n") { "${it.title}\n${it.body}" }
                        onAssistant("${area.prompt}\n\nUse these saved Suric notes as context. Treat them as private user-provided context, and do not infer missing facts:\n$noteContext")
                    }
                }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                    Text(if (section == "Shopping") "Compare saved options with Ask Suric" else "Ask Suric about saved notes", maxLines = 1)
                }
            }
        }
    }
    savedEntries.firstOrNull { it.id == shareEntryId }?.let { entry ->
        AlertDialog(
            onDismissRequest = { shareEntryId = null },
            title = { Text("Share a readable copy?") },
            text = { Text("The selected app and recipient can read this copy. Suric will include only “${entry.title}” and its contents; it will not include your other notes or sign-in details.") },
            confirmButton = {
                TextButton(onClick = {
                    shareEntryId = null
                    runCatching {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, SharedEntryCodec.encode(entry))
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share a copy"))
                    }.onFailure { scope.launch { snackbar.showSnackbar(it.message ?: "Couldn’t open the Android share sheet.") } }
                }) { Text("Choose app") }
            },
            dismissButton = { TextButton(onClick = { shareEntryId = null }) { Text("Cancel") } }
        )
    }
    if (showEditor && (editingId == null || editing != null)) {
        EntryEditorSheet(
            section = section,
            area = area,
            initial = editing,
            onDismiss = { showEditor = false },
            onSave = { targetSection, title, details, sourceUrl, sourceCheckedAt ->
                if (editing == null) vm.addEntry(targetSection, title, details, sourceUrl, sourceCheckedAt)
                else vm.updateEntry(editing!!.copy(
                    section = targetSection,
                    title = title,
                    body = details,
                    sourceUrl = sourceUrl.takeIf { targetSection == "Shopping" },
                    sourceCheckedAt = sourceCheckedAt.takeIf { targetSection == "Shopping" }
                ))
                showEditor = false
            }
        )
    }
}

@Composable
private fun MusicSectionScreen(vm: ConnectionViewModel, onMenu: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allEntries by vm.personalEntries.collectAsState()
    val tracks = allEntries.filter { it.section == "Music" && it.body.startsWith("suric-audio-v1:") }
    val otherEntries = allEntries.filter { it.section == "Music" && !it.body.startsWith("suric-audio-v1:") }
    val musicArea = personalAreas.first { it.name == "Music" }
    var editingEntryId by rememberSaveable { mutableStateOf<String?>(null) }
    val editingEntry = otherEntries.firstOrNull { it.id == editingEntryId }
    val preferences = remember { context.getSharedPreferences("suric_music", android.content.Context.MODE_PRIVATE) }
    var currentId by rememberSaveable { mutableStateOf(preferences.getString("last_track", null)) }
    var isPlaying by remember { mutableStateOf(false) }
    var isPrepared by remember { mutableStateOf(false) }
    var duration by remember { mutableIntStateOf(0) }
    var position by remember { mutableIntStateOf(preferences.getInt("last_position", 0)) }
    var message by remember { mutableStateOf<String?>(null) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    val selectedTrack = tracks.firstOrNull { it.id == currentId }
    val selectedAudio = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        scope.launch {
            uris.forEach { uri ->
                runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                val name = runCatching {
                    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) cursor.getString(0) else null
                    }
                }.getOrNull()?.substringBeforeLast('.')?.takeIf(String::isNotBlank) ?: "Local audio"
                runCatching { vm.addEntry("Music", name, "suric-audio-v1:$uri") }
                    .onFailure { message = it.message ?: "Couldn’t add this audio file." }
            }
        }
    }
    fun playTrack(entry: PersonalEntry) {
        val uri = Uri.parse(entry.body.removePrefix("suric-audio-v1:"))
        if (entry.id == currentId && isPrepared && player != null) {
            player?.start()
            isPlaying = true
            return
        }
        runCatching {
            player?.release()
            isPlaying = false
            isPrepared = false
            val next = MediaPlayer()
            next.setAudioAttributes(android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(android.media.AudioAttributes.USAGE_MEDIA).build())
            next.setDataSource(context, uri)
            next.setOnPreparedListener { ready ->
                player = ready
                duration = ready.duration.coerceAtLeast(0)
                val saved = if (entry.id == preferences.getString("last_track", null)) preferences.getInt("last_position", 0) else 0
                if (saved in 1 until ready.duration) ready.seekTo(saved)
                position = saved.coerceIn(0, ready.duration.coerceAtLeast(0))
                isPrepared = true
                ready.start()
                isPlaying = true
                currentId = entry.id
                preferences.edit().putString("last_track", entry.id).apply()
                message = null
            }
            next.setOnCompletionListener { completed ->
                position = 0
                isPlaying = false
                preferences.edit().putInt("last_position", 0).apply()
                if (tracks.isNotEmpty()) {
                    val nextTrack = tracks.getOrNull((tracks.indexOfFirst { it.id == entry.id } + 1) % tracks.size)
                    if (nextTrack != null && nextTrack.id != entry.id) playTrack(nextTrack)
                }
            }
            next.setOnErrorListener { _, _, _ ->
                isPrepared = false
                isPlaying = false
                message = "This audio file could not be played. Re-select it if access has expired."
                true
            }
            player = next
            currentId = entry.id
            next.prepareAsync()
        }.onFailure { message = it.message ?: "This file could not be opened. Re-select it if its permission has expired." }
    }
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            player?.let { active ->
                position = active.currentPosition.coerceAtLeast(0)
                preferences.edit().putInt("last_position", position).apply()
            }
            delay(750)
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            player?.let { active ->
                runCatching { preferences.edit().putInt("last_position", active.currentPosition.coerceAtLeast(0)).apply() }
                active.release()
            }
        }
    }
    fun openExternal(url: String, title: String) {
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(Intent.createChooser(intent, title))
        }.onFailure { message = "No app could open that destination." }
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Music", maxLines = 1, fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onMenu, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Menu, contentDescription = "Open sections") } }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(18.dp, 12.dp, 18.dp, 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Listen to local audio", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Text("Choose audio files with Android’s document picker. Suric stores their references and plays them on this screen; playback pauses when you leave Music.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = { selectedAudio.launch(arrayOf("audio/*")) }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Choose audio files") }
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Online music", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("These open another app or an online page. Internet access and the external service are required.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        OutlinedButton(onClick = { openExternal("https://www.youtube.com/", "Open with NewPipe or another app") }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Open with NewPipe or another app") }
                        OutlinedButton(onClick = { openExternal("https://www.naxi.rs/radio", "Open Naxi Radio") }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Open Naxi Radio") }
                        TextButton(onClick = { openExternal("https://github.com/TeamNewPipe/NewPipe/releases", "Open NewPipe official releases") }, modifier = Modifier.heightIn(min = 48.dp)) { Text("NewPipe official releases") }
                    }
                }
            }
            message?.let { item { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) } }
            item { Text("Your local queue (${tracks.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
            if (tracks.isEmpty()) item { EmptyState("Music") }
            items(tracks, key = { it.id }) { track ->
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = if (track.id == currentId) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                            Text(track.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Text(if (track.id == currentId && isPlaying) "Playing" else "On this device", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { playTrack(track) }, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.PlayArrow, contentDescription = "Play ${track.title}") }
                        IconButton(onClick = { scope.launch { vm.deleteEntry(track.id) } }, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Close, contentDescription = "Remove ${track.title} from queue") }
                    }
                }
            }
            if (otherEntries.isNotEmpty()) item { Text("Other items saved in Music", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            items(otherEntries, key = { "music-entry:${it.id}" }) { entry ->
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(entry.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(entry.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3)
                        }
                        IconButton(onClick = { editingEntryId = entry.id }, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Edit, contentDescription = "Edit ${entry.title}") }
                    }
                }
            }
            if (selectedTrack != null) item {
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(selectedTrack.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Slider(value = position.toFloat().coerceIn(0f, duration.coerceAtLeast(1).toFloat()), valueRange = 0f..duration.coerceAtLeast(1).toFloat(), onValueChange = { position = it.toInt() }, onValueChangeFinished = { runCatching { player?.seekTo(position) }.onFailure { message = "Couldn’t seek in this audio file." } })
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("${position / 60000}:${(position / 1000 % 60).toString().padStart(2, '0')}", style = MaterialTheme.typography.labelSmall)
                            Row {
                                IconButton(onClick = {
                                    val index = tracks.indexOfFirst { it.id == currentId }
                                    tracks.getOrNull((index - 1).coerceAtLeast(0))?.let(::playTrack)
                                }) { Icon(Icons.Default.SkipPrevious, contentDescription = "Previous track") }
                                IconButton(onClick = {
                                    if (isPlaying) { player?.pause(); isPlaying = false } else if (isPrepared) { player?.start(); isPlaying = true } else selectedTrack.let(::playTrack)
                                }) { Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = if (isPlaying) "Pause" else "Play") }
                                IconButton(onClick = {
                                    val index = tracks.indexOfFirst { it.id == currentId }
                                    tracks.getOrNull((index + 1).mod(tracks.size.coerceAtLeast(1)))?.let(::playTrack)
                                }) { Icon(Icons.Default.SkipNext, contentDescription = "Next track") }
                            }
                            Text("${duration / 60000}:${(duration / 1000 % 60).toString().padStart(2, '0')}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
    if (editingEntry != null) {
        EntryEditorSheet(
            section = "Music",
            area = musicArea,
            initial = editingEntry,
            onDismiss = { editingEntryId = null },
            onSave = { targetSection, title, body, sourceUrl, checkedAt ->
                vm.updateEntry(editingEntry!!.copy(
                    section = targetSection,
                    title = title,
                    body = body,
                    sourceUrl = sourceUrl.takeIf { targetSection == "Shopping" },
                    sourceCheckedAt = checkedAt.takeIf { targetSection == "Shopping" }
                ))
                editingEntryId = null
            }
        )
    }
}

@Composable
private fun OfflineArticleDetail(article: OfflineArticle, isFavourite: Boolean, onFavourite: (Boolean) -> Unit, onBack: () -> Unit, onShare: () -> Unit, onCopy: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(article.section, maxLines = 1) },
                navigationIcon = { IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.ArrowBack, contentDescription = "Back to library") } },
                actions = { IconButton(onClick = { onFavourite(!isFavourite) }, modifier = Modifier.size(48.dp)) { Icon(if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = if (isFavourite) "Remove from favourites" else "Add to favourites", tint = if (isFavourite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) } }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Text(article.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(article.summary, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text("Bundled offline • ${article.section} • revision ${article.revision}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(18.dp))
            article.body.split("\n\n").forEach { paragraph ->
                val heading = paragraph.takeIf { it.length <= 46 && it == it.uppercase() && it.any(Char::isLetter) }
                if (heading != null) {
                    Text(heading, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                } else {
                    Text(paragraph, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.height(12.dp))
            }
            Button(onClick = onCopy, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) {
                Text("Copy to My Space")
            }
            OutlinedButton(onClick = onShare, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Share article link")
            }
            Text("The copy is saved separately from this bundled article and stays on this device.", Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun OfflineRecipeDetail(
    recipe: OfflineRecipe,
    isFavourite: Boolean,
    onFavourite: (Boolean) -> Unit,
    onBack: () -> Unit,
    onAsk: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("${recipe.country} recipe", maxLines = 1) },
                navigationIcon = { IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.ArrowBack, contentDescription = "Back to recipes") } },
                actions = { IconButton(onClick = { onFavourite(!isFavourite) }, modifier = Modifier.size(48.dp)) { Icon(if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = if (isFavourite) "Remove from favourites" else "Add to favourites", tint = if (isFavourite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) } }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Text(recipe.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            recipe.englishName?.takeIf { !it.equals(recipe.name, ignoreCase = true) }?.let {
                Text(it, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Text(listOfNotNull(recipe.country, recipe.region).joinToString(" • "), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(recipe.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text("Prep ${recipe.prepMinutes} min • Cook ${recipe.cookMinutes} min • Serves ${recipe.servings} • ${recipe.difficulty}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))
            Text("Ingredients", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            recipe.ingredients.forEach { ingredient ->
                Text("• $ingredient", Modifier.padding(top = 7.dp), style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(Modifier.height(20.dp))
            Text("Method", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            recipe.instructions.forEachIndexed { index, step ->
                Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                    Text("${index + 1}.", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text(step, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                }
            }
            RecipeDetailNote("Serve with", recipe.sides)
            RecipeDetailNote("Substitutions", recipe.substitutions)
            RecipeDetailNote("Storage", recipe.storage)
            RecipeDetailNote("Food safety", recipe.safety)
            Text("Offline • revision ${recipe.revision} • ${recipe.tags.joinToString(" · ")}", Modifier.padding(top = 16.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(14.dp))
            Button(onClick = onAsk, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) { Text("Ask Suric about this recipe") }
            OutlinedButton(onClick = onCopy, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) { Text("Copy to My Space") }
            OutlinedButton(onClick = onShare, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Share recipe")
            }
        }
    }
}

@Composable
private fun RecipeDetailNote(title: String, body: String) {
    Column(Modifier.fillMaxWidth().padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionHero(area: PersonalArea) {
    Box(Modifier.fillMaxWidth().height(176.dp).clip(RoundedCornerShape(24.dp))) {
        Image(painterResource(area.image), contentDescription = "${area.name} at home", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xD9000000)))))
        Text(area.name, Modifier.align(Alignment.BottomStart).padding(18.dp), style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun EmptyState(section: String) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (section == "Shopping") "No options saved yet" else "A fresh start", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(5.dp))
            Text(if (section == "Shopping") "Add a product or option you are researching." else "Add a note or task whenever you’re ready.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EntryCard(entry: PersonalEntry, showCheck: Boolean, onToggle: (PersonalEntry) -> Unit, onEdit: () -> Unit, onShare: () -> Unit, onOpenSource: (String) -> Unit, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 6.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (showCheck) Checkbox(checked = entry.completed, onCheckedChange = { onToggle(entry.copy(completed = it)) }, modifier = Modifier.semantics { contentDescription = if (entry.completed) "Mark ${entry.title} not done" else "Mark ${entry.title} done" })
            Column(Modifier.weight(1f).padding(vertical = 6.dp, horizontal = if (showCheck) 6.dp else 8.dp)) {
                Text(entry.title.ifBlank { "Untitled note" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, color = if (entry.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                if (entry.body.isNotBlank()) Text(entry.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3)
                entry.sourceCheckedAt?.takeIf { it.isNotBlank() }?.let { Text("Checked by you: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                entry.sourceUrl?.let { url ->
                    TextButton(onClick = { onOpenSource(url) }, contentPadding = PaddingValues(horizontal = 0.dp), modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("Open retailer link", maxLines = 1)
                    }
                }
            }
            IconButton(onClick = onShare, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Share, contentDescription = "Share a copy of ${entry.title}", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            IconButton(onClick = onEdit, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Edit, contentDescription = "Edit ${entry.title}", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.DeleteOutline, contentDescription = "Delete ${entry.title}", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun CalendarEventCard(event: LocalCalendarEvent, adding: Boolean, saved: Boolean, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(enabled = !adding && !saved, onClick = onClick), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(event.startsAtMillis)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(when { adding -> "Adding…"; saved -> "Saved"; else -> "Add" }, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntryEditorSheet(section: String, area: PersonalArea, initial: PersonalEntry?, onDismiss: () -> Unit, onSave: suspend (String, String, String, String?, String?) -> Unit) {
    var targetSection by rememberSaveable(initial?.id) { mutableStateOf(initial?.section ?: section) }
    var sectionMenuExpanded by remember { mutableStateOf(false) }
    var title by rememberSaveable(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var sourceUrl by rememberSaveable(initial?.id) { mutableStateOf(initial?.sourceUrl.orEmpty()) }
    var sourceCheckedAt by rememberSaveable(initial?.id) { mutableStateOf(initial?.sourceCheckedAt.orEmpty()) }
    val initialDetails = if (section == "Bills" && initial != null) {
        val firstLine = initial.body.lineSequence().firstOrNull().orEmpty()
        if (firstLine.startsWith("Amount:") || firstLine.startsWith("Due:")) initial.body.lineSequence().drop(1).joinToString("\n") else initial.body
    } else initial?.body.orEmpty()
    var details by rememberSaveable(initial?.id) { mutableStateOf(initialDetails) }
    val billMeta = initial?.body.orEmpty().lineSequence().firstOrNull().orEmpty()
    var amount by rememberSaveable(initial?.id) { mutableStateOf(billMeta.substringAfter("Amount: ", "").substringBefore(" • Due: ").trim()) }
    var dueDate by rememberSaveable(initial?.id) { mutableStateOf(billMeta.substringAfter("Due: ", "").trim()) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var showDiscardConfirmation by rememberSaveable(initial?.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val dirty = targetSection != (initial?.section ?: section) || title != initial?.title.orEmpty() || details != initialDetails ||
        (section == "Shopping" && (sourceUrl != initial?.sourceUrl.orEmpty() || sourceCheckedAt != initial?.sourceCheckedAt.orEmpty())) ||
        (section == "Bills" && (amount != billMeta.substringAfter("Amount: ", "").substringBefore(" • Due: ").trim() || dueDate != billMeta.substringAfter("Due: ", "").trim()))
    val latestDirty by rememberUpdatedState(dirty)
    fun requestClose() { if (dirty) showDiscardConfirmation = true else onDismiss() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true, confirmValueChange = { target ->
        if (target == SheetValue.Hidden && latestDirty) { showDiscardConfirmation = true; false } else true
    })
    ModalBottomSheet(onDismissRequest = { requestClose() }, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp).padding(bottom = 20.dp)) {
            Text(if (initial == null) (if (section == "Shopping") "Add research option" else "Add to ${area.name}") else if (section == "Shopping") "Edit research option" else "Edit ${area.name}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(if (section == "Shopping") "Save details you checked yourself. Suric does not verify retailer pages." else area.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(18.dp))
            ExposedDropdownMenuBox(expanded = sectionMenuExpanded, onExpandedChange = { sectionMenuExpanded = it }) {
                OutlinedTextField(
                    value = targetSection,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Save in section") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectionMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(16.dp)
                )
                ExposedDropdownMenu(expanded = sectionMenuExpanded, onDismissRequest = { sectionMenuExpanded = false }) {
                    PersonalEntryStore.SECTIONS.sorted().forEach { destination ->
                        DropdownMenuItem(text = { Text(destination) }, onClick = { targetSection = destination; sectionMenuExpanded = false })
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = title, onValueChange = { title = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text(if (section == "Shopping") "Product or option" else if (section == "Bills") "Bill or reminder" else "Title") }, singleLine = true, keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences), shape = RoundedCornerShape(16.dp))
            if (section == "Shopping") {
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = sourceUrl, onValueChange = { sourceUrl = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text("Retailer link (optional)") }, placeholder = { Text("https://...") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, capitalization = KeyboardCapitalization.None), shape = RoundedCornerShape(16.dp))
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = sourceCheckedAt, onValueChange = { sourceCheckedAt = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text("When did you check it? (optional)") }, placeholder = { Text("For example, 17 October") }, singleLine = true, keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences), shape = RoundedCornerShape(16.dp))
            }
            if (section == "Bills") {
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text("Amount (optional)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = RoundedCornerShape(16.dp))
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = dueDate, onValueChange = { dueDate = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text("Due date (optional)") }, placeholder = { Text("For example, 15 November") }, singleLine = true, keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences), shape = RoundedCornerShape(16.dp))
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = details, onValueChange = { details = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text(if (section == "Shopping") "Price, delivery, features, notes" else "Details (optional)") }, minLines = 3, maxLines = 5, keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences), shape = RoundedCornerShape(16.dp))
            error?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = ::requestClose, enabled = !saving, modifier = Modifier.weight(1f).heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) { Text("Cancel") }
                Button(
                    onClick = {
                        if (title.isBlank()) { error = "Add a short title first."; return@Button }
                        if (saving) return@Button
                        val cleanedUrl = if (targetSection == "Shopping") runCatching { ShoppingResearchRules.cleanSourceUrl(sourceUrl) }.getOrElse { error = it.message ?: "Check the retailer link."; return@Button } else null
                        saving = true
                        val savedDetails = if (section == "Bills") buildString {
                            val meta = listOf(amount.trim().takeIf { it.isNotEmpty() }?.let { "Amount: $it" }, dueDate.trim().takeIf { it.isNotEmpty() }?.let { "Due: $it" }).filterNotNull().joinToString(" • ")
                            if (meta.isNotEmpty()) append(meta)
                            if (details.isNotBlank()) { if (isNotEmpty()) append("\n"); append(details.trim()) }
                        } else details.trim()
                        scope.launch {
                            runCatching { onSave(targetSection, title.trim(), savedDetails, cleanedUrl, sourceCheckedAt.trim().takeIf { targetSection == "Shopping" && it.isNotEmpty() }) }
                                .onFailure { error = it.message ?: "Couldn’t save this item. Your existing notes are unchanged." }
                            saving = false
                        }
                    }, enabled = title.isNotBlank() && !saving, modifier = Modifier.weight(1f).heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)
                ) {
                    if (saving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary) else Text(if (initial == null) "Save" else "Save changes", maxLines = 1)
                }
            }
        }
    }
    if (showDiscardConfirmation) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirmation = false },
            title = { Text("Discard your changes?") },
            text = { Text("Your unsaved ${if (section == "Shopping") "research option" else "details"} will be lost.") },
            confirmButton = { TextButton(onClick = { showDiscardConfirmation = false; onDismiss() }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Discard") } },
            dismissButton = { TextButton(onClick = { showDiscardConfirmation = false }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Keep editing") } }
        )
    }
}

@Composable
private fun ConnectionScreen(state: AuthState, vm: ConnectionViewModel) {
    val failed = state as? AuthState.Failed
    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.suric_home), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(310.dp).align(Alignment.TopCenter))
        Box(Modifier.fillMaxWidth().height(310.dp).background(Brush.verticalGradient(listOf(Color(0x33000000), MaterialTheme.colorScheme.background))).align(Alignment.TopCenter))
        Card(Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = 22.dp), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(8.dp)) {
            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Welcome to Suric", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("A calmer place for the things you do every day.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(26.dp))
                Text(when {
                    state == AuthState.SigningIn -> "Waiting for ChatGPT sign-in…"
                    failed != null -> "We couldn’t connect"
                    else -> "Connect your ChatGPT account"
                }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                failed?.let { Spacer(Modifier.height(10.dp)); Text(it.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
                Spacer(Modifier.height(20.dp))
                if (state == AuthState.SigningIn) {
                    CircularProgressIndicator(Modifier.size(26.dp), strokeWidth = 2.dp)
                    TextButton(onClick = vm::cancelSignIn, modifier = Modifier.heightIn(min = 48.dp)) { Text("Cancel sign-in") }
                } else Button(onClick = vm::signIn, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(18.dp)) { Text("Continue with ChatGPT", maxLines = 1) }
                Spacer(Modifier.height(12.dp))
                Text("Your notes stay on this device. ChatGPT only receives what you choose to send.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Text("To revoke Suric’s ChatGPT access, remove it in ChatGPT Settings → Apps.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationScreen(email: String?, vm: ConnectionViewModel, onMenu: () -> Unit) {
    val state by vm.conversation.collectAsState()
    var selectedImage by rememberSaveable { mutableStateOf<Uri?>(null) }
    var previewImage by rememberSaveable { mutableStateOf<String?>(null) }
    BackHandler(enabled = previewImage != null) { previewImage = null }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            vm.keepImageAccess(uri)
            selectedImage = uri
        }
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Ask Suric", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, maxLines = 1); Text(email ?: "Connected", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1) } },
                navigationIcon = { IconButton(onClick = onMenu, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Menu, contentDescription = "Open sections") } },
                actions = { IconButton(onClick = vm::disconnect, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Logout, contentDescription = "Disconnect ChatGPT") } }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp, color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.fillMaxWidth().imePadding().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp)) {
                    selectedImage?.let { uri ->
                        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            PhotoAttachment(uri.toString(), "Selected photo", Modifier.size(72.dp), onClick = { previewImage = uri.toString() })
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Photo ready to send", style = MaterialTheme.typography.labelLarge)
                                Text("Only sent when you tap Send", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { selectedImage = null }, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Close, contentDescription = "Remove photo") }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { picker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.AddPhotoAlternate, "Attach a photo") }
                        OutlinedTextField(value = state.draft, onValueChange = vm::updateDraft, modifier = Modifier.weight(1f), placeholder = { Text("Ask a question", maxLines = 1) }, maxLines = 4, shape = RoundedCornerShape(20.dp))
                        IconButton(onClick = { vm.send(image = selectedImage); selectedImage = null }, enabled = !state.isLoading && (state.draft.isNotBlank() || selectedImage != null), modifier = Modifier.size(52.dp)) { Icon(Icons.AutoMirrored.Filled.Send, "Send") }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), contentPadding = PaddingValues(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (state.messages.isEmpty()) item {
                Column(Modifier.fillMaxWidth().padding(top = 52.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("What’s on your mind?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text("Ask about your home, plans or projects.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            itemsIndexed(state.messages, key = { index, _ -> index }) { _, message ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.role == "user") Arrangement.End else Arrangement.Start) {
                Surface(
                    color = if (message.role == "user") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomEnd = if (message.role == "user") 7.dp else 22.dp, bottomStart = if (message.role == "user") 22.dp else 7.dp),
                    modifier = Modifier.fillMaxWidth(if (message.role == "user") .90f else 1f)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        message.imageUri?.let { uri ->
                            PhotoAttachment(uri, "Photo attached to this message", Modifier.size(168.dp), onClick = { previewImage = uri })
                        }
                        Text(message.text, style = MaterialTheme.typography.bodyLarge, color = if (message.role == "user") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                    }
                }
                }
            }
            if (state.isLoading) item { Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp); Spacer(Modifier.width(10.dp)); Text("Suric is thinking…", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            state.modelLabel?.let { label -> item { Text("Model: $label", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            state.error?.let { item { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(16.dp)) { Text(it, color = MaterialTheme.colorScheme.onErrorContainer); TextButton(onClick = vm::retry, modifier = Modifier.heightIn(min = 48.dp)) { Text("Try again") } } } } }
        }
    }
    previewImage?.let { uri -> PhotoPreviewDialog(uri, onDismiss = { previewImage = null }) }
}

@Composable
private fun PhotoAttachment(uri: String, description: String, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Crop, onClick: () -> Unit) {
    val context = LocalContext.current
    var bitmap by remember(uri) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(uri) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                val imageUri = Uri.parse(uri)
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(imageUri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                val sample = generateSequence(1) { it * 2 }.takeWhile { it <= 64 }
                    .lastOrNull { bounds.outWidth / it >= 720 || bounds.outHeight / it >= 720 } ?: 1
                val options = BitmapFactory.Options().apply { inSampleSize = sample }
                context.contentResolver.openInputStream(imageUri)?.use { BitmapFactory.decodeStream(it, null, options) }
            }.getOrNull()
        }
    }
    Box(modifier.clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        bitmap?.let { Image(it.asImageBitmap(), contentDescription = description, contentScale = contentScale, modifier = Modifier.fillMaxSize()) }
            ?: Text("Photo unavailable", Modifier.padding(6.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PhotoPreviewDialog(uri: String, onDismiss: () -> Unit) {
    var scale by remember(uri) { mutableStateOf(1f) }
    val zoomState = rememberTransformableState { zoomChange, _, _ -> scale = (scale * zoomChange).coerceIn(1f, 5f) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Color.Black).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
            PhotoAttachment(
                uri = uri,
                description = "Photo preview",
                modifier = Modifier.fillMaxWidth().fillMaxHeight(.88f).graphicsLayer(scaleX = scale, scaleY = scale).transformable(zoomState),
                contentScale = ContentScale.Fit,
                onClick = {}
            )
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).size(48.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close photo preview", tint = Color.White)
            }
        }
    }
}
