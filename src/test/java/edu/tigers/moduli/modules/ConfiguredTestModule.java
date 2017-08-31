/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.moduli.modules;

import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.StartModuleException;


public class ConfiguredTestModule extends AModule
{
	private String configProperty;
	
	
	@Override
	public void initModule() throws InitModuleException
	{
		configProperty = getSubnodeConfiguration().getString("testProperty");
	}
	
	
	@Override
	public void deinitModule()
	{
		
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		
	}
	
	
	@Override
	public void stopModule()
	{
		
	}
	
	
	public String getConfigProperty()
	{
		return configProperty;
	}
}
