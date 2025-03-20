package wesseling.io.fasttime.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import wesseling.io.fasttime.R
import wesseling.io.fasttime.model.DateFormat
import wesseling.io.fasttime.model.ThemePreference
import wesseling.io.fasttime.model.TimeFormat
import wesseling.io.fasttime.model.UpdateFrequency
import wesseling.io.fasttime.settings.PreferencesManager
import wesseling.io.fasttime.util.BatteryOptimizationHelper
import wesseling.io.fasttime.util.LocaleHelper
import androidx.compose.foundation.clickable

/**
 * Settings screen for the app
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager.getInstance(context) }
    val preferences by remember { 
        mutableStateOf(preferencesManager.dateTimePreferences) 
    }.apply {
        this.value = preferencesManager.dateTimePreferences
    }
    
    // Current language preference
    val currentLanguage = remember { 
        mutableStateOf(LocaleHelper.getCurrentLanguage(context)) 
    }
    
    // Remember if we need to show a restart dialog after language change
    var showRestartDialog by remember { mutableStateOf(false) }
    
    // Remember selected language for dialog
    var selectedLanguage by remember { mutableStateOf(currentLanguage.value) }
    
    // Permission request launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, enable notifications
            preferencesManager.toggleFastingStateNotifications(true)
        } else {
            // Permission denied, keep notifications disabled
            preferencesManager.toggleFastingStateNotifications(false)
        }
    }
    
    // Function to check and request notification permission
    val checkAndRequestNotificationPermission: (Boolean) -> Unit = { enable ->
        if (enable) {
            // Runtime permission for notifications is only required on Android 13+ (API 33)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                when {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        // Permission already granted, enable notifications
                        preferencesManager.toggleFastingStateNotifications(true)
                    }
                    else -> {
                        // Request permission
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            } else {
                // For Android 12 and below, no runtime permission needed for notifications
                preferencesManager.toggleFastingStateNotifications(true)
            }
        } else {
            // Simply disable notifications
            preferencesManager.toggleFastingStateNotifications(false)
        }
    }
    
    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button_description))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Theme section
            SectionTitle(stringResource(R.string.settings_category_theme))
            ThemeSelector(
                currentTheme = preferences.themePreference,
                onThemeSelected = { theme -> 
                    preferencesManager.updateTheme(theme)
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Language section
            SectionTitle(stringResource(R.string.settings_category_language))
            LanguageSelector(
                currentLanguage = currentLanguage.value,
                onLanguageSelected = { language ->
                    selectedLanguage = language
                    showRestartDialog = true
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Date format section
            SectionTitle(stringResource(R.string.settings_category_date_format))
            DateFormatSelector(
                currentDateFormat = preferences.dateFormat,
                onDateFormatSelected = { dateFormat ->
                    preferencesManager.updateDateFormat(dateFormat)
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Time format section
            SectionTitle(stringResource(R.string.settings_category_time_format))
            TimeFormatSelector(
                currentTimeFormat = preferences.timeFormat,
                onTimeFormatSelected = { timeFormat ->
                    preferencesManager.updateTimeFormat(timeFormat)
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Notifications section
            SectionTitle(stringResource(R.string.settings_category_notifications))
            NotificationSettings(
                enableFastingStateNotifications = preferences.enableFastingStateNotifications,
                onEnableFastingStateNotificationsChanged = { enabled ->
                    checkAndRequestNotificationPermission(enabled)
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Widget update frequency section (combined with battery optimization)
            SectionTitle(stringResource(R.string.settings_category_widget_updates))
            BatteryOptimizationSettings(context = context)
            UpdateFrequencySelector(
                currentUpdateFrequency = preferences.updateFrequency,
                onUpdateFrequencySelected = { frequency ->
                    preferencesManager.updateUpdateFrequency(frequency)
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            // About section
            SectionTitle(stringResource(R.string.settings_category_about))
            AboutSection()
            
            // Add some space at the bottom
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Show restart dialog if needed
    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text(stringResource(R.string.language_dialog_title)) },
            text = { Text(stringResource(R.string.language_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    // Apply language change
                    LocaleHelper.setLanguageCode(context, selectedLanguage.code)
                    val updatedContext = LocaleHelper.updateLocale(context, selectedLanguage.code)
                    
                    // Show toast with the new context to ensure it uses the new locale
                    Toast.makeText(
                        updatedContext, 
                        updatedContext.getString(R.string.language_changed_message, selectedLanguage.displayName), 
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Hide dialog
                    showRestartDialog = false
                    
                    // Force a complete app restart by restarting the main activity
                    val packageManager = context.packageManager
                    val intent = packageManager.getLaunchIntentForPackage(context.packageName)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK or 
                                        Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        
                        // Use finishAffinity to close all activities in the task
                        if (context is androidx.activity.ComponentActivity) {
                            context.finishAffinity()
                        }
                    }
                }) {
                    Text(stringResource(R.string.language_dialog_continue))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showRestartDialog = false 
                    selectedLanguage = currentLanguage.value  // Reset selection
                }) {
                    Text(stringResource(R.string.language_dialog_cancel))
                }
            }
        )
    }
}

/**
 * Section title component for settings
 */
@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
} 