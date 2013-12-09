public class PopulationInitiator 
{
	static void initialisePopulation(Individual[] population,int populationSize,ProblemInstance problemInstance)
	{
		//	out.print("Initial population : \n");
		for(int i=0; i<populationSize; i++)
		{
			population[i] = new Individual(problemInstance);
			population[i].initialise();
			//problemInstance.out.println("Printing individual "+ i +" : \n");
			//population[i].print();
		}
	}
}
