import java.util.ArrayList;


public class Uniform_VariedEdgeRecombnation_Crossover 
{
	private static  int DEPOT; 
	static ProblemInstance problemInstance;
	static void crossOver_Uniform_VariedEdgeRecombination(ProblemInstance pi,Individual parent1,Individual parent2,Individual child)
	{
		problemInstance = pi;
		DEPOT = problemInstance.customerCount;
		
		//with 50% probability swap parents
		int ran = Utility.randomIntInclusive(1);
		if(ran ==1)
		{
			Individual temp = parent1;
			parent1 = parent2;
			parent2 = temp;
		}
		
		UniformCrossoverPeriodAssigment.uniformCrossoverForPeriodAssignment(child,parent1, parent2,problemInstance);
		variedEdgeRecombinationCrossoverForRoutes(child, parent1, parent2);
		//update cost and penalty
		child.calculateCostAndPenalty();
	}

	
	private static void variedEdgeRecombinationCrossoverForRoutes(Individual child, Individual parent1, Individual parent2)
	{
		int coin;

		for(int period=0;period<problemInstance.periodCount;period++)
		{
			 
			// Step 1. Assign client to vehicle
			// Step 2. Create Neighbour/Adjacency List
			// Step 3. Create Route For Each Vehicle
			
			//assignedVehicle[c] => v means, client c is under vehicle v
			int[] assignedVehicle = new int[problemInstance.customerCount];
			for(int i=0;i<problemInstance.customerCount;i++) assignedVehicle[i] = -1;
			
			//numberOfCustomerServed[v] -> n means, vehicle v serves n clients
			int[] numberOfCustomerServed = new int[problemInstance.vehicleCount];
			
			
			ArrayList<ArrayList<Integer>> adjacencyList = new ArrayList<ArrayList<Integer>>();
			
			for(int client =0;client<problemInstance.customerCount;client++)
			{
				adjacencyList.add(new ArrayList<Integer>());
			}
			
			ArrayList<ArrayList<Integer>> vehicleAdjacencyList = new ArrayList<ArrayList<Integer>>();
			
			for(int vehicle =0;vehicle<problemInstance.vehicleCount;vehicle++)
			{
				vehicleAdjacencyList.add(new ArrayList<Integer>());
			}
			
			
			
			assignClientToVehicle(child,parent1,parent2,assignedVehicle, numberOfCustomerServed,period);
			createAdjacencyList(adjacencyList, vehicleAdjacencyList, period, assignedVehicle,child,parent1,parent2);
			
			
			boolean print=true;
			if(period==0 && print)
			{
				for(int client=0;client<problemInstance.customerCount;client++)
					problemInstance.out.print(client+" -> "+assignedVehicle[client]+" ");
				problemInstance.out.println("");
			

				for(int client=0;client<problemInstance.customerCount;client++)
				{				
					problemInstance.out.print("Neigbours of "+client+" : ");
					for(int i=0;i< adjacencyList.get(client).size();i++)
						problemInstance.out.print(adjacencyList.get(client).get(i)+" ");
					problemInstance.out.println("");
					
				}		
						
			}
		}
	}
	
	
	
	private static void assignClientToVehicle(Individual child, Individual parent1, Individual parent2, int[] assignedVehicle, int[] numberOfCustomerServed,int period) 
	{
		for(int client=0; client<problemInstance.customerCount;client++)
		{
			if(child.periodAssignment[period][client]==false) continue;
			
			int vehicle1 = RouteUtilities.assignedVehicle(parent1, client, period, problemInstance);
			int vehicle2 = RouteUtilities.assignedVehicle(parent2, client, period, problemInstance);
			
			int chosenVehicle=500000;
			
			if(vehicle1==-1 && vehicle2==-1) System.out.println("ERROR!!!! NEVER SHOULD HAPPEN!!!!!!!!!! in varied edge recombination op....");
	
			else if(vehicle1==-1)
				chosenVehicle = vehicle2;
			else if(vehicle2==-1)
				chosenVehicle = vehicle1; 
			else 
			{
				int coin = Utility.randomIntInclusive(1);
				if(coin==0) chosenVehicle = vehicle1;
				else chosenVehicle = vehicle2;
			}
			
			assignedVehicle[client]=chosenVehicle;
			numberOfCustomerServed[chosenVehicle]++;
		}
	}

	private static void createAdjacencyList(ArrayList<ArrayList<Integer>> adjacencyList, ArrayList<ArrayList<Integer>> vehicleAdjacencyList, int period, int[] assignedVehicle,Individual child, Individual parent1, Individual parent2)
	{
		for(int client=0;client<problemInstance.customerCount;client++)
		{
			if(child.periodAssignment[period][client]==false)continue;
			updateNeighbourLists(parent1, child, adjacencyList, vehicleAdjacencyList, client, period, assignedVehicle);
			updateNeighbourLists(parent2, child, adjacencyList, vehicleAdjacencyList, client, period, assignedVehicle);
		}
	}
	
	private static void updateNeighbourLists(Individual parent,Individual child, ArrayList<ArrayList<Integer>> adjacencyList, ArrayList<ArrayList<Integer>> vehicleAdjacencyList, int client, int period, int[] assignedVehicle )
	{
		int vehicle = RouteUtilities.assignedVehicle(parent, client, period, problemInstance);
		if(vehicle==-1) return;
		
		ArrayList<Integer> route = parent.routes.get(period).get(vehicle);
		int index = route.indexOf(client); 
		
		if(index ==- 1) System.out.println("ERROR! NEVER SHOULD HAPPEN !!! CROSSOVER!!");
		
		
		if(index==0 || index == route.size()-1)
		{
			if(!adjacencyList.get(client).contains(DEPOT))
				adjacencyList.get(client).add(DEPOT);
			vehicleAdjacencyList.get(vehicle).add(client);
		}
	
		if(index>0)
		{
			int neighbour = route.get(index-1);
			int neighbrVehicle = RouteUtilities.assignedVehicle(parent, neighbour, period, problemInstance);
			if( neighbrVehicle == vehicle && child.periodAssignment[period][neighbour]==true)
			{
				if(!adjacencyList.get(client).contains(neighbour))
					adjacencyList.get(client).add(neighbour);
			}
		}
		
		if(index+1<route.size())
		{
			int neighbour = route.get(index+1);
			int neighbrVehicle = RouteUtilities.assignedVehicle(parent, neighbour, period, problemInstance);
			if( neighbrVehicle == vehicle && child.periodAssignment[period][neighbour]==true)
			{
				if(!adjacencyList.get(client).contains(neighbour))
					adjacencyList.get(client).add(neighbour);
			}
		}
		
	}
	
}
