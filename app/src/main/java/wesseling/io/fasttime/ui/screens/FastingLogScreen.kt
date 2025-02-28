package wesseling.io.fasttime.ui.screens

import android.content.Context
import android.content.Intent
import android.util.Log
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
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
                                context = context,
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
                                        putExtra(Intent.EXTRA_TEXT, fast.toShareText(context))
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
                                        .padding(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
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
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
    
    // Fast details dialog
    selectedFast?.let { fast ->
        FastDetailsDialog(
            fast = fast,
            context = context,
            onDismiss = { selectedFast = null }
        )
    }
}

@Composable
fun FastingLogItem(
    fast: CompletedFast,
    context: Context,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val fastingStateColor = getColorForFastingState(fast.maxFastingState)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 2.dp,
            color = fastingStateColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
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
                    text = fast.getFormattedStartTime(context),
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
                        .background(fastingStateColor)
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
                        tint = fastingStateColor
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(40.dp)
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
    context: Context,
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
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                DetailItem(
                    label = "Date",
                    value = fast.getFormattedStartTime(context)
                )
                
                DetailItem(
                    label = "Duration",
                    value = fast.getFormattedDuration(),
                    valueColor = fastingStateColor
                )
                
                DetailItem(
                    label = "Started",
                    value = fast.getFormattedStartTime(context)
                )
                
                DetailItem(
                    label = "Ended",
                    value = fast.getFormattedEndTime(context)
                )
                
                DetailItem(
                    label = "Fasting State Reached",
                    value = fast.maxFastingState.displayName,
                    valueColor = fastingStateColor
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
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = fastingStateColor.copy(alpha = 0.7f)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 1.dp
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
                                tint = fastingStateColor,
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
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = fastingStateColor.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 1.dp
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
                                tint = fastingStateColor,
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
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = fastingStateColor
                )
            ) {
                Text("Close")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun DetailItem(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
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
            color = valueColor,
            fontWeight = if (valueColor != MaterialTheme.colorScheme.onSurface) FontWeight.SemiBold else FontWeight.Normal
        )
        
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            thickness = 0.5.dp
        )
    }
}

// Helper functions
private fun getAchievementText(fast: CompletedFast): String {
    val hours = fast.durationMillis / (1000 * 60 * 60)
    
    return when (fast.maxFastingState) {
        FastingState.NOT_FASTING -> "You completed a feeding period of ${hours}h. This is important for nutrient intake and energy replenishment."
        FastingState.EARLY_FAST -> "You started the fasting process and maintained it for ${hours}h. This is the first step toward metabolic benefits."
        FastingState.KETOSIS -> "You reached ketosis and maintained it for ${hours - FastingState.KETOSIS.hourThreshold}h! Your body has switched to burning fat for energy."
        FastingState.AUTOPHAGY -> "You achieved autophagy for ${hours - FastingState.AUTOPHAGY.hourThreshold}h! Your body is actively cleaning out damaged cells and regenerating new ones."
        FastingState.DEEP_FASTING -> "You reached deep fasting for ${hours - FastingState.DEEP_FASTING.hourThreshold}h! This is where maximum cellular regeneration and metabolic benefits occur."
    }
}

private fun getHealthBenefitsText(fast: CompletedFast): String {
    val hours = fast.durationMillis / (1000 * 60 * 60)
    
    return when {
        hours < 12 -> "Short fasting periods help regulate blood sugar levels, reduce insulin resistance, and give your digestive system a much-needed rest. Even brief fasts can improve metabolic health."
        hours < 18 -> "At this stage, your body has depleted glycogen stores and is beginning to switch to fat burning. Growth hormone levels start to increase, and insulin levels drop significantly."
        hours < 24 -> "Your insulin levels have dropped significantly, making stored body fat more accessible for energy. Cellular repair processes are beginning, and fat oxidation is increasing."
        hours < 48 -> "Autophagy is in full swing, where your body cleans out damaged cells and regenerates new ones. Ketone levels are elevated, providing a clean energy source for your brain and reducing inflammation."
        hours < 72 -> "Growth hormone levels have increased substantially, protecting lean muscle mass and metabolic health. Your body is experiencing enhanced fat burning and cellular cleanup."
        else -> "Extended fasting provides deep cellular cleanup, enhanced mental clarity, and significant metabolic benefits. Your body is in a state of profound renewal with increased stem cell production and immune system regeneration."
    }
}

// Extension function to format CompletedFast for sharing
private fun CompletedFast.toShareText(context: Context): String {
    return """
        I completed a ${getFormattedDuration()} fast using FastTrack!
        
        🕒 Started: ${getFormattedStartTime(context)}
        🏁 Ended: ${getFormattedEndTime(context)}
        ✨ Reached: ${maxFastingState.displayName}
        💪 Benefits: ${when (maxFastingState) {
            FastingState.NOT_FASTING -> "Nutrient replenishment"
            FastingState.EARLY_FAST -> "Beginning fat burning"
            FastingState.KETOSIS -> "Fat burning mode"
            FastingState.AUTOPHAGY -> "Cellular cleanup"
            FastingState.DEEP_FASTING -> "Maximum regeneration"
        }}
        
        #FastTrack #Fasting #IntermittentFasting #${maxFastingState.displayName.replace(" ", "")}
    """.trimIndent()
} 