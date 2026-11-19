package br.com.Sabesp.service;

import br.com.Sabesp.dto.PrefeituraRequestDTO;
import br.com.Sabesp.dto.PrefeituraResponseDTO;
import br.com.Sabesp.exception.ResourceNotFoundException;
import br.com.Sabesp.model.Prefeitura;
import br.com.Sabesp.repository.PrefeituraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrefeituraService {

    private final PrefeituraRepository prefeituraRepository;

    @Transactional(readOnly = true)
    public List<PrefeituraResponseDTO> listarTodos() {
        return prefeituraRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PrefeituraResponseDTO buscarPorId(Long id) {
        Prefeitura prefeitura = prefeituraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prefeitura", id));
        return toResponseDTO(prefeitura);
    }

    @Transactional
    public PrefeituraResponseDTO criar(PrefeituraRequestDTO dto) {
        Prefeitura prefeitura = new Prefeitura();
        prefeitura.setNome(dto.getNome());
        prefeitura.setEmail(dto.getEmail());
        prefeitura.setSenha(dto.getSenha());
        Prefeitura salva = prefeituraRepository.save(prefeitura);
        return toResponseDTO(salva);
    }

    @Transactional
    public PrefeituraResponseDTO atualizar(Long id, PrefeituraRequestDTO dto) {
        Prefeitura prefeitura = prefeituraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prefeitura", id));

        prefeitura.setNome(dto.getNome());
        prefeitura.setEmail(dto.getEmail());

        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            prefeitura.setSenha(dto.getSenha());
        }

        Prefeitura atualizada = prefeituraRepository.save(prefeitura);
        return toResponseDTO(atualizada);
    }

    @Transactional
    public void deletar(Long id) {
        if (!prefeituraRepository.existsById(id)) {
            throw new ResourceNotFoundException("Prefeitura", id);
        }
        prefeituraRepository.deleteById(id);
    }

    // ── Conversão ──────────────────────────────────────────

    private PrefeituraResponseDTO toResponseDTO(Prefeitura prefeitura) {
        PrefeituraResponseDTO dto = new PrefeituraResponseDTO();
        dto.setIdPrefeitura(prefeitura.getIdPrefeitura());
        dto.setNome(prefeitura.getNome());
        dto.setEmail(prefeitura.getEmail());
        return dto;
    }
}

