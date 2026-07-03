package com.example.web

import org.jetbrains.compose.web.renderComposable
import org.jetbrains.compose.web.dom.*
import androidx.compose.runtime.*
import com.example.shared.SharedConstants
import com.example.shared.SharedProfile

fun main() {
    val sampleProfile = SharedProfile(
        name = "Alexandre Silva",
        role = "Arquiteto Cloud & DevOps Specialist",
        bio = "Especialista em automação de infraestrutura de TI corporativa, redes Cisco/Fortinet e implantação de microsserviços escaláveis com Kubernetes na nuvem AWS.",
        skills = listOf(
            "AWS Solutions", "Docker & Kubernetes", "Cisco CCNA", 
            "Fortinet Firewalls", "Kotlin / Jetpack Compose", "Node.js", 
            "CI/CD Pipelines", "Linux Administration", "Python"
        )
    )

    renderComposable(rootElementId = "root") {
        Div(attrs = { classes("max-w-4xl", "mx-auto", "px-6", "py-12") }) {
            // Header Hero Area
            Header(attrs = { classes("text-center", "mb-16", "relative") }) {
                // Shared module version badge
                Span(attrs = {
                    classes(
                        "inline-flex", "items-center", "gap-1.5", "px-3", "py-1.5", 
                        "rounded-full", "text-xs", "font-medium", "bg-indigo-500/10", 
                        "text-indigo-400", "mb-6", "border", "border-indigo-500/20"
                    )
                }) {
                    Text("DevFolio Pro Web v" + SharedConstants.APP_VERSION)
                }

                H1(attrs = {
                    classes(
                        "text-5xl", "sm:text-6xl", "font-extrabold", "tracking-tight", 
                        "bg-gradient-to-r", "from-indigo-400", "via-purple-400", 
                        "to-pink-400", "bg-clip-text", "text-transparent", "mb-4"
                    )
                }) {
                    Text(sampleProfile.name)
                }

                P(attrs = { classes("text-xl", "text-indigo-300", "font-semibold", "mb-6") }) {
                    Text(sampleProfile.role)
                }

                P(attrs = { classes("text-lg", "text-slate-400", "max-w-2xl", "mx-auto", "leading-relaxed") }) {
                    Text(sampleProfile.bio)
                }
            }

            // Main Columns
            Div(attrs = { classes("grid", "grid-cols-1", "md:grid-cols-3", "gap-8") }) {
                // Left Column: Skills and Metadata
                Div(attrs = { classes("md:col-span-1", "space-y-6") }) {
                    Div(attrs = {
                        classes(
                            "bg-slate-900/60", "backdrop-blur-xl", "rounded-2xl", 
                            "p-6", "border", "border-slate-800"
                        )
                    }) {
                        H2(attrs = { classes("text-lg", "font-bold", "text-white", "mb-4") }) {
                            Text("Habilidades Chave")
                        }
                        
                        Div(attrs = { classes("flex", "flex-wrap", "gap-2") }) {
                            sampleProfile.skills.forEach { skill ->
                                Span(attrs = {
                                    classes(
                                        "px-2.5", "py-1", "rounded-lg", "text-xs", 
                                        "font-medium", "bg-slate-800", "text-slate-300", 
                                        "border", "border-slate-700/50"
                                    )
                                }) {
                                    Text(skill)
                                }
                            }
                        }
                    }

                    Div(attrs = {
                        classes(
                            "bg-slate-900/60", "backdrop-blur-xl", "rounded-2xl", 
                            "p-6", "border", "border-slate-800"
                        )
                    }) {
                        H2(attrs = { classes("text-lg", "font-bold", "text-white", "mb-4") }) {
                            Text("Contato & Links")
                        }
                        
                        Ul(attrs = { classes("space-y-3", "text-sm", "text-slate-300") }) {
                            Li {
                                A(
                                    href = "mailto:contato@devfoliopro.com",
                                    attrs = { classes("hover:text-indigo-400", "transition-colors") }
                                ) {
                                    Text("📧 contato@devfoliopro.com")
                                }
                            }
                            Li {
                                A(
                                    href = "https://github.com",
                                    attrs = { classes("hover:text-indigo-400", "transition-colors") }
                                ) {
                                    Text("🐙 GitHub Profile")
                                }
                            }
                            Li {
                                A(
                                    href = "https://linkedin.com",
                                    attrs = { classes("hover:text-indigo-400", "transition-colors") }
                                ) {
                                    Text("💼 LinkedIn Profissional")
                                }
                            }
                        }
                    }
                }

                // Right Column: Experience & Suggested Paths
                Div(attrs = { classes("md:col-span-2", "space-y-6") }) {
                    // Work Experience Card
                    Div(attrs = {
                        classes(
                            "bg-slate-900/60", "backdrop-blur-xl", "rounded-2xl", 
                            "p-6", "border", "border-slate-800"
                        )
                    }) {
                        H2(attrs = { classes("text-xl", "font-bold", "text-white", "mb-6") }) {
                            Text("Experiência em Destaque")
                        }

                        Div(attrs = { classes("space-y-6") }) {
                            Div(attrs = { classes("relative", "pl-6", "border-l-2", "border-indigo-500/30") }) {
                                Div(attrs = {
                                    classes(
                                        "absolute", "-left-[9px]", "top-1.5", "w-4", "h-4", 
                                        "rounded-full", "bg-indigo-500", "border-4", 
                                        "border-slate-950"
                                    )
                                })
                                H3(attrs = { classes("text-lg", "font-bold", "text-slate-100") }) {
                                    Text("SRE & Arquiteto Cloud Senior")
                                }
                                P(attrs = { classes("text-sm", "text-indigo-400", "mb-2") }) {
                                    Text("NuvemCorporativa S.A. | 2024 - Presente")
                                }
                                P(attrs = { classes("text-slate-400", "text-sm", "leading-relaxed") }) {
                                    Text("Liderança técnica na migração de servidores legados para arquitetura de microsserviços no Kubernetes. Redução de 40% nos custos operacionais com estratégias inteligentes na AWS.")
                                }
                            }

                            Div(attrs = { classes("relative", "pl-6", "border-l-2", "border-indigo-500/30") }) {
                                Div(attrs = {
                                    classes(
                                        "absolute", "-left-[9px]", "top-1.5", "w-4", "h-4", 
                                        "rounded-full", "bg-indigo-500", "border-4", 
                                        "border-slate-950"
                                    )
                                })
                                H3(attrs = { classes("text-lg", "font-bold", "text-slate-100") }) {
                                    Text("Analista de Redes & Segurança (NOC)")
                                }
                                P(attrs = { classes("text-sm", "text-indigo-400", "mb-2") }) {
                                    Text("GlobalTech Telecom | 2021 - 2024")
                                }
                                P(attrs = { classes("text-slate-400", "text-sm", "leading-relaxed") }) {
                                    Text("Administração corporativa de firewalls FortiGate e roteadores Cisco. Monitoramento em alta disponibilidade (99.9% de uptime) de infraestrutura de missão crítica.")
                                }
                            }
                        }
                    }

                    // Suggested Certifications Card
                    Div(attrs = {
                        classes(
                            "bg-slate-900/60", "backdrop-blur-xl", "rounded-2xl", 
                            "p-6", "border", "border-slate-800"
                        )
                    }) {
                        H2(attrs = { classes("text-xl", "font-bold", "text-white", "mb-2") }) {
                            Text("Metas de Certificação (Sugeridos por IA)")
                        }
                        P(attrs = { classes("text-sm", "text-slate-400", "mb-6") }) {
                            Text("Certificações sugeridas pelo motor de IA integradas do DevFolio Pro para aceleração de carreira:")
                        }

                        Div(attrs = { classes("space-y-4") }) {
                            // Cert 1
                            Div(attrs = { classes("bg-slate-950/50", "rounded-xl", "p-4", "border", "border-slate-800/80") }) {
                                Div(attrs = { classes("flex", "justify-between", "items-start", "mb-2") }) {
                                    H4(attrs = { classes("font-bold", "text-indigo-300") }) {
                                        Text("AWS Certified Solutions Architect")
                                    }
                                    Span(attrs = { classes("text-xs", "font-semibold", "bg-indigo-500/10", "text-indigo-300", "px-2.5", "py-0.5", "rounded-full") }) {
                                        Text("Cloud & DevOps")
                                    }
                                }
                                P(attrs = { classes("text-slate-400", "text-xs", "mb-3") }) {
                                    Text("Duração estimada: 80 horas | Custo: US$ 150")
                                }
                                P(attrs = { classes("text-slate-300", "text-sm", "leading-relaxed") }) {
                                    Text("Garante conhecimento profundo em modelagem de arquiteturas resilientes e econômicas na nuvem AWS.")
                                }
                            }

                            // Cert 2
                            Div(attrs = { classes("bg-slate-950/50", "rounded-xl", "p-4", "border", "border-slate-800/80") }) {
                                Div(attrs = { classes("flex", "justify-between", "items-start", "mb-2") }) {
                                    H4(attrs = { classes("font-bold", "text-indigo-300") }) {
                                        Text("Fortinet NSE 4 - Network Security Professional")
                                    }
                                    Span(attrs = { classes("text-xs", "font-semibold", "bg-purple-500/10", "text-purple-300", "px-2.5", "py-0.5", "rounded-full") }) {
                                        Text("Segurança")
                                    }
                                }
                                P(attrs = { classes("text-slate-400", "text-xs", "mb-3") }) {
                                    Text("Duração estimada: 40 horas | Custo: US$ 150")
                                }
                                P(attrs = { classes("text-slate-300", "text-sm", "leading-relaxed") }) {
                                    Text("Valida as competências de configuração, gerenciamento e monitoramento de segurança com equipamentos FortiGate.")
                                }
                            }
                        }
                    }
                }
            }

            // Footer
            Footer(attrs = { classes("text-center", "mt-16", "text-slate-500", "text-xs") }) {
                Text("Gerado automaticamente via DevFolio Pro em arquitetura Kotlin Multiplatform.")
            }
        }
    }
}
