
public class FirstChoiceHillClimbing extends LocalSearch {

	Mutation mutaion;
	
	public FirstChoiceHillClimbing() {
		// TODO Auto-generated constructor stub
		
		mutaion = new Mutation();
	}
	@Override
	public void improve(Individual initialNode, double loadPenaltyFactor, double routeTimePenaltyFactor) 
	{
		// TODO Auto-generated method stub

		//Mutation mutation = new Mutation();
		int retry=0;
		
		Individual node,neighbour;
		node = new Individual(initialNode);
		
		while(retry<50)
		{			
			neighbour = new Individual(node);
			applyMutation(neighbour);
			TotalCostCalculator.calculateCost(neighbour, loadPenaltyFactor, routeTimePenaltyFactor);
			
			//better
			if(neighbour.costWithPenalty <= node.costWithPenalty)
			{
				node = neighbour;
				retry=0;
			}
			else
			{
				retry++;
			}
		}
		
		
		initialNode.copyIndividual(node);
		
	}

	
	void applyMutation(Individual offspring)
	{
			mutaion.applyMutation(offspring);
	}
}
