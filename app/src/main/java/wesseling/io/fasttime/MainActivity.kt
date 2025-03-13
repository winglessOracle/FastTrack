package wesseling.io.fasttime

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import wesseling.io.fasttime.model.CompletedFast
import wesseling.io.fasttime.model.FastingState
import wesseling.io.fasttime.repository.FastingRepository
import wesseling.io.fasttime.timer.FastingTimer
import wesseling.io.fasttime.ui.components.FastingLegend
import wesseling.io.fasttime.ui.components.FastingSummaryDialog
import wesseling.io.fasttime.ui.components.FastingTimerButton
import wesseling.io.fasttime.ui.screens.FastingLogScreen
import wesseling.io.fasttime.ui.screens.HelpScreen
import wesseling.io.fasttime.ui.screens.SettingsScreen
import wesseling.io.fasttime.ui.theme.FastTrackTheme

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Check if we need to show a completed fast from the widget
        val showCompletedFast = intent.getBooleanExtra("SHOW_COMPLETED_FAST", false)
        var completedFast: CompletedFast? = null
        
        if (showCompletedFast) {
            try {
                val startTime = intent.getLongExtra("START_TIME", 0)
                val endTime = intent.getLongExtra("END_TIME", 0)
                val duration = intent.getLongExtra("DURATION", 0)
                val maxStateOrdinal = intent.getIntExtra("MAX_STATE", 0)
                
                if (duration > 0) {
                    val maxState = if (maxStateOrdinal >= 0 && maxStateOrdinal < FastingState.entries.size) {
                        FastingState.entries[maxStateOrdinal]
                    } else {
                        FastingState.NOT_FASTING
                    }
                    
                    completedFast = CompletedFast(
                        startTimeMillis = startTime,
                        endTimeMillis = endTime,
                        durationMillis = duration,
                        maxFastingState = maxState
                    )
                    
                    Log.d(TAG, "Received completed fast from widget: $completedFast")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing completed fast from intent", e)
            }
        }
        
        setContent {
            FastTrackTheme {
                val finalCompletedFast = completedFast
                
                if (finalCompletedFast != null) {
                    val repository = remember { FastingRepository.getInstance(this) }
                    var showDialog by remember { mutableStateOf(true) }
                    
                    if (showDialog) {
                        FastingSummaryDialog(
                            completedFast = finalCompletedFast,
                            onSave = { fast ->
                                repository.saveFast(fast)
                                showDialog = false
                            },
                            onDismiss = {
                                showDialog = false
                            }
                        )
                    }
                }
                
                FastTrackApp()
            }
        }
    }
    
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }
    
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }
    
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }
}

@Composable
fun FastTrackApp() {
    var currentScreen by remember { mutableStateOf("main") }
    
    // Observe lifecycle events
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    Log.d("FastTrackApp", "Lifecycle ON_STOP")
                }
                Lifecycle.Event.ON_DESTROY -> {
                    Log.d("FastTrackApp", "Lifecycle ON_DESTROY")
                }
                else -> {}
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    when (currentScreen) {
        "main" -> MainScreen(
            onNavigateToLog = { currentScreen = "log" },
            onNavigateToSettings = { currentScreen = "settings" },
            onNavigateToHelp = { currentScreen = "help" }
        )
        "log" -> FastingLogScreen(
            onBackPressed = { currentScreen = "main" }
        )
        "settings" -> SettingsScreen(
            onBackPressed = { currentScreen = "main" }
        )
        "help" -> HelpScreen(
            onBackPressed = { currentScreen = "main" }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToLog: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHelp: () -> Unit
) {
    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "FastTrack",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // App description
                Text(
                    text = "Track your fasting progress and health benefits",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // Fasting log button with icon (moved to top for easier access)
                Button(
                    onClick = onNavigateToLog,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = null
                        )
                        Text("View Fasting Log")
                    }
                }
                
                // Fasting timer button
                FastingTimerButton()
                
                // Fasting legend
                FastingLegend()
                
                // Help button
                Button(
                    onClick = onNavigateToHelp,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null
                        )
                        Text("How to Use FastTrack")
                    }
                }
            }
        }
    }
}