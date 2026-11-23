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

/**
 * Serviço responsável pelo gerenciamento da tabela de referência de status de chamados.
 * <p>
 * Os status representam os estados possíveis de um chamado de esgoto ao longo
 * do seu ciclo de vida (ex.: "Aberto", "Em Andamento", "Concluído", "Cancelado").
 * Esta é uma tabela de referência (lookup table) utilizada pelo {@link ChamadoService}.
 * </p>
 *
 * @see StatusChamado
 * @see StatusChamadoRepository
 */
@Service
@RequiredArgsConstructor // Lombok gera construtor com todos os campos final (injeção via construtor)
public class StatusChamadoService {

    /** Repositório para operações de persistência de status de chamado. */
    private final StatusChamadoRepository statusChamadoRepository;

    // ── Métodos de Consulta (Leitura) ────────────────────────────────

    /**
     * Lista todos os status de chamado disponíveis no sistema.
     *
     * @return lista de {@link StatusChamadoDTO} com todos os status
     */
    @Transactional(readOnly = true) // Transação somente leitura — otimiza performance
    public List<StatusChamadoDTO> listarTodos() {
        // Busca todos os status e converte cada um para DTO via stream
        return statusChamadoRepository.findAll()
                .stream()
                .map(this::toDTO) // Converte cada StatusChamado para StatusChamadoDTO
                .collect(Collectors.toList());
    }

    /**
     * Busca um status de chamado específico pelo seu identificador único.
     *
     * @param id identificador do status
     * @return {@link StatusChamadoDTO} com os dados do status encontrado
     * @throws ResourceNotFoundException se o status não for encontrado
     */
    @Transactional(readOnly = true)
    public StatusChamadoDTO buscarPorId(Long id) {
        // Busca o status pelo ID; lança exceção 404 se não existir
        StatusChamado sc = statusChamadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Status do Chamado", id));
        return toDTO(sc);
    }

    // ── Métodos de Escrita (Criação, Atualização, Exclusão) ──────────

    /**
     * Cria um novo status de chamado na tabela de referência.
     *
     * @param dto dados do status (descrição, ex.: "Em Andamento")
     * @return {@link StatusChamadoDTO} com os dados do status criado
     */
    @Transactional // Transação de escrita — permite inserção no banco
    public StatusChamadoDTO criar(StatusChamadoDTO dto) {
        // Cria uma nova entidade e define a descrição do status
        StatusChamado sc = new StatusChamado();
        sc.setDescricao(dto.getDescricao()); // Descrição textual do status (ex.: "Aberto")
        // Persiste o novo status no banco de dados
        StatusChamado salvo = statusChamadoRepository.save(sc);
        return toDTO(salvo);
    }

    /**
     * Atualiza a descrição de um status de chamado existente.
     *
     * @param id  identificador do status a ser atualizado
     * @param dto novos dados do status
     * @return {@link StatusChamadoDTO} com os dados do status atualizado
     * @throws ResourceNotFoundException se o status não for encontrado
     */
    @Transactional // Transação de escrita — permite alteração no banco
    public StatusChamadoDTO atualizar(Long id, StatusChamadoDTO dto) {
        // Busca o status existente; lança exceção 404 se não encontrar
        StatusChamado sc = statusChamadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Status do Chamado", id));
        // Atualiza a descrição do status
        sc.setDescricao(dto.getDescricao());
        // Persiste a alteração no banco de dados
        StatusChamado atualizado = statusChamadoRepository.save(sc);
        return toDTO(atualizado);
    }

    /**
     * Remove um status de chamado da tabela de referência pelo seu identificador.
     * <p>
     * <strong>Atenção:</strong> remover um status que está sendo utilizado por
     * chamados existentes pode causar erros de integridade referencial.
     * </p>
     *
     * @param id identificador do status a ser removido
     * @throws ResourceNotFoundException se o status não for encontrado
     */
    @Transactional // Transação de escrita — permite exclusão no banco
    public void deletar(Long id) {
        // Verifica se o status existe antes de tentar excluir
        if (!statusChamadoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Status do Chamado", id);
        }
        // Remove o status do banco de dados
        statusChamadoRepository.deleteById(id);
    }

    // ── Conversão (Entidade → DTO) ──────────────────────────────────

    /**
     * Converte uma entidade {@link StatusChamado} para {@link StatusChamadoDTO}.
     * <p>
     * Método público utilizado por outros serviços (como {@link ChamadoService})
     * para incluir os dados de status nas respostas de chamados.
     * </p>
     *
     * @param sc entidade StatusChamado a ser convertida
     * @return DTO com o ID e a descrição do status
     */
    public StatusChamadoDTO toDTO(StatusChamado sc) {
        StatusChamadoDTO dto = new StatusChamadoDTO();
        dto.setIdStatus(sc.getIdStatus());       // ID único do status
        dto.setDescricao(sc.getDescricao());     // Descrição textual (ex.: "Aberto", "Concluído")
        return dto;
    }
}
