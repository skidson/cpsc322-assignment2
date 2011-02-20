package ca.ubc.cpsc322.scheduler;

public interface Evaluator {
	public int violatedConstraints(SchedulingInstance pInstance, ScheduleChoice[] pCandidateSchedule) throws Exception;
}
