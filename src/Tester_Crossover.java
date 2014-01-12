import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;


public class Tester_Crossover  implements GeneticAlgorithm
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
	
	
	public Tester_Crossover(ProblemInstance problemInstance) 
	{
		// TODO Auto-generated constructor stub
		this.problemInstance = problemInstance;
		out = problemInstance.out;
		
		population = new Individual[POPULATION_SIZE];
		offspringPopulation = new Individual[NUMBER_OF_OFFSPRING];
		temporaryPopulation = new Individual[NUMBER_OF_GENERATION];
		
		fitness = new double[POPULATION_SIZE];
		cdf = new double[POPULATION_SIZE];
		
		loadPenaltyFactor = 10;
		routeTimePenaltyFactor = 10;
		
	}

	public Individual run() 
	{
		//problemInstance.print();
		// INITIALISE POPULATION
		
		initialisePopulation();
		TotalCostCalculator.calculateCostofPopulation(population,0,POPULATION_SIZE, loadPenaltyFactor, routeTimePenaltyFactor);
		
		
		Individual child = new Individual(problemInstance);
		problemInstance.out.println("PARENT 1");
		population[0].miniPrint();
		problemInstance.out.println("PARENT 2");
		population[1].miniPrint();
		
		Uniform_VariedEdgeRecombnation_Crossover.crossOver_Uniform_VariedEdgeRecombination(problemInstance, population[0], population[1],child );
		
		problemInstance.out.println("Child 1");
		child.miniPrint();
		
		Utility.sort(population);
		


		if(Solver.showViz==true)
		{
			Solver.visualiser.drawIndividual(population[0], "Best Initial");
			Solver.visualiser.drawIndividual(population[POPULATION_SIZE-1], "Worst Initial");
		}
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
