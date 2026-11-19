package br.com.Sabesp.repository;

import br.com.Sabesp.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByChamadoIdChamado(Long idChamado);
}

