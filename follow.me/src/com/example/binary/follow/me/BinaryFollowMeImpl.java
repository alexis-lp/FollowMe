package com.example.binary.follow.me;

import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.light.BinaryLight;

import java.util.ArrayList;
import java.util.Map;

import org.example.follow.me.configuration.FollowMeConfiguration;

import fr.liglab.adele.icasa.device.light.DimmerLight;
import fr.liglab.adele.icasa.device.light.Photometer;

public class BinaryFollowMeImpl implements DeviceListener, FollowMeConfiguration {

	/** Field for presenceSensors dependency */
	private PresenceSensor[] presenceSensors;
	/** Field for binaryLights dependency */
	private BinaryLight[] binaryLights;

	/** Field for dimmerLights dependency */
	private DimmerLight[] dimmerLights;
	/** Field for photometers dependency */
	private Photometer[] photometers;

	private ArrayList<String> occupiedZones = new ArrayList<>();

	/**
	 * The name of the location property
	 */
	public static final String LOCATION_PROPERTY_NAME = "Location";

	/**
	 * The name of the location for unknown value
	 */
	public static final String LOCATION_UNKNOWN = "unknown";

	/** 
	* The maximum number of lights to turn on when a user enters the room :
	**/
	private int maxLightsToTurnOnPerRoom;

	/**
	* The maximum energy consumption allowed in a room in Watt:
	**/
	private double maximumEnergyConsumptionAllowedInARoom;

	/**
	* The targeted illuminance in each room
	**/
	private double targetedIlluminance = 4000.0d;

	/**
	 * Watt to lumens conversion factor
	 * It has been considered that: 1 Watt=680.0 lumens at 555nm.
	 */
	public final static double ONE_WATT_TO_ONE_LUMEN = 680.0d;

	/** 
	 * Maximal power of a light in Watts 
	 */
	public final static double LIGHT_POWER = 100.0d;

	/** 
	 * Bind Method for presenceSensors dependency
	 * This method is used to manage device listener  
	 */
	public void bindPeresenceSensor(PresenceSensor presenceSensor, Map properties) {
		System.out.println("bind presence sensor " + presenceSensor.getSerialNumber());
		// Add the listener to the presence sensor
		presenceSensor.addListener(this);
	}

	/** 
	 * Unbind Method for presenceSensors dependency 
	 * This method is used to manage device listener
	 */
	public void unbindPresenceSensor(PresenceSensor presenceSensor, Map properties) {
		System.out.println("unbind presence sensor " + presenceSensor.getSerialNumber());
		// Remove the listener from the presence sensor
		presenceSensor.removeListener(this);
	}

	/** Bind Method for binaryLights dependency */
	public void bindBinaryLights(BinaryLight binaryLight, Map properties) {
		System.out.println("bind binary light " + binaryLight.getSerialNumber());
		binaryLight.addListener(this);
	}

	/** Unbind Method for binaryLights dependency */
	public void unbindBinaryLights(BinaryLight binaryLight, Map properties) {
		System.out.println("unbind binary light " + binaryLight.getSerialNumber());
		binaryLight.removeListener(this);
	}

	/** Bind Method for dimmerLights dependency */
	public void bindDimmerLights(DimmerLight dimmerLight, Map properties) {
		System.out.println("bind dimmer light " + dimmerLight.getSerialNumber());
		dimmerLight.addListener(this);
	}

	/** Unbind Method for dimmerLights dependency */
	public void unbindDimmerLights(DimmerLight dimmerLight, Map properties) {
		System.out.println("unbind dimmer light " + dimmerLight.getSerialNumber());
		dimmerLight.removeListener(this);
	}

	/** Bind Method for photometers dependency */
	public void bindPhotometer(Photometer photometer, Map properties) {
		System.out.println("bind photometer " + photometer.getSerialNumber());
		photometer.addListener(this);
	}

	/** Unbind Method for photometers dependency */
	public void unbindPhotometer(Photometer photometer, Map properties) {
		System.out.println("unbind photometer " + photometer.getSerialNumber());
		photometer.removeListener(this);
	}

