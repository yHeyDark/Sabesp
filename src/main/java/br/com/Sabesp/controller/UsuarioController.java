package br.com.Sabesp.controller;

import br.com.Sabesp.dto.UsuarioRequestDTO;
import br.com.Sabesp.dto.UsuarioResponseDTO;
import br.com.Sabesp.model.enums.TipoUsuario;
import br.com.Sabesp.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST responsável pelo CRUD de Usuários com controle de acesso baseado
 * em papéis (RBAC — Role-Based Access Control).
 *
 * <p>Base path: {@code /api/usuarios}</p>
 *
 * <p>Usuários podem possuir um dos três perfis definidos em {@link TipoUsuario}:</p>
 * <ul>
 *   <li><b>COMUM</b> — cidadão comum que abre chamados</li>
 *   <li><b>SABESP</b> — funcionário da Sabesp responsável pelo atendimento</li>
 *   <li><b>PREFEITURA</b> — funcionário da Prefeitura com privilégios administrativos</li>
 * </ul>
 *
 * <p><b>Regra RBAC de criação de contas:</b></p>
 * <ul>
 *   <li>A <b>Sabesp não cria conta de ninguém</b> (qualquer criação é negada com 403);</li>
 *   <li><b>Nenhuma instituição cria contas de cidadão ({@code COMUM})</b> — o cidadão se
 *       auto-cadastra em {@code POST /api/auth/registro};</li>
 *   <li><b>Somente a Prefeitura</b> cria contas institucionais ({@code SABESP} e
 *       {@code PREFEITURA}), identificada pelo cabeçalho {@code X-User-Role}.</li>
 * </ul>
 *
 * <p>A injeção de dependência do {@link UsuarioService} é feita via
 * construtor gerado pelo Lombok ({@code @RequiredArgsConstructor}).</p>
 */
@RestController // Controlador REST — respostas JSON automáticas via Jackson
@RequestMapping("/api/usuarios") // Prefixo de URL para todos os endpoints deste controlador
@RequiredArgsConstructor // Lombok: gera construtor com os campos final (injeção de dependência)
public class UsuarioController {

    /** Serviço de negócios para operações de Usuário. Injetado via construtor. */
    private final UsuarioService usuarioService;

    /**
     * Lista todos os usuários cadastrados no sistema.
     *
     * <p><b>Endpoint:</b> {@code GET /api/usuarios}</p>
     * <p><b>HTTP 200 OK</b> — retorna a lista completa de usuários.</p>
     *
     * @return {@link ResponseEntity} contendo a lista de {@link UsuarioResponseDTO}
     */
    @GetMapping // HTTP GET na raiz do controlador
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    /**
     * Busca um usuário específico pelo seu identificador único.
     *
     * <p><b>Endpoint:</b> {@code GET /api/usuarios/{id}}</p>
     * <p><b>HTTP 200 OK</b> — retorna o usuário encontrado.</p>
     *
     * @param id identificador único do usuário (extraído da URL)
     * @return {@link ResponseEntity} contendo o {@link UsuarioResponseDTO}
     */
    @GetMapping("/{id}") // GET com variável de caminho {id}
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    /**
     * Busca um usuário pelo seu endereço de e-mail.
     *
     * <p><b>Endpoint:</b> {@code GET /api/usuarios/email/{email}}</p>
     * <p><b>HTTP 200 OK</b> — retorna o usuário com o e-mail informado.</p>
     * <p>Útil para autenticação e verificação de existência de conta.</p>
     *
     * @param email endereço de e-mail do usuário (extraído da URL)
     * @return {@link ResponseEntity} contendo o {@link UsuarioResponseDTO}
     */
    @GetMapping("/email/{email}") // Endpoint de busca por e-mail
    public ResponseEntity<UsuarioResponseDTO> buscarPorEmail(@PathVariable String email) {
        return ResponseEntity.ok(usuarioService.buscarPorEmail(email));
    }

