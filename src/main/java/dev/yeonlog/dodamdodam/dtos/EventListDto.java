package dev.yeonlog.dodamdodam.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventListDto {
    private Long id;
    private String title;
    private String target;
    private int capacity;
    private String eventStartAt;
    private String eventEndAt;
    private String applyStartAt;
    private String applyEndAt;
    private String status;
    private String posterImage;
    private int applicantCount;
}
