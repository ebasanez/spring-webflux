package es.bprojects.coures.webflux.domain.service;

import java.util.Date;
import java.util.Locale;

import org.springframework.stereotype.Service;

import es.bprojects.coures.webflux.application.ProductService;
import es.bprojects.coures.webflux.domain.Product;
import es.bprojects.coures.webflux.infrastructure.persistence.ProductsRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author ebasanez
 * @since 2021-06-02
 */

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

	private final ProductsRepository productsRepository;

	@Override
	public Flux<Product> findAll() {
		return productsRepository.findAll().map(this::toDomain);
	}

	@Override
	public Mono<Product> findById(String id) {
		return productsRepository.findById(id).map(this::toDomain);
	}

	@Override
	public Mono<Product> insert(Product product) {
		es.bprojects.coures.webflux.infrastructure.persistence.model.Product entity = new es.bprojects.coures.webflux.infrastructure.persistence.model.Product();
		toEntity(entity,product);
		entity.setCreatedAt(new Date());
		return productsRepository.save(entity).map(this::toDomain);
	}

	@Override
	public Mono<Product> update(Product product) {
		Mono<es.bprojects.coures.webflux.infrastructure.persistence.model.Product> entity = productsRepository.findById(product.getId());
		return entity
				.map(e ->toEntity(e,product))
				.flatMap(productsRepository::save)
				.map(this::toDomain);
	}

	@Override
	public Mono<Void> delete(String id) {
		return productsRepository.deleteById(id);
	}

	private final es.bprojects.coures.webflux.infrastructure.persistence.model.Product toEntity(
			es.bprojects.coures.webflux.infrastructure.persistence.model.Product entity, Product domain) {
		entity.setName(domain.getName());
		entity.setPrice(domain.getPrice());
		return entity;
	}

	private final Product toDomain(es.bprojects.coures.webflux.infrastructure.persistence.model.Product entity) {
		return Product.builder()
				.id(entity.getId())
				.name(entity.getName().toUpperCase(Locale.ROOT))
				.price(entity.getPrice())
				.createdAt(entity.getCreatedAt())
				.build();
	}

}
