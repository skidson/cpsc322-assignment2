package ca.ubc.cpsc322.scheduler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A stub for your second scheduler
 */
public class FavouriteSLSScheduler extends Scheduler {
	private SchedulingInstance pInstance;
	private static double MUTATION_RATE = 0.75;
	private static double APOCALYPSE_RATE = 0.01;
	
	// For finding optimal rates.
	public static void main(String args[]) throws IOException {
		Scheduler scheduler = new FavouriteSLSScheduler();
		Evaluator evaluator = new OrigEvaluator();
		for (int m = 0; m < 5; m++) {
			System.out.println("MUTATION RATE " + m*0.2);
			((FavouriteSLSScheduler)scheduler).setMutationRate(m*0.2);
			for (int a = 0; a < 5; a++) {
				System.out.println("APOCALYPSE RATE " + a*0.1);
				((FavouriteSLSScheduler)scheduler).setApocalypseRate(m*0.1);
				// Large instances with 10 rooms, 50 courses, 500 students, 20 timeslots
				SchedulerTester.test(scheduler, evaluator, "instances/large_1.txt", 0, 10, false);
				SchedulerTester.test(scheduler, evaluator, "instances/large_2.txt", 0, 10, false);
				SchedulerTester.test(scheduler, evaluator, "instances/large_3.txt", 0, 10, false);
				SchedulerTester.test(scheduler, evaluator, "instances/large_4.txt", 0, 10, false);
				SchedulerTester.test(scheduler, evaluator, "instances/large_5.txt", 0, 10, false);
			}
		}
	}
	
	/**
	 * @see scheduler.Scheduler#authorsAndStudentIDs()
	 */
	public String authorsAndStudentIDs() {
		return ("Jeffrey Payan \n18618074 \nStephen Kidson \n15345077");
	}

	/**
	 * @throws Exception 
	 * @see scheduler.Scheduler#schedule(scheduler.SchedulingInstance)
	 */
	public ScheduleChoice[] solve(SchedulingInstance pInstance) throws Exception {
		this.pInstance = pInstance;
		
		// Populate the domain
		List<ScheduleChoice> workingDomain = new ArrayList<ScheduleChoice>();
		for (int room = 0; room < pInstance.numRooms; room++) {
			for (int timeslot = 0; timeslot < pInstance.numTimeslots; timeslot++) {
				ScheduleChoice choice = new ScheduleChoice();
				choice.room = room;
				choice.timeslot = timeslot;
				workingDomain.add(choice);
			}
		}
		
		final List<ScheduleChoice> WORLD = copy(workingDomain);
		
		// Initialize to a random variable assignment
		ScheduleChoice[][] humanity = apocalypse(WORLD);
		ScheduleChoice[] adam = humanity[0];
		ScheduleChoice[] eve = humanity[1];
		int min = Integer.MAX_VALUE;
		while(!timeIsUp() && min > 0) {
			if (r.nextDouble() < APOCALYPSE_RATE) {
				humanity = apocalypse(WORLD);
				adam = humanity[0].clone();
				eve = humanity[1].clone();
			}
			
			// Finds two prime specimens and nominates them Adam and Eve.
			for (int i = 0; i < humanity.length; i++) {
				int score = evaluator.violatedConstraints(pInstance, humanity[i]);
				if (score < min) {
					min = score;
					eve = adam.clone();
					adam = humanity[i].clone();
				}
			}
			
			// Start new generation
			humanity[0] = adam.clone();
			humanity[1] = eve.clone();
			humanity[2] = sex(adam, eve, copy(WORLD));
			humanity[3] = sex(eve, adam, copy(WORLD));
			min = evaluator.violatedConstraints(pInstance, adam);
		}
		return adam;
	}
	
	/**
	 * Copies the contents of the passed List using each element's clone
	 * method and returns a deep copy of the List.
	 * @param domain
	 * @return
	 */
	private List<ScheduleChoice> copy(List<ScheduleChoice> domain) {
		List<ScheduleChoice> copy = new ArrayList<ScheduleChoice>();
		for(ScheduleChoice choice : domain)
			copy.add(choice.clone());
		return copy;
	}
	
