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

/**
 * Serviço principal para gerenciamento de chamados de esgoto.
 * <p>
 * Contém toda a lógica de negócio para operações CRUD de chamados,
 * incluindo regras específicas como: cidadão COMUM só pode abrir
 * chamado com status "Aberto" (id=1), atualizações parciais de campos,
 * e alteração isolada de status.
 * </p>
 *
 * @see Chamado
 * @see ChamadoRepository
 */
@Service
@RequiredArgsConstructor // Lombok gera construtor com todos os campos final (injeção via construtor)
public class ChamadoService {

    // ── Repositórios (acesso ao banco de dados) ──────────────────────

    /** Repositório para operações de persistência de chamados. */
    private final ChamadoRepository chamadoRepository;

    /** Repositório para busca e validação de usuários. */
    private final UsuarioRepository usuarioRepository;

    /** Repositório para busca e validação de endereços. */
    private final EnderecoRepository enderecoRepository;

    /** Repositório para busca e validação de tipos de problema. */
    private final TipoProblemaRepository tipoProblemaRepository;

    /** Repositório para busca e validação de status de chamado. */
    private final StatusChamadoRepository statusChamadoRepository;

    // ── Serviços auxiliares (conversão de entidades para DTOs) ────────

    /** Serviço de usuário — utilizado para converter Usuario em UsuarioResponseDTO. */
    private final UsuarioService usuarioService;

    /** Serviço de endereço — utilizado para converter Endereco em EnderecoResponseDTO. */
    private final EnderecoService enderecoService;

    /** Serviço de tipo de problema — utilizado para converter TipoProblema em TipoProblemaDTO. */
    private final TipoProblemaService tipoProblemaService;

    /** Serviço de status de chamado — utilizado para converter StatusChamado em StatusChamadoDTO. */
    private final StatusChamadoService statusChamadoService;

    // ── Métodos de Consulta (Leitura) ────────────────────────────────

    /**
     * Lista todos os chamados cadastrados no sistema.
     *
     * @return lista de {@link ChamadoResponseDTO} com todos os chamados
     */
    @Transactional(readOnly = true) // Transação somente leitura — otimiza performance
    public List<ChamadoResponseDTO> listarTodos() {
        // Busca todos os chamados, converte cada entidade para DTO via stream
        return chamadoRepository.findAll()
                .stream()
                .map(this::toResponseDTO) // Converte cada Chamado para ChamadoResponseDTO
                .collect(Collectors.toList());
    }

    /**
     * Busca um chamado específico pelo seu identificador único.
     *
     * @param id identificador do chamado
     * @return {@link ChamadoResponseDTO} com os dados do chamado encontrado
     * @throws ResourceNotFoundException se o chamado não for encontrado
     */
    @Transactional(readOnly = true)
    public ChamadoResponseDTO buscarPorId(Long id) {
        // Busca o chamado pelo ID; lança exceção 404 se não existir
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chamado", id));
        return toResponseDTO(chamado);
    }

