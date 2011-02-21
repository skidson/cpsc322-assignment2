package ca.ubc.cpsc322.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * A stub for your Greedy Descent With Restarts scheduler
 */
public class GreedyDescentWithRestartsScheduler extends Scheduler {

	/**
	 * @see scheduler.Scheduler#authorsAndStudentIDs()
	 */
	public String authorsAndStudentIDs() {
		// TODO Your Code Here!
		return ("Jeffrey Payan \n18618074 \nStephen Kidson \n15310577");
	}

	/**
	 * @throws Exception 
	 * @see scheduler.Scheduler#schedule(scheduler.SchedulingInstance)
	 */
	public ScheduleChoice[] solve(SchedulingInstance pInstance) throws Exception {
		//Set of Variables (Each exam has its own variable) Domain = all possible (Room, Timeslot) combos
		//Constraint 2 exams cannot be scheduled in the same room/timeslot
		List<int[]> domains = new ArrayList<int[]>();
		int count =0;
		ScheduleChoice[] bestScheduleFound = new ScheduleChoice[pInstance.numCourses];
		for(int i = 0; i < pInstance.numCourses; i++) bestScheduleFound[i] = new ScheduleChoice();
		
		for(int i = 0; i < (pInstance.numRooms * pInstance.numTimeslots); i++){
			domains.add(new int[2]);
		}
		
		for(int i = 0; i < pInstance.numRooms; i++){
			for(int j = 0; j < pInstance.numTimeslots; j++){
				int[] filler = new int[2];
				filler[0] = i;
				filler[1] = j;
				domains.set(count, filler);
				count++;
			}
		}
		
		for(int i = 0; i < pInstance.numCourses; i++){
			int[] choice = domains.remove(r.nextInt(domains.size()));
			bestScheduleFound[i].room = choice[0];
			bestScheduleFound[i].timeslot = choice[1];
			
		}
		
		while( !timeIsUp() && evaluator.violatedConstraints(pInstance, bestScheduleFound)>0 ){
			//Generate random domain for all exams	
			
			/*while(r.nextDouble() < .8 && evaluator.violatedConstraints(pInstance, bestScheduleFound) == Integer.MAX_VALUE){
				for (int i = 0; i < bestScheduleFound.length; i++) {
					for (int j = i+1; j < bestScheduleFound.length; j++) {
						while (bestScheduleFound[i].timeslot == bestScheduleFound[j].timeslot && bestScheduleFound[i].room == bestScheduleFound[j].room) {
							bestScheduleFound[i].timeslot = r.nextInt(pInstance.numTimeslots);
						}
					}
				}
			}*/
			
			
				 
				
			}
		return bestScheduleFound;
	}

}
