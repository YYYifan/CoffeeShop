package hw3;

import java.util.HashSet;
import java.util.List;

import hw3.SimulationEvent;
import hw3.SimulationEvent.EventType;

/**
 * Validates a simulation
 */
public class Validate {
 

	// Helper method for validating the simulation
	private static boolean check(boolean check, String message) {
		if (!check) {
			System.err.println("SIMULATION INVALID : " + message);
			return false;
		}
		return true;
	}

	/**
	 * Validates the given list of events is a valid simulation. Returns true if the
	 * simulation is valid, false otherwise.
	 *
	 * @param events
	 *            - a list of events generated by the simulation in the order they
	 *            were generated.
	 *
	 * @returns res - whether the simulation was valid or not
	 */
	public static boolean validateSimulation(List<SimulationEvent> events, int numCustomers, int numCooks,
			int numTables, int machineCapacity) {

		boolean flag = true;
		/*
		 * In hw3 you will write validation code for things such as: 
		 * /Should not have more eaters than specified 
		 * /Should not have more cooks than specified 
		 * /The coffee shop capacity should not be exceeded 
		 * /The capacity of each machine should not be exceeded 
		 * /Eater should not receive order until cook completes it
		 * /Eater should not leave coffee shop until order is received 
		 * /Eater should not place more than one order 
		 * /Cook should not work on order before it is placed
		 */
		flag = check(events.get(0).event == SimulationEvent.EventType.SimulationStarting,
				"Simulation didn't start with initiation event") && flag;
		flag = check(events.get(events.size() - 1).event == SimulationEvent.EventType.SimulationEnded,
				"Simulation didn't end with termination event") && flag;
		//Should not have more eaters than specified 
		flag = check(customerNumTest(events,numCustomers),"The number of eaters exceeded than specified") && flag;
		//The coffee shop capacity should not be exceeded 
		flag = check(restaurantCapacityCheck(events, numTables), "The number of customers exceeded number of tables") && flag;
		//Should not have more cooks than specified 
		flag = check(cookNumCheck(events,numCooks),"The number of cooks exceeded than specified") && flag;
		//The capacity of each machine should not be exceeded 
		flag = check(machineCapacityCheck(events, machineCapacity),"Machine capacity exceeded") && flag;
		//Eater should not receive order until cook completes it
		flag = check(cookCompletesBeforeCustomerReceives(events, numCustomers),
				"Customer received order before cook completed") && flag;
		//Eater should not leave coffee shop until order is received 
		flag = check(customerLeaveTest(events, numCustomers), "Customer left before receiving order") && flag;
		//Eater should not place more than one order 
		flag = check(numOfOrderTest(events, numCustomers), "Eater place more than one order") && flag;
		//Cook should not work on order before it is placed
		flag = check(customerBeforeCookOrders(events), "Cook received an order before a customer placed it.") && flag;
		
		
		
		flag = check(customerEnteredBeforePlacing(events, numCustomers),
				"Customer ordered before entering restaurant") && flag;
		flag = check(customerReceivedBeforeLeaving(events, numCustomers),
				"Customer received order before cook finished") && flag;
		flag = check(machineFinishesFoodBeforeCookFinishesFood(events, numCustomers),
				"A machine finished food before cook finished food") && flag;
		flag = check(allCustomersServed(events, numCustomers), "Not all customers were served") && flag;
		flag = check(cookTakesOrderBeforeCooksStartsFood(events), "Cook started food before cook took the order") && flag;
		flag = check(cookStartsFoodBeforeMachine(events),
				"A machine started cooking a food item before a cook started that item") && flag;
		flag = check(machineStartsFoodBeforeFinishes(events),
				"A machine finished a food item before the machine started that item") && flag;
		flag = check(cookFinishesFoodsBeforeCompletesOrder(events),
				"A cook finished an order before the cook finished all the food in that order") && flag;
		return flag;
	}

	private static boolean customerNumTest(List<SimulationEvent> events, int numCustomers) {
		int numCustomer = 0;
		boolean result = false;
		for(SimulationEvent event : events) {
			if(event.event == SimulationEvent.EventType.CustomerEnteredCoffeeShop) {
				numCustomer++;
			}
		}
		if(numCustomer == numCustomers) result = true;
		return result;
	}
	
