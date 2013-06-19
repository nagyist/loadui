package com.eviware.loadui.util.statistics;

/**
 * @Author Henrik
 */
public class MathUtils
{
	public static double calculateStandardDeviation( long numberOfValues, long sumOfValues, long sumOfSquares )
	{
		// using formula from http://stackoverflow.com/a/10365293/481583
		return Math.sqrt(1.0 / (numberOfValues - 1) * (sumOfSquares - 1.0 / numberOfValues * Math.pow(sumOfValues, 2)));
	}
}
