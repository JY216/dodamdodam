package dev.yeonlog.dodamdodam.entities;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookLikeEntity {
    private Long id;
    private String userId;
    private Long bookId;
    private LocalDateTime createdAt;
}
