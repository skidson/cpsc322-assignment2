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
		ScheduleChoice[] bestSchedule = restart(workingDomain);
		int v = 0;
		int min = evaluator.violatedConstraints(pInstance, bestSchedule);
		
		while(!timeIsUp() && evaluator.violatedConstraints(pInstance, bestSchedule) > 0) {
			ScheduleChoice[] tempSchedule = bestSchedule.clone();
			ScheduleChoice[] bestChoice = bestSchedule.clone();
			
			// Random restart
			if (r.nextDouble() < RESTART_RATE) {
				workingDomain = copy(DOMAIN);
				bestSchedule = restart(workingDomain);
			}
			
			// Greedy Descent with Two Stage Selection
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
				tempSchedule = swap(tempSchedule.clone(), v, j);
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
	
	private ScheduleChoice[] swap(ScheduleChoice[] schedule, int a, int b) {
		ScheduleChoice temp = schedule[a];
		schedule[a] = schedule[b];
		schedule[b] = temp;
		return schedule;
	}
	
	private List<ScheduleChoice> copy(List<ScheduleChoice> domain) {
		List<ScheduleChoice> copy = new ArrayList<ScheduleChoice>();
		for(ScheduleChoice choice : domain)
			copy.add(choice.clone());
		return copy;
	}
	
	private ScheduleChoice[] restart(List<ScheduleChoice> domain) {
		ScheduleChoice[] schedule = new ScheduleChoice[pInstance.numCourses];
		for(int i = 0; i < pInstance.numCourses; i++)
			// remove() avoids violating hard constraint, get() allows duplicates
			schedule[i] = domain.remove(r.nextInt(domain.size())); 
		return schedule;
	}
}