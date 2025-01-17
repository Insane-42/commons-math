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
package org.apache.commons.math4.legacy.distribution;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math4.legacy.exception.MathArithmeticException;
import org.apache.commons.math4.legacy.exception.NotANumberException;
import org.apache.commons.math4.legacy.exception.NotFiniteNumberException;
import org.apache.commons.math4.legacy.exception.NotPositiveException;
import org.apache.commons.math4.legacy.exception.NotStrictlyPositiveException;
import org.apache.commons.math4.legacy.exception.NullArgumentException;
import org.apache.commons.math4.legacy.exception.util.LocalizedFormats;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.DiscreteProbabilityCollectionSampler;
import org.apache.commons.math4.legacy.util.MathArrays;
import org.apache.commons.math4.legacy.util.Pair;

/**
 * <p>A generic implementation of a
 * <a href="http://en.wikipedia.org/wiki/Probability_distribution#Discrete_probability_distribution">
 * discrete probability distribution (Wikipedia)</a> over a finite sample space,
 * based on an enumerated list of &lt;value, probability&gt; pairs.  Input probabilities must all be non-negative,
 * but zero values are allowed and their sum does not have to equal one. Constructors will normalize input
 * probabilities to make them sum to one.</p>
 *
 * <p>The list of &lt;value, probability&gt; pairs does not, strictly speaking, have to be a function and it can
 * contain null values.  The pmf created by the constructor will combine probabilities of equal values and
 * will treat null values as equal.  For example, if the list of pairs &lt;"dog", 0.2&gt;, &lt;null, 0.1&gt;,
 * &lt;"pig", 0.2&gt;, &lt;"dog", 0.1&gt;, &lt;null, 0.4&gt; is provided to the constructor, the resulting
 * pmf will assign mass of 0.5 to null, 0.3 to "dog" and 0.2 to pig.</p>
 *
 * @param <T> type of the elements in the sample space.
 * @since 3.2
 */
public class EnumeratedDistribution<T> implements Serializable {
    /** Serializable UID. */
    private static final long serialVersionUID = 20160319L;
    /**
     * List of random variable values.
     */
    private final List<T> singletons;
    /**
     * Probabilities of respective random variable values. For i = 0, ..., singletons.size() - 1,
     * probability[i] is the probability that a random variable following this distribution takes
     * the value singletons[i].
     */
    private final double[] probabilities;
    /**
     * Cumulative probabilities, cached to speed up sampling.
     */
    private final double[] cumulativeProbabilities;

    /**
     * Create an enumerated distribution using the given random number generator
     * and probability mass function enumeration.
     *
     * @param pmf probability mass function enumerated as a list of
     * {@code <T, probability>} pairs.
     * @throws NotPositiveException if any of the probabilities are negative.
     * @throws NotFiniteNumberException if any of the probabilities are infinite.
     * @throws NotANumberException if any of the probabilities are NaN.
     * @throws MathArithmeticException all of the probabilities are 0.
     */
    public EnumeratedDistribution(final List<Pair<T, Double>> pmf)
        throws NotPositiveException,
               MathArithmeticException,
               NotFiniteNumberException,
               NotANumberException {
        singletons = new ArrayList<>(pmf.size());
        final double[] probs = new double[pmf.size()];
        int count = 0;
        for (Pair<T, Double> sample : pmf) {
            singletons.add(sample.getKey());
            final double p = sample.getValue();
            if (p < 0) {
                throw new NotPositiveException(sample.getValue());
            }
            if (Double.isInfinite(p)) {
                throw new NotFiniteNumberException(p);
            }
            if (Double.isNaN(p)) {
                throw new NotANumberException();
            }
            probs[count++] = p;
        }

        probabilities = MathArrays.normalizeArray(probs, 1.0);

        cumulativeProbabilities = new double[probabilities.length];
        double sum = 0;
        for (int i = 0; i < probabilities.length; i++) {
            sum += probabilities[i];
            cumulativeProbabilities[i] = sum;
        }
    }

