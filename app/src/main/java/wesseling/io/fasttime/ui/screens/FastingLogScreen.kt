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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import kotlinx.coroutines.launch
import wesseling.io.fasttime.model.CompletedFast
import wesseling.io.fasttime.model.DateTimePreferences
import wesseling.io.fasttime.model.FastingState
import wesseling.io.fasttime.repository.FastingRepository
import wesseling.io.fasttime.ui.theme.getColorForFastingState
import wesseling.io.fasttime.util.DateTimeFormatter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun FastingLogScreen(
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val preferences = remember { DateTimePreferences() }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var allFasts by remember { mutableStateOf<List<CompletedFast>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var fastToDelete by remember { mutableStateOf<CompletedFast?>(null) }
    var showFastDetails by remember { mutableStateOf<CompletedFast?>(null) }
    
    val repository = remember { FastingRepository.getInstance(context) }
    
    // Function to refresh data with error handling
    fun refreshData() {
        isLoading = true
        coroutineScope.launch {
        try {
            allFasts = repository.getAllFasts()
        } catch (e: Exception) {
                Log.e("FastingLogScreen", "Error loading fasts", e)
                snackbarHostState.showSnackbar("Failed to load fasting data")
        } finally {
                isLoading = false
            }
        }
    }
    
    // Pull-to-refresh state
    val pullRefreshState = rememberPullRefreshState(isLoading, ::refreshData)
    
    // Load data when screen is first displayed
    LaunchedEffect(key1 = repository) {
        try {
            allFasts = repository.getAllFasts()
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
                
                // Share the file
                val uri = Uri.fromFile(file)
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
    
    Scaffold(
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
                            fasts = allFasts,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Header with delete all button
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
                                text = "${allFasts.size} sessions",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    // List of fasts
                    items(allFasts) { fast ->
                        FastingLogItem(
                            fast = fast,
                            preferences = preferences,
                            onDeleteClick = { fastToDelete = fast },
                            onShareClick = { shareFast(context, fast) },
                            onInfoClick = { showFastDetails = fast }
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
}

@Composable
fun FastingLogItem(
    fast: CompletedFast,
    preferences: DateTimePreferences,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    val fastingStateColor = getColorForFastingState(fast.maxFastingState)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInfoClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Date and duration row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
        ) {
            Text(
                        text = DateTimeFormatter.formatDateTime(fast.startTimeMillis, preferences),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Text(
                        text = DateTimeFormatter.formatDuration(fast.durationMillis),
                        style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                        color = fastingStateColor
                    )
                }
                
                // Fasting state indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(fastingStateColor)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = fast.maxFastingState.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = fastingStateColor
                    )
                }
            }
            
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
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
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete"
                    )
                }
                
                IconButton(
                    onClick = onInfoClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Details",
                        tint = fastingStateColor
                    )
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
        return fastsList.sumOf { it.durationMillis } / (1000.0 * 60 * 60)
    }
    
    fun calculateAverageFastingHours(fastsList: List<CompletedFast>): Double {
        if (fastsList.isEmpty()) return 0.0
        return calculateTotalFastingHours(fastsList) / fastsList.size
    }
    
    fun calculateLongestFast(fasts: List<CompletedFast>): Double {
        if (fasts.isEmpty()) return 0.0
        return (fasts.maxOfOrNull { it.durationMillis } ?: 0L) / (1000.0 * 60 * 60)
    }
    
    fun calculateTotalFasts(fastsList: List<CompletedFast>): Int {
        return fastsList.size
    }
    
    fun calculateHighestFastingState(fastsList: List<CompletedFast>): FastingState {
        if (fastsList.isEmpty()) return FastingState.NOT_FASTING
        return fastsList.maxByOrNull { it.maxFastingState.ordinal }?.maxFastingState ?: FastingState.NOT_FASTING
    }
    
    fun calculateFastingStateAchievements(fastsList: List<CompletedFast>): Map<FastingState, Int> {
        val achievements = mutableMapOf<FastingState, Int>()
        FastingState.entries.forEach { state ->
            if (state != FastingState.NOT_FASTING) {
                achievements[state] = fastsList.count { it.maxFastingState == state }
            }
        }
        return achievements
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
            
            Divider(
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
                
                Divider(
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
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for ((state, count) in achievements) {
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

@Composable
fun StatisticItem(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
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
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(stateColor.copy(alpha = 0.2f))
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = stateColor
            )
        }
        
        Text(
            text = state.displayName,
            style = MaterialTheme.typography.bodySmall,
            color = stateColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

fun shareFast(context: Context, fast: CompletedFast) {
    val shareText = fast.toShareText(context)
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Fasting Achievement"))
}

fun CompletedFast.toShareText(context: Context): String {
    val preferences = DateTimePreferences()
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