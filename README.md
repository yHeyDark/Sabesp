# 🌊 Sabesp – Sistema de Chamados para Problemas de Esgoto

Sistema **Java Web** desenvolvido com **Spring Boot** para a **Secretaria de Meio Ambiente de Santos**, com o objetivo de registrar, gerenciar e acompanhar chamados relacionados a problemas de infraestrutura de esgoto na cidade.

> 🎓 Projeto acadêmico desenvolvido para a disciplina de Desenvolvimento para Servidores I – **Fatec Baixada Santista - Rubens Lara (Fatec RL)**.

---

## 📌 Sobre o projeto

Problemas de esgoto afetam diretamente a saúde pública e o meio ambiente local. Este sistema conecta **Cidadãos**, **Sabesp** e **Prefeitura** em um fluxo integrado: o munícipe registra a ocorrência, a Sabesp atua operacionalmente sobre ela e a Prefeitura realiza o monitoramento do serviço.

## 👥 Perfis de Usuário e Funcionalidades

### 🧑 Pessoa Comum (Cidadão)
- **Auto-cadastro** pelo modal público da Central de Chamados (o perfil é sempre forçado a `COMUM` no backend).
- **Registrar chamado** contendo:
  - Endereço completo da ocorrência
  - Categoria/Tipo do problema
  - Data e hora do registro
  - Upload de imagem (foto evidenciando o problema)
- **Acompanhar** apenas os próprios chamados e **comentar** neles.

### 🛠️ Sabesp (Operacional)
- **Painel de Gestão:** visualizar todos os chamados registrados.
- **Fluxo de Trabalho:** alterar o status da solicitação (`Aberto` → `Em andamento` → `Resolvido` → `Fechado`).
- **Feedback:** registrar comentários técnicos e atualizações sobre o atendimento.
- **Base de endereços:** cadastrar novos endereços na malha operacional.
- ⛔ **Não cria conta de ninguém** — nem cidadãos, nem institucionais.

### 🏛️ Prefeitura (Auditoria/Monitoramento)
- **Dashboard:** acompanhar o volume e o status de todos os chamados.
- **Filtros Avanancados:** buscar ocorrências por status, tipo, período de data e localização.
- **Gestão de contas:** **somente a Prefeitura cria contas institucionais** (Sabesp e Prefeitura) na aba *Base de Cadastros*. Ela **não** cria contas de cidadão.

### 🔐 Regra de criação de contas (resumo)

| Quem solicita | Conta COMUM (cidadão) | Conta SABESP | Conta PREFEITURA |
|---|---|---|---|
| Público (auto-cadastro `/api/auth/registro`) | ✅ (perfil forçado a `COMUM`) | ❌ 403 | ❌ 403 |
| Sabesp | ❌ 403 | ❌ 403 | ❌ 403 |
| Prefeitura | ❌ 403 | ✅ | ✅ |

> Ou seja: **cidadãos se auto-cadastram**, **somente a Prefeitura cria contas institucionais** e a **Sabesp não cria conta de ninguém**.

## 🧰 Tecnologias e Stack

- **Linguagem:** Java 21
- **Framework Core:** Spring Boot 4 (Spring MVC — `spring-boot-starter-webmvc`)
- **Persistência:** Spring Data JPA + Hibernate
- **Banco de Dados:** H2 em memória (perfil `dev`, padrão) / PostgreSQL (perfil `prod`)
- **Validação:** Bean Validation (`spring-boot-starter-validation`)
- **Documentação da API:** springdoc-openapi (Swagger UI em `/swagger-ui.html`)
- **Mapeamento de objetos:** Lombok
- **Build & Dependências:** Maven (com Maven Wrapper — `mvnw`)
- **Front-end:** HTML5, CSS3 e JavaScript puros (vanilla) — **sem** template engine e **sem** framework CSS
- **Comunicação front↔back:** REST/JSON + cabeçalhos de identificação (`X-User-Role`, `X-User-Id`)

> ⚠️ O projeto **não** usa Thymeleaf, Bootstrap nem Spring Security: as páginas são HTML estático servido pelo próprio Spring Boot e o controle de acesso (RBAC) é feito na aplicação, via cabeçalhos enviados pelo front-end e validados no backend.

## 🏗️ Tipo de arquitetura e por que foi escolhida

### Arquitetura em camadas (Layered Architecture) — padrão MVC

