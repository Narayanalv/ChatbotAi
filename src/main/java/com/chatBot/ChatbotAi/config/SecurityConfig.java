package com.chatBot.ChatbotAi.config;

import com.chatBot.ChatbotAi.JWT.AuthTokenFilter;
import com.chatBot.ChatbotAi.JWT.ApiKeyFilter;
import com.chatBot.ChatbotAi.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.sql.DataSource;
import java.util.List;

@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity
public class SecurityConfig {
    @Autowired
    private DataSource dataSource;
    @Autowired
    private AuthTokenFilter authTokenFilter;
    @Autowired
    private ApiKeyFilter apiKeyFilter;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
//                .cors(Customizer.withDefaults())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((requests)
                        -> requests.requestMatchers(
                                "/api/register",
                        "/api/login",
                        "/api/verifyOTP",
                        "/api/resendOTP",
                        "/api/forgotPassword",
                        "/api/resetPassword",
                        "/api/verifyForgot",
                        "/test").permitAll()
                        .anyRequest()
                        .authenticated())
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class);
//        http.oauth2Login(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public UserDetailsService userDetailsService() {
//        return new JdbcUserDetailsManager(dataSource);
//    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With")); // ← explicit, not "*"
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
