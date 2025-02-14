package yelly.suda.global.config;

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import yelly.suda.domain.oauth2.SudaOAuth2SuccessHandler;
import yelly.suda.domain.oauth2.SudaOAuth2UserService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final SudaOAuth2UserService userService;
    private final SudaOAuth2SuccessHandler successHandler;

    @Value("#{environment['allow-origin']}")
    private String allowOrigin;

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        /* session 정책 */
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        /* cors 설정 */
        http.cors(corsCustomizer -> corsCustomizer.configurationSource(request -> {
            CorsConfiguration corsConfig = new CorsConfiguration();
            corsConfig.setAllowedOrigins(Collections.singletonList(allowOrigin));
            corsConfig.setAllowedMethods(Collections.singletonList("*"));
            corsConfig.setAllowedHeaders(Collections.singletonList("*"));
            corsConfig.setAllowCredentials(true);
            corsConfig.setExposedHeaders(Collections.singletonList("Authorization"));
            corsConfig.setMaxAge(3600L);
            return corsConfig;
        }));

        /* csrf 설정 */
        http.csrf(AbstractHttpConfigurer::disable);

        /* request filter */
        http.authorizeHttpRequests(request ->
                request.requestMatchers("/api/v1/**").permitAll()
                        .requestMatchers("/suda/**").permitAll() // STOMP 허용
                        .anyRequest().authenticated());

        /* OAuth2 */
        http.oauth2Login(oauth2Config ->
                oauth2Config.loginPage(allowOrigin + "/login")
                        .userInfoEndpoint(endpointConfig -> endpointConfig.userService(userService))
                        .successHandler(successHandler));

        return http.build();
    }
}
