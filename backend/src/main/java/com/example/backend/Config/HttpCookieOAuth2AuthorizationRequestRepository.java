package com.example.backend.Config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Component
public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    private static final String OAUTH2_AUTH_REQUEST_ATTR = "oauth2_auth_request";

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return (OAuth2AuthorizationRequest) request.getSession()
                .getAttribute(OAUTH2_AUTH_REQUEST_ATTR);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authorizationRequest == null) {
            request.getSession().removeAttribute(OAUTH2_AUTH_REQUEST_ATTR);
            request.getSession().removeAttribute(REDIRECT_URI_PARAM_COOKIE_NAME);
            return;
        }

        request.getSession().setAttribute(OAUTH2_AUTH_REQUEST_ATTR, authorizationRequest);

        // Lưu redirect_uri từ query param vào session
        String redirectUri = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        if (StringUtils.hasText(redirectUri)) {
            request.getSession().setAttribute(REDIRECT_URI_PARAM_COOKIE_NAME, redirectUri);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                  HttpServletResponse response) {
        return loadAuthorizationRequest(request);
    }

    public static String getRedirectUri(HttpServletRequest request) {
        Object uri = request.getSession().getAttribute(REDIRECT_URI_PARAM_COOKIE_NAME);
        return uri != null ? uri.toString() : null;
    }
}