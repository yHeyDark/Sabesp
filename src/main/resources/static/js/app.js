// ============================================================
// SABESP GEOSANEAMENTO — LÓGICA DA APLICAÇÃO & RBAC
// ============================================================
//
// Arquivo principal do SPA (Single Page Application) do sistema
// Sabesp GeoSaneamento. Responsável por toda a lógica de front-end:
//   • Comunicação assíncrona com a API REST (fetch)
//   • Controle de acesso baseado em perfis (RBAC) no lado do cliente
//     — SABESP:      técnico operacional (acesso total, despacho, exclusão)
//     — PREFEITURA:  fiscal de auditoria (visualiza tudo, cadastra usuários)
//     — COMUM:       cidadão morador (abre chamados, acompanha os seus)
//   • Renderização dinâmica de chamados, métricas/KPIs e filtros
//   • Gerenciamento de sessão via localStorage
//   • Upload de fotos com compressão client-side via Canvas
//   • Sistema de notificações (toasts) para feedback ao usuário
//   • Modal de detalhes com painel técnico (SABESP) e linha do tempo
//   • Lightbox para exibição de fotos em tela cheia
//   • Fluxo de autenticação: login, registro de cidadão e recuperação de senha
// ============================================================

// ── Estado Global da Aplicação ───────────────────────────────
// Variáveis mantidas em memória durante toda a sessão do SPA.
// São preenchidas ao carregar dados da API e atualizadas
// conforme o usuário interage com o sistema.

/** Lista completa de chamados (ocorrências) retornados pela API */
let chamados = [];

/** Lista de todos os usuários cadastrados no sistema */
let usuarios = [];

/** Lista de endereços disponíveis para vincular a chamados */
let enderecos = [];

/** Lista de tipos de problema (ex: "Vazamento", "Obstrução") */
let tiposProblema = [];

/** Lista de status possíveis de um chamado (ex: "Aberto", "Em andamento", "Resolvido") */
let statusList = [];

/**
 * Armazena a imagem do chamado em formato Base64 (data URL) após
 * compressão via Canvas. Enviada no payload ao criar novo chamado.
 * null quando nenhuma imagem foi selecionada.
 */
let imagemChamadoBase64 = null;

/**
 * Dados do usuário atualmente logado (objeto com idUsuario, nome, email,
 * tipoUsuario, etc.). null se ninguém estiver logado.
 * Persistido em localStorage como JSON sob a chave "sabesp_user".
 */
let usuarioLogado = null;

/**
 * ID do chamado atualmente aberto no modal de detalhes.
 * Usado para operações de atualização de status e envio de comentários.
 */
let chamadoAtualId = null;

// ── Inicialização ao Carregar o DOM ──────────────────────────
// Ponto de entrada da aplicação. Quando o documento HTML termina de
// ser parseado pelo navegador (DOMContentLoaded), executa a sequência
// de inicialização: restaurar sessão → carregar dados → configurar
// eventos → atualizar a interface conforme o perfil do usuário.
document.addEventListener("DOMContentLoaded", async () => {
    // 1. Tenta restaurar o usuário logado a partir do localStorage
    carregarUsuarioSalvo();

    // 2. Carrega todos os dados iniciais da API (chamados, usuarios, endereços, etc.)
    await carregarDadosIniciais();

    // 3. Vincula todos os event listeners aos elementos do DOM
    configurarEventos();

    // 4. Aplica as regras de RBAC na interface (exibe/oculta elementos por perfil)
    atualizarInterfaceUsuario();
});

// ── Gestão de Sessão do Usuário ──────────────────────────────
/**
 * Restaura a sessão do usuário logado a partir do localStorage.
 * O sistema persiste os dados do usuário como JSON na chave "sabesp_user"
 * para que o login sobreviva a recarregamentos de página.
 *
 * Se o JSON estiver corrompido ou inexistente, define usuarioLogado = null,
 * forçando o usuário a fazer login novamente.
 */
function carregarUsuarioSalvo() {
    // Busca o objeto serializado no localStorage
    const salvo = localStorage.getItem("sabesp_user");
    if (salvo) {
        try {
            // Desserializa o JSON para o objeto de usuário
            usuarioLogado = JSON.parse(salvo);
        } catch (e) {
            // JSON inválido — descarta a sessão corrompida
            usuarioLogado = null;
        }
    }
    // Se não houver usuário salvo, inicia sem usuário logado (deve fazer login)
    if (!usuarioLogado) {
        usuarioLogado = null;
    }
}

// ── Carregamento de Dados da API ────────────────────────────
/**
 * Carrega em paralelo todos os dados iniciais necessários para o SPA:
 * usuários, endereços, tipos de problema, status de chamado e os próprios
 * chamados. Utiliza Promise.all para disparar todas as requisições
 * simultaneamente, reduzindo o tempo total de carregamento.
 *
 * Após o carregamento, popula os selects/dropdowns do formulário.
 * Exibe toast de erro se qualquer requisição falhar.
 */
async function carregarDadosIniciais() {
    try {
        // Dispara as 5 requisições GET em paralelo
        await Promise.all([
            carregarUsuarios(),
            carregarEnderecos(),
            carregarTiposProblema(),
            carregarStatus(),
            carregarChamados()
        ]);
        // Com os dados carregados, popula os dropdowns do formulário
        atualizarSelects();
    } catch (err) {
        console.error("Erro ao carregar dados do servidor:", err);
        showToast("Erro ao carregar dados do servidor.", "error");
    }
}

/**
 * Monta os cabeçalhos de autenticação/autorização para requisições à API.
 * O backend utiliza cabeçalhos customizados (X-User-Role e X-User-Id)
 * para aplicar regras de RBAC no lado do servidor.
 *
 * NOTA: Este é um mecanismo simplificado de autenticação. Em produção,
 * deveria usar tokens JWT ou sessões server-side.
 *
 * @returns {Object} Objeto com os cabeçalhos X-User-Role e X-User-Id
 *                   (vazio se nenhum usuário estiver logado)
 */
function getAuthHeaders() {
    const headers = {};
    if (usuarioLogado) {
        // Envia o tipo/perfil do usuário (SABESP, PREFEITURA ou COMUM)
        headers["X-User-Role"] = usuarioLogado.tipoUsuario;
        // Envia o ID numérico do usuário para o backend identificar o solicitante
        headers["X-User-Id"] = usuarioLogado.idUsuario;
    }
    return headers;
}

/**
 * Busca todos os chamados (ocorrências) do backend via GET /api/chamados.
 * Envia os cabeçalhos de autenticação para que o backend possa filtrar
 * chamados com base no perfil do usuário (ex: cidadão vê apenas os seus).
 *
 * Após receber os dados:
 *   1. Armazena na variável global `chamados`
 *   2. Aplica os filtros atuais (status, busca, origem) e re-renderiza a tabela
 *   3. Atualiza os cards de métricas/KPIs
 */
async function carregarChamados() {
    try {
        const res = await fetch("/api/chamados", {
            headers: getAuthHeaders()
        });
        if (!res.ok) throw new Error("Falha ao obter chamados");
        // Armazena a lista completa de chamados no estado global
        chamados = await res.json();
        // Re-aplica filtros e re-renderiza a tabela com os dados atualizados
        aplicarFiltros();
        // Recalcula e exibe os KPIs (total, abertos, em andamento, resolvidos)
        atualizarMetricas(chamados);
    } catch (e) {
        console.error("Erro ao carregar chamados:", e);
    }
}

/**
 * Busca todos os usuários cadastrados via GET /api/usuarios.
 * Popula a variável global `usuarios` para uso nos selects de solicitante.
 */
async function carregarUsuarios() {
    try {
        const res = await fetch("/api/usuarios");
        if (res.ok) usuarios = await res.json();
    } catch (e) { console.error(e); }
}

/**
 * Busca todos os endereços cadastrados via GET /api/enderecos.
 * Popula a variável global `enderecos` para uso no select de localização.
 */
async function carregarEnderecos() {
    try {
        const res = await fetch("/api/enderecos");
        if (res.ok) enderecos = await res.json();
    } catch (e) { console.error(e); }
}

/**
 * Busca todos os tipos de problema via GET /api/tipos-problema.
 * Popula a variável global `tiposProblema` para uso no select de categoria.
 */
async function carregarTiposProblema() {
    try {
        const res = await fetch("/api/tipos-problema");
        if (res.ok) tiposProblema = await res.json();
    } catch (e) { console.error(e); }
}

/**
 * Busca todos os status de chamado via GET /api/status-chamado.
 * Popula a variável global `statusList` para uso nos selects e filtros.
 */
async function carregarStatus() {
    try {
        const res = await fetch("/api/status-chamado");
        if (res.ok) statusList = await res.json();
    } catch (e) { console.error(e); }
}

// ── Atualização dos Dropdowns e Seletores (com RBAC) ─────────
/**
 * Popula todos os elementos <select> (dropdowns) do formulário de
 * novo chamado, dos filtros e do modal técnico, aplicando regras de
 * RBAC conforme o perfil do usuário logado.
 *
 * Regras de RBAC aplicadas:
 *   • COMUM (cidadão):
 *     — Select de solicitante: travado no próprio usuário (não pode abrir para outros)
 *     — Select de status: travado em "Aberto" (cidadão não define status)
 *   • SABESP / PREFEITURA:
 *     — Select de solicitante: pode selecionar qualquer cidadão
 *     — Select de status: pode escolher qualquer status
 */
