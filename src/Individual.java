import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;


public class Individual 
{
	//representation
	boolean periodAssignment[][];
	int permutation[][];
	int routePartition[][];
	
	
	double cost;
	
	double costWithPenalty;
//	Utility utility;
	boolean isFeasible;
	boolean feasibilitySet;

	
	double loadViolation[][];
	double totalLoadViolation;

	//double totalRouteTime;
	
	double totalRouteTimeViolation;
	double distance = -1;
	
	ProblemInstance problemInstance;
	
	

	public Individual()
	{
		cost = -1;
		costWithPenalty = -1;
		feasibilitySet = false;
		isFeasible = false;	
	}
	
	
	public void initialise() 
	{
		// TODO Auto-generated method stub

		int i,j;
		
		for( i=0;i<problemInstance.periodCount;i++)
		{
			// initially every permutation is identity permutation
			for( j=0;j<problemInstance.customerCount;j++)
			{
				permutation[i][j] = j;
			}
		}
		
		// NOW INITIALISE WITH VALUES

		//initialize period assignment

		int freq,allocated,random,tmp;

		//Randomly allocate period to clients equal to their frequencies
		for(int client=0; client < problemInstance.customerCount; client++)
		{
			freq = problemInstance.frequencyAllocation[client];
			allocated=0;

			while(allocated!=freq)
			{
				random = Utility.randomIntInclusive(problemInstance.periodCount-1);
				
				if(periodAssignment[random][client]==false)
				{
					periodAssignment[random][client]=true;
					allocated++;
				}
			}
		}
		
		
		//initialize permutation map - KNUTH SHUFFLE
		for(int period=0; period < problemInstance.periodCount;period++)
		{
			//apply knuths shuffle
			for( i = problemInstance.customerCount -1 ;i>0 ;i-- )
			{
				j = Utility.randomIntInclusive(i);
				
				if(i==j)continue;

				tmp = permutation[period][i];
				permutation[period][i] = permutation[period][j];
				permutation[period][j] = tmp;
			}
		}
		
		//NEED TO GENERATE #vehicle-1 (not distinct - distinct) random numbers in increasing order from [0,#customer - 1]
		// DEVICE some faster and smarter algorithm

		// route for vehicle i is  [ routePartition[i-1]+1 , routePartition[i] ]
		// given that routePartition[i-1]+1 <= routePartition[i]

		//bool found;
		
		/*
		for(int period=0;period<problemInstance.periodCount;period++)
		{
			allocated = 0;
			while(allocated != problemInstance.vehicleCount-1)
			{
				random = Utility.randomIntInclusive(problemInstance.customerCount-1);
	
				routePartition[period][allocated]=random;
				sort(period,random,allocated);
				allocated++;
			}
			routePartition[period][problemInstance.vehicleCount-1] = problemInstance.customerCount-1;
		}
		*/
		

		int avgStepSize = problemInstance.customerCount / problemInstance.vehicleCount;
		int deviation = avgStepSize / 3;

		//problemInstance.out.println("Step Size : "+avgStepSize+" Deviation : "+deviation);
		
		for(int period=0;period<problemInstance.periodCount;period++)
		{
			allocated = 0;
					
			while(allocated != problemInstance.vehicleCount-1)
			{
				int minus  = Utility.randomIntInclusive(1);
				int base = avgStepSize * (allocated+1);
				random = Utility.randomIntInclusive(deviation);
				
				int partition = base;
				if(minus==1) partition = base - random;
				else partition = base + random;
				
				routePartition[period][allocated]=partition;
				sort(period,partition,allocated);
				allocated++;
			}
			routePartition[period][problemInstance.vehicleCount-1] = problemInstance.customerCount-1;
		}

		
		calculateCostAndPenalty();

	}
	

	public void initialise2() 
	{
		// TODO Auto-generated method stub

		int i,j;
		int coin;
		for( i=0;i<problemInstance.periodCount;i++)
		{
			// initially every permutation is identity permutation or reverse identity 
			coin = Utility.randomIntInclusive(1);
			if(coin==0)
			{
				for( j=0;j<problemInstance.customerCount;j++)
				{
					permutation[i][j] = j;
				}
			}
			else
			{
				for( j=0;j<problemInstance.customerCount;j++)
				{
					permutation[i][j] = problemInstance.customerCount-1-j;
				}
			}
		}
		
		// NOW INITIALISE WITH VALUES

		//initialize period assignment

		int freq,allocated,random,tmp;

		//Randomly allocate period to clients equal to their frequencies
		for(int client=0; client < problemInstance.customerCount; client++)
		{
			freq = problemInstance.frequencyAllocation[client];
			allocated=0;

			while(allocated!=freq)
			{
				random = Utility.randomIntInclusive(problemInstance.periodCount-1);
				
				if(periodAssignment[random][client]==false)
				{
					periodAssignment[random][client]=true;
					allocated++;
				}
			}
		}
		
		
		
		
		//initialize permutation map
		for(int period=0; period < problemInstance.periodCount;period++)
		{
			
			coin = Utility.randomIntInclusive(1);


			
			if(coin==0) // apply randomization
			{	
				int boundary = Utility.randomIntInclusive(problemInstance.customerCount-1);
				
				//problemInstance.out.println("Boundary : "+boundary);
				
				int coin2 = Utility.randomIntInclusive(1);
				int coin3;
				
				String st;
				//st = (coin2==0)?" Left to right":" right to left";
				//problemInstance.out.println("1st segment :"+st);
				
				if(coin2==0)//left to right for 1st segment
				{
					for(i=0;i<boundary;i++)
					{
						coin3 = Utility.randomIntInclusive(1);
						
						if(coin3==0)
						{
							tmp = permutation[period][i];
							permutation[period][i] = permutation[period][i+1];
							permutation[period][i+1] = tmp;
						}
					}
				}
				else //right to left
				{	
					for(i=boundary-1;i>=0;i--)
					{
						coin3 = Utility.randomIntInclusive(1);
						
						if(coin3==0)
						{
							tmp = permutation[period][i];
							permutation[period][i] = permutation[period][i+1];
							permutation[period][i+1] = tmp;
						}
					}
					
				}
				
				coin2 = Utility.randomIntInclusive(1);
				
				//st = (coin2==0)?" Left to right":" right to left";
				//problemInstance.out.println("2nt segment :"+st);
				
				if(coin2==0)//left to right for 2ndt segment
				{
					for(i=boundary;i<problemInstance.customerCount-1;i++)
					{
						coin3 = Utility.randomIntInclusive(1);
						
						if(coin3==0)
						{
							tmp = permutation[period][i];
							permutation[period][i] = permutation[period][i+1];
							permutation[period][i+1] = tmp;
						}
					}
				}
				else //right to left
				{	
					for(i=problemInstance.customerCount-2;i>=boundary;i--)
					{
						coin3 = Utility.randomIntInclusive(1);
						
						if(coin3==0)
						{
							tmp = permutation[period][i];
							permutation[period][i] = permutation[period][i+1];
							permutation[period][i+1] = tmp;
						}
					}
					
				}

				
			}
		
			else
			{
				//apply knuths shuffle
				for( i = problemInstance.customerCount -1 ;i>0 ;i-- )
				{
					j = Utility.randomIntInclusive(i);
					
					if(i==j)continue;
	
					tmp = permutation[period][i];
					permutation[period][i] = permutation[period][j];
					permutation[period][j] = tmp;
				}
			}
		}
		
		//NEED TO GENERATE #vehicle-1 (not distinct - distinct) random numbers in increasing order from [0,#customer - 1]
		// DEVICE some faster and smarter algorithm

		// route for vehicle i is  [ routePartition[i-1]+1 , routePartition[i] ]
		// given that routePartition[i-1]+1 <= routePartition[i]

		

		int avgStepSize = problemInstance.customerCount / problemInstance.vehicleCount;
		int deviation = avgStepSize / 3;

		//problemInstance.out.println("Step Size : "+avgStepSize+" Deviation : "+deviation);
		
		for(int period=0;period<problemInstance.periodCount;period++)
		{
			allocated = 0;
					
			while(allocated != problemInstance.vehicleCount-1)
			{
				int minus  = Utility.randomIntInclusive(1);
				int base = avgStepSize * (allocated+1);
				random = Utility.randomIntInclusive(deviation);
				
				int partition = base;
				if(minus==1) partition = base - random;
				else partition = base + random;
				
				routePartition[period][allocated]=partition;
				sort(period,partition,allocated);
				allocated++;
			}
			routePartition[period][problemInstance.vehicleCount-1] = problemInstance.customerCount-1;
		}

		
		calculateCostAndPenalty();

	}
	


