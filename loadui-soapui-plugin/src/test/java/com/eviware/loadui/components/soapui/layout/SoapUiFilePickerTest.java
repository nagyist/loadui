package com.eviware.loadui.components.soapui.layout;

import com.eviware.loadui.api.ui.dialog.FilePickerDialogFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static com.eviware.loadui.components.soapui.layout.FileResolverTest.canonicalPath;
import static com.eviware.loadui.components.soapui.layout.FileResolverTest.relPath;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author renato
 */
public class SoapUiFilePickerTest
{

	SoapUiFilePicker filePicker;
	File baseDirForRelativePaths = new File( "base" ).getAbsoluteFile();

	@Before
	public void setup()
	{
		File selectedFile = new File( "abc" );
		FilePickerDialogFactory factory = mock( FilePickerDialogFactory.class );

		when( factory.showOpenDialog( anyString(), anyString(), anyString() ) )
				.thenReturn( selectedFile );

		filePicker = new SoapUiFilePicker( "Picker Title", ".xml", "*.xml",
				factory, baseDirForRelativePaths );
	}

	@Test
	public void canFigureOutWhereToStartARelativePath_FileUnderBaseDir()
	{
		File selected = new File( baseDirForRelativePaths, "selected.xml" );

		filePicker.setIsRelativePath( true );
		filePicker.setSelected( selected );

		assertThat( filePicker.textLabel.getText(), equalTo( baseDirForRelativePaths.getAbsolutePath() + File.separator ) );
		assertThat( filePicker.textField.getText(), equalTo( "selected.xml" ) );
		assertThat( filePicker.selectedProperty().get(), equalTo( selected ) );
	}

	@Test
	public void canFigureOutWhereToStartARelativePath_FileNotUnderBaseDir()
	{
		File selected = new File( baseDirForRelativePaths, relPath( "..", "another", "selected.xml" ) );

		filePicker.setIsRelativePath( true );
		filePicker.setSelected( selected );

		assertThat( filePicker.textLabel.getText(),
				equalTo( baseDirForRelativePaths.getAbsolutePath() + File.separator ) );
		assertThat( filePicker.textField.getText(),
				equalTo( relPath( "..", "another", "selected.xml" ) ) );
		assertThat( filePicker.selectedProperty().get(), equalTo( selected ) );
	}

	@Test
	public void canFigureOutAbsolutePath()
	{
		File selected = new File( "selected.xml" );

		filePicker.setIsRelativePath( false );
		filePicker.setSelected( selected );

		assertThat( filePicker.textLabel.getText(), equalTo( "" ) );
		assertThat( filePicker.textField.getText(), equalTo( new File( "selected.xml" ).getAbsolutePath() ) );
		assertThat( filePicker.selectedProperty().get(), equalTo( selected ) );
	}

	@Test
	public void canTurnAnAbsolutePathIntoARelativePath()
	{
		File selected = new File( baseDirForRelativePaths, "selected.xml" );

		filePicker.setIsRelativePath( false );
		filePicker.setSelected( selected );
		filePicker.setIsRelativePath( true );

		assertThat( filePicker.textLabel.getText(), equalTo( baseDirForRelativePaths.getAbsolutePath() + File.separator ) );
		assertThat( filePicker.textField.getText(), equalTo( "selected.xml" ) );
		assertThat( filePicker.selectedProperty().get(), equalTo( selected ) );
	}

	@Test
	public void canTurnARelativePathIntoAnAbsolutePath()
	{
		File selected = new File( baseDirForRelativePaths, "selected.xml" );

		filePicker.setIsRelativePath( true );
		filePicker.setSelected( selected );
		filePicker.setIsRelativePath( false );

		assertThat( filePicker.textLabel.getText(), equalTo( "" ) );
		assertThat( filePicker.textField.getText(), equalTo( new File( baseDirForRelativePaths, "selected.xml" ).getAbsolutePath() ) );
		assertThat( filePicker.selectedProperty().get(), equalTo( selected ) );
	}

