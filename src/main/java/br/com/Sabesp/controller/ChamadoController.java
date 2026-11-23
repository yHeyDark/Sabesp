package br.com.Sabesp.controller;

import br.com.Sabesp.dto.ChamadoRequestDTO;
import br.com.Sabesp.dto.ChamadoResponseDTO;
import br.com.Sabesp.model.enums.TipoUsuario;
import br.com.Sabesp.service.ChamadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller principal da API de chamados de esgoto
 * (Secretaria de Meio Ambiente de Santos).
 *
 * <p>Gerencia o ciclo de vida dos chamados: listagem, consulta por diversos
 * critérios, criação, atualização, alteração de status e exclusão.</p>
 *
 * <p><b>Regras de RBAC (controle de acesso por perfil)</b>, aplicadas por meio
 * dos cabeçalhos {@code X-User-Role} e {@code X-User-Id}:</p>
 * <ul>
 *   <li><b>COMUM</b> — cria chamados e vê apenas os próprios chamados;</li>
 *   <li><b>SABESP</b> — altera o status e exclui chamados;</li>
 *   <li><b>PREFEITURA</b> — fiscaliza os chamados (acesso de consulta ampliado).</li>
 * </ul>
 *
 * @author Equipe Sabesp
 */
@RestController
@RequestMapping("/api/chamados")
@RequiredArgsConstructor
public class ChamadoController {

    private final ChamadoService chamadoService;

    /**
     * Lista os chamados do sistema.
     *
     * <p><b>Verbo HTTP:</b> GET
     * <br><b>Rota:</b> {@code /api/chamados}
     * <br><b>Acesso:</b> usuário COMUM (com {@code X-User-Id}) recebe apenas os
     * próprios chamados; demais perfis (SABESP, PREFEITURA) recebem todos.</p>
     *
     * @param userRole perfil do usuário, via cabeçalho {@code X-User-Role} (opcional)
     * @param idUsuario identificador do usuário, via cabeçalho {@code X-User-Id} (opcional)
     * @return {@code 200 OK} com a lista de chamados
     */
    @GetMapping
    public ResponseEntity<List<ChamadoResponseDTO>> listarTodos(
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-User-Id", required = false) Long idUsuario) {
        // Cidadão comum vê apenas seus próprios chamados
        if (TipoUsuario.COMUM.name().equalsIgnoreCase(userRole) && idUsuario != null) {
            return ResponseEntity.ok(chamadoService.buscarPorUsuario(idUsuario));
        }
        return ResponseEntity.ok(chamadoService.listarTodos());
    }

    /**
     * Lista os chamados abertos pelo próprio usuário logado.
     *
     * <p><b>Verbo HTTP:</b> GET
     * <br><b>Rota:</b> {@code /api/chamados/meus}
     * <br><b>Acesso:</b> qualquer perfil autenticado; usa o cabeçalho
     * {@code X-User-Id} para filtrar por usuário.</p>
     *
     * @param idUsuario identificador do usuário, via cabeçalho {@code X-User-Id}
     * @return {@code 200 OK} com a lista de chamados do usuário;
     *         {@code 400 BAD REQUEST} se o cabeçalho {@code X-User-Id} não for informado
     */
    @GetMapping("/meus")
    public ResponseEntity<List<ChamadoResponseDTO>> listarMeusChamados(
            @RequestHeader(value = "X-User-Id", required = false) Long idUsuario) {
        if (idUsuario == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(chamadoService.buscarPorUsuario(idUsuario));
    }

    /**
     * Busca um chamado específico pelo seu identificador.
     *
     * <p><b>Verbo HTTP:</b> GET
     * <br><b>Rota:</b> {@code /api/chamados/{id}}
     * <br><b>Acesso:</b> qualquer perfil autenticado (consulta de acompanhamento).</p>
     *
     * @param id identificador do chamado
     * @return {@code 200 OK} com os dados do chamado;
     *         {@code 404 NOT FOUND} se não existir
     */
    @GetMapping("/{id}")
    public ResponseEntity<ChamadoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(chamadoService.buscarPorId(id));
    }

