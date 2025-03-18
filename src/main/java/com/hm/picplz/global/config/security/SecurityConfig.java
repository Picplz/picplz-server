package com.hm.picplz.global.config.security;

import com.hm.picplz.domain.auth.OAuth2.MyAuthenticationSuccessHandler;
import com.hm.picplz.domain.auth.jwt.JwtAccessDeniedHandler;
import com.hm.picplz.domain.auth.jwt.JwtAuthenticationEntryPoint;
import com.hm.picplz.domain.auth.jwt.JwtFilter;
import com.hm.picplz.domain.auth.jwt.JwtTokenProvider;
import com.hm.picplz.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    /* 권한 제외 대상 */
    private static final String[] permitAllUrl = new String[]{
            "/swagger-ui/**",
            "/api-docs/",
            "/swagger-ui/",
            "/swagger-resources/",
            "/v3/api-docs/",
            "/webjars/",
            "/members/test",
            "/**"
     };

     /* Admin 접근 권한 */
    private static final String[] permitAdminUrl = new String[]{
    };


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
            throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(CorsConfig.corsConfigurationSource())
                )
                .sessionManagement((sessionManagement) ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(authenticationManager -> authenticationManager
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .addFilterBefore(new JwtFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(request -> {
                    request.requestMatchers(permitAllUrl)
                            .permitAll();
                    request.requestMatchers(permitAdminUrl)
                            .hasRole("ADMIN");
                    request.anyRequest()
                            .authenticated();
                })
                .oauth2Login(oauth2Login -> oauth2Login
                        .successHandler(new MyAuthenticationSuccessHandler(jwtTokenProvider, memberRepository)))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
