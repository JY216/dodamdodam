package dev.yeonlog.dodamdodam.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserOverdueDto {
    private LocalDate overdue;
}
