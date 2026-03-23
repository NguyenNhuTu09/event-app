package com.example.backend.Config;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.backend.Models.Entity.User;
import com.example.backend.Service.AuthService;
import com.example.backend.Service.JwtService;
import com.example.backend.Service.RefreshTokenService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    private static final List<String> ALLOWED_REDIRECT_URIS = List.of(
        "https://ems.webie.com.vn/oauth2/redirect",   // web
        "myapp://oauth2/redirect"                      // Android deep link
    );

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        String targetUrl = determineTargetUrl(request, response, authentication);
        response.sendRedirect(targetUrl);
    }

    private String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) {
        // Lấy redirect_uri client truyền lên
        String redirectUri = HttpCookieOAuth2AuthorizationRequestRepository.getRedirectUri(request);

        // Validate — chỉ cho phép URI trong whitelist
        if (StringUtils.hasText(redirectUri) && isAuthorizedRedirectUri(redirectUri)) {
            // dùng redirect_uri từ client
        } else {
            // fallback về web
            redirectUri = "https://ems.webie.com.vn/oauth2/redirect";
        }

        DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
        String email   = oauthUser.getAttribute("email");
        String name    = oauthUser.getAttribute("name");
        String picture = oauthUser.getAttribute("picture");

        User user = authService.processOAuthPostLogin(email, name, picture);

        String accessToken  = jwtService.generateToken(user.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        return UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("uid", user.getUid())
                .build().toUriString();
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        URI clientRedirectUri = URI.create(uri);
        return ALLOWED_REDIRECT_URIS.stream().anyMatch(authorizedUri -> {
            URI authorized = URI.create(authorizedUri);
            // So sánh scheme + host + path, bỏ qua query params
            return authorized.getScheme().equalsIgnoreCase(clientRedirectUri.getScheme())
                && authorized.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                && authorized.getPath().equalsIgnoreCase(clientRedirectUri.getPath());
        });
    }
}


