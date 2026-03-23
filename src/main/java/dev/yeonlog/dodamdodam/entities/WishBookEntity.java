package dev.yeonlog.dodamdodam.entities;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class WishBookEntity {
    private long id;
    private String userId;
    private String title;
    private String author;
    private String publisher;
    private String publishYear;
    private String isbn;
    private Integer price;
    private String reason;
    private String status;
    private LocalDateTime createdAt;

    private String userName;
}
