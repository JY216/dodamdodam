package dev.yeonlog.dodamdodam.entities;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class BookCopyEntity {
    private long id;
    private long bookId;
    private String status;
}
