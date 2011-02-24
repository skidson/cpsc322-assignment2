package ca.ubc.cpsc322.scheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * A stub for your Greedy Descent With Restarts scheduler
 */
public class Steve_GreedyDescentWithRestartsScheduler extends Scheduler {
	private static final double RESTART_RATE = 0.6;
	private static final int DEPTH = 100;
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
			ScheduleChoice[] tempSchedule = bestSchedule.clone();
			ScheduleChoice[] bestChoice = bestSchedule.clone();
			
			while(!timeIsUp() && evaluator.violatedConstraints(pInstance, bestSchedule) > 0) {
				// Random restart
				if (r.nextDouble() < RESTART_RATE) {
					workingDomain = copy(DOMAIN);
					bestSchedule = restart(pInstance, workingDomain);
				}
				
				int min = evaluator.violatedConstraints(pInstance, bestSchedule);
				
				// General Local Search Algorithm
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
				
				// Greedy Descent
				// For each course, find which swap results in lowest # conflicts, then set as best
				// Note: checks all neighbours before making a decision.
				for (int i = 0; i < bestSchedule.length; i++) {
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
				}
				
				// Random Walks
				// Randomly check possible neighbours and transition at first occurrence of improvement
				/*for (int i = 0; i < DEPTH; i++) {
					if (min == 0)
						break;
					int choice = r.nextInt(workingDomain.size() + bestSchedule.length);
					if (choice >= workingDomain.size()) {
						choice -= workingDomain.size();
						tempSchedule = swap(tempSchedule, r.nextInt(tempSchedule.length), choice);
					} else 
						tempSchedule = swap(tempSchedule, workingDomain, r.nextInt(tempSchedule.length), choice);
					
					int score = evaluator.violatedConstraints(pInstance, tempSchedule);
					if (score < min) {
						min = score;
						bestChoice = tempSchedule.clone();
//						System.err.println("New best schedule: " + min + " violations."); // debug
					}
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
	
	private ScheduleChoice[] swap(ScheduleChoice[] schedule, List<ScheduleChoice> domain, int a, int b) {
		// We are not concerned with maintaining the swap location b in the list
		domain.add(schedule[a]);
		schedule[a] = domain.remove(b);
		return schedule;
	}
	
	private ScheduleChoice[] restart(SchedulingInstance pInstance, List<ScheduleChoice> domain) {
		ScheduleChoice[] schedule = new ScheduleChoice[pInstance.numCourses];
		for(int i = 0; i < pInstance.numCourses; i++)
			schedule[i] = domain.remove(r.nextInt(domain.size()));
		return schedule;
	}
	
	// debugging
	private void printDomain(List<ScheduleChoice> domain) {
		System.err.println("#\t\t" + "ROOM\tTIMESLOT");
		for (int i = 0; i < domain.size(); i++) {
			System.err.println(i + ")\t\t" + domain.get(i).room + "\t" + domain.get(i).timeslot);
		}
	}
	
}
