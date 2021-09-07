package es.bprojects.courses.webflux.infrastructure.client.dto;

import java.util.Date;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author ebasanez
 * @since 2021-08-07
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ProductClientDto {

	private String id;
	private String name;
	private float price;
	private Date createdAt;
	private String category;
	private String photo;

}
