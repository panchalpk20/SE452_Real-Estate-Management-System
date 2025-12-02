package edu.final_project.hot_properties.config;


import edu.final_project.hot_properties.auth.CustomAccessDeniedHandler;
import edu.final_project.hot_properties.auth.CustomAuthenticationEntryPoint;
import edu.final_project.hot_properties.auth.filters.GlobalRateLimiterFilter;
import edu.final_project.hot_properties.auth.filters.JwtAuthenticationFilter;
import edu.final_project.hot_properties.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final CustomUserDetailsService userDetailsService;

    private final GlobalRateLimiterFilter globalRateLimiterFilter;

    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomUserDetailsService userDetailsService,
                          GlobalRateLimiterFilter globalRateLimiterFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
        this.globalRateLimiterFilter = globalRateLimiterFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // First, rate limit EVERY request as early as possible:
                .addFilterBefore(globalRateLimiterFilter, UsernamePasswordAuthenticationFilter.class)

                // Then, process JWT authentication:
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/register").permitAll()
                        //need basic auth
                        .requestMatchers("/profile").authenticated()
                        .requestMatchers("/logout1").authenticated()
                        .requestMatchers("/dashboard").authenticated()
                        .requestMatchers("/profile").authenticated()
                        .requestMatchers("/editprofile").authenticated()

                        // agent specific
                        .requestMatchers("/properties/add").hasAuthority("AGENT")
                        .requestMatchers("/properties/edit/**").hasAuthority("AGENT")
                        .requestMatchers("/properties/delete/**").hasAuthority("AGENT")
                        .requestMatchers("/properties/manage/**").hasAuthority("AGENT")
                        .requestMatchers("/properties/{propertyId}/images/{imageId}/delete").hasAuthority("AGENT")
                        .requestMatchers("/messages/agent").hasAuthority("AGENT")
                        .requestMatchers("/messages/view/**").hasAuthority("AGENT")
                        .requestMatchers("/messages/reply/**").hasAuthority("AGENT")

                        //BUYER specific
                        .requestMatchers("/properties/view/**").hasAuthority("BUYER")
                        .requestMatchers("/properties/list").hasAnyAuthority("BUYER")
                        .requestMatchers("/favorites/**").hasAuthority("BUYER")
                        .requestMatchers("/messages/buyer").hasAuthority("BUYER")
                        .requestMatchers("/messages/send").hasAuthority("BUYER")
                        
                        //admin specific
                        .requestMatchers("/agents/**").hasAuthority("ADMIN")


                        .requestMatchers("/", "/index", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .anyRequest().permitAll()
                )
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint())  // Handles 401 errors
                        .accessDeniedHandler(new CustomAccessDeniedHandler())
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder encoder) throws Exception {

        //noinspection deprecation
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        //noinspection deprecation
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(encoder);

        return new ProviderManager(provider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
