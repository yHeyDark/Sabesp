# 🌊 Sabesp – Sistema de Chamados para Problemas de Esgoto

Sistema **Java Web** desenvolvido com **Spring Boot** para a **Secretaria de Meio Ambiente de Santos**, com o objetivo de registrar e acompanhar chamados relacionados a problemas de esgoto na cidade.

> Projeto acadêmico desenvolvido para a disciplina de Spring Boot – [Fatec Rubens Lar].

---

## 📌 Sobre o projeto

Problemas de esgoto afetam diretamente a saúde pública e o meio ambiente. Este sistema conecta **cidadãos**, **Sabesp** e **Prefeitura** em um único fluxo: o cidadão registra a ocorrência, a Sabesp atua sobre ela e a Prefeitura acompanha tudo.

## 👥 Perfis de usuário e funcionalidades

### 🧑 Pessoa comum
- Registrar um chamado informando:
  - Endereço da ocorrência
  - Tipo do problema
  - Data e hora
  - Imagem (foto do problema)

### 🛠️ Sabesp
- Visualizar os chamados registrados
- Alterar o status da solicitação (ex.: Aberto → Em andamento → Resolvido)
- Escrever comentários sobre o atendimento

### 🏛️ Prefeitura
- Acompanhar todos os chamados
- Aplicar filtros (status, tipo, data, endereço, etc.)

## 🧰 Tecnologias

- Java 22+
- Spring Boot
- Spring MVC
- Spring Data JPA
- Spring Security (controle de acesso por perfil)
- Thymeleaf
- Banco de dados relacional (SQL SERVER / H2)
- Maven
- HTML, CSS e Bootstrap

## 🗂️ Estrutura sugerida

```
src/main/java/.../
├── controller/     # Controladores web
├── model/          # Entidades (Chamado, Usuario, Comentario...)
├── repository/     # Repositórios JPA
├── service/        # Regras de negócio
└── config/         # Segurança e configurações

src/main/resources/
├── templates/      # Páginas Thymeleaf
├── static/         # CSS, JS e imagens
└── application.properties
```

## 🔄 Fluxo de um chamado

1. O cidadão registra o chamado (endereço, tipo, data, hora e imagem).
2. A Sabesp visualiza o chamado, atualiza o status e comenta.
3. A Prefeitura acompanha e filtra os chamados para monitoramento.

## 🚀 Como executar

```bash
# 1. Clone o repositório
git clone https://github.com/seu-usuario/esgoto-santos.git

# 2. Entre na pasta
cd esgoto-santos

# 3. Configure o banco de dados em src/main/resources/application.properties

# 4. Execute a aplicação
./mvnw spring-boot:run
```

Acesse em: `http://localhost:8080`

## ✅ Requisitos do trabalho

- [ ] Sistema Java Web com framework Spring
- [ ] Registro de chamado por pessoa comum (endereço, tipo, data, hora, imagem)
- [ ] Visualização, alteração de status e comentário pela Sabesp
- [ ] Acompanhamento e filtros pela Prefeitura

## 👨‍💻 Autores

- Derik da Silva Rocha
- Kawan Weber
- Beatria batista
- Danilo
- Beatria Giovanna

## 📄 Licença

Projeto acadêmico.
