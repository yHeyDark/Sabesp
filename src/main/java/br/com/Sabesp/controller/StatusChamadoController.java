package br.com.Sabesp.controller;

import br.com.Sabesp.dto.StatusChamadoDTO;
import br.com.Sabesp.service.StatusChamadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/status-chamado")
@RequiredArgsConstructor
public class StatusChamadoController {

    private final StatusChamadoService statusChamadoService;

    @GetMapping
    public ResponseEntity<List<StatusChamadoDTO>> listarTodos() {
        return ResponseEntity.ok(statusChamadoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StatusChamadoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(statusChamadoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<StatusChamadoDTO> criar(@RequestBody StatusChamadoDTO dto) {
        StatusChamadoDTO criado = statusChamadoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StatusChamadoDTO> atualizar(@PathVariable Long id,
                                                       @RequestBody StatusChamadoDTO dto) {
        return ResponseEntity.ok(statusChamadoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        statusChamadoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

