package org.example.time;

public enum MomentOfTheDay {
    MORNING(6), AFTERNOON(12), EVENING(18), NIGHT(24);
	 
    /**
     * Gets the moment of the day corresponding to the hour.
     * 
     * @param hour
     *            the given hour
     * @return the corresponding moment of the day
     */
    synchronized MomentOfTheDay getCorrespondingMoment(int hour) {
        assert ((0 <= hour) && (hour <= 24));
        if (hour <= 6) {
        	return MomentOfTheDay.NIGHT;
        }
        else if (hour <= 12) {
        	return MomentOfTheDay.MORNING;
        }
        else if (hour <= 18 ) {
        	return MomentOfTheDay.AFTERNOON;
        }
        else if (hour <= 24) {
        	return MomentOfTheDay.EVENING;
        }
    
        else {
        	System.out.println("Error");
        	return null;
        }
    }
 
    /**
     * The hour when the moment start.
     */
    private final int startHour;
 
    /**
     * Build a new moment of the day :
     * 
     * @param startHour
     *            when the moment start.
     */
    MomentOfTheDay(int startHour) {
        assert ((0 <= startHour) && (startHour <= 24));
        this.startHour = startHour;
    }

}
