package es.bprojects.courses.webflux.infrastructure.config;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import es.bprojects.courses.webflux.infrastructure.handler.ProductHandler;

/**
 * @author ebasanez
 * @since 2021-08-08
 */
@Configuration
public class RouterConfiguration {

	@Value("${product.handler.baseUrl}")
	private String baseUrl;

	@Bean
	RouterFunction<ServerResponse> productRoutes(ProductHandler productHandler) {
		return route(GET(baseUrl), r -> productHandler.list())
				.andRoute(GET(baseUrl + "/{id}"), productHandler::get)
				.andRoute(POST(baseUrl), productHandler::create)
				.andRoute(PUT(baseUrl + "/{id}"), productHandler::update)
				.andRoute(DELETE(baseUrl + "/{id}"), productHandler::delete)
				.andRoute(PUT(baseUrl + "/{id}/photo"), productHandler::uploadPhoto)
				.andRoute(POST(baseUrl + "/photo"), productHandler::createWithPhoto)
				;

	}

}
