package dev.yeonlog.dodamdodam.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class JournalDto {
    private Long id;
    private Long userId;
    private String title;
    private String bookTitle;
    private String author;
    private int star;
    private String status;
    private int pages;
    private String content;
    private String readDate;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
