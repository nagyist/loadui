package com.eviware.loadui.components.soapui.layout;

import com.eviware.loadui.api.ui.dialog.FilePickerDialogFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author renato
 */
public class SoapUiFilePickerTest
{

	SoapUiFilePicker filePicker;

	@Before
	public void setup()
	{
		File selectedFile = new File( "abc" );
		File baseDirForRelativePaths = new File( "base" );
		FilePickerDialogFactory factory = mock( FilePickerDialogFactory.class );

		when( factory.showOpenDialog( anyString(), anyString(), anyString() ) )
				.thenReturn( selectedFile );

		filePicker = new SoapUiFilePicker( "Picker Title", ".xml", "*.xml",
				factory, baseDirForRelativePaths );
	}

	@Test
	public void canFigureOutWhereToStartARelativePath_FileUnderBaseDir()
	{

		filePicker.setIsRelativePath( true );
		filePicker.setSelected( new File( "base", "selected.xml" ) );

		assertThat( filePicker.textLabel.getText(), equalTo( new File( "base" ).getAbsolutePath() + File.separator ) );
		assertThat( filePicker.textField.getText(), equalTo( "selected.xml" ) );
	}

	@Ignore( "under construction" )
	@Test
	public void canFigureOutWhereToStartARelativePath_FileNotUnderBaseDir()
	{

		filePicker.setIsRelativePath( true );
		filePicker.setSelected( new File( "another", "selected.xml" ) );

		assertThat( filePicker.textLabel.getText(), equalTo( new File( "base" ).getAbsolutePath() + File.separator ) );
		assertThat( filePicker.textField.getText(), equalTo( "selected.xml" ) );
	}

	@Test
	public void canFigureOutAbsolutePath()
	{
		filePicker.setIsRelativePath( false );
		filePicker.setSelected( new File( "selected.xml" ) );

		assertThat( filePicker.textLabel.getText(), equalTo( "" ) );
		assertThat( filePicker.textField.getText(), equalTo( new File( "selected.xml" ).getAbsolutePath() ) );
	}

	@Test
	public void canTurnAnAbsolutePathIntoARelativePath()
	{
		filePicker.setIsRelativePath( false );
		filePicker.setSelected( new File( "base", "selected.xml" ) );
		filePicker.setIsRelativePath( true );

		assertThat( filePicker.textLabel.getText(), equalTo( new File( "base" ).getAbsolutePath() + File.separator ) );
		assertThat( filePicker.textField.getText(), equalTo( "selected.xml" ) );
	}

	@Test
	public void canTurnARelativePathIntoAnAbsolutePath()
	{
		filePicker.setIsRelativePath( true );
		filePicker.setSelected( new File( "base", "selected.xml" ) );
		filePicker.setIsRelativePath( false );

		assertThat( filePicker.textLabel.getText(), equalTo( "" ) );
		assertThat( filePicker.textField.getText(), equalTo( new File( "base", "selected.xml" ).getAbsolutePath() ) );
	}

	@Test
	public void whenSettingFileToNotUnderProjectFileDirPathMustBeRelative()
	{
		filePicker.setIsRelativePath( false );
		filePicker.setSelected( new File( "base", "selected.xml" ) );
		filePicker.setIsRelativePath( true );

		assertThat( filePicker.textLabel.getText(), equalTo( new File( "base" ).getAbsolutePath() + File.separator ) );
		assertThat( filePicker.textField.getText(), equalTo( "selected.xml" ) );
	}

	@Test
	public void fieldsAreUpdatedAfterSomeTimeAfterUserStopsTyping_AndTextChangesColorsIfFileExistsOrNot() throws Exception
	{
		File oldFile = new File( "old" );
		filePicker.setSelected( oldFile );

		File baseDir = new File( "base" );
		File happyFile = mock( File.class );
		when( happyFile.exists() ).thenReturn( true );
		when( happyFile.isFile() ).thenReturn( true );

		File badFile = mock( File.class );
		when( badFile.exists() ).thenReturn( false );

		SoapUiFilePicker.FileResolver mockResolver = mock( SoapUiFilePicker.FileResolver.class );
		when( mockResolver.resolveFromText( false, baseDir, "a" ) ).thenReturn( badFile );
		when( mockResolver.resolveFromText( false, baseDir, "ab" ) ).thenReturn( happyFile );
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

		File baseDir = new File( "base" );
		File badFile = mock( File.class );
		when( badFile.exists() ).thenReturn( false );

		SoapUiFilePicker.FileResolver mockResolver = mock( SoapUiFilePicker.FileResolver.class );
		when( mockResolver.resolveFromText( false, baseDir, "bad" ) ).thenReturn( badFile );
		filePicker.fileResolver = mockResolver;

		filePicker.setUpdateTextDelay( 10 );

		filePicker.onFileTextUpdated( "bad" );

		Thread.sleep( 25 );

		assertThat( filePicker.textField.getStyle(), containsString( "red" ) );
	}

	@Test
	public void textTurnsBlackWhenUserTypesFileNameThatExists() throws Exception
	{
		File baseDir = new File( "base" );
		filePicker.setIsRelativePath( false );

		File happyFile = mock( File.class );
		when( happyFile.exists() ).thenReturn( true );
		when( happyFile.isFile() ).thenReturn( true );

		File badFile = mock( File.class );
		when( badFile.exists() ).thenReturn( false );

		SoapUiFilePicker.FileResolver mockResolver = mock( SoapUiFilePicker.FileResolver.class );
		when( mockResolver.resolveFromText( false, baseDir, "bad" ) ).thenReturn( badFile );
		when( mockResolver.resolveFromText( false, baseDir, "good" ) ).thenReturn( happyFile );
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
		File baseDir = new File( "base" );
		filePicker.setIsRelativePath( false );

		File happyFile = mock( File.class );
		when( happyFile.exists() ).thenReturn( true );
		when( happyFile.isFile() ).thenReturn( true );
		when( happyFile.getAbsolutePath() ).thenReturn( new File( baseDir, "good" ).getAbsolutePath() );

		File badFile = mock( File.class );
		when( badFile.exists() ).thenReturn( false );

		SoapUiFilePicker.FileResolver mockResolver = mock( SoapUiFilePicker.FileResolver.class );
		when( mockResolver.resolveFromText( false, baseDir, "bad" ) ).thenReturn( badFile );
		when( mockResolver.resolveFromText( false, baseDir, "good" ) ).thenReturn( happyFile );
		filePicker.fileResolver = mockResolver;

		filePicker.setUpdateTextDelay( 10 );

		filePicker.onFileTextUpdated( "good" );
		Thread.sleep( 25 );
		assertThat( filePicker.textField.getStyle(), not( containsString( "red" ) ) );

		filePicker.onFileTextUpdated( "bad" );

		Thread.sleep( 25 );

		assertThat( filePicker.textField.getStyle(), containsString( "red" ) );

		filePicker.setIsRelativePath( true );

		assertThat( filePicker.textLabel.getText(), equalTo( new File( "base" ).getAbsolutePath() + File.separator ) );
		assertThat( filePicker.textField.getText(), equalTo( "good" ) );
		assertThat( filePicker.textField.getStyle(), not( containsString( "red" ) ) );
	}


}
