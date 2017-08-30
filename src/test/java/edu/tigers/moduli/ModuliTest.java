/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.moduli;

import static org.junit.Assert.assertEquals;
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
		assertEquals(ModulesState.RESOLVED, moduli.getModulesState().get());
		
		moduli.startModules();
		assertEquals(ModulesState.ACTIVE, moduli.getModulesState().get());
		
		moduli.stopModules();
		assertEquals(ModulesState.RESOLVED, moduli.getModulesState().get());
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
		assertEquals(ModulesState.RESOLVED, moduli.getModulesState().get());
		
		moduli.startModules();
		assertEquals(ModulesState.ACTIVE, moduli.getModulesState().get());
		
		moduli.stopModules();
		assertEquals(ModulesState.RESOLVED, moduli.getModulesState().get());
	}
	
	
	@Test
	public void testGlobalConfiguration() throws Exception
	{
		moduli.loadModulesSafe(MODULE_CONFIG_PATH + "empty_config.xml");
		String env = moduli.getGlobalConfiguration().getString("environment");
		assertEquals("MODULI", env);
	}
	
	
	@Test
	public void testModuleConfiguration() throws Exception
	{
		moduli.loadModulesSafe(MODULE_CONFIG_PATH + "test_config.xml");
		moduli.startModules();
		ConfiguredTestModule module = moduli.getModule(ConfiguredTestModule.class);
		assertEquals("exists", module.getConfigProperty());
	}
	
}