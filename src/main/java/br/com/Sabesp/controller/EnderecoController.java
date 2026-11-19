package br.com.Sabesp.controller;

import br.com.Sabesp.dto.EnderecoRequestDTO;
import br.com.Sabesp.dto.EnderecoResponseDTO;
import br.com.Sabesp.service.EnderecoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enderecos")
@RequiredArgsConstructor
public class EnderecoController {

    private final EnderecoService enderecoService;

    @GetMapping
    public ResponseEntity<List<EnderecoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(enderecoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnderecoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(enderecoService.buscarPorId(id));
    }

    @GetMapping("/bairro/{bairro}")
    public ResponseEntity<List<EnderecoResponseDTO>> buscarPorBairro(@PathVariable String bairro) {
        return ResponseEntity.ok(enderecoService.buscarPorBairro(bairro));
    }

    @GetMapping("/cidade/{cidade}")
    public ResponseEntity<List<EnderecoResponseDTO>> buscarPorCidade(@PathVariable String cidade) {
        return ResponseEntity.ok(enderecoService.buscarPorCidade(cidade));
    }

    @PostMapping
    public ResponseEntity<EnderecoResponseDTO> criar(@RequestBody EnderecoRequestDTO dto) {
        EnderecoResponseDTO criado = enderecoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnderecoResponseDTO> atualizar(@PathVariable Long id,
                                                          @RequestBody EnderecoRequestDTO dto) {
        return ResponseEntity.ok(enderecoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        enderecoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

