/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.moduli;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
		assertEquals(ModulesState.NOT_LOADED, moduli.getModulesState().get());
		
		moduli.loadModulesSafe(MODULE_CONFIG_PATH + "test_config.xml");
		assertThat(moduli.getModulesState().get(), is(ModulesState.RESOLVED));
		
		moduli.startModules();
		assertThat(moduli.getModulesState().get(), is(ModulesState.ACTIVE));
		
		moduli.stopModules();
		assertThat(moduli.getModulesState().get(), is(ModulesState.RESOLVED));
	}
	
	
	@Test
	public void testModuleCycle() throws Exception
	{
		moduli.loadModulesSafe(MODULE_CONFIG_PATH + "test_config.xml");
		TestModule module = moduli.getModule(TestModule.class);
		assertTrue(module.isConstructed());
		
		moduli.startModules();
		assertTrue(module.isInitialized());
		assertTrue(module.isStarted());
		
		moduli.stopModules();
		assertTrue(module.isStopped());
		assertTrue(module.isDeinitialized());
	}
	
	
	@Test
	public void testEmptyConfig() throws Exception
	{
		moduli.loadModulesSafe(MODULE_CONFIG_PATH + "empty_config.xml");
		assertThat(moduli.getModulesState().get(), is(ModulesState.RESOLVED));
		
		moduli.startModules();
		assertThat(moduli.getModulesState().get(), is(ModulesState.ACTIVE));
		
		moduli.stopModules();
		assertThat(moduli.getModulesState().get(), is(ModulesState.RESOLVED));
	}
	
	
	@Test
	public void testGlobalConfiguration() throws Exception
	{
		moduli.loadModulesSafe(MODULE_CONFIG_PATH + "empty_config.xml");
		String env = moduli.getGlobalConfiguration().getString("environment");
		assertThat(env, is("MODULI"));
	}
	
	
	@Test
	public void testModuleConfiguration() throws Exception
	{
		moduli.loadModulesSafe(MODULE_CONFIG_PATH + "test_config.xml");
		moduli.startModules();
		ConfiguredTestModule module = moduli.getModule(ConfiguredTestModule.class);
		assertThat(module.getConfigProperty(), is("exists"));
	}
	
}