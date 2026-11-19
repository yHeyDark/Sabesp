package br.com.Sabesp.service;

import br.com.Sabesp.dto.StatusChamadoDTO;
import br.com.Sabesp.exception.ResourceNotFoundException;
import br.com.Sabesp.model.StatusChamado;
import br.com.Sabesp.repository.StatusChamadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatusChamadoService {

    private final StatusChamadoRepository statusChamadoRepository;

    @Transactional(readOnly = true)
    public List<StatusChamadoDTO> listarTodos() {
        return statusChamadoRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StatusChamadoDTO buscarPorId(Long id) {
        StatusChamado sc = statusChamadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Status do Chamado", id));
        return toDTO(sc);
    }

    @Transactional
    public StatusChamadoDTO criar(StatusChamadoDTO dto) {
        StatusChamado sc = new StatusChamado();
        sc.setDescricao(dto.getDescricao());
        StatusChamado salvo = statusChamadoRepository.save(sc);
        return toDTO(salvo);
    }

    @Transactional
    public StatusChamadoDTO atualizar(Long id, StatusChamadoDTO dto) {
        StatusChamado sc = statusChamadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Status do Chamado", id));
        sc.setDescricao(dto.getDescricao());
        StatusChamado atualizado = statusChamadoRepository.save(sc);
        return toDTO(atualizado);
    }

    @Transactional
    public void deletar(Long id) {
        if (!statusChamadoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Status do Chamado", id);
        }
        statusChamadoRepository.deleteById(id);
    }

    // ── Conversão ──────────────────────────────────────────

    public StatusChamadoDTO toDTO(StatusChamado sc) {
        StatusChamadoDTO dto = new StatusChamadoDTO();
        dto.setIdStatus(sc.getIdStatus());
        dto.setDescricao(sc.getDescricao());
        return dto;
    }
}

