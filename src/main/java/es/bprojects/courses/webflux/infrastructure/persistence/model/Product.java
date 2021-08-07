package es.bprojects.courses.webflux.infrastructure.persistence.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author ebasanez
 * @since 2021-05-30
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Document(collection = "product")
public class Product {

	public Product(String name, Float price) {
		this.name = name;
		this.price = price;
	}

	public Product(String name, Float price, Category category) {
		this(name,price);
		this.category = category;
	}

	@Id
	private String id;

	private Category category;

	private String name;

	private Float price;

	private Date createdAt;

	private String photo;

}
