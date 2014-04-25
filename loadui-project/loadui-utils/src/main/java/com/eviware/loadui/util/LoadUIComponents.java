package com.eviware.loadui.util;

/**
 * LoadUI known components
 */
public enum LoadUIComponents
{
	FIXED_RATE( "Fixed Rate" ),
	HTTP_RUNNER( "HTTP Runner" ),
	WEB_RUNNER( "Web Runner PREVIEW" ),
	REST_RUNNER( "REST Runner" ),
	DEJA_RUNNER( "DejaClick Runner" ),
	SOAPUI_RUNNER( "SoapUI Runner" ),
	FIXED_LOAD( "Fixed Load" ),
	RAMP_LOAD( "Ramp Load" ),
	RAMP( "Ramp" ),
	RAMP_SEQUENCE( "Ramp Sequence" ),
	TABLE_LOG( "Table Log" );

	private final String name;

	LoadUIComponents( String name )
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public String cssName()
	{
		return StringUtils.toCssName( name );
	}

	public String cssClass()
	{
		return "." + this.cssName();
	}

	public String defaultComponentLabel()
	{
		return defaultComponentLabel( 1 );
	}
	public String defaultComponentLabel(int number)
	{
		return name + " " + number;
	}


}