	public void initialise3() 
	{
		// TODO Auto-generated method stub

		int i,j;
		int coin;
		for( i=0;i<problemInstance.periodCount;i++)
		{
			// initially every permutation is identity permutation or reverse identity 
			coin = Utility.randomIntInclusive(1);
			if(coin==0)
			{
				for( j=0;j<problemInstance.customerCount;j++)
				{
					permutation[i][j] = j;
				}
			}
			else
			{
				for( j=0;j<problemInstance.customerCount;j++)
				{
					permutation[i][j] = problemInstance.customerCount-1-j;
				}
			}
		}
		
		// NOW INITIALISE WITH VALUES

		//initialize period assignment

		int freq,allocated,random,tmp;

		//Randomly allocate period to clients equal to their frequencies
		for(int client=0; client < problemInstance.customerCount; client++)
		{
			freq = problemInstance.frequencyAllocation[client];
			allocated=0;

			while(allocated!=freq)
			{
				random = Utility.randomIntInclusive(problemInstance.periodCount-1);
				
				if(periodAssignment[random][client]==false)
				{
					periodAssignment[random][client]=true;
					allocated++;
				}
			}
		}
		
		
		
		
		//initialize permutation map
		//apply knuths shuffle for period 0
		for( i = problemInstance.customerCount -1 ;i>0 ;i-- )
		{
			j = Utility.randomIntInclusive(i);
			
			if(i==j)continue;

			tmp = permutation[0][i];
			permutation[0][i] = permutation[0][j];
			permutation[0][j] = tmp;
		}
		
		for(int period=1; period < problemInstance.periodCount;period++)
		{
			
			coin = Utility.randomIntInclusive(1);

				
			int coin2 = Utility.randomIntInclusive(1);
			int coin3;
			
			String st;
			
			for( i = 0 ;i <problemInstance.customerCount ;i++ )
			{
				permutation[period][i] = permutation[0][i];
			}
			
			
			
			
			//st = (coin2==0)?" Left to right":" right to left";
			//problemInstance.out.println("1st segment :"+st);
			/*
			if(coin2==0)//left to right
			{
				for(i=0;i<problemInstance.customerCount-1;i++)
				{
					coin3 = Utility.randomIntInclusive(7);
					
					if(coin3==0)
					{
						tmp = permutation[period][i];
						permutation[period][i] = permutation[period][i+1];
						permutation[period][i+1] = tmp;
					}
				}
			}
			else //right to left
			{	
				for(i=problemInstance.customerCount-2;i>=0;i--)
				{
					coin3 = Utility.randomIntInclusive(7);
					
					if(coin3==0)
					{
						tmp = permutation[period][i];
						permutation[period][i] = permutation[period][i+1];
						permutation[period][i+1] = tmp;
					}
				}
				
			}
			
			*/
					
		
		}
		
		//NEED TO GENERATE #vehicle-1 (not distinct - distinct) random numbers in increasing order from [0,#customer - 1]
		// DEVICE some faster and smarter algorithm

		// route for vehicle i is  [ routePartition[i-1]+1 , routePartition[i] ]
		// given that routePartition[i-1]+1 <= routePartition[i]

		

		int avgStepSize = problemInstance.customerCount / problemInstance.vehicleCount;
		int deviation = avgStepSize / 3;

		//problemInstance.out.println("Step Size : "+avgStepSize+" Deviation : "+deviation);
		
		for(int period=0;period<problemInstance.periodCount;period++)
		{
			allocated = 0;
					
			while(allocated != problemInstance.vehicleCount-1)
			{
				int minus  = Utility.randomIntInclusive(1);
				int base = avgStepSize * (allocated+1);
				random = Utility.randomIntInclusive(deviation);
				
				int partition = base;
				if(minus==1) partition = base - random;
				else partition = base + random;
				
				routePartition[period][allocated]=partition;
				sort(period,partition,allocated);
				allocated++;
			}
			routePartition[period][problemInstance.vehicleCount-1] = problemInstance.customerCount-1;
		}

		
		calculateCostAndPenalty();

	}
	

	public Individual(ProblemInstance problemInstance)
	{
		this.problemInstance = problemInstance;
		
		// ALLOCATING periodCount * customerCount Matrix for Period Assignment
		periodAssignment = new boolean[problemInstance.periodCount][problemInstance.customerCount];
		
		//ALLOCATING permutation map matrix -> period * customer
		permutation = new int[problemInstance.periodCount][problemInstance.customerCount];
		
		
		//allocating routeAllocation
		routePartition = new int[problemInstance.periodCount][problemInstance.vehicleCount];

		loadViolation = new double[problemInstance.periodCount][problemInstance.vehicleCount];
	}
	
	
	/** Makes a copy cat individual.Copy Constructor.
	 * 
		* copies problem instance, periodAssignment, permutation, routePartition.
		 * <br>
		 * @param original
		 */
	public Individual(Individual original)
	{
	    int i,j;
		problemInstance = original.problemInstance;

		periodAssignment = new boolean[problemInstance.periodCount][problemInstance.customerCount];
		for( i=0;i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.customerCount;j++)
			{
				periodAssignment[i][j] = original.periodAssignment[i][j];
			}
		}



