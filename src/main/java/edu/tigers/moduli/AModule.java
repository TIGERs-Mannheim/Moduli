package edu.tigers.moduli;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.SubnodeConfiguration;

import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.StartModuleException;


/**
 * Structure for
 */
public abstract class AModule
{
	// --------------------------------------------------------------------------
	// --- instance variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	
	// --- module-infos ---
	private String						id;
	private String						type;
	private SubnodeConfiguration	subnodeConfiguration;
	private List<String>				dependencies	= new ArrayList<String>();
	private boolean					startModule		= true;
	
	
	// --------------------------------------------------------------------------
	// --- abstract-methods -----------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Inits module.
	 * 
	 * @throws InitModuleException
	 */
	public abstract void initModule() throws InitModuleException;
	
	
	/**
	 * DeInits module.
	 */
	public abstract void deinitModule();
	
	
	/**
	 * Starts module.
	 * 
	 * @throws StartModuleException
	 */
	public abstract void startModule() throws StartModuleException;
	
	
	/**
	 * Stops module.
	 */
	public abstract void stopModule();
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return
	 */
	public String getId()
	{
		return id;
	}
	
	
	/**
	 * @param id
	 */
	public void setId(final String id)
	{
		this.id = id;
	}
	
	
	/**
	 * @return
	 */
	public String getType()
	{
		return type;
	}
	
	
	/**
	 * @param type
	 */
	public void setType(final String type)
	{
		this.type = type;
	}
	
	
	/**
	 * @return
	 */
	public List<String> getDependencies()
	{
		return dependencies;
	}
	
	
	/**
	 * @param dependencies
	 */
	public void setDependencies(final List<String> dependencies)
	{
		this.dependencies = dependencies;
	}
	
	
	/**
	 * @return
	 */
	public SubnodeConfiguration getSubnodeConfiguration()
	{
		return subnodeConfiguration;
	}
	
	
	void setSubnodeConfiguration(final SubnodeConfiguration subnodeConfiguration)
	{
		this.subnodeConfiguration = subnodeConfiguration;
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("[id=");
		builder.append(id);
		builder.append(", type=");
		builder.append(type);
		builder.append("]");
		return builder.toString();
	}
	
	
	/**
	 * @return the active
	 */
	public boolean isStartModule()
	{
		return startModule;
	}
	
	
	/**
	 * @param startModule
	 */
	public void setStartModule(final boolean startModule)
	{
		this.startModule = startModule;
	}
}
