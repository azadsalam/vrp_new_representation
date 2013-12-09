import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;

import javax.rmi.CORBA.Util;


public class Scheme9_DUSS1 implements GeneticAlgorithm
{
	//Algorithm parameters
	int POPULATION_SIZE = 10; 
	int NUMBER_OF_OFFSPRING = 10;   
	int NUMBER_OF_GENERATION = 1000;	
	double loadPenaltyFactor = 10;
	double routeTimePenaltyFactor = 1;

	
	//Algorithm data structures
	Individual population[];
	Individual offspringPopulation[];
	Individual parentOffspringTotalPopulation[];

	//Operators
	MutationWithVariedStepSize mutationWithVariedStepSize;
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
	

	
	public Scheme9_DUSS1(ProblemInstance problemInstance) 
	{
		// TODO Auto-generated constructor stub
		this.problemInstance = problemInstance;
		out = problemInstance.out;


		mutationWithVariedStepSize = new MutationWithVariedStepSize();
		
		
		//Change here if needed
		population = new Individual[POPULATION_SIZE];
		offspringPopulation = new Individual[NUMBER_OF_OFFSPRING];		
		parentOffspringTotalPopulation = new Individual[POPULATION_SIZE + NUMBER_OF_OFFSPRING];
		
		//Add additional code here
		rouletteWheelSelection = new RoutletteWheelSelection();
	    fussSelection = new FUSS();
		survivalSelectionOperator = new DUSS1(); 
		survivalSelectionOperator.setProblemInsctance(problemInstance);

		localSearch = new FirstChoiceHillClimbing();
		localImprovement = new LocalImprovementBasedOnFussandElititst(loadPenaltyFactor, routeTimePenaltyFactor, localSearch, POPULATION_SIZE);	
	}