    /**
     * Busca todos os chamados abertos por um usuário específico.
     *
     * @param idUsuario identificador do usuário
     * @return lista de {@link ChamadoResponseDTO} do usuário informado
     */
    @Transactional(readOnly = true)
    public List<ChamadoResponseDTO> buscarPorUsuario(Long idUsuario) {
        // Consulta derivada do Spring Data — busca pelo ID do usuário associado
        return chamadoRepository.findByUsuarioIdUsuario(idUsuario)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca todos os chamados filtrados por um determinado status.
     *
     * @param idStatus identificador do status desejado (ex.: 1=Aberto, 2=Em Andamento)
     * @return lista de {@link ChamadoResponseDTO} com o status informado
     */
    @Transactional(readOnly = true)
    public List<ChamadoResponseDTO> buscarPorStatus(Long idStatus) {
        // Consulta derivada do Spring Data — busca pelo ID do status do chamado
        return chamadoRepository.findByStatusChamadoIdStatus(idStatus)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca todos os chamados associados a um determinado bairro.
     *
     * @param bairro nome do bairro para filtro
     * @return lista de {@link ChamadoResponseDTO} localizados no bairro informado
     */
    @Transactional(readOnly = true)
    public List<ChamadoResponseDTO> buscarPorBairro(String bairro) {
        // Consulta derivada do Spring Data — busca pelo bairro do endereço associado
        return chamadoRepository.findByEnderecoBairro(bairro)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ── Métodos de Escrita (Criação, Atualização, Exclusão) ──────────

    /**
     * Cria um novo chamado de esgoto no sistema.
     * <p>
     * <strong>Regra de negócio:</strong> se o usuário solicitante for do tipo
     * {@link TipoUsuario#COMUM} (cidadão), o status é forçado para "Aberto"
     * (id=1), independentemente do valor informado no DTO.
     * </p>
     *
     * @param dto dados do chamado a ser criado
     * @return {@link ChamadoResponseDTO} com os dados do chamado criado
     * @throws ResourceNotFoundException se usuário, endereço, tipo de problema
     *                                   ou status não forem encontrados
     */
    @Transactional // Transação de escrita — permite inserção no banco
    public ChamadoResponseDTO criar(ChamadoRequestDTO dto) {
        // ── Regra de negócio: cidadão comum só abre chamado como "Aberto" ──
        if (dto.getIdUsuario() != null) {
            // Busca o usuário solicitante para verificar seu tipo
            Usuario usuarioSolicitante = usuarioRepository.findById(dto.getIdUsuario())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário", dto.getIdUsuario()));
            // Se for cidadão COMUM, força o status para "Aberto" (id=1)
            if (usuarioSolicitante.getTipoUsuario() == TipoUsuario.COMUM) {
                dto.setIdStatus(1L); // Status "Aberto" é sempre o ID 1 na tabela de referência
            }
        }

        // ── Busca e validação de todas as entidades relacionadas ──
        // Busca o usuário que está abrindo o chamado
        Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", dto.getIdUsuario()));
        // Busca o endereço onde ocorre o problema de esgoto
        Endereco endereco = enderecoRepository.findById(dto.getIdEndereco())
                .orElseThrow(() -> new ResourceNotFoundException("Endereço", dto.getIdEndereco()));
        // Busca o tipo de problema reportado (ex.: vazamento, entupimento)
        TipoProblema tipoProblema = tipoProblemaRepository.findById(dto.getIdTipoProblema())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de Problema", dto.getIdTipoProblema()));
        // Busca o status inicial do chamado
        StatusChamado statusChamado = statusChamadoRepository.findById(dto.getIdStatus())
                .orElseThrow(() -> new ResourceNotFoundException("Status do Chamado", dto.getIdStatus()));

        // ── Montagem da entidade Chamado ──
        Chamado chamado = new Chamado();
        chamado.setDataChamado(dto.getDataChamado());       // Data de abertura do chamado
        chamado.setHoraChamado(dto.getHoraChamado());        // Hora de abertura do chamado
        chamado.setDescricao(dto.getDescricao());            // Descrição detalhada do problema
        chamado.setImagemUrl(dto.getImagemUrl());            // URL da imagem/foto do problema (opcional)
        chamado.setUsuario(usuario);                         // Associa o usuário solicitante
        chamado.setEndereco(endereco);                       // Associa o endereço do problema
        chamado.setTipoProblema(tipoProblema);               // Associa o tipo de problema
        chamado.setStatusChamado(statusChamado);             // Associa o status inicial

        // Persiste o chamado no banco de dados
        Chamado salvo = chamadoRepository.save(chamado);
        // Retorna o DTO de resposta com todos os dados do chamado criado
        return toResponseDTO(salvo);
    }

    /**
     * Atualiza parcialmente um chamado existente.
     * <p>
     * Permite atualização seletiva: apenas os campos preenchidos no DTO
     * serão alterados. Campos nulos ou vazios são ignorados, evitando
     * sobrescrita acidental de dados existentes.
     * </p>
     *
     * @param id  identificador do chamado a ser atualizado
     * @param dto dados parciais para atualização
     * @return {@link ChamadoResponseDTO} com os dados do chamado atualizado
     * @throws ResourceNotFoundException se o chamado ou entidades relacionadas
     *                                   não forem encontrados
     */
    @Transactional // Transação de escrita — permite alteração no banco
    public ChamadoResponseDTO atualizar(Long id, ChamadoRequestDTO dto) {
        // Busca o chamado existente; lança exceção 404 se não encontrar
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chamado", id));

        // ── Atualização de entidades relacionadas (se informadas) ──

        // Atualiza o endereço somente se um novo ID de endereço foi fornecido
        if (dto.getIdEndereco() != null) {
            Endereco endereco = enderecoRepository.findById(dto.getIdEndereco())
                    .orElseThrow(() -> new ResourceNotFoundException("Endereço", dto.getIdEndereco()));
            chamado.setEndereco(endereco);
        }
        // Atualiza o tipo de problema somente se um novo ID foi fornecido
        if (dto.getIdTipoProblema() != null) {
            TipoProblema tp = tipoProblemaRepository.findById(dto.getIdTipoProblema())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de Problema", dto.getIdTipoProblema()));
            chamado.setTipoProblema(tp);
        }
        // Atualiza o status somente se um novo ID de status foi fornecido
        if (dto.getIdStatus() != null) {
            StatusChamado sc = statusChamadoRepository.findById(dto.getIdStatus())
                    .orElseThrow(() -> new ResourceNotFoundException("Status do Chamado", dto.getIdStatus()));
            chamado.setStatusChamado(sc);
        }

        // ── Atualização de campos de texto/data/hora (evita null acidental) ──

        // Atualiza a descrição somente se foi informada e não está em branco
        if (dto.getDescricao() != null && !dto.getDescricao().isBlank()) {
            chamado.setDescricao(dto.getDescricao());
        }
        // Atualiza a data do chamado somente se informada
        if (dto.getDataChamado() != null) {
            chamado.setDataChamado(dto.getDataChamado());
        }
        // Atualiza a hora do chamado somente se informada
        if (dto.getHoraChamado() != null) {
            chamado.setHoraChamado(dto.getHoraChamado());
        }
        // Atualiza a URL da imagem somente se informada
        if (dto.getImagemUrl() != null) {
            chamado.setImagemUrl(dto.getImagemUrl());
        }

        // Persiste as alterações no banco de dados
        Chamado atualizado = chamadoRepository.save(chamado);
        return toResponseDTO(atualizado);
    }

    /**
     * Altera apenas o status de um chamado existente.
     * <p>
     * Método dedicado para transições de status (ex.: Aberto → Em Andamento → Concluído),
     * sem necessidade de enviar todos os outros campos do chamado.
     * </p>
     *
     * @param idChamado identificador do chamado
     * @param idStatus  identificador do novo status
     * @return {@link ChamadoResponseDTO} com os dados do chamado após a alteração
     * @throws ResourceNotFoundException se o chamado ou o status não forem encontrados
     */
    @Transactional // Transação de escrita — atualiza o status no banco
    public ChamadoResponseDTO alterarStatus(Long idChamado, Long idStatus) {
        // Busca o chamado pelo ID
        Chamado chamado = chamadoRepository.findById(idChamado)
                .orElseThrow(() -> new ResourceNotFoundException("Chamado", idChamado));
        // Busca o novo status pelo ID
        StatusChamado status = statusChamadoRepository.findById(idStatus)
                .orElseThrow(() -> new ResourceNotFoundException("Status do Chamado", idStatus));
        // Atualiza o status do chamado
        chamado.setStatusChamado(status);
        // Persiste a alteração
        Chamado atualizado = chamadoRepository.save(chamado);
        return toResponseDTO(atualizado);
    }

    /**
     * Remove um chamado do sistema pelo seu identificador.
     *
     * @param id identificador do chamado a ser removido
     * @throws ResourceNotFoundException se o chamado não for encontrado
     */
    @Transactional // Transação de escrita — permite exclusão no banco
    public void deletar(Long id) {
        // Verifica se o chamado existe antes de tentar excluir
        if (!chamadoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Chamado", id);
        }
        // Remove o chamado do banco de dados
        chamadoRepository.deleteById(id);
    }

    // ── Conversão (Entidade → DTO de Resposta) ──────────────────────

    /**
     * Converte uma entidade {@link Chamado} para {@link ChamadoResponseDTO}.
     * <p>
     * Utiliza os serviços auxiliares para converter as entidades relacionadas
     * (usuário, endereço, tipo de problema, status) em seus respectivos DTOs,
     * garantindo que dados sensíveis (como senha do usuário) não sejam expostos.
     * </p>
     *
     * @param chamado entidade Chamado a ser convertida
     * @return DTO de resposta com todos os dados do chamado e suas relações
     */
    private ChamadoResponseDTO toResponseDTO(Chamado chamado) {
        ChamadoResponseDTO dto = new ChamadoResponseDTO();
        dto.setIdChamado(chamado.getIdChamado());             // ID único do chamado
        dto.setDataChamado(chamado.getDataChamado());          // Data de abertura
        dto.setHoraChamado(chamado.getHoraChamado());          // Hora de abertura
        dto.setDescricao(chamado.getDescricao());              // Descrição do problema
        dto.setImagemUrl(chamado.getImagemUrl());              // URL da foto (opcional)
        dto.setStatus(statusChamadoService.toDTO(chamado.getStatusChamado()));       // Status convertido para DTO
        dto.setUsuario(usuarioService.toResponseDTO(chamado.getUsuario()));           // Usuário convertido (sem senha)
        dto.setEndereco(enderecoService.toResponseDTO(chamado.getEndereco()));        // Endereço convertido para DTO
        dto.setTipoProblema(tipoProblemaService.toDTO(chamado.getTipoProblema()));   // Tipo de problema convertido
        return dto;
    }
}
