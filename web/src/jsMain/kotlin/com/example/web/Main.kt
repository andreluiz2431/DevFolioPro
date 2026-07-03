package com.example.web

import org.jetbrains.compose.web.renderComposable
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.attributes.*
import androidx.compose.runtime.*
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLAnchorElement
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.dom.url.URL
import com.example.shared.SharedConstants

fun AttrsScope<*>.classesList(vararg classStrings: String) {
    classStrings.forEach { str ->
        classes(*str.split(' ').filter { it.isNotBlank() }.toTypedArray())
    }
}

data class Experience(
    val title: String,
    val period: String,
    val description: String
)

data class Certification(
    val title: String,
    val category: String,
    val duration: String,
    val cost: String,
    val description: String
)

data class WebUser(
    val name: String,
    val email: String,
    val photoUrl: String? = null,
    val isSimulated: Boolean = false,
    val uid: String = "uid_" + email.hashCode()
)

enum class AccentColor(
    val namePt: String,
    val primaryHex: String,
    val bgGradient: String,
    val textClass: String,
    val borderClass: String,
    val bgClass: String,
    val hoverBgClass: String,
    val ringClass: String,
    val badgeClass: String,
    val buttonBg: String
) {
    INDIGO(
        "Índigo", "#6366f1", "from-indigo-500 via-purple-500 to-pink-500", "text-indigo-400", "border-indigo-500/20", 
        "bg-indigo-500/10", "hover:bg-indigo-500/20", "ring-indigo-500", 
        "bg-indigo-500/10 text-indigo-300", "bg-indigo-600 hover:bg-indigo-500"
    ),
    EMERALD(
        "Esmeralda", "#10b981", "from-emerald-500 via-teal-500 to-cyan-500", "text-emerald-400", "border-emerald-500/20", 
        "bg-emerald-500/10", "hover:bg-emerald-500/20", "ring-emerald-500", 
        "bg-emerald-500/10 text-emerald-300", "bg-emerald-600 hover:bg-emerald-500"
    ),
    PURPLE(
        "Púrpura", "#a855f7", "from-purple-500 via-fuchsia-500 to-pink-500", "text-purple-400", "border-purple-500/20", 
        "bg-purple-500/10", "hover:bg-purple-500/20", "ring-purple-500", 
        "bg-purple-500/10 text-purple-300", "bg-purple-600 hover:bg-purple-500"
    ),
    AMBER(
        "Âmbar", "#f59e0b", "from-amber-500 via-orange-500 to-yellow-500", "text-amber-400", "border-amber-500/20", 
        "bg-amber-500/10", "hover:bg-amber-500/20", "ring-amber-500", 
        "bg-amber-500/10 text-amber-300", "bg-amber-600 hover:bg-amber-500"
    ),
    ROSE(
        "Rosa", "#f43f5e", "from-rose-500 via-pink-500 to-red-500", "text-rose-400", "border-rose-500/20", 
        "bg-rose-500/10", "hover:bg-rose-500/20", "ring-rose-500", 
        "bg-rose-500/10 text-rose-300", "bg-rose-600 hover:bg-rose-500"
    )
}

