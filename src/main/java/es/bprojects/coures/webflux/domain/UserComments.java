package es.bprojects.coures.webflux.domain;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * @author ebasanez
 * @since 2021-05-26
 */
@ToString
@RequiredArgsConstructor
public class UserComments {

	private final User user;
	private final Comments comments;

}
