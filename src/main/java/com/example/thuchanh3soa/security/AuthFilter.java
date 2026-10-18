package com.example.thuchanh3soa.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Middleware: xac thuc qua service cua bai thuc hanh 2.
 * Chuyen header Authorization sang GET {auth.service.url}/auth.
 * TH2 tra 200 -> cho di tiep, 401 -> tu choi.
 */
@Component
public class AuthFilter extends OncePerRequestFilter {

    private final RestClient restClient;

    public AuthFilter(@Value("${auth.service.url}") String authServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(authServiceUrl).build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().equals("/error");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            reject(response, 401, "Chua co token");
            return;
        }

        try {
            restClient.get()
                    .uri("/auth")
                    .header("Authorization", header)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            reject(response, 401, "Token khong hop le hoac da het han");
            return;
        } catch (RestClientException e) {
            reject(response, 503, "Khong ket noi duoc service xac thuc (TH2)");
            return;
        }

        chain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