	private static boolean cookNumCheck(List<SimulationEvent> events, int numCooks) {
		boolean result = false;
		int numOfCook = 0;
		for(SimulationEvent e : events) {
			if(e.event == SimulationEvent.EventType.CookStarting) numOfCook++;
		}
		if(numOfCook == numCooks) result = true;
		return result;
	}
	
	private static boolean numOfOrderTest(List<SimulationEvent> events, int numCustomers) {
		boolean result = false;
		int numOfOrder = 0;
		for(SimulationEvent e : events) {
			if(e.event == SimulationEvent.EventType.CustomerPlacedOrder) numOfOrder++;
		}
		if(numOfOrder == numCustomers) result = true;
		return result;
	}
	private static boolean customerLeaveTest(List<SimulationEvent> events, int numCustomers) {
		// Customer should not leave coffee shop until order is received test
		int orderReceivedIndex = 0;
		int customerLeftIndex = 0;
		for (int i = 0; i < numCustomers; i++) {
			for (int j = 0; j < events.size(); j++) {
				if (events.get(j).toString().contains("Customer " + i)) {
					if (events.get(j).event == SimulationEvent.EventType.CustomerLeavingCoffeeShop) {
						customerLeftIndex = j;
					}
					if (events.get(j).event == SimulationEvent.EventType.CustomerReceivedOrder) {
						orderReceivedIndex = j;
					}
				}
			}
			if (orderReceivedIndex > customerLeftIndex) {
				return false;
			}
		}
		return true;
	}

	private static boolean restaurantCapacityCheck(List<SimulationEvent> events, int numTables) {
		boolean result = true;
		int currNumCustomers = 0;
		// Max Customers Test
		for (SimulationEvent e : events) {
			if (e.event == SimulationEvent.EventType.CustomerEnteredCoffeeShop) {
				currNumCustomers++;
			}
			if (e.event == SimulationEvent.EventType.CustomerLeavingCoffeeShop) {
				currNumCustomers--;
			}
			if (currNumCustomers > numTables) {
				result = false;
			}
		}
		return result;
	}

	private static boolean machineCapacityCheck(List<SimulationEvent> events, int machineCapacity) {
		boolean resultBurgers = true;
		int currNumBurgers = 0;
		boolean resultFries = true;
		int currNumFries = 0;
		boolean resultCoffees = true;
		int currNumCoffees = 0;
		// Machine Capacity Test
		for (SimulationEvent e : events) {
			if (e.event == SimulationEvent.EventType.MachineStartingFood) {
				if (e.machine.machineName.equals("Grill")) {
					currNumBurgers++;
				} else if (e.machine.machineName.equals("Fryer")) {
					currNumFries++;
				} else if (e.machine.machineName.equals("CoffeeMaker2000")) {
					currNumCoffees++;
				}
			}
			if (e.event == SimulationEvent.EventType.MachineDoneFood) {
				if (e.machine.machineName.equals("Grill")) {
					currNumBurgers--;
				} else if (e.machine.machineName.equals("Fryer")) {
					currNumFries--;
				} else if (e.machine.machineName.equals("CoffeeMaker2000")) {
					currNumCoffees--;
				}
			}
			if (currNumBurgers > machineCapacity) {
				resultBurgers = false;
			}
			if (currNumFries > machineCapacity) {
				resultFries = false;
			}
			if (currNumCoffees > machineCapacity) {
				resultCoffees = false;
			}
		}

		return check(resultBurgers, "Exceeded grill capacity") && check(resultFries, "Exceeded fryer capacity")
				&& check(resultCoffees, "Exceeded coffee maker capacity");
	}