function atualizarSelects() {
    // ── Select de Solicitantes (quem está abrindo o chamado) ──
    const selUser = document.getElementById("chamadoUsuario");
    if (selUser) {
        if (usuarioLogado && usuarioLogado.tipoUsuario === "COMUM") {
            // RBAC: Cidadão comum só pode abrir chamados em seu próprio nome
            // O select é travado (disabled) com apenas a opção do próprio usuário
            selUser.innerHTML = `<option value="${usuarioLogado.idUsuario}" selected>${usuarioLogado.nome} (Você - Cidadão)</option>`;
            selUser.disabled = true;
        } else {
            // SABESP/PREFEITURA: pode selecionar qualquer cidadão como solicitante
            selUser.disabled = false;
            selUser.innerHTML = '<option value="">Selecione o cidadão solicitante...</option>';
            // Itera por todos os usuários cadastrados para popular o dropdown
            usuarios.forEach(u => {
                // Marca "(Você)" ao lado do nome do próprio usuário logado para fácil identificação
                const isSelf = usuarioLogado && u.idUsuario === usuarioLogado.idUsuario ? " (Você)" : "";
                selUser.innerHTML += `<option value="${u.idUsuario}">${u.nome}${isSelf} (${u.email})</option>`;
            });
            // Pré-seleciona o próprio usuário logado por conveniência
            if (usuarioLogado) selUser.value = usuarioLogado.idUsuario;
        }
    }

    // ── Select de Endereços (local da ocorrência) ──
    // Sem regras de RBAC — todos os perfis veem todos os endereços cadastrados
    const selEnd = document.getElementById("chamadoEndereco");
    if (selEnd) {
        selEnd.innerHTML = '<option value="">Selecione o endereço da ocorrência...</option>';
        enderecos.forEach(e => {
            // Exibe logradouro, número (ou S/N se ausente), bairro e cidade
            selEnd.innerHTML += `<option value="${e.idEndereco}">${e.logradouro}, ${e.numero || 'S/N'} - ${e.bairro}, ${e.cidade}</option>`;
        });
    }

    // ── Select de Tipos de Problema (categoria da ocorrência) ──
    // Sem regras de RBAC — todos os perfis veem todos os tipos
    const selTp = document.getElementById("chamadoTipoProblema");
    if (selTp) {
        selTp.innerHTML = '<option value="">Selecione o tipo de problema...</option>';
        tiposProblema.forEach(t => {
            selTp.innerHTML += `<option value="${t.idTipoProblema}">${t.descricao}</option>`;
        });
    }

    // ── Select de Status Inicial do Chamado (Cidadão só pode abrir como 'Aberto') ──
    const selSt = document.getElementById("chamadoStatus");
    if (selSt) {
        selSt.innerHTML = '';
        if (usuarioLogado && usuarioLogado.tipoUsuario === "COMUM") {
            // RBAC: Cidadão comum não pode escolher o status inicial.
            // O chamado é aberto sempre como "Aberto" (ou o primeiro status encontrado
            // cuja descrição contenha "aberto"). O select é travado (disabled).
            const statusAberto = statusList.find(s => s.descricao.toLowerCase().includes("aberto")) || { idStatus: 1, descricao: "Aberto" };
            selSt.innerHTML = `<option value="${statusAberto.idStatus}">${statusAberto.descricao}</option>`;
            selSt.disabled = true;
        } else {
            // SABESP/PREFEITURA: pode definir qualquer status ao criar chamado
            selSt.disabled = false;
            statusList.forEach(s => {
                selSt.innerHTML += `<option value="${s.idStatus}">${s.descricao}</option>`;
            });
        }
    }

    // ── Filtro por Status (dropdown de filtro na tela de listagem) ──
    // Sem regras de RBAC — todos os perfis podem filtrar por qualquer status
    const filtroSt = document.getElementById("filtroStatus");
    if (filtroSt) {
        filtroSt.innerHTML = '<option value="TODOS">Todos os Status</option>';
        statusList.forEach(s => {
            filtroSt.innerHTML += `<option value="${s.idStatus}">${s.descricao}</option>`;
        });
    }

    // ── Select no Modal Técnico de Despacho (para alterar status) ──
    // Este select aparece apenas para perfil SABESP dentro do modal de detalhes.
    // A visibilidade do painel que contém este select é controlada em abrirModalDetalhes().
    const modalNovoSt = document.getElementById("modalNovoStatus");
    if (modalNovoSt) {
        modalNovoSt.innerHTML = '';
        statusList.forEach(s => {
            modalNovoSt.innerHTML += `<option value="${s.idStatus}">${s.descricao}</option>`;
        });
    }
}

// ── Renderização da Tabela de Chamados com Fotos de Evidência ─
/**
 * Renderiza a tabela HTML de chamados no DOM a partir de uma lista filtrada.
 * Cada linha exibe: ID, foto de evidência (miniatura clicável), tipo de
 * problema, endereço (bairro + logradouro), solicitante, status (com badge
 * colorido) e botões de ação.
 *
 * Regras de RBAC na tabela:
 *   • Botão "Excluir" (🗑️): visível APENAS para perfil SABESP
 *   • Botão "Inspecionar" (🔍): visível para todos os perfis
 *
 * @param {Array} lista - Lista de chamados já filtrada para renderizar
 */
