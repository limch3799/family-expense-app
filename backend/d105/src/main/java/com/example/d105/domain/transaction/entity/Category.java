package com.example.d105.domain.transaction.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "categories", schema = "d105")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @Column(name = "category_id")
    private Short categoryId;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

}
