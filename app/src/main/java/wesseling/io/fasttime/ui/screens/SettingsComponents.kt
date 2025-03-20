package wesseling.io.fasttime.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import wesseling.io.fasttime.R
import wesseling.io.fasttime.model.DateFormat
import wesseling.io.fasttime.model.ThemePreference
import wesseling.io.fasttime.model.TimeFormat
import wesseling.io.fasttime.model.UpdateFrequency
import wesseling.io.fasttime.util.BatteryOptimizationHelper
import wesseling.io.fasttime.util.LocaleHelper

/**
 * Theme selector component for settings
 */
@Composable
fun ThemeSelector(
    currentTheme: ThemePreference,
    onThemeSelected: (ThemePreference) -> Unit
) {
    Column {
        ThemePreference.values().forEach { theme ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentTheme == theme,
                    onClick = { onThemeSelected(theme) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(theme.displayNameResId),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Date format selector component for settings
 */
@Composable
fun DateFormatSelector(
    currentDateFormat: DateFormat,
    onDateFormatSelected: (DateFormat) -> Unit
) {
    Column {
        DateFormat.values().forEach { dateFormat ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentDateFormat == dateFormat,
                    onClick = { onDateFormatSelected(dateFormat) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(dateFormat.displayNameResId),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.settings_date_format_example, dateFormat.pattern),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Time format selector component for settings
 */
@Composable
fun TimeFormatSelector(
    currentTimeFormat: TimeFormat,
    onTimeFormatSelected: (TimeFormat) -> Unit
) {
    Column {
        TimeFormat.values().forEach { timeFormat ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentTimeFormat == timeFormat,
                    onClick = { onTimeFormatSelected(timeFormat) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(timeFormat.displayNameResId),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.settings_time_format_example, timeFormat.pattern),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Notification settings component for settings
 */
@Composable
fun NotificationSettings(
    enableFastingStateNotifications: Boolean,
    onEnableFastingStateNotificationsChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Switch(
            checked = enableFastingStateNotifications,
            onCheckedChange = { onEnableFastingStateNotificationsChanged(it) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = stringResource(R.string.settings_notifications_fasting_state),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.settings_notifications_fasting_state_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Battery optimization settings component for settings
 */
@Composable
fun BatteryOptimizationSettings(
    context: Context
) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = stringResource(R.string.battery_icon_description),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.settings_category_battery),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            var isIgnoringBatteryOptimizations by remember { 
                mutableStateOf(BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)) 
            }
            
            val batteryOptimizationLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) {
                // Check the status again after returning from the settings
                isIgnoringBatteryOptimizations = BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
            }
            
            Text(
                text = stringResource(R.string.settings_battery_opt_explanation),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isIgnoringBatteryOptimizations) {
                Text(
                    text = stringResource(R.string.settings_battery_opt_disabled),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        batteryOptimizationLauncher.launch(
                            BatteryOptimizationHelper.createBatteryOptimizationSettingsIntent()
                        )
                    }
                ) {
                    Text(stringResource(R.string.settings_battery_settings))
                }
            } else {
                Text(
                    text = stringResource(R.string.settings_battery_opt_enabled),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        BatteryOptimizationHelper.createBatteryOptimizationIntent(context)?.let {
                            batteryOptimizationLauncher.launch(it)
                        }
                    }
                ) {
                    Text(stringResource(R.string.settings_disable_battery_opt))
                }
            }
        }
    }
}

/**
 * Update frequency selector component for settings
 */
@Composable
fun UpdateFrequencySelector(
    currentUpdateFrequency: UpdateFrequency,
    onUpdateFrequencySelected: (UpdateFrequency) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.settings_battery_opt_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        UpdateFrequency.values().forEach { frequency ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentUpdateFrequency == frequency,
                    onClick = { onUpdateFrequencySelected(frequency) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(frequency.displayNameResId),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * About section component for settings
 */
@Composable
fun AboutSection() {
    Column {
        Text(
            text = stringResource(R.string.settings_about_app) + " by Wingless Oracle",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.settings_about_version),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.settings_about_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Language selector component for settings
 */
@Composable
fun LanguageSelector(
    currentLanguage: LocaleHelper.Companion.AppLanguage,
    onLanguageSelected: (LocaleHelper.Companion.AppLanguage) -> Unit
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showLanguageDialog = true }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Language,
            contentDescription = stringResource(R.string.language_icon_description),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.language_selection),
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = currentLanguage.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.language_select_dialog_title)) },
            text = {
                Column {
                    LocaleHelper.Companion.AppLanguage.values().forEach { language ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLanguageSelected(language)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = language == currentLanguage,
                                onClick = {
                                    onLanguageSelected(language)
                                    showLanguageDialog = false
                                }
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(text = stringResource(R.string.language_name_format, language.displayName))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.action_dismiss))
                }
            }
        )
    }
} 