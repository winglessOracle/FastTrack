package wesseling.io.fasttime

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import wesseling.io.fasttime.model.CompletedFast
import wesseling.io.fasttime.model.FastingState
import wesseling.io.fasttime.repository.FastingRepository
import wesseling.io.fasttime.settings.PreferencesManager
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
        const val SHOW_COMPLETED_FAST = "SHOW_COMPLETED_FAST"
        const val SHOW_FASTING_DETAILS = "SHOW_FASTING_DETAILS"
        const val START_TIME = "START_TIME"
        const val END_TIME = "END_TIME"
        const val DURATION = "DURATION"
        const val MAX_STATE = "MAX_STATE"
    }
    
    // Register the permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Notification permission granted")
            // Update the notification preference to true since permission is granted
            val preferencesManager = PreferencesManager.getInstance(this)
            if (!preferencesManager.dateTimePreferences.enableFastingStateNotifications) {
                preferencesManager.toggleFastingStateNotifications(true)
            }
        } else {
            Log.d(TAG, "Notification permission denied")
            // Update the notification preference to false since permission is denied
            val preferencesManager = PreferencesManager.getInstance(this)
            if (preferencesManager.dateTimePreferences.enableFastingStateNotifications) {
                preferencesManager.toggleFastingStateNotifications(false)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Check and request notification permission for Android 13+ (API 33+)
        checkAndRequestNotificationPermission()
        
        var completedFast: CompletedFast? = null
        var initialScreen = "main"
        var shouldResetTimer = false
        
        // Handle intent extras
        if (intent?.getBooleanExtra(SHOW_COMPLETED_FAST, false) == true) {
            try {
                val startTime = intent.getLongExtra(START_TIME, 0)
                val endTime = intent.getLongExtra(END_TIME, 0)
                val duration = intent.getLongExtra(DURATION, 0)
                
                // Replace deprecated getSerializableExtra with the newer version
                val maxState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getSerializableExtra(MAX_STATE, FastingState::class.java) ?: FastingState.NOT_FASTING
                } else {
                    @Suppress("DEPRECATION")
                    intent.getSerializableExtra(MAX_STATE) as? FastingState ?: FastingState.NOT_FASTING
                }
                
                if (startTime > 0 && endTime > 0 && duration > 0) {
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
        } else if (intent?.getBooleanExtra(SHOW_FASTING_DETAILS, false) == true) {
            // Navigate to the fasting log screen when SHOW_FASTING_DETAILS is true
            initialScreen = "log"
            Log.d(TAG, "Navigating to fasting log from notification")
        } else if (intent?.action == "wesseling.io.fasttime.RESET_TIMER") {
            // Handle reset timer action from notification
            shouldResetTimer = true
            Log.d(TAG, "Reset timer action received from notification")
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
                
                // Handle timer reset if needed
                if (shouldResetTimer) {
                    DisposableEffect(Unit) {
                        val timer = FastingTimer.getInstance(this@MainActivity)
                        val resetCompletedFast = timer.resetTimer()
                        
                        // If a fast was completed, show the summary dialog
                        if (resetCompletedFast != null) {
                            val repository = FastingRepository.getInstance(this@MainActivity)
                            repository.saveFast(resetCompletedFast)
                            Log.d(TAG, "Timer reset from notification, saved completed fast")
                        } else {
                            Log.d(TAG, "Timer reset from notification, no completed fast")
                        }
                        
                        onDispose { }
                    }
                }
                
                FastTrackApp(initialScreen = initialScreen)
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
    
    /**
     * Check if notification permission is granted and request it if not
     */
    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission is already granted
                    Log.d(TAG, "Notification permission already granted")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show permission rationale if needed
                    Log.d(TAG, "Should show notification permission rationale")
                    // Request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Request permission
                    Log.d(TAG, "Requesting notification permission")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}

@Composable
fun FastTrackApp(initialScreen: String = "main") {
    var currentScreen by remember { mutableStateOf(initialScreen) }
    
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
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        .clip(RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.animateContentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "View Fasting Log",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                
                // Fasting timer button
                FastingTimerButton()
                
                // Fasting legend
                FastingLegend()
                
                // Help button
                Button(
                    onClick = onNavigateToHelp,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                        )
                        .clip(RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ),
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.animateContentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "How to Use FastTrack",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
            }
        }
    }
}