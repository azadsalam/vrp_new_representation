
public class UniformCrossoverPeriodAssigment 
{

	public static  void uniformCrossoverForPeriodAssignment(Individual child1,Individual child2, Individual parent1, Individual parent2,ProblemInstance problemInstance)
	{
		int coin;
		int i;
		
		Individual temp1,temp2;
		for(i=0;i<problemInstance.customerCount;i++)
		{
			coin = Utility.randomIntInclusive(1);
			
			if(coin==0)
			{
				temp1=child1;
				temp2=child2;
			}
			else
			{
				temp1=child2;
				temp2=child1;
			}	
			
			for(int period = 0; period<problemInstance.periodCount; period++)
			{
				//if(parent1==null)System.out.print("nul");
				temp1.periodAssignment[period][i] = parent1.periodAssignment[period][i];
				temp2.periodAssignment[period][i] = parent2.periodAssignment[period][i];
			}
		}
		
	}

	public static  void uniformCrossoverForPeriodAssignment(Individual child, Individual parent1, Individual parent2,ProblemInstance problemInstance)
	{
		int coin;
		int i;
		
		Individual temp1,temp2;
		for(i=0;i<problemInstance.customerCount;i++)
		{
			coin = Utility.randomIntInclusive(1);
			
			
			for(int period = 0; period<problemInstance.periodCount; period++)
			{
				
				if(coin==0)
				{
					child.periodAssignment[period][i] = parent1.periodAssignment[period][i];

				}
				else
				{
					child.periodAssignment[period][i] = parent2.periodAssignment[period][i];
				}
				
			}
		}
		
	}

}
