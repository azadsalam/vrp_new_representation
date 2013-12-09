public abstract class LocalImprovement 
{
	public LocalSearch localSearch;
	public double loadPenaltyFactor ;
	public double routeTimePenaltyFactor ;
	int count;
	
	public LocalImprovement(double loadPenaltyFactor, double routeTimePenaltyFactor,LocalSearch localSearch,int populationSize) 
	{
		// TODO Auto-generated constructor stub
		this.loadPenaltyFactor = loadPenaltyFactor;
		this.routeTimePenaltyFactor = routeTimePenaltyFactor;
		this.localSearch = localSearch;
		count = populationSize/4 ;
	}
	public abstract void initialise(Individual[] population); 

	public void run(Individual[] population)
	{		
		//System.out.print("Count : "+count);

		Individual selected[] = new Individual[count];
		for(int i=0;i<count;i++)
		{
			selected[i] = selectIndividualForImprovement(population);
			//System.out.print(" "+selected[i].costWithPenalty);
			
		}
		
		//System.out.println("");
		
		for (int i = 0; i < count; i++) {
			
			localSearch.improve(selected[i], loadPenaltyFactor, routeTimePenaltyFactor);			
		}
	}
	
	public abstract Individual selectIndividualForImprovement(Individual[] population);
}
