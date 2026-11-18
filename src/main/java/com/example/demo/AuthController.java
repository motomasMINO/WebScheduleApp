package com.example.demo;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

// 認証関連のAPIを提供するコントローラー
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, ScheduleRepository scheduleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.scheduleRepository = scheduleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ユーザー登録
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        // ユーザー名が既に存在する場合は400を返す
        if (userRepository.existsById(user.getUsername())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("ユーザー名は既に存在します");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("登録成功");
    }

    // ログイン中ユーザー確認
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {

        // ログインしていない場合は401を返す
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(
                Map.of("username", authentication.getName())
        );
    }

    // 既存のDELETEハンドラーでアカウント削除＆サーバー側でログアウト
    @DeleteMapping("/me")
    public ResponseEntity<?> deleteMe(Authentication authentication) {
        // ログインしていない場合は401を返す
       if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
       }

       String username = authentication.getName();
       scheduleRepository.deleteByUsername(username);
       userRepository.deleteById(username);

       return ResponseEntity.noContent().build();
    }

    // POSTでアカウント削除＆サーバー側でログアウト(互換性向上)
    @PostMapping("/me/delete")
    public ResponseEntity<?> deleteMePost(Authentication authentication, HttpServletRequest request) {
        // ログインしていない場合は401を返す
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = authentication.getName();
        scheduleRepository.deleteByUsername(username);
        userRepository.deleteById(username);

        // サーブレット ログアウト / セッション無効化 / SecurityContextクリア
        try {
            request.logout();
        } catch (ServletException e) {
            // ログアウトに失敗しても次の処理を続ける
        }
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        SecurityContextHolder.clearContext();

        return ResponseEntity.noContent().build();
    }
}