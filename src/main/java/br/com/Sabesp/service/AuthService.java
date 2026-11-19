package br.com.Sabesp.service;

import br.com.Sabesp.dto.LoginRequestDTO;
import br.com.Sabesp.dto.UsuarioRequestDTO;
import br.com.Sabesp.dto.UsuarioResponseDTO;
import br.com.Sabesp.model.Usuario;
import br.com.Sabesp.model.enums.TipoUsuario;
import br.com.Sabesp.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    @Transactional(readOnly = true)
    public UsuarioResponseDTO login(LoginRequestDTO dto) {
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty() ||
            dto.getSenha() == null || dto.getSenha().trim().isEmpty()) {
            throw new IllegalArgumentException("E-mail e senha são obrigatórios.");
        }

        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail().trim())
                .orElseThrow(() -> new IllegalArgumentException("E-mail ou senha incorretos."));

        if (!usuario.getSenha().equals(dto.getSenha().trim())) {
            throw new IllegalArgumentException("E-mail ou senha incorretos.");
        }

        return usuarioService.toResponseDTO(usuario);
    }

    @Transactional
    public UsuarioResponseDTO registrar(UsuarioRequestDTO dto) {
        if (dto.getTipoUsuario() == null) {
            dto.setTipoUsuario(TipoUsuario.COMUM);
        }
        return usuarioService.criar(dto);
    }
}

