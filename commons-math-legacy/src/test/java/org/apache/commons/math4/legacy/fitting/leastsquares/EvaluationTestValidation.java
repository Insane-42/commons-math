/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.apache.commons.math4.legacy.fitting.leastsquares;

import org.apache.commons.math4.legacy.linear.ArrayRealVector;
import org.apache.commons.math4.legacy.linear.DiagonalMatrix;
import org.apache.commons.math4.legacy.linear.RealVector;
import org.apache.commons.math4.legacy.stat.descriptive.StatisticalSummary;
import org.apache.commons.math4.legacy.stat.descriptive.SummaryStatistics;
import org.apache.commons.math4.legacy.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * This class demonstrates the main functionality of the
 * {@link LeastSquaresProblem.Evaluation}, common to the
 * optimizer implementations in package
 * {@link org.apache.commons.math4.legacy.fitting.leastsquares}.
 * <br>
 * Not enabled by default, as the class name does not end with "Test".
 * <br>
 * Invoke by running
 * <pre><code>
 *  mvn test -Dtest=EvaluationTestValidation
 * </code></pre>
 * or by running
 * <pre><code>
 *  mvn test -Dtest=EvaluationTestValidation -DargLine="-DmcRuns=1234 -server"
 * </code></pre>
 */
public class EvaluationTestValidation {
    /** Number of runs. */
    private static final int MONTE_CARLO_RUNS = Integer.parseInt(System.getProperty("mcRuns",
                                                                                    "100"));

    /**
     * Using a Monte-Carlo procedure, this test checks the error estimations
     * as provided by the square-root of the diagonal elements of the
     * covariance matrix.
     * <br>
     * The test generates sets of observations, each sampled from
     * a Gaussian distribution.
     * <br>
     * The optimization problem solved is defined in class
     * {@link StraightLineProblem}.
     * <br>
     * The output (on stdout) will be a table summarizing the distribution
     * of parameters generated by the Monte-Carlo process and by the direct
     * estimation provided by the diagonal elements of the covariance matrix.
     */
    @Test
    public void testParametersErrorMonteCarloObservations() {
        // Error on the observations.
        final double yError = 15;

        // True values of the parameters.
        final double slope = 123.456;
        final double offset = -98.765;

        // Samples generator.
        final RandomStraightLinePointGenerator lineGenerator
            = new RandomStraightLinePointGenerator(slope, offset,
                                                   yError,
                                                   -1e3, 1e4,
                                                   138577L);

        // Number of observations.
        final int numObs = 100; // XXX Should be a command-line option.
        // number of parameters.
        final int numParams = 2;

        // Parameters found for each of Monte-Carlo run.
        final SummaryStatistics[] paramsFoundByDirectSolution = new SummaryStatistics[numParams];
        // Sigma estimations (square-root of the diagonal elements of the
        // covariance matrix), for each Monte-Carlo run.
        final SummaryStatistics[] sigmaEstimate = new SummaryStatistics[numParams];

        // Initialize statistics accumulators.
        for (int i = 0; i < numParams; i++) {
            paramsFoundByDirectSolution[i] = new SummaryStatistics();
            sigmaEstimate[i] = new SummaryStatistics();
        }

        final RealVector init = new ArrayRealVector(new double[]{ slope, offset }, false);

        // Monte-Carlo (generates many sets of observations).
        final int mcRepeat = MONTE_CARLO_RUNS;
        int mcCount = 0;
        while (mcCount < mcRepeat) {
            // Observations.
            final Point2D.Double[] obs = lineGenerator.generate(numObs);

            final StraightLineProblem problem = new StraightLineProblem(yError);
            for (int i = 0; i < numObs; i++) {
                final Point2D.Double p = obs[i];
                problem.addPoint(p.x, p.y);
            }

            // Direct solution (using simple regression).
            final double[] regress = problem.solve();

            // Estimation of the standard deviation (diagonal elements of the
            // covariance matrix).
            final LeastSquaresProblem lsp = builder(problem).build();

            final RealVector sigma = lsp.evaluate(init).getSigma(1e-14);

            // Accumulate statistics.
            for (int i = 0; i < numParams; i++) {
                paramsFoundByDirectSolution[i].addValue(regress[i]);
                sigmaEstimate[i].addValue(sigma.getEntry(i));
            }

            // Next Monte-Carlo.
            ++mcCount;
        }

        // Print statistics.
        final String line = "--------------------------------------------------------------";
        System.out.println("                 True value       Mean        Std deviation");
        for (int i = 0; i < numParams; i++) {
            System.out.println(line);
            System.out.println("Parameter #" + i);

            StatisticalSummary s = paramsFoundByDirectSolution[i].getSummary();
            System.out.printf("              %+.6e   %+.6e   %+.6e\n",
                              init.getEntry(i),
                              s.getMean(),
                              s.getStandardDeviation());

            s = sigmaEstimate[i].getSummary();
            System.out.printf("sigma: %+.6e (%+.6e)\n",
                              s.getMean(),
                              s.getStandardDeviation());
        }
        System.out.println(line);

        // Check the error estimation.
        for (int i = 0; i < numParams; i++) {
            Assert.assertEquals(paramsFoundByDirectSolution[i].getSummary().getStandardDeviation(),
                                sigmaEstimate[i].getSummary().getMean(),
                                8e-2);
        }
    }

