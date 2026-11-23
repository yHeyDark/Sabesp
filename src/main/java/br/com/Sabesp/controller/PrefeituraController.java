package br.com.Sabesp.controller;

import br.com.Sabesp.dto.PrefeituraRequestDTO;
import br.com.Sabesp.dto.PrefeituraResponseDTO;
import br.com.Sabesp.service.PrefeituraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST responsável pelo CRUD de Prefeituras.
 *
 * <p>Base path: {@code /api/prefeituras}</p>
 *
 * <p>Prefeituras representam as entidades municipais que utilizam o sistema para
 * gerenciar chamados de infraestrutura hídrica junto à Sabesp. Cada Prefeitura
 * pode ter usuários, filtros de pesquisa e chamados associados.</p>
 *
 * <p>A injeção de dependência do {@link PrefeituraService} é feita via
 * construtor gerado pelo Lombok ({@code @RequiredArgsConstructor}).</p>
 */
@RestController // Controlador REST — respostas JSON automáticas via Jackson
@RequestMapping("/api/prefeituras") // Prefixo de URL para todos os endpoints deste controlador
@RequiredArgsConstructor // Lombok: gera construtor com os campos final (injeção de dependência)
public class PrefeituraController {

    /** Serviço de negócios para operações de Prefeitura. Injetado via construtor. */
    private final PrefeituraService prefeituraService;

    /**
     * Lista todas as prefeituras cadastradas no sistema.
     *
     * <p><b>Endpoint:</b> {@code GET /api/prefeituras}</p>
     * <p><b>HTTP 200 OK</b> — retorna a lista completa de prefeituras.</p>
     *
     * @return {@link ResponseEntity} contendo a lista de {@link PrefeituraResponseDTO}
     */
    @GetMapping // HTTP GET na raiz do controlador
    public ResponseEntity<List<PrefeituraResponseDTO>> listarTodos() {
        return ResponseEntity.ok(prefeituraService.listarTodos());
    }

    /**
     * Busca uma prefeitura específica pelo seu identificador único.
     *
     * <p><b>Endpoint:</b> {@code GET /api/prefeituras/{id}}</p>
     * <p><b>HTTP 200 OK</b> — retorna a prefeitura encontrada.</p>
     *
     * @param id identificador único da prefeitura (extraído da URL)
     * @return {@link ResponseEntity} contendo o {@link PrefeituraResponseDTO}
     */
    @GetMapping("/{id}") // GET com variável de caminho {id}
    public ResponseEntity<PrefeituraResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(prefeituraService.buscarPorId(id));
    }

    /**
     * Cria uma nova prefeitura no sistema.
     *
     * <p><b>Endpoint:</b> {@code POST /api/prefeituras}</p>
     * <p><b>HTTP 201 Created</b> — retorna a prefeitura recém-criada no corpo da resposta.</p>
     *
     * @param dto corpo da requisição com os dados da prefeitura ({@link PrefeituraRequestDTO})
     * @return {@link ResponseEntity} com o {@link PrefeituraResponseDTO} criado e status 201
     */
    @PostMapping // HTTP POST — criação de novo recurso
    public ResponseEntity<PrefeituraResponseDTO> criar(@RequestBody PrefeituraRequestDTO dto) {
        // Delega a criação ao serviço de negócios
        PrefeituraResponseDTO criado = prefeituraService.criar(dto);
        // Retorna HTTP 201 Created com o recurso criado no corpo
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    /**
     * Atualiza uma prefeitura existente pelo seu identificador.
     *
     * <p><b>Endpoint:</b> {@code PUT /api/prefeituras/{id}}</p>
     * <p><b>HTTP 200 OK</b> — retorna a prefeitura atualizada.</p>
     *
     * @param id  identificador da prefeitura a ser atualizada
     * @param dto corpo da requisição com os dados atualizados ({@link PrefeituraRequestDTO})
     * @return {@link ResponseEntity} com o {@link PrefeituraResponseDTO} atualizado
     */
    @PutMapping("/{id}") // HTTP PUT — atualização completa do recurso
    public ResponseEntity<PrefeituraResponseDTO> atualizar(@PathVariable Long id,
                                                            @RequestBody PrefeituraRequestDTO dto) {
        return ResponseEntity.ok(prefeituraService.atualizar(id, dto));
    }

    /**
     * Remove uma prefeitura do sistema pelo seu identificador.
     *
     * <p><b>Endpoint:</b> {@code DELETE /api/prefeituras/{id}}</p>
     * <p><b>HTTP 204 No Content</b> — indica remoção bem-sucedida, sem corpo na resposta.</p>
     *
     * @param id identificador da prefeitura a ser removida
     * @return {@link ResponseEntity} vazio com status 204
     */
    @DeleteMapping("/{id}") // HTTP DELETE — remoção do recurso
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        // Delega a exclusão ao serviço
        prefeituraService.deletar(id);
        // HTTP 204 No Content — sucesso sem corpo de resposta
        return ResponseEntity.noContent().build();
    }
}
