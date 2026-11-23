package br.com.Sabesp.controller;

import br.com.Sabesp.dto.TipoProblemaDTO;
import br.com.Sabesp.service.TipoProblemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST responsável pelo CRUD de Tipos de Problema.
 *
 * <p>Base path: {@code /api/tipos-problema}</p>
 *
 * <p>Tipos de problema categorizam os chamados no sistema (ex.: Vazamento, Falta d'Água,
 * Esgoto, Qualidade da Água). Funcionam como dados de referência (tabela auxiliar)
 * utilizados para classificar e filtrar chamados.</p>
 *
 * <p>Utiliza um único DTO ({@link TipoProblemaDTO}) tanto para requisição quanto para
 * resposta, pois a entidade é simples (tipicamente id + descrição).</p>
 *
 * <p>A injeção de dependência do {@link TipoProblemaService} é feita via
 * construtor gerado pelo Lombok ({@code @RequiredArgsConstructor}).</p>
 */
@RestController // Controlador REST — respostas JSON automáticas via Jackson
@RequestMapping("/api/tipos-problema") // Prefixo de URL para todos os endpoints
@RequiredArgsConstructor // Lombok: gera construtor com os campos final (injeção de dependência)
public class TipoProblemaController {

    /** Serviço de negócios para operações de Tipo de Problema. Injetado via construtor. */
    private final TipoProblemaService tipoProblemaService;

    /**
     * Lista todos os tipos de problema cadastrados no sistema.
     *
     * <p><b>Endpoint:</b> {@code GET /api/tipos-problema}</p>
     * <p><b>HTTP 200 OK</b> — retorna a lista completa de tipos de problema.</p>
     * <p>Usado para popular dropdowns/selects de categorização na interface do usuário.</p>
     *
     * @return {@link ResponseEntity} contendo a lista de {@link TipoProblemaDTO}
     */
    @GetMapping // HTTP GET na raiz do controlador
    public ResponseEntity<List<TipoProblemaDTO>> listarTodos() {
        return ResponseEntity.ok(tipoProblemaService.listarTodos());
    }

    /**
     * Busca um tipo de problema específico pelo seu identificador único.
     *
     * <p><b>Endpoint:</b> {@code GET /api/tipos-problema/{id}}</p>
     * <p><b>HTTP 200 OK</b> — retorna o tipo de problema encontrado.</p>
     *
     * @param id identificador único do tipo de problema (extraído da URL)
     * @return {@link ResponseEntity} contendo o {@link TipoProblemaDTO}
     */
    @GetMapping("/{id}") // GET com variável de caminho {id}
    public ResponseEntity<TipoProblemaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tipoProblemaService.buscarPorId(id));
    }

    /**
     * Cria um novo tipo de problema no sistema.
     *
     * <p><b>Endpoint:</b> {@code POST /api/tipos-problema}</p>
     * <p><b>HTTP 201 Created</b> — retorna o tipo recém-criado no corpo da resposta.</p>
     *
     * @param dto corpo da requisição com os dados do tipo ({@link TipoProblemaDTO})
     * @return {@link ResponseEntity} com o {@link TipoProblemaDTO} criado e status 201
     */
    @PostMapping // HTTP POST — criação de novo recurso
    public ResponseEntity<TipoProblemaDTO> criar(@RequestBody TipoProblemaDTO dto) {
        // Delega a criação ao serviço de negócios
        TipoProblemaDTO criado = tipoProblemaService.criar(dto);
        // Retorna HTTP 201 Created com o recurso criado no corpo
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    /**
     * Atualiza um tipo de problema existente pelo seu identificador.
     *
     * <p><b>Endpoint:</b> {@code PUT /api/tipos-problema/{id}}</p>
     * <p><b>HTTP 200 OK</b> — retorna o tipo de problema atualizado.</p>
     *
     * @param id  identificador do tipo a ser atualizado
     * @param dto corpo da requisição com os dados atualizados ({@link TipoProblemaDTO})
     * @return {@link ResponseEntity} com o {@link TipoProblemaDTO} atualizado
     */
    @PutMapping("/{id}") // HTTP PUT — atualização completa do recurso
    public ResponseEntity<TipoProblemaDTO> atualizar(@PathVariable Long id,
                                                      @RequestBody TipoProblemaDTO dto) {
        return ResponseEntity.ok(tipoProblemaService.atualizar(id, dto));
    }

    /**
     * Remove um tipo de problema do sistema pelo seu identificador.
     *
     * <p><b>Endpoint:</b> {@code DELETE /api/tipos-problema/{id}}</p>
     * <p><b>HTTP 204 No Content</b> — indica remoção bem-sucedida, sem corpo na resposta.</p>
     * <p><b>Atenção:</b> remover um tipo de problema pode impactar chamados existentes que
     * o referenciam. Considere verificar dependências antes da exclusão.</p>
     *
     * @param id identificador do tipo a ser removido
     * @return {@link ResponseEntity} vazio com status 204
     */
    @DeleteMapping("/{id}") // HTTP DELETE — remoção do recurso
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        // Delega a exclusão ao serviço
        tipoProblemaService.deletar(id);
        // HTTP 204 No Content — sucesso sem corpo de resposta
        return ResponseEntity.noContent().build();
    }
}
