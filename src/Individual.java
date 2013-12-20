import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;


public class Individual 
{
	//representation
	boolean periodAssignment[][];
	Vector<Vector<ArrayList<Integer>>> routes;
	
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
	static	double distanceRatioToEachVehicle[][]=null;
	static	double cumulativeDistanceRatioToEachVehicle[][]=null;
	
	static int[][] clientsSortedWithDistance = null;
	
	static	double closenessToEachDepot[][]=null;
	static	double cumulativeClosenessToEachDepot[][]=null;
	
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
		
		// NOW INITIALISE WITH VALUES
		//initialize period assignment

		int freq,allocated,random;
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
		

		
		assignRoutesWithClosestDepotWithNeighbourCheckHeuristic();
		//randomizeAllRoute();
		calculateCostAndPenalty();


	}
	
	private void assignRoutesWithClosestDepotWithNeighbourCheckHeuristic()
	{
		//Assign customer to route
		boolean[] clientMap = new boolean[problemInstance.customerCount];
		
		int assigned=0;
		
		while(assigned<problemInstance.customerCount)
		{
			int clientNo = Utility.randomIntInclusive(problemInstance.customerCount-1);
			if(clientMap[clientNo]) continue;
			clientMap[clientNo]=true;
			assigned++;
			
			
			for(int period=0;period<problemInstance.periodCount;period++)
			{		
				if(periodAssignment[period][clientNo]==false)continue;

				int depot = closestDepot(clientNo);	
				insertClientToRouteThatMinimizesTheIncreaseInActualCost(clientNo, depot, period);
			}			
		}
	}
	
	private int closestDepot(int client)
	{
		int selectedDepot=-1;
		double maxProbable = closenessToEachDepot[client][0];
		//	System.out.print("Client : "+client+" Rand : " +rand );
		for(int depot=0;depot<problemInstance.depotCount;depot++)
		{
			if(maxProbable<=closenessToEachDepot[client][depot])
			{
				selectedDepot = depot;
				maxProbable = closenessToEachDepot[client][depot];
			}
		}
		return selectedDepot ;
	}


	private void insertClientToRouteThatMinimizesTheIncreaseInActualCost(int client,int depot,int period)
	{
		double min = 99999999;
		int chosenVehicle =- 1;
		int chosenInsertPosition =- 1;
		double cost;
		
		double [][]costMatrix = problemInstance.costMatrix;
		int depotCount = problemInstance.depotCount;
		
		ArrayList<Integer> vehiclesUnderThisDepot = problemInstance.vehiclesUnderThisDepot.get(depot);
		
		for(int i=0; i<vehiclesUnderThisDepot.size(); i++)
		{
			int vehicle = vehiclesUnderThisDepot.get(i);
			
			ArrayList<Integer> route = routes.get(period).get(vehicle);
			
			if(route.size()==0)
			{
				cost = costMatrix[depot][depotCount+client] + costMatrix[depotCount+client][depot];
				if(cost<min)
				{
					min=cost;
					chosenVehicle = vehicle;
					chosenInsertPosition = 0;
				}
				continue;
			}
			
			
			cost = costMatrix[depot][depotCount+client] + costMatrix[depotCount+client][depotCount+route.get(0)];
			cost -= (costMatrix[depot][depotCount+route.get(0)]);
			if(cost<min)
			{
				min=cost;
				chosenVehicle = vehicle;
				chosenInsertPosition = 0;
			}
			
			for(int insertPosition=1;insertPosition<route.size();insertPosition++)
			{
				//insert the client between insertPosition-1 and insertPosition and check 
				cost = costMatrix[depotCount+route.get(insertPosition-1)][depotCount+client] + costMatrix[depotCount+client][depotCount+route.get(insertPosition)];
				cost -= (costMatrix[depotCount+route.get(insertPosition-1)][depotCount+route.get(insertPosition)]);
				if(cost<min)
				{
					min=cost;
					chosenVehicle = vehicle;
					chosenInsertPosition = insertPosition;
				}
			}
			
			cost = costMatrix[depotCount+route.get(route.size()-1)][depotCount+client] + costMatrix[depotCount+client][depot];
			cost-=(costMatrix[depotCount+route.get(route.size()-1)][depot]);
			
			if(cost<min)
			{
				min=cost;
				chosenVehicle = vehicle;
				chosenInsertPosition = route.size();
			}
			
		}
		routes.get(period).get(chosenVehicle).add(chosenInsertPosition, client);
	}


	//see you later 
	class MinimumCostInfo
	{
		public int vehicle;
		public int insertPosition;
		public double minimumIncreaseInCost;
		public double loadViolation;
	}
	private MinimumCostInfo getMinimumCostIncreseInfo(int client,int vehicle,int period,double[][] thisRouteLoad)
	{
		MinimumCostInfo minimumCostInfo = new MinimumCostInfo();
		double min = 99999999;
		int chosenInsertPosition =- 1;
		double cost;
		
		double [][]costMatrix = problemInstance.costMatrix;
		int depotCount = problemInstance.depotCount;
		int depot = problemInstance.depotAllocation[vehicle];	
		ArrayList<Integer> route = routes.get(period).get(vehicle);
		
		
		double loadViolation =  (thisRouteLoad[period][vehicle] + problemInstance.demand[client]) - problemInstance.loadCapacity[vehicle];
		
		if(loadViolation>0)
		{
			minimumCostInfo.insertPosition=-1;
			minimumCostInfo.minimumIncreaseInCost=99999999;
			minimumCostInfo.vehicle = vehicle;
			minimumCostInfo.loadViolation = loadViolation;
			return minimumCostInfo;
		}
		
		if(route.size()==0)
		{
			cost = costMatrix[depot][depotCount+client] + costMatrix[depotCount+client][depot];
						
			minimumCostInfo.insertPosition=0;
			minimumCostInfo.minimumIncreaseInCost=cost;
			minimumCostInfo.vehicle = vehicle;
			minimumCostInfo.loadViolation = loadViolation;
			return minimumCostInfo;
		}
		
		cost=0;
		cost = costMatrix[depot][depotCount+client] + costMatrix[depotCount+client][depotCount+route.get(0)];
		cost -= (costMatrix[depot][depotCount+route.get(0)]);
		if(cost<min)
		{
			min=cost;
			chosenInsertPosition = 0;
		}
		
		for(int insertPosition=1;insertPosition<route.size();insertPosition++)
		{
			//insert the client between insertPosition-1 and insertPosition and check 
			cost = costMatrix[depotCount+route.get(insertPosition-1)][depotCount+client] + costMatrix[depotCount+client][depotCount+route.get(insertPosition)];
			cost -= (costMatrix[depotCount+route.get(insertPosition-1)][depotCount+route.get(insertPosition)]);
			if(cost<min)
			{
				min=cost;
				chosenInsertPosition = insertPosition;
			}
		}
		
		cost = costMatrix[depotCount+route.get(route.size()-1)][depotCount+client] + costMatrix[depotCount+client][depot];
		cost-=(costMatrix[depotCount+route.get(route.size()-1)][depot]);
		
		if(cost<min)
		{
			min=cost;
			chosenInsertPosition = route.size();
		}
			
		minimumCostInfo.insertPosition=chosenInsertPosition;
		minimumCostInfo.minimumIncreaseInCost=min;
		minimumCostInfo.vehicle = vehicle;
		minimumCostInfo.loadViolation = loadViolation;
		return minimumCostInfo;
	}
	////////////////

	
	/*---------------------THESE ARE OF NO USE NOW ---------------------*/
	
	/**
	 * Sorts clients according to their distance from corresponding depot, closer clients have smaller indices
	 * @param problemInstance
	 */
	
	/*public static void initialiseSortedClientArray(ProblemInstance problemInstance)
	{
		int depotCount = problemInstance.depotCount;
		int clientCount= problemInstance.customerCount;
		clientsSortedWithDistance = new int[depotCount][clientCount];
		double[][] cost = problemInstance.costMatrix;
		for(int depot=0; depot<depotCount;depot++)
		{
			for(int client=0;client<clientCount;client++)
			{
				clientsSortedWithDistance[depot][client]=client;
			}
		}
		
		//sort each array
		for(int depot=0; depot<depotCount;depot++)
		{
			for(int i=0;i<clientCount;i++)
			{
				for(int j=i+1;j<clientCount;j++)
				{
					//if j< i then swap
					if(cost[depot][depotCount+clientsSortedWithDistance[depot][i]] > cost[depot][depotCount+clientsSortedWithDistance[depot][j]])
					{
						int tmp = clientsSortedWithDistance[depot][i];
						clientsSortedWithDistance[depot][i] = clientsSortedWithDistance[depot][j];
						clientsSortedWithDistance[depot][j] = tmp;
 					}
				}
			}
		}
		
		
		//print to confirm if right ?
		for(int depot=0; depot<depotCount;depot++)
		{
			System.out.println("Depot : "+depot);
			
			for(int i=0;i<clientCount;i++)
			{
				System.out.print(clientsSortedWithDistance[depot][i] +" " );
			}
			System.out.println();
			
			for(int i=0;i<clientCount;i++)
			{
				System.out.print(cost[depot][depotCount+clientsSortedWithDistance[depot][i]] +" " );
			}
			System.out.println();
			
		}
		
		
	}
	
	*/
	
	/* Heuristic -> each client assigned to closest Depot 
	 * client added to the position where cost in minimum
	 * every vehicle gets the first client a closest client

	private void assignRoutesWithClosestDepotWithNeighbourCheckHeuristic2()
	{
		//Assign customer to route
		boolean[] clientMap = new boolean[problemInstance.customerCount];
		
		int assigned=0;
		
		// assign each vehicle with the closest client 
		for(int period=0;period<problemInstance.periodCount;period++)
		{
			for(int depot=0;depot<problemInstance.depotCount;depot++)
			{
				ArrayList<Integer> vehiclesUnderThisDepot = problemInstance.vehiclesUnderThisDepot.get(depot);
				
				int clientIndex=0;
				
				for(int i=0; i<vehiclesUnderThisDepot.size(); i++)
				{
					int vehicle = vehiclesUnderThisDepot.get(i);
					
					while(true)
					{
						if(clientIndex >= problemInstance.customerCount)
						{
							System.out.println("NOT ENOUGH CLIENTS!!");
						}
						
						if(periodAssignment[period][clientsSortedWithDistance[depot][clientIndex]]==true)
						{	
							if(routes.get(period).get(vehicle).size() > 0)
							{
								System.out.println("\n\nNEVER SHOULD HAPPEN");
							}
							routes.get(period).get(vehicle).add(clientsSortedWithDistance[depot][clientIndex]);
							clientIndex++;
							break;
						}
						clientIndex++;						
					}
					
				}
				

			}
		}
			
		
		//
		
		while(assigned<problemInstance.customerCount)
		{
			int clientNo = Utility.randomIntInclusive(problemInstance.customerCount-1);
			if(clientMap[clientNo]) continue;
			clientMap[clientNo]=true;
			assigned++;
			
			
			for(int period=0;period<problemInstance.periodCount;period++)
			{		
				if(periodAssignment[period][clientNo]==false)continue;

				int depot = closestDepot(clientNo);	
				insertClientToRouteThatMinimizesTheIncreaseInActualCost2(clientNo, depot, period);
			}			
		}
	}
	private void insertClientToRouteThatMinimizesTheIncreaseInActualCost2(int client,int depot,int period)
	{
		double min = 99999999;
		int chosenVehicle =- 1;
		int chosenInsertPosition =- 1;
		double cost;
		
		double [][]costMatrix = problemInstance.costMatrix;
		int depotCount = problemInstance.depotCount;
		
		ArrayList<Integer> vehiclesUnderThisDepot = problemInstance.vehiclesUnderThisDepot.get(depot);
		
		for(int i=0; i<vehiclesUnderThisDepot.size(); i++)
		{
			int vehicle = vehiclesUnderThisDepot.get(i);
			
			ArrayList<Integer> route = routes.get(period).get(vehicle);
						
			if(route.size()==0)
			{
				cost = costMatrix[depot][depotCount+client] + costMatrix[depotCount+client][depot];
				if(cost<min)
				{
					min=cost;
					chosenVehicle = vehicle;
					chosenInsertPosition = 0;
				}
				System.out.println("Period "+period+" Vehicle "+vehicle+" has initially empty route");
				continue;
			}
			cost = costMatrix[depot][depotCount+client] + costMatrix[depotCount+client][depotCount+route.get(0)];
			cost -= (costMatrix[depot][depotCount+route.get(0)]);
			if(cost<min)
			{
				min=cost;
				chosenVehicle = vehicle;
				chosenInsertPosition = 0;
			}
			
			for(int insertPosition=1;insertPosition<route.size();insertPosition++)
			{
				//insert the client between insertPosition-1 and insertPosition and check 
				cost = costMatrix[depotCount+route.get(insertPosition-1)][depotCount+client] + costMatrix[depotCount+client][depotCount+route.get(insertPosition)];
				cost -= (costMatrix[depotCount+route.get(insertPosition-1)][depotCount+route.get(insertPosition)]);
				if(cost<min)
				{
					min=cost;
					chosenVehicle = vehicle;
					chosenInsertPosition = insertPosition;
				}
			}
			
			cost = costMatrix[depotCount+route.get(route.size()-1)][depotCount+client] + costMatrix[depotCount+client][depot];
			cost-=(costMatrix[depotCount+route.get(route.size()-1)][depot]);
			
			if(cost<min)
			{
				min=cost;
				chosenVehicle = vehicle;
				chosenInsertPosition = route.size();
			}
			
		}
		routes.get(period).get(chosenVehicle).add(chosenInsertPosition, client);
	}

*/
	
	/*
	private void insertClientToRouteThatMinimizesTheIncreaseInCost(int client,int depot,int period)
	{
		double min = 99999999;
		int chosenVehicle =- 1;
		int chosenInsertPosition =- 1;
		double cost;
		
		double [][]costMatrix = problemInstance.costMatrix;
		int depotCount = problemInstance.depotCount;
		
		ArrayList<Integer> vehiclesUnderThisDepot = problemInstance.vehiclesUnderThisDepot.get(depot);
		
		for(int i=0; i<vehiclesUnderThisDepot.size(); i++)
		{
			int vehicle = vehiclesUnderThisDepot.get(i);
			
			ArrayList<Integer> route = routes.get(period).get(vehicle);
			
			if(route.size()==0)
			{
				cost = costMatrix[depot][depotCount+client] + costMatrix[depotCount+client][depot];
				if(cost<min)
				{
					min=cost;
					chosenVehicle = vehicle;
					chosenInsertPosition = 0;
				}
				continue;
			}
			
			
			cost = costMatrix[depot][depotCount+client] + costMatrix[depotCount+client][depotCount+route.get(0)];
			if(cost<min)
			{
				min=cost;
				chosenVehicle = vehicle;
				chosenInsertPosition = 0;
			}
			
			for(int insertPosition=1;insertPosition<route.size();insertPosition++)
			{
				//insert the client between insertPosition-1 and insertPosition and check 
				cost = costMatrix[depotCount+route.get(insertPosition-1)][depotCount+client] + costMatrix[depotCount+client][depotCount+route.get(insertPosition)];
				if(cost<min)
				{
					min=cost;
					chosenVehicle = vehicle;
					chosenInsertPosition = insertPosition;
				}
			}
			
			cost = costMatrix[depotCount+route.get(route.size()-1)][depotCount+client] + costMatrix[depotCount+client][depot];
			if(cost<min)
			{
				min=cost;
				chosenVehicle = vehicle;
				chosenInsertPosition = route.size();
			}
			
		}
		routes.get(period).get(chosenVehicle).add(chosenInsertPosition, client);
	}
	*/
	
	private void assignRoutesWithClosenessProportionalHeuristic()
	{

		//Assign customer to route
		boolean[] clientMap = new boolean[problemInstance.customerCount];
		
		int assigned=0;
		
	//	int[] cl=new int[problemInstance.customerCount];
		while(assigned<problemInstance.customerCount)
		{
			int clientNo = Utility.randomIntInclusive(problemInstance.customerCount-1);
			if(clientMap[clientNo]) continue;
			clientMap[clientNo]=true;
			assigned++;
			//cl[assigned-1] = clientNo;
					

			for(int period=0;period<problemInstance.periodCount;period++)
			{		
				if(periodAssignment[period][clientNo]==false)continue;

				int vehicle = mostProbableRoute(clientNo);				
				routes.get(period).get(vehicle).add(clientNo);
			}
		}
		/*Arrays.sort(cl);
		for(int i=0;i<cl.length;i++)
			System.out.print(cl[i]+" ");
		System.out.println("");*/
	}
	
	/**
	 *  At first selects a depot with probability proportional to closeness to the depot
	 *  Then among the vehicles under this selected depot
	 *  assigns to the cheapest route  
	 */
	private void assignRoutesWithClosestDepotProportionalWithNeighbourCheckHeuristic()
	{
		//Assign customer to route
		boolean[] clientMap = new boolean[problemInstance.customerCount];
		
		int assigned=0;
		
		while(assigned<problemInstance.customerCount)
		{
			int clientNo = Utility.randomIntInclusive(problemInstance.customerCount-1);
			if(clientMap[clientNo]) continue;
			clientMap[clientNo]=true;
			assigned++;
			
			
			for(int period=0;period<problemInstance.periodCount;period++)
			{		
				if(periodAssignment[period][clientNo]==false)continue;

				int depot = mostProbableDepot(clientNo);	
				insertClientToRouteThatMinimizesTheIncreaseInActualCost(clientNo, depot, period);
			}			
		}
	}
	
	
	private int mostProbableDepot(int client)
	{
		double rand = Utility.randomDouble(0, 1);
	//	System.out.print("Client : "+client+" Rand : " +rand );
		for(int depot=0;depot<problemInstance.depotCount;depot++)
		{
			if(rand<=cumulativeClosenessToEachDepot[client][depot])
			{
				//System.out.println("Chosen Vehicle : " +vehicle );
				return depot;
			}
		}
		return -1;
	}
	
	
	public static void calculateAssignmentProbalityForDiefferentDepot(ProblemInstance problemInstance) 
	{
		double sum=0;
		closenessToEachDepot = new double[problemInstance.customerCount][problemInstance.depotCount];
		cumulativeClosenessToEachDepot = new double[problemInstance.customerCount][problemInstance.depotCount];
		
		for(int client=0;client<problemInstance.customerCount;client++)
		{
			for(int depot=0;depot<problemInstance.depotCount;depot++)
			{
				closenessToEachDepot[client][depot] = (1/problemInstance.costMatrix[depot][problemInstance.depotCount+client]);
			}
						
			sum=0;
			for(int depot=0;depot<problemInstance.depotCount;depot++)
			{
				sum+= closenessToEachDepot[client][depot];
			}
			

			/*for(int depot=0;depot<problemInstance.depotCount;depot++)
				System.out.print(closenessToEachDepot[client][depot]+" ");
			System.out.println("Sum : "+sum);
			*/
			
			for(int depot=0;depot<problemInstance.depotCount;depot++)
			{
				closenessToEachDepot[client][depot] /= sum;
			}
			
			cumulativeClosenessToEachDepot[client][0] = closenessToEachDepot[client][0];
			for(int depot=1;depot<problemInstance.depotCount;depot++)
			{
				cumulativeClosenessToEachDepot[client][depot] = cumulativeClosenessToEachDepot[client][depot-1]+ closenessToEachDepot[client][depot];
			}
			
			
			/*for(int depot=0;depot<problemInstance.depotCount;depot++)
				System.out.print(closenessToEachDepot[client][depot]+" ");
			System.out.println();			
			
			for(int depot=0;depot<problemInstance.depotCount;depot++)
				System.out.print(cumulativeClosenessToEachDepot[client][depot]+" ");
				
			System.out.println();*/
		}
	}

	private void randomizeAllRoute()
	{
		//randomize the pattern for each route
		//adjacent swap
		int coin;
		int ran;
		
		for(int vehicle=0;vehicle<problemInstance.vehicleCount;vehicle++)
		{
			ran = Utility.randomIntInclusive(3);
			
			for(int period=0;period<problemInstance.periodCount;period++)
			{		
				ArrayList<Integer> route = routes.get(period).get(vehicle);
					
				if(ran==0 || ran==1)   // knuth shuffle
				{
					for( int i = route.size()-1;i>=1;i--)
				    {
						int j = Utility.randomIntInclusive(0, i);
						int tmp = route.get(j);
						route.set(j, route.get(i));
						route.set(i, tmp);
				    } 
				}
				else if(ran==2)
				{
					for(int i=1;i<route.size();i++)
					{
						coin = Utility.randomIntInclusive(1);
						if(coin==1)
						{
							int tmp = route.get(i-1);
							route.set(i-1, route.get(i));
							route.set(i, tmp);
						}
					}
				}
				else
				{
					for(int i=route.size()-1;i>0;i--)
					{
						coin = Utility.randomIntInclusive(1);
						if(coin==1)
						{
							int tmp = route.get(i-1);
							route.set(i-1, route.get(i));
							route.set(i, tmp);
						}
					}
				}
			}
		}
			
	}
	
 	
	private int closestRoute(int client)
	{
		double max=-1;
		int index = -1;
	//	System.out.print("Client : "+client+" Rand : " +rand );
		for(int vehicle=0;vehicle<problemInstance.vehicleCount;vehicle++)
		{
			if(max<distanceRatioToEachVehicle[client][vehicle])
			{
				max = distanceRatioToEachVehicle[client][vehicle];
				index = vehicle;
			}
		}
		//System.out.println("Client : "+client+" vehicle : " +index );
		return index;
	}
	

	private int mostProbableRoute(int client)
	{
		double rand = Utility.randomDouble(0, 1);
	//	System.out.print("Client : "+client+" Rand : " +rand );
		for(int vehicle=0;vehicle<problemInstance.vehicleCount;vehicle++)
		{
			if(rand<cumulativeDistanceRatioToEachVehicle[client][vehicle])
			{
				//System.out.println("Chosen Vehicle : " +vehicle );
				return vehicle;
			}
		}
		return -1;
	}
	
	public static void calculateProbalityForDiefferentVehicle(ProblemInstance problemInstance) 
	{
		int depot;
		double sum=0;
		distanceRatioToEachVehicle = new double[problemInstance.customerCount][problemInstance.vehicleCount];
		cumulativeDistanceRatioToEachVehicle= new double[problemInstance.customerCount][problemInstance.vehicleCount];
		for(int client=0;client<problemInstance.customerCount;client++)
		{

			for(int vehicle=0;vehicle<problemInstance.vehicleCount;vehicle++)
			{
				depot = problemInstance.depotAllocation[vehicle];
				distanceRatioToEachVehicle[client][vehicle] = (1/problemInstance.costMatrix[depot][problemInstance.depotCount+client]);
			}
			
			
			sum=0;
			for(int vehicle=0;vehicle<problemInstance.vehicleCount;vehicle++)
			{
				sum+= distanceRatioToEachVehicle[client][vehicle];
			}
			

			/*for(int i=0;i<problemInstance.vehicleCount;i++)
				System.out.print(distanceRatioToEachVehicle[client][i]+" ");
			System.out.println("Sum : "+sum);
			*/
			
			for(int vehicle=0;vehicle<problemInstance.vehicleCount;vehicle++)
			{
				distanceRatioToEachVehicle[client][vehicle] /= sum;
			}
			
			cumulativeDistanceRatioToEachVehicle[client][0] = distanceRatioToEachVehicle[client][0];
			for(int vehicle=1;vehicle<problemInstance.vehicleCount;vehicle++)
			{
				cumulativeDistanceRatioToEachVehicle[client][vehicle] = cumulativeDistanceRatioToEachVehicle[client][vehicle-1]+ distanceRatioToEachVehicle[client][vehicle];
			}
			
			/*
			for(int i=0;i<problemInstance.vehicleCount;i++)
				System.out.print(distanceRatioToEachVehicle[client][i]+" ");
			System.out.println();			
			
			for(int i=0;i<problemInstance.vehicleCount;i++)
				System.out.print(cumulativeDistanceRatioToEachVehicle[client][i]+" ");
			
			System.out.println();*/
		}
	}
	
	
	/* THESE ARE OF NO USE NOW - END -------------------------------------- */
	
	public Individual(ProblemInstance problemInstance)
	{
		this.problemInstance = problemInstance;
		
		// ALLOCATING periodCount * customerCount Matrix for Period Assignment
		periodAssignment = new boolean[problemInstance.periodCount][problemInstance.customerCount];
		//ALlocating routes
		routes =  new Vector<Vector<ArrayList<Integer>>>();
		
		for(int period=0;period<problemInstance.periodCount;period++)
		{
			routes.add(new Vector<ArrayList<Integer>>());
			for(int vehicle=0;vehicle<problemInstance.vehicleCount;vehicle++)
			{
				routes.get(period).add(new ArrayList<Integer>());
			}
		}

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
		problemInstance = original.problemInstance;

		periodAssignment = new boolean[problemInstance.periodCount][problemInstance.customerCount];
		for(int i=0;i<problemInstance.periodCount;i++)
		{
			for(int j=0;j<problemInstance.customerCount;j++)
			{
				periodAssignment[i][j] = original.periodAssignment[i][j];
			}
		}

		for(int period=0;period<problemInstance.periodCount;period++)
		{
			routes.add(new Vector<ArrayList<Integer>>());

			for(int vehicle=0;vehicle<problemInstance.vehicleCount;vehicle++)
			{
				routes.get(period).add(new ArrayList<Integer>());
			}
		}


		for(int period=0;period<problemInstance.periodCount;period++)
		{
			for(int vehicle=0;vehicle<problemInstance.vehicleCount;vehicle++)
			{
				ArrayList<Integer> originalRoute = original.routes.get(period).get(vehicle);
				ArrayList<Integer> thisRoute = routes.get(period).get(vehicle);
				thisRoute.clear();//lagbena eta yet :P
				
				for(int i=0;i<originalRoute.size();i++)
				{
					thisRoute.add(originalRoute.get(i).intValue());
				}
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

		for(int period=0;period<problemInstance.periodCount;period++)
		{
			for(int vehicle=0;vehicle<problemInstance.vehicleCount;vehicle++)
			{
				ArrayList<Integer> originalRoute = original.routes.get(period).get(vehicle);
				ArrayList<Integer> thisRoute = routes.get(period).get(vehicle);
				thisRoute.clear();
				
				for( i=0;i<originalRoute.size();i++)
				{
					thisRoute.add(originalRoute.get(i).intValue());
				}
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

/**
	//calcuate fitness for each period for each vehicle
	// route for vehicle i is  [ routePartition[i-1]+1 , routePartition[i] ]
	// given that routePartition[i-1]+1 <= routePartition[i]
	//ignoring travelling time for now - for cordeau MDVRP
	// only service time is considered
*/	
	double calculateCost(int period,int vehicle)
	{
		int assignedDepot;		
		int clientNode,previous;

		ArrayList<Integer> route = routes.get(period).get(vehicle);
		assignedDepot = problemInstance.depotAllocation[vehicle];
        
		if(route.isEmpty())return 0;

		double costForPV = 0;
		double clientDemand=0;
		double totalRouteTime=0;
		
		//First client er service time
		totalRouteTime = problemInstance.serviceTime[route.get(0)];
		clientDemand = problemInstance.demand[route.get(0)];
		for(int i=1;i<route.size();i++)
		{
			clientNode = route.get(i);
			previous = route.get(i-1);
			
			if(periodAssignment[period][clientNode]==false) System.out.println("NEVER SHOULD HAPPEN!!!!! THIS CLIENT IS NOT PRESENT IN THIS PERIOD");
			
			costForPV +=	problemInstance.costMatrix[previous+problemInstance.depotCount][clientNode+problemInstance.depotCount];

			totalRouteTime += problemInstance.serviceTime[clientNode]; //adding service time for that node
            clientDemand += problemInstance.demand[clientNode];        //Caluculate total client demand for corresponding period,vehicle
			
			//ignoring travelling time for now - for cordeau MDVRP
			//totalRouteTime += problemInstance.travellingTimeMatrix[previous+problemInstance.depotCount][clientNode+problemInstance.depotCount];

		}

        costForPV += problemInstance.costMatrix[assignedDepot][route.get(0)+problemInstance.depotCount];
        costForPV += problemInstance.costMatrix[route.get(route.size()-1)+problemInstance.depotCount][assignedDepot];

//  	totalRouteTime += problemInstance.travellingTimeMatrix[assignedDepot][activeStart+problemInstance.depotCount];
//      totalRouteTime += problemInstance.travellingTimeMatrix[activeEnd+problemInstance.depotCount][assignedDepot];
    
        loadViolation[period][vehicle] = clientDemand - problemInstance.loadCapacity[vehicle];

		double routeTimeViolation = totalRouteTime - problemInstance.timeConstraintsOfVehicles[period][vehicle] ;
		if(routeTimeViolation>0) totalRouteTimeViolation += routeTimeViolation;

		return costForPV;
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

		out.print("Routes : \n");
		for(int period=0;period<problemInstance.periodCount;period++)
		{
			for(int vehicle=0;vehicle<problemInstance.vehicleCount;vehicle++)
			{
				out.print("< ");
				ArrayList<Integer> route = routes.get(period).get(vehicle);
				for(int clientIndex=0;clientIndex<route.size();clientIndex++)
				{
						out.print(route.get(clientIndex)+" ");
				}
				out.print("> ");
			}
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
	
	
	void mutateRouteWithInsertion()
	{
		boolean success;
		do
		{
			int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
			int vehicle = Utility.randomIntInclusive(problemInstance.vehicleCount-1);
			success = mutateRouteWithInsertion(period,vehicle);
		}while(success==false);
	}
	
	private boolean mutateRouteWithInsertion(int period,int vehicle)
	{
		ArrayList<Integer> route = routes.get(period).get(vehicle);
		int size=route.size(); 
		if(route.size()<2) return false;
		
		int selectedClientIndex = Utility.randomIntInclusive(route.size()-1);
		int selectedClient = route.get(selectedClientIndex);
		
		
		int newIndex;
		do
		{
			newIndex = Utility.randomIntInclusive(route.size()-1);
		}while(newIndex==selectedClientIndex);
				
		route.remove(selectedClientIndex);
		route.add(newIndex, selectedClient);
		
		//problemInstance.out.println("Period : "+period+" vehicle : "+vehicle+" selected Client : "+selectedClient+" "+ " new Position : "+newIndex);
		return true;
	}
	
	//DO NOT updates cost and penalty	
	void mutateRouteBySwapping()
	{
		boolean success = false;
		do
		{
			int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
			int vehicle = Utility.randomIntInclusive(problemInstance.vehicleCount-1);
			success = mutateRouteBySwapping(period, vehicle);
		}while(success==false);
	}

	
	//returns true if permutation successful
	private boolean mutateRouteBySwapping(int period,int vehicle)
	{
		ArrayList<Integer> route = routes.get(period).get(vehicle);

		if(route.size() < 2) return false;
		
		int first = Utility.randomIntInclusive(route.size()-1);

		int second;
		do
		{
			second = Utility.randomIntInclusive(route.size()-1);
		}
		while(second == first);

		//problemInstance.out.println("Period : "+period+" vehicle : "+vehicle+" SELCTED FOR SWAP "+route.get(first)+" "+route.get(second));
		
		int temp = route.get(first);
		route.set(first, route.get(second));
		route.set(second,temp);
				
		return true;
	}
	
	
	/** DO NOT updates cost and penalty
	*/
	void mutateTwoDifferentRouteBySwapping()
	{	
		if(problemInstance.vehicleCount<2)return;
		
		boolean success = false;
		int retry = 0;
		do
		{
			int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
			int vehicle1 = Utility.randomIntInclusive(problemInstance.vehicleCount-1);
			int vehicle2 = Utility.randomIntInclusive(problemInstance.vehicleCount-1);
			if(vehicle1==vehicle2)continue;
			success = mutateTwoDifferentRouteBySwapping(period, vehicle1,vehicle2);
			retry++;
		}while(success==false && retry<10);
		
		if(success==false)
		{
			//System.out.println("mutateTwoDifferentRouteBySwapping Failed !!");
			Solver.mutateRouteOfTwoDiefferentFailed++;
		}
	
	}
	
	/** DO NOT updates cost and penalty
	*/
	void mutateTwoDifferentRouteBySubstitution()
	{	
		if(problemInstance.vehicleCount<2)return;
		
		boolean success = false;
		int retry = 0;
		do
		{
			int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
			int vehicle1 = Utility.randomIntInclusive(problemInstance.vehicleCount-1);
			int vehicle2 = Utility.randomIntInclusive(problemInstance.vehicleCount-1);
			if(vehicle1==vehicle2)continue;
			success = mutateTwoDifferentRouteBySubstitution(period, vehicle1,vehicle2);
			retry++;
		}while(success==false && retry<10);
		
		if(success==false)
		{
			//System.out.println("mutateTwoDifferentRouteBySwapping Failed !!");
			Solver.mutateRouteOfTwoDiefferentFailed++;
		}
	
	}

	//returns if permutation successful
	private boolean mutateTwoDifferentRouteBySubstitution(int period,int vehicle1,int vehicle2)
	{
		ArrayList<Integer> route1 = routes.get(period).get(vehicle1);
		ArrayList<Integer> route2 = routes.get(period).get(vehicle2);

		if(route1.size()==0 || route2.size()==0) return false;
		
		int first = Utility.randomIntInclusive(route1.size()-1);
		int second = Utility.randomIntInclusive(route2.size());
		
		//problemInstance.out.println("Period : "+period+" vehicles  : "+vehicle1+" "+vehicle2+" SELCTED FOR SWAP "+route1.get(first)+" "+route2.get(second));

		
		route2.add (second , route1.get(first));
		route1.remove(first);
		return true;
	}


	
	//returns if permutation successful
	private boolean mutateTwoDifferentRouteBySwapping(int period,int vehicle1,int vehicle2)
	{
		ArrayList<Integer> route1 = routes.get(period).get(vehicle1);
		ArrayList<Integer> route2 = routes.get(period).get(vehicle2);

		if(route1.size()==0 || route2.size()==0) return false;
		
		int first = Utility.randomIntInclusive(route1.size()-1);
		int second = Utility.randomIntInclusive(route2.size()-1);
		
		//problemInstance.out.println("Period : "+period+" vehicles  : "+vehicle1+" "+vehicle2+" SELCTED FOR SWAP "+route1.get(first)+" "+route2.get(second));

		int temp = route1.get(first);
		route1.set(first, route2.get(second));
		route2.set(second,temp);
		return true;
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
	//need to edit this- must repair 
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

		int vehicle = removeClientFromPeriod(previouslyAssigned,clientNo);
		addClientIntoPeriod(previouslyUnassigned,vehicle,clientNo);

		//problemInstance.out.println("previouslyAssigned Period : "+previouslyAssigned+"previouslyUnassigned : "+previouslyUnassigned+" vehicle  : "+vehicle+" client "+clientNo);

		return true;
	}
	
	private void addClientIntoPeriod(int period, int vehicle, int client)
	{
		ArrayList<Integer> route = routes.get(period).get(vehicle);
		
		int position = Utility.randomIntInclusive(route.size());
		route.add(position,client);
	}
	/** Removes client from that periods route
	 * 
	 * @param period
	 * @param client
	 * @return number of the vehicle, of which route it was present.. <br/> -1 if it wasnt present in any route
	 */
	private int removeClientFromPeriod(int period, int client)
	{
		
		for(int vehicle=0;vehicle<problemInstance.vehicleCount;vehicle++)
		{
			ArrayList<Integer> route = routes.get(period).get(vehicle);
			if(route.contains(client))
			{
				route.remove(new Integer(client));
				return vehicle;
			}
		}
		return -1;
	}
		
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
			
			for(int period = 0; period<problemInstance.periodCount; period++)
			{
				//if(parent1==null)System.out.print("nul");
				temp1.periodAssignment[period][i] = parent1.periodAssignment[period][i];
				temp2.periodAssignment[period][i] = parent2.periodAssignment[period][i];
			}
		}
		
	}

	private static  void uniformCrossoverForRoutes(Individual child1,Individual child2, Individual parent1, Individual parent2,ProblemInstance problemInstance)
	{
		int coin;
		
		Individual temp1,temp2;
		for(int period = 0; period<problemInstance.periodCount; period++)
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
			
			
			for(int vehicle=0;vehicle<problemInstance.vehicleCount;vehicle++)
			{
				
				ArrayList<Integer> parent1Route = parent1.routes.get(period).get(vehicle);
				ArrayList<Integer> parent2Route = parent2.routes.get(period).get(vehicle);
				ArrayList<Integer> child1Route = temp1.routes.get(period).get(vehicle);
				ArrayList<Integer> child2Route = temp2.routes.get(period).get(vehicle);
				
				child1Route.clear();
				child2Route.clear();
				
				//copy temp1 <- parent1				
				for(int clientIndex=0;clientIndex<parent1Route.size();clientIndex++)
				{
					int node = parent1Route.get(clientIndex);
					if(temp1.periodAssignment[period][node])
						child1Route.add(node);
				}
				
				//copy temp2 <- parent2				
				for(int clientIndex=0;clientIndex<parent2Route.size();clientIndex++)
				{
					int node = parent2Route.get(clientIndex);
					if(temp2.periodAssignment[period][node])
						child2Route.add(node);
				}
			}
			
			
			for(int client=0;client<problemInstance.customerCount;client++)
			{
				//repair offspring route 1
				if(temp1.periodAssignment[period][client]==true)
				{
					if(doesRouteContainThisClient(problemInstance, temp1, period, client)==false)
					{
						int vehicle = temp1.mostProbableRoute(client);
						temp1.routes.get(period).get(vehicle).add(client);
					}
				}
				//repair offspring route 2

				if(temp2.periodAssignment[period][client]==true)
				{
					if(doesRouteContainThisClient(problemInstance, temp2, period, client)==false)
					{
						temp2.routes.get(period).get(temp2.mostProbableRoute(client)).add(client);
					}
				}
			}
			
		}
		
	}
		
	static void crossOver_Uniform_Uniform(ProblemInstance problemInstance,Individual parent1,Individual parent2,Individual child1,Individual child2)
	{
		//with 50% probability swap parents
		int ran = Utility.randomIntInclusive(1);
		if(ran ==1)
		{
			Individual temp = parent1;
			parent1 = parent2;
			parent2 = temp;
		}
		
		uniformCrossoverForPeriodAssignment(child1,child2,parent1, parent2,problemInstance);
		uniformCrossoverForRoutes(child1, child2, parent1, parent2, problemInstance);
		
		
		//update cost and penalty
		child1.calculateCostAndPenalty();
		child2.calculateCostAndPenalty();
	}

	boolean validationTest()
	{
		// 1. All client match their frequency 
		// 2. All client only served once in a period
		// 3. 
		
		
		// CHECKING IF FREQUENCY RESTRICTION IS MET OR NOT
		for(int client=0;client<problemInstance.customerCount;client++)
		{
			int freq=0;

			for(int period=0; period<problemInstance.periodCount;period++)
			{
				if(doesRouteContainThisClient(problemInstance, this, period, client))	freq++;
			}
			
			if(problemInstance.frequencyAllocation[client] != freq) return false;
		}
		
		
		// 2. All client only served once in a period
		for(int client=0;client<problemInstance.customerCount;client++)
		{
			for(int period=0; period<problemInstance.periodCount;period++)
			{
				boolean present = periodAssignment[period][client];
				int count = numberOfTimesClientGetsServedInAPeriod(problemInstance, this, period, client);
				if(present== true)
				{
					if(count != 1) return false;
				}
				else
				{
					if(count != 0) return false;
				}
			}	
		}

		return true;
	}
	
	/**
	 * Checks if the client is present in any route or not for the specified period
	 * @param problemInstance
	 * @param individual
	 * @param period
	 * @param client
	 * @return true if client is present in some route <br/> else false
	 */
	private static boolean doesRouteContainThisClient(ProblemInstance problemInstance, Individual individual, int period, int client)
	{

		for(int vehicle=0;vehicle<problemInstance.vehicleCount;vehicle++)
		{
			if(individual.routes.get(period).get(vehicle).contains(client))
			{
				return true;
			}
		}	
		return false;
	}
	
	/**
	 * Checks if how many times the client is present in any route for the specified period
	 * @param problemInstance
	 * @param individual
	 * @param period
	 * @param client
	 * @return 0 if client is not present in some route <br/> 
	 * 1 if client is present exactly once in that period <br/> 
	 * 2 if client is present more than once in that period
	 */
	private static int numberOfTimesClientGetsServedInAPeriod(ProblemInstance problemInstance, Individual individual, int period, int client)
	{
		int count=0;
		for(int vehicle=0;vehicle<problemInstance.vehicleCount;vehicle++)
		{
			int first = individual.routes.get(period).get(vehicle).indexOf(client);
			int last = individual.routes.get(period).get(vehicle).lastIndexOf(client);
			
			if(first != -1)
			{
				if(first==last) count++;
				else count+=2;
			}
		}	
		return count;
	}

	/**
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
	
	*/
	


	
}
