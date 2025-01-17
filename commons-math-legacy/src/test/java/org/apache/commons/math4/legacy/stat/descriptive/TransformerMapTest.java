/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.math4.legacy.stat.descriptive;

import org.apache.commons.math4.legacy.TestUtils;
import org.junit.Assert;
import org.junit.Test;


/**
 */
public class TransformerMapTest {
    /**
     *
     */
    @Test
    public void testPutTransformer(){
        UnivariateStatistic.NumberTransformer expected = new UnivariateStatistic.DefaultTransformer();

        TransformerMap map = new TransformerMap();
        map.putTransformer(TransformerMapTest.class, expected);
        Assert.assertEquals(expected, map.getTransformer(TransformerMapTest.class));
    }

    /**
     *
     */
    @Test
    public void testContainsClass(){
        UnivariateStatistic.NumberTransformer expected = new UnivariateStatistic.DefaultTransformer();
        TransformerMap map = new TransformerMap();
        map.putTransformer(TransformerMapTest.class, expected);
        Assert.assertTrue(map.containsClass(TransformerMapTest.class));
    }

    /**
     *
     */
    @Test
    public void testContainsTransformer(){
        UnivariateStatistic.NumberTransformer expected = new UnivariateStatistic.DefaultTransformer();
        TransformerMap map = new TransformerMap();
        map.putTransformer(TransformerMapTest.class, expected);
        Assert.assertTrue(map.containsTransformer(expected));
    }

    /**
     *
     */
    @Test
    public void testRemoveTransformer(){
        UnivariateStatistic.NumberTransformer expected = new UnivariateStatistic.DefaultTransformer();

        TransformerMap map = new TransformerMap();
        map.putTransformer(TransformerMapTest.class, expected);
        Assert.assertTrue(map.containsClass(TransformerMapTest.class));
        Assert.assertTrue(map.containsTransformer(expected));
        map.removeTransformer(TransformerMapTest.class);
        Assert.assertFalse(map.containsClass(TransformerMapTest.class));
        Assert.assertFalse(map.containsTransformer(expected));
    }

    /**
     *
     */
    @Test
    public void testClear(){
        UnivariateStatistic.NumberTransformer expected = new UnivariateStatistic.DefaultTransformer();

        TransformerMap map = new TransformerMap();
        map.putTransformer(TransformerMapTest.class, expected);
        Assert.assertTrue(map.containsClass(TransformerMapTest.class));
        map.clear();
        Assert.assertFalse(map.containsClass(TransformerMapTest.class));
    }

    /**
     *
     */
    @Test
    public void testClasses(){
        UnivariateStatistic.NumberTransformer expected = new UnivariateStatistic.DefaultTransformer();
        TransformerMap map = new TransformerMap();
        map.putTransformer(TransformerMapTest.class, expected);
        Assert.assertTrue(map.classes().contains(TransformerMapTest.class));
    }

    /**
     *
     */
    @Test
    public void testTransformers(){
        UnivariateStatistic.NumberTransformer expected = new UnivariateStatistic.DefaultTransformer();
        TransformerMap map = new TransformerMap();
        map.putTransformer(TransformerMapTest.class, expected);
        Assert.assertTrue(map.transformers().contains(expected));
    }

    @Test
    public void testSerial(){
        UnivariateStatistic.NumberTransformer expected = new UnivariateStatistic.DefaultTransformer();
        TransformerMap map = new TransformerMap();
        map.putTransformer(TransformerMapTest.class, expected);
        Assert.assertEquals(map, TestUtils.serializeAndRecover(map));
    }

}
