package com.example.ui.profile

import android.widget.Toast
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.ui.viewmodel.ConflictResolution
import com.example.ui.viewmodel.FirebaseSyncUiState
import com.example.ui.theme.toColor

@Composable
fun ProfileDialog(
    onDismiss: () -> Unit,
    viewModel: PortfolioViewModel,
    onGoogleSignInClick: () -> Unit,
    onSimulatedSignInClick: () -> Unit
) {
    val context = LocalContext.current
    val themeSettings by viewModel.themeSettings.collectAsState()
    val primaryColor = themeSettings.primaryColorHex.toColor()
    
    val currentUser by viewModel.firebaseSyncManager.currentUser.collectAsState()
    val savedResumes by viewModel.savedResumes.collectAsState()
    val selectedResumeName by viewModel.selectedResumeName.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("profile_dialog_container")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentUser != null) "Minha Conta" else "Acesse sua Conta",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (currentUser != null) {
                    val user = currentUser!!
                    // Profile Info Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (!user.photoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = user.photoUrl,
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(primaryColor.copy(alpha = 0.1f))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(primaryColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (user.displayName ?: "U").take(1).uppercase(),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.displayName ?: "Usuário",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = user.email ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val badgeText = if (user.isSimulated) "Modo Simulado" else "Firebase Cloud"
                                val badgeColor = if (user.isSimulated) Color(0xFFE65100) else Color(0xFF0D47A1)
                                Surface(
                                    color = badgeColor.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = badgeText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = badgeColor,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Portfolios selection list
                    Text(
                        text = "Selecione o currículo para visualização:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        items(savedResumes) { resumeName ->
                            val isSelected = resumeName == selectedResumeName
                            Card(
                                onClick = {
                                    viewModel.syncWithCloud(ConflictResolution.PULL_OVERWRITE, resumeName)
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) primaryColor.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("resume_item_$resumeName")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Article,
                                            contentDescription = null,
                                            tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = resumeName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selecionado",
                                            tint = primaryColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Sync Status message if active
                    when (val state = syncState) {
                        is FirebaseSyncUiState.Loading -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = primaryColor,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Alterando visualização...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = primaryColor
                                )
                            }
                        }
                        is FirebaseSyncUiState.Error -> {
                            Text(
                                text = state.error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        else -> {}
                    }

                    // Logout Button
                    Button(
                        onClick = {
                            viewModel.signOut()
                            onDismiss()
                            Toast.makeText(context, "Sessão encerrada localmente.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("logout_button")
                    ) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sair da Conta", fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Logged out state UI
                    Text(
                        text = "Mantenha seu portfólio salvo com segurança na nuvem! Faça login para sincronizar dados em tempo real ou transferir para outros dispositivos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onGoogleSignInClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("google_login_dialog_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(12.dp),
                            enabled = syncState !is FirebaseSyncUiState.Loading
                        ) {
                            Icon(imageVector = Icons.Default.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Entrar com Google (Firebase)", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onSimulatedSignInClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("simulated_login_dialog_button"),
                            border = BorderStroke(1.dp, primaryColor),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor),
                            shape = RoundedCornerShape(12.dp),
                            enabled = syncState !is FirebaseSyncUiState.Loading
                        ) {
                            Icon(imageVector = Icons.Default.SmartToy, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Entrar com Conta de Teste (Simulado)", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = primaryColor
                            )
                            Text(
                                text = "O modo simulado permite salvar backups e portfólios virtualmente, ideal para testar imediatamente no navegador!",
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
