package ca.ubc.cpsc322.sudoku;

/**
 * Place for your code.
 */
public class SudokuSolver {
	private Cell[][] domains = new Cell[9][9];
	/**
	 * @return names of the authors and their student IDs (1 per line).
	 */
	public String authors() {
		return "Stephen Kidson - #15345077\n\rJeff Payan - #18618074";
	}

	/**
	 * Performs constraint satisfaction on the given Sudoku board using Arc Consistency and Domain Splitting.
	 * 
	 * @param board the 2d int array representing the Sudoku board. Zeros indicate unfilled cells.
	 * @return the solved Sudoku board
	 */
	public int[][] solve(int[][] board) {
		init(board);
		
		
		
		return board;
	}
			
	private void init(int[][] board) {
		for (int x = 0; x < board.length; x++)
			for (int y = 0; y < board.length; y++)
				domains[x][y] = new Cell(board[x][y]);
	}
	
	/**
	 * Returns whether the suggested value is valid for the cell located at (x,y) 
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean validate(int value, int x, int y) {
		// Check that the 3x3 square this cell is located in does not already contain this value
		
		
		// Check that this cell's row does not already contain this value
		for (int i = 0; i < 9; i++) {
			if (i == x)
				continue;
			if (domains[i][y].isComplete() && domains[i][y].getValue() == value)
				return false;
		}
		// Check that this cell's column does not already contain this value
		for (int i = 0; i < 9; i++) {
			if (i == y)
				continue;
			if (domains[x][i].isComplete() && domains[x][i].getValue() == value)
				return false;
		}
		return true;
	}
	
	public class Cell {
		boolean[] domain = { true, true, true, true, true, true, true, true, true };
		
		public Cell(int value) {
			this.setValue(value);
		}
		
		/**
		 * Removes value from this cell's possible domain. Returns true if domain only has one result, else false.
		 * @param value value to remove from this cell's domain.
		 */
		public void remove(int value) {
			if (!isComplete())
				domain[value-1] = false;
		}
		
		public boolean isComplete() {
			int count = 0;
			for (boolean valid : domain)
				if (valid)
					count++;
			if (count == 1)
				return true;
			return false;
		}
		
		public void setValue(int value) {
			for (int i = 0; i < domain.length; i++) {
				if (!(i == (value-1)))
					domain[i] = false;
			}
		}
		
		public int getValue() {
			int value = 0;
			for (int i = 0; i < domain.length; i++) {
				if (domain[i])
					value = i;
			}
			return value+1;
		}
		
	}
	
}
