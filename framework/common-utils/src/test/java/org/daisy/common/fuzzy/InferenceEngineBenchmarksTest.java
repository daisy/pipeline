package org.daisy.common.fuzzy;

import java.util.Collection;
import java.util.Random;

import org.daisy.common.fuzzy.FuzzySet;
import org.daisy.common.fuzzy.FuzzyVariable;
import org.daisy.common.fuzzy.InferenceEngine;
import org.daisy.common.fuzzy.MembershipFunctions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.google.common.primitives.Doubles;
/**
 * To see how it would scale according with the name of jobs submmited.
 *
 */
@BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 0)
public class InferenceEngineBenchmarksTest   {
        @Rule
        public TestRule benchmarkRun = new BenchmarkRule();

        final static Random random = new Random();
        static FuzzyVariable f1;
        static FuzzyVariable f2;
        static FuzzyVariable f3;
        static InferenceEngine eng;
        final static int MAX=100000;
        static double[][] nums =new double[MAX][3]; 
        static{
                f1= new FuzzyVariable();
                f1.add(new FuzzySet("low",10,MembershipFunctions.newTrapezoidFunction(0.0,0.0,0.3,0.5)));
                f1.add(new FuzzySet("med",20,MembershipFunctions.newTrapezoidFunction(0.2,0.4,0.6,0.8)));
                f1.add(new FuzzySet("high",30,MembershipFunctions.newTrapezoidFunction(0.6,0.65,0.7,1)));

                f2= new FuzzyVariable();
                f2.add(new FuzzySet("low",10,MembershipFunctions.newTrapezoidFunction(0.0,0.0,0.3,0.5)));
                f2.add(new FuzzySet("med",20,MembershipFunctions.newTrapezoidFunction(0.2,0.4,0.6,0.8)));
                f2.add(new FuzzySet("high",30,MembershipFunctions.newTrapezoidFunction(0.6,0.65,0.7,1)));

                f3= new FuzzyVariable();
                f3.add(new FuzzySet("low",10,MembershipFunctions.newTrapezoidFunction(0.0,0.0,0.3,0.5)));
                f3.add(new FuzzySet("med",20,MembershipFunctions.newTrapezoidFunction(0.2,0.4,0.6,0.8)));
                f3.add(new FuzzySet("high",30,MembershipFunctions.newTrapezoidFunction(0.6,0.65,0.7,1)));

                eng= new InferenceEngine();
                eng.add(f1).add(f2).add(f3);
                for (int i=0; i<MAX;i++){
                        nums[i]=new double[]{random.nextDouble(),random.nextDouble(),random.nextDouble()};
                }
        }

        @Before
        public void setUp(){
        }
       
          
        @Test
        public void simple20(){
                for (int i=0;i<20;i++)
                        eng.getScore(nums[random.nextInt(MAX)]);
        }

        @Test
        public void simple50(){
                for (int i=0;i<50;i++)
                        eng.getScore(nums[random.nextInt(MAX)]);
        }

        @Test
        public void simple100(){
                for (int i=0;i<100;i++)
                        eng.getScore(nums[random.nextInt(MAX)]);
        }

        @Test
        public void simple500(){
                for (int i=0;i<500;i++)
                        eng.getScore(nums[random.nextInt(MAX)]);
        }

        @Test
        public void simple1000(){
                for (int i=0;i<1000;i++)
                        eng.getScore(nums[random.nextInt(MAX)]);
        }

        @Test
        public void simple10000(){
                for (int i=0;i<10000;i++)
                        eng.getScore(nums[random.nextInt(MAX)]);
        }
        @Test
        public void simple100000(){
                for (int i=0;i<100000;i++)
                        eng.getScore(nums[random.nextInt(MAX)]);
        }
}
