package es.bprojects.coures.webflux.domain;

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
public class Category {

	private final String id;
	private final String name;

}