	/** Component Lifecycle Method */
	public void stop() {
		for (PresenceSensor sensor : presenceSensors) {
			sensor.removeListener(this);
		}
		for (BinaryLight binaryLight : binaryLights) {
			binaryLight.removeListener(this);
		}
		for (DimmerLight dimmerLight : dimmerLights) {
			dimmerLight.removeListener(this);
		}
		for (Photometer photometer : photometers) {
			photometer.removeListener(this);
		}
		System.out.println("Component is stopping...");
	}

	/** Component Lifecycle Method */
	public void start() {
		System.out.println("Component is starting...");
	}

	/**
	 * This method is part of the DeviceListener interface and is called when a
	 * subscribed device property is modified.
	 * 
	 * @param device
	 *            is the device whose property has been modified.
	 * @param propertyName
	 *            is the name of the modified property.
	 * @param oldValue
	 *            is the old value of the property
	 * @param newValue
	 *            is the new value of the property
	 */
	public void devicePropertyModified(GenericDevice device, String propertyName, Object oldValue, Object newValue) {

		//we assume that we listen only to presence sensor events, binary Lights and Dimmer Lights event 
		if (device instanceof PresenceSensor) {
			//based on that assumption we can cast the generic device without checking via instanceof
			PresenceSensor changingSensor = (PresenceSensor) device;

			// If the presence sensosr sensed presence
			if (propertyName.equals(PresenceSensor.PRESENCE_SENSOR_SENSED_PRESENCE)) {
				//get the location of the changing sensor ;
				String detectorLocation = (String) changingSensor.getPropertyValue(LOCATION_PROPERTY_NAME);

				if (!detectorLocation.equals(LOCATION_UNKNOWN)) {
					if (changingSensor.getSensedPresence()) {
						occupiedZones.add(detectorLocation);
					} else {
						occupiedZones.remove(detectorLocation);
					}
					updateLightsValueInLocation(detectorLocation);
				}
			}
		} else if ((device instanceof BinaryLight) || (device instanceof DimmerLight)) {
			if (propertyName.contentEquals(LOCATION_PROPERTY_NAME)) {
				String newLightLocation = (String) newValue;
				String oldLightLocation = (String) oldValue;

				updateLightsValueInLocation(newLightLocation);
				updateLightsValueInLocation(oldLightLocation);

			}
		}
	}

	/**
	 * This method refresh the full values of the different lights in all the rooms
	 * 
	 */
	private void updateAllLights() {
		for (String zone : occupiedZones) {
			updateLightsValueInLocation(zone);
		}
	}

	/**
	 * This method refresh the full values of the different lights in a room
	 * 
	 * @param presenceSensor
	 *            one of the presence sensor in the room 
	 * @param location
	 *            the value of the location
	 */
	private synchronized void updateLightsValueInLocation(String location) {
		PresenceSensor presenceSensor = getPresenceSensorFromLocation(location);

		ArrayList<BinaryLight> sameLocationBinaryLights = getBinaryLightFromLocation(location);
		ArrayList<DimmerLight> sameLocationDimmerLights = getDimmerLightFromLocation(location);

		int numberOfLightsTurnedOn = 0;
		double energyConsumptionInLocation = 0;

		for (BinaryLight binaryLight : sameLocationBinaryLights) {

			if (presenceSensor == null) {
				binaryLight.turnOff();
			} else {
				// and switch them on/off depending on the sensed presence
				if ((presenceSensor.getSensedPresence()) && (numberOfLightsTurnedOn < this.maxLightsToTurnOnPerRoom)) {

					binaryLight.turnOn();
					numberOfLightsTurnedOn = numberOfLightsTurnedOn + 1;
					energyConsumptionInLocation = energyConsumptionInLocation + LIGHT_POWER;

				} else {
					binaryLight.turnOff();

				}
			}
		}

		///Same thing for dimmer lights 
		for (DimmerLight dimmerLight : sameLocationDimmerLights) {
			if (presenceSensor == null) {
				dimmerLight.setPowerLevel(0);
			} else {
				// and switch them on/off depending on the sensed presence
				if ((presenceSensor.getSensedPresence())) {
					if (numberOfLightsTurnedOn < this.maxLightsToTurnOnPerRoom) {
						dimmerLight.setPowerLevel(1.0);
						numberOfLightsTurnedOn = numberOfLightsTurnedOn + 1;
						energyConsumptionInLocation = energyConsumptionInLocation + LIGHT_POWER;
					} else if (energyConsumptionInLocation < this.maximumEnergyConsumptionAllowedInARoom) {
						dimmerLight.setPowerLevel(
								(this.maximumEnergyConsumptionAllowedInARoom - energyConsumptionInLocation)
										/ LIGHT_POWER);
						energyConsumptionInLocation = this.maximumEnergyConsumptionAllowedInARoom;
					} else {
						dimmerLight.setPowerLevel(0);
					}

				} else {
					dimmerLight.setPowerLevel(0);
				}
			}
		}

	}

