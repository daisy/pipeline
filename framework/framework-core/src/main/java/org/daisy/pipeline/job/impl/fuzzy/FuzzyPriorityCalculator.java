package org.daisy.pipeline.job.impl.fuzzy;

import org.daisy.common.fuzzy.InferenceEngine;
import org.daisy.common.priority.PrioritizableRunnable;
import org.daisy.common.priority.PriorityCalculator;

import com.google.common.base.Supplier;
import com.google.common.primitives.Doubles;



/**
 * Computes its final priority from a set of priorities and 
 * the time spent in the execution queue. This computation is calculated
 * using a fuzzy {@link InferenceEngine}.
 *
 */
public class FuzzyPriorityCalculator<T> implements PriorityCalculator<T>{
        /**
         * Inference engine used to compute the final priority
         */
        private final InferenceEngine inferenceEngine;
        /**
         * score given by the engine for the current runnable status 
         */
        private double score;

        private Supplier<double[]> crispsSupplier;

        private Supplier<T> prioritySourceSupplier;

        /**
         * @param inferenceEngine
         */
        public FuzzyPriorityCalculator(InferenceEngine inferenceEngine, Supplier<double[]> crispsSupplier,Supplier<T> prioritySourceSupplier) {
                this.inferenceEngine = inferenceEngine;
                this.crispsSupplier=crispsSupplier;
                this.prioritySourceSupplier=prioritySourceSupplier;
        }

        /**
         * Returns the score of this FuzzyRunnable computed with InfereneceEngine
         */
        @Override
        public double getPriority(PrioritizableRunnable<T> runnable) {
                //Lazy score calcualtion and caching
                if(runnable.isDirty()){
                        double[] crispValues=Doubles.concat(new double[]{runnable.getRelativeWaitingTime()},this.crispsSupplier.get());
                        this.score=-1*this.inferenceEngine.getScore(crispValues);
                        runnable.markDirty(false);
                }
                return this.score;
        }

        /**
         * @return the crispsSupplier
         */
        public Supplier<double[]> getCrispsSupplier() {
                return crispsSupplier;
        }


        @Override
        public T prioritySource() {
                return this.prioritySourceSupplier.get();
        }
}
