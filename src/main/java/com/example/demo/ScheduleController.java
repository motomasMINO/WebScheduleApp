package com.example.demo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleRepository repository;

    public ScheduleController(ScheduleRepository repository) {
        this.repository = repository;
    }

    // 全件取得
    @GetMapping
    public List<Schedule> getAll() {
        return repository.findAll();
    }

    // 日付検索
    @GetMapping("/search")
    public List<Schedule> getByDate(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return repository.findByStartTimeBetween(startOfDay, endOfDay);
    }

    // 1件取得
    @GetMapping("/{id}")
    public Schedule getById(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found with id " + id));
    }

    // 登録
    @PostMapping
    public Schedule create(@RequestBody Schedule schedule) {
        return repository.save(schedule);
    }

    // 更新
    @PutMapping("/{id}")
    public Schedule update(@PathVariable Long id, @RequestBody Schedule schedule) {
        Optional<Schedule> existing = repository.findById(id);
        if (existing.isPresent()) {
            Schedule s = existing.get();
            s.setTitle(schedule.getTitle());
            s.setDescription(schedule.getDescription());
            s.setStartTime(schedule.getStartTime());
            s.setEndTime(schedule.getEndTime());
            return repository.save(s);
        } else {
            throw new RuntimeException("Schedule not found with id " + id);
        }
    }

    // 削除
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}