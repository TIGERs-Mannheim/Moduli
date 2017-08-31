/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.moduli.modules;

import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.StartModuleException;


public class TestModule extends AModule
{
	private boolean isConstructed = false;
	private boolean isInitialized = false;
	private boolean isStarted = false;
	private boolean isStopped = false;
	private boolean isDeinitialized = false;
	
	
	public TestModule()
	{
		this.isConstructed = true;
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
		isInitialized = true;
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		isStarted = true;
	}
	
	
	@Override
	public void stopModule()
	{
		isStopped = true;
	}
	
	
	@Override
	public void deinitModule()
	{
		isDeinitialized = true;
	}
	
	
	public boolean isConstructed()
	{
		return isConstructed;
	}
	
	
	public boolean isInitialized()
	{
		return isInitialized;
	}
	
	
	public boolean isStarted()
	{
		return isStarted;
	}
	
	
	public boolean isStopped()
	{
		return isStopped;
	}
	
	
	public boolean isDeinitialized()
	{
		return isDeinitialized;
	}
}
