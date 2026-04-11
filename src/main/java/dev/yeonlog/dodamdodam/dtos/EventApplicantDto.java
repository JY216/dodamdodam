package dev.yeonlog.dodamdodam.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventApplicantDto {
    private Long applicationId;
    private String userId;
    private String name;
    private String mobileFirst;
    private String mobileSecond;
    private String mobileThird;
    private String status;
    private String createdAt;
}
