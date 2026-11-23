package br.com.Sabesp.service;

import br.com.Sabesp.dto.PrefeituraRequestDTO;
import br.com.Sabesp.dto.PrefeituraResponseDTO;
import br.com.Sabesp.exception.ResourceNotFoundException;
import br.com.Sabesp.model.Prefeitura;
import br.com.Sabesp.repository.PrefeituraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço responsável pelo gerenciamento de contas de Prefeitura no sistema.
 * <p>
 * Permite operações CRUD completas para Prefeituras, que representam as
 * entidades governamentais municipais que acompanham e gerenciam os chamados
 * de esgoto. Cada Prefeitura possui nome, e-mail e senha de acesso.
 * </p>
 *
 * @see Prefeitura
 * @see PrefeituraRepository
 */
@Service
@RequiredArgsConstructor // Lombok gera construtor com todos os campos final (injeção via construtor)
public class PrefeituraService {

    /** Repositório para operações de persistência de Prefeituras no banco de dados. */
    private final PrefeituraRepository prefeituraRepository;

    // ── Métodos de Consulta (Leitura) ────────────────────────────────

    /**
     * Lista todas as Prefeituras cadastradas no sistema.
     *
     * @return lista de {@link PrefeituraResponseDTO} com todas as Prefeituras (sem senhas)
     */
    @Transactional(readOnly = true) // Transação somente leitura — otimiza performance
    public List<PrefeituraResponseDTO> listarTodos() {
        // Busca todas as Prefeituras e converte cada uma para DTO via stream
        return prefeituraRepository.findAll()
                .stream()
                .map(this::toResponseDTO) // Converte cada Prefeitura para PrefeituraResponseDTO
                .collect(Collectors.toList());
    }

    /**
     * Busca uma Prefeitura específica pelo seu identificador único.
     *
     * @param id identificador da Prefeitura
     * @return {@link PrefeituraResponseDTO} com os dados da Prefeitura encontrada
     * @throws ResourceNotFoundException se a Prefeitura não for encontrada
     */
    @Transactional(readOnly = true)
    public PrefeituraResponseDTO buscarPorId(Long id) {
        // Busca a Prefeitura pelo ID; lança exceção 404 se não existir
        Prefeitura prefeitura = prefeituraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prefeitura", id));
        return toResponseDTO(prefeitura);
    }

    // ── Métodos de Escrita (Criação, Atualização, Exclusão) ──────────

    /**
     * Cria uma nova conta de Prefeitura no sistema.
     *
     * @param dto dados da Prefeitura (nome, e-mail, senha)
     * @return {@link PrefeituraResponseDTO} com os dados da Prefeitura criada
     */
    @Transactional // Transação de escrita — permite inserção no banco
    public PrefeituraResponseDTO criar(PrefeituraRequestDTO dto) {
        // Cria uma nova entidade Prefeitura e preenche com os dados do DTO
        Prefeitura prefeitura = new Prefeitura();
        prefeitura.setNome(dto.getNome());       // Nome da Prefeitura (ex.: "Prefeitura de São Paulo")
        prefeitura.setEmail(dto.getEmail());     // E-mail de acesso da Prefeitura
        prefeitura.setSenha(dto.getSenha());     // Senha de acesso
        // Persiste a nova Prefeitura no banco de dados
        Prefeitura salva = prefeituraRepository.save(prefeitura);
        return toResponseDTO(salva);
    }

    /**
     * Atualiza os dados de uma Prefeitura existente.
     * <p>
     * A senha só é atualizada se for informada e não estiver em branco,
     * evitando sobrescrita acidental com valor nulo.
     * </p>
     *
     * @param id  identificador da Prefeitura a ser atualizada
     * @param dto novos dados da Prefeitura
     * @return {@link PrefeituraResponseDTO} com os dados da Prefeitura atualizada
     * @throws ResourceNotFoundException se a Prefeitura não for encontrada
     */
    @Transactional // Transação de escrita — permite alteração no banco
    public PrefeituraResponseDTO atualizar(Long id, PrefeituraRequestDTO dto) {
        // Busca a Prefeitura existente; lança exceção 404 se não encontrar
        Prefeitura prefeitura = prefeituraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prefeitura", id));

        // Atualiza os campos básicos
        prefeitura.setNome(dto.getNome());       // Atualiza o nome
        prefeitura.setEmail(dto.getEmail());     // Atualiza o e-mail

        // Atualiza a senha somente se foi informada e não está em branco
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            prefeitura.setSenha(dto.getSenha());
        }

        // Persiste as alterações no banco de dados
        Prefeitura atualizada = prefeituraRepository.save(prefeitura);
        return toResponseDTO(atualizada);
    }

    /**
     * Remove uma Prefeitura do sistema pelo seu identificador.
     *
     * @param id identificador da Prefeitura a ser removida
     * @throws ResourceNotFoundException se a Prefeitura não for encontrada
     */
    @Transactional // Transação de escrita — permite exclusão no banco
    public void deletar(Long id) {
        // Verifica se a Prefeitura existe antes de tentar excluir
        if (!prefeituraRepository.existsById(id)) {
            throw new ResourceNotFoundException("Prefeitura", id);
        }
        // Remove a Prefeitura do banco de dados
        prefeituraRepository.deleteById(id);
    }

    // ── Conversão (Entidade → DTO de Resposta) ──────────────────────

    /**
     * Converte uma entidade {@link Prefeitura} para {@link PrefeituraResponseDTO}.
     * <p>
     * <strong>Nota de segurança:</strong> a senha da Prefeitura é intencionalmente
     * omitida do DTO de resposta, assim como ocorre com a senha de usuários.
     * </p>
     *
     * @param prefeitura entidade Prefeitura a ser convertida
     * @return DTO de resposta com os dados da Prefeitura (sem a senha)
     */
    private PrefeituraResponseDTO toResponseDTO(Prefeitura prefeitura) {
        PrefeituraResponseDTO dto = new PrefeituraResponseDTO();
        dto.setIdPrefeitura(prefeitura.getIdPrefeitura()); // ID único da Prefeitura
        dto.setNome(prefeitura.getNome());                  // Nome da Prefeitura
        dto.setEmail(prefeitura.getEmail());                // E-mail de contato/acesso
        // NOTA: a senha NÃO é incluída no DTO de resposta por motivos de segurança
        return dto;
    }
}
