package br.com.Sabesp.controller;

import br.com.Sabesp.dto.FiltroPesquisaRequestDTO;
import br.com.Sabesp.dto.FiltroPesquisaResponseDTO;
import br.com.Sabesp.service.FiltroPesquisaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST responsável pelo CRUD de Filtros de Pesquisa.
 *
 * <p>Base path: {@code /api/filtros-pesquisa}</p>
 *
 * <p>Filtros de pesquisa são critérios salvos vinculados a uma Prefeitura,
 * permitindo que os usuários reutilizem buscas frequentes no sistema de chamados.
 * Além do CRUD padrão, há um endpoint para buscar filtros por Prefeitura.</p>
 *
 * <p>A injeção de dependência do {@link FiltroPesquisaService} é feita via
 * construtor gerado pelo Lombok ({@code @RequiredArgsConstructor}).</p>
 */
@RestController // Marca como controlador REST — respostas JSON automáticas
@RequestMapping("/api/filtros-pesquisa") // Prefixo de URL para todos os endpoints deste controlador
@RequiredArgsConstructor // Lombok: gera construtor com os campos final (injeção de dependência)
public class FiltroPesquisaController {

    /** Serviço de negócios para operações de Filtro de Pesquisa. Injetado via construtor. */
    private final FiltroPesquisaService filtroPesquisaService;

    /**
     * Lista todos os filtros de pesquisa cadastrados no sistema.
     *
     * <p><b>Endpoint:</b> {@code GET /api/filtros-pesquisa}</p>
     * <p><b>HTTP 200 OK</b> — retorna a lista completa de filtros de pesquisa.</p>
     *
     * @return {@link ResponseEntity} contendo a lista de {@link FiltroPesquisaResponseDTO}
     */
    @GetMapping // HTTP GET na raiz do controlador
    public ResponseEntity<List<FiltroPesquisaResponseDTO>> listarTodos() {
        return ResponseEntity.ok(filtroPesquisaService.listarTodos());
    }

    /**
     * Busca um filtro de pesquisa específico pelo seu identificador único.
     *
     * <p><b>Endpoint:</b> {@code GET /api/filtros-pesquisa/{id}}</p>
     * <p><b>HTTP 200 OK</b> — retorna o filtro encontrado.</p>
     *
     * @param id identificador único do filtro (extraído da URL)
     * @return {@link ResponseEntity} contendo o {@link FiltroPesquisaResponseDTO}
     */
    @GetMapping("/{id}") // GET com variável de caminho {id}
    public ResponseEntity<FiltroPesquisaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(filtroPesquisaService.buscarPorId(id));
    }

    /**
     * Busca todos os filtros de pesquisa vinculados a uma Prefeitura específica.
     *
     * <p><b>Endpoint:</b> {@code GET /api/filtros-pesquisa/prefeitura/{idPrefeitura}}</p>
     * <p><b>HTTP 200 OK</b> — retorna a lista de filtros associados à Prefeitura informada.</p>
     * <p>Permite que a interface carregue apenas os filtros relevantes para a
     * Prefeitura logada, evitando exposição de dados de outras Prefeituras.</p>
     *
     * @param idPrefeitura identificador da Prefeitura cujos filtros serão retornados
     * @return {@link ResponseEntity} contendo a lista filtrada de {@link FiltroPesquisaResponseDTO}
     */
    @GetMapping("/prefeitura/{idPrefeitura}") // Endpoint para filtrar por Prefeitura
    public ResponseEntity<List<FiltroPesquisaResponseDTO>> buscarPorPrefeitura(@PathVariable Long idPrefeitura) {
        return ResponseEntity.ok(filtroPesquisaService.buscarPorPrefeitura(idPrefeitura));
    }

    /**
     * Cria um novo filtro de pesquisa no sistema.
     *
     * <p><b>Endpoint:</b> {@code POST /api/filtros-pesquisa}</p>
     * <p><b>HTTP 201 Created</b> — retorna o filtro recém-criado no corpo da resposta.</p>
     *
     * @param dto corpo da requisição com os dados do filtro ({@link FiltroPesquisaRequestDTO})
     * @return {@link ResponseEntity} com o {@link FiltroPesquisaResponseDTO} criado e status 201
     */
    @PostMapping // HTTP POST — criação de novo recurso
    public ResponseEntity<FiltroPesquisaResponseDTO> criar(@RequestBody FiltroPesquisaRequestDTO dto) {
        // Delega a criação ao serviço de negócios
        FiltroPesquisaResponseDTO criado = filtroPesquisaService.criar(dto);
        // Retorna HTTP 201 Created com o recurso no corpo
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    /**
     * Atualiza um filtro de pesquisa existente pelo seu identificador.
     *
     * <p><b>Endpoint:</b> {@code PUT /api/filtros-pesquisa/{id}}</p>
     * <p><b>HTTP 200 OK</b> — retorna o filtro atualizado.</p>
     *
     * @param id  identificador do filtro a ser atualizado
     * @param dto corpo da requisição com os dados atualizados ({@link FiltroPesquisaRequestDTO})
     * @return {@link ResponseEntity} com o {@link FiltroPesquisaResponseDTO} atualizado
     */
    @PutMapping("/{id}") // HTTP PUT — atualização completa do recurso
    public ResponseEntity<FiltroPesquisaResponseDTO> atualizar(@PathVariable Long id,
                                                                @RequestBody FiltroPesquisaRequestDTO dto) {
        return ResponseEntity.ok(filtroPesquisaService.atualizar(id, dto));
    }

    /**
     * Remove um filtro de pesquisa do sistema pelo seu identificador.
     *
     * <p><b>Endpoint:</b> {@code DELETE /api/filtros-pesquisa/{id}}</p>
     * <p><b>HTTP 204 No Content</b> — indica remoção bem-sucedida, sem corpo na resposta.</p>
     *
     * @param id identificador do filtro a ser removido
     * @return {@link ResponseEntity} vazio com status 204
     */
    @DeleteMapping("/{id}") // HTTP DELETE — remoção do recurso
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        // Delega a exclusão ao serviço
        filtroPesquisaService.deletar(id);
        // HTTP 204 No Content — sucesso sem corpo de resposta
        return ResponseEntity.noContent().build();
    }
}
