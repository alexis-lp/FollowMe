package org.example.time;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;

public class MomentOfTheDayImpl implements MomentOfTheDayService, PeriodicRunnable {

	/** The current moment of the day */
	private MomentOfTheDay currentMomentOfTheDay = MomentOfTheDay.NIGHT;

	/** The current time of the day */
	int currentTime = 0;
	
	/** The list of listeners */ 
	private ArrayList<MomentOfTheDayListener> momentListeners = new ArrayList<MomentOfTheDayListener>();

	@Override
	public synchronized MomentOfTheDay getMomentOfTheDay() {
		return currentMomentOfTheDay;
	}

	@Override
	public long getPeriod() {
		return 1;
	}

	@Override
	public TimeUnit getUnit() {
		return TimeUnit.HOURS;
	}

	@Override
	public synchronized void run() {
		
		if (currentTime == 24) {
			currentTime = 0;
			
		}
		currentTime++;
		currentMomentOfTheDay = currentMomentOfTheDay.getCorrespondingMoment(currentTime);
		for (MomentOfTheDayListener momentOfTheDayListener : momentListeners) {
			momentOfTheDayListener.momentOfTheDayHasChanged(currentMomentOfTheDay);
		}
		
		//System.out.println(" Current Time : " + currentTime);
		//System.out.println(" Moment of the day : " + currentMomentOfTheDay.toString());
		
	}

	
	/** Component Lifecycle Method */
	public void start() {
		System.out.println("Time Bundle is starting...");
	}
	
	/** Component Lifecycle Method */
	public void stop() {
		System.out.println("Time Bundle is stopping");
	}
	
	
	@Override
	public synchronized void register(MomentOfTheDayListener listener) {
			momentListeners.add(listener);
	}

	@Override
	public synchronized void unregister(MomentOfTheDayListener listener) {
		momentListeners.remove(listener);
	}



}
