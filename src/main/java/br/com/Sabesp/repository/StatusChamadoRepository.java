package br.com.Sabesp.repository;

import br.com.Sabesp.model.StatusChamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatusChamadoRepository extends JpaRepository<StatusChamado, Long> {

    Optional<StatusChamado> findByDescricao(String descricao);
}