    /**
     * <p>For a random variable {@code X} whose values are distributed according to
     * this distribution, this method returns {@code P(X = x)}. In other words,
     * this method represents the probability mass function (PMF) for the
     * distribution.</p>
     *
     * <p>Note that if {@code x1} and {@code x2} satisfy {@code x1.equals(x2)},
     * or both are null, then {@code probability(x1) = probability(x2)}.</p>
     *
     * @param x the point at which the PMF is evaluated
     * @return the value of the probability mass function at {@code x}
     */
    double probability(final T x) {
        double probability = 0;

        for (int i = 0; i < probabilities.length; i++) {
            if ((x == null && singletons.get(i) == null) ||
                (x != null && x.equals(singletons.get(i)))) {
                probability += probabilities[i];
            }
        }

        return probability;
    }

    /**
     * <p>Return the probability mass function as a list of &lt;value, probability&gt; pairs.</p>
     *
     * <p>Note that if duplicate and / or null values were provided to the constructor
     * when creating this EnumeratedDistribution, the returned list will contain these
     * values.  If duplicates values exist, what is returned will not represent
     * a pmf (i.e., it is up to the caller to consolidate duplicate mass points).</p>
     *
     * @return the probability mass function.
     */
    public List<Pair<T, Double>> getPmf() {
        final List<Pair<T, Double>> samples = new ArrayList<>(probabilities.length);

        for (int i = 0; i < probabilities.length; i++) {
            samples.add(new Pair<>(singletons.get(i), probabilities[i]));
        }

        return samples;
    }

    /**
     * Creates a {@link Sampler}.
     *
     * @param rng Random number generator.
     * @return a new sampler instance.
     */
    public Sampler createSampler(final UniformRandomProvider rng) {
        return new Sampler(rng);
    }

    /**
     * Sampler functionality.
     *
     * <ul>
     *  <li>
     *   The cumulative probability distribution is created (and sampled from)
     *   using the input order of the {@link EnumeratedDistribution#EnumeratedDistribution(List)
     *   constructor arguments}: A different input order will create a different
     *   sequence of samples.
     *   The samples will only be reproducible with the same RNG starting from
     *   the same RNG state and the same input order to constructor.
     *  </li>
     *  <li>
     *   The minimum supported probability is 2<sup>-53</sup>.
     *  </li>
     * </ul>
     */
    public class Sampler {
        /** Underlying sampler. */
        private final DiscreteProbabilityCollectionSampler<T> sampler;

        /**
         * @param rng Random number generator.
         */
        Sampler(UniformRandomProvider rng) {
            sampler = new DiscreteProbabilityCollectionSampler<T>(rng, singletons, probabilities);
        }

        /**
         * Generates a random value sampled from this distribution.
         *
         * @return a random value.
         */
        public T sample() {
            return sampler.sample();
        }

        /**
         * Generates a random sample from the distribution.
         *
         * @param sampleSize the number of random values to generate.
         * @return an array representing the random sample.
         * @throws NotStrictlyPositiveException if {@code sampleSize} is not
         * positive.
         */
        public Object[] sample(int sampleSize) throws NotStrictlyPositiveException {
            if (sampleSize <= 0) {
                throw new NotStrictlyPositiveException(LocalizedFormats.NUMBER_OF_SAMPLES,
                                                       sampleSize);
            }

            final Object[] out = new Object[sampleSize];

            for (int i = 0; i < sampleSize; i++) {
                out[i] = sample();
            }

            return out;
        }

        /**
         * Generates a random sample from the distribution.
         * <p>
         * If the requested samples fit in the specified array, it is returned
         * therein. Otherwise, a new array is allocated with the runtime type of
         * the specified array and the size of this collection.
         *
         * @param sampleSize the number of random values to generate.
         * @param array the array to populate.
         * @return an array representing the random sample.
         * @throws NotStrictlyPositiveException if {@code sampleSize} is not positive.
         * @throws NullArgumentException if {@code array} is null
         */
        public T[] sample(int sampleSize, final T[] array) throws NotStrictlyPositiveException {
            if (sampleSize <= 0) {
                throw new NotStrictlyPositiveException(LocalizedFormats.NUMBER_OF_SAMPLES, sampleSize);
            }

            if (array == null) {
                throw new NullArgumentException(LocalizedFormats.INPUT_ARRAY);
            }

            T[] out;
            if (array.length < sampleSize) {
                @SuppressWarnings("unchecked") // safe as both are of type T
                final T[] unchecked = (T[]) Array.newInstance(array.getClass().getComponentType(), sampleSize);
                out = unchecked;
            } else {
                out = array;
            }

            for (int i = 0; i < sampleSize; i++) {
                out[i] = sample();
            }

            return out;
        }
    }
}
