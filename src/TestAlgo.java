import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;


public class TestAlgo  implements GeneticAlgorithm
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
	
	
	public TestAlgo(ProblemInstance problemInstance) 
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
		routeTimePenaltyFactor = 1;
		
	}

	public Individual run() 
	{
		
		int selectedParent1,selectedParent2;
		int i;
		
		Individual parent1,parent2,offspring1,offspring2;

		
		Individual.calculateProbalityForDiefferentVehicle(problemInstance);
		//problemInstance.print();
		// INITIALISE POPULATION
		initialisePopulation();
//		TotalCostCalculator.calculateCostofPopulation(population,0,POPULATION_SIZE, loadPenaltyFactor, routeTimePenaltyFactor);
	
		
		for(int generation=0;generation<1;generation++)
		{
			for(i=0;i<POPULATION_SIZE;i+=2)
			{
				parent1 = population[i];
				parent2 = population[i+1];
				
				offspring1 = new Individual(problemInstance);
				offspring2 = new Individual(problemInstance);
			
				Individual.crossOver_Uniform_Uniform(problemInstance, parent1, parent2, offspring1, offspring2);
				
				
				problemInstance.out.println("OFFSPRNG BEFORE: ");
				offspring1.print();
				
				offspring1.mutatePeriodAssignment();
				
				problemInstance.out.println("OFFSPRNG After: ");
				offspring1.print();
				
				
				/*problemInstance.out.println("PARENT1 : ");
				parent1.print();
				problemInstance.out.println("PARENT2 : ");
				parent2.print();
				problemInstance.out.println("OFFSPRNG1 : ");
				offspring1.print();
				problemInstance.out.println("OFFSPRNG2 : ");
				offspring2.print();*/
			}
		}

		return population[0];

	}
	
	
	

	
	
	void initialisePopulation()
	{
		out.print("Initial population : \n");
		for(int i=0; i<POPULATION_SIZE; i++)
		{
			population[i] = new Individual(problemInstance);
			population[i].initialise();
			//out.println("Printing individual "+ i +" : \n");
			population[i].print();
		}
	}

	
	public int getNumberOfGeeration() {
		// TODO Auto-generated method stub
		return NUMBER_OF_GENERATION;
	}

}
