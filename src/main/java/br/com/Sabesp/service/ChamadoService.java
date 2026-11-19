package br.com.Sabesp.service;

import br.com.Sabesp.dto.ChamadoRequestDTO;
import br.com.Sabesp.dto.ChamadoResponseDTO;
import br.com.Sabesp.exception.ResourceNotFoundException;
import br.com.Sabesp.model.*;
import br.com.Sabesp.model.enums.TipoUsuario;
import br.com.Sabesp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EnderecoRepository enderecoRepository;
    private final TipoProblemaRepository tipoProblemaRepository;
    private final StatusChamadoRepository statusChamadoRepository;

    private final UsuarioService usuarioService;
    private final EnderecoService enderecoService;
    private final TipoProblemaService tipoProblemaService;
    private final StatusChamadoService statusChamadoService;

    @Transactional(readOnly = true)
    public List<ChamadoResponseDTO> listarTodos() {
        return chamadoRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChamadoResponseDTO buscarPorId(Long id) {
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chamado", id));
        return toResponseDTO(chamado);
    }

    @Transactional(readOnly = true)
    public List<ChamadoResponseDTO> buscarPorUsuario(Long idUsuario) {
        return chamadoRepository.findByUsuarioIdUsuario(idUsuario)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChamadoResponseDTO> buscarPorStatus(Long idStatus) {
        return chamadoRepository.findByStatusChamadoIdStatus(idStatus)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChamadoResponseDTO> buscarPorBairro(String bairro) {
        return chamadoRepository.findByEnderecoBairro(bairro)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChamadoResponseDTO criar(ChamadoRequestDTO dto) {
        // Validação: cidadão comum só pode abrir chamado no próprio nome e com status "Aberto"
        if (dto.getIdUsuario() != null) {
            Usuario usuarioSolicitante = usuarioRepository.findById(dto.getIdUsuario())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário", dto.getIdUsuario()));
            if (usuarioSolicitante.getTipoUsuario() == TipoUsuario.COMUM) {
                dto.setIdStatus(1L); // Força status "Aberto" para cidadão
            }
        }

        Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", dto.getIdUsuario()));
        Endereco endereco = enderecoRepository.findById(dto.getIdEndereco())
                .orElseThrow(() -> new ResourceNotFoundException("Endereço", dto.getIdEndereco()));
        TipoProblema tipoProblema = tipoProblemaRepository.findById(dto.getIdTipoProblema())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de Problema", dto.getIdTipoProblema()));
        StatusChamado statusChamado = statusChamadoRepository.findById(dto.getIdStatus())
                .orElseThrow(() -> new ResourceNotFoundException("Status do Chamado", dto.getIdStatus()));

        Chamado chamado = new Chamado();
        chamado.setDataChamado(dto.getDataChamado());
        chamado.setHoraChamado(dto.getHoraChamado());
        chamado.setDescricao(dto.getDescricao());
        chamado.setImagemUrl(dto.getImagemUrl());
        chamado.setUsuario(usuario);
        chamado.setEndereco(endereco);
        chamado.setTipoProblema(tipoProblema);
        chamado.setStatusChamado(statusChamado);

        Chamado salvo = chamadoRepository.save(chamado);
        return toResponseDTO(salvo);
    }

    @Transactional
    public ChamadoResponseDTO atualizar(Long id, ChamadoRequestDTO dto) {
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chamado", id));

        // Endereço, tipo de problema e status podem ser atualizados
        if (dto.getIdEndereco() != null) {
            Endereco endereco = enderecoRepository.findById(dto.getIdEndereco())
                    .orElseThrow(() -> new ResourceNotFoundException("Endereço", dto.getIdEndereco()));
            chamado.setEndereco(endereco);
        }
        if (dto.getIdTipoProblema() != null) {
            TipoProblema tp = tipoProblemaRepository.findById(dto.getIdTipoProblema())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de Problema", dto.getIdTipoProblema()));
            chamado.setTipoProblema(tp);
        }
        if (dto.getIdStatus() != null) {
            StatusChamado sc = statusChamadoRepository.findById(dto.getIdStatus())
                    .orElseThrow(() -> new ResourceNotFoundException("Status do Chamado", dto.getIdStatus()));
            chamado.setStatusChamado(sc);
        }

        // Campos de texto/data/hora só são sobrescritos se vierem preenchidos (evita null acidental)
        if (dto.getDescricao() != null && !dto.getDescricao().isBlank()) {
            chamado.setDescricao(dto.getDescricao());
        }
        if (dto.getDataChamado() != null) {
            chamado.setDataChamado(dto.getDataChamado());
        }
        if (dto.getHoraChamado() != null) {
            chamado.setHoraChamado(dto.getHoraChamado());
        }
        if (dto.getImagemUrl() != null) {
            chamado.setImagemUrl(dto.getImagemUrl());
        }

        Chamado atualizado = chamadoRepository.save(chamado);
        return toResponseDTO(atualizado);
    }

    @Transactional
    public ChamadoResponseDTO alterarStatus(Long idChamado, Long idStatus) {
        Chamado chamado = chamadoRepository.findById(idChamado)
                .orElseThrow(() -> new ResourceNotFoundException("Chamado", idChamado));
        StatusChamado status = statusChamadoRepository.findById(idStatus)
                .orElseThrow(() -> new ResourceNotFoundException("Status do Chamado", idStatus));
        chamado.setStatusChamado(status);
        Chamado atualizado = chamadoRepository.save(chamado);
        return toResponseDTO(atualizado);
    }

    @Transactional
    public void deletar(Long id) {
        if (!chamadoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Chamado", id);
        }
        chamadoRepository.deleteById(id);
    }

    // ── Conversão ──────────────────────────────────────────

    private ChamadoResponseDTO toResponseDTO(Chamado chamado) {
        ChamadoResponseDTO dto = new ChamadoResponseDTO();
        dto.setIdChamado(chamado.getIdChamado());
        dto.setDataChamado(chamado.getDataChamado());
        dto.setHoraChamado(chamado.getHoraChamado());
        dto.setDescricao(chamado.getDescricao());
        dto.setImagemUrl(chamado.getImagemUrl());
        dto.setStatus(statusChamadoService.toDTO(chamado.getStatusChamado()));
        dto.setUsuario(usuarioService.toResponseDTO(chamado.getUsuario()));
        dto.setEndereco(enderecoService.toResponseDTO(chamado.getEndereco()));
        dto.setTipoProblema(tipoProblemaService.toDTO(chamado.getTipoProblema()));
        return dto;
    }
}

