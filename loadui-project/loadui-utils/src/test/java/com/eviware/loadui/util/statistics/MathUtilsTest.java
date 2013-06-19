package com.eviware.loadui.util.statistics;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.Collection;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

/**
 * @Author Henrik
 */
public class MathUtilsTest
{
	public Function<Integer, Double> squared = new Function<Integer, Double>()
	{
		@Nullable
		@Override
		public Double apply( @Nullable Integer input )
		{
			return Math.pow(input, 2);
		}
	};

	@Test
	public void standardDeviationTest()
	{
		Collection<Integer> sampleSet = ImmutableList.of(98, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2);

		int numberOfValues = sampleSet.size();
		long sumOfSquares = sum(Iterables.transform(sampleSet, squared));
		long sumOfValues = sum(sampleSet);

		double result = MathUtils.calculateStandardDeviation(numberOfValues, sumOfValues, sumOfSquares);

		assertThat(result, is(closeTo(29.0381585, 0.05)));
	}

	private long sum( Iterable<? extends Number> iterable )
	{
		long sum = 0;
		for( Number value : iterable )
		{
			sum += value.longValue();
		}
		return sum;
	}
}
