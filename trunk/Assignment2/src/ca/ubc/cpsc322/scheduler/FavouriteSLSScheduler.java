package ca.ubc.cpsc322.scheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * A stub for your second scheduler
 */
public class FavouriteSLSScheduler extends Scheduler {
	ScheduleChoice[] bestSchedule;
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
		ScheduleChoice[] bestScheduleFound = null;
		
		// Populate the domain
		List<ScheduleChoice> DOMAIN = new ArrayList<ScheduleChoice>();
		for (int room = 0; room < pInstance.numRooms; room++) {
			for (int timeslot = 0; timeslot < pInstance.numTimeslots; timeslot++) {
				ScheduleChoice choice = new ScheduleChoice();
				choice.room = room;
				choice.timeslot = timeslot;
				DOMAIN.add(choice);
			}
		}
		this.bestSchedule = restart(copy(DOMAIN));
		while(!timeIsUp() && evaluator.violatedConstraints(pInstance, bestScheduleFound) > 0){
			Searcher searcher = new Searcher(restart(copy(DOMAIN)), copy(DOMAIN));
			new Thread(searcher).start();
		}
		
		return this.bestSchedule;
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
	
	private void submit(ScheduleChoice[] test) throws Exception {
		synchronized(bestSchedule) {
			if (evaluator.violatedConstraints(pInstance, test) < evaluator.violatedConstraints(pInstance, bestSchedule))
				bestSchedule = test.clone();
		}
	}
	
	private class Searcher implements Runnable {
		private ScheduleChoice[] schedule;
		private List<ScheduleChoice> domain;
		
		public Searcher(ScheduleChoice[] schedule, List<ScheduleChoice> domain) {
			this.schedule = schedule.clone();
			this.domain = copy(domain);
		}
		
		public void run() {
			ScheduleChoice[] bestChoice = schedule.clone();
			List<ScheduleChoice> tempDomain = copy(domain);
			int min;
			try {
				min = evaluator.violatedConstraints(pInstance, schedule);
				for(int i = 0; i < schedule.length; i++) {
					ScheduleChoice[] tempSchedule = schedule.clone();
					int index = r.nextInt(tempSchedule.length + domain.size());
					tempDomain = copy(domain);
					if (index > tempSchedule.length) {
						index -= tempSchedule.length;
						tempSchedule = swap(tempSchedule, tempDomain, i, index);
					} else
						tempSchedule = swap(tempSchedule, i, index);
					int score = evaluator.violatedConstraints(pInstance, tempSchedule);
					if (score < min) {
						min = score;
						bestChoice = tempSchedule.clone();
					}
				}
				submit(bestChoice);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
