package es.bprojects.coures.webflux.infrastructure.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * @author ebasanez
 * @since 2021-06-13
 */
@Getter
@Setter
@Component
@ConfigurationProperties
public class FileProperties {

	private String uploadFileDestination;
}
