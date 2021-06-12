package es.bprojects.coures.webflux.domain.service;

import java.util.Date;
import java.util.Locale;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Service;

import es.bprojects.coures.webflux.application.ProductService;
import es.bprojects.coures.webflux.domain.Category;
import es.bprojects.coures.webflux.domain.Product;
import es.bprojects.coures.webflux.infrastructure.persistence.CategoryRepository;
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
	private final CategoryRepository categoryRepository;

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
		return toEntity(product)
				.flatMap(productsRepository::save)
				.map(this::toDomain);
	}

	@Override
	public Mono<Product> update(Product product) {
		return productsRepository.findById(product.getId())
				.flatMap(p ->
						categoryRepository.findById(product.getCategory())
								.map(c -> {
									p.setName(product.getName());
									p.setPrice(product.getPrice());
									p.setCategory(c);
									return p;
								}
						)
				)
				.flatMap(productsRepository::save)
				.map(this::toDomain);
	}

	@Override
	public Mono<Void> deleteProduct(String id) {
		return productsRepository.deleteById(id);
	}

	/*
	Category methods
	 */
	@Override
	public Flux<Category> findAllCategories() {
		return categoryRepository.findAll().map(this::toDomain);
	}

	@Override
	public Mono<Category> findCategoryById(String id) {
		return categoryRepository.findById(id).map(this::toDomain);
	}

	@Override
	public Mono<Category> insert(@NotNull @Valid Category category) {
		es.bprojects.coures.webflux.infrastructure.persistence.model.Category entity =
				new es.bprojects.coures.webflux.infrastructure.persistence.model.Category();
		toEntity(entity, category);
		return categoryRepository.save(entity).map(this::toDomain);
	}

	private final Mono<es.bprojects.coures.webflux.infrastructure.persistence.model.Product> toEntity(Product domain) {

		return categoryRepository.findById(domain.getCategory())
				.map(c -> {
					es.bprojects.coures.webflux.infrastructure.persistence.model.Product p =
							new es.bprojects.coures.webflux.infrastructure.persistence.model.Product();
					p.setName(domain.getName());
					p.setPrice(domain.getPrice());
					p.setCreatedAt(new Date());
					p.setCategory(c);
					return p;
				});
	}

	private final Product toDomain(es.bprojects.coures.webflux.infrastructure.persistence.model.Product entity) {
		return Product.builder()
				.id(entity.getId())
				.name(entity.getName().toUpperCase(Locale.ROOT))
				.price(entity.getPrice())
				.category(entity.getCategory().getId())
				.createdAt(entity.getCreatedAt())
				.build();
	}

	private final Category toDomain(es.bprojects.coures.webflux.infrastructure.persistence.model.Category entity) {
		return Category.builder()
				.id(entity.getId())
				.name(entity.getName().toUpperCase(Locale.ROOT))
				.build();
	}

	private final es.bprojects.coures.webflux.infrastructure.persistence.model.Category toEntity(
			es.bprojects.coures.webflux.infrastructure.persistence.model.Category entity, Category domain) {
		entity.setName(domain.getName());
		return entity;
	}

}
