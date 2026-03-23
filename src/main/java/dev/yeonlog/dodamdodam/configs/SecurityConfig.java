package dev.yeonlog.dodamdodam.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").hasRole("ADMIN")  // 관리자만
                        .requestMatchers("/", "/login", "/register", "/search", "/bestseller", "/assets/**", "/user/**", "/ai/**", "/mypage/**", "/wish-book", "/wish-book/**").permitAll() // 누구나
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/login")          // 로그인 페이지 경로
                        .loginProcessingUrl("/login") // form action 경로
                        .defaultSuccessUrl("/")       // 로그인 성공 시
                        .failureUrl("/login?error")   // 로그인 실패 시
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)  // 세션 삭제
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