    /**
     * Cria um novo usuário no sistema com controle de acesso RBAC.
     *
     * <p><b>Endpoint:</b> {@code POST /api/usuarios}</p>
     *
     * <p><b>Regras de controle de acesso (RBAC) — quem pode criar qual conta:</b></p>
     * <ul>
     *   <li><b>Sabesp não cria conta de ninguém</b> — requisições com
     *       {@code X-User-Role: SABESP} são sempre negadas (<b>HTTP 403</b>).</li>
     *   <li><b>Nenhuma instituição cria conta de cidadão ({@code COMUM})</b> —
     *       cidadãos se auto-cadastram pelo endpoint público
     *       {@code POST /api/auth/registro}. Tentativas por aqui recebem <b>HTTP 403</b>.</li>
     *   <li><b>Somente a Prefeitura cria contas institucionais</b> ({@code SABESP} e
     *       {@code PREFEITURA}) — exige {@code X-User-Role: PREFEITURA}; caso contrário
     *       retorna <b>HTTP 403 Forbidden</b>.</li>
     * </ul>
     *
     * <p><b>HTTP 201 Created</b> — retorna o usuário recém-criado no corpo da resposta.</p>
     * <p><b>HTTP 403 Forbidden</b> — quando a combinação papel-solicitante × tipo-criado
     * viola as regras acima.</p>
     *
     * @param dto      corpo da requisição com os dados do usuário ({@link UsuarioRequestDTO})
     * @param userRole papel do usuário solicitante, enviado no cabeçalho HTTP {@code X-User-Role}
     * @return {@link ResponseEntity} com o {@link UsuarioResponseDTO} criado (201)
     *         ou mensagem de erro (403)
     */
    @PostMapping // HTTP POST — criação de novo recurso
    public ResponseEntity<?> criar(@RequestBody UsuarioRequestDTO dto,
                                                    @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        // ── Regra 1: a Sabesp não pode criar conta de ninguém ──
        // A equipe operacional não administra usuários; qualquer tentativa é negada.
        if ("SABESP".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acesso negado: a Sabesp não pode criar contas de usuário.");
        }

        // ── Regra 2: nenhuma instituição cria conta de cidadão (COMUM) ──
        // Cidadãos se auto-cadastram pelo modal público (POST /api/auth/registro).
        if (dto.getTipoUsuario() == TipoUsuario.COMUM) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acesso negado: contas de cidadão são criadas pelo auto-cadastro público (/api/auth/registro).");
        }

        // ── Regra 3: somente a Prefeitura cria contas institucionais ──
        // Impede que cidadãos ou outras partes elevem privilégios criando
        // contas administrativas (SABESP/PREFEITURA) sem autorização.
        if (!"PREFEITURA".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acesso negado: apenas a Prefeitura pode criar contas de Sabesp e Prefeitura.");
        }

        // Delega a criação ao serviço de negócios
        UsuarioResponseDTO criado = usuarioService.criar(dto);

        // Retorna HTTP 201 Created com o recurso criado no corpo
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    /**
     * Atualiza um usuário existente pelo seu identificador.
     *
     * <p><b>Endpoint:</b> {@code PUT /api/usuarios/{id}}</p>
     * <p><b>HTTP 200 OK</b> — retorna o usuário atualizado.</p>
     *
     * @param id  identificador do usuário a ser atualizado
     * @param dto corpo da requisição com os dados atualizados ({@link UsuarioRequestDTO})
     * @return {@link ResponseEntity} com o {@link UsuarioResponseDTO} atualizado
     */
    @PutMapping("/{id}") // HTTP PUT — atualização completa do recurso
    public ResponseEntity<UsuarioResponseDTO> atualizar(@PathVariable Long id,
                                                         @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.atualizar(id, dto));
    }

    /**
     * Remove um usuário do sistema pelo seu identificador.
     *
     * <p><b>Endpoint:</b> {@code DELETE /api/usuarios/{id}}</p>
     * <p><b>HTTP 204 No Content</b> — indica remoção bem-sucedida, sem corpo na resposta.</p>
     *
     * @param id identificador do usuário a ser removido
     * @return {@link ResponseEntity} vazio com status 204
     */
    @DeleteMapping("/{id}") // HTTP DELETE — remoção do recurso
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        // Delega a exclusão ao serviço
        usuarioService.deletar(id);
        // HTTP 204 No Content — sucesso sem corpo de resposta
        return ResponseEntity.noContent().build();
    }
}
