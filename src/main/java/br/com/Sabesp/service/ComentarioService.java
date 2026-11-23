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

/**
 * Serviço responsável pelo gerenciamento de comentários/acompanhamentos em chamados.
 * <p>
 * Permite adicionar, editar, listar e remover comentários vinculados a chamados
 * de esgoto. Cada comentário é associado a um chamado e ao usuário que o criou,
 * com registro automático da data/hora de criação.
 * </p>
 *
 * @see Comentario
 * @see ComentarioRepository
 */
@Service
@RequiredArgsConstructor // Lombok gera construtor com todos os campos final (injeção via construtor)
public class ComentarioService {

    /** Repositório para operações de persistência de comentários. */
    private final ComentarioRepository comentarioRepository;

    /** Repositório para validação de existência de chamados. */
    private final ChamadoRepository chamadoRepository;

    /** Repositório para validação de existência de usuários. */
    private final UsuarioRepository usuarioRepository;

    /** Serviço de usuário — utilizado para converter Usuario em DTO de resposta. */
    private final UsuarioService usuarioService;

    // ── Métodos de Consulta (Leitura) ────────────────────────────────

    /**
     * Lista todos os comentários cadastrados no sistema.
     *
     * @return lista de {@link ComentarioResponseDTO} com todos os comentários
     */
    @Transactional(readOnly = true) // Transação somente leitura — otimiza performance
    public List<ComentarioResponseDTO> listarTodos() {
        // Busca todos os comentários e converte cada um para DTO via stream
        return comentarioRepository.findAll()
                .stream()
                .map(this::toResponseDTO) // Converte cada Comentario para ComentarioResponseDTO
                .collect(Collectors.toList());
    }

    /**
     * Busca um comentário específico pelo seu identificador único.
     *
     * @param id identificador do comentário
     * @return {@link ComentarioResponseDTO} com os dados do comentário encontrado
     * @throws ResourceNotFoundException se o comentário não for encontrado
     */
    @Transactional(readOnly = true)
    public ComentarioResponseDTO buscarPorId(Long id) {
        // Busca o comentário pelo ID; lança exceção 404 se não existir
        Comentario comentario = comentarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comentário", id));
        return toResponseDTO(comentario);
    }

    /**
     * Busca todos os comentários associados a um chamado específico.
     * <p>
     * Útil para exibir o histórico de acompanhamento/comunicação de um chamado.
     * </p>
     *
     * @param idChamado identificador do chamado
     * @return lista de {@link ComentarioResponseDTO} vinculados ao chamado
     */
    @Transactional(readOnly = true)
    public List<ComentarioResponseDTO> buscarPorChamado(Long idChamado) {
        // Consulta derivada do Spring Data — busca pelo ID do chamado associado
        return comentarioRepository.findByChamadoIdChamado(idChamado)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ── Métodos de Escrita (Criação, Atualização, Exclusão) ──────────

    /**
     * Cria um novo comentário vinculado a um chamado e a um usuário.
     * <p>
     * A data/hora do comentário é registrada automaticamente com o momento
     * atual ({@link LocalDateTime#now()}).
     * </p>
     *
     * @param dto dados do comentário a ser criado (texto, ID do chamado, ID do usuário)
     * @return {@link ComentarioResponseDTO} com os dados do comentário criado
     * @throws ResourceNotFoundException se o chamado ou o usuário não forem encontrados
     */
    @Transactional // Transação de escrita — permite inserção no banco
    public ComentarioResponseDTO criar(ComentarioRequestDTO dto) {
        // Busca o chamado ao qual o comentário será vinculado
        Chamado chamado = chamadoRepository.findById(dto.getIdChamado())
                .orElseThrow(() -> new ResourceNotFoundException("Chamado", dto.getIdChamado()));
        // Busca o usuário autor do comentário
        Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", dto.getIdUsuario()));

        // Monta a entidade Comentario com os dados informados
        Comentario comentario = new Comentario();
        comentario.setTexto(dto.getTexto());                    // Texto do comentário
        comentario.setDataComentario(LocalDateTime.now());      // Data/hora atual do servidor
        comentario.setChamado(chamado);                          // Associa ao chamado
        comentario.setUsuario(usuario);                          // Associa ao autor

        // Persiste o comentário no banco de dados
        Comentario salvo = comentarioRepository.save(comentario);
        return toResponseDTO(salvo);
    }

    /**
     * Atualiza um comentário existente.
     * <p>
     * O texto é sempre atualizado. O chamado e o usuário associados só são
     * alterados se os respectivos IDs forem informados no DTO (atualização parcial).
     * </p>
     *
     * @param id  identificador do comentário a ser atualizado
     * @param dto dados para atualização
     * @return {@link ComentarioResponseDTO} com os dados do comentário atualizado
     * @throws ResourceNotFoundException se o comentário, chamado ou usuário
     *                                   não forem encontrados
     */
    @Transactional // Transação de escrita — permite alteração no banco
    public ComentarioResponseDTO atualizar(Long id, ComentarioRequestDTO dto) {
        // Busca o comentário existente; lança exceção 404 se não encontrar
        Comentario comentario = comentarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comentário", id));

        // Atualiza o texto do comentário (campo obrigatório)
        comentario.setTexto(dto.getTexto());

        // Atualiza o chamado vinculado somente se um novo ID foi informado
        if (dto.getIdChamado() != null) {
            Chamado chamado = chamadoRepository.findById(dto.getIdChamado())
                    .orElseThrow(() -> new ResourceNotFoundException("Chamado", dto.getIdChamado()));
            comentario.setChamado(chamado);
        }
        // Atualiza o usuário autor somente se um novo ID foi informado
        if (dto.getIdUsuario() != null) {
            Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário", dto.getIdUsuario()));
            comentario.setUsuario(usuario);
        }

        // Persiste as alterações no banco de dados
        Comentario atualizado = comentarioRepository.save(comentario);
        return toResponseDTO(atualizado);
    }

    /**
     * Remove um comentário do sistema pelo seu identificador.
     *
     * @param id identificador do comentário a ser removido
     * @throws ResourceNotFoundException se o comentário não for encontrado
     */
    @Transactional // Transação de escrita — permite exclusão no banco
    public void deletar(Long id) {
        // Verifica se o comentário existe antes de tentar excluir
        if (!comentarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Comentário", id);
        }
        // Remove o comentário do banco de dados
        comentarioRepository.deleteById(id);
    }

    // ── Conversão (Entidade → DTO de Resposta) ──────────────────────

    /**
     * Converte uma entidade {@link Comentario} para {@link ComentarioResponseDTO}.
     * <p>
     * Inclui os dados do usuário autor convertidos via {@link UsuarioService},
     * garantindo que a senha não seja exposta na resposta.
     * </p>
     *
     * @param comentario entidade Comentario a ser convertida
     * @return DTO de resposta com os dados do comentário
     */
    private ComentarioResponseDTO toResponseDTO(Comentario comentario) {
        ComentarioResponseDTO dto = new ComentarioResponseDTO();
        dto.setIdComentario(comentario.getIdComentario());     // ID único do comentário
        dto.setDataComentario(comentario.getDataComentario()); // Data/hora de criação
        dto.setTexto(comentario.getTexto());                   // Conteúdo textual do comentário
        dto.setIdChamado(comentario.getChamado().getIdChamado()); // ID do chamado vinculado
        dto.setUsuario(usuarioService.toResponseDTO(comentario.getUsuario())); // Autor (sem senha)
        return dto;
    }
}