function renderizarChamados(lista) {
    // Referência ao <tbody> da tabela de chamados
    const tbody = document.getElementById("tabelaChamadosCorpo");
    if (!tbody) return;
    // Limpa o conteúdo anterior da tabela
    tbody.innerHTML = "";

    // Exibe mensagem quando nenhum chamado atende aos filtros
    if (!lista || lista.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 2.5rem; color: #94a3b8; font-weight: 600;">Nenhum chamado encontrado para os filtros selecionados.</td></tr>`;
        return;
    }

    // RBAC: verifica se o usuário logado tem perfil SABESP para exibir botão de exclusão
    const podeExcluir = usuarioLogado && usuarioLogado.tipoUsuario === "SABESP";

    // Itera por cada chamado da lista filtrada para gerar as linhas da tabela
    lista.forEach(c => {
        // Determina a classe CSS do badge de status (cores diferenciadas por estado)
        const badgeClass = getBadgeClass(c.status?.descricao);
        // Formata a data de DD-MM-AAAA (ISO reverso) para DD/MM/AAAA (formato BR)
        const dataFormatada = c.dataChamado ? c.dataChamado.split("-").reverse().join("/") : "-";
        // Extrai apenas HH:MM da hora completa (HH:MM:SS)
        const horaFormatada = c.horaChamado ? c.horaChamado.substring(0, 5) : "-";

        // ── Célula de Foto de Evidência ──
        // Exibe miniatura clicável se houver imagemUrl, caso contrário badge "Sem foto"
        let fotoHtml = `<span class="no-photo-badge">Sem foto</span>`;
        if (c.imagemUrl) {
            // Miniatura com onclick para abrir no lightbox em tela cheia
            // e link "Ver" que abre o modal de detalhes completo
            fotoHtml = `
                <div class="table-photo-cell">
                    <img src="${c.imagemUrl}" alt="Foto da Ocorrência" class="table-photo-thumb" 
                         onclick="abrirFotoDireta('${c.imagemUrl}')" title="Clique para ampliar a foto">
                    <span style="font-size: 0.72rem; color: var(--sabesp-primary); font-weight: 700; cursor: pointer;" 
                          onclick="abrirModalDetalhes(${c.idChamado})">Ver</span>
                </div>
            `;
        }

        // RBAC: botão de exclusão renderizado apenas para técnicos SABESP
        const btnExcluir = podeExcluir
            ? `<button class="btn btn-danger btn-sm" onclick="excluirChamado(${c.idChamado})" title="Excluir Ocorrência">🗑️</button>`
            : '';

        // Monta a linha (<tr>) completa da tabela com todas as colunas
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

/**
 * Retorna a classe CSS correspondente ao status do chamado para colorir
 * o badge/pill na tabela.
 *
 * Mapeamento:
 *   • "aberto"    → classe "aberto"    (cor azul/primária)
 *   • "andamento" → classe "andamento" (cor amarela/laranja)
 *   • "resolvido" → classe "resolvido" (cor verde)
 *   • qualquer outro → classe "fechado" (cor cinza/neutra)
 *
 * @param {string} descricao - Descrição textual do status
 * @returns {string} Nome da classe CSS para o badge
 */
function getBadgeClass(descricao) {
    if (!descricao) return "fechado";
    const desc = descricao.toLowerCase();
    if (desc.includes("aberto")) return "aberto";
    if (desc.includes("andamento")) return "andamento";
    if (desc.includes("resolvido")) return "resolvido";
    return "fechado";
}

/**
 * Calcula e atualiza os KPIs (Key Performance Indicators) exibidos
 * nos cards de métricas no topo do painel de chamados.
 *
 * Métricas calculadas:
 *   • Total:       quantidade total de chamados
 *   • Abertos:     chamados com status contendo "aberto"
 *   • Em andamento: chamados com status contendo "andamento"
 *   • Resolvidos:  chamados com status contendo "resolvido"
 *
 * @param {Array} lista - Lista completa de chamados (sem filtros)
 */
function atualizarMetricas(lista) {
    // Referências aos elementos dos cards de KPI no DOM
    const elTotal = document.getElementById("statTotal");
    const elAbertos = document.getElementById("statAbertos");
    const elAndamento = document.getElementById("statAndamento");
    const elResolvidos = document.getElementById("statResolvidos");

    // Atualiza o texto de cada card com a contagem correspondente
    if (elTotal) elTotal.innerText = lista.length;
    if (elAbertos) elAbertos.innerText = lista.filter(c => c.status?.descricao?.toLowerCase().includes("aberto")).length;
    if (elAndamento) elAndamento.innerText = lista.filter(c => c.status?.descricao?.toLowerCase().includes("andamento")).length;
    if (elResolvidos) elResolvidos.innerText = lista.filter(c => c.status?.descricao?.toLowerCase().includes("resolvido")).length;
}

// ── Filtros de Chamados ──────────────────────────────────────
/**
 * Aplica os filtros ativos (status, busca textual e origem) sobre a
 * lista global de chamados e re-renderiza a tabela com os resultados.
 *
 * Filtros disponíveis:
 *   1. Origem ("TODOS" / "MEUS"):
 *      — "MEUS" exibe apenas os chamados abertos pelo usuário logado
 *      — RBAC: cidadão COMUM tem este filtro pré-selecionado em "MEUS"
 *   2. Status: filtra por um status específico (idStatus) ou "TODOS"
 *   3. Busca textual: pesquisa case-insensitive em descrição, bairro,
 *      logradouro, nome do solicitante, tipo de problema e ID do chamado
 *
 * É chamada automaticamente ao alterar qualquer filtro e ao recarregar chamados.
 */
function aplicarFiltros() {
    // Lê os valores atuais dos três controles de filtro
    const statusVal = document.getElementById("filtroStatus")?.value || "TODOS";
    const buscaVal = document.getElementById("filtroBusca")?.value.toLowerCase().trim() || "";
    const filtroOrigem = document.getElementById("filtroOrigem");
    const origemVal = filtroOrigem ? filtroOrigem.value : "TODOS";

    // Cria uma cópia da lista global para filtrar sem alterar o original
    let filtrados = [...chamados];

    // Filtro por origem: "MEUS" filtra apenas chamados do usuário logado
    if (origemVal === "MEUS" && usuarioLogado) {
        filtrados = filtrados.filter(c => c.usuario?.idUsuario === usuarioLogado.idUsuario);
    }

    // Filtro por status: compara o idStatus numérico
    if (statusVal !== "TODOS") {
        filtrados = filtrados.filter(c => c.status?.idStatus === parseInt(statusVal));
    }

    // Busca textual: pesquisa em múltiplos campos do chamado
    if (buscaVal) {
        filtrados = filtrados.filter(c =>
            // Pesquisa na descrição do chamado
            (c.descricao && c.descricao.toLowerCase().includes(buscaVal)) ||
            // Pesquisa no bairro do endereço
            (c.endereco?.bairro && c.endereco.bairro.toLowerCase().includes(buscaVal)) ||
            // Pesquisa no logradouro do endereço
            (c.endereco?.logradouro && c.endereco.logradouro.toLowerCase().includes(buscaVal)) ||
            // Pesquisa no nome do solicitante
            (c.usuario?.nome && c.usuario.nome.toLowerCase().includes(buscaVal)) ||
            // Pesquisa na descrição do tipo de problema
            (c.tipoProblema?.descricao && c.tipoProblema.descricao.toLowerCase().includes(buscaVal)) ||
            // Pesquisa pelo ID do chamado (ex: "#42")
            (`#${c.idChamado}`.includes(buscaVal))
        );
    }

    // Re-renderiza a tabela com a lista filtrada
    renderizarChamados(filtrados);
}

// ── Modal de Detalhes, Análise & Fotos ───────────────────────
/**
 * Abre o modal de detalhes de um chamado específico, preenchendo todos
 * os campos com os dados do chamado selecionado.
 *
 * Funcionalidades:
 *   1. Exibe informações gerais (tipo, status, descrição, endereço, solicitante)
 *   2. Exibe foto de evidência (se houver) com opção de ampliar no lightbox
 *   3. RBAC no painel técnico:
 *      — SABESP:  exibe painel técnico com select para alterar status (despacho)
 *      — Outros:  exibe painel informativo de cidadão (somente leitura)
 *   4. Carrega a linha do tempo de comentários/acompanhamento
 *
 * @param {number} id - ID do chamado a ser detalhado
 */
async function abrirModalDetalhes(id) {
    // Armazena o ID do chamado ativo para uso em operações subsequentes
    // (alteração de status, envio de comentário, etc.)
    chamadoAtualId = id;

    // Busca o chamado na lista global pelo ID
    const chamado = chamados.find(c => c.idChamado === id);
    if (!chamado) return;

    // ── Preenchimento dos campos de informação do modal ──
    document.getElementById("modalChamadoId").innerText = `#${chamado.idChamado}`;
    document.getElementById("detalheTipo").innerText = chamado.tipoProblema?.descricao || '-';
    document.getElementById("detalheStatus").innerText = chamado.status?.descricao || '-';
    document.getElementById("detalheDescricao").innerText = chamado.descricao || 'Sem descrição informada.';
    // Monta o endereço completo com logradouro, número, bairro, cidade e CEP
    document.getElementById("detalheEndereco").innerText = chamado.endereco ?
        `${chamado.endereco.logradouro}, ${chamado.endereco.numero || 'S/N'} - ${chamado.endereco.bairro}, ${chamado.endereco.cidade} (${chamado.endereco.cep || ''})` : '-';
    // Monta os dados do solicitante com nome, e-mail e telefone
    document.getElementById("detalheSolicitante").innerText = chamado.usuario ?
        `${chamado.usuario.nome} (${chamado.usuario.email} | ${chamado.usuario.telefone || 'Sem fone'})` : '-';

    // ── Foto de Evidência no Modal ──
    // Exibe a imagem do chamado se disponível, caso contrário mostra aviso "sem foto"
    const imgContainer = document.getElementById("detalheImagemContainer");
    const imgElem = document.getElementById("detalheImagem");
    const semFotoAviso = document.getElementById("semFotoAviso");

    if (chamado.imagemUrl) {
        // Chamado tem foto: exibe a imagem e oculta o aviso
        imgElem.src = chamado.imagemUrl;
        imgContainer.style.display = "flex";
        if (semFotoAviso) semFotoAviso.style.display = "none";
    } else {
        // Chamado sem foto: oculta o container de imagem e exibe o aviso
        imgContainer.style.display = "none";
        imgElem.src = "";
        if (semFotoAviso) semFotoAviso.style.display = "block";
    }

    // ── Painel Técnico vs Cidadão (RBAC) ──
    // O modal possui dois painéis mutuamente exclusivos:
    //   • painelTecnicoStatus: visível APENAS para SABESP — permite alterar status
    //   • painelCidadaoInfo:   visível para COMUM e PREFEITURA — informativo
    const painelTecnico = document.getElementById("painelTecnicoStatus");
    const painelCidadao = document.getElementById("painelCidadaoInfo");

    if (usuarioLogado && usuarioLogado.tipoUsuario === "SABESP") {
        // RBAC: Técnico SABESP — exibe painel de despacho com select de novo status
        if (painelTecnico) {
            painelTecnico.style.display = "block";
            // Pré-seleciona o status atual do chamado no dropdown de alteração
            const modalNovoSt = document.getElementById("modalNovoStatus");
            if (modalNovoSt && chamado.status?.idStatus) {
                modalNovoSt.value = chamado.status.idStatus;
            }
        }
        // Oculta o painel informativo de cidadão
        if (painelCidadao) painelCidadao.style.display = "none";
    } else {
        // RBAC: Cidadão ou Prefeitura — oculta painel técnico, exibe informativo
        if (painelTecnico) painelTecnico.style.display = "none";
        if (painelCidadao) painelCidadao.style.display = "block";
    }

    // Carrega os comentários/acompanhamentos do chamado na linha do tempo
    await carregarComentarios(id);

    // Torna o modal visível adicionando a classe CSS "active"
    document.getElementById("modalDetalhes").classList.add("active");
}

/**
 * Fecha o modal de detalhes removendo a classe CSS "active",
 * que controla a visibilidade via CSS (display/opacity/transform).
 */
function fecharModal() {
    document.getElementById("modalDetalhes").classList.remove("active");
}

// ── Lightbox de Fotos em Tela Cheia ──────────────────────────
/**
 * Expande a foto de evidência do modal de detalhes para visualização
 * em tela cheia usando o lightbox. Verifica se a imagem existe e não
 * é apenas a URL da página atual (que indicaria src vazio ou inválido).
 */
function expandirFotoLightbox() {
    const imgElem = document.getElementById("detalheImagem");
    // Verifica se há uma imagem válida (src não vazio e diferente da URL da página)
    if (imgElem && imgElem.src && imgElem.src !== window.location.href) {
        abrirFotoDireta(imgElem.src);
    }
}

/**
 * Abre o lightbox (overlay de tela cheia) exibindo uma foto na resolução
 * original. Usado tanto pelo botão de expandir no modal quanto pelo
 * clique na miniatura da tabela de chamados.
 *
 * @param {string} src - URL ou data URL (Base64) da imagem a ser exibida
 */
function abrirFotoDireta(src) {
    const lightbox = document.getElementById("modalFotoLightbox");
    const lbImg = document.getElementById("lightboxImg");
    if (lightbox && lbImg) {
        // Define a fonte da imagem no lightbox
        lbImg.src = src;
        // Torna o lightbox visível com a classe "active"
        lightbox.classList.add("active");
    }
}

/**
 * Fecha o lightbox de foto em tela cheia removendo a classe CSS "active".
 */
function fecharFotoLightbox() {
    const lightbox = document.getElementById("modalFotoLightbox");
    if (lightbox) lightbox.classList.remove("active");
}

// ── Alteração Técnica de Status (Exclusivo SABESP) ───────────
/**
 * Salva a alteração de status de um chamado feita pelo técnico SABESP
 * através do painel técnico no modal de detalhes.
 *
 * Fluxo:
 *   1. Verificação de RBAC: apenas perfil SABESP pode alterar status
 *   2. Envia requisição PATCH para /api/chamados/{id}/status/{novoStatusId}
 *   3. Atualiza o chamado na lista global (evita recarregar tudo)
 *   4. Atualiza o modal, re-aplica filtros e recalcula métricas
 *
 * RBAC: Bloqueado para COMUM e PREFEITURA — exibe toast de acesso negado.
 */
async function salvarStatusChamadoModal() {
    // Verifica se há um chamado selecionado no modal
    if (!chamadoAtualId) return;

    // RBAC: Apenas técnicos SABESP podem alterar status de chamados
    if (!usuarioLogado || usuarioLogado.tipoUsuario !== "SABESP") {
        showToast("Acesso negado: apenas técnicos Sabesp podem alterar o status.", "error");
        return;
    }

    // Lê o novo status selecionado no dropdown do painel técnico
    const novoStatusId = document.getElementById("modalNovoStatus").value;

    try {
        // Envia a alteração de status via PATCH, com cabeçalho de autorização
        const res = await fetch(`/api/chamados/${chamadoAtualId}/status/${novoStatusId}`, {
            method: "PATCH",
            headers: { "X-User-Role": usuarioLogado.tipoUsuario }
        });

        if (!res.ok) throw new Error("Erro ao atualizar status");

        // Recebe o chamado atualizado na resposta do backend
        const atualizado = await res.json();
        showToast(`Status atualizado para: ${atualizado.status?.descricao || 'Novo Status'}`, "success");

        // Atualiza o chamado na lista global em memória (sem recarregar da API)
        const index = chamados.findIndex(c => c.idChamado === chamadoAtualId);
        if (index !== -1) {
            chamados[index] = atualizado;
        }

        // Atualiza o campo de status no modal que ainda está aberto
        document.getElementById("detalheStatus").innerText = atualizado.status?.descricao || '-';
        // Re-aplica filtros para refletir a mudança na tabela
        aplicarFiltros();
        // Recalcula os KPIs com a lista atualizada
        atualizarMetricas(chamados);
    } catch (err) {
        showToast("Falha ao salvar status.", "error");
    }
}

// ── Exclusão de Chamado (Exclusivo SABESP) ───────────────────
/**
 * Exclui permanentemente um chamado do sistema.
 *
 * Fluxo:
 *   1. Verificação de RBAC: apenas perfil SABESP pode excluir
 *   2. Solicita confirmação do usuário via confirm() (prevenção contra cliques acidentais)
 *   3. Envia requisição DELETE para /api/chamados/{id}
 *   4. Remove o chamado da lista global e re-renderiza
 *
 * RBAC: Bloqueado para COMUM e PREFEITURA — exibe toast de acesso negado.
 *
 * @param {number} id - ID do chamado a ser excluído
 */
async function excluirChamado(id) {
    // RBAC: Apenas técnicos SABESP podem excluir chamados
    if (!usuarioLogado || usuarioLogado.tipoUsuario !== "SABESP") {
        showToast("Acesso negado: apenas técnicos Sabesp podem excluir chamados.", "error");
        return;
    }
    // Solicita confirmação do técnico antes de prosseguir com a exclusão irreversível
    if (!confirm(`Deseja excluir permanentemente o chamado #${id}?`)) return;

    try {
        // Envia a requisição DELETE com cabeçalho de autorização RBAC
        const res = await fetch(`/api/chamados/${id}`, {
            method: "DELETE",
            headers: { "X-User-Role": usuarioLogado.tipoUsuario }
        });

        if (!res.ok) throw new Error("Erro ao excluir");

        // Remove o chamado da lista global em memória
        chamados = chamados.filter(c => c.idChamado !== id);
        showToast(`Chamado #${id} excluído com sucesso.`, "success");
        // Re-aplica filtros e recalcula KPIs para refletir a remoção
        aplicarFiltros();
        atualizarMetricas(chamados);
    } catch (err) {
        showToast("Falha ao excluir chamado.", "error");
    }
}

// ── Histórico de Comentários / Linha do Tempo ────────────────
/**
 * Carrega os comentários (acompanhamentos) de um chamado específico e
 * renderiza a linha do tempo no modal de detalhes.
 *
 * Cada comentário exibe:
 *   • Nome do autor e badge do tipo de usuário (Munícipe, Sabesp, Prefeitura)
 *   • Data e hora formatados em pt-BR
 *   • Texto do comentário
 *
 * Os comentários são estilizados visualmente de forma diferente por perfil:
 *   • SABESP:      classe "sabesp-comment" (destaque operacional verde)
 *   • PREFEITURA:  classe "pref-comment" (destaque institucional roxo)
 *   • COMUM:       classe padrão "comment-bubble" (estilo de munícipe)
 *
 * @param {number} idChamado - ID do chamado cujos comentários serão carregados
 */
async function carregarComentarios(idChamado) {
    const container = document.getElementById("listaComentarios");
    if (!container) return;
    // Exibe indicador de carregamento enquanto busca os comentários
    container.innerHTML = "<p style='color:#94a3b8; font-size:0.85rem;'>Carregando comentários...</p>";

    try {
        // Busca os comentários do chamado via GET /api/comentarios/chamado/{id}
        const res = await fetch(`/api/comentarios/chamado/${idChamado}`);
        const comentarios = await res.json();

        // Limpa o container e renderiza cada comentário
        container.innerHTML = "";
        if (comentarios.length === 0) {
            // Nenhum comentário registrado ainda
            container.innerHTML = "<p style='color:#94a3b8; font-size:0.85rem;'>Nenhum acompanhamento registrado ainda.</p>";
            return;
        }

        // Itera pelos comentários e renderiza cada "bolha" da linha do tempo
        comentarios.forEach(c => {
            // Formata a data/hora para o padrão brasileiro (DD/MM/AAAA HH:MM:SS)
            const dataHora = c.dataComentario ? new Date(c.dataComentario).toLocaleString("pt-BR") : "-";
            // Identifica o tipo/perfil do autor do comentário
            const tipo = c.usuario?.tipoUsuario || "COMUM";

            // Classe CSS base para a bolha de comentário
            let classeBolha = "comment-bubble";
            // Badge padrão para cidadão (munícipe)
            let badgeTipo = `<span style="font-size:0.7rem; color:#0284c7;">Munícipe</span>`;

            // Estilização diferenciada por perfil do autor
            if (tipo === "SABESP") {
                // Comentários de técnicos SABESP: estilo verde operacional
                classeBolha += " sabesp-comment";
                badgeTipo = `<span style="font-size:0.7rem; color:#15803d; font-weight:700;">🛠️ Sabesp Operacional</span>`;
            } else if (tipo === "PREFEITURA") {
                // Comentários de fiscais da Prefeitura: estilo roxo institucional
                classeBolha += " pref-comment";
                badgeTipo = `<span style="font-size:0.7rem; color:#7e22ce; font-weight:700;">🏛️ Fiscalização Prefeitura</span>`;
            }

            // Renderiza a bolha do comentário com metadados e texto
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
        // Exibe mensagem de erro na timeline se a requisição falhar
        container.innerHTML = "<p style='color:red;'>Erro ao carregar comentários.</p>";
    }
}

/**
 * Envia um novo comentário para o chamado atualmente aberto no modal.
 *
 * Fluxo:
 *   1. Valida que o texto não está vazio
 *   2. Verifica se o usuário está autenticado
 *   3. Envia POST para /api/comentarios com o texto, ID do chamado e ID do usuário
 *   4. Limpa o campo de texto e recarrega a lista de comentários
 *
 * Requer autenticação — qualquer perfil logado (SABESP, PREFEITURA, COMUM)
 * pode enviar comentários.
 */
async function enviarComentario() {
    // Lê o texto digitado no campo de comentário do modal
    const texto = document.getElementById("novoComentarioTexto").value;
    if (!texto.trim()) {
        showToast("Digite uma mensagem antes de enviar.", "error");
        return;
    }

    // Verificação de autenticação: é necessário estar logado para comentar
    if (!usuarioLogado) {
        showToast("Usuário não autenticado.", "error");
        return;
    }

    // ID do usuário que está enviando o comentário
    const idUsuario = usuarioLogado.idUsuario;

    try {
        // Envia o comentário via POST com cabeçalho de tipo de conteúdo e perfil
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

        // Limpa o campo de texto após envio bem-sucedido
        document.getElementById("novoComentarioTexto").value = "";
        showToast("Comentário adicionado à linha do tempo!", "success");
        // Recarrega a lista de comentários para incluir o novo
        await carregarComentarios(chamadoAtualId);
    } catch (err) {
        showToast("Falha ao registrar comentário.", "error");
    }
}

// ── Atualização da Interface por Perfil (RBAC Front-End) ────
/**
 * Atualiza toda a interface do SPA com base no perfil do usuário logado.
 * Esta é a função central de RBAC no front-end — controla a visibilidade
 * e conteúdo de múltiplos elementos da UI.
 *
 * Elementos controlados por perfil:
 *
 * TODOS OS PERFIS LOGADOS:
 *   • Exibe bloco de usuário logado, oculta bloco de visitante
 *   • Exibe nome e avatar com a inicial do nome
 *
 * SABESP (Técnico Operacional):
 *   • Badge: "🛠️ Técnico Sabesp" (verde)
 *   • Botão "Cadastros" no nav: VISÍVEL (acesso a cadastros rápidos)
 *   • Categoria Admin: VISÍVEL (painel de administração)
 *   • Banner: "Central Operacional Sabesp — Engenharia & Despacho"
 *   • Filtro de origem: "TODOS" (vê todos os chamados da cidade)
 *   • Subtítulo: "Fila Geral de Chamados da Cidade de Santos"
 *
 * PREFEITURA (Fiscal de Auditoria):
 *   • Badge: "🏛️ Fiscal Prefeitura" (cinza)
 *   • Botão "Cadastros" no nav: OCULTO
 *   • Categoria Admin: OCULTA
 *   • Banner: "Painel de Fiscalização Municipal — Auditoria de Serviços"
 *   • Filtro de origem: "TODOS" (vê todos os chamados para auditoria)
 *   • Subtítulo: "Auditoria de Todas as Ocorrências Notificadas"
 *
 * COMUM (Cidadão Morador):
 *   • Badge: "👤 Cidadão" (azul)
 *   • Botão "Cadastros" no nav: OCULTO
 *   • Categoria Admin: OCULTA
 *   • Banner: "Portal do Cidadão — Atendimento de Esgoto e Saneamento"
 *   • Filtro de origem: "MEUS" (vê apenas seus próprios chamados por padrão)
 *   • Subtítulo: "Seus Chamados e Ocorrências Registradas"
 *
 * VISITANTE (não logado):
 *   • Exibe bloco de visitante com link para login
 *   • Oculta bloco de usuário logado e botão de cadastros
 *
 * Ao final, re-popula os selects (atualizarSelects) e re-aplica
 * os filtros (aplicarFiltros) para garantir consistência.
 */
function atualizarInterfaceUsuario() {
    // ── Referências aos elementos da interface que variam por perfil ──
    const infoLogged = document.getElementById("userInfoLogged");     // Bloco de info do usuário logado
    const infoGuest = document.getElementById("userInfoGuest");       // Bloco de info para visitantes
    const navNome = document.getElementById("navUserNome");           // Nome do usuário no navbar
    const navBadge = document.getElementById("navUserBadge");         // Badge de perfil no navbar
    const navBtnCad = document.getElementById("navBtnCadastros");     // Botão "Cadastros" no nav (admin)
    const catAdmin = document.getElementById("categoriaAdmin");       // Painel da categoria de administração
    const filtroOrigem = document.getElementById("filtroOrigem");     // Select de filtro "Todos/Meus"
    const avatarChar = document.getElementById("userAvatarChar");     // Inicial do nome no avatar circular
    const banner = document.getElementById("bannerContextual");       // Banner hero contextual por perfil
    const bannerTitulo = document.getElementById("bannerTitulo");     // Título do banner
    const bannerDesc = document.getElementById("bannerDescricao");    // Descrição do banner
    const roleCallout = document.getElementById("roleDescriptionCallout"); // Callout com nível de acesso
    const subtituloChamados = document.getElementById("subtituloChamados"); // Subtítulo da seção de chamados
    const cardNovoUsuario = document.getElementById("cardNovoUsuario");   // Card de criação de contas (só Prefeitura)
    const cardNovoEndereco = document.getElementById("cardNovoEndereco"); // Card de criação de endereços (só Sabesp)

    if (usuarioLogado) {
        // ── Usuário está logado: exibe informações de sessão ──
        if (infoLogged) infoLogged.style.display = "block";
        if (infoGuest) infoGuest.style.display = "none";
        if (navNome) navNome.innerText = usuarioLogado.nome;
        // Exibe a primeira letra do nome como avatar
        if (avatarChar) avatarChar.innerText = usuarioLogado.nome.charAt(0).toUpperCase();

        // Reseta a classe base do banner (remove classes de perfil anteriores)
        if (banner) banner.className = "role-hero-banner";

        // ── Personalização por perfil ──
        if (usuarioLogado.tipoUsuario === "SABESP") {
            // Perfil SABESP: acesso total ao sistema operacional
            if (navBadge) { navBadge.innerText = "🛠️ Técnico Sabesp"; navBadge.className = "status-pill resolvido"; }
            if (navBtnCad) navBtnCad.style.display = "inline-flex";  // Exibe botão de cadastros (base de endereços)
            if (catAdmin) catAdmin.style.display = "block";           // Exibe painel admin
            // RBAC: a Sabesp NÃO cria contas de usuário — só a base de endereços
            if (cardNovoUsuario) cardNovoUsuario.style.display = "none";
            if (cardNovoEndereco) cardNovoEndereco.style.display = "block";
            if (banner) banner.classList.add("banner-sabesp");
            if (bannerTitulo) bannerTitulo.innerText = "Central Operacional Sabesp — Engenharia & Despacho";
            if (bannerDesc) bannerDesc.innerText = "Acesso total à rede: altere status de ocorrências, gerencie equipes de campo e atualize a base cadastral.";
            if (roleCallout) roleCallout.innerHTML = `Nível: <strong style="color:var(--role-sabesp)">Técnico Sabesp (Controle Total & Despacho)</strong>`;
            if (filtroOrigem) filtroOrigem.value = "TODOS";           // Vê todos os chamados
            if (subtituloChamados) subtituloChamados.innerText = "Fila Geral de Chamados da Cidade de Santos";

        } else if (usuarioLogado.tipoUsuario === "PREFEITURA") {
            // Perfil PREFEITURA: acesso de auditoria e fiscalização
            if (navBadge) { navBadge.innerText = "🏛️ Fiscal Prefeitura"; navBadge.className = "status-pill fechado"; }
            // RBAC: só a Prefeitura cria contas institucionais (Sabesp/Prefeitura)
            if (navBtnCad) navBtnCad.style.display = "inline-flex";   // Exibe botão de cadastros
            if (catAdmin) catAdmin.style.display = "block";            // Exibe categoria admin
            if (cardNovoUsuario) cardNovoUsuario.style.display = "block"; // Cartão de contas visível
            if (cardNovoEndereco) cardNovoEndereco.style.display = "none"; // Endereços são da Sabesp
            if (banner) banner.classList.add("banner-prefeitura");
            if (bannerTitulo) bannerTitulo.innerText = "Painel de Fiscalização Municipal — Auditoria de Serviços";
            if (bannerDesc) bannerDesc.innerText = "Auditoria de chamados de esgoto: consulte fotos de evidência, vistorie ocorrências e emita notificações.";
            if (roleCallout) roleCallout.innerHTML = `Nível: <strong style="color:var(--role-pref)">Fiscalização Prefeitura (Auditoria & Pareceres)</strong>`;
            if (filtroOrigem) filtroOrigem.value = "TODOS";            // Vê todos os chamados (auditoria)
            if (subtituloChamados) subtituloChamados.innerText = "Auditoria de Todas as Ocorrências Notificadas";

        } else {
            // Perfil COMUM: cidadão morador — acesso limitado
            if (navBadge) { navBadge.innerText = "👤 Cidadão"; navBadge.className = "status-pill aberto"; }
            if (navBtnCad) navBtnCad.style.display = "none";          // Sem acesso a cadastros
            if (catAdmin) catAdmin.style.display = "none";             // Sem painel admin
            if (cardNovoUsuario) cardNovoUsuario.style.display = "none";
            if (cardNovoEndereco) cardNovoEndereco.style.display = "none";
            if (banner) banner.classList.add("banner-comum");
            if (bannerTitulo) bannerTitulo.innerText = "Portal do Cidadão — Atendimento de Esgoto e Saneamento";
            if (bannerDesc) bannerDesc.innerText = "Abra solicitações de desobstrução com fotos, acompanhe o status da sua rua e interaja com os técnicos.";
            if (roleCallout) roleCallout.innerHTML = `Nível: <strong style="color:var(--role-comum)">Cidadão Morador (Abertura e Acompanhamento)</strong>`;
            if (filtroOrigem) filtroOrigem.value = "MEUS";             // Vê apenas seus próprios chamados
            if (subtituloChamados) subtituloChamados.innerText = "Seus Chamados e Ocorrências Registradas";
        }
    } else {
        // ── Visitante (não logado): interface mínima ──
        if (infoLogged) infoLogged.style.display = "none";    // Oculta bloco de usuário
        if (infoGuest) infoGuest.style.display = "block";     // Exibe bloco de visitante
        if (navBtnCad) navBtnCad.style.display = "none";      // Sem botão de cadastros
        if (cardNovoUsuario) cardNovoUsuario.style.display = "none"; // Sem criação de contas
        if (cardNovoEndereco) cardNovoEndereco.style.display = "none";
    }

    // Re-popula todos os dropdowns com as regras de RBAC atualizadas
    atualizarSelects();
    // Re-aplica filtros para refletir o novo contexto do perfil
    aplicarFiltros();
}

// ── Login e Troca Rápida de Perfil ───────────────────────────
/**
 * Abre o modal de login/autenticação adicionando a classe CSS "active".
 * O modal contém três telas intercambiáveis: login, registro e recuperação de senha.
 */
function abrirModalLogin() {
    document.getElementById("modalLogin").classList.add("active");
}

/**
 * Fecha o modal de login removendo a classe CSS "active".
 * Sempre retorna para a tela de login (não registro ou recuperação)
 * ao fechar, garantindo que o próximo acesso comece pela tela correta.
 */
function fecharModalLogin() {
    document.getElementById("modalLogin").classList.remove("active");
    // Sempre retorna para a tela de login ao fechar o modal
    alternarAbaAuth("login");
}

// ── Alternância das telas do modal de autenticação ───────────
// telas possíveis: "login" | "registro" | "recuperar"
/**
 * Alterna entre as três telas do modal de autenticação:
 *   • "login":     formulário de login com e-mail e senha
 *   • "registro":  formulário de criação de conta de cidadão
 *   • "recuperar": formulário de recuperação/redefinição de senha
 *
 * Ao trocar de tela:
 *   1. Oculta todas as telas e exibe apenas a selecionada
 *   2. Atualiza o título do modal conforme a tela
 *   3. Limpa os campos do formulário da tela exibida (evita dados presos)
 *
 * @param {string} aba - Identificador da tela: "login", "registro" ou "recuperar"
 */
function alternarAbaAuth(aba) {
    // Referências das três telas do modal (cada uma é um <div> com display toggle)
    const telas = {
        login: document.getElementById("abaAuthLogin"),
        registro: document.getElementById("abaAuthRegistro"),
        recuperar: document.getElementById("abaAuthRecuperar")
    };

    // Títulos exibidos no cabeçalho conforme a tela ativa
    const titulos = {
        login: "🔐 Entrar — Central do Cidadão",
        registro: "📝 Criar Conta de Cidadão",
        recuperar: "🔑 Recuperar Senha"
    };

    // Esconde todas as telas e mostra apenas a solicitada
    Object.keys(telas).forEach(chave => {
        if (telas[chave]) {
            telas[chave].style.display = (chave === aba) ? "block" : "none";
        }
    });

    // Atualiza o título do modal
    const titulo = document.getElementById("modalLoginTitulo");
    if (titulo && titulos[aba]) {
        titulo.innerText = titulos[aba];
    }

    // Limpa os campos ao trocar de tela (evita dados presos entre navegações)
    if (aba === "login") {
        document.getElementById("loginEmail").value = "";
        document.getElementById("loginSenha").value = "";
    } else if (aba === "registro") {
        document.getElementById("formRegistroPublico").reset();
    } else if (aba === "recuperar") {
        document.getElementById("formRecuperarSenha").reset();
    }
}

/**
 * Realiza o login do usuário via API REST.
 *
 * Fluxo:
 *   1. Envia POST /api/auth/login com e-mail e senha em JSON
 *   2. Se a resposta indicar erro, exibe a mensagem do backend
 *   3. RBAC de redirecionamento: se o usuário retornado for SABESP ou PREFEITURA,
 *      redireciona para o portal institucional correspondente (/login-sabesp.html
 *      ou /login-prefeitura.html), pois este modal é exclusivo de cidadãos
 *   4. Se for COMUM (cidadão): salva no estado global e localStorage,
 *      fecha o modal, atualiza a interface e exibe boas-vindas
 *
 * @param {string} email - E-mail do usuário
 * @param {string} senha - Senha do usuário
 */
async function fazerLogin(email, senha) {
    try {
        // Envia credenciais para o endpoint de autenticação
        const res = await fetch("/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, senha })
        });

        if (!res.ok) {
            // Tenta extrair a mensagem de erro do corpo da resposta
            const errData = await res.json().catch(() => ({}));
            throw new Error(errData.mensagem || "E-mail ou senha incorretos.");
        }

        // Parse dos dados do usuário autenticado
        const data = await res.json();

        // ── Redireciona contas institucionais para o portal certo ──
        // Este modal é exclusivo de cidadãos (COMUM). Técnico Sabesp e
        // Fiscal Prefeitura fazem login nas páginas dedicadas.
        if (data.tipoUsuario === "SABESP" || data.tipoUsuario === "PREFEITURA") {
            // Determina qual portal institucional abrir
            const portal = data.tipoUsuario === "SABESP" ? "/login-sabesp.html" : "/login-prefeitura.html";
            showToast(`Este perfil usa o portal institucional. Redirecionando...`, "success");
            // Passa o e-mail pré-preenchido para o portal correspondente via query string
            window.location.href = `${portal}?email=${encodeURIComponent(email)}`;
            return;
        }

        // ── Login de cidadão bem-sucedido ──
        // Salva o usuário no estado global e persiste no localStorage
        usuarioLogado = data;
        localStorage.setItem("sabesp_user", JSON.stringify(data));
        // Fecha o modal de login
        fecharModalLogin();
        // Atualiza toda a interface com base no perfil do novo usuário
        atualizarInterfaceUsuario();
        showToast(`Bem-vindo(a), ${data.nome}!`, "success");
    } catch (err) {
        showToast(err.message || "Erro ao realizar login.", "error");
    }
}

/**
 * Função auxiliar para login rápido (atalho para fazerLogin).
 * Utilizada nos botões de "Entrar como..." para troca rápida de perfil
 * durante demonstrações ou testes.
 *
 * @param {string} email - E-mail do usuário
 * @param {string} senha - Senha do usuário
 */
async function loginRapido(email, senha) {
    await fazerLogin(email, senha);
}

/**
 * Recupera a senha do usuário.
 * Envia o e-mail da conta e a nova senha para o backend.
 * Se a conta existir, a senha é atualizada e o usuário volta para o login.
 *
 * Fluxo:
 *   1. Envia POST /api/auth/recuperar-senha com e-mail e nova senha
 *   2. Se bem-sucedido, exibe toast de sucesso e redireciona para a tela de login
 *   3. Se falhar (conta não encontrada, etc.), exibe mensagem de erro
 *
 * @param {string} email - E-mail da conta a recuperar
 * @param {string} novaSenha - Nova senha desejada
 */
async function recuperarSenha(email, novaSenha) {
    try {
        // Envia a solicitação de redefinição de senha
        const res = await fetch("/api/auth/recuperar-senha", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, novaSenha })
        });

        if (!res.ok) {
            // Tenta extrair a mensagem de erro do backend
            const errData = await res.json().catch(() => ({}));
            throw new Error(errData.mensagem || "Não foi possível redefinir a senha.");
        }

        // Redefinição bem-sucedida: limpa o formulário e retorna para o login
        showToast("Senha redefinida com sucesso! Faça login com a nova senha.", "success");
        document.getElementById("formRecuperarSenha").reset();
        alternarAbaAuth("login");
    } catch (err) {
        showToast(err.message || "Erro ao redefinir senha.", "error");
    }
}

/**
 * Encerra a sessão do usuário (logout).
 *
 * Fluxo:
 *   1. Remove os dados do usuário do localStorage
 *   2. Limpa o estado global (usuarioLogado = null)
 *   3. Atualiza a interface para o estado de visitante
 *   4. Exibe toast de confirmação e abre o modal de login
 */
function fazerLogout() {
    // Remove a sessão persistida no localStorage
    localStorage.removeItem("sabesp_user");
    // Limpa o estado global
    usuarioLogado = null;
    // Atualiza a interface para refletir o estado deslogado
    atualizarInterfaceUsuario();
    showToast("Sessão finalizada.", "success");
    // Abre o modal de login para que o usuário possa entrar novamente
    abrirModalLogin();
}

// ── Utilitários para Processamento de Imagens ──────────────
/**
 * Limpa todos os elementos relacionados ao upload de imagem no formulário
 * de novo chamado: variável Base64, input de arquivo, container de preview
 * e botão de remoção.
 *
 * Chamada ao:
 *   • Resetar o formulário de novo chamado
 *   • Remover manualmente a imagem (botão "Remover foto")
 *   • Enviar o chamado com sucesso (limpa para o próximo)
 */
function limparImagemForm() {
    // Limpa o dado Base64 armazenado em memória
    imagemChamadoBase64 = null;
    // Reseta o input de arquivo para permitir re-seleção do mesmo arquivo
    const inputImg = document.getElementById("chamadoImagemArquivo");
    if (inputImg) inputImg.value = "";
    // Oculta o container de pré-visualização da imagem
    const prevCont = document.getElementById("previewImagemContainer");
    if (prevCont) prevCont.style.display = "none";
    // Limpa a src da imagem de preview
    const prevImg = document.getElementById("previewImagem");
    if (prevImg) prevImg.src = "";
    // Oculta o botão de remover imagem
    const btnRem = document.getElementById("btnRemoverImagem");
    if (btnRem) btnRem.style.display = "none";
}

/**
 * Comprime e converte uma imagem selecionada pelo usuário usando a API Canvas.
 * Este processamento é feito inteiramente no lado do cliente (browser) para:
 *   • Reduzir o tamanho do arquivo antes do envio (economia de banda)
 *   • Padronizar o formato para JPEG
 *   • Limitar as dimensões máximas mantendo a proporção (aspect ratio)
 *
 * Fluxo técnico:
 *   1. Lê o arquivo do input usando FileReader como data URL
 *   2. Cria um objeto Image para obter as dimensões originais
 *   3. Calcula as novas dimensões respeitando maxWidth/maxHeight e proporção
 *   4. Desenha a imagem redimensionada em um Canvas temporário
 *   5. Exporta o Canvas como data URL JPEG com a qualidade especificada
 *
 * @param {File} file - Arquivo de imagem selecionado pelo usuário (do input type="file")
 * @param {number} maxWidth - Largura máxima em pixels (padrão: 1200)
 * @param {number} maxHeight - Altura máxima em pixels (padrão: 1200)
 * @param {number} qualidade - Qualidade JPEG de 0 a 1 (padrão: 0.85 = 85%)
 * @returns {Promise<string>} Promessa que resolve com a data URL JPEG comprimida
 */
function comprimirEConverterImagem(file, maxWidth = 1200, maxHeight = 1200, qualidade = 0.85) {
    return new Promise((resolve, reject) => {
        // Passo 1: Lê o arquivo como data URL usando FileReader
        const reader = new FileReader();
        reader.onload = (e) => {
            // Passo 2: Cria uma imagem HTML para obter dimensões e dados de pixel
            const img = new Image();
            img.onload = () => {
                // Passo 3: Calcula novas dimensões mantendo a proporção (aspect ratio)
                let width = img.width;
                let height = img.height;

                // Redimensiona apenas se a imagem excede os limites máximos
                if (width > maxWidth || height > maxHeight) {
                    if (width > height) {
                        // Imagem paisagem: limita pela largura
                        height = Math.round((height * maxWidth) / width);
                        width = maxWidth;
                    } else {
                        // Imagem retrato ou quadrada: limita pela altura
                        width = Math.round((width * maxHeight) / height);
                        height = maxHeight;
                    }
                }

                // Passo 4: Cria um Canvas temporário e desenha a imagem redimensionada
                const canvas = document.createElement("canvas");
                canvas.width = width;
                canvas.height = height;
                const ctx = canvas.getContext("2d");
                // Desenha a imagem no canvas com as novas dimensões
                ctx.drawImage(img, 0, 0, width, height);

                // Passo 5: Exporta o canvas como data URL JPEG com compressão
                const dataUrl = canvas.toDataURL("image/jpeg", qualidade);
                resolve(dataUrl);
            };
            // Callback de erro ao carregar a imagem
            img.onerror = reject;
            // Inicia o carregamento da imagem a partir do data URL do FileReader
            img.src = e.target.result;
        };
        // Callback de erro ao ler o arquivo
        reader.onerror = reject;
        // Inicia a leitura do arquivo como data URL (Base64)
        reader.readAsDataURL(file);
    });
}

// ── Configuração de Todos os Eventos do Sistema ──────────────
/**
 * Configura (vincula) todos os event listeners da aplicação.
 * Chamada uma única vez durante a inicialização (DOMContentLoaded).
 *
 * Eventos configurados:
 *   1. Navegação entre abas/views (com RBAC para aba de novo chamado)
 *   2. Upload e pré-visualização de imagem (com compressão via Canvas)
 *   3. Remoção de imagem anexada
 *   4. Submit do formulário de novo chamado (com RBAC para cidadão)
 *   5. Filtros de busca (origem, status, texto)
 *   6. Submit do formulário de login
 *   7. Submit do formulário de registro público (criação de conta de cidadão)
 *   8. Submit do formulário de recuperação de senha
 *   9. Submit do formulário de cadastro rápido de usuário (RBAC: só Prefeitura)
 *  10. Submit do formulário de cadastro rápido de endereço
 */
function configurarEventos() {
    // ── 1. Navegação entre Abas (Views) ──
    // Cada botão .nav-btn possui um atributo data-view indicando qual painel mostrar
    document.querySelectorAll(".nav-btn[data-view]").forEach(btn => {
        btn.addEventListener("click", () => {
            const target = btn.getAttribute("data-view");

            // RBAC: apenas cidadão comum pode acessar a aba de novo chamado
            // Técnicos SABESP e fiscais PREFEITURA não abrem chamados pelo SPA
            if (target === "view-novo") {
                if (!usuarioLogado || usuarioLogado.tipoUsuario !== "COMUM") {
                    showToast("Acesso negado: apenas cidadãos podem registrar novos chamados.", "error");
                    return;
                }
            }

            // Remove a classe "active" de todos os botões e painéis
            document.querySelectorAll(".nav-btn").forEach(b => b.classList.remove("active"));
            document.querySelectorAll(".view-panel").forEach(p => p.classList.remove("active"));

            // Ativa o botão clicado e o painel correspondente
            btn.classList.add("active");
            const panel = document.getElementById(target);
            if (panel) panel.classList.add("active");
        });
    });

    // ── 2. Upload e Pré-visualização de Imagem ──
    // Listener no input type="file" para processar a imagem selecionada
    const inputImg = document.getElementById("chamadoImagemArquivo");
    if (inputImg) {
        inputImg.addEventListener("change", async (e) => {
            const file = e.target.files[0];
            if (file) {
                // Valida que o arquivo é uma imagem (tipo MIME começa com "image/")
                if (!file.type.startsWith("image/")) {
                    showToast("Por favor, selecione um arquivo de imagem válido.", "error");
                    limparImagemForm();
                    return;
                }
                try {
                    // Comprime a imagem via Canvas e armazena o resultado Base64
                    imagemChamadoBase64 = await comprimirEConverterImagem(file);

                    // Exibe a pré-visualização da imagem comprimida
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

    // ── 3. Botão de Remoção de Imagem Anexada ──
    const btnRemoverImg = document.getElementById("btnRemoverImagem");
    if (btnRemoverImg) {
        btnRemoverImg.addEventListener("click", () => {
            limparImagemForm();
            showToast("Foto removida.", "success");
        });
    }

    // ── 4. Formulário de Novo Chamado (com RBAC) ──
    const formNovo = document.getElementById("formNovoChamado");
    if (formNovo) {
        // Ao resetar o formulário, também limpa a imagem anexada
        formNovo.addEventListener("reset", () => {
            limparImagemForm();
        });

        // Submit: cria uma nova ocorrência via POST /api/chamados
        formNovo.addEventListener("submit", async (e) => {
            e.preventDefault();

            // Gera data e hora atuais para o chamado
            const now = new Date();
            const dataAtual = now.toISOString().split("T")[0];       // Formato AAAA-MM-DD
            const horaAtual = now.toTimeString().split(" ")[0];       // Formato HH:MM:SS

            // RBAC: Solicitante — se for cidadão comum, trava no próprio ID
            // (cidadão não pode abrir chamados em nome de terceiros)
            const idUsuarioFinal = (usuarioLogado && usuarioLogado.tipoUsuario === "COMUM")
                ? usuarioLogado.idUsuario
                : (parseInt(document.getElementById("chamadoUsuario").value) || (usuarioLogado ? usuarioLogado.idUsuario : 1));

            // RBAC: Status inicial — cidadão comum abre sempre como 1 (Aberto)
            // (cidadão não pode definir status diferente de "Aberto")
            const idStatusFinal = (usuarioLogado && usuarioLogado.tipoUsuario === "COMUM")
                ? 1
                : (parseInt(document.getElementById("chamadoStatus").value) || 1);

            // Monta o payload JSON para a API
            const payload = {
                dataChamado: dataAtual,
                horaChamado: horaAtual,
                descricao: document.getElementById("chamadoDescricao").value,
                idUsuario: idUsuarioFinal,
                idEndereco: parseInt(document.getElementById("chamadoEndereco").value),
                idTipoProblema: parseInt(document.getElementById("chamadoTipoProblema").value),
                idStatus: idStatusFinal,
                // Envia a imagem como data URL Base64 (ou null se sem foto)
                imagemUrl: imagemChamadoBase64
            };

            try {
                // Envia o novo chamado para a API com cabeçalhos de autenticação
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
                // Limpa o formulário e a imagem para o próximo chamado
                formNovo.reset();
                limparImagemForm();
                // Recarrega a lista de chamados para incluir o novo
                await carregarChamados();

                // Direciona automaticamente para a aba de chamados (listagem)
                document.querySelector('.nav-btn[data-view="view-chamados"]').click();
            } catch (err) {
                showToast("Erro ao cadastrar chamado.", "error");
            }
        });
    }

    // ── 5. Filtros de Busca ──
    // Vincula os eventos de alteração dos filtros para re-aplicar a filtragem

    // Filtro por origem (Todos/Meus chamados)
    const filtroOrigem = document.getElementById("filtroOrigem");
    if (filtroOrigem) filtroOrigem.addEventListener("change", aplicarFiltros);

    // Filtro por status (dropdown de status)
    const filtroStatus = document.getElementById("filtroStatus");
    if (filtroStatus) filtroStatus.addEventListener("change", aplicarFiltros);

    // Busca textual (campo de pesquisa — dispara a cada caractere digitado)
    const filtroBusca = document.getElementById("filtroBusca");
    if (filtroBusca) filtroBusca.addEventListener("input", aplicarFiltros);

    // ── 6. Formulário de Login ──
    const formLogin = document.getElementById("formLogin");
    if (formLogin) {
        formLogin.addEventListener("submit", async (e) => {
            e.preventDefault();
            // Lê e-mail e senha dos campos do formulário e tenta fazer login
            const email = document.getElementById("loginEmail").value;
            const senha = document.getElementById("loginSenha").value;
            await fazerLogin(email, senha);
        });
    }

    // ── 7. Formulário de Registro Público (Cidadão) ──
    // Permite que novos cidadãos criem suas contas diretamente pelo SPA.
    // O tipo de usuário é fixado como "COMUM" (cidadão).
    const formRegPub = document.getElementById("formRegistroPublico");
    if (formRegPub) {
        formRegPub.addEventListener("submit", async (e) => {
            e.preventDefault();

            // Validação simples: senha e confirmação precisam ser iguais
            const senha = document.getElementById("regSenha").value;
            const confirmacao = document.getElementById("regSenhaConfirm").value;
            if (senha !== confirmacao) {
                showToast("As senhas não coincidem. Confira e tente novamente.", "error");
                return;
            }

            // Monta o payload para criação de conta via POST /api/auth/registro
            const payload = {
                nome: document.getElementById("regNome").value,
                email: document.getElementById("regEmail").value,
                senha: senha,
                telefone: document.getElementById("regTelefone").value,
                tipoUsuario: "COMUM"  // Registro público cria apenas contas de cidadão
            };
            try {
                const res = await fetch("/api/auth/registro", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload)
                });
                if (!res.ok) {
                    // Tenta extrair mensagem de erro do backend (ex: e-mail já cadastrado)
                    const errData = await res.json().catch(() => ({}));
                    throw new Error(errData.mensagem || "Erro ao criar conta");
                }
                // Conta criada com sucesso — faz login automático
                const novoUser = await res.json();
                showToast("Conta criada com sucesso! Você já está logado.", "success");
                formRegPub.reset();
                // Define o novo usuário como logado (login automático pós-registro)
                usuarioLogado = novoUser;
                localStorage.setItem("sabesp_user", JSON.stringify(novoUser));
                fecharModalLogin();
                // Recarrega a lista de usuários e atualiza a interface
                await carregarUsuarios();
                atualizarInterfaceUsuario();
            } catch (err) {
                showToast(err.message || "Falha ao criar conta.", "error");
            }
        });
    }

    // ── 8. Formulário de Recuperação de Senha ──
    const formRecuperar = document.getElementById("formRecuperarSenha");
    if (formRecuperar) {
        formRecuperar.addEventListener("submit", async (e) => {
            e.preventDefault();

            // Validação simples: nova senha e confirmação precisam ser iguais
            const novaSenha = document.getElementById("recNovaSenha").value;
            const confirmacao = document.getElementById("recNovaSenhaConfirm").value;
            if (novaSenha !== confirmacao) {
                showToast("As senhas não coincidem. Confira e tente novamente.", "error");
                return;
            }

            // Envia a solicitação de redefinição (e-mail + nova senha)
            await recuperarSenha(document.getElementById("recEmail").value, novaSenha);
        });
    }

    // ── 9. Cadastros Rápidos de Usuário (RBAC: APENAS Prefeitura pode criar contas) ──
    // Este formulário está no painel administrativo e cadastra contas INSTITUCIONAIS
    // (SABESP ou PREFEITURA) com senha padrão "123" (o usuário deve alterar depois).
    // Regras da regra de contas:
    //   • A Sabesp NÃO cria conta de ninguém;
    //   • Nenhuma instituição cria conta de cidadão (COMUM) — cidadão se auto-cadastra
    //     pelo modal público de registro (/api/auth/registro);
    //   • Somente a Prefeitura cria contas institucionais.
    const formRapidoUser = document.getElementById("formRapidoUsuario");
    if (formRapidoUser) {
        formRapidoUser.addEventListener("submit", async (e) => {
            e.preventDefault();

            // RBAC no front: apenas a Prefeitura pode cadastrar contas por aqui
            if (!usuarioLogado || usuarioLogado.tipoUsuario !== "PREFEITURA") {
                showToast("Acesso negado: apenas a Prefeitura pode cadastrar novos usuários.", "error");
                return;
            }

            const tipoSelecionado = document.getElementById("userTipo").value;
            // Guarda extra: cidadãos (COMUM) nunca são criados por este formulário
            if (tipoSelecionado === "COMUM") {
                showToast("Contas de cidadão não são criadas aqui: use o auto-cadastro público.", "error");
                return;
            }

            // Monta o payload com senha padrão "123" (usuário deve alterar depois)
            const payload = {
                nome: document.getElementById("userNome").value,
                email: document.getElementById("userEmail").value,
                senha: "123",
                telefone: document.getElementById("userTelefone").value,
                tipoUsuario: tipoSelecionado
            };
            try {
                // Envia POST /api/usuarios com cabeçalhos RBAC
                const res = await fetch("/api/usuarios", {
                    method: "POST",
                    headers: { "Content-Type": "application/json", "X-User-Role": usuarioLogado.tipoUsuario, "X-User-Id": usuarioLogado.idUsuario },
                    body: JSON.stringify(payload)
                });
                if (!res.ok) {
                    // Exibe a mensagem de erro vinda do backend (ex.: 403 RBAC)
                    const msg = await res.text().catch(() => "");
                    throw new Error(msg || "Erro ao cadastrar");
                }
                showToast("Usuário cadastrado com sucesso!", "success");
                formRapidoUser.reset();
                // Recarrega a lista de usuários e atualiza os selects
                await carregarUsuarios();
                atualizarSelects();
            } catch (err) {
                showToast(err.message || "Erro ao salvar usuário.", "error");
            }
        });
    }

    // ── 10. Cadastro Rápido de Endereço ──
    // Formulário para adicionar novos endereços ao sistema.
    // Não possui restrição de RBAC específica neste formulário
    // (a visibilidade do painel que o contém é controlada pelo RBAC da interface).
    const formRapidoEnd = document.getElementById("formRapidoEndereco");
    if (formRapidoEnd) {
        formRapidoEnd.addEventListener("submit", async (e) => {
            e.preventDefault();
            // Monta o payload com todos os campos de endereço
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
                // Envia POST /api/enderecos para criar o novo endereço
                const res = await fetch("/api/enderecos", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload)
                });
                if (!res.ok) throw new Error("Erro ao cadastrar");
                showToast("Endereço adicionado com sucesso!", "success");
                formRapidoEnd.reset();
                // Recarrega a lista de endereços e atualiza os selects
                await carregarEnderecos();
                atualizarSelects();
            } catch (err) {
                showToast("Erro ao salvar endereço.", "error");
            }
        });
    }
}

// ── Toasts de Notificação ────────────────────────────────────
/**
 * Exibe uma notificação toast temporária na tela para feedback ao usuário.
 *
 * Os toasts são inseridos no container #toastContainer (geralmente posicionado
 * no canto superior direito via CSS) e removidos automaticamente após 4 segundos.
 *
 * Tipos de toast:
 *   • "success": ícone ✓, estilo verde (confirmação de ação bem-sucedida)
 *   • "error":   ícone ✕, estilo vermelho (alerta de erro ou acesso negado)
 *
 * @param {string} mensagem - Texto a ser exibido no toast
 * @param {string} tipo - Tipo do toast: "success" (padrão) ou "error"
 */
function showToast(mensagem, tipo = "success") {
    // Referência ao container de toasts no DOM
    const container = document.getElementById("toastContainer");
    if (!container) return;

    // Cria o elemento do toast com classe CSS dinâmica (toast-success ou toast-error)
    const toast = document.createElement("div");
    toast.className = `toast toast-${tipo}`;
    // Define o ícone de acordo com o tipo (✓ para sucesso, ✕ para erro)
    toast.innerHTML = tipo === "success" ? `✓ ${mensagem}` : `✕ ${mensagem}`;
    // Insere o toast no container (aparece na tela)
    container.appendChild(toast);

    // Remove o toast automaticamente após 4 segundos (4000ms)
    setTimeout(() => {
        toast.remove();
    }, 4000);
}