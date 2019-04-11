package hw3;

import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * Simulation is the main class used to run the simulation. You may add any
 * fields (static or instance) or any methods you wish.
 */
public class Simulation {
	// List to track simulation events during simulation
	public static List<SimulationEvent> events;

	public static List<Customer> numOfCustomers = new ArrayList<Customer>();
	public static List<Customer> orderList = new ArrayList<Customer>();
	public static Map<Customer, Boolean> customer_recieve_order = new HashMap<Customer, Boolean>();

	public static Machine burger_machine;
	public static Machine fries_machine;
	public static Machine coffee_machine;

	// invariant: 0<= numOfCustomers <= numTables;
	// invariant: 0<= orderList
	// preconditon:none
	// post condition: adding an instance of simulationEvent into a list and print
	// it.
	// exception:none
	/**
	 * Used by other classes in the simulation to log events
	 * 
	 * @param event
	 */
	
	public static void logEvent(SimulationEvent event) {
		events.add(event);
		System.out.println(event);
	}

	/**
	 * Function responsible for performing the simulation. Returns a List of
	 * SimulationEvent objects, constructed any way you see fit. This List will be
	 * validated by a call to Validate.validateSimulation. This method is called
	 * from Simulation.main(). We should be able to test your code by only calling
	 * runSimulation.
	 * 
	 * Parameters:
	 * 
	 * @param numCustomers
	 *            the number of customers wanting to enter the coffee shop
	 * @param numCooks
	 *            the number of cooks in the simulation
	 * @param numTables
	 *            the number of tables in the coffe shop (i.e. coffee shop capacity)
	 * @param machineCapacity
	 *            the capacity of all machines in the coffee shop
	 * @param randomOrders
	 *            a flag say whether or not to give each customer a random order
	 *
	 */
	// preconditon:none

	// post condition: This method firstly starts to generate instances of
	// simulationEvent, initialize machine objects and
	// cook threads. Then build up customers threads and generates orders for them.
	// Finally, interrupt cooks and shut down machines after customers are finished.
	// The simulation ended.

	// exception:none
	public static List<SimulationEvent> runSimulation(int numCustomers, int numCooks, int numTables,
			int machineCapacity, boolean randomOrders) {

		// This method's signature MUST NOT CHANGE.

		// We are providing this events list object for you.
		// It is the ONLY PLACE where a concurrent collection object is
		// allowed to be used.
		events = Collections.synchronizedList(new ArrayList<SimulationEvent>());
		Random random = new Random();

		// Start the simulation
		logEvent(SimulationEvent.startSimulation(numCustomers, numCooks, numTables, machineCapacity));

		// Set things up you might need

		// Start up machines
		// initialize machine grill with name, food type and capacity
		// initialize machine frier with name, food type and capacity
		// initialize machine coffee maker with name, food type and capacity
		fries_machine = new Machine("fries_machine", FoodType.fries, machineCapacity);
		burger_machine = new Machine("burger_machine", FoodType.burger, machineCapacity);
		coffee_machine = new Machine("coffee_machine", FoodType.coffee, machineCapacity);
		logEvent(SimulationEvent.machineStarting(fries_machine, FoodType.fries, machineCapacity));
		logEvent(SimulationEvent.machineStarting(burger_machine, FoodType.burger, machineCapacity));
		logEvent(SimulationEvent.machineStarting(coffee_machine, FoodType.coffee, machineCapacity));

		// Let cooks in
		// initialize an thread arrayList.
		// then initialize each thread with putting cook object in
		// call start function.
		Thread[] cooks = new Thread[numCooks];
		for (int i = 0; i < numCooks; i++) {
			cooks[i] = new Thread(new Cook("cook" + i));
			cooks[i].start();
		}

		// Build the customers.
		Thread[] customers = new Thread[numCustomers];
		LinkedList<Food> order;
		if (!randomOrders) {
			order = new LinkedList<Food>();
			order.add(FoodType.burger);
			order.add(FoodType.fries);
			order.add(FoodType.fries);
			order.add(FoodType.coffee);
			for (int i = 0; i < customers.length; i++) {
				customers[i] = new Thread(
						new Customer("Customer " + i, order, random.nextInt(3) + 1, random.nextInt(10)));
			}
		} else {
			for (int i = 0; i < customers.length; i++) {
				Random rnd = new Random(27);
				int burgerCount = rnd.nextInt(3);
				int friesCount = rnd.nextInt(3);
				int coffeeCount = rnd.nextInt(3);
				order = new LinkedList<Food>();
				for (int b = 0; b < burgerCount; b++) {
					order.add(FoodType.burger);
				}
				for (int f = 0; f < friesCount; f++) {
					order.add(FoodType.fries);
				}
				for (int c = 0; c < coffeeCount; c++) {
					order.add(FoodType.coffee);
				}
				customers[i] = new Thread(
						new Customer("Customer " + i, order, random.nextInt(3) + 1, random.nextInt(10)));
			}
		}

		// Now "let the customers know the shop is open" by
		// starting them running in their own thread.
		for (int i = 0; i < customers.length; i++) {
			customers[i].start();
			// NOTE: Starting the customer does NOT mean they get to go
			// right into the shop. There has to be a table for
			// them. The Customer class' run method has many jobs
			// to do - one of these is waiting for an available
			// table...
		}

		try {
			// Wait for customers to finish
			// -- you need to add some code here...
			for (int i = 0; i < customers.length; i++) {
				customers[i].join();
			}

			// Then send cooks home...
			// The easiest way to do this might be the following, where
			// we interrupt their threads. There are other approaches
			// though, so you can change this if you want to.
			for (int i = 0; i < cooks.length; i++)
				cooks[i].interrupt();
			for (int i = 0; i < cooks.length; i++)
				cooks[i].join();

		} catch (InterruptedException e) {
			System.out.println("Simulation thread interrupted.");
		}

		// Shut down machines
		logEvent(SimulationEvent.machineEnding(fries_machine));
		logEvent(SimulationEvent.machineEnding(burger_machine));
		logEvent(SimulationEvent.machineEnding(coffee_machine));

		// Done with simulation
		logEvent(SimulationEvent.endSimulation());

		return events;
	}

	/**
	 * Entry point for the simulation.
	 *
	 * @param args
	 *            the command-line arguments for the simulation. There should be
	 *            exactly four arguments: the first is the number of customers, the
	 *            second is the number of cooks, the third is the number of tables
	 *            in the coffee shop, and the fourth is the number of items each
	 *            cooking machine can make at the same time.
	 */
	public static void main(String args[]) throws InterruptedException {
		// Parameters to the simulation
		/*
		 * if (args.length != 4) { System.err.
		 * println("usage: java Simulation <#customers> <#cooks> <#tables> <capacity> <randomorders"
		 * ); System.exit(1); } int numCustomers = new Integer(args[0]).intValue(); int
		 * numCooks = new Integer(args[1]).intValue(); int numTables = new
		 * Integer(args[2]).intValue(); int machineCapacity = new
		 * Integer(args[3]).intValue(); boolean randomOrders = new Boolean(args[4]);
		 */
		int numCustomers = 20;
		int numCooks = 10;
		int numTables = 10;
		int machineCapacity = 8;
		boolean randomOrders = true;

		// Run the simulation and then
		// feed the result into the method to validate simulation.
		System.out.println("Did it work? " + Validate.validateSimulation(
				runSimulation(
						numCustomers, numCooks, numTables, machineCapacity, randomOrders), 
						numCustomers, numCooks,numTables, machineCapacity));
	}

}