	/**
	 * Creates an array initialized with the values from the specified domain.
	 * @param domain a List of acceptable values.
	 * @param unique whether values in the domain are unique (i.e. duplicates may exist).
	 * @return
	 */
	private ScheduleChoice[] restart(List<ScheduleChoice> domain, boolean unique) {
		ScheduleChoice[] schedule = new ScheduleChoice[pInstance.numCourses];
		for(int i = 0; i < pInstance.numCourses; i++) {
			if (unique)
				schedule[i] = domain.remove(r.nextInt(domain.size()));
			else
				schedule[i] = domain.get(r.nextInt(domain.size()));
		}
		return schedule;
	}
	
	/**
	 * Produces a child that inherits aspects of both parents & random defects
	 * @param male
	 * @param female
	 * @param domain
	 * @return
	 */
	private ScheduleChoice[] sex(ScheduleChoice[] male, 
			ScheduleChoice[] female, 
			List<ScheduleChoice> domain) {
		ScheduleChoice[] child = new ScheduleChoice[male.length];
		for (int i = 0; i < male.length; i += 2)
			child[i] = male[i].clone();
		for (int i = 1; i < male.length; i += 2)
			child[i] = female[i].clone();
		while(r.nextDouble() < MUTATION_RATE)
			child[r.nextInt(child.length)] = domain.get(r.nextInt(domain.size())).clone();
		return child;
	}
	
	private ScheduleChoice[][] apocalypse(List<ScheduleChoice> domain) {
		ScheduleChoice[] adam = restart(copy(domain), true), eve = restart(copy(domain), true);
		ScheduleChoice[][] humanity = new ScheduleChoice[4][pInstance.numCourses];
		humanity[0] = adam.clone();
		humanity[1] = eve.clone();
		humanity[2] = sex(adam, eve, copy(domain));
		humanity[3] = sex(eve, adam, copy(domain));
		return humanity;
	}
	
	public static void setMutationRate(double rate) {
		MUTATION_RATE = rate;
	}
	
	public static void setApocalypseRate(double rate) {
		APOCALYPSE_RATE = rate;
	}

}

// Below are various variations we attempted.

//General Local Search Algorithm
// Systematically checks all variables and swaps with a random value from domain
/*for (int i = 0; i < bestSchedule.length; i++) {
	tempSchedule = bestSchedule.clone();
	int index = r.nextInt(workingDomain.size() + bestSchedule.length);
	if (index >= workingDomain.size()) {
		index -= workingDomain.size();
		tempSchedule = swap(tempSchedule, i, index);
		int score = evaluator.violatedConstraints(pInstance, tempSchedule);
		if (score < min) {
			min = score;
			bestChoice = tempSchedule.clone();
		}
	} else {
		ScheduleChoice temp = tempSchedule[i];
		tempSchedule[i] = workingDomain.get(index);
		int score = evaluator.violatedConstraints(pInstance, tempSchedule);
		if (score < min) {
			min = score;
			workingDomain.set(index, temp);
			bestChoice = tempSchedule.clone();
		}
	}
}*/

// Greedy Descent with One Stage Selection and Stagnation Detection
/*int stagnant = DEPTH;
while (evaluator.violatedConstraints(pInstance, tempSchedule) >= Integer.MAX_VALUE ||
		stagnant > 0) {
	tempSchedule = bestSchedule.clone();
	tempSchedule[r.nextInt(tempSchedule.length)] = DOMAIN.get(r.nextInt(DOMAIN.size()));
	int score = evaluator.violatedConstraints(pInstance, tempSchedule);
	if (score < min) {
		min = score;
		bestChoice = tempSchedule.clone();
	} else
		stagnant--;
}*/

// Greedy Descent with Two Stage Selection
// For each course, find which swap results in lowest # conflicts, then set as best
// Checks all neighbours before making a decision.
/*for (int i = 0; i < bestSchedule.length; i++) {
	// Swap with unused options in working domain
	for (ScheduleChoice choice : workingDomain) {
		tempSchedule = bestSchedule.clone();
		tempSchedule[i] = choice;
		int score = evaluator.violatedConstraints(pInstance, tempSchedule);
		if (score < min) {
			min = score;
			bestChoice = tempSchedule.clone();
		}
	}
	// Swap with another course's exam slot
	for (int j = i + 1; j < bestSchedule.length; j++) {
		tempSchedule = bestSchedule.clone();
		tempSchedule = swap(tempSchedule.clone(), i, j);
		int score = evaluator.violatedConstraints(pInstance, tempSchedule);
		if (score < min) {
			min = score;
			bestChoice = tempSchedule.clone();
		}
	}
}*/

