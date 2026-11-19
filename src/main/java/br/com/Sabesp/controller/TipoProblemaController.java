package br.com.Sabesp.controller;

import br.com.Sabesp.dto.TipoProblemaDTO;
import br.com.Sabesp.service.TipoProblemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-problema")
@RequiredArgsConstructor
public class TipoProblemaController {

    private final TipoProblemaService tipoProblemaService;

    @GetMapping
    public ResponseEntity<List<TipoProblemaDTO>> listarTodos() {
        return ResponseEntity.ok(tipoProblemaService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoProblemaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tipoProblemaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<TipoProblemaDTO> criar(@RequestBody TipoProblemaDTO dto) {
        TipoProblemaDTO criado = tipoProblemaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoProblemaDTO> atualizar(@PathVariable Long id,
                                                      @RequestBody TipoProblemaDTO dto) {
        return ResponseEntity.ok(tipoProblemaService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        tipoProblemaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

