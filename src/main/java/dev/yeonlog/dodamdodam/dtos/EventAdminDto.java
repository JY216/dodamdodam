package dev.yeonlog.dodamdodam.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EventAdminDto {
    private Long id;
    private String title;
    private String description;
    private String target;
    private int capacity;
    private String eventStartAt;
    private String eventEndAt;
    private String applyStartAt;
    private String applyEndAt;
    private String status;
    private LocalDateTime createdAt;
    private String posterImage;
    private int applicantCount;
}
