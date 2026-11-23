/**
 * ============================================================
 *  PORTAL LOGIN — autenticação institucional
 *  Usado pelas páginas login-sabesp.html e login-prefeitura.html
 *
 *  Cada página define o perfil exigido no atributo:
 *      <body data-portal-role="SABESP">   ou   <body data-portal-role="PREFEITURA">
 *
 *  O que este arquivo faz:
 *   1. Valida e envia o login para /api/auth/login
 *   2. Garante que a conta pertence ao perfil do portal
 *   3. Salva a sessão e redireciona para a central de chamados
 *   4. Permite trocar a senha pela rota /api/auth/recuperar-senha
 * ============================================================
 */

// Perfil que este portal aceita (lido do <body data-portal-role="...">)
const PORTAL_ROLE = document.body.dataset.portalRole;
// Nome amigável do portal usado nas mensagens
const PORTAL_NOME = PORTAL_ROLE === "SABESP" ? "Sabesp" : "Prefeitura";

document.addEventListener("DOMContentLoaded", () => {
    // Referências dos elementos da página
    const formLogin = document.getElementById("formPortalLogin");
    const formRecuperar = document.getElementById("formPortalRecuperar");
    const alerta = document.getElementById("portalAlert");

    // ── Pré-preenche o e-mail quando vindo da Central de Chamados ──
    // Ex.: /login-sabesp.html?email=fulano@sabesp.com.br
    const params = new URLSearchParams(window.location.search);
    const emailVindo = params.get("email");
    if (emailVindo) {
        document.getElementById("portalEmail").value = emailVindo;
        document.getElementById("portalSenha").focus();
    }

    // ── Se já existe sessão válida deste portal, entra direto ──
    const salvo = localStorage.getItem("sabesp_user");
    if (salvo) {
        try {
            const usuario = JSON.parse(salvo);
            if (usuario && usuario.tipoUsuario === PORTAL_ROLE) {
                window.location.href = "/chamados";
                return;
            }
        } catch (e) {
            localStorage.removeItem("sabesp_user");
        }
    }

    // ── Mostra mensagem de erro ou sucesso no topo do cartão ──
    function mostrarAlerta(mensagem, tipo = "erro") {
        if (!alerta) return;
        alerta.textContent = mensagem;
        alerta.className = `portal-alert portal-alert--${tipo === "ok" ? "ok" : "erro"}`;
        alerta.style.display = "block";
    }

    // ── Alterna entre as telas "Entrar" e "Recuperar senha" ──
    function alternarTela(tela) {
        const loginVisivel = tela === "login";
        formLogin.style.display = loginVisivel ? "block" : "none";
        formRecuperar.style.display = loginVisivel ? "none" : "block";
        if (alerta) alerta.style.display = "none";
        // Troca o título e o subtítulo do cartão conforme a tela
        document.getElementById("portalTitulo").textContent =
            loginVisivel ? `Entrar no Portal ${PORTAL_NOME}` : `Trocar senha — Portal ${PORTAL_NOME}`;
        document.getElementById("portalSubtitulo").textContent =
            loginVisivel
                ? `Acesso exclusivo da equipe ${PORTAL_NOME}. Cidadãos devem usar a Central de Chamados.`
                : "Informe o e-mail cadastrado e escolha uma nova senha.";
    }

    // Liga o link "Esqueci minha senha"
    const linkEsqueci = document.getElementById("linkEsqueciSenha");
    if (linkEsqueci) {
        linkEsqueci.addEventListener("click", (e) => {
            e.preventDefault();
            alternarTela("recuperar");
        });
    }
    // Liga o link "Voltar" da tela de recuperação
    const linkVoltar = document.getElementById("linkVoltarLogin");
    if (linkVoltar) {
        linkVoltar.addEventListener("click", (e) => {
            e.preventDefault();
            alternarTela("login");
        });
    }

    // ── Envio do formulário de login ──
    formLogin.addEventListener("submit", async (e) => {
        e.preventDefault();
        const email = document.getElementById("portalEmail").value.trim();
        const senha = document.getElementById("portalSenha").value;

        if (!email || !senha) {
            mostrarAlerta("Preencha o e-mail e a senha.");
            return;
        }

        try {
            const res = await fetch("/api/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, senha })
            });

            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                throw new Error(err.mensagem || "E-mail ou senha incorretos.");
            }

            const usuario = await res.json();

            // Bloqueia contas de outro perfil (ex.: cidadão tentando entrar no portal Sabesp)
            if (usuario.tipoUsuario !== PORTAL_ROLE) {
                const perfilConta = usuario.tipoUsuario === "COMUM" ? "Cidadão" : "Outro portal";
                mostrarAlerta(
                    `Esta conta é do perfil "${perfilConta}" e não pode acessar o Portal ${PORTAL_NOME}.`
                );
                return;
            }

            // Salva a sessão e segue para a central
            localStorage.setItem("sabesp_user", JSON.stringify(usuario));
            window.location.href = "/chamados";
        } catch (err) {
            mostrarAlerta(err.message || "Erro ao realizar login.");
        }
    });

    // ── Envio do formulário de recuperação de senha ──
    formRecuperar.addEventListener("submit", async (e) => {
        e.preventDefault();
        const email = document.getElementById("recPortalEmail").value.trim();
        const novaSenha = document.getElementById("recPortalSenha").value;
        const confirmacao = document.getElementById("recPortalSenhaConfirm").value;

        // Validação simples: senhas precisam ser iguais
        if (novaSenha !== confirmacao) {
            mostrarAlerta("As senhas não coincidem. Confira e tente novamente.");
            return;
        }
        if (novaSenha.length < 6) {
            mostrarAlerta("A nova senha deve ter pelo menos 6 caracteres.");
            return;
        }

        try {
            const res = await fetch("/api/auth/recuperar-senha", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, novaSenha })
            });

            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                throw new Error(err.mensagem || "Não foi possível redefinir a senha.");
            }

            // Volta para a tela de login com mensagem de sucesso
            formRecuperar.reset();
            alternarTela("login");
            mostrarAlerta("Senha redefinida com sucesso! Faça login com a nova senha.", "ok");
        } catch (err) {
            mostrarAlerta(err.message || "Erro ao redefinir senha.");
        }
    });
});
