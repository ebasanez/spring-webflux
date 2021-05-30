package es.bprojects.coures.webflux;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import es.bprojects.coures.webflux.domain.Comments;
import es.bprojects.coures.webflux.domain.User;
import es.bprojects.coures.webflux.domain.UserComments;
import io.netty.channel.unix.Limits;
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
		exampleBackPressure();
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

	private static void exampleIntervalFromCreate() {
		Flux.create(emitter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				private Integer counter = 0;

				@Override
				// Rises counter and emit incremented value.
				public void run() {
					emitter.next(++counter);
					// At 10th iteration, finish timer and emmiter.
					if (counter >= 10) {
						timer.cancel();
						emitter.complete();
					}
				}
			}, 1000, 1000);
		})
				.doOnComplete(() -> log.info("Finished!"))
				.subscribe(e -> log.info(e.toString()));

	}

	private static void exampleIntervalFromCreateWithError() {
		Flux.create(emitter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				private Integer counter = 0;

				@Override
				// Rises counter and emit incremented value.
				public void run() {
					emitter.next(++counter);
					// At 5th iteration, finish timer and flux emits an error.
					if (counter >= 5) {
						timer.cancel();
						emitter.error(new InterruptedException("Error, flux stopped at 5"));
					}
				}
			}, 1000, 1000);
		})
				.doOnComplete(() -> log.info("Finished!"))
				.subscribe(e -> log.info(e.toString()));

	}

	public static void exampleBackPressure() {

		Flux.range(1, 10)
				.log()
				.subscribe(new Subscriber<Integer>() {

					private Subscription subscription;
					private final int limit = 3;
					private int consumed = 0;

					@Override
					public void onSubscribe(Subscription subscription) {
						this.subscription = subscription;
						subscription.request(limit); // We ask publisher (flux) to send a number of elements.
					}

					@Override
					public void onNext(Integer integer) {
						log.info(integer.toString());
						consumed++;
						// If we have already processed [limit] elements, we ask for [limit] more elements
						if (consumed == limit ){
							subscription.request(limit);
							consumed = 0;
						}
					}

					@Override
					public void onError(Throwable throwable) {

					}

					@Override
					public void onComplete() {

					}
				});
	}
}
