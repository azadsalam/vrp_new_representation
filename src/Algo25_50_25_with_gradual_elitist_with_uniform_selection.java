import java.io.PrintWriter;
import java.util.Scanner;


public class Algo25_50_25_with_gradual_elitist_with_uniform_selection implements GeneticAlgorithm
{
	PrintWriter out; 
	
	int BEST = 0;
	int MODERATE = 1;
	int WORST = 2; 
	
	int bestStart,moderateStart,worstStart;
	int bestInterval,moderateInterval,worstInterval; 
	
	int POPULATION_SIZE = 200;//must be even
	int NUMBER_OF_OFFSPRING = POPULATION_SIZE;   
	int NUMBER_OF_GENERATION = 1000;
	
	int INITIAL_DEGREE_OF_ELITISM = 30;
	int FINAL_DEGREE_OF_ELITISM = 80;
	double elitistRatio;
	
	ProblemInstance problemInstance;
	Individual population[];

	// for selection - roulette wheel
	double fitness[];
	double cdf[];

	double loadPenaltyFactor;
	double routeTimePenaltyFactor;
	
	Mutation mutation;
	
	Individual parent1,parent2;
	
	public Algo25_50_25_with_gradual_elitist_with_uniform_selection(ProblemInstance problemInstance) 
	{
		// TODO Auto-generated constructor stub
		this.problemInstance = problemInstance;
		out = problemInstance.out;
		
		mutation = new Mutation();
		
		population = new Individual[POPULATION_SIZE+NUMBER_OF_OFFSPRING];
				
		loadPenaltyFactor = 10;
		routeTimePenaltyFactor = 1;
	
		//if pop = 100
		//[0 - 25]
		bestStart = 0;
		bestInterval = POPULATION_SIZE/4  ;
		
		//[26 - 75]
		moderateStart = bestInterval + 1;
		moderateInterval = POPULATION_SIZE/2 - 1 ;
		
		//[76-100]
		worstStart = moderateStart + moderateInterval + 1;
		worstInterval = bestInterval;
		

	}

	public Individual run() 
	{
		elitistRatio = (FINAL_DEGREE_OF_ELITISM - INITIAL_DEGREE_OF_ELITISM) / NUMBER_OF_GENERATION;
		
		int i,generation;
		
		Individual offspring1,offspring2;

		PopulationInitiator.initialisePopulation(population, POPULATION_SIZE+NUMBER_OF_OFFSPRING, problemInstance);
		
		
		for( generation=0;generation<NUMBER_OF_GENERATION;generation++)
		{
						
			TotalCostCalculator.calculateCostofPopulation(population,0, POPULATION_SIZE, loadPenaltyFactor, routeTimePenaltyFactor) ;
			Solver.gatherExcelData(population, POPULATION_SIZE, generation);
			
			Utility.sort(population,POPULATION_SIZE);
			
			
			i=0;
			while(i<NUMBER_OF_OFFSPRING)
			{
				selectParent();
				
				offspring1 = new Individual(problemInstance);
				offspring2 = new Individual(problemInstance);
				Individual.crossOver(problemInstance, parent1, parent2, offspring1, offspring2);	
				
				mutation.applyMutation(offspring1);
				mutation.applyMutation(offspring2);
				
				population[i+POPULATION_SIZE] = offspring1;
				i++;
				population[i+POPULATION_SIZE] = offspring2;
				i++;
			}

			TotalCostCalculator.calculateCostofPopulation(population, POPULATION_SIZE,NUMBER_OF_OFFSPRING, loadPenaltyFactor, routeTimePenaltyFactor) ;
			Utility.sort(population);
			
			//semi elitist approach, the a portion of best individuals always make to next generation
			int n =  ((INITIAL_DEGREE_OF_ELITISM +(int)(elitistRatio* generation))* POPULATION_SIZE) / 100 ;
			
			int rand,rand2,index;
			// do some selection mechanism here
			//binary tournament
			for(i=n;i<POPULATION_SIZE;i++)
			{
				rand  = Utility.randomIntInclusive(i, POPULATION_SIZE*2 - 1);
				 
				Individual temp = population[i];
				population[i]= population[rand];
				population[rand]=temp;
			}
		}


		TotalCostCalculator.calculateCostofPopulation(population,0,POPULATION_SIZE, loadPenaltyFactor, routeTimePenaltyFactor);
		Utility.sort(population);
		Solver.gatherExcelData(population, POPULATION_SIZE, generation);
		

		if(Solver.outputToFile)
		{
			out.print("\n\n\n\n\n--------------------------------------------------\n");
		//	calculateCostWithPenalty(0, POPULATION_SIZE, generation, true);
			out.print("\n\n\nFINAL POPULATION\n\n");
			for( i=0;i<POPULATION_SIZE;i++)
			{
				out.println("\n\nIndividual : "+i);
				population[i].print();
			}
		}
		
		return population[0];

	}
	
	// selects pair of parents according to probability
	void selectParent()
	{
		int random = Utility.randomIntInclusive(100);
		int p1,p2;
		
		if(random<10)	  { p1 = BEST ; p2 = BEST;}
		else if(random<30){ p1 = BEST ; p2 = WORST;}
		else if(random<50){ p1 = BEST ; p2 = MODERATE;}
		else if(random<65){ p1 = MODERATE ; p2 = MODERATE;}
		else if(random<80){ p1 = MODERATE ; p2 = WORST;}
		else 			  { p1 = WORST ; p2 = WORST;}
		
		parent1 = selectParent(p1);
		parent2 = selectParent(p2);
	}
	//picks a parent randomly from a category
	Individual selectParent(int category)
	{
		
		int index;
		if(category == BEST)
		{
			index = Utility.randomIntInclusive(bestInterval) + bestStart;
		}
		else if(category == MODERATE)
		{
			index = Utility.randomIntInclusive(moderateInterval) + moderateStart;
		}
		else
		{
			index = Utility.randomIntInclusive(worstInterval) + worstStart;
		}
		
		if(index>=POPULATION_SIZE) index = Utility.randomIntInclusive(POPULATION_SIZE-1); // NEVER SHOULD HAPPEN
		
		return population[index];
	}
	
	public int getNumberOfGeeration()
	{
		return NUMBER_OF_GENERATION;
	}
}
