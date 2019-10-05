/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.moduli;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.DefaultConfigurationNode;
import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

import edu.tigers.moduli.exceptions.DependencyException;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.LoadModulesException;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.moduli.listenerVariables.ModulesStateVariable;


/**
 * Main-class of moduli.
 * It contains the handling of the modules.
 */
public class Moduli
{
	private static final Logger log = Logger.getLogger(Moduli.class.getName());
	private final Map<Class<? extends AModule>, AModule> modules = new HashMap<>();
	private List<AModule> orderedModules = new LinkedList<>();
	private SubnodeConfiguration globalConfiguration;
	private ModulesStateVariable modulesState = new ModulesStateVariable();
	private XMLConfiguration config;


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
	 */
	public void loadModules(final String xmlFile) throws LoadModulesException, DependencyException
	{
		modules.clear();
		orderedModules.clear();

		modulesState.set(ModulesState.NOT_LOADED);
		loadModulesFromFile(xmlFile);

		DirectedGraph<AModule, DefaultEdge> dependencyGraph = buildDependencyGraph();
		new TopologicalOrderIterator<>(dependencyGraph).forEachRemaining(m -> orderedModules.add(0, m));

		modulesState.set(ModulesState.RESOLVED);
	}


	private void loadModulesFromFile(String xmlFile) throws LoadModulesException
	{
		try
		{
			config = new XMLConfiguration(xmlFile);

			setGlobalConfiguration();

			constructModules();

		} catch (ConfigurationException e)
		{
			throw new LoadModulesException("Configuration contains errors: " + e.getMessage(), e);
		} catch (ClassNotFoundException e)
		{
			throw new LoadModulesException("Class in configuration can't be found: " + e.getMessage(), e);
		} catch (ClassCastException e)
		{
			throw new LoadModulesException("Given implementation is not an instance of AModule: " + e.getMessage(), e);
		} catch (SecurityException e)
		{
			throw new LoadModulesException("Security issue at configuration : " + e.getMessage(), e);
		} catch (IllegalArgumentException e)
		{
			throw new LoadModulesException("An argument isn't valid : " + e.getMessage(), e);
		}
	}


	private void setGlobalConfiguration()
	{
		globalConfiguration = getModuleConfig("globalConfiguration");
	}


	@SuppressWarnings("unchecked")
	private void constructModules() throws ClassNotFoundException, LoadModulesException
	{
		for (int i = 0; i <= config.getMaxIndex("module"); i++)
		{
			Class<? extends AModule> id = (Class<? extends AModule>) Class
					.forName(config.getString(moduleMessage(i, "[@id]")));

			Class<? extends AModule> clazz = getImplementation(i, id);

			SubnodeConfiguration moduleConfig = getModuleConfig(moduleMessage(i, ".properties"));

			AModule module = (AModule) createObject(clazz);

			module.setSubnodeConfiguration(moduleConfig);

			module.setId(id);

			checkModuleIsUnique(module);

			// --- set dependency-list ---
			String[] rawDependencyList = config.getStringArray(moduleMessage(i, ".dependency"));
			List<Class<? extends AModule>> dependencyList = new ArrayList<>();
			for (String dependency : rawDependencyList)
			{
				dependencyList.add((Class<? extends AModule>) Class.forName(dependency));
			}
			module.setDependencies(dependencyList);


			modules.put(id, module);

			log.trace("Module created: " + module);
		}
	}


	@SuppressWarnings("unchecked")
	private Class<? extends AModule> getImplementation(final int i, final Class<? extends AModule> id)
			throws ClassNotFoundException
	{
		final String implementationKey = moduleMessage(i, ".implementation");
		if (config.containsKey(implementationKey))
		{
			return (Class<? extends AModule>) Class.forName(config.getString(implementationKey));
		}
		return id;
	}


	private SubnodeConfiguration getModuleConfig(final String key)
	{
		try
		{
			return config.configurationAt(key);
		} catch (IllegalArgumentException e)
		{
			return new SubnodeConfiguration(new HierarchicalConfiguration(), new DefaultConfigurationNode());
		}
	}


