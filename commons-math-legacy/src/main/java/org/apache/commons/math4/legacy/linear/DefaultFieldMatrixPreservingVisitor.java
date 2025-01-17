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

package org.apache.commons.math4.legacy.linear;

import org.apache.commons.math4.legacy.FieldElement;

/**
 * Default implementation of the {@link FieldMatrixPreservingVisitor} interface.
 * <p>
 * This class is a convenience to create custom visitors without defining all
 * methods. This class provides default implementations that do nothing.
 * </p>
 *
 * @param <T> the type of the field elements
 * @since 2.0
 */
public class DefaultFieldMatrixPreservingVisitor<T extends FieldElement<T>>
    implements FieldMatrixPreservingVisitor<T> {
    /** Zero element of the field. */
    private final T zero;

    /** Build a new instance.
     * @param zero additive identity of the field
     */
    public DefaultFieldMatrixPreservingVisitor(final T zero) {
        this.zero = zero;
    }

    /** {@inheritDoc} */
    @Override
    public void start(int rows, int columns,
                      int startRow, int endRow, int startColumn, int endColumn) {
    }

    /** {@inheritDoc} */
    @Override
    public void visit(int row, int column, T value) {}

    /** {@inheritDoc} */
    @Override
    public T end() {
        return zero;
    }
}
