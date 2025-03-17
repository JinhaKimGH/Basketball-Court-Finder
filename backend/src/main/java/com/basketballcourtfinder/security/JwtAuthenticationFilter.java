package com.basketballcourtfinder.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  @Value("${jwt.secret}")
  private String jwtSecret;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {
    String path = request.getRequestURI();
    List<String> list = Arrays.asList("/api/users/login", "/api/users/sign-up", "/api/courts/around");
    if (list.contains(path)) {
      filterChain.doFilter(request, response);
      return;
    }

    // Get token from HttpOnly cookie
    String token = null;
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("BCourtFindertoken".equals(cookie.getName())) {
          token = cookie.getValue();
          break;
        }
      }
    }

    if (token != null) {
      try {
        // Key for verification
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();

        // Check for token expiration
        Date expiration = claims.getExpiration();
        if (expiration != null && expiration.before(new Date())) {
          response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has expired");
          return;
        }

        // Extract user ID from claims
        Long userId = claims.get("userID", Long.class);

        // Create Authentication token with user ID as principal
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            userId, null, Collections.emptyList());

        // Set the authentication in the security context
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

      } catch (Exception e) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
        return;
      }
    }

    filterChain.doFilter(request, response);
  }
}
