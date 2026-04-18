package dev.yeonlog.dodamdodam.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookDetailDto {
    private Long id;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private String publishDate;
    private String coverImage;
    private String categoryName;
    private int totalQuantity;
    private String status;
}