package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AppDatabase
import com.example.data.remote.GithubApiService
import com.example.data.repository.PortfolioRepository
import com.example.ui.home.HomeScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.DynamicPortfolioTheme
import com.example.ui.theme.toColor
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.ui.viewmodel.PortfolioViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create database, api client, repository and ViewModel
        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val apiService = GithubApiService.create()
        val repository = PortfolioRepository(database.portfolioDao(), apiService)
        
        val viewModel: PortfolioViewModel by viewModels {
            PortfolioViewModelFactory(repository)
        }

        setContent {
            val themeSettings by viewModel.themeSettings.collectAsState()

            DynamicPortfolioTheme(settings = themeSettings) {
                var selectedTab by remember { mutableStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        val primaryColor = themeSettings.primaryColorHex.toColor()
                        NavigationBar(
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == 0) Icons.Default.Badge else Icons.Outlined.Badge,
                                        contentDescription = "Portfólio"
                                    )
                                },
                                label = { Text("Portfólio") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = primaryColor,
                                    selectedTextColor = primaryColor,
                                    indicatorColor = primaryColor.copy(alpha = 0.1f)
                                ),
                                modifier = Modifier.testTag("tab_portfolio")
                            )

                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == 1) Icons.Default.Settings else Icons.Outlined.Settings,
                                        contentDescription = "Ajustes"
                                    )
                                },
                                label = { Text("Ajustes") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = primaryColor,
                                    selectedTextColor = primaryColor,
                                    indicatorColor = primaryColor.copy(alpha = 0.1f)
                                ),
                                modifier = Modifier.testTag("tab_ajustes")
                            )
                        }
                    }
                ) { innerPadding ->
                    if (selectedTab == 0) {
                        HomeScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        SettingsScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
