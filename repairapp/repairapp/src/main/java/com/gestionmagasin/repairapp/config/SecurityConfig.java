package com.gestionmagasin.repairapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Configuration : Indique à Spring que cette classe contient des configurations beans
 * @EnableWebSecurity : Active la sécurité web de Spring Security
 * @EnableMethodSecurity : Permet l'utilisation des annotations de sécurité sur les méthodes (@PreAuthorize, etc.)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * SecurityFilterChain : Définit la chaîne de filtres de sécurité
     * Cette configuration définit :
     * - Les règles CORS (Cross-Origin Resource Sharing)
     * - La désactivation CSRF pour les API
     * - Les autorisations d'accès aux URLs
     * - La configuration du formulaire de login
     * - La gestion de la déconnexion
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/signup",
                    "/login",
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/assets/**",
                    "/webjars/**",
                    "/repairs/track",
                    "/repairs/track/**",
                    "/access-denied",
                    "/admin/dashboard"
                ).permitAll()
                .requestMatchers("/admin/**").hasAuthority("ADMINISTRATOR")
                .requestMatchers("/admin/shops/**").hasAuthority("ADMINISTRATOR")
                .requestMatchers("/shop/**", "/repairers/**").hasAnyAuthority("ADMINISTRATOR", "PROPRIETAIRE")
                .requestMatchers("/repairs/**").hasAnyAuthority("REPAIRER", "ADMINISTRATOR")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard")
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
            );

        return http.build();
    }

    /**
     * CorsConfigurationSource : Configure les règles CORS
     * CORS permet de contrôler quels domaines externes peuvent accéder à l'API
     * Ici, on autorise tous les origines (*) - À configurer de manière plus restrictive en production
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * PasswordEncoder : Définit l'algorithme de hachage des mots de passe
     * BCrypt est un algorithme de hachage sécurisé avec "salt" automatique
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager : Composant central de l'authentification dans Spring Security
     * - Gère le processus d'authentification
     * - Vérifie les credentials utilisateur
     * - Crée le contexte de sécurité après authentification réussie
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}