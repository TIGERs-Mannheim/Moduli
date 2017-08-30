/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.moduli;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.tigers.moduli.modules.TestModule;


public class ModuliTest
{
	
	private static final String MODULE_CONFIG_PATH = "src/test/resources/test_config.xml";
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
	public void testSingleModuleLoad()
	{
		moduli.loadModulesSafe(MODULE_CONFIG_PATH);
		TestModule module = moduli.getModule(TestModule.class);
		assertTrue(module.isConstructed());
	}
	
}