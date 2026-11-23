package br.com.Sabesp.controller;

import br.com.Sabesp.dto.EnderecoRequestDTO;
import br.com.Sabesp.dto.EnderecoResponseDTO;
import br.com.Sabesp.service.EnderecoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST responsável pelo CRUD de Endereços.
 *
 * <p>Base path: {@code /api/enderecos}</p>
 *
 * <p>Endereços são usados para localizar geograficamente os chamados e demais
 * entidades do sistema. Além do CRUD básico, este controlador oferece buscas
 * por bairro e por cidade, facilitando a filtragem de registros por localidade.</p>
 *
 * <p>A injeção de dependência do {@link EnderecoService} é realizada via
 * construtor gerado pelo Lombok ({@code @RequiredArgsConstructor}).</p>
 */
@RestController // Marca como controlador REST — respostas serializadas em JSON automaticamente
@RequestMapping("/api/enderecos") // Prefixo de URL comum a todos os endpoints deste controlador
@RequiredArgsConstructor // Lombok: gera construtor com todos os campos final (injeção de dependência)
public class EnderecoController {

    /** Serviço de negócios para operações com Endereço. Injetado via construtor pelo Lombok. */
    private final EnderecoService enderecoService;

    /**
     * Lista todos os endereços cadastrados no sistema.
     *
     * <p><b>Endpoint:</b> {@code GET /api/enderecos}</p>
     * <p><b>HTTP 200 OK</b> — retorna a lista completa de endereços.</p>
     *
     * @return {@link ResponseEntity} contendo a lista de {@link EnderecoResponseDTO}
     */
    @GetMapping // HTTP GET na raiz do controlador
    public ResponseEntity<List<EnderecoResponseDTO>> listarTodos() {
        // Delega ao serviço e envolve o resultado em ResponseEntity com status 200
        return ResponseEntity.ok(enderecoService.listarTodos());
    }

    /**
     * Busca um endereço específico pelo seu identificador único.
     *
     * <p><b>Endpoint:</b> {@code GET /api/enderecos/{id}}</p>
     * <p><b>HTTP 200 OK</b> — retorna o endereço encontrado.</p>
     *
     * @param id identificador único do endereço (extraído da URL via {@code @PathVariable})
     * @return {@link ResponseEntity} contendo o {@link EnderecoResponseDTO}
     */
    @GetMapping("/{id}") // GET com variável de caminho {id}
    public ResponseEntity<EnderecoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(enderecoService.buscarPorId(id));
    }

    /**
     * Busca endereços filtrando pelo nome do bairro.
     *
     * <p><b>Endpoint:</b> {@code GET /api/enderecos/bairro/{bairro}}</p>
     * <p><b>HTTP 200 OK</b> — retorna todos os endereços do bairro informado.</p>
     *
     * @param bairro nome do bairro para filtrar (extraído da URL)
     * @return {@link ResponseEntity} contendo a lista filtrada de {@link EnderecoResponseDTO}
     */
    @GetMapping("/bairro/{bairro}") // Endpoint de busca por bairro
    public ResponseEntity<List<EnderecoResponseDTO>> buscarPorBairro(@PathVariable String bairro) {
        return ResponseEntity.ok(enderecoService.buscarPorBairro(bairro));
    }

    /**
     * Busca endereços filtrando pelo nome da cidade.
     *
     * <p><b>Endpoint:</b> {@code GET /api/enderecos/cidade/{cidade}}</p>
     * <p><b>HTTP 200 OK</b> — retorna todos os endereços da cidade informada.</p>
     *
     * @param cidade nome da cidade para filtrar (extraído da URL)
     * @return {@link ResponseEntity} contendo a lista filtrada de {@link EnderecoResponseDTO}
     */
    @GetMapping("/cidade/{cidade}") // Endpoint de busca por cidade
    public ResponseEntity<List<EnderecoResponseDTO>> buscarPorCidade(@PathVariable String cidade) {
        return ResponseEntity.ok(enderecoService.buscarPorCidade(cidade));
    }

    /**
     * Cria um novo endereço no sistema.
     *
     * <p><b>Endpoint:</b> {@code POST /api/enderecos}</p>
     * <p><b>HTTP 201 Created</b> — retorna o endereço recém-criado no corpo da resposta.</p>
     *
     * @param dto corpo da requisição com os dados do endereço ({@link EnderecoRequestDTO})
     * @return {@link ResponseEntity} com o {@link EnderecoResponseDTO} criado e status 201
     */
    @PostMapping // HTTP POST na raiz — criação de novo recurso
    public ResponseEntity<EnderecoResponseDTO> criar(@RequestBody EnderecoRequestDTO dto) {
        // Delega a criação ao serviço de negócios
        EnderecoResponseDTO criado = enderecoService.criar(dto);
        // Retorna HTTP 201 Created com o recurso criado no corpo
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    /**
     * Atualiza um endereço existente pelo seu identificador.
     *
     * <p><b>Endpoint:</b> {@code PUT /api/enderecos/{id}}</p>
     * <p><b>HTTP 200 OK</b> — retorna o endereço atualizado.</p>
     *
     * @param id  identificador do endereço a ser atualizado
     * @param dto corpo da requisição com os dados atualizados ({@link EnderecoRequestDTO})
     * @return {@link ResponseEntity} com o {@link EnderecoResponseDTO} atualizado
     */
    @PutMapping("/{id}") // HTTP PUT — atualização completa do recurso identificado por {id}
    public ResponseEntity<EnderecoResponseDTO> atualizar(@PathVariable Long id,
                                                          @RequestBody EnderecoRequestDTO dto) {
        return ResponseEntity.ok(enderecoService.atualizar(id, dto));
    }

    /**
     * Remove um endereço do sistema pelo seu identificador.
     *
     * <p><b>Endpoint:</b> {@code DELETE /api/enderecos/{id}}</p>
     * <p><b>HTTP 204 No Content</b> — indica remoção bem-sucedida, sem corpo na resposta.</p>
     *
     * @param id identificador do endereço a ser removido
     * @return {@link ResponseEntity} vazio com status 204
     */
    @DeleteMapping("/{id}") // HTTP DELETE — remoção do recurso
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        // Delega a exclusão ao serviço
        enderecoService.deletar(id);
        // HTTP 204 No Content — sucesso sem corpo de resposta
        return ResponseEntity.noContent().build();
    }
}
