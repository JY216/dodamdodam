package dev.yeonlog.dodamdodam.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoanDto {
    private Long loanId;
    private String bookTitle;
    private String loanDate;
    private String dueDate;
    private String returnDate;
    private String status;
}
