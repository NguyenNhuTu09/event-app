package com.example.backend.Models.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "category_translations",
       uniqueConstraints = @UniqueConstraint(columnNames = {"category_id", "language_code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 10)
    private String languageCode;

    @Column(nullable = false)
    private String name;

    private String seoTitle;
    private String seoDescription;
}