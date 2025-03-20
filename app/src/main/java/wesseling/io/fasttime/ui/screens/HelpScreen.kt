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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import wesseling.io.fasttime.R

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
    val tabs = listOf(
        stringResource(R.string.help_tab_basics),
        stringResource(R.string.help_tab_fasting_types),
        stringResource(R.string.help_tab_tips)
    )
    
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
                        text = stringResource(R.string.help_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_description)
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
            text = stringResource(R.string.help_intro),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Divider()
        
        // Individual Differences in Fasting
        HelpSection(
            title = stringResource(R.string.help_unique_journey),
            content = stringResource(R.string.help_unique_journey_content)
        )
        
        // Main App Usage
        HelpSectionWithItems(
            title = stringResource(R.string.help_using_app),
            items = listOf(
                stringResource(R.string.help_app_item_1),
                stringResource(R.string.help_app_item_2),
                stringResource(R.string.help_app_item_3),
                stringResource(R.string.help_app_item_4),
                stringResource(R.string.help_app_item_5)
            )
        )
        
        // Fasting Log Features
        HelpSectionWithItems(
            title = stringResource(R.string.help_fasting_log_features),
            items = listOf(
                stringResource(R.string.help_log_item_1),
                stringResource(R.string.help_log_item_2),
                stringResource(R.string.help_log_item_3),
                stringResource(R.string.help_log_item_4),
                stringResource(R.string.help_log_item_5),
                stringResource(R.string.help_log_item_6),
                stringResource(R.string.help_log_item_7)
            )
        )
        
        // Hydration Section
        HelpSectionWithItems(
            title = stringResource(R.string.help_hydration),
            items = listOf(
                stringResource(R.string.help_hydration_item_1),
                stringResource(R.string.help_hydration_item_2),
                stringResource(R.string.help_hydration_item_3),
                stringResource(R.string.help_hydration_item_4),
                stringResource(R.string.help_hydration_item_5)
            )
        )
        
        // Fasting States
        HelpSectionWithItems(
            title = stringResource(R.string.help_fasting_states),
            items = listOf(
                stringResource(R.string.help_states_item_1),
                stringResource(R.string.help_states_item_2),
                stringResource(R.string.help_states_item_3),
                stringResource(R.string.help_states_item_4),
                stringResource(R.string.help_states_item_5),
                stringResource(R.string.help_states_item_6),
                stringResource(R.string.help_states_item_7)
            )
        )
        
        // Add Achievement Tracking information section
        HelpSection(
            title = stringResource(R.string.help_achievement),
            content = stringResource(R.string.help_achievement_content)
        )
        
        // Add Weekly Deep Fast Achievement section
        HelpSection(
            title = stringResource(R.string.help_weekly),
            content = stringResource(R.string.help_weekly_content)
        )
        
        // Widget Usage
        HelpSectionWithItems(
            title = stringResource(R.string.help_widget),
            items = listOf(
                stringResource(R.string.help_widget_item_1),
                stringResource(R.string.help_widget_item_2),
                stringResource(R.string.help_widget_item_3),
                stringResource(R.string.help_widget_item_4),
                stringResource(R.string.help_widget_item_5),
                stringResource(R.string.help_widget_item_6),
                stringResource(R.string.help_widget_item_7)
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
            text = stringResource(R.string.help_fasting_protocols),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Divider()
        
        // Detailed Introduction
        HelpSection(
            title = stringResource(R.string.help_ideal_protocol),
            content = stringResource(R.string.help_ideal_protocol_content)
        )
        
        // 16:8 Method
        HelpSectionWithItems(
            title = stringResource(R.string.help_16_8),
            items = listOf(
                stringResource(R.string.help_16_8_item_1),
                stringResource(R.string.help_16_8_item_2),
                stringResource(R.string.help_16_8_item_3),
                stringResource(R.string.help_16_8_item_4),
                stringResource(R.string.help_16_8_item_5)
            )
        )
        
        // 18:6 Method
        HelpSectionWithItems(
            title = stringResource(R.string.help_18_6),
            items = listOf(
                stringResource(R.string.help_18_6_item_1),
                stringResource(R.string.help_18_6_item_2),
                stringResource(R.string.help_18_6_item_3),
                stringResource(R.string.help_18_6_item_4),
                stringResource(R.string.help_18_6_item_5)
            )
        )
        
        // 20:4 Method (Warrior Diet)
        HelpSectionWithItems(
            title = stringResource(R.string.help_20_4),
            items = listOf(
                stringResource(R.string.help_20_4_item_1),
                stringResource(R.string.help_20_4_item_2),
                stringResource(R.string.help_20_4_item_3),
                stringResource(R.string.help_20_4_item_4),
                stringResource(R.string.help_20_4_item_5)
            )
        )
        
        // OMAD (One Meal A Day)
        HelpSectionWithItems(
            title = stringResource(R.string.help_omad),
            items = listOf(
                stringResource(R.string.help_omad_item_1),
                stringResource(R.string.help_omad_item_2),
                stringResource(R.string.help_omad_item_3),
                stringResource(R.string.help_omad_item_4),
                stringResource(R.string.help_omad_item_5)
            )
        )
        
        // 5:2 Diet
        HelpSectionWithItems(
            title = stringResource(R.string.help_5_2),
            items = listOf(
                stringResource(R.string.help_5_2_item_1),
                stringResource(R.string.help_5_2_item_2),
                stringResource(R.string.help_5_2_item_3),
                stringResource(R.string.help_5_2_item_4),
                stringResource(R.string.help_5_2_item_5)
            )
        )
        
        // Alternate Day Fasting
        HelpSectionWithItems(
            title = stringResource(R.string.help_alternate),
            items = listOf(
                stringResource(R.string.help_alternate_item_1),
                stringResource(R.string.help_alternate_item_2),
                stringResource(R.string.help_alternate_item_3),
                stringResource(R.string.help_alternate_item_4),
                stringResource(R.string.help_alternate_item_5)
            )
        )
        
        // Extended Fasting
        HelpSectionWithItems(
            title = stringResource(R.string.help_extended),
            items = listOf(
                stringResource(R.string.help_extended_item_1),
                stringResource(R.string.help_extended_item_2),
                stringResource(R.string.help_extended_item_3),
                stringResource(R.string.help_extended_item_4),
                stringResource(R.string.help_extended_item_5),
                stringResource(R.string.help_extended_item_6)
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
            text = stringResource(R.string.help_fasting_tips),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Divider()
        
        // Battery Optimization & Energy Efficiency
        HelpSectionWithItems(
            title = stringResource(R.string.help_battery),
            items = listOf(
                stringResource(R.string.help_battery_item_1),
                stringResource(R.string.help_battery_item_2),
                stringResource(R.string.help_battery_item_3),
                stringResource(R.string.help_battery_item_4),
                stringResource(R.string.help_battery_item_5)
            )
        )
        
        // Getting Started
        HelpSectionWithItems(
            title = stringResource(R.string.help_getting_started),
            items = listOf(
                stringResource(R.string.help_getting_started_item_1),
                stringResource(R.string.help_getting_started_item_2),
                stringResource(R.string.help_getting_started_item_3),
                stringResource(R.string.help_getting_started_item_4),
                stringResource(R.string.help_getting_started_item_5)
            )
        )
        
        // Using the Fasting Log
        HelpSectionWithItems(
            title = stringResource(R.string.help_using_log),
            items = listOf(
                stringResource(R.string.help_using_log_item_1),
                stringResource(R.string.help_using_log_item_2),
                stringResource(R.string.help_using_log_item_3),
                stringResource(R.string.help_using_log_item_4),
                stringResource(R.string.help_using_log_item_5)
            )
        )
        
        // Electrolyte Management
        HelpSectionWithItems(
            title = stringResource(R.string.help_electrolytes),
            items = listOf(
                stringResource(R.string.help_electrolytes_item_1),
                stringResource(R.string.help_electrolytes_item_2),
                stringResource(R.string.help_electrolytes_item_3),
                stringResource(R.string.help_electrolytes_item_4),
                stringResource(R.string.help_electrolytes_item_5),
                stringResource(R.string.help_electrolytes_item_6)
            )
        )
        
        // Hydration
        HelpSectionWithItems(
            title = stringResource(R.string.help_stay_hydrated),
            items = listOf(
                stringResource(R.string.help_stay_hydrated_item_1),
                stringResource(R.string.help_stay_hydrated_item_2),
                stringResource(R.string.help_stay_hydrated_item_3),
                stringResource(R.string.help_stay_hydrated_item_4),
                stringResource(R.string.help_stay_hydrated_item_5)
            )
        )
        
        // Managing Hunger
        HelpSectionWithItems(
            title = stringResource(R.string.help_hunger),
            items = listOf(
                stringResource(R.string.help_hunger_item_1),
                stringResource(R.string.help_hunger_item_2),
                stringResource(R.string.help_hunger_item_3),
                stringResource(R.string.help_hunger_item_4),
                stringResource(R.string.help_hunger_item_5)
            )
        )
        
        // Breaking Your Fast
        HelpSectionWithItems(
            title = stringResource(R.string.help_breaking_fast),
            items = listOf(
                stringResource(R.string.help_breaking_item_1),
                stringResource(R.string.help_breaking_item_2),
                stringResource(R.string.help_breaking_item_3),
                stringResource(R.string.help_breaking_item_4),
                stringResource(R.string.help_breaking_item_5),
                stringResource(R.string.help_breaking_item_6),
                stringResource(R.string.help_breaking_item_7)
            )
        )
        
        // Nutrition During Eating Windows
        HelpSectionWithItems(
            title = stringResource(R.string.help_nutrition),
            items = listOf(
                stringResource(R.string.help_nutrition_item_1),
                stringResource(R.string.help_nutrition_item_2),
                stringResource(R.string.help_nutrition_item_3),
                stringResource(R.string.help_nutrition_item_4),
                stringResource(R.string.help_nutrition_item_5)
            )
        )
        
        // When to Stop Fasting
        HelpSectionWithItems(
            title = stringResource(R.string.help_when_stop),
            items = listOf(
                stringResource(R.string.help_when_stop_item_1),
                stringResource(R.string.help_when_stop_item_2),
                stringResource(R.string.help_when_stop_item_3),
                stringResource(R.string.help_when_stop_item_4),
                stringResource(R.string.help_when_stop_item_5)
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Disclaimer
        Text(
            text = stringResource(R.string.help_disclaimer),
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