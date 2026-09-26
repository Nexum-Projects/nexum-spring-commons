package com.nexum.commons.ratelimit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    @Test
    void blocksAfterLimitWithinWindow() {
        RateLimiter limiter = new RateLimiter(3, 60_000);
        for (int i = 0; i < 3; i++) {
            assertTrue(limiter.allow("k", 1_000));
        }
        assertFalse(limiter.allow("k", 1_001));
    }

    @Test
    void newWindowResetsTheCount() {
        RateLimiter limiter = new RateLimiter(1, 60_000);
        assertTrue(limiter.allow("k", 0));
        assertFalse(limiter.allow("k", 1));
        assertTrue(limiter.allow("k", 60_000));
    }

    @Test
    void keysAreIndependent() {
        RateLimiter limiter = new RateLimiter(1, 60_000);
        assertTrue(limiter.allow("a", 0));
        assertTrue(limiter.allow("b", 0));
    }
}
