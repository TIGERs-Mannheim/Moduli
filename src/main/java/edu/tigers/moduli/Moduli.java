/*
 * *********************************************************
 * Copyright (c) 2010 DHBW Mannheim - Tigers Mannheim
 * Project: tigers-centralSoftware
 * Date: 04.03.2010
 * Authors:
 * Bernhard Perun <bernhard.perun@googlemail.com>
 * *********************************************************
 */

package edu.tigers.moduli;

import edu.tigers.moduli.exceptions.*;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.moduli.listenerVariables.ModulesStateVariable;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Main-class of moduli.
 * It contains the handling of the modules.
 */
public class Moduli
{
	private SubnodeConfiguration		globalConfiguration;
												
	private static final Logger		log					= Logger.getLogger(Moduli.class.getName());

	private final ArrayList<AModule> moduleList = new ArrayList<>();

	private ModulesStateVariable modulesState = new ModulesStateVariable();
																		
	private static final Class<?>[]	PROP_ARGS_CLASS	= new Class[] { SubnodeConfiguration.class };
																		
																		
	/**
	 * Getter modulesState.
	 * 
	 * @return the state of the modules
	 */
	public ModulesStateVariable getModulesState()
	{
		return modulesState;
	}
	
	
	/**
	 * Setter modulesState.
	 * Only to use if you know what you are doing ;).
	 * 
	 * @param modulesState the new modulesState to set
	 */
	public void setModulesState(final ModulesStateVariable modulesState)
	{
		this.modulesState = modulesState;
	}
	
	
	/**
	 * Getter global configuration
	 * 
	 * @return the global configuration
	 */
	public SubnodeConfiguration getGlobalConfiguration()
	{
		return globalConfiguration;
	}
	
	
	/**
	 * Loads all available modules from configuration-file into modulesList.
	 * 
	 * @param xmlFile (module-)configuration-file
	 * @throws LoadModulesException an error occurs... Can't continue.
	 * @throws DependencyException when dependencies are not met
	 */
	public void loadModules(final String xmlFile) throws LoadModulesException, DependencyException
	{
		moduleList.clear();
		
		modulesState.set(ModulesState.NOT_LOADED);
		fillModuleListWithNewModules(xmlFile);


		checkDependencies();

		modulesState.set(ModulesState.RESOLVED);
	}

	private void fillModuleListWithNewModules(String xmlFile) throws LoadModulesException {
		try {
			XMLConfiguration config;

			config = new XMLConfiguration(xmlFile);

			// --- set moduli-folder ---
			String implsPath = config.getString("moduliPath");
			if (!implsPath.isEmpty())
			{
				implsPath += ".";
			}

			// --- set globalConfiguration ---
			globalConfiguration = config.configurationAt("globalConfiguration");

			// --- load modules into modulesList ---
			for (int i = 0; i <= config.getMaxIndex("module"); i++)
			{

				// --- create implementation- and properties-class ---
				Class<?> clazz = Class.forName(implsPath + config.getString("module(" + i + ").implementation"));

				// --- get properties from configuration and put it into a object[] ---
				SubnodeConfiguration moduleConfig = config.configurationAt("module(" + i + ").properties");
				Object[] propArgs = new Object[] { moduleConfig };

				// --- get constructor of implementation-class with subnodeConfiguration-parameter ---
				Constructor<?> clazzConstructor = clazz.getConstructor(PROP_ARGS_CLASS);

				// --- create object (use constructor) ---
				AModule module = (AModule) createObject(clazzConstructor, propArgs);

				// --- set module config ---
				module.setSubnodeConfiguration(moduleConfig);

				// --- set id ---
				module.setId(config.getString("module(" + i + ")[@id]"));

				// --- check if module is unique ---
				for (AModule m : moduleList)
				{
					if (m.getId().equals(module.getId()))
					{
						throw new LoadModulesException("module-id '" + module.getId() + "' isn't unique.");
					}
				}

				// --- set dependency-list ---
				List<String> depList = Arrays.asList(config.getStringArray("module(" + i + ").dependency"));
				module.setDependencies(depList);


				// --- put module into moduleList ---
				moduleList.add(module);

				log.trace("Module created: " + module);
			}

		} catch (ConfigurationException e) {
			throw new LoadModulesException("Configuration contains errors: " + e.getMessage(), e);
		} catch (ClassNotFoundException e)
		{
			throw new LoadModulesException("Class in configuration can't be found: " + e.getMessage(), e);
		} catch (SecurityException e)
		{
			throw new LoadModulesException("Security issue at configuration : " + e.getMessage(), e);
		} catch (NoSuchMethodException e)
		{
			throw new LoadModulesException(
					"Can't find a constructor <init>(SubnodeConfiguration) of this class. Please add one. : "
							+ e.getMessage(),
					e);
		} catch (IllegalArgumentException e)
		{
			throw new LoadModulesException("An argument isn't valid : " + e.getMessage(), e);
		}
	}


