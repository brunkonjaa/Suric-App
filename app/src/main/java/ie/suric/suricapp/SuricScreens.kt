package ie.suric.suricapp

import android.graphics.Bitmap
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items as rowItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
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
import androidx.core.content.ContextCompat
import androidx.activity.compose.BackHandler
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.SheetValue
import ie.suric.suricapp.auth.AuthState
import ie.suric.suricapp.local.CalendarEvents
import ie.suric.suricapp.local.LocalCalendarEvent
import ie.suric.suricapp.local.PersonalEntry
import ie.suric.suricapp.local.PersonalHistoryEvent
import ie.suric.suricapp.local.PhotoAttachmentStore
import ie.suric.suricapp.local.ExternalLibraryResource
import ie.suric.suricapp.local.PersonalLibraryState
import ie.suric.suricapp.local.PersonalLibraryStore
import ie.suric.suricapp.local.MyDayPreferences
import ie.suric.suricapp.local.MyDayPreferencesStore
import ie.suric.suricapp.local.ShoppingResearchRules
import ie.suric.suricapp.local.SectionVisibility
import ie.suric.suricapp.local.SectionVisibilityStore
import ie.suric.suricapp.local.StarterItem
import ie.suric.suricapp.local.StarterLibrary
import ie.suric.suricapp.local.StarterType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import java.text.DateFormat
import java.time.LocalDate
import java.util.Date

