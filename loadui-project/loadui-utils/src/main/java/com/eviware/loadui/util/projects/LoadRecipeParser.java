package com.eviware.loadui.util.projects;

import com.eviware.loadui.api.traits.Validatable;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadRecipeParser
{

	private Gson gson;

	private static Logger log = LoggerFactory.getLogger( LoadRecipeParser.class );

	public Body parse( String json )
	{
		gson = new Gson();
		Body messageBody = gson.fromJson( json, Body.class );
		return messageBody;
	}

	public Body parse( JsonReader reader )
	{
		gson = new Gson();
		Body messageBody = gson.fromJson( reader, Body.class );
		return messageBody;
	}

	@SuppressWarnings("unused")
	public static class Body implements Validatable
	{
		private LoadRecipe loadRecipe;

		public LoadRecipe getLoadRecipe()
		{
			return loadRecipe;
		}

		@Override
		public boolean isValid()
		{
			return validate( loadRecipe );
		}
	}

	@SuppressWarnings("unused")
	public static class LoadRecipe implements Validatable
	{
		private Limit[] limits;
		private Scenario[] scenarios;

		public Scenario[] getScenarios()
		{
			return scenarios;
		}

		public Limit[] getLimits()
		{
			return limits;
		}

		@Override
		public boolean isValid()
		{
			try
			{
				Preconditions.checkNotNull( limits, missingJsonElementErrorMessage( "limits" ) );
				Preconditions.checkNotNull( scenarios, missingJsonElementErrorMessage( "scenarios" ) );
			}
			catch( NullPointerException e )
			{
				log.error( e.getMessage() );
				return false;
			}
			return validate( limits ) && validate( scenarios );
		}
	}

	@SuppressWarnings( "unused" )
	public static class Limit implements Validatable
	{
		private String type;
		private Integer maxValue;

		public Number getMaxValue()
		{
			return maxValue;
		}

		public String getType()
		{
			return type;
		}

		@Override
		public boolean isValid()
		{
			try
			{
				Preconditions.checkNotNull( type, missingJsonElementErrorMessage( "type" ) );
				Preconditions.checkNotNull( maxValue, missingJsonElementErrorMessage( "maxValue" ) );
				Preconditions.checkArgument( maxValue > 0, "Required JSON object maxValue is not a positive number." );
			}
			catch( IllegalArgumentException | NullPointerException e )
			{
				log.error( e.getMessage() );
				return false;
			}
			return true;
		}
	}

	@SuppressWarnings( "unused" )
	public static class Scenario implements Validatable
	{
		private String label;
		private String runner;
		private LoadProfile loadProfile;
		private Property[] properties;

		public Property[] getProperties()
		{
			return properties;
		}

		public LoadProfile getLoadProfile()
		{
			return loadProfile;
		}

		public String getRunner()
		{
			return runner;
		}

		public String getLabel()
		{
			return label;
		}

		@Override
		public boolean isValid()
		{
			try
			{
				Preconditions.checkNotNull( runner, missingJsonElementErrorMessage( "runner" ) );
				Preconditions.checkNotNull( loadProfile, missingJsonElementErrorMessage( "loadProfile" ) );

			}
			catch( NullPointerException e )
			{
				log.error( e.getMessage() );
				return false;
			}

			if( properties != null && properties.length > 0 )
			{
				return validate( properties );
			}
			return validate( loadProfile );
		}
	}

	@SuppressWarnings( "unused" )
	public static class Property implements Validatable
	{
		private String key;
		private Object value;
		private String type;

		public String getType()
		{
			return type;
		}

		public Object getValue()
		{
			return value;
		}

		public String getKey()
		{
			return key;
		}

		@Override
		public boolean isValid()
		{
			try
			{
				Preconditions.checkNotNull( key, missingJsonElementErrorMessage( "key" ) );
				Preconditions.checkNotNull( value, missingJsonElementErrorMessage( "value" ) );
				Preconditions.checkNotNull( type, missingJsonElementErrorMessage("type") );
			}
			catch( NullPointerException e )
			{
				log.error( e.getMessage() );
				return false;
			}
			return true;
		}
	}

	@SuppressWarnings( "unused" )
	public static class LoadProfile implements Validatable
	{
		private String type;
		private KeyFrame[] keyFrames;

		public KeyFrame[] getKeyFrames()
		{
			return keyFrames;
		}

		public String getType()
		{
			return type;
		}

		@Override
		public boolean isValid()
		{
			try
			{
				Preconditions.checkNotNull( type, missingJsonElementErrorMessage("type") );
				Preconditions.checkNotNull( keyFrames, missingJsonElementErrorMessage( "keyFrames" ) );
				Preconditions.checkArgument( keyFrames.length > 0, "Required minimum length of JSON object keyFrames is 1, found " + keyFrames.length + "." );

				return validate( keyFrames );
			}
			catch( NullPointerException | IllegalArgumentException e )
			{
				log.error( e.getMessage() );
				return false;
			}
		}
	}

	@SuppressWarnings( "unused" )
	private class KeyFrame implements Validatable
	{
		private Integer time;
		private Integer value;

		public Integer getRate()
		{
			return value;
		}

		public Integer getTime()
		{
			return time;
		}

		@Override
		public boolean isValid()
		{
			try
			{
				Preconditions.checkNotNull( time, missingJsonElementErrorMessage( "time" ) );
				Preconditions.checkNotNull( value, missingJsonElementErrorMessage( "value" ));
			}
			catch( NullPointerException e )
			{
				log.error( e.getMessage() );				return false;
			}
			return true;
		}
	}

	private static boolean validate( Validatable[] validatables )
	{
		if( validatables == null )
		{
			return false;
		}
		for( Validatable v : validatables )
		{
			if( !v.isValid() )
				return false;
		}
		return true;
	}

	private static boolean validate( Validatable validatable )
	{
		if( validatable == null )
		{
			return false;
		}

		if( !validatable.isValid() )
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	
	private static String missingJsonElementErrorMessage( String concernedElement ){
		return "Required JSON object " + concernedElement + " cannot be found";
	}
}


