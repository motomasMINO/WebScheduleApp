package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

// ユーザー情報を管理するリポジトリ
public interface UserRepository extends JpaRepository<User, String> {}