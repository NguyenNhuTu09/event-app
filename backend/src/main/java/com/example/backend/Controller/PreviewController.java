package com.example.backend.Controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.Models.Entity.PostTranslation;
import com.example.backend.Repository.PostTranslationRepository;
import com.example.backend.Utils.PostStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/preview")
@RequiredArgsConstructor
@Tag(name = "Social Media Preview (OG Tags)")
public class PreviewController {

    private final PostTranslationRepository translationRepository;

    private static final String FRONTEND_BASE_URL = "https://ems.webie.com.vn";

    private static final String DEFAULT_IMAGE = "https://ems.webie.com.vn/og-default.jpg";

    // Tên site hiển thị trong OG tags
    private static final String SITE_NAME = "EMS Webie";

    @Operation(summary = "Trả về HTML với OG tags cho Social Media Crawler")
    @GetMapping("/{lang}/news/{slug}")
    public ResponseEntity<String> previewPost(
            @PathVariable String lang,
            @PathVariable String slug) {

        // 1. Tìm bài viết trong DB
        PostTranslation translation = translationRepository
                .findBySlugAndLanguageCodeAndPostStatus(slug, lang, PostStatus.PUBLISHED)
                .orElse(null);

        // 2. Lấy thông tin — fallback về giá trị mặc định nếu không tìm thấy
        String title       = (translation != null && translation.getTitle() != null)
                                ? escapeHtml(translation.getTitle())
                                : SITE_NAME;

        String description = (translation != null && translation.getSummary() != null)
                                ? escapeHtml(translation.getSummary())
                                : "";

        String image       = (translation != null
                                && translation.getPost() != null
                                && translation.getPost().getThumbnailUrl() != null
                                && !translation.getPost().getThumbnailUrl().isBlank())
                                ? translation.getPost().getThumbnailUrl()
                                : DEFAULT_IMAGE;

        // 3. Đảm bảo image là absolute URL
        if (!image.startsWith("http")) {
            image = FRONTEND_BASE_URL + "/" + image.replaceAll("^/+", "");
        }

        // 4. URL trang thực tế để redirect user sau khi crawler đọc xong
        String pageUrl = FRONTEND_BASE_URL + "/" + lang + "/news/" + slug;

        // 5. Build HTML response
        String html = buildHtml(title, description, image, pageUrl, lang);

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    // ── HELPER: Build HTML với đầy đủ OG + Twitter Card tags ─────────────────

    private String buildHtml(String title, String description, String image,
                              String pageUrl, String lang) {
        return """
                <!DOCTYPE html>
                <html lang="%s">
                <head>
                  <meta charset="UTF-8"/>
                  <title>%s</title>

                  <!-- ═══ Open Graph (Facebook, Zalo, LinkedIn) ═══ -->
                  <meta property="og:type"         content="article"/>
                  <meta property="og:site_name"    content="%s"/>
                  <meta property="og:url"          content="%s"/>
                  <meta property="og:title"        content="%s"/>
                  <meta property="og:description"  content="%s"/>
                  <meta property="og:image"        content="%s"/>
                  <meta property="og:image:width"  content="1200"/>
                  <meta property="og:image:height" content="630"/>
                  <meta property="og:image:alt"    content="%s"/>
                  <meta property="og:locale"       content="%s"/>

                  <!-- ═══ Twitter Card ═══ -->
                  <meta name="twitter:card"        content="summary_large_image"/>
                  <meta name="twitter:title"       content="%s"/>
                  <meta name="twitter:description" content="%s"/>
                  <meta name="twitter:image"       content="%s"/>

                  <!-- ═══ Redirect user thường về React (crawler sẽ không follow) ═══ -->
                  <meta http-equiv="refresh" content="0;url=%s"/>
                </head>
                <body>
                  <p>Đang chuyển hướng... <a href="%s">Nhấn vào đây nếu không được chuyển tự động</a></p>
                </body>
                </html>
                """.formatted(
                        lang,
                        title,
                        SITE_NAME,
                        pageUrl,
                        title,
                        description,
                        image,
                        title,
                        lang.equals("vi") ? "vi_VN" : "en_US",
                        title,
                        description,
                        image,
                        pageUrl,
                        pageUrl
                );
    }

    // ── HELPER: Escape ký tự đặc biệt tránh break HTML attribute ─────────────

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}