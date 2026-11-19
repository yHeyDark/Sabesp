package br.com.Sabesp.service;

import br.com.Sabesp.dto.ComentarioRequestDTO;
import br.com.Sabesp.dto.ComentarioResponseDTO;
import br.com.Sabesp.exception.ResourceNotFoundException;
import br.com.Sabesp.model.Chamado;
import br.com.Sabesp.model.Comentario;
import br.com.Sabesp.model.Usuario;
import br.com.Sabesp.repository.ChamadoRepository;
import br.com.Sabesp.repository.ComentarioRepository;
import br.com.Sabesp.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    @Transactional(readOnly = true)
    public List<ComentarioResponseDTO> listarTodos() {
        return comentarioRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ComentarioResponseDTO buscarPorId(Long id) {
        Comentario comentario = comentarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comentário", id));
        return toResponseDTO(comentario);
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponseDTO> buscarPorChamado(Long idChamado) {
        return comentarioRepository.findByChamadoIdChamado(idChamado)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ComentarioResponseDTO criar(ComentarioRequestDTO dto) {
        Chamado chamado = chamadoRepository.findById(dto.getIdChamado())
                .orElseThrow(() -> new ResourceNotFoundException("Chamado", dto.getIdChamado()));
        Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", dto.getIdUsuario()));

        Comentario comentario = new Comentario();
        comentario.setTexto(dto.getTexto());
        comentario.setDataComentario(LocalDateTime.now());
        comentario.setChamado(chamado);
        comentario.setUsuario(usuario);

        Comentario salvo = comentarioRepository.save(comentario);
        return toResponseDTO(salvo);
    }

    @Transactional
    public ComentarioResponseDTO atualizar(Long id, ComentarioRequestDTO dto) {
        Comentario comentario = comentarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comentário", id));

        comentario.setTexto(dto.getTexto());

        if (dto.getIdChamado() != null) {
            Chamado chamado = chamadoRepository.findById(dto.getIdChamado())
                    .orElseThrow(() -> new ResourceNotFoundException("Chamado", dto.getIdChamado()));
            comentario.setChamado(chamado);
        }
        if (dto.getIdUsuario() != null) {
            Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário", dto.getIdUsuario()));
            comentario.setUsuario(usuario);
        }

        Comentario atualizado = comentarioRepository.save(comentario);
        return toResponseDTO(atualizado);
    }

    @Transactional
    public void deletar(Long id) {
        if (!comentarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Comentário", id);
        }
        comentarioRepository.deleteById(id);
    }

    // ── Conversão ──────────────────────────────────────────

    private ComentarioResponseDTO toResponseDTO(Comentario comentario) {
        ComentarioResponseDTO dto = new ComentarioResponseDTO();
        dto.setIdComentario(comentario.getIdComentario());
        dto.setDataComentario(comentario.getDataComentario());
        dto.setTexto(comentario.getTexto());
        dto.setIdChamado(comentario.getChamado().getIdChamado());
        dto.setUsuario(usuarioService.toResponseDTO(comentario.getUsuario()));
        return dto;
    }
}

