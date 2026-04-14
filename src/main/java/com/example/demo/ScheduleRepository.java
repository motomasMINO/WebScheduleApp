package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

// Scheduleエンティティのリポジトリインターフェース
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByStartTimeBetween(LocalDateTime start, LocalDateTime end); // 開始日時が指定された範囲内のスケジュールを取得するメソッド
}