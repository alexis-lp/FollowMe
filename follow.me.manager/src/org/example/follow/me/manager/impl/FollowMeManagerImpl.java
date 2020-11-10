package org.example.follow.me.manager.impl;

import org.example.follow.me.configuration.FollowMeConfiguration;
import org.example.follow.me.manager.EnergyGoal;
import org.example.follow.me.manager.FollowMeAdministration;
import org.example.follow.me.manager.IlluminanceGoal;
import fr.liglab.adele.icasa.service.preferences.Preferences;
import fr.liglab.adele.icasa.simulator.Person;
import fr.liglab.adele.icasa.simulator.listener.PersonListener;
import fr.liglab.adele.icasa.location.LocatedDevice;
import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.service.location.PersonLocationService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.example.time.MomentOfTheDay;
import org.example.time.MomentOfTheDayListener;
import org.example.time.MomentOfTheDayService;

	

	
public class FollowMeManagerImpl implements FollowMeAdministration, MomentOfTheDayListener {

	/** Field for FollowMeConfigurationService dependency */
	private FollowMeConfiguration followMeConfigurationService;
	/** Field for preferencesService dependency */
	private Preferences preferencesService;
	/** Field for personLocationService dependency */
	private PersonLocationService personLocationService;
	
	/** Field for momentsOfTheDay dependency */
	private MomentOfTheDayService[] momentsOfTheDay;
	
	/** Field for the persons in the flat */ 
	private Set<String> persons = new HashSet<>();
	
	
	/**
	* User preferences for illuminance
	**/
	public static final String USER_PROP_ILLUMINANCE = "illuminance";
	public static final String USER_PROP_ILLUMINANCE_VALUE_SOFT = "SOFT";
	public static final String USER_PROP_ILLUMINANCE_VALUE_MEDIUM = "MEDIUM";
	public static final String USER_PROP_ILLUMINANCE_VALUE_FULL = "FULL";
	
	/**
	 * Illuminance predefined values
	 */
	public static final int ILLUMINANCE_VALUE_SOFT = 1;
	public static final int ILLUMINANCE_VALUE_MEDIUM = 2;
	public static final int ILLUMINANCE_VALUE_FULL = 3;
	
	/**
	 * Energy goal predefined values
	 */
	public static final double ENERGY_VALUE_LOW = 100d;
	public static final double ENERGY_VALUE_MEDIUM = 250d;
	public static final double ENERGY_VALUE_HIGH = 300d;
	
	

	public IlluminanceGoal getIlluminancePreferenceForUser(String Name) {
		String userPref = (String) preferencesService.getUserPropertyValue(Name, USER_PROP_ILLUMINANCE);
		IlluminanceGoal goal = stringToIlluminance(userPref);
		return goal;
	}

	public synchronized void setIlluminancePreferenceForUser(String userName, IlluminanceGoal goal) {
		persons.add(userName);
		System.out.println(persons.toString());
		String userPref = illuminanceToString(goal);
		preferencesService.setUserPropertyValue(userName, USER_PROP_ILLUMINANCE, userPref);
		
		int NumberOfLightsToTurnOn = this.computeNewIlluminance();
		followMeConfigurationService.setMaximumNumberOfLightsToTurnOn(NumberOfLightsToTurnOn);

	}

	private synchronized int computeNewIlluminance() {
		assert (!persons.isEmpty());
		int pref = 0;
		IlluminanceGoal illuminance;
		String userPref;
		for (String person : persons) {
			userPref = (String) preferencesService.getUserPropertyValue(person, USER_PROP_ILLUMINANCE);
			illuminance = stringToIlluminance(userPref);
			pref += illuminance.getNumberOfLightsToTurnOn();
		}

		pref = (int) (pref / persons.size());
		System.out.println("Pref is : " + pref);
		return pref;
	}

	public Set<String> getPersonInLocation(String location) {
		Set<String> personNames = personLocationService.getPersonInZone(location);
		return personNames;
	}

	public void setIlluminancePreference(IlluminanceGoal illuminanceGoal) {
		int NumberOfLightsToTurnOn = illuminanceGoal.getNumberOfLightsToTurnOn();
		followMeConfigurationService.setMaximumNumberOfLightsToTurnOn(NumberOfLightsToTurnOn);

	}
	

