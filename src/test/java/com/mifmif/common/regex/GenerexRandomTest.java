/**
 * 
 */
package com.mifmif.common.regex;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Myk Kolisnyk
 *
 */
@RunWith(Parameterized.class)
public class GenerexRandomTest {

    private String pattern;
    private int minLength;
    private int maxLength;
    
    public GenerexRandomTest(String description,
            String patternValue,
            int minLengthValue,
            int maxLengthValue) {
        this.pattern = patternValue;
        this.minLength = minLengthValue;
        this.maxLength = maxLengthValue;
    }
    
    @Parameters(name = "Test random: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"Sample multicharacter expression","[A-Z]{5,9}", 4 , 8},
                {"Sample expression","[0-3]([a-c]|[e-g]{1,2})", 1 , 3},
                {"E-mail format","([a-z0-9]+)[@]([a-z0-9]+)[.]([a-z0-9]+)", 8 , 12},
                {"Any number","(\\d+)", 4 , 8},
                {"Any non-number","(\\D+)", 4 , 8},
                {"Any word","(\\w+)", 4 , 8},
                {"Any non-word","(\\W+)", 4 , 8},
                {"Any text","(.*)", 4 , 8}
        });
    }
    
    @Test
    public void testSimpleRandom() {
        Generex generex = new Generex(pattern);
        String result = generex.random();
        Assert.assertTrue(
                String.format("The string '%s' doesn't match the '%s' pattern", result, pattern),
                result.matches(pattern));
    }
    @Test
    public void testRandomWithMinLength() {
        Generex generex = new Generex(pattern);
        String result = generex.random(minLength);
        Assert.assertTrue(
                String.format("The string '%s' doesn't match the '%s' pattern", result, pattern),
                result.matches(pattern));
        Assert.assertTrue(
                String.format("The string '%s' size doesn't fit the minimal size of %d", result, minLength),
                result.length() >= minLength);
    }
    @Test
    public void testRandomWithMaxLength() {
        Generex generex = new Generex(pattern);
        String result = generex.random(minLength, maxLength);
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
