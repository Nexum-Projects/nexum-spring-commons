package com.nexum.commons.username;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsernamePolicyTest {
    private final UsernamePolicy policy = new UsernamePolicy(UsernameProperties.defaults());

    @ParameterizedTest
    @ValueSource(strings = {"danieltistoj1", "maria_hernandez23", "abc", "a2345678901234567890123456789012"})
    void acceptsSlugUsernames(String username) {
        assertTrue(policy.isValid(username));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Maria Lopez", "maría", "maria-hernandez", "maria.lopez", "ab", "1maria", "_maria",
            "a23456789012345678901234567890123"})
    void rejectsEverythingElse(String username) {
        assertFalse(policy.isValid(username));
    }

    @Test
    void normalizesToTrimmedLowercase() {
        assertEquals("maria_lopez", policy.normalize("  Maria_Lopez "));
        assertNull(policy.normalize(null));
    }

    @Test
    void disabledPolicyAcceptsAnythingAndOnlyTrims() {
        UsernamePolicy disabled = new UsernamePolicy(new UsernameProperties(false, "^[a-z]+$", "msg"));
        assertTrue(disabled.isValid("María López!"));
        assertEquals("María López", disabled.normalize("  María López "));
    }

    @Test
    void patternIsConfigurable() {
        UsernamePolicy dashes = new UsernamePolicy(new UsernameProperties(true, "^[a-z][a-z0-9-]{2,31}$", "msg"));
        assertTrue(dashes.isValid("maria-hernandez"));
        assertFalse(dashes.isValid("maria_hernandez"));
    }
}
