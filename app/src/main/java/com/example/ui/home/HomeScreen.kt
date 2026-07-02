package com.example.ui.home

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entities.ExperienceEntity
import com.example.data.local.entities.ProfileEntity
import com.example.data.local.entities.SectionOrderEntity
import com.example.data.local.entities.SkillEntity
import com.example.data.remote.models.GithubRepo
import com.example.ui.theme.toColor
import com.example.ui.viewmodel.GithubReposUiState
import com.example.ui.viewmodel.PortfolioViewModel

@Composable
fun HomeScreen(
    viewModel: PortfolioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()
    val skills by viewModel.skills.collectAsState()
    val experiences by viewModel.experiences.collectAsState()
    val sections by viewModel.sectionOrders.collectAsState()
    val githubReposState by viewModel.githubReposState.collectAsState()
    val themeSettings by viewModel.themeSettings.collectAsState()

    val primaryColor = themeSettings.primaryColorHex.toColor()
    val secondaryColor = themeSettings.secondaryColorHex.toColor()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_scroll"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Header (Fixed at the top of the home stream)
        item {
            HeroHeaderCard(profile = profile, primaryColor = primaryColor, secondaryColor = secondaryColor)
        }

        // Render dynamic sections according to database order
        sections.sortedBy { it.displayOrder }.forEach { section ->
            when (section.sectionId) {
                "sobre" -> {
                    item(key = "sobre") {
                        SectionWrapper(title = section.title, icon = Icons.Default.Person, primaryColor = primaryColor) {
                            AboutSection(profile = profile)
                        }
                    }
                }
                "skills" -> {
                    item(key = "skills") {
                        SectionWrapper(title = section.title, icon = Icons.Default.Terminal, primaryColor = primaryColor) {
                            SkillsSection(skills = skills, primaryColor = primaryColor, secondaryColor = secondaryColor)
                        }
                    }
                }
                "experiencia" -> {
                    item(key = "experiencia") {
                        SectionWrapper(title = section.title, icon = Icons.Default.WorkHistory, primaryColor = primaryColor) {
                            ExperienceTimeline(experiences = experiences, primaryColor = primaryColor)
                        }
                    }
                }
                "projetos" -> {
                    item(key = "projetos") {
                        SectionWrapper(title = section.title, icon = Icons.Default.Source, primaryColor = primaryColor) {
                            GithubReposSection(
                                state = githubReposState,
                                primaryColor = primaryColor,
                                onRefresh = { viewModel.fetchGithubRepos(profile.githubUsername) }
                            )
                        }
                    }
                }
                "contato" -> {
                    item(key = "contato") {
                        SectionWrapper(title = section.title, icon = Icons.Default.ContactMail, primaryColor = primaryColor) {
                            ContactSection(profile = profile, primaryColor = primaryColor, secondaryColor = secondaryColor)
                        }
                    }
                }
            }
        }

        // Footer spacing
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SectionWrapper(
    title: String,
    icon: ImageVector,
    primaryColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Composable
fun HeroHeaderCard(
    profile: ProfileEntity,
    primaryColor: Color,
    secondaryColor: Color
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_header_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(primaryColor, secondaryColor)
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Profile Avatar/Icon Placeholder
                val initials = profile.name.split(" ")
                    .filter { it.trim().isNotEmpty() }
                    .map { it.first().uppercase() }
                    .take(2)
                    .joinToString("")
                    .ifEmpty { "AS" }

                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!profile.photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = profile.photoUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.testTag("profile_name")
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = profile.role,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f)
                    ),
                    modifier = Modifier.testTag("profile_role")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Localização",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = profile.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // LinkedIn & Email Quick Actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val uri = Uri.parse(profile.linkedinUrl)
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Não foi possível abrir o LinkedIn", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = primaryColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("linkedin_button")
                    ) {
                        Icon(imageVector = Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("LinkedIn", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${profile.email}")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Nenhum aplicativo de email encontrado", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("E-mail")
                    }
                }
            }
        }
    }
}

