package br.com.Sabesp.service;

import br.com.Sabesp.dto.FiltroPesquisaRequestDTO;
import br.com.Sabesp.dto.FiltroPesquisaResponseDTO;
import br.com.Sabesp.exception.ResourceNotFoundException;
import br.com.Sabesp.model.FiltroPesquisa;
import br.com.Sabesp.model.Prefeitura;
import br.com.Sabesp.repository.FiltroPesquisaRepository;
import br.com.Sabesp.repository.PrefeituraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço responsável pelo gerenciamento de filtros de pesquisa da Prefeitura.
 * <p>
 * Permite criar, atualizar, listar e remover filtros de pesquisa que são
 * utilizados pela Prefeitura para consultar chamados com critérios específicos
 * (período, tipo de problema, status, bairro, cidade, etc.).
 * Cada filtro é vinculado a uma Prefeitura.
 * </p>
 *
 * @see FiltroPesquisa
 * @see FiltroPesquisaRepository
 */
@Service
@RequiredArgsConstructor // Lombok gera construtor com todos os campos final (injeção via construtor)
public class FiltroPesquisaService {

    /** Repositório para operações de persistência de filtros de pesquisa. */
    private final FiltroPesquisaRepository filtroPesquisaRepository;

    /** Repositório para validação e busca de prefeituras. */
    private final PrefeituraRepository prefeituraRepository;

    // ── Métodos de Consulta (Leitura) ────────────────────────────────

    /**
     * Lista todos os filtros de pesquisa cadastrados no sistema.
     *
     * @return lista de {@link FiltroPesquisaResponseDTO} com todos os filtros
     */
    @Transactional(readOnly = true) // Transação somente leitura — otimiza performance
    public List<FiltroPesquisaResponseDTO> listarTodos() {
        // Busca todos os filtros e converte cada um para DTO via stream
        return filtroPesquisaRepository.findAll()
                .stream()
                .map(this::toResponseDTO) // Converte cada FiltroPesquisa para DTO
                .collect(Collectors.toList());
    }

    /**
     * Busca um filtro de pesquisa específico pelo seu identificador único.
     *
     * @param id identificador do filtro
     * @return {@link FiltroPesquisaResponseDTO} com os dados do filtro encontrado
     * @throws ResourceNotFoundException se o filtro não for encontrado
     */
    @Transactional(readOnly = true)
    public FiltroPesquisaResponseDTO buscarPorId(Long id) {
        // Busca o filtro pelo ID; lança exceção 404 se não existir
        FiltroPesquisa filtro = filtroPesquisaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filtro de Pesquisa", id));
        return toResponseDTO(filtro);
    }

