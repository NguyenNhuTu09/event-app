package com.example.backend.Service.ServiceImpl;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.DTO.Request.FeaturedPostRequestDTO;
import com.example.backend.DTO.Request.PostRequestDTO;
import com.example.backend.DTO.Response.AdminPostResponseDTO;
import com.example.backend.DTO.Response.PostResponseDTO;
import com.example.backend.Exception.ResourceNotFoundException;
import com.example.backend.Models.Entity.Category;
import com.example.backend.Models.Entity.CategoryTranslation;
import com.example.backend.Models.Entity.Post;
import com.example.backend.Models.Entity.PostTranslation;
import com.example.backend.Models.Entity.User;
import com.example.backend.Repository.CategoryRepository;
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
    private final CategoryRepository categoryRepository;

    // 1. LẤY DANH SÁCH BÀI VIẾT (PUBLISHED) — trả về thêm focusKeyword, tags
    // public Page<PostResponseDTO> getAllPublishedPosts(Pageable pageable, String lang) {
    //     return postRepository.findByStatusOrderByCreatedAtDesc(PostStatus.PUBLISHED, pageable)
    //             .map(post -> mapToDTO(post, lang));
    // }

    @Transactional
    public Page<PostResponseDTO> getAllPublishedPosts(Pageable pageable, String lang) {
        // Query 1: load post + translations + author
        List<Post> postsWithTranslations = postRepository.findPublishedWithTranslations(PostStatus.PUBLISHED);

        // Query 2: load category + category.translations, map theo id
        Map<Long, Post> postsWithCategory = postRepository.findPublishedWithCategory(PostStatus.PUBLISHED)
                .stream()
                .collect(Collectors.toMap(Post::getId, p -> p));

        // Gộp category vào post từ query 1
        postsWithTranslations.forEach(post -> {
            Post postWithCat = postsWithCategory.get(post.getId());
            if (postWithCat != null) {
                post.setCategory(postWithCat.getCategory());
            }
        });

        // Phân trang thủ công
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), postsWithTranslations.size());
        List<Post> pageContent = postsWithTranslations.subList(start, end);

        return new org.springframework.data.domain.PageImpl<>(
                pageContent.stream()
                        .map(post -> mapToDTO(post, lang))
                        .collect(Collectors.toList()),
                pageable,
                postsWithTranslations.size()
        );
    }

    // 2. LẤY CHI TIẾT QUA SLUG — trả về thêm focusKeyword, tags
    @Transactional
    public PostResponseDTO getPostBySlugAndLang(String slug, String lang) {
        PostTranslation translation = translationRepository
                .findBySlugAndLanguageCodeAndPostStatus(slug, lang, PostStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không tồn tại ở ngôn ngữ này"));

        Post post = translation.getPost();
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);

        return mapTranslationToDTO(post, translation);
    }

    // 3. TẠO BÀI VIẾT — nhận thêm focusKeyword, tags
    @Transactional
    public PostResponseDTO createPost(PostRequestDTO request, Long userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category không tồn tại với ID: " + request.getCategoryId()));
        }
        Post post = Post.builder()
                .thumbnailUrl(request.getThumbnailUrl())
                .status(request.getStatus() != null ? request.getStatus() : PostStatus.DRAFT)
                .author(author)
                .category(category)
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
                        .focusKeyword(transReq.getFocusKeyword())   // THÊM
                        .tags(transReq.getTags())                   // THÊM
                        .build();
                post.addTranslation(translation);
            });
        }

        Post savedPost = postRepository.save(post);
        return mapToDTO(savedPost, "vi");
    }

    // 4. CẬP NHẬT BÀI VIẾT — nhận thêm focusKeyword, tags
    @Transactional
    public PostResponseDTO updatePost(Long id, PostRequestDTO request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        post.setThumbnailUrl(request.getThumbnailUrl());
        post.setStatus(request.getStatus());

        if (request.getStatus() != null && request.getStatus() != PostStatus.PUBLISHED) {
            post.setFeatured(false);
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category không tồn tại với ID: " + request.getCategoryId()));
            post.setCategory(category);
        } else {
            post.setCategory(null);
        }

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
                    existingTrans.setFocusKeyword(transReq.getFocusKeyword()); // THÊM
                    existingTrans.setTags(transReq.getTags());                 // THÊM
                } else {
                    PostTranslation newTranslation = PostTranslation.builder()
                            .languageCode(langCode)
                            .title(transReq.getTitle())
                            .slug(generateSlug(transReq.getTitle()))
                            .summary(transReq.getSummary())
                            .content(transReq.getContent())
                            .seoTitle(transReq.getSeoTitle())
                            .seoDescription(transReq.getSeoDescription())
                            .focusKeyword(transReq.getFocusKeyword())          // THÊM
                            .tags(transReq.getTags())                          // THÊM
                            .build();
                    post.addTranslation(newTranslation);
                }
            });
        }

        return mapToDTO(postRepository.save(post), "vi");
    }

    // 5. XÓA BÀI VIẾT
    @Transactional
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Post not found");
        }
        postRepository.deleteById(id);
    }

    // 6. LẤY THEO ID CHO ADMIN — trả về AdminPostResponseDTO (1 call, đủ hết translations)
    public AdminPostResponseDTO getPostByIdForAdmin(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không tồn tại với ID: " + id));
        return mapToAdminDTO(post);
    }

    // 7. GIỮ LẠI để PostController (user) vẫn dùng được
    public PostResponseDTO getPostById(Long id, String lang) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không tồn tại với ID: " + id));
        return mapToDTO(post, lang);
    }

    

    private PostResponseDTO mapToDTO(Post post, String targetLang) {
        if (post.getTranslations() == null || post.getTranslations().isEmpty()) {
            return PostResponseDTO.builder()
                    .id(post.getId())
                    .thumbnailUrl(post.getThumbnailUrl())
                    .authorName(post.getAuthor() != null ? post.getAuthor().getUsername() : "Unknown")
                    .viewCount(post.getViewCount())
                    .createdAt(post.getCreatedAt())
                    .title("[No translation]")
                    .build();
        }

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
        Map<String, String> categoryAlternateSlugs = null;
        if (post.getCategory() != null && post.getCategory().getTranslations() != null) {
            categoryAlternateSlugs = post.getCategory().getTranslations().stream()
                    .filter(t -> t.getSlug() != null)
                    .collect(Collectors.toMap(
                        CategoryTranslation::getLanguageCode,
                        CategoryTranslation::getSlug
                    ));
        }
        Map<String, String> alternateSlugs = new HashMap<>();
        if (post.getTranslations() != null) {
            for (PostTranslation pt : post.getTranslations()) {
                alternateSlugs.put(pt.getLanguageCode(), pt.getSlug());
            }
        }

        Long categoryId = null;
        String categorySlug = null;
        String categoryName = null;

        if (post.getCategory() != null) {
            Category category = post.getCategory();
            categoryId = category.getId();

            String lang = translation.getLanguageCode();
            if (category.getTranslations() != null && !category.getTranslations().isEmpty()) {
                CategoryTranslation catTrans = category.getTranslations().stream()
                        .filter(t -> t.getLanguageCode().equalsIgnoreCase(lang))
                        .findFirst()
                        .orElseGet(() -> category.getTranslations().stream()
                                .filter(t -> t.getLanguageCode().equalsIgnoreCase("vi"))
                                .findFirst()
                                .orElse(category.getTranslations().get(0)));
                categoryName = catTrans.getName();
                categorySlug = catTrans.getSlug(); 
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
                .focusKeyword(translation.getFocusKeyword())   // THÊM
                .tags(translation.getTags())                   // THÊM
                .alternateSlugs(alternateSlugs)
                .thumbnailUrl(post.getThumbnailUrl())
                .authorName(post.getAuthor() != null ? post.getAuthor().getUsername() : "Unknown")
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .isFeatured(post.isFeatured())
                .categoryId(categoryId)        // THÊM
                .categorySlug(categorySlug)    // THÊM
                .categoryName(categoryName)    // THÊM
                .categoryAlternateSlugs(categoryAlternateSlugs)
                .build();
    }

    // Mapper riêng cho Admin — trả về toàn bộ translations dạng Map
    private AdminPostResponseDTO mapToAdminDTO(Post post) {
        Map<String, AdminPostResponseDTO.TranslationDetail> translationMap = post.getTranslations().stream()
                .collect(Collectors.toMap(
                        PostTranslation::getLanguageCode,
                        t -> AdminPostResponseDTO.TranslationDetail.builder()
                                .title(t.getTitle())
                                .slug(t.getSlug())
                                .summary(t.getSummary())
                                .content(t.getContent())
                                .seoTitle(t.getSeoTitle())
                                .seoDescription(t.getSeoDescription())
                                .focusKeyword(t.getFocusKeyword())   // THÊM
                                .tags(t.getTags())                   // THÊM
                                .build()
                ));

        return AdminPostResponseDTO.builder()
                .id(post.getId())
                .thumbnailUrl(post.getThumbnailUrl())
                .status(post.getStatus())
                .authorName(post.getAuthor() != null ? post.getAuthor().getUsername() : "Unknown")
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .translations(translationMap)
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

    @Transactional
    public List<PostResponseDTO> getFeaturedPosts(String lang) {
        List<Post> postsWithTranslations = postRepository.findFeaturedWithTranslations(PostStatus.PUBLISHED);


        System.out.println(">>> Featured posts count: " + postsWithTranslations.size());
        postsWithTranslations.forEach(p -> {
            System.out.println(">>> Post id=" + p.getId()
                + " | isFeatured=" + p.isFeatured()
                + " | translations size=" + (p.getTranslations() != null ? p.getTranslations().size() : "NULL")
                + " | author=" + (p.getAuthor() != null ? p.getAuthor().getUsername() : "NULL"));
        });

        if (postsWithTranslations.isEmpty()) return List.of();

        Map<Long, Post> postsWithCategory = postRepository.findFeaturedWithCategory(PostStatus.PUBLISHED)
                .stream()
                .collect(Collectors.toMap(Post::getId, p -> p));

        

        postsWithTranslations.forEach(post -> {
            Post postWithCat = postsWithCategory.get(post.getId());
            if (postWithCat != null) {
                post.setCategory(postWithCat.getCategory());
            }
            // LOG sau khi gộp category
            System.out.println(">>> Post id=" + post.getId()
                + " | category=" + (post.getCategory() != null ? post.getCategory().getId() : "NULL"));
        });

        List<PostResponseDTO> result = postsWithTranslations.stream()
                .map(post -> {
                    PostResponseDTO dto = mapToDTO(post, lang);
                    // LOG kết quả map
                    System.out.println(">>> mapToDTO post id=" + post.getId()
                        + " | result=" + (dto != null ? "OK - title: " + dto.getTitle() : "NULL"));
                    return dto;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());

        System.out.println(">>> Final result size: " + result.size());
        return result;
    }

    // Set danh sách bài viết nổi bật (Admin)
    @Transactional
    public List<PostResponseDTO> updateFeaturedPosts(FeaturedPostRequestDTO request) {
        if (request.getPostIds() == null || request.getPostIds().isEmpty()) {
            // Reset tất cả về không nổi bật
            List<Post> currentFeatured = postRepository.findAllByIsFeaturedTrue();
            currentFeatured.forEach(p -> p.setFeatured(false));
            postRepository.saveAll(currentFeatured);
            return List.of();
        }

        // Bỏ nổi bật tất cả post hiện tại
        List<Post> currentFeatured = postRepository.findAllByIsFeaturedTrue();
        currentFeatured.forEach(p -> p.setFeatured(false));
        postRepository.saveAll(currentFeatured);

        // Set nổi bật cho danh sách mới
        List<Post> newFeatured = postRepository.findAllById(request.getPostIds());

        if (newFeatured.size() != request.getPostIds().size()) {
            throw new ResourceNotFoundException("Một hoặc nhiều ID bài viết không tồn tại");
        }

        newFeatured.forEach(post -> {
            if (post.getStatus() != PostStatus.PUBLISHED) {
                throw new IllegalArgumentException(
                    "Bài viết \"" + post.getId() + "\" chưa được publish, không thể set nổi bật");
            }
            post.setFeatured(true);
        });

        postRepository.saveAll(newFeatured);

        return getFeaturedPosts("vi");
    }


    @Transactional
    public Map<String, Object> removeFeaturedPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không tồn tại với ID: " + postId));

        if (!post.isFeatured()) {
            throw new IllegalArgumentException("Bài viết ID " + postId + " không nằm trong danh sách nổi bật");
        }

        post.setFeatured(false);
        postRepository.save(post);

        return Map.of(
            "success", true,
            "message", "Đã xóa bài viết ID " + postId + " khỏi danh sách nổi bật"
        );
    }
}



