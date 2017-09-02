/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.moduli;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.moduli.modules.ConfiguredTestModule;
import edu.tigers.moduli.modules.TestModule;


public class ModuliTest
{
	
	private static final String MODULE_CONFIG_PATH = "src/test/resources/";
	private Moduli moduli;
	
	
	@Before
	public void setUp() throws Exception
	{
		moduli = new Moduli();
	}
	
	
	@After
	public void tearDown() throws Exception
	{
		moduli = null;
	}
	
	
	@Test
	public void testModuliCycle() throws Exception
	{
		assertThat(moduli.getModulesState().get()).isEqualTo(ModulesState.NOT_LOADED);
		
		moduli.loadModulesSafe(MODULE_CONFIG_PATH + "test_config.xml");
		assertThat(moduli.getModulesState().get()).isEqualTo(ModulesState.RESOLVED);
		assertThat(moduli.isModuleLoaded(TestModule.class)).isTrue();
		
		moduli.startModules();
		assertThat(moduli.getModulesState().get()).isEqualTo(ModulesState.ACTIVE);
		
		moduli.stopModules();
		assertThat(moduli.getModulesState().get()).isEqualTo(ModulesState.RESOLVED);
	}
	
	
	@Test
	public void testModuleCycle() throws Exception
	{
		moduli.loadModulesSafe(MODULE_CONFIG_PATH + "test_config.xml");
		TestModule module = moduli.getModule(TestModule.class);
		assertThat(module.isConstructed()).isTrue();
		
		moduli.startModules();
		assertThat(module.isInitialized()).isTrue();
		assertThat(module.isStarted()).isTrue();
		
		moduli.stopModules();
		assertThat(module.isStopped()).isTrue();
		assertThat(module.isDeinitialized()).isTrue();
	}
	
	
	@Test
	public void testEmptyConfig() throws Exception
	{
		moduli.loadModulesSafe(MODULE_CONFIG_PATH + "empty_config.xml");
		assertThat(moduli.getModulesState().get()).isEqualTo(ModulesState.RESOLVED);
		assertThat(moduli.isModuleLoaded(TestModule.class)).isFalse();
		
		moduli.startModules();
		assertThat(moduli.getModulesState().get()).isEqualTo(ModulesState.ACTIVE);
		
		moduli.stopModules();
		assertThat(moduli.getModulesState().get()).isEqualTo(ModulesState.RESOLVED);
	}
	
	
	@Test
	public void testGlobalConfiguration() throws Exception
	{
		moduli.loadModulesSafe(MODULE_CONFIG_PATH + "empty_config.xml");
		String env = moduli.getGlobalConfiguration().getString("environment");
		assertThat(env).isEqualTo("MODULI");
	}
	
	
	@Test
	public void testModuleConfiguration() throws Exception
	{
		moduli.loadModulesSafe(MODULE_CONFIG_PATH + "test_config.xml");
		moduli.startModules();
		ConfiguredTestModule module = moduli.getModule(ConfiguredTestModule.class);
		assertThat(module.getConfigProperty()).isEqualTo("exists");
	}
	
}