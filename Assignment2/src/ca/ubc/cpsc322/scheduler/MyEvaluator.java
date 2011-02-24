package ca.ubc.cpsc322.scheduler;

public class MyEvaluator implements Evaluator{

	public int violatedConstraints(SchedulingInstance pInstance, ScheduleChoice[] pCandidateSchedule) throws Exception {
		int result = 0;
		
		if (pCandidateSchedule == null){
			return Integer.MAX_VALUE;
		}
			
		/* Throw error for incomplete schedules */
		if (pCandidateSchedule.length != pInstance.numCourses) {
			throw new Exception("Incomplete Exam Schedule. Have to assign a time and room to every exam!");
		}

		/* Throw error for using more rooms or timeslots than available */
		for (int i = 0; i < pCandidateSchedule.length; i++) {
			if (pCandidateSchedule[i].timeslot >= pInstance.numTimeslots || pCandidateSchedule[i].timeslot < 0) {
				throw new Exception("Timeslot must be in {0,...," + (pInstance.numTimeslots-1) + "}, but is " + pCandidateSchedule[i].timeslot );
			}
			if (pCandidateSchedule[i].room >= pInstance.numRooms || pCandidateSchedule[i].room < 0) {
				throw new Exception("Room must be in {0,...," + (pInstance.numRooms-1) + "}, but is " + pCandidateSchedule[i].room );
			}
		}

		/* Check for collisions of exams (same time and slot) */
		for (int i = 0; i < pCandidateSchedule.length; i++) {
			for (int j = i+1; j < pCandidateSchedule.length; j++) {
				if (pCandidateSchedule[i].timeslot == pCandidateSchedule[j].timeslot && pCandidateSchedule[i].room == pCandidateSchedule[j].room) {
					//=== We cannot schedule two exams into the same timeslot and room.
					return Integer.MAX_VALUE;
				}
			}
		}
		
		return result;
	}

}
