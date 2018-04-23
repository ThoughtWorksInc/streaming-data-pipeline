package com.free2wheelers.services;

import org.junit.Test;

import static org.junit.Assert.assertNotEquals;

public class MetadataGeneratorTest {

    @Test
    public void shouldGenerateDifferentUUID() {
        MetadataGenerator generator = new MetadataGenerator();
        String s1 = generator.generateUniqueKey();
        String s2 = generator.generateUniqueKey();
        assertNotEquals(s1, s2);
    }
}
