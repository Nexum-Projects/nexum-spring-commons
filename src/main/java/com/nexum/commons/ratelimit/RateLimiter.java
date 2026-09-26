package com.nexum.commons.ratelimit;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Ventana fija en memoria por clave.
 * ponytail: se pierde al reiniciar y no se comparte entre instancias; con más de una instancia
 * pasar a Redis o Bucket4j.
 */
public final class RateLimiter {
    private static final int PURGE_THRESHOLD = 10_000;

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final int limit;
    private final long windowMs;

    public RateLimiter(int limit, long windowMs) {
        this.limit = limit;
        this.windowMs = windowMs;
    }

    public boolean allow(String key, long nowMs) {
        if (windows.size() > PURGE_THRESHOLD) {
            windows.values().removeIf(window -> nowMs - window.startMs >= windowMs);
        }
        Window window = windows.compute(key, (ignored, current) -> {
            if (current == null || nowMs - current.startMs >= windowMs) {
                return new Window(nowMs, 1);
            }
            current.count++;
            return current;
        });
        return window.count <= limit;
    }

    private static final class Window {
        private final long startMs;
        private int count;

        private Window(long startMs, int count) {
            this.startMs = startMs;
            this.count = count;
        }
    }
}