		permutation = new int[problemInstance.periodCount][problemInstance.customerCount];
		for( i=0;i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.customerCount;j++)
			{
				permutation[i][j] = original.permutation[i][j];
			}
		}


		routePartition = new int[problemInstance.periodCount][problemInstance.vehicleCount];
		for( i=0;i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.vehicleCount;j++)
			{
				routePartition[i][j] = original.routePartition[i][j];
			}
		}
		

		cost = original.cost;
		costWithPenalty = original.costWithPenalty;

		//allocate demanViolationMatrix

        loadViolation = new double[problemInstance.periodCount][problemInstance.vehicleCount];

	}
	
	public void copyIndividual(Individual original)
	{
		int i,j;
		problemInstance = original.problemInstance;

		for( i=0;i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.customerCount;j++)
			{
				periodAssignment[i][j] = original.periodAssignment[i][j];
			}
		}

		for( i=0;i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.customerCount;j++)
			{
				permutation[i][j] = original.permutation[i][j];
			}
		}

		for( i=0;i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.vehicleCount;j++)
			{
				routePartition[i][j] = original.routePartition[i][j];
			}
		}
		
		cost = original.cost;
		costWithPenalty = original.costWithPenalty;

	}

	/**
	 * Calculates cost and penalty of every individual
	 * For route time violation travelling times are not considered
	 * route time violation = maximum duration of a route - Sum of service time
	 */
	void calculateCostAndPenalty()
	{
		double tempCost = 0;

		totalLoadViolation = 0;
		totalRouteTimeViolation = 0;
        
		//double temlLoad;
		for(int i=0;i<problemInstance.periodCount;i++)
		{
			for(int j=0;j<problemInstance.vehicleCount;j++)
			{
				tempCost += calculateCost(i,j);
                //calculate the total load violation
                //Add only when actually the load is violated i.e. violation is positive
                if(loadViolation[i][j]>0) totalLoadViolation += loadViolation[i][j];
			}
		}
		
		cost = tempCost;

		if(totalLoadViolation>0  || totalRouteTimeViolation > 0)
		{
			isFeasible = false;
		}
		else isFeasible = true;

		feasibilitySet = true;
		
	} 


	//calcuate fitness for each period for each vehicle
	// route for vehicle i is  [ routePartition[i-1]+1 , routePartition[i] ]
	// given that routePartition[i-1]+1 <= routePartition[i]
	//ignoring travelling time for now - for cordeau MDVRP
	// only service time is considered


	double calculateCost(int period,int vehicle)
	{
		int assignedDepot;
		assignedDepot = problemInstance.depotAllocation[vehicle];
		double costForPV = 0;
		int start,end; // marks the first and last position of corresponding route for the array permutation

		if(vehicle == 0) start = 0;
		else start = routePartition[period][vehicle-1]+1;

		end = routePartition[period][vehicle];

		if(end<start) return 0;

		int activeStart=-1,activeEnd=-1,previous=-1,clientNode;

        double clientDemand=0;
		double totalRouteTime=0;
		for(int i=start;i<=end;i++)
		{
			clientNode = permutation[period][i];
			if(periodAssignment[period][clientNode]==false) continue;

			if(activeStart == -1) activeStart = clientNode;
			activeEnd = clientNode;

			totalRouteTime += problemInstance.serviceTime[clientNode]; //adding service time for that node

            //Caluculate total client demand for corresponding period,vehicle
            clientDemand += problemInstance.demand[clientNode];

			if(previous == -1)
			{
				previous = clientNode;
				continue;
			}

			costForPV +=	problemInstance.costMatrix[previous+problemInstance.depotCount][clientNode+problemInstance.depotCount];
			
			//ignoring travelling time for now - for cordeau MDVRP
			//totalRouteTime += problemInstance.travellingTimeMatrix[previous+problemInstance.depotCount][clientNode+problemInstance.depotCount];
			
			previous = clientNode;

		}

        if(activeStart!=-1 && activeEnd != -1)
        {
            costForPV += problemInstance.costMatrix[assignedDepot][activeStart+problemInstance.depotCount];
            costForPV += problemInstance.costMatrix[activeEnd+problemInstance.depotCount][assignedDepot];

//			totalRouteTime += problemInstance.travellingTimeMatrix[assignedDepot][activeStart+problemInstance.depotCount];
//            totalRouteTime += problemInstance.travellingTimeMatrix[activeEnd+problemInstance.depotCount][assignedDepot];
        }
        loadViolation[period][vehicle] = clientDemand - problemInstance.loadCapacity[vehicle];

		double routeTimeViolation = totalRouteTime - problemInstance.timeConstraintsOfVehicles[period][vehicle] ;
		if(routeTimeViolation>0) totalRouteTimeViolation += routeTimeViolation;

		return costForPV;

	}
	

	// sorts the array routePartition in increasing order
	// input -> routePartition array [0, upperbound ], with,n inserted at the last in the array
	// output -> sorted array [0, upperbound]
	void sort(int period,int n,int upperbound)
	{
		int tmp;
		for(int v = upperbound-1;v>=0;v--)
		{
			if(routePartition[period][v]>routePartition[period][v+1])
			{
				tmp = routePartition[period][v];
				routePartition[period][v] = routePartition[period][v+1];
				routePartition[period][v+1] = tmp;
			}
			else
				break;
		}
	}
	
	void print()
	{
		//if(problemInstance == null) System.out.println("OUT IS NULL");
		PrintWriter out = this.problemInstance.getPrintWriter();
		int i,j;
		
		out.println("PERIOD ASSIGMENT : ");
		for( i=0;i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.customerCount;j++)
			{
				if(periodAssignment[i][j])	out.print("1 ");
				else out.print("0 ");
				
			}
			out.println();
		}

		out.print("Permutation : \n");
		for( i=0; i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.customerCount;j++)
			{
				out.print(permutation[i][j]+" ");
			}
			out.println();
		}

		out.print("Route partition : \n");
		
		for(i=0;i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.vehicleCount;j++)
				out.print(routePartition[i][j] +" ");
			out.println();
		}

        // print load violation

		out.print("LOAD VIOLATION MATRIX : \n");
        for( i=0;i<problemInstance.periodCount;i++)
        {
            for( j=0;j<problemInstance.vehicleCount;j++)
            {
            	out.print(loadViolation[i][j]+" ");
            }
            out.println();
        }

        out.println("Is Feasible : "+isFeasible);
        out.println("Total Load Violation : "+totalLoadViolation);        
        out.println("Total route time violation : "+totalRouteTimeViolation);		
		out.println("Cost : " + cost);
		out.println("Cost with penalty : "+costWithPenalty);
		out.println();
		
	}
	
	void miniPrint()
	{
		PrintWriter out = this.problemInstance.getPrintWriter();
		int i,j;
		
		out.println("PERIOD ASSIGMENT : ");
		for( i=0;i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.customerCount;j++)
			{
				if(periodAssignment[i][j])	out.print("1 ");
				else out.print("0 ");
				
			}
			out.println();
		}
		
		/*

		out.print("Permutation : \n");
		for( i=0; i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.customerCount;j++)
			{
				out.print(permutation[i][j]+" ");
			}
			out.println();
		}

		out.print("Route partition : \n");
		
		for(i=0;i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.vehicleCount;j++)
				out.print(routePartition[i][j] +" ");
			out.println();
		}
		

        // print load violation
        out.println("Is Feasible : "+isFeasible);
        out.println("Total Load Violation : "+totalLoadViolation);        
        out.println("Total route time violation : "+totalRouteTimeViolation);		
		out.println("Cost : " + cost);
		out.println("Cost with penalty : "+costWithPenalty);
		out.println("\n");
		*/
	}
	
	/*
	// swaps even if neither of the customers get visited that day
	//  updates cost and penalty
	void mutatePermutationUC(int period)
	{
		int first = Utility.randomIntInclusive(problemInstance.customerCount-1);

		int second;
		int count=0;
		do
		{
			second = Utility.randomIntInclusive(problemInstance.customerCount-1);
			count++;
			if(count==problemInstance.customerCount)break;
		}
		while(second == first);

		int temp = permutation[period][first];
		permutation[period][first] = permutation[period][second];
		permutation[period][second] = temp;

		calculateCostAndPenalty();
		// FITNESS CAN BE UPDATED HERE
	}
	
	
		// updates cost and penalty
	void mutatePermutationWithinSingleRouteUC()
	{
		boolean success = false;
		do
		{
			int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
			int vehicle = Utility.randomIntInclusive(problemInstance.vehicleCount-1);
			success = mutatePermutationWithinSingleRoute(period, vehicle);
		}while(success==false);
		calculateCostAndPenalty();
	}
	
	
		// updates cost and penalty
	void mutatePermutationOfDifferentRouteUC()
	{	
		if(problemInstance.vehicleCount<2)return;
		
		boolean success = false;
		do
		{
			int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
			int vehicle1 = Utility.randomIntInclusive(problemInstance.vehicleCount-1);
			int vehicle2 = Utility.randomIntInclusive(problemInstance.vehicleCount-1);
			if(vehicle1==vehicle2)continue;
			success = mutatePermutationOfDifferentRoute(period, vehicle1,vehicle2);
		}while(success==false);
		
		calculateCostAndPenalty();
	}

	// updates cost and penalty
	void mutateRoutePartitionUC()
	{
		//nothing to do if only one vehicle
		if(problemInstance.vehicleCount == 1) return ;
		
		int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
		mutateRoutePartition(period);
		
		calculateCostAndPenalty();
	}

	// updates cost + penalty
	// if sobgula client er frequency = period hoy tahole, period assignment mutation er kono effect nai
	void mutatePeriodAssignmentUC()
	{
		boolean success;
		int clientNo;
		int total = problemInstance.customerCount;
		do
		{
			clientNo = Utility.randomIntInclusive(problemInstance.customerCount-1);
			success = mutatePeriodAssignment(clientNo);
			total--;
		}while(success==false && total>0);
		
		calculateCostAndPenalty();
	}
	
	*/
	/** swaps even if neither of the customers get visited that day
	<br> do NOT updates cost and penalty
	*/

	
	
	void mutatePermutationWithInsertion()
	{
		int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
		boolean success;
		do
		{
			success = mutatePermutationWithInsertion(period);
		}while(success==false);
	}
	
	private boolean mutatePermutationWithInsertion(int period)
	{
		int left = Utility.randomIntInclusive(0,problemInstance.customerCount-1);
		int right = Utility.randomIntInclusive(0,problemInstance.customerCount-1);
		
		if(left==right) return false;
		if(left > right)
		{
			int tmp = left;
			left = right;
			right = tmp;
		}
		
		int clockwise  =  Utility.randomIntInclusive(1);
		
		//for(int j =0;j<problemInstance.customerCount;j++)
		//	System.out.print(" "+permutation[period][j]);
		//System.out.println("");
		
		//clockwise - left goes right , all the rest go left
		if(clockwise==1)
		{
			int saved = permutation[period][left];
			for(int i=left+1;i<=right;i++)
			{
				permutation[period][i-1]=permutation[period][i];
			}
			permutation[period][right] = saved;
			//System.out.println("period : "+period+" "+left+" -> "+right+" "+" all go left");
		}
		else //amticlockwise - right goes left, all other go right
		{
			int saved = permutation[period][right];
			
			for(int i = right-1;i>=left;i--)
			{
				permutation[period][i+1] = permutation[period][i];
			}
			permutation[period][left]=saved;
			//System.out.println("period : "+period+" "+left+" -> "+right+" "+" all go right");

		}
		
		//for(int j =0;j<problemInstance.customerCount;j++)
		//	System.out.print(" "+permutation[period][j]);
		//System.out.println("\n\n");
		
		return true;
	}
	
	
	void mutatePermutationWithReversal()
	{
		int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
		boolean success;
		do
		{
			success = mutatePermutationWithReversal(period);
		}while(success==false);
	}
	
	private boolean mutatePermutationWithReversal(int period)
	{
		int left = Utility.randomIntInclusive(0,problemInstance.customerCount-1);
		int right = Utility.randomIntInclusive(0,problemInstance.customerCount-1);
		
		if(left==right) return false;
		if(left > right)
		{
			int tmp = left;
			left = right;
			right = tmp;
		}
		
		int rotations = Utility.randomIntInclusive(1,((right-left+1)/2));
		
		int rotateLeft  =  Utility.randomIntInclusive(1);
		
		/*
		System.out.println("APPLIED :D :D :D");
		String rt = (rotateLeft==1)?"Left rotate":"right rotate";
		System.out.println("period : "+period+" "+rt+" [ "+left+" , "+right+" ] "+" rotations :  "+rotations+"\n\n");
		for(int j =0;j<problemInstance.customerCount;j++)
		{	
			if(j==left)System.out.print(" |");
			System.out.print(" "+permutation[period][j]);
			if(j==right)System.out.print(" |");
		}
		System.out.println("");
		
		*/
		for(int r=0;r<rotations;r++)
		{	
			//left rotate - left goes right , all the rest go left
			if(rotateLeft==1)
			{
				int saved = permutation[period][left];
				for(int i=left+1;i<=right;i++)
				{
					permutation[period][i-1]=permutation[period][i];
				}
				permutation[period][right] = saved;
				//System.out.println("period : "+period+" "+left+" -> "+right+" "+" all go left");
			}
			else //amticlockwise - right goes left, all other go right
			{
				int saved = permutation[period][right];
				
				for(int i = right-1;i>=left;i--)
				{
					permutation[period][i+1] = permutation[period][i];
				}
				permutation[period][left]=saved;
				//System.out.println("period : "+period+" "+left+" -> "+right+" "+" all go right");
	
			}
		}
		
/*
		for(int j =0;j<problemInstance.customerCount;j++)
		{	
			if(j==left)System.out.print(" |");
			System.out.print(" "+permutation[period][j]);
			if(j==right)System.out.print(" |");
		}
		System.out.println("\n\n");
		*/
		return true;
	}
	
	void mutatePermutationWithRotation()
	{
		int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
		boolean success;
		do
		{
			success = mutatePermutationWithRotation(period);
		}while(success==false);
	
	}
	
	
	private boolean mutatePermutationWithRotation(int period)
	{
		int left = Utility.randomIntInclusive(0,problemInstance.customerCount-1);
		int right = Utility.randomIntInclusive(0,problemInstance.customerCount-1);
		
		if(left==right) return false;
		if(left > right)
		{
			int tmp = left;
			left = right;
			right = tmp;
		}
		
		int rotations = Utility.randomIntInclusive(1,((right-left+1)/2));
		
		int rotateLeft  =  Utility.randomIntInclusive(1);
		
		/*
		System.out.println("APPLIED :D :D :D");
		String rt = (rotateLeft==1)?"Left rotate":"right rotate";
		System.out.println("period : "+period+" "+rt+" [ "+left+" , "+right+" ] "+" rotations :  "+rotations+"\n\n");
		for(int j =0;j<problemInstance.customerCount;j++)
		{	
			if(j==left)System.out.print(" |");
			System.out.print(" "+permutation[period][j]);
			if(j==right)System.out.print(" |");
		}
		System.out.println("");
		
		*/
		for(int r=0;r<rotations;r++)
		{	
			//left rotate - left goes right , all the rest go left
			if(rotateLeft==1)
			{
				int saved = permutation[period][left];
				for(int i=left+1;i<=right;i++)
				{
					permutation[period][i-1]=permutation[period][i];
				}
				permutation[period][right] = saved;
				//System.out.println("period : "+period+" "+left+" -> "+right+" "+" all go left");
			}
			else //amticlockwise - right goes left, all other go right
			{
				int saved = permutation[period][right];
				
				for(int i = right-1;i>=left;i--)
				{
					permutation[period][i+1] = permutation[period][i];
				}
				permutation[period][left]=saved;
				//System.out.println("period : "+period+" "+left+" -> "+right+" "+" all go right");
	
			}
		}
		
/*
		for(int j =0;j<problemInstance.customerCount;j++)
		{	
			if(j==left)System.out.print(" |");
			System.out.print(" "+permutation[period][j]);
			if(j==right)System.out.print(" |");
		}
		System.out.println("\n\n");
		*/
		return true;
	}

	void mutatePermutationWithAdjacentSwap()
	{
		int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
		boolean success;
		do
		{
			success = mutatePermutationWithAdjacentSwap(period);
		}while(success==false);
	}

	
	private boolean mutatePermutationWithAdjacentSwap(int period)
	{
		int left = Utility.randomIntInclusive(0,problemInstance.customerCount-1);
		int right = Utility.randomIntInclusive(0,problemInstance.customerCount-1);
		
		if(left==right) return false;
		if(left > right)
		{
			int tmp = left;
			left = right;
			right = tmp;
		}
				
		int direction  =  Utility.randomIntInclusive(1);
		
		/*
		System.out.println("APPLIED :D :D :D");
		String rt = (direction==1)?"Left ":"right ";
		System.out.println("period : "+period+" "+rt+" [ "+left+" , "+right+" ] "+"  \n\n");
		for(int j =0;j<problemInstance.customerCount;j++)
		{	
			if(j==left)System.out.print(" |");
			System.out.print(" "+permutation[period][j]);
			if(j==right)System.out.print(" |");
		}
		System.out.println("");
		
		*/
		
		int coin ;	
		//from left to right 
		if(direction==1)
		{
			int saved;
			for(int i=left;i<right;i++)
			{
				coin = Utility.randomIntInclusive(1);
				if(coin==0)
				{
						saved = permutation[period][i];
						permutation[period][i]=permutation[period][i+1];
						permutation[period][i+1] = saved;
				}
			}
			
		}
		else // right to left
		{
			for(int i=right-1;i>=left;i--)
			{
				int saved;
				coin = Utility.randomIntInclusive(1);
				if(coin==0)
				{
						saved = permutation[period][i];
						permutation[period][i]=permutation[period][i+1];
						permutation[period][i+1] = saved;
				}
			}
			
		}
	
		
/*
		for(int j =0;j<problemInstance.customerCount;j++)
		{	
			if(j==left)System.out.print(" |");
			System.out.print(" "+permutation[period][j]);
			if(j==right)System.out.print(" |");
		}
		System.out.println("\n\n");
		*/
		return true;
	}


	void mutatePermutationBySwappingAnyTwo()
	{
		int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
		mutatePermutationBySwappingAnyTwo(period);
	}
	
	void mutatePermutationMultipleTimesBySwappingAnyTwo(int count)
	{
		for(int i=0; i<count ;i++)
		{
			int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
			mutatePermutationBySwappingAnyTwo(period);
		}
	}
	
	private void mutatePermutationBySwappingAnyTwo(int period)
	{
		int first = Utility.randomIntInclusive(problemInstance.customerCount-1);

		int second;
		int count=0;
		do
		{
			second = Utility.randomIntInclusive(problemInstance.customerCount-1);
			count++;
			if(count==problemInstance.customerCount)break;
		}
		while(second == first);

		int temp = permutation[period][first];
		permutation[period][first] = permutation[period][second];
		permutation[period][second] = temp;
	}
	
	
	//DO NOT updates cost and penalty
	
	void mutatePermutationWithRotationWithinSingleRoute(int count)
	{
		boolean success = false;
		for(int i=0;i<count;i++)
		{
			do
			{
				int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
				int vehicle = Utility.randomIntInclusive(problemInstance.vehicleCount-1);
				success = mutatePermutationWithRotationWithinSingleRoute(period, vehicle);
			}while(success==false);
		}
	}
	
	private boolean mutatePermutationWithRotationWithinSingleRoute(int period,int vehicle)
	{
		
		int left,right;
		
		if(vehicle == 0) left = 0;
		else left = routePartition[period][vehicle-1]+1;

		right = routePartition[period][vehicle];

		if(right<=left) return false;
		
		
		int rotations = Utility.randomIntInclusive(1,((right-left+1)/2));
		
		int rotateLeft  =  Utility.randomIntInclusive(1);
		
		/*
		System.out.println("APPLIED :D :D :D");
		String rt = (rotateLeft==1)?" Left rotate":" Right rotate";
		System.out.println("period : "+period+" "+" vehicle "+vehicle+rt+" [ "+left+" , "+right+" ] "+" rotations :  "+rotations+"\n\n");
		for(int j =0;j<problemInstance.customerCount;j++)
		{	
			if(j==left)System.out.print(" |");
			System.out.print(" "+permutation[period][j]);
			if(j==right)System.out.print(" |");
		}
		System.out.println("");
		*/
		
		for(int r=0;r<rotations;r++)
		{	
			//left rotate - left goes right , all the rest go left
			if(rotateLeft==1)
			{
				int saved = permutation[period][left];
				for(int i=left+1;i<=right;i++)
				{
					permutation[period][i-1]=permutation[period][i];
				}
				permutation[period][right] = saved;
				//System.out.println("period : "+period+" "+left+" -> "+right+" "+" all go left");
			}
			else //amticlockwise - right goes left, all other go right
			{
				int saved = permutation[period][right];
				
				for(int i = right-1;i>=left;i--)
				{
					permutation[period][i+1] = permutation[period][i];
				}
				permutation[period][left]=saved;
				//System.out.println("period : "+period+" "+left+" -> "+right+" "+" all go right");
	
			}
		}
		
/*
		for(int j =0;j<problemInstance.customerCount;j++)
		{	
			if(j==left)System.out.print(" |");
			System.out.print(" "+permutation[period][j]);
			if(j==right)System.out.print(" |");
		}
		System.out.println("\n\n");
		*/
		return true;
	}

	void mutatePermutationWithinSingleRouteBySwapping()
	{
		boolean success = false;
		do
		{
			int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
			int vehicle = Utility.randomIntInclusive(problemInstance.vehicleCount-1);
			success = mutatePermutationWithinSingleRouteBySwapping(period, vehicle);
		}while(success==false);
	}

	
	//returns true if permutation successful
	private boolean mutatePermutationWithinSingleRouteBySwapping(int period,int vehicle)
	{
		int start,end;
		
		if(vehicle == 0) start = 0;
		else start = routePartition[period][vehicle-1]+1;

		end = routePartition[period][vehicle];

		if(end<=start) return false;
		
		int first = Utility.randomIntInclusive(start,end);

		int second;
		do
		{
			second = Utility.randomIntInclusive(start,end);
		}
		while(second == first);

		int temp = permutation[period][first];
		permutation[period][first] = permutation[period][second];
		permutation[period][second] = temp;

		
		return true;
		
	}
	
	
	/** DO NOT updates cost and penalty
	*/
	void mutatePermutationOfDifferentRouteBySwapping()
	{	
		if(problemInstance.vehicleCount<2)return;
		
		boolean success = false;
		do
		{
			int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
			int vehicle1 = Utility.randomIntInclusive(problemInstance.vehicleCount-1);
			int vehicle2 = Utility.randomIntInclusive(problemInstance.vehicleCount-1);
			if(vehicle1==vehicle2)continue;
			success = mutatePermutationOfDifferentRouteBySwapping(period, vehicle1,vehicle2);
		}while(success==false);
	
	}
	

	//returns if permutation successful
	private boolean mutatePermutationOfDifferentRouteBySwapping(int period,int vehicle1,int vehicle2)
	{
		int start1,end1,start2,end2;
		
		if(vehicle1 == 0) start1 = 0;
		else start1 = routePartition[period][vehicle1-1]+1;

		end1 = routePartition[period][vehicle1];

		if(end1<start1) return false;
		
		
		if(vehicle2 == 0) start2 = 0;
		else start2 = routePartition[period][vehicle2-1]+1;

		end2 = routePartition[period][vehicle2];

		if(end2<start2) return false;
		
		int first = Utility.randomIntInclusive(start1,end1);
		int second = Utility.randomIntInclusive(start2,end2);
		

		
		int temp = permutation[period][first];
		permutation[period][first] = permutation[period][second];
		permutation[period][second] = temp;

		
		return true;
		
	}

	

	/** do not updates cost and penalty
	*/
	void mutateRoutePartitionWithRandomStepSize()
	{
		//nothing to do if only one vehicle
		if(problemInstance.vehicleCount == 1) return ;
		
		int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
		mutateRoutePartitionWithRandomStepSize(period);
		
	}
	
	//moves some red line / route partition line
    //
	//NEW ONE 
	private void mutateRoutePartitionWithRandomStepSize(int period)
	{
		int distance,increment;

		while(true)
		{
			int seperatorIndex = Utility.randomIntInclusive(problemInstance.vehicleCount-2);
			int dir = Utility.randomIntInclusive(1); // 0-> left , 1-> right
			if(dir==0)//move the seperator left
			{
				if(seperatorIndex==0) distance = routePartition[period][0] ;
				else distance = routePartition[period][seperatorIndex] - routePartition[period][seperatorIndex-1];
				if(distance==0)continue;
				
				int max  = distance/4;						
				increment = Utility.randomIntInclusive(max);
				
				if(increment<1)increment=1;				
				routePartition[period][seperatorIndex] -= increment;
				return;
			}
			else	//move the seperator right
			{
				distance = routePartition[period][seperatorIndex+1] - routePartition[period][seperatorIndex] ;
				if(distance==0)continue;
				int max  = distance/4;						
				increment = Utility.randomIntInclusive(max);
				if(increment<1)increment=1;
				routePartition[period][seperatorIndex] += increment;
				return;
			}
		}

	}
	

	
	
	/** do not updates cost and penalty
	*/
	void mutateRoutePartition()
	{
		//nothing to do if only one vehicle
		if(problemInstance.vehicleCount == 1) return ;
		
		int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
		mutateRoutePartition(period);
		

	}
	

