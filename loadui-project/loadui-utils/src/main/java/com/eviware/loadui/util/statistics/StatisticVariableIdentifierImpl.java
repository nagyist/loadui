package com.eviware.loadui.util.statistics;

import com.eviware.loadui.api.statistics.StatisticVariableIdentifier;
import org.apache.commons.codec.digest.DigestUtils;

public class StatisticVariableIdentifierImpl implements StatisticVariableIdentifier
{

	private final String statisticHolderId;
	private final String variableLabel;
	private final String variableType;
	private final String hash;

	public StatisticVariableIdentifierImpl( String statisticHolderId, String variableLabel, String variableType )
	{
		this.statisticHolderId = statisticHolderId;
		this.variableLabel = variableLabel;
		this.variableType = variableType;
		this.hash = DigestUtils.md5Hex( statisticHolderId + variableLabel + variableType );
	}

	@Override
	public String getStatisticHolderId()
	{
		return statisticHolderId;
	}

	@Override
	public String getVariableLabel()
	{
		return variableLabel;
	}

	@Override
	public String getVariableType()
	{
		return variableType;
	}

	@Override
	public String getHash()
	{
		return hash;
	}

}


