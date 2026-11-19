// ============================================================
// SABESP GEOSANEAMENTO — LÓGICA DA APLICAÇÃO & RBAC
// ============================================================

// Estado Global
let chamados = [];
let usuarios = [];
let enderecos = [];
let tiposProblema = [];
let statusList = [];
let imagemChamadoBase64 = null;
let usuarioLogado = null;
let chamadoAtualId = null;

// Inicialização ao carregar o DOM
document.addEventListener("DOMContentLoaded", async () => {
    carregarUsuarioSalvo();
    await carregarDadosIniciais();
    configurarEventos();
    atualizarInterfaceUsuario();
});

// ── Gestão de Sessão do Usuário ──────────────────────────────
function carregarUsuarioSalvo() {
    const salvo = localStorage.getItem("sabesp_user");
    if (salvo) {
        try {
            usuarioLogado = JSON.parse(salvo);
        } catch (e) {
            usuarioLogado = null;
        }
    }
    // Se não houver usuário salvo, inicia sem usuário logado (deve fazer login)
    if (!usuarioLogado) {
        usuarioLogado = null;
    }
}

// ── Carregamento de Dados da API ────────────────────────────
async function carregarDadosIniciais() {
    try {
        await Promise.all([
            carregarUsuarios(),
            carregarEnderecos(),
            carregarTiposProblema(),
            carregarStatus(),
            carregarChamados()
        ]);
        atualizarSelects();
    } catch (err) {
        console.error("Erro ao carregar dados do servidor:", err);
        showToast("Erro ao carregar dados do servidor.", "error");
    }
}

function getAuthHeaders() {
    const headers = {};
    if (usuarioLogado) {
        headers["X-User-Role"] = usuarioLogado.tipoUsuario;
        headers["X-User-Id"] = usuarioLogado.idUsuario;
    }
    return headers;
}

async function carregarChamados() {
    try {
        const res = await fetch("/api/chamados", {
            headers: getAuthHeaders()
        });
        if (!res.ok) throw new Error("Falha ao obter chamados");
        chamados = await res.json();
        aplicarFiltros();
        atualizarMetricas(chamados);
    } catch (e) {
        console.error("Erro ao carregar chamados:", e);
    }
}

async function carregarUsuarios() {
    try {
        const res = await fetch("/api/usuarios");
        if (res.ok) usuarios = await res.json();
    } catch (e) { console.error(e); }
}

async function carregarEnderecos() {
    try {
        const res = await fetch("/api/enderecos");
        if (res.ok) enderecos = await res.json();
    } catch (e) { console.error(e); }
}

async function carregarTiposProblema() {
    try {
        const res = await fetch("/api/tipos-problema");
        if (res.ok) tiposProblema = await res.json();
    } catch (e) { console.error(e); }
}

async function carregarStatus() {
    try {
        const res = await fetch("/api/status-chamado");
        if (res.ok) statusList = await res.json();
    } catch (e) { console.error(e); }
}

// ── Atualização dos Dropdowns e Seletores (com RBAC) ─────────
function atualizarSelects() {
    // Select de Solicitantes
    const selUser = document.getElementById("chamadoUsuario");
    if (selUser) {
        if (usuarioLogado && usuarioLogado.tipoUsuario === "COMUM") {
            selUser.innerHTML = `<option value="${usuarioLogado.idUsuario}" selected>${usuarioLogado.nome} (Você - Cidadão)</option>`;
            selUser.disabled = true;
        } else {
            selUser.disabled = false;
            selUser.innerHTML = '<option value="">Selecione o cidadão solicitante...</option>';
            usuarios.forEach(u => {
                const isSelf = usuarioLogado && u.idUsuario === usuarioLogado.idUsuario ? " (Você)" : "";
                selUser.innerHTML += `<option value="${u.idUsuario}">${u.nome}${isSelf} (${u.email})</option>`;
            });
            if (usuarioLogado) selUser.value = usuarioLogado.idUsuario;
        }
    }

    // Select de Endereços
    const selEnd = document.getElementById("chamadoEndereco");
    if (selEnd) {
        selEnd.innerHTML = '<option value="">Selecione o endereço da ocorrência...</option>';
        enderecos.forEach(e => {
            selEnd.innerHTML += `<option value="${e.idEndereco}">${e.logradouro}, ${e.numero || 'S/N'} - ${e.bairro}, ${e.cidade}</option>`;
        });
    }

    // Select de Tipos de Problema
    const selTp = document.getElementById("chamadoTipoProblema");
    if (selTp) {
        selTp.innerHTML = '<option value="">Selecione o tipo de problema...</option>';
        tiposProblema.forEach(t => {
            selTp.innerHTML += `<option value="${t.idTipoProblema}">${t.descricao}</option>`;
        });
    }

    // Select de Status Inicial (Cidadão só pode abrir como 'Aberto')
    const selSt = document.getElementById("chamadoStatus");
    if (selSt) {
        selSt.innerHTML = '';
        if (usuarioLogado && usuarioLogado.tipoUsuario === "COMUM") {
            const statusAberto = statusList.find(s => s.descricao.toLowerCase().includes("aberto")) || { idStatus: 1, descricao: "Aberto" };
            selSt.innerHTML = `<option value="${statusAberto.idStatus}">${statusAberto.descricao}</option>`;
            selSt.disabled = true;
        } else {
            selSt.disabled = false;
            statusList.forEach(s => {
                selSt.innerHTML += `<option value="${s.idStatus}">${s.descricao}</option>`;
            });
        }
    }

    // Filtro por Status
    const filtroSt = document.getElementById("filtroStatus");
    if (filtroSt) {
        filtroSt.innerHTML = '<option value="TODOS">Todos os Status</option>';
        statusList.forEach(s => {
            filtroSt.innerHTML += `<option value="${s.idStatus}">${s.descricao}</option>`;
        });
    }

    // Select no Modal Técnico de Despacho
    const modalNovoSt = document.getElementById("modalNovoStatus");
    if (modalNovoSt) {
        modalNovoSt.innerHTML = '';
        statusList.forEach(s => {
            modalNovoSt.innerHTML += `<option value="${s.idStatus}">${s.descricao}</option>`;
        });
    }
}