/*
	//moves some red line
	//no effect if only one vehicle
	private void mutateRoutePartition(int period)
	{
		//nothing to do if only one vehicle
		if(problemInstance.vehicleCount == 1) return ;

		//pick a red line/seperator
		//generate random number in [0,vehicleCount-1)

		int distance,increment;

		while(true)
		{
			int seperatorIndex = Utility.randomIntInclusive(problemInstance.vehicleCount-2);
			int dir = Utility.randomIntInclusive(1); // 0-> left , 1-> right
			if(dir==0)//move the seperator left
			{
				if(seperatorIndex==0) distance = routePartition[period][0] ;
				else distance = routePartition[period][seperatorIndex] - routePartition[period][seperatorIndex-1];
				// if the line can not merge with the previous one ,
				// difference = routePartition[seperatorIndex] - 1 - routePartition[seperatorIndex-1]

				// increment should be in [1,distance]
				if(distance==0)continue;
				increment = 1 + Utility.randomIntInclusive(distance-1);
				routePartition[period][seperatorIndex] -= increment;
				return;
			}
			else	//move the seperator right
			{
				distance = routePartition[period][seperatorIndex+1] - routePartition[period][seperatorIndex] ;
				if(distance==0)continue;
				increment = 1 + Utility.randomIntInclusive(distance-1);
				routePartition[period][seperatorIndex] += increment;
				return;
			}
		}

	}

*/
	
	//moves some red line / route partition line
    // only single step left or right
	//NEW ONE 
	private void mutateRoutePartition(int period)
	{
		//nothing to do if only one vehicle
		if(problemInstance.vehicleCount == 1) return ;

		//pick a red line/seperator
		//generate random number in [0,vehicleCount-1)

		int distance,increment;

		while(true)
		{
			int seperatorIndex = Utility.randomIntInclusive(problemInstance.vehicleCount-2);
			int dir = Utility.randomIntInclusive(1); // 0-> left , 1-> right
			if(dir==0)//move the seperator left
			{
				if(seperatorIndex==0) distance = routePartition[period][0] ;
				else distance = routePartition[period][seperatorIndex] - routePartition[period][seperatorIndex-1];
				// if the line can not merge with the previous one ,
				// difference = routePartition[seperatorIndex] - 1 - routePartition[seperatorIndex-1]

				// increment should be in [1,distance]
				if(distance==0)continue;
				increment = 1;
				routePartition[period][seperatorIndex] -= increment;
				return;
			}
			else	//move the seperator right
			{
				distance = routePartition[period][seperatorIndex+1] - routePartition[period][seperatorIndex] ;
				if(distance==0)continue;
				increment = 1 ;
				routePartition[period][seperatorIndex] += increment;
				return;
			}
		}

	}
	

	/** do not updates cost + penalty
	// if sobgula client er frequency = period hoy tahole, period assignment mutation er kono effect nai
	*/
	void mutatePeriodAssignment()
	{
		boolean success;
		int clientNo;
		int total = problemInstance.customerCount;
		do
		{
			clientNo = Utility.randomIntInclusive(problemInstance.customerCount-1);
			success = mutatePeriodAssignment(clientNo);
			total--;
		}while(success==false && total>0);
	
	}
	
	//returns 0 if it couldnt mutate as period == freq
	private boolean mutatePeriodAssignment(int clientNo)
	{
		//no way to mutate per. ass. as freq. == period
		if(problemInstance.frequencyAllocation[clientNo] == problemInstance.periodCount) return false;
		if(problemInstance.frequencyAllocation[clientNo] == 0) return false;		

		int previouslyAssigned; // one period that was assigned to client
		do
		{
			previouslyAssigned = Utility.randomIntInclusive(problemInstance.periodCount-1);
		} while (periodAssignment[previouslyAssigned][clientNo]==false);

		int previouslyUnassigned;//one period that was NOT assigned to client
		do
		{
			previouslyUnassigned = Utility.randomIntInclusive(problemInstance.periodCount-1);
		} while (periodAssignment[previouslyUnassigned][clientNo]==true);

		periodAssignment[previouslyAssigned][clientNo] = false;
		periodAssignment[previouslyUnassigned][clientNo]= true;


		return true;
	}
	
	
	/*
	void mutatePermutationWithSingleGreedySwap()
	{
		boolean success = false;
		int retry = 10;
		do
		{
			retry--;
			int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
			int vehicle = Utility.randomIntInclusive(problemInstance.vehicleCount-1);
			success = mutatePermutationWithSingleGreedySwap(period, vehicle);
		}while(success==false && retry>0);
	}

	
	
	private boolean mutatePermutationWithSingleGreedySwap(int period,int vehicle)
	{
		int start,end;
		
		if(vehicle == 0) start = 0;
		else start = routePartition[period][vehicle-1]+1;

		end = routePartition[period][vehicle];

		if(end<=start) return false;
		
		int first = Utility.randomIntInclusive(start,end-1);
		first = getNextClientIndex(period, vehicle, first);
		
		
		int second=first+1;
		second = getNextClientIndex(period, vehicle, second);
		
		if( first == -1 || second == -1 )return false;
		
		if(first==-100 || second==-100) 
		{
			System.out.println("NEVER SHOULD HAVE BEEN PRINTED !!!!!!!! in mutatePermutationWithSingleGreedySwap in Individual ");
			return false;
		}
		
		
		int prevIndex = getPreviousClientIndex(period, vehicle, first);
		int nextIndex = getNextClientIndex(period, vehicle, second );
		
				
		int b = permutation[period][first];
		int c = permutation[period][second];
		
		double dis1,dis1Prime,dis2,dos2Prime;
		
		
		

		return true;
		
	}
	
	*/

	/**
	 * returns the next client (inclusive first) in that vehicle's route if there is one, else return -1
	 * <br/> 
	 * @param period
	 * @param vehicle
	 * @param index index of the client
	 * @return
	 * nodeIndex of the next Node in route
	 * <br/>-100 if parameter node is out of range, not part of this vehicles route
	 * <br/>-1 if this is the last presentNode
	 */
	private int getNextClientIndex(int period, int vehicle, int index)
	{
		int end = routePartition[period][vehicle];
		
		int start;
		if(vehicle == 0) start = 0;
		else start = routePartition[period][vehicle-1]+1;

		if(index<start || index>end)
		{
			System.out.println("PROBLEM IN getNextClientIndex function in Individual");
			return -100;
		}	
		while(index<=end)
		{
			if(periodAssignment[period][permutation[period][index]] == true)
			{
				return index;
			}
			index++;
		}
		return -1;
	}
	
	/**
	 * returns the previous client (inclusive first) in that vehicle's route if there is one
	 * <br/> 
	 * @param period
	 * @param vehicle
	 * @param index
	 * @return
	 * nodeIndex of the previous Node in route
	 * -100 if parameter node is out of range, not part of this vehicles route
	 * -1 if this is the first presentNode
	 */
	private int getPreviousClientIndex(int period, int vehicle, int index)
	{
		int start;
		
		if(vehicle == 0) start = 0;
		else start = routePartition[period][vehicle-1]+1;

		int end = routePartition[period][vehicle];
		
		if(index<start || index>end)
		{
			System.out.println("PROBLEM IN getPreviousClientIndex function in Individual");
			return -100;
		}	
		while(index>=start)
		{
			if(periodAssignment[period][permutation[period][index]] == true)
			{
				return index;
			}
			index--;
		}		
		return -1;
	}
	
	
	/*
	 * 
	 * void mutatePermutationWithinSingleGreedySwap()
	{
		boolean success = false;
		int retry = 10;
		do
		{
			retry--;
			int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
			int vehicle = Utility.randomIntInclusive(problemInstance.vehicleCount-1);
			success = mutatePermutationWithinSingleGreedySwap(period, vehicle);
		}while(success==false && retry>0);
	}
	//returns true if permutation successful
	private boolean mutatePermutationWithinSingleGreedySwap(int period,int vehicle)
	{
		int start,end;
		
		if(vehicle == 0) start = 0;
		else start = routePartition[period][vehicle-1]+1;

		end = routePartition[period][vehicle];

		if(end<=start) return false;
		if(end-start<2) return false;
		
		int first = Utility.randomIntInclusive(start,end-2);
		first = getNextClientIndex(period, vehicle, first);
		
		int second=first+1;
		second = getNextClientIndex(period, vehicle, second);
		
		int third = second+1;
		third = getNextClientIndex(period, vehicle, third);
		
		if( first ==-1 || second == -1 || third==-1)
			return false;
		
		

		int client1 = permutation[period][first];
		int client2 = permutation[period][second];
		int client3 = permutation[period][third];
		
		int d = problemInstance.depotCount;
		
		
		double temp1 = problemInstance.costMatrix[client1+d][client2+d] + problemInstance.costMatrix[client1+d][client3+d];
		double temp2 = problemInstance.costMatrix[client2+d][client1+d] + problemInstance.costMatrix[client2+d][client3+d];
		double temp3 = problemInstance.costMatrix[client3+d][client1+d] + problemInstance.costMatrix[client3+d][client2+d];

		int coin;
		if(temp1<temp2 && temp1<temp3)
		{
			permutation[period][second] = client1;
			
			coin = Utility.randomIntInclusive(1);
			if(coin==0)
			{
				permutation[period][first] = client2;
				permutation[period][third] = client3;
			}
			else
			{
				permutation[period][first] = client3;
				permutation[period][third] = client2;
			}
		}
		
		else if(temp2<temp1 && temp2<temp3)
		{
			permutation[period][second] = client2;
			
			coin = Utility.randomIntInclusive(1);
			if(coin==0)
			{
				permutation[period][first] = client1;
				permutation[period][third] = client3;
			}
			else
			{
				permutation[period][first] = client3;
				permutation[period][third] = client1;
			}
		}
		else			
		{
			permutation[period][second] = client3;
			
			coin = Utility.randomIntInclusive(1);
			if(coin==0)
			{
				permutation[period][first] = client1;
				permutation[period][third] = client2;
			}
			else
			{
				permutation[period][first] = client2;
				permutation[period][third] = client1;
			}
		}
		return true;
		
	}
	
	*/
	
	
	private static  void uniformCrossoverForPeriodAssignment(Individual child1,Individual child2, Individual parent1, Individual parent2,ProblemInstance problemInstance)
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
			
			//problemInstance.out.println("Customer "+i+ " coin : "+coin);
			
			
			for(int period = 0; period<problemInstance.periodCount; period++)
			{
				//if(parent1==null)System.out.print("nul");
				temp1.periodAssignment[period][i] = parent1.periodAssignment[period][i];
				temp2.periodAssignment[period][i] = parent2.periodAssignment[period][i];
			}
		}
		
	}

	static void crossOver_Uniform_VPMX_sortedCrissCross(ProblemInstance problemInstance,Individual parent1,Individual parent2,Individual child1,Individual child2)
	{
		//with 50% probability swap parents
		int ran = Utility.randomIntInclusive(1);
		if(ran ==1)
		{
			Individual temp = parent1;
			parent1 = parent2;
			parent2 = temp;
		}
		
		// UNIFORM CROSSOVER FOR PeriodAssignment
		uniformCrossoverForPeriodAssignment(child1,child2,parent1, parent2,problemInstance);
		
		
		//child 1 gets permutation of first n period from parent 1
		
		VPMX(child1, child2, parent1, parent2, problemInstance);
		
		
		// crossover route partition
		sortedCrisscrossCrossoverForRoutePartition(child1, child2, parent1, parent2, problemInstance);
		
		
		//update cost and penalty
		child1.calculateCostAndPenalty();
		child2.calculateCostAndPenalty();
		
		//System.out.println(" "+n);
	}

	
	static void sortedCrisscrossCrossoverForRoutePartition(Individual child1,Individual child2, Individual parent1, Individual parent2,ProblemInstance problemInstance)
	{
		for(int period=0;period<problemInstance.periodCount;period++)
		{
			int temp[] = new int[problemInstance.vehicleCount*2];
			int i;
			for(i=0;i<problemInstance.vehicleCount;i++) temp[i] = parent1.routePartition[period][i];
			for(i=0;i<problemInstance.vehicleCount;i++) temp[i+problemInstance.vehicleCount] = parent2.routePartition[period][i];
			
			
			for(i=0;i<problemInstance.vehicleCount*2;i++)
			{
				for(int j=i+1;j<problemInstance.vehicleCount*2;j++)
				{
					if(temp[i]>temp[j])
					{
						int tmp = temp[i];
						temp[i] = temp[j];
						temp[j]=tmp;
					}
				}
			}
			
			for( i=0;i<problemInstance.vehicleCount;i++) child1.routePartition[period][i] = temp[2*i];
			for( i=0;i<problemInstance.vehicleCount;i++) child2.routePartition[period][i] = temp[2*i+1];
			
		}
		
	}
	
	/** 
	 * 
	  */
	static void crossOver(ProblemInstance problemInstance,Individual parent1,Individual parent2,Individual child1,Individual child2)
	{
		//with 50% probability swap parents
		int ran = Utility.randomIntInclusive(1);
		if(ran ==1)
		{
			Individual temp = parent1;
			parent1 = parent2;
			parent2 = temp;
		}
		
		
		//child 1 gets first n customers assignment from parent 1 and rest from parent 2
		//child 2 gets first n customers assignment from parent 2 and rest from parent 1
		int n = Utility.randomIntInclusive(problemInstance.customerCount);
		
		
		copyPeriodAssignmentFromParents(child1, parent1, parent2, n ,problemInstance);
		copyPeriodAssignmentFromParents(child2, parent2, parent1, n ,problemInstance);
		
		//child 1 gets permutation of first n period from parent 1
		n = Utility.randomIntInclusive(problemInstance.periodCount);
		
		copyPermutation(child1, parent1, parent2, n, problemInstance);
		copyPermutation(child2, parent2, parent1, n, problemInstance);
		
		
		// crossover route partition
		
		for(int period=0;period<problemInstance.periodCount;period++)
		{
			int temp[] = new int[problemInstance.vehicleCount*2];
			int i;
			for(i=0;i<problemInstance.vehicleCount;i++) temp[i] = parent1.routePartition[period][i];
			for(i=0;i<problemInstance.vehicleCount;i++) temp[i+problemInstance.vehicleCount] = parent2.routePartition[period][i];
			
			
			for(i=0;i<problemInstance.vehicleCount*2;i++)
			{
				for(int j=i+1;j<problemInstance.vehicleCount*2;j++)
				{
					if(temp[i]>temp[j])
					{
						int tmp = temp[i];
						temp[i] = temp[j];
						temp[j]=tmp;
					}
				}
			}
			
			for( i=0;i<problemInstance.vehicleCount;i++) child1.routePartition[period][i] = temp[2*i];
			for( i=0;i<problemInstance.vehicleCount;i++) child2.routePartition[period][i] = temp[2*i+1];
			
		}
		
		child1.calculateCostAndPenalty();
		child2.calculateCostAndPenalty();
		
		//System.out.println(" "+n);
	}

	//copy first n row from parent 1's permutation
	private static void copyPermutation(Individual child, Individual parent1, Individual parent2,int n,ProblemInstance problemInstance) 
	{
		int i;
		
		for(i=0;i<problemInstance.customerCount;i++)
		{
			for(int period = 0;period<n;period++)
			{
				child.permutation[period][i] = parent1.permutation[period][i];
			}
		}
		
		for(i=0;i<problemInstance.customerCount;i++)
		{
			for(int period = n;period<problemInstance.periodCount;period++)
			{
				child.permutation[period][i] = parent2.permutation[period][i];
			}
		}
		
	}
	
	//copies first n columns from parent1 and rest of them from parent 2 
	private static  void copyPeriodAssignmentFromParents(Individual child, Individual parent1, Individual parent2,int n,ProblemInstance problemInstance)
	{
		int i;
		for(int period = 0; period<problemInstance.periodCount; period++)
		{
			for(i=0;i<n;i++)
			{
				//if(parent1==null)System.out.print("nul");
				child.periodAssignment[period][i] = parent1.periodAssignment[period][i];
			}
		}
		for(int period = 0; period<problemInstance.periodCount; period++)
		{
			for(i=n;i<problemInstance.customerCount;i++)
			{
				child.periodAssignment[period][i] = parent2.periodAssignment[period][i];
			}
		}
	}

	public static double distance(ProblemInstance problemInstance, Individual first,Individual second)
	{
		boolean print=false;

		if(print)
		{
			problemInstance.out.println("In distance function : ");
		}
		
		double distance=0;
		int distanceX=0;
		int distanceY=0;
		int distanceZ=0;
		
		double X,Y,Z;
		double tmp;
		for(int i=0;i<problemInstance.periodCount;i++)
		{
			for(int j=0;j<problemInstance.customerCount;j++)
			{
				//distance for periodAssigment
				if(first.periodAssignment[i][j] != second.periodAssignment[i][j])
					distanceX++;
			}
		}
		
		
		tmp = (double)problemInstance.periodCount*problemInstance.customerCount;
		X = distanceX / tmp;
		
		
		distanceY=0;
		for(int i=0;i<problemInstance.periodCount;i++)
		{
			for(int j=0;j<problemInstance.customerCount;j++)
			{
				//distance for permutation - A distance (Campos)
				//distanceY += Math.abs(first.permutation[i][j] - second.permutation[i][j]);
				
				//hamming distance
				if(first.permutation[i][j] != second.permutation[i][j])
					distanceY++;
			}
		}
		
		tmp = problemInstance.periodCount*problemInstance.customerCount;
		Y = distanceY/tmp;
		
			
		distanceZ=0;
		for(int i=0;i<problemInstance.periodCount;i++)
		{
			for(int j=0;j<problemInstance.vehicleCount;j++)
			{
				//distance for route partition - A distance
				distanceZ += Math.abs(first.routePartition[i][j] - second.routePartition[i][j]);
			}
		}
		
		//as the last element is always same
		tmp = problemInstance.periodCount * problemInstance.customerCount * (problemInstance.vehicleCount-1);
		if(tmp ==0)Z=0;
		else Z = (double)distanceZ/tmp; 
	
		distance = (X+Y+Z)/3;
		
		if(print)
		{
			problemInstance.out.println("distanceX : "+distanceX+" distanceY : "+distanceY+" distanceZ : " +distanceZ);
			problemInstance.out.println("maxX : "+(problemInstance.periodCount*problemInstance.customerCount)
								+" maxY : "+ (problemInstance.periodCount*problemInstance.customerCount)
								+" maxZ : " +( problemInstance.periodCount * problemInstance.customerCount * (problemInstance.vehicleCount-1)) + "\n");
			
			
		}
		
		return distance;
	}
	
	
	static void crossOverWithVPMX(ProblemInstance problemInstance,Individual parent1,Individual parent2,Individual child1,Individual child2)
	{
		int ran = Utility.randomIntInclusive(1);
		if(ran ==1)
		{
			Individual temp = parent1;
			parent1 = parent2;
			parent2 = temp;
		}
		
		
		//child 1 gets first n customers assignment from parent 1 and rest from parent 2
		//child 2 gets first n customers assignment from parent 2 and rest from parent 1
		int n = Utility.randomIntInclusive(problemInstance.customerCount);
		
		
		copyPeriodAssignmentFromParents(child1, parent1, parent2, n ,problemInstance);
		copyPeriodAssignmentFromParents(child2, parent2, parent1, n ,problemInstance);
		
		//child 1 gets permutation of first n period from parent 1
		n = Utility.randomIntInclusive(problemInstance.periodCount);
		
		VPMX(child1,child2,parent1,parent2,problemInstance);
		
		
		// crossover route partition
		
		for(int period=0;period<problemInstance.periodCount;period++)
		{
			int temp[] = new int[problemInstance.vehicleCount*2];
			int i;
			for(i=0;i<problemInstance.vehicleCount;i++) temp[i] = parent1.routePartition[period][i];
			for(i=0;i<problemInstance.vehicleCount;i++) temp[i+problemInstance.vehicleCount] = parent2.routePartition[period][i];
			
			
			for(i=0;i<problemInstance.vehicleCount*2;i++)
			{
				for(int j=i+1;j<problemInstance.vehicleCount*2;j++)
				{
					if(temp[i]>temp[j])
					{
						int tmp = temp[i];
						temp[i] = temp[j];
						temp[j]=tmp;
					}
				}
			}
			
			for( i=0;i<problemInstance.vehicleCount;i++) child1.routePartition[period][i] = temp[2*i];
			for( i=0;i<problemInstance.vehicleCount;i++) child2.routePartition[period][i] = temp[2*i+1];
			
		}
		
		child1.calculateCostAndPenalty();
		child2.calculateCostAndPenalty();
	}
	private static void VPMX(Individual child1, Individual child2,
			Individual parent1, Individual parent2,
			ProblemInstance problemInstance2) {
		int count=problemInstance2.customerCount;
		
		// TODO Auto-generated method stub
		for(int i=0;i<problemInstance2.periodCount;i++)
		{
			
			int start1=Utility.randomIntInclusive(0, count-2);
			int end1=Utility.randomIntInclusive(start1+1, count-1);
			int diff=end1-start1;
			
			int start2=Utility.randomIntInclusive(0, count-1-diff);
			int end2=start2+diff;
			
			for(int s=0;s<start1;s++) child1.permutation[i][s]=parent1.permutation[i][s];
			for(int e=end1+1;e<count;e++) child1.permutation[i][e]=parent1.permutation[i][e];
			for(int m=start1;m<=end1;m++)  child1.permutation[i][m]=parent2.permutation[i][start2+m-start1] ;
			
			for(int s=0;s<start2;s++) child2.permutation[i][s]=parent2.permutation[i][s];
			for(int e=end2+1;e<count;e++) child2.permutation[i][e]=parent2.permutation[i][e];
			for(int m=start2;m<=end2;m++)  child2.permutation[i][m]=parent1.permutation[i][start1+m-start2] ;
			/*System.out.println();
			for(int i=0;i<count;i++){
				System.out.print(child1[i]+" ");
			}
			System.out.println();
			for(int i=0;i<count;i++){
				System.out.print(child2[i]+" ");
			}*/
			
			HashMap<Integer, Integer> OneToTwo=new HashMap<Integer,Integer>();
			HashMap<Integer, Integer> TwoToOne=new HashMap<Integer,Integer>();
			
			for(int it=0;it<=(end1-start1);it++){
				OneToTwo.put(parent1.permutation[i][start1+it], parent2.permutation[i][start2+it]);
				TwoToOne.put(parent2.permutation[i][start2+it], parent1.permutation[i][start1+it]);
			}
			int []rest1=new int[count-(end1-start1+1)];
			int iter=0;
			for(int it=0;it<start1;it++){
				rest1[iter++]=parent1.permutation[i][it];
			}
			for(int it=end1+1;it<count;it++){
				rest1[iter++]=parent1.permutation[i][it];
			}
			
			int []rest2=new int[count-(end2-start2+1)];
			iter=0;
			for(int it=0;it<start2;it++){
				rest2[iter++]=parent2.permutation[i][it];
			}
			for(int it=end2+1;it<count;it++){
				rest2[iter++]=parent2.permutation[i][it];
			}
			
			for(int it=0;it<rest1.length;it++){
				OneToTwo.put(rest1[it], rest2[it]);
				TwoToOne.put(rest2[it], rest1[it]);
			}
			
			/*Set<Integer>set=OneToTwo.keySet();
			for (Iterator iterator = set.iterator(); iterator.hasNext();) {
				Integer integer = (Integer) iterator.next();
				System.out.print("\n"+integer+"->"+OneToTwo.get(integer));
				
			}
			System.out.println();
			set=TwoToOne.keySet();
			for (Iterator iterator = set.iterator(); iterator.hasNext();) {
				Integer integer = (Integer) iterator.next();
				System.out.print("\n"+integer+"->"+TwoToOne.get(integer));
				
			}*/
			
			/********************** child 1 check kortesi *******************************/
			int notSafeStart=start1;
			int notSafeEnd=end1;
			while(notSafeStart<=notSafeEnd){
				int num_retry=0;
				boolean retired=false;
				while(checkConflict(child1,i,notSafeStart,notSafeEnd,count)){
					num_retry++;
					if(num_retry==count){
						//System.out.println("Breaked!!");
						retired=true;
						break;
					}
					int p=child1.permutation[i][notSafeStart];
					child1.permutation[i][notSafeStart]=OneToTwo.get(p);
				}
				if(retired) break;
				notSafeStart++;
			}
			/***********************************************************************/
			
			/********************** child 2 check kortesi *******************************/
			notSafeStart=start2;
			notSafeEnd=end2;
			while(notSafeStart<=notSafeEnd){
				int num_retry=0;
				boolean retired=false;
				while(checkConflict(child2,i,notSafeStart,notSafeEnd,count)){
					num_retry++;
					if(num_retry==count){
						retired=true;
						break;
					}
					int p=child2.permutation[i][notSafeStart];
					child2.permutation[i][notSafeStart]=TwoToOne.get(p);
				}
				if(retired) break;
				notSafeStart++;
			}
			/***********************************************************************/
			
			
		}
	}


	private static boolean checkConflict(Individual child1,int period, int notSafeStart,
			int notSafeEnd, int count) {
		// TODO Auto-generated method stub
		
			int p=child1.permutation[period][notSafeStart];
			for(int it=0;it<notSafeStart;it++){
				if(child1.permutation[period][it]==p){
					return true;
				}
			}
			for(int it=notSafeEnd+1;it<count;it++){
				if(child1.permutation[period][it]==p){
					return true;
				}
			}
			return false;

	}






	class Route
	{
		
	}
	
}
