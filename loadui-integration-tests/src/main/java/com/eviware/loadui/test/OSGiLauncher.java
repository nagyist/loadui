/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.test;

import com.eviware.loadui.launcher.LoadUILauncher;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import java.util.Properties;

public class OSGiLauncher extends LoadUILauncher
{
	public OSGiLauncher( String[] args )
	{
		super( args );
	}

	@Override
	public void init()
	{
		super.init();
	}

	@Override
	protected void processOsgiExtraPackages()
	{

	}

	@Override
	public void start()
	{
		super.start();
	}

	@Override
	protected String commandLineServiceOsgiFilter()
	{
		return null;
	}

	public BundleContext getBundleContext()
	{
		return framework.getBundleContext();
	}

	public void stop() throws BundleException
	{
		framework.stop();
	}

	@Override
	protected void afterStart()
	{
		// no action
	}

	public Properties getConfig()
	{
		return configProps;
	}
}
