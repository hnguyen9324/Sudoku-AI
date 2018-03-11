import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

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
		// Part 1
		if(forwardChecking() == false)
			return false;
		// Part 2
		else
		{
			for(Variable v : network.getVariables())
			{
				Set<Integer> set = new HashSet<Integer>();

				if(!v.isAssigned())
				{
					
					for(Variable neighbor : network.getNeighborsOfVariable(v))
					{
						if(!neighbor.isAssigned())
						{
							int uniqueValue =-1;
							int numberOfMiss = 0; // number of times elements do not match
							
							// Either v and n have same domains or domain of v has 2 more elements than n
							if (v.getValues().equals(neighbor.getValues()) || v.getValues().size() - neighbor.getValues().size() >= 2)
							{
								continue;
							}
							
							// Domain size of v is smaller or equal to n
							else if(v.getValues().size() <= neighbor.getValues().size())
							{
								for(int i = 0; i < v.size(); i++)
								{
									if(v.getValues().get(i) == neighbor.getValues().get(i))
										continue;
									else
									{
										numberOfMiss++;
										if(numberOfMiss <= 1)
											uniqueValue = v.getValues().get(i);
									}
								}
								if(uniqueValue >= 0)
									set.add(uniqueValue);
							}
							
							// Domain size of v is 1 element larger than n
							else
							{
								int index = 0;
								for(int i = 0; i < neighbor.size(); i++)
								{
									if(v.getValues().get(i) == neighbor.getValues().get(i))
									{
										index++;
										continue;
									}
									else
									{
										index++;
										numberOfMiss++;
										if(numberOfMiss <= 1)
											uniqueValue = v.getValues().get(i);
									}
								}
								if(numberOfMiss <= 1)
									uniqueValue = v.getValues().get(index);
								if(uniqueValue >= 0)
									set.add(uniqueValue);
							}
						}
					}
					if(set.size() == 1)
					{
						System.out.println(v.getName() + " " + set);
						trail.push(v);
						v.assignValue(set.iterator().next());
					}
					if(v.getValues().size() == 0)
						return false;
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
		int mrv = 9999;
		int neighborCount = 0;

		List<Integer> neighborDomain = new LinkedList<Integer>();
		//Go through each variable and find the min variable size
		//Select the smallest domain size
		for(Variable v : network.getVariables())
		{	
			if (!v.isAssigned())
			{
				//Go through the neighbors of the unassigned variable
				for (Variable neighborVar : network.getNeighborsOfVariable(v))
				{	
					if (neighborVar.isAssigned())
					{
						if (neighborDomain.isEmpty())
							neighborDomain.add(neighborVar.getAssignment());
						else
						{
							//Prevent adding duplicate domain values 
							boolean isDuplicate = false;
							for (Integer value: neighborDomain)
							{
								if (value == neighborVar.getAssignment())
								{
									isDuplicate = true;
									break;
								}
							}
							if (!isDuplicate)
								neighborDomain.add(neighborVar.getAssignment());
						}
					}
				}
				neighborCount = v.getDomain().size() - neighborDomain.size();
				//Pick the variable with the smallest domain
				if (neighborCount < mrv)
				{
					unassignedVar = v;
					mrv = neighborCount;
				}
				neighborCount = 0;
				neighborDomain.clear();
			}
		}
		//System.out.println("Variable select: " + unassignedVar);
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
		int degree = 0;
		int neighborCount = 0;
		//Go through each variable and find the min variable size
		//Select the smallest domain size and return
		for(Variable v : network.getVariables())
		{	
			//Check if variable is unassigned
			if (!v.isAssigned())
			{
				//Go through the neighbors of the unassigned variable
				for (Variable neighborVar : network.getNeighborsOfVariable(v))
				{	
					//count the unassigned neighbor
					if (!neighborVar.isAssigned())
						neighborCount++;
				}
				//Pick the unassigned variable with highest degree
				if (neighborCount > degree)
				{
					unassignedVar = v;
					degree = neighborCount;
				}
				System.out.println(v + " Count= " + neighborCount);
				neighborCount = 0;
			}
		}
		//System.out.println("Variable select: " + unassignedVar);
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
		Variable unassignedVar = null;
		Variable mrvVar = null;
		List<Variable> mrvList = new LinkedList<Variable>();
		int mrv = 9999;
		int neighborCount = 0;
		int degree = 0;
		
		List<Integer> neighborDomain = new LinkedList<Integer>();
		//Go through each variable and find the min variable size
		//Select the smallest domain size
		for(Variable v : network.getVariables())
		{	
			if (!v.isAssigned())
			{
				//Go through the neighbors of the unassigned variable
				for (Variable neighborVar : network.getNeighborsOfVariable(v))
				{	
					if (neighborVar.isAssigned())
					{
						if (neighborDomain.isEmpty())
							neighborDomain.add(neighborVar.getAssignment());
						else
						{
							//Prevent adding duplicate domain values 
							boolean isDuplicate = false;
							for (Integer value: neighborDomain)
							{
								if (value == neighborVar.getAssignment())
								{
									isDuplicate = true;
									break;
								}
							}
							if (!isDuplicate)
								neighborDomain.add(neighborVar.getAssignment());
						}
					}
				}
				neighborCount = v.getDomain().size() - neighborDomain.size();
				//Pick the variable with the smallest domain
				if (neighborCount < mrv)
				{
					if (!mrvList.isEmpty())
						mrvList.clear();
					mrvList.add(v);
					mrv = neighborCount;
				}
				//if the variable domain size are equal to mrv then add to mrv variable list
				else if (neighborCount == mrv)
					mrvList.add(v);
				//Reset variables
				neighborCount = 0;
				neighborDomain.clear();
			}
		}
		
		if (mrvList.size() > 1)
		{
			//Get unassigned variable with the most unassigned neighbors by using degree check as a tie breaker
			for (Variable v: mrvList)
			{
				//System.out.println("MRV: " + v);
				//Count the neighbor of the variable selected by MRV
				for (Variable neighborVar: network.getNeighborsOfVariable(v))
				{
					if (!neighborVar.isAssigned())
						neighborCount++;	
				}
				//Select the variable that has the highest degree count
				if (neighborCount > degree)
				{
					unassignedVar = v;
					degree = neighborCount;
				}
				neighborCount = 0;
			}
		}
		else if (mrvList.size() == 1)
			unassignedVar = mrvList.get(0);
		System.out.println("Variable select: " + unassignedVar);
		return unassignedVar;
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
		List<Integer> sortedLCV = new LinkedList<Integer>();
		Map<Integer,Integer> domainMap = new HashMap<Integer,Integer>();
		//Traverse every domain in variable
		for (Integer val: v.getDomain())
		{
			int count = 0;
			//Check its neighbor
			for (Variable neighborVar: network.getNeighborsOfVariable(v))
			{
				if (!neighborVar.isAssigned())
				{
					for (Integer v2: neighborVar.getDomain())
					{
						if (v2 == val)
							count++;
					}
					
				}
				else if (neighborVar.getValues().get(0) == val)
					count++;
			}
			domainMap.put(val, count);
		}
		//Convert Map to List of Map
		List<Map.Entry<Integer, Integer>> list =
			new LinkedList<Map.Entry<Integer, Integer>>(domainMap.entrySet());
		
		//Sort list with Collections.sort(), provide a custom Comparator
		//Try switch the i1 i2 position in ascending order
		Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
		    public int compare(Map.Entry<Integer, Integer> i1,
				       Map.Entry<Integer, Integer> i2) {
			return (i1.getValue()).compareTo(i2.getValue());
		    }
		});

		//Loop the sorted list and put it into a new insertion order Map LinkedHashMap
		for (Map.Entry<Integer, Integer> entry : list) {
		    sortedLCV.add(entry.getKey());
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
