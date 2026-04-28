package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// ユーザー情報を表すエンティティ
@Entity
@Table(name = "users")
public class User {

    @Id
    private String username;

    private String password;

    // ゲッターとセッター
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}