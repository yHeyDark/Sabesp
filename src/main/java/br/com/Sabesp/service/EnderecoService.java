package br.com.Sabesp.service;

import br.com.Sabesp.dto.EnderecoRequestDTO;
import br.com.Sabesp.dto.EnderecoResponseDTO;
import br.com.Sabesp.exception.ResourceNotFoundException;
import br.com.Sabesp.model.Endereco;
import br.com.Sabesp.repository.EnderecoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnderecoService {

    private final EnderecoRepository enderecoRepository;

    @Transactional(readOnly = true)
    public List<EnderecoResponseDTO> listarTodos() {
        return enderecoRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EnderecoResponseDTO buscarPorId(Long id) {
        Endereco endereco = enderecoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço", id));
        return toResponseDTO(endereco);
    }

    @Transactional(readOnly = true)
    public List<EnderecoResponseDTO> buscarPorBairro(String bairro) {
        return enderecoRepository.findByBairro(bairro)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EnderecoResponseDTO> buscarPorCidade(String cidade) {
        return enderecoRepository.findByCidade(cidade)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public EnderecoResponseDTO criar(EnderecoRequestDTO dto) {
        Endereco endereco = new Endereco();
        mapDtoToEntity(dto, endereco);
        Endereco salvo = enderecoRepository.save(endereco);
        return toResponseDTO(salvo);
    }

    @Transactional
    public EnderecoResponseDTO atualizar(Long id, EnderecoRequestDTO dto) {
        Endereco endereco = enderecoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço", id));
        mapDtoToEntity(dto, endereco);
        Endereco atualizado = enderecoRepository.save(endereco);
        return toResponseDTO(atualizado);
    }

    @Transactional
    public void deletar(Long id) {
        if (!enderecoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Endereço", id);
        }
        enderecoRepository.deleteById(id);
    }

    // ── Conversão ──────────────────────────────────────────

    private void mapDtoToEntity(EnderecoRequestDTO dto, Endereco endereco) {
        endereco.setLogradouro(dto.getLogradouro());
        endereco.setNumero(dto.getNumero());
        endereco.setComplemento(dto.getComplemento());
        endereco.setBairro(dto.getBairro());
        endereco.setCidade(dto.getCidade());
        endereco.setEstado(dto.getEstado());
        endereco.setCep(dto.getCep());
    }

    public EnderecoResponseDTO toResponseDTO(Endereco endereco) {
        EnderecoResponseDTO dto = new EnderecoResponseDTO();
        dto.setIdEndereco(endereco.getIdEndereco());
        dto.setLogradouro(endereco.getLogradouro());
        dto.setNumero(endereco.getNumero());
        dto.setComplemento(endereco.getComplemento());
        dto.setBairro(endereco.getBairro());
        dto.setCidade(endereco.getCidade());
        dto.setEstado(endereco.getEstado());
        dto.setCep(endereco.getCep());
        return dto;
    }
}

