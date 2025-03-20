package wesseling.io.fasttime.widget

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import wesseling.io.fasttime.model.FastingState
import wesseling.io.fasttime.ui.components.FastingStateInfoDialog
import wesseling.io.fasttime.ui.theme.FastTrackTheme
import wesseling.io.fasttime.util.LocaleHelper

/**
 * Activity that displays detailed information about a specific fasting state
 * This is opened when the user taps on the state pill in the widget
 */
class FastingStateInfoActivity : ComponentActivity() {
    
    companion object {
        const val EXTRA_FASTING_STATE_ORDINAL = "fasting_state_ordinal"
    }
    
    override fun attachBaseContext(base: Context) {
        // Apply the saved language settings to the base context
        val languageCode = LocaleHelper.getLanguageCode(base)
        val updatedContext = LocaleHelper.updateLocale(base, languageCode)
        super.attachBaseContext(updatedContext)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply saved language settings to ensure proper localization
        val languageCode = LocaleHelper.getLanguageCode(this)
        LocaleHelper.updateLocale(this, languageCode)
        
        // Get the fasting state ordinal from the intent extras
        val stateOrdinal = intent.getIntExtra(EXTRA_FASTING_STATE_ORDINAL, 0)
        
        // Convert the ordinal to a FastingState
        val fastingState = if (stateOrdinal >= 0 && stateOrdinal < FastingState.entries.size) {
            FastingState.entries[stateOrdinal]
        } else {
            FastingState.NOT_FASTING
        }
        
        setContent {
            FastTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FastingStateInfoDialog(
                        fastingState = fastingState,
                        onDismiss = { finish() }
                    )
                }
            }
        }
    }
} 