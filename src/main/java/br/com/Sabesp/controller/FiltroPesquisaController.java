package br.com.Sabesp.controller;

import br.com.Sabesp.dto.FiltroPesquisaRequestDTO;
import br.com.Sabesp.dto.FiltroPesquisaResponseDTO;
import br.com.Sabesp.service.FiltroPesquisaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/filtros-pesquisa")
@RequiredArgsConstructor
public class FiltroPesquisaController {

    private final FiltroPesquisaService filtroPesquisaService;

    @GetMapping
    public ResponseEntity<List<FiltroPesquisaResponseDTO>> listarTodos() {
        return ResponseEntity.ok(filtroPesquisaService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FiltroPesquisaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(filtroPesquisaService.buscarPorId(id));
    }

    @GetMapping("/prefeitura/{idPrefeitura}")
    public ResponseEntity<List<FiltroPesquisaResponseDTO>> buscarPorPrefeitura(@PathVariable Long idPrefeitura) {
        return ResponseEntity.ok(filtroPesquisaService.buscarPorPrefeitura(idPrefeitura));
    }

    @PostMapping
    public ResponseEntity<FiltroPesquisaResponseDTO> criar(@RequestBody FiltroPesquisaRequestDTO dto) {
        FiltroPesquisaResponseDTO criado = filtroPesquisaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FiltroPesquisaResponseDTO> atualizar(@PathVariable Long id,
                                                                @RequestBody FiltroPesquisaRequestDTO dto) {
        return ResponseEntity.ok(filtroPesquisaService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        filtroPesquisaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

