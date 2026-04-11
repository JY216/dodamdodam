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
public class LoanEntity {
    private long id;
    private String userId;
    private long copyId;
    private LocalDateTime loanDate;
    private LocalDate dueDate;
    private LocalDateTime returnDate;
    private String status;
    private LocalDateTime createdAt;

    private String userName;
    private String bookTitle;
    private String bookAuthor;
    private String bookCover;
    private long dday;

    private int extendCount;
}
