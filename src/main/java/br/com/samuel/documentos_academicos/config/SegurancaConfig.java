package br.com.samuel.documentos_academicos.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.samuel.documentos_academicos.security.JwtAuthenticationFilter;
import br.com.samuel.documentos_academicos.security.JwtService;
import br.com.samuel.documentos_academicos.security.SegurancaExceptionHandler;

@Configuration
@EnableWebSecurity
public class SegurancaConfig {

    // Documentação interativa (springdoc). Desabilitada no perfil prod via properties.
    private static final String[] ROTAS_DOCUMENTACAO = {
            "/v3/api-docs",
            "/v3/api-docs/**",      // inclui /v3/api-docs/swagger-config, buscado pela UI
            "/v3/api-docs.yaml",
            "/swagger-ui.html",     // redireciona para /swagger-ui/index.html
            "/swagger-ui/**"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origens}") String origens) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(origens.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtService jwtService,
                                                   ObjectMapper objectMapper,
                                                   CorsConfigurationSource corsConfigurationSource) throws Exception {
        SegurancaExceptionHandler segurancaHandler = new SegurancaExceptionHandler(objectMapper);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // --- públicos ---
                        .requestMatchers(HttpMethod.POST, "/api/auth/login",
                                "/api/auth/esqueci-senha", "/api/auth/redefinir-senha").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                        .requestMatchers(ROTAS_DOCUMENTACAO).permitAll()

                        // --- gerenciamento de status: administrativo ---
                        .requestMatchers(HttpMethod.POST, "/api/status/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/status/**").hasRole("ADMIN")

                        // --- cadastros: escrita é administrativa ---
                        .requestMatchers(HttpMethod.POST, "/api/alunos/**", "/api/cursos/**",
                                "/api/tipos-documento/**", "/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/alunos/**", "/api/cursos/**",
                                "/api/tipos-documento/**", "/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/alunos/**", "/api/cursos/**",
                                "/api/tipos-documento/**", "/api/usuarios/**").hasRole("ADMIN")



                        // --- toda exclusão é administrativa ---
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")

                        // --- solicitações: criar e movimentar ---
                        .requestMatchers(HttpMethod.POST, "/api/solicitacoes/**").hasAnyRole("ADMIN", "OPERADOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/solicitacoes/**").hasAnyRole("ADMIN", "OPERADOR")

                        // --- leitura: qualquer autenticado ---
                        .requestMatchers(HttpMethod.GET, "/api/**").authenticated()

                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(segurancaHandler)   // 401
                        .accessDeniedHandler(segurancaHandler))       // 403
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, objectMapper),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}