package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Scheduleエンティティのリポジトリインターフェース
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    // 自分の予定をすべて取得
    List<Schedule> findByUsername(String username);

    // 自分の予定を開始日時の範囲で取得
    List<Schedule> findByUsernameAndStartTimeBetween(String username, LocalDateTime start, LocalDateTime end);

    // 自分の予定をIDで取得
    Optional<Schedule> findByIdAndUsername(Long id, String username);

    // 自分の予定を削除
    void deleteByUsername(String username);
}