// ── Renderização da Tabela de Chamados com Fotos de Evidência ─
function renderizarChamados(lista) {
    const tbody = document.getElementById("tabelaChamadosCorpo");
    if (!tbody) return;
    tbody.innerHTML = "";

    if (!lista || lista.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 2.5rem; color: #94a3b8; font-weight: 600;">Nenhum chamado encontrado para os filtros selecionados.</td></tr>`;
        return;
    }

    const podeExcluir = usuarioLogado && usuarioLogado.tipoUsuario === "SABESP";

    lista.forEach(c => {
        const badgeClass = getBadgeClass(c.status?.descricao);
        const dataFormatada = c.dataChamado ? c.dataChamado.split("-").reverse().join("/") : "-";
        const horaFormatada = c.horaChamado ? c.horaChamado.substring(0, 5) : "-";

        // Célula de Foto de Evidência
        let fotoHtml = `<span class="no-photo-badge">Sem foto</span>`;
        if (c.imagemUrl) {
            fotoHtml = `
                <div class="table-photo-cell">
                    <img src="${c.imagemUrl}" alt="Foto da Ocorrência" class="table-photo-thumb" 
                         onclick="abrirFotoDireta('${c.imagemUrl}')" title="Clique para ampliar a foto">
                    <span style="font-size: 0.72rem; color: var(--sabesp-primary); font-weight: 700; cursor: pointer;" 
                          onclick="abrirModalDetalhes(${c.idChamado})">Ver</span>
                </div>
            `;
        }

        const btnExcluir = podeExcluir
            ? `<button class="btn btn-danger btn-sm" onclick="excluirChamado(${c.idChamado})" title="Excluir Ocorrência">🗑️</button>`
            : '';

        tbody.innerHTML += `
            <tr>
                <td><strong style="font-family: var(--font-mono); color: var(--sabesp-navy);">#${c.idChamado}</strong></td>
                <td>${fotoHtml}</td>
                <td><strong>${c.tipoProblema?.descricao || 'Geral'}</strong></td>
                <td>
                    <div style="font-weight: 600; color: var(--text-primary);">${c.endereco?.bairro || 'Santos'}</div>
                    <div style="font-size: 0.78rem; color: var(--text-muted);">${c.endereco ? `${c.endereco.logradouro}, ${c.endereco.numero || 'S/N'}` : '-'}</div>
                </td>
                <td>${c.usuario?.nome || '-'}</td>
                <td><span class="status-pill ${badgeClass}">● ${c.status?.descricao || 'N/A'}</span></td>
                <td>
                    <div style="display: flex; gap: 0.35rem; align-items: center;">
                        <button class="btn btn-secondary btn-sm" onclick="abrirModalDetalhes(${c.idChamado})">
                            🔍 Inspecionar
                        </button>
                        ${btnExcluir}
                    </div>
                </td>
            </tr>
        `;
    });
}

function getBadgeClass(descricao) {
    if (!descricao) return "fechado";
    const desc = descricao.toLowerCase();
    if (desc.includes("aberto")) return "aberto";
    if (desc.includes("andamento")) return "andamento";
    if (desc.includes("resolvido")) return "resolvido";
    return "fechado";
}

