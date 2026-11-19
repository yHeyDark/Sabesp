package br.com.Sabesp.repository;

import br.com.Sabesp.model.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    List<Chamado> findByUsuarioIdUsuario(Long idUsuario);

    List<Chamado> findByStatusChamadoIdStatus(Long idStatus);

    List<Chamado> findByEnderecoBairro(String bairro);

    long countByStatusChamadoDescricao(String descricao);
}

