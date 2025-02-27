package wesseling.io.fasttime.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import wesseling.io.fasttime.model.CompletedFast
import wesseling.io.fasttime.model.FastingState
import wesseling.io.fasttime.repository.FastingRepository
import wesseling.io.fasttime.ui.theme.getColorForFastingState

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FastingLogScreen(
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { FastingRepository(context) }
    var allFasts by remember { mutableStateOf(emptyList<CompletedFast>()) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var selectedFast by remember { mutableStateOf<CompletedFast?>(null) }
    
    // Pagination state
    val pageSize = 20
    var currentPage by remember { mutableStateOf(0) }
    var hasMoreData by remember { mutableStateOf(true) }
    
    // Error handling
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Pull-to-refresh state
    var refreshing by remember { mutableStateOf(false) }
    
    // Function to refresh data with error handling
    val refreshData = {
        refreshing = true
        try {
            allFasts = repository.getAllFasts()
            currentPage = 0
            hasMoreData = allFasts.size > pageSize
        } catch (e: Exception) {
            Log.e("FastingLogScreen", "Error refreshing data", e)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Failed to load fasting data")
            }
        } finally {
            refreshing = false
        }
    }
    
    // Load initial data
    LaunchedEffect(key1 = repository) {
        try {
            allFasts = repository.getAllFasts()
            hasMoreData = allFasts.size > pageSize
        } catch (e: Exception) {
            Log.e("FastingLogScreen", "Error loading initial data", e)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Failed to load fasting data")
            }
        }
    }
    
    // Calculate visible fasts based on pagination
    val visibleFasts = remember(allFasts, currentPage, pageSize) {
        val sortedFasts = allFasts.sortedByDescending { it.endTimeMillis }
        val endIndex = minOf((currentPage + 1) * pageSize, sortedFasts.size)
        sortedFasts.subList(0, endIndex)
    }
    
    val pullRefreshState = rememberPullRefreshState(refreshing, { refreshData() })
    
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (allFasts.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState)
                        .padding(16.dp)
                        .background(MaterialTheme.colorScheme.background),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "No fasts",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "No fasting sessions recorded yet",
                                style = MaterialTheme.typography.headlineMedium,
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
                    }
                }
            } else {
                // Fasting log list
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState)
                ) {
                    // Header with delete all button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your completed fasts",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        IconButton(
                            onClick = { showDeleteAllDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "Delete all fasts",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
                    ) {
                        items(visibleFasts) { fast ->
                            FastingLogItem(
                                fast = fast,
                                onClick = { selectedFast = fast },
                                onDelete = {
                                    try {
                                        repository.deleteFast(fast.id)
                                        allFasts = repository.getAllFasts()
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Fast deleted")
                                        }
                                    } catch (e: Exception) {
                                        Log.e("FastingLogScreen", "Error deleting fast", e)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Failed to delete fast")
                                        }
                                    }
                                },
                                onShare = {
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, fast.toShareText())
                                        type = "text/plain"
                                    }
                                    ContextCompat.startActivity(
                                        context,
                                        Intent.createChooser(shareIntent, "Share your fast"),
                                        null
                                    )
                                }
                            )
                        }
                        
                        // Load more item
                        if (hasMoreData && !refreshing) {
                            item {
                                Button(
                                    onClick = {
                                        currentPage++
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text("Load more")
                                }
                            }
                        }
                    }
                }
            }
            
            // Pull to refresh indicator
            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
    
    // Delete all confirmation dialog
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = {
                Text("Delete all fasts?")
            },
            text = {
                Text("This will permanently delete all your recorded fasting sessions. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            repository.deleteAllFasts()
                            allFasts = emptyList()
                            showDeleteAllDialog = false
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("All fasts deleted")
                            }
                        } catch (e: Exception) {
                            Log.e("FastingLogScreen", "Error deleting all fasts", e)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Failed to delete fasts")
                            }
                        }
                    }
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
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
    
    // Fast details dialog
    selectedFast?.let { fast ->
        FastDetailsDialog(
            fast = fast,
            onDismiss = { selectedFast = null }
        )
    }
}

@Composable
fun FastingLogItem(
    fast: CompletedFast,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with date and duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fast.getFormattedStartTime(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = fast.getFormattedDuration(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Fasting state indicator
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(getColorForFastingState(fast.maxFastingState))
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Reached: ${fast.maxFastingState.displayName}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Note preview if available
            if (!fast.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Note: ${fast.note}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Details",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun FastDetailsDialog(
    fast: CompletedFast,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Fasting Details",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                DetailItem(
                    label = "Date",
                    value = fast.getFormattedStartTime()
                )
                
                DetailItem(
                    label = "Duration",
                    value = fast.getFormattedDuration()
                )
                
                DetailItem(
                    label = "Started",
                    value = fast.getFormattedStartTime()
                )
                
                DetailItem(
                    label = "Ended",
                    value = fast.getFormattedEndTime()
                )
                
                DetailItem(
                    label = "Fasting State Reached",
                    value = fast.maxFastingState.displayName
                )
                
                if (!fast.note.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Note",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = fast.note,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Achievement section
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Achievement",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "Achievement",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = getAchievementText(fast),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Health benefits section
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccessTime,
                                contentDescription = "Health Benefits",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "Health Benefits",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = getHealthBenefitsText(fast),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Close")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun DetailItem(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}

// Helper functions
private fun getAchievementText(fast: CompletedFast): String {
    return when (fast.maxFastingState) {
        FastingState.NOT_FASTING -> "You completed a feeding period."
        FastingState.EARLY_FAST -> "You started the fasting process."
        FastingState.KETOSIS -> "You reached ketosis! Your body is now burning fat for energy."
        FastingState.AUTOPHAGY -> "You achieved autophagy! Your body is cleaning out damaged cells."
        FastingState.DEEP_FASTING -> "You reached deep fasting! Maximum cellular regeneration achieved."
    }
}

private fun getHealthBenefitsText(fast: CompletedFast): String {
    val hours = fast.durationMillis / (1000 * 60 * 60)
    
    return when {
        hours < 12 -> "Short fasting periods help regulate blood sugar and give your digestive system a rest."
        hours < 18 -> "At this stage, your body has depleted glycogen stores and is beginning to switch to fat burning."
        hours < 24 -> "Your insulin levels have dropped significantly, making stored body fat more accessible for energy."
        hours < 48 -> "Autophagy is beginning, where your body cleans out damaged cells and regenerates new ones."
        hours < 72 -> "Growth hormone levels increase, protecting lean muscle mass and metabolic health."
        else -> "Extended fasting provides deep cellular cleanup, enhanced mental clarity, and significant metabolic benefits."
    }
}

// Extension function to format CompletedFast for sharing
private fun CompletedFast.toShareText(): String {
    return """
        I completed a ${getFormattedDuration()} fast using FastTrack!
        
        🕒 Started: ${getFormattedStartTime()}
        🏁 Ended: ${getFormattedEndTime()}
        ✨ Reached: ${maxFastingState.displayName}
        
        #FastTrack #Fasting #IntermittentFasting
    """.trimIndent()
} 