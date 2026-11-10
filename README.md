# 🌊 Sabesp – Sistema de Chamados para Problemas de Esgoto

Sistema **Java Web** desenvolvido com **Spring Boot** para a **Secretaria de Meio Ambiente de Santos**, com o objetivo de registrar, gerenciar e acompanhar chamados relacionados a problemas de infraestrutura de esgoto na cidade.

> 🎓 Projeto acadêmico desenvolvido para a disciplina de Spring Boot – **Fatec Baixada Santista - Rubens Lara (Fatec RL)**.

---

## 📌 Sobre o projeto

Problemas de esgoto afetam diretamente a saúde pública e o meio ambiente local. Este sistema conecta **Cidadãos**, **Sabesp** e **Prefeitura** em um fluxo integrado: o munícipe registra a ocorrência, a Sabesp atua operacionalmente sobre ela e a Prefeitura realiza o monitoramento do serviço.

## 👥 Perfis de Usuário e Funcionalidades

### 🧑 Pessoa Comum (Cidadão)
- **Registrar chamado** contendo:
  - Endereço completo da ocorrência
  - Categoria/Tipo do problema
  - Data e hora do registro
  - Upload de imagem (foto evidenciando o problema)

### 🛠️ Sabesp (Operacional)
- **Painel de Gestão:** Visualizar todos os chamados registrados.
- **Fluxo de Trabalho:** Alterar o status da solicitação (ex.: `Aberto` → `Em andamento` → `Resolvido`).
- **Feedback:** Registrar comentários técnicos e atualizações sobre o atendimento.

### 🏛️ Prefeitura (Auditoria/Monitoramento)
- **Dashboard:** Acompanhar o volume e o status de todos os chamados.
- **Filtros Avançados:** Buscar ocorrências por status, tipo, período de data e localização.

## 🧰 Tecnologias e Stack

- **Linguagem:** Java 21+
- **Framework Core:** Spring Boot
- **Web & Roteamento:** Spring MVC
- **Persistência:** Spring Data JPA
- **Segurança:** Spring Security (Controle de acesso baseado em Roles/Perfis)
- **Template Engine:** Thymeleaf
- **Banco de Dados:** PostgreSQL (Produção) / H2 Database (Testes locais)
- **Build & Dependências:** Maven
- **Front-end:** HTML5, CSS3, JavaScript e Bootstrap

## 🗂️ Estrutura de Pacotes

```text
src/main/java/br/com/sabesp/
├── controller/     # Endpoints e roteamento web
├── model/          # Entidades de Domínio (Chamado, Usuario, Comentario...)
├── repository/     # Interfaces de acesso ao banco (Spring Data JPA)
├── service/        # Regras de negócio e validações
└── config/         # Configurações de Segurança e Beans

src/main/resources/
├── templates/      # Views em Thymeleaf (.html)
├── static/         # Assets estáticos (CSS, JS, Imagens)
└── application.properties # Variáveis de ambiente e conexão com o banco

```

## 🔄 Fluxo de Vida do Chamado

1. O **Cidadão** identifica o vazamento/problema, acessa o portal e registra o chamado com as evidências (foto e endereço).
2. A **Sabesp** recebe a notificação no painel operacional, inicia o atendimento, atualiza o status para *Em andamento* e insere comentários técnicos.
3. A **Prefeitura** monitora as métricas de resolução e filtra os dados para garantir o cumprimento dos prazos.

## 🚀 Como executar o projeto localmente

```bash
# 1. Clone este repositório
git clone [https://github.com/yHeyDark/Sabesp.git](https://github.com/yHeyDark/Sabesp.git)

# 2. Acesse o diretório do projeto
cd Sabesp

# 3. Configure as credenciais do PostgreSQL no arquivo:
# src/main/resources/application.properties

# 4. Execute a aplicação via Maven Wrapper
./mvnw spring-boot:run

```

Acesse a aplicação no navegador via: `http://localhost:8080`

## ✅ Requisitos do Trabalho (Checklist)

* [ ] Sistema Java Web com framework Spring
* [ ] Registro de chamado por pessoa comum (endereço, tipo, data, hora, imagem)
* [ ] Visualização, alteração de status e comentário pela Sabesp
* [ ] Acompanhamento e filtros pela Prefeitura

## 👨‍💻 Autores e Colaboradores

* Derik da Silva Rocha
* Kawan Weber
* Beatria Batista
* Danilo
* Beatria Giovanna

## 📄 Licença

Projeto acadêmico - Código aberto para fins educacionais.

```
Projeto acadêmico - Código aberto para fins educacionais.
```
