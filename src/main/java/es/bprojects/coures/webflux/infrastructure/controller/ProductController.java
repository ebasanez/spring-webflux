package es.bprojects.coures.webflux.infrastructure.controller;

import java.time.Duration;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

import es.bprojects.coures.webflux.application.ProductService;
import es.bprojects.coures.webflux.domain.Category;
import es.bprojects.coures.webflux.domain.Product;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author ebasanez
 * @since 2021-05-30
 */
@Slf4j
@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productsService;

	@GetMapping
	public Mono<String> list(Model model) {
		Flux<Product> products = productsService.findAll();
		model.addAttribute("products", products);
		model.addAttribute("title", "Product list");
		return Mono.just("list");
	}

	/**
	 * Similar to list, but handling back-pressure with data-driver.
	 */
	@GetMapping(params = "data-driver=true")
	public Mono<String> listDataDriver(Model model) {
		// Add a delay to the flux to show how webflux will handle it
		Flux<Product> products = productsService.findAll().delayElements(Duration.ofSeconds(1));
		// Instead of the flux, we put in the model a ReactiveDataDriverContextVariable that wraps and handle de Flux with a buffers size (2):
		// Thymeleaf will handle the back-pressure automatically, rendering those as are published.
		model.addAttribute("products", new ReactiveDataDriverContextVariable(products, 2));
		model.addAttribute("title", "Product list");
		return Mono.just("list");
	}

	/**
	 * Similar to list, but handling back-pressure with chunks (configured via properties in application.properties).
	 */
	@GetMapping(params = "chunked=full")
	public Mono<String> listChunked(Model model) {
		Flux<Product> products = productsService.findAll().repeat(5000);
		model.addAttribute("products", products);
		model.addAttribute("title", "Product list");
		return Mono.just("list-chunked");
	}

	@GetMapping("/form")
	public Mono<String> showForm(Model model) {
		model.addAttribute("title", "New title form");
		model.addAttribute("product", Mono.just(new ProductDto()));
		return Mono.just("form");
	}

	@GetMapping("/form/{id}")
	public Mono<String> showEditForm(@PathVariable String id, Model model) {
		model.addAttribute("title", "New title form");
		model.addAttribute("product", productsService.findById(id).map(this::toDto).defaultIfEmpty(new ProductDto()));
		return Mono.just("form");
	}

	@PostMapping("/form")
	public Mono<String> insert(@Valid ProductDto product, BindingResult bindingResult, Model model) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("title", "Errors on title form");
			model.addAttribute("product", product);
			return Mono.just("form");
		}
		Product productBean = Product.builder().name(product.name).price(product.price).category(product.category).build();
		return productsService.insert(productBean).doOnNext(p -> {
			log.info("Product {} inserted", product.getName());
		}).thenReturn("redirect:/product");
	}

	@PostMapping("/form/{id}")
	public Mono<String> update(@PathVariable String id, ProductDto inputDto) {
		Product product = Product.builder()
				.id(id)
				.name(inputDto.name)
				.price(inputDto.price)
				.category(inputDto.category)
				.build();
		return productsService.update(product)
				.doOnNext(p -> log.info("Product {} inserted", product.getName()))
				.thenReturn("redirect:/product");
	}

	@PostMapping("/delete/{id}")
	public Mono<String> delete(@PathVariable String id) {
		return productsService.findById(id)
				.switchIfEmpty(Mono.error(new InterruptedException("Product id does not exist")))
				.map(Product::getId)
				.flatMap(productsService::deleteProduct)
				.then(Mono.just("redirect:/product?message=product-deleted"))
				.onErrorResume(ex -> Mono.just("redirect:/product?message=product-id-does-not-exist"));
	}

	@ModelAttribute("categories")
	Flux<Category> getCategories(){
		return productsService.findAllCategories();
	}

	@Getter
	@Setter
	@ToString
	@EqualsAndHashCode
	public static class ProductDto {
		private String id;
		@NotNull
		private String name;
		@Min(0)
		@NotNull
		private Float price;
		@NotNull
		private String category;

	}

	private ProductDto toDto(Product bean) {
		ProductDto dto = new ProductDto();
		dto.setId(bean.getId());
		dto.setName(bean.getName());
		dto.setPrice(bean.getPrice());
		dto.setCategory(bean.getCategory());
		return dto;
	}

}
