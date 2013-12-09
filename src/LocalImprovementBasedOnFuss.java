
public class LocalImprovementBasedOnFuss extends LocalImprovement 
{
	public LocalImprovementBasedOnFuss(double loadPenaltyFactor,
			double routeTimePenaltyFactor, LocalSearch localSearch,
			int populationSize) {
		super(loadPenaltyFactor, routeTimePenaltyFactor, localSearch, populationSize);
		// TODO Auto-generated constructor stub
		
		count = populationSize/2;
	}

	SelectionOperator selectionOperator;
	@Override
	public void initialise(Individual[] population) {
		// TODO Auto-generated method stub
		selectionOperator = new FUSS();
		selectionOperator.initialise(population, true);
	}

	@Override
	public Individual selectIndividualForImprovement(Individual[] population) {
		// TODO Auto-generated method stub
		return selectionOperator.getIndividual(population);
	}


}
