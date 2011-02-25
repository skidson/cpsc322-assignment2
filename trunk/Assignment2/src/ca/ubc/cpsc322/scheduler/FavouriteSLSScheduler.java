package ca.ubc.cpsc322.scheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom scheduler that implements a genetic local search algorithm.
 * @see Scheduler
 */
public class FavouriteSLSScheduler extends Scheduler {
	private SchedulingInstance pInstance;
	private static double mutationRate = 0.65;
	private static double apocalypseRate = 0.0;
	
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
		
		final List<ScheduleChoice> WORLD = copy(workingDomain);
		
		// Initialize to a random variable assignment
		ScheduleChoice[][] humanity = apocalypse(WORLD);
		ScheduleChoice[] adam = humanity[0];
		ScheduleChoice[] eve = humanity[1];
		int min = Integer.MAX_VALUE;
		while(!timeIsUp() && min > 0) {
			if (r.nextDouble() < apocalypseRate) {
				workingDomain = copy(WORLD);
				humanity = apocalypse(WORLD);
				adam = humanity[0].clone();
				eve = humanity[1].clone();
			}
			
			// Finds two prime specimens and nominates them Adam and Eve.
			for (int i = 0; i < humanity.length; i++) {
				int score = evaluator.violatedConstraints(pInstance, humanity[i]);
				if (score < min) {
					min = score;
					eve = adam.clone();
					adam = humanity[i].clone();
				}
			}
			
			// Start new generation
			humanity[0] = adam.clone();
			humanity[1] = eve.clone();
			humanity[2] = procreate(adam, eve, copy(workingDomain));
			humanity[3] = procreate(eve, adam, copy(workingDomain));
			min = evaluator.violatedConstraints(pInstance, adam);
		}
		return adam;
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
	
	/**
	 * Produces a child that inherits aspects of both parents & random defects based on this
	 * scheduler's mutation rate. It is typical to make two calls to this method and swap
	 * the roles of male and female.
	 * @param male 
	 * @param female
	 * @param domain the domain of possible variables.
	 * @return
	 */
	private ScheduleChoice[] procreate(ScheduleChoice[] male, 
			ScheduleChoice[] female, 
			List<ScheduleChoice> domain) {
		ScheduleChoice[] child = new ScheduleChoice[male.length];
		for (int i = 0; i < male.length; i += 2)
			child[i] = male[i].clone();
		for (int i = 1; i < male.length; i += 2)
			child[i] = female[i].clone();
		while(r.nextDouble() < mutationRate) {
			int gene = r.nextInt(child.length);
			ScheduleChoice trait = child[gene].clone();
			child[gene] = domain.remove(r.nextInt(domain.size())).clone();
			domain.add(trait);
		}
		return child;
	}
	
	/**
	 * Generates newly initialized parents and offspring.
	 * @param domain
	 * @return
	 */
	private ScheduleChoice[][] apocalypse(List<ScheduleChoice> domain) {
		ScheduleChoice[] adam = restart(copy(domain), true), eve = restart(copy(domain), true);
		ScheduleChoice[][] humanity = new ScheduleChoice[4][pInstance.numCourses];
		humanity[0] = adam.clone();
		humanity[1] = eve.clone();
		humanity[2] = procreate(adam, eve, copy(domain));
		humanity[3] = procreate(eve, adam, copy(domain));
		return humanity;
	}
	
	public static void setMutationRate(double rate) {
		mutationRate = rate;
	}
	
	public static void setApocalypseRate(double rate) {
		apocalypseRate = rate;
	}

}