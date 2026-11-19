package br.com.Sabesp.repository;

import br.com.Sabesp.model.FiltroPesquisa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FiltroPesquisaRepository extends JpaRepository<FiltroPesquisa, Long> {

    List<FiltroPesquisa> findByPrefeituraIdPrefeitura(Long idPrefeitura);
}

