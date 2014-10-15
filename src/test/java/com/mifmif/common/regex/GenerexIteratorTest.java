package com.mifmif.common.regex;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.mifmif.common.regex.util.Iterator;

@RunWith(Parameterized.class)
public class GenerexIteratorTest {

    private String pattern;
    private Generex generex;
    
    @Parameters(name = "Test random: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"Sample multicharacter expression","[A-B]{5,9}"},
                {"Sample expression","[0-3]([a-c]|[e-g]{1,2})"},
                {"Number format","\\d{3,4}"},
                //{"Any non-number","\\D{3,4}"},
                {"Any word","\\w{1,2}"},
                {"Empty string",""},
                //{"Any non-word","\\W{1,2}"}
        });
    }

    public GenerexIteratorTest(String description,
            String patternValue) {
        this.pattern = patternValue;
    }

    @Before
    public void setUp() throws Exception {
        generex = new Generex(pattern);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testIterateThroughAllGeneratedStrings() {
        Iterator iterator = generex.iterator();
        while (iterator.hasNext()) {
            String result = iterator.next();
            Assert.assertTrue(
                    String.format("The string '%s' doesn't match the '%s' pattern", result, pattern),
                    result.matches(pattern));

        }
    }

    /*
    @Test
    public void testIterateShouldReturnTheSameAsGetMatchedStrings() {
        int count = 1;
        Iterator iterator = generex.iterator();
        while (iterator.hasNext()) {
            String matchedResult = generex.getMatchedString(count);
            String result = iterator.next();
            Assert.assertEquals(String.format("Iteration %d mismatch", count), result, matchedResult);
            count++;
        }
    }*/
}
