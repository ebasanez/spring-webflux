package es.bprojects.courses.webflux.domain.service;

import java.io.File;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import es.bprojects.courses.webflux.application.ProductService;
import es.bprojects.courses.webflux.domain.Category;
import es.bprojects.courses.webflux.domain.Product;
import es.bprojects.courses.webflux.infrastructure.persistence.CategoryRepository;
import es.bprojects.courses.webflux.infrastructure.persistence.ProductsRepository;
import es.bprojects.courses.webflux.infrastructure.utils.FileProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author ebasanez
 * @since 2021-06-02
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

	private final ProductsRepository productsRepository;
	private final CategoryRepository categoryRepository;
	private final FileProperties fileProperties;

	@Override
	public Flux<Product> findAll() {
		return productsRepository.findAll().map(this::toDomain);
	}

	@Override
	public Mono<Product> findById(String id) {
		return productsRepository.findById(id).map(this::toDomain);
	}

	@Override
	public Mono<Product> findByName(String name) {
		return productsRepository.findByName(name).map(this::toDomain);
	}

	@Override
	public Mono<Product> insert(Product product, FilePart photo) {
		String photoFileName = generateFileName(photo);
		return toEntity(product, photoFileName)
				.flatMap(productsRepository::save)
				.flatMap(p -> {
					if (photoFileName != null) {
						photo.transferTo(new File(fileProperties.getUploadFileDestination() + photoFileName));
					}
					return Mono.just(toDomain(p));
				}).switchIfEmpty(Mono.error(new Exception("Error saving product")));
	}

	@Override
	public Mono<Product> update(Product product, FilePart file) {
		String fileName = generateFileName(file);
		return productsRepository.findById(product.getId())
				.flatMap(p ->
				{
					log.info(p.toString());
					return categoryRepository.findById(product.getCategory())
							.map(c -> {
										p.setName(product.getName());
										p.setPrice(product.getPrice());
										p.setPhoto(fileName);
										p.setCategory(c);
										return p;
									}
							).switchIfEmpty(Mono.error(new IllegalArgumentException("No category "+ product.getCategory())));
				})
				.flatMap(productsRepository::save)
				.flatMap(p -> {
					if (fileName != null) {
						file.transferTo(new File(fileProperties.getUploadFileDestination() + fileName));
					}
					return Mono.just(toDomain(p));
				});
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
	public Mono<Category> findCategoryByName(@NotNull String name) {
		return categoryRepository.findByName(name).map(this::toDomain);
	}

	@Override
	public Mono<Category> insert(@NotNull @Valid Category category) {
		es.bprojects.courses.webflux.infrastructure.persistence.model.Category entity =
				new es.bprojects.courses.webflux.infrastructure.persistence.model.Category();
		toEntity(entity, category);
		return categoryRepository.save(entity).map(this::toDomain);
	}

	private final Mono<es.bprojects.courses.webflux.infrastructure.persistence.model.Product> toEntity(Product domain, String photoFileName) {

		return categoryRepository.findById(domain.getCategory())
				.map(c -> {
					es.bprojects.courses.webflux.infrastructure.persistence.model.Product p =
							new es.bprojects.courses.webflux.infrastructure.persistence.model.Product();
					p.setName(domain.getName());
					p.setPrice(domain.getPrice());
					p.setPhoto(photoFileName);
					p.setCreatedAt(new Date());
					p.setCategory(c);
					return p;
				});
	}

	private final Product toDomain(es.bprojects.courses.webflux.infrastructure.persistence.model.Product entity) {
		return Product.builder()
				.id(entity.getId())
				.name(entity.getName().toUpperCase(Locale.ROOT))
				.price(entity.getPrice())
				.category(entity.getCategory().getId())
				.photo(entity.getPhoto())
				.createdAt(entity.getCreatedAt())
				.build();
	}

	private final Category toDomain(es.bprojects.courses.webflux.infrastructure.persistence.model.Category entity) {
		return Category.builder()
				.id(entity.getId())
				.name(entity.getName().toUpperCase(Locale.ROOT))
				.build();
	}

	private final es.bprojects.courses.webflux.infrastructure.persistence.model.Category toEntity(
			es.bprojects.courses.webflux.infrastructure.persistence.model.Category entity, Category domain) {
		entity.setName(domain.getName());
		return entity;
	}

	private String generateFileName(final FilePart filePart) {
		if (filePart == null || filePart.filename().isEmpty()) {
			return null;
		}
		return UUID.randomUUID().toString() + "-" + filePart.filename()
				.replace(" ", "")
				.replace(":", "")
				.replace("\\", "");

	}


}
