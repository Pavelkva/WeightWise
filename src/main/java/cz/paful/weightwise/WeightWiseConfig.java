package cz.paful.weightwise;

import com.github.benmanes.caffeine.cache.Caffeine;
import cz.paful.weightwise.filters.JwtRequestFilter;
import cz.paful.weightwise.service.UserService;
import cz.paful.weightwise.util.JwtTokenUtil;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@EnableScheduling
public class WeightWiseConfig implements WebMvcConfigurer {

    public static final String USER_WEIGHT_CACHE_KEY = "user_weight_cache";

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
                .headers(h -> h
                        .cacheControl(HeadersConfigurer.CacheControlConfig::disable)
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CaffeineCacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(USER_WEIGHT_CACHE_KEY);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(1000));
        return cacheManager;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Povoluje CORS pro všechny cesty
                .allowedOrigins("https://pavelweb.onrender.com", "http://localhost:63342")// Povoluje pouze konkrétní doménu
                .allowedMethods("*") // Povoluje specifické HTTP metody
                .allowedHeaders("*") // Povoluje specifické hlavičky
                .exposedHeaders("ETag")
                .allowCredentials(true) // Povoluje odesílání cookies
                .maxAge(3600); // Maximální doba platnosti preflight requestu
    }

}
