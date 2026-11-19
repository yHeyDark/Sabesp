package br.com.Sabesp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

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

