package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Spring Securityの設定クラス
@Configuration
public class SecurityConfig {

    // パスワードエンコーダ
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // DBからユーザー取得
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
       // ユーザー名でユーザーを検索し、Spring SecurityのUserオブジェクトに変換して返す
       return username -> userRepository.findById(username)
            .map(user -> org.springframework.security.core.userdetails.User
            .withUsername(user.getUsername())
            .password(user.getPassword())
            .roles("USER")
            .build()
        )
        .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + username));
    }

    // セキュリティ設定
    @Bean
    @Order(1)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        // APIエンドポイントはCSRF無効化＆認証必須、未認証アクセスは401を返す
        http
            .securityMatcher("/api/**")
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/signup").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendError(HttpStatus.UNAUTHORIZED.value())
                )
            );
        return http.build();
    }

    // Webエンドポイントのセキュリティ設定は別のSecurityFilterChainで定義
    @Bean
    @Order(2)
    public SecurityFilterChain webChain(HttpSecurity http) throws Exception {
        // WebエンドポイントはCSRF無効化＆認証必須、フォームログインとログアウトを設定
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/Index.html", "/Login.html", "/Signup.html",
                    "/Script.js", "/Login.js", "/Signup.js", "/Style.css",
                    "/css/**", "/js/**", "/images/**",
                    "/**/*.css", "/**/*.js"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/Login.html")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/Index.html", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/Index.html")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );
        return http.build();
    }
}