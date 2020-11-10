package org.example.follow.me.manager;

/**
 * The Interface FollowMeAdministration allows the administrator to configure
 * its preference regarding the management of the Follow Me application.
 */

public interface FollowMeAdministration {
	/**
     * Sets the illuminance preference. The manager will try to adjust the
     * illuminance in accordance with this goal.
     * 
     * @param illuminanceGoal
     *            the new illuminance preference
     */
    public void setIlluminancePreference(IlluminanceGoal illuminanceGoal);
 
    /**
     * Get the current illuminance preference.
     * 
     * @return the new illuminance preference
     */
    public IlluminanceGoal getIlluminancePreference();
    
    /**
     * Configure the energy saving goal.
     * @param energyGoal : the targeted energy goal.
     */
    public void setEnergySavingGoal(EnergyGoal energyGoal);
 
    /**
     * Gets the current energy goal.
     * 
     * @return the current energy goal.
     */
    public EnergyGoal getEnergyGoal();
    
    /**
     * Gets the current illuminance preference of the specified user
     * 
     * @param UserName The name of the user
     * 
     * @return The illumance goal corresponding to the user preference
     */
    public IlluminanceGoal getIlluminancePreferenceForUser(String UserName);
    
    /**
     * Sets the illuminance preference for the specified user. The manager will try to adjust the illuminance in accordance 
     * with this goal
     * 
     * @param UserName The name of the user 
     * 
     * @param goal The new illuminance preference
     */
    public void setIlluminancePreferenceForUser(String UserName, IlluminanceGoal goal);
    
    

}
