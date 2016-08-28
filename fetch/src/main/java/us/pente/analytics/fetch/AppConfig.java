package us.pente.analytics.fetch;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;

@Configuration
public class AppConfig {
    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        return new PenteOrgClientHttpRequestFactory();
    }
}
