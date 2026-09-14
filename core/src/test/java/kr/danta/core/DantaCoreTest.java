package kr.danta.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DantaCoreTest {
    @Test
    void exposesProjectVersion() {
        assertEquals("0.1.0-SNAPSHOT", DantaCore.VERSION);
    }
}
