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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AppDatabase
import com.example.data.remote.GithubApiService
import com.example.data.repository.PortfolioRepository
import com.example.ui.home.HomeScreen
import com.example.ui.coach.ResumeCoachScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.DynamicPortfolioTheme
import com.example.ui.theme.toColor
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.ui.viewmodel.PortfolioViewModelFactory
import com.example.ui.onboarding.OnboardingDialog
import android.content.Context

// Additional profile/authentication imports
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import coil.compose.AsyncImage
import com.example.BuildConfig
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.profile.ProfileDialog

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create database, api client, repository and ViewModel
        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val apiService = GithubApiService.create()
        val repository = PortfolioRepository(database.portfolioDao(), apiService)
        val firebaseSyncManager = com.example.data.remote.FirebaseSyncManager(applicationContext)
        
        val viewModel: PortfolioViewModel by viewModels {
            PortfolioViewModelFactory(repository, firebaseSyncManager)
        }

        setContent {
            val themeSettings by viewModel.themeSettings.collectAsState()
            val currentUser by viewModel.firebaseSyncManager.currentUser.collectAsState()
            val syncState by viewModel.syncState.collectAsState()
            val context = LocalContext.current

            var showProfileDialog by remember { mutableStateOf(false) }
            var showSimulatedLogin by remember { mutableStateOf(false) }

            val sharedPrefs = remember { context.getSharedPreferences("portfolio_sync_prefs", Context.MODE_PRIVATE) }
            var hasShownOnboarding by remember {
                mutableStateOf(sharedPrefs.getBoolean("onboarding_shown", false))
            }
            var showOnboardingDialog by remember {
                mutableStateOf(currentUser == null && !hasShownOnboarding)
            }

            val googleSignInLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    val idToken = account.idToken
                    if (idToken != null) {
                        viewModel.signInWithGoogle(idToken)
                    } else {
                        Toast.makeText(context, "Não foi possível obter a credencial do Google.", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Erro no Google Sign-In: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }

            DynamicPortfolioTheme(settings = themeSettings) {
                var selectedTab by remember { mutableStateOf(0) }
                val primaryColor = themeSettings.primaryColorHex.toColor()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
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
                                        imageVector = if (selectedTab == 1) Icons.Default.Star else Icons.Outlined.Star,
                                        contentDescription = "Melhorias"
                                    )
                                },
                                label = { Text("Melhorias") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = primaryColor,
                                    selectedTextColor = primaryColor,
                                    indicatorColor = primaryColor.copy(alpha = 0.1f)
                                ),
                                modifier = Modifier.testTag("tab_melhorias")
                            )

                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == 2) Icons.Default.Settings else Icons.Outlined.Settings,
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

                            NavigationBarItem(
                                selected = false,
                                onClick = { showProfileDialog = true },
                                icon = {
                                    val user = currentUser
                                    if (user != null) {
                                        if (!user.photoUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = user.photoUrl,
                                                contentDescription = "Foto de perfil",
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(primaryColor.copy(alpha = 0.1f))
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(primaryColor),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = (user.displayName ?: "U").take(1).uppercase(),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = "Perfil"
                                        )
                                    }
                                },
                                label = { Text("Perfil") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = primaryColor,
                                    selectedTextColor = primaryColor,
                                    indicatorColor = primaryColor.copy(alpha = 0.1f)
                                ),
                                modifier = Modifier.testTag("tab_perfil")
                            )
                        }
                    }
                ) { innerPadding ->
                    when (selectedTab) {
                        0 -> {
                            HomeScreen(
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        1 -> {
                            ResumeCoachScreen(
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        else -> {
                            SettingsScreen(
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }

            val triggerGoogleSignIn = {
                val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                val rIdStr = if (resId != 0) context.getString(resId) else ""
                val webClientId = if (!rIdStr.isNullOrBlank() && rIdStr != "YOUR_GOOGLE_WEB_CLIENT_ID") {
                    rIdStr
                } else {
                    BuildConfig.GOOGLE_WEB_CLIENT_ID
                }

                if (webClientId.isBlank() || webClientId == "YOUR_GOOGLE_WEB_CLIENT_ID") {
                    Toast.makeText(
                        context,
                        "Web Client ID do Google não configurado no .env ou no google-services.json! Use a Conta de Teste (Simulada) para testar.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    try {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(webClientId)
                            .requestEmail()
                            .build()
                        val googleSignInClient = GoogleSignIn.getClient(context, gso)
                        googleSignInClient.signOut()
                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Erro ao iniciar Google Sign-In: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }

            // Dialogs
            if (showOnboardingDialog) {
                OnboardingDialog(
                    onDismiss = { showOnboardingDialog = false },
                    viewModel = viewModel,
                    onGoogleSignInClick = triggerGoogleSignIn,
                    onSimulatedSignInClick = { showSimulatedLogin = true }
                )
            }

            if (showProfileDialog) {
                ProfileDialog(
                    onDismiss = { showProfileDialog = false },
                    viewModel = viewModel,
                    onGoogleSignInClick = triggerGoogleSignIn,
                    onSimulatedSignInClick = {
                        showSimulatedLogin = true
                    }
                )
            }

            if (showSimulatedLogin) {
                com.example.ui.settings.SimulatedLoginDialog(
                    onDismiss = { showSimulatedLogin = false },
                    onConfirm = { email, name ->
                        showSimulatedLogin = false
                        viewModel.signInSimulated(email, name)
                    },
                    primaryColor = themeSettings.primaryColorHex.toColor()
                )
            }
        }
    }
}
