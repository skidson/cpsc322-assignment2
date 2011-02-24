package ca.ubc.cpsc322.scheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * A stub for your second scheduler
 */
public class FavouriteSLSScheduler extends Scheduler {
	SchedulingInstance pInstance;
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
		ScheduleChoice[] bestSchedule = restart(copy(DOMAIN));
		int min = evaluator.violatedConstraints(pInstance, bestSchedule);
		int v = 0;
		while(!timeIsUp() && min > 0) {
			
			ScheduleChoice[] tempSchedule = bestSchedule.clone();
			ScheduleChoice[] bestChoice = bestSchedule.clone();
			
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
	
	private ScheduleChoice[] restart(List<ScheduleChoice> domain) {
		ScheduleChoice[] schedule = new ScheduleChoice[pInstance.numCourses];
		for(int i = 0; i < pInstance.numCourses; i++)
			schedule[i] = domain.remove(r.nextInt(domain.size()));
		return schedule;
	}
	
	private ScheduleChoice[] swap(ScheduleChoice[] schedule, int a, int b) {
		ScheduleChoice temp = schedule[a];
		schedule[a] = schedule[b];
		schedule[b] = temp;
		return schedule;
	}
	
	private ScheduleChoice[] swap(ScheduleChoice[] schedule, List<ScheduleChoice> domain, int a, int b) {
		// We are not concerned with maintaining the swap location b in the list
		domain.add(schedule[a]);
		schedule[a] = domain.remove(b);
		return schedule;
	}
	
	private List<ScheduleChoice> copy(List<ScheduleChoice> domain) {
		List<ScheduleChoice> copy = new ArrayList<ScheduleChoice>();
		for(ScheduleChoice choice : domain)
			copy.add(choice.clone());
		return copy;
	}

}
