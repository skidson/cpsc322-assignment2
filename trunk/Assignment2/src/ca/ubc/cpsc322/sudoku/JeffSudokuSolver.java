package ca.ubc.cpsc322.sudoku;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ca.ubc.cpsc322.sudoku.SudokuSolver.Cell;


public class JeffSudokuSolver {
	private Cell[][] cells = new Cell[9][9];
	/**
	 * @return names of the authors and their student IDs (1 per line).
	 */
	public String authors() {
		return "Stephen Kidson - #15345077\nJeff Payan - #18618074";
	}

	/**
	 * Performs constraint satisfaction on the given Sudoku board using Arc Consistency and Domain Splitting.
	 * 
	 * @param board the 2d int array representing the Sudoku board. Zeros indicate unfilled cells.
	 * @return the solved Sudoku board
	 */
	public int[][] solve(int[][] board) throws Exception {
		int[][] filledBoard = board;

		init(board);
		//Try to solve with just Arc Consistency
		ArcConsistency(board, cells);
		filledBoard = fillBoard(filledBoard, cells);

		if(isComplete(board)) return filledBoard;

		int[][] finalBoard = domainSplitSolve(filledBoard, cells);


		if (finalBoard == null){
			throw new Exception("No Solutions");
		}
		
		return finalBoard;
	}

	private void init(int[][] board) {
		//Initialize the cells and their domains
		for (int x = 0; x < 9; x++)
			for (int y = 0; y < 9; y++){
				cells[x][y] = new Cell(board[x][y], x, y);
				cells[x][y].initDomain();
			}
	}

	public Cell[][] copyCells(Cell[][] origCells){
		Cell[][] copy = new Cell[9][9];
		
		for (int x = 0; x < 9; x++)
			for (int y = 0; y < 9; y++) {
				copy[x][y] = origCells[x][y].clone();
			}
		return copy;
	}

	public int[][] copyBoard(int[][] board){
		int[][] copy = new int[9][9];
		for (int x = 0 ; x < 9 ; x++) {
			for(int y = 0 ; y < 9 ; y++) {
				copy[x][y] = board[x][y];
			}
		}
		return copy;
	}

	public boolean isComplete(int[][] board){
		//Check to see if any domains have more than one value
		if (board == null) return false;
		
		for(int x = 0 ; x < 9 ; x++) {
			for (int y = 0 ; y < 9 ; y++) {
				if (board[x][y] == 0) return false;
			}
		}
		return true;
	}

	public int[][] fillBoard(int[][] board, Cell[][] cells) {
		//If the cell has only one value in its domain, set its value to that remaining value in the domain
		int[][] filledBoard = board;
		for (int i = 0 ; i < 9 ; ++i) {
			for (int j = 0 ; j < 9 ; ++j) {
				if (cells[i][j].getDomain().size() == 1) {
					cells[i][j].value = cells[i][j].domains.iterator().next();
					filledBoard[i][j] = cells[i][j].value;
				}
			}
		}
		return filledBoard;
	}


	//Arc Consistency
	public void ArcConsistency(int[][] board, Cell[][] cells){
		List<Arc> TDA = new ArrayList<Arc>();

		for (int x = 0; x < 9; x++)
			for (int y = 0; y < 9; y++)
				createArcs(TDA, x ,y);

		while(TDA.size() != 0) {
			//Remove an arc from TDA and get the cells that it is attached to
			Arc arc = TDA.remove(0);
			Cell from = arc.getFrom();
			Cell to = arc.getTo();

			//Get the domains of the cells that the arc was connected to
			Set<Integer> fromDomain = from.getDomain();
			Set<Integer> toDomain = to.getDomain();
			Set<Integer> possible = new HashSet<Integer>();

			//For every value in the domain of this cell, check it against the other end of the arc
			// if they are not equal, add it to the possible values that this cell's domain
			//could be equal too.
			for (int i : fromDomain) {
				Iterator<Integer> domItr = toDomain.iterator();
				while(true) {
					if (domItr.hasNext()) {
						int j = domItr.next();
						if (i != j) {
							possible.add(i);
							break;
						}
					}
					else {
						break;
					}
				}
			}
			//As long as the possible values for this cell's domain does not equal
			//its current domain, add all arcs this cell could have modified
			//by changing its domain, and then set its domain to the possible
			//set that was generated
			if(!possible.equals(fromDomain)){
				addModifiedDomainsToTDA(TDA, from.coord[0], from.coord[1]);
				cells[from.coord[0]][from.coord[1]].domains = possible;					
			}
		}
	}

