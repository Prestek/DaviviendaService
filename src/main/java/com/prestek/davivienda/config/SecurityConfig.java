package com.prestek.davivienda.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!test")
public class SecurityConfig {

    private final CorsFilter corsFilter;

    @Value("${CLERK_ISSUER}")
    private String clerkIssuer;

    @Value("${CLERK_JWKS_URL}")
    private String clerkJwksUrl;

    public SecurityConfig(CorsFilter corsFilter) {
        this.corsFilter = corsFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.disable())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private class JwtAuthenticationFilter extends OncePerRequestFilter {

        private JWKSet jwkSet;
        private long jwkSetCacheTime = 0;
        private static final long CACHE_DURATION = 3600000;

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                FilterChain filterChain)
                throws ServletException, IOException {

            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            try {
                String token = authHeader.substring(7);
                SignedJWT signedJWT = SignedJWT.parse(token);

                Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
                String issuer = signedJWT.getJWTClaimsSet().getIssuer();

                if (expirationTime == null || expirationTime.before(Date.from(Instant.now()))) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
                    return;
                }

                if (!clerkIssuer.equals(issuer)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid issuer");
                    return;
                }

                if (jwkSet == null || System.currentTimeMillis() - jwkSetCacheTime > CACHE_DURATION) {
                    jwkSet = loadJWKSet(clerkJwksUrl);
                    jwkSetCacheTime = System.currentTimeMillis();
                }

                String keyId = signedJWT.getHeader().getKeyID();
                JWK jwk = jwkSet.getKeyByKeyId(keyId);

                if (jwk == null) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid key ID");
                    return;
                }

                RSAKey rsaKey = jwk.toRSAKey();
                JWSVerifier verifier = new RSASSAVerifier(rsaKey);
                signedJWT.verify(verifier);

                String subject = signedJWT.getJWTClaimsSet().getSubject();

                // Extraer rol del token
                List<SimpleGrantedAuthority> authorities = Collections.emptyList();
                Object roleClaim = signedJWT.getJWTClaimsSet().getClaim("role");

                if (roleClaim != null) {
                    if (roleClaim instanceof String string) {
                        String roleWithPrefix = "ROLE_" + string.toUpperCase();
                        authorities = List.of(new SimpleGrantedAuthority(roleWithPrefix));
                    } else if (roleClaim instanceof List) {
                        authorities = ((List<?>) roleClaim).stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toString().toUpperCase()))
                                .collect(Collectors.toList());
                    }
                } else {
                    System.out.println("WARNING: No role claim found in token!");
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(subject,
                        null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }

            filterChain.doFilter(request, response);
        }

        private JWKSet loadJWKSet(String jwksUrl) throws Exception {
            URL url = URI.create(jwksUrl).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            try (InputStream inputStream = connection.getInputStream()) {
                return JWKSet.load(inputStream);
            } finally {
                connection.disconnect();
            }
        }
    }
}