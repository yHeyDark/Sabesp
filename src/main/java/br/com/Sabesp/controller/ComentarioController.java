package br.com.Sabesp.controller;

import br.com.Sabesp.dto.ComentarioRequestDTO;
import br.com.Sabesp.dto.ComentarioResponseDTO;
import br.com.Sabesp.model.enums.TipoUsuario;
import br.com.Sabesp.service.ComentarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST responsável pelo CRUD de Comentários (acompanhamentos) dos chamados.
 *
 * <p>Base path: {@code /api/comentarios}</p>
 *
 * <p>Comentários representam mensagens de acompanhamento associadas a um chamado.
 * Qualquer perfil autenticado (COMUM, SABESP ou PREFEITURA) pode criar um comentário,
 * desde que envie o cabeçalho {@code X-User-Role} com um valor válido.</p>
 *
 * <p>A anotação {@code @RequiredArgsConstructor} do Lombok gera automaticamente
 * o construtor com os campos {@code final}, possibilitando a injeção de dependência
 * do {@link ComentarioService}.</p>
 */
@RestController // Marca esta classe como controlador REST; cada método retorna JSON por padrão
@RequestMapping("/api/comentarios") // Define o prefixo de URL para todos os endpoints deste controlador
@RequiredArgsConstructor // Lombok: gera construtor com os campos final (injeção de dependência)
public class ComentarioController {

    /** Serviço de negócios para operações de Comentário. Injetado via construtor pelo Lombok. */
    private final ComentarioService comentarioService;

    /**
     * Lista todos os comentários cadastrados no sistema.
     *
     * <p><b>Endpoint:</b> {@code GET /api/comentarios}</p>
     * <p><b>HTTP 200 OK</b> — retorna a lista completa de comentários no corpo da resposta.</p>
     *
     * @return {@link ResponseEntity} contendo a lista de {@link ComentarioResponseDTO}
     */
    @GetMapping // Mapeia requisições HTTP GET na raiz do controlador (/api/comentarios)
    public ResponseEntity<List<ComentarioResponseDTO>> listarTodos() {
        // Delega ao serviço e retorna HTTP 200 com a lista no corpo
        return ResponseEntity.ok(comentarioService.listarTodos());
    }

    /**
     * Busca um comentário específico pelo seu identificador único.
     *
     * <p><b>Endpoint:</b> {@code GET /api/comentarios/{id}}</p>
     * <p><b>HTTP 200 OK</b> — retorna o comentário encontrado.</p>
     * <p>Caso o ID não exista, o serviço lançará uma exceção tratada globalmente.</p>
     *
     * @param id identificador único do comentário (extraído da URL)
     * @return {@link ResponseEntity} contendo o {@link ComentarioResponseDTO}
     */
    @GetMapping("/{id}") // Mapeia GET com variável de caminho {id}
    public ResponseEntity<ComentarioResponseDTO> buscarPorId(@PathVariable Long id) {
        // @PathVariable extrai o valor {id} da URL e o converte para Long
        return ResponseEntity.ok(comentarioService.buscarPorId(id));
    }

    /**
     * Busca todos os comentários vinculados a um chamado específico.
     *
     * <p><b>Endpoint:</b> {@code GET /api/comentarios/chamado/{idChamado}}</p>
     * <p><b>HTTP 200 OK</b> — retorna a lista de comentários do chamado informado.</p>
     *
     * @param idChamado identificador do chamado cujos comentários serão retornados
     * @return {@link ResponseEntity} contendo a lista de {@link ComentarioResponseDTO}
     */
    @GetMapping("/chamado/{idChamado}") // Endpoint para filtrar comentários por chamado
    public ResponseEntity<List<ComentarioResponseDTO>> buscarPorChamado(@PathVariable Long idChamado) {
        return ResponseEntity.ok(comentarioService.buscarPorChamado(idChamado));
    }

    /**
     * Cria um novo comentário/acompanhamento para um chamado.
     *
     * <p><b>Endpoint:</b> {@code POST /api/comentarios}</p>
     * <p><b>Controle de acesso (RBAC):</b> o cabeçalho {@code X-User-Role} é obrigatório
     * e deve conter um dos valores: {@code COMUM}, {@code SABESP} ou {@code PREFEITURA}.
     * Caso contrário, a requisição é rejeitada com <b>HTTP 403 Forbidden</b>.</p>
     * <p><b>HTTP 201 Created</b> — retorna o comentário recém-criado no corpo da resposta.</p>
     *
     * @param dto    corpo da requisição com os dados do comentário ({@link ComentarioRequestDTO})
     * @param userRole papel do usuário enviado no cabeçalho HTTP {@code X-User-Role}
     * @return {@link ResponseEntity} com o {@link ComentarioResponseDTO} criado ou 403 se não autorizado
     */
    @PostMapping // Mapeia requisições HTTP POST na raiz (/api/comentarios)
    public ResponseEntity<ComentarioResponseDTO> criar(@RequestBody ComentarioRequestDTO dto,
                                                       @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        // --- Verificação RBAC ---
        // Qualquer perfil autenticado pode registrar acompanhamento/comentário.
        // Se o cabeçalho X-User-Role estiver ausente ou não corresponder a nenhum dos
        // três perfis válidos (COMUM, SABESP, PREFEITURA), nega o acesso com HTTP 403.
        if (userRole == null || (!userRole.equals("COMUM") && !userRole.equals("SABESP") && !userRole.equals("PREFEITURA"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Delega a criação ao serviço de negócios
        ComentarioResponseDTO criado = comentarioService.criar(dto);

        // Retorna HTTP 201 Created com o recurso criado no corpo
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    /**
     * Atualiza um comentário existente pelo seu identificador.
     *
     * <p><b>Endpoint:</b> {@code PUT /api/comentarios/{id}}</p>
     * <p><b>HTTP 200 OK</b> — retorna o comentário atualizado no corpo da resposta.</p>
     * <p>O corpo da requisição deve conter todos os campos atualizáveis do comentário.</p>
     *
     * @param id  identificador do comentário a ser atualizado
     * @param dto corpo da requisição com os dados atualizados ({@link ComentarioRequestDTO})
     * @return {@link ResponseEntity} com o {@link ComentarioResponseDTO} atualizado
     */
    @PutMapping("/{id}") // Mapeia requisições HTTP PUT para atualização completa do recurso
    public ResponseEntity<ComentarioResponseDTO> atualizar(@PathVariable Long id,
                                                            @RequestBody ComentarioRequestDTO dto) {
        // Delega a atualização ao serviço e retorna HTTP 200
        return ResponseEntity.ok(comentarioService.atualizar(id, dto));
    }

    /**
     * Remove um comentário do sistema pelo seu identificador.
     *
     * <p><b>Endpoint:</b> {@code DELETE /api/comentarios/{id}}</p>
     * <p><b>HTTP 204 No Content</b> — indica que o recurso foi removido com sucesso,
     * sem retornar corpo na resposta.</p>
     *
     * @param id identificador do comentário a ser removido
     * @return {@link ResponseEntity} vazio com status 204
     */
    @DeleteMapping("/{id}") // Mapeia requisições HTTP DELETE para remoção do recurso
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        // Delega a exclusão ao serviço
        comentarioService.deletar(id);
        // Retorna HTTP 204 No Content — operação bem-sucedida sem corpo de resposta
        return ResponseEntity.noContent().build();
    }
}
