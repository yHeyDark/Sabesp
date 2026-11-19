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

@RestController
@RequestMapping("/api/comentarios")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService comentarioService;

    @GetMapping
    public ResponseEntity<List<ComentarioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(comentarioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComentarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(comentarioService.buscarPorId(id));
    }

    @GetMapping("/chamado/{idChamado}")
    public ResponseEntity<List<ComentarioResponseDTO>> buscarPorChamado(@PathVariable Long idChamado) {
        return ResponseEntity.ok(comentarioService.buscarPorChamado(idChamado));
    }

    @PostMapping
    public ResponseEntity<ComentarioResponseDTO> criar(@RequestBody ComentarioRequestDTO dto,
                                                       @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        // Qualquer perfil autenticado pode registrar acompanhamento/comentário
        if (userRole == null || (!userRole.equals("COMUM") && !userRole.equals("SABESP") && !userRole.equals("PREFEITURA"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        ComentarioResponseDTO criado = comentarioService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ComentarioResponseDTO> atualizar(@PathVariable Long id,
                                                            @RequestBody ComentarioRequestDTO dto) {
        return ResponseEntity.ok(comentarioService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        comentarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

