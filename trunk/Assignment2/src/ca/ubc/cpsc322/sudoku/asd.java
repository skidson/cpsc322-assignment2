package ca.ubc.cpsc322.sudoku;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public class asd {

	/**
	 * Solves a sudoku via arc consistency and domain splitting.
	 * 
	 * @param A 2D Array that represents the sudoku board. 1-9 indicates a filled in cell, 0 indicates an empty cell.
	 * @return the solved Sudoku board
	 * @throws Exception 
	 */
	public int[][] solve(int[][] board) throws Exception 
	{
		if (isBoardComplete(board))
		{
			return board;
		}

		List<List<Set<Integer>>> domains;
		int[][] nextBoard = board;
		
		domains = new ArrayList<List<Set<Integer>>>(9);
		initializeDomains(board, domains);
		makeArcConsistent(board, domains);
				
		nextBoard = fillBoard(nextBoard, domains);
		
		if (isBoardComplete(board))
		{
			return nextBoard;
		}
		
		int[][] result = solveWithDomainSplit(nextBoard, domains);
		
		if (result == null)
		{
			throw new Exception("No solution.");
		}
		
		return result;

	}
	
	/**
	 * 
	 * @param board The board to be solved
	 * @param domain The board's domain
	 * @return A solved board
	 * @throws Exception If there are many solutions
	 */
	public int[][] solveWithDomainSplit(int[][] board, List<List<Set<Integer>>> domain) throws Exception
	{
		if (isBoardComplete(board))
		{
			return board;
		}
		
		List<List<Set<Integer>>> domain1 = copyDomains(domain);
		List<List<Set<Integer>>> domain2 = copyDomains(domain);
		
		splitDomains(domain1, domain2);
		int[][] nextBoard1 = copyBoard(board);
		int[][] nextBoard2 = copyBoard(board);

		fillBoard(nextBoard1, domain1);
		makeArcConsistent(nextBoard1, domain1);
		fillBoard(nextBoard2, domain2);
		makeArcConsistent(nextBoard2, domain2);

		if (isDomainFeasible(domain1))
		{
			nextBoard1 = solveWithDomainSplit(nextBoard1, domain1);
		}
		else
		{
			nextBoard1 = null;
		}
		
		if(isDomainFeasible(domain2))
		{
			nextBoard2 = solveWithDomainSplit(nextBoard2, domain2);
		}
		else
		{
			nextBoard2 = null;
		}
		
		if (isBoardComplete(nextBoard1) && isBoardComplete(nextBoard2) && !areBoardsEqual(nextBoard1, nextBoard2))
		{
			throw new Exception("Many solutions.");
		}
		
		if (isBoardComplete(nextBoard1))
		{
			return nextBoard1;
		}
		if (isBoardComplete(nextBoard2))
		{
			return nextBoard2;
		}
		
		return null;
	}
	
	/**
	 * Checks if two boards are equal in every cell
	 * @param board1
	 * @param board2
	 * @return yes/no
	 */
	public boolean areBoardsEqual( int[][] board1, int[][] board2)
	{
		
		if (board1 == null || board2 == null)
		{
			if (board1 == null && board2 == null)
			{
				return true;
			}
			return false;
		}
		
		for(int i = 0 ; i < 9 ; ++i)
		{
			for (int j = 0 ; j < 9 ; ++j)
			{
				if (board1[i][j] != board2[i][j])
				{
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Checks if for every cell, the domain of it has size >0
	 * @param domains The domains
	 * @return yes/no
	 */
	public boolean isDomainFeasible(List<List<Set<Integer>>> domains)
	{
		for (int i = 0 ; i < 9 ; ++i)
		{
			for (int j = 0 ; j < 9 ; ++j)
			{
				if (domains.get(i).get(j).size() == 0)
				{
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Checks if for each cell, there is a value 1,2,...,9
	 * @param board
	 * @return yes/no
	 */
	public boolean isBoardComplete(int[][] board)
	{
		if (board == null)
		{
			return false;
		}
		for(int i = 0 ; i < 9 ; ++i)
		{
			for (int j = 0 ; j < 9 ; ++j)
			{
				if (board[i][j] == 0)
				{
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Fills the board with values according to the domain. The cell will be filled in only when there is 1 item in that cell's domain.
	 * @param board
	 * @param doms
	 * @return a board with all cells that have one item in the domain filled in with that domain value.
	 */
	public int[][] fillBoard(int[][] board, List<List<Set<Integer>>> doms)
	{
		int[][] filledBoard = board;
		for (int i = 0 ; i < 9 ; ++i)
		{
			for (int j = 0 ; j < 9 ; ++j)
			{
				if (doms.get(i).get(j).size() == 1)
				{
					filledBoard[i][j] = doms.get(i).get(j).iterator().next();
				}
			}
		}
		return filledBoard;
	}

	/**
	 * Splits the domains in half for the first cell it finds that has more than 1 domain value
	 * @param doms1
	 * @param doms2
	 */
	public void splitDomains(List<List<Set<Integer>>> doms1, List<List<Set<Integer>>> doms2)
	{
		
		for ( int i = 0 ; i < 9 ; ++i )
		{
			for ( int j = 0 ; j < 9 ; ++j )
			{
				if ( doms1.get(i).get(j).size() > 1 )
				{
					int size = doms1.get(i).get(j).size();
					ArrayList<Integer> dom1nums = new ArrayList<Integer>(5);
					ArrayList<Integer> dom2nums = new ArrayList<Integer>(5);
					Iterator<Integer> itr = doms1.get(i).get(j).iterator();

					for (int k = 0 ; k < size ; ++k )
					{
						if (k <= (size-2)/2)
						{
							dom1nums.add(itr.next());
						}
						else
						{
							dom2nums.add(itr.next());
						}
					}
					
					for(Integer k : dom1nums)
					{
						doms2.get(i).get(j).remove(k);
					}
					for(Integer k : dom2nums)
					{
						doms1.get(i).get(j).remove(k);
					}
					
					return;
				}
			}
		}
		
		
		
	}
	
	/**
	 * Makes a copy of the board
	 * @param board
	 * @return a copy of the board
	 */
	private int[][] copyBoard(int[][] board)
	{
		int[][] copy = new int[board.length][board[0].length];
		for (int i = 0 ; i < board.length ; ++i)
		{
			for(int j = 0 ; j < board[0].length ; ++j)
			{
				copy[i][j] = board[i][j];
			}
		}
		return copy;
	}
	
	/**
	 * Creates a copy of the domains
	 * @param doms
	 * @return a copy of the domains
	 */
	private List<List<Set<Integer>>> copyDomains(List<List<Set<Integer>>> doms)
	{
		List<List<Set<Integer>>> copy = new ArrayList<List<Set<Integer>>>(9);
		//Copy doms to domSplitDomains
		for (int i = 0 ; i < 9 ; ++i)
		{
			copy.add(new ArrayList<Set<Integer>>(9));
			for( int j = 0 ; j < 9 ; ++j )
			{
				copy.get(i).add(new HashSet<Integer>(9));
				Iterator<Integer> itr = doms.get(i).get(j).iterator();
				while( itr.hasNext() )
				{
					copy.get(i).get(j).add(itr.next());
				}
			}
		}
		return copy;
	}
	
	/**
	 * Fills in values for the domain
	 * @param board
	 * @param domains an empty List<List<Set<Integer>>>
	 */
	public void initializeDomains(int[][] board, List<List<Set<Integer>>> domains)
	{
		for (int i = 0 ; i < 9 ; ++i)
		{
			domains.add(new ArrayList<Set<Integer>>(9) );
			for (int j = 0 ; j < 9 ; ++j)
			{
				domains.get(i).add(new HashSet<Integer>() );
				if (board[i][j] >= 1 && board[i][j] <= 9)
				{
					//The cell is already filled in so that should be the only value in the domain.
					domains.get(i).get(j).add(board[i][j]);
				}
				else
				{
					for (int k = 1 ; k <= 9 ; ++k)
					{
						//Blank, so add numbers 1-9 to the domain
						domains.get(i).get(j).add(k);
					}
				}
			}
		}
	}
	
	/**
	 * Makes domains arc consistent depending on the board
	 * @param board
	 * @param domains
	 */
	public void makeArcConsistent(int[][] board, List<List<Set<Integer>>> domains)
	{
		List<Arc> TDA = new LinkedList<Arc>();
		for (int i = 0 ; i < 9 ; ++i)
		{
			for (int j = 0 ; j < 9 ; ++j)
			{
				addRelevantCellsToTDA(TDA, i, j);
			}
		}

		while (TDA.size() != 0)
		{
			Arc arc = TDA.remove(0);
			int[] from = arc.getFrom();
			int[] to = arc.getTo();
			Set<Integer> fromDomain = domains.get(from[0]).get(from[1]);
			Set<Integer> toDomain = domains.get(to[0]).get(to[1]);
			Set<Integer> feasible = new HashSet<Integer>();
			
			for (int i : fromDomain)
			{
				Iterator<Integer> itr = toDomain.iterator();
				while(true)
				{
					if (itr.hasNext())
					{
						int j = itr.next();
						if (i == 0 && j == 2);
						if (i != j)
						{
							feasible.add(i);
							break;
						}
					}
					else
					{
						break;
					}
				}
				
			}
			
			if (!feasible.equals(fromDomain))
			{
				revisitCellsToTDA(TDA, from[0], from[1]);
				domains.get(from[0]).set(from[1], feasible);
			}	
		}
	}
	
	/**
	 * Revisits the cells after a domain value is removed
	 * @param TDA
	 * @param i the y position of the cell
	 * @param j the x position of the cell
	 */
	private void revisitCellsToTDA(List<Arc> TDA, int i, int j)
	{
		for (int k = 0; k < 9 ; ++k)
		{
			//Add all other cells horizontal and vertical
			if (k != i)
				TDA.add(new Arc(k, j, i, j));
			if (k != j)
				TDA.add(new Arc(i, k, i, j));
		}
		
		//Figure out which 3x3 grid it's in and all missed local cells
		int subgrid_y = i / 3;
		int subgrid_x = j / 3;
		for (int m = subgrid_y * 3 ; m < subgrid_y * 3 + 3 ; ++m)
		{
			for (int n = subgrid_x * 3 ; n < subgrid_x * 3 + 3 ; ++n)
			{
				if (m != i && n != j)
				{
					TDA.add(new Arc(m, n, i, j));
				}
			}
		}
	}
	
	/**
	 * Adds all cells that the cell at i,j should compare against
	 * @param TDA
	 * @param i
	 * @param j
	 */
	private void addRelevantCellsToTDA(List<Arc> TDA, int i, int j)
	{
		for (int k = 0; k < 9 ; ++k)
		{
			//Add all other cells horizontal and vertical
			if (k != i)
				TDA.add(new Arc(i, j, k, j));
			if (k != j)
				TDA.add(new Arc(i, j, i, k));
		}
		
		//Figure out which 3x3 grid it's in and all missed local cells
		int subgrid_y = i / 3;
		int subgrid_x = j / 3;
		for (int m = subgrid_y * 3 ; m < subgrid_y * 3 + 3 ; ++m)
		{
			for (int n = subgrid_x * 3 ; n < subgrid_x * 3 + 3 ; ++n)
			{
				if (m != i && n != j)
				{
					TDA.add(new Arc(i, j, m, n));
				}
			}
		}
	}
	
	/**
	 * An arc (in this case) contains a fromNode and toNode which is the coordinates of a cell. 
	 * @author lijamez
	 *
	 */
	private class Arc
	{
		int[] fromNode;
		int[] toNode;
		
		public Arc(int a, int b, int c, int d)
		{
			fromNode = new int[2];
			fromNode[0] = a;
			fromNode[1] = b;
			toNode = new int[2];
			toNode[0] = c;
			toNode[1] = d;
		}

		public int[] getFrom()
		{
			return fromNode;
		}

		public int[] getTo()
		{
			return toNode;
		}
	}

}

