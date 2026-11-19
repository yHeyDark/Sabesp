package br.com.Sabesp.controller;

import br.com.Sabesp.dto.PrefeituraRequestDTO;
import br.com.Sabesp.dto.PrefeituraResponseDTO;
import br.com.Sabesp.service.PrefeituraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prefeituras")
@RequiredArgsConstructor
public class PrefeituraController {

    private final PrefeituraService prefeituraService;

    @GetMapping
    public ResponseEntity<List<PrefeituraResponseDTO>> listarTodos() {
        return ResponseEntity.ok(prefeituraService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrefeituraResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(prefeituraService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<PrefeituraResponseDTO> criar(@RequestBody PrefeituraRequestDTO dto) {
        PrefeituraResponseDTO criado = prefeituraService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PrefeituraResponseDTO> atualizar(@PathVariable Long id,
                                                            @RequestBody PrefeituraRequestDTO dto) {
        return ResponseEntity.ok(prefeituraService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        prefeituraService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

