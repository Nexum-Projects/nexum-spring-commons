package com.nexum.commons.ratelimit;

import com.nexum.commons.error.CommonErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Limita los POST bajo {@link RateLimitProperties#pathPrefix()} por IP y ruta, y el login además por email.
 * La IP es getRemoteAddr(): detrás de un proxy activar server.forward-headers-strategy=native
 * para que Tomcat la reescriba solo si el proxy es de confianza (no se parsea X-Forwarded-For).
 */
public class RateLimitFilter extends OncePerRequestFilter {
    private static final int MAX_BODY_BYTES = 4096;
    // ponytail: el email se extrae con una regex sobre el cuerpo JSON del login, sin parsearlo; suficiente para
    // un cuerpo plano con el campo email. Parsear con Jackson si el login cambia de formato.
    private static final Pattern EMAIL = Pattern.compile("\"email\"\\s*:\\s*\"([^\"]{1,255})\"");

    private final RateLimitProperties properties;
    private final RateLimiter ipLimiter;
    private final RateLimiter emailLimiter;
    private final long retryAfterSeconds;

    public RateLimitFilter(RateLimitProperties properties) {
        long windowMs = properties.windowSeconds() * 1000L;
        this.properties = properties;
        this.ipLimiter = new RateLimiter(properties.authLimit(), windowMs);
        this.emailLimiter = new RateLimiter(properties.loginEmailLimit(), windowMs);
        this.retryAfterSeconds = properties.windowSeconds();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = path(request);
        return !HttpMethod.POST.matches(request.getMethod())
                || !path.startsWith(properties.pathPrefix())
                || properties.excludedPaths().contains(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long now = System.currentTimeMillis();
        String path = path(request);
        HttpServletRequest forwarded = request;
        boolean allowed = ipLimiter.allow(request.getRemoteAddr() + " " + path, now);

        if (allowed && properties.loginPath().equals(path)) {
            int length = request.getContentLength();
            if (length > 0 && length <= MAX_BODY_BYTES) {
                CachedBodyRequest cached = new CachedBodyRequest(request);
                forwarded = cached;
                Matcher matcher = EMAIL.matcher(new String(cached.body, StandardCharsets.UTF_8));
                if (matcher.find()) {
                    allowed = emailLimiter.allow(matcher.group(1).trim().toLowerCase(Locale.ROOT), now);
                }
            }
        }

        if (!allowed) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"code\":\"" + CommonErrorCode.TOO_MANY_REQUESTS.name()
                    + "\",\"message\":\"Too many requests. Try again later.\","
                    + "\"statusCode\":429,\"type\":\"TOO_MANY_REQUESTS\",\"details\":null}");
            return;
        }
        filterChain.doFilter(forwarded, response);
    }

    /** Ruta sin el context path; no depende del mapeo del DispatcherServlet ({@code spring.mvc.servlet.path}). */
    private static String path(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }

    private static final class CachedBodyRequest extends HttpServletRequestWrapper {
        private final byte[] body;

        private CachedBodyRequest(HttpServletRequest request) throws IOException {
            super(request);
            this.body = request.getInputStream().readAllBytes();
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream source = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override
                public int read() {
                    return source.read();
                }

                @Override
                public boolean isFinished() {
                    return source.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}