	/**
	 * Return all BinaryLight from the given location
	 * 
	 * @param location
	 *            : the given location
	 * @return the list of matching BinaryLights
	 */
	private synchronized ArrayList<BinaryLight> getBinaryLightFromLocation(String location) {
		ArrayList<BinaryLight> binaryLightsLocations = new ArrayList<BinaryLight>();
		for (BinaryLight binLight : binaryLights) {
			if (binLight.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
				binaryLightsLocations.add(binLight);
			}
		}
		return binaryLightsLocations;
	}

	/**
	 * Return all DimmerLight from the given location
	 * 
	 * @param location
	 *            : the given location
	 * @return the list of matching DimmerLights
	 */
	private synchronized ArrayList<DimmerLight> getDimmerLightFromLocation(String location) {
		ArrayList<DimmerLight> dimmerLightsLocations = new ArrayList<DimmerLight>();
		for (DimmerLight dimLight : dimmerLights) {
			if (dimLight.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
				dimmerLightsLocations.add(dimLight);
			}
		}
		return dimmerLightsLocations;
	}

	/**
	 * Return all presence sensors in the given location
	 * 
	 * @param location The given location
	 * @return the list of Presence Sensors in the location
	 */
	private synchronized PresenceSensor getPresenceSensorFromLocation(String location) {
		for (PresenceSensor presenceSensor : presenceSensors) {
			if (presenceSensor.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
				return presenceSensor;
			}
		}
		return null;
	}

	@Override
	public int getMaximumNumberOfLightsToTurnOn() {
		return maxLightsToTurnOnPerRoom;
	}

	@Override
	public synchronized void setMaximumNumberOfLightsToTurnOn(int maximumNumberOfLightsToTurnOn) {
		this.maxLightsToTurnOnPerRoom = maximumNumberOfLightsToTurnOn;
		this.maximumEnergyConsumptionAllowedInARoom = ((double) maximumNumberOfLightsToTurnOn) * LIGHT_POWER;
		updateAllLights();
	}

	@Override
	public double getMaximumAllowedEnergyInRoom() {
		return maximumEnergyConsumptionAllowedInARoom;
	}

	@Override
	public synchronized void setMaximumAllowedEnergyInRoom(double maximumEnergy) {
		this.maximumEnergyConsumptionAllowedInARoom = maximumEnergy;
		this.maxLightsToTurnOnPerRoom = (int) (maximumEnergy / LIGHT_POWER);
		// Update the number of light on in the occupied room
		updateAllLights();

	}

	@Override
	public ArrayList<String> getOccupiedZones() {
		return occupiedZones;
	}

	@Override
	public void deviceAdded(GenericDevice arg0) {

	}

	@Override
	public void deviceEvent(GenericDevice arg0, Object arg1) {

	}

	@Override
	public void devicePropertyAdded(GenericDevice arg0, String arg1) {
	}

	@Override
	public void devicePropertyRemoved(GenericDevice arg0, String arg1) {
	}

	@Override
	public void deviceRemoved(GenericDevice arg0) {

	}

}
