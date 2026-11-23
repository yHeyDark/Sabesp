package br.com.Sabesp.service;

import br.com.Sabesp.dto.UsuarioRequestDTO;
import br.com.Sabesp.dto.UsuarioResponseDTO;
import br.com.Sabesp.exception.ResourceNotFoundException;
import br.com.Sabesp.model.Usuario;
import br.com.Sabesp.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço responsável pelo gerenciamento de usuários do sistema.
 * <p>
 * Contém operações CRUD completas para usuários (cidadãos e administradores).
 * O método {@link #toResponseDTO(Usuario)} é público e utilizado por outros serviços
 * (como AuthService e ComentarioService) para converter entidades Usuario em DTOs
 * de resposta, <strong>omitindo a senha por segurança</strong>.
 * </p>
 *
 * @see Usuario
 * @see UsuarioRepository
 */
@Service
@RequiredArgsConstructor // Lombok gera construtor com todos os campos final (injeção via construtor)
public class UsuarioService {

    /** Repositório para operações de persistência de usuários no banco de dados. */
    private final UsuarioRepository usuarioRepository;

    // ── Métodos de Consulta (Leitura) ────────────────────────────────

    /**
     * Lista todos os usuários cadastrados no sistema.
     *
     * @return lista de {@link UsuarioResponseDTO} com todos os usuários (sem senhas)
     */
    @Transactional(readOnly = true) // Transação somente leitura — otimiza performance
    public List<UsuarioResponseDTO> listarTodos() {
        // Busca todos os usuários e converte cada um para DTO via stream
        return usuarioRepository.findAll()
                .stream()
                .map(this::toResponseDTO) // Converte cada Usuario para UsuarioResponseDTO (sem senha)
                .collect(Collectors.toList());
    }

    /**
     * Busca um usuário específico pelo seu identificador único.
     *
     * @param id identificador do usuário
     * @return {@link UsuarioResponseDTO} com os dados do usuário encontrado
     * @throws ResourceNotFoundException se o usuário não for encontrado
     */
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        // Busca o usuário pelo ID; lança exceção 404 se não existir
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
        return toResponseDTO(usuario);
    }

    /**
     * Busca um usuário pelo seu endereço de e-mail.
     *
     * @param email endereço de e-mail do usuário
     * @return {@link UsuarioResponseDTO} com os dados do usuário encontrado
     * @throws ResourceNotFoundException se nenhum usuário for encontrado com o e-mail informado
     */
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorEmail(String email) {
        // Busca o usuário pelo e-mail; lança exceção se não encontrar
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com email: " + email));
        return toResponseDTO(usuario);
    }

    // ── Métodos de Escrita (Criação, Atualização, Exclusão) ──────────

    /**
     * Cria um novo usuário no sistema.
     * <p>
     * A data de cadastro é registrada automaticamente com o momento atual
     * ({@link LocalDateTime#now()}).
     * </p>
     *
     * @param dto dados do novo usuário (nome, e-mail, senha, telefone, tipo)
     * @return {@link UsuarioResponseDTO} com os dados do usuário recém-criado
     */
    @Transactional // Transação de escrita — permite inserção no banco
    public UsuarioResponseDTO criar(UsuarioRequestDTO dto) {
        // Cria uma nova entidade Usuario e preenche com os dados do DTO
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());                 // Nome completo do usuário
        usuario.setEmail(dto.getEmail());               // E-mail (usado para login)
        usuario.setSenha(dto.getSenha());               // Senha de acesso
        usuario.setTelefone(dto.getTelefone());         // Telefone de contato
        usuario.setTipoUsuario(dto.getTipoUsuario());   // Tipo: COMUM ou ADMIN
        usuario.setDataCadastro(LocalDateTime.now());    // Data/hora de cadastro (automática)

        // Persiste o novo usuário no banco de dados
        Usuario salvo = usuarioRepository.save(usuario);
        // Retorna o DTO de resposta (sem a senha)
        return toResponseDTO(salvo);
    }

    /**
     * Atualiza os dados de um usuário existente.
     * <p>
     * A senha só é atualizada se for informada e não estiver em branco,
     * evitando sobrescrita acidental com valor nulo.
     * </p>
     *
     * @param id  identificador do usuário a ser atualizado
     * @param dto novos dados do usuário
     * @return {@link UsuarioResponseDTO} com os dados do usuário atualizado
     * @throws ResourceNotFoundException se o usuário não for encontrado
     */
    @Transactional // Transação de escrita — permite alteração no banco
    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO dto) {
        // Busca o usuário existente; lança exceção 404 se não encontrar
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));

        // Atualiza os campos básicos do usuário
        usuario.setNome(dto.getNome());                 // Atualiza o nome
        usuario.setEmail(dto.getEmail());               // Atualiza o e-mail
        usuario.setTelefone(dto.getTelefone());         // Atualiza o telefone
        usuario.setTipoUsuario(dto.getTipoUsuario());   // Atualiza o tipo de usuário

        // Atualiza a senha somente se foi informada e não está em branco
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            usuario.setSenha(dto.getSenha());
        }

        // Persiste as alterações no banco de dados
        Usuario atualizado = usuarioRepository.save(usuario);
        return toResponseDTO(atualizado);
    }

    /**
     * Remove um usuário do sistema pelo seu identificador.
     *
     * @param id identificador do usuário a ser removido
     * @throws ResourceNotFoundException se o usuário não for encontrado
     */
    @Transactional // Transação de escrita — permite exclusão no banco
    public void deletar(Long id) {
        // Verifica se o usuário existe antes de tentar excluir
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário", id);
        }
        // Remove o usuário do banco de dados
        usuarioRepository.deleteById(id);
    }

    // ── Conversão (Entidade → DTO de Resposta) ──────────────────────

    /**
     * Converte uma entidade {@link Usuario} para {@link UsuarioResponseDTO}.
     * <p>
     * <strong>Importante:</strong> este método é público e utilizado por diversos
     * serviços (AuthService, ChamadoService, ComentarioService) para garantir
     * que a senha do usuário <strong>nunca seja exposta</strong> nas respostas da API.
     * O campo senha é intencionalmente omitido do DTO de resposta.
     * </p>
     *
     * @param usuario entidade Usuario a ser convertida
     * @return DTO de resposta com os dados do usuário (sem a senha)
     */
    public UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setIdUsuario(usuario.getIdUsuario());       // ID único do usuário
        dto.setNome(usuario.getNome());                 // Nome completo
        dto.setEmail(usuario.getEmail());               // E-mail
        dto.setTelefone(usuario.getTelefone());         // Telefone
        dto.setTipoUsuario(usuario.getTipoUsuario());   // Tipo (COMUM ou ADMIN)
        dto.setDataCadastro(usuario.getDataCadastro()); // Data de cadastro
        // NOTA: a senha NÃO é incluída no DTO de resposta por motivos de segurança
        return dto;
    }
}
