package com.nexum.commons.security;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenUtilsTest {

    @Test
    void urlSafeTokenHasTheExpectedLengthAndAlphabet() {
        String token = TokenUtils.generateUrlSafeToken(32);
        assertEquals(43, token.length()); // 32 bytes en Base64 sin relleno
        assertTrue(token.matches("[A-Za-z0-9_-]+"));
    }

    @Test
    void tokensDoNotRepeat() {
        Set<String> tokens = new HashSet<>();
        for (int i = 0; i < 1_000; i++) {
            tokens.add(TokenUtils.generateUrlSafeToken(16));
        }
        assertEquals(1_000, tokens.size());
    }

    @Test
    void sha256HexMatchesTheKnownVector() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", TokenUtils.sha256Hex("abc"));
    }
}
