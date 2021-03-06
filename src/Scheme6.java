import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;

import javax.rmi.CORBA.Util;


public class Scheme6 implements GeneticAlgorithm
{
	//Algorithm parameters
	int POPULATION_SIZE = 100; 
	int NUMBER_OF_OFFSPRING = 100;   
	int NUMBER_OF_GENERATION = 100;
	double loadPenaltyFactor = 0;
	double routeTimePenaltyFactor = 0;

	//Algorithm data structures
	Individual population[];
	Individual offspringPopulation[];
	Individual parentOffspringTotalPopulation[];

	//Operators
	Mutation mutation;
    SelectionOperator rouletteWheelSelection;
    SelectionOperator fussSelection;
    SelectionOperator survivalSelectionOperator;
    LocalImprovement localImprovement;
    LocalSearch localSearch;
	
	//Utility Functions	
	PrintWriter out; 
	ProblemInstance problemInstance;

	//Temprary Variables
	Individual parent1,parent2;
	

	
	public Scheme6(ProblemInstance problemInstance) 
	{
		// TODO Auto-generated constructor stub
		this.problemInstance = problemInstance;
		out = problemInstance.out;


		mutation = new Mutation();
		
		
		//Change here if needed
		population = new Individual[POPULATION_SIZE];
		offspringPopulation = new Individual[NUMBER_OF_OFFSPRING];		
		parentOffspringTotalPopulation = new Individual[POPULATION_SIZE + NUMBER_OF_OFFSPRING];
		
		//Add additional code here
		rouletteWheelSelection = new RoutletteWheelSelection();
	    fussSelection = new FUSS();
		survivalSelectionOperator = new FUSS(); 

		localSearch = new FirstChoiceHillClimbing();
		localImprovement = new LocalImprovementBasedOnFussandElititst(loadPenaltyFactor, routeTimePenaltyFactor, localSearch, POPULATION_SIZE);	
	}