function atualizarMetricas(lista) {
    const elTotal = document.getElementById("statTotal");
    const elAbertos = document.getElementById("statAbertos");
    const elAndamento = document.getElementById("statAndamento");
    const elResolvidos = document.getElementById("statResolvidos");

    if (elTotal) elTotal.innerText = lista.length;
    if (elAbertos) elAbertos.innerText = lista.filter(c => c.status?.descricao?.toLowerCase().includes("aberto")).length;
    if (elAndamento) elAndamento.innerText = lista.filter(c => c.status?.descricao?.toLowerCase().includes("andamento")).length;
    if (elResolvidos) elResolvidos.innerText = lista.filter(c => c.status?.descricao?.toLowerCase().includes("resolvido")).length;
}

// ── Filtros de Chamados ──────────────────────────────────────
function aplicarFiltros() {
    const statusVal = document.getElementById("filtroStatus")?.value || "TODOS";
    const buscaVal = document.getElementById("filtroBusca")?.value.toLowerCase().trim() || "";
    const filtroOrigem = document.getElementById("filtroOrigem");
    const origemVal = filtroOrigem ? filtroOrigem.value : "TODOS";

    let filtrados = [...chamados];

    // Filtro por origem: "MEUS" filtra apenas chamados do usuário logado
    if (origemVal === "MEUS" && usuarioLogado) {
        filtrados = filtrados.filter(c => c.usuario?.idUsuario === usuarioLogado.idUsuario);
    }

    // Filtro por status
    if (statusVal !== "TODOS") {
        filtrados = filtrados.filter(c => c.status?.idStatus === parseInt(statusVal));
    }

    // Busca textual
    if (buscaVal) {
        filtrados = filtrados.filter(c =>
            (c.descricao && c.descricao.toLowerCase().includes(buscaVal)) ||
            (c.endereco?.bairro && c.endereco.bairro.toLowerCase().includes(buscaVal)) ||
            (c.endereco?.logradouro && c.endereco.logradouro.toLowerCase().includes(buscaVal)) ||
            (c.usuario?.nome && c.usuario.nome.toLowerCase().includes(buscaVal)) ||
            (c.tipoProblema?.descricao && c.tipoProblema.descricao.toLowerCase().includes(buscaVal)) ||
            (`#${c.idChamado}`.includes(buscaVal))
        );
    }

    renderizarChamados(filtrados);
}

// ── Modal de Detalhes, Análise & Fotos ───────────────────────
async function abrirModalDetalhes(id) {
    chamadoAtualId = id;
    const chamado = chamados.find(c => c.idChamado === id);
    if (!chamado) return;

    document.getElementById("modalChamadoId").innerText = `#${chamado.idChamado}`;
    document.getElementById("detalheTipo").innerText = chamado.tipoProblema?.descricao || '-';
    document.getElementById("detalheStatus").innerText = chamado.status?.descricao || '-';
    document.getElementById("detalheDescricao").innerText = chamado.descricao || 'Sem descrição informada.';
    document.getElementById("detalheEndereco").innerText = chamado.endereco ?
        `${chamado.endereco.logradouro}, ${chamado.endereco.numero || 'S/N'} - ${chamado.endereco.bairro}, ${chamado.endereco.cidade} (${chamado.endereco.cep || ''})` : '-';
    document.getElementById("detalheSolicitante").innerText = chamado.usuario ?
        `${chamado.usuario.nome} (${chamado.usuario.email} | ${chamado.usuario.telefone || 'Sem fone'})` : '-';

    // Foto de Evidência no Modal
    const imgContainer = document.getElementById("detalheImagemContainer");
    const imgElem = document.getElementById("detalheImagem");
    const semFotoAviso = document.getElementById("semFotoAviso");

    if (chamado.imagemUrl) {
        imgElem.src = chamado.imagemUrl;
        imgContainer.style.display = "flex";
        if (semFotoAviso) semFotoAviso.style.display = "none";
    } else {
        imgContainer.style.display = "none";
        imgElem.src = "";
        if (semFotoAviso) semFotoAviso.style.display = "block";
    }

    // Painel Técnico vs Cidadão (RBAC)
    const painelTecnico = document.getElementById("painelTecnicoStatus");
    const painelCidadao = document.getElementById("painelCidadaoInfo");

    if (usuarioLogado && usuarioLogado.tipoUsuario === "SABESP") {
        if (painelTecnico) {
            painelTecnico.style.display = "block";
            const modalNovoSt = document.getElementById("modalNovoStatus");
            if (modalNovoSt && chamado.status?.idStatus) {
                modalNovoSt.value = chamado.status.idStatus;
            }
        }
        if (painelCidadao) painelCidadao.style.display = "none";
    } else {
        if (painelTecnico) painelTecnico.style.display = "none";
        if (painelCidadao) painelCidadao.style.display = "block";
    }

    await carregarComentarios(id);
    document.getElementById("modalDetalhes").classList.add("active");
}

