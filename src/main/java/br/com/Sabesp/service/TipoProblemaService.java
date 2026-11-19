package br.com.Sabesp.service;

import br.com.Sabesp.dto.TipoProblemaDTO;
import br.com.Sabesp.exception.ResourceNotFoundException;
import br.com.Sabesp.model.TipoProblema;
import br.com.Sabesp.repository.TipoProblemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TipoProblemaService {

    private final TipoProblemaRepository tipoProblemaRepository;

    @Transactional(readOnly = true)
    public List<TipoProblemaDTO> listarTodos() {
        return tipoProblemaRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TipoProblemaDTO buscarPorId(Long id) {
        TipoProblema tp = tipoProblemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de Problema", id));
        return toDTO(tp);
    }

    @Transactional
    public TipoProblemaDTO criar(TipoProblemaDTO dto) {
        TipoProblema tp = new TipoProblema();
        tp.setDescricao(dto.getDescricao());
        TipoProblema salvo = tipoProblemaRepository.save(tp);
        return toDTO(salvo);
    }

    @Transactional
    public TipoProblemaDTO atualizar(Long id, TipoProblemaDTO dto) {
        TipoProblema tp = tipoProblemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de Problema", id));
        tp.setDescricao(dto.getDescricao());
        TipoProblema atualizado = tipoProblemaRepository.save(tp);
        return toDTO(atualizado);
    }

    @Transactional
    public void deletar(Long id) {
        if (!tipoProblemaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tipo de Problema", id);
        }
        tipoProblemaRepository.deleteById(id);
    }

    // ── Conversão ──────────────────────────────────────────

    public TipoProblemaDTO toDTO(TipoProblema tp) {
        TipoProblemaDTO dto = new TipoProblemaDTO();
        dto.setIdTipoProblema(tp.getIdTipoProblema());
        dto.setDescricao(tp.getDescricao());
        return dto;
    }
}