internal data class PersonalArea(
    val name: String,
    val summary: String,
    val prompt: String,
    val image: Int
) {
    val displayName: String get() = when (name) { "Shopping" -> "Shopping Assistant"; "Projects" -> "My Projects"; else -> name }
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
    PersonalArea("Notes", "A private place for useful details", "Use my selected personal notes only as context for the question I ask.", R.drawable.suric_home),
    PersonalArea("Tasks", "Reminders and household routines", "Help me plan this task clearly and realistically.", R.drawable.suric_home),
    PersonalArea("Projects", "Plans, steps and materials", "Help me organise a personal project into practical steps.", R.drawable.suric_diy),
    PersonalArea("My Devices", "My electronics and configuration notes", "Use the saved model and firmware details; do not assume compatibility when details are missing.", R.drawable.suric_retro),
    PersonalArea("Inventory", "Tools, supplies and equipment I own", "Help me organise household inventory and avoid duplicating items.", R.drawable.suric_home)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SuricApp(vm: ConnectionViewModel, darkTheme: Boolean, sharedText: String?, onThemeChange: (Boolean) -> Unit) {
    val auth by vm.auth.collectAsState()
    val savedPersonalEntries by vm.personalEntries.collectAsState()
    val context = LocalContext.current
    val visibilityStore = remember(context) { SectionVisibilityStore(context) }
    val libraryStore = remember(context) { PersonalLibraryStore(context) }
    var visibleSections by remember { mutableStateOf(visibilityStore.visibleSections()) }
    var sectionOrder by remember { mutableStateOf(visibilityStore.homeOrder()) }
    val initialLibraryRead = remember(libraryStore) { runCatching { libraryStore.load() } }
    var libraryState by remember { mutableStateOf(initialLibraryRead.getOrDefault(PersonalLibraryState())) }
    var libraryError by remember { mutableStateOf(initialLibraryRead.exceptionOrNull()?.message) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var route by rememberSaveable { mutableStateOf("home") }
    var selectedSection by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedStarterId by rememberSaveable { mutableStateOf<String?>(null) }
    var importedSharedText by rememberSaveable { mutableStateOf(false) }
    var pendingSharedLink by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(sharedText) {
        if (!sharedText.isNullOrBlank() && !importedSharedText) {
            val sharedUrl = Regex("https://[^\\s<>\\\"']+", RegexOption.IGNORE_CASE)
                .find(sharedText)?.value?.trimEnd('.', ',', ';', ')', ']', '}')
            if (sharedUrl != null && PersonalLibraryStore.cleanHttpsUrl(sharedUrl) != null) {
                pendingSharedLink = sharedUrl
            } else {
                runCatching { vm.importSharedText(sharedText) }.onSuccess {
                    selectedSection = "Notes"
                    route = "home"
                    importedSharedText = true
                }
            }
        }
    }

    BackHandler(enabled = drawerState.currentValue == DrawerValue.Closed && (selectedStarterId != null || selectedSection != null || route != "home")) {
        when {
            selectedStarterId != null -> selectedStarterId = null
            selectedSection != null -> selectedSection = null
            else -> route = "home"
        }
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
                            label = { Text("Search my records", maxLines = 1) },
                            selected = route == "personal_search",
                            onClick = { selectedSection = null; selectedStarterId = null; route = "personal_search"; scope.launch { drawerState.close() } },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
                        )
                    }
                    item {
                        NavigationDrawerItem(
                            label = { Text("My Day", maxLines = 1) },
                            selected = route == "myday",
                            onClick = { selectedSection = null; selectedStarterId = null; route = "myday"; scope.launch { drawerState.close() } },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
                        )
                    }
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
                            label = { Text("My Library", maxLines = 1) },
                            selected = route == "library",
                            onClick = { selectedSection = null; selectedStarterId = null; route = "library"; scope.launch { drawerState.close() } },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
                        )
                    }
                    item {
                        NavigationDrawerItem(
                            label = { Text("Settings", maxLines = 1) },
                            selected = route == "settings",
                            onClick = { selectedSection = null; selectedStarterId = null; route = "settings"; scope.launch { drawerState.close() } },
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
                        TextButton(onClick = { vm.disconnect(); selectedSection = null; selectedStarterId = null; route = "home"; scope.launch { drawerState.close() } }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
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
            route == "assistant" -> ConversationScreen(auth = auth, vm = vm, onMenu = { scope.launch { drawerState.open() } }, onSignIn = vm::signIn)
            route == "myday" -> MyDayScreen(vm = vm, onMenu = { scope.launch { drawerState.open() } }, onOpenSection = { selectedSection = it; route = "home" })
            route == "personal_search" -> PersonalSearchScreen(vm = vm, onMenu = { scope.launch { drawerState.open() } }, onOpenSection = { selectedSection = it; route = "home" })
            route == "library" -> StarterLibraryScreen(
                selectedId = selectedStarterId,
                libraryState = libraryState,
                libraryError = libraryError,
                onSelect = {
                    selectedStarterId = it
                    runCatching { libraryStore.recordViewed(it) }.onSuccess { updated -> libraryState = updated; libraryError = null }
                        .onFailure { error -> libraryError = error.message ?: "Could not update recently viewed items." }
                },
                onBack = { if (selectedStarterId != null) selectedStarterId = null else route = "home" },
                onMenu = { scope.launch { drawerState.open() } },
                onCopy = { item -> vm.addEntry(item.destination, item.title, item.asPersonalText()) },
                onToggleFavourite = { id ->
                    runCatching { libraryStore.toggleFavourite(id) }.onSuccess { updated -> libraryState = updated; libraryError = null }
                        .onFailure { error -> libraryError = error.message ?: "Could not update favourites."; throw error }
                },
                onOpenExternal = { resource ->
                    runCatching { libraryStore.recordExternalViewed(resource.id) }.onSuccess { updated -> libraryState = updated; libraryError = null }
                        .onFailure { error -> libraryError = error.message ?: "Could not update recently viewed items." }
                },
                onCreateProject = { item ->
                    vm.addEntry("Projects", "Project: ${item.title}", "Started from offline guide: ${item.title} (${item.id})\n\n${item.summary}", recordType = "project", category = item.category, tags = listOf("from-guide", item.category))
                },
                onCreateTask = { item ->
                    vm.addEntry("Tasks", "Explore: ${item.title}", "From offline guide: ${item.title} (${item.id})\n\n${item.summary}", recordType = "task", category = item.category, project = "Project: ${item.title}", tags = listOf("from-guide", item.category))
                },
                onAskAbout = { item ->
                    selectedSection = null
                    selectedStarterId = null
                    route = "assistant"
                    vm.updateDraft("Explain this Suric offline guide in practical terms. Use only the guide content below and general knowledge. Do not claim to have checked live information.\n\n${item.title}\n${item.summary}\n\n${item.blocks.joinToString("\n\n") { block -> "${block.heading}\n${block.lines.joinToString("\n")}" }}")
                }
            )
            route == "settings" -> SettingsScreen(
                darkTheme = darkTheme,
                visibleSections = visibleSections,
                sectionOrder = sectionOrder,
                onThemeChange = onThemeChange,
                onVisibilityChange = { updated ->
                    withContext(Dispatchers.IO) { visibilityStore.saveVisibleSections(updated) }
                    visibleSections = SectionVisibility.sanitize(updated)
                },
                onRestoreDefaults = {
                    withContext(Dispatchers.IO) { visibilityStore.restoreDefaults() }
                    visibleSections = SectionVisibility.sectionIds.toSet()
                    withContext(Dispatchers.IO) { visibilityStore.saveHomeOrder(SectionVisibility.sectionIds) }
                    sectionOrder = SectionVisibility.sectionIds
                },
                onOrderChange = { updated ->
                    withContext(Dispatchers.IO) { visibilityStore.saveHomeOrder(updated) }
                    sectionOrder = SectionVisibility.sanitizeOrder(updated)
                },
                onMenu = { scope.launch { drawerState.open() } }
            )
            selectedSection != null -> PersonalSectionScreen(
                section = selectedSection!!,
                vm = vm,
                onMenu = { scope.launch { drawerState.open() } },
                onAssistant = { context -> selectedSection = null; route = "assistant"; vm.send(context) }
            )
            else -> SuricHome(
                connected = auth is AuthState.SignedIn,
                onMenu = { scope.launch { drawerState.open() } },
                onAssistant = { route = "assistant" },
                onSection = { selectedSection = it },
                visibleSections = visibleSections,
                sectionOrder = sectionOrder,
                onMyDay = { route = "myday" },
                onLibrary = { route = "library" },
                onSettings = { route = "settings" }
            )
        }
    }
    pendingSharedLink?.let { url ->
        IncomingShareSheet(
            url = url,
            onDismiss = { pendingSharedLink = null; importedSharedText = true },
            onSave = { destination, note ->
                if (destination == "My Library") {
                    val (_, updated) = libraryStore.addExternalResource(null, url)
                    libraryState = updated
                    val noteBody = "$note\n\n$url".trim()
                    if (note.isNotBlank() && savedPersonalEntries.none { it.section == "Notes" && it.title == "Note about shared link" && it.body == noteBody })
                        vm.addEntry("Notes", "Note about shared link", noteBody, recordType = "resource", tags = listOf("shared-link"))
                    selectedSection = null
                    selectedStarterId = null
                    route = "library"
                } else {
                    val title = "Shared link · ${Uri.parse(url).host.orEmpty()}"
                    val body = buildString { if (note.isNotBlank()) append(note.trim()).append("\n\n"); append(url) }
                    if (savedPersonalEntries.none { it.section == "Notes" && it.title == title && it.body == body })
                        vm.addEntry("Notes", title, body, recordType = "resource", tags = listOf("shared-link"))
                    selectedSection = "Notes"
                    route = "home"
                }
                pendingSharedLink = null
                importedSharedText = true
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IncomingShareSheet(url: String, onDismiss: () -> Unit, onSave: suspend (String, String) -> Unit) {
    var destination by rememberSaveable(url) { mutableStateOf("My Library") }
    var note by rememberSaveable(url) { mutableStateOf("") }
    var saving by rememberSaveable(url) { mutableStateOf(false) }
    var error by rememberSaveable(url) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    ModalBottomSheet(onDismissRequest = { if (!saving) onDismiss() }) {
        Column(Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp).padding(bottom = 22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Save shared link", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text(url, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Choose where it belongs. A personal note is stored in your encrypted Notes area.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { destination = "My Library" }) {
                RadioButton(selected = destination == "My Library", onClick = { destination = "My Library" })
                Text("My Library", modifier = Modifier.padding(start = 4.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { destination = "Notes" }) {
                RadioButton(selected = destination == "Notes", onClick = { destination = "Notes" })
                Text("Notes", modifier = Modifier.padding(start = 4.dp))
            }
            OutlinedTextField(value = note, onValueChange = { note = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text("Personal note (optional)") }, minLines = 2, maxLines = 4, shape = RoundedCornerShape(16.dp))
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDismiss, enabled = !saving, modifier = Modifier.weight(1f).heightIn(min = 52.dp)) { Text("Cancel") }
                Button(onClick = {
                    if (saving) return@Button
                    saving = true
                    scope.launch { runCatching { onSave(destination, note) }.onFailure { error = it.message ?: "Could not save this shared link." }; saving = false }
                }, enabled = !saving, modifier = Modifier.weight(1f).heightIn(min = 52.dp)) {
                    if (saving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Save link")
                }
            }
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

@Composable
private fun MyDayScreen(vm: ConnectionViewModel, onMenu: () -> Unit, onOpenSection: (String) -> Unit) {
    val entries by vm.personalEntries.collectAsState()
    val localDataError by vm.localDataError.collectAsState()
    val context = LocalContext.current
    val myDayStore = remember(context) { MyDayPreferencesStore(context) }
    var dashboard by remember(myDayStore) { mutableStateOf(myDayStore.load()) }
    var customize by rememberSaveable { mutableStateOf(false) }
    var showQuickTask by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val today = LocalDate.now().toString()
    val weekEnd = LocalDate.now().plusDays(7).toString()
    val actionItems = entries.filter { entry ->
        !entry.completed && entry.recordType != "project" && (entry.recordType == "task" || entry.dueDate != null)
    }.filter { it.dueDate == null || it.dueDate <= weekEnd }
        .sortedWith(compareBy<PersonalEntry> { it.dueDate ?: "9999-99-99" }.thenByDescending { it.updatedAtEpochMillis })
    val projects = entries.filter { it.recordType == "project" && !it.completed }.take(5)
    val snackbar = remember { SnackbarHostState() }
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("My Day", maxLines = 1, fontWeight = FontWeight.SemiBold) }, navigationIcon = { IconButton(onClick = onMenu, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Menu, "Open sections") } }, actions = { IconButton(onClick = { customize = true }, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Tune, "Customize My Day") } }) },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = { showQuickTask = true }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("Quick task") }) }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp)) {
                        Text(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM")), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(if (actionItems.isEmpty()) "Nothing needs attention in the next seven days." else "${actionItems.size} item${if (actionItems.size == 1) "" else "s"} need attention.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            if (localDataError != null) item { Text(localDataError.orEmpty(), color = MaterialTheme.colorScheme.error) }
            dashboard.order.forEach { section ->
                if (section == "tasks" && section in dashboard.visibleSections) {
                    item { Text("Tasks and reminders", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
                    if (actionItems.isEmpty()) item { Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Text("Add a task from My Day or give an existing record a due date. The same saved item will appear here and in its section.", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                    items(actionItems, key = { it.id }) { entry ->
                        MyDayEntryCard(entry, today, onComplete = { changed -> scope.launch {
                            val updated = completionUpdate(changed)
                            runCatching { vm.updateEntry(updated) }.onSuccess { if (updated.dueDate != changed.dueDate) snackbar.showSnackbar("Next occurrence: ${updated.dueDate}") }
                                .onFailure { snackbar.showSnackbar(it.message ?: "Could not update this item.") }
                        } }, onOpen = { onOpenSection(entry.section) }, onPostpone = {
                            val nextDate = runCatching { LocalDate.parse(entry.dueDate ?: today).plusDays(1).toString() }.getOrDefault(LocalDate.now().plusDays(1).toString())
                            scope.launch { runCatching { vm.updateEntry(entry.copy(dueDate = nextDate)) }.onFailure { snackbar.showSnackbar(it.message ?: "Could not postpone this item.") } }
                        })
                    }
                } else if (section == "projects" && section in dashboard.visibleSections && projects.isNotEmpty()) {
                    item { Text("Active projects", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp)) }
                    items(projects, key = { "project-${it.id}" }) { entry ->
                        Card(Modifier.fillMaxWidth().clickable { onOpenSection(entry.section) }, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(16.dp)) { Text(entry.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold); if (entry.body.isNotBlank()) Text(entry.body, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                    }
                } else if (section == "bills" && section in dashboard.visibleSections) {
                    item { OutlinedButton(onClick = { onOpenSection("Bills") }, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)) { Text("Bills and payment dates", maxLines = 1) } }
                }
            }
        }
    }
    if (customize) {
        AlertDialog(onDismissRequest = { customize = false }, title = { Text("Customize My Day") }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                dashboard.order.forEachIndexed { index, section ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = section in dashboard.visibleSections, onCheckedChange = { checked ->
                            val next = dashboard.copy(visibleSections = if (checked) dashboard.visibleSections + section else dashboard.visibleSections - section)
                            runCatching { myDayStore.save(next) }.onSuccess { dashboard = next }
                        })
                        Text(section.replaceFirstChar { it.uppercase() }, Modifier.weight(1f))
                        IconButton(onClick = {
                            if (index > 0) {
                                val order = dashboard.order.toMutableList().apply { add(index - 1, removeAt(index)) }
                                val next = dashboard.copy(order = order)
                                runCatching { myDayStore.save(next) }.onSuccess { dashboard = next }
                            }
                        }, enabled = index > 0, modifier = Modifier.size(44.dp)) { Icon(Icons.Default.ArrowUpward, "Move up") }
                        IconButton(onClick = {
                            if (index < dashboard.order.lastIndex) {
                                val order = dashboard.order.toMutableList().apply { add(index + 1, removeAt(index)) }
                                val next = dashboard.copy(order = order)
                                runCatching { myDayStore.save(next) }.onSuccess { dashboard = next }
                            }
                        }, enabled = index < dashboard.order.lastIndex, modifier = Modifier.size(44.dp)) { Icon(Icons.Default.ArrowDownward, "Move down") }
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = { customize = false }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Done") } })
    }
    if (showQuickTask) {
        EntryEditorSheet(section = "Tasks", area = personalAreas.first { it.name == "Tasks" }, initial = null, onDismiss = { showQuickTask = false }, onSave = { title, details, sourceUrl, sourceCheckedAt, recordType, category, dueDate, repeatRule, project, tags, reminderEnabled, photoName ->
            vm.addEntry("Tasks", title, details, sourceUrl, sourceCheckedAt, recordType, category, dueDate, repeatRule, project, tags, reminderEnabled, photoName)
            showQuickTask = false
        })
    }
}

@Composable
private fun MyDayEntryCard(entry: PersonalEntry, today: String, onComplete: (PersonalEntry) -> Unit, onOpen: () -> Unit, onPostpone: () -> Unit) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = entry.completed, onCheckedChange = { onComplete(entry.copy(completed = it)) }, modifier = Modifier.semantics { contentDescription = "Complete ${entry.title}" })
            Column(Modifier.weight(1f).padding(horizontal = 6.dp, vertical = 4.dp)) {
                Text(entry.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2)
                Text(buildString {
                    append(if (entry.recordType == "meal") "Planned meal" else entry.section)
                    entry.dueDate?.let { append(" · "); append(if (it < today) "Overdue · $it" else if (it == today) "Today" else it) }
                    entry.repeatRule?.let { append(" · "); append(it) }
                }, style = MaterialTheme.typography.bodySmall, color = if (entry.dueDate != null && entry.dueDate < today) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                if (entry.body.isNotBlank()) Text(entry.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onOpen, contentPadding = PaddingValues(horizontal = 6.dp), modifier = Modifier.heightIn(min = 44.dp)) { Text("Open section") }
                    if (entry.dueDate != null) TextButton(onClick = onPostpone, contentPadding = PaddingValues(horizontal = 6.dp), modifier = Modifier.heightIn(min = 44.dp)) { Text("Tomorrow") }
                }
            }
        }
    }
}

