package es.bprojects.courses.webflux.domain.service;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import es.bprojects.courses.webflux.application.ProductService;
import es.bprojects.courses.webflux.domain.Product;
import es.bprojects.courses.webflux.infrastructure.client.dto.ProductClientDto;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author ebasanez
 * @since 2021-08-08
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

	private final WebClient webClient;

	@Override
	public Flux<Product> findAll() {
		return webClient.get()
				.accept(APPLICATION_JSON)
				.retrieve()
				.bodyToFlux(ProductClientDto.class)
				.map(this::toDomain);
	}

	@Override
	public Mono<Product> findById(String id) {
		return webClient.get()
				.uri("/{id}", Map.of("id", id))
				.accept(APPLICATION_JSON)
				// You can use retrieve + bodyToMono or exchangeToMono indistinctly
				.retrieve()
				.bodyToMono(ProductClientDto.class)
				.map(this::toDomain);
	}

	@Override
	public Mono<Product> insert(Product product) {
		return webClient.post()
				.accept(APPLICATION_JSON)
				.contentType(APPLICATION_JSON)
				.bodyValue(toDto(product))
				.retrieve()
				.bodyToMono(ProductClientDto.class)
				.map(this::toDomain);
	}

	@Override
	public Mono<Product> update(Product product) {
		return webClient.put()
				.uri("/{id}", Map.of("id", product.getId()))
				.accept(APPLICATION_JSON)
				.contentType(APPLICATION_JSON)
				.bodyValue(toDto(product))
				.retrieve()
				.bodyToMono(ProductClientDto.class)
				.map(this::toDomain);
	}

	@Override
	public Mono<Void> delete(String id) {
		return webClient.delete()
				.uri("/{id}", Map.of("id", id))
				.accept(APPLICATION_JSON)
				.retrieve()
				.bodyToMono(Void.class);
	}

	@Override
	public Mono<Product> upload(String id, FilePart file) {
		// Esto es así porque sí
		MultipartBodyBuilder parts = new MultipartBodyBuilder();
		parts
				.asyncPart("file", file.content(), DataBuffer.class)
				.headers(h -> h.setContentDispositionFormData("file", file.filename()));
		return webClient.put()
				.uri("/{id}/photo", Map.of("id", id))
				.contentType(MULTIPART_FORM_DATA)
				.bodyValue(parts.build())
				.retrieve()
				.bodyToMono(ProductClientDto.class)
				.map(this::toDomain);
	}

	private final Product toDomain(ProductClientDto dto) {
		return Product.builder()
				.id(dto.getId())
				.name(dto.getName())
				.price(dto.getPrice())
				.category(dto.getCategory())
				.photo(dto.getPhoto())
				.createdAt(dto.getCreatedAt())
				.build();
	}


	private final ProductClientDto toDto(Product entity) {
		final ProductClientDto dto = new ProductClientDto();
		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setPrice(entity.getPrice());
		dto.setCategory(entity.getCategory());
		dto.setPhoto(entity.getPhoto());
		dto.setCreatedAt(entity.getCreatedAt());
		return dto;
	}
}