	public IlluminanceGoal getIlluminancePreference() {
		int numberOfLightsToTurnOn = followMeConfigurationService.getMaximumNumberOfLightsToTurnOn();
		IlluminanceGoal illuminanceGoal;
		switch (numberOfLightsToTurnOn) {
		case ILLUMINANCE_VALUE_SOFT:
			illuminanceGoal = IlluminanceGoal.SOFT;
			break;
		case ILLUMINANCE_VALUE_MEDIUM:
			illuminanceGoal = IlluminanceGoal.MEDIUM;
			break;
		case ILLUMINANCE_VALUE_FULL:
			illuminanceGoal = IlluminanceGoal.FULL;
			break;
		default:
			throw new IllegalArgumentException("Invalid illuminance goal entered : " + numberOfLightsToTurnOn);
		}

		return illuminanceGoal;
	}

	public void setEnergySavingGoal(EnergyGoal energyGoal) {
		double maximumEnergyConsumption = energyGoal.getMaximumEnergyInRoom();
		followMeConfigurationService.setMaximumAllowedEnergyInRoom(maximumEnergyConsumption);

	}

	public EnergyGoal getEnergyGoal() {
		double maximumEnergyConsumption = followMeConfigurationService.getMaximumAllowedEnergyInRoom();
		EnergyGoal energyGoal;
		if (maximumEnergyConsumption == ENERGY_VALUE_LOW) {
			energyGoal = EnergyGoal.LOW;
		} else if (maximumEnergyConsumption == ENERGY_VALUE_MEDIUM) {
			energyGoal = EnergyGoal.MEDIUM;
		} else if (maximumEnergyConsumption == ENERGY_VALUE_HIGH) {
			energyGoal = EnergyGoal.HIGH;
		} else {
			throw new IllegalArgumentException(
					"The energy goal does not belong to enum but is equal to : " + maximumEnergyConsumption);
		}
		return energyGoal;
	}

	/** Component Lifecycle Method */
	public void stop() {
		System.out.println("Follow me manager is stopping");
	}

	/** Component Lifecycle Method */
	public void start() {
		System.out.println("Follow me manager is starting...");
	}

	public String illuminanceToString(IlluminanceGoal goal) {
		String userPref;
		switch (goal) {
		case SOFT:
			userPref = USER_PROP_ILLUMINANCE_VALUE_SOFT;
			break;
		case MEDIUM:
			userPref = USER_PROP_ILLUMINANCE_VALUE_MEDIUM;
			break;
		case FULL:
			userPref = USER_PROP_ILLUMINANCE_VALUE_FULL;
			break;
		default:
			userPref = "Not defined";
			break;
		}
		return userPref;
	}

	public IlluminanceGoal stringToIlluminance(String goal) {
		IlluminanceGoal illuminanceGoal;
		switch (goal) {
		case USER_PROP_ILLUMINANCE_VALUE_SOFT:
			illuminanceGoal = IlluminanceGoal.SOFT;
			break;
		case USER_PROP_ILLUMINANCE_VALUE_MEDIUM:
			illuminanceGoal = IlluminanceGoal.MEDIUM;
			break;
		case USER_PROP_ILLUMINANCE_VALUE_FULL:
			illuminanceGoal = IlluminanceGoal.FULL;
			break;
		default:
			throw new IllegalArgumentException(
					"The illuminance goal does not belong to enum but is equal to : " + goal);
		}
		return illuminanceGoal;
	}

	/** Bind Method for momentsOfTheDay dependency */
	public void bindMomentOfTheDay(MomentOfTheDayService momentOfTheDayService, Map properties) {
		momentOfTheDayService.register(this);
		System.out.println("Bind a new moment of the time");
	}

	/** Unbind Method for momentsOfTheDay dependency */
	public void unbindMomentOfTheDay(MomentOfTheDayService momentOfTheDayService, Map properties) {
		momentOfTheDayService.unregister(this);
		System.out.println("Unbind a new moment of the time");
	}

	@Override
	public void momentOfTheDayHasChanged(MomentOfTheDay newMomentOfTheDay) {
		// Print a message every hour
		System.out.println("One hour has elapsed....");
		
	}

}
