# DevFolio Pro 🚀
**Seu Portfólio Inteligente, Gerenciador de Currículos Dinâmicos e Acelerador de Carreira para Android**

O **DevFolio Pro** é um aplicativo Android moderno, inteligente e focado no profissional de tecnologia do futuro. Ele foi projetado para desenvolvedores, engenheiros de infraestrutura, administradores de sistemas e especialistas de TI que desejam criar, gerenciar, personalizar e alavancar suas carreiras através de ferramentas automatizadas.

Agora, o aplicativo foi expandido para ir muito além de configurações simples: o antigo menu **"Ajustes"** foi totalmente transformado no **"Painel de Serviços"**, um hub completo de ferramentas profissionais que inclui análises inteligentes por IA, sugestões automáticas de estudo e geração de metas de certificação.

---

## 📸 Funcionalidades Principais

### 1. 🛠️ Painel de Serviços (Antigo menu "Ajustes")
* **Nova Identidade Visual:** Reformulado com o ícone de ferramentas (`Build`) e focado no enriquecimento profissional.
* **Centralização de Recursos:** Gerenciamento centralizado de temas, sincronizações avançadas e o novo recomendador inteligente de cursos.

### 2. 🎓 Recomendador de Cursos e Certificados (Novo!)
* **Análise de Competências por IA:** Com base no cargo atual, biografia, habilidades técnicas salvas e experiências de trabalho registradas no seu currículo local, o aplicativo faz um diagnóstico completo de carreira.
* **Sugestões de Alta Relevância:** Gera recomendações de certificações consagradas no mercado mundial (como AWS, Fortinet, Cisco, Google Android, Docker & Kubernetes, Scrum, etc.) adequadas ao seu perfil.
* **Mapeamento de Métricas Cruciais:**
  * **Tempo Estimado:** Quanto tempo leva para concluir ou se preparar.
  * **Custo Estimado:** Valores médios ou gratuidade dos exames/treinamentos.
  * **Serventia no Mercado:** Uma explicação detalhada de por que aquela certificação é importante e o que ela agrega ao seu perfil.
  * **Área de Atuação:** Direcionamento claro do campo de trabalho focado (ex: *Segurança de Redes*, *Cloud Computing*, *Desenvolvimento Mobile*).
* **"Focar Objetivo" (Integração Direta):** Adicione qualquer recomendação sugerida diretamente ao seu currículo local como um certificado planejado com um único clique.
* **Fallbacks Offline Inteligentes:** Caso as chaves do Gemini não estejam configuradas, o aplicativo conta com um motor semântico local que analisa termos específicos de infraestrutura, cloud, desenvolvimento web ou mobile para listar os melhores caminhos de estudo locais.

### 3. 📂 Gerenciamento de Currículos Dinâmicos
* **Múltiplos Perfis na Nuvem:** Crie e salve variações do seu portfólio de acordo com os focos das vagas desejadas (ex: "Desenvolvedor Android", "Especialista em Redes/NOC").
* **Seleção Rápida:** Alterne dinamicamente a visualização local do portfólio importando qualquer uma das versões salvas no Firebase.
* **Persistência Offline-First:** Toda a edição e leitura de dados é realizada localmente no Room Database, garantindo funcionamento sem internet e máxima performance.

### 4. 🧠 Melhoria de Perfil por IA (Gemini API)
* **Otimização de Biografia e Cargo:** Forneça o cargo desejado e a IA analisará suas experiências atuais para reescrever seu título e bio de forma atraente, profissional e com as palavras-chave mais buscadas por recrutadores.

### 5. 🌐 Sincronização Inteligente & Nuvem (Firebase)
* **Envio & Recebimento (Push/Pull):** Escolha se deseja mesclar dados, substituir a cópia da nuvem pelas alterações locais, ou sobrescrever as configurações locais com a nuvem.
* **Detecção de Conflitos:** Interface intuitiva que permite escolher resoluções de conflitos amigáveis no salvamento.

### 6. 🔑 Autenticação Flexível (Real e Simulada)
* **Login Oficial com Google (Firebase Auth):** Conecte-se com segurança usando sua conta Google.
* **Modo de Teste Simulado (Simulated Auth):** Permite que qualquer pessoa avalie e teste o fluxo completo de sincronização de múltiplos currículos em tempo real no navegador sem requerer chaves privadas configuradas previamente.

### 7. 🧑‍🎨 Menu de Perfil Dinâmico (Barra de Navegação)
* **Visualização Rápida de Sessão:** Ícone dinâmico na barra inferior que exibe a foto de perfil do Google do usuário conectado (ou suas iniciais se não possuir foto).
* **Painel de Controle Flutuante:**
  * Informações completas da conta e tipo de sessão (Real vs. Simulada).
  * Lista rápida de portfólios disponíveis na nuvem para alteração imediata de visualização.
  * **Logout Seguro (Wipe Local):** Ao sair da conta, todos os dados confidenciais do portfólio armazenados localmente no Room Database são excluídos de forma segura para proteger sua privacidade em dispositivos compartilhados.

