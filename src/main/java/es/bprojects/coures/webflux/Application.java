package es.bprojects.coures.webflux;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import es.bprojects.coures.webflux.domain.Comments;
import es.bprojects.coures.webflux.domain.User;
import es.bprojects.coures.webflux.domain.UserComments;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * @author ebasanez
 * @since 2021-05-26
 */
@Slf4j
@SpringBootApplication
public class Application implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		exampleCombineZipWith();
	}

	private void exampleCombineMono() {
		Mono<User> userMono = Mono.fromCallable(() -> new User("John", "Doe"));
		Mono<Comments> commentsMono = Mono.fromCallable(() -> {
			Comments comments = new Comments();
			comments.addComment("Comment1");
			comments.addComment("Comment2");
			return comments;
		});

		// Combine both fluxes:
		userMono.flatMap(u -> commentsMono.map(c -> new UserComments(u, c))).subscribe(
				uc -> log.info(uc.toString())
		);

	}

	private void exampleCombineZipWith() {
		Mono<User> userMono = Mono.fromCallable(() -> new User("John", "Doe"));
		Mono<Comments> commentsMono = Mono.fromCallable(() -> {
			Comments comments = new Comments();
			comments.addComment("Comment1");
			comments.addComment("Comment2");
			return comments;
		});

		// Combine both fluxes:
		Mono<UserComments> userWithCommentsMono = userMono.zipWith(commentsMono, (u, c) -> new UserComments(u, c));
		userWithCommentsMono.subscribe(uc -> log.info(uc.toString()));
	}
}
