package br.com.Sabesp.service;

import br.com.Sabesp.dto.EnderecoRequestDTO;
import br.com.Sabesp.dto.EnderecoResponseDTO;
import br.com.Sabesp.exception.ResourceNotFoundException;
import br.com.Sabesp.model.Endereco;
import br.com.Sabesp.repository.EnderecoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço responsável pelo gerenciamento de endereços no sistema.
 * <p>
 * Permite operações CRUD completas sobre endereços, além de buscas
 * filtradas por bairro e cidade. Os endereços são utilizados pelos
 * chamados de esgoto para indicar a localização do problema.
 * </p>
 *
 * @see Endereco
 * @see EnderecoRepository
 */
@Service
@RequiredArgsConstructor // Lombok gera construtor com todos os campos final (injeção via construtor)
public class EnderecoService {

    /** Repositório para operações de persistência de endereços no banco de dados. */
    private final EnderecoRepository enderecoRepository;

    // ── Métodos de Consulta (Leitura) ────────────────────────────────

    /**
     * Lista todos os endereços cadastrados no sistema.
     *
     * @return lista de {@link EnderecoResponseDTO} com todos os endereços
     */
    @Transactional(readOnly = true) // Transação somente leitura — otimiza performance
    public List<EnderecoResponseDTO> listarTodos() {
        // Busca todos os endereços e converte cada um para DTO via stream
        return enderecoRepository.findAll()
                .stream()
                .map(this::toResponseDTO) // Converte cada Endereco para EnderecoResponseDTO
                .collect(Collectors.toList());
    }

    /**
     * Busca um endereço específico pelo seu identificador único.
     *
     * @param id identificador do endereço
     * @return {@link EnderecoResponseDTO} com os dados do endereço encontrado
     * @throws ResourceNotFoundException se o endereço não for encontrado
     */
    @Transactional(readOnly = true)
    public EnderecoResponseDTO buscarPorId(Long id) {
        // Busca o endereço pelo ID; lança exceção 404 se não existir
        Endereco endereco = enderecoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço", id));
        return toResponseDTO(endereco);
    }

    /**
     * Busca todos os endereços localizados em um determinado bairro.
     *
     * @param bairro nome do bairro para filtro
     * @return lista de {@link EnderecoResponseDTO} do bairro informado
     */
    @Transactional(readOnly = true)
    public List<EnderecoResponseDTO> buscarPorBairro(String bairro) {
        // Consulta derivada do Spring Data — filtra por nome do bairro
        return enderecoRepository.findByBairro(bairro)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca todos os endereços localizados em uma determinada cidade.
     *
     * @param cidade nome da cidade para filtro
     * @return lista de {@link EnderecoResponseDTO} da cidade informada
     */
    @Transactional(readOnly = true)
    public List<EnderecoResponseDTO> buscarPorCidade(String cidade) {
        // Consulta derivada do Spring Data — filtra por nome da cidade
        return enderecoRepository.findByCidade(cidade)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ── Métodos de Escrita (Criação, Atualização, Exclusão) ──────────

    /**
     * Cria um novo endereço no sistema.
     *
     * @param dto dados do endereço a ser criado (logradouro, número, bairro, etc.)
     * @return {@link EnderecoResponseDTO} com os dados do endereço criado
     */
    @Transactional // Transação de escrita — permite inserção no banco
    public EnderecoResponseDTO criar(EnderecoRequestDTO dto) {
        // Cria uma nova entidade e mapeia os campos do DTO
        Endereco endereco = new Endereco();
        mapDtoToEntity(dto, endereco); // Preenche os campos da entidade com os dados do DTO
        // Persiste o endereço no banco de dados
        Endereco salvo = enderecoRepository.save(endereco);
        return toResponseDTO(salvo);
    }

    /**
     * Atualiza um endereço existente com novos dados.
     *
     * @param id  identificador do endereço a ser atualizado
     * @param dto novos dados do endereço
     * @return {@link EnderecoResponseDTO} com os dados do endereço atualizado
     * @throws ResourceNotFoundException se o endereço não for encontrado
     */
    @Transactional // Transação de escrita — permite alteração no banco
    public EnderecoResponseDTO atualizar(Long id, EnderecoRequestDTO dto) {
        // Busca o endereço existente; lança exceção 404 se não encontrar
        Endereco endereco = enderecoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço", id));
        // Atualiza os campos da entidade com os novos dados do DTO
        mapDtoToEntity(dto, endereco);
        // Persiste as alterações no banco de dados
        Endereco atualizado = enderecoRepository.save(endereco);
        return toResponseDTO(atualizado);
    }

    /**
     * Remove um endereço do sistema pelo seu identificador.
     *
     * @param id identificador do endereço a ser removido
     * @throws ResourceNotFoundException se o endereço não for encontrado
     */
    @Transactional // Transação de escrita — permite exclusão no banco
    public void deletar(Long id) {
        // Verifica se o endereço existe antes de tentar excluir
        if (!enderecoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Endereço", id);
        }
        // Remove o endereço do banco de dados
        enderecoRepository.deleteById(id);
    }

    // ── Conversão e Mapeamento ──────────────────────────────────────

    /**
     * Mapeia os campos de um {@link EnderecoRequestDTO} para uma entidade {@link Endereco}.
     * <p>
     * Método auxiliar reutilizado tanto na criação quanto na atualização
     * de endereços, evitando duplicação de código de mapeamento.
     * </p>
     *
     * @param dto      DTO de requisição com os dados de entrada
     * @param endereco entidade de destino que receberá os valores
     */
    private void mapDtoToEntity(EnderecoRequestDTO dto, Endereco endereco) {
        endereco.setLogradouro(dto.getLogradouro());   // Rua/Avenida/Travessa
        endereco.setNumero(dto.getNumero());            // Número do imóvel
        endereco.setComplemento(dto.getComplemento());  // Complemento (apto, bloco, etc.)
        endereco.setBairro(dto.getBairro());            // Bairro
        endereco.setCidade(dto.getCidade());            // Cidade
        endereco.setEstado(dto.getEstado());            // Estado (UF)
        endereco.setCep(dto.getCep());                  // CEP (Código de Endereçamento Postal)
    }

    /**
     * Converte uma entidade {@link Endereco} para {@link EnderecoResponseDTO}.
     * <p>
     * Método público utilizado por outros serviços (como ChamadoService)
     * para incluir os dados de endereço nas respostas de chamados.
     * </p>
     *
     * @param endereco entidade Endereco a ser convertida
     * @return DTO de resposta com todos os campos do endereço
     */
    public EnderecoResponseDTO toResponseDTO(Endereco endereco) {
        EnderecoResponseDTO dto = new EnderecoResponseDTO();
        dto.setIdEndereco(endereco.getIdEndereco());     // ID único do endereço
        dto.setLogradouro(endereco.getLogradouro());     // Rua/Avenida
        dto.setNumero(endereco.getNumero());              // Número
        dto.setComplemento(endereco.getComplemento());    // Complemento
        dto.setBairro(endereco.getBairro());              // Bairro
        dto.setCidade(endereco.getCidade());              // Cidade
        dto.setEstado(endereco.getEstado());              // Estado
        dto.setCep(endereco.getCep());                    // CEP
        return dto;
    }
}
