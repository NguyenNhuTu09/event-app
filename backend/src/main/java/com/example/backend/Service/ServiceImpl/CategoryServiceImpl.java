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

    // ================================================================
    // PUBLIC API
    // ================================================================

    // 1. Lấy toàn bộ cây category (chỉ gốc + con) theo ngôn ngữ
    public List<CategoryResponseDTO> getCategoryTree(String lang) {
        List<Category> roots = categoryRepository
                .findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc();

        return roots.stream()
                .map(root -> mapToDTO(root, lang, true))
                .collect(Collectors.toList());
    }

    // 2. Lấy chi tiết 1 category theo slug
    public CategoryResponseDTO getCategoryBySlug(String slug, String lang) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category không tồn tại: " + slug));

        return mapToDTO(category, lang, true);
    }

    // ================================================================
    // ADMIN API
    // ================================================================

    // 3. Lấy tất cả category cho admin (flat list, đủ translations)
    public List<AdminCategoryResponseDTO> getAllForAdmin() {
        return categoryRepository.findAll().stream()
                .map(this::mapToAdminDTO)
                .collect(Collectors.toList());
    }

    // 4. Lấy chi tiết 1 category cho admin theo ID
    public AdminCategoryResponseDTO getCategoryByIdForAdmin(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category không tồn tại với ID: " + id));

        return mapToAdminDTO(category);
    }

    // 5. Tạo mới category
    @Transactional
    public AdminCategoryResponseDTO createCategory(CategoryRequestDTO request) {
        // Xử lý parent
        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category không tồn tại"));

            // Chỉ cho phép tối đa 2 cấp
            if (parent.getParent() != null) {
                throw new IllegalArgumentException("Chỉ hỗ trợ tối đa 2 cấp category");
            }
        }

        // Auto-generate slug nếu không truyền vào
        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? request.getSlug()
                : generateSlugFromRequest(request);

        if (categoryRepository.existsBySlug(slug)) {
            slug += "-" + System.currentTimeMillis();
        }

        Category category = Category.builder()
                .slug(slug)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .parent(parent)
                .build();

        if (request.getTranslations() != null) {
            request.getTranslations().forEach((langCode, transReq) -> {
                CategoryTranslation translation = CategoryTranslation.builder()
                        .languageCode(langCode)
                        .name(transReq.getName())
                        .seoTitle(transReq.getSeoTitle())
                        .seoDescription(transReq.getSeoDescription())
                        .build();
                category.addTranslation(translation);
            });
        }

        return mapToAdminDTO(categoryRepository.save(category));
    }

    // 6. Cập nhật category
    @Transactional
    public AdminCategoryResponseDTO updateCategory(Long id, CategoryRequestDTO request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category không tồn tại với ID: " + id));

        // Cập nhật parent
        if (request.getParentId() != null) {
            // Không cho phép tự set chính mình làm parent
            if (request.getParentId().equals(id)) {
                throw new IllegalArgumentException("Category không thể là parent của chính nó");
            }

            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category không tồn tại"));

            if (parent.getParent() != null) {
                throw new IllegalArgumentException("Chỉ hỗ trợ tối đa 2 cấp category");
            }

            category.setParent(parent);
        } else {
            // parentId = null → nâng lên thành category gốc
            category.setParent(null);
        }

        // Cập nhật slug nếu có truyền vào
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String newSlug = request.getSlug();
            if (!newSlug.equals(category.getSlug()) && categoryRepository.existsBySlug(newSlug)) {
                newSlug += "-" + System.currentTimeMillis();
            }
            category.setSlug(newSlug);
        }

        if (request.getDisplayOrder() != null) category.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) category.setIsActive(request.getIsActive());

        // Cập nhật translations
        if (request.getTranslations() != null) {
            request.getTranslations().forEach((langCode, transReq) -> {
                Optional<CategoryTranslation> existingOpt = category.getTranslations().stream()
                        .filter(t -> t.getLanguageCode().equalsIgnoreCase(langCode))
                        .findFirst();

                if (existingOpt.isPresent()) {
                    CategoryTranslation existing = existingOpt.get();
                    existing.setName(transReq.getName());
                    existing.setSeoTitle(transReq.getSeoTitle());
                    existing.setSeoDescription(transReq.getSeoDescription());
                } else {
                    CategoryTranslation newTrans = CategoryTranslation.builder()
                            .languageCode(langCode)
                            .name(transReq.getName())
                            .seoTitle(transReq.getSeoTitle())
                            .seoDescription(transReq.getSeoDescription())
                            .build();
                    category.addTranslation(newTrans);
                }
            });
        }

        return mapToAdminDTO(categoryRepository.save(category));
    }

    // 7. Xóa category
    @Transactional
    public String deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category không tồn tại với ID: " + id));

        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            throw new IllegalStateException("Không thể xóa category đang có category con");
        }

        if (category.getPosts() != null && !category.getPosts().isEmpty()) {
            throw new IllegalStateException("Không thể xóa category đang có bài viết");
        }

        String name = category.getTranslations().stream()
                .filter(t -> t.getLanguageCode().equalsIgnoreCase("vi"))
                .findFirst()
                .map(t -> t.getName())
                .orElse("ID: " + id);

        categoryRepository.deleteById(id);

        return "Đã xóa category \"" + name + "\" thành công";
    }

    // ================================================================
    // MAPPER
    // ================================================================

    private CategoryResponseDTO mapToDTO(Category category, String lang, boolean includeChildren) {
        CategoryTranslation trans = resolveTranslation(category, lang);

        // Map parent info
        CategoryResponseDTO.ParentInfo parentInfo = null;
        if (category.getParent() != null) {
            CategoryTranslation parentTrans = resolveTranslation(category.getParent(), lang);
            parentInfo = CategoryResponseDTO.ParentInfo.builder()
                    .id(category.getParent().getId())
                    .slug(category.getParent().getSlug())
                    .name(parentTrans != null ? parentTrans.getName() : "")
                    .build();
        }

        // Map children (chỉ 1 cấp, không đệ quy tiếp)
        List<CategoryResponseDTO> children = null;
        if (includeChildren && category.getChildren() != null) {
            children = category.getChildren().stream()
                    .filter(child -> Boolean.TRUE.equals(child.getIsActive()))
                    .map(child -> mapToDTO(child, lang, false))
                    .collect(Collectors.toList());
        }

        return CategoryResponseDTO.builder()
                .id(category.getId())
                .slug(category.getSlug())
                .languageCode(lang)
                .name(trans != null ? trans.getName() : "")
                .seoTitle(trans != null ? trans.getSeoTitle() : null)
                .seoDescription(trans != null ? trans.getSeoDescription() : null)
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .parent(parentInfo)
                .children(children)
                .build();
    }

    private AdminCategoryResponseDTO mapToAdminDTO(Category category) {
        // Map tất cả translations thành Map<langCode, TranslationDetail>
        Map<String, AdminCategoryResponseDTO.TranslationDetail> translationMap =
                category.getTranslations().stream()
                        .collect(Collectors.toMap(
                                CategoryTranslation::getLanguageCode,
                                t -> AdminCategoryResponseDTO.TranslationDetail.builder()
                                        .name(t.getName())
                                        .seoTitle(t.getSeoTitle())
                                        .seoDescription(t.getSeoDescription())
                                        .build()
                        ));

        // Map parent (shallow)
        AdminCategoryResponseDTO.ParentInfo parentInfo = null;
        if (category.getParent() != null) {
            parentInfo = AdminCategoryResponseDTO.ParentInfo.builder()
                    .id(category.getParent().getId())
                    .slug(category.getParent().getSlug())
                    .build();
        }

        // Map children (shallow, không đệ quy)
        List<AdminCategoryResponseDTO> children = null;
        if (category.getChildren() != null) {
            children = category.getChildren().stream()
                    .map(this::mapToAdminDTO)
                    .collect(Collectors.toList());
        }

        return AdminCategoryResponseDTO.builder()
                .id(category.getId())
                .slug(category.getSlug())
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .parent(parentInfo)
                .translations(translationMap)
                .children(children)
                .build();
    }

    // Resolve translation theo lang, fallback về "vi", rồi fallback về phần tử đầu tiên
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

    // Auto-generate slug từ tên "vi", fallback sang "en", fallback sang timestamp
    private String generateSlugFromRequest(CategoryRequestDTO request) {
        if (request.getTranslations() == null) return "category-" + System.currentTimeMillis();

        String name = null;
        if (request.getTranslations().containsKey("vi")) {
            name = request.getTranslations().get("vi").getName();
        } else if (request.getTranslations().containsKey("en")) {
            name = request.getTranslations().get("en").getName();
        } else {
            name = request.getTranslations().values().iterator().next().getName();
        }

        return generateSlug(name);
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