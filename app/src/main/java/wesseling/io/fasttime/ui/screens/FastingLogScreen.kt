package wesseling.io.fasttime.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import wesseling.io.fasttime.model.CompletedFast
import wesseling.io.fasttime.model.DateTimePreferences
import wesseling.io.fasttime.model.FastingState
import wesseling.io.fasttime.model.TimeFormat
import wesseling.io.fasttime.repository.FastingRepository
import wesseling.io.fasttime.settings.PreferencesManager
import wesseling.io.fasttime.ui.theme.getColorForFastingState
import wesseling.io.fasttime.util.DateTimeFormatter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

// Add these enum classes for sorting and filtering
enum class SortOption(val displayName: String) {
    DATE_DESC("Date (Newest First)"),
    DATE_ASC("Date (Oldest First)"),
    DURATION_DESC("Duration (Longest First)"),
    DURATION_ASC("Duration (Shortest First)")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun FastingLogScreen(
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager.getInstance(context) }
    val preferences = remember { preferencesManager.dateTimePreferences }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var allFasts by remember { mutableStateOf<List<CompletedFast>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var fastToDelete by remember { mutableStateOf<CompletedFast?>(null) }
    var showFastDetails by remember { mutableStateOf<CompletedFast?>(null) }
    var fastToEdit by remember { mutableStateOf<CompletedFast?>(null) }
    var showEditConfirmation by remember { mutableStateOf(false) }
    
    // Add state variables for sorting and filtering
    var currentSortOption by remember { mutableStateOf(SortOption.DATE_DESC) }
    var selectedFastingState by remember { mutableStateOf<FastingState?>(null) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    
    val repository = remember { FastingRepository.getInstance(context) }
    
    // Function to refresh data with error handling
    fun refreshData() {
        isLoading = true
        coroutineScope.launch {
            try {
                Log.d("FastingLogScreen", "Refreshing fasts from repository")
                val fasts = repository.getAllFasts()
                Log.d("FastingLogScreen", "Refreshed ${fasts.size} fasts from repository")
                
                // Log details of each fast for debugging
                fasts.forEachIndexed { index, fast ->
                    Log.d("FastingLogScreen", "Fast $index: id=${fast.id}, duration=${fast.durationMillis}, state=${fast.maxFastingState}")
                }
                
                allFasts = fasts
            } catch (e: Exception) {
                Log.e("FastingLogScreen", "Error loading fasts", e)
                snackbarHostState.showSnackbar("Failed to load fasting data")
            } finally {
                isLoading = false
            }
        }
    }
    
    // Function to sort and filter fasts
    fun getSortedAndFilteredFasts(): List<CompletedFast> {
        // First apply filter
        val filteredFasts = if (selectedFastingState != null) {
            allFasts.filter { it.maxFastingState == selectedFastingState }
        } else {
            allFasts
        }
        
        // Then apply sorting
        return when (currentSortOption) {
            SortOption.DATE_DESC -> filteredFasts.sortedByDescending { it.endTimeMillis }
            SortOption.DATE_ASC -> filteredFasts.sortedBy { it.endTimeMillis }
            SortOption.DURATION_DESC -> filteredFasts.sortedByDescending { it.durationMillis }
            SortOption.DURATION_ASC -> filteredFasts.sortedBy { it.durationMillis }
        }
    }
    
    // Pull-to-refresh state
    val pullRefreshState = rememberPullRefreshState(isLoading, ::refreshData)
    
    // Load data when screen is first displayed
    LaunchedEffect(key1 = repository) {
        try {
            Log.d("FastingLogScreen", "Loading fasts from repository")
            val fasts = repository.getAllFasts()
            Log.d("FastingLogScreen", "Loaded ${fasts.size} fasts from repository")
            
            // Log details of each fast for debugging
            fasts.forEachIndexed { index, fast ->
                Log.d("FastingLogScreen", "Fast $index: id=${fast.id}, duration=${fast.durationMillis}, state=${fast.maxFastingState}")
            }
            
            allFasts = fasts
        } catch (e: Exception) {
            Log.e("FastingLogScreen", "Error loading fasts", e)
            snackbarHostState.showSnackbar("Failed to load fasting data")
        }
    }
    
    // Function to export all fasts to CSV
    fun exportAllFasts() {
        coroutineScope.launch {
            try {
                val fasts = repository.getAllFasts()
                if (fasts.isEmpty()) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("No fasting logs to export")
                    }
                    return@launch
                }
                
                // Create CSV content
                val csvContent = StringBuilder()
                csvContent.append("Start Time,End Time,Duration,Max Fasting State\n")
                
                fasts.forEach { fast ->
                    csvContent.append("${DateTimeFormatter.formatDateTime(fast.startTimeMillis, preferences)},")
                    csvContent.append("${DateTimeFormatter.formatDateTime(fast.endTimeMillis, preferences)},")
                    csvContent.append("${DateTimeFormatter.formatDuration(fast.durationMillis)},")
                    csvContent.append("${fast.maxFastingState.displayName}\n")
                }
                
                val fullContent = csvContent.toString()
                
                // Create file in app's cache directory
                val fileName = "fasting_log_${SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())}.csv"
                val file = File(context.cacheDir, fileName)
                FileWriter(file).use { it.write(fullContent) }
                
                // Share the file using FileProvider
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "FastTrack Fasting Log")
                    putExtra(Intent.EXTRA_TEXT, "Here's my fasting log from FastTrack!")
                    type = "text/csv"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                context.startActivity(Intent.createChooser(shareIntent, "Share Fasting Log"))
                
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Fasting log exported successfully")
                }
            } catch (e: Exception) {
                Log.e("FastingLogScreen", "Error exporting all fasts", e)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Failed to export fasting logs")
                }
            }
        }
    }
    
    // Function to handle editing a fast
    fun editFast(fast: CompletedFast) {
        coroutineScope.launch {
            try {
                repository.updateFast(fast)
                refreshData()
                snackbarHostState.showSnackbar("Fast updated successfully")
            } catch (e: Exception) {
                Log.e("FastingLogScreen", "Error updating fast", e)
                snackbarHostState.showSnackbar("Failed to update fast")
            }
        }
    }
    
    // Function to add a new fast entry
    fun addNewFastEntry() {
        coroutineScope.launch {
            try {
                Log.d("FastingLogScreen", "Adding new fast entry to repository")
                
                // Create a new fast entry with current time
                val currentTime = System.currentTimeMillis()
                val startTime = currentTime - (16 * 60 * 60 * 1000) // 16 hours ago
                
                val newFast = CompletedFast(
                    startTimeMillis = startTime,
                    endTimeMillis = currentTime,
                    durationMillis = currentTime - startTime,
                    maxFastingState = FastingState.METABOLIC_SHIFT,
                    note = "Fast entry created manually"
                )
                
                // Save the new fast entry
                repository.saveFast(newFast)
                
                // Refresh the data
                refreshData()
                
                // Show a snackbar
                snackbarHostState.showSnackbar("New fast entry created")
            } catch (e: Exception) {
                Log.e("FastingLogScreen", "Error adding new fast entry", e)
                snackbarHostState.showSnackbar("Failed to add fast entry")
            }
        }
    }
    
    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Fasting Log",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Add sort button
                    IconButton(onClick = { showSortDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort"
                        )
                    }
                    
                    // Add filter button
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter"
                        )
                    }
                    
                    // Refresh button
                    IconButton(
                        onClick = { refreshData() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Fasting Log"
                        )
                    }
                    // Add new fast entry button
                    IconButton(
                        onClick = { addNewFastEntry() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add New Fast Entry"
                        )
                    }
                    // Export button
                    IconButton(
                        onClick = { exportAllFasts() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export Fasting Log"
                        )
                    }
                    // Delete all button
                    IconButton(
                        onClick = { showDeleteAllDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Delete All Fasts"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            val sortedAndFilteredFasts = getSortedAndFilteredFasts()
            
            if (allFasts.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                        contentDescription = null,
                                modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "No fasting sessions recorded yet",
                        style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Complete a fast to see it in your log",
                        style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else if (sortedAndFilteredFasts.isEmpty()) {
                // No results after filtering
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "No matching fasting sessions",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Try changing your filter settings",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { selectedFastingState = null },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Clear Filter")
                    }
                }
            } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Summary section
                    item {
                        FastingLogSummary(
                            fasts = sortedAndFilteredFasts,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Active filters indicator
                    if (selectedFastingState != null) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Filtered by: ${selectedFastingState?.displayName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                TextButton(
                                    onClick = { selectedFastingState = null }
                                ) {
                                    Text("Clear")
                                }
                            }
                        }
                    }
                    
                    // Header with count
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Fasting History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Text(
                                text = "${sortedAndFilteredFasts.size} sessions",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    // List of fasts
                    items(sortedAndFilteredFasts) { fast ->
                        FastingLogItem(
                            fast = fast,
                            preferences = preferences,
                            onDeleteClick = { fastToDelete = fast },
                            onShareClick = { shareFast(context, fast) },
                            onInfoClick = { showFastDetails = fast },
                            onEditClick = { 
                                fastToEdit = fast
                                showEditConfirmation = true
                            }
                        )
                    }
                    
                    item {
                            Spacer(modifier = Modifier.height(16.dp))
                    }
                        }
                    }
                    
            // Pull to refresh indicator
                    PullRefreshIndicator(
                refreshing = isLoading,
                        state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Delete all confirmation dialog
            if (showDeleteAllDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Delete All Fasting Logs") },
            text = { Text("Are you sure you want to delete all your fasting logs? This action cannot be undone.") },
                    confirmButton = {
                TextButton(
                            onClick = {
                        coroutineScope.launch {
                                try {
                                    repository.deleteAllFasts()
                                    allFasts = emptyList()
                                snackbarHostState.showSnackbar("All fasting logs deleted")
                                } catch (e: Exception) {
                                    Log.e("FastingLogScreen", "Error deleting all fasts", e)
                                snackbarHostState.showSnackbar("Failed to delete fasting logs")
                                    }
                                }
                        showDeleteAllDialog = false
                            },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Delete All")
                        }
                    },
                    dismissButton = {
                TextButton(
                    onClick = { showDeleteAllDialog = false }
                ) {
                            Text("Cancel")
                        }
            }
                )
            }
            
            // Delete single fast confirmation dialog
    fastToDelete?.let { fast ->
                AlertDialog(
            onDismissRequest = { fastToDelete = null },
            title = { Text("Delete Fasting Log") },
            text = { Text("Are you sure you want to delete this fasting log? This action cannot be undone.") },
                    confirmButton = {
                TextButton(
                            onClick = {
                        coroutineScope.launch {
                                try {
                                repository.deleteFast(fast.id)
                                        allFasts = repository.getAllFasts()
                                snackbarHostState.showSnackbar("Fasting log deleted")
                                } catch (e: Exception) {
                                    Log.e("FastingLogScreen", "Error deleting fast", e)
                                snackbarHostState.showSnackbar("Failed to delete fast")
                                    }
                                }
                        fastToDelete = null
                            },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                TextButton(
                    onClick = { fastToDelete = null }
                ) {
                            Text("Cancel")
                        }
            }
        )
    }
    
    // Fast details dialog
    showFastDetails?.let { fast ->
        FastDetailsDialog(
            fast = fast,
            preferences = preferences,
            onDismiss = { showFastDetails = null }
        )
    }
    
    // Edit confirmation dialog
    if (showEditConfirmation && fastToEdit != null) {
        AlertDialog(
            onDismissRequest = { 
                showEditConfirmation = false
                fastToEdit = null
            },
                    title = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                        imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                    Text(
                        text = "Confirm Edit",
                        style = MaterialTheme.typography.titleLarge
                    )
                        }
                    },
                    text = { 
                Text("Are you sure you want to edit this fast? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEditConfirmation = false
                        // fastToEdit is already set, no need to change it
                    }
                ) {
                    Text("Edit")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showEditConfirmation = false
                        fastToEdit = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Edit fast dialog
    fastToEdit?.let { fast ->
        EditFastDialog(
            fast = fast,
            preferences = preferences,
            onSave = { updatedFast ->
                editFast(updatedFast)
                fastToEdit = null
            },
            onDismiss = { fastToEdit = null }
        )
    }
    
    // Sort dialog
    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text("Sort By") },
            text = {
                Column {
                    SortOption.values().forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentSortOption = option
                                    showSortDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (currentSortOption == option) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Spacer(modifier = Modifier.width(24.dp))
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Text(
                                text = option.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (currentSortOption == option) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
    
    // Filter dialog
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filter By Fasting State") },
            text = {
                Column {
                    // Add "All" option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedFastingState = null
                                showFilterDialog = false
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedFastingState == null) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.width(24.dp))
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = "All States",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selectedFastingState == null) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Add each fasting state as an option
                    FastingState.values().forEach { state ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedFastingState = state
                                    showFilterDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedFastingState == state) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = getColorForFastingState(state),
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(4.dp)
                                        .background(
                                            color = getColorForFastingState(state).copy(alpha = 0.3f),
                                            shape = CircleShape
                                        )
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Text(
                                text = state.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selectedFastingState == state) 
                                    getColorForFastingState(state)
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun FastingLogItem(
    fast: CompletedFast,
    preferences: DateTimePreferences,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit,
    onInfoClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val fastingStateColor = getColorForFastingState(fast.maxFastingState)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInfoClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Duration at the top with smaller font
            Text(
                text = DateTimeFormatter.formatDuration(fast.durationMillis),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = fastingStateColor
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Start and end time on a full line with centered arrow
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = DateTimeFormatter.formatDateTime(fast.startTimeMillis, preferences),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.alignByBaseline()
                )
                
                Text(
                    text = " → ",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .alignByBaseline()
                )
                
                Text(
                    text = DateTimeFormatter.formatDateTime(fast.endTimeMillis, preferences),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.alignByBaseline()
                )
            }
            
            // Display notes if they exist
            if (fast.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = fast.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                )
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                thickness = 1.dp
            )
            
            // Bottom row with fasting state pill on left and action buttons on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fasting state indicator moved to bottom left
                Box(
                    modifier = Modifier
                        .background(
                            color = fastingStateColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(fastingStateColor)
                        )
                        
                        Spacer(modifier = Modifier.width(6.dp))
                        
                        Text(
                            text = fast.maxFastingState.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = fastingStateColor
                        )
                    }
                }
                
                // Action buttons
                Row {
                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = fastingStateColor
                        )
                    }
                    
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FastDetailsDialog(
    fast: CompletedFast,
    preferences: DateTimePreferences,
    onDismiss: () -> Unit
) {
    val fastingStateColor = getColorForFastingState(fast.maxFastingState)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(fastingStateColor)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                    text = "Fasting Details",
                    style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
        text = {
            Column(
                    modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                DetailItem(
                    label = "Started",
                    value = DateTimeFormatter.formatDateTime(fast.startTimeMillis, preferences),
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
                
                DetailItem(
                    label = "Ended",
                    value = DateTimeFormatter.formatDateTime(fast.endTimeMillis, preferences),
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
                
                DetailItem(
                    label = "Duration",
                    value = DateTimeFormatter.formatDuration(fast.durationMillis),
                    valueColor = fastingStateColor
                )
                
                DetailItem(
                    label = "Fasting State",
                    value = fast.maxFastingState.displayName,
                    valueColor = fastingStateColor
                )
                
                // Display notes if they exist
                if (fast.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    Text(
                        text = fast.note,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
fun DetailItem(
    label: String,
    value: String,
    valueColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

/**
 * A summary section for the fasting log showing statistics and achievements
 */
@Composable
fun FastingLogSummary(
    fasts: List<CompletedFast>,
    modifier: Modifier = Modifier
) {
    if (fasts.isEmpty()) return
    
    // Helper functions to calculate fasting statistics
    fun calculateTotalFastingHours(fastsList: List<CompletedFast>): Double {
        // Only count hours from fasts that reach at least GLYCOGEN_DEPLETION (12+ hours)
        return fastsList
            .filter { 
                it.maxFastingState != FastingState.NOT_FASTING && 
                it.maxFastingState != FastingState.EARLY_FAST 
            }
            .sumOf { it.durationMillis } / (1000.0 * 60 * 60)
    }
    
    fun calculateAverageFastingHours(fastsList: List<CompletedFast>): Double {
        // Filter to only include fasts that reach at least GLYCOGEN_DEPLETION (12+ hours)
        val validFasts = fastsList.filter { 
            it.maxFastingState != FastingState.NOT_FASTING && 
            it.maxFastingState != FastingState.EARLY_FAST 
        }
        
        if (validFasts.isEmpty()) return 0.0
        return validFasts.sumOf { it.durationMillis } / (1000.0 * 60 * 60) / validFasts.size
    }
    
    fun calculateLongestFast(fasts: List<CompletedFast>): Double {
        // Filter to only include fasts that reach at least GLYCOGEN_DEPLETION (12+ hours)
        val validFasts = fasts.filter { 
            it.maxFastingState != FastingState.NOT_FASTING && 
            it.maxFastingState != FastingState.EARLY_FAST 
        }
        
        if (validFasts.isEmpty()) return 0.0
        return (validFasts.maxOfOrNull { it.durationMillis } ?: 0L) / (1000.0 * 60 * 60)
    }
    
    fun calculateTotalFasts(fastsList: List<CompletedFast>): Int {
        // Only count fasts that reach at least GLYCOGEN_DEPLETION (12+ hours)
        return fastsList.count { 
            it.maxFastingState != FastingState.NOT_FASTING && 
            it.maxFastingState != FastingState.EARLY_FAST 
        }
    }
    
    fun calculateHighestFastingState(fastsList: List<CompletedFast>): FastingState {
        // Filter to only include fasts that reach at least GLYCOGEN_DEPLETION (12+ hours)
        val validFasts = fastsList.filter { 
            it.maxFastingState != FastingState.NOT_FASTING && 
            it.maxFastingState != FastingState.EARLY_FAST 
        }
        
        if (validFasts.isEmpty()) return FastingState.NOT_FASTING
        return validFasts.maxByOrNull { it.maxFastingState.ordinal }?.maxFastingState ?: FastingState.NOT_FASTING
    }
    
    fun calculateFastingStateAchievements(fastsList: List<CompletedFast>): Map<FastingState, Int> {
        val achievements = mutableMapOf<FastingState, Int>()
        // Only include states from GLYCOGEN_DEPLETION (12+ hours) and beyond
        val validStates = FastingState.values().filter { 
            it != FastingState.NOT_FASTING && it != FastingState.EARLY_FAST 
        }
        
        for (state in validStates) {
            achievements[state] = fastsList.count { it.maxFastingState == state }
        }
        
        // Add secret achievement for weekly deep fasts (24+ hours)
        // This counts the number of consecutive weeks with at least one deep fast
        val deepFasts = fastsList.filter { it.maxFastingState.ordinal >= FastingState.DEEP_KETOSIS.ordinal }
            .sortedBy { it.endTimeMillis }
        
        if (deepFasts.isNotEmpty()) {
            // Group fasts by week
            val calendar = Calendar.getInstance()
            val weeklyFasts = mutableMapOf<Pair<Int, Int>, Boolean>() // (year, weekOfYear) -> hasDeepFast
            
            for (fast in deepFasts) {
                calendar.timeInMillis = fast.endTimeMillis
                val year = calendar.get(Calendar.YEAR)
                val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
                weeklyFasts[Pair(year, weekOfYear)] = true
            }
            
            // Find consecutive weeks
            val sortedWeeks = weeklyFasts.keys.sortedWith(compareBy({ it.first }, { it.second }))
            var currentStreak = 1
            var maxStreak = 1
            
            for (i in 1 until sortedWeeks.size) {
                val prevWeek = sortedWeeks[i-1]
                val currWeek = sortedWeeks[i]
                
                // Check if weeks are consecutive
                val isConsecutive = if (prevWeek.second == 52 && currWeek.second == 1) {
                    // Year boundary case
                    currWeek.first - prevWeek.first == 1
                } else {
                    prevWeek.first == currWeek.first && currWeek.second - prevWeek.second == 1
                }
                
                if (isConsecutive) {
                    currentStreak++
                    maxStreak = maxOf(maxStreak, currentStreak)
                } else {
                    currentStreak = 1
                }
            }
            
            // Only add the achievement if there's at least one streak
            if (maxStreak > 0) {
                // Use a special key for the secret achievement
                achievements[FastingState.EXTENDED_FAST] = maxStreak
            }
        }
        
        // Return a map sorted by state ordinal
        return achievements.toSortedMap(compareBy { it.ordinal })
    }
    
    val totalFasts = calculateTotalFasts(fasts)
    val totalHours = calculateTotalFastingHours(fasts)
    val averageHours = calculateAverageFastingHours(fasts)
    val longestFast = calculateLongestFast(fasts)
    val highestState = calculateHighestFastingState(fasts)
    val achievements = calculateFastingStateAchievements(fasts)
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Fasting Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Add informational note about 12+ hour requirement
            Text(
                text = "Only fasts of 12+ hours are counted",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp)
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )
            
            // Statistics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem(
                    label = "Total Fasts",
                    value = totalFasts.toString(),
                    modifier = Modifier.weight(1f)
                )
                
                StatisticItem(
                    label = "Total Hours",
                    value = "%.1f".format(totalHours),
                    modifier = Modifier.weight(1f)
                )
                
                StatisticItem(
                    label = "Average",
                    value = "%.1f h".format(averageHours),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem(
                    label = "Longest Fast",
                    value = "%.1f h".format(longestFast),
                    modifier = Modifier.weight(1f)
                )
                
                StatisticItem(
                    label = "Best State",
                    value = highestState.displayName,
                    valueColor = getColorForFastingState(highestState),
                    modifier = Modifier.weight(1f)
                )
            }
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )
            
            // Achievements section
            Text(
                text = "Achievements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            val achievementsList = achievements.toList()
            if (achievementsList.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(((achievementsList.size / 3 + 1) * 70).dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalArrangement = Arrangement.Center
                ) {
                    items(achievementsList) { (state, count) ->
                        if (count > 0) {
                            AchievementItem(
                                state = state,
                                count = count
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun AchievementItem(
    state: FastingState,
    count: Int
) {
    val stateColor = getColorForFastingState(state)
    
    // Special handling for the secret achievement (weekly deep fasts)
    val isSecretAchievement = state == FastingState.EXTENDED_FAST
    val displayText = if (isSecretAchievement) {
        when {
            count >= 26 -> "🏆 Legend" // Half a year of weekly deep fasts
            count >= 12 -> "🥇 Gold"   // Three months of weekly deep fasts
            count >= 4 -> "🥈 Silver"  // One month of weekly deep fasts
            else -> "🥉 Bronze"        // Starting streak
        }
    } else {
        state.displayName
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isSecretAchievement) 
                        Color(0xFFFFD700).copy(alpha = 0.2f) 
                    else 
                        stateColor.copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSecretAchievement) Color(0xFFFFD700) else stateColor
            )
        }
        
        Text(
            text = displayText,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSecretAchievement) Color(0xFFFFD700) else stateColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

fun shareFast(context: Context, fast: CompletedFast) {
    val preferencesManager = PreferencesManager.getInstance(context)
    val preferences = preferencesManager.dateTimePreferences
    val shareText = fast.toShareText(preferences)
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Fasting Achievement"))
}

fun CompletedFast.toShareText(preferences: DateTimePreferences): String {
    val startDate = DateTimeFormatter.formatDateTime(startTimeMillis, preferences)
    val duration = DateTimeFormatter.formatDuration(durationMillis)
    val fastingStateName = maxFastingState.displayName
    
    return """
        I completed a $duration fast with FastTrack! 🎉
        
        Started: $startDate
        Achieved: $fastingStateName
        
        #FastTrack #Fasting #IntermittentFasting #${fastingStateName.replace(" ", "")}
    """.trimIndent()
}

@Composable
fun EditFastDialog(
    fast: CompletedFast,
    preferences: DateTimePreferences,
    onSave: (CompletedFast) -> Unit,
    onDismiss: () -> Unit
) {
    var note by remember { mutableStateOf(fast.note) }
    var startTimeMillis by remember { mutableStateOf(fast.startTimeMillis) }
    var endTimeMillis by remember { mutableStateOf(fast.endTimeMillis) }
    
    // Calculate duration and fasting state based on current start/end times
    val durationMillis = endTimeMillis - startTimeMillis
    val maxFastingState = FastingState.getStateForDuration(durationMillis)
    
    val fastingStateColor = getColorForFastingState(maxFastingState)
    
    // Date/time picker states
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    // Error state
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Edit Fast",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                // Start time section with edit button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Started",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        
                        Text(
                            text = DateTimeFormatter.formatDateTime(startTimeMillis, preferences),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    IconButton(onClick = { showStartDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Start Time",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // End time section with edit button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ended",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        
                        Text(
                            text = DateTimeFormatter.formatDateTime(endTimeMillis, preferences),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    IconButton(onClick = { showEndDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit End Time",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Duration (calculated)
                DetailItem(
                    label = "Duration",
                    value = DateTimeFormatter.formatDuration(durationMillis),
                    valueColor = fastingStateColor
                )
                
                // Fasting State (calculated)
                DetailItem(
                    label = "Fasting State",
                    value = maxFastingState.displayName,
                    valueColor = fastingStateColor
                )
                
                // Error message if any
                errorMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    ),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    // Validate times
                    if (endTimeMillis <= startTimeMillis) {
                        errorMessage = "End time must be after start time"
                    } else if (endTimeMillis > System.currentTimeMillis()) {
                        errorMessage = "End time cannot be in the future"
                    } else {
                        // Create updated fast with new times, duration and state
                        val updatedFast = fast.copy(
                            startTimeMillis = startTimeMillis,
                            endTimeMillis = endTimeMillis,
                            durationMillis = durationMillis,
                            maxFastingState = maxFastingState,
                            note = note
                        )
                        onSave(updatedFast)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
    
    // Date/Time Pickers
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            onDateSelected = { date ->
                // Keep the time part, update only the date part
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = startTimeMillis
                val oldHour = calendar.get(Calendar.HOUR_OF_DAY)
                val oldMinute = calendar.get(Calendar.MINUTE)
                
                calendar.timeInMillis = date
                calendar.set(Calendar.HOUR_OF_DAY, oldHour)
                calendar.set(Calendar.MINUTE, oldMinute)
                
                startTimeMillis = calendar.timeInMillis
                showStartDatePicker = false
                showStartTimePicker = true // Show time picker after date
            },
            initialDate = startTimeMillis
        )
    }
    
    if (showStartTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            onTimeSelected = { hour, minute ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = startTimeMillis
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                startTimeMillis = calendar.timeInMillis
                showStartTimePicker = false
                
                // Clear any error messages when user makes changes
                errorMessage = null
            },
            initialTimeMillis = startTimeMillis
        )
    }
    
    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            onDateSelected = { date ->
                // Keep the time part, update only the date part
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = endTimeMillis
                val oldHour = calendar.get(Calendar.HOUR_OF_DAY)
                val oldMinute = calendar.get(Calendar.MINUTE)
                
                calendar.timeInMillis = date
                calendar.set(Calendar.HOUR_OF_DAY, oldHour)
                calendar.set(Calendar.MINUTE, oldMinute)
                
                endTimeMillis = calendar.timeInMillis
                showEndDatePicker = false
                showEndTimePicker = true // Show time picker after date
            },
            initialDate = endTimeMillis
        )
    }
    
    if (showEndTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            onTimeSelected = { hour, minute ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = endTimeMillis
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                endTimeMillis = calendar.timeInMillis
                showEndTimePicker = false
                
                // Clear any error messages when user makes changes
                errorMessage = null
            },
            initialTimeMillis = endTimeMillis
        )
    }
}

/**
 * A dialog for selecting a date
 */
@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (Long) -> Unit,
    initialDate: Long
) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = initialDate
    
    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { 
            Text(
                text = "Select Date",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                // Year picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Year:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(80.dp)
                    )
                    
                    // Simple year picker with +/- buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { selectedYear-- }
                        ) {
                            Text("-", fontWeight = FontWeight.Bold)
                        }
                        
                        Text(
                            text = selectedYear.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        
                        IconButton(
                            onClick = { selectedYear++ }
                        ) {
                            Text("+", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Month picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Month:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(80.dp)
                    )
                    
                    // Simple month picker with +/- buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { 
                                if (selectedMonth > 0) selectedMonth-- 
                                else {
                                    selectedMonth = 11
                                    selectedYear--
                                }
                            }
                        ) {
                            Text("-", fontWeight = FontWeight.Bold)
                        }
                        
                        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                                              "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                        
                        Text(
                            text = monthNames[selectedMonth],
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        
                        IconButton(
                            onClick = { 
                                if (selectedMonth < 11) selectedMonth++ 
                                else {
                                    selectedMonth = 0
                                    selectedYear++
                                }
                            }
                        ) {
                            Text("+", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Day picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Day:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(80.dp)
                    )
                    
                    // Simple day picker with +/- buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { 
                                if (selectedDay > 1) selectedDay-- 
                                else {
                                    // Go to previous month's last day
                                    val tempCalendar = Calendar.getInstance()
                                    tempCalendar.set(selectedYear, selectedMonth, 1)
                                    tempCalendar.add(Calendar.DAY_OF_MONTH, -1)
                                    selectedDay = tempCalendar.get(Calendar.DAY_OF_MONTH)
                                    selectedMonth = tempCalendar.get(Calendar.MONTH)
                                    selectedYear = tempCalendar.get(Calendar.YEAR)
                                }
                            }
                        ) {
                            Text("-", fontWeight = FontWeight.Bold)
                        }
                        
                        Text(
                            text = selectedDay.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        
                        IconButton(
                            onClick = { 
                                // Calculate max days in current month
                                val tempCalendar = Calendar.getInstance()
                                tempCalendar.set(selectedYear, selectedMonth, 1)
                                val maxDays = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                                
                                if (selectedDay < maxDays) selectedDay++ 
                                else {
                                    // Go to next month's first day
                                    selectedDay = 1
                                    if (selectedMonth < 11) selectedMonth++
                                    else {
                                        selectedMonth = 0
                                        selectedYear++
                                    }
                                }
                            }
                        ) {
                            Text("+", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val resultCalendar = Calendar.getInstance()
                    resultCalendar.set(selectedYear, selectedMonth, selectedDay)
                    onDateSelected(resultCalendar.timeInMillis)
                }
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Cancel")
            }
        }
    )
}

/**
 * A dialog for selecting a time
 */
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    initialTimeMillis: Long
) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = initialTimeMillis
    
    // Get user preferences for time format
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager.getInstance(context) }
    val preferences = preferencesManager.dateTimePreferences
    val use12HourFormat = preferences.timeFormat == TimeFormat.HOURS_12
    
    // Store 24-hour format internally
    var selectedHour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }
    
    // For 12-hour format display
    var isAM by remember { 
        mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY) < 12) 
    }
    
    // Convert 24-hour to 12-hour format for display
    val displayHour = if (use12HourFormat) {
        val h = selectedHour % 12
        if (h == 0) 12 else h  // Convert 0 to 12 for 12-hour format
    } else {
        selectedHour
    }
    
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { 
            Text(
                text = "Select Time",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hour and minute pickers side by side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour picker
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Hour",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        IconButton(
                            onClick = { 
                                if (use12HourFormat) {
                                    // In 12-hour format, cycle 1-12
                                    val newDisplayHour = if (displayHour < 12) displayHour + 1 else 1
                                    // Convert to 24-hour for internal storage
                                    selectedHour = if (isAM) {
                                        if (newDisplayHour == 12) 0 else newDisplayHour
                                    } else {
                                        if (newDisplayHour == 12) 12 else newDisplayHour + 12
                                    }
                                } else {
                                    // In 24-hour format, cycle 0-23
                                    selectedHour = if (selectedHour < 23) selectedHour + 1 else 0
                                    isAM = selectedHour < 12
                                }
                            }
                        ) {
                            Text("+", fontWeight = FontWeight.Bold)
                        }
                        
                        Text(
                            text = if (use12HourFormat) {
                                String.format("%d", displayHour) // No leading zero for 12-hour
                            } else {
                                String.format("%02d", displayHour) // Leading zero for 24-hour
                            },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        IconButton(
                            onClick = { 
                                if (use12HourFormat) {
                                    // In 12-hour format, cycle 12-1
                                    val newDisplayHour = if (displayHour > 1) displayHour - 1 else 12
                                    // Convert to 24-hour for internal storage
                                    selectedHour = if (isAM) {
                                        if (newDisplayHour == 12) 0 else newDisplayHour
                                    } else {
                                        if (newDisplayHour == 12) 12 else newDisplayHour + 12
                                    }
                                } else {
                                    // In 24-hour format, cycle 23-0
                                    selectedHour = if (selectedHour > 0) selectedHour - 1 else 23
                                    isAM = selectedHour < 12
                                }
                            }
                        ) {
                            Text("-", fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Minute picker
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Minute",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        IconButton(
                            onClick = { 
                                selectedMinute = if (selectedMinute < 59) selectedMinute + 1 else 0
                            }
                        ) {
                            Text("+", fontWeight = FontWeight.Bold)
                        }
                        
                        Text(
                            text = String.format("%02d", selectedMinute),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        IconButton(
                            onClick = { 
                                selectedMinute = if (selectedMinute > 0) selectedMinute - 1 else 59
                            }
                        ) {
                            Text("-", fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    // AM/PM selector for 12-hour format
                    if (use12HourFormat) {
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "AM/PM",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // AM button
                            Button(
                                onClick = { 
                                    if (!isAM) {
                                        isAM = true
                                        // Update the hour in 24-hour format
                                        selectedHour -= 12
                                        if (selectedHour < 0) selectedHour += 24
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAM) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isAM) 
                                        MaterialTheme.colorScheme.onPrimary 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text("AM")
                            }
                            
                            // PM button
                            Button(
                                onClick = { 
                                    if (isAM) {
                                        isAM = false
                                        // Update the hour in 24-hour format
                                        selectedHour += 12
                                        if (selectedHour >= 24) selectedHour -= 24
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!isAM) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (!isAM) 
                                        MaterialTheme.colorScheme.onPrimary 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text("PM")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onTimeSelected(selectedHour, selectedMinute)
                }
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Cancel")
            }
        }
    )
} 