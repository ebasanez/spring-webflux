package es.bprojects.coures.webflux.controller;

import java.time.Duration;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

import es.bprojects.coures.webflux.dto.ProductDto;
import es.bprojects.coures.webflux.model.Product;
import es.bprojects.coures.webflux.persistence.ProductsRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

/**
 * @author ebasanez
 * @since 2021-05-30
 */
@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

	private final ProductsRepository productsRepository;

	@GetMapping
	public String list(Model model) {
		Flux<ProductDto> products = productsRepository.findAll().map(this::toDto);
		model.addAttribute("products", products);
		model.addAttribute("title", "Product list");
		return "list";
	}

	/**
	 * Similar to list, but handling back-pressure with data-driver.
	 */
	@GetMapping(params = "data-driver=true")
	public String listDataDriver(Model model) {
		// Add a delay to the flux to show how webflux will handle it
		Flux<ProductDto> products = productsRepository.findAll().map(this::toDto).delayElements(Duration.ofSeconds(1));
		// Instead of the flux, we put in the model a ReactiveDataDriverContextVariable that wraps and handle de Flux with a buffers size (2):
		// Thymeleaf will handle the back-pressure automatically, rendering those as are published.
		model.addAttribute("products", new ReactiveDataDriverContextVariable(products, 2));
		model.addAttribute("title", "Product list");
		return "list";
	}

	/**
	 * Similar to list, but handling back-pressure with chunks (configured via properties in application.properties).
	 */
	@GetMapping(params="chunked=full")
	public String listChunked(Model model) {
		Flux<ProductDto> products = productsRepository.findAll().map(this::toDto).repeat(5000);
		model.addAttribute("products", products);
		model.addAttribute("title", "Product list");
		return "list-chunked";
	}

	private final ProductDto toDto(Product entity) {
		return ProductDto.builder()
				.id(entity.getId())
				.name(entity.getName().toUpperCase(Locale.ROOT))
				.price(entity.getPrice())
				.createdAt(entity.getCreatedAt())
				.build();
	}
}
