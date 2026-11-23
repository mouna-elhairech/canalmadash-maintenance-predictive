package com.canal.canalmadash.config;

import com.canal.canalmadash.security.JwtAuthFilter;
import com.canal.canalmadash.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Evite le mot de passe genere automatiquement par Spring Boot (le login passe par AuthService).
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException(username);
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // L'ordre compte: la premiere regle qui correspond s'applique.
                // Les droits viennent des fonctionnalites du role (autorites FONC_<CODE>).
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 1. Public
                        .requestMatchers("/api/auth/login", "/api/ml/health").permitAll()
                        // 2. Tout compte connecte
                        .requestMatchers("/api/auth/logout", "/api/ai/**", "/api/ml/**").authenticated()
                        // 3. Exports et rapports
                        .requestMatchers(HttpMethod.GET,
                                "/api/dashboard/maintenance/export",
                                "/api/dashboard/maintenance/retards/export",
                                "/api/dashboard/maintenance/machines/*/historique/export",
                                "/api/dashboard/maintenance/report",
                                "/api/dashboard/maintenance/retards/report",
                                "/api/dashboard/maintenance/machines/*/historique/report",
                                "/api/dashboard/maintenance/monthly-report")
                        .hasAuthority("FONC_RAPPORT_CONSULTER")
                        // 4. Lecture: tout compte connecte
                        .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                        // 5. Machines
                        .requestMatchers(HttpMethod.POST, "/api/machines/*/etat")
                        .hasAuthority("FONC_MACHINE_CHANGER_ETAT")
                        .requestMatchers("/api/machines/**")
                        .hasAuthority("FONC_MACHINE_CREER_MODIFIER_SUPPRIMER")
                        // 6. Demandes de maintenance
                        .requestMatchers(HttpMethod.POST, "/api/demandes-maintenance")
                        .hasAuthority("FONC_DEMANDE_CREER")
                        .requestMatchers("/api/demandes-maintenance/**")
                        .hasAuthority("FONC_DEMANDE_PILOTER")
                        // 7. Interventions
                        .requestMatchers(HttpMethod.POST, "/api/maintenances")
                        .hasAuthority("FONC_INTERVENTION_CREER")
                        .requestMatchers("/api/maintenances/pieces/*/retour")
                        .hasAuthority("FONC_PIECE_GERER_STOCK")
                        .requestMatchers("/api/interventions-externes/**")
                        .hasAuthority("FONC_INTERVENTION_CREER")
                        // 8. Pieces de rechange
                        .requestMatchers("/api/pieces-rechange/**")
                        .hasAuthority("FONC_PIECE_GERER_STOCK")
                        // 9. Controles periodiques
                        .requestMatchers("/api/regles-preventives/**")
                        .hasAuthority("FONC_CONTROLE_PERIODIQUE_GERER")
                        // 10. Parametrage
                        .requestMatchers("/api/score-criticite-parametres/**")
                        .hasAuthority("FONC_SCORE_PARAMETRER")
                        .requestMatchers("/api/roles-application/**")
                        .hasAuthority("FONC_ROLE_PARAMETRER")
                        .requestMatchers("/api/typeAction/**", "/api/typeMaintenance/**")
                        .hasAuthority("FONC_PAGE_PARAM_TYPES")
                        // 11. Tout autre endpoint /api non prevu est refuse
                        .requestMatchers("/api/**").denyAll()
                        .anyRequest().permitAll())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, ex) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"message\":\"Non authentifie\"}");
                        })
                        .accessDeniedHandler((request, response, ex) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"message\":\"Acces refuse\"}");
                        }))
                .addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}