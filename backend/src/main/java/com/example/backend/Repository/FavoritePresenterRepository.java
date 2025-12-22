package com.example.backend.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.Models.Entity.FavoritePresenter;

@Repository
public interface FavoritePresenterRepository extends JpaRepository<FavoritePresenter, Long> {
    boolean existsByUser_IdAndPresenter_PresenterId(Long userId, Integer presenterId);

    Optional<FavoritePresenter> findByUser_IdAndPresenter_PresenterId(Long userId, Integer presenterId);

    List<FavoritePresenter> findByUser_IdOrderByLikedAtDesc(Long userId);

}
