package com.eviware.loadui.test;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * TestStates are used for integration testing where it is undesirable to
 * completely setup and tear down the environment from scratch for each test.
 * Instead, TestStates can serve as good known states to use as a starting point
 * for tests. TestStates are arranged in a tree structure, with each state
 * holding a pointer to its parent.
 * 
 * @author dain.nilsson
 */
public abstract class TestState
{
	private static final Joiner BREADCRUMB_JOINER = Joiner.on( " > " );

	private static final Logger log = LoggerFactory.getLogger( TestState.class );

	/**
	 * The initial root state, which forms the root of the TestState tree.
	 */
	public static final TestState ROOT = new TestState()
	{
		@Override
		protected void enterFromParent()
		{
			throw new UnsupportedOperationException( "ROOT node has no parent!" );
		}

		@Override
		protected void exitToParent()
		{
			throw new UnsupportedOperationException( "ROOT node has no parent!" );
		}
	};

	private static TestState currentState = ROOT;

	private final String name;
	private final TestState parent;

	private TestState()
	{
		name = "Root";
		parent = null;
	}

	protected TestState( String name, TestState parent )
	{
		this.name = Preconditions.checkNotNull( name );
		this.parent = Preconditions.checkNotNull( parent );
	}

	public final String getName()
	{
		return name;
	}

	public final TestState getParent()
	{
		return parent;
	}

	/**
	 * Transitions into the TestState, regardless of previous state.
	 * 
	 * @throws Exception
	 */
	public final void enter()
	{
		transitionTo( this );
	}

	@Override
	public String toString()
	{
		return name;
	}

	protected abstract void enterFromParent() throws Exception;

	protected abstract void exitToParent() throws Exception;

	private static void transitionTo( TestState newState )
	{
		while( currentState != newState )
		{
			if( !getParents( newState ).contains( currentState ) )
			{
				log.info( "Exiting state: {}", getBreadcrumbs( currentState ) );
				try
				{
					currentState.exitToParent();
				}
				catch( Exception e )
				{
					throw new RuntimeException( e );
				}
				currentState = currentState.getParent();
			}
			else
			{
				TestState next = newState;
				while( next.getParent() != currentState )
				{
					next = next.getParent();
				}
				log.info( "Entering state: {}", getBreadcrumbs( next ) );
				try
				{
					next.enterFromParent();
				}
				catch( Exception e )
				{
					throw new RuntimeException( e );
				}
				currentState = next;
			}
		}
	}

	private static List<TestState> getParents( TestState state )
	{
		List<TestState> parents = Lists.newArrayList();
		while( state.getParent() != null )
		{
			state = state.getParent();
			parents.add( state );
		}

		return parents;
	}

	private static String getBreadcrumbs( TestState state )
	{
		List<TestState> states = Lists.newArrayList( Lists.reverse( getParents( state ) ) );
		states.add( state );

		return BREADCRUMB_JOINER.join( states );
	}
}
