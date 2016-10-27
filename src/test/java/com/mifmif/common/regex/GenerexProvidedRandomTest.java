package com.mifmif.common.regex;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class GenerexProvidedRandomTest {
    private final String pattern;
    private final int minLength;
    private final int maxLength;
    private final Random random;
    private final String expectedResult;

    public GenerexProvidedRandomTest(
        String description,
        String patternValue,
        int minLengthValue,
        int maxLengthValue,
        String expectedResult) {

        this.pattern = patternValue;
        this.minLength = minLengthValue;
        this.maxLength = maxLengthValue;
        this.random = new Random(- 1L);
        this.expectedResult = expectedResult;
    }

    @Parameters(name = "Test random: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "Sample multicharacter expression", "[A-Z]{5,9}", 4, 8, "VFWMLRSM" },
            { "Sample expression", "[0-3]([a-c]|[e-g]{1,2})", 1, 3, "1c" },
            { "E-mail format", "([a-z0-9]+)[@]([a-z0-9]+)[.]([a-z0-9]+)", 8, 24, "59w8@i.0mz0" },
            { "Any number", "(\\d+)", 4, 8, "5488" },
            { "Any non-number", "(\\D+)", 4, 8, "\u0011朂\u0006\u0000" },
            { "Any word", "(\\w+)", 4, 8, "V_M8" },
            { "Any non-word", "(\\W+)", 4, 8, "`窱\\?" },
            { "Any text", "(.*)", 4, 8, "̤駍" }
        });
    }

    @Test
    public void testSimpleRandom() {
        Generex generex = new Generex(pattern, random);

        String result = generex.random();

        Assert.assertTrue(result.startsWith(expectedResult));
        Assert.assertTrue(
            String.format("The string '%s' doesn't match the '%s' pattern", result, pattern),
            result.matches(pattern));
    }

    @Test
    public void testRandomWithMinLength() {
        Generex generex = new Generex(pattern, random);

        String result = generex.random(minLength);

        Assert.assertTrue(result.startsWith(expectedResult));
        Assert.assertTrue(
            String.format("The string '%s' doesn't match the '%s' pattern", result, pattern),
            result.matches(pattern));
        Assert.assertTrue(
            String.format("The string '%s' size doesn't fit the minimal size of %d", result, minLength),
            result.length() >= minLength);
    }

    @Test
    public void testRandomWithMaxLength() {
        Generex generex = new Generex(pattern, random);

        String result = generex.random(minLength, maxLength);

        Assert.assertTrue(result.startsWith(expectedResult));
        Assert.assertTrue(
            String.format("The string '%s' doesn't match the '%s' pattern", result, pattern),
            result.matches(pattern));
        Assert.assertTrue(
            String.format("The string '%s' size doesn't fit the minimal size of %d", result, minLength),
            result.length() >= minLength);
        Assert.assertTrue(
            String.format("The string '%s' size doesn't fit the maximal size of %d", result, maxLength),
            result.length() <= maxLength);
    }
}
