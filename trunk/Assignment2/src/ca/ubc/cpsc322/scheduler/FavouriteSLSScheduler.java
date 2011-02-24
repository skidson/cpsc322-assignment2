package ca.ubc.cpsc322.scheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * A stub for your second scheduler
 */
public class FavouriteSLSScheduler extends Scheduler {
	private SchedulingInstance pInstance;
	private static final int CACHE_SIZE = 3;
	private static final double RESTART_RATE = 0.6;
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
		final List<ScheduleChoice> DOMAIN = copy(workingDomain);
		
		// Initialize to a random variable assignment
		ScheduleChoice[] bestSchedule = restart(copy(DOMAIN), true);
		ScheduleChoice[][] cache = new ScheduleChoice[CACHE_SIZE][bestSchedule.length];
		
		// Initialize the cache
		for (int i = 0; i < CACHE_SIZE; i++)
			cache[i] = bestSchedule;
		
		int min = evaluator.violatedConstraints(pInstance, bestSchedule);
		
		// Greedy Descent with Two Stage Selection and Steve's 1337 skillz
		// Restarts when stagnant. Randomly revert to cached value.
		int v = 0; int c = 0;
		final int STAGNANT = DOMAIN.size()/bestSchedule.length;
		while(!timeIsUp() && min > 0) {
			ScheduleChoice[] tempSchedule = bestSchedule.clone();
			ScheduleChoice[] bestChoice = bestSchedule.clone();
			
			// Cache the current schedule and return to previously cached value
			if (r.nextDouble() < RESTART_RATE) {
				workingDomain = copy(DOMAIN);
				bestSchedule = restart(workingDomain, true);
			}
			
			// Swap with unused options in working domain
			for (ScheduleChoice choice : workingDomain) {
				tempSchedule = bestSchedule.clone();
				tempSchedule[v%tempSchedule.length] = choice;
				int score = evaluator.violatedConstraints(pInstance, tempSchedule);
				if (score < min) {
					cache[(c++)%CACHE_SIZE] = bestSchedule.clone();
					min = score;
					bestChoice = tempSchedule.clone();
				}
			}
			// Swap with another course's exam slot
			for (int j = (v%tempSchedule.length) + 1; j < bestSchedule.length; j++) {
				tempSchedule = bestSchedule.clone();
				swap(tempSchedule, (v%tempSchedule.length), j);
				int score = evaluator.violatedConstraints(pInstance, tempSchedule);
				if (score < min) {
					cache[(c++)%CACHE_SIZE] = bestSchedule.clone();
					min = score;
					bestChoice = tempSchedule.clone();
				}
			}
			if (v++ >= STAGNANT)
				bestChoice = cache[c++%CACHE_SIZE];
			bestSchedule = bestChoice.clone();
			min = evaluator.violatedConstraints(pInstance, bestSchedule);
		}
		
		// Return the best value in cache.
		int index = 0;
		min = evaluator.violatedConstraints(pInstance, cache[0]);
		for (int i = 1; i < CACHE_SIZE; i++) {
			int score = evaluator.violatedConstraints(pInstance, cache[i]);
			if (score < min) {
				min = score;
				index = i;
			}
		}
		
		return cache[index];
	}
	
	/**
	 * Swaps the two elements of this array at the indicated indices.
	 * @param array the array to be modified.
	 * @param a the index of the first element.
	 * @param b the index of the second element.
	 */
	private void swap(Object[] array, int a, int b) {
		Object temp = array[a];
		array[a] = array[b];
		array[b] = temp;
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

}

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
