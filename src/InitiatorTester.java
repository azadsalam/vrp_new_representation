import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;


public class InitiatorTester  implements GeneticAlgorithm
{
	PrintWriter out; 
	
	int POPULATION_SIZE = 10;
	int NUMBER_OF_OFFSPRING = 10;
	int NUMBER_OF_GENERATION = 1;
	
	ProblemInstance problemInstance;
	Individual population[];

	//for storing new generated offsprings
	Individual offspringPopulation[];

	//for temporary storing
	Individual temporaryPopulation[];

	// for selection - roulette wheel
	double fitness[];
	double cdf[];

	double loadPenaltyFactor;
	double routeTimePenaltyFactor;
	
	
	public InitiatorTester(ProblemInstance problemInstance) 
	{
		// TODO Auto-generated constructor stub
		this.problemInstance = problemInstance;
		out = problemInstance.out;
		
		population = new Individual[POPULATION_SIZE];
		offspringPopulation = new Individual[NUMBER_OF_OFFSPRING];
		temporaryPopulation = new Individual[NUMBER_OF_GENERATION];
		
		fitness = new double[POPULATION_SIZE];
		cdf = new double[POPULATION_SIZE];
		
		loadPenaltyFactor = 0;
		routeTimePenaltyFactor = 0;
		
	}

	public Individual run() 
	{
		
				
		//problemInstance.print();
		// INITIALISE POPULATION
		initialisePopulation();
		TotalCostCalculator.calculateCostofPopulation(population,0,POPULATION_SIZE, loadPenaltyFactor, routeTimePenaltyFactor);
		Utility.sort(population);

		double min = population[0].costWithPenalty;
		double max = population[0].costWithPenalty;
		double total=0;
		
		for(int i=0;i<POPULATION_SIZE;i++)
		{
			if(population[i].costWithPenalty<min)
				min = population[i].costWithPenalty;
			if(population[i].costWithPenalty>max)
				max = population[i].costWithPenalty;
			
			total +=  population[i].costWithPenalty;
			
		}
		

		System.out.println("Best : "+min +" avg : "+(total/POPULATION_SIZE)+" worst : "+max);
		
		for(int i=0; i<POPULATION_SIZE; i++)
		{
	
			out.println("Printing individual "+ i +" : \n");
			population[i].print();
		}

		boolean suc=false;
		
		Solver.visualiser.drawIndividual(population[0], "Best Initial");
		
		return population[0];
	}
	
	
	

	
	
	void initialisePopulation()
	{
		Individual.calculateAssignmentProbalityForDiefferentDepot(problemInstance);
		Individual.calculateProbalityForDiefferentVehicle(problemInstance);
		//out.print("Initial population : \n");
		for(int i=0; i<POPULATION_SIZE; i++)
		{
			population[i] = new Individual(problemInstance);
			population[i].initialise();
			//out.println("Printing Initial individual "+ i +" : \n");
			TotalCostCalculator.calculateCost(population[i], loadPenaltyFactor, routeTimePenaltyFactor);
			//population[i].print();
		}
	}

	
	public int getNumberOfGeeration() {
		// TODO Auto-generated method stub
		return NUMBER_OF_GENERATION;
	}

}
