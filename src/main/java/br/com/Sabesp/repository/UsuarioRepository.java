package br.com.Sabesp.repository;

import br.com.Sabesp.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório de {@link Usuario} (usuários da plataforma de chamados).
 *
 * <p>Fornece consulta para buscar um usuário pelo seu e-mail de acesso.</p>
 *
 * @author Equipe Sabesp
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca um usuário pelo seu e-mail.
     *
     * @param email e-mail do usuário
     * @return {@link Optional} contendo o usuário, ou vazio se não encontrado
     */
    Optional<Usuario> findByEmail(String email);
}

