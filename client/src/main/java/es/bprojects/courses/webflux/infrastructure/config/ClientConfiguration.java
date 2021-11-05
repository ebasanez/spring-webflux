package es.bprojects.courses.webflux.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author ebasanez
 * @since 2021-08-07
 */
@Configuration
public class ClientConfiguration {

	@Value("${services.server}")
	private String server;

	@Bean
	@LoadBalanced
	public WebClient.Builder webClient(){
		return WebClient.builder().baseUrl(server);
	}

}
