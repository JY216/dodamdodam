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
public class BookEntity {
    private long id;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private LocalDate publishDate;
    private int categoryId;
    private int totalQuantity;
    private String status;
    private boolean isRecommended;
    private String recommendReason;
    private Integer price;
    private LocalDateTime createdAt;

    private String categoryName;
    private int availableQuantity;

    private String coverImage;
}
