package es.bprojects.coures.webflux.domain.service;

import java.io.File;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import es.bprojects.coures.webflux.application.ProductService;
import es.bprojects.coures.webflux.domain.Category;
import es.bprojects.coures.webflux.domain.Product;
import es.bprojects.coures.webflux.infrastructure.persistence.CategoryRepository;
import es.bprojects.coures.webflux.infrastructure.persistence.ProductsRepository;
import es.bprojects.coures.webflux.infrastructure.utils.FileProperties;
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
	public Mono<Void> insert(Product product, FilePart photo) {
		String photoFileName = generateFileName(photo);
		return toEntity(product, photoFileName)
				.flatMap(productsRepository::save)
				.flatMap(p -> {
					if (photoFileName != null) {
						return photo.transferTo(new File(fileProperties.getUploadFileDestination() + photoFileName));
					} else {
						return Mono.empty();
					}
				});
	}

	@Override
	public Mono<Void> update(Product product, FilePart file) {
		String fileName = generateFileName(file);
		return productsRepository.findById(product.getId())
				.flatMap(p ->
						categoryRepository.findById(product.getCategory())
								.map(c -> {
											p.setName(product.getName());
											p.setPrice(product.getPrice());
											p.setPhoto(fileName);
											p.setCategory(c);
											return p;
										}
								)
				)
				.flatMap(productsRepository::save)
				.flatMap(p -> {
					if (fileName != null) {
						return file.transferTo(new File(fileProperties.getUploadFileDestination() + fileName));
					} else {
						return Mono.empty();
					}
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
	public Mono<Category> insert(@NotNull @Valid Category category) {
		es.bprojects.coures.webflux.infrastructure.persistence.model.Category entity =
				new es.bprojects.coures.webflux.infrastructure.persistence.model.Category();
		toEntity(entity, category);
		return categoryRepository.save(entity).map(this::toDomain);
	}

	private final Mono<es.bprojects.coures.webflux.infrastructure.persistence.model.Product> toEntity(Product domain, String photoFileName) {

		return categoryRepository.findById(domain.getCategory())
				.map(c -> {
					es.bprojects.coures.webflux.infrastructure.persistence.model.Product p =
							new es.bprojects.coures.webflux.infrastructure.persistence.model.Product();
					p.setName(domain.getName());
					p.setPrice(domain.getPrice());
					p.setPhoto(photoFileName);
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
				.photo(entity.getPhoto())
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
