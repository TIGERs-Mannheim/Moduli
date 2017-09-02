/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.moduli.modules;

import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.StartModuleException;


public class ConcreteTestModule extends TestModule
{
	private boolean isConstructed = false;
	private boolean isInitialized = false;
	private boolean isStarted = false;
	private boolean isStopped = false;
	private boolean isDeinitialized = false;
	
	
	public ConcreteTestModule()
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
	
	
	@Override
	public boolean isConstructed()
	{
		return isConstructed;
	}
	
	
	@Override
	public boolean isInitialized()
	{
		return isInitialized;
	}
	
	
	@Override
	public boolean isStarted()
	{
		return isStarted;
	}
	
	
	@Override
	public boolean isStopped()
	{
		return isStopped;
	}
	
	
	@Override
	public boolean isDeinitialized()
	{
		return isDeinitialized;
	}
}