@Composable
fun AboutSection(profile: ProfileEntity) {
    Text(
        text = profile.bio,
        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SkillsSection(
    skills: List<SkillEntity>,
    primaryColor: Color,
    secondaryColor: Color
) {
    val devSkills = skills.filter { it.category == "Desenvolvimento" }
    val infraSkills = skills.filter { it.category == "Infraestrutura" }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (devSkills.isNotEmpty()) {
            Column {
                Text(
                    text = "DESENVOLVIMENTO DE SOFTWARE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    devSkills.forEach { skill ->
                        SkillBadge(skillName = skill.name, badgeColor = primaryColor.copy(alpha = 0.12f), textColor = primaryColor)
                    }
                }
            }
        }

        if (infraSkills.isNotEmpty()) {
            Column {
                Text(
                    text = "REDES & INFRAESTRUTURA DE TI",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = secondaryColor
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    infraSkills.forEach { skill ->
                        SkillBadge(skillName = skill.name, badgeColor = secondaryColor.copy(alpha = 0.12f), textColor = secondaryColor)
                    }
                }
            }
        }
        
        if (skills.isEmpty()) {
            Text(
                text = "Nenhuma habilidade adicionada. Vá em configurações para cadastrar suas habilidades.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SkillBadge(
    skillName: String,
    badgeColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(badgeColor)
            .border(BorderStroke(1.dp, textColor.copy(alpha = 0.2f)), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = skillName,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            ),
            color = textColor
        )
    }
}

@Composable
fun ExperienceTimeline(
    experiences: List<ExperienceEntity>,
    primaryColor: Color
) {
    if (experiences.isEmpty()) {
        Text(
            text = "Nenhuma experiência profissional cadastrada ainda.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        experiences.forEachIndexed { index, exp ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                // Timeline Line with Node Circle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(32.dp)
                ) {
                    // Node dot
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                            .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    )
                    // Dotted line connecting nodes
                    if (index < experiences.size - 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(2.dp)
                                .drawBehind {
                                    val size = this.size
                                    var y = 0f
                                    val interval = 10f
                                    while (y < size.height) {
                                        drawLine(
                                            color = primaryColor.copy(alpha = 0.4f),
                                            start = Offset(size.width / 2, y),
                                            end = Offset(size.width / 2, y + 6f),
                                            strokeWidth = 2.dp.toPx()
                                        )
                                        y += interval
                                    }
                                }
                        )
                    }
                }

                // Experience details
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp, bottom = 24.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = exp.role,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${exp.company} • ${exp.period}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = exp.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun GithubReposSection(
    state: GithubReposUiState,
    primaryColor: Color,
    onRefresh: () -> Unit
) {
    when (state) {
        is GithubReposUiState.Idle -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text(
                    text = "Nenhum usuário do GitHub cadastrado.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        is GithubReposUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = primaryColor)
            }
        }
        is GithubReposUiState.Error -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Button(
                    onClick = onRefresh,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Tentar novamente")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tentar Novamente")
                }
            }
        }
        is GithubReposUiState.Success -> {
            val repos = state.repos
            if (repos.isEmpty()) {
                Text(
                    text = "Nenhum repositório público encontrado para este usuário.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Show top 3-4 repositories
                    repos.take(4).forEach { repo ->
                        GithubRepoCard(repo = repo, primaryColor = primaryColor)
                    }
                    
                    if (repos.size > 4) {
                        Text(
                            text = "E mais ${repos.size - 4} repositórios adicionais no GitHub.",
                            style = MaterialTheme.typography.labelSmall,
                            color = primaryColor,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GithubRepoCard(
    repo: GithubRepo,
    primaryColor: Color
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(repo.htmlUrl))
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast
                        .makeText(context, "Não foi possível abrir o repositório", Toast.LENGTH_SHORT)
                        .show()
                }
            },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = repo.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                // Stars Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Stars",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = repo.stargazersCount.toString(),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (!repo.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = repo.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main language tag
                repo.language?.let { lang ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(getLanguageColor(lang))
                        )
                        Text(
                            text = lang,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Link symbol
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Ver repositório",
                        style = MaterialTheme.typography.bodySmall.copy(color = primaryColor, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

fun getLanguageColor(language: String): Color {
    return when (language.lowercase()) {
        "kotlin" -> Color(0xFF7F52FF)
        "java" -> Color(0xFFB07219)
        "javascript" -> Color(0xFFF1E05A)
        "typescript" -> Color(0xFF3178C6)
        "python" -> Color(0xFF3572A5)
        "html" -> Color(0xFFE34C26)
        "css" -> Color(0xFF563D7C)
        "shell" -> Color(0xFF89E051)
        "c++" -> Color(0xFFF34B7D)
        "go" -> Color(0xFF00ADD8)
        else -> Color(0xFF9E9E9E)
    }
}

@Composable
fun ContactSection(
    profile: ProfileEntity,
    primaryColor: Color,
    secondaryColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ContactRow(icon = Icons.Outlined.Email, label = "E-mail Profissional", value = profile.email, primaryColor = primaryColor)
        ContactRow(icon = Icons.Outlined.Phone, label = "Telefone / WhatsApp", value = profile.phone, primaryColor = primaryColor)
        ContactRow(icon = Icons.Outlined.LocationOn, label = "Endereço", value = profile.location, primaryColor = primaryColor)
        ContactRow(icon = Icons.Outlined.Link, label = "LinkedIn", value = profile.linkedinUrl, primaryColor = primaryColor)
    }
}

@Composable
fun ContactRow(
    icon: ImageVector,
    label: String,
    value: String,
    primaryColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(primaryColor.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Simple FlowRow implementation for older layouts
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable androidx.compose.foundation.layout.FlowRowScope.() -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = content
    )
}
