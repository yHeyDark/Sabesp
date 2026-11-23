package br.com.Sabesp.repository;

import br.com.Sabesp.model.TipoProblema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório de {@link TipoProblema} (tipos de problema reportados nos chamados).
 *
 * <p>Utiliza apenas as operações padrão do Spring Data JPA
 * ({@code findAll}, {@code save}, {@code findById}, etc.).</p>
 *
 * @author Equipe Sabesp
 */
@Repository
public interface TipoProblemaRepository extends JpaRepository<TipoProblema, Long> {
}

