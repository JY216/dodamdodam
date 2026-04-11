package dev.yeonlog.dodamdodam.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserEventDto {
    private Long applicationId;
    private String eventTitle;
    private String eventStartAt;
    private String eventEndAt;
    private String status;
}
