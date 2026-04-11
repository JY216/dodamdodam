package dev.yeonlog.dodamdodam.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAdminDto {
    private String userId;
    private String name;
    private String email;
    private String birth;
    private String gender;
    private String mobileFirst;
    private String mobileSecond;
    private String mobileThird;
    private String role;
    private String status;
    private String createdAt;
    private int loanCount;
}


