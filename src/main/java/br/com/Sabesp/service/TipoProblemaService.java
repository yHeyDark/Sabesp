package br.com.Sabesp.service;

import br.com.Sabesp.dto.TipoProblemaDTO;
import br.com.Sabesp.exception.ResourceNotFoundException;
import br.com.Sabesp.model.TipoProblema;
import br.com.Sabesp.repository.TipoProblemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço responsável pelo gerenciamento da tabela de referência de tipos de problema.
 * <p>
 * Os tipos de problema categorizam os chamados de esgoto (ex.: "Vazamento",
 * "Entupimento", "Mau Cheiro", "Transbordamento"). Esta é uma tabela de
 * referência (lookup table) utilizada pelo {@link ChamadoService} para
 * classificar cada chamado.
 * </p>
 *
 * @see TipoProblema
 * @see TipoProblemaRepository
 */
@Service
@RequiredArgsConstructor // Lombok gera construtor com todos os campos final (injeção via construtor)
public class TipoProblemaService {

    /** Repositório para operações de persistência de tipos de problema. */
    private final TipoProblemaRepository tipoProblemaRepository;

    // ── Métodos de Consulta (Leitura) ────────────────────────────────

    /**
     * Lista todos os tipos de problema disponíveis no sistema.
     *
     * @return lista de {@link TipoProblemaDTO} com todos os tipos de problema
     */
    @Transactional(readOnly = true) // Transação somente leitura — otimiza performance
    public List<TipoProblemaDTO> listarTodos() {
        // Busca todos os tipos de problema e converte cada um para DTO via stream
        return tipoProblemaRepository.findAll()
                .stream()
                .map(this::toDTO) // Converte cada TipoProblema para TipoProblemaDTO
                .collect(Collectors.toList());
    }

    /**
     * Busca um tipo de problema específico pelo seu identificador único.
     *
     * @param id identificador do tipo de problema
     * @return {@link TipoProblemaDTO} com os dados do tipo de problema encontrado
     * @throws ResourceNotFoundException se o tipo de problema não for encontrado
     */
    @Transactional(readOnly = true)
    public TipoProblemaDTO buscarPorId(Long id) {
        // Busca o tipo de problema pelo ID; lança exceção 404 se não existir
        TipoProblema tp = tipoProblemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de Problema", id));
        return toDTO(tp);
    }

    // ── Métodos de Escrita (Criação, Atualização, Exclusão) ──────────

    /**
     * Cria um novo tipo de problema na tabela de referência.
     *
     * @param dto dados do tipo de problema (descrição, ex.: "Vazamento de Esgoto")
     * @return {@link TipoProblemaDTO} com os dados do tipo de problema criado
     */
    @Transactional // Transação de escrita — permite inserção no banco
    public TipoProblemaDTO criar(TipoProblemaDTO dto) {
        // Cria uma nova entidade e define a descrição do tipo de problema
        TipoProblema tp = new TipoProblema();
        tp.setDescricao(dto.getDescricao()); // Descrição textual (ex.: "Entupimento")
        // Persiste o novo tipo de problema no banco de dados
        TipoProblema salvo = tipoProblemaRepository.save(tp);
        return toDTO(salvo);
    }

    /**
     * Atualiza a descrição de um tipo de problema existente.
     *
     * @param id  identificador do tipo de problema a ser atualizado
     * @param dto novos dados do tipo de problema
     * @return {@link TipoProblemaDTO} com os dados do tipo de problema atualizado
     * @throws ResourceNotFoundException se o tipo de problema não for encontrado
     */
    @Transactional // Transação de escrita — permite alteração no banco
    public TipoProblemaDTO atualizar(Long id, TipoProblemaDTO dto) {
        // Busca o tipo de problema existente; lança exceção 404 se não encontrar
        TipoProblema tp = tipoProblemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de Problema", id));
        // Atualiza a descrição do tipo de problema
        tp.setDescricao(dto.getDescricao());
        // Persiste a alteração no banco de dados
        TipoProblema atualizado = tipoProblemaRepository.save(tp);
        return toDTO(atualizado);
    }

    /**
     * Remove um tipo de problema da tabela de referência pelo seu identificador.
     * <p>
     * <strong>Atenção:</strong> remover um tipo de problema que está sendo utilizado
     * por chamados existentes pode causar erros de integridade referencial.
     * </p>
     *
     * @param id identificador do tipo de problema a ser removido
     * @throws ResourceNotFoundException se o tipo de problema não for encontrado
     */
    @Transactional // Transação de escrita — permite exclusão no banco
    public void deletar(Long id) {
        // Verifica se o tipo de problema existe antes de tentar excluir
        if (!tipoProblemaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tipo de Problema", id);
        }
        // Remove o tipo de problema do banco de dados
        tipoProblemaRepository.deleteById(id);
    }

    // ── Conversão (Entidade → DTO) ──────────────────────────────────

    /**
     * Converte uma entidade {@link TipoProblema} para {@link TipoProblemaDTO}.
     * <p>
     * Método público utilizado por outros serviços (como {@link ChamadoService})
     * para incluir os dados do tipo de problema nas respostas de chamados.
     * </p>
     *
     * @param tp entidade TipoProblema a ser convertida
     * @return DTO com o ID e a descrição do tipo de problema
     */
    public TipoProblemaDTO toDTO(TipoProblema tp) {
        TipoProblemaDTO dto = new TipoProblemaDTO();
        dto.setIdTipoProblema(tp.getIdTipoProblema()); // ID único do tipo de problema
        dto.setDescricao(tp.getDescricao());             // Descrição textual (ex.: "Vazamento")
        return dto;
    }
}
