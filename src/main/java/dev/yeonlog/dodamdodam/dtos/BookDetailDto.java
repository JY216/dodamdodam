package dev.yeonlog.dodamdodam.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class BookDetailDto {
    private long id;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private LocalDate publishDate;
    private String coverImage;
    private String description;
    private String series;
    private String pages;
    private List<String> keywords;
    private int categoryId;
    private String categoryName;
    private int totalQuantity;
    private int availableQuantity;
    private String status;
}
