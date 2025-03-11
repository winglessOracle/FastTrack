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
import androidx.compose.material3.Divider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import wesseling.io.fasttime.model.FastingState
import wesseling.io.fasttime.ui.theme.AutophagyGreen
import wesseling.io.fasttime.ui.theme.DeepFastingPurple
import wesseling.io.fasttime.ui.theme.EarlyFastingYellow
import wesseling.io.fasttime.ui.theme.KetosisBlue
import wesseling.io.fasttime.ui.theme.NotFastingGray

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
                text = "Fasting Stages",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Tap on any stage to learn more about its scientific benefits",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Divider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
            
            LegendItem(
                state = FastingState.NOT_FASTING,
                color = NotFastingGray,
                timeRange = "0-5 hours",
                onClick = { selectedState = FastingState.NOT_FASTING }
            )
            
            LegendItem(
                state = FastingState.EARLY_FAST,
                color = EarlyFastingYellow,
                timeRange = "5-13 hours",
                onClick = { selectedState = FastingState.EARLY_FAST }
            )
            
            LegendItem(
                state = FastingState.KETOSIS,
                color = KetosisBlue,
                timeRange = "13-17 hours",
                onClick = { selectedState = FastingState.KETOSIS }
            )
            
            LegendItem(
                state = FastingState.AUTOPHAGY,
                color = AutophagyGreen,
                timeRange = "17-25 hours",
                onClick = { selectedState = FastingState.AUTOPHAGY }
            )
            
            LegendItem(
                state = FastingState.DEEP_FASTING,
                color = DeepFastingPurple,
                timeRange = "25+ hours",
                onClick = { selectedState = FastingState.DEEP_FASTING }
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
            Text(
                text = state.description,
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