package com.example.backend.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.Models.Entity.ActivityCategories;

@Repository
public interface ActivityCategoriesRepository extends JpaRepository<ActivityCategories, Integer> {

    Optional<ActivityCategories> findByCategoryName(String categoryName);

    boolean existsByCategoryName(String categoryName);
}