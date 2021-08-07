package es.bprojects.courses.webflux.infrastructure.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import es.bprojects.courses.webflux.application.ProductService;
import es.bprojects.courses.webflux.domain.Category;
import es.bprojects.courses.webflux.domain.Product;
import es.bprojects.courses.webflux.infrastructure.controller.dto.ProductDto;
import reactor.core.publisher.Mono;

/**
 * @author ebasanez
 * @since 2021-08-06
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RouterFunctionConfiguration_IT {

	@Value("${config.handler.endpoints}")
	String baseUrl;

	@Autowired
	private ProductService productService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	public void list() {
		// Act
		webTestClient.get()
				.uri(baseUrl)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				// Assert
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBodyList(ProductDto.class)
				.hasSize(9);
	}

	@Test
	public void list_using_consumeWith() {
		// Act
		webTestClient.get()
				.uri(baseUrl)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				// Assert
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBodyList(ProductDto.class)
				.consumeWith(response -> {
					List<ProductDto> products = response.getResponseBody();
					Assertions.assertThat(products.size() == 9).isTrue();
				});
	}

	@Test
	public void get() {
		// Arrange
		Product product = productService.findByName("TV Panasonic").block();

		// Act
		WebTestClient.ResponseSpec response = webTestClient.get()
				.uri(baseUrl + "/{id}", Map.of("id", product.getId()))
				.accept(MediaType.APPLICATION_JSON)
				.exchange();

		// Assert
		response
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.name").isEqualTo("TV PANASONIC");
	}

	@Test
	public void get_using_consumesWith() {
		// Arrange
		Product product = productService.findByName("TV Panasonic").block();

		// Act
		WebTestClient.ResponseSpec response = webTestClient.get()
				.uri(baseUrl + "/{id}", Map.of("id", product.getId()))
				.accept(MediaType.APPLICATION_JSON)
				.exchange();

		// Assert
		response
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody(ProductDto.class)
				.consumeWith(r -> {
					ProductDto dto = r.getResponseBody();
					Assertions.assertThat(dto.getId()).isNotEmpty();
					Assertions.assertThat(dto.getName()).isEqualTo("TV PANASONIC");
				});
	}

	@Test
	public void create() {
		// Arrange
		Category category = productService.findCategoryByName("electronics").block();
		ProductDto dto = new ProductDto();
		dto.setName("New product");
		dto.setPrice(1000f);
		dto.setCategory(category.getId());

		// Act
		WebTestClient.ResponseSpec response = webTestClient.post()
				.uri(baseUrl)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Mono.just(dto), ProductDto.class)
				.exchange();

		// Assert
		response
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.name").isEqualTo("NEW PRODUCT")
				.jsonPath("$.category").isEqualTo(category.getId());
	}

	@Test
	public void edit() {
		// Arrange
		Product originalProduct = productService.findByName("TV Panasonic").block();
		ProductDto editedProduct = new ProductDto();
		editedProduct.setName("edited " + originalProduct.getName());
		editedProduct.setPrice(100f + originalProduct.getPrice());
		editedProduct.setCategory(originalProduct.getCategory());
		// Act
		WebTestClient.ResponseSpec response = webTestClient.put()
				.uri(baseUrl + "/{id}", Map.of("id", originalProduct.getId()))
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Mono.just(editedProduct), ProductDto.class)
				.exchange();

		// Assert
		response
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.name").isEqualTo("EDITED " + originalProduct.getName().toUpperCase())
				.jsonPath("$.price").isEqualTo(100f + originalProduct.getPrice())
				.jsonPath("$.category").isEqualTo(originalProduct.getCategory());
	}

	@Test
	public void delete() {
		// Arrange
		Product product = productService.findByName("Golf stick").block();

		// Act
		WebTestClient.ResponseSpec response = webTestClient.delete()
				.uri(baseUrl + "/{id}", Map.of("id", product.getId()))
				.exchange();

		// Assert
		response.expectStatus().isNoContent();

		// Additional test to check item does not exist anymore.
		webTestClient.get().uri(baseUrl+"/{id}", Map.of("id", product.getId())).exchange()
				.expectStatus().isNotFound();
	}

}