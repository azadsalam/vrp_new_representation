import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;

import javax.rmi.CORBA.Util;


public class Scheme8_with_single_threshold_Archive implements GeneticAlgorithm
{
	//Algorithm parameters
	int POPULATION_SIZE = 1500; 
	int NUMBER_OF_OFFSPRING = 1500;   
	int NUMBER_OF_GENERATION = 1000;	
	double loadPenaltyFactor = 20;
	double routeTimePenaltyFactor = 20;

	
	//Algorithm data structures
	Individual population[];
	Individual offspringPopulation[];
	Individual parentOffspringTotalPopulation[];

	//Operators
	MutationWithVariedStepSize mutationWithVariedStepSize;
	//ProportionateMutation proportionateMutation;
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
	
	Vector<Individual> archive;
	
	public Scheme8_with_single_threshold_Archive(ProblemInstance problemInstance) 
	{
		// TODO Auto-generated constructor stub
		this.problemInstance = problemInstance;
		out = problemInstance.out;


		//proportionateMutation = new ProportionateMutation(problemInstance, 3, 0.1, NUMBER_OF_GENERATION);
		mutationWithVariedStepSize = new MutationWithVariedStepSize();
		
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
	
		archive = new Vector<Individual>();
	}

	public Individual run() 
	{
		int i,generation;
		
		Individual offspring1,offspring2;

		PopulationInitiator.initialisePopulation(population, POPULATION_SIZE, problemInstance);
		TotalCostCalculator.calculateCostofPopulation(population,0, POPULATION_SIZE, loadPenaltyFactor, routeTimePenaltyFactor) ;
		PrintWriter intermeDiateCSV=null;
		
		int continuosInjection=0; 
		int unImprovedGeneration=0;
		int criticalValue = 15;
		
		int failure=0;
		
		double lastFailureBest=-1;
		double previousBest=population[0].costWithPenalty;
//		double bestBeforeInjection=-1;
		
		for( generation=0;generation<NUMBER_OF_GENERATION && failure < 13;generation++)
		{
			//For collecting min,max,avg
			Solver.gatherExcelData(population, POPULATION_SIZE, generation);
			TotalCostCalculator.calculateCostofPopulation(population,0, POPULATION_SIZE, loadPenaltyFactor, routeTimePenaltyFactor) ;
			
			//  Best individual always reproduces K=degreeOfPolyGamy times + roulette wheel
			
			fussSelection.initialise(population, false);
			rouletteWheelSelection.initialise(population, false);
			
			int noOfPrincess;
			
			// FOR NOW NO EXTRA KHATIR ;-) 
			noOfPrincess = 2;
			
			
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
				out.println("generation : "+generation+" NO. of duplicates: " + initList.size());
				for(i=0;i<initList.size();i++)
				{	
					parentOffspringTotalPopulation[initList.get(i)] = new Individual(problemInstance);
					parentOffspringTotalPopulation[initList.get(i)].initialise();
					
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
			
			int injectionSize=0;
						
			
			
			if(unImprovedGeneration==30)
			{
				injectionSize = POPULATION_SIZE * 25 /100;
			}
			
			else if(unImprovedGeneration==60)
			{
				injectionSize = POPULATION_SIZE * 50 /100;
			}
			
			else if(unImprovedGeneration >= 100)
			{
				injectionSize = 0 ;
				unImprovedGeneration = 0;	
				
				for(i=0;i<elitistRatio;i++)
				{
					archive.add(population[i]);
					population[i] = new Individual(problemInstance);
					population[i].initialise();
				
				}
				
				previousBest = population[i].costWithPenalty;
				
				TotalCostCalculator.calculateCostofPopulation(population, 0, elitistRatio, loadPenaltyFactor, routeTimePenaltyFactor);
				
			}
			
				
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
			
			if(injectionSize>0)
			{
//				System.out.println("injected random population : "+injectionSize);
//				break;
			}
			
			System.out.println("Generation : " + generation+" Best : "+ population[0].costWithPenalty + " unimproved : "+unImprovedGeneration);
		
		}
		
		
		
		Individual archiveArray[] = new Individual[archive.size()];
		for(i=0;i<archive.size();i++) archiveArray[i] = archive.get(i);
		TotalCostCalculator.calculateCostofPopulation(archiveArray, 0, archiveArray.length, loadPenaltyFactor, routeTimePenaltyFactor);
		Utility.sort(archiveArray);

		
		TotalCostCalculator.calculateCostofPopulation(population,0,POPULATION_SIZE, loadPenaltyFactor, routeTimePenaltyFactor);
		Utility.sort(population);
		
		
		System.out.println("FINAL ARCHIVE Size : "+archive.size());
		for(i=0;i<archive.size();i++)
			System.out.print(archiveArray[i].costWithPenalty+" ");
		System.out.println();

		Solver.gatherExcelData(population, POPULATION_SIZE, generation);
		
		
		if(Solver.outputToFile)
		{
			out.print("\n\n\n\n\n--------------------------------------------------\n");

			out.print("\n\n\nFinal Archive\n\n");
			for( i=0;i<archiveArray.length;i++)
			{
				out.println("\n\nArchive Individual : "+i);
				archiveArray[i].print();
			}

			
			out.print("\n\n\nFINAL POPULATION\n\n");
			for( i=0;i<POPULATION_SIZE;i++)
			{
				out.println("\n\nIndividual : "+i);
				population[i].print();
			}
		}
		
		Individual best;
		if(population[0].costWithPenalty < archiveArray[0].costWithPenalty )
		{
			best =  population[0];
		}
		else
		{
			best = archiveArray[0];
		}
		
		System.out.println("Best : "+ best.costWithPenalty);
		return best;
	}
	
	public int getNumberOfGeeration()
	{
		return NUMBER_OF_GENERATION;
	}
}