function fecharModal() {
    document.getElementById("modalDetalhes").classList.remove("active");
}

// ── Lightbox de Fotos em Tela Cheia ──────────────────────────
function expandirFotoLightbox() {
    const imgElem = document.getElementById("detalheImagem");
    if (imgElem && imgElem.src && imgElem.src !== window.location.href) {
        abrirFotoDireta(imgElem.src);
    }
}

function abrirFotoDireta(src) {
    const lightbox = document.getElementById("modalFotoLightbox");
    const lbImg = document.getElementById("lightboxImg");
    if (lightbox && lbImg) {
        lbImg.src = src;
        lightbox.classList.add("active");
    }
}

function fecharFotoLightbox() {
    const lightbox = document.getElementById("modalFotoLightbox");
    if (lightbox) lightbox.classList.remove("active");
}

// ── Alteração Técnica de Status (Exclusivo SABESP) ───────────
async function salvarStatusChamadoModal() {
    if (!chamadoAtualId) return;

    if (!usuarioLogado || usuarioLogado.tipoUsuario !== "SABESP") {
        showToast("Acesso negado: apenas técnicos Sabesp podem alterar o status.", "error");
        return;
    }

    const novoStatusId = document.getElementById("modalNovoStatus").value;

    try {
        const res = await fetch(`/api/chamados/${chamadoAtualId}/status/${novoStatusId}`, {
            method: "PATCH",
            headers: { "X-User-Role": usuarioLogado.tipoUsuario }
        });

        if (!res.ok) throw new Error("Erro ao atualizar status");

        const atualizado = await res.json();
        showToast(`Status atualizado para: ${atualizado.status?.descricao || 'Novo Status'}`, "success");

        const index = chamados.findIndex(c => c.idChamado === chamadoAtualId);
        if (index !== -1) {
            chamados[index] = atualizado;
        }

        document.getElementById("detalheStatus").innerText = atualizado.status?.descricao || '-';
        aplicarFiltros();
        atualizarMetricas(chamados);
    } catch (err) {
        showToast("Falha ao salvar status.", "error");
    }
}

// ── Exclusão de Chamado (Exclusivo SABESP) ───────────────────
async function excluirChamado(id) {
    if (!usuarioLogado || usuarioLogado.tipoUsuario !== "SABESP") {
        showToast("Acesso negado: apenas técnicos Sabesp podem excluir chamados.", "error");
        return;
    }
    if (!confirm(`Deseja excluir permanentemente o chamado #${id}?`)) return;

    try {
        const res = await fetch(`/api/chamados/${id}`, {
            method: "DELETE",
            headers: { "X-User-Role": usuarioLogado.tipoUsuario }
        });

        if (!res.ok) throw new Error("Erro ao excluir");

        chamados = chamados.filter(c => c.idChamado !== id);
        showToast(`Chamado #${id} excluído com sucesso.`, "success");
        aplicarFiltros();
        atualizarMetricas(chamados);
    } catch (err) {
        showToast("Falha ao excluir chamado.", "error");
    }
}

// ── Histórico de Comentários / Linha do Tempo ────────────────
async function carregarComentarios(idChamado) {
    const container = document.getElementById("listaComentarios");
    if (!container) return;
    container.innerHTML = "<p style='color:#94a3b8; font-size:0.85rem;'>Carregando comentários...</p>";

    try {
        const res = await fetch(`/api/comentarios/chamado/${idChamado}`);
        const comentarios = await res.json();

        container.innerHTML = "";
        if (comentarios.length === 0) {
            container.innerHTML = "<p style='color:#94a3b8; font-size:0.85rem;'>Nenhum acompanhamento registrado ainda.</p>";
            return;
        }

        comentarios.forEach(c => {
            const dataHora = c.dataComentario ? new Date(c.dataComentario).toLocaleString("pt-BR") : "-";
            const tipo = c.usuario?.tipoUsuario || "COMUM";
            let classeBolha = "comment-bubble";
            let badgeTipo = `<span style="font-size:0.7rem; color:#0284c7;">Munícipe</span>`;

            if (tipo === "SABESP") {
                classeBolha += " sabesp-comment";
                badgeTipo = `<span style="font-size:0.7rem; color:#15803d; font-weight:700;">🛠️ Sabesp Operacional</span>`;
            } else if (tipo === "PREFEITURA") {
                classeBolha += " pref-comment";
                badgeTipo = `<span style="font-size:0.7rem; color:#7e22ce; font-weight:700;">🏛️ Fiscalização Prefeitura</span>`;
            }

            container.innerHTML += `
                <div class="${classeBolha}">
                    <div class="comment-meta">
                        <div><strong>${c.usuario?.nome || 'Atendente'}</strong> &bull; ${badgeTipo}</div>
                        <span>${dataHora}</span>
                    </div>
                    <div style="color: var(--text-primary);">${c.texto}</div>
                </div>
            `;
        });
    } catch (e) {
        container.innerHTML = "<p style='color:red;'>Erro ao carregar comentários.</p>";
    }
}

