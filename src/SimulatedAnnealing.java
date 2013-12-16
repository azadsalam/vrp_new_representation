import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Artificial Intelligence A Modern Approach (3rd Edition): Figure 4.5, page
 * 126.<br>
 * <br>
 *
 * <pre>
 * function SIMULATED-ANNEALING(problem, schedule) returns a solution state
 *                    
 *   current &lt;- MAKE-NODE(problem.INITIAL-STATE)
 *   for t = 1 to INFINITY do
 *     T &lt;- schedule(t)
 *     if T = 0 then return current
 *     next &lt;- a randomly selected successor of current
 *     /\E &lt;- next.VALUE - current.value
 *     if /\E &gt; 0 then current &lt;- next
 *     else current &lt;- next only with probability e&circ;(/\E/T)
 * </pre>
 *
 * Figure 4.5 The simulated annealing search algorithm, a version of stochastic
 * hill climbing where some downhill moves are allowed. Downhill moves are
 * accepted readily early in the annealing schedule and then less often as time
 * goes on. The schedule input determines the value of the temperature T as a
 * function of time.
 *
 * @author Ravi Mohan
 * @author Mike Stampone
 */
class SimulatedAnnealing  extends LocalSearch
{

        private Scheduler scheduler;
        private Random rand;
        private Mutation mutation;

        /**
         * Constructs a simulated annealing search from the specified heuristic
         * function and a default scheduler.
         *
         * @param hf
         *            a heuristic function
         */
        public SimulatedAnnealing() 
        {         
        	scheduler = new Scheduler();
        	rand = new Random();
        	mutation = new Mutation();
        }



    	@Override
    	public void improve(Individual initialNode, double loadPenaltyFactor, double routeTimePenaltyFactor) 
    	{
    		// TODO Auto-generated method stub

    		//Mutation mutation = new Mutation();    		
    		Individual current,next;
    		current = new Individual(initialNode);
    		next = null;
                    
    		// for t = 1 to INFINITY do
            int timeStep = 0;
            
            // temperature <- schedule(t)
            do
            {
	            double temperature = scheduler.getTemp(timeStep);
	            //System.out.println("TimeStep - Temp : "+timeStep+" "+temperature );
	            timeStep++;
	            // if temperature = 0 then return current
	            if (temperature == 0.0) 
	            {
	                    break;
	            }
	
	            // next <- a randomly selected successor of current
	            next = new Individual(current);
    			applyMutation(next);
    			TotalCostCalculator.calculateCost(next, loadPenaltyFactor, routeTimePenaltyFactor);
	            
    			// /\E <- current.VALUE - next.value
    			// if del E +ve -> next better 
	    		double deltaE = current.costWithPenalty - next.costWithPenalty;
	
	            if (shouldAccept(temperature, deltaE))
	            {
	                    current = next;
	            }
	            
            }while(true);

            //System.out.println("Before - After : "+initialNode.costWithPenalty + " "+current.costWithPenalty);
            if(initialNode.costWithPenalty>current.costWithPenalty)
            	initialNode.copyIndividual(current);

    	}
		
        // if /\E > 0 then current <- next
        // else current <- next only with probability e^(/\E/T)
        private boolean shouldAccept(double temperature, double deltaE) 
        {
                return (deltaE > 0.0)
                                || (rand.nextDouble() <= probabilityOfAcceptance(
                                                temperature, deltaE));
        }

        /**
         * Returns <em>e</em><sup>&delta<em>E / T</em></sup>
         *
         * @param temperature
         *            <em>T</em>, a "temperature" controlling the probability of
         *            downward steps
         * @param deltaE
         *            VALUE[<em>next</em>] - VALUE[<em>current</em>]
         * @return <em>e</em><sup>&delta<em>E / T</em></sup>
         */
        public double probabilityOfAcceptance(double temperature, double deltaE) {
                return Math.exp(deltaE / temperature);
        }

		void applyMutation(Individual offspring)
		{
			
			mutation.applyMutation(offspring);
//			offspring.calculateCostAndPenalty();

		}
}

/**
 * @author Ravi Mohan
 *
 */
class Scheduler {

        private final int k, limit;

        private final double lam;

        public Scheduler(int k, double lam, int limit) {
                this.k = k;
                this.lam = lam;
                this.limit = limit;
        }

        public Scheduler() 
        {
                this.k = 20;
                this.lam = 0.045;
                this.limit = 400;
        }

        public double getTemp(int t) {
                if (t < limit)
                {
                        double res = k * Math.exp((-1) * lam * t);
                        return res;
                } 
                else 
                {
                        return 0.0;
                }
        }
}