    /**
     * Busca todos os filtros de pesquisa vinculados a uma Prefeitura específica.
     *
     * @param idPrefeitura identificador da Prefeitura
     * @return lista de {@link FiltroPesquisaResponseDTO} da Prefeitura informada
     */
    @Transactional(readOnly = true)
    public List<FiltroPesquisaResponseDTO> buscarPorPrefeitura(Long idPrefeitura) {
        // Consulta derivada do Spring Data — filtra pelo ID da Prefeitura associada
        return filtroPesquisaRepository.findByPrefeituraIdPrefeitura(idPrefeitura)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ── Métodos de Escrita (Criação, Atualização, Exclusão) ──────────

    /**
     * Cria um novo filtro de pesquisa vinculado a uma Prefeitura.
     *
     * @param dto dados do filtro (datas, tipo de problema, status, bairro, cidade, etc.)
     * @return {@link FiltroPesquisaResponseDTO} com os dados do filtro criado
     * @throws ResourceNotFoundException se a Prefeitura informada não for encontrada
     */
    @Transactional // Transação de escrita — permite inserção no banco
    public FiltroPesquisaResponseDTO criar(FiltroPesquisaRequestDTO dto) {
        // Busca a Prefeitura à qual o filtro será vinculado
        Prefeitura prefeitura = prefeituraRepository.findById(dto.getIdPrefeitura())
                .orElseThrow(() -> new ResourceNotFoundException("Prefeitura", dto.getIdPrefeitura()));

        // Cria nova entidade e mapeia os campos do DTO
        FiltroPesquisa filtro = new FiltroPesquisa();
        mapDtoToEntity(dto, filtro);        // Preenche os critérios de filtro
        filtro.setPrefeitura(prefeitura);    // Associa à Prefeitura dona do filtro

        // Persiste o filtro no banco de dados
        FiltroPesquisa salvo = filtroPesquisaRepository.save(filtro);
        return toResponseDTO(salvo);
    }

    /**
     * Atualiza um filtro de pesquisa existente.
     * <p>
     * A Prefeitura vinculada só é alterada se um novo ID for informado
     * no DTO (atualização parcial para o vínculo).
     * </p>
     *
     * @param id  identificador do filtro a ser atualizado
     * @param dto novos dados do filtro
     * @return {@link FiltroPesquisaResponseDTO} com os dados do filtro atualizado
     * @throws ResourceNotFoundException se o filtro ou a Prefeitura não forem encontrados
     */
    @Transactional // Transação de escrita — permite alteração no banco
    public FiltroPesquisaResponseDTO atualizar(Long id, FiltroPesquisaRequestDTO dto) {
        // Busca o filtro existente; lança exceção 404 se não encontrar
        FiltroPesquisa filtro = filtroPesquisaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filtro de Pesquisa", id));

        // Atualiza a Prefeitura vinculada somente se um novo ID foi informado
        if (dto.getIdPrefeitura() != null) {
            Prefeitura prefeitura = prefeituraRepository.findById(dto.getIdPrefeitura())
                    .orElseThrow(() -> new ResourceNotFoundException("Prefeitura", dto.getIdPrefeitura()));
            filtro.setPrefeitura(prefeitura);
        }

        // Atualiza os critérios de filtro com os novos valores do DTO
        mapDtoToEntity(dto, filtro);

        // Persiste as alterações no banco de dados
        FiltroPesquisa atualizado = filtroPesquisaRepository.save(filtro);
        return toResponseDTO(atualizado);
    }

    /**
     * Remove um filtro de pesquisa do sistema pelo seu identificador.
     *
     * @param id identificador do filtro a ser removido
     * @throws ResourceNotFoundException se o filtro não for encontrado
     */
    @Transactional // Transação de escrita — permite exclusão no banco
    public void deletar(Long id) {
        // Verifica se o filtro existe antes de tentar excluir
        if (!filtroPesquisaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Filtro de Pesquisa", id);
        }
        // Remove o filtro do banco de dados
        filtroPesquisaRepository.deleteById(id);
    }

    // ── Conversão e Mapeamento ──────────────────────────────────────

    /**
     * Mapeia os campos de um {@link FiltroPesquisaRequestDTO} para uma entidade {@link FiltroPesquisa}.
     * <p>
     * Método auxiliar reutilizado tanto na criação quanto na atualização,
     * preenchendo todos os critérios de filtro na entidade de destino.
     * </p>
     *
     * @param dto    DTO de requisição com os critérios de pesquisa
     * @param filtro entidade de destino que receberá os valores
     */
    private void mapDtoToEntity(FiltroPesquisaRequestDTO dto, FiltroPesquisa filtro) {
        filtro.setDataInicio(dto.getDataInicio());           // Data de início do período de pesquisa
        filtro.setDataFim(dto.getDataFim());                 // Data de fim do período de pesquisa
        filtro.setTipoProblema(dto.getTipoProblema());       // Tipo de problema a filtrar
        filtro.setStatus(dto.getStatus());                   // Status dos chamados a filtrar
        filtro.setBairro(dto.getBairro());                   // Bairro para filtro geográfico
        filtro.setCidade(dto.getCidade());                   // Cidade para filtro geográfico
        filtro.setOutrosCriterios(dto.getOutrosCriterios()); // Critérios adicionais livres
    }

    /**
     * Converte uma entidade {@link FiltroPesquisa} para {@link FiltroPesquisaResponseDTO}.
     *
     * @param filtro entidade FiltroPesquisa a ser convertida
     * @return DTO de resposta com todos os critérios do filtro e o ID da Prefeitura
     */
    private FiltroPesquisaResponseDTO toResponseDTO(FiltroPesquisa filtro) {
        FiltroPesquisaResponseDTO dto = new FiltroPesquisaResponseDTO();
        dto.setIdFiltro(filtro.getIdFiltro());               // ID único do filtro
        dto.setDataInicio(filtro.getDataInicio());            // Início do período
        dto.setDataFim(filtro.getDataFim());                  // Fim do período
        dto.setTipoProblema(filtro.getTipoProblema());        // Tipo de problema filtrado
        dto.setStatus(filtro.getStatus());                    // Status filtrado
        dto.setBairro(filtro.getBairro());                    // Bairro filtrado
        dto.setCidade(filtro.getCidade());                    // Cidade filtrada
        dto.setOutrosCriterios(filtro.getOutrosCriterios());  // Critérios adicionais
        dto.setIdPrefeitura(filtro.getPrefeitura().getIdPrefeitura()); // ID da Prefeitura dona do filtro
        return dto;
    }
}