    /**
     * In this test, the set of observations is fixed.
     * Using a Monte-Carlo procedure, it generates sets of parameters,
     * and determine the parameter change that will result in the
     * normalized chi-square becoming larger by one than the value from
     * the best fit solution.
     * <br>
     * The optimization problem solved is defined in class
     * {@link StraightLineProblem}.
     * <br>
     * The output (on stdout) will be a list of lines containing:
     * <ul>
     *  <li>slope of the straight line,</li>
     *  <li>intercept of the straight line,</li>
     *  <li>chi-square of the solution defined by the above two values.</li>
     * </ul>
     * The output is separated into two blocks (with a blank line between
     * them); the first block will contain all parameter sets for which
     * {@code chi2 < chi2_b + 1}
     * and the second block, all sets for which
     * {@code chi2 >= chi2_b + 1}
     * where {@code chi2_b} is the lowest chi-square (corresponding to the
     * best solution).
     */
    @Test
    public void testParametersErrorMonteCarloParameters() {
        // Error on the observations.
        final double yError = 15;

        // True values of the parameters.
        final double slope = 123.456;
        final double offset = -98.765;

        // Samples generator.
        final RandomStraightLinePointGenerator lineGenerator
            = new RandomStraightLinePointGenerator(slope, offset,
                                                   yError,
                                                   -1e3, 1e4,
                                                   13839013L);

        // Number of observations.
        final int numObs = 10;
        // number of parameters.

        // Create a single set of observations.
        final Point2D.Double[] obs = lineGenerator.generate(numObs);

        final StraightLineProblem problem = new StraightLineProblem(yError);
        for (int i = 0; i < numObs; i++) {
            final Point2D.Double p = obs[i];
            problem.addPoint(p.x, p.y);
        }

        // Direct solution (using simple regression).
        final RealVector regress = new ArrayRealVector(problem.solve(), false);

        // Dummy optimizer (to compute the chi-square).
        final LeastSquaresProblem lsp = builder(problem).build();

        // Get chi-square of the best parameters set for the given set of
        // observations.
        final double bestChi2N = getChi2N(lsp, regress);
        final RealVector sigma = lsp.evaluate(regress).getSigma(1e-14);

        // Monte-Carlo (generates a grid of parameters).
        final int mcRepeat = MONTE_CARLO_RUNS;
        final int gridSize = (int) FastMath.sqrt(mcRepeat);

        // Parameters found for each of Monte-Carlo run.
        // Index 0 = slope
        // Index 1 = offset
        // Index 2 = normalized chi2
        final List<double[]> paramsAndChi2 = new ArrayList<>(gridSize * gridSize);

        final double slopeRange = 10 * sigma.getEntry(0);
        final double offsetRange = 10 * sigma.getEntry(1);
        final double minSlope = slope - 0.5 * slopeRange;
        final double minOffset = offset - 0.5 * offsetRange;
        final double deltaSlope =  slopeRange/ gridSize;
        final double deltaOffset = offsetRange / gridSize;
        for (int i = 0; i < gridSize; i++) {
            final double s = minSlope + i * deltaSlope;
            for (int j = 0; j < gridSize; j++) {
                final double o = minOffset + j * deltaOffset;
                final double chi2N = getChi2N(lsp,
                        new ArrayRealVector(new double[] {s, o}, false));

                paramsAndChi2.add(new double[] {s, o, chi2N});
            }
        }

        // Output (for use with "gnuplot").

        // Some info.

        // For plotting separately sets of parameters that have a large chi2.
        final double chi2NPlusOne = bestChi2N + 1;
        int numLarger = 0;

        final String lineFmt = "%+.10e %+.10e   %.8e\n";

        // Point with smallest chi-square.
        System.out.printf(lineFmt, regress.getEntry(0), regress.getEntry(1), bestChi2N);
        System.out.println(); // Empty line.

        // Points within the confidence interval.
        for (double[] d : paramsAndChi2) {
            if (d[2] <= chi2NPlusOne) {
                System.out.printf(lineFmt, d[0], d[1], d[2]);
            }
        }
        System.out.println(); // Empty line.

        // Points outside the confidence interval.
        for (double[] d : paramsAndChi2) {
            if (d[2] > chi2NPlusOne) {
                ++numLarger;
                System.out.printf(lineFmt, d[0], d[1], d[2]);
            }
        }
        System.out.println(); // Empty line.

        System.out.println("# sigma=" + sigma.toString());
        System.out.println("# " + numLarger + " sets filtered out");
    }

    LeastSquaresBuilder builder(StraightLineProblem problem){
        return new LeastSquaresBuilder()
                .model(problem.getModelFunction(), problem.getModelFunctionJacobian())
                .target(problem.target())
                .weight(new DiagonalMatrix(problem.weight()))
                //unused start point to avoid NPE
                .start(new double[2]);
    }
    /**
     * @return the normalized chi-square.
     */
    private double getChi2N(LeastSquaresProblem lsp,
                            RealVector params) {
        final double cost = lsp.evaluate(params).getCost();
        return cost * cost / (lsp.getObservationSize() - params.getDimension());
    }
}

