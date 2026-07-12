package com.transitops.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RbacInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String role = (String) request.getSession().getAttribute("role");

        // Allow Thymeleaf pages, login/logout, session check, and static assets
        if (uri.equals("/") || uri.equals("/index") || uri.startsWith("/login") || uri.equals("/logout") ||
                uri.equals("/api/session/user") || uri.startsWith("/static/") || uri.endsWith(".css") || uri.endsWith(".js")) {
            return true;
        }

        // If not logged in, block API access
        if (role == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please login first");
            return false;
        }

        // --- RBAC Rules ---
        if (role.equals("FLEET_MANAGER")) {
            return true; // Fleet Manager has access to everything
        }
        else if (role.equals("DRIVER")) {
            if (uri.startsWith("/api/trips") || uri.startsWith("/api/dashboard") || uri.startsWith("/api/vehicles") || uri.startsWith("/api/drivers")) {
                return true;
            }
        }
        else if (role.equals("SAFETY_OFFICER")) {
            if (uri.startsWith("/api/drivers") || uri.startsWith("/api/dashboard") || (uri.startsWith("/api/trips") && request.getMethod().equals("GET")) ||
                    (uri.startsWith("/api/vehicles") && request.getMethod().equals("GET"))) {
                return true;
            }
        }
        else if (role.equals("FINANCIAL_ANALYST")) {
            if (uri.startsWith("/api/expenses") || uri.startsWith("/api/fuel") || uri.startsWith("/api/analytics") || uri.startsWith("/api/dashboard") || (uri.startsWith("/api/vehicles") && request.getMethod().equals("GET"))) {
                return true;
            }
        }

        // If role doesn't match any rule
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied for your role");
        return false;
    }
}