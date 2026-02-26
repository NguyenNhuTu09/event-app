// File: com.example.backend.Service.ServiceImpl.PostServiceImpl.java
package com.example.backend.Service.ServiceImpl;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.DTO.Request.PostRequestDTO;
import com.example.backend.DTO.Response.PostResponseDTO;
import com.example.backend.Exception.ResourceNotFoundException;
import com.example.backend.Models.Entity.Post;
import com.example.backend.Models.Entity.PostTranslation;
import com.example.backend.Models.Entity.User;
import com.example.backend.Repository.PostRepository;
import com.example.backend.Repository.PostTranslationRepository;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Utils.PostStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostServiceImpl {
    private final PostRepository postRepository;
    private final PostTranslationRepository translationRepository;
    private final UserRepository userRepository;

    // 1. LẤY DANH SÁCH BÀI VIẾT (PUBLISHED)
    public Page<PostResponseDTO> getAllPublishedPosts(Pageable pageable, String lang) {
        return postRepository.findByStatusOrderByCreatedAtDesc(PostStatus.PUBLISHED, pageable)
                .map(post -> mapToDTO(post, lang));
    }

    // 2. LẤY CHI TIẾT QUA SLUG (Dành cho User Frontend)
    @Transactional
    public PostResponseDTO getPostBySlugAndLang(String slug, String lang) {
        PostTranslation translation = translationRepository
                .findBySlugAndLanguageCodeAndPostStatus(slug, lang, PostStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không tồn tại ở ngôn ngữ này"));
        
        Post post = translation.getPost();
        
        // Tăng view Count ở bảng cha
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
        
        return mapTranslationToDTO(post, translation);
    }

    // 3. TẠO BÀI VIẾT (Dành cho Admin)
    @Transactional
    public PostResponseDTO createPost(PostRequestDTO request, Long userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Post post = Post.builder()
                .thumbnailUrl(request.getThumbnailUrl())
                .status(request.getStatus() != null ? request.getStatus() : PostStatus.DRAFT)
                .author(author)
                .viewCount(0L)
                .build();

        if (request.getTranslations() != null) {
            request.getTranslations().forEach((langCode, transReq) -> {
                PostTranslation translation = PostTranslation.builder()
                        .languageCode(langCode)
                        .title(transReq.getTitle())
                        .slug(generateSlug(transReq.getTitle()))
                        .summary(transReq.getSummary())
                        .content(transReq.getContent())
                        .seoTitle(transReq.getSeoTitle())
                        .seoDescription(transReq.getSeoDescription())
                        .build();
                post.addTranslation(translation);
            });
        }

        Post savedPost = postRepository.save(post);
        return mapToDTO(savedPost, "vi"); // Trả về mặc định bản tiếng việt sau khi tạo
    }

    // 4. CẬP NHẬT BÀI VIẾT (HÀM ĐANG BỊ THIẾU)
    @Transactional
    public PostResponseDTO updatePost(Long id, PostRequestDTO request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        post.setThumbnailUrl(request.getThumbnailUrl());
        post.setStatus(request.getStatus());

        if (request.getTranslations() != null) {
            request.getTranslations().forEach((langCode, transReq) -> {
                Optional<PostTranslation> existingTransOpt = post.getTranslations().stream()
                        .filter(t -> t.getLanguageCode().equalsIgnoreCase(langCode))
                        .findFirst();

                if (existingTransOpt.isPresent()) {
                    PostTranslation existingTrans = existingTransOpt.get();
                    if (!existingTrans.getTitle().equals(transReq.getTitle())) {
                        existingTrans.setSlug(generateSlug(transReq.getTitle()));
                    }
                    existingTrans.setTitle(transReq.getTitle());
                    existingTrans.setSummary(transReq.getSummary());
                    existingTrans.setContent(transReq.getContent());
                    existingTrans.setSeoTitle(transReq.getSeoTitle());
                    existingTrans.setSeoDescription(transReq.getSeoDescription());
                } else {
                    PostTranslation newTranslation = PostTranslation.builder()
                            .languageCode(langCode)
                            .title(transReq.getTitle())
                            .slug(generateSlug(transReq.getTitle()))
                            .summary(transReq.getSummary())
                            .content(transReq.getContent())
                            .seoTitle(transReq.getSeoTitle())
                            .seoDescription(transReq.getSeoDescription())
                            .build();
                    post.addTranslation(newTranslation);
                }
            });
        }

        return mapToDTO(postRepository.save(post), "vi");
    }

    // 5. XÓA BÀI VIẾT (HÀM ĐANG BỊ THIẾU)
    @Transactional
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Post not found");
        }
        postRepository.deleteById(id);
    }

    // 6. LẤY CHI TIẾT THEO ID ĐỂ ADMIN SỬA (HÀM ĐANG BỊ THIẾU)
    public PostResponseDTO getPostById(Long id, String lang) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không tồn tại với ID: " + id));
        return mapToDTO(post, lang);
    }

    // --- CÁC HÀM MAPPER VÀ HELPER ---

    private PostResponseDTO mapToDTO(Post post, String targetLang) {
        if (post.getTranslations() == null || post.getTranslations().isEmpty()) return null;

        PostTranslation currentTrans = post.getTranslations().stream()
                .filter(t -> t.getLanguageCode().equalsIgnoreCase(targetLang))
                .findFirst()
                .orElseGet(() -> post.getTranslations().stream()
                        .filter(t -> t.getLanguageCode().equalsIgnoreCase("vi"))
                        .findFirst()
                        .orElse(post.getTranslations().get(0)));

        return mapTranslationToDTO(post, currentTrans);
    }

    private PostResponseDTO mapTranslationToDTO(Post post, PostTranslation translation) {
        Map<String, String> alternateSlugs = new HashMap<>();
        if (post.getTranslations() != null) {
            for (PostTranslation pt : post.getTranslations()) {
                alternateSlugs.put(pt.getLanguageCode(), pt.getSlug()); 
            }
        }

        return PostResponseDTO.builder()
                .id(post.getId())
                .languageCode(translation.getLanguageCode())
                .title(translation.getTitle())
                .slug(translation.getSlug())
                .summary(translation.getSummary())
                .content(translation.getContent())
                .seoTitle(translation.getSeoTitle())
                .seoDescription(translation.getSeoDescription())
                .alternateSlugs(alternateSlugs) // <-- NHÉT BIẾN VÀO ĐÂY
                .thumbnailUrl(post.getThumbnailUrl())
                .authorName(post.getAuthor() != null ? post.getAuthor().getUsername() : "Unknown")
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .build();
    }

    private String generateSlug(String title) {
        if (title == null || title.isEmpty()) return "untitled-" + System.currentTimeMillis();
        
        String slug = Normalizer.normalize(title, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        slug = pattern.matcher(slug).replaceAll("");
        slug = slug.toLowerCase(Locale.ENGLISH);
        slug = slug.replaceAll("[^a-z0-9\\s-]", "");
        slug = slug.replaceAll("\\s+", "-");

        if (translationRepository.existsBySlug(slug)) {
            slug += "-" + System.currentTimeMillis(); 
        }
        return slug;
    }
}