    /**
     * Lista todos os chamados de um usuário específico.
     *
     * <p><b>Verbo HTTP:</b> GET
     * <br><b>Rota:</b> {@code /api/chamados/usuario/{idUsuario}}
     * <br><b>Acesso:</b> qualquer perfil autenticado (usado principalmente por
     * SABESP e PREFEITURA para fiscalização).</p>
     *
     * @param idUsuario identificador do usuário dono dos chamados
     * @return {@code 200 OK} com a lista de chamados do usuário
     */
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<ChamadoResponseDTO>> buscarPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(chamadoService.buscarPorUsuario(idUsuario));
    }

    /**
     * Lista os chamados que possuem um determinado status.
     *
     * <p><b>Verbo HTTP:</b> GET
     * <br><b>Rota:</b> {@code /api/chamados/status/{idStatus}}
     * <br><b>Acesso:</b> qualquer perfil autenticado; utilizado por SABESP e
     * PREFEITURA para acompanhar a fila de atendimento.</p>
     *
     * @param idStatus identificador do status (ex.: 1 = Aberto)
     * @return {@code 200 OK} com a lista de chamados no status informado
     */
    @GetMapping("/status/{idStatus}")
    public ResponseEntity<List<ChamadoResponseDTO>> buscarPorStatus(@PathVariable Long idStatus) {
        return ResponseEntity.ok(chamadoService.buscarPorStatus(idStatus));
    }

    /**
     * Lista os chamados de um determinado bairro.
     *
     * <p><b>Verbo HTTP:</b> GET
     * <br><b>Rota:</b> {@code /api/chamados/bairro/{bairro}}
     * <br><b>Acesso:</b> qualquer perfil autenticado (apoio à fiscalização da
     * Prefeitura e à distribuição de ocorrências pela SABESP).</p>
     *
     * @param bairro nome do bairro
     * @return {@code 200 OK} com a lista de chamados do bairro
     */
    @GetMapping("/bairro/{bairro}")
    public ResponseEntity<List<ChamadoResponseDTO>> buscarPorBairro(@PathVariable String bairro) {
        return ResponseEntity.ok(chamadoService.buscarPorBairro(bairro));
    }

    /**
     * Cria um novo chamado de esgoto.
     *
     * <p><b>Verbo HTTP:</b> POST
     * <br><b>Rota:</b> {@code /api/chamados}
     * <br><b>Acesso:</b> somente o perfil COMUM (cidadão). Perfis SABESP e
     * PREFEITURA recebem {@code 403 FORBIDDEN}. O dono ({@code X-User-Id}) e o
     * status inicial ("Aberto") são definidos pelo servidor.</p>
     *
     * @param dto dados do chamado a ser criado
     * @param userRole perfil do usuário, via cabeçalho {@code X-User-Role}
     * @param idUsuario identificador do usuário, via cabeçalho {@code X-User-Id}
     * @return {@code 201 CREATED} com o chamado criado;
     *         {@code 403 FORBIDDEN} se o perfil não for COMUM ou o usuário não
     *         estiver identificado
     */
    @PostMapping
    public ResponseEntity<?> criar(@RequestBody ChamadoRequestDTO dto,
                                                    @RequestHeader(value = "X-User-Role", required = false) String userRole,
                                                    @RequestHeader(value = "X-User-Id", required = false) Long idUsuario) {
        // APENAS cidadão comum pode registrar chamados
        if (!TipoUsuario.COMUM.name().equalsIgnoreCase(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acesso negado: apenas cidadãos podem registrar novos chamados.");
        }
        if (idUsuario == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Usuário não identificado.");
        }
        dto.setIdUsuario(idUsuario);
        // Cidadão sempre abre como "Aberto" (id = 1)
        dto.setIdStatus(1L);

        ChamadoResponseDTO criado = chamadoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChamadoResponseDTO> atualizar(@PathVariable Long id,
                                                         @RequestBody ChamadoRequestDTO dto) {
        return ResponseEntity.ok(chamadoService.atualizar(id, dto));
    }

    /**
     * Atualização de status: permitida apenas para SABESP.
     * O frontend envia o cabeçalho X-User-Role com o tipoUsuario do usuário logado.
     */
    @PatchMapping("/{id}/status/{idStatus}")
    public ResponseEntity<?> alterarStatus(@PathVariable Long id,
                                           @PathVariable Long idStatus,
                                           @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        // APENAS Sabesp pode alterar o status operacional
        if (!"SABESP".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acesso negado: apenas técnicos Sabesp podem alterar o status do chamado.");
        }
        return ResponseEntity.ok(chamadoService.alterarStatus(id, idStatus));
    }

    /**
     * Exclusão de chamado: permitida apenas para SABESP.
     * O frontend envia o cabeçalho X-User-Role com o tipoUsuario do usuário logado.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id,
                                     @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (!"SABESP".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acesso negado: apenas técnicos Sabesp podem excluir chamados.");
        }
        chamadoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}