	private void checkModuleIsUnique(final AModule module) throws LoadModulesException
	{
		if (modules.containsKey(module.getId()))
		{
			throw new LoadModulesException("module-id '" + module.getId() + "' isn't unique.");
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
		initModules(orderedModules);
		startUpModules(orderedModules);

		modulesState.set(ModulesState.ACTIVE);
	}


	private DirectedGraph<AModule, DefaultEdge> buildDependencyGraph() throws DependencyException
	{
		try
		{
			DirectedAcyclicGraph<AModule, DefaultEdge> dependencyGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);
			for (AModule module : modules.values())
			{
				dependencyGraph.addVertex(module);
				for (Class<? extends AModule> dependencyId : module.getDependencies())
				{
					AModule dependency = modules.get(dependencyId);
					if (dependency == null)
					{
						throw new DependencyException(
								"Dependency " + dependencyId + " is required by " + module + ", but not started.");
					}
					dependencyGraph.addVertex(dependency);
					dependencyGraph.addEdge(module, dependency);
				}
			}
			return dependencyGraph;
		} catch (IllegalArgumentException e)
		{
			throw new DependencyException("Cycle in dependencies: ", e);
		}
	}


	private void initModules(List<AModule> orderedModules) throws InitModuleException
	{
		for (AModule m : orderedModules)
		{
			try
			{
				log.trace("Initializing module " + m);
				m.initModule();
				log.trace(moduleMessage(m, "initialized"));
			} catch (Exception err)
			{
				throw new InitModuleException("Could not initialize module " + m, err);
			}
		}
	}


	private void startUpModules(List<AModule> orderedModules) throws StartModuleException
	{
		for (AModule m : orderedModules)
		{
			if (!m.isStartModule())
			{
				continue;
			}
			try
			{
				log.trace("Starting module " + m);
				m.startModule();
				log.trace(moduleMessage(m, "started"));
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
		List<AModule> reversedModules = new ArrayList<>(orderedModules);
		Collections.reverse(reversedModules);

		internalStopModules(reversedModules);

		deinitModules(reversedModules);

		modulesState.set(ModulesState.RESOLVED);
	}


	private void internalStopModules(final List<AModule> reversedModules)
	{
		for (AModule m : reversedModules)
		{
			if (!m.isStartModule())
			{
				continue;
			}
			try
			{
				m.stopModule();
				log.trace(moduleMessage(m, "stopped"));
			} catch (Exception err)
			{
				log.error("Exception while stopping module: " + m, err);
			}
		}
	}


	private void deinitModules(final List<AModule> reversedModules)
	{
		for (AModule m : reversedModules)
		{
			try
			{
				m.deinitModule();
				log.trace(moduleMessage(m, "deinitialized"));
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
		return new ArrayList<>(modules.values());
	}


	/**
	 * Gets a module from current module-list.
	 *
	 * @param moduleId the type of the model
	 * @return the instance of the module for the id
	 * @throws ModuleNotFoundException if the module couldn't be found
	 */
	public <T extends AModule> T getModule(Class<T> moduleId)
	{
		return getModuleOpt(moduleId)
				.orElseThrow(() -> new ModuleNotFoundException(moduleMessage(moduleId, "not found")));
	}


	/**
	 * Gets a module from current module-list.
	 *
	 * @param moduleId the type of the model
	 * @return the instance of the module for the id
	 */
	@SuppressWarnings("unchecked")
	public <T extends AModule> Optional<T> getModuleOpt(Class<T> moduleId)
	{
		final AModule aModule = modules.get(moduleId);
		if (aModule != null)
		{
			return Optional.of((T) aModule);
		}
		return modules.values().stream()
				.filter(m -> m.getClass().equals(moduleId))
				.map(a -> (T) a)
				.findFirst();
	}


	/**
	 * Check whether a module is loaded.
	 *
	 * @param moduleId the Class of the module
	 * @return if the module is loaded
	 */
	public boolean isModuleLoaded(Class<? extends AModule> moduleId)
	{
		return modules.containsKey(moduleId)
				|| modules.values().stream().map(Object::getClass).anyMatch(c -> c.equals(moduleId));
	}


	/**
	 * Creates an object from a clazz.
	 *
	 * @param clazz
	 */
	private Object createObject(final Class<? extends AModule> clazz)
	{
		try
		{
			Constructor<? extends AModule> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			return constructor.newInstance();
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException
				| IllegalArgumentException e)
		{
			throw new IllegalArgumentException("Error constructing module", e);
		}
	}


	private String moduleMessage(Object module, String message)
	{
		return "Module " + module + " " + message;
	}


	private String moduleMessage(int moduleNumber, String property)
	{
		return "module(" + moduleNumber + ")" + property;
	}
}
