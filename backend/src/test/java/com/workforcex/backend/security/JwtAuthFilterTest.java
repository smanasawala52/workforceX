package com.workforcex.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock private JwtUtil jwtUtil;
    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(jwtUtil, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_noAuthHeaderAndNotRawDocumentRequest_skipsAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/jobs");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil, userDetailsService);
    }

    @Test
    void doFilterInternal_validBearerToken_setsAuthentication() throws Exception {
        UserDetails userDetails = new User(
                "+919876543210", "hash", List.of(new SimpleGrantedAuthority("ROLE_WORKER")));

        when(request.getHeader("Authorization")).thenReturn("Bearer valid.token.here");
        when(jwtUtil.extractMobileNumber("valid.token.here")).thenReturn("+919876543210");
        when(userDetailsService.loadUserByUsername("+919876543210")).thenReturn(userDetails);
        when(jwtUtil.isTokenValid("valid.token.here", "+919876543210")).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(userDetails);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_invalidToken_doesNotSetAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer bad.token.here");
        when(jwtUtil.extractMobileNumber("bad.token.here")).thenReturn("+919876543210");
        when(userDetailsService.loadUserByUsername("+919876543210"))
                .thenReturn(new User("+919876543210", "hash", List.of()));
        when(jwtUtil.isTokenValid("bad.token.here", "+919876543210")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_malformedToken_extractThrows_treatedAsUnauthenticated() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer garbage");
        when(jwtUtil.extractMobileNumber("garbage")).thenThrow(new RuntimeException("bad token"));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilterInternal_alreadyAuthenticated_doesNotOverwriteContext() throws Exception {
        UserDetails existing = new User("+919999999999", "hash", List.of());
        var existingAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                existing, null, existing.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getHeader("Authorization")).thenReturn("Bearer valid.token.here");
        when(jwtUtil.extractMobileNumber("valid.token.here")).thenReturn("+919876543210");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(existingAuth);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilterInternal_rawDocumentRequestWithTokenParam_usesQueryParamToken() throws Exception {
        UserDetails userDetails = new User(
                "+919876543210", "hash", List.of(new SimpleGrantedAuthority("ROLE_EMPLOYER")));

        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/verification/documents/123/raw");
        when(request.getParameter("token")).thenReturn("query.param.token");
        when(jwtUtil.extractMobileNumber("query.param.token")).thenReturn("+919876543210");
        when(userDetailsService.loadUserByUsername("+919876543210")).thenReturn(userDetails);
        when(jwtUtil.isTokenValid("query.param.token", "+919876543210")).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(userDetails);
    }

    @Test
    void doFilterInternal_rawDocumentRequestWithoutTokenParam_skipsAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/verification/documents/123/raw");
        when(request.getParameter("token")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil, userDetailsService);
    }

    @Test
    void doFilterInternal_authHeaderNotBearer_andNotRawDocument_ignoresHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic somecreds");
        when(request.getRequestURI()).thenReturn("/api/jobs");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil, userDetailsService);
    }
}
