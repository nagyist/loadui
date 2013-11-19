package com.eviware.loadui.components.soapui.utils;

import com.eviware.loadui.integration.SoapUIProjectLoader;
import com.eviware.soapui.impl.wsdl.WsdlProjectPro;
import com.eviware.soapui.impl.wsdl.WsdlProjectProFactory;
import com.eviware.soapui.model.project.ProjectFactoryRegistry;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.dialogs.XFileDialogs;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author renato
 */
public class CompositeProjectUtilsTest
{

	CompositeProjectUtils utils = new CompositeProjectUtils();
	WsdlProjectProFactory proFactory = new WsdlProjectProFactory();

	@Before
	public void setup()
	{
		ProjectFactoryRegistry.registrerProjectFactory( WsdlProjectProFactory.WSDL_TYPE, proFactory );
	}

	@Test( expected = IllegalArgumentException.class )
	public void throwsExceptionIfTryingToConvertNonDirectory()
	{
		File nonDir = mock( File.class );
		when( nonDir.exists() ).thenReturn( true );
		when( nonDir.isDirectory() ).thenReturn( false );

		utils.fromCompositeDirectory( nonDir );
	}

	@Test( expected = IllegalArgumentException.class )
	public void throwsExceptionIfFileDoesNotExist()
	{
		File nonDir = mock( File.class );
		when( nonDir.exists() ).thenReturn( false );
		when( nonDir.isDirectory() ).thenReturn( true );

		utils.fromCompositeDirectory( nonDir );
	}

	@Test
	public void createsASoapUIProjectFileFromCompositeProjectDirectory() throws Exception
	{
		File originalSoapuiProjectFile = new File( getClass().getResource( "/soapUI-loadUI-plugin-project.xml" ).getFile() );

		File compositeProject = makeProjectCompositeAndGetIt(
				originalSoapuiProjectFile,
				new File( Files.createTempDir(), "test-soapui-project" ) );

		File result = utils.fromCompositeDirectory( compositeProject );

		assertThat( result.isFile(), is( true ) );

		Collection<String> resultEntries = getXmlEntries( readFileToString( result ) );
		Collection<String> expectedEntries = getXmlEntries( readFileToString( originalSoapuiProjectFile ) );

		assertThat( resultEntries, hasSize( 29 ) );
		assertThat( resultEntries, hasItems( Iterables.toArray( expectedEntries, String.class ) ) );
	}

	private Collection<String> getXmlEntries( String xmlResult )
	{
		return filter( newArrayList( Splitter.on( '\n' ).trimResults().split( xmlResult ) ), isEntry() );
	}

	private File makeProjectCompositeAndGetIt( File originalFile, File toSave ) throws Exception
	{
		XFileDialogs soapuiFileDialogs = mock( XFileDialogs.class );
		when( soapuiFileDialogs.saveAs( any(), anyString(), anyString(), anyString(), any( File.class ) ) )
				.thenReturn( toSave );

		UISupport.setFileDialogs( soapuiFileDialogs );

		WsdlProjectPro project = proFactory.createNew();
		project.loadProject( originalFile.toURI().toURL() );
		project.setComposite( true );
		project.save();

		// non composite project must be unloaded here
		SoapUIProjectLoader.getInstance().releaseProject( project );

		return new File( project.getPath() );
	}

	private static Predicate<String> isEntry()
	{
		return new Predicate<String>()
		{
			@Override
			public boolean apply( @Nullable String input )
			{
				return input != null && input.trim().startsWith( "<con:entry key" );
			}
		};
	}

}