```text
   Navegador (HTML/CSS/JS estáticos)
              │  fetch() + JSON + cabeçalhos X-User-*
              ▼
 ┌──────────────────────────────────────────────┐
 │  controller/   → Recebe a requisição REST,   │
 │                  valida papel (RBAC),        │
 │                  devolve HTTP + DTO          │
 ├──────────────────────────────────────────────┤
 │  service/      → Regras de negócio           │
 │                  (status forçado, validações, │
 │                   criação de contas)         │
 ├──────────────────────────────────────────────┤
 │  repository/   → Acesso ao banco             │
 │                  (Spring Data JPA)           │
 ├──────────────────────────────────────────────┤
 │  model/        → Entidades JPA + enums       │
 └──────────────────────────────────────────────┘
              │
              ▼
        H2 (dev) / PostgreSQL (prod)

  dto/          → contratos de entrada/saída (não expõe entidades)
  exception/    → tratamento global de erros (GlobalExceptionHandler)
  config/       → CORS, OpenAPI e demais beans
```

### Por que essa arquitetura?

1. **Separação de responsabilidades** — cada camada tem uma única preocupação: o *controller* só conversa com o HTTP, o *service* só concentra regra de negócio e o *repository* só fala com o banco. Isso deixa o código fácil de localizar, testar e evoluir.
2. **Facilidade para impor o RBAC** — as regras "só cidadão abre chamado", "só Sabesp muda status" e "só Prefeitura cria contas institucionais" ficam centralizadas nos *controllers/services*, em um único lugar auditável, em vez de espalhadas pela interface.
3. **Desacoplamento do banco** — trocar H2 (dev) por PostgreSQL (prod) é apenas mudar o perfil de Spring (`application-dev.properties` / `application-prod.properties`), sem tocar em nenhuma linha de código, graças ao Spring Data JPA.
4. **DTOs em vez de entidades** — a API nunca devolve a entidade JPA diretamente; usa `RequestDTO`/`ResponseDTO`, o que evita vazar dados sensíveis (ex.: senha) e protege o modelo interno de mudanças.
5. **Complexidade adequada ao projeto** — é a arquitetura padrão e amplamente ensinada para aplicações Spring Boot (estilo MVC). Adotar microsserviços, CQRS ou hexagonal traria custo desnecessário para um sistema com 3 perfis e um conjunto pequeno de recursos.

## 🗂️ Estrutura do Projeto

```text
sabesp/
├── pom.xml                        # Dependências e build (Maven)
├── mvnw / mvnw.cmd                # Maven Wrapper (não exige Maven instalado)
├── README.md                      # Este documento
│
└── src/main/
    ├── java/br/com/Sabesp/
    │   ├── SabespApplication.java     # Classe principal (ponto de entrada Spring Boot)
    │   ├── config/
    │   │   ├── OpenApiConfig.java     # Metadados do Swagger
    │   │   └── WebConfig.java         # CORS (libera o front estático)
    │   ├── controller/                # Endpoints REST (10 controllers)
    │   │   ├── AuthController         #   /api/auth → login, registro, recuperar-senha
    │   │   ├── ChamadoController      #   /api/chamados → CRUD de chamados
    │   │   ├── UsuarioController      #   /api/usuarios → CRUD com RBAC de criação
    │   │   ├── ComentarioController   #   /api/chamados/{id}/comentarios
    │   │   ├── EnderecoController     #   /api/enderecos
    │   │   ├── PrefeituraController   #   /api/prefeituras
    │   │   ├── TipoProblemaController #   /api/tipos-problema
    │   │   ├── StatusChamadoController#   /api/status-chamado
    │   │   ├── FiltroPesquisaController # /api/filtros
    │   │   └── HomeController         #   redireciona "/" → landing estática
    │   ├── service/                   # Regras de negócio (9 services)
    │   ├── repository/                # Interfaces Spring Data JPA (8 repositórios)
    │   ├── model/
    │   │   ├── *.java                 # Entidades: Chamado, Usuario, Comentario,
    │   │   │                          #   Endereco, Prefeitura, TipoProblema,
    │   │   │                          #   StatusChamado, FiltroPesquisa
    │   │   └── enums/
    │   │       ├── TipoUsuario.java       # COMUM | SABESP | PREFEITURA
    │   │       └── StatusChamadoEnum.java # ABERTO | EM_ANDAMENTO | RESOLVIDO | FECHADO
    │   ├── dto/                       # 17 DTOs de entrada/saída
    │   └── exception/                 # GlobalExceptionHandler + ResourceNotFoundException
    │
    └── resources/
        ├── application.properties         # perfil ativo (dev), limites de upload
        ├── application-dev.properties     # H2 em memória (jdbc:h2:mem:sabespdb)
        ├── application-prod.properties    # PostgreSQL
        ├── data.sql                       # dados iniciais (tipos, status, endereços, usuários demo)
        └── static/                        # Front-end (HTML/CSS/JS puro)
            ├── index.html                 # Landing page institucional (público)
            ├── corporativo.html           # Área Corporativa → escolha do portal
            ├── login-sabesp.html          # Login do Portal Sabesp (role SABESP)
            ├── login-prefeitura.html      # Login do Portal Prefeitura (role PREFEITURA)
            ├── chamados.html              # Central de Chamados (SPA por perfis)
            ├── css/style.css              # Estilos de todas as páginas
            ├── js/app.js                  # Lógica da Central de Chamados (RBAC, filtros, CRUDs)
            └── js/portal-login.js         # Lógica compartilhada dos logins institucionais
```

