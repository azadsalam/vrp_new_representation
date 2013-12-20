
public class Mutation 
{	
	//ProblemInstance problemInstance;
	
	
	/*
	public Mutation(ProblemInstance problemInstance) 
	{
		// TODO Auto-generated constructor stub
		this.problemInstance = problemInstance;
	}
	*/
	
	
	void applyMutation(Individual offspring)
	{
		int rand = 4;
		if(offspring.problemInstance.periodCount==1)rand--;
		
		int selectedMutationOperator = Utility.randomIntInclusive(rand);
		
		if(selectedMutationOperator==0)
		{
			offspring.mutateRouteBySwapping();
		}
		else if (selectedMutationOperator == 1)
		{			
			offspring.mutateTwoDifferentRouteBySwapping();
		}
		else if (selectedMutationOperator == 2)
		{
			offspring.mutateRouteWithInsertion();
		}
		else if (selectedMutationOperator == 3)
		{
			offspring.mutateTwoDifferentRouteBySubstitution();
		}
		else 
		{
			offspring.mutatePeriodAssignment();
		}
		
		offspring.calculateCostAndPenalty();
		
	}

}
