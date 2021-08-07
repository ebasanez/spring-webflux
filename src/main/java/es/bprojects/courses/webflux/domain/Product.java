package es.bprojects.courses.webflux.domain;

import java.util.Date;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author ebasanez
 * @since 2021-05-30
 */
@Getter
@Builder
@ToString
@EqualsAndHashCode
public class Product {

	private final String id;
	private final String name;
	private final float price;
	private final Date createdAt;
	private final String category;
	private final String photo;

}
