package es.bprojects.coures.webflux.infrastructure.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ViewResolverRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.thymeleaf.spring5.ISpringWebFluxTemplateEngine;
import org.thymeleaf.spring5.SpringWebFluxTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.reactive.ThymeleafReactiveViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;

import lombok.Setter;

/**
 * @author ebasanez
 * @since 2021-06-12
 */
	@Setter
	@Configuration
	public class ViewConfiguration implements ApplicationContextAware, WebFluxConfigurer {

		ApplicationContext applicationContext;

		@Bean
		public ITemplateResolver thymeleafTemplateResolver() {
			final SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
			resolver.setApplicationContext(this.applicationContext);
			resolver.setPrefix("classpath:templates/");
			resolver.setSuffix(".html");
			resolver.setTemplateMode(TemplateMode.HTML);
			resolver.setCacheable(false);
			resolver.setCheckExistence(false);
			return resolver;
		}
		@Bean
		public ISpringWebFluxTemplateEngine thymeleafTemplateEngine() {
			SpringWebFluxTemplateEngine templateEngine = new SpringWebFluxTemplateEngine();
			templateEngine.setTemplateResolver(thymeleafTemplateResolver());
			return templateEngine;
		}
		@Bean
		public ThymeleafReactiveViewResolver thymeleafReactiveViewResolver() {
			ThymeleafReactiveViewResolver viewResolver = new ThymeleafReactiveViewResolver();
			viewResolver.setTemplateEngine(thymeleafTemplateEngine());
			return viewResolver;
		}
		@Override
		public void configureViewResolvers(ViewResolverRegistry registry) {
			registry.viewResolver(thymeleafReactiveViewResolver());
		}

	}
