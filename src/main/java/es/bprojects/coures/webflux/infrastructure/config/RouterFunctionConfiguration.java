package es.bprojects.coures.webflux.infrastructure.config;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import es.bprojects.coures.webflux.infrastructure.handler.ProductHandler;

/**
 * @author ebasanez
 * @since 2021-07-13
 */
@Configuration
public class RouterFunctionConfiguration {

	@Autowired
	private ProductHandler productHandler;

	@Bean
	public RouterFunction<ServerResponse> routes() {
		return route(GET("/api/v2/products"), productHandler::list)
				.andRoute(GET("/api/v2/products/{id}"), productHandler::get)
				.andRoute(POST("/api/v2/products"), productHandler::create)
				.andRoute(PUT("/api/v2/products/{id}"), productHandler::update)
				.andRoute(DELETE("/api/v2/products/{id}"),productHandler::delete)
				.andRoute(PUT("/api/v2/products/{id}/photo"), productHandler::upload)
				.andRoute(POST("/api/v2/products/photo"), productHandler::createWithPhoto)
				;
	}

}
