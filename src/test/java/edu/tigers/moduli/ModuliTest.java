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
	public void testSingleModuleLoad() throws Exception
	{
		assertEquals(ModulesState.NOT_LOADED, moduli.getModulesState().get());
		
		moduli.loadModulesSafe(MODULE_CONFIG_PATH + "test_config.xml");
		assertEquals(ModulesState.RESOLVED, moduli.getModulesState().get());
		TestModule module = moduli.getModule(TestModule.class);
		assertTrue(module.isConstructed());
		
		moduli.startModules();
		assertEquals(ModulesState.ACTIVE, moduli.getModulesState().get());
		assertTrue(module.isInitialized());
		assertTrue(module.isStarted());
		
		moduli.stopModules();
		assertEquals(ModulesState.RESOLVED, moduli.getModulesState().get());
		assertTrue(module.isStopped());
		assertTrue(module.isDeinitialized());
	}
	
}