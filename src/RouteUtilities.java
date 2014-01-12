import java.util.ArrayList;


public class RouteUtilities {
	
	/**
	 * Checks if the client is present in any route or not,  for the specified period
	 * @param problemInstance
	 * @param individual
	 * @param period
	 * @param client
	 * @return true if client is present in some route <br/> else false
	 */
	static boolean doesRouteContainThisClient(ProblemInstance problemInstance, Individual individual, int period, int client)
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
	 * Returns the vehicleNumber which serves the client  
	 * @param individual
	 * @param client
	 * @param period
	 * @param problemInstance
	 * @return vehicleNumber, v ; if the client is present in the period
	 * <br/> -1 if not present
	 */	
	static int assignedVehicle(Individual individual, int client, int period,ProblemInstance problemInstance)
	{
		for(int vehicle=0;vehicle<problemInstance.vehicleCount;vehicle++)
		{
			ArrayList<Integer> route = individual.routes.get(period).get(vehicle);
			if(route.contains(client)) return vehicle;
		}	
		return -1;
	}

}
