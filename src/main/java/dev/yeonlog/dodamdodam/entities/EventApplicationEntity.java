package dev.yeonlog.dodamdodam.entities;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventApplicationEntity {
    private Long id;
    private Long eventId;
    private String userId;
    private String status;
    private LocalDateTime createdAt;

    private String userName;
    private String eventTitle;
    private LocalDateTime eventStartAt;
    private LocalDateTime eventEndAt;
    private String eventStatus;
}
