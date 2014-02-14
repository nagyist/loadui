package com.eviware.loadui.util.groovy.resolvers;

import com.eviware.loadui.util.groovy.GroovyResolver;
import com.google.common.base.Preconditions;
import groovy.lang.Closure;
import groovy.lang.MissingMethodException;
import groovy.util.ObjectGraphBuilder;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class JavaFxResolver implements GroovyResolver.Methods
{
	static final Logger log = LoggerFactory.getLogger( JavaFxResolver.class );

	private final ObjectGraphBuilder builder = new ObjectGraphBuilder();

	private final Map<String, List<String>> classesByPackage = new HashMap<>();

	public JavaFxResolver()
	{
		builder.setClassLoader( getClass().getClassLoader() );

		// TODO add support for more JavaFX stuff - only added what was necessary for current components to run
		classesByPackage.put( "javafx.scene.control", asList( "tableView", "tableColumn" ) );
	}

	@Nullable
	@Override
	public Object invokeMethod( @Nonnull String methodName, Object... args ) throws MissingMethodException
	{
		Preconditions.checkNotNull( methodName );

		try
		{
			synchronized( builder )
			{
				for( String pkg : classesByPackage.keySet() )
				{
					if( classesByPackage.get( pkg ).contains( methodName ) )
					{
						Preconditions.checkArgument( args.length >= 1 );
						builder.setClassNameResolver( pkg );
						return builder.invokeMethod( methodName, args[0] );
					}
				}
				if( methodName.equals( "fileChooser" ) )
				{
					Preconditions.checkArgument( args.length >= 1 );
					boolean hasFileChooser = args.length >= 2;
					builder.setClassNameResolver( FileChooser.class.getPackage().getName() );
					FileChooser chooser = ( FileChooser )builder.invokeMethod( methodName, args[0] );
					if( hasFileChooser )
					{
						List<String> extFilterArgs = ( List<String> )args[1];
						chooser.getExtensionFilters().add( new FileChooser.ExtensionFilter(
								extFilterArgs.get( 0 ), extFilterArgs.get( 1 ) ) );
					}
					return chooser;
				}
				if( methodName.equals( "javaFxCallback" ) )
				{
					Preconditions.checkArgument( args.length >= 1 );
					final Closure<?> closure = ( Closure<?> )args[0];
					return new Callback<TableColumn.CellDataFeatures<Object, Object>, ObservableValue<Object>>()
					{
						@Override
						public ObservableValue call( TableColumn.CellDataFeatures<Object, Object> val )
						{
							return ( ObservableValue )closure.call( val );
						}
					};
				}
				if( methodName.equals( "observableValue" ) )
				{
					Preconditions.checkArgument( args.length >= 1 );
					final Closure<?> closure = ( Closure<?> )args[0];
					return new SimpleObjectProperty<Object>()
					{
						@Override
						public Object getValue()
						{
							return closure.call();
						}
					};
				}
				if( methodName.equals( "changeListener" ) )
				{
					Preconditions.checkArgument( args.length >= 1 );
					final Closure<?> closure = ( Closure<?> )args[0];
					return new ChangeListener<Object>()
					{

						@Override
						public void changed( ObservableValue<?> observableValue, Object old, Object current )
						{
							closure.call( observableValue, old, current );
						}
					};
				}
				if( methodName.equals( "inJavaFxThread" ) )
				{
					Preconditions.checkArgument( args.length >= 1 );
					final Closure<?> closure = ( Closure<?> )args[0];
					Platform.runLater( closure );
					return null;
				}
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		throw new MissingMethodException( methodName, JavaFxResolver.class, args );
	}

}
