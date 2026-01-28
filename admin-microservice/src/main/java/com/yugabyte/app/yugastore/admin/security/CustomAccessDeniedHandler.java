package com.yugabyte.app.yugastore.admin.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yugabyte.app.yugastore.admin.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Custom access denied handler for returning 403 errors with proper format
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            "Forbidden",
            "You do not have permission to access this resource. Please contact your administrator if you believe this is an error."
        );

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
