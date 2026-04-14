package com.example.demo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;
import java.util.Optional;

// スケジュール管理のRESTコントローラー
@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleRepository repository; // スケジュールのリポジトリ

    // コンストラクタでリポジトリを注入
    public ScheduleController(ScheduleRepository repository) {
        this.repository = repository; // スケジュールのリポジトリを初期化
    }

    // 全件取得
    @GetMapping
    public List<Schedule> getAll() {
        return repository.findAll(); // 全てのスケジュールを取得して返す
    }

    // 日付検索
    @GetMapping("/search")
    public List<Schedule> getByDate(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay(); // 指定された日付の開始日時を取得
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX); // 指定された日付の終了日時を取得
        return repository.findByStartTimeBetween(startOfDay, endOfDay); // 開始日時が指定された日付の範囲内のスケジュールを取得
    }

    // 1件取得
    @GetMapping("/{id}")
    public Schedule getById(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found with id " + id)); // 存在しないIDの場合は例外をスロー
    }

    // 登録
    @PostMapping
    public Schedule create(@RequestBody Schedule schedule) {
        return repository.save(schedule); // IDは自動生成されるため、リクエストボディには含めない
    }

    // 更新
    @PutMapping("/{id}")
    public Schedule update(@PathVariable Long id, @RequestBody Schedule schedule) {
        Optional<Schedule> existing = repository.findById(id); // 更新対象のスケジュールが存在するか確認
        // 存在する場合は更新、存在しない場合は例外をスロー
        if (existing.isPresent()) {
            Schedule s = existing.get();
            s.setTitle(schedule.getTitle()); // タイトルを更新
            s.setDescription(schedule.getDescription()); // 説明を更新
            s.setStartTime(schedule.getStartTime()); // 開始日時を更新
            s.setEndTime(schedule.getEndTime()); // 終了日時を更新
            return repository.save(s); // 更新後のスケジュールを保存して返す
        } else {
            throw new RuntimeException("Schedule not found with id " + id); // 存在しないIDの場合は例外をスロー
        }
    }

    // 削除
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id); // IDに対応するスケジュールを削除
    }
}