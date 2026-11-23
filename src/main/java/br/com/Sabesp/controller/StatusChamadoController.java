package br.com.Sabesp.controller;

import br.com.Sabesp.dto.StatusChamadoDTO;
import br.com.Sabesp.service.StatusChamadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST responsável pelo CRUD de Status de Chamado.
 *
 * <p>Base path: {@code /api/status-chamado}</p>
 *
 * <p>Status de chamado representa os possíveis estados em que um chamado pode se
 * encontrar durante seu ciclo de vida (ex.: Aberto, Em Andamento, Concluído, Cancelado).
 * Este controlador expõe endpoints para gerenciar esses status como dados de referência
 * (tabela auxiliar) do sistema.</p>
 *
 * <p>Utiliza um único DTO ({@link StatusChamadoDTO}) tanto para requisição quanto para
 * resposta, pois a entidade é simples (tipicamente id + descrição).</p>
 *
 * <p>A injeção de dependência do {@link StatusChamadoService} é feita via
 * construtor gerado pelo Lombok ({@code @RequiredArgsConstructor}).</p>
 */
@RestController // Controlador REST — retorna JSON automaticamente
@RequestMapping("/api/status-chamado") // Prefixo de URL para todos os endpoints
@RequiredArgsConstructor // Lombok: gera construtor com os campos final (injeção de dependência)
public class StatusChamadoController {

    /** Serviço de negócios para operações de Status de Chamado. Injetado via construtor. */
    private final StatusChamadoService statusChamadoService;

    /**
     * Lista todos os status de chamado cadastrados no sistema.
     *
     * <p><b>Endpoint:</b> {@code GET /api/status-chamado}</p>
     * <p><b>HTTP 200 OK</b> — retorna a lista completa de status de chamado.</p>
     * <p>Usado para popular dropdowns/selects na interface do usuário.</p>
     *
     * @return {@link ResponseEntity} contendo a lista de {@link StatusChamadoDTO}
     */
    @GetMapping // HTTP GET na raiz do controlador
    public ResponseEntity<List<StatusChamadoDTO>> listarTodos() {
        return ResponseEntity.ok(statusChamadoService.listarTodos());
    }

    /**
     * Busca um status de chamado específico pelo seu identificador único.
     *
     * <p><b>Endpoint:</b> {@code GET /api/status-chamado/{id}}</p>
     * <p><b>HTTP 200 OK</b> — retorna o status encontrado.</p>
     *
     * @param id identificador único do status (extraído da URL)
     * @return {@link ResponseEntity} contendo o {@link StatusChamadoDTO}
     */
    @GetMapping("/{id}") // GET com variável de caminho {id}
    public ResponseEntity<StatusChamadoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(statusChamadoService.buscarPorId(id));
    }

    /**
     * Cria um novo status de chamado no sistema.
     *
     * <p><b>Endpoint:</b> {@code POST /api/status-chamado}</p>
     * <p><b>HTTP 201 Created</b> — retorna o status recém-criado no corpo da resposta.</p>
     *
     * @param dto corpo da requisição com os dados do status ({@link StatusChamadoDTO})
     * @return {@link ResponseEntity} com o {@link StatusChamadoDTO} criado e status 201
     */
    @PostMapping // HTTP POST — criação de novo recurso
    public ResponseEntity<StatusChamadoDTO> criar(@RequestBody StatusChamadoDTO dto) {
        // Delega a criação ao serviço de negócios
        StatusChamadoDTO criado = statusChamadoService.criar(dto);
        // Retorna HTTP 201 Created com o recurso criado no corpo
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    /**
     * Atualiza um status de chamado existente pelo seu identificador.
     *
     * <p><b>Endpoint:</b> {@code PUT /api/status-chamado/{id}}</p>
     * <p><b>HTTP 200 OK</b> — retorna o status atualizado.</p>
     *
     * @param id  identificador do status a ser atualizado
     * @param dto corpo da requisição com os dados atualizados ({@link StatusChamadoDTO})
     * @return {@link ResponseEntity} com o {@link StatusChamadoDTO} atualizado
     */
    @PutMapping("/{id}") // HTTP PUT — atualização completa do recurso
    public ResponseEntity<StatusChamadoDTO> atualizar(@PathVariable Long id,
                                                       @RequestBody StatusChamadoDTO dto) {
        return ResponseEntity.ok(statusChamadoService.atualizar(id, dto));
    }

    /**
     * Remove um status de chamado do sistema pelo seu identificador.
     *
     * <p><b>Endpoint:</b> {@code DELETE /api/status-chamado/{id}}</p>
     * <p><b>HTTP 204 No Content</b> — indica remoção bem-sucedida, sem corpo na resposta.</p>
     * <p><b>Atenção:</b> remover um status pode impactar chamados existentes que o utilizam.
     * Considere verificar dependências antes da exclusão.</p>
     *
     * @param id identificador do status a ser removido
     * @return {@link ResponseEntity} vazio com status 204
     */
    @DeleteMapping("/{id}") // HTTP DELETE — remoção do recurso
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        // Delega a exclusão ao serviço
        statusChamadoService.deletar(id);
        // HTTP 204 No Content — sucesso sem corpo de resposta
        return ResponseEntity.noContent().build();
    }
}