async function enviarComentario() {
    const texto = document.getElementById("novoComentarioTexto").value;
    if (!texto.trim()) {
        showToast("Digite uma mensagem antes de enviar.", "error");
        return;
    }

    if (!usuarioLogado) {
        showToast("Usuário não autenticado.", "error");
        return;
    }

    const idUsuario = usuarioLogado.idUsuario;

    try {
        const res = await fetch("/api/comentarios", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-User-Role": usuarioLogado.tipoUsuario
            },
            body: JSON.stringify({
                texto: texto,
                idChamado: chamadoAtualId,
                idUsuario: idUsuario
            })
        });

        if (!res.ok) throw new Error("Erro ao registrar comentário");

        document.getElementById("novoComentarioTexto").value = "";
        showToast("Comentário adicionado à linha do tempo!", "success");
        await carregarComentarios(chamadoAtualId);
    } catch (err) {
        showToast("Falha ao registrar comentário.", "error");
    }
}

// ── Atualização da Interface por Perfil (RBAC Front-End) ────
function atualizarInterfaceUsuario() {
    const infoLogged = document.getElementById("userInfoLogged");
    const infoGuest = document.getElementById("userInfoGuest");
    const navNome = document.getElementById("navUserNome");
    const navBadge = document.getElementById("navUserBadge");
    const navBtnCad = document.getElementById("navBtnCadastros");
    const catAdmin = document.getElementById("categoriaAdmin");
    const filtroOrigem = document.getElementById("filtroOrigem");
    const avatarChar = document.getElementById("userAvatarChar");
    const banner = document.getElementById("bannerContextual");
    const bannerTitulo = document.getElementById("bannerTitulo");
    const bannerDesc = document.getElementById("bannerDescricao");
    const roleCallout = document.getElementById("roleDescriptionCallout");
    const subtituloChamados = document.getElementById("subtituloChamados");

    if (usuarioLogado) {
        if (infoLogged) infoLogged.style.display = "block";
        if (infoGuest) infoGuest.style.display = "none";
        if (navNome) navNome.innerText = usuarioLogado.nome;
        if (avatarChar) avatarChar.innerText = usuarioLogado.nome.charAt(0).toUpperCase();

        if (banner) banner.className = "role-hero-banner";

        if (usuarioLogado.tipoUsuario === "SABESP") {
            if (navBadge) { navBadge.innerText = "🛠️ Técnico Sabesp"; navBadge.className = "status-pill resolvido"; }
            if (navBtnCad) navBtnCad.style.display = "inline-flex";
            if (catAdmin) catAdmin.style.display = "block";
            if (banner) banner.classList.add("banner-sabesp");
            if (bannerTitulo) bannerTitulo.innerText = "Central Operacional Sabesp — Engenharia & Despacho";
            if (bannerDesc) bannerDesc.innerText = "Acesso total à rede: altere status de ocorrências, gerencie equipes de campo e atualize a base cadastral.";
            if (roleCallout) roleCallout.innerHTML = `Nível: <strong style="color:var(--role-sabesp)">Técnico Sabesp (Controle Total & Despacho)</strong>`;
            if (filtroOrigem) filtroOrigem.value = "TODOS";
            if (subtituloChamados) subtituloChamados.innerText = "Fila Geral de Chamados da Cidade de Santos";

        } else if (usuarioLogado.tipoUsuario === "PREFEITURA") {
            if (navBadge) { navBadge.innerText = "🏛️ Fiscal Prefeitura"; navBadge.className = "status-pill fechado"; }
            if (navBtnCad) navBtnCad.style.display = "none";
            if (catAdmin) catAdmin.style.display = "none";
            if (banner) banner.classList.add("banner-prefeitura");
            if (bannerTitulo) bannerTitulo.innerText = "Painel de Fiscalização Municipal — Auditoria de Serviços";
            if (bannerDesc) bannerDesc.innerText = "Auditoria de chamados de esgoto: consulte fotos de evidência, vistorie ocorrências e emita notificações.";
            if (roleCallout) roleCallout.innerHTML = `Nível: <strong style="color:var(--role-pref)">Fiscalização Prefeitura (Auditoria & Pareceres)</strong>`;
            if (filtroOrigem) filtroOrigem.value = "TODOS";
            if (subtituloChamados) subtituloChamados.innerText = "Auditoria de Todas as Ocorrências Notificadas";

        } else {
            if (navBadge) { navBadge.innerText = "👤 Cidadão"; navBadge.className = "status-pill aberto"; }
            if (navBtnCad) navBtnCad.style.display = "none";
            if (catAdmin) catAdmin.style.display = "none";
            if (banner) banner.classList.add("banner-comum");
            if (bannerTitulo) bannerTitulo.innerText = "Portal do Cidadão — Atendimento de Esgoto e Saneamento";
            if (bannerDesc) bannerDesc.innerText = "Abra solicitações de desobstrução com fotos, acompanhe o status da sua rua e interaja com os técnicos.";
            if (roleCallout) roleCallout.innerHTML = `Nível: <strong style="color:var(--role-comum)">Cidadão Morador (Abertura e Acompanhamento)</strong>`;
            if (filtroOrigem) filtroOrigem.value = "MEUS";
            if (subtituloChamados) subtituloChamados.innerText = "Seus Chamados e Ocorrências Registradas";
        }
    } else {
        if (infoLogged) infoLogged.style.display = "none";
        if (infoGuest) infoGuest.style.display = "block";
        if (navBtnCad) navBtnCad.style.display = "none";
    }

    atualizarSelects();
    aplicarFiltros();
}

