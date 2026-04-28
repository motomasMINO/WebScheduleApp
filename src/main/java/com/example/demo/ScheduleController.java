package com.example.demo;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public List<Schedule> getAll(Principal principal) {
        // ログインユーザーの情報がPrincipalに格納されているため、そこからユーザー名を取得してスケジュールを検索
        if(principal == null) {
            throw new RuntimeException("ログインしてください");
        }
        String username = principal.getName();
        return repository.findByUsername(username); // ログインユーザーのスケジュールをすべて取得
    }

    // 日付検索
    @GetMapping("/search")
    public List<Schedule> getByDate(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, Principal principal) {
        if(principal == null) {
            throw new RuntimeException("ログインしてください");
        }
        String username = principal.getName();

        LocalDateTime start = date.atStartOfDay(); // 指定された日付の開始日時を取得
        LocalDateTime end = date.atTime(LocalTime.MAX); // 指定された日付の終了日時を取得
        return repository.findByUsernameAndStartTimeBetween(username, start, end); // ログインユーザーのスケジュールを指定された日付の範囲で取得
    }

    // 1件取得
    @GetMapping("/{id}")
    public Schedule getById(@PathVariable Long id, Principal principal) {
        if(principal == null) {
            throw new RuntimeException("ログインしてください");
        }
        String username = principal.getName();

        return repository.findByIdAndUsername(id, username) // IDとログインユーザーの名前でスケジュールを検索
                .orElseThrow(() -> new RuntimeException("データが見つかりません: " + id)); // 存在しないIDの場合は例外をスロー
    }

    // 登録
    @PostMapping
    public Schedule create(@RequestBody Schedule schedule, Principal principal) {
        if(principal == null) {
            throw new RuntimeException("ログインしてください");
        }
        
        String username = principal.getName(); // ログインユーザーの名前を取得
        schedule.setUsername(username); // スケジュールにログインユーザーの名前を設定

        return repository.save(schedule); // IDは自動生成されるため、リクエストボディには含めない
    }

    // 更新
    @PutMapping("/{id}")
    public Schedule update(@PathVariable Long id, @RequestBody Schedule schedule, Principal principal) {
        if(principal == null) {
            throw new RuntimeException("ログインしてください");
        }
        String username = principal.getName();
        Optional<Schedule> existing = repository.findByIdAndUsername(id, username); // 更新対象のスケジュールが存在するか確認

        // 存在する場合は更新、存在しない場合は例外をスロー
        if (existing.isPresent()) {
            Schedule s = existing.get(); // 更新対象のスケジュールを取得
            s.setTitle(schedule.getTitle()); // タイトルを更新
            s.setDescription(schedule.getDescription()); // 説明を更新
            s.setStartTime(schedule.getStartTime()); // 開始日時を更新
            s.setEndTime(schedule.getEndTime()); // 終了日時を更新
            return repository.save(s); // 更新後のスケジュールを保存して返す
        } else {
            throw new RuntimeException("データが見つかりません: " + id); // 存在しないIDの場合は例外をスロー
        }
    }

    // 削除(自分の予定のみ削除可)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Principal principal) {
        if(principal == null) {
            throw new RuntimeException("ログインしてください");
        }
        String username = principal.getName();
        Optional<Schedule> existing = repository.findByIdAndUsername(id, username);
        
        // 存在する場合は削除、存在しない場合は例外をスロー
        if(existing.isPresent()) {
            repository.delete(existing.get());
        } else {
            throw new RuntimeException("データが見つかりません: " + id); // 存在しないIDの場合は例外をスロー
        }
    }
}