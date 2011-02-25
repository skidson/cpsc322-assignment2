package ca.ubc.cpsc322.sudoku;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SudokuSolver {
	private Cell[][] cells = new Cell[9][9];
	/**
	 * @return names of the authors and their student IDs (1 per line).
	 */
	public String authors() {
		return "Stephen Kidson\n15345077\nJeff Payan\n18618074";
	}

	/**
	 * Performs constraint satisfaction on the given Sudoku board using Arc Consistency and Domain Splitting.
	 * 
	 * @param board the 2d int array representing the Sudoku board. Zeros indicate unfilled cells.
	 * @return the solved Sudoku board
	 */
	public int[][] solve(int[][] board) throws Exception {
		init(board);
		
		//Try to solve with just Arc Consistency
		arcConsistency(cells);
		board = fillBoard(board, cells);
		if (isComplete(board)) 
			return board;
		
		int[][] finalBoard = domainSplitSolve(board, cells);
		if (finalBoard == null)
			throw new Exception("No Solutions");
		return finalBoard;
	}

	// Make all arcs in the given board Arc Consistent
	public void arcConsistency(Cell[][] cells) {
		List<Arc> TDA = new LinkedList<Arc>();

		for (int x = 0; x < 9; x++)
			for (int y = 0; y < 9; y++)
				getTodoArcs(cells, TDA, x, y);

		while(TDA.size() > 0) {
			// Remove an arc from TDA and get the cells that it is attached to
			Arc arc = TDA.remove(0);

			// Get the domains of the cells that the arc was connected to
			Set<Integer> fromDomain = arc.getFrom().domains;
			Set<Integer> toDomain = arc.getTo().domains;
			Set<Integer> possible = new HashSet<Integer>();

			// For every value in the domain of this cell, check it against the other end of the arc
			// if they are not equal, add it to the possible values that this cell's domain
			// could be equal too.
			for (int i : fromDomain) {
				Iterator<Integer> iterator = toDomain.iterator();
				
				while(iterator.hasNext()) {
					int j = iterator.next();
					if (i != j) {
						possible.add(i);
						break;
					}
				}
			}
			// As long as the possible values for this cell's domain does not equal
			// its current domain, add all arcs this cell could have modified
			// by changing its domain, and then set its domain to the possible
			// set that was generated
			if(!possible.equals(fromDomain)){
				getTodoArcs(cells, TDA, arc.getFrom().x, arc.getFrom().y);
				cells[arc.getFrom().x][arc.getFrom().y].domains = possible;	
			}
		}
	}
	
	// Revisit cells who's arcs may no longer be consistent due to the change of a domain on a connected cell
	public void getTodoArcs(Cell[][] cells, List<Arc> TDA, int x, int y){
		// Add all cells from this cell's row
		for (int i = 0; i < 9; i++)
			if (i != y )
				TDA.add(new Arc(cells[x][i], cells[x][y]));
		// Add all cells from this cells column
		for (int i = 0; i < 9; i++)
			if (i != x)
				TDA.add(new Arc(cells[i][y], cells[x][y]));

		//Add all the missing cells from this cell's square
		for (int m = y/3 * 3 ; m < y/3 * 3 + 3 ; m++)
			for (int n = x/3 * 3 ; n < x/3 * 3 + 3 ; n++)
				if (m != y && n != x)
					TDA.add(new Arc(cells[n][m], cells[x][y]));
	}
	
	public int[][] domainSplitSolve(int[][] board, Cell[][] cells) throws Exception{
		if (isComplete(board))
			return board;
		// Make 2 deep copies of cells arrays
		Cell[][] temp1 = copyCells(cells);
		Cell[][] temp2 = copyCells(cells);

		splitDomains(temp1, temp2);

		// Make 2 deep copies of the board
		int[][] copyboard1 = board.clone();
		int[][] copyboard2 = board.clone();

		// Fill the boards with any possible values and then run arc consistency on the
		// boards with split domains
		fillBoard(copyboard1, temp1);
		arcConsistency(temp1);
		fillBoard(copyboard2, temp2);
		arcConsistency(temp2);
		
		// If all the domains in the first split are valid, that is have at least one valid value
		// split another domain
		if (checkDomains(temp1))
			copyboard1 = domainSplitSolve(copyboard1, temp1);
		else 
			copyboard1 = null;

		if (checkDomains(temp2))
			copyboard2 = domainSplitSolve(copyboard2, temp2);
		else
			copyboard2 = null;
		
		
		// Bonus question : If both boards are completely solved and they are equal then there are
		// at least two solutions, throw exception
		if (isComplete(copyboard1) && isComplete(copyboard2) && !areBoardsEqual(copyboard1, copyboard2))
			throw new Exception("Many solutions.");
		
		//if either board is complete, return it otherwise return null
		if (isComplete(copyboard1)) 
			return copyboard1;

		if (isComplete(copyboard2)) 
			return copyboard2;

		return null;
	}

	// Find the first domain with more than one value and split it in half
	public void splitDomains(Cell[][] temp1, Cell[][] temp2){
		for ( int x = 0 ; x < 9 ; x++ ) {
			for ( int y = 0 ; y < 9 ; y++ ) {
				if ( temp1[x][y].domains.size() > 1 ) {
					int size = temp1[x][y].domains.size();
					List<Integer> firstHalf = new ArrayList<Integer>();
					List<Integer> secondHalf = new ArrayList<Integer>();
					Iterator<Integer> itr = temp1[x][y].domains.iterator();
					// Split the domain
					for (int k = 0 ; k < size ; k++)
						if (k <= (size-2)/2)
							firstHalf.add(itr.next());
						else
							secondHalf.add(itr.next());
					// Remove the values from original domains
					for(Integer k : firstHalf) 
						temp2[x][y].domains.remove(k);
					for(Integer k : secondHalf) 
						temp1[x][y].domains.remove(k);
					return;
				}
			}
		}
	}
	
	/* **************************************** HELPER FUNCTIONS ************************************ */
	private void init(int[][] board) {
		//Initialize the cells and their domains
		for (int x = 0; x < 9; x++)
			for (int y = 0; y < 9; y++){
				cells[x][y] = new Cell(board[x][y], x, y);
				cells[x][y].initDomain();
			}
	}

	private Cell[][] copyCells(Cell[][] origCells){
		Cell[][] copy = new Cell[9][9];
		for (int x = 0; x < 9; x++)
			for (int y = 0; y < 9; y++)
				copy[x][y] = origCells[x][y].clone();
		return copy;
	}
	
	// Check if the board is complete (False if any value is still 0 (Unsolved))
	private boolean isComplete(int[][] board){
		if (board == null)
			return false;
		for (int x = 0 ; x < 9 ; x++)
			for (int y = 0 ; y < 9 ; y++)
				if (board[x][y] == 0) 
					return false;
		return true;
	}

	// Fill the board will all known values (only one value in cell's domain)
	public int[][] fillBoard(int[][] board, Cell[][] cells) {	
		for (int i = 0 ; i < 9 ; ++i)
			for (int j = 0 ; j < 9 ; ++j)
				if (cells[i][j].domains.size() == 1)
					board[i][j] = cells[i][j].domains.iterator().next();
		return board;
	}
	
	// Check if two boards are identical
	public boolean areBoardsEqual( int[][] board1, int[][] board2) {
		if (board1 == null || board2 == null) {
			if (board1 == null && board2 == null) 
				return true;
			return false;
		}

		for(int i = 0 ; i < 9 ; ++i)
			for (int j = 0 ; j < 9 ; ++j)
				if (board1[i][j] != board2[i][j]) 
					return false;
		return true;
	}

	/**
	 * Ensure every domain has at least one value
	 * @param checkCells the cell array
	 * @return true/false
	 */
	public boolean checkDomains(Cell[][] checkCells) {
		for (int x = 0 ; x < 9 ; x++)
			for (int y = 0 ; y < 9 ; y++)
				if (checkCells[x][y].domains.size() == 0) 
					return false;
		return true;
	}

	/* ************************ EMBEDDED CLASSES ************************ */
	
	// A class to store information about every Cell on the sudoku board
	private class Cell {
		public Set<Integer> domains;
		public int value;
		public int x, y;

		public Cell(int value, int x, int y) {
			this.value = value;
			this.x = x;
			this.y = y;
		}
		
		// Initialize the domain to (1-9) if the value is 0
		// Otherwise set domain = value;
		public void initDomain() {
			domains = new HashSet<Integer>();
			if (this.value == 0)
				for(int i = 1; i < 10; i++)
					domains.add(i);
			else 
				domains.add(value);
		}
		
		public Cell clone() {
			Cell clone = new Cell(this.value, this.x, this.y);
			clone.domains = new HashSet<Integer>();
			for (Integer i : this.domains)
				clone.domains.add(i.intValue());
			return clone;
		}
	}
	
	// A class to store arcs
	private class Arc {
		Cell fromNode;
		Cell toNode;

		public Arc(Cell a, Cell b) {
			fromNode = a;
			toNode = b;
		}

		public Cell getFrom() {
			return fromNode;
		}

		public Cell getTo() {
			return toNode;
		}
	}
}