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
package org.apache.commons.math4.legacy.ml.distance;

import org.apache.commons.math4.legacy.exception.DimensionMismatchException;
import org.apache.commons.math4.legacy.util.MathArrays;

/**
 * Calculates the L<sub>1</sub> (sum of abs) distance between two points.
 *
 * @since 3.2
 */
public class ManhattanDistance implements DistanceMeasure {

    /** Serializable version identifier. */
    private static final long serialVersionUID = -9108154600539125566L;

    /** {@inheritDoc} */
    @Override
    public double compute(double[] a, double[] b)
    throws DimensionMismatchException {
        return MathArrays.distance1(a, b);
    }

}
