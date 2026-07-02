# DevFolio Pro 🚀
**Seu Portfólio Inteligente e Gerenciador de Currículos Dinâmicos para Android**

O **DevFolio Pro** é um aplicativo Android moderno e inteligente projetado para desenvolvedores e profissionais de tecnologia que precisam criar, gerenciar e personalizar seus portfólios e currículos de forma rápida e adaptada para diferentes oportunidades do mercado.

Através do poder da **Inteligência Artificial (Gemini API)** e de uma arquitetura offline-first com **sincronização automática na nuvem (Firebase)**, o DevFolio Pro permite que você mantenha múltiplas versões do seu currículo (ex: "Principal", "Vaga Mobile", "DevOps") prontas para serem alternadas com um único clique.

---

## 📸 Funcionalidades Principais

### 1. 📂 Gerenciamento de Currículos Dinâmicos
* **Múltiplos Perfis na Nuvem:** Crie e salve variações do seu portfólio de acordo com os focos das vagas desejadas.
* **Seleção Rápida:** Alterne dinamicamente a visualização local do portfólio importando qualquer uma das versões salvas no Firebase.
* **Persistência Offline-First:** Toda a edição e leitura de dados é realizada localmente em um banco de dados relacional criptografado, garantindo alta velocidade e funcionamento sem internet.

### 2. 🧠 Melhoria de Perfil por IA (Gemini API)
* **Otimização de Biografia e Cargo:** Forneça o cargo desejado (ex: *Engenheiro de Software Sênior*) e a IA analisará suas experiências atuais para reescrever seu título e bio de forma atraente, profissional e focada nas competências certas.

### 3. 🌐 Sincronização Inteligente & Nuvem (Firebase)
* **Envio & Recebimento (Push/Pull):** Escolha se deseja mesclar dados, substituir a cópia da nuvem pelas alterações locais, ou sobrescrever as configurações locais com a nuvem.
* **Detecção de Conflitos:** Interface intuitiva que permite escolher resoluções de conflitos amigáveis no salvamento.

### 4. 🔑 Autenticação Flexível (Real e Simulada)
* **Login Oficial com Google (Firebase Auth):** Conecte-se com segurança usando sua conta Google.
* **Modo de Teste Simulado (Simulated Auth):** Permite que qualquer pessoa avalie e teste o fluxo completo de sincronização de múltiplos currículos em tempo real no navegador sem requerer chaves privadas configuradas previamente.

### 5. 🧑‍🎨 Menu de Perfil Dinâmico (Barra de Navegação)
* **Visualização Rápida de Sessão:** Ícone dinâmico na barra inferior que exibe a foto de perfil do Google do usuário conectado (ou suas iniciais se não possuir foto).
* **Painel de Controle Flutuante:**
  * Informações completas da conta e tipo de sessão (Real vs. Simulada).
  * Lista rápida de portfólios disponíveis na nuvem para alteração imediata de visualização.
  * **Logout Seguro (Wipe Local):** Ao sair da conta, todos os dados confidenciais do portfólio armazenados localmente no Room Database são excluídos de forma segura para proteger sua privacidade em dispositivos compartilhados.

### 6. 🎨 Customização Visual (Material 3)
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
* **Gemini REST Client:** Conectividade direta e segura com a API do Gemini para reestruturação inteligente do portfólio.

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
│   │   └── SettingsScreen  # Configurações do App, Sincronização avançada e Seleção de Cores
│   │
│   ├── theme
│   │   └── Theme.kt        # Sistema de temas e cores dinâmicas (Material 3)
│   │
│   └── viewmodel
│       └── PortfolioViewModel   # Detentor do estado de tela e regras de negócios da UI
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
# Chave da API do Google Gemini (para as otimizações de IA)
GEMINI_API_KEY=sua_chave_do_gemini_aqui

# ID de Cliente da Web do Google (para Autenticação com o Google no Firebase)
GOOGLE_WEB_CLIENT_ID=seu_web_client_id_do_google
```

*Nota: Se você deseja testar sem configurar chaves de terceiros, o **Modo de Teste Simulado** funcionará perfeitamente para simular logins e sincronizações de currículos no Firebase.*

### 3. Configurar o Firebase (Opcional para Login Real)
Se deseja habilitar o Firebase Auth e Firestore reais:
1. Crie um projeto no console do Firebase.
2. Adicione um app Android com o `applicationId` configurado em seu `app/build.gradle.kts`.
3. Baixe o arquivo `google-services.json` e insira-o na pasta `/app/`.
4. Habilite o provedor de login "Google" no Firebase Auth e crie um banco Firestore.

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
