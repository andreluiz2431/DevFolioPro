package com.example.ui.coach

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entities.SkillEntity
import com.example.ui.viewmodel.ConflictResolution
import com.example.data.remote.models.RecommendedSkillSuggestion
import com.example.data.remote.models.ResumeImprovements
import com.example.ui.home.FlowRow
import com.example.ui.theme.toColor
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.ui.viewmodel.ResumeCoachUiState
import com.example.ui.viewmodel.FirebaseSyncUiState

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ResumeCoachScreen(
    viewModel: PortfolioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeSettings by viewModel.themeSettings.collectAsState()
    val primaryColor = themeSettings.primaryColorHex.toColor()
    
    val profile by viewModel.profile.collectAsState()
    val skills by viewModel.skills.collectAsState()
    val experiences by viewModel.experiences.collectAsState()
    val coachState by viewModel.resumeCoachState.collectAsState()
    
    // Target roles list
    val predefinedRoles = listOf(
        "Suporte de TI",
        "Analista de Infraestrutura",
        "Engenheiro DevOps",
        "Desenvolvedor Front-end",
        "Desenvolvedor Back-end",
        "Desenvolvedor Full-stack",
        "Desenvolvedor Mobile"
    )
    
    var selectedRole by remember { mutableStateOf("") }
    var customRoleInput by remember { mutableStateOf("") }
    var jobDescriptionInput by remember { mutableStateOf("") }
    
    // Track accepted items locally for visual feedback
    var profileAccepted by remember { mutableStateOf(false) }
    var acceptedSkills by remember { mutableStateOf(setOf<String>()) }
    var acceptedExperiences by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(coachState) {
        if (coachState is ResumeCoachUiState.Idle) {
            profileAccepted = false
            acceptedSkills = emptySet()
            acceptedExperiences = emptySet()
        }
    }

    val isUnlocked by viewModel.isCoursesFeatureUnlockedState.collectAsState()
    val testUsageCount by viewModel.testUsageCountState.collectAsState()
    val currentUser by viewModel.firebaseSyncManager.currentUser.collectAsState()

    val selectedResumeName by viewModel.selectedResumeName.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var saveAsNew by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf("") }

    LaunchedEffect(syncState) {
        if (syncState is FirebaseSyncUiState.Success) {
            Toast.makeText(context, (syncState as FirebaseSyncUiState.Success).message, Toast.LENGTH_SHORT).show()
        } else if (syncState is FirebaseSyncUiState.Error) {
            Toast.makeText(context, (syncState as FirebaseSyncUiState.Error).error, Toast.LENGTH_LONG).show()
        }
    }

    val checkoutUrl by viewModel.mercadoPagoCheckoutUrl.collectAsState()
    if (checkoutUrl != null) {
        Dialog(
            onDismissRequest = { viewModel.resetMercadoPagoCheckout() },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Title bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Checkout Seguro Mercado Pago",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        
                        IconButton(onClick = { viewModel.resetMercadoPagoCheckout() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar checkout"
                            )
                        }
                    }
                    
                    HorizontalDivider()
                    
                    // WebView loading the checkout
                    Box(modifier = Modifier.weight(1f)) {
                        androidx.compose.ui.viewinterop.AndroidView(
                            factory = { ctx ->
                                android.webkit.WebView(ctx).apply {
                                    settings.apply {
                                        javaScriptEnabled = true
                                        domStorageEnabled = true
                                        databaseEnabled = true
                                        loadWithOverviewMode = true
                                        useWideViewPort = true
                                        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                        javaScriptCanOpenWindowsAutomatically = true
                                        setSupportZoom(true)
                                        builtInZoomControls = true
                                        displayZoomControls = false
                                    }
                                    
                                    val cookieManager = android.webkit.CookieManager.getInstance()
                                    cookieManager.setAcceptCookie(true)
                                    cookieManager.setAcceptThirdPartyCookies(this, true)
                                    
                                    webChromeClient = object : android.webkit.WebChromeClient() {
                                        override fun onReceivedTitle(view: android.webkit.WebView?, title: String?) {
                                            super.onReceivedTitle(view, title)
                                            val lowerTitle = title?.lowercase() ?: ""
                                            val hasSuccessWord = lowerTitle.contains("sucesso") || 
                                                                 lowerTitle.contains("aprovado") || 
                                                                 lowerTitle.contains("pago com sucesso") || 
                                                                 lowerTitle.contains("pagamento aprovado") || 
                                                                 lowerTitle.contains("success") || 
                                                                 lowerTitle.contains("approved") || 
                                                                 lowerTitle.contains("congrats") || 
                                                                 lowerTitle.contains("concluído") || 
                                                                 lowerTitle.contains("concluido") || 
                                                                 lowerTitle.contains("parabéns") || 
                                                                 lowerTitle.contains("parabens") || 
                                                                 lowerTitle.contains("obrigado")
                                            
                                            val hasNegativeWord = lowerTitle.contains("falha") || 
                                                                  lowerTitle.contains("falhou") || 
                                                                  lowerTitle.contains("erro") || 
                                                                  lowerTitle.contains("negado") || 
                                                                  lowerTitle.contains("recusado")
                                            
                                            if (hasSuccessWord && !hasNegativeWord) {
                                                viewModel.unlockCoursesFeature()
                                                viewModel.resetMercadoPagoCheckout()
                                            }
                                        }
                                    }
                                    
                                    webViewClient = object : android.webkit.WebViewClient() {
                                        private fun checkPaymentSuccess(url: String?): Boolean {
                                            val lowerUrl = url?.lowercase() ?: ""
                                            return lowerUrl.contains("success") || 
                                                   lowerUrl.contains("approved") || 
                                                   lowerUrl.contains("pending") || 
                                                   lowerUrl.contains("congrats") || 
                                                   lowerUrl.contains("feedback") || 
                                                   lowerUrl.contains("complete") || 
                                                   lowerUrl.contains("concluido") || 
                                                   lowerUrl.contains("sucesso") || 
                                                   lowerUrl.contains("aprovado") || 
                                                   lowerUrl.contains("status=approved") || 
                                                   lowerUrl.contains("status=pending") || 
                                                   lowerUrl.contains("payment-success")
                                        }
 
                                        override fun shouldOverrideUrlLoading(view: android.webkit.WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                                            val urlString = request?.url?.toString() ?: ""
                                            if (checkPaymentSuccess(urlString)) {
                                                viewModel.unlockCoursesFeature()
                                                viewModel.resetMercadoPagoCheckout()
                                                return true
                                            }
                                            return false
                                        }
                                    }
                                    loadUrl(checkoutUrl ?: "")
                                }
                            },
                            update = { webView ->
                                // Update url if changed
                            }
                        )
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    text = "Opções de Envio à Nuvem",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Como deseja salvar as informações do currículo atual no Firebase?",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { saveAsNew = false }
                        ) {
                            RadioButton(
                                selected = !saveAsNew,
                                onClick = { saveAsNew = false }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Substituir currículo atual: '$selectedResumeName'",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { saveAsNew = true }
                        ) {
                            RadioButton(
                                selected = saveAsNew,
                                onClick = { saveAsNew = true }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Criar novo currículo (separado)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    if (saveAsNew) {
                        OutlinedTextField(
                            value = newNameInput,
                            onValueChange = { newNameInput = it },
                            label = { Text("Nome/Propósito do Currículo") },
                            placeholder = { Text("Ex: Vaga Mobile, DevOps...") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("coach_new_resume_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                focusedLabelColor = primaryColor
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (saveAsNew && newNameInput.isBlank()) {
                            Toast.makeText(context, "Por favor, digite um nome válido!", Toast.LENGTH_SHORT).show()
                        } else {
                            val targetId = if (saveAsNew) newNameInput.trim() else selectedResumeName
                            viewModel.syncWithCloud(ConflictResolution.PUSH_OVERWRITE, targetId)
                            showSaveDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    enabled = syncState !is FirebaseSyncUiState.Loading
                ) {
                    if (syncState is FirebaseSyncUiState.Loading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Salvar")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.outline)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "AI Resume Coach",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (currentUser == null) {
                // Not logged in lock screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Acesso Bloqueado: Conecte-se",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Para desbloquear o AI Resume Coach e analisar seu currículo utilizando inteligência artificial, você precisa primeiro entrar na sua conta.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Por favor, faça login ou cadastre-se na aba Serviços (Perfil / Sincronizar Nuvem).",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = primaryColor,
                        textAlign = TextAlign.Center
                    )
                }
            } else if (!isUnlocked && testUsageCount >= 2 && coachState !is ResumeCoachUiState.Success && coachState !is ResumeCoachUiState.Loading) {
                // Not premium lock screen (Free trial expired)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .background(MaterialTheme.colorScheme.surface),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Fase de Teste Expirada",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "2 DE 2 TESTES REALIZADOS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Você já utilizou os seus 2 testes de melhoria de currículo. Adquira a licença premium agora para ter acesso ilimitado ao AI Resume Coach e continuar aprimorando seu perfil!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Feedbacks inteligentes sobre cada seção do currículo", style = MaterialTheme.typography.bodyMedium)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Aprimoramento focado em vagas de TI desejadas", style = MaterialTheme.typography.bodyMedium)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sincronizado com a mesma licença de cursos premium", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Desbloqueie este recurso agora por apenas R$ 19,90",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            viewModel.startMercadoPagoCheckout(currentUser?.email ?: "")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Adquirir Licença Premium")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Se preferir, gerencie suas licenças na aba Serviços.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                when (val state = coachState) {
                is ResumeCoachUiState.Idle -> {
                    // Let the user select target role first
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        if (!isUnlocked) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(primaryColor.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Fase de testes",
                                                tint = primaryColor,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            val remaining = (2 - testUsageCount).coerceAtLeast(0)
                                            Text(
                                                text = "Fase de Teste Ativa",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Você possui $remaining de 2 testes restantes nesta conta.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = primaryColor.copy(alpha = 0.05f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Como funciona?",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = primaryColor
                                    )
                                    Text(
                                        text = "O Coach de Currículo por IA analisa seu perfil atual, suas habilidades, experiências e repositórios do GitHub para sugerir melhorias de impacto personalizadas, alinhadas exatamente com a vaga de seu interesse.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        item {
                            Text(
                                text = "1. Escolha sua Vaga Alvo:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        // Grid-like display of chips for predefined roles
                        item {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                predefinedRoles.forEach { role ->
                                    val isSelected = selectedRole == role
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedRole = role
                                            customRoleInput = "" // clear custom typing
                                        },
                                        label = { Text(role) },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = primaryColor.copy(alpha = 0.15f),
                                            selectedLabelColor = primaryColor,
                                            selectedLeadingIconColor = primaryColor
                                        ),
                                        modifier = Modifier.testTag("chip_role_${role.replace(" ", "_")}")
                                    )
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Ou digite um cargo personalizado:",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = customRoleInput,
                                onValueChange = {
                                    customRoleInput = it
                                    selectedRole = "" // clear chip selection
                                },
                                label = { Text("Ex: Engenheiro de IA, Gerente de Projetos...") },
                                leadingIcon = { Icon(Icons.Default.Work, null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_role_input"),
                                singleLine = true
                            )
                        }

                        item {
                            Text(
                                text = "2. Descrição de Vaga Específica (Texto Longo - Opcional):",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = jobDescriptionInput,
                                onValueChange = { jobDescriptionInput = it },
                                label = { Text("Cole aqui a descrição completa da vaga ofertada pela empresa...") },
                                leadingIcon = { Icon(Icons.Default.Description, null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .testTag("job_description_input"),
                                maxLines = 8,
                                singleLine = false
                            )
                        }

                        item {
                            val rawRole = selectedRole.ifBlank { customRoleInput }
                            val activeRole = rawRole.ifBlank {
                                if (jobDescriptionInput.isNotBlank()) "Vaga de interesse (conforme descrição)" else ""
                            }
                            Button(
                                onClick = {
                                    if (activeRole.isBlank()) {
                                        Toast.makeText(context, "Selecione um cargo ou insira a descrição de uma vaga!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.generateResumeImprovements(activeRole, jobDescriptionInput.ifBlank { null })
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("analyze_resume_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                            ) {
                                Icon(Icons.Default.TrendingUp, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analisar e Sugerir Melhorias", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                is ResumeCoachUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = primaryColor)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Otimizando seu currículo...",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Analisando seu histórico profissional, seus repositórios no GitHub e mapeando os termos ideais para o algoritmo de recrutadores...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                is ResumeCoachUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Ocorreu um erro",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                val rawRole = selectedRole.ifBlank { customRoleInput }
                                val activeRole = rawRole.ifBlank {
                                    if (jobDescriptionInput.isNotBlank()) "Vaga de interesse (conforme descrição)" else ""
                                }
                                if (activeRole.isNotBlank()) {
                                    viewModel.generateResumeImprovements(activeRole, jobDescriptionInput.ifBlank { null })
                                } else {
                                    viewModel.resetResumeCoachState()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Text("Tentar Novamente")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.resetResumeCoachState() }) {
                            Text("Voltar", color = primaryColor)
                        }
                    }
                }

                is ResumeCoachUiState.Success -> {
                    val improvements = state.improvements
                    val activeRole = selectedRole.ifBlank { customRoleInput }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        if (!isUnlocked) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(primaryColor.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Fase de testes",
                                                tint = primaryColor,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            val remaining = (2 - testUsageCount).coerceAtLeast(0)
                                            Text(
                                                text = "Fase de Teste Ativa",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Você possui $remaining de 2 testes restantes nesta conta.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            // Header showing the target vacancy analyzed
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = primaryColor
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Sugestões de Melhoria para:",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            text = activeRole,
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.resetResumeCoachState() },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                                        )
                                    ) {
                                        Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.onPrimary)
                                    }
                                }
                            }
                        }

                        // Section 1: Title and Biography Improvement
                        improvements.improvedProfile?.let { profSuggestion ->
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SectionHeader(title = "Título & Resumo Profissional", icon = Icons.Default.Badge, primaryColor = primaryColor)

                                    ElevatedCard(
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            // Before (Current)
                                            Column {
                                                Text(
                                                    text = "Antes (Atual):",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = profile.role,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = profile.bio,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                            // After (Suggested)
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "Depois (Sugerido por IA):",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = primaryColor
                                                    )
                                                    Spacer(modifier = Modifier.weight(1f))
                                                    if (profileAccepted) {
                                                        Icon(
                                                            imageVector = Icons.Default.CheckCircle,
                                                            contentDescription = "Aplicado",
                                                            tint = primaryColor,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = profSuggestion.role ?: "",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = if (profileAccepted) primaryColor else MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = profSuggestion.bio ?: "",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        Toast.makeText(context, "Sugestão de perfil ignorada.", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("Descartar", color = MaterialTheme.colorScheme.outline)
                                                }

                                                Button(
                                                    onClick = {
                                                        viewModel.applyProfileImprovement(
                                                            role = profSuggestion.role ?: profile.role,
                                                            bio = profSuggestion.bio ?: profile.bio
                                                        )
                                                        profileAccepted = true
                                                        Toast.makeText(context, "Título e Resumo atualizados com sucesso!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.weight(1.2f).testTag("replace_profile_button"),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (profileAccepted) primaryColor.copy(alpha = 0.2f) else primaryColor,
                                                        contentColor = if (profileAccepted) primaryColor else MaterialTheme.colorScheme.onPrimary
                                                    ),
                                                    enabled = !profileAccepted
                                                ) {
                                                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(if (profileAccepted) "Aplicado!" else "Substituir Dados")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section 2: Recommended Skills
                        improvements.recommendedSkills?.let { recSkills ->
                            if (recSkills.isNotEmpty()) {
                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        SectionHeader(title = "Habilidades Recomendadas", icon = Icons.Default.Lightbulb, primaryColor = primaryColor)

                                        Text(
                                            text = "Adicione estas competências altamente requisitadas para destacar seu portfólio na área de $activeRole:",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )

                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            recSkills.forEach { skill ->
                                                val skillAlreadyAdded = skills.any { s -> s.name.trim().lowercase() == skill.name?.trim()?.lowercase() }
                                                val isLocalAccepted = skill.name?.let { acceptedSkills.contains(it) } == true || skillAlreadyAdded

                                                Card(
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                                    ),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.padding(12.dp)) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Text(
                                                                text = skill.name ?: "",
                                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            SuggestionChip(
                                                                onClick = {},
                                                                label = { Text(skill.category ?: "Desenvolvimento", fontSize = 10.sp) },
                                                                colors = SuggestionChipDefaults.suggestionChipColors(
                                                                    containerColor = primaryColor.copy(alpha = 0.1f),
                                                                    labelColor = primaryColor
                                                                )
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            
                                                            if (isLocalAccepted) {
                                                                Icon(
                                                                    imageVector = Icons.Default.CheckCircle,
                                                                    contentDescription = "Adicionado",
                                                                    tint = primaryColor,
                                                                    modifier = Modifier.size(24.dp)
                                                                )
                                                            } else {
                                                                IconButton(
                                                                    onClick = {
                                                                        skill.name?.let { name ->
                                                                            viewModel.addRecommendedSkill(
                                                                                name = name,
                                                                                category = skill.category ?: "Desenvolvimento"
                                                                            )
                                                                            acceptedSkills = acceptedSkills + name
                                                                            Toast.makeText(context, "$name adicionada!", Toast.LENGTH_SHORT).show()
                                                                        }
                                                                    },
                                                                    modifier = Modifier.size(36.dp)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Add,
                                                                        contentDescription = "Adicionar",
                                                                        tint = primaryColor
                                                                    )
                                                                }
                                                            }
                                                        }
                                                        if (!skill.justification.isNullOrBlank()) {
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(
                                                                text = skill.justification,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section 3: Experiences Improved Description
                        improvements.improvedExperiences?.let { impExps ->
                            if (impExps.isNotEmpty()) {
                                item {
                                    SectionHeader(title = "Experiências Otimizadas", icon = Icons.Default.Work, primaryColor = primaryColor)
                                }

                                items(impExps) { expSuggestion ->
                                    val matchKey = "${expSuggestion.company}_${expSuggestion.role}"
                                    val isApplied = acceptedExperiences.contains(matchKey)
                                    val originalExp = experiences.find {
                                        it.company.trim().equals(expSuggestion.company?.trim(), ignoreCase = true) &&
                                        it.role.trim().equals(expSuggestion.role?.trim(), ignoreCase = true)
                                    }

                                    ElevatedCard(
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = expSuggestion.role ?: "",
                                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                                    )
                                                    Text(
                                                        text = "${expSuggestion.company} • ${expSuggestion.period ?: originalExp?.period ?: ""}",
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                        color = primaryColor
                                                    )
                                                }
                                                if (isApplied) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Aplicado",
                                                        tint = primaryColor,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }

                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                            if (originalExp != null) {
                                                Column {
                                                    Text(
                                                        text = "Descrição Atual:",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.outline
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = originalExp.description,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                            }

                                            Column {
                                                Text(
                                                    text = "Descrição Otimizada para IA:",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = primaryColor
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = expSuggestion.improvedDescription ?: "",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        Toast.makeText(context, "Sugestão ignorada.", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("Descartar", color = MaterialTheme.colorScheme.outline)
                                                }

                                                Button(
                                                    onClick = {
                                                        expSuggestion.company?.let { comp ->
                                                            expSuggestion.role?.let { r ->
                                                                viewModel.applyExperienceImprovement(
                                                                    company = comp,
                                                                    role = r,
                                                                    improvedDescription = expSuggestion.improvedDescription ?: ""
                                                                )
                                                                acceptedExperiences = acceptedExperiences + matchKey
                                                                Toast.makeText(context, "Experiência atualizada!", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.weight(1.2f),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isApplied) primaryColor.copy(alpha = 0.2f) else primaryColor,
                                                        contentColor = if (isApplied) primaryColor else MaterialTheme.colorScheme.onPrimary
                                                    ),
                                                    enabled = !isApplied
                                                ) {
                                                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(if (isApplied) "Aplicado!" else "Substituir")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section 4: Global actions
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = primaryColor.copy(alpha = 0.08f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Ações Globais",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = primaryColor
                                    )

                                    Button(
                                        onClick = {
                                            // Apply ALL improvements
                                            // Profile
                                            improvements.improvedProfile?.let { prof ->
                                                viewModel.applyProfileImprovement(
                                                    role = prof.role ?: profile.role,
                                                    bio = prof.bio ?: profile.bio
                                                )
                                                profileAccepted = true
                                            }
                                            // Skills
                                            improvements.recommendedSkills?.forEach { s ->
                                                s.name?.let { name ->
                                                    viewModel.addRecommendedSkill(name, s.category ?: "Desenvolvimento")
                                                    acceptedSkills = acceptedSkills + name
                                                }
                                            }
                                            // Experiences
                                            improvements.improvedExperiences?.forEach { exp ->
                                                exp.company?.let { c ->
                                                    exp.role?.let { r ->
                                                        viewModel.applyExperienceImprovement(
                                                            company = c,
                                                            role = r,
                                                            improvedDescription = exp.improvedDescription ?: ""
                                                        )
                                                        acceptedExperiences = acceptedExperiences + "${c}_${r}"
                                                    }
                                                }
                                            }
                                            Toast.makeText(context, "Todas as sugestões foram aplicadas!", Toast.LENGTH_LONG).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.CheckCircle, null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Substituir Todos os Dados", fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            if (currentUser == null) {
                                                Toast.makeText(context, "Por favor, faça login primeiro para sincronizar com a nuvem!", Toast.LENGTH_LONG).show()
                                            } else {
                                                saveAsNew = false
                                                newNameInput = ""
                                                showSaveDialog = true
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.CloudUpload, null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Enviar para Nuvem", fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            viewModel.resetResumeCoachState()
                                            Toast.makeText(context, "Sugestões descartadas.", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Descartar Todas as Opções", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    primaryColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(primaryColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
