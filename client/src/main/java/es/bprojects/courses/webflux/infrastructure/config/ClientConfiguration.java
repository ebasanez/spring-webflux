package es.bprojects.courses.webflux.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author ebasanez
 * @since 2021-08-07
 */
@Configuration
public class ClientConfiguration {

	@Value("${services.server.host}")
	private String serverHost;

	@Bean
	public WebClient webClient(){
		return WebClient.create(serverHost);
	}

}