	/**
	 * Load modules and catch exceptions
	 * 
	 * @param filename of the moduli config
	 */
	public void loadModulesSafe(final String filename)
	{
		try
		{
			// --- get modules from configuration-file ---
			loadModules(filename);
			log.debug("Loaded config: " + filename);
		} catch (final LoadModulesException | DependencyException e)
		{
			log.error(e.getMessage() + " (moduleConfigFile: '" + filename
					+ "') ", e);
		}
	}
	
	
	/**
	 * Starts all modules in modulesList.
	 * 
	 * @throws InitModuleException if the initialization of a module fails
	 * @throws StartModuleException if the start of a module fails
	 */
	public void startModules() throws InitModuleException, StartModuleException
	{
		initModules();
		startUpModules();

		modulesState.set(ModulesState.ACTIVE);
	}

	private void initModules() throws InitModuleException {
		for (AModule m : moduleList) {
			try
			{
				log.trace("Initializing module " + m);
				m.initModule();
				log.trace("Module " + m + " initialized");
			} catch (Exception err)
			{
				throw new InitModuleException("Could not initialize module " + m, err);
			}
		}
	}

	private void startUpModules() throws StartModuleException {
		for (AModule m : moduleList) {
			if (!m.isStartModule())
			{
				continue;
			}
			try
			{
				log.trace("Starting module " + m);
				m.startModule();
				log.trace("Module " + m + " started");
			} catch (Exception err)
			{
				throw new StartModuleException("Could not initialize module " + m, err);
			}
		}
	}


	/**
	 * Stops all modules in modulesList.
	 */
	public void stopModules()
	{
		internalStopModules();

		deinitModules();

		modulesState.set(ModulesState.RESOLVED);
	}

	private void internalStopModules() {
		for (AModule m : moduleList) {
			if (!m.isStartModule())
			{
				continue;
			}
			try
			{
				m.stopModule();
				log.trace("Module " + m + " stopped");
			} catch (Exception err)
			{
				log.error("Exception while stopping module: " + m, err);
			}
		}
	}

	private void deinitModules() {
		for (AModule m : moduleList) {
			try
			{
				m.deinitModule();
				log.trace("Module " + m + " deinitialized");
			} catch (Exception err)
			{
				log.error("Exception while deinitializing module: " + m, err);
			}
		}
	}


	/**
	 * Returns a list with all loaded modules.
	 * 
	 * @return all modules
	 */
	public List<AModule> getModules()
	{
		return moduleList;
	}
	
	
	/**
	 * Gets a module from current module-list.
	 * 
	 * @param moduleId module-id-string
	 * @throws ModuleNotFoundException if the module couldn't be found
	 * @return the instance of the module for the id
	 */
	public AModule getModule(final String moduleId) throws ModuleNotFoundException
	{
		// --- search for the module ---
		for (AModule m : moduleList)
		{
			if (m.getId().equals(moduleId))
			{
				return m;
			}
		}
		
		// --- if nothing was found, throw a ModuleNotFoundException ---
		throw new ModuleNotFoundException("Module " + moduleId + " not found");
	}
	
	
	/**
	 * Checks, if dependencies can be resolved.
	 * 
	 * @throws DependencyException ... if at least one modules can't be resolved
	 */
	private void checkDependencies() throws DependencyException
	{
		
		// --- variable which indicates if dependencies are okay ---
		boolean dependenciesOk = false;

		// --- check if all dependencies can be resolved ---
		for (AModule m : moduleList)
		{
			
			for (String dependency : m.getDependencies())
			{
				// --- reset depOk ---
				dependenciesOk = false;

				for (AModule n : moduleList) {
					if (n.getId().equals(dependency))
					{
						// --- dep is okay ---
						dependenciesOk = true;
						break;
					}
				}
				
				// --- check if one dependencies isn't met ---
				if (!dependenciesOk) {
					throw new DependencyException("Dependency '" + dependency + "' isn't met at module '" + m.getId() + "'");
				}
				
			}
			
		}
	}
	
	
	/**
	 * Creates an object from a constructor and its arguments.
	 */
	private Object createObject(final Constructor<?> constructor, final Object[] arguments)
	{
		try
		{
			return constructor.newInstance(arguments);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e)
		{
			log.error(e.getMessage(), e);
		}
		return null;
	}
}
