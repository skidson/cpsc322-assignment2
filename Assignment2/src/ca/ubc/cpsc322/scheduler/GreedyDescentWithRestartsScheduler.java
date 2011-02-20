package ca.ubc.cpsc322.scheduler;

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
		ScheduleChoice[] bestScheduleFound = new ScheduleChoice[pInstance.numCourses];
		ScheduleChoice[] tempSched = new ScheduleChoice[pInstance.numCourses];
		//Generate random seed for all courses	
		for(int i = 0; i < pInstance.numCourses; i++){
			bestScheduleFound[i] = new ScheduleChoice();
			tempSched[i] = new ScheduleChoice();
			bestScheduleFound[i].room = r.nextInt(pInstance.numRooms);
			bestScheduleFound[i].timeslot = r.nextInt(pInstance.numTimeslots);
		}
		
		//This sucks.. for improvements
		//Need to determine all possible conflicts and change variable that reduces conflicts the most
		
		//need to work in students some how
		while( !timeIsUp() && evaluator.violatedConstraints(pInstance, bestScheduleFound)>0 ){
			for(int i = 0; i < pInstance.numCourses; i++){
				tempSched[i].room = r.nextInt(pInstance.numRooms);
				tempSched[i].timeslot = r.nextInt(pInstance.numTimeslots);
				
				if(evaluator.violatedConstraints(pInstance, tempSched) < evaluator.violatedConstraints(pInstance, bestScheduleFound)){
					bestScheduleFound[i].room = tempSched[i].room;
					bestScheduleFound[i].timeslot = tempSched[i].timeslot;
				}
				 
				
			}
			//randomly restart 40% of the time
			if(r.nextDouble() > .6){
				for(int j = 0; j < pInstance.numCourses; j++){
					bestScheduleFound[j].room = r.nextInt(pInstance.numRooms);
					bestScheduleFound[j].timeslot = r.nextInt(pInstance.numTimeslots);
				}
			}
		}
		return bestScheduleFound;
	}

}
