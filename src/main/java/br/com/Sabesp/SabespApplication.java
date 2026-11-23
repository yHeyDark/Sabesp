package br.com.Sabesp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal da aplicação Spring Boot da API de chamados de esgoto (Santos).
 *
 * <p>Ponto de entrada da aplicação; inicializa o contexto Spring
 * com a autoconfiguração e os componentes do pacote {@code br.com.Sabesp}.</p>
 *
 * @author Equipe Sabesp
 */
@SpringBootApplication
public class SabespApplication {

	public static void main(String[] args) {
		SpringApplication.run(SabespApplication.class, args);
	}

}