	private static boolean customerEnteredBeforePlacing(List<SimulationEvent> events, int numCustomers) {
		boolean[] customerEntered = new boolean[numCustomers];
		boolean[] customerPlacedOrder = new boolean[numCustomers];
		boolean result = true;
		for (SimulationEvent e : events) {
			if (e.event == EventType.CustomerEnteredCoffeeShop) {
				String name = e.customer.toString();
				int custNum = Integer.parseInt(name.substring(9));
				customerEntered[custNum] = true;
			}
			if (e.event == EventType.CustomerPlacedOrder) {
				String name = e.customer.toString();
				int custNum = Integer.parseInt(name.substring(9));
				customerPlacedOrder[custNum] = true;
				if (customerPlacedOrder[custNum] && !customerEntered[custNum]) {
					result = false;
				}
			}
		}
		return result;
	}

	private static boolean machineFinishesFoodBeforeCookFinishesFood(List<SimulationEvent> events, int numOrders) {
		int numBurgersFinished = 0; // Number of burgers finished by Grill
		int numBurgersTaken = 0; // Number of burgers finished by Cook
		int numFriesFinished = 0; // Number of fries finished by Fryer
		int numFriesTaken = 0; // Number of fries finished by Cook
		int numCoffeesFinished = 0; // Number of coffees finished by
									// CoffeeMaker2000
		int numCoffeesTaken = 0; // Number of coffees finished by Cook
		boolean result = true;
		for (SimulationEvent e : events) {
			if (e.event == EventType.CookFinishedFood) {
				if (e.food == FoodType.burger) {
					numBurgersTaken++;
				} else if (e.food == FoodType.fries) {
					numFriesTaken++;
				} else if (e.food == FoodType.coffee) {
					numCoffeesTaken++;
				}
				if (numBurgersTaken > numBurgersFinished || numFriesTaken > numFriesFinished
						|| numCoffeesTaken > numCoffeesFinished) {
					result = false;
					break;
				}
			} else if (e.event == EventType.MachineDoneFood) {
				if (e.food == FoodType.burger) {
					numBurgersFinished++;
				} else if (e.food == FoodType.fries) {
					numFriesFinished++;
				} else if (e.food == FoodType.coffee) {
					numCoffeesFinished++;
				} else {
					throw new IllegalStateException("Invalid machine name: " + e.machine.machineName);
				}
			}
		}
		return result;
	}

	private static boolean cookCompletesBeforeCustomerReceives(List<SimulationEvent> events, int numOrders) {
		boolean[] customerReceived = new boolean[numOrders];
		boolean[] cookCompleted = new boolean[numOrders];
		boolean result = true;
		for (SimulationEvent e : events) {
			if (e.event == EventType.CustomerReceivedOrder) {
				int order = e.orderNumber;
				customerReceived[order] = true;
				if (customerReceived[order] && !cookCompleted[order]) {
					result = false;
				}
			}
			if (e.event == EventType.CookCompletedOrder) {
				int order = e.orderNumber;
				cookCompleted[order] = true;
			}
		}
		return result;
	}

	private static boolean customerReceivedBeforeLeaving(List<SimulationEvent> events, int numCustomers) {
		boolean[] customerReceived = new boolean[numCustomers];
		boolean[] customerLeft = new boolean[numCustomers];
		boolean result = true;
		for (SimulationEvent e : events) {
			if (e.event == EventType.CustomerReceivedOrder) {
				String name = e.customer.toString();
				int custNum = Integer.parseInt(name.substring(9));
				customerReceived[custNum] = true;
			}
			if (e.event == EventType.CustomerLeavingCoffeeShop) {
				String name = e.customer.toString();
				int custNum = Integer.parseInt(name.substring(9));
				customerLeft[custNum] = true;
				if (customerLeft[custNum] && !customerReceived[custNum]) {
					result = false;
				}
			}
		}
		return result;
	}

	private static boolean allCustomersServed(List<SimulationEvent> events, int numCustomers) {
		boolean result = true;
		boolean[] customerServed = new boolean[numCustomers];
		for (SimulationEvent e : events) {
			if (e.event == EventType.CustomerLeavingCoffeeShop) {
				String name = e.customer.toString();
				int custNum = Integer.parseInt(name.substring(9));
				customerServed[custNum] = true;
			}
		}
		for (boolean served : customerServed) {
			if (!served) {
				result = false;
				break;
			}
		}
		return result;
	}

