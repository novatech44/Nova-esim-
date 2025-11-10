package com.e_sim.security;

import com.e_sim.config.AppProperties;
import com.e_sim.exception.UnauthorizedException;
import com.nimbusds.jose.shaded.gson.JsonObject;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AppProperties authProps;
    private final UserInfoService userDetailsService;
    private final JwtUtil jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            if (shouldNotFilter(request)) {
                chain.doFilter(request, response);
                return;
            }

            final String token = extractToken(request);
            if (token.isBlank()) throw new UnauthorizedException("Unauthorized");

            authenticateRequest(request, token);
            chain.doFilter(request, response);

        } catch (JwtException e) {
            handleAuthError(response, "Invalid or expired token",
                    HttpServletResponse.SC_UNAUTHORIZED, e);
        } catch (UnauthorizedException e) {
            handleAuthError(response, "Authorization is required for this resource. " +
                            "Please provide a valid token. ",
                    HttpServletResponse.SC_BAD_REQUEST, e);
        } catch (AuthenticationException e) {
            handleAuthError(response, "Authentication failed",
                    HttpServletResponse.SC_FORBIDDEN, e);
        } catch (Exception e) {
            handleAuthError(response, "Internal server error",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    private boolean shouldSkipAuthentication(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !uri.contains(authProps.getPathPrefix()) ||
                Arrays.stream(authProps.getPermitAllPaths())
                        .anyMatch(uri::contains);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        return header != null && header.startsWith(BEARER_PREFIX)
                ? header.substring(BEARER_PREFIX.length()).trim()
                : "";
    }

    private void authenticateRequest(HttpServletRequest request, String token) {
        String username = jwtService.getUsernameFromToken(token);

        if (!jwtService.isNotTokenExpired(token) || username == null) {
            throw new JwtException("Invalid token");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        var authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authToken);
        log.debug("Authenticated user: {}", username);
    }

    private void handleAuthError(HttpServletResponse response, String message,
                                 int status, Exception e) throws IOException {
        log.error("Authentication error: {} - {}", message, e.getMessage());

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        JsonObject json = new JsonObject();
        json.addProperty("responseMessage", message);
        json.addProperty("responseCode", status);

        try (PrintWriter writer = response.getWriter()) {
            writer.write(json.toString());
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return shouldSkipAuthentication(request);
    }
}