	@Test
	public void afterTurningRelativePathToAbsolutePathCanChangeTextToUpdateFile() throws Exception
	{
		filePicker.setIsRelativePath( false );

		File firstSelected = mock( File.class );
		when( firstSelected.exists() ).thenReturn( true );
		when( firstSelected.isFile() ).thenReturn( true );
		when( firstSelected.getPath() ).thenReturn( "first.xml" );
		when( firstSelected.getAbsolutePath() ).thenReturn( new File( baseDirForRelativePaths, "first.xml" ).getAbsolutePath() );

		File secondSelected = mock( File.class );
		File secondFile = new File( baseDirForRelativePaths, relPath( "other", "second.xml" ) );
		String secondAbsPath = secondFile.getAbsolutePath();
		when( secondSelected.exists() ).thenReturn( true );
		when( secondSelected.isFile() ).thenReturn( true );
		when( secondSelected.getPath() ).thenReturn( relPath( "other", "second.xml" ) );
		when( secondSelected.getAbsolutePath() ).thenReturn( secondAbsPath );

		SoapUiFilePicker.FileResolver mockResolver = mock( SoapUiFilePicker.FileResolver.class );
		when( mockResolver.resolveFromText( false, baseDirForRelativePaths, "first.xml" ) ).thenReturn( firstSelected );
		when( mockResolver.resolveFromText( false, baseDirForRelativePaths, secondAbsPath ) ).thenReturn( secondSelected );
		when( mockResolver.abs2rel( any( File.class ), any( File.class ) ) ).thenCallRealMethod();
		when( mockResolver.rel2abs( any( File.class ), any( File.class ) ) ).thenCallRealMethod();
		filePicker.fileResolver = mockResolver;

		filePicker.setUpdateTextDelay( 10 );

		filePicker.setIsRelativePath( true );
		filePicker.setSelected( new File( baseDirForRelativePaths, "first.xml" ) );
		filePicker.setIsRelativePath( false );

		filePicker.onFileTextUpdated( secondAbsPath );

		Thread.sleep( 20 );

		assertThat( filePicker.textLabel.getText(), equalTo( "" ) );
		assertThat( filePicker.textField.getText(), equalTo( secondAbsPath ) );
		assertThat( canonicalPath( filePicker.selectedProperty().get().getAbsolutePath() ),
				equalTo( canonicalPath( secondAbsPath ) ) );
	}

	@Test
	public void whenSettingFileToNotUnderProjectFileDirPathMustBeRelative()
	{
		File selected = new File( baseDirForRelativePaths, "selected.xml" );

		filePicker.setIsRelativePath( false );
		filePicker.setSelected( selected );
		filePicker.setIsRelativePath( true );

		assertThat( filePicker.textLabel.getText(), equalTo( baseDirForRelativePaths.getAbsolutePath() + File.separator ) );
		assertThat( filePicker.textField.getText(), equalTo( "selected.xml" ) );
		assertThat( filePicker.selectedProperty().get(), equalTo( selected ) );
	}

	@Test
	public void fieldsAreUpdatedSomeTimeAfterUserStopsTyping_AndTextChangesColorsIfFileExistsOrNot() throws Exception
	{
		File oldFile = new File( "old" );
		filePicker.setSelected( oldFile );

		File happyFile = mock( File.class );
		when( happyFile.exists() ).thenReturn( true );
		when( happyFile.isFile() ).thenReturn( true );

		File badFile = mock( File.class );
		when( badFile.exists() ).thenReturn( false );

		SoapUiFilePicker.FileResolver mockResolver = mock( SoapUiFilePicker.FileResolver.class );
		when( mockResolver.resolveFromText( false, baseDirForRelativePaths, "a" ) ).thenReturn( badFile );
		when( mockResolver.resolveFromText( false, baseDirForRelativePaths, "ab" ) ).thenReturn( happyFile );
		filePicker.setUpdateTextDelay( 50 );
		filePicker.fileResolver = mockResolver;

		// actual value is not updated even after delay if new value is not good
		filePicker.onFileTextUpdated( "a" );
		Thread.sleep( 75 );
		assertThat( filePicker.selectedProperty().get(), equalTo( oldFile ) );
		assertThat( filePicker.textField.getStyle(), containsString( "red" ) );

		// ... and is not updated for a while if user keeps typing
		filePicker.onFileTextUpdated( "ab" );
		Thread.sleep( 30 );
		assertThat( filePicker.selectedProperty().get(), equalTo( oldFile ) );

		// ... but after the updateTextDelay is up, the selected file is updated if it exists (happy files always exist)
		Thread.sleep( 50 );
		assertThat( filePicker.selectedProperty().get(), equalTo( happyFile ) );
		assertThat( filePicker.textField.getStyle(), not( containsString( "red" ) ) );
	}

