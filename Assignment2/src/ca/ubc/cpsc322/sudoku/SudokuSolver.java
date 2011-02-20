package ca.ubc.cpsc322.sudoku;

import java.util.Vector;

/**
 * Place for your code.
 */
public class SudokuSolver {

	/**
	 * @return names of the authors and their student IDs (1 per line).
	 */
	public String authors() {
		// TODO write it;
		return "NAMES OF THE AUTHORS AND THEIR STUDENT IDs (1 PER LINE)";
	}

	/**
	 * Performs constraint satisfaction on the given Sudoku board using Arc Consistency and Domain Splitting.
	 * 
	 * @param board the 2d int array representing the Sudoku board. Zeros indicate unfilled cells.
	 * @return the solved Sudoku board
	 */
	public int[][] solve(int[][] board) {
	
		return board;
	}
	
	public class Cell {
		private Vector<Integer> domain;
		private int location;
		
		public Cell(int location){
			this.location = location;
			for (int i = 1; i<10; i++){
				domain.add(i);
			}
		}
		
		public int getLocation(){
			return this.location;
		}
		
		public Vector<Integer> getDomain(){
			return domain;
		}
		
		public void setDomain(Vector<Integer> domain){
			this.domain = domain;
		}
		
	}
	
	public class Row{
		Vector<Cell> cells = new Vector<Cell>();
		
		public Row(Vector<Cell> cells){
			this.cells = cells;
		}
		
		public boolean checkRow(){
			return false;
		}
	}
	
	public class Square{
		Vector<Cell> cells = new Vector<Cell>();
		
		public Square(Vector<Cell> cells){
			this.cells = cells;
		}
		
		public boolean checkSquare(){
			return false;
		}
	}
}
