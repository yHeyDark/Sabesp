package br.com.Sabesp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do OpenAPI/springdoc da API de chamados de esgoto.
 *
 * <p>Define os metadados (título, descrição, versão, contato e licença)
 * exibidos na documentação Swagger disponível em {@code /swagger-ui.html}.</p>
 *
 * @author Equipe Sabesp
 */
@Configuration
public class OpenApiConfig {

    /**
     * Cria o bean {@link OpenAPI} com as informações gerais da documentação.
     *
     * @return objeto {@link OpenAPI} configurado com título, descrição,
     *         versão, contato e licença da API
     */
    @Bean
    public OpenAPI sabespOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sabesp — API de Chamados para Problemas de Esgoto")
                        .description("API REST para gerenciamento de chamados de problemas de esgoto, "
                                + "permitindo que cidadãos registrem ocorrências e a Sabesp/Prefeitura "
                                + "acompanhe e resolva os problemas.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe Sabesp")
                                .email("suporte@sabesp.com.br"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}

