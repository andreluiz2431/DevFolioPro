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
    val email: String
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
        var authEmailInput by remember { mutableStateOf("") }
        var authNameInput by remember { mutableStateOf("") }

        // AI Resume Coach State
        var coachSelectedRole by remember { mutableStateOf("Engenheiro DevOps") }
        var isCoachAnalyzing by remember { mutableStateOf(false) }
        var showCoachResults by remember { mutableStateOf(false) }
        var coachMatchScore by remember { mutableStateOf(0) }
        var coachSuggestions by remember { mutableStateOf("") }
        var coachMissingSkills by remember { mutableStateOf(emptyList<String>()) }
        var coachSuccessFeedback by remember { mutableStateOf(false) }

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
                            classes("w-10", "h-10", "rounded-xl", "bg-gradient-to-tr", "flex", "items-center", "justify-center", "font-black", "text-white")
                            classes(*accentColor.bgGradient.split(' ').toTypedArray())
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
                            // User is signed-in (Simulated Firebase Sync Status)
                            Div(attrs = { classes("flex", "items-center", "gap-3", "bg-slate-950/60", "p-2", "rounded-xl", "border", "border-slate-800") }) {
                                Div(attrs = { 
                                    classes("w-8", "h-8", "rounded-full", "bg-gradient-to-tr", "flex", "items-center", "justify-center", "text-xs", "font-bold", "text-white") 
                                    classes(*accentColor.bgGradient.split(' ').toTypedArray())
                                }) {
                                    Text(user.name.take(1).uppercase())
                                }
                                Div(attrs = { classes("hidden", "sm:block") }) {
                                    P(attrs = { classes("text-xs", "font-bold", "text-white", "leading-tight") }) {
                                        Text(user.name)
                                    }
                                    P(attrs = { classes("text-[10px]", "text-emerald-400", "flex", "items-center", "gap-1") }) {
                                        Text("● Sincronizado na Nuvem")
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
                        classes(
                            "px-4", "py-2.5", "rounded-xl", "text-sm", "font-bold", "transition-all", "flex", "items-center", "gap-2",
                            if (selectedTab == "portfolio") "bg-slate-900 text-white border border-slate-800 shadow-lg" else "text-slate-400 hover:text-white"
                        )
                        onClick { selectedTab = "portfolio" }
                    }) {
                        Text("📂 Portfólio")
                    }

                    // AI Resume Coach Tab (Melhorias)
                    Button(attrs = {
                        classes(
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
                        classes(
                            "px-4", "py-2.5", "rounded-xl", "text-sm", "font-bold", "transition-all", "flex", "items-center", "gap-2",
                            if (selectedTab == "settings") "bg-slate-900 text-white border border-slate-800 shadow-lg" else "text-slate-400 hover:text-white"
                        )
                        onClick { selectedTab = "settings" }
                    }) {
                        Text("🛠️ Ajustes & Dados")
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
                                // Left Sidebar Info
                                Div(attrs = { classes("md:col-span-1", "space-y-8") }) {
                                    // Skills Card
                                    Div(attrs = { classes("bg-slate-900/60", "backdrop-blur-xl", "rounded-2xl", "p-6", "border", "border-slate-800") }) {
                                        H3(attrs = { classes("text-sm", "font-extrabold", "text-slate-400", "uppercase", "tracking-wider", "mb-4", "flex", "items-center", "gap-2") }) {
                                            Text("⚡ Habilidades")
                                        }
                                        Div(attrs = { classes("flex", "flex-wrap", "gap-2") }) {
                                            skillsState.forEach { skill ->
                                                Span(attrs = {
                                                    classes(
                                                        "px-2.5", "py-1", "rounded-lg", "text-xs", "font-semibold", 
                                                        "bg-slate-800", "text-slate-300", "border", "border-slate-700/60"
                                                    )
                                                }) {
                                                    Text(skill)
                                                }
                                            }
                                        }
                                    }

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

                                // Right Detailed Area
                                Div(attrs = { classes("md:col-span-2", "space-y-8") }) {
                                    // Bio/Sobre Card
                                    Div(attrs = { classes("bg-slate-900/60", "backdrop-blur-xl", "rounded-2xl", "p-6", "border", "border-slate-800") }) {
                                        H3(attrs = { classes("text-sm", "font-extrabold", "text-slate-400", "uppercase", "tracking-wider", "mb-4") }) {
                                            Text("👤 Sobre Mim")
                                        }
                                        P(attrs = { classes("text-slate-300", "leading-relaxed", "text-base") }) {
                                            Text(bioState)
                                        }
                                    }

                                    // Experiences Timeline
                                    Div(attrs = { classes("bg-slate-900/60", "backdrop-blur-xl", "rounded-2xl", "p-6", "border", "border-slate-800") }) {
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

                                    // Certifications List
                                    Div(attrs = { classes("bg-slate-900/60", "backdrop-blur-xl", "rounded-2xl", "p-6", "border", "border-slate-800") }) {
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
                                    classes(
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
                                                    }
                                                    "Desenvolvedor Back-end" -> {
                                                        coachMatchScore = 65
                                                        coachSuggestions = "Muito bom conhecimento geral em redes e Kubernetes, mas para atuar como Back-end faltam linguagens e frameworks típicos de aplicações corporativas, além de filas/mensageria."
                                                        coachMissingSkills = listOf("Spring Boot", "PostgreSQL", "Redis", "gRPC")
                                                    }
                                                    "Desenvolvedor Front-end" -> {
                                                        coachMatchScore = 58
                                                        coachSuggestions = "O portfólio tem foco extremo em Cloud e redes. Para atuar no Front-end de alto nível, sugerimos reforçar conhecimento em frameworks reativos modernos de CSS e SPA/SSR."
                                                        coachMissingSkills = listOf("TypeScript", "Tailwind CSS", "Next.js", "Redux Toolkit")
                                                    }
                                                    "Desenvolvedor Full-stack" -> {
                                                        coachMatchScore = 60
                                                        coachSuggestions = "Você tem bases ótimas de infraestrutura e conteinerização. Um desenvolvedor fullstack moderno precisa balancear isso com bibliotecas robustas de banco de dados e componentização."
                                                        coachMissingSkills = listOf("PostgreSQL", "TypeScript", "Next.js", "Docker")
                                                    }
                                                    "Analista de Infraestrutura" -> {
                                                        coachMatchScore = 95
                                                        coachSuggestions = "Excepcional aderência à vaga de Infraestrutura! Você domina redes Fortinet, Cisco, Linux e AWS. Sugerimos apenas focar em boas práticas de governança de TI e virtualização local."
                                                        coachMissingSkills = listOf("Virtualização (VMware)", "AWS Cloud", "Linux Administration", "Fortinet Firewalls")
                                                    }
                                                    else -> {
                                                        coachMatchScore = 70
                                                        coachSuggestions = "Kotlin/Jetpack Compose já constam em suas habilidades, o que é ótimo para Mobile! Sugerimos expandir para multiplataforma geral de forma a maximizar sua portabilidade."
                                                        coachMissingSkills = listOf("Kotlin Multiplatform", "SwiftUI", "Jetpack Compose", "App Store Deployment")
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

                                    // Written Suggestions Card
                                    Div(attrs = { classes("space-y-2") }) {
                                        P(attrs = { classes("text-xs", "font-extrabold", "text-slate-400", "uppercase", "tracking-wider") }) {
                                            Text("💡 Sugestões de Melhorias:")
                                        }
                                        P(attrs = { classes("text-sm", "text-slate-300", "leading-relaxed", "bg-slate-950/20", "p-4", "rounded-xl", "border", "border-slate-800/80") }) {
                                            Text(coachSuggestions)
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
                                    Div(attrs = { classes("pt-2") }) {
                                        if (coachSuccessFeedback) {
                                            Div(attrs = { classes("bg-emerald-500/10", "border", "border-emerald-500/20", "text-emerald-300", "p-3", "rounded-xl", "text-center", "text-xs", "font-bold") }) {
                                                Text("✅ Habilidades incorporadas com sucesso! Verifique a aba 'Portfólio'.")
                                            }
                                        } else {
                                            Button(attrs = {
                                                classes("w-full", "py-3", "rounded-xl", "text-xs", "font-bold", "bg-emerald-600", "hover:bg-emerald-500", "text-white", "transition-all")
                                                onClick {
                                                    // Merge missing skills and trigger feedback
                                                    val currentSet = skillsState.toSet()
                                                    val newSet = currentSet + coachMissingSkills
                                                    skillsState = newSet.toList()
                                                    coachSuccessFeedback = true
                                                }
                                            }) {
                                                Text("✨ Aplicar Sugestões de Habilidades no Currículo")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "settings" -> {
                        // --- SETTINGS AND PROFILE CONFIGURATION VIEW ---
                        Div(attrs = { classes("space-y-12") }) {
                            // Category: Design and Colors (Real-Time Styling)
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
                                                classes(
                                                    "p-3", "rounded-xl", "text-xs", "font-bold", "transition-all", "border", "flex", "flex-col", "items-center", "gap-1.5",
                                                    if (accentColor == color) "border-white bg-slate-800 text-white shadow-md shadow-white/5" else "border-slate-800 bg-slate-950/60 text-slate-400 hover:text-white"
                                                )
                                                onClick { accentColor = color }
                                            }) {
                                                // Dynamic visual color dot
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

                            // Category: Personal Info
                            Div(attrs = { classes("bg-slate-900/60", "rounded-2xl", "p-6", "border", "border-slate-800", "space-y-6") }) {
                                H3(attrs = { classes("text-base", "font-bold", "text-white", "border-b", "border-slate-800", "pb-3") }) {
                                    Text("👤 Dados Pessoais & Perfil")
                                }
                                Div(attrs = { classes("grid", "grid-cols-1", "sm:grid-cols-2", "gap-4") }) {
                                    // Name input
                                    Div(attrs = { classes("space-y-1.5") }) {
                                        Label(attrs = { classes("text-xs", "font-bold", "text-slate-400") }) { Text("Nome Completo") }
                                        Input(type = InputType.Text, attrs = {
                                            classes("w-full", "bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-sm", "text-white", "focus:outline-none", "focus:ring-1", "focus:ring-slate-700")
                                            value(nameState)
                                            onInput { event -> nameState = event.value }
                                        })
                                    }

                                    // Role input
                                    Div(attrs = { classes("space-y-1.5") }) {
                                        Label(attrs = { classes("text-xs", "font-bold", "text-slate-400") }) { Text("Cargo / Especialidade") }
                                        Input(type = InputType.Text, attrs = {
                                            classes("w-full", "bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-sm", "text-white", "focus:outline-none", "focus:ring-1", "focus:ring-slate-700")
                                            value(roleState)
                                            onInput { event -> roleState = event.value }
                                        })
                                    }

                                    // Email input
                                    Div(attrs = { classes("space-y-1.5") }) {
                                        Label(attrs = { classes("text-xs", "font-bold", "text-slate-400") }) { Text("E-mail de Contato") }
                                        Input(type = InputType.Text, attrs = {
                                            classes("w-full", "bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-sm", "text-white", "focus:outline-none", "focus:ring-1", "focus:ring-slate-700")
                                            value(emailState)
                                            onInput { event -> emailState = event.value }
                                        })
                                    }

                                    // Github Username
                                    Div(attrs = { classes("space-y-1.5") }) {
                                        Label(attrs = { classes("text-xs", "font-bold", "text-slate-400") }) { Text("GitHub Username") }
                                        Input(type = InputType.Text, attrs = {
                                            classes("w-full", "bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-sm", "text-white", "focus:outline-none", "focus:ring-1", "focus:ring-slate-700")
                                            value(githubUser)
                                            onInput { event -> githubUser = event.value }
                                        })
                                    }
                                }

                                // Biography text input
                                Div(attrs = { classes("space-y-1.5") }) {
                                    Label(attrs = { classes("text-xs", "font-bold", "text-slate-400") }) { Text("Sobre Mim / Biografia") }
                                    TextArea(value = bioState, attrs = {
                                        classes("w-full", "bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-sm", "text-white", "h-24", "focus:outline-none", "focus:ring-1", "focus:ring-slate-700")
                                        onInput { event -> bioState = event.value }
                                    })
                                }
                            }

                            // Category: Skills Management
                            Div(attrs = { classes("bg-slate-900/60", "rounded-2xl", "p-6", "border", "border-slate-800", "space-y-6") }) {
                                H3(attrs = { classes("text-base", "font-bold", "text-white", "border-b", "border-slate-800", "pb-3") }) {
                                    Text("⚡ Gerenciamento de Habilidades")
                                }

                                // Skills Badges with delete button
                                Div(attrs = { classes("flex", "flex-wrap", "gap-2") }) {
                                    skillsState.forEach { skill ->
                                        Span(attrs = {
                                            classes("inline-flex", "items-center", "gap-1.5", "px-3", "py-1.5", "rounded-lg", "text-xs", "font-semibold", "bg-slate-950", "text-slate-300", "border", "border-slate-800")
                                        }) {
                                            Text(skill)
                                            Button(attrs = {
                                                classes("text-slate-500", "hover:text-rose-400", "font-bold", "ml-1")
                                                onClick {
                                                    skillsState = skillsState.filter { it != skill }
                                                }
                                            }) {
                                                Text("×")
                                            }
                                        }
                                    }
                                }

                                // Add skill inline form
                                Div(attrs = { classes("flex", "gap-3") }) {
                                    Input(type = InputType.Text, attrs = {
                                        classes("bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-sm", "text-white", "flex-1", "focus:outline-none")
                                        placeholder("Adicionar nova habilidade...")
                                        value(editSkillInput)
                                        onInput { event -> editSkillInput = event.value }
                                    })
                                    Button(attrs = {
                                        classes("px-5", "py-3", "rounded-xl", "text-xs", "font-bold", "text-white", "transition-colors")
                                        classes(*accentColor.buttonBg.split(' ').toTypedArray())
                                        onClick {
                                            if (editSkillInput.isNotBlank()) {
                                                val clean = editSkillInput.trim()
                                                if (!skillsState.contains(clean)) {
                                                    skillsState = skillsState + clean
                                                }
                                                editSkillInput = ""
                                            }
                                        }
                                    }) {
                                        Text("Adicionar")
                                    }
                                }
                            }

                            // Category: Experience Management
                            Div(attrs = { classes("bg-slate-900/60", "rounded-2xl", "p-6", "border", "border-slate-800", "space-y-6") }) {
                                H3(attrs = { classes("text-base", "font-bold", "text-white", "border-b", "border-slate-800", "pb-3") }) {
                                    Text("💼 Gerenciamento de Experiências")
                                }

                                // Interactive Experience List (Delete enabled)
                                Div(attrs = { classes("space-y-4") }) {
                                    experiencesState.forEach { exp ->
                                        Div(attrs = { classes("flex", "justify-between", "items-start", "bg-slate-950/40", "p-4", "rounded-xl", "border", "border-slate-800/60") }) {
                                            Div {
                                                H4(attrs = { classes("font-bold", "text-slate-200") }) { Text(exp.title) }
                                                P(attrs = { classes("text-[10px]", "text-slate-500") }) { Text(exp.period) }
                                            }
                                            Button(attrs = {
                                                classes("text-slate-500", "hover:text-rose-400", "text-xs", "font-bold", "px-2", "py-1")
                                                onClick {
                                                    experiencesState = experiencesState.filter { it != exp }
                                                }
                                            }) {
                                                Text("Remover")
                                            }
                                        }
                                    }
                                }

                                // Add new experience subform
                                Div(attrs = { classes("p-4", "bg-slate-950/40", "rounded-xl", "border", "border-slate-800/60", "space-y-4") }) {
                                    H4(attrs = { classes("text-xs", "font-bold", "text-slate-300", "uppercase") }) { Text("Adicionar Nova Experiência") }
                                    Div(attrs = { classes("grid", "grid-cols-1", "sm:grid-cols-2", "gap-4") }) {
                                        Input(type = InputType.Text, attrs = {
                                            classes("bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-sm", "text-white", "focus:outline-none")
                                            placeholder("Cargo (ex: Engenheiro DevOps Senior)")
                                            value(expTitleInput)
                                            onInput { event -> expTitleInput = event.value }
                                        })
                                        Input(type = InputType.Text, attrs = {
                                            classes("bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-sm", "text-white", "focus:outline-none")
                                            placeholder("Empresa | Período (ex: AWS | 2022 - 2024)")
                                            value(expPeriodInput)
                                            onInput { event -> expPeriodInput = event.value }
                                        })
                                    }
                                    TextArea(value = expDescInput, attrs = {
                                        classes("w-full", "bg-slate-950", "border", "border-slate-800", "rounded-xl", "p-3", "text-sm", "text-white", "h-20", "focus:outline-none")
                                        placeholder("Descrição breve das responsabilidades e realizações...")
                                        onInput { event -> expDescInput = event.value }
                                    })
                                    Button(attrs = {
                                        classes("px-5", "py-2.5", "rounded-xl", "text-xs", "font-bold", "text-white", "transition-colors")
                                        classes(*accentColor.buttonBg.split(' ').toTypedArray())
                                        onClick {
                                            if (expTitleInput.isNotBlank() && expPeriodInput.isNotBlank()) {
                                                experiencesState = experiencesState + Experience(
                                                    title = expTitleInput.trim(),
                                                    period = expPeriodInput.trim(),
                                                    description = expDescInput.trim()
                                                )
                                                expTitleInput = ""
                                                expPeriodInput = ""
                                                expDescInput = ""
                                            }
                                        }
                                    }) {
                                        Text("Adicionar Experiência")
                                    }
                                }
                            }

                            // Category: Course Recommender / Course Suggestion
                            Div(attrs = { classes("bg-slate-900/60", "rounded-2xl", "p-6", "border", "border-slate-800", "space-y-6") }) {
                                H3(attrs = { classes("text-base", "font-bold", "text-white", "border-b", "border-slate-800", "pb-3") }) {
                                    Text("🎓 Sugestão de Cursos & Certificados")
                                }
                                P(attrs = { classes("text-slate-400", "text-xs") }) {
                                    Text("Descubra certificações altamente valorizadas recomendadas pelo DevFolio Pro:")
                                }

                                Div(attrs = { classes("grid", "grid-cols-1", "md:grid-cols-2", "gap-4") }) {
                                    // AWS Certified Solutions Architect
                                    Div(attrs = { classes("bg-slate-950/40", "p-4", "rounded-xl", "border", "border-slate-800/80", "space-y-3") }) {
                                        Div(attrs = { classes("flex", "justify-between", "items-start", "gap-2") }) {
                                            H4(attrs = { classes("font-bold", "text-indigo-300") }) { Text("AWS Certified Solutions Architect") }
                                            Span(attrs = { classes("text-[9px]", "font-bold", "bg-indigo-500/10", "text-indigo-400", "px-2", "py-0.5", "rounded-full") }) { Text("Recomendado") }
                                        }
                                        P(attrs = { classes("text-[10px]", "text-slate-500") }) { Text("Duração: 80h | US$ 150") }
                                        Button(attrs = {
                                            classes("w-full", "py-2", "rounded-lg", "text-[11px]", "font-bold", "bg-slate-800", "hover:bg-slate-700", "transition-all")
                                            onClick {
                                                val exists = certificationsState.any { it.title.contains("AWS Certified Solutions Architect") }
                                                if (!exists) {
                                                    certificationsState = certificationsState + Certification(
                                                        "AWS Certified Solutions Architect", "Cloud & DevOps", "80 horas", "US$ 150", 
                                                        "Garante conhecimento profundo em modelagem de arquiteturas resilientes e econômicas na nuvem AWS."
                                                    )
                                                }
                                            }
                                        }) {
                                            Text("Adicionar ao Currículo")
                                        }
                                    }

                                    // Certified Kubernetes Administrator (CKA)
                                    Div(attrs = { classes("bg-slate-950/40", "p-4", "rounded-xl", "border", "border-slate-800/80", "space-y-3") }) {
                                        Div(attrs = { classes("flex", "justify-between", "items-start", "gap-2") }) {
                                            H4(attrs = { classes("font-bold", "text-indigo-300") }) { Text("Certified Kubernetes Administrator (CKA)") }
                                            Span(attrs = { classes("text-[9px]", "font-bold", "bg-indigo-500/10", "text-indigo-400", "px-2", "py-0.5", "rounded-full") }) { Text("Recomendado") }
                                        }
                                        P(attrs = { classes("text-[10px]", "text-slate-500") }) { Text("Duração: 60h | US$ 375") }
                                        Button(attrs = {
                                            classes("w-full", "py-2", "rounded-lg", "text-[11px]", "font-bold", "bg-slate-800", "hover:bg-slate-700", "transition-all")
                                            onClick {
                                                val exists = certificationsState.any { it.title.contains("Kubernetes Administrator") }
                                                if (!exists) {
                                                    certificationsState = certificationsState + Certification(
                                                        "Certified Kubernetes Administrator (CKA)", "Kubernetes & Cloud", "60 horas", "US$ 375", 
                                                        "Valida habilidades práticas de administração, configuração e deploy de clusters de Kubernetes corporativos."
                                                    )
                                                }
                                            }
                                        }) {
                                            Text("Adicionar ao Currículo")
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
                                            // Trigger browser native print dialogue
                                            window.print()
                                        }
                                    }) {
                                        Text("🖨️ Exportar PDF / Imprimir")
                                    }

                                    // JSON Backup Download (Highly polished direct JS Blob trigger)
                                    Button(attrs = {
                                        classes("flex-1", "py-3", "rounded-xl", "text-xs", "font-bold", "bg-slate-800", "hover:bg-slate-700", "text-slate-300", "border", "border-slate-700/60", "transition-all")
                                        onClick {
                                            try {
                                                // Format clean, readable backup data structure manually
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
                                classes("flex-1", "py-3", "rounded-xl", "text-xs", "font-bold", "text-white", "transition-colors")
                                classes(*accentColor.buttonBg.split(' ').toTypedArray())
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
