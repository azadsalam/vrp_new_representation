
public class FirstChoiceHillClimbing extends LocalSearch {

	
	@Override
	public void improve(Individual initialNode, double loadPenaltyFactor, double routeTimePenaltyFactor) 
	{
		// TODO Auto-generated method stub

		//Mutation mutation = new Mutation();
		int retry=0;
		
		Individual node,neighbour;
		node = new Individual(initialNode);
		
		while(retry<15)
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
		
		int rand = 4;
		
		int selectedMutationOperator = Utility.randomIntInclusive(rand);
		
		if(selectedMutationOperator==0)
		{
			offspring.mutateRoutePartition();
		}
		else if (selectedMutationOperator == 1)
		{
			if(offspring.problemInstance.periodCount==1)
			{
				offspring.mutatePermutation();	
			}
			else
			{
				offspring.mutatePeriodAssignment();
			}
		}
		else if (selectedMutationOperator ==2)
		{
			offspring.mutatePermutation();
		}
		else if (selectedMutationOperator ==3)
		{
			offspring.mutateRouteWithInsertion();
		}
		
		else if (selectedMutationOperator ==4)
		{
			offspring.mutateRoutePartitionWithRandomStepSize();
		}
		
	
	}
}
