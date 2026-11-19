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

@RestController
@RequestMapping("/api/chamados")
@RequiredArgsConstructor
public class ChamadoController {

    private final ChamadoService chamadoService;

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

    @GetMapping("/meus")
    public ResponseEntity<List<ChamadoResponseDTO>> listarMeusChamados(
            @RequestHeader(value = "X-User-Id", required = false) Long idUsuario) {
        if (idUsuario == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(chamadoService.buscarPorUsuario(idUsuario));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChamadoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(chamadoService.buscarPorId(id));
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<ChamadoResponseDTO>> buscarPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(chamadoService.buscarPorUsuario(idUsuario));
    }

    @GetMapping("/status/{idStatus}")
    public ResponseEntity<List<ChamadoResponseDTO>> buscarPorStatus(@PathVariable Long idStatus) {
        return ResponseEntity.ok(chamadoService.buscarPorStatus(idStatus));
    }

    @GetMapping("/bairro/{bairro}")
    public ResponseEntity<List<ChamadoResponseDTO>> buscarPorBairro(@PathVariable String bairro) {
        return ResponseEntity.ok(chamadoService.buscarPorBairro(bairro));
    }

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


