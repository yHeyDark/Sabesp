package br.com.Sabesp.repository;

import br.com.Sabesp.model.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

    List<Endereco> findByBairro(String bairro);

    List<Endereco> findByCidade(String cidade);
}

