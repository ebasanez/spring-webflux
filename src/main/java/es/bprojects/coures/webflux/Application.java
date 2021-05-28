package es.bprojects.coures.webflux;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import es.bprojects.coures.webflux.domain.Comments;
import es.bprojects.coures.webflux.domain.User;
import es.bprojects.coures.webflux.domain.UserComments;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
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
		log.info("Start main");
		exampleInfiniteIntervalAndErrorWithRetry();
		log.info("End main");
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

	private static void exampleCombineZipWith() {
		Mono<User> userMono = Mono.fromCallable(() -> new User("John", "Doe"));
		Mono<Comments> commentsMono = Mono.fromCallable(() -> {
			Comments comments = new Comments();
			comments.addComment("Comment1");
			comments.addComment("Comment2");
			return comments;
		});

		// Combine both fluxes:
		Mono<UserComments> userWithCommentsMono = userMono.zipWith(commentsMono, (u, c) -> new UserComments(u, c));
		//Other way to zip and map:
		/* 		Mono<UserComments> userWithCommentsMono = userMono.zipWith(commentsMono)
				.map(tuple -> new UserComments(tuple.getT1(), tuple.getT2()));
		 */
		userWithCommentsMono.subscribe(uc -> log.info(uc.toString()));
	}

	private static void exampleCombineZipWithRanges() {
		Flux.just(1, 2, 3, 4)
				.map(i -> i * 2)
				.zipWith(Flux.range(0, 4), (first, second) -> String.format("First flux: %d, Second flux: %d", first, second))
				.subscribe(log::info);
	}

	/**
	 * Non-blocking
	 */
	private static void exampleCombineZipWithInterval() {
		Flux<Integer> range = Flux.range(1, 12);
		Flux<Long> delay = Flux.interval(Duration.ofSeconds(1));

		range.zipWith(delay, (r, d) -> r)
				.subscribe(i -> log.info(i.toString()));
	}

	/**
	 * Blocking
	 */
	private static void exampleCombineZipWithIntervalBlocking() {
		Flux<Integer> range = Flux.range(1, 12);
		Flux<Long> delay = Flux.interval(Duration.ofSeconds(1));

		range.zipWith(delay, (r, d) -> r)
				.doOnNext(i -> log.info(i.toString()))
				.blockLast();
	}

	/**
	 * Infinite flux blocked until termination with a latch
	 */
	private static void exampleInfiniteInterval() throws InterruptedException {

		CountDownLatch latch = new CountDownLatch(1);

		Flux.interval(Duration.ofSeconds(1))
				.doOnTerminate(latch::countDown)
				.map(i -> "Hola " + i)
				.doOnNext(log::info)
				.subscribe();

		latch.await();
	}

	/**
	 * Infinite flux blocked until termination with a latch, that end via error
	 */
	private static void exampleInfiniteIntervalAndError() throws InterruptedException {

		CountDownLatch latch = new CountDownLatch(1);

		Flux.interval(Duration.ofSeconds(1))
				.doOnTerminate(latch::countDown)
				.flatMap(i -> {
					if (i >= 5) {
						return Flux.error(new InterruptedException("Only until 5"));
					}
					return Flux.just(i);
				})
				.map(i -> "Hello " + i)
				// Subscribe to both processing and error
				.subscribe(log::info, e -> log.error(e.getMessage()));

		latch.await();
	}

	private static void exampleInfiniteIntervalAndErrorWithRetry() throws InterruptedException {

		CountDownLatch latch = new CountDownLatch(1);

		Flux.interval(Duration.ofSeconds(1))
				.doOnTerminate(latch::countDown)
				.flatMap(i -> {
					if (i >= 5) {
						return Flux.error(new InterruptedException("Only until 5"));
					}
					return Flux.just(i);
				})
				.map(i -> "Hello " + i)
				// Retries COMPLETE flux n times if it ends with an error
				.retry(2)
				.subscribe(log::info, e -> log.error(e.getMessage()));

		latch.await();
	}

}
