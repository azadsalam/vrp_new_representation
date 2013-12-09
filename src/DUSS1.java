public class DUSS1 extends SelectionOperator
{

	double min,max;
	boolean survivalSelection;
	boolean mark[];
	
	
	
	/**
	 * Sorts the population
	 * 
	 */
	@Override
	public void initialise(Individual[] population, boolean survivalSelection) {
		// TODO Auto-generated method stub
		super.initialise(population,survivalSelection);
		
		Utility.sort(population);
		
		sortByDistance(population, population[0]);
		
		min = population[0].distance;
		max = population[population.length-1].distance;
		
		this.survivalSelection = survivalSelection;
		if(survivalSelection) mark = new boolean[population.length];
		
	}
	
	public void setProblemInsctance(ProblemInstance problemInstance) 
	{
		this.problemInstance = problemInstance; 
	}
	
	//if survival selection, makes sure that one individual is selected only ones
	@Override
	public Individual getIndividual(Individual[] population) 
	{
		if(problemInstance==null)
		{
			System.out.println("Set Problem Instance");
			return null;
		}
		// TODO Auto-generated method stub
		int index;

		if(survivalSelection)
		{
		    do
		    {
		    	index = getIndividualIndex(population);
		    }while(mark[index]==true);
		    
		    mark[index]=true;
		    
		    return population[index];
		}
		else
		{
			index = getIndividualIndex(population);
			return population[index];
		}
	}
	
	public int getIndividualIndex(Individual[] population) 
	{
		// TODO Auto-generated method stub
		
	    double randomDistance = Utility.randomDouble(min, max);
	    //System.out.println("rand : " + randomDistance);
	    
	    int i; 
	    for(i=0;i<population.length;i++)
	    {
	    	if(population[i].distance >= randomDistance)
	    		break;
	    }
		
	    if( Math.abs(population[i].distance - randomDistance) < Math.abs(population[i-1].distance - randomDistance) )
	    	return i;
	    else
	    	return (i-1);
	    
	}
	
	
	/**
	 * Sorts On The basis of distance , ascending
	 * @param array
	 * @param origin
	 */
	public void sortByDistance(Individual[] array,Individual origin)
	{
		Individual temp;
		int length = array.length;
		
		for(int i=0;i<length;i++)
			array[i].distance = Individual.distance(problemInstance, origin, array[i]);
		
		//FOR NOW DONE SELECTION SORT
		//AFTERWARDS REPLACE IT WITH QUICK SORT OR SOME OTHER O(n logn) sort
		for(int i=0;i<length;i++)
		{
			for(int j=i+1;j<length;j++)
			{
				if(array[i].distance > array[j].distance)
				{
					temp = array[i];
					array[i] =array[j];
					array[j] = temp;
				}
			}
		}

	}



}

