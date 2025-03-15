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
                title = { 
                    Text(
                        text = "Help & Information",
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
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
        
        // Individual Differences in Fasting
        HelpSection(
            title = "Everyone's Fasting Journey is Unique",
            content = "Fasting affects each person differently based on metabolism, body composition, activity level, and health history. What works for one person may not work for another. Listen to your body and adjust your fasting approach accordingly. Start slowly and gradually increase your fasting duration as your body adapts. Remember that consistency is more important than intensity, especially when beginning your fasting journey."
        )
        
        // Main App Usage
        HelpSectionWithItems(
            title = "Using the Main App",
            items = listOf(
                "Tap START to begin your fasting timer",
                "The app tracks your fasting duration automatically",
                "Your fasting state changes based on duration",
                "Tap RESET to end your fast and save it to your log",
                "View your fasting history in the Fasting Log"
            )
        )
        
        // Fasting Log Features
        HelpSectionWithItems(
            title = "Fasting Log Features",
            items = listOf(
                "View a summary of your fasting achievements",
                "Sort entries by date (newest/oldest first)",
                "Sort entries by duration (longest/shortest first)",
                "Filter entries by fasting state to find specific achievements",
                "Tap on an entry to view detailed information",
                "Edit or delete entries as needed"
            )
        )
        
        // Hydration Section
        HelpSectionWithItems(
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
        HelpSectionWithItems(
            title = "Understanding Fasting States",
            items = listOf(
                "Fed State (Gray): 0-4 hours, digestion & absorption",
                "Early Fasting (Yellow): 4-12 hours, fat burning begins",
                "Glycogen Depletion (Orange): 12-18 hours, fat metabolism increases (minimum for achievements)",
                "Metabolic Shift (Blue): 18-24 hours, ketosis begins",
                "Deep Ketosis (Green): 24-48 hours, autophagy peaks",
                "Immune Reset (Purple): 48-72 hours, stem cell production",
                "Extended Fast (Magenta): 72+ hours, cellular rejuvenation"
            )
        )
        
        // Achievement Tracking
        HelpSection(
            title = "Achievement Tracking",
            content = "FastTrack only counts fasts that reach at least the Glycogen Depletion stage (12+ hours) for achievements and statistics. Shorter fasts are still tracked in your log but are not counted in your totals or achievements. This ensures that your statistics reflect meaningful fasting periods that provide metabolic benefits."
        )
        
        // Widget Usage
        HelpSectionWithItems(
            title = "Widget Features",
            items = listOf(
                "Add the FastTrack widget to your home screen",
                "Tap START to begin a fast directly from your home screen",
                "Tap RESET to end your fast",
                "Tap the hours to adjust the start time (when fasting) or open the app (when not fasting)",
                "Tap the state pill (colored text showing your current fasting state) to view detailed information about that fasting state",
                "The widget updates automatically to show your current fasting state"
            )
        )
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
        // Introduction
        Text(
            text = "Common Fasting Protocols",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Divider()
        
        // Detailed Introduction
        HelpSection(
            title = "Finding Your Ideal Fasting Protocol",
            content = "Fasting is not one-size-fits-all. Different protocols offer varying benefits and levels of difficulty. Consider your lifestyle, goals, and experience level when choosing a fasting method. You may need to experiment with several approaches before finding what works best for you. The protocols below range from beginner-friendly to advanced. Start with an easier protocol and gradually progress as your body adapts to fasting."
        )
        
        // 16:8 Method
        HelpSectionWithItems(
            title = "16:8 Method (Leangains)",
            items = listOf(
                "Fast for 16 hours, eat during an 8-hour window",
                "Popular window: 12pm to 8pm",
                "Suitable for beginners",
                "Can be done daily",
                "Good for weight management and metabolic health"
            )
        )
        
        // 18:6 Method
        HelpSectionWithItems(
            title = "18:6 Method",
            items = listOf(
                "Fast for 18 hours, eat during a 6-hour window",
                "Popular window: 2pm to 8pm",
                "Intermediate level",
                "Enhanced fat burning and ketosis",
                "May improve insulin sensitivity"
            )
        )
        
        // 20:4 Method (Warrior Diet)
        HelpSectionWithItems(
            title = "20:4 Method (Warrior Diet)",
            items = listOf(
                "Fast for 20 hours, eat during a 4-hour window",
                "Popular window: 4pm to 8pm",
                "Advanced level",
                "Significant autophagy benefits",
                "May enhance growth hormone production"
            )
        )
        
        // OMAD (One Meal A Day)
        HelpSectionWithItems(
            title = "OMAD (One Meal A Day)",
            items = listOf(
                "23:1 ratio - eat just one meal per day",
                "Advanced level",
                "Maximum autophagy benefits",
                "Significant metabolic benefits",
                "Requires careful nutritional planning"
            )
        )
        
        // 5:2 Diet
        HelpSectionWithItems(
            title = "5:2 Diet",
            items = listOf(
                "Eat normally 5 days a week",
                "Restrict calories (500-600) on 2 non-consecutive days",
                "Good for those who find daily fasting difficult",
                "Flexible scheduling",
                "Shown to improve metabolic markers"
            )
        )
        
        // Alternate Day Fasting
        HelpSectionWithItems(
            title = "Alternate Day Fasting",
            items = listOf(
                "Fast every other day",
                "Advanced level",
                "Significant weight loss potential",
                "May improve cardiovascular health",
                "Requires careful planning and adaptation"
            )
        )
        
        // Extended Fasting
        HelpSectionWithItems(
            title = "Extended Fasting",
            items = listOf(
                "Fasts lasting 24-72+ hours",
                "Expert level - not for beginners",
                "Maximum autophagy and cellular rejuvenation",
                "Significant immune system reset",
                "Should be done under medical supervision",
                "Not recommended for regular practice"
            )
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
        // Introduction
        Text(
            text = "Tips for Successful Fasting",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Divider()
        
        // Getting Started
        HelpSectionWithItems(
            title = "Getting Started",
            items = listOf(
                "Start with shorter fasting windows (12-14 hours)",
                "Gradually increase your fasting duration",
                "Be consistent with your fasting schedule",
                "Track your progress with FastTrack",
                "Listen to your body and adjust as needed"
            )
        )
        
        // Using the Fasting Log
        HelpSectionWithItems(
            title = "Using the Fasting Log Effectively",
            items = listOf(
                "Sort by date to track your recent progress",
                "Sort by duration to identify your longest fasts",
                "Filter by fasting state to see how often you reach specific milestones",
                "Use the summary statistics to understand your overall progress",
                "Review your log regularly to identify patterns and improvements"
            )
        )
        
        // Electrolyte Management - New detailed section
        HelpSectionWithItems(
            title = "Electrolyte Management",
            items = listOf(
                "Sodium: Add a pinch of sea salt to water (1/4 tsp in 1L water)",
                "Potassium: Consider potassium salt substitutes for fasts >24 hours",
                "Magnesium: Magnesium glycinate or citrate supplements can help with muscle cramps",
                "Balanced electrolyte mix: 1/2 tsp salt, 1/4 tsp potassium salt in 1L water",
                "Signs of electrolyte imbalance: headaches, dizziness, muscle cramps",
                "For extended fasts (>48h), consider commercial electrolyte supplements"
            )
        )
        
        // Hydration - Keep existing section
        HelpSectionWithItems(
            title = "Stay Hydrated",
            items = listOf(
                "Drink plenty of water throughout your fast",
                "Add electrolytes for longer fasts (24+ hours)",
                "Black coffee and unsweetened tea are permitted",
                "Avoid artificial sweeteners during fasting",
                "Carbonated water can help with hunger pangs"
            )
        )
        
        // Managing Hunger
        HelpSectionWithItems(
            title = "Managing Hunger",
            items = listOf(
                "Hunger comes in waves and typically passes",
                "Stay busy during peak hunger times",
                "Drink water when hunger strikes",
                "Light exercise can reduce hunger",
                "Remember: hunger is not an emergency"
            )
        )
        
        // Breaking Your Fast - Enhanced with more details
        HelpSectionWithItems(
            title = "Breaking Your Fast Properly",
            items = listOf(
                "For fasts <24h: Start with easily digestible foods like bone broth, avocado, or eggs",
                "For fasts >24h: Begin with bone broth, then wait 30-60 minutes before eating solid food",
                "For fasts >48h: Start with clear broth, wait 1 hour, then small protein/fat meal, wait 2-3 hours before normal eating",
                "Avoid breaking fast with: processed carbs, large meals, dairy, nuts, or raw vegetables",
                "Good first foods: bone broth, soft-boiled eggs, avocado, cooked leafy greens, fermented foods",
                "Eat slowly and mindfully, chewing thoroughly",
                "Listen to your body and stop eating if you feel discomfort"
            )
        )
        
        // Nutrition During Eating Windows
        HelpSectionWithItems(
            title = "Nutrition During Eating Windows",
            items = listOf(
                "Focus on nutrient-dense whole foods",
                "Include adequate protein (0.8-1g per lb of lean body mass)",
                "Don't neglect healthy fats",
                "Include plenty of vegetables and fiber",
                "Consider your total caloric needs"
            )
        )
        
        // When to Stop Fasting
        HelpSectionWithItems(
            title = "When to Stop Fasting",
            items = listOf(
                "If you feel unwell beyond normal hunger",
                "If you experience dizziness or weakness",
                "If you have a medical condition that requires food",
                "During illness or high stress periods",
                "During pregnancy or breastfeeding"
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
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun HelpSectionWithItems(
    title: String,
    items: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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