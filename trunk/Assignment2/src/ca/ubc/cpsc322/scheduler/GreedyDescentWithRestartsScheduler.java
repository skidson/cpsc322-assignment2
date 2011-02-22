package ca.ubc.cpsc322.scheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * A stub for your Greedy Descent With Restarts scheduler
 */
public class GreedyDescentWithRestartsScheduler extends Scheduler {

	/**
	 * @see scheduler.Scheduler#authorsAndStudentIDs()
	 */
	public String authorsAndStudentIDs() {
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
		int count =0, randomNum;
		int[] choice = new int[2];
		ScheduleChoice[] bestScheduleFound = new ScheduleChoice[pInstance.numCourses];
		ScheduleChoice[] tempSched = new ScheduleChoice[pInstance.numCourses];
		
		for(int i = 0; i < pInstance.numCourses; i++) {
			bestScheduleFound[i] = new ScheduleChoice();
			tempSched[i] = new ScheduleChoice();	
		}
		for(int i = 0; i < (pInstance.numRooms * pInstance.numTimeslots); i++){
			domains.add(new int[2]);
		}
		
		for(int i = 0; i < pInstance.numRooms; i++){
			for(int j = 0; j < pInstance.numTimeslots; j++){
				int[] filler = {i,j};
				domains.set(count++, filler);;
			}
		}
		
		for(int i = 0; i < pInstance.numCourses; i++){
			choice = domains.remove(r.nextInt(domains.size()));
			bestScheduleFound[i].room = choice[0];
			bestScheduleFound[i].timeslot = choice[1];
		}
		tempSched = bestScheduleFound;
		
		while( !timeIsUp() && evaluator.violatedConstraints(pInstance, bestScheduleFound)>0 ){
			//If there is some unused timeslot, swap a doubled up to that one
			//Put old domain back so it can be reused
			randomNum = r.nextInt(pInstance.numCourses);
			choice[0] = tempSched[randomNum].room;
			choice[1] = tempSched[randomNum].timeslot;
			domains.add(choice);
			//Randomly choose new domain
			choice = domains.remove(r.nextInt(domains.size()));
			tempSched[randomNum].room = choice[0];
			tempSched[randomNum].timeslot = choice[1];
			if(evaluator.violatedConstraints(pInstance, tempSched) < evaluator.violatedConstraints(pInstance, bestScheduleFound)){
				bestScheduleFound[randomNum].room = tempSched[randomNum].room;
				bestScheduleFound[randomNum].timeslot = tempSched[randomNum].timeslot;
			}
		}
		
		return bestScheduleFound;
	}

}
