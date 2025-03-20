package wesseling.io.fasttime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import wesseling.io.fasttime.R
import wesseling.io.fasttime.model.FastingState
import wesseling.io.fasttime.ui.theme.NotFastingGray
import wesseling.io.fasttime.ui.theme.EarlyFastingYellow
import wesseling.io.fasttime.ui.theme.GlycogenDepletionOrange
import wesseling.io.fasttime.ui.theme.MetabolicShiftBlue
import wesseling.io.fasttime.ui.theme.DeepKetosisGreen
import wesseling.io.fasttime.ui.theme.ImmuneResetPurple
import wesseling.io.fasttime.ui.theme.ExtendedFastMagenta

/**
 * A component that displays a legend explaining the different fasting states,
 * their time thresholds, and associated colors.
 */
@Composable
fun FastingLegend(
    modifier: Modifier = Modifier
) {
    var selectedState by remember { mutableStateOf<FastingState?>(null) }
    
    // Show the dialog if a state is selected
    selectedState?.let { state ->
        FastingStateInfoDialog(
            fastingState = state,
            onDismiss = { selectedState = null }
        )
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.help_fasting_protocols),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = stringResource(R.string.help_fasting_tips),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
            
            LegendItem(
                state = FastingState.NOT_FASTING,
                color = NotFastingGray,
                timeRange = stringResource(R.string.fasting_time_range_0_4),
                onClick = { selectedState = FastingState.NOT_FASTING }
            )
            
            LegendItem(
                state = FastingState.EARLY_FAST,
                color = EarlyFastingYellow,
                timeRange = stringResource(R.string.fasting_time_range_4_12),
                onClick = { selectedState = FastingState.EARLY_FAST }
            )
            
            LegendItem(
                state = FastingState.GLYCOGEN_DEPLETION,
                color = GlycogenDepletionOrange,
                timeRange = stringResource(R.string.fasting_time_range_12_18),
                onClick = { selectedState = FastingState.GLYCOGEN_DEPLETION }
            )
            
            LegendItem(
                state = FastingState.METABOLIC_SHIFT,
                color = MetabolicShiftBlue,
                timeRange = stringResource(R.string.fasting_time_range_18_24),
                onClick = { selectedState = FastingState.METABOLIC_SHIFT }
            )
            
            LegendItem(
                state = FastingState.DEEP_KETOSIS,
                color = DeepKetosisGreen,
                timeRange = stringResource(R.string.fasting_time_range_24_48),
                onClick = { selectedState = FastingState.DEEP_KETOSIS }
            )
            
            LegendItem(
                state = FastingState.IMMUNE_RESET,
                color = ImmuneResetPurple,
                timeRange = stringResource(R.string.fasting_time_range_48_72),
                onClick = { selectedState = FastingState.IMMUNE_RESET }
            )
            
            LegendItem(
                state = FastingState.EXTENDED_FAST,
                color = ExtendedFastMagenta,
                timeRange = stringResource(R.string.fasting_time_range_72_plus),
                onClick = { selectedState = FastingState.EXTENDED_FAST }
            )
        }
    }
}

/**
 * A single item in the fasting legend, showing a color, title, and description.
 */
@Composable
private fun LegendItem(
    state: FastingState,
    color: Color,
    timeRange: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(color)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            val nameResId = when (state) {
                FastingState.NOT_FASTING -> R.string.fasting_state_not_fasting_name
                FastingState.EARLY_FAST -> R.string.fasting_state_early_fast_name
                FastingState.GLYCOGEN_DEPLETION -> R.string.fasting_state_glycogen_depletion_name
                FastingState.METABOLIC_SHIFT -> R.string.fasting_state_metabolic_shift_name
                FastingState.DEEP_KETOSIS -> R.string.fasting_state_deep_ketosis_name
                FastingState.IMMUNE_RESET -> R.string.fasting_state_immune_reset_name
                FastingState.EXTENDED_FAST -> R.string.fasting_state_extended_fast_name
            }
            
            Text(
                text = stringResource(nameResId),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = timeRange,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 