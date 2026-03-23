package dev.yeonlog.dodamdodam.entities;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class CategoryEntity {
    private int id;
    private String name;
}