	@Test
	public void textTurnsRedWhenUserTypesFileNameThatDoesNotExist() throws Exception
	{
		assertThat( filePicker.textField.getStyle(), not( containsString( "red" ) ) );

		File badFile = mock( File.class );
		when( badFile.exists() ).thenReturn( false );

		SoapUiFilePicker.FileResolver mockResolver = mock( SoapUiFilePicker.FileResolver.class );
		when( mockResolver.resolveFromText( false, baseDirForRelativePaths, "bad" ) ).thenReturn( badFile );
		filePicker.fileResolver = mockResolver;

		filePicker.setUpdateTextDelay( 10 );

		filePicker.onFileTextUpdated( "bad" );

		Thread.sleep( 25 );

		assertThat( filePicker.textField.getStyle(), containsString( "red" ) );
	}

	@Test
	public void textTurnsBlackWhenUserTypesFileNameThatExists() throws Exception
	{
		filePicker.setIsRelativePath( false );

		File happyFile = mock( File.class );
		when( happyFile.exists() ).thenReturn( true );
		when( happyFile.isFile() ).thenReturn( true );

		File badFile = mock( File.class );
		when( badFile.exists() ).thenReturn( false );

		SoapUiFilePicker.FileResolver mockResolver = mock( SoapUiFilePicker.FileResolver.class );
		when( mockResolver.resolveFromText( false, baseDirForRelativePaths, "bad" ) ).thenReturn( badFile );
		when( mockResolver.resolveFromText( false, baseDirForRelativePaths, "good" ) ).thenReturn( happyFile );
		filePicker.fileResolver = mockResolver;

		filePicker.setUpdateTextDelay( 10 );

		filePicker.onFileTextUpdated( "bad" );
		Thread.sleep( 25 );
		filePicker.onFileTextUpdated( "good" );
		Thread.sleep( 25 );

		assertThat( filePicker.textField.getStyle(), not( containsString( "red" ) ) );
	}

	@Test
	public void textTurnsBlackAndFieldsAreUpdatedAfterUserTypesBadFileNameThenChangesIsRelativePathProperty() throws Exception
	{
		filePicker.setIsRelativePath( false );

		File happyFile = mock( File.class );
		when( happyFile.getAbsoluteFile() ).thenReturn( happyFile );
		when( happyFile.exists() ).thenReturn( true );
		when( happyFile.isFile() ).thenReturn( true );
		when( happyFile.toPath() ).thenReturn( new File( baseDirForRelativePaths.getAbsoluteFile(), "good" ).toPath() );

		File badFile = mock( File.class );
		when( badFile.exists() ).thenReturn( false );

		SoapUiFilePicker.FileResolver mockResolver = mock( SoapUiFilePicker.FileResolver.class );
		when( mockResolver.resolveFromText( false, baseDirForRelativePaths, "good" ) ).thenReturn( happyFile );
		when( mockResolver.resolveFromText( false, baseDirForRelativePaths, "bad" ) ).thenReturn( badFile );
		when( mockResolver.resolveFromText( true, baseDirForRelativePaths, "bad" ) ).thenReturn( badFile );
		when( mockResolver.abs2rel( any( File.class ), any( File.class ) ) ).thenCallRealMethod();

		filePicker.fileResolver = mockResolver;

		filePicker.setUpdateTextDelay( 10 );
		filePicker.onFileTextUpdated( "good" );
		Thread.sleep( 25 );

		assertThat( filePicker.textField.getStyle(), not( containsString( "red" ) ) );

		filePicker.onFileTextUpdated( "bad" );

		Thread.sleep( 25 );

		assertThat( filePicker.textField.getStyle(), containsString( "red" ) );

		filePicker.setIsRelativePath( true );

		assertThat( filePicker.textLabel.getText(), equalTo( baseDirForRelativePaths.getAbsolutePath() + File.separator ) );
		assertThat( filePicker.textField.getText(), equalTo( "good" ) );
		assertThat( filePicker.textField.getStyle(), not( containsString( "red" ) ) );
	}


}
