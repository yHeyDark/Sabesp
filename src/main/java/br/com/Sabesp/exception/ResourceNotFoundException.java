package br.com.Sabesp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando um recurso não é encontrado.
 *
 * <p>Mapeada para HTTP 404 ({@code NOT_FOUND}) e tratada
 * globalmente pelo {@link GlobalExceptionHandler}.</p>
 *
 * @author Equipe Sabesp
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Cria a exceção com uma mensagem descritiva.
     *
     * @param message mensagem explicando o que não foi encontrado
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Cria a exceção identificando o recurso e o id não encontrados.
     *
     * @param resourceName nome do recurso (ex.: "Chamado", "Usuário")
     * @param id identificador do recurso não encontrado
     */
    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s não encontrado(a) com id: %d", resourceName, id));
    }
}

