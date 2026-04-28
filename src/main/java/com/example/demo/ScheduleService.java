package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

// スケジュール管理のビジネスロジックを担当するサービスクラス
@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository; // スケジュールのリポジトリを自動注入

    public List<Schedule> getSchedulesByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay(); // 指定された日付の開始日時を取得
        LocalDateTime end = date.atTime(LocalTime.MAX); // 指定された日付の終了日時を取得
        return scheduleRepository.findByStartTimeBetween(start, end); // 開始日時が指定された日付の範囲内のスケジュールを取得
    }

    // その他のビジネスロジックをここに追加できます（例：スケジュールの登録、更新、削除など）
    public Schedule save(Schedule schedule) {
        return scheduleRepository.save(schedule); // スケジュールを保存して返す
    }
}