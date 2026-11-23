package br.com.Sabesp.service;

import br.com.Sabesp.dto.LoginRequestDTO;
import br.com.Sabesp.dto.RecuperarSenhaDTO;
import br.com.Sabesp.dto.UsuarioRequestDTO;
import br.com.Sabesp.dto.UsuarioResponseDTO;
import br.com.Sabesp.model.Usuario;
import br.com.Sabesp.model.enums.TipoUsuario;
import br.com.Sabesp.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável pela autenticação e gerenciamento de acesso dos usuários.
 * <p>
 * Oferece funcionalidades de login, registro de novos usuários e
 * recuperação de senha. Atua como camada intermediária entre o controller
 * de autenticação e os repositórios/serviços de usuário.
 * </p>
 *
 * @see UsuarioService
 * @see UsuarioRepository
 */
@Service
@RequiredArgsConstructor // Lombok gera o construtor com todos os campos final (injeção de dependência)
public class AuthService {

    /** Repositório para acesso direto à tabela de usuários no banco de dados. */
    private final UsuarioRepository usuarioRepository;

    /** Serviço de usuário utilizado para criação e conversão de DTOs. */
    private final UsuarioService usuarioService;

    /**
     * Realiza o login do usuário com base no e-mail e senha informados.
     * <p>
     * Valida se os campos obrigatórios foram preenchidos, busca o usuário
     * pelo e-mail e compara a senha fornecida com a senha armazenada.
     * Retorna os dados do usuário (sem a senha) em caso de sucesso.
     * </p>
     *
     * @param dto objeto contendo o e-mail e a senha informados pelo usuário
     * @return {@link UsuarioResponseDTO} com os dados do usuário autenticado
     * @throws IllegalArgumentException se e-mail/senha estiverem vazios ou incorretos
     */
    @Transactional(readOnly = true) // Transação somente leitura — não altera dados no banco
    public UsuarioResponseDTO login(LoginRequestDTO dto) {
        // Verifica se o e-mail e a senha foram informados (campos obrigatórios)
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty() ||
            dto.getSenha() == null || dto.getSenha().trim().isEmpty()) {
            throw new IllegalArgumentException("E-mail e senha são obrigatórios.");
        }

        // Busca o usuário pelo e-mail informado; lança exceção se não encontrar
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail().trim())
                .orElseThrow(() -> new IllegalArgumentException("E-mail ou senha incorretos."));

        // Compara a senha informada com a senha armazenada no banco
        if (!usuario.getSenha().equals(dto.getSenha().trim())) {
            // Mensagem genérica por segurança — não revela se o e-mail existe
            throw new IllegalArgumentException("E-mail ou senha incorretos.");
        }

        // Converte a entidade Usuario para o DTO de resposta (sem expor a senha)
        return usuarioService.toResponseDTO(usuario);
    }

    /**
     * Registra um novo usuário no sistema (auto-cadastro público de cidadãos).
     * <p>
     * <b>Regra de contas:</b> este endpoint é exclusivo de cidadãos — o tipo é
     * <b>sempre forçado</b> para {@link TipoUsuario#COMUM}, independentemente do
     * valor enviado na requisição. Isso impede que alguém se cadastre como
     * {@code SABESP} ou {@code PREFEITURA} por esta via (contas institucionais
     * são criadas somente pela Prefeitura em {@code POST /api/usuarios}).
     * Delega a criação efetiva ao {@link UsuarioService}.
     * </p>
     *
     * @param dto objeto contendo os dados do novo usuário (nome, e-mail, senha, etc.)
     * @return {@link UsuarioResponseDTO} com os dados do usuário recém-criado
     */
    @Transactional // Transação de escrita — permite inserção no banco
    public UsuarioResponseDTO registrar(UsuarioRequestDTO dto) {
        // Força o perfil COMUM: o auto-cadastro público só cria contas de cidadão,
        // ignorando qualquer tipo (SABESP/PREFEITURA) enviado na requisição.
        dto.setTipoUsuario(TipoUsuario.COMUM);
        // Delega a criação do usuário para o UsuarioService
        return usuarioService.criar(dto);
    }

    /**
     * Recupera a senha de um usuário pelo e-mail informado.
     * <p>
     * Se o e-mail existir no banco, a senha é atualizada pela nova senha fornecida.
     * Valida o comprimento mínimo da nova senha (6 caracteres).
     * </p>
     *
     * @param dto objeto contendo o e-mail do usuário e a nova senha desejada
     * @return {@link UsuarioResponseDTO} com os dados do usuário após a atualização
     * @throws IllegalArgumentException se o e-mail estiver vazio, a senha for curta
     *                                  ou o e-mail não for encontrado
     */
    @Transactional // Transação de escrita — atualiza a senha no banco
    public UsuarioResponseDTO recuperarSenha(RecuperarSenhaDTO dto) {
        // Valida se o e-mail foi informado
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("O e-mail é obrigatório.");
        }
        // Valida o comprimento mínimo da nova senha (pelo menos 6 caracteres)
        if (dto.getNovaSenha() == null || dto.getNovaSenha().length() < 6) {
            throw new IllegalArgumentException("A nova senha deve ter pelo menos 6 caracteres.");
        }

        // Busca o usuário pelo e-mail; lança exceção se a conta não existir
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail().trim())
                .orElseThrow(() -> new IllegalArgumentException("Não encontramos uma conta com este e-mail."));

        // Atualiza a senha do usuário com a nova senha fornecida
        usuario.setSenha(dto.getNovaSenha());
        // Persiste a alteração no banco de dados
        Usuario atualizado = usuarioRepository.save(usuario);

        // Retorna o DTO de resposta com os dados atualizados (sem a senha)
        return usuarioService.toResponseDTO(atualizado);
    }
}
