package org.example.follow.me.manager.command;
 
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.example.follow.me.manager.FollowMeAdministration;
import org.example.follow.me.manager.IlluminanceGoal;
import org.example.time.*;
import org.example.follow.me.manager.EnergyGoal;
 
import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;
 
//Define this class as an implementation of a component :
@Component
//Create an instance of the component
@Instantiate(name = "follow.me.mananger.command")
//Use the handler command and declare the command as a command provider. The
//namespace is used to prevent name collision.
@CommandProvider(namespace = "followme")

public class FollowMeManagerCommandImpl {
 
    // Declare a dependency to a FollowMeAdministration service
    @Requires
    private FollowMeAdministration m_administrationService;
 
    // Declare a dependency to a moment of the day service
    @Requires(optional=true)
    private MomentOfTheDayService m_momentOftheDay;
 
 
    /**
     * Felix shell command implementation to sets the illuminance preference.
     *
     * @param goal the new illuminance preference ("SOFT", "MEDIUM", "FULL")
     */
 
    // Each command should start with a @Command annotation
    @Command
    public void setIlluminancePreference(String userName, String goal) {
    	

    	// The targeted goal
        IlluminanceGoal illuminanceGoal;
        
        // Convert the goal string into an illuminance
        // goal and fail if the entry is not "SOFT", "MEDIUM" or "HIGH"
        switch(goal) {
        	case "SOFT":
        		illuminanceGoal = IlluminanceGoal.SOFT;
        		break;
        	case "MEDIUM":
        		illuminanceGoal = IlluminanceGoal.MEDIUM;
        		break;
        	case "HIGH":
        		illuminanceGoal = IlluminanceGoal.FULL;
        		break;
        	default :
        		throw new IllegalArgumentException("Invalid illuminance goal entered : " + goal);
        }
        if (userName == null) {
        	m_administrationService.setIlluminancePreference(illuminanceGoal);
        }
        else {
        	m_administrationService.setIlluminancePreferenceForUser(userName, illuminanceGoal);
        }
    }
 
    @Command
    public void getIlluminancePreference(String userName){
    	IlluminanceGoal illuminanceGoal;
    	if (userName == null) {
    		illuminanceGoal = m_administrationService.getIlluminancePreference();
    	}
    	else {
    		illuminanceGoal = m_administrationService.getIlluminancePreferenceForUser(userName);
    	}
    	String goal ;
    	switch(illuminanceGoal) {
    		case SOFT:
    			goal = "SOFT";
    			break;
    		case MEDIUM:
    			goal = "MEDIUM";
    			break;
    		case FULL:
    			goal = "HIGH";
    			break;
    		default :
    			goal = "Not defined";
    			break;
    	}
    	if (userName == null) {
    		System.out.println("The illuminance goal for everybody is  " + goal ); 
    	}
    	else {
    		System.out.println("The illuminance goal for " + userName + " is  " + goal ); 
    	}
        }
     
    
    
    /**
     * Felix shell command implementation to sets the energy goal preference.
     *
     * @param goal the new energy goal preference ("LOW", "MEDIUM", "HIGH")
     */
 
  
    @Command
    public void setEnergyPreference(String goal) {
        // The targeted goal
        EnergyGoal energyGoal;
        
        // Convert the goal string into an illuminance
        // goal and fail if the entry is not "LOW", "MEDIUM" or "HIGH"
        switch(goal) {
        	case "LOW":
        		energyGoal = EnergyGoal.LOW;
        		break;
        	case "MEDIUM":
        		energyGoal = EnergyGoal.MEDIUM;
        		break;
        	case "HIGH":
        		energyGoal = EnergyGoal.HIGH;
        		break;
        	default :
        		throw new IllegalArgumentException("Invalid energy goal entered : " + goal);
        }
        
        m_administrationService.setEnergySavingGoal(energyGoal);
    }
 
    @Command
    public void getEnergyPreference(){
    	EnergyGoal energyGoal = m_administrationService.getEnergyGoal();
    	String goal ;
    	switch(energyGoal) {
    		case LOW:
    			goal = "LOW";
    			break;
    		case MEDIUM:
    			goal = "MEDIUM";
    			break;
    		case HIGH:
    			goal = "HIGH";
    			break;
    		default :
    			goal = "Not defined";
    			break;
    	}
    
        System.out.println("Energy mode = " + goal ); 
        }
     
    
    /**
     * Felix shell command implementation to get the moment of the Day.
     *
     */
    @Command
    public synchronized void getMomentOfTheDay(){
    	
    	MomentOfTheDay currentMoment = m_momentOftheDay.getMomentOfTheDay();
    	String moment;
    	switch(currentMoment) {
    		case MORNING:
    			moment = "MORNING";
    			break;
    		case AFTERNOON:
    			moment = "AFTERNOON";
    			break;
    		case EVENING:
    			moment = "EVENING";
    			break;
    		case NIGHT:
    			moment = "NIGHT";
    			break;
    		default :
    			moment = "Not defined";
    			break;
    	}
        System.out.println("Moment of the day : " + moment ); 
       }
     
    
   /** Component Lifecycle Method */
   @Invalidate
    public void stop() {
    	System.out.println("Follow me command is stopping");
    }	

    /** Component Lifecycle Method */
    @Validate
    public void start() {
    	System.out.println("Follow me command is starting...");
	}

}
