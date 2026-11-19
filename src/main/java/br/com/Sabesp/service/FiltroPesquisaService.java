package br.com.Sabesp.service;

import br.com.Sabesp.dto.FiltroPesquisaRequestDTO;
import br.com.Sabesp.dto.FiltroPesquisaResponseDTO;
import br.com.Sabesp.exception.ResourceNotFoundException;
import br.com.Sabesp.model.FiltroPesquisa;
import br.com.Sabesp.model.Prefeitura;
import br.com.Sabesp.repository.FiltroPesquisaRepository;
import br.com.Sabesp.repository.PrefeituraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FiltroPesquisaService {

    private final FiltroPesquisaRepository filtroPesquisaRepository;
    private final PrefeituraRepository prefeituraRepository;

    @Transactional(readOnly = true)
    public List<FiltroPesquisaResponseDTO> listarTodos() {
        return filtroPesquisaRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FiltroPesquisaResponseDTO buscarPorId(Long id) {
        FiltroPesquisa filtro = filtroPesquisaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filtro de Pesquisa", id));
        return toResponseDTO(filtro);
    }

    @Transactional(readOnly = true)
    public List<FiltroPesquisaResponseDTO> buscarPorPrefeitura(Long idPrefeitura) {
        return filtroPesquisaRepository.findByPrefeituraIdPrefeitura(idPrefeitura)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public FiltroPesquisaResponseDTO criar(FiltroPesquisaRequestDTO dto) {
        Prefeitura prefeitura = prefeituraRepository.findById(dto.getIdPrefeitura())
                .orElseThrow(() -> new ResourceNotFoundException("Prefeitura", dto.getIdPrefeitura()));

        FiltroPesquisa filtro = new FiltroPesquisa();
        mapDtoToEntity(dto, filtro);
        filtro.setPrefeitura(prefeitura);

        FiltroPesquisa salvo = filtroPesquisaRepository.save(filtro);
        return toResponseDTO(salvo);
    }

    @Transactional
    public FiltroPesquisaResponseDTO atualizar(Long id, FiltroPesquisaRequestDTO dto) {
        FiltroPesquisa filtro = filtroPesquisaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filtro de Pesquisa", id));

        if (dto.getIdPrefeitura() != null) {
            Prefeitura prefeitura = prefeituraRepository.findById(dto.getIdPrefeitura())
                    .orElseThrow(() -> new ResourceNotFoundException("Prefeitura", dto.getIdPrefeitura()));
            filtro.setPrefeitura(prefeitura);
        }

        mapDtoToEntity(dto, filtro);

        FiltroPesquisa atualizado = filtroPesquisaRepository.save(filtro);
        return toResponseDTO(atualizado);
    }

    @Transactional
    public void deletar(Long id) {
        if (!filtroPesquisaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Filtro de Pesquisa", id);
        }
        filtroPesquisaRepository.deleteById(id);
    }

    // ── Conversão ──────────────────────────────────────────

    private void mapDtoToEntity(FiltroPesquisaRequestDTO dto, FiltroPesquisa filtro) {
        filtro.setDataInicio(dto.getDataInicio());
        filtro.setDataFim(dto.getDataFim());
        filtro.setTipoProblema(dto.getTipoProblema());
        filtro.setStatus(dto.getStatus());
        filtro.setBairro(dto.getBairro());
        filtro.setCidade(dto.getCidade());
        filtro.setOutrosCriterios(dto.getOutrosCriterios());
    }

    private FiltroPesquisaResponseDTO toResponseDTO(FiltroPesquisa filtro) {
        FiltroPesquisaResponseDTO dto = new FiltroPesquisaResponseDTO();
        dto.setIdFiltro(filtro.getIdFiltro());
        dto.setDataInicio(filtro.getDataInicio());
        dto.setDataFim(filtro.getDataFim());
        dto.setTipoProblema(filtro.getTipoProblema());
        dto.setStatus(filtro.getStatus());
        dto.setBairro(filtro.getBairro());
        dto.setCidade(filtro.getCidade());
        dto.setOutrosCriterios(filtro.getOutrosCriterios());
        dto.setIdPrefeitura(filtro.getPrefeitura().getIdPrefeitura());
        return dto;
    }
}

