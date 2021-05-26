package es.bprojects.coures.webflux.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author ebasanez
 * @since 2021-05-26
 */
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class User {

	private final String name;
	private final String surname;

}