	public Individual run() 
	{
		int i,generation;
		
		Individual offspring1,offspring2;

		PopulationInitiator.initialisePopulation(population, POPULATION_SIZE, problemInstance);
		TotalCostCalculator.calculateCostofPopulation(population,0, POPULATION_SIZE, loadPenaltyFactor, routeTimePenaltyFactor) ;
		
		
		int continuosInjection=0; 
		int unImprovedGeneration=0;
		
		double previousBest=population[0].costWithPenalty;
//		double bestBeforeInjection=-1;
		
		for( generation=0;generation<NUMBER_OF_GENERATION;generation++)
		{
			//For collecting min,max,avg
			Solver.gatherExcelData(population, POPULATION_SIZE, generation);
			TotalCostCalculator.calculateCostofPopulation(population,0, POPULATION_SIZE, loadPenaltyFactor, routeTimePenaltyFactor) ;
			
			//  Best individual always reproduces K=degreeOfPolyGamy times + roulette wheel
			
			fussSelection.initialise(population, false);
			rouletteWheelSelection.initialise(population, false);
			
			int noOfPrincess;
			
			if(unImprovedGeneration<=5000)
			{
				noOfPrincess = 2;
			}
			else
			{
				unImprovedGeneration=0;
				noOfPrincess = POPULATION_SIZE/4;
				System.out.println("Polygamy Starts : "+noOfPrincess);
			}
			
			i=0;
			while(i<noOfPrincess)
			{
				parent1 = population[0];
				parent2 = fussSelection.getIndividual(population);
				
				offspring1 = new Individual(problemInstance);
				offspring2 = new Individual(problemInstance);
				
				Individual.crossOver(problemInstance, parent1, parent2, offspring1, offspring2);	
				
				mutationWithVariedStepSize.applyMutation(offspring1,generation);
				mutationWithVariedStepSize.applyMutation(offspring2,generation);
				
				offspringPopulation[i] = offspring1;
				i++;
				offspringPopulation[i] = offspring2;
				i++;
			}
			
			while(i<NUMBER_OF_OFFSPRING)
			{
				parent1 = rouletteWheelSelection.getIndividual(population);
				parent2 = fussSelection.getIndividual(population);
				
				offspring1 = new Individual(problemInstance);
				offspring2 = new Individual(problemInstance);
			
				Individual.crossOver(problemInstance, parent1, parent2, offspring1, offspring2);	
				
				mutationWithVariedStepSize.applyMutation(offspring1,generation);
				mutationWithVariedStepSize.applyMutation(offspring2,generation);
				
				offspringPopulation[i] = offspring1;
				i++;
				offspringPopulation[i] = offspring2;
				i++;
			}

			TotalCostCalculator.calculateCostofPopulation(offspringPopulation, 0,NUMBER_OF_OFFSPRING, loadPenaltyFactor, routeTimePenaltyFactor) ;
			Utility.concatPopulation(parentOffspringTotalPopulation, population, offspringPopulation);
			
			localImprovement.initialise(parentOffspringTotalPopulation);
			localImprovement.run(parentOffspringTotalPopulation);
			
			TotalCostCalculator.calculateCostofPopulation(parentOffspringTotalPopulation, 0, POPULATION_SIZE, loadPenaltyFactor, routeTimePenaltyFactor);
			

			/* REMOVE DUPLICATE */			
			Utility.sort(parentOffspringTotalPopulation);
		
			Vector<Integer> initList = new Vector<Integer>();
			for(i=1;i<parentOffspringTotalPopulation.length;i++)
			{
				double d = Individual.distance(problemInstance, parentOffspringTotalPopulation[i], parentOffspringTotalPopulation[i-1]);
				if(d==0)
				{
					initList.add(new Integer(i));
				}
			}
			
			if(!initList.isEmpty())
			{
				
				out.println("gene : "+generation+" NO. of duplicates: " + initList.size());
				for(i=0;i<initList.size();i++)
				{
					//out.println("PREV : ");
					//parentOffspringTotalPopulation[initList.get(i)-1].print();
					
					//out.println("DUplicate : ");
					//parentOffspringTotalPopulation[initList.get(i)].print();
					
					
					parentOffspringTotalPopulation[initList.get(i)] = new Individual(problemInstance);
					parentOffspringTotalPopulation[initList.get(i)].initialise();
					
					//out.println("New  : ");
					//parentOffspringTotalPopulation[initList.get(i)].print();
					
				}
				
				out.println("\n\n");
				
				
			}
			/* REMOVE DUPLICATE END*/

			
			
			//Preserving the k% best individual + injection + FUSS approach, the n portion of best individuals always make to next generation
			Utility.sort(parentOffspringTotalPopulation);
			
			
			int elitistRatio = POPULATION_SIZE * 10 /100 ;
			for(i=0;i<elitistRatio;i++)
			{
				population[i]=parentOffspringTotalPopulation[i];
			}
			//System.out.println("elitist : "+elitistRatio);
			
			/*
			population[0] = parentOffspringTotalPopulation[0];
			
			int index2=1;
			int index1=1;
			
			while(index1 < elitistRatio)
			{
				double d = Individual.distance(problemInstance, parentOffspringTotalPopulation[index2],population[index1-1]);
				if(d==0){	}
				else
				{
					population[index1] = parentOffspringTotalPopulation[index2];
					index1++;
				}
				
				index2++;
			}
			*/
			int injectionSize=0;
						
			/*
			if(unImprovedGeneration>20)
			{
				injectionSize = POPULATION_SIZE *20 /100;
				unImprovedGeneration=0;
			}
			*/
						
			for(i=elitistRatio;i<elitistRatio+injectionSize;i++)
			{
				population[i] = new Individual(problemInstance);
				population[i].initialise();
			}
			
			Individual total[] = new Individual[POPULATION_SIZE+NUMBER_OF_OFFSPRING-elitistRatio-injectionSize];
			System.arraycopy(parentOffspringTotalPopulation, elitistRatio, total, 0, total.length);
			
			survivalSelectionOperator.initialise(total, true);

			for( i=elitistRatio+injectionSize;i<POPULATION_SIZE;i++)
			{
				population[i]= survivalSelectionOperator.getIndividual(total);
			}
			
						
			TotalCostCalculator.calculateCostofPopulation(population, 0, POPULATION_SIZE, loadPenaltyFactor, routeTimePenaltyFactor);
			Utility.sort(population);	
			
			System.out.println("Generation : " + generation+" Best : "+ population[0].costWithPenalty);
			
			if( population[0].costWithPenalty < previousBest)
			{
				unImprovedGeneration=0;
				previousBest=population[0].costWithPenalty;
			}
			else
			{
				
				unImprovedGeneration++;
				//System.out.println("UnImproved gen : "+unImprovedGeneration+" previousBest : "+previousBest);
			}
			
			/*
			if(injectionSize>0)
			{
				System.out.println("injection Size : "+injectionSize);
				break;
			}
*/
		//	if(generation==50)
		//	break;
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
