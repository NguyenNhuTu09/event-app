package com.example.backend.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.Models.Entity.FavoritePresenter;

@Repository
public interface FavoritePresenterRepository extends JpaRepository<FavoritePresenter, Long> {
    boolean existsByUser_IdAndPresenter_PresenterId(Long userId, Integer presenterId);

    Optional<FavoritePresenter> findByUser_IdAndPresenter_PresenterId(Long userId, Integer presenterId);

    List<FavoritePresenter> findByUser_IdOrderByLikedAtDesc(Long userId);

    /** Xoá tài khoản: xoá toàn bộ diễn giả yêu thích của user bằng một câu SQL. */
    @Modifying
    @Query("DELETE FROM FavoritePresenter f WHERE f.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);
}