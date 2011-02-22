package ca.ubc.cpsc322.scheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * A stub for your Greedy Descent With Restarts scheduler
 */
public class Steve_GreedyDescentWithRestartsScheduler extends Scheduler {
	private static final double RESTART_RATE = 0.2;
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
		//Set of Variables (Each exam has its own variable) Domain = all possible (Room, Timeslot) combos
		//Constraint 2 exams cannot be scheduled in the same room/timeslot
		List<ScheduleChoice> DOMAIN = new ArrayList<ScheduleChoice>();
		ScheduleChoice[] bestSchedule = new ScheduleChoice[pInstance.numCourses];
		try {
			// Fill the domain of all possible ScheduleChoices
			for (int room = 0; room < pInstance.numRooms; room++) {
				for (int timeslot = 0; timeslot < pInstance.numTimeslots; timeslot++) {
					ScheduleChoice possibility = new ScheduleChoice();
					possibility.room = room;
					possibility.timeslot = timeslot;
					DOMAIN.add(possibility);
				}
			}
			
			List<ScheduleChoice> workingDomain = copy(DOMAIN);
			bestSchedule = restart(pInstance, workingDomain);
			while(!timeIsUp() && evaluator.violatedConstraints(pInstance, bestSchedule) > 0) {
				// Random restart
				if (r.nextDouble() > (1.0-RESTART_RATE)) {
					workingDomain = copy(DOMAIN);
					bestSchedule = restart(pInstance, workingDomain);
				}
				
				// Greedy Descent
				// For each course, find which swap results in lowest # conflicts, then set as best
				ScheduleChoice[] tempSchedule = bestSchedule.clone();
				ScheduleChoice[] bestChoice = bestSchedule.clone();
				for (int i = 0; i < bestSchedule.length; i++) {
					int min = evaluator.violatedConstraints(pInstance, bestSchedule);
					
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
					for (int j = 0; j < bestSchedule.length; j++) {
						tempSchedule = bestSchedule.clone();
						tempSchedule = swap(tempSchedule.clone(), i, j);
						int score = evaluator.violatedConstraints(pInstance, tempSchedule);
						if (score < min) {
							min = score;
							bestChoice = tempSchedule.clone();
						}
					}
					
				}
				bestSchedule = bestChoice.clone();
			}
			} catch (Exception e) {
				e.printStackTrace();
			}
		return bestSchedule;
	}
	
	private List<ScheduleChoice> copy(List<ScheduleChoice> domain) {
		List<ScheduleChoice> copy = new ArrayList<ScheduleChoice>();
		for(ScheduleChoice choice : domain)
			copy.add(choice.clone());
		return copy;
	}
	
	private ScheduleChoice[] swap(ScheduleChoice[] schedule, int a, int b) {
		ScheduleChoice temp = schedule[a];
		schedule[a] = schedule[b];
		schedule[b] = temp;
		return schedule;
	}
	
	private ScheduleChoice[] restart(SchedulingInstance pInstance, List<ScheduleChoice> domain) {
		ScheduleChoice[] schedule = new ScheduleChoice[pInstance.numCourses];
		for(int i = 0; i < pInstance.numCourses; i++)
			schedule[i] = domain.remove(r.nextInt(domain.size()));
		return schedule;
	}
	
}
