package com.example.demo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

// スケジュール管理のビジネスロジックを担当するサービスクラス
@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository; // スケジュールのリポジトリ

    public ScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    // 自分の予定をすべて取得
    public List<Schedule> getAllSchedules(String username) {
        return scheduleRepository.findByUsername(username); // ログインユーザーのスケジュールをすべて取得
    }

    // 自分の予定を日付で検索
    public List<Schedule> getSchedulesByDate(String username, LocalDate date) {
        LocalDateTime start = date.atStartOfDay(); // 指定された日付の開始日時を取得
        LocalDateTime end = date.atTime(LocalTime.MAX); // 指定された日付の終了日時を取得
        return scheduleRepository.findByUsernameAndStartTimeBetween(username, start, end); // 開始日時が指定された日付の範囲内のスケジュールを取得
    }

    // 自分の予定をIDで取得
    public Schedule getScheduleById(String username, Long id) {
        return scheduleRepository.findByIdAndUsername(id, username)
                .orElseThrow(() -> new RuntimeException("データが見つかりません"));
    }

    // 予定を登録
    public Schedule save(String username, Schedule schedule) {
        schedule.setUsername(username);
        return scheduleRepository.save(schedule); // スケジュールを保存して返す
    }

    // 予定を更新
    public Schedule update(String username, Long id, Schedule input) {
        // ログインユーザーのスケジュールで指定されたIDのスケジュールを検索
        Optional<Schedule> existing = scheduleRepository.findByIdAndUsername(id, username);

        // 既存のスケジュールが存在する場合は更新して保存、存在しない場合は例外をスロー
        if (existing.isPresent()) {
            Schedule s = existing.get();
            s.setTitle(input.getTitle());
            s.setDescription(input.getDescription());
            s.setStartTime(input.getStartTime());
            s.setEndTime(input.getEndTime());
            return scheduleRepository.save(s);
        } else {
            throw new RuntimeException("データが見つかりません");
        }
    }

    // 予定を削除
    public void delete(String username, Long id) {
        // ログインユーザーのスケジュールで指定されたIDのスケジュールを検索し、存在する場合は削除、存在しない場合は例外をスロー
        Schedule existing = scheduleRepository.findByIdAndUsername(id, username)
                .orElseThrow(() -> new RuntimeException("データが見つかりません"));
        scheduleRepository.delete(existing);
    }
}