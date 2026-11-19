package br.com.Sabesp.repository;

import br.com.Sabesp.model.TipoProblema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoProblemaRepository extends JpaRepository<TipoProblema, Long> {
}

