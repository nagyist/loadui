package com.eviware.loadui.groovy;

import com.eviware.loadui.api.component.ComponentContext;
import com.eviware.loadui.impl.component.categories.BaseCategory;
import com.eviware.loadui.util.groovy.GroovyResolver;
import com.eviware.loadui.util.groovy.resolvers.JavaFxResolver;

import java.util.Arrays;

public class JavaFxBehaviorSupport extends GroovyBehaviorSupport
{
	public JavaFxBehaviorSupport( GroovyBehaviorProvider behaviorProvider, BaseCategory behavior, ComponentContext context )
	{
		super( behaviorProvider, behavior, context );
	}

	@Override
	protected GroovyResolver[] provideGroovyResolvers()
	{
		GroovyResolver[] superResolvers = super.provideGroovyResolvers();
		GroovyResolver[] allResolvers = Arrays.copyOf( superResolvers, superResolvers.length + 1, GroovyResolver[].class );
		allResolvers[allResolvers.length - 1] = new JavaFxResolver();
		return allResolvers;
	}
}
