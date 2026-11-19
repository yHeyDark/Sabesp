package br.com.Sabesp.repository;

import br.com.Sabesp.model.Prefeitura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrefeituraRepository extends JpaRepository<Prefeitura, Long> {

    Optional<Prefeitura> findByEmail(String email);
}