	public Individual run() 
	{
		int i,generation;
		
		Individual offspring1,offspring2;

		Individual.calculateAssignmentProbalityForDiefferentDepot(problemInstance);
		Individual.calculateProbalityForDiefferentVehicle(problemInstance);
		PopulationInitiator.initialisePopulation(population, POPULATION_SIZE, problemInstance);
		TotalCostCalculator.calculateCostofPopulation(population,0, POPULATION_SIZE, loadPenaltyFactor, routeTimePenaltyFactor) ;
		
		
		int continuosInjection=0; 
		//int unImprovedGeneration=0;
		
		double previousBest=-1;
		double bestBeforeInjection=-1;
		
		for( generation=0;generation<NUMBER_OF_GENERATION;generation++)
		{
			//For collecting min,max,avg
			Solver.gatherExcelData(population, POPULATION_SIZE, generation);
			TotalCostCalculator.calculateCostofPopulation(population,0, POPULATION_SIZE, loadPenaltyFactor, routeTimePenaltyFactor) ;
			
			//  Best individual always reproduces K=1 times + roulette wheel
			
			
			fussSelection.initialise(population, false);
			rouletteWheelSelection.initialise(population, false);
			
			i=0;
			
			parent1 = population[0];
			parent2 = fussSelection.getIndividual(population);
			
			offspring1 = new Individual(problemInstance);
			offspring2 = new Individual(problemInstance);
			
			Crossover_Uniform_Uniform.crossOver_Uniform_Uniform(problemInstance, parent1, parent2, offspring1, offspring2);	
			
			mutation.applyMutation(offspring1);
			mutation.applyMutation(offspring2);
			
			offspringPopulation[i] = offspring1;
			i++;
			offspringPopulation[i] = offspring2;
			i++;
			
			while(i<NUMBER_OF_OFFSPRING)
			{
				parent1 = rouletteWheelSelection.getIndividual(population);
				parent2 = fussSelection.getIndividual(population);
				
				offspring1 = new Individual(problemInstance);
				offspring2 = new Individual(problemInstance);
			
				Crossover_Uniform_Uniform.crossOver_Uniform_Uniform(problemInstance, parent1, parent2, offspring1, offspring2);	
				
				//System.out.println("off : "+i);
				mutation.applyMutation(offspring1);
				mutation.applyMutation(offspring2);
				
				offspringPopulation[i] = offspring1;
				i++;
				offspringPopulation[i] = offspring2;
				i++;
			}

			TotalCostCalculator.calculateCostofPopulation(offspringPopulation, 0,NUMBER_OF_OFFSPRING, loadPenaltyFactor, routeTimePenaltyFactor) ;
			Utility.concatPopulation(parentOffspringTotalPopulation, population, offspringPopulation);
			
			
			
			for(int p=0;p<parentOffspringTotalPopulation.length;p++)
			{
				if(parentOffspringTotalPopulation[p].validationTest()==false)
					System.out.println("Individual is invalid :(!");
			}
			
			//localImprovement.initialise(parentOffspringTotalPopulation);
			//localImprovement.run(parentOffspringTotalPopulation);
			
			TotalCostCalculator.calculateCostofPopulation(parentOffspringTotalPopulation, 0, POPULATION_SIZE, loadPenaltyFactor, routeTimePenaltyFactor);
			
			//Preserving the k% best individual + FUSS approach, the n portion of best individuals always make to next generation
			Utility.sort(parentOffspringTotalPopulation);

			int elitistRatio = POPULATION_SIZE * 10 /100 ;
			
			population[0] = parentOffspringTotalPopulation[0];
			
			int index2=1;
			int index1=1;
			
			while(index1 < elitistRatio)
			{
				population[index1] = parentOffspringTotalPopulation[index2];
				index1++;
				index2++;
			}
			
			
			Individual total[] = new Individual[POPULATION_SIZE+NUMBER_OF_OFFSPRING-elitistRatio];
			System.arraycopy(parentOffspringTotalPopulation, elitistRatio, total, 0, total.length);
			
			survivalSelectionOperator.initialise(total, true);
			for( i=elitistRatio;i<POPULATION_SIZE;i++)
			{
				population[i]= survivalSelectionOperator.getIndividual(total);
			}
			
			
			
			/*
			if(unImprovedGeneration>=5)
			{
				unImprovedGeneration=0;
				
				int sizeParentOffspring=parentOffspringTotalPopulation.length;
				int margin=sizeParentOffspring/4;
					
				
				Utility.sort(parentOffspringTotalPopulation);
				
				if(bestBeforeInjection == -1)
				{
					bestBeforeInjection = population[0].costWithPenalty;
				}
				else if(population[0].costWithPenalty == bestBeforeInjection)
				{
					//margin = 1;
				}
				
				
				ProblemInstance pInstance=population[0].problemInstance;
				for(int noInject=margin;noInject<sizeParentOffspring;noInject++)
				{
					Individual newIndividual=new Individual(pInstance);
					newIndividual.initialise();
					
					//double p = Utility.randomDouble(0, 1);
					//if(p<=0.5)localSearch.improve(newIndividual, loadPenaltyFactor, routeTimePenaltyFactor);
					
					parentOffspringTotalPopulation[noInject]=newIndividual;
				}
				TotalCostCalculator.calculateCostofPopulation(parentOffspringTotalPopulation, 0,parentOffspringTotalPopulation.length, loadPenaltyFactor, routeTimePenaltyFactor) ;
			}
			*/
			/************************ End of population Injection Block **************************/

			
			Utility.sort(population);	
			
			double tmpSum=0;
			for(int tmpi=0;tmpi<POPULATION_SIZE;tmpi++)
				tmpSum += population[tmpi].costWithPenalty;
			
			System.out.println("Gen : "+ generation + " Best : "+population[0].costWithPenalty+" Avg : "+(tmpSum/POPULATION_SIZE));
			
			
			
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
	
	public int getNumberOfGeeration()
	{
		return NUMBER_OF_GENERATION;
	}
}
