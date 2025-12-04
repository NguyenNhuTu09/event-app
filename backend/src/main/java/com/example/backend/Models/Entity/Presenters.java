package com.example.backend.Models.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name = "presenters")
@NoArgsConstructor
@ToString
public class Presenters {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "presenter_id")
    private Integer presenterId;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 255)
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Size(max = 255)
    @Column(name = "company", length = 255)
    private String company;

    @Size(max = 255)
    @Column(name = "title", length = 255)
    private String title;
}