	public int[][] domainSplitSolve(int[][] board, Cell[][] cells) throws Exception{
		if (isComplete(board)) {
			return board;
		}
		//Make 2 deep copies of cells arrays
		Cell[][] cells1 = copyCells(cells);
		Cell[][] cells2 = copyCells(cells);
		
		splitDomains(cells1, cells2);
		
		//Make 2 deep copies of the board
		int[][] copyboard1 = copyBoard(board);
		int[][] copyboard2 = copyBoard(board);

		fillBoard(copyboard1, cells1);
		ArcConsistency(copyboard1, cells1);
		fillBoard(copyboard2, cells2);
		ArcConsistency(copyboard2, cells2);

		if (checkDomains(cells1)) {
			System.out.println("Going deeper");
			copyboard1 = domainSplitSolve(copyboard1, cells1);
		}
		else copyboard1 = null;

		if(checkDomains(cells2)) {
			System.out.println("Going deeper");
			copyboard2 = domainSplitSolve(copyboard2, cells2);
		}
		else copyboard2 = null;

		if (isComplete(copyboard1) && isComplete(copyboard2) && !areBoardsEqual(copyboard1, copyboard2)) {
			throw new Exception("Many solutions.");
		}

		if (isComplete(copyboard1)) return copyboard1;

		if (isComplete(copyboard2)) return copyboard2;

		return null;
	}
	
	public void splitDomains(Cell[][] cells1, Cell[][] cells2){
		for ( int x = 0 ; x < 9 ; x++ ) {
			for ( int y = 0 ; y < 9 ; y++ ) {
				if ( cells1[x][y].domains.size() > 1 ) {
					int size = cells1[x][y].domains.size();
					ArrayList<Integer> dom1nums = new ArrayList<Integer>(5);
					ArrayList<Integer> dom2nums = new ArrayList<Integer>(5);
					Iterator<Integer> itr = cells1[x][y].domains.iterator();

					for (int k = 0 ; k < size ; k++) {
						if (k <= (size-2)/2) {
							dom1nums.add(itr.next());
						} else {
							dom2nums.add(itr.next());
						}
					}
					
					for(Integer k : dom1nums) cells1[x][y].domains.remove(k);
					for(Integer k : dom2nums) cells2[x][y].domains.remove(k);
					
					return;
				}
			}
		}
		
	}
	
	public boolean areBoardsEqual( int[][] board1, int[][] board2) {
		if (board1 == null || board2 == null){
			if (board1 == null && board2 == null) return true;
			return false;
		}
		
		for(int i = 0 ; i < 9 ; ++i)
			for (int j = 0 ; j < 9 ; ++j)
				if (board1[i][j] != board2[i][j]) return false;
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
				if (checkCells[x][y].domains.size() == 0) return false;
		return true;
	}
	
	
	public void createArcs(List<Arc> TDA, int x, int y){
		// Add all cells from this cell's row

		for (int i = 0; i < 9; i++)
			if (i != y ){
				TDA.add(new Arc(cells[x][y], cells[x][i]));
			}
		// Add all cells from this cell's column
		for (int i = 0; i < 9; i++)
			if (i != x){
				TDA.add(new Arc(cells[x][y], cells[i][y]));
			}

		//Find missing cells in the square where the cell is located
		for (int m = y/3 * 3 ; m < y/3 * 3 + 3 ; m++) {
			for (int n = x/3 * 3 ; n < x/3 * 3 + 3 ; n++) {
				if (m != y && n != x) {
					TDA.add(new Arc(cells[x][y], cells[n][m]));
				}
			}
		}
	}

	public void addModifiedDomainsToTDA(List<Arc> TDA, int x, int y){
		// Add all cells from this cell's row
		for (int i = 0; i < 9; i++)
			if (i != y ){
				TDA.add(new Arc(cells[x][i], cells[x][y]));
			}
		// Add all cells from this cells column
		for (int i = 0; i < 9; i++)
			if (i != x){
				TDA.add(new Arc(cells[i][y], cells[x][y]));
			}

		//Add all the missing cells from this cell's square
		for (int m = y/3 * 3 ; m < y/3 * 3 + 3 ; m++) {
			for (int n = x/3 * 3 ; n < x/3 * 3 + 3 ; n++) {
				if (m != y && n != x) {
					TDA.add(new Arc(cells[n][m], cells[x][y]));
				}
			}
		}
	}
	/* ************************ EMBEDDED CLASSES ************************ */

	public class Cell {
		Set<Integer> domains = new HashSet<Integer>();
		int value;
		int[] coord = new int[2];

		public Cell(int value, int x, int y) {
			this.value = value;
			coord[0] = x;
			coord[1] = y;
		}

		public void initDomain(){
			if (this.value == 0){
				for(int i = 1; i < 10; i++)
					domains.add(i);
			}else domains.add(value);
		}
		
		public Cell clone(){
			Cell theClone = new Cell(this.value, this.coord[0], this.coord[1]);
			theClone.domains = new HashSet<Integer>();
			for(Integer i : this.domains) theClone.domains.add(i);
			return theClone;
		}
		/**
		 * Removes the specified value from this cell's domain. Returns true if this results in the cell
		 * being complete, false otherwise.
		 * @param value
		 * @return A boolean indicating if this cell is now solved.
		 */
		public Set<Integer> getDomain() {
			return domains;
		}			
	}

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