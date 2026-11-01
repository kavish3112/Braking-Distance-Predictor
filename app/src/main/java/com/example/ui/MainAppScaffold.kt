package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.screens.HistoryAndPresetsScreen
import com.example.ui.screens.PredictorScreen
import com.example.ui.screens.ScenarioCompareScreen
import com.example.ui.screens.VisualizerGraphScreen
import com.example.ui.viewmodel.BrakingViewModel

enum class AppTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    PREDICTOR("Predictor", Icons.Filled.DirectionsCar, Icons.Outlined.DirectionsCar, "nav_predictor"),
    VISUALIZER("Curves", Icons.Filled.AutoGraph, Icons.Outlined.AutoGraph, "nav_visualizer"),
    COMPARE("Compare", Icons.Filled.CompareArrows, Icons.Outlined.CompareArrows, "nav_compare"),
    HISTORY("History", Icons.Filled.History, Icons.Outlined.History, "nav_history")
}

@Composable
fun MainAppScaffold(
    viewModel: BrakingViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                AppTab.entries.forEachIndexed { index, tab ->
                    val isSelected = selectedTabIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(text = tab.label) },
                        modifier = Modifier.testTag(tab.testTag),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTabIndex) {
                0 -> PredictorScreen(
                    viewModel = viewModel,
                    onNavigateToCompare = { selectedTabIndex = 2 }
                )
                1 -> VisualizerGraphScreen(viewModel = viewModel)
                2 -> ScenarioCompareScreen(viewModel = viewModel)
                3 -> HistoryAndPresetsScreen(
                    viewModel = viewModel,
                    onLoadedToPredictor = { selectedTabIndex = 0 }
                )
            }
        }
    }
}
