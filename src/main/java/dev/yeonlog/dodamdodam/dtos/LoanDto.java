package dev.yeonlog.dodamdodam.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoanDto {
    private Long id;
    private String bookTitle;
    private String author;
    private String loanDate;
    private String dueDate;
    private String returnDate;
    private String status;
}