## 🔑 Autenticação e RBAC

O sistema **não** usa sessão servidor-side nem token JWT: após o login (`POST /api/auth/login`), o front-end guarda o usuário no `localStorage` e envia os cabeçalhos `X-User-Role` (perfil) e `X-User-Id` (id) em cada requisição. O backend valida esses cabeçalhos em cada endpoint.

Regras principais:

| Regra | Quem pode |
|---|---|
| Criar chamado (status sempre `ABERTO`) | Somente `COMUM` |
| Alterar status / deletar chamado | Somente `SABESP` |
| Visualizar/filtrar tudo (auditoria) | `PREFEITURA` (só consulta — não altera) |
| Criar conta institucional (`SABESP`/`PREFEITURA`) | Somente `PREFEITURA` |
| Criar conta de cidadão | Auto-cadastro público (`/api/auth/registro`, perfil forçado a `COMUM`) |
| Criar qualquer conta sendo `SABESP` | ❌ Ninguém — a Sabesp não cria conta de ninguém |

## 🔄 Fluxo de Vida do Chamado

1. O **Cidadão** identifica o vazamento/problema, acessa a Central de Chamados, faz login (ou auto-cadastro) e registra o chamado com as evidências (foto e endereço) — o status nasce como **Aberto**.
2. A **Sabesp** recebe a notificação no painel operacional, inicia o atendimento, atualiza o status para *Em andamento* e insere comentários técnicos; depois marca como *Resolvido* e *Fechado*.
3. A **Prefeitura** monitora as métricas de resolução, filtra os dados para garantir o cumprimento dos prazos e administra as contas institucionais.

## 🌐 Mapa de páginas

| Rota | Página | Público |
|---|---|---|
| `/` | Landing page institucional (Sobre, Serviços, Números, Atendimento) | Todos |
| `/corporativo.html` | Área Corporativa — escolha do portal | Todos |
| `/login-sabesp.html` | Login Portal Sabesp | Institucional |
| `/login-prefeitura.html` | Login Portal Prefeitura | Institucional |
| `/chamados` | Central de Chamados (SPA com visão por perfil) | Logados |
| `/swagger-ui.html` | Documentação da API (springdoc) | Todos (link removido da UI) |

## 🚀 Como executar o projeto localmente

```bash
# 1. Clone este repositório
git clone https://github.com/yHeyDark/Sabesp.git

# 2. Acesse o diretório do projeto
cd Sabesp

# 3. Execute a aplicação via Maven Wrapper (perfil dev = H2 em memória)
./mvnw spring-boot:run        # Linux/macOS
.\mvnw.cmd spring-boot:run    # Windows
```

Acesse no navegador: `http://localhost:8080`

> Para produção, configure o PostgreSQL em `application-prod.properties` e ative o perfil com `--spring.profiles.active=prod`.

### 👤 Contas de demonstração (criadas via `data.sql`)

| Perfil | E-mail | Senha |
|---|---|---|
| Cidadão | `carlos.silva@email.com` | `123456` |
| Sabesp | `marina.santos@sabesp.com.br` | `admin123` |
| Prefeitura | `fiscalizacao@prefeitura.sp.gov.br` | `pref123` |

> Não há botões de "conta teste" na interface — as contas acima servem apenas para login manual.

## ✅ Requisitos do Trabalho (Checklist)

* [x] Sistema Java Web com framework Spring
* [x] Registro de chamado por pessoa comum (endereço, tipo, data, hora, imagem)
* [x] Visualização, alteração de status e comentário pela Sabesp
* [x] Acompanhamento e filtros pela Prefeitura
* [x] Controle de acesso por perfil (RBAC) e regra de criação de contas
* [x] Site responsivo (menu hambúrguer e breakpoints até 480px)
* [x] Código documentado (Javadoc em português em todas as camadas)

## 👨‍💻 Autores e Colaboradores

* Derik da Silva Rocha
* Kawan Weber
* Beatriz Batista
* Danilo
* Beatriz Giovanna

## 📄 Licença

Projeto acadêmico - Código aberto para fins educacionais.