@Composable
private fun PersonalSearchScreen(vm: ConnectionViewModel, onMenu: () -> Unit, onOpenSection: (String) -> Unit) {
    val entries by vm.personalEntries.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    val normalized = query.trim().lowercase()
    val matches = entries.filter { entry -> normalized.isNotEmpty() && listOfNotNull(entry.title, entry.body, entry.section, entry.category, entry.project, entry.dueDate, entry.tags.joinToString(" ")).any { it.lowercase().contains(normalized) } }
        .sortedByDescending { it.updatedAtEpochMillis }
    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text("Search my records", maxLines = 1, fontWeight = FontWeight.SemiBold) }, navigationIcon = { IconButton(onClick = onMenu, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Menu, "Open sections") } })
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Text("Search stays on this device and includes your notes, projects, tasks, bills, devices, inventory and saved section records.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item { OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Search title, details, category or tags") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true, shape = RoundedCornerShape(16.dp)) }
            if (query.isNotBlank() && matches.isEmpty()) item { Text("No matching personal records.", Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(matches, key = { it.id }) { entry ->
                Card(Modifier.fillMaxWidth().clickable { onOpenSection(entry.section) }, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(entry.title.ifBlank { "Untitled" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        Text("${entry.section} · ${entry.recordType.replaceFirstChar { it.uppercase() }}${entry.dueDate?.let { " · Due $it" }.orEmpty()}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        if (entry.body.isNotBlank()) Text(entry.body, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

private fun nextOccurrence(entry: PersonalEntry): PersonalEntry {
    val completedAt = System.currentTimeMillis()
    if (entry.repeatRule.isNullOrBlank() || entry.dueDate.isNullOrBlank()) return entry.copy(completed = true, lastCompletedAtEpochMillis = completedAt)
    val date = runCatching { LocalDate.parse(entry.dueDate) }.getOrNull() ?: return entry.copy(completed = true, lastCompletedAtEpochMillis = completedAt)
    val next = when (entry.repeatRule.lowercase()) {
        "daily" -> date.plusDays(1)
        "weekly" -> date.plusWeeks(1)
        "monthly" -> date.plusMonths(1)
        "yearly" -> date.plusYears(1)
        else -> return entry.copy(completed = true, lastCompletedAtEpochMillis = completedAt)
    }
    return entry.copy(completed = false, dueDate = next.toString(), lastCompletedAtEpochMillis = completedAt)
}

private fun completionUpdate(entry: PersonalEntry): PersonalEntry =
    if (entry.completed) {
        val completed = nextOccurrence(entry)
        if (entry.recordType == "bill") completed.copy(history = (entry.history + PersonalHistoryEvent(
            System.currentTimeMillis(), "Payment recorded", entry.body.lineSequence().firstOrNull().orEmpty()
        )).takeLast(100)) else completed
    } else entry.copy(lastCompletedAtEpochMillis = null)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuricHome(connected: Boolean, onMenu: () -> Unit, onAssistant: () -> Unit, onSection: (String) -> Unit,
    visibleSections: Set<String>, sectionOrder: List<String>, onMyDay: () -> Unit, onLibrary: () -> Unit, onSettings: () -> Unit) {
    val orderedAreas = sectionOrder.mapNotNull { id -> personalAreas.firstOrNull { it.name == id } }
    val visibleAreas = orderedAreas.filter { it.name in SectionVisibility.sanitize(visibleSections) }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Suric", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onMenu, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Menu, contentDescription = "Open sections") } },
                actions = { ConnectionPill(connected = connected) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            HomeHero(onAssistant)
            FilledTonalButton(onClick = onMyDay, modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp).heightIn(min = 48.dp), shape = RoundedCornerShape(16.dp)) { Text("Open My Day") }
            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onLibrary, modifier = Modifier.weight(1f).heightIn(min = 48.dp), shape = RoundedCornerShape(16.dp)) { Text("My Library", maxLines = 1) }
                TextButton(onClick = onSettings, modifier = Modifier.heightIn(min = 48.dp)) { Text("Settings", maxLines = 1) }
            }
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 10.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("Your spaces", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Text("Choose what you need right now.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("${visibleAreas.size} shown", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (visibleAreas.isEmpty()) {
                Card(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 18.dp, vertical = 6.dp), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxSize().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text("No Home cards are showing", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text("Your sections and saved information are still here. Turn cards back on in Settings.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onSettings, modifier = Modifier.heightIn(min = 48.dp)) { Text("Open Settings") }
                    }
                }
            } else LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                gridItems(visibleAreas, key = { it.name }) { area -> AreaTile(area, onClick = { onSection(area.name) }) }
            }
            Text(if (connected) "ChatGPT connected" else "Local mode · Ask Suric is optional", Modifier.align(Alignment.CenterHorizontally).padding(bottom = 6.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
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
private fun ConnectionPill(connected: Boolean) {
    Row(Modifier.padding(end = 16.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 11.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
        Spacer(Modifier.width(7.dp))
        Text(if (connected) "Connected" else "AI optional", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, maxLines = 1)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    darkTheme: Boolean,
    visibleSections: Set<String>,
    sectionOrder: List<String>,
    onThemeChange: (Boolean) -> Unit,
    onVisibilityChange: suspend (Set<String>) -> Unit,
    onOrderChange: suspend (List<String>) -> Unit,
    onRestoreDefaults: suspend () -> Unit,
    onMenu: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val operationMutex = remember { Mutex() }
    var saving by remember { mutableStateOf(false) }
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Settings", maxLines = 1, fontWeight = FontWeight.SemiBold) }, navigationIcon = { IconButton(onClick = onMenu, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Menu, "Open sections") } }) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Text("Appearance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
            item {
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) { Text("Dark appearance", style = MaterialTheme.typography.bodyLarge); Text("Saved on this device", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        Switch(checked = darkTheme, onCheckedChange = onThemeChange, modifier = Modifier.semantics { contentDescription = "Toggle dark appearance" })
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) { Text("Home sections", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold); Text("Choose which cards appear and arrange their order. Hidden sections and records stay available in the drawer.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        TextButton(onClick = {
                        if (!operationMutex.tryLock()) return@TextButton
                        saving = true
                        scope.launch { try { runCatching { onRestoreDefaults() }.onFailure { snackbar.showSnackbar(it.message ?: "Could not restore defaults.") } } finally { saving = false; operationMutex.unlock() } }
                    }, enabled = !saving, modifier = Modifier.heightIn(min = 48.dp)) { Text("Restore defaults", maxLines = 1) }
                }
            }
            items(sectionOrder.mapNotNull { id -> personalAreas.firstOrNull { it.name == id } }, key = { it.name }) { area ->
                val checked = area.name in visibleSections
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    val position = sectionOrder.indexOf(area.name)
                    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f).padding(vertical = 8.dp)) { Text(area.displayName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium); Text("Home card visibility", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        IconButton(onClick = {
                            if (saving || position <= 0 || !operationMutex.tryLock()) return@IconButton
                            val updated = sectionOrder.toMutableList().apply { add(position - 1, removeAt(position)) }
                            saving = true
                            scope.launch { try { runCatching { onOrderChange(updated) }.onFailure { snackbar.showSnackbar(it.message ?: "Could not save this order.") } } finally { saving = false; operationMutex.unlock() } }
                        }, enabled = !saving && position > 0, modifier = Modifier.size(48.dp).semantics { contentDescription = "Move ${area.displayName} up" }) { Icon(Icons.Default.ArrowUpward, contentDescription = null) }
                        IconButton(onClick = {
                            if (saving || position < 0 || position >= sectionOrder.lastIndex || !operationMutex.tryLock()) return@IconButton
                            val updated = sectionOrder.toMutableList().apply { add(position + 1, removeAt(position)) }
                            saving = true
                            scope.launch { try { runCatching { onOrderChange(updated) }.onFailure { snackbar.showSnackbar(it.message ?: "Could not save this order.") } } finally { saving = false; operationMutex.unlock() } }
                        }, enabled = !saving && position in 0 until sectionOrder.lastIndex, modifier = Modifier.size(48.dp).semantics { contentDescription = "Move ${area.displayName} down" }) { Icon(Icons.Default.ArrowDownward, contentDescription = null) }
                        Switch(checked = checked, enabled = !saving, onCheckedChange = { show ->
                            if (!operationMutex.tryLock()) return@Switch
                            val updated = if (show) visibleSections + area.name else visibleSections - area.name
                            saving = true
                            scope.launch { try { runCatching { onVisibilityChange(updated) }.onFailure { snackbar.showSnackbar(it.message ?: "Could not save this setting.") } } finally { saving = false; operationMutex.unlock() } }
                        }, modifier = Modifier.semantics { contentDescription = "Show ${area.displayName} Home card" })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StarterLibraryScreen(
    selectedId: String?,
    libraryState: PersonalLibraryState,
    libraryError: String?,
    onSelect: (String) -> Unit,
    onBack: () -> Unit,
    onMenu: () -> Unit,
    onCopy: suspend (StarterItem) -> Unit,
    onToggleFavourite: (String) -> Unit,
    onOpenExternal: (ExternalLibraryResource) -> Unit,
    onCreateProject: suspend (StarterItem) -> Unit,
    onCreateTask: suspend (StarterItem) -> Unit,
    onAskAbout: (StarterItem) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val copyMutex = remember { Mutex() }
    val guideActionMutex = remember { Mutex() }
    val uriHandler = LocalUriHandler.current
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    var collection by rememberSaveable { mutableStateOf("All") }
    var copying by rememberSaveable { mutableStateOf(false) }
    val selected = selectedId?.let { StarterLibrary.byId(it) }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (selected == null) "My Library" else selected.title, maxLines = if (selected == null) 1 else 2, fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = if (selected == null) onMenu else onBack, modifier = Modifier.size(48.dp)) { if (selected == null) Icon(Icons.Default.Menu, "Open sections") else Icon(Icons.Default.Close, "Back to library") } },
                actions = {
                    selected?.let { article ->
                        IconButton(onClick = { runCatching { onToggleFavourite(article.id) }.onFailure { error -> scope.launch { snackbar.showSnackbar(error.message ?: "Could not save this item.") } } }, modifier = Modifier.size(48.dp).semantics { contentDescription = if (article.id in libraryState.favourites) "Remove ${article.title} from favourites" else "Save ${article.title} to favourites" }) {
                            Icon(if (article.id in libraryState.favourites) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, tint = if (article.id in libraryState.favourites) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        if (selected != null) {
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                libraryError?.let { message -> item { LibraryEmptyState("My Library could not be updated", message) } }
                selected.imageKey?.let { key -> item { StarterBanner(key, selected.category) } }
                item {
                    Text(selected.category.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text(selected.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Text(selected.summary, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${selected.type.name.lowercase().replaceFirstChar { it.uppercase() }} · revision ${selected.revision}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                items(selected.blocks, key = { selected.id + it.heading }) { block ->
                    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(block.heading, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(6.dp))
                            block.lines.forEachIndexed { index, line ->
                                val marker = when {
                                    selected.type == StarterType.RECIPE && block.heading == "Method" -> "${index + 1}.  "
                                    selected.type == StarterType.CHECKLIST -> "☐  "
                                    else -> "•  "
                                }
                                Text(marker + line, Modifier.padding(vertical = 3.dp), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
                val relatedItems = selected.relatedIds.mapNotNull(StarterLibrary::byId)
                if (relatedItems.isNotEmpty()) {
                    item { Text("Related guides", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                    items(relatedItems, key = { "related-${selected.id}-${it.id}" }) { related ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onSelect(related.id) },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(related.category, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                Text(related.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text(related.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3)
                            }
                        }
                    }
                }
                if (selected.sources.isNotEmpty() || selected.reviewed != null) item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        selected.reviewed?.let { Text("Content reviewed: $it", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        selected.sources.forEach { (label, url) ->
                            TextButton(onClick = {
                                val clean = PersonalLibraryStore.cleanHttpsUrl(url)
                                if (clean == null) scope.launch { snackbar.showSnackbar("This source link is invalid.") }
                                else runCatching { uriHandler.openUri(clean) }.onFailure { scope.launch { snackbar.showSnackbar("Could not open this source link.") } }
                            }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
                                Text("Open source: $label", modifier = Modifier.weight(1f), maxLines = 2)
                                Icon(Icons.Default.OpenInNew, contentDescription = null)
                            }
                        }
                    }
                }
                item {
                    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Use this guide", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Create a linked project or task in your personal spaces, or ask Suric about the guide when you choose.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = {
                                    if (!guideActionMutex.tryLock()) return@OutlinedButton
                                    scope.launch { try { runCatching { onCreateProject(selected) }.onSuccess { snackbar.showSnackbar("Project created in My Projects.") }.onFailure { snackbar.showSnackbar(it.message ?: "Could not create the project.") } } finally { guideActionMutex.unlock() } }
                                }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("Start project", maxLines = 1) }
                                OutlinedButton(onClick = {
                                    if (!guideActionMutex.tryLock()) return@OutlinedButton
                                    scope.launch { try { runCatching { onCreateTask(selected) }.onSuccess { snackbar.showSnackbar("Task added to My Day.") }.onFailure { snackbar.showSnackbar(it.message ?: "Could not create the task.") } } finally { guideActionMutex.unlock() } }
                                }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("Add task", maxLines = 1) }
                            }
                            TextButton(onClick = { onAskAbout(selected) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Ask Suric about this guide") }
                        }
                    }
                }
                item {
                    Button(onClick = {
                        if (!copyMutex.tryLock()) return@Button
                        copying = true
                        scope.launch {
                            try {
                                runCatching { onCopy(selected) }
                                    .onSuccess { snackbar.showSnackbar("Copied to ${selected.destination}. You can edit your copy there.") }
                                    .onFailure { snackbar.showSnackbar(it.message ?: "Could not save the personal copy.") }
                            } finally { copying = false; copyMutex.unlock() }
                        }
                    }, enabled = !copying, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) {
                        if (copying) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary) else Text("Copy to my space")
                    }
                }
            }
        } else {
            val matchingItems = StarterLibrary.search(query, category)
            val results = if (collection == "Saved") matchingItems.filter { it.id in libraryState.favourites }
                .sortedByDescending { libraryState.favouriteSavedAt[it.id] ?: 0L } else matchingItems
            val matchingExternal = libraryState.externalResources.filter { resource ->
                (collection != "Saved" || resource.id in libraryState.favourites) &&
                    (query.isBlank() || resource.title.contains(query.trim(), true) || resource.url.contains(query.trim(), true))
            }.sortedByDescending { if (collection == "Saved") libraryState.favouriteSavedAt[it.id] ?: 0L else it.addedAtEpochMillis }
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                libraryError?.let { message -> item { LibraryEmptyState("Saved library data needs attention", message) } }
                item { Text("Offline guides and recipes, plus web links you choose to save.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                item {
                    OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(16.dp), placeholder = { Text("Search the library") }, leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }, trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Close, "Clear search") } })
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 2.dp)) {
                        listOf("All", "Saved · newest", "Recently viewed").forEach { option ->
                            val target = if (option.startsWith("Saved")) "Saved" else option
                            item { FilterChip(selected = collection == target, onClick = { collection = target }, label = { Text(option, maxLines = 1) }) }
                        }
                    }
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 2.dp)) {
                        item { FilterChip(selected = category == null, onClick = { category = null }, label = { Text("All") }) }
                        rowItems(StarterLibrary.categories, key = { it }) { option -> FilterChip(selected = category == option, onClick = { category = if (category == option) null else option }, label = { Text(option, maxLines = 1) }) }
                    }
                }
                if (collection == "Recently viewed") {
                    val recentItems = libraryState.recentlyViewedIds.mapNotNull(StarterLibrary::byId)
                    val recentExternal = libraryState.recentlyViewedIds.mapNotNull { id -> libraryState.externalResources.firstOrNull { it.id == id } }
                    item { Text("Recently viewed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                    items(recentItems, key = { "recent-${it.id}" }) { article ->
                        LibraryArticleCard(article, article.id in libraryState.favourites, onClick = { onSelect(article.id) }, onFavourite = { runCatching { onToggleFavourite(article.id) }.onFailure { scope.launch { snackbar.showSnackbar(it.message ?: "Could not update favourites.") } } })
                    }
                    items(recentExternal, key = { "recent-${it.id}" }) { resource ->
                        ExternalResourceCard(resource, resource.id in libraryState.favourites, onClick = {
                            onOpenExternal(resource)
                            runCatching { uriHandler.openUri(resource.url) }.onFailure { scope.launch { snackbar.showSnackbar("Could not open this link.") } }
                        }, onFavourite = { runCatching { onToggleFavourite(resource.id) }.onFailure { scope.launch { snackbar.showSnackbar(it.message ?: "Could not update favourites.") } } })
                    }
                    if (recentItems.isEmpty() && recentExternal.isEmpty()) item { LibraryEmptyState("Nothing viewed yet", "Open a guide or saved link and it will appear here.") }
                } else {
                    item { Text("${results.size + matchingExternal.size} items", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    if (results.isEmpty() && matchingExternal.isEmpty()) item {
                    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("No matching items", style = MaterialTheme.typography.titleMedium); Text("Try another word or category.", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                    }
                    items(results, key = { it.id }) { article ->
                        LibraryArticleCard(article, article.id in libraryState.favourites, onClick = { onSelect(article.id) }, onFavourite = { runCatching { onToggleFavourite(article.id) }.onFailure { scope.launch { snackbar.showSnackbar(it.message ?: "Could not update favourites.") } } })
                    }
                    items(matchingExternal, key = { it.id }) { resource ->
                        ExternalResourceCard(resource, resource.id in libraryState.favourites, onClick = {
                            onOpenExternal(resource)
                            runCatching { uriHandler.openUri(resource.url) }.onFailure { scope.launch { snackbar.showSnackbar("Could not open this link.") } }
                        }, onFavourite = { runCatching { onToggleFavourite(resource.id) }.onFailure { scope.launch { snackbar.showSnackbar(it.message ?: "Could not update favourites.") } } })
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryArticleCard(item: StarterItem, favourite: Boolean, onClick: () -> Unit, onFavourite: () -> Unit) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 96.dp).padding(start = 14.dp, end = 4.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f).clickable(onClick = onClick).padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                item.imageKey?.let { key -> Image(painterResource(starterImage(key)), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(64.dp).clip(RoundedCornerShape(14.dp))) }
                Column(Modifier.weight(1f).padding(start = if (item.imageKey == null) 0.dp else 12.dp)) {
                    Text(item.category, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(item.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3)
                    Text(item.type.name.lowercase().replaceFirstChar { it.uppercase() } + " · offline", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onFavourite, modifier = Modifier.size(48.dp).semantics { contentDescription = if (favourite) "Remove ${item.title} from favourites" else "Save ${item.title} to favourites" }) {
                Icon(if (favourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, tint = if (favourite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ExternalResourceCard(resource: ExternalLibraryResource, favourite: Boolean, onClick: () -> Unit, onFavourite: () -> Unit) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 76.dp).padding(start = 14.dp, end = 4.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).clickable(onClick = onClick).padding(vertical = 5.dp)) {
                Text("Saved web resource · internet required", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(resource.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(resource.url, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            }
            IconButton(onClick = onClick, modifier = Modifier.size(48.dp).semantics { contentDescription = "Open ${resource.title}" }) { Icon(Icons.Default.OpenInNew, contentDescription = null) }
            IconButton(onClick = onFavourite, modifier = Modifier.size(48.dp).semantics { contentDescription = if (favourite) "Remove ${resource.title} from favourites" else "Save ${resource.title} to favourites" }) {
                Icon(if (favourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, tint = if (favourite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LibraryEmptyState(title: String, message: String) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StarterBanner(imageKey: String, title: String) {
    Box(Modifier.fillMaxWidth().height(128.dp).clip(RoundedCornerShape(22.dp))) {
        Image(painterResource(starterImage(imageKey)), contentDescription = "$title reference image", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0x99000000), Color.Transparent))))
        Text(title, Modifier.align(Alignment.BottomStart).padding(16.dp), style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

private fun starterImage(key: String): Int = when (key) {
    "suric_garden" -> R.drawable.suric_garden
    "suric_pottery" -> R.drawable.suric_pottery
    "suric_shopping" -> R.drawable.suric_shopping
    "suric_bills" -> R.drawable.suric_bills
    "suric_diy" -> R.drawable.suric_diy
    "suric_cleaning" -> R.drawable.suric_cleaning
    "suric_cooking" -> R.drawable.suric_cooking
    "suric_retro" -> R.drawable.suric_retro
    else -> R.drawable.suric_home
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalSectionScreen(section: String, vm: ConnectionViewModel, onMenu: () -> Unit, onAssistant: (String) -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val area = personalAreas.first { it.name == section }
    val allSavedEntries by vm.personalEntries.collectAsState()
    val savedEntries = allSavedEntries.filter { it.section == section }.let { records ->
        if (section == "Cooking") records.sortedWith(compareBy<PersonalEntry> { if (it.recordType == "meal") 0 else 1 }.thenBy { it.dueDate ?: "9999-99-99" }.thenByDescending { it.updatedAtEpochMillis }) else records
    }
    val localDataError by vm.localDataError.collectAsState()
    var editingId by rememberSaveable(section) { mutableStateOf<String?>(null) }
    val editing = savedEntries.firstOrNull { it.id == editingId }
    var showEditor by rememberSaveable(section) { mutableStateOf(false) }
    var calendarEvents by remember { mutableStateOf<List<LocalCalendarEvent>>(emptyList()) }
    var calendarMessage by remember { mutableStateOf<String?>(null) }
    var calendarLoading by remember { mutableStateOf(false) }
    var addingCalendarEvents by remember { mutableStateOf(emptySet<String>()) }
    var savedCalendarEvents by remember { mutableStateOf(emptySet<String>()) }
    val snackbar = remember { SnackbarHostState() }

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
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Plan a meal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Add a Cooking record, choose type Meal, and set its planned date. It will appear in My Day and can be shared through Android’s app chooser.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            item {
                PhoneIntegrationCard(
                    section = section,
                    savedEntries = savedEntries,
                    onResult = { message -> scope.launch { snackbar.showSnackbar(message) } }
                )
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
                val unpaid = savedEntries.filterNot { it.completed }
                val paid = savedEntries.count { it.completed || it.lastCompletedAtEpochMillis != null }
                val overdue = unpaid.count { it.dueDate?.let { date -> date < LocalDate.now().toString() } == true }
                item {
                    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            Text("Payment overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("${unpaid.size} unpaid · $paid marked paid${if (overdue > 0) " · $overdue overdue" else ""}", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Amounts and payment status are entered by you; Suric has no bank connection.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
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
                            val eventDate = java.time.Instant.ofEpochMilli(event.startsAtMillis).atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString()
                            runCatching { vm.addEntry("Bills", event.title, "Calendar date: $date\n${event.notes.orEmpty()}".trim(), recordType = "task", dueDate = eventDate) }
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
                Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (section == "Shopping") "Saved options" else "Saved here", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text("${savedEntries.size}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (savedEntries.isEmpty() && localDataError == null) item { EmptyState(section) }
            items(savedEntries, key = { it.id }) { entry ->
                EntryCard(
                    entry = entry,
                    showCheck = section in setOf("Garden", "Cleaning", "DIY & Home", "Cooking", "Retro & Android", "Pottery", "Bills", "Tasks", "Projects"),
                    onToggle = { changed -> scope.launch {
                        val updated = completionUpdate(changed)
                        runCatching { vm.updateEntry(updated) }.onSuccess { if (updated.dueDate != changed.dueDate) snackbar.showSnackbar("Next occurrence: ${updated.dueDate}") }
                            .onFailure { snackbar.showSnackbar(it.message ?: "Couldn’t update this item.") }
                    } },
                    onEdit = { editingId = entry.id; showEditor = true },
                    onOpenSource = { url -> scope.launch { runCatching { uriHandler.openUri(url) }.onFailure { snackbar.showSnackbar("Couldn’t open that link.") } } },
                    onDelete = {
                        scope.launch {
                            runCatching { vm.deleteEntry(entry.id) }
                                .onSuccess {
                                    val result = snackbar.showSnackbar("Removed ${entry.title.ifBlank { "item" }}", actionLabel = "Undo", withDismissAction = true)
                                    if (result == SnackbarResult.ActionPerformed) {
                                        runCatching { vm.restoreEntry(entry) }.onFailure { snackbar.showSnackbar(it.message ?: "Couldn’t restore this item.") }
                                    } else withContext(Dispatchers.IO) { PhotoAttachmentStore(context).delete(entry.photoName) }
                                }
                                .onFailure { snackbar.showSnackbar(it.message ?: "Couldn’t delete this item.") }
                        }
                    }
                )
                if (section == "Cooking" && entry.recordType in setOf("recipe", "meal")) {
                    OutlinedButton(onClick = {
                        val share = Intent(Intent.ACTION_SEND)
                            .setType("text/plain")
                            .putExtra(Intent.EXTRA_SUBJECT, entry.title)
                            .putExtra(Intent.EXTRA_TEXT, "${entry.title}\n\n${entry.body}".trim())
                        launchExternal(context, Intent.createChooser(share, "Share recipe or ingredients"), { message -> scope.launch { snackbar.showSnackbar(message) } }, "No compatible app could receive this recipe.")
                    }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                        Text("Share recipe or ingredients", maxLines = 1)
                    }
                }
                if (section == "Garden" && entry.recordType == "plant") {
                    OutlinedButton(onClick = {
                        scope.launch {
                            val event = PersonalHistoryEvent(System.currentTimeMillis(), "Watered")
                            runCatching { vm.updateEntry(entry.copy(history = (entry.history + event).takeLast(100))) }
                                .onSuccess { snackbar.showSnackbar("Watering recorded for ${entry.title}.") }
                                .onFailure { snackbar.showSnackbar(it.message ?: "Could not record watering.") }
                        }
                    }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                        Text("Record watering · ${entry.title}", maxLines = 1)
                    }
                }
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
    if (showEditor && (editingId == null || editing != null)) {
        EntryEditorSheet(
            section = section,
            area = area,
            initial = editing,
            onDismiss = { showEditor = false },
            onSave = { title, details, sourceUrl, sourceCheckedAt, recordType, category, dueDate, repeatRule, project, tags, reminderEnabled, photoName ->
                if (editing == null) vm.addEntry(section, title, details, sourceUrl, sourceCheckedAt, recordType, category, dueDate, repeatRule, project, tags, reminderEnabled, photoName)
                else vm.updateEntry(editing!!.copy(title = title, body = details, sourceUrl = sourceUrl, sourceCheckedAt = sourceCheckedAt, recordType = recordType, category = category, dueDate = dueDate, repeatRule = repeatRule, project = project, tags = tags, reminderEnabled = reminderEnabled, photoName = photoName))
                showEditor = false
            }
        )
    }
}

@Composable
private fun PhoneIntegrationCard(section: String, savedEntries: List<PersonalEntry>, onResult: (String) -> Unit) {
    val context = LocalContext.current
    val query = when (section) {
        "Garden" -> "garden centre near me"
        "DIY & Home" -> "hardware store near me"
        "Retro & Android" -> "electronics repair near me"
        else -> null
    }
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Use phone apps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("These actions open an Android app only after you tap them.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    val intent = Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
                        .putExtra(CalendarContract.Events.TITLE, "Suric · $section")
                        .putExtra(CalendarContract.Events.DESCRIPTION, "Reminder from Suric. Add details and a date in your calendar app.")
                    launchExternal(context, intent, onResult, "No calendar app could handle this action.")
                }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("Calendar", maxLines = 1) }
                if (query != null) OutlinedButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(query)}"))
                    launchExternal(context, intent, onResult, "No maps app could handle this search.")
                }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("Find nearby", maxLines = 1) }
                OutlinedButton(onClick = {
                    val details = savedEntries.take(20).joinToString("\n\n") { entry -> "${entry.title}\n${entry.body}" }
                    val share = Intent(Intent.ACTION_SEND).setType("text/plain")
                        .putExtra(Intent.EXTRA_SUBJECT, "Suric · $section")
                        .putExtra(Intent.EXTRA_TEXT, "Suric · $section\n\n$details".trim())
                    launchExternal(context, Intent.createChooser(share, "Share selected $section information"), onResult, "No app could share this information.")
                }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("Share", maxLines = 1) }
            }
        }
    }
}

private fun launchExternal(context: android.content.Context, intent: Intent, onResult: (String) -> Unit, failure: String) {
    runCatching { context.startActivity(intent) }.onFailure { onResult(failure) }
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
private fun EntryCard(entry: PersonalEntry, showCheck: Boolean, onToggle: (PersonalEntry) -> Unit, onEdit: () -> Unit, onOpenSource: (String) -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    val attachmentStore = remember(context) { PhotoAttachmentStore(context) }
    val thumbnail by produceState<Bitmap?>(null, entry.photoName) {
        value = entry.photoName?.let { name -> withContext(Dispatchers.IO) { attachmentStore.thumbnail(name) } }
    }
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 6.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (showCheck) Checkbox(checked = entry.completed, onCheckedChange = { onToggle(entry.copy(completed = it)) }, modifier = Modifier.semantics { contentDescription = if (entry.completed) "Mark ${entry.title} not done" else "Mark ${entry.title} done" })
            thumbnail?.let { bitmap ->
                Image(bitmap.asImageBitmap(), contentDescription = "Photo attached to ${entry.title}", contentScale = ContentScale.Crop, modifier = Modifier.size(68.dp).clip(RoundedCornerShape(12.dp)))
                Spacer(Modifier.width(8.dp))
            }
            Column(Modifier.weight(1f).padding(vertical = 6.dp, horizontal = if (showCheck) 6.dp else 8.dp)) {
                Text(entry.title.ifBlank { "Untitled note" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, color = if (entry.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                if (entry.body.isNotBlank()) Text(entry.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3)
                val meta = listOfNotNull(entry.category, entry.dueDate?.let { "Due $it" }, entry.repeatRule, entry.project?.let { "Project: $it" }, entry.tags.takeIf { it.isNotEmpty() }?.joinToString(prefix = "#", separator = " #")).joinToString(" · ")
                if (meta.isNotBlank()) Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                entry.lastCompletedAtEpochMillis?.let { completedAt -> Text(if (entry.recordType == "bill") "Last paid: ${DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(completedAt))}" else "Last completed: ${DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(completedAt))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                entry.history.takeLast(3).asReversed().forEach { event ->
                    val detail = event.details.takeIf(String::isNotBlank)?.let { " · $it" }.orEmpty()
                    Text("${event.action}: ${DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(event.atEpochMillis))}$detail", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                entry.sourceCheckedAt?.takeIf { it.isNotBlank() }?.let { Text("Checked by you: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                entry.sourceUrl?.let { url ->
                    TextButton(onClick = { onOpenSource(url) }, contentPadding = PaddingValues(horizontal = 0.dp), modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("Open retailer link", maxLines = 1)
                    }
                }
            }
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
private fun EntryEditorSheet(section: String, area: PersonalArea, initial: PersonalEntry?, onDismiss: () -> Unit, onSave: suspend (String, String, String?, String?, String, String?, String?, String?, String?, List<String>, Boolean, String?) -> Unit) {
    val context = LocalContext.current
    val attachmentStore = remember(context) { PhotoAttachmentStore(context) }
    val scope = rememberCoroutineScope()
    var title by rememberSaveable(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var sourceUrl by rememberSaveable(initial?.id) { mutableStateOf(initial?.sourceUrl.orEmpty()) }
    var sourceCheckedAt by rememberSaveable(initial?.id) { mutableStateOf(initial?.sourceCheckedAt.orEmpty()) }
    var recordType by rememberSaveable(initial?.id) { mutableStateOf(initial?.recordType ?: when (section) { "Tasks", "Cleaning" -> "task"; "Projects" -> "project"; "My Devices" -> "device"; "Inventory" -> "inventory"; "Bills" -> "bill"; "Cooking" -> "recipe"; "Garden" -> "plant"; else -> "note" }) }
    var category by rememberSaveable(initial?.id) { mutableStateOf(initial?.category.orEmpty()) }
    var dueDate by rememberSaveable(initial?.id) { mutableStateOf(initial?.dueDate.orEmpty()) }
    var reminderEnabled by rememberSaveable(initial?.id) { mutableStateOf(initial?.reminderEnabled ?: false) }
    var photoName by rememberSaveable(initial?.id) { mutableStateOf(initial?.photoName) }
    var repeatRule by rememberSaveable(initial?.id) { mutableStateOf(initial?.repeatRule ?: "None") }
    var project by rememberSaveable(initial?.id) { mutableStateOf(initial?.project.orEmpty()) }
    var tags by rememberSaveable(initial?.id) { mutableStateOf(initial?.tags?.joinToString(", ").orEmpty()) }
    var typeExpanded by remember { mutableStateOf(false) }
    var repeatExpanded by remember { mutableStateOf(false) }
    val initialDetails = if (section == "Bills" && initial != null) {
        val firstLine = initial.body.lineSequence().firstOrNull().orEmpty()
        if (firstLine.startsWith("Amount:") || firstLine.startsWith("Due:")) initial.body.lineSequence().drop(1).joinToString("\n") else initial.body
    } else initial?.body.orEmpty()
    var details by rememberSaveable(initial?.id) { mutableStateOf(initialDetails) }
    val billMeta = initial?.body.orEmpty().lineSequence().firstOrNull().orEmpty()
    var amount by rememberSaveable(initial?.id) { mutableStateOf(billMeta.substringAfter("Amount: ", "").substringBefore(" • Due: ").trim()) }
    var billDueDate by rememberSaveable(initial?.id) { mutableStateOf(billMeta.substringAfter("Due: ", "").trim()) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var showDiscardConfirmation by rememberSaveable(initial?.id) { mutableStateOf(false) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            runCatching { withContext(Dispatchers.IO) { attachmentStore.copyFromPicker(uri) } }
                .onSuccess { copied ->
                    if (photoName != initial?.photoName) withContext(Dispatchers.IO) { attachmentStore.delete(photoName) }
                    photoName = copied
                    error = null
                }
                .onFailure { error = it.message ?: "Could not save that photo." }
        }
    }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        reminderEnabled = granted
        if (!granted) error = "Notification permission was declined. You can save the date without an alert."
    }
    val dirty = title != initial?.title.orEmpty() || details != initialDetails ||
        recordType != (initial?.recordType ?: when (section) { "Tasks", "Cleaning" -> "task"; "Projects" -> "project"; "My Devices" -> "device"; "Inventory" -> "inventory"; "Bills" -> "bill"; "Cooking" -> "recipe"; "Garden" -> "plant"; else -> "note" }) ||
        category != initial?.category.orEmpty() || dueDate != initial?.dueDate.orEmpty() || reminderEnabled != (initial?.reminderEnabled ?: false) || photoName != initial?.photoName || repeatRule != (initial?.repeatRule ?: "None") || project != initial?.project.orEmpty() || tags != initial?.tags?.joinToString(", ").orEmpty() ||
        (section == "Shopping" && (sourceUrl != initial?.sourceUrl.orEmpty() || sourceCheckedAt != initial?.sourceCheckedAt.orEmpty())) ||
        (section == "Bills" && (amount != billMeta.substringAfter("Amount: ", "").substringBefore(" • Due: ").trim() || billDueDate != billMeta.substringAfter("Due: ", "").trim()))
    val latestDirty by rememberUpdatedState(dirty)
    fun discardAndClose() {
        if (photoName != initial?.photoName) scope.launch { withContext(Dispatchers.IO) { attachmentStore.delete(photoName) } }
        showDiscardConfirmation = false
        onDismiss()
    }
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
            OutlinedTextField(value = title, onValueChange = { title = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text(if (section == "Shopping") "Product or option" else if (section == "Bills") "Bill or reminder" else "Title") }, singleLine = true, keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences), shape = RoundedCornerShape(16.dp))
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { typeExpanded = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) { Text("Type · ${recordType.replaceFirstChar { it.uppercase() }}", Modifier.fillMaxWidth()) }
                DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                    listOf("note", "task", "project", "device", "inventory", "bill", "recipe", "meal", "plant", "resource").forEach { value ->
                        DropdownMenuItem(text = { Text(value.replaceFirstChar { it.uppercase() }) }, onClick = { recordType = value; typeExpanded = false })
                    }
                }
            }
            OutlinedTextField(value = category, onValueChange = { category = it }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), label = { Text("Category (optional)") }, singleLine = true, shape = RoundedCornerShape(16.dp))
            OutlinedTextField(value = dueDate, onValueChange = { dueDate = it.trim(); error = null; if (it.isBlank()) reminderEnabled = false }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), label = { Text(if (section == "Cooking" && recordType == "meal") "Planned date (optional)" else "Due date (optional)") }, placeholder = { Text("YYYY-MM-DD") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii), shape = RoundedCornerShape(16.dp))
            if (dueDate.isNotBlank()) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = reminderEnabled, onCheckedChange = { enabled ->
                        if (enabled && android.os.Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        else reminderEnabled = enabled
                    })
                    Text("Notify me on this date around 9 AM", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Box(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                OutlinedButton(onClick = { repeatExpanded = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) { Text("Repeat · $repeatRule", Modifier.fillMaxWidth()) }
                DropdownMenu(expanded = repeatExpanded, onDismissRequest = { repeatExpanded = false }) {
                    listOf("None", "Daily", "Weekly", "Monthly", "Yearly").forEach { value -> DropdownMenuItem(text = { Text(value) }, onClick = { repeatRule = value; repeatExpanded = false }) }
                }
            }
            OutlinedTextField(value = project, onValueChange = { project = it }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), label = { Text("Linked project (optional)") }, singleLine = true, shape = RoundedCornerShape(16.dp))
            OutlinedTextField(value = tags, onValueChange = { tags = it }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), label = { Text("Tags, separated by commas") }, singleLine = true, shape = RoundedCornerShape(16.dp))
            if (section == "Shopping") {
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = sourceUrl, onValueChange = { sourceUrl = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text("Retailer link (optional)") }, placeholder = { Text("https://...") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, capitalization = KeyboardCapitalization.None), shape = RoundedCornerShape(16.dp))
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = sourceCheckedAt, onValueChange = { sourceCheckedAt = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text("When did you check it? (optional)") }, placeholder = { Text("For example, 17 October") }, singleLine = true, keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences), shape = RoundedCornerShape(16.dp))
            }
            if (section == "Bills") {
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text("Amount in EUR (optional)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = RoundedCornerShape(16.dp))
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = billDueDate, onValueChange = { billDueDate = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text("Bill due date (optional)") }, placeholder = { Text("For example, 15 November") }, singleLine = true, keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences), shape = RoundedCornerShape(16.dp))
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = details, onValueChange = { details = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text(if (section == "Shopping") "Price, delivery, features, notes" else "Details (optional)") }, minLines = 3, maxLines = 5, keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences), shape = RoundedCornerShape(16.dp))
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { photoPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (photoName == null) "Attach photo" else "Change photo")
                }
                if (photoName != null) {
                    Text("Photo attached", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    IconButton(onClick = {
                        val previous = photoName
                        photoName = null
                        if (previous != initial?.photoName) scope.launch { withContext(Dispatchers.IO) { attachmentStore.delete(previous) } }
                    }, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Close, contentDescription = "Remove attached photo") }
                }
            }
            error?.let { message ->
                Spacer(Modifier.height(8.dp))
                Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                if (message.startsWith("Notification permission was declined")) {
                    TextButton(onClick = {
                        launchExternal(context, Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName), { error = it }, "Could not open notification settings.")
                    }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Open notification settings") }
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = ::requestClose, enabled = !saving, modifier = Modifier.weight(1f).heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) { Text("Cancel") }
                Button(
                    onClick = {
                        if (title.isBlank()) { error = "Add a short title first."; return@Button }
                        if (dueDate.isNotBlank() && runCatching { LocalDate.parse(dueDate) }.isFailure) { error = "Use a valid date in YYYY-MM-DD format."; return@Button }
                        if (repeatRule != "None" && dueDate.isBlank()) { error = "Set a due date before choosing a repeat schedule."; return@Button }
                        if (reminderEnabled && dueDate.isBlank()) { error = "Add a date before setting a reminder."; return@Button }
                        if (saving) return@Button
                        val cleanedUrl = if (section == "Shopping") runCatching { ShoppingResearchRules.cleanSourceUrl(sourceUrl) }.getOrElse { error = it.message ?: "Check the retailer link."; return@Button } else null
                        saving = true
                        val savedDetails = if (section == "Bills") buildString {
                            val meta = listOf(amount.trim().takeIf { it.isNotEmpty() }?.let { "Amount: $it" }, billDueDate.trim().takeIf { it.isNotEmpty() }?.let { "Due: $it" }).filterNotNull().joinToString(" • ")
                            if (meta.isNotEmpty()) append(meta)
                            if (details.isNotBlank()) { if (isNotEmpty()) append("\n"); append(details.trim()) }
                        } else details.trim()
                        scope.launch {
                            runCatching { onSave(title.trim(), savedDetails, cleanedUrl, sourceCheckedAt.trim().takeIf { section == "Shopping" && it.isNotEmpty() }, recordType, category.trim().takeIf(String::isNotEmpty), dueDate.trim().takeIf(String::isNotEmpty), repeatRule.takeIf { it != "None" }, project.trim().takeIf(String::isNotEmpty), tags.split(",").map(String::trim).filter(String::isNotEmpty).distinct(), reminderEnabled, photoName) }
                                .onSuccess { if (initial?.photoName != photoName) withContext(Dispatchers.IO) { attachmentStore.delete(initial?.photoName) } }
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
            confirmButton = { TextButton(onClick = ::discardAndClose, modifier = Modifier.heightIn(min = 48.dp)) { Text("Discard") } },
            dismissButton = { TextButton(onClick = { showDiscardConfirmation = false }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Keep editing") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationScreen(auth: AuthState, vm: ConnectionViewModel, onMenu: () -> Unit, onSignIn: () -> Unit) {
    val email = (auth as? AuthState.SignedIn)?.email
    val connected = auth is AuthState.SignedIn
    val state by vm.conversation.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var savingIndex by remember { mutableIntStateOf(-1) }
    var savedIndices by remember { mutableStateOf(setOf<Int>()) }
    var selectedImage by rememberSaveable { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { selectedImage = it }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Ask Suric", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, maxLines = 1); Text(email ?: "Optional AI assistance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1) } },
                navigationIcon = { IconButton(onClick = onMenu, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Menu, contentDescription = "Open sections") } },
                actions = { if (connected) IconButton(onClick = vm::disconnect, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Logout, contentDescription = "Disconnect ChatGPT") } }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp, color = MaterialTheme.colorScheme.surface) {
                if (!connected) {
                    Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 18.dp, vertical = 14.dp)) {
                        Text("Connect ChatGPT only when you want an AI response. Your notes, library and settings stay available without it.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = onSignIn, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), enabled = auth != AuthState.SigningIn) {
                            if (auth == AuthState.SigningIn) Text("Waiting for sign-in…") else Text("Continue with ChatGPT")
                        }
                        if (auth == AuthState.SigningIn) TextButton(onClick = vm::cancelSignIn, modifier = Modifier.align(Alignment.CenterHorizontally).heightIn(min = 48.dp)) { Text("Cancel sign-in") }
                        (auth as? AuthState.Failed)?.let { Text(it.message, Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                    }
                } else Column(Modifier.fillMaxWidth().imePadding().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp)) {
                    selectedImage?.let { Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Photo ready to send", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                        IconButton(onClick = { selectedImage = null }, modifier = Modifier.size(44.dp)) { Icon(Icons.Default.Close, contentDescription = "Remove photo") }
                    } }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { picker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.AddPhotoAlternate, "Attach a photo") }
                        OutlinedTextField(value = state.draft, onValueChange = vm::updateDraft, modifier = Modifier.weight(1f), placeholder = { Text("Ask a question", maxLines = 1) }, maxLines = 4, shape = RoundedCornerShape(20.dp))
                        IconButton(onClick = { vm.send(image = selectedImage); selectedImage = null }, enabled = !state.isLoading && (state.draft.isNotBlank() || selectedImage != null), modifier = Modifier.size(52.dp)) { Icon(Icons.AutoMirrored.Filled.Send, "Send") }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), contentPadding = PaddingValues(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (state.messages.isEmpty()) item {
                Column(Modifier.fillMaxWidth().padding(top = 52.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("What’s on your mind?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text("Ask about your home, plans or projects.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (!connected && state.messages.isEmpty()) item {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text("Ask Suric is optional. Your personal areas work locally and remain available offline.", Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.bodyLarge)
                }
            }
            itemsIndexed(state.messages, key = { index, _ -> index }) { index, message ->
                Column(Modifier.fillMaxWidth(), horizontalAlignment = if (message.role == "user") Alignment.End else Alignment.Start) {
                    Surface(
                        color = if (message.role == "user") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomEnd = if (message.role == "user") 7.dp else 22.dp, bottomStart = if (message.role == "user") 22.dp else 7.dp),
                        modifier = Modifier.fillMaxWidth(if (message.role == "user") .90f else 1f)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(message.text, style = MaterialTheme.typography.bodyLarge, color = if (message.role == "user") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                            if (message.role == "assistant" && message.completed) {
                                Spacer(Modifier.height(8.dp))
                                TextButton(onClick = {
                                    if (savingIndex >= 0 || index in savedIndices) return@TextButton
                                    savingIndex = index
                                    scope.launch {
                                        runCatching { vm.saveAssistantResponse(message.text) }
                                            .onSuccess { savedIndices = savedIndices + index; snackbar.showSnackbar("Saved to Notes on this device.") }
                                            .onFailure { snackbar.showSnackbar(it.message ?: "Could not save this answer.") }
                                        savingIndex = -1
                                    }
                                }, enabled = savingIndex < 0 && index !in savedIndices, modifier = Modifier.heightIn(min = 48.dp), contentPadding = PaddingValues(horizontal = 4.dp)) {
                                    Text(when { index in savedIndices -> "Saved to Notes"; savingIndex == index -> "Saving…"; else -> "Save to Notes" }, maxLines = 1)
                                }
                            }
                        }
                }
                }
            }
            if (state.isLoading) item { Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp); Spacer(Modifier.width(10.dp)); Text("Suric is thinking…", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            state.modelLabel?.let { label -> item { Text("Model: $label", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            state.error?.let { item { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(16.dp)) { Text(it, color = MaterialTheme.colorScheme.onErrorContainer); if (connected) TextButton(onClick = vm::retry, modifier = Modifier.heightIn(min = 48.dp)) { Text("Try again") } else TextButton(onClick = onSignIn, modifier = Modifier.heightIn(min = 48.dp)) { Text("Connect ChatGPT") } } } } }
        }
    }
}
