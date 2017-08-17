/*
 * *********************************************************
 * Copyright (c) 2009 DHBW Mannheim - Tigers Mannheim
 * Project: tigers-centralSoftware
 * Date: 06.10.2009
 * Authors: Bernhard Perun <bernhard.perun@googlemail.com>
 * *********************************************************
 */
package edu.tigers.moduli.listenerVariables;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


/**
 * Create a listener on central stateApplication - variable at main-model.
 */
public class ModulesStateVariable
{
	private final PropertyChangeSupport support;
	private ModulesState stateModules = ModulesState.NOT_LOADED;
	
	
	/**
	 * Initialize support for property-change-listener.
	 */
	public ModulesStateVariable()
	{
		support = new PropertyChangeSupport(this);
	}
	
	
	/**
	 * Getter-method.
	 *
	 * @return the state of the module
	 */
	public ModulesState get()
	{
		return stateModules;
	}
	
	
	/**
	 * Setter-method.
	 * 
	 * @param stateApplicationNew new state
	 */
	public void set(final ModulesState stateApplicationNew)
	{
		ModulesState oldValue = stateModules;
		stateModules = stateApplicationNew;
		support.firePropertyChange("stateModules", oldValue, stateApplicationNew);
	}
	
	
	/**
	 * @param moduleState the state of the module
	 * @return whether the given module state equals the state of this module
	 */
	public boolean equals(final ModulesState moduleState)
	{
		return stateModules.equals(moduleState);
	}
	
	
	/**
	 * Add a change-listener.
	 * 
	 * @param listener PropertyChangeListener
	 */
	public void addChangeListener(final PropertyChangeListener listener)
	{
		support.addPropertyChangeListener(listener);
	}
	
	
	/**
	 * Remove a change-listener.
	 * 
	 * @param listener PropertyChangeListener
	 */
	public void removeChangeListener(final PropertyChangeListener listener)
	{
		support.removePropertyChangeListener(listener);
	}
	
}
