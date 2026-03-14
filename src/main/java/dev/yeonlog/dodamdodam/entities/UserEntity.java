package dev.yeonlog.dodamdodam.entities;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class UserEntity {
    private String userId;
    private String password;
    private String name;
    private String email;
    private LocalDate birth;
    private String gender;
    private String role;
    private String status;
    private String addressPrimary;
    private String addressSecondary;
    private String mobileFirst;
    private String mobileSecond;
    private String mobileThird;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate overdue;
    private int overcnt;
}
