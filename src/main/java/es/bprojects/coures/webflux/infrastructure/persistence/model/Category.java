package es.bprojects.coures.webflux.infrastructure.persistence.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author ebasanez
 * @since 2021-06-07
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Document("category")
public class Category {

	@Id
	private String id;
	private String name;

	public Category(String name) {
		this.name = name;
	}

}
