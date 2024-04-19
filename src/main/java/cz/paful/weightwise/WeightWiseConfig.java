package cz.paful.weightwise;

import cz.paful.weightwise.filters.JwtRequestFilter;
import cz.paful.weightwise.service.UserService;
import cz.paful.weightwise.util.JwtTokenUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WeightWiseConfig implements WebMvcConfigurer {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtRequestFilter jwtRequestFilter(UserService userService, JwtTokenUtil jwtTokenUtil) {
        return new JwtRequestFilter(userService, jwtTokenUtil);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtRequestFilter jwtRequestFilter) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("user/**", "health-check")
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Povoluje CORS pro všechny cesty
                .allowedOrigins("http://localhost:63342")
                .allowedOrigins("https://pavelweb.onrender.com")// Povoluje pouze konkrétní doménu
                .allowedMethods("*") // Povoluje specifické HTTP metody
                .allowedHeaders("*") // Povoluje specifické hlavičky
                .allowCredentials(true) // Povoluje odesílání cookies
                .maxAge(3600); // Maximální doba platnosti preflight requestu
    }

}
