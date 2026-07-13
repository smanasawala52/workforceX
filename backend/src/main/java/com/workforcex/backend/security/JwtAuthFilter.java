package com.workforcex.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (isRawDocumentRequest(request)) {
            // Narrow, deliberate exception: Android opens this URL via an
            // external ACTION_VIEW intent (browser/PDF viewer), which cannot
            // attach an Authorization header. Only this single dev-only
            // local-storage endpoint accepts a token query param; every
            // other endpoint still requires the header. In prod this path
            // isn't used at all (documents are served via signed Supabase
            // Storage URLs instead).
            token = request.getParameter("token");
        }

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String mobileNumber = extractMobileNumberSafely(token);

        if (mobileNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(mobileNumber);

            if (jwtUtil.isTokenValid(token, mobileNumber)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractMobileNumberSafely(String token) {
        try {
            return jwtUtil.extractMobileNumber(token);
        } catch (Exception e) {
            return null; // malformed/expired token - treat as unauthenticated
        }
    }

    private boolean isRawDocumentRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null
                && uri.startsWith("/api/verification/documents/")
                && uri.endsWith("/raw");
    }
}
