package com.mrcrayfish.guns.compat;

import com.github.exopandora.shouldersurfing.api.model.Perspective;
import com.mrcrayfish.guns.GunMod;

import java.lang.reflect.Method;

/**
 * Author: MrCrayfish
 */
public class ShoulderSurfingHelper
{
	private static WorkingApi workingApi = null;
	
	private record ApiDescriptor(String className, String getInstanceMethod, String isShoulderSurfingMethod, String changePerspectiveMethod)
	{
	}
	
	private static final ApiDescriptor[] API_CANDIDATES = {
			// Newer ShoulderSurfing Reloaded API
			new ApiDescriptor("com.github.exopandora.shouldersurfing.client.ShoulderSurfingImpl", "getInstance", "isShoulderSurfing", "changePerspective"),
			// Legacy API (ShoulderInstance)
			new ApiDescriptor("com.github.exopandora.shouldersurfing.client.ShoulderInstance", "getInstance", "doShoulderSurfing", "changePerspective")};
	
	private record WorkingApi(Object instance, Method isShoulderSurfingMethod, Method changePerspectiveMethod)
	{
		boolean isShoulderSurfing()
		{
			try
			{
				return (boolean) isShoulderSurfingMethod.invoke(instance);
			}
			catch(Exception e)
			{
				GunMod.LOGGER.error("Failed to invoke isShoulderSurfing on working API", e);
				return false;
			}
		}
		
		void changePerspective(Perspective perspective)
		{
			try
			{
				changePerspectiveMethod.invoke(instance, perspective);
			}
			catch(Exception e)
			{
				GunMod.LOGGER.error("Failed to invoke changePerspective on working API", e);
			}
		}
	}
	
	static
	{
		// Try each API candidate once during class loading
		for(ApiDescriptor candidate : API_CANDIDATES)
		{
			try
			{
				Class<?> clazz = Class.forName(candidate.className);
				Method getInstance = clazz.getDeclaredMethod(candidate.getInstanceMethod);
				Object instance = getInstance.invoke(null);
				
				Method isMethod = clazz.getDeclaredMethod(candidate.isShoulderSurfingMethod);
				Method changeMethod = clazz.getDeclaredMethod(candidate.changePerspectiveMethod, Perspective.class);
				
				workingApi = new WorkingApi(instance, isMethod, changeMethod);
				GunMod.LOGGER.info("ShoulderSurfing helper initialized with API: {}", candidate.className);
				break;
			}
			catch(Exception e)
			{
				// Log at DEBUG level – expected when mod is not present or API version mismatched
				GunMod.LOGGER.debug("Failed to load ShoulderSurfing API candidate {}: {}", candidate.className, e.getMessage());
			}
		}
		
		if(workingApi == null)
		{
			GunMod.LOGGER.info("ShoulderSurfing mod not detected – helper disabled");
		}
	}
	
	public static boolean isShoulderSurfing()
	{
		if(workingApi == null || !GunMod.shoulderSurfingLoaded)
		{
			return false;
		}
		return workingApi.isShoulderSurfing();
	}
	
	public static void changePerspective(String perspective)
	{
		if(workingApi == null || !GunMod.shoulderSurfingLoaded)
		{
			return;
		}
		
		Perspective target = switch(perspective.toUpperCase())
		{
			case "FIRST_PERSON" -> Perspective.FIRST_PERSON;
			case "THIRD_PERSON_BACK" -> Perspective.THIRD_PERSON_BACK;
			case "THIRD_PERSON_FRONT" -> Perspective.THIRD_PERSON_FRONT;
			default -> Perspective.SHOULDER_SURFING;
		};
		workingApi.changePerspective(target);
	}
}