// Greedy Descent with Two Stage Selection and Stagnation Detection
/*for (int i = 0; i < bestSchedule.length; i++) {
	int stagnant = DEPTH;
	// Try swapping with unused options in working domain
	for (ScheduleChoice choice : workingDomain) {
		tempSchedule = bestSchedule.clone();
		tempSchedule[i] = choice;
		int score = evaluator.violatedConstraints(pInstance, tempSchedule);
		if (score < min) {
			min = score;
			bestChoice = tempSchedule.clone();
		} else
			stagnant--;
		if (stagnant == 0)
			break;
	}
	// Try swapping with another course's exam slot
	for (int j = i + 1; j < bestSchedule.length; j++) {
		tempSchedule = bestSchedule.clone();
		tempSchedule = swap(tempSchedule.clone(), i, j);
		int score = evaluator.violatedConstraints(pInstance, tempSchedule);
		if (score < min) {
			min = score;
			bestChoice = tempSchedule.clone();
		}  else
			stagnant--;
		if (stagnant == 0)
			break;
	}
}*/

// Greedy Descent with with Two Stage Selection and Simulated Annealing
/*for (int i = 0; i < bestSchedule.length; i++) {
	// Try swapping with unused options in working domain
	for (ScheduleChoice choice : workingDomain) {
		tempSchedule = bestSchedule.clone();
		tempSchedule[i] = choice;
		int score = evaluator.violatedConstraints(pInstance, tempSchedule);
		if (score < min || r.nextDouble() < (score-min)/temperature) {
			min = score;
			bestChoice = tempSchedule.clone();
		}
	}
	// Try swapping with another course's exam slot
	for (int j = i + 1; j < bestSchedule.length; j++) {
		tempSchedule = bestSchedule.clone();
		tempSchedule = swap(tempSchedule.clone(), i, j);
		int score = evaluator.violatedConstraints(pInstance, tempSchedule);
		if (score < min || r.nextDouble() < (score-min)/temperature) {
			min = score;
			bestChoice = tempSchedule.clone();
		}
	}
}
// Annealing schedule
temperature *= 0.90;*/

// Depth-Based Greedy Descent with Two Stage Selection
// Randomly check possible neighbours and transition at first occurrence of improvement up
// to depth
/*for (int i = 0; i < DEPTH; i++) {
	List<ScheduleChoice> tempDomain = copy(workingDomain);
	if (min == 0)
		break;
	int choice = r.nextInt(workingDomain.size() + bestSchedule.length);
	if (choice >= workingDomain.size()) {
		choice -= workingDomain.size();
		tempSchedule = swap(tempSchedule, r.nextInt(tempSchedule.length), choice);
	} else {
		tempDomain = copy(workingDomain);
		tempSchedule = swap(tempSchedule, tempDomain, r.nextInt(tempSchedule.length), choice);
	}
	int score = evaluator.violatedConstraints(pInstance, tempSchedule);
	if (score < min) {
		min = score;
		workingDomain = copy(tempDomain);
		bestChoice = tempSchedule.clone();
	}
}*/

// Greedy Descent with Random Steps and Hard Constraint Requirement
/*while(evaluator.violatedConstraints(pInstance, bestChoice) >= Integer.MAX_VALUE) {
	tempSchedule[r.nextInt(tempSchedule.length)] = DOMAIN.get(r.nextInt(DOMAIN.size()));
	int score = evaluator.violatedConstraints(pInstance, tempSchedule);
	if (score < min) {
		min = score;
		bestChoice = tempSchedule.clone();
	} 
}*/

// Depth-Based Random Walks with Hard Constraint Requirement
/*for (int i = 0; i < DEPTH; i++) {
	do {
		tempSchedule = bestChoice.clone();
		tempSchedule[r.nextInt(tempSchedule.length)] = DOMAIN.get(r.nextInt(DOMAIN.size()));
	} while(evaluator.violatedConstraints(pInstance, tempSchedule) >= Integer.MAX_VALUE);
	bestChoice = tempSchedule.clone();
}*/

// Totally Fucking Random
/*tempSchedule = bestSchedule.clone();
for (int i = 0; i < bestSchedule.length; i++) {
	tempSchedule[i] = DOMAIN.get(r.nextInt(DOMAIN.size()));
	int score = evaluator.violatedConstraints(pInstance, tempSchedule);
	if (score < min) {
		min = score;
		bestChoice = tempSchedule.clone();
	}
}*/