// ── Login e Troca Rápida de Perfil ───────────────────────────
function abrirModalLogin() {
    document.getElementById("modalLogin").classList.add("active");
}

function fecharModalLogin() {
    document.getElementById("modalLogin").classList.remove("active");
}

function alternarAbaAuth(aba) {
    const abaLogin = document.getElementById("abaAuthLogin");
    const abaReg = document.getElementById("abaAuthRegistro");
    const tabLogin = document.getElementById("tabBtnLogin");
    const tabReg = document.getElementById("tabBtnRegistro");

    if (aba === "login") {
        abaLogin.style.display = "block";
        abaReg.style.display = "none";
        tabLogin.style.background = "#e0f2fe";
        tabLogin.style.color = "var(--sabesp-primary)";
        tabReg.style.background = "transparent";
        tabReg.style.color = "var(--text-muted)";
    } else {
        abaLogin.style.display = "none";
        abaReg.style.display = "block";
        tabReg.style.background = "#e0f2fe";
        tabReg.style.color = "var(--sabesp-primary)";
        tabLogin.style.background = "transparent";
        tabLogin.style.color = "var(--text-muted)";
    }
}

async function fazerLogin(email, senha) {
    try {
        const res = await fetch("/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, senha })
        });

        if (!res.ok) {
            const errData = await res.json().catch(() => ({}));
            throw new Error(errData.mensagem || "E-mail ou senha incorretos.");
        }

        const data = await res.json();
        usuarioLogado = data;
        localStorage.setItem("sabesp_user", JSON.stringify(data));
        fecharModalLogin();
        atualizarInterfaceUsuario();
        showToast(`Bem-vindo(a), ${data.nome}!`, "success");
    } catch (err) {
        showToast(err.message || "Erro ao realizar login.", "error");
    }
}

async function loginRapido(email, senha) {
    await fazerLogin(email, senha);
}

function fazerLogout() {
    localStorage.removeItem("sabesp_user");
    usuarioLogado = null;
    atualizarInterfaceUsuario();
    showToast("Sessão finalizada.", "success");
    abrirModalLogin();
}

// ── Utilitários para Processamento de Imagens ──────────────
function limparImagemForm() {
    imagemChamadoBase64 = null;
    const inputImg = document.getElementById("chamadoImagemArquivo");
    if (inputImg) inputImg.value = "";
    const prevCont = document.getElementById("previewImagemContainer");
    if (prevCont) prevCont.style.display = "none";
    const prevImg = document.getElementById("previewImagem");
    if (prevImg) prevImg.src = "";
    const btnRem = document.getElementById("btnRemoverImagem");
    if (btnRem) btnRem.style.display = "none";
}

function comprimirEConverterImagem(file, maxWidth = 1200, maxHeight = 1200, qualidade = 0.85) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = (e) => {
            const img = new Image();
            img.onload = () => {
                let width = img.width;
                let height = img.height;

                if (width > maxWidth || height > maxHeight) {
                    if (width > height) {
                        height = Math.round((height * maxWidth) / width);
                        width = maxWidth;
                    } else {
                        width = Math.round((width * maxHeight) / height);
                        height = maxHeight;
                    }
                }

                const canvas = document.createElement("canvas");
                canvas.width = width;
                canvas.height = height;
                const ctx = canvas.getContext("2d");
                ctx.drawImage(img, 0, 0, width, height);

                const dataUrl = canvas.toDataURL("image/jpeg", qualidade);
                resolve(dataUrl);
            };
            img.onerror = reject;
            img.src = e.target.result;
        };
        reader.onerror = reject;
        reader.readAsDataURL(file);
    });
}

