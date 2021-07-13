package es.bprojects.coures.webflux.infrastructure.controller.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author ebasanez
 * @since 2021-06-22
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ProductDto {
	private String id;
	@NotNull(message = "name")
	private String name;
	@Min(value = 100, message = "Product minimum price is 100")
	@NotNull(message = "price")
	private Float price;
	@NotNull(message = "category")
	private String category;
	private String photo;
}
