package com.example.demo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// 予定を表すエンティティクラス
@Entity
public class Schedule {
    // IDは自動生成されるように設定
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // 予定のタイトル
    private String description; // 予定の説明
    private LocalDateTime startTime; // 予定の開始日時
    private LocalDateTime endTime; // 予定の終了日時

    // ゲッターとセッター
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}