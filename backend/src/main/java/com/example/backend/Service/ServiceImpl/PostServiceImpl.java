package com.example.backend.Service.ServiceImpl;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.backend.DTO.Request.PostRequestDTO;
import com.example.backend.DTO.Response.PostResponseDTO;
import com.example.backend.Exception.ResourceNotFoundException;
import com.example.backend.Models.Entity.Post;
import com.example.backend.Models.Entity.User;
import com.example.backend.Repository.PostRepository;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Utils.PostStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostServiceImpl {
    private final PostRepository postRepository;
    private final UserRepository userRepository;


    public Page<PostResponseDTO> getAllPublishedPosts(Pageable pageable) {
        return postRepository.findByStatusOrderByCreatedAtDesc(PostStatus.PUBLISHED, pageable)
                .map(this::mapToDTO);
    }

    public PostResponseDTO getPostBySlug(String slug) {
        Post post = postRepository.findBySlugAndStatus(slug, PostStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không tồn tại"));
        
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
        
        return mapToDTO(post);
    }


    public PostResponseDTO createPost(PostRequestDTO request, Long userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String slug = generateSlug(request.getTitle());

        Post post = Post.builder()
                .title(request.getTitle())
                .slug(slug)
                .summary(request.getSummary())
                .content(request.getContent())
                .thumbnailUrl(request.getThumbnailUrl())
                .status(request.getStatus() != null ? request.getStatus() : PostStatus.DRAFT)
                .author(author)
                .viewCount(0L)
                .build();

        return mapToDTO(postRepository.save(post));
    }

    public PostResponseDTO updatePost(Long id, PostRequestDTO request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        post.setTitle(request.getTitle());
        post.setSummary(request.getSummary());
        post.setContent(request.getContent());
        post.setThumbnailUrl(request.getThumbnailUrl());
        post.setStatus(request.getStatus());

        return mapToDTO(postRepository.save(post));
    }

    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Post not found");
        }
        postRepository.deleteById(id);
    }


    private PostResponseDTO mapToDTO(Post post) {
        return PostResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .summary(post.getSummary())
                .content(post.getContent())
                .thumbnailUrl(post.getThumbnailUrl())
                .authorName(post.getAuthor() != null ? post.getAuthor().getUsername() : "Unknown")
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .build();
    }

    private String generateSlug(String title) {
        String slug = Normalizer.normalize(title, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        slug = pattern.matcher(slug).replaceAll("");
        slug = slug.toLowerCase(Locale.ENGLISH);
        slug = slug.replaceAll("[^a-z0-9\\s-]", "");
        slug = slug.replaceAll("\\s+", "-");

        if (postRepository.existsBySlug(slug)) {
            slug += "-" + System.currentTimeMillis(); 
        }
        return slug;
    }

    public PostResponseDTO getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không tồn tại với ID: " + id));
        
        return mapToDTO(post);
    }
}