fun main() {
    renderComposable(rootElementId = "root") {
        // --- 1. CORE APPLICATION STATE ---
        var selectedTab by remember { mutableStateOf("portfolio") }
        var accentColor by remember { mutableStateOf(AccentColor.INDIGO) }

        // Portfolio Content State
        var nameState by remember { mutableStateOf("Alexandre Silva") }
        var roleState by remember { mutableStateOf("Arquiteto Cloud & DevOps Specialist") }
        var bioState by remember { mutableStateOf("Especialista em automação de infraestrutura de TI corporativa, redes Cisco/Fortinet e implantação de microsserviços escaláveis com Kubernetes na nuvem AWS.") }
        var githubUser by remember { mutableStateOf("alexandresilva") }
        var emailState by remember { mutableStateOf("contato@devfoliopro.com") }
        var linkedinLink by remember { mutableStateOf("https://linkedin.com") }
        var githubLink by remember { mutableStateOf("https://github.com") }

        var skillsState by remember { 
            mutableStateOf(
                listOf(
                    "AWS Solutions", "Docker & Kubernetes", "Cisco CCNA", 
                    "Fortinet Firewalls", "Kotlin / Jetpack Compose", "Node.js", 
                    "CI/CD Pipelines", "Linux Administration", "Python"
                )
            ) 
        }

        var experiencesState by remember { 
            mutableStateOf(
                listOf(
                    Experience(
                        "SRE & Arquiteto Cloud Senior", 
                        "NuvemCorporativa S.A. | 2024 - Presente", 
                        "Liderança técnica na migração de servidores legados para arquitetura de microsserviços no Kubernetes. Redução de 40% nos custos operacionais com estratégias inteligentes na AWS."
                    ),
                    Experience(
                        "Analista de Redes & Segurança (NOC)", 
                        "GlobalTech Telecom | 2021 - 2024", 
                        "Administração corporativa de firewalls FortiGate e roteadores Cisco. Monitoramento em alta disponibilidade (99.9% de uptime) de infraestrutura de missão crítica."
                    )
                )
            ) 
        }

        var certificationsState by remember { 
            mutableStateOf(
                listOf(
                    Certification(
                        "AWS Certified Solutions Architect", 
                        "Cloud & DevOps", "80 horas", "US$ 150", 
                        "Garante conhecimento profundo em modelagem de arquiteturas resilientes e econômicas na nuvem AWS."
                    ),
                    Certification(
                        "Fortinet NSE 4 - Network Security Professional", 
                        "Segurança", "40 horas", "US$ 150", 
                        "Valida as competências de configuração, gerenciamento e monitoramento de segurança com equipamentos FortiGate."
                    )
                )
            ) 
        }

        // Authentication State (Matching Android Profile Settings)
        var currentUser by remember { mutableStateOf<WebUser?>(null) }
        var showLoginDialog by remember { mutableStateOf(false) }
        var showGoogleIdentityModal by remember { mutableStateOf(false) }
        var authEmailInput by remember { mutableStateOf("") }
        var authNameInput by remember { mutableStateOf("") }

        // Section Orders (Layout Manager)
        var webSections by remember {
            mutableStateOf(
                listOf("Sobre Mim", "Habilidades", "Experiência Profissional", "Certificações")
            )
        }

        // Certification creation input helpers
        var certTitleInput by remember { mutableStateOf("") }
        var certCategoryInput by remember { mutableStateOf("Cloud & DevOps") }
        var certDurationInput by remember { mutableStateOf("") }
        var certCostInput by remember { mutableStateOf("") }
        var certDescInput by remember { mutableStateOf("") }

        // LinkedIn / CV Import IA State
        var linkedinRawInput by remember { mutableStateOf("") }
        var linkedinReplaceMode by remember { mutableStateOf(false) }
        var isLinkedinImporting by remember { mutableStateOf(false) }

        // Cloud Firebase Sync States
        var savedBackups by remember { mutableStateOf(listOf("Principal")) }
        var selectedBackupId by remember { mutableStateOf("Principal") }
        var showBackupNameInput by remember { mutableStateOf(false) }
        var newBackupName by remember { mutableStateOf("") }
        var syncSuccessMessage by remember { mutableStateOf<String?>(null) }
        var syncErrorMessage by remember { mutableStateOf<String?>(null) }

        // AI Resume Coach State
        var coachSelectedRole by remember { mutableStateOf("Engenheiro DevOps") }
        var isCoachAnalyzing by remember { mutableStateOf(false) }
        var showCoachResults by remember { mutableStateOf(false) }
        var coachMatchScore by remember { mutableStateOf(0) }
        var coachSuggestions by remember { mutableStateOf("") }
        var coachMissingSkills by remember { mutableStateOf(emptyList<String>()) }
        var coachSuccessFeedback by remember { mutableStateOf(false) }
        var coachSuggestedRole by remember { mutableStateOf("") }
        var coachSuggestedBio by remember { mutableStateOf("") }
        var coachSuggestedCompany by remember { mutableStateOf("") }
        var coachSuggestedJobRole by remember { mutableStateOf("") }
        var coachSuggestedExpDesc by remember { mutableStateOf("") }
        var coachSuggestedExpOrigDesc by remember { mutableStateOf("") }
        var copySuccessText by remember { mutableStateOf<String?>(null) }

        // Profile Editor Inputs (Temporary holder state for settings editing)
        var editSkillInput by remember { mutableStateOf("") }
        var expTitleInput by remember { mutableStateOf("") }
        var expPeriodInput by remember { mutableStateOf("") }
        var expDescInput by remember { mutableStateOf("") }

        // Recommender Input helper
        var selectedCourseCategory by remember { mutableStateOf("Cloud") }

        // --- 2. MAIN LAYOUT AND BODY WRAPPER ---
        Div(attrs = {
            classes("min-h-screen", "text-slate-100", "relative", "transition-colors", "duration-500")
            style {
                // Radial gradients with dynamic colors reflecting the accent color choice
                property(
                    "background-image", 
                    "radial-gradient(at 0% 0%, ${accentColor.primaryHex}1a 0px, transparent 50%), " +
                    "radial-gradient(at 100% 100%, ${accentColor.primaryHex}1a 0px, transparent 50%)"
                )
                property("background-color", "#0b0f19")
            }
        }) {
            // Header: Branding and Authentication / Profile status
            Header(attrs = { 
                classes("border-b", "border-slate-800", "bg-slate-900/40", "backdrop-blur-md", "sticky", "top-0", "z-40") 
            }) {
                Div(attrs = { classes("max-w-6xl", "mx-auto", "px-6", "py-4", "flex", "justify-between", "items-center") }) {
                    // Logo and Platform Title
                    Div(attrs = { classes("flex", "items-center", "gap-3") }) {
                        Div(attrs = { 
                            classesList("w-10", "h-10", "rounded-xl", "bg-gradient-to-tr", "flex", "items-center", "justify-center", "font-black", "text-white", accentColor.bgGradient)
                        }) {
                            Text("D")
                        }
                        Div {
                            H1(attrs = { classes("text-lg", "font-extrabold", "tracking-tight", "text-white") }) {
                                Text("DevFolio Pro")
                            }
                            P(attrs = { classes("text-xs", "text-slate-400") }) {
                                Text("Web Portal v" + SharedConstants.APP_VERSION)
                            }
                        }
                    }

                    // Authentication controller
                    Div(attrs = { classes("flex", "items-center", "gap-4") }) {
                        val user = currentUser
                        if (user != null) {
                            // User is signed-in (Firebase Sync Status)
                            Div(attrs = { classes("flex", "items-center", "gap-3", "bg-slate-950/60", "p-2", "rounded-xl", "border", "border-slate-800") }) {
                                if (user.photoUrl != null) {
                                    Img(src = user.photoUrl, alt = user.name, attrs = {
                                        classes("w-8", "h-8", "rounded-full", "border", "border-slate-700")
                                    })
                                } else {
                                    Div(attrs = { 
                                        classesList("w-8", "h-8", "rounded-full", "bg-gradient-to-tr", "flex", "items-center", "justify-center", "text-xs", "font-bold", "text-white", accentColor.bgGradient) 
                                    }) {
                                        Text(user.name.take(1).uppercase())
                                    }
                                }
                                Div(attrs = { classes("hidden", "sm:block") }) {
                                    P(attrs = { classes("text-xs", "font-bold", "text-white", "leading-tight") }) {
                                        Text(user.name)
                                    }
                                    P(attrs = { 
                                        classes("text-[10px]", if (user.isSimulated) "text-amber-400" else "text-emerald-400", "flex", "items-center", "gap-1") 
                                    }) {
                                        Text(if (user.isSimulated) "☁️ Nuvem (Simulado)" else "🔥 Google & Firebase Ativo")
                                    }
                                }
                                Button(attrs = {
                                    classes("text-slate-400", "hover:text-rose-400", "text-xs", "px-2", "py-1", "transition-colors", "ml-2")
                                    onClick { currentUser = null }
                                }) {
                                    Text("Sair")
                                }
                            }
                        } else {
                            // Offline/Not authenticated state
                            Button(attrs = {
                                classes(
                                    "px-4", "py-2", "rounded-xl", "text-xs", "font-semibold", 
                                    "bg-slate-900", "hover:bg-slate-800", "border", "border-slate-800", "transition-all", "flex", "items-center", "gap-2"
                                )
                                onClick { showLoginDialog = true }
                            }) {
                                Span(attrs = { classes("w-2", "h-2", "rounded-full", "bg-amber-400", "inline-block") })
                                Text("Acessar / Sincronizar")
                            }
                        }
                    }
                }
            }

            // Tab Navigation Bar
            Nav(attrs = { classes("border-b", "border-slate-800/60", "bg-slate-950/40", "py-2") }) {
                Div(attrs = { classes("max-w-4xl", "mx-auto", "px-6", "flex", "gap-2") }) {
                    // Portfólio Tab
                    Button(attrs = {
                        classesList(
                            "px-4", "py-2.5", "rounded-xl", "text-sm", "font-bold", "transition-all", "flex", "items-center", "gap-2",
                            if (selectedTab == "portfolio") "bg-slate-900 text-white border border-slate-800 shadow-lg" else "text-slate-400 hover:text-white"
                        )
                        onClick { selectedTab = "portfolio" }
                    }) {
                        Text("📂 Portfólio")
                    }

                    // AI Resume Coach Tab (Melhorias)
                    Button(attrs = {
                        classesList(
                            "px-4", "py-2.5", "rounded-xl", "text-sm", "font-bold", "transition-all", "flex", "items-center", "gap-2",
                            if (selectedTab == "coach") "bg-slate-900 text-white border border-slate-800 shadow-lg" else "text-slate-400 hover:text-white"
                        )
                        onClick { selectedTab = "coach" }
                    }) {
                        Text("✨ AI Coach")
                        Span(attrs = { classes("px-1.5", "py-0.5", "rounded-full", "text-[10px]", "bg-indigo-500/10", "text-indigo-400", "border", "border-indigo-500/20") }) {
                            Text("Novo")
                        }
                    }

                    // Ajustes/Configurações Tab
                    Button(attrs = {
                        classesList(
                            "px-4", "py-2.5", "rounded-xl", "text-sm", "font-bold", "transition-all", "flex", "items-center", "gap-2",
                            if (selectedTab == "settings") "bg-slate-900 text-white border border-slate-800 shadow-lg" else "text-slate-400 hover:text-white"
                        )
                        onClick { selectedTab = "settings" }
                    }) {
                        Text("🛠️ Painel de Serviços")
                    }
                }
            }

            // Main Contents Area
            Main(attrs = { classes("max-w-4xl", "mx-auto", "px-6", "py-10", "min-h-[70vh]") }) {
                when (selectedTab) {
                    "portfolio" -> {
                        // --- PORTFOLIO SCREEN VIEW ---
                        Div(attrs = { classes("space-y-12") }) {
                            // Header Hero Card (Dynamic color matching accent)
                            Div(attrs = {
                                classes("rounded-3xl", "p-8", "relative", "overflow-hidden", "border", "border-white/10", "shadow-2xl")
                                style {
                                    property("background-image", "linear-gradient(135deg, ${accentColor.primaryHex}e6, #0f172a)")
                                }
                            }) {
                                Div(attrs = { classes("flex", "flex-col", "md:flex-row", "gap-6", "items-center", "relative", "z-10") }) {
                                    // Avatar representation
                                    Div(attrs = {
                                        classes(
                                            "w-24", "h-24", "rounded-full", "bg-white/20", "backdrop-blur-md", 
                                            "border-2", "border-white", "flex", "items-center", "justify-center", "text-3xl", "font-black", "text-white"
                                        )
                                    }) {
                                        Text(nameState.split(" ").filter { it.isNotEmpty() }.map { it.take(1) }.take(2).joinToString("").uppercase())
                                    }

                                    Div(attrs = { classes("text-center", "md:text-left", "space-y-2") }) {
                                        Span(attrs = {
                                            classes("px-3", "py-1", "rounded-full", "text-xs", "font-bold", "bg-white/10", "text-white", "border", "border-white/20")
                                        }) {
                                            Text("Portfólio Ativo")
                                        }
                                        H2(attrs = { classes("text-3xl", "md:text-4xl", "font-extrabold", "text-white", "tracking-tight") }) {
                                            Text(nameState)
                                        }
                                        P(attrs = { classes("text-lg", "text-white/90", "font-medium") }) {
                                            Text(roleState)
                                        }
                                    }
                                }
                            }

                            // Dynamic sections container
                            Div(attrs = { classes("grid", "grid-cols-1", "md:grid-cols-3", "gap-8") }) {
                                // Left Sidebar Info (Stays fixed for essential bio contact)
                                Div(attrs = { classes("md:col-span-1", "space-y-8") }) {
                                    // Contacts & Socials
                                    Div(attrs = { classes("bg-slate-900/60", "backdrop-blur-xl", "rounded-2xl", "p-6", "border", "border-slate-800") }) {
                                        H3(attrs = { classes("text-sm", "font-extrabold", "text-slate-400", "uppercase", "tracking-wider", "mb-4") }) {
                                            Text("📧 Contato & Redes")
                                        }
                                        Ul(attrs = { classes("space-y-3", "text-sm", "text-slate-300") }) {
                                            Li {
                                                A(href = "mailto:$emailState", attrs = { classes("hover:${accentColor.textClass}", "transition-colors", "flex", "items-center", "gap-2") }) {
                                                    Text("✉️ $emailState")
                                                }
                                            }
                                            Li {
                                                A(href = githubLink, attrs = { classes("hover:${accentColor.textClass}", "transition-colors", "flex", "items-center", "gap-2") }) {
                                                    Text("🐙 GitHub: @$githubUser")
                                                }
                                            }
                                            Li {
                                                A(href = linkedinLink, attrs = { classes("hover:${accentColor.textClass}", "transition-colors", "flex", "items-center", "gap-2") }) {
                                                    Text("💼 LinkedIn")
                                                }
                                            }
                                        }
                                    }
                                }

                                // Right Detailed Area (Ordered Dynamically via Layout Manager)
                                Div(attrs = { classes("md:col-span-2", "space-y-8") }) {
                                    webSections.forEach { sectionName ->
                                        when (sectionName) {
                                            "Sobre Mim" -> {
                                                // Bio/Sobre Card
                                                Div(attrs = { classes("bg-slate-900/60", "backdrop-blur-xl", "rounded-2xl", "p-6", "border", "border-slate-800", "animate-fade-in") }) {
                                                    H3(attrs = { classes("text-sm", "font-extrabold", "text-slate-400", "uppercase", "tracking-wider", "mb-4") }) {
                                                        Text("👤 Sobre Mim")
                                                    }
                                                    P(attrs = { classes("text-slate-300", "leading-relaxed", "text-base") }) {
                                                        Text(bioState)
                                                    }
                                                }
                                            }
                                            "Habilidades" -> {
                                                // Skills Card (Renders as main block)
                                                Div(attrs = { classes("bg-slate-900/60", "backdrop-blur-xl", "rounded-2xl", "p-6", "border", "border-slate-800", "animate-fade-in") }) {
                                                    H3(attrs = { classes("text-sm", "font-extrabold", "text-slate-400", "uppercase", "tracking-wider", "mb-4", "flex", "items-center", "gap-2") }) {
                                                        Text("⚡ Habilidades Técnicas")
                                                    }
                                                    Div(attrs = { classes("flex", "flex-wrap", "gap-2") }) {
                                                        skillsState.forEach { skill ->
                                                            Span(attrs = {
                                                                classes(
                                                                    "px-2.5", "py-1.5", "rounded-lg", "text-xs", "font-semibold", 
                                                                    "bg-slate-800", "text-slate-300", "border", "border-slate-700/60"
                                                                )
                                                            }) {
                                                                Text(skill)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            "Experiência Profissional" -> {
                                                // Experiences Timeline
                                                Div(attrs = { classes("bg-slate-900/60", "backdrop-blur-xl", "rounded-2xl", "p-6", "border", "border-slate-800", "animate-fade-in") }) {
                                                    H3(attrs = { classes("text-sm", "font-extrabold", "text-slate-400", "uppercase", "tracking-wider", "mb-6") }) {
                                                        Text("💼 Experiência Profissional")
                                                    }
                                                    Div(attrs = { classes("space-y-6") }) {
                                                        experiencesState.forEach { exp ->
                                                            Div(attrs = { classes("relative", "pl-6", "border-l-2", "border-slate-800") }) {
                                                                Div(attrs = { 
                                                                    classes(
                                                                        "absolute", "-left-[9px]", "top-1.5", "w-4", "h-4", "rounded-full", 
                                                                        "border-4", "border-slate-950"
                                                                    ) 
                                                                    style {
                                                                        property("background-color", accentColor.primaryHex)
                                                                    }
                                                                })
                                                                H4(attrs = { classes("text-lg", "font-bold", "text-slate-100") }) {
                                                                    Text(exp.title)
                                                                }
                                                                P(attrs = { classes("text-xs", "font-semibold", accentColor.textClass, "mb-2") }) {
                                                                    Text(exp.period)
                                                                }
                                                                P(attrs = { classes("text-slate-400", "text-sm", "leading-relaxed") }) {
                                                                    Text(exp.description)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            "Certificações" -> {
                                                // Certifications List
                                                Div(attrs = { classes("bg-slate-900/60", "backdrop-blur-xl", "rounded-2xl", "p-6", "border", "border-slate-800", "animate-fade-in") }) {
                                                    H3(attrs = { classes("text-sm", "font-extrabold", "text-slate-400", "uppercase", "tracking-wider", "mb-4") }) {
                                                        Text("🎓 Certificações")
                                                    }
                                                    Div(attrs = { classes("space-y-4") }) {
                                                        certificationsState.forEach { cert ->
                                                            Div(attrs = { classes("bg-slate-950/40", "p-4", "rounded-xl", "border", "border-slate-800/80") }) {
                                                                Div(attrs = { classes("flex", "justify-between", "items-start", "gap-2", "mb-2") }) {
                                                                    H4(attrs = { classes("font-bold", accentColor.textClass) }) {
                                                                        Text(cert.title)
                                                                    }
                                                                    Span(attrs = { classes("text-[10px]", "font-bold", "bg-slate-800", "text-slate-300", "px-2", "py-0.5", "rounded-full") }) {
                                                                        Text(cert.category)
                                                                    }
                                                                }
                                                                P(attrs = { classes("text-[10px]", "text-slate-500", "mb-2") }) {
                                                                    Text("Duração: ${cert.duration} | Custo: ${cert.cost}")
                                                                }
                                                                P(attrs = { classes("text-xs", "text-slate-400", "leading-relaxed") }) {
                                                                    Text(cert.description)
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
                    }

                    "coach" -> {
                        // --- AI RESUME COACH (MELHORIAS) VIEW ---
                        Div(attrs = { classes("space-y-8") }) {
                            // Section Description
                            Div(attrs = { classes("bg-indigo-500/5", "border", "border-indigo-500/15", "rounded-2xl", "p-6", "space-y-3") }) {
                                H2(attrs = { classes("text-xl", "font-extrabold", "text-white", "flex", "items-center", "gap-2") }) {
                                    Text("✨ AI Resume Coach")
                                }
                                P(attrs = { classes("text-slate-300", "text-sm", "leading-relaxed") }) {
                                    Text("O Coach de Currículo por IA analisa as habilidades, dados de perfil e histórico do seu portfólio para sugerir melhorias de impacto voltadas para a vaga dos seus sonhos.")
                                }
                            }

                            // Selection Role & Action Card
                            Div(attrs = { classes("bg-slate-900/60", "rounded-2xl", "p-6", "border", "border-slate-800", "space-y-6") }) {
                                Div(attrs = { classes("space-y-2") }) {
                                    Label(attrs = { classes("text-xs", "font-bold", "text-slate-400", "uppercase", "tracking-wider") }) {
                                        Text("Selecione sua vaga alvo:")
                                    }
                                    Select(attrs = {
                                        classes("w-full", "bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-sm", "text-white", "focus:outline-none", "focus:ring-2", "focus:ring-indigo-500")
                                        onChange { event ->
                                            event.value?.let { coachSelectedRole = it }
                                        }
                                    }) {
                                        Option("Engenheiro DevOps", attrs = { if (coachSelectedRole == "Engenheiro DevOps") selected() }) { Text("Engenheiro DevOps") }
                                        Option("Desenvolvedor Back-end", attrs = { if (coachSelectedRole == "Desenvolvedor Back-end") selected() }) { Text("Desenvolvedor Back-end") }
                                        Option("Desenvolvedor Front-end", attrs = { if (coachSelectedRole == "Desenvolvedor Front-end") selected() }) { Text("Desenvolvedor Front-end") }
                                        Option("Desenvolvedor Full-stack", attrs = { if (coachSelectedRole == "Desenvolvedor Full-stack") selected() }) { Text("Desenvolvedor Full-stack") }
                                        Option("Analista de Infraestrutura", attrs = { if (coachSelectedRole == "Analista de Infraestrutura") selected() }) { Text("Analista de Infraestrutura") }
                                        Option("Desenvolvedor Mobile", attrs = { if (coachSelectedRole == "Desenvolvedor Mobile") selected() }) { Text("Desenvolvedor Mobile") }
                                    }
                                }

                                Button(attrs = {
                                    classesList(
                                        "w-full", "py-3.5", "rounded-xl", "text-sm", "font-bold", "text-white", 
                                        "transition-all", "flex", "items-center", "justify-center", "gap-2",
                                        if (isCoachAnalyzing) "bg-slate-800 cursor-not-allowed" else "bg-indigo-600 hover:bg-indigo-500 shadow-lg shadow-indigo-600/10"
                                    )
                                    onClick {
                                        if (!isCoachAnalyzing) {
                                            isCoachAnalyzing = true
                                            showCoachResults = false
                                            coachSuccessFeedback = false

                                            // Simulate async analysis delay using a window timer in JS
                                            window.setTimeout({
                                                isCoachAnalyzing = false
                                                showCoachResults = true
                                                
                                                // Generate mock AI output based on chosen role
                                                when (coachSelectedRole) {
                                                    "Engenheiro DevOps" -> {
                                                        coachMatchScore = 72
                                                        coachSuggestions = "Seu portfólio tem excelentes bases de Cloud e Redes, mas para DevOps é altamente recomendado demonstrar conhecimento direto em IaC (Infraestrutura como Código) e GitOps para automatização de deployments."
                                                        coachMissingSkills = listOf("Terraform", "GitOps (ArgoCD)", "Prometheus", "Helm")
                                                        coachSuggestedRole = "Sênior Cloud & DevOps Engineer"
                                                        coachSuggestedBio = "Especialista DevOps com sólida experiência em automação de infraestrutura híbrida corporativa, design de arquiteturas AWS escaláveis, segurança em redes Cisco/Fortinet e implantação de orquestradores de contêineres como Kubernetes (EKS) utilizando práticas de GitOps e IaC (Terraform)."
                                                        coachSuggestedCompany = "Tech Solution Corp"
                                                        coachSuggestedJobRole = "Engenheiro DevOps Specialist"
                                                        coachSuggestedExpDesc = "Automatizei e gerenciei o ciclo de vida completo de deploys e infraestrutura corporativa na AWS com Terraform (IaC) e Helm, integrando segurança de borda Fortinet e migrando microsserviços legados para arquitetura de contêineres Kubernetes com GitOps."
                                                        coachSuggestedExpOrigDesc = "Criação de máquinas na AWS, configuração de redes corporativas Cisco e segurança básica de servidores Linux."
                                                    }
                                                    "Desenvolvedor Back-end" -> {
                                                        coachMatchScore = 65
                                                        coachSuggestions = "Muito bom conhecimento geral em redes e Kubernetes, mas para atuar como Back-end faltam linguagens e frameworks típicos de aplicações corporativas, além de filas/mensageria."
                                                        coachMissingSkills = listOf("Spring Boot", "PostgreSQL", "Redis", "gRPC")
                                                        coachSuggestedRole = "Desenvolvedor Back-end Sênior | Java & Spring Boot"
                                                        coachSuggestedBio = "Engenheiro de Software especializado no desenvolvimento de APIs de alta performance com Spring Boot, Java e Go, orquestração de contêineres via Kubernetes e implementação de mensageria com Redis e Apache Kafka para arquiteturas distribuídas e resilientes."
                                                        coachSuggestedCompany = "Global Tech Corp"
                                                        coachSuggestedJobRole = "Desenvolvedor Back-end Sênior"
                                                        coachSuggestedExpDesc = "Construí APIs altamente disponíveis no back-end corporativo utilizando Spring Boot, Go e PostgreSQL, implementando filas assíncronas com mensageria distribuída para processamento de transações em tempo real e deploys escaláveis em infraestrutura Kubernetes."
                                                        coachSuggestedExpOrigDesc = "Desenvolvimento de APIs internas simples, manutenção de scripts e monitoramento básico de servidores."
                                                    }
                                                    "Desenvolvedor Front-end" -> {
                                                        coachMatchScore = 58
                                                        coachSuggestions = "O portfólio tem foco extremo em Cloud e redes. Para atuar no Front-end de alto nível, sugerimos reforçar conhecimento em frameworks reativos modernos de CSS e SPA/SSR."
                                                        coachMissingSkills = listOf("TypeScript", "Tailwind CSS", "Next.js", "Redux Toolkit")
                                                        coachSuggestedRole = "Desenvolvedor Front-end Specialist | React & Next.js"
                                                        coachSuggestedBio = "Desenvolvedor focado em interfaces modernas de altíssimo desempenho com React, Next.js e TypeScript, estilizadas com Tailwind CSS e integrando estados globais complexos para portfólios e dashboards corporativos responsivos."
                                                        coachSuggestedCompany = "Creative Web Solutions"
                                                        coachSuggestedJobRole = "Desenvolvedor Front-end Specialist"
                                                        coachSuggestedExpDesc = "Liderei o desenvolvimento de interfaces responsivas para painéis corporativos complexos usando React, Next.js e TypeScript, garantindo otimização de SEO de ponta a ponta e integração direta com APIs RESTful no back-end."
                                                        coachSuggestedExpOrigDesc = "Criação de páginas web corporativas estáticas, suporte básico de CSS e customização de templates."
                                                    }
                                                    "Desenvolvedor Full-stack" -> {
                                                        coachMatchScore = 60
                                                        coachSuggestions = "Você tem bases ótimas de infraestrutura e conteinerização. Um desenvolvedor fullstack moderno precisa balancear isso com bibliotecas robustas de banco de dados e componentização."
                                                        coachMissingSkills = listOf("PostgreSQL", "TypeScript", "Next.js", "Docker")
                                                        coachSuggestedRole = "Desenvolvedor Full-stack Sênior | Node.js & React"
                                                        coachSuggestedBio = "Engenheiro Full-stack especializado na modelagem de microsserviços integrados no back-end e desenvolvimento de interfaces fluidas e dinâmicas com React e TypeScript no front-end, operando com excelência em Docker e Kubernetes."
                                                        coachSuggestedCompany = "Digital Enterprise Inc"
                                                        coachSuggestedJobRole = "Desenvolvedor Full-stack Sênior"
                                                        coachSuggestedExpDesc = "Desenvolvi aplicações web completas ponta a ponta, implementando APIs RESTful seguras no back-end em Node.js e telas modernas com React e Tailwind, garantindo integração com bancos relacionais e contêineres Docker."
                                                        coachSuggestedExpOrigDesc = "Desenvolvimento de páginas administrativas simples, scripts shell e manutenção em bancos SQL locais."
                                                    }
                                                    "Analista de Infraestrutura" -> {
                                                        coachMatchScore = 95
                                                        coachSuggestions = "Excepcional aderência à vaga de Infraestrutura! Você domina redes Fortinet, Cisco, Linux e AWS. Sugerimos apenas focar em boas práticas de governança de TI e virtualização local."
                                                        coachMissingSkills = listOf("Virtualização (VMware)", "AWS Cloud", "Linux Administration", "Fortinet Firewalls")
                                                        coachSuggestedRole = "Arquiteto de Infraestrutura, Cloud & SecOps Sênior"
                                                        coachSuggestedBio = "Especialista sênior certificado Cisco e Fortinet, focado no design de arquiteturas de rede hibridas resilientes, administração profunda de servidores Linux RedHat/CentOS e securitização física e lógica contra ameaças virtuais."
                                                        coachSuggestedCompany = "Core Telecom & Security"
                                                        coachSuggestedJobRole = "Arquiteto de Infraestrutura SecOps"
                                                        coachSuggestedExpDesc = "Planejei e executei o redesign completo da rede corporativa multizona integrando switches Cisco e firewalls Fortinet de alta disponibilidade, reduzindo vulnerabilidades em 95% e mantendo infraestrutura Linux/AWS sob auditoria de conformidade."
                                                        coachSuggestedExpOrigDesc = "Instalação física de roteadores, suporte básico a usuários de rede e configuração inicial de firewalls."
                                                    }
                                                    else -> {
                                                        coachMatchScore = 70
                                                        coachSuggestions = "Kotlin/Jetpack Compose já constam em suas habilidades, o que é ótimo para Mobile! Sugerimos expandir para multiplataforma geral de forma a maximizar sua portabilidade."
                                                        coachMissingSkills = listOf("Kotlin Multiplatform", "SwiftUI", "Jetpack Compose", "App Store Deployment")
                                                        coachSuggestedRole = "Desenvolvedor Mobile Kotlin / KMP Specialist"
                                                        coachSuggestedBio = "Engenheiro Mobile apaixonado por desenvolvimento multiplataforma nativo utilizando Kotlin Multiplatform (KMP), Compose Multiplatform e SwiftUI, aplicando Clean Architecture e Jetpack Compose na plataforma Android."
                                                        coachSuggestedCompany = "Mobile First Studio"
                                                        coachSuggestedJobRole = "Engenheiro Mobile Sênior"
                                                        coachSuggestedExpDesc = "Construí e lancei aplicativos móveis escaláveis de alta fidelidade com Jetpack Compose e SwiftUI, projetando camadas modulares em Kotlin Multiplatform (KMP) e gerenciando pipelines automatizados de publicação para Google Play e App Store."
                                                        coachSuggestedExpOrigDesc = "Desenvolvimento de pequenos módulos em Android nativo com layouts clássicos e testes manuais de aplicativos."
                                                    }
                                                }
                                            }, 1200)
                                        }
                                    }
                                }) {
                                    if (isCoachAnalyzing) {
                                        Span(attrs = { classes("animate-spin", "text-lg") }) { Text("⏳") }
                                        Text("Analisando Estruturas & Projetos...")
                                    } else {
                                        Text("🚀 Iniciar Análise por IA")
                                    }
                                }
                            }

                            // Analysis Results View
                            if (showCoachResults) {
                                Div(attrs = { classes("bg-slate-900/60", "rounded-2xl", "p-6", "border", "border-slate-800", "space-y-6", "animate-fade-in") }) {
                                    H3(attrs = { classes("text-lg", "font-bold", "text-white", "flex", "items-center", "gap-2") }) {
                                        Text("🔍 Resultado do Coach de IA para: $coachSelectedRole")
                                    }

                                    // Floating Copy Success Toast
                                    if (copySuccessText != null) {
                                        Div(attrs = { classes("bg-emerald-500/10", "border", "border-emerald-500/30", "text-emerald-300", "p-3.5", "rounded-xl", "text-xs", "font-bold", "flex", "items-center", "justify-center", "gap-2") }) {
                                            Text("📋 " + copySuccessText!!)
                                        }
                                    }

                                    // Match Score Indicator
                                    Div(attrs = { classes("flex", "items-center", "gap-4", "bg-slate-950/40", "p-4", "rounded-xl") }) {
                                        // Circular score meter representation
                                        Div(attrs = { 
                                            classes(
                                                "w-16", "h-16", "rounded-full", "border-4", 
                                                if (coachMatchScore >= 80) "border-emerald-500" else if (coachMatchScore >= 60) "border-indigo-500" else "border-amber-500",
                                                "flex", "items-center", "justify-center", "font-black", "text-lg", "text-white"
                                            ) 
                                        }) {
                                            Text("$coachMatchScore%")
                                        }
                                        Div {
                                            P(attrs = { classes("text-sm", "font-bold", "text-white") }) {
                                                Text("Aderência com a Vaga")
                                            }
                                            P(attrs = { classes("text-xs", "text-slate-400") }) {
                                                Text("Match baseado na listagem atual de habilidades e competências.")
                                            }
                                        }
                                    }

                                    // Written Suggestions Card with individual Copy text
                                    Div(attrs = { classes("space-y-2") }) {
                                        Div(attrs = { classes("flex", "justify-between", "items-center") }) {
                                            P(attrs = { classes("text-xs", "font-extrabold", "text-slate-400", "uppercase", "tracking-wider") }) {
                                                Text("💡 Sugestões de Melhorias:")
                                            }
                                            Button(attrs = {
                                                classes("text-[10px]", "font-bold", "text-indigo-400", "hover:text-indigo-300", "bg-slate-800", "px-2.5", "py-1", "rounded-md", "transition-colors")
                                                onClick {
                                                    window.navigator.clipboard.writeText(coachSuggestions)
                                                    copySuccessText = "Sugestões de melhorias copiadas!"
                                                    window.setTimeout({ copySuccessText = null }, 2000)
                                                }
                                            }) {
                                                Text("📋 Copiar Texto")
                                            }
                                        }
                                        P(attrs = { classes("text-sm", "text-slate-300", "leading-relaxed", "bg-slate-950/20", "p-4", "rounded-xl", "border", "border-slate-800/80") }) {
                                            Text(coachSuggestions)
                                        }
                                    }

                                    // Título & Resumo Otimizado (Before vs Suggested)
                                    Div(attrs = { classes("bg-slate-950/30", "p-5", "rounded-xl", "border", "border-slate-800/80", "space-y-4") }) {
                                        H4(attrs = { classes("text-xs", "font-extrabold", "text-slate-400", "uppercase", "tracking-wider", "flex", "items-center", "gap-1.5") }) {
                                            Text("👤 Otimização de Título & Resumo")
                                        }
                                        Div(attrs = { classes("grid", "grid-cols-1", "md:grid-cols-2", "gap-4") }) {
                                            // Before (Current)
                                            Div(attrs = { classes("space-y-2", "opacity-75") }) {
                                                P(attrs = { classes("text-[10px]", "font-bold", "text-slate-500") }) { Text("Antes (Atual):") }
                                                P(attrs = { classes("text-xs", "font-bold", "text-white") }) { Text(roleState) }
                                                P(attrs = { classes("text-[11px]", "text-slate-400", "leading-relaxed") }) { Text(bioState) }
                                            }
                                            // After (Suggested)
                                            Div(attrs = { classes("space-y-2", "border-t", "md:border-t-0", "md:border-l", "border-slate-800/80", "pt-4", "md:pt-0", "md:pl-4") }) {
                                                P(attrs = { classes("text-[10px]", "font-bold", accentColor.textClass) }) { Text("Depois (Sugerido por IA):") }
                                                P(attrs = { classes("text-xs", "font-bold", "text-emerald-400") }) { Text(coachSuggestedRole) }
                                                P(attrs = { classes("text-[11px]", "text-slate-300", "leading-relaxed") }) { Text(coachSuggestedBio) }
                                            }
                                        }
                                        // Action buttons
                                        Div(attrs = { classes("flex", "flex-col", "sm:flex-row", "gap-2.5", "pt-2") }) {
                                            Button(attrs = {
                                                classes("flex-1", "py-2", "rounded-lg", "text-xs", "font-bold", "bg-indigo-600/20", "hover:bg-indigo-600/40", "text-indigo-300", "border", "border-indigo-500/20", "transition-all")
                                                onClick {
                                                    roleState = coachSuggestedRole
                                                    bioState = coachSuggestedBio
                                                    copySuccessText = "Título e Resumo aplicados com sucesso!"
                                                    window.setTimeout({ copySuccessText = null }, 2000)
                                                }
                                            }) {
                                                Text("✍️ Substituir Dados no Portfólio")
                                            }
                                            Button(attrs = {
                                                classes("py-2", "px-4", "rounded-lg", "text-xs", "font-bold", "bg-slate-800", "hover:bg-slate-700", "text-slate-300", "transition-all")
                                                onClick {
                                                    val copyText = "Cargo Alvo: $coachSuggestedRole\nResumo:\n$coachSuggestedBio"
                                                    window.navigator.clipboard.writeText(copyText)
                                                    copySuccessText = "Título e Bio copiados!"
                                                    window.setTimeout({ copySuccessText = null }, 2000)
                                                }
                                            }) {
                                                Text("📋 Copiar Texto")
                                            }
                                        }
                                    }

                                    // Experiência Otimizada (Before vs Suggested)
                                    if (coachSuggestedCompany.isNotBlank()) {
                                        Div(attrs = { classes("bg-slate-950/30", "p-5", "rounded-xl", "border", "border-slate-800/80", "space-y-4") }) {
                                            H4(attrs = { classes("text-xs", "font-extrabold", "text-slate-400", "uppercase", "tracking-wider") }) {
                                                Text("💼 Experiência Otimizada por IA")
                                            }
                                            Div(attrs = { classes("space-y-3") }) {
                                                P(attrs = { classes("text-xs", "font-bold", "text-white") }) {
                                                    Text(coachSuggestedCompany + " • " + coachSuggestedJobRole)
                                                }
                                                Div(attrs = { classes("grid", "grid-cols-1", "md:grid-cols-2", "gap-4") }) {
                                                    Div(attrs = { classes("space-y-1.5", "opacity-75") }) {
                                                        P(attrs = { classes("text-[10px]", "font-bold", "text-slate-500") }) { Text("Descrição Atual:") }
                                                        P(attrs = { classes("text-[11px]", "text-slate-400", "leading-relaxed") }) { Text(coachSuggestedExpOrigDesc) }
                                                    }
                                                    Div(attrs = { classes("space-y-1.5", "border-t", "md:border-t-0", "md:border-l", "border-slate-800/80", "pt-3", "md:pt-0", "md:pl-4") }) {
                                                        P(attrs = { classes("text-[10px]", "font-bold", accentColor.textClass) }) { Text("Descrição Otimizada por IA:") }
                                                        P(attrs = { classes("text-[11px]", "text-slate-300", "leading-relaxed") }) { Text(coachSuggestedExpDesc) }
                                                    }
                                                }
                                            }
                                            // Action buttons
                                            Div(attrs = { classes("flex", "flex-col", "sm:flex-row", "gap-2.5", "pt-2") }) {
                                                Button(attrs = {
                                                    classes("flex-1", "py-2", "rounded-lg", "text-xs", "font-bold", "bg-indigo-600/20", "hover:bg-indigo-600/40", "text-indigo-300", "border", "border-indigo-500/20", "transition-all")
                                                    onClick {
                                                        val updatedExps = experiencesState.toMutableList()
                                                        val existingIndex = updatedExps.indexOfFirst { it.company.contains(coachSuggestedCompany, ignoreCase = true) || it.role.contains(coachSuggestedJobRole, ignoreCase = true) }
                                                        if (existingIndex >= 0) {
                                                            updatedExps[existingIndex] = Experience(coachSuggestedJobRole, updatedExps[existingIndex].period, coachSuggestedExpDesc)
                                                        } else {
                                                            updatedExps.add(Experience(coachSuggestedJobRole, "Atual", coachSuggestedExpDesc))
                                                        }
                                                        experiencesState = updatedExps
                                                        copySuccessText = "Experiência atualizada no portfólio!"
                                                        window.setTimeout({ copySuccessText = null }, 2000)
                                                    }
                                                }) {
                                                    Text("✍️ Substituir Experiência no Portfólio")
                                                }
                                                Button(attrs = {
                                                    classes("py-2", "px-4", "rounded-lg", "text-xs", "font-bold", "bg-slate-800", "hover:bg-slate-700", "text-slate-300", "transition-all")
                                                    onClick {
                                                        window.navigator.clipboard.writeText("Empresa: $coachSuggestedCompany\nCargo: $coachSuggestedJobRole\nDescrição Otimizada: $coachSuggestedExpDesc")
                                                        copySuccessText = "Experiência copiada!"
                                                        window.setTimeout({ copySuccessText = null }, 2000)
                                                    }
                                                }) {
                                                    Text("📋 Copiar Texto")
                                                }
                                            }
                                        }
                                    }

                                    // Missing Skills suggestions
                                    Div(attrs = { classes("space-y-3") }) {
                                        P(attrs = { classes("text-xs", "font-extrabold", "text-slate-400", "uppercase", "tracking-wider") }) {
                                            Text("🛠️ Habilidades Recomendadas para Adicionar:")
                                        }
                                        Div(attrs = { classes("flex", "flex-wrap", "gap-2") }) {
                                            coachMissingSkills.forEach { skill ->
                                                Span(attrs = { 
                                                    classes("px-3", "py-1.5", "rounded-lg", "text-xs", "font-semibold", "bg-indigo-500/10", "text-indigo-300", "border", "border-indigo-500/20") 
                                                }) {
                                                    Text("+ $skill")
                                                }
                                            }
                                        }
                                    }

                                    // CTA to Auto-Apply Improvements
                                    Div(attrs = { classes("space-y-3", "pt-4", "border-t", "border-slate-800/80") }) {
                                        if (coachSuccessFeedback) {
                                            Div(attrs = { classes("bg-emerald-500/10", "border", "border-emerald-500/20", "text-emerald-300", "p-3", "rounded-xl", "text-center", "text-xs", "font-bold") }) {
                                                Text("✅ Habilidades incorporadas com sucesso! Verifique a aba 'Portfólio'.")
                                            }
                                        } else {
                                            Button(attrs = {
                                                classes("w-full", "py-3", "rounded-xl", "text-xs", "font-bold", "bg-emerald-600", "hover:bg-emerald-500", "text-white", "transition-all")
                                                onClick {
                                                    val currentSet = skillsState.toSet()
                                                    val newSet = currentSet + coachMissingSkills
                                                    skillsState = newSet.toList()
                                                    coachSuccessFeedback = true
                                                    copySuccessText = "Habilidades recomendadas adicionadas!"
                                                    window.setTimeout({ copySuccessText = null }, 2000)
                                                }
                                            }) {
                                                Text("✨ Aplicar Sugestões de Habilidades no Currículo")
                                            }
                                        }

                                        // Global Actions Card
                                        Div(attrs = { classes("grid", "grid-cols-1", "sm:grid-cols-2", "gap-3", "pt-1") }) {
                                            Button(attrs = {
                                                classes("py-3", "rounded-xl", "text-xs", "font-bold", "bg-indigo-600", "hover:bg-indigo-500", "text-white", "transition-all")
                                                onClick {
                                                    // Apply ALL improvements
                                                    // Role & Bio
                                                    roleState = coachSuggestedRole
                                                    bioState = coachSuggestedBio
                                                    // Skills
                                                    val currentSet = skillsState.toSet()
                                                    val newSet = currentSet + coachMissingSkills
                                                    skillsState = newSet.toList()
                                                    // Experience
                                                    val updatedExps = experiencesState.toMutableList()
                                                    val existingIndex = updatedExps.indexOfFirst { it.company.contains(coachSuggestedCompany, ignoreCase = true) || it.role.contains(coachSuggestedJobRole, ignoreCase = true) }
                                                    if (existingIndex >= 0) {
                                                        updatedExps[existingIndex] = Experience(coachSuggestedJobRole, updatedExps[existingIndex].period, coachSuggestedExpDesc)
                                                    } else {
                                                        updatedExps.add(Experience(coachSuggestedJobRole, "Atual", coachSuggestedExpDesc))
                                                    }
                                                    experiencesState = updatedExps
                                                    
                                                    coachSuccessFeedback = true
                                                    copySuccessText = "Todas as melhorias aplicadas!"
                                                    window.setTimeout({ copySuccessText = null }, 2000)
                                                }
                                            }) {
                                                Text("⚡ Aplicar Todas as Sugestões")
                                            }

                                            Button(attrs = {
                                                classes("py-3", "rounded-xl", "text-xs", "font-bold", "bg-slate-800", "hover:bg-slate-700", "text-slate-300", "border", "border-slate-700/60", "transition-all")
                                                onClick {
                                                    val completeReport = """
                                                        === RELATÓRIO DO COACH DE CARREIRA IA ===
                                                        Vaga Alvo: $coachSelectedRole
                                                        Match Score: $coachMatchScore%
                                                        
                                                        SUGESTÕES GERAIS:
                                                        $coachSuggestions
                                                        
                                                        HABILIDADES RECOMENDADAS:
                                                        ${coachMissingSkills.joinToString(", ")}
                                                        
                                                        TÍTULO & RESUMO SUGERIDOS:
                                                        Novo Título: $coachSuggestedRole
                                                        Novo Resumo: $coachSuggestedBio
                                                        
                                                        EXPERIÊNCIA RECOMENDADA:
                                                        Empresa/Cargo: $coachSuggestedCompany - $coachSuggestedJobRole
                                                        Descrição Otimizada: $coachSuggestedExpDesc
                                                    """.trimIndent()
                                                    
                                                    window.navigator.clipboard.writeText(completeReport)
                                                    copySuccessText = "Relatório completo de IA copiado!"
                                                    window.setTimeout({ copySuccessText = null }, 2000)
                                                }
                                            }) {
                                                Text("📋 Copiar Relatório Completo")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "settings" -> {
                        // --- PAINEL DE SERVIÇOS & AJUSTES VIEW ---
                        Div(attrs = { classes("space-y-12") }) {
                            // Section: Cloud Sync & Firebase
                            Div(attrs = { classes("bg-slate-900/60", "rounded-2xl", "p-6", "border", "border-slate-800", "space-y-6") }) {
                                H3(attrs = { classes("text-base", "font-bold", "text-white", "border-b", "border-slate-800", "pb-3", "flex", "items-center", "gap-2") }) {
                                    Text("🔥 Sincronização na Nuvem (Firebase)")
                                }

                                val user = currentUser
                                if (user == null) {
                                    Div(attrs = { classes("bg-slate-950/40", "p-5", "rounded-2xl", "border", "border-slate-800/80", "space-y-4", "text-center") }) {
                                        P(attrs = { classes("text-xs", "text-slate-400", "leading-relaxed") }) {
                                            Text("Sincronize seu portfólio em tempo real com o Firebase Cloud Firestore e gerencie múltiplos currículos na nuvem de forma prática.")
                                        }
                                        Div(attrs = { classes("flex", "flex-col", "sm:flex-row", "gap-3", "justify-center") }) {
                                            // Simulated Google Sign-In button
                                            Button(attrs = {
                                                classes("px-4", "py-2.5", "rounded-xl", "text-xs", "font-bold", "bg-white", "hover:bg-slate-100", "text-slate-900", "transition-all", "flex", "items-center", "justify-center", "gap-2")
                                                onClick { showGoogleIdentityModal = true }
                                            }) {
                                                Span { Text("🔑") }
                                                Text("Entrar com Google (Firebase)")
                                            }
                                            // Test account button
                                            Button(attrs = {
                                                classes("px-4", "py-2.5", "rounded-xl", "text-xs", "font-bold", "bg-slate-800", "hover:bg-slate-700", "text-slate-300", "border", "border-slate-700/60", "transition-all")
                                                onClick { showLoginDialog = true }
                                            }) {
                                                Text("Acessar com Conta de Teste")
                                            }
                                        }
                                    }
                                } else {
                                    // Signed in view: Firebase Panel
                                    Div(attrs = { classes("space-y-6") }) {
                                        // User card info
                                        Div(attrs = { classes("flex", "flex-col", "sm:flex-row", "justify-between", "items-start", "sm:items-center", "gap-4", "bg-slate-950/60", "p-4", "rounded-xl", "border", "border-slate-800/80") }) {
                                            Div(attrs = { classes("flex", "items-center", "gap-3") }) {
                                                if (user.photoUrl != null) {
                                                    Img(src = user.photoUrl, alt = user.name, attrs = {
                                                        classes("w-10", "h-10", "rounded-full", "border", "border-slate-700")
                                                    })
                                                } else {
                                                    Div(attrs = { 
                                                        classesList("w-10", "h-10", "rounded-full", "bg-gradient-to-tr", "flex", "items-center", "justify-center", "text-sm", "font-bold", "text-white", accentColor.bgGradient) 
                                                    }) {
                                                        Text(user.name.take(1).uppercase())
                                                    }
                                                }
                                                Div {
                                                    P(attrs = { classes("text-sm", "font-bold", "text-white") }) {
                                                        Text(user.name)
                                                    }
                                                    P(attrs = { classes("text-xs", "text-slate-400") }) {
                                                        Text(user.email)
                                                    }
                                                    Span(attrs = { 
                                                        classesList(
                                                            "mt-1", "inline-block", "px-2", "py-0.5", "rounded-full", "text-[10px]", "font-bold",
                                                            if (user.isSimulated) "bg-amber-500/10 text-amber-300 border border-amber-500/20" else "bg-emerald-500/10 text-emerald-300 border border-emerald-500/20"
                                                        ) 
                                                    }) {
                                                        Text(if (user.isSimulated) "Modo Backup Local" else "Autenticação via Google Firebase")
                                                    }
                                                }
                                            }
                                            Button(attrs = {
                                                classes("px-3", "py-1.5", "rounded-lg", "text-xs", "font-semibold", "bg-rose-500/10", "hover:bg-rose-500/20", "text-rose-400", "border", "border-rose-500/20", "transition-all")
                                                onClick { currentUser = null }
                                            }) {
                                                Text("Desconectar")
                                            }
                                        }

                                        // Sync feedback
                                        syncSuccessMessage?.let { msg ->
                                            Div(attrs = { classes("bg-emerald-500/10", "border", "border-emerald-500/20", "text-emerald-300", "p-4", "rounded-xl", "text-xs", "font-semibold") }) {
                                                Text(msg)
                                            }
                                        }

                                        // Multi-resume slots selection
                                        Div(attrs = { classes("space-y-3", "bg-slate-950/20", "p-4", "rounded-xl", "border", "border-slate-800/60") }) {
                                            P(attrs = { classes("text-xs", "font-bold", "text-slate-300") }) {
                                                Text("Selecione o Slot / Identificador de Backup:")
                                            }
                                            Div(attrs = { classes("flex", "flex-col", "sm:flex-row", "gap-3") }) {
                                                Select(attrs = {
                                                    classes("bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-2.5", "text-xs", "text-white", "focus:outline-none")
                                                    onChange { event ->
                                                        event.value?.let { selectedBackupId = it }
                                                    }
                                                }) {
                                                    savedBackups.forEach { backupId ->
                                                        Option(backupId, attrs = { if (selectedBackupId == backupId) selected() }) { Text("Slot: $backupId") }
                                                    }
                                                }

                                                if (showBackupNameInput) {
                                                    Input(type = InputType.Text, attrs = {
                                                        classes("bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-2.5", "text-xs", "text-white", "placeholder-slate-600", "focus:outline-none")
                                                        placeholder("Nome do Backup (ex: DevOps)")
                                                        value(newBackupName)
                                                        onInput { event -> newBackupName = event.value }
                                                    })
                                                    Button(attrs = {
                                                        classesList("px-3", "py-2", "rounded-xl", "text-xs", "font-bold", "text-white", "transition-all", accentColor.buttonBg)
                                                        onClick {
                                                            val cleanName = newBackupName.trim().replace(" ", "_")
                                                            if (cleanName.isNotBlank() && !savedBackups.contains(cleanName)) {
                                                                savedBackups = savedBackups + cleanName
                                                                selectedBackupId = cleanName
                                                                // Persist updated backups list in localStorage
                                                                window.localStorage.setItem("cloud_resumes_list_${user.uid}", savedBackups.joinToString("|"))
                                                            }
                                                            showBackupNameInput = false
                                                            newBackupName = ""
                                                        }
                                                    }) {
                                                        Text("Salvar Slot")
                                                    }
                                                } else {
                                                    Button(attrs = {
                                                        classes("px-3", "py-2", "rounded-xl", "text-xs", "font-bold", "bg-slate-800", "hover:bg-slate-700", "text-slate-300", "border", "border-slate-700/60", "transition-all")
                                                        onClick { showBackupNameInput = true }
                                                    }) {
                                                        Text("+ Novo Slot")
                                                    }
                                                }
                                            }

                                            // Dashboard core sync buttons (Matches mobile Firebase Sync upload/download/delete)
                                            Div(attrs = { classes("grid", "grid-cols-1", "sm:grid-cols-3", "gap-3", "pt-2") }) {
                                                // Upload / Enviar para o Firebase
                                                Button(attrs = {
                                                    classesList("py-2.5", "rounded-xl", "text-xs", "font-bold", "text-white", "transition-all", "flex", "items-center", "justify-center", "gap-2", accentColor.buttonBg)
                                                    onClick {
                                                        try {
                                                            // Save fields to localStorage prefixed with current uid & resumeId to simulate Firestore Sync
                                                            val prefix = "cloud_data_${user.uid}_${selectedBackupId}_"
                                                            window.localStorage.setItem(prefix + "name", nameState)
                                                            window.localStorage.setItem(prefix + "role", roleState)
                                                            window.localStorage.setItem(prefix + "bio", bioState)
                                                            window.localStorage.setItem(prefix + "email", emailState)
                                                            window.localStorage.setItem(prefix + "github", githubUser)
                                                            window.localStorage.setItem(prefix + "accent", accentColor.name)
                                                            window.localStorage.setItem(prefix + "skills", skillsState.joinToString("|"))
                                                            window.localStorage.setItem(prefix + "sections", webSections.joinToString("|"))
                                                            window.localStorage.setItem(prefix + "experiences", experiencesState.joinToString("|||") { "${it.title}::${it.period}::${it.description}" })
                                                            window.localStorage.setItem(prefix + "certifications", certificationsState.joinToString("|||") { "${it.title}::${it.category}::${it.duration}::${it.cost}::${it.description}" })
                                                            
                                                            syncSuccessMessage = "Sincronizado na nuvem Firebase sob o ID '$selectedBackupId' com sucesso!"
                                                        } catch (e: Exception) {
                                                            syncErrorMessage = "Erro de sincronização Firebase: " + e.message
                                                        }
                                                    }
                                                }) {
                                                    Text("☁️ Enviar para Nuvem")
                                                }

                                                // Download / Baixar do Firebase
                                                Button(attrs = {
                                                    classes("py-2.5", "rounded-xl", "text-xs", "font-bold", "bg-indigo-900/40", "hover:bg-indigo-900/60", "text-indigo-200", "border", "border-indigo-800/40", "transition-all")
                                                    onClick {
                                                        try {
                                                            val prefix = "cloud_data_${user.uid}_${selectedBackupId}_"
                                                            val nameVal = window.localStorage.getItem(prefix + "name")
                                                            if (nameVal != null) {
                                                                nameState = nameVal
                                                                window.localStorage.getItem(prefix + "role")?.let { roleState = it }
                                                                window.localStorage.getItem(prefix + "bio")?.let { bioState = it }
                                                                window.localStorage.getItem(prefix + "email")?.let { emailState = it }
                                                                window.localStorage.getItem(prefix + "github")?.let { githubUser = it }
                                                                window.localStorage.getItem(prefix + "accent")?.let {
                                                                    try { accentColor = AccentColor.valueOf(it) } catch(e: Exception){}
                                                                }
                                                                window.localStorage.getItem(prefix + "skills")?.let {
                                                                    skillsState = it.split("|").filter { it.isNotBlank() }
                                                                }
                                                                window.localStorage.getItem(prefix + "sections")?.let {
                                                                    webSections = it.split("|").filter { it.isNotBlank() }
                                                                }
                                                                window.localStorage.getItem(prefix + "experiences")?.let { expVal ->
                                                                    if (expVal.isNotBlank()) {
                                                                        experiencesState = expVal.split("|||").map {
                                                                            val parts = it.split("::")
                                                                            Experience(parts[0], parts[1], parts.getOrElse(2) { "" })
                                                                        }
                                                                    }
                                                                }
                                                                window.localStorage.getItem(prefix + "certifications")?.let { certVal ->
                                                                    if (certVal.isNotBlank()) {
                                                                        certificationsState = certVal.split("|||").map {
                                                                            val parts = it.split("::")
                                                                            Certification(parts[0], parts[1], parts[2], parts[3], parts.getOrElse(4) { "" })
                                                                        }
                                                                    }
                                                                }
                                                                syncSuccessMessage = "Slot '$selectedBackupId' recuperado da nuvem Firebase com sucesso!"
                                                            } else {
                                                                window.alert("Nenhum dado encontrado para o slot '$selectedBackupId' na nuvem. Envie os dados primeiro!")
                                                            }
                                                        } catch (e: Exception) {
                                                            window.alert("Erro ao recuperar: " + e.message)
                                                        }
                                                    }
                                                }) {
                                                    Text("📥 Baixar da Nuvem")
                                                }

                                                // Delete / Excluir do Firebase
                                                Button(attrs = {
                                                    classes("py-2.5", "rounded-xl", "text-xs", "font-bold", "bg-rose-900/20", "hover:bg-rose-900/40", "text-rose-400", "border", "border-rose-900/30", "transition-all")
                                                    onClick {
                                                        if (window.confirm("Excluir definitivamente o slot '$selectedBackupId' do Firebase Cloud Firestore?")) {
                                                            val prefix = "cloud_data_${user.uid}_${selectedBackupId}_"
                                                            window.localStorage.removeItem(prefix + "name")
                                                            window.localStorage.removeItem(prefix + "role")
                                                            window.localStorage.removeItem(prefix + "bio")
                                                            window.localStorage.removeItem(prefix + "email")
                                                            window.localStorage.removeItem(prefix + "github")
                                                            window.localStorage.removeItem(prefix + "accent")
                                                            window.localStorage.removeItem(prefix + "skills")
                                                            window.localStorage.removeItem(prefix + "sections")
                                                            window.localStorage.removeItem(prefix + "experiences")
                                                            window.localStorage.removeItem(prefix + "certifications")
                                                            syncSuccessMessage = "Backup do slot '$selectedBackupId' removido da nuvem."
                                                        }
                                                    }
                                                }) {
                                                    Text("❌ Limpar do Firebase")
                                                }
                                            }
                                        }

                                        // Clickable Slot backups grid list
                                        Div(attrs = { classes("space-y-2") }) {
                                            P(attrs = { classes("text-xs", "font-bold", "text-slate-400") }) {
                                                Text("Listagem de Currículos no Firebase:")
                                            }
                                            Div(attrs = { classes("grid", "grid-cols-2", "sm:grid-cols-4", "gap-3") }) {
                                                savedBackups.forEach { bId ->
                                                    Button(attrs = {
                                                        classes(
                                                            "p-3.5", "rounded-xl", "text-xs", "font-bold", "text-left", "transition-all", "border",
                                                            if (selectedBackupId == bId) "bg-indigo-500/10 border-indigo-500/40 text-indigo-300" else "bg-slate-950/40 border-slate-800 text-slate-400 hover:text-white"
                                                        )
                                                        onClick { selectedBackupId = bId }
                                                    }) {
                                                        P(attrs = { classes("font-bold", "truncate") }) { Text(bId) }
                                                        P(attrs = { classes("text-[9px]", "text-slate-500") }) { Text("Ativar Slot") }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Section: Theme Design
                            Div(attrs = { classes("bg-slate-900/60", "rounded-2xl", "p-6", "border", "border-slate-800", "space-y-6") }) {
                                H3(attrs = { classes("text-base", "font-bold", "text-white", "border-b", "border-slate-800", "pb-3") }) {
                                    Text("🎨 Tema & Design do Web Portfólio")
                                }
                                Div(attrs = { classes("space-y-4") }) {
                                    P(attrs = { classes("text-xs", "font-bold", "text-slate-400") }) {
                                        Text("Escolha a cor de destaque (Accent Color):")
                                    }
                                    Div(attrs = { classes("grid", "grid-cols-2", "sm:grid-cols-5", "gap-3") }) {
                                        AccentColor.values().forEach { color ->
                                            Button(attrs = {
                                                classesList(
                                                    "p-3", "rounded-xl", "text-xs", "font-bold", "transition-all", "border", "flex", "flex-col", "items-center", "gap-1.5",
                                                    if (accentColor == color) "border-white bg-slate-800 text-white shadow-md shadow-white/5" else "border-slate-800 bg-slate-950/60 text-slate-400 hover:text-white"
                                                )
                                                onClick { accentColor = color }
                                            }) {
                                                Div(attrs = { 
                                                    classes("w-4", "h-4", "rounded-full") 
                                                    style { property("background-color", color.primaryHex) }
                                                })
                                                Text(color.namePt)
                                            }
                                        }
                                    }
                                }
                            }

                            // Section: Layout Manager (Gestão de Layout / Reordenar Seções)
                            Div(attrs = { classes("bg-slate-900/60", "rounded-2xl", "p-6", "border", "border-slate-800", "space-y-6") }) {
                                Div(attrs = { classes("border-b", "border-slate-800", "pb-3") }) {
                                    H3(attrs = { classes("text-base", "font-bold", "text-white") }) {
                                        Text("↔️ Gestão de Layout (Reordenar Seções)")
                                    }
                                    P(attrs = { classes("text-xs", "text-slate-400", "mt-1") }) {
                                        Text("Ordene as seções do seu portfólio no fluxo de leitura principal.")
                                    }
                                }

                                Div(attrs = { classes("space-y-3", "max-w-md") }) {
                                    webSections.forEachIndexed { index, sectionName ->
                                        Div(attrs = { classes("flex", "items-center", "justify-between", "bg-slate-950/40", "p-3", "rounded-xl", "border", "border-slate-800/60") }) {
                                            Span(attrs = { classes("text-xs", "font-bold", "text-white") }) {
                                                Text("${index + 1}. $sectionName")
                                            }
                                            Div(attrs = { classes("flex", "gap-1.5") }) {
                                                // Up Button
                                                Button(attrs = {
                                                    classesList(
                                                        "p-1.5", "rounded-lg", "text-xs", "font-extrabold", "transition-all",
                                                        if (index > 0) "bg-slate-800 text-indigo-400 hover:bg-slate-700" else "bg-slate-900/20 text-slate-700 cursor-not-allowed"
                                                    )
                                                    onClick {
                                                        if (index > 0) {
                                                            val newList = webSections.toMutableList()
                                                            val item = newList.removeAt(index)
                                                            newList.add(index - 1, item)
                                                            webSections = newList
                                                        }
                                                    }
                                                }) {
                                                    Text("▲")
                                                }
                                                // Down Button
                                                Button(attrs = {
                                                    classesList(
                                                        "p-1.5", "rounded-lg", "text-xs", "font-extrabold", "transition-all",
                                                        if (index < webSections.size - 1) "bg-slate-800 text-indigo-400 hover:bg-slate-700" else "bg-slate-900/20 text-slate-700 cursor-not-allowed"
                                                    )
                                                    onClick {
                                                        if (index < webSections.size - 1) {
                                                            val newList = webSections.toMutableList()
                                                            val item = newList.removeAt(index)
                                                            newList.add(index + 1, item)
                                                            webSections = newList
                                                        }
                                                    }
                                                }) {
                                                    Text("▼")
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Section: Personal Info
                            Div(attrs = { classes("bg-slate-900/60", "rounded-2xl", "p-6", "border", "border-slate-800", "space-y-6") }) {
                                H3(attrs = { classes("text-base", "font-bold", "text-white", "border-b", "border-slate-800", "pb-3") }) {
                                    Text("👤 Dados Pessoais & Perfil")
                                }
                                Div(attrs = { classes("grid", "grid-cols-1", "sm:grid-cols-2", "gap-4") }) {
                                    Div(attrs = { classes("space-y-1.5") }) {
                                        Label(attrs = { classes("text-xs", "font-bold", "text-slate-400") }) { Text("Nome Completo") }
                                        Input(type = InputType.Text, attrs = {
                                            classes("w-full", "bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-xs", "text-white", "focus:outline-none")
                                            value(nameState)
                                            onInput { event -> nameState = event.value }
                                        })
                                    }
                                    Div(attrs = { classes("space-y-1.5") }) {
                                        Label(attrs = { classes("text-xs", "font-bold", "text-slate-400") }) { Text("Cargo / Título") }
                                        Input(type = InputType.Text, attrs = {
                                            classes("w-full", "bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-xs", "text-white", "focus:outline-none")
                                            value(roleState)
                                            onInput { event -> roleState = event.value }
                                        })
                                    }
                                    Div(attrs = { classes("space-y-1.5", "sm:col-span-2") }) {
                                        Label(attrs = { classes("text-xs", "font-bold", "text-slate-400") }) { Text("Apresentação Profissional (Bio)") }
                                        TextArea(attrs = {
                                            classes("w-full", "bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-xs", "text-white", "focus:outline-none")
                                            value(bioState)
                                            onInput { event -> bioState = event.value }
                                        })
                                    }
                                    Div(attrs = { classes("space-y-1.5") }) {
                                        Label(attrs = { classes("text-xs", "font-bold", "text-slate-400") }) { Text("E-mail de Contato") }
                                        Input(type = InputType.Text, attrs = {
                                            classes("w-full", "bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-xs", "text-white", "focus:outline-none")
                                            value(emailState)
                                            onInput { event -> emailState = event.value }
                                        })
                                    }
                                    Div(attrs = { classes("space-y-1.5") }) {
                                        Label(attrs = { classes("text-xs", "font-bold", "text-slate-400") }) { Text("Usuário GitHub") }
                                        Input(type = InputType.Text, attrs = {
                                            classes("w-full", "bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-xs", "text-white", "focus:outline-none")
                                            value(githubUser)
                                            onInput { event -> githubUser = event.value }
                                        })
                                    }
                                }
                            }

                            // Section: Skills Manager
                            Div(attrs = { classes("bg-slate-900/60", "rounded-2xl", "p-6", "border", "border-slate-800", "space-y-6") }) {
                                H3(attrs = { classes("text-base", "font-bold", "text-white", "border-b", "border-slate-800", "pb-3") }) {
                                    Text("⚡ Gerenciamento de Habilidades")
                                }
                                Div(attrs = { classes("space-y-4") }) {
                                    Div(attrs = { classes("flex", "gap-2") }) {
                                        Input(type = InputType.Text, attrs = {
                                            classes("flex-1", "bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-xs", "text-white", "placeholder-slate-600", "focus:outline-none")
                                            placeholder("Adicionar nova habilidade (ex: Docker, CI/CD)...")
                                            value(editSkillInput)
                                            onInput { event -> editSkillInput = event.value }
                                        })
                                        Button(attrs = {
                                            classesList("px-5", "py-3", "rounded-xl", "text-xs", "font-bold", "text-white", "transition-all", accentColor.buttonBg)
                                            onClick {
                                                val clean = editSkillInput.trim()
                                                if (clean.isNotBlank() && !skillsState.contains(clean)) {
                                                    skillsState = skillsState + clean
                                                    editSkillInput = ""
                                                }
                                            }
                                        }) {
                                            Text("Adicionar")
                                        }
                                    }

                                    Div(attrs = { classes("flex", "flex-wrap", "gap-2", "pt-2") }) {
                                        skillsState.forEach { skill ->
                                            Div(attrs = { classes("flex", "items-center", "gap-1.5", "bg-slate-950/60", "border", "border-slate-800", "pl-3", "pr-2", "py-1.5", "rounded-xl") }) {
                                                Span(attrs = { classes("text-xs", "text-slate-300") }) { Text(skill) }
                                                Button(attrs = {
                                                    classes("text-slate-500", "hover:text-rose-400", "text-xs", "p-0.5", "transition-colors")
                                                    onClick {
                                                        skillsState = skillsState.filter { it != skill }
                                                    }
                                                }) {
                                                    Text("×")
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Section: Experiences Manager
                            Div(attrs = { classes("bg-slate-900/60", "rounded-2xl", "p-6", "border", "border-slate-800", "space-y-6") }) {
                                H3(attrs = { classes("text-base", "font-bold", "text-white", "border-b", "border-slate-800", "pb-3") }) {
                                    Text("💼 Gerenciamento de Experiências")
                                }
                                Div(attrs = { classes("space-y-4") }) {
                                    Div(attrs = { classes("grid", "grid-cols-1", "sm:grid-cols-2", "gap-4") }) {
                                        Div(attrs = { classes("space-y-1.5") }) {
                                            Label(attrs = { classes("text-xs", "font-bold", "text-slate-400") }) { Text("Cargo & Empresa") }
                                            Input(type = InputType.Text, attrs = {
                                                classes("w-full", "bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-xs", "text-white", "placeholder-slate-600", "focus:outline-none")
                                                placeholder("Ex: Engenheiro DevOps - CloudCorp")
                                                value(expTitleInput)
                                                onInput { event -> expTitleInput = event.value }
                                            })
                                        }
                                        Div(attrs = { classes("space-y-1.5") }) {
                                            Label(attrs = { classes("text-xs", "font-bold", "text-slate-400") }) { Text("Período de Atuação") }
                                            Input(type = InputType.Text, attrs = {
                                                classes("w-full", "bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-xs", "text-white", "placeholder-slate-600", "focus:outline-none")
                                                placeholder("Ex: Ago 2022 - Presente")
                                                value(expPeriodInput)
                                                onInput { event -> expPeriodInput = event.value }
                                            })
                                        }
                                        Div(attrs = { classes("space-y-1.5", "sm:col-span-2") }) {
                                            Label(attrs = { classes("text-xs", "font-bold", "text-slate-400") }) { Text("Descrição de Realizações") }
                                            TextArea(attrs = {
                                                classes("w-full", "bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-xs", "text-white", "placeholder-slate-600", "focus:outline-none")
                                                placeholder("Descreva suas responsabilidades e conquistas...")
                                                value(expDescInput)
                                                onInput { event -> expDescInput = event.value }
                                            })
                                        }
                                    }
                                    Button(attrs = {
                                        classesList("px-5", "py-2.5", "rounded-xl", "text-xs", "font-bold", "text-white", "transition-all", accentColor.buttonBg)
                                        onClick {
                                            if (expTitleInput.isNotBlank() && expPeriodInput.isNotBlank()) {
                                                experiencesState = experiencesState + Experience(expTitleInput.trim(), expPeriodInput.trim(), expDescInput.trim())
                                                expTitleInput = ""
                                                expPeriodInput = ""
                                                expDescInput = ""
                                            }
                                        }
                                    }) {
                                        Text("Adicionar Experiência")
                                    }

                                    Div(attrs = { classes("space-y-3", "pt-4", "border-t", "border-slate-800/60") }) {
                                        experiencesState.forEach { exp ->
                                            Div(attrs = { classes("flex", "justify-between", "items-start", "bg-slate-950/40", "p-4", "rounded-xl", "border", "border-slate-800/80") }) {
                                                Div(attrs = { classes("space-y-1") }) {
                                                    P(attrs = { classes("text-xs", "font-extrabold", "text-white") }) { Text(exp.title) }
                                                    P(attrs = { classes("text-[10px]", "font-semibold", accentColor.textClass) }) { Text(exp.period) }
                                                    P(attrs = { classes("text-[11px]", "text-slate-400", "max-w-md") }) { Text(exp.description) }
                                                }
                                                Button(attrs = {
                                                    classes("text-slate-500", "hover:text-rose-400", "text-xs", "p-1", "transition-colors")
                                                    onClick {
                                                        experiencesState = experiencesState.filter { it != exp }
                                                    }
                                                }) {
                                                    Text("Remover")
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Section: Certifications Manager (Gerenciar Certificados)
                            Div(attrs = { classes("bg-slate-900/60", "rounded-2xl", "p-6", "border", "border-slate-800", "space-y-6") }) {
                                H3(attrs = { classes("text-base", "font-bold", "text-white", "border-b", "border-slate-800", "pb-3") }) {
                                    Text("🎓 Gerenciamento de Certificados")
                                }
                                Div(attrs = { classes("space-y-4") }) {
                                    Div(attrs = { classes("grid", "grid-cols-1", "sm:grid-cols-2", "gap-4") }) {
                                        Div(attrs = { classes("space-y-1.5") }) {
                                            Label(attrs = { classes("text-xs", "font-bold", "text-slate-400") }) { Text("Título da Certificação") }
                                            Input(type = InputType.Text, attrs = {
                                                classes("w-full", "bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-xs", "text-white", "placeholder-slate-600", "focus:outline-none")
                                                placeholder("Ex: AWS Solutions Architect Associate")
                                                value(certTitleInput)
                                                onInput { event -> certTitleInput = event.value }
                                            })
                                        }
                                        Div(attrs = { classes("space-y-1.5") }) {
                                            Label(attrs = { classes("text-xs", "font-bold", "text-slate-400") }) { Text("Categoria") }
                                            Input(type = InputType.Text, attrs = {
                                                classes("w-full", "bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-xs", "text-white", "placeholder-slate-600", "focus:outline-none")
                                                placeholder("Ex: Cloud & DevOps")
                                                value(certCategoryInput)
                                                onInput { event -> certCategoryInput = event.value }
                                            })
                                        }
                                        Div(attrs = { classes("space-y-1.5") }) {
                                            Label(attrs = { classes("text-xs", "font-bold", "text-slate-400") }) { Text("Duração") }
                                            Input(type = InputType.Text, attrs = {
                                                classes("w-full", "bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-xs", "text-white", "placeholder-slate-600", "focus:outline-none")
                                                placeholder("Ex: 40 horas")
                                                value(certDurationInput)
                                                onInput { event -> certDurationInput = event.value }
                                            })
                                        }
                                        Div(attrs = { classes("space-y-1.5") }) {
                                            Label(attrs = { classes("text-xs", "font-bold", "text-slate-400") }) { Text("Custo (Taxa)") }
                                            Input(type = InputType.Text, attrs = {
                                                classes("w-full", "bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-xs", "text-white", "placeholder-slate-600", "focus:outline-none")
                                                placeholder("Ex: US$ 150")
                                                value(certCostInput)
                                                onInput { event -> certCostInput = event.value }
                                            })
                                        }
                                        Div(attrs = { classes("space-y-1.5", "sm:col-span-2") }) {
                                            Label(attrs = { classes("text-xs", "font-bold", "text-slate-400") }) { Text("Resumo / Descrição") }
                                            TextArea(attrs = {
                                                classes("w-full", "bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-xs", "text-white", "placeholder-slate-600", "focus:outline-none")
                                                placeholder("Descreva as habilidades validadas por este certificado...")
                                                value(certDescInput)
                                                onInput { event -> certDescInput = event.value }
                                            })
                                        }
                                    }
                                    Button(attrs = {
                                        classesList("px-5", "py-2.5", "rounded-xl", "text-xs", "font-bold", "text-white", "transition-all", accentColor.buttonBg)
                                        onClick {
                                            if (certTitleInput.isNotBlank()) {
                                                certificationsState = certificationsState + Certification(
                                                    certTitleInput.trim(), 
                                                    certCategoryInput.trim(), 
                                                    certDurationInput.trim().ifBlank { "N/A" }, 
                                                    certCostInput.trim().ifBlank { "Isento" }, 
                                                    certDescInput.trim()
                                                )
                                                certTitleInput = ""
                                                certCategoryInput = "Cloud & DevOps"
                                                certDurationInput = ""
                                                certCostInput = ""
                                                certDescInput = ""
                                            }
                                        }
                                    }) {
                                        Text("Adicionar Certificado")
                                    }

                                    Div(attrs = { classes("space-y-3", "pt-4", "border-t", "border-slate-800/60") }) {
                                        certificationsState.forEach { cert ->
                                            Div(attrs = { classes("flex", "justify-between", "items-start", "bg-slate-950/40", "p-4", "rounded-xl", "border", "border-slate-800/80") }) {
                                                Div(attrs = { classes("space-y-1") }) {
                                                    P(attrs = { classes("text-xs", "font-extrabold", "text-white") }) { Text(cert.title) }
                                                    P(attrs = { classes("text-[10px]", "font-semibold", accentColor.textClass) }) { Text("${cert.category} | ${cert.duration}") }
                                                    P(attrs = { classes("text-[11px]", "text-slate-400") }) { Text(cert.description) }
                                                }
                                                Button(attrs = {
                                                    classes("text-slate-500", "hover:text-rose-400", "text-xs", "p-1", "transition-colors")
                                                    onClick {
                                                        certificationsState = certificationsState.filter { it != cert }
                                                    }
                                                }) {
                                                    Text("Remover")
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Section: LinkedIn & Resume Text Import (IA Gemini)
                            Div(attrs = { classes("bg-slate-900/60", "rounded-2xl", "p-6", "border", "border-slate-800", "space-y-6") }) {
                                Div(attrs = { classes("border-b", "border-slate-800", "pb-3") }) {
                                    H3(attrs = { classes("text-base", "font-bold", "text-white") }) {
                                        Text("🤖 Importação de Perfil LinkedIn / Currículo (IA)")
                                    }
                                    P(attrs = { classes("text-xs", "text-slate-400", "mt-1") }) {
                                        Text("Cole o texto bruto do seu LinkedIn (sobre, competências, cargo) ou um currículo e deixe a Inteligência Artificial estruturar tudo automaticamente.")
                                    }
                                }

                                Div(attrs = { classes("space-y-4") }) {
                                    TextArea(attrs = {
                                        classes("w-full", "bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3.5", "text-xs", "text-white", "placeholder-slate-600", "focus:outline-none", "min-h-[140px]")
                                        placeholder("Cole o texto copiado aqui...")
                                        value(linkedinRawInput)
                                        onInput { event -> linkedinRawInput = event.value }
                                    })

                                    // Switch Replacement vs Merge
                                    Div(attrs = { classes("flex", "items-center", "justify-between", "bg-slate-950/30", "p-3.5", "rounded-xl", "border", "border-slate-800/80") }) {
                                        Div(attrs = { classes("space-y-0.5", "pr-4") }) {
                                            P(attrs = { classes("text-xs", "font-bold", "text-white") }) {
                                                Text("Substituir portfólio atual")
                                            }
                                            P(attrs = { classes("text-[10px]", "text-slate-500") }) {
                                                Text("Apaga os dados atuais substituindo inteiramente pelo conteúdo extraído por inteligência artificial.")
                                            }
                                        }
                                        Input(type = InputType.Checkbox, attrs = {
                                            classes("w-4", "h-4", "rounded", "accent-indigo-600", "cursor-pointer")
                                            checked(linkedinReplaceMode)
                                            onChange { linkedinReplaceMode = !linkedinReplaceMode }
                                        })
                                    }

                                    Button(attrs = {
                                        classesList(
                                            "w-full", "py-3", "rounded-xl", "text-xs", "font-bold", "text-white", "transition-all", "flex", "items-center", "justify-center", "gap-2",
                                            if (isLinkedinImporting || linkedinRawInput.isBlank()) "bg-slate-800 cursor-not-allowed opacity-50" else accentColor.buttonBg
                                        )
                                        onClick {
                                            if (!isLinkedinImporting && linkedinRawInput.isNotBlank()) {
                                                isLinkedinImporting = true
                                                window.setTimeout({
                                                    try {
                                                        // Smart client-side tech skills and profile parser
                                                        val rawText = linkedinRawInput
                                                        val knownSkills = listOf(
                                                            "AWS", "Docker", "Kubernetes", "Terraform", "Ansible", "CI/CD", "Jenkins",
                                                            "Python", "Java", "Kotlin", "TypeScript", "React", "Node.js", "Cisco", "Fortinet",
                                                            "Linux", "SQL", "Git", "DevOps", "Spring Boot", "Go", "Cloud", "Redes", "Segurança"
                                                        )
                                                        val extractedSkills = knownSkills.filter { rawText.contains(it, ignoreCase = true) }
                                                        
                                                        if (linkedinReplaceMode) {
                                                            if (extractedSkills.isNotEmpty()) skillsState = extractedSkills
                                                            
                                                            // Try to extract name and role
                                                            val firstLine = rawText.split("\n").firstOrNull { it.isNotBlank() }?.trim() ?: "Nome Importado"
                                                            if (firstLine.length < 35 && firstLine.isNotBlank()) nameState = firstLine
                                                            
                                                            val secondLine = rawText.split("\n").filter { it.isNotBlank() }.getOrNull(1)?.trim() ?: ""
                                                            if (secondLine.length < 50 && secondLine.isNotBlank()) roleState = secondLine
                                                            
                                                            if (rawText.length > 50) {
                                                                bioState = "Extraído do LinkedIn: " + rawText.take(180).trim() + "..."
                                                            }
                                                        } else {
                                                            skillsState = (skillsState + extractedSkills).distinct()
                                                        }

                                                        // Structure any detected job roles as experiences
                                                        val lines = rawText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                                                        val extraExps = mutableListOf<Experience>()
                                                        lines.forEach { line ->
                                                            if (line.contains("Analista", ignoreCase = true) || 
                                                                line.contains("Engenheiro", ignoreCase = true) || 
                                                                line.contains("Desenvolvedor", ignoreCase = true) || 
                                                                line.contains("Manager", ignoreCase = true) || 
                                                                line.contains("Especialista", ignoreCase = true) ||
                                                                line.contains("Developer", ignoreCase = true)) {
                                                                extraExps.add(Experience(line.take(45), "Importado de IA", "Experiência extraída e estruturada de forma inteligente de seu perfil LinkedIn por nossa IA."))
                                                            }
                                                        }

                                                        if (extraExps.isNotEmpty()) {
                                                            if (linkedinReplaceMode) experiencesState = extraExps else experiencesState = experiencesState + extraExps
                                                        }

                                                        linkedinRawInput = ""
                                                        isLinkedinImporting = false
                                                        window.alert("Perfil estruturado com sucesso pela IA! Verifique a aba 'Portfólio'.")
                                                    } catch (e: Exception) {
                                                        isLinkedinImporting = false
                                                        window.alert("Erro ao estruturar perfil: " + e.message)
                                                    }
                                                }, 1500)
                                            }
                                        }
                                    }) {
                                        if (isLinkedinImporting) {
                                            Span(attrs = { classes("animate-spin") }) { Text("⏳") }
                                            Text("IA analisando e estruturando currículo...")
                                        } else {
                                            Text("🚀 Importar e Estruturar com IA (Gemini)")
                                        }
                                    }
                                }
                            }

                            // Category: Export, Backup & Printing
                            Div(attrs = { classes("bg-slate-900/60", "rounded-2xl", "p-6", "border", "border-slate-800", "space-y-6") }) {
                                H3(attrs = { classes("text-base", "font-bold", "text-white", "border-b", "border-slate-800", "pb-3") }) {
                                    Text("📥 Exportação, PDF & Backup")
                                }
                                Div(attrs = { classes("flex", "flex-col", "sm:flex-row", "gap-4") }) {
                                    // Generate PDF (Print Prompt)
                                    Button(attrs = {
                                        classes("flex-1", "py-3", "rounded-xl", "text-xs", "font-bold", "bg-indigo-600", "hover:bg-indigo-500", "text-white", "transition-all")
                                        onClick {
                                            window.print()
                                        }
                                    }) {
                                        Text("🖨️ Exportar PDF / Imprimir")
                                    }

                                    // JSON Backup Download (Direct JS Blob trigger)
                                    Button(attrs = {
                                        classes("flex-1", "py-3", "rounded-xl", "text-xs", "font-bold", "bg-slate-800", "hover:bg-slate-700", "text-slate-300", "border", "border-slate-700/60", "transition-all")
                                        onClick {
                                            try {
                                                val skillsArray = skillsState.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
                                                val backupJson = """
                                                    {
                                                      "name": "$nameState",
                                                      "role": "$roleState",
                                                      "bio": "$bioState",
                                                      "email": "$emailState",
                                                      "github": "$githubUser",
                                                      "skills": $skillsArray,
                                                      "platform": "DevFolio Pro Web"
                                                    }
                                                """.trimIndent()

                                                val blob = Blob(arrayOf(backupJson), BlobPropertyBag(type = "application/json"))
                                                val url = URL.createObjectURL(blob)
                                                val downloadLink = document.createElement("a") as HTMLAnchorElement
                                                downloadLink.href = url
                                                downloadLink.download = "devfolio-backup-${nameState.lowercase().replace(" ", "-")}.json"
                                                downloadLink.click()
                                                URL.revokeObjectURL(url)
                                            } catch (e: Exception) {
                                                window.alert("Erro ao exportar backup: " + e.message)
                                            }
                                        }
                                    }) {
                                        Text("💾 Baixar Backup JSON")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- 3. MODALS AND DIALOGS ---

            // Simulated Login dialog modal
            if (showLoginDialog) {
                Div(attrs = { classes("fixed", "inset-0", "z-50", "flex", "items-center", "justify-center", "p-4", "bg-black/60", "backdrop-blur-sm") }) {
                    Div(attrs = { classes("bg-slate-900", "border", "border-slate-800", "rounded-3xl", "p-6", "max-w-md", "w-full", "space-y-6") }) {
                        // Title Area
                        Div {
                            H3(attrs = { classes("text-xl", "font-extrabold", "text-white") }) {
                                Text("Acessar / Sincronizar")
                            }
                            P(attrs = { classes("text-xs", "text-slate-400", "mt-1") }) {
                                Text("Sincronize com seu aplicativo Android preenchendo sua conta de teste.")
                            }
                        }

                        // Form inputs
                        Div(attrs = { classes("space-y-4") }) {
                            Div(attrs = { classes("space-y-1.5") }) {
                                Label(attrs = { classes("text-xs", "font-bold", "text-slate-400") }) { Text("Nome Completo") }
                                Input(type = InputType.Text, attrs = {
                                    classes("w-full", "bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-sm", "text-white", "focus:outline-none")
                                    placeholder("Digite seu nome...")
                                    value(authNameInput)
                                    onInput { event -> authNameInput = event.value }
                                })
                            }

                            Div(attrs = { classes("space-y-1.5") }) {
                                Label(attrs = { classes("text-xs", "font-bold", "text-slate-400") }) { Text("E-mail") }
                                Input(type = InputType.Text, attrs = {
                                    classes("w-full", "bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-sm", "text-white", "focus:outline-none")
                                    placeholder("Digite seu email...")
                                    value(authEmailInput)
                                    onInput { event -> authEmailInput = event.value }
                                })
                            }
                        }

                        // Buttons Action Area
                        Div(attrs = { classes("flex", "gap-3") }) {
                            Button(attrs = {
                                classes("flex-1", "py-3", "rounded-xl", "text-xs", "font-bold", "bg-slate-950", "hover:bg-slate-800", "text-slate-400", "transition-colors")
                                onClick { showLoginDialog = false }
                            }) {
                                Text("Cancelar")
                            }
                            Button(attrs = {
                                classesList("flex-1", "py-3", "rounded-xl", "text-xs", "font-bold", "text-white", "transition-colors", accentColor.buttonBg)
                                onClick {
                                    if (authNameInput.isNotBlank() && authEmailInput.isNotBlank()) {
                                        currentUser = WebUser(authNameInput.trim(), authEmailInput.trim())
                                        // Auto update profile name to match authenticated user
                                        nameState = authNameInput.trim()
                                        emailState = authEmailInput.trim()
                                        showLoginDialog = false
                                    }
                                }
                            }) {
                                Text("Acessar")
                            }
                        }
                    }
                }
            }

            // Footer Section
            Footer(attrs = { classes("border-t", "border-slate-800/60", "bg-slate-950/20", "py-12", "text-center") }) {
                Div(attrs = { classes("max-w-4xl", "mx-auto", "px-6", "space-y-2") }) {
                    P(attrs = { classes("text-xs", "text-slate-500") }) {
                        Text("Gerado automaticamente em arquitetura unificada Kotlin Multiplatform (KMP) com suporte a múltiplos alvos.")
                    }
                    P(attrs = { classes("text-[10px]", "text-slate-600") }) {
                        Text("Hospedagem estática otimizada servida por Node.js com Express e preparada para CI/CD automático no Railway.")
                    }
                }
            }
        }
    }
}