	/**
	 * Tests that cooks don't take orders before customers place them
	 * 
	 * @param events
	 * @return
	 */
	private static boolean customerBeforeCookOrders(List<SimulationEvent> events) {
		HashSet<Integer> set = new HashSet<Integer>();
		boolean passed = true;

		for (SimulationEvent event : events) {
			if (event.event == SimulationEvent.EventType.CustomerPlacedOrder) {
				set.add(event.orderNumber);
			}
			if (event.event == SimulationEvent.EventType.CookReceivedOrder) {
				if (!set.contains(event.orderNumber)) {
					passed = false;
				}
			}

		}
		return passed;
	}

	/**
	 * Tests that cooks don't start food for an order that hasn't been received
	 * 
	 * @param events
	 * @return
	 */
	private static boolean cookTakesOrderBeforeCooksStartsFood(List<SimulationEvent> events) {
		HashSet<Integer> set = new HashSet<Integer>();
		boolean passed = true;

		for (SimulationEvent event : events) {
			if (event.event == SimulationEvent.EventType.CookReceivedOrder) {
				set.add(event.orderNumber);
			}
			if (event.event == SimulationEvent.EventType.CookStartedFood) {
				if (!set.contains(event.orderNumber)) {
					passed = false;
				}
			}

		}
		return passed;
	}

	/**
	 * Tests that cooks don't start food for an order that hasn't been received
	 * 
	 * @param events
	 * @return
	 */
	private static boolean cookStartsFoodBeforeMachine(List<SimulationEvent> events) {
		boolean passed = true;

		int numBurgers = 0;
		int numFries = 0;
		int numCoffee = 0;

		for (SimulationEvent event : events) {
			if (event.event == SimulationEvent.EventType.CookStartedFood) {
				if (event.food == FoodType.burger) {
					numBurgers++;
				}
				if (event.food == FoodType.fries) {
					numFries++;
				}
				if (event.food == FoodType.coffee) {
					numCoffee++;
				}
			}
			if (event.event == SimulationEvent.EventType.MachineStartingFood) {
				if (event.food == FoodType.burger) {
					numBurgers--;
				}
				if (event.food == FoodType.fries) {
					numFries--;
				}
				if (event.food == FoodType.coffee) {
					numCoffee--;
				}
			}

			if (numBurgers < 0 || numFries < 0 || numCoffee < 0) {
				passed = false;
			}
		}
		return passed;
	}

	/**
	 * Tests that cooks don't start food for an order that hasn't been received
	 * 
	 * @param events
	 * @return
	 */
	private static boolean machineStartsFoodBeforeFinishes(List<SimulationEvent> events) {
		boolean passed = true;

		int numBurgers = 0;
		int numFries = 0;
		int numCoffee = 0;

		for (SimulationEvent event : events) {
			if (event.event == SimulationEvent.EventType.MachineStartingFood) {
				if (event.food == FoodType.burger) {
					numBurgers++;
				}
				if (event.food == FoodType.fries) {
					numFries++;
				}
				if (event.food == FoodType.coffee) {
					numCoffee++;
				}
			}
			if (event.event == SimulationEvent.EventType.MachineDoneFood) {
				if (event.food == FoodType.burger) {
					numBurgers--;
				}
				if (event.food == FoodType.fries) {
					numFries--;
				}
				if (event.food == FoodType.coffee) {
					numCoffee--;
				}
			}

			if (numBurgers < 0 || numFries < 0 || numCoffee < 0) {
				passed = false;
			}
		}
		return passed;
	}

	/**
	 * Tests that cooks don't start food for an order that hasn't been received
	 * 
	 * @param events
	 * @return
	 */
	private static boolean cookFinishesFoodsBeforeCompletesOrder(List<SimulationEvent> events) {
		boolean passed = true;
		HashSet<Integer> set = new HashSet<Integer>();

		for (SimulationEvent event : events) {
			if (event.event == SimulationEvent.EventType.CookCompletedOrder) {
				set.add(event.orderNumber);
			}
			if (event.event == SimulationEvent.EventType.CookFinishedFood) {
				if (set.contains(event.orderNumber)) {
					passed = false;
				}
			}
		}
		return passed;
	}
}