### 8. 🎨 Customização Visual (Material 3)
* **Dynamic Theme & Color Picker:** Escolha sua cor de preferência e veja toda a interface do aplicativo adaptar-se harmonicamente.
* **Modo Claro/Escuro:** Tema adaptável para maior conforto visual.

---

## 🛠️ Arquitetura e Tecnologias

O projeto adota os padrões mais recomendados de engenharia de software para a plataforma Android:

* **Jetpack Compose:** Construção de interfaces nativas declarativas e totalmente responsivas utilizando Material Design 3.
* **MVVM (Model-View-ViewModel):** Separação clara de responsabilidades com Unidirectional Data Flow (UDF).
* **Room Database:** Persistência de dados segura com Kotlin Symbol Processing (KSP) e operações reativas em Flow.
* **Coroutines & Flow:** Gerenciamento eficiente e não bloqueante de tarefas de rede, banco de dados e UI assíncrona.
* **Coil:** Carregamento rápido e em cache de imagens remotas (fotos de perfil).
* **Gemini REST Client:** Conectividade direta e segura com a API do Gemini para sugestão de melhorias, refinamento de textos e curadoria educacional.

---

## 📂 Estrutura de Pastas

```text
/app/src/main/java/com/example
│
├── data
│   ├── local
│   │   ├── entities        # Tabelas do banco de dados (ProfileEntity, SkillEntity, ExperienceEntity)
│   │   ├── AppDatabase     # Ponto de acesso ao Room Database
│   │   └── PortfolioDao    # Consultas e inserções SQL abstratas
│   │
│   ├── remote
│   │   └── FirebaseSyncManager  # Gerenciador da sincronização Firebase / Modo Simulado
│   │
│   └── repository
│       └── PortfolioRepository  # Camada unificada de dados (Room + Chamadas de IA)
│
├── ui
│   ├── profile
│   │   └── ProfileDialog   # Caixa de diálogo de gerenciamento do perfil e alternação de currículos
│   │
│   ├── settings
│   │   └── SettingsScreen  # Tela de Serviços do App, Recomendador de Certificados, Sincronização e Temas
│   │
│   ├── theme
│   │   └── Theme.kt        # Sistema de temas e cores dinâmicas (Material 3)
│   │
│   └── viewmodel
│       └── PortfolioViewModel   # Detentor do estado de tela, regras de negócios e requisições da IA
│
└── MainActivity.kt         # Ponto de entrada do aplicativo e hospedeiro da navegação
```

---

## 🚀 Como Configurar e Executar

### 1. Clonar o Repositório e Abrir no Android Studio
Certifique-se de utilizar a versão mais recente do **Android Studio** compatível com o Gradle Kotlin DSL e Jetpack Compose.

### 2. Configurar as Variáveis de Ambiente
Crie um arquivo `.env` na raiz do seu projeto Android baseado no `.env.example` fornecido:

```env
# Chave da API do Google Gemini (para as otimizações de IA e recomendações de certificados)
GEMINI_API_KEY=sua_chave_do_gemini_aqui

# ID de Cliente da Web do Google (para Autenticação com o Google no Firebase)
GOOGLE_WEB_CLIENT_ID=seu_web_client_id_do_google
```

*Nota: Se você deseja testar sem configurar chaves de terceiros, o **Modo de Teste Simulado** funcionará perfeitamente para simular logins e sincronizações de currículos no Firebase, e as recomendações de certificados utilizarão a inteligência local integrada.*

---

## 🧪 Práticas de Teste

O projeto inclui um arcabouço robusto para testes automáticos de qualidade:

* **Testes de Unidade:** Validação de lógicas de negócio do ViewModel e do Repositório.
* **Testes Robolectric (Local JVM):** Simulação confiável de ciclos de vida de Activities e interações do Compose sem a necessidade de manter emuladores pesados abertos.
* **Visual Verification (Roborazzi):** Testes de regressão visual para garantir que as alterações no código de UI não quebrem o alinhamento e as cores dos elementos.

### Comandos de Teste Úteis:
* Executar todos os testes locais:
  ```bash
  gradle :app:testDebugUnitTest
  ```
* Gravar novos modelos de tela (Screenshots de Referência):
  ```bash
  gradle :app:recordRoborazziDebug
  ```
* Verificar se as alterações visuais causaram regressões inesperadas:
  ```bash
  gradle :app:verifyRoborazziDebug
  ```

---

## 💎 Design e Usabilidade

O DevFolio Pro respeita rigorosamente as diretrizes do **Material Design 3**:
* **Áreas de Toque Adequadas:** Todas as opções interativas e botões possuem área mínima de clique de **48dp** para garantir excelente acessibilidade.
* **Feedback Tátil/Visual:** Ripples integrados e indicadores de progresso dinâmicos para cada interação na rede ou na nuvem.
* **Espaçamento Generoso:** Margens e preenchimentos baseados em múltiplos de `8dp` que dão clareza e evitam poluição visual.

---
*DevFolio Pro - Transformando suas experiências profissionais no seu melhor portfólio.*
