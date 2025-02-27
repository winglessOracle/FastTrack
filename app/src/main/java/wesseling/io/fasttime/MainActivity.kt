package wesseling.io.fasttime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import wesseling.io.fasttime.ui.components.FastingLegend
import wesseling.io.fasttime.ui.components.FastingTimerButton
import wesseling.io.fasttime.ui.screens.FastingLogScreen
import wesseling.io.fasttime.ui.screens.SettingsScreen
import wesseling.io.fasttime.ui.theme.FastTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            FastTrackTheme {
                FastTimeApp()
            }
        }
    }
}

@Composable
fun FastTimeApp() {
    var currentScreen by remember { mutableStateOf("main") }
    
    when (currentScreen) {
        "main" -> MainScreen(
            onNavigateToLog = { currentScreen = "log" },
            onNavigateToSettings = { currentScreen = "settings" }
        )
        "log" -> FastingLogScreen(
            onBackPressed = { currentScreen = "main" }
        )
        "settings" -> SettingsScreen(
            onBackPressed = { currentScreen = "main" }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToLog: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
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
                    IconButton(onClick = onNavigateToLog) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = "Fasting Log"
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // App description
                Text(
                    text = "Track your fasting progress and health benefits",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Fasting timer button
                FastingTimerButton()
                
                // Fasting legend
                FastingLegend()
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Fasting log button
                Button(
                    onClick = onNavigateToLog,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("View Fasting Log")
                }
                
                // Settings button
                Button(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Settings")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}