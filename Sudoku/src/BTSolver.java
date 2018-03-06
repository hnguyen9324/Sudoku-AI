import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class BTSolver
{

	// =================================================================
	// Properties
	// =================================================================

	private ConstraintNetwork network;
	private SudokuBoard sudokuGrid;
	private Trail trail;

	private boolean hasSolution = false;

	public String varHeuristics;
	public String valHeuristics;
	public String cChecks;

	// =================================================================
	// Constructors
	// =================================================================

	public BTSolver ( SudokuBoard sboard, Trail trail, String val_sh, String var_sh, String cc )
	{
		this.network    = new ConstraintNetwork( sboard );
		this.sudokuGrid = sboard;
		this.trail      = trail;

		varHeuristics = var_sh;
		valHeuristics = val_sh;
		cChecks       = cc;
	}

	// =================================================================
	// Consistency Checks
	// =================================================================

	// Basic consistency check, no propagation done
	private boolean assignmentsCheck ( )
	{
		for ( Constraint c : network.getConstraints() )
			if ( ! c.isConsistent() )
				return false;

		return true;
	}

	/**
	 * Part 1 TODO: Implement the Forward Checking Heuristic
	 *
	 * This function will do both Constraint Propagation and check
	 * the consistency of the network
	 *
	 * (1) If a variable is assigned then eliminate that value from
	 *     the square's neighbors.
	 *
	 * Note: remember to trail.push variables before you change their domain
	 * Return: true is assignment is consistent, false otherwise
	 */
	private boolean forwardChecking ( )
	{
		//Go through each variable and assign a value and check for consistency
		for (Variable v: network.getVariables())
		{
			if (v.isAssigned())
			{	
				//check the neighbor of variable v and removes value from its from neighbor
				for (Variable neighborVar: network.getNeighborsOfVariable(v))
				{
					//Check if variable v value is the same as its neighbor 
					if (neighborVar.getAssignment() == v.getAssignment())
						return false;
					else if (!neighborVar.isAssigned())
					{	
						for (Integer domainVal: neighborVar.getValues())
						{	
							//Push variable to the stack if variable contains the domain
							if (v.getAssignment() == domainVal)
							{
								trail.push(neighborVar);
								break;
							}
						}
					}
					//Eliminate variable from its neighbor
					neighborVar.removeValueFromDomain(v.getAssignment());
					//if neighbor variable has no value after remove, then it is not consistent
					if (neighborVar.getDomain().size() == 0)
						return false;
				}
			}
		}
		return true;
	}

	/**
	 * Part 2 TODO: Implement both of Norvig's Heuristics
	 *
	 * This function will do both Constraint Propagation and check
	 * the consistency of the network
	 *
	 * (1) If a variable is assigned then eliminate that value from
	 *     the square's neighbors.
	 *
	 * (2) If a constraint has only one possible place for a value
	 *     then put the value there.
	 *
	 * Note: remember to trail.push variables before you change their domain
	 * Return: true is assignment is consistent, false otherwise
	 */
	private boolean norvigCheck ( )
	{
		int assignment;
		for(Variable v : network.getVariables())
		{
			if(v.isAssigned())
			{
				assignment = v.getAssignment();
				for(Variable neighbor : network.getNeighborsOfVariable(v))
				{
					for(Integer i : neighbor.getValues())
					{
						//System.out.println("Integer i = " + i);
						if(i == assignment) 
						{
							System.out.println("Integer i = " + i + " and assignment = " + assignment);
							trail.push(neighbor);
							//neighbor.removeValueFromDomain(assignment);


						}/*
							trail.push(neighbor);
							neighbor.removeValueFromDomain(assignment);
							if (neighbor.getDomain().size() == 0)
								return false;
							if (neighbor.getDomain().size() == 1)
								neighbor.assignValue(assignment);
						}*/
					}
				}
			}
		}
		return true;
	}

	/**
	 * Optional TODO: Implement your own advanced Constraint Propagation
	 *
	 * Completing the three tourn heuristic will automatically enter
	 * your program into a tournament.
	 */
	private boolean getTournCC ( )
	{
		return false;
	}

	// =================================================================
	// Variable Selectors
	// =================================================================

	// Basic variable selector, returns first unassigned variable
	private Variable getfirstUnassignedVariable()
	{
		for ( Variable v : network.getVariables() )
			if ( ! v.isAssigned() )
				return v;

		// Everything is assigned
		return null;
	}

	/**
	 * Part 1 TODO: Implement the Minimum Remaining Value Heuristic
	 *
	 * Return: The unassigned variable with the smallest domain
	 */
	private Variable getMRV ( )
	{
		Variable unassignedVar = null;
		int mrv = 0;
		int neighborCount = 0;
		//Go through each variable and find the min variable size
		//Select the smallest domain size and return
		for(Variable v : network.getVariables())
		{	
			if (!v.isAssigned())
			{
				//Go through the neighbors of the unassigned variable
				for (Variable neighborVar : network.getNeighborsOfVariable(v))
				{
					if (neighborVar.isAssigned())
						neighborCount++;
				}
				//Pick the unassigned variable with highest assigned neighbor
				if (neighborCount > mrv)
				{
					unassignedVar = v;
					mrv = neighborCount;
				}
				neighborCount = 0;
			}
		}
		System.out.println(network.getModifiedConstraints());
		return unassignedVar;
	}

	/**
	 * Part 2 TODO: Implement the Degree Heuristic
	 *
	 * Return: The unassigned variable with the most unassigned neighbors
	 */
	private Variable getDegree ( )
	{
		Variable unassignedVar = null;
		int unassignedCount = 0;
		int degree = 0;
		for(Variable v : network.getVariables())
		{

			if(!v.isAssigned())
			{
				for(Variable neighbor : network.getNeighborsOfVariable(v))
				{
					if(!neighbor.isAssigned())
					{
						unassignedCount++;
					}
				}
				if(unassignedCount > degree)
				{
					degree = unassignedCount;
					unassignedVar = v;
				}
				System.out.println("V: " + v.getName() + " unassignedCount: " + unassignedCount);
				unassignedCount = 0;
			}
		}
		return unassignedVar;
	}

	/**
	 * Part 2 TODO: Implement the Minimum Remaining Value Heuristic
	 *                with Degree Heuristic as a Tie Breaker
	 *
	 * Return: The unassigned variable with, first, the smallest domain
	 *         and, second, the most unassigned neighbors
	 */
	private Variable MRVwithTieBreaker ( )
	{
		//Variable tieBreaker = getDegree();
		//return tieBreaker;
		return null;
	}

	/**
	 * Optional TODO: Implement your own advanced Variable Heuristic
	 *
	 * Completing the three tourn heuristic will automatically enter
	 * your program into a tournament.
	 */
	private Variable getTournVar ( )
	{
		return null;
	}

	// =================================================================
	// Value Selectors
	// =================================================================

	// Default Value Ordering
	public List<Integer> getValuesInOrder ( Variable v )
	{
		List<Integer> values = v.getDomain().getValues();

		Comparator<Integer> valueComparator = new Comparator<Integer>(){

			@Override
			public int compare(Integer i1, Integer i2) {
				return i1.compareTo(i2);
			}
		};
		Collections.sort(values, valueComparator);
		return values;
	}

	/**
	 * Part 1 TODO: Implement the Least Constraining Value Heuristic
	 *
	 * The Least constraining value is the one that will knock the least
	 * values out of it's neighbors domain.
	 *
	 * Return: A list of v's domain sorted by the LCV heuristic
	 *         The LCV is first and the MCV is last
	 */
	public List<Integer> getValuesLCVOrder ( Variable v )
	{
		// THIS IS A TEST 
		List<Integer> sortedLCV = new ArrayList<Integer>(); // the LCV's in increasing size
		List<Integer> domainList = new ArrayList<Integer>(); // Elements of the domain
		List<Integer> countList = new ArrayList<Integer>(); //	Size of the count after comparison
		for(Integer vElement : v.getDomain())
		{
			domainList.add(vElement);
			int count = 0;
			for(Variable neighbor : network.getNeighborsOfVariable(v))
			{
				for(Integer neighborElement : neighbor.getDomain())
				{
					if(vElement == neighborElement)
						count++;
				}
			}
			countList.add(count);
		}
		int minIndex = 0;
		for(Integer countListItem : countList)
		{
			minIndex = countList.indexOf(Collections.min(countList)); 
			sortedLCV.add( domainList.get( minIndex ) );
			countList.set(minIndex, Integer.MAX_VALUE); 
		}
		return sortedLCV;
	}

	/**
	 * Optional TODO: Implement your own advanced Value Heuristic
	 *
	 * Completing the three tourn heuristic will automatically enter
	 * your program into a tournament.
	 */
	public List<Integer> getTournVal ( Variable v )
	{
		return null;
	}

	//==================================================================
	// Engine Functions
	//==================================================================

	public void solve ( )
	{
		if ( hasSolution )
			return;

		// Variable Selection
		Variable v = selectNextVariable();

		if ( v == null )
		{
			for ( Variable var : network.getVariables() )
			{
				// If all variables haven't been assigned
				if ( ! var.isAssigned() )
				{
					System.out.println( "Error" );
					return;
				}
			}

			// Success
			hasSolution = true;
			return;
		}

		// Attempt to assign a value
		for ( Integer i : getNextValues( v ) )
		{
			// Store place in trail and push variable's state on trail
			trail.placeTrailMarker();
			trail.push( v );

			// Assign the value
			v.assignValue( i );

			// Propagate constraints, check consistency, recurse
			if ( checkConsistency() )
				solve();

			// If this assignment succeeded, return
			if ( hasSolution )
				return;

			// Otherwise backtrack
			trail.undo();
		}
	}

	private boolean checkConsistency ( )
	{
		switch ( cChecks )
		{
			case "forwardChecking":
				return forwardChecking();

			case "norvigCheck":
				return norvigCheck();

			case "tournCC":
				return getTournCC();

			default:
				return assignmentsCheck();
		}
	}

	private Variable selectNextVariable ( )
	{
		switch ( varHeuristics )
		{
			case "MinimumRemainingValue":
				return getMRV();

			case "Degree":
				return getDegree();

			case "MRVwithTieBreaker":
				return MRVwithTieBreaker();

			case "tournVar":
				return getTournVar();

			default:
				return getfirstUnassignedVariable();
		}
	}

	public List<Integer> getNextValues ( Variable v )
	{
		switch ( valHeuristics )
		{
			case "LeastConstrainingValue":
				return getValuesLCVOrder( v );

			case "tournVal":
				return getTournVal( v );

			default:
				return getValuesInOrder( v );
		}
	}

	public boolean hasSolution ( )
	{
		return hasSolution;
	}

	public SudokuBoard getSolution ( )
	{
		return network.toSudokuBoard ( sudokuGrid.getP(), sudokuGrid.getQ() );
	}

	public ConstraintNetwork getNetwork ( )
	{
		return network;
	}
}