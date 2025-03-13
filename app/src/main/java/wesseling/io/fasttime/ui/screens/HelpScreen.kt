@file:Suppress("DEPRECATION")

package wesseling.io.fasttime.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

/**
 * Help screen that displays information about how to use the app.
 * 
 * Note: This screen currently uses the deprecated Accompanist Pager library.
 * We're keeping it for now because the new Compose Pager API is still in alpha
 * and has compatibility issues. We'll migrate to the official API once it's stable.
 * 
 * File-level @Suppress("DEPRECATION") is used to silence warnings about the deprecated API.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onBackPressed: () -> Unit
) {
    val tabs = listOf("Basics", "Fasting Types", "Tips")
    
    // Using Accompanist Pager
    @OptIn(ExperimentalPagerApi::class)
    val pagerState = rememberPagerState(initialPage = 0)
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text("How to Use FastTrack") },
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
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }
            }
            
            // Using Accompanist Pager
            @OptIn(ExperimentalPagerApi::class)
            HorizontalPager(
                count = tabs.size,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> BasicsTab()
                    1 -> FastingTypesTab()
                    2 -> TipsTab()
                }
            }
        }
    }
}

@Composable
fun BasicsTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Introduction
        Text(
            text = "FastTrack helps you track your fasting time with precision",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Divider()
        
        // Main App Usage
        HelpSection(
            title = "Using the Main App",
            items = listOf(
                "Tap START to begin your fasting timer",
                "The app tracks your fasting duration automatically",
                "Your fasting state changes based on duration",
                "Tap RESET to end your fast and save it to your log",
                "View your fasting history in the Fasting Log"
            )
        )
        
        // Hydration Section
        HelpSection(
            title = "Importance of Hydration",
            items = listOf(
                "Fasting concerns food, not water - stay hydrated!",
                "Drink plenty of water throughout your fast",
                "Hydration helps manage hunger and maintain energy",
                "Consider electrolytes during longer fasts",
                "Black coffee and unsweetened tea are also permitted"
            )
        )
        
        // Fasting States
        HelpSection(
            title = "Understanding Fasting States",
            items = listOf(
                "Fed State (Gray): 0-4 hours, digestion & absorption",
                "Early Fasting (Yellow): 4-12 hours, fat burning begins",
                "Glycogen Depletion (Orange): 12-18 hours, fat metabolism increases",
                "Metabolic Shift (Blue): 18-24 hours, ketosis begins",
                "Deep Ketosis (Green): 24-48 hours, autophagy peaks",
                "Immune Reset (Purple): 48-72 hours, stem cell production",
                "Extended Fast (Magenta): 72+ hours, cellular rejuvenation"
            )
        )
        
        // Widget Usage
        HelpSection(
            title = "Using the Home Screen Widget",
            items = listOf(
                "Add widget: Long-press home screen → Widgets → FastTrack",
                "Tap START to begin a fast from your home screen",
                "Tap RESET to end your current fast",
                "Tap HOURS display when fasting to adjust your start time",
                "Tap HOURS display when not fasting to open the main app",
                "Widget color changes based on your fasting state",
                "Green border appears when a fast is in progress"
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun FastingTypesTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Popular Fasting Protocols",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "FastTrack tracks the time you spend fasting. Here are some popular protocols:",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        // 16:8 Protocol
        HelpSection(
            title = "16:8 (Leangains)",
            items = listOf(
                "Schedule: 16 hours fasting, 8 hours eating",
                "Most popular and beginner-friendly approach",
                "Easier to maintain, fits into most lifestyles",
                "Best for beginners and daily routine",
                "Try eating between 12pm-8pm or 10am-6pm"
            )
        )
        
        // 18:6 Protocol
        HelpSection(
            title = "18:6",
            items = listOf(
                "Schedule: 18 hours fasting, 6 hours eating",
                "Popular intermediate protocol",
                "Reaches Glycogen Depletion and begins Metabolic Shift",
                "Best for those who have mastered 16:8",
                "Try eating between 12pm-6pm or 1pm-7pm"
            )
        )
        
        // 20:4 Protocol
        HelpSection(
            title = "20:4 (Warrior Diet)",
            items = listOf(
                "Schedule: 20 hours fasting, 4 hours eating",
                "Popular among experienced fasters",
                "Fully enters Metabolic Shift stage",
                "Best for experienced fasters seeking results",
                "Try one large meal with small snacks"
            )
        )
        
        // OMAD Protocol
        HelpSection(
            title = "OMAD (One Meal A Day)",
            items = listOf(
                "Schedule: 23 hours fasting, 1 hour eating",
                "Growing in popularity among advanced fasters",
                "Approaches Deep Ketosis stage with increased autophagy",
                "Best for experienced fasters",
                "Ensure your one meal is nutritionally complete"
            )
        )
        
        // 5:2 Protocol
        HelpSection(
            title = "5:2 (The Fast Diet)",
            items = listOf(
                "5 days normal eating, 2 days restricted calories",
                "Popular for those who don't want daily fasting",
                "Offers flexibility and psychological ease",
                "Best for those who find daily fasting challenging",
                "Try Monday and Thursday as fasting days"
            )
        )
        
        // Extended Fasting
        HelpSection(
            title = "Extended Fasting (36-72+ hours)",
            items = listOf(
                "Fasting for 36-72+ hours, done occasionally",
                "Practiced by advanced fasters",
                "Reaches Deep Ketosis and potentially Immune Reset stages",
                "Promotes autophagy, stem cell production, and cellular renewal",
                "Only for experienced fasters with medical supervision"
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Disclaimer
        Text(
            text = "Always consult with a healthcare professional before starting any fasting regimen",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun TipsTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Tips for Your Fasting Journey",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        // For Beginners
        HelpSection(
            title = "For Beginners",
            items = listOf(
                "Start with a 12:12 schedule and gradually extend",
                "Stay hydrated with water, black coffee, or tea",
                "Plan nutritious, balanced meals for eating windows",
                "Be patient as your body adapts to fasting",
                "Use FastTrack to track how you feel at different durations",
                "Don't stress if you need to break your fast early",
                "Align your eating window with your social schedule"
            )
        )
        
        // For Intermediate Fasters
        HelpSection(
            title = "For Intermediate Fasters",
            items = listOf(
                "Try various fasting schedules to find what works best",
                "Consider light exercise during fasted states",
                "Focus on nutrient-dense foods during eating windows",
                "Adjust your schedule based on your energy needs",
                "Consider electrolytes for fasts longer than 18 hours",
                "Use the fasting log to identify patterns",
                "Gradually work up to longer fasts if that's your goal"
            )
        )
        
        // For Advanced Fasters
        HelpSection(
            title = "For Advanced Fasters",
            items = listOf(
                "Try 24-72 hour fasts occasionally",
                "Focus on nutrient density during refeeding periods",
                "Monitor health markers with regular check-ups",
                "Listen to your body - know when to push and when to rest",
                "Integrate fasting with exercise and stress management",
                "Remember your fasting journey is unique to you",
                "Consider working with healthcare providers knowledgeable about fasting"
            )
        )
        
        // Common Challenges
        HelpSection(
            title = "Overcoming Common Challenges",
            items = listOf(
                "Hunger pangs: Try drinking water or herbal tea",
                "Low energy: Schedule demanding activities during eating windows",
                "Social events: Consider adjusting your fasting window for special occasions",
                "Plateaus: Try changing your fasting schedule or duration",
                "Cravings: Distract yourself with activities or light exercise",
                "Headaches: Ensure proper hydration and electrolyte balance",
                "Difficulty sleeping: Consider adjusting your eating window to end earlier"
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Disclaimer
        Text(
            text = "Always consult with a healthcare professional before starting any fasting regimen",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun HelpSection(
    title: String,
    items: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            
            items.forEach { item ->
                Text(
                    text = "• $item",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
} 