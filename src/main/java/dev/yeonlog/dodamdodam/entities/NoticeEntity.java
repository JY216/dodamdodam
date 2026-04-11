package dev.yeonlog.dodamdodam.entities;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeEntity {
    private Long id;
    private String title;
    private String content;
    private boolean isPinned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
