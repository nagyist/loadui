package com.eviware.loadui.util.projects;

import com.eviware.loadui.api.traits.ValidatableObject;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

/**
 * Created by osten on 1/23/14.
 */
public class LoadRecipeParser
{

	private Gson gson;

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
	public static class Body implements ValidatableObject
	{
		private LoadRecipe loadRecipe;

		public LoadRecipe getLoadRecipe()
		{
			return loadRecipe;
		}

		@Override
		public boolean isValid()
		{
			return loadRecipe.isValid();
		}
	}

	@SuppressWarnings("unused")
	public static class LoadRecipe implements ValidatableObject
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
				Preconditions.checkNotNull( limits, "Required JSON object limits cannot be found." );
				Preconditions.checkNotNull( scenarios, "Required JSON object scenarios cannot be found. " );

				for( Limit limit : limits )
				{
					Preconditions.checkState( limit.isValid() );
				}

				for( Scenario scenario : scenarios )
				{
					Preconditions.checkState( scenario.isValid() );
				}
			}
			catch( Exception e )
			{
				return false;
			}
			return true;
		}
	}

	@SuppressWarnings( "unused" )
	public static class Limit implements ValidatableObject
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
				Preconditions.checkNotNull( type, "Required JSON object type cannot be found." );
				Preconditions.checkNotNull( maxValue, "Required JSON object maxValue cannot be found." );
				Preconditions.checkArgument( maxValue > 0, "Required JSON object maxValue is not a positive number." );
			}
			catch( Exception e )
			{
				System.err.println( e.getMessage() );
				return false;
			}
			return true;
		}
	}

	@SuppressWarnings( "unused" )
	public static class Scenario implements ValidatableObject
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
				Preconditions.checkNotNull( runner, "Required JSON object runner cannot be found." );
				Preconditions.checkNotNull( loadProfile, "Required JSON object runner cannot be found." );
				Preconditions.checkState( loadProfile.isValid() );
			}
			catch( Exception e )
			{
				System.err.println( e.getMessage() );
				return false;
			}

			boolean isValid = true;
			if( properties != null && properties.length > 0 )
			{
				for( Property p : properties )
				{
					if( !p.isValid() )
					{
						isValid = false;
					}
				}
			}

			return isValid;
		}
	}

	@SuppressWarnings( "unused" )
	public static class Property implements ValidatableObject
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
				Preconditions.checkNotNull( key, "Required JSON object key cannot be found." );
				Preconditions.checkNotNull( value, "Required JSON object value cannot be found." );
				Preconditions.checkNotNull( type, "Required JSON object type cannot be found" );
			}
			catch( Exception e )
			{
				System.err.println( e.getMessage() );
				return false;
			}
			return true;
		}
	}

	@SuppressWarnings( "unused" )
	public static class LoadProfile implements ValidatableObject
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
				Preconditions.checkNotNull( type, "Required JSON object type cannot be found" );
				Preconditions.checkNotNull( keyFrames, "Required JSON object keyFrames cannot be found." );
				Preconditions.checkArgument( keyFrames.length > 0, "Required minimum length of JSON object keyFrames is 1, found " + keyFrames.length + ".");

				for( KeyFrame k : keyFrames){
					Preconditions.checkState( k.isValid() );
				}
			}
			catch( Exception e )
			{
				System.err.println( e.getMessage() );
				return false;
			}
			return true;
		}
	}


	@SuppressWarnings( "unused" )
	private class KeyFrame implements ValidatableObject
	{
		private int time;
		private int rate;

		public int getRate()
		{
			return rate;
		}

		public int getTime()
		{
			return time;
		}

		@Override
		public boolean isValid()
		{
			try{
				Preconditions.checkNotNull( time, "Required JSON object rate cannot be found." );
				Preconditions.checkNotNull( rate, "Required JSON object rate cannot be found." );
			}catch(Exception e){
				System.err.println( e.getMessage() );
				return false;
			}
			return true;
		}
	}
}


