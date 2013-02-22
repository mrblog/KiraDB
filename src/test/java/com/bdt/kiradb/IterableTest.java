package com.bdt.kiradb;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class IterableTest {

    private final Integer integer;

    public IterableTest(Integer integer) {
        this.integer = integer;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> theData() {
        Object[][] data = new Object[][]{
                {1}, {2}, {3}, {4}
        };

        return Arrays.asList(data);
    }

    @Test
    public void testMe() {
//        System.out.printf("i: %d\n", integer);
    }


}
