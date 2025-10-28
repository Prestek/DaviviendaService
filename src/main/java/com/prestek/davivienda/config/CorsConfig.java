package com.prestek.davivienda.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
 * Class that handles the CORS configuration
 */
@Configuration
@EnableWebMvc
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsConfig implements WebMvcConfigurer {

    @Value("${ALLOWED_ORIGINS_HTTP}")
    private String allowedOriginsHttp;

    @Value("${ALLOWED_ORIGINS_HTTPS}")
    private String allowedOriginsHttps;

    /*
     * Method that registers the CORS filter
     *
     * @return FilterRegistrationBean, the filter registration bean
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // Combine HTTP and HTTPS origins
        List<String> allOrigins = Arrays.asList(
                        allowedOriginsHttp.split(","),
                        allowedOriginsHttps.split(",")).stream()
                .flatMap(Arrays::stream)
                .toList();
        config.setAllowedOrigins(allOrigins);
        config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}