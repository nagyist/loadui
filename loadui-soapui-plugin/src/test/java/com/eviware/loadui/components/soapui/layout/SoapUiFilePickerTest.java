package com.eviware.loadui.components.soapui.layout;

import com.eviware.loadui.api.ui.dialog.FilePickerDialogFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static com.eviware.loadui.components.soapui.layout.FileResolverTest.*;
import static com.eviware.loadui.components.soapui.layout.SoapUiFilePicker.INVALID_CSS_CLASS;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

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
				factory, baseDirForRelativePaths, null, null );
	}

	@Test
	public void canFigureOutWhereToStartARelativePath_FileUnderBaseDir()
	{
		File selectedFile = new File( baseDirForRelativePaths, "selected.xml" );

		SoapUiFilePicker.FileResolver resolver = spy( filePicker.fileResolver );
		when( resolver.isAcceptable( selectedFile ) ).thenReturn( true );
		filePicker.fileResolver = resolver;

		filePicker.setIsRelativePath( true );
		filePicker.setAbsolutePath( selectedFile.getAbsolutePath() );

		assertThat( filePicker.textLabel.getText(), equalTo( baseDirForRelativePaths.getAbsolutePath() + File.separator ) );
		assertThat( filePicker.textField.getText(), equalTo( "selected.xml" ) );
		assertThat( filePicker.selectedProperty().get(), equalTo( "selected.xml" ) );
	}

	@Test
	public void canFigureOutWhereToStartARelativePath_FileNotUnderBaseDir()
	{
		final String selected = relPath( "..", "another", "selected.xml" );
		File selectedFile = new File( baseDirForRelativePaths, selected );

		SoapUiFilePicker.FileResolver resolver = spy( filePicker.fileResolver );
		when( resolver.isAcceptable( selectedFile ) ).thenReturn( true );
		filePicker.fileResolver = resolver;

		filePicker.setIsRelativePath( true );
		filePicker.setAbsolutePath( selectedFile.getAbsolutePath() );

		assertThat( filePicker.textLabel.getText(),
				equalTo( baseDirForRelativePaths.getAbsolutePath() + File.separator ) );
		assertThat( filePicker.textField.getText(),
				equalTo( selected ) );
		assertThat( filePicker.selectedProperty().get(), equalTo( selected ) );
	}

	@Test
	public void canFigureOutAbsolutePath()
	{
		String selected = absPath( "mydir", "selected.xml" );

		SoapUiFilePicker.FileResolver resolver = spy( filePicker.fileResolver );
		when( resolver.isAcceptable( new File( selected ) ) ).thenReturn( true );
		filePicker.fileResolver = resolver;

		filePicker.setIsRelativePath( false );
		filePicker.setAbsolutePath( selected );

		assertThat( filePicker.textLabel.getText(), equalTo( "" ) );
		assertThat( filePicker.textField.getText(), equalTo( selected ) );
		assertThat( filePicker.selectedProperty().get(), equalTo( selected ) );
	}

	@Test
	public void canTurnAnAbsolutePathIntoARelativePath()
	{
		String selected = new File( baseDirForRelativePaths, "selected.xml" ).getAbsolutePath();

		SoapUiFilePicker.FileResolver resolver = spy( filePicker.fileResolver );
		when( resolver.isAcceptable( new File( baseDirForRelativePaths, "selected.xml" ) ) )
				.thenReturn( true );
		filePicker.fileResolver = resolver;

		filePicker.setIsRelativePath( false );
		filePicker.setAbsolutePath( selected );
		filePicker.setIsRelativePath( true );

		assertThat( filePicker.textLabel.getText(), equalTo( baseDirForRelativePaths.getAbsolutePath() + File.separator ) );
		assertThat( filePicker.textField.getText(), equalTo( "selected.xml" ) );
		assertThat( filePicker.selectedProperty().get(), equalTo( "selected.xml" ) );
	}

	@Test
	public void canTurnARelativePathIntoAnAbsolutePath()
	{
		String selected = relPath( "mydir", "selected.xml" );
		File expectedAbsFile = new File( baseDirForRelativePaths, selected );

		SoapUiFilePicker.FileResolver resolver = spy( filePicker.fileResolver );
		when( resolver.isAcceptable( expectedAbsFile ) ).thenReturn( true );
		filePicker.fileResolver = resolver;

		filePicker.setIsRelativePath( true );
		filePicker.setAbsolutePath( new File( baseDirForRelativePaths, selected ).getAbsolutePath() );
		filePicker.setIsRelativePath( false );

		assertThat( filePicker.textLabel.getText(), equalTo( "" ) );
		assertThat( filePicker.textField.getText(), equalTo( expectedAbsFile.getAbsolutePath() ) );
		assertThat( filePicker.selectedProperty().get(), equalTo( expectedAbsFile.getAbsolutePath() ) );
	}

	@Test
	public void afterTurningRelativePathToAbsolutePathCanChangeTextToUpdateFile() throws Exception
	{
		filePicker.setIsRelativePath( false );

		File firstSelected = new File( baseDirForRelativePaths, "first.xml" );
		File secondSelected = new File( baseDirForRelativePaths, relPath( "other", "second.xml" ) );

		SoapUiFilePicker.FileResolver resolver = spy( filePicker.fileResolver );
		when( resolver.isAcceptable( firstSelected ) ).thenReturn( true );
		when( resolver.isAcceptable( secondSelected ) ).thenReturn( true );
		filePicker.fileResolver = resolver;

		filePicker.setUpdateTextDelay( 10 );

		filePicker.setIsRelativePath( true );
		filePicker.setAbsolutePath( firstSelected.getAbsolutePath() );
		filePicker.setIsRelativePath( false );

		filePicker.onFileTextUpdated( secondSelected.getAbsolutePath() );

		Thread.sleep( 20 );

		assertThat( filePicker.textLabel.getText(), equalTo( "" ) );
		assertThat( filePicker.textField.getText(), equalTo( secondSelected.getAbsolutePath() ) );
		assertThat( canonicalPath( filePicker.selectedProperty().get() ),
				equalTo( canonicalPath( secondSelected.getAbsolutePath() ) ) );
	}

	@Test
	public void whenSettingFileToNotUnderProjectFileDirPathMustBeRelative()
	{
		File selected = new File( baseDirForRelativePaths, "selected.xml" );

		SoapUiFilePicker.FileResolver resolver = spy( filePicker.fileResolver );
		when( resolver.isAcceptable( selected ) ).thenReturn( true );
		filePicker.fileResolver = resolver;

		filePicker.setIsRelativePath( false );
		filePicker.setAbsolutePath( selected.getPath() );
		filePicker.setIsRelativePath( true );

		assertThat( filePicker.textLabel.getText(), equalTo( baseDirForRelativePaths.getAbsolutePath() + File.separator ) );
		assertThat( filePicker.textField.getText(), equalTo( "selected.xml" ) );
		assertThat( filePicker.selectedProperty().get(), equalTo( "selected.xml" ) );
	}

	@Ignore
	@Test
	public void fieldsAreUpdatedSomeTimeAfterUserStopsTyping_AndTextChangesColorsIfFileExistsOrNot() throws Exception
	{
		File happyFile = new File( absPath( "good" ) );
		File badFile = new File( absPath( "bad" ) );

		SoapUiFilePicker.FileResolver resolver = spy( filePicker.fileResolver );
		when( resolver.isAcceptable( new File( absPath( "old" ) ) ) ).thenReturn( true );
		when( resolver.isAcceptable( badFile ) ).thenReturn( false );
		when( resolver.isAcceptable( happyFile ) ).thenReturn( true );
		filePicker.fileResolver = resolver;

		filePicker.setAbsolutePath( absPath( "old" ) );
		filePicker.setUpdateTextDelay( 50 );
		filePicker.fileResolver = resolver;

		// actual value is not updated even after delay if new value is not good
		filePicker.onFileTextUpdated( "bad" );
		Thread.sleep( 75 );
		assertThat( filePicker.selectedProperty().get(), equalTo( absPath( "old" ) ) );
		assertThat( filePicker.textField.getStyleClass(), hasItem( "invalid" ) );

		// ... and is not updated for a while if user keeps typing
		filePicker.onFileTextUpdated( "good" );
		Thread.sleep( 30 );
		assertThat( filePicker.selectedProperty().get(), equalTo( absPath( "old" ) ) );

		// ... but after the updateTextDelay is up, the selected file is updated if it exists (happy files always exist)
		Thread.sleep( 50 );
		assertThat( filePicker.selectedProperty().get(), equalTo( absPath( "good" ) ) );
		assertThat( filePicker.textField.getStyleClass(), not( hasItem( "invalid" ) ) );
	}

	@Ignore
	@Test
	public void textTurnsRedWhenUserTypesFileNameThatDoesNotExist() throws Exception
	{
		assertThat( filePicker.textField.getStyleClass(), not( hasItem( "invalid" ) ) );

		File badFile = mock( File.class );
		SoapUiFilePicker.FileResolver mockResolver = mock( SoapUiFilePicker.FileResolver.class );
		when( badFile.exists() ).thenReturn( false );
		when( mockResolver.resolveFromText( false, baseDirForRelativePaths, "bad" ) ).thenReturn( badFile );
		filePicker.fileResolver = mockResolver;

		filePicker.setUpdateTextDelay( 10 );

		filePicker.onFileTextUpdated( "bad" );

		Thread.sleep( 25 );

		assertThat( filePicker.textField.getStyleClass(), hasItem( "invalid" ) );
	}

	@Test
	public void textTurnsBlackWhenUserTypesFileNameThatExists() throws Exception
	{
		SoapUiFilePicker.FileResolver resolver = spy( filePicker.fileResolver );
		when( resolver.isAcceptable( new File( absPath( "bad" ) ) ) ).thenReturn( false );
		when( resolver.isAcceptable( new File( absPath( "good" ) ) ) ).thenReturn( true );
		filePicker.fileResolver = resolver;

		filePicker.setIsRelativePath( false );
		filePicker.setUpdateTextDelay( 10 );

		filePicker.onFileTextUpdated( "bad" );
		Thread.sleep( 25 );
		filePicker.onFileTextUpdated( "good" );
		Thread.sleep( 25 );

		assertThat( filePicker.textField.getStyleClass(), not( hasItem( "invalid" ) ) );
	}

	@Ignore
	@Test
	public void textTurnsBlackAndFieldsAreUpdatedAfterUserTypesBadFileNameThenChangesIsRelativePathProperty() throws Exception
	{
		filePicker.setIsRelativePath( false );

		File happyFile = new File( baseDirForRelativePaths, "good" );
		File badFile = new File( baseDirForRelativePaths, "bad" );

		SoapUiFilePicker.FileResolver resolver = spy( filePicker.fileResolver );
		when( resolver.isAcceptable( happyFile ) ).thenReturn( true );
		when( resolver.isAcceptable( badFile ) ).thenReturn( false );
		filePicker.fileResolver = resolver;

		filePicker.setUpdateTextDelay( 10 );
		filePicker.onFileTextUpdated( happyFile.getAbsolutePath() );
		Thread.sleep( 25 );

		assertThat( filePicker.textField.getStyleClass(), not( hasItem( "invalid" ) ) );

		filePicker.onFileTextUpdated( badFile.getAbsolutePath() );

		Thread.sleep( 25 );

		assertThat( filePicker.textField.getStyleClass(), hasItem( "invalid" ) );

		filePicker.setIsRelativePath( true );

		assertThat( filePicker.textLabel.getText(), equalTo( baseDirForRelativePaths.getAbsolutePath() + File.separator ) );
		assertThat( filePicker.textField.getText(), equalTo( "good" ) );
		assertThat( filePicker.textField.getStyleClass(), not( hasItem( INVALID_CSS_CLASS ) ) );
	}


}
