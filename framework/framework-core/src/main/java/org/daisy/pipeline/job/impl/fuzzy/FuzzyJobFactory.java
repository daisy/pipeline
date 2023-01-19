package org.daisy.pipeline.job.impl.fuzzy;

import org.daisy.common.fuzzy.FuzzySet;
import org.daisy.common.fuzzy.FuzzyVariable;
import org.daisy.common.fuzzy.InferenceEngine;
import org.daisy.common.fuzzy.MembershipFunctions;
import org.daisy.common.priority.PrioritizableRunnable;
import org.daisy.pipeline.job.AbstractJob;
import org.daisy.pipeline.job.Job;

import com.google.common.base.Supplier;

public class FuzzyJobFactory {
        


        public static PrioritizableRunnable<Job> newFuzzyRunnable(final AbstractJob job, Runnable runnable){
                return new PrioritizableRunnable<Job>(runnable,
                                new FuzzyPriorityCalculator<Job>(ENGINE,
                                        new Supplier<double[]>() {
                                                @Override
                                                public double[] get() {
                                                        return new double[]{
                                                                job.getContext().getClient().getPriority().asDouble(),
                                                                job.getPriority().asDouble()};
                                                }

                                        }
                                        ,
                                        new Supplier<Job>() {
                                                @Override
                                                public Job get() {
                                                        return job;
                                                }
                                        })

                );


        }





        static final InferenceEngine ENGINE;
        static{
                //The weight values are set so that there will be no starvation
                //a very old task with low prios will exectute a brand new task with the higest prios
                //time is the real fuzzy deal here
                FuzzyVariable time=new FuzzyVariable();
                time.add(new FuzzySet("new task",30,MembershipFunctions.newTrapezoidFunction(0.0,0.0,0.0,0.5)));
                time.add(new FuzzySet("regular task",60,MembershipFunctions.newTrapezoidFunction(0.0,0.5,0.5,1.0)));
                time.add(new FuzzySet("old task",90,MembershipFunctions.newTrapezoidFunction(0.5,1.0,1.0,1.0)));
                //client priority
                FuzzyVariable clientPriority=new FuzzyVariable();
                clientPriority.add(new FuzzySet("low priority",10,MembershipFunctions.newEqualsFunction(0.0)));
                clientPriority.add(new FuzzySet("medium priority",20,MembershipFunctions.newEqualsFunction(0.5)));
                clientPriority.add(new FuzzySet("high priority",30,MembershipFunctions.newEqualsFunction(1.0)));
                //client priority
                FuzzyVariable jobPriority=new FuzzyVariable();
                jobPriority.add(new FuzzySet("low priority",10,MembershipFunctions.newEqualsFunction(0.0)));
                jobPriority.add(new FuzzySet("medium priority",20,MembershipFunctions.newEqualsFunction(0.5)));
                jobPriority.add(new FuzzySet("high priority",30,MembershipFunctions.newEqualsFunction(1.0)));
        
                ENGINE=new InferenceEngine();
                //add the fuzzy variables
                ENGINE.add(time);
                ENGINE.add(clientPriority);
                ENGINE.add(jobPriority);

        }
}
