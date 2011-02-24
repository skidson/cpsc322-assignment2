package ca.ubc.cpsc322.scheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * A stub for your Greedy Descent With Restarts scheduler
 */
public class GreedyDescentWithRestartsScheduler extends Scheduler {
	private SchedulingInstance pInstance;
	private static double RESTART_RATE = 0.6;
	/**
	 * @see scheduler.Scheduler#authorsAndStudentIDs()
	 */
	public String authorsAndStudentIDs() {
		return ("Jeffrey Payan\n18618074\nStephen Kidson\n15345077");
	}

	/**
	 * @throws Exception 
	 * @see scheduler.Scheduler#schedule(scheduler.SchedulingInstance)
	 */
	public ScheduleChoice[] solve(SchedulingInstance pInstance) throws Exception {
		this.pInstance = pInstance;
		List<ScheduleChoice> workingDomain = new ArrayList<ScheduleChoice>();
		
		// Fill the domain of all possible ScheduleChoices
		for (int room = 0; room < pInstance.numRooms; room++) {
			for (int timeslot = 0; timeslot < pInstance.numTimeslots; timeslot++) {
				ScheduleChoice possibility = new ScheduleChoice();
				possibility.room = room;
				possibility.timeslot = timeslot;
				workingDomain.add(possibility);
			}
		}
		
		final List<ScheduleChoice> DOMAIN = copy(workingDomain);
		ScheduleChoice[] bestSchedule = restart(copy(DOMAIN), true);
		int min = evaluator.violatedConstraints(pInstance, bestSchedule);
		
		// Greedy Descent with Two Stage Selection
		// For each course, find which swap results in lowest # conflicts, then set as best.
		// Checks all neighbouring values for current variable v before making a decision.
		int v = 0;
		while(!timeIsUp() && min > 0) {
			ScheduleChoice[] tempSchedule = bestSchedule.clone();
			ScheduleChoice[] bestChoice = bestSchedule.clone();
			
			// Random restart
			if (r.nextDouble() < RESTART_RATE) {
				workingDomain = copy(DOMAIN);
				bestSchedule = restart(workingDomain, true);
			}
			
			// Swap with unused options in working domain
			for (ScheduleChoice choice : workingDomain) {
				tempSchedule = bestSchedule.clone();
				tempSchedule[v] = choice;
				int score = evaluator.violatedConstraints(pInstance, tempSchedule);
				if (score < min) {
					min = score;
					bestChoice = tempSchedule.clone();
				}
			}
			
			// Swap with another course's exam slot
			for (int j = v + 1; j < bestSchedule.length; j++) {
				tempSchedule = bestSchedule.clone();
				swap(tempSchedule.clone(), v, j);
				int score = evaluator.violatedConstraints(pInstance, tempSchedule);
				if (score < min) {
					min = score;
					bestChoice = tempSchedule.clone();
				}
			}
			
			v = (v+1)%bestSchedule.length;
			bestSchedule = bestChoice.clone();
			min = evaluator.violatedConstraints(pInstance, bestSchedule);
		}
		return bestSchedule;
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