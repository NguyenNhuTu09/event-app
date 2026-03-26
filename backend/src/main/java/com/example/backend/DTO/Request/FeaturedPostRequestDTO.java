package com.example.backend.DTO.Request;

import java.util.List;

import lombok.Data;

@Data
public class FeaturedPostRequestDTO {
    private List<Long> postIds; // danh sách ID post muốn set nổi bật
}