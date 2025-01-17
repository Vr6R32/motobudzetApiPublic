package pl.motobudzet.api.infrastructure.configuration.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class AddressLoggingFilter extends OncePerRequestFilter {

    protected final List<String> IGNORED_PATHS = Arrays.asList("api/static", "css/", "actuator/prometheus", "js/", "api/spec", "api/brands", "api/cities", "api/models", "api/advertisements", "favicon.ico");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        boolean shouldIgnore = IGNORED_PATHS.stream().anyMatch(requestURI::contains);

        if (shouldIgnore) {
            filterChain.doFilter(request, response);
        } else {
            logRequestDetails(request);
            filterChain.doFilter(request, response);
        }
    }

    private void logRequestDetails(HttpServletRequest request) {

        String realIp = request.getHeader("X-Forwarded-For");
        if (realIp == null) {
            realIp = request.getRemoteAddr();
        }

        StringBuilder logMessage = new StringBuilder("Method -> " + request.getMethod() +
                " : " + "URL -> " + "[" + request.getRequestURI() + "]" +
                " : " + " IP -> " + realIp);

        Map<String, String[]> parameterMap = request.getParameterMap();
        if (!parameterMap.isEmpty()) {
            logMessage.append(" : Parameters -> ");
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String paramName = entry.getKey();
                String paramValue = Arrays.toString(entry.getValue());
                logMessage.append(paramName).append("=").append(paramValue).append(", ");
            }
            logMessage.delete(logMessage.length() - 2, logMessage.length());
        }

        log.info(logMessage.toString());
    }
}