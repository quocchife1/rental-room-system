package com.example.rental.repository;

import com.example.rental.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    // Tìm ảnh theo ID bài đăng
    List<PostImage> findByPostId(Long postId);

    // Tìm ảnh thumbnail của bài đăng
    PostImage findByPostIdAndIsThumbnailTrue(Long postId);
}