// ── Configuração de Todos os Eventos do Sistema ──────────────
function configurarEventos() {
    // Navegação entre Abas
    document.querySelectorAll(".nav-btn[data-view]").forEach(btn => {
        btn.addEventListener("click", () => {
            const target = btn.getAttribute("data-view");

            // RBAC: apenas cidadão comum pode acessar a aba de novo chamado
            if (target === "view-novo") {
                if (!usuarioLogado || usuarioLogado.tipoUsuario !== "COMUM") {
                    showToast("Acesso negado: apenas cidadãos podem registrar novos chamados.", "error");
                    return;
                }
            }

            document.querySelectorAll(".nav-btn").forEach(b => b.classList.remove("active"));
            document.querySelectorAll(".view-panel").forEach(p => p.classList.remove("active"));

            btn.classList.add("active");
            const panel = document.getElementById(target);
            if (panel) panel.classList.add("active");
        });
    });

    // Upload e Pré-visualização de Imagem
    const inputImg = document.getElementById("chamadoImagemArquivo");
    if (inputImg) {
        inputImg.addEventListener("change", async (e) => {
            const file = e.target.files[0];
            if (file) {
                if (!file.type.startsWith("image/")) {
                    showToast("Por favor, selecione um arquivo de imagem válido.", "error");
                    limparImagemForm();
                    return;
                }
                try {
                    imagemChamadoBase64 = await comprimirEConverterImagem(file);
                    const prevImg = document.getElementById("previewImagem");
                    const prevCont = document.getElementById("previewImagemContainer");
                    const btnRem = document.getElementById("btnRemoverImagem");

                    if (prevImg) prevImg.src = imagemChamadoBase64;
                    if (prevCont) prevCont.style.display = "block";
                    if (btnRem) btnRem.style.display = "inline-block";
                    showToast("Foto anexada com sucesso!", "success");
                } catch (err) {
                    console.error("Erro ao processar imagem:", err);
                    showToast("Não foi possível carregar a imagem.", "error");
                }
            }
        });
    }

    const btnRemoverImg = document.getElementById("btnRemoverImagem");
    if (btnRemoverImg) {
        btnRemoverImg.addEventListener("click", () => {
            limparImagemForm();
            showToast("Foto removida.", "success");
        });
    }

    // Formulário de Novo Chamado
    const formNovo = document.getElementById("formNovoChamado");
    if (formNovo) {
        formNovo.addEventListener("reset", () => {
            limparImagemForm();
        });

        formNovo.addEventListener("submit", async (e) => {
            e.preventDefault();

            const now = new Date();
            const dataAtual = now.toISOString().split("T")[0];
            const horaAtual = now.toTimeString().split(" ")[0];

            // Solicitante: se for cidadão comum, trava no próprio ID
            const idUsuarioFinal = (usuarioLogado && usuarioLogado.tipoUsuario === "COMUM")
                ? usuarioLogado.idUsuario
                : (parseInt(document.getElementById("chamadoUsuario").value) || (usuarioLogado ? usuarioLogado.idUsuario : 1));

            // Status: cidadão comum abre sempre como 1 (Aberto)
            const idStatusFinal = (usuarioLogado && usuarioLogado.tipoUsuario === "COMUM")
                ? 1
                : (parseInt(document.getElementById("chamadoStatus").value) || 1);

            const payload = {
                dataChamado: dataAtual,
                horaChamado: horaAtual,
                descricao: document.getElementById("chamadoDescricao").value,
                idUsuario: idUsuarioFinal,
                idEndereco: parseInt(document.getElementById("chamadoEndereco").value),
                idTipoProblema: parseInt(document.getElementById("chamadoTipoProblema").value),
                idStatus: idStatusFinal,
                imagemUrl: imagemChamadoBase64
            };

            try {
                const res = await fetch("/api/chamados", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        "X-User-Role": usuarioLogado ? usuarioLogado.tipoUsuario : "",
                        "X-User-Id": usuarioLogado ? usuarioLogado.idUsuario : ""
                    },
                    body: JSON.stringify(payload)
                });

                if (!res.ok) throw new Error("Erro ao criar chamado");

                showToast("Ocorrência registrada com sucesso!", "success");
                formNovo.reset();
                limparImagemForm();
                await carregarChamados();

                // Direciona para a aba de chamados
                document.querySelector('.nav-btn[data-view="view-chamados"]').click();
            } catch (err) {
                showToast("Erro ao cadastrar chamado.", "error");
            }
        });
    }

    // Filtros de Busca
    const filtroOrigem = document.getElementById("filtroOrigem");
    if (filtroOrigem) filtroOrigem.addEventListener("change", aplicarFiltros);

    const filtroStatus = document.getElementById("filtroStatus");
    if (filtroStatus) filtroStatus.addEventListener("change", aplicarFiltros);

    const filtroBusca = document.getElementById("filtroBusca");
    if (filtroBusca) filtroBusca.addEventListener("input", aplicarFiltros);

    // Form de Login
    const formLogin = document.getElementById("formLogin");
    if (formLogin) {
        formLogin.addEventListener("submit", async (e) => {
            e.preventDefault();
            const email = document.getElementById("loginEmail").value;
            const senha = document.getElementById("loginSenha").value;
            await fazerLogin(email, senha);
        });
    }

    // Form de Registro Público (Cidadão)
    const formRegPub = document.getElementById("formRegistroPublico");
    if (formRegPub) {
        formRegPub.addEventListener("submit", async (e) => {
            e.preventDefault();
            const payload = {
                nome: document.getElementById("regNome").value,
                email: document.getElementById("regEmail").value,
                senha: document.getElementById("regSenha").value,
                telefone: document.getElementById("regTelefone").value,
                tipoUsuario: "COMUM"
            };
            try {
                const res = await fetch("/api/auth/registro", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload)
                });
                if (!res.ok) {
                    const errData = await res.json().catch(() => ({}));
                    throw new Error(errData.mensagem || "Erro ao criar conta");
                }
                const novoUser = await res.json();
                showToast("Conta criada com sucesso!", "success");
                formRegPub.reset();
                usuarioLogado = novoUser;
                localStorage.setItem("sabesp_user", JSON.stringify(novoUser));
                fecharModalLogin();
                await carregarUsuarios();
                atualizarInterfaceUsuario();
            } catch (err) {
                showToast(err.message || "Falha ao criar conta.", "error");
            }
        });
    }

    // Cadastros Rápidos (APENAS Prefeitura pode criar contas)
    const formRapidoUser = document.getElementById("formRapidoUsuario");
    if (formRapidoUser) {
        formRapidoUser.addEventListener("submit", async (e) => {
            e.preventDefault();

            if (!usuarioLogado || usuarioLogado.tipoUsuario !== "PREFEITURA") {
                showToast("Acesso negado: apenas a Prefeitura pode cadastrar novos usuários.", "error");
                return;
            }

            const payload = {
                nome: document.getElementById("userNome").value,
                email: document.getElementById("userEmail").value,
                senha: "123",
                telefone: document.getElementById("userTelefone").value,
                tipoUsuario: document.getElementById("userTipo").value
            };
            try {
                const res = await fetch("/api/usuarios", {
                    method: "POST",
                    headers: { "Content-Type": "application/json", "X-User-Role": usuarioLogado.tipoUsuario, "X-User-Id": usuarioLogado.idUsuario },
                    body: JSON.stringify(payload)
                });
                if (!res.ok) throw new Error("Erro ao cadastrar");
                showToast("Usuário cadastrado com sucesso!", "success");
                formRapidoUser.reset();
                await carregarUsuarios();
                atualizarSelects();
            } catch (err) {
                showToast("Erro ao salvar usuário.", "error");
            }
        });
    }

    const formRapidoEnd = document.getElementById("formRapidoEndereco");
    if (formRapidoEnd) {
        formRapidoEnd.addEventListener("submit", async (e) => {
            e.preventDefault();
            const payload = {
                logradouro: document.getElementById("endLogradouro").value,
                numero: document.getElementById("endNumero").value,
                complemento: document.getElementById("endComplemento").value,
                bairro: document.getElementById("endBairro").value,
                cidade: document.getElementById("endCidade").value,
                estado: document.getElementById("endEstado").value,
                cep: document.getElementById("endCep").value
            };
            try {
                const res = await fetch("/api/enderecos", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload)
                });
                if (!res.ok) throw new Error("Erro ao cadastrar");
                showToast("Endereço adicionado com sucesso!", "success");
                formRapidoEnd.reset();
                await carregarEnderecos();
                atualizarSelects();
            } catch (err) {
                showToast("Erro ao salvar endereço.", "error");
            }
        });
    }
}

// ── Toasts de Notificação ────────────────────────────────────
function showToast(mensagem, tipo = "success") {
    const container = document.getElementById("toastContainer");
    if (!container) return;
    const toast = document.createElement("div");
    toast.className = `toast toast-${tipo}`;
    toast.innerHTML = tipo === "success" ? `✓ ${mensagem}` : `✕ ${mensagem}`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.remove();
    }, 4000);
}