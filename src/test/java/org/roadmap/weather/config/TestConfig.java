package org.roadmap.weather.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.mock;

@Configuration
@ComponentScan("org.roadmap.weather.service")
@ComponentScan("org.roadmap.weather.repository")
@ComponentScan("org.roadmap.weather.mapper")
@ComponentScan("org.roadmap.weather.client")
@PropertySource("classpath:application-test.properties")
@Import(DatabaseConfig.class)
public class TestConfig {
    @Bean
    static RestTemplate restTemplate() {
        return mock(RestTemplate.class);
    }

    @Bean
    static MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }

    @Bean(destroyMethod = "shutdown")
    static ExecutorService weatherApiExecutor() {
        return Executors.newFixedThreadPool(4);
    }
}
