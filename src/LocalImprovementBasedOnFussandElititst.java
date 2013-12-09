
public class LocalImprovementBasedOnFussandElititst extends LocalImprovement 
{
	public LocalImprovementBasedOnFussandElititst(double loadPenaltyFactor,
			double routeTimePenaltyFactor, LocalSearch localSearch,
			int populationSize) {
		super(loadPenaltyFactor, routeTimePenaltyFactor, localSearch, populationSize);
		// TODO Auto-generated constructor stub
		
		count = populationSize/4;
	}

	SelectionOperator selectionOperator;
	int elitistCount;

	@Override
	public void initialise(Individual[] population) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run(Individual[] population) 
	{
		//5% elitist
		elitistCount = population.length  * 5 / 100;
		count = population.length / 3;
		
		selectionOperator = new FUSS();
		selectionOperator.initialise(population, true);
		
		// TODO Auto-generated method stub
		Individual selected[] = new Individual[count];
		
		//System.out.println("Count : "+count+" Elitist COunt : "+ elitistCount + " pop : "+population.length   );
		
		int i;
		for(i=0;i<elitistCount;i++)
		{
			selected[i] = population[i];
		}
		
		for(i=elitistCount;i<count;i++)
		{
			selected[i] = selectIndividualForImprovement(population);
			//System.out.print(" "+selected[i].costWithPenalty);
			
		}
		for ( i = 0; i < count; i++) {
			localSearch.improve(selected[i], loadPenaltyFactor, routeTimePenaltyFactor);			
		}

	}
	@Override
	public Individual selectIndividualForImprovement(Individual[] population) {
		// TODO Auto-generated method stub
		return selectionOperator.getIndividual(population);
	}


}
