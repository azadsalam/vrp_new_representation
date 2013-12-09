
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
	
	
	void applyMutation(Individual offspring,int generation)
	{
		int rand = 4;
		if(offspring.problemInstance.periodCount==1)rand--;
		
		int selectedMutationOperator = Utility.randomIntInclusive(rand);
		
		if(selectedMutationOperator==0)
		{
			offspring.mutatePermutationWithinSingleRouteBySwapping();
		}
		else if (selectedMutationOperator == 1)
		{			
			offspring.mutatePermutationOfDifferentRouteBySwapping();
		}
		else if (selectedMutationOperator == 2)
		{
			offspring.mutatePermutationWithInsertion();
		}
		else if (selectedMutationOperator == 3)
		{
			offspring.mutateRoutePartitionWithRandomStepSize();
		}
		else 
		{
			offspring.mutatePeriodAssignment();
		}
		
		offspring.calculateCostAndPenalty();
		
	}

}
