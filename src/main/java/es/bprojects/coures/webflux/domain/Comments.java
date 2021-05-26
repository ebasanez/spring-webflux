package es.bprojects.coures.webflux.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.ToString;

/**
 * @author ebasanez
 * @since 2021-05-26
 */
@Getter
@ToString
public class Comments {

	private final List<String> comments = new ArrayList<>();

	public void addComment(String comment) {
		comments.add(comment);
	}

}
