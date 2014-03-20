package com.eviware.loadui.webdata;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;

public class StreamConsumer
{

	public byte[] consume( InputStream is ) throws IOException
	{
		return ByteStreams.toByteArray( is );
	}

}
