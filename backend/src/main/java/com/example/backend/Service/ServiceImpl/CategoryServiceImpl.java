package com.example.backend.Service.ServiceImpl;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.DTO.Request.CategoryRequestDTO;
import com.example.backend.DTO.Request.CategoryTranslationRequestDTO;
import com.example.backend.DTO.Response.AdminCategoryResponseDTO;
import com.example.backend.DTO.Response.CategoryResponseDTO;
import com.example.backend.Exception.ResourceNotFoundException;
import com.example.backend.Models.Entity.Category;
import com.example.backend.Models.Entity.CategoryTranslation;
import com.example.backend.Repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl {

    private final CategoryRepository categoryRepository;

    // 1. Lấy cây category
    public List<CategoryResponseDTO> getCategoryTree(String lang) {
        List<Category> roots = categoryRepository
                .findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc();
        return roots.stream()
                .map(root -> mapToDTO(root, lang, true))
                .collect(Collectors.toList());
    }

    // 2. Lấy chi tiết theo translation slug (không cần lang vì slug đã unique per lang)
    public CategoryResponseDTO getCategoryBySlug(String slug, String lang) {
        Category category = categoryRepository.findByTranslationSlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category không tồn tại: " + slug));
        return mapToDTO(category, lang, true);
    }

    // 3. Admin — flat list
    public List<AdminCategoryResponseDTO> getAllForAdmin() {
        return categoryRepository.findAll().stream()
                .map(this::mapToAdminDTO)
                .collect(Collectors.toList());
    }

    // 4. Admin — theo ID
    public AdminCategoryResponseDTO getCategoryByIdForAdmin(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category không tồn tại với ID: " + id));
        return mapToAdminDTO(category);
    }

    // 5. Tạo mới
    @Transactional
    public AdminCategoryResponseDTO createCategory(CategoryRequestDTO request) {
        Category parent = resolveParent(request.getParentId(), null);

        Category category = Category.builder()
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .parent(parent)
                .build();

        if (request.getTranslations() != null) {
            request.getTranslations().forEach((langCode, transReq) -> {
                String slug = resolveTranslationSlug(transReq, null);
                CategoryTranslation translation = CategoryTranslation.builder()
                        .languageCode(langCode)
                        .name(transReq.getName())
                        .slug(slug)
                        .seoTitle(transReq.getSeoTitle())
                        .seoDescription(transReq.getSeoDescription())
                        .build();
                category.addTranslation(translation);
            });
        }

        return mapToAdminDTO(categoryRepository.save(category));
    }

    // 6. Cập nhật
    @Transactional
    public AdminCategoryResponseDTO updateCategory(Long id, CategoryRequestDTO request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category không tồn tại với ID: " + id));

        category.setParent(resolveParent(request.getParentId(), id));

        if (request.getDisplayOrder() != null) category.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) category.setIsActive(request.getIsActive());

        if (request.getTranslations() != null) {
            request.getTranslations().forEach((langCode, transReq) -> {
                Optional<CategoryTranslation> existingOpt = category.getTranslations().stream()
                        .filter(t -> t.getLanguageCode().equalsIgnoreCase(langCode))
                        .findFirst();

                if (existingOpt.isPresent()) {
                    CategoryTranslation existing = existingOpt.get();
                    // Chỉ update slug nếu có truyền slug mới
                    if (transReq.getSlug() != null && !transReq.getSlug().isBlank()
                            && !transReq.getSlug().equals(existing.getSlug())) {
                        String newSlug = transReq.getSlug();
                        if (categoryRepository.existsByTranslationSlug(newSlug)) {
                            newSlug += "-" + System.currentTimeMillis();
                        }
                        existing.setSlug(newSlug);
                    }
                    existing.setName(transReq.getName());
                    existing.setSeoTitle(transReq.getSeoTitle());
                    existing.setSeoDescription(transReq.getSeoDescription());
                } else {
                    String slug = resolveTranslationSlug(transReq, null);
                    CategoryTranslation newTrans = CategoryTranslation.builder()
                            .languageCode(langCode)
                            .name(transReq.getName())
                            .slug(slug)
                            .seoTitle(transReq.getSeoTitle())
                            .seoDescription(transReq.getSeoDescription())
                            .build();
                    category.addTranslation(newTrans);
                }
            });
        }

        return mapToAdminDTO(categoryRepository.save(category));
    }

    // 7. Xóa
    @Transactional
    public String deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category không tồn tại với ID: " + id));

        if (category.getChildren() != null && !category.getChildren().isEmpty())
            throw new IllegalStateException("Không thể xóa category đang có category con");

        if (category.getPosts() != null && !category.getPosts().isEmpty())
            throw new IllegalStateException("Không thể xóa category đang có bài viết");

        String name = category.getTranslations().stream()
                .filter(t -> t.getLanguageCode().equalsIgnoreCase("vi"))
                .findFirst()
                .map(CategoryTranslation::getName)
                .orElse("ID: " + id);

        categoryRepository.deleteById(id);
        return "Đã xóa category \"" + name + "\" thành công";
    }

    // ================================================================
    // MAPPER
    // ================================================================

    private CategoryResponseDTO mapToDTO(Category category, String lang, boolean includeChildren) {
        CategoryTranslation trans = resolveTranslation(category, lang);

        // alternateSlugs — map tất cả lang -> slug
        Map<String, String> alternateSlugs = category.getTranslations().stream()
                .filter(t -> t.getSlug() != null)
                .collect(Collectors.toMap(CategoryTranslation::getLanguageCode, CategoryTranslation::getSlug));

        // Parent info
        CategoryResponseDTO.ParentInfo parentInfo = null;
        if (category.getParent() != null) {
            CategoryTranslation parentTrans = resolveTranslation(category.getParent(), lang);
            Map<String, String> parentAlternateSlugs = category.getParent().getTranslations().stream()
                    .filter(t -> t.getSlug() != null)
                    .collect(Collectors.toMap(CategoryTranslation::getLanguageCode, CategoryTranslation::getSlug));
            parentInfo = CategoryResponseDTO.ParentInfo.builder()
                    .id(category.getParent().getId())
                    .slug(parentTrans != null ? parentTrans.getSlug() : null)
                    .alternateSlugs(parentAlternateSlugs)
                    .name(parentTrans != null ? parentTrans.getName() : "")
                    .build();
        }

        // Children
        List<CategoryResponseDTO> children = null;
        if (includeChildren && category.getChildren() != null) {
            children = category.getChildren().stream()
                    .filter(child -> Boolean.TRUE.equals(child.getIsActive()))
                    .map(child -> mapToDTO(child, lang, false))
                    .collect(Collectors.toList());
        }

        return CategoryResponseDTO.builder()
                .id(category.getId())
                .slug(trans != null ? trans.getSlug() : null)
                .languageCode(lang)
                .name(trans != null ? trans.getName() : "")
                .seoTitle(trans != null ? trans.getSeoTitle() : null)
                .seoDescription(trans != null ? trans.getSeoDescription() : null)
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .alternateSlugs(alternateSlugs)
                .parent(parentInfo)
                .children(children)
                .build();
    }

    private AdminCategoryResponseDTO mapToAdminDTO(Category category) {
        Map<String, AdminCategoryResponseDTO.TranslationDetail> translationMap =
                category.getTranslations().stream()
                        .collect(Collectors.toMap(
                                CategoryTranslation::getLanguageCode,
                                t -> AdminCategoryResponseDTO.TranslationDetail.builder()
                                        .name(t.getName())
                                        .slug(t.getSlug())          // THÊM
                                        .seoTitle(t.getSeoTitle())
                                        .seoDescription(t.getSeoDescription())
                                        .build()
                        ));

        AdminCategoryResponseDTO.ParentInfo parentInfo = null;
        if (category.getParent() != null) {
            parentInfo = AdminCategoryResponseDTO.ParentInfo.builder()
                    .id(category.getParent().getId())
                    .build();
        }

        List<AdminCategoryResponseDTO> children = null;
        if (category.getChildren() != null) {
            children = category.getChildren().stream()
                    .map(this::mapToAdminDTO)
                    .collect(Collectors.toList());
        }

        return AdminCategoryResponseDTO.builder()
                .id(category.getId())
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .parent(parentInfo)
                .translations(translationMap)
                .children(children)
                .build();
    }

    // ================================================================
    // HELPER
    // ================================================================

    private Category resolveParent(Long parentId, Long currentId) {
        if (parentId == null) return null;
        if (parentId.equals(currentId))
            throw new IllegalArgumentException("Category không thể là parent của chính nó");
        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent category không tồn tại"));
        if (parent.getParent() != null)
            throw new IllegalArgumentException("Chỉ hỗ trợ tối đa 2 cấp category");
        return parent;
    }

    // Generate hoặc dùng slug được truyền vào, tự động thêm timestamp nếu trùng
    private String resolveTranslationSlug(CategoryTranslationRequestDTO transReq, String existingSlug) {
        String slug = (transReq.getSlug() != null && !transReq.getSlug().isBlank())
                ? transReq.getSlug()
                : generateSlug(transReq.getName());
        if (!slug.equals(existingSlug) && categoryRepository.existsByTranslationSlug(slug)) {
            slug += "-" + System.currentTimeMillis();
        }
        return slug;
    }

    private CategoryTranslation resolveTranslation(Category category, String lang) {
        if (category.getTranslations() == null || category.getTranslations().isEmpty()) return null;
        return category.getTranslations().stream()
                .filter(t -> t.getLanguageCode().equalsIgnoreCase(lang))
                .findFirst()
                .orElseGet(() -> category.getTranslations().stream()
                        .filter(t -> t.getLanguageCode().equalsIgnoreCase("vi"))
                        .findFirst()
                        .orElse(category.getTranslations().get(0)));
    }

    private String generateSlug(String name) {
        if (name == null || name.isBlank()) return "category-" + System.currentTimeMillis();
        String slug = Normalizer.normalize(name, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        slug = pattern.matcher(slug).replaceAll("");
        slug = slug.toLowerCase(Locale.ENGLISH);
        slug = slug.replaceAll("[^a-z0-9\\s-]", "");
        slug = slug.replaceAll("\\s+", "-");
        return slug;
    }
}