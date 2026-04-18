package dev.yeonlog.dodamdodam.entities;

import lombok.*;

import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class JournalEntity {
    private Long id;
    private String userId;
    private String title;
    private String bookTitle;
    private String author;
    private String content;
    private LocalDate date;
    private int star;
    private String status;
    private int pages;
    private String createdAt;
}
