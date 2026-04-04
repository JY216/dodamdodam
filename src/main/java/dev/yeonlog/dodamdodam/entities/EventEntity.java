package dev.yeonlog.dodamdodam.entities;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventEntity {
    private Long id;
    private String title;
    private String description;
    private String target;
    private int capacity;
    private LocalDateTime eventStartAt;
    private LocalDateTime eventEndAt;
    private LocalDateTime applyStartAt;
    private LocalDateTime applyEndAt;
    private String status;
    private LocalDateTime createdAt;
    private String posterImage;

    private int applicationCount;
}
