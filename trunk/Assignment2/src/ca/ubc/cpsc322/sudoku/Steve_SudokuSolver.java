package ca.ubc.cpsc322.sudoku;

/**
 * Place for your code.
 */
public class Steve_SudokuSolver {
	private Cell[][] domains = new Cell[9][9];
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
	public int[][] solve(int[][] board) {
		init(board);
		
		while(!isFinished())
			for (int x = 0; x < 9; x++)
				for (int y = 0; y < 9; y++)
					probe(x, y);
		
		for (int x = 0; x < 9; x++)
			for (int y = 0; y < 9; y++)
				board[x][y] = domains[x][y].getValue();
		
		
		return board;
	}
			
	private void init(int[][] board) {
		for (int y = 0; y < 9; y++)
			for (int x = 0; x < 9; x++)
				domains[x][y] = new Cell(board[x][y]);
//		for (int i = 0; i < 9; i++) {
//			System.out.println();
//			for (int j = 0; j < 9; j++)
//				System.out.print(domains[j][i].toString() + "\t\t\t");
//		}
	}
	
	/**
	 * Reduces this cell's domain, then others potentially affected.
	 * @param x
	 * @param y
	 */
	private void probe(int x, int y) {
		if (domains[x][y].isComplete())
			return;
		
		int x_rel = x/3, y_rel = y/3;
		boolean solved = false;
		
		for (int i = 0; i < 9; i++)
			solved = domains[x][y].remove(domains[i][y].getValue());
		for (int i = 0; i < 9; i++)
			solved = domains[x][y].remove(domains[x][i].getValue()) | solved;
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				if ((i != x) || (j != y))
					solved = domains[x][y].remove(domains[3*x_rel + i][3*y_rel + j].getValue()) | solved;
		
		// In sudoku, other cells are not affected unless this one has reached a solution
		// If this cell has been solved, check affected cells
//		if (solved) {
//			for (int i = 0; i < 9; i++)
//				if (i != x)
//					probe(i, y);
//			for (int i = 0; i < 9; i++)
//				if (i != y)
//					probe(x, i);
//			for (int i = 0; i < 3; i++)
//				for (int j = 0; j < 3; j++)
//					if ((i != x) || (j != y))
//						probe(3*x_rel + i, 3*y_rel + j);
//			System.out.println(this.toString()); // debug
//		}
	}
	
	/**
	 * Returns whether the suggested value is valid for the cell located at (x,y) 
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean validate(int value, int x, int y) {
		// Check that this cell's row does not already contain this value
		for (int i = 0; i < 9; i++)
			if (i != x && domains[i][y].getValue() == value)
				return false;
		// Check that this cell's column does not already contain this value
		for (int i = 0; i < 9; i++)
			if (i != y && domains[x][i].getValue() == value)
				return false;
		// Check that the 3x3 square this cell is located in does not already contain this value
		// Note that we do not have to check the squares in the same row or column as this has already been done
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				if (((i != x) || (j != y)) &&
					domains[x/3 + i][y/3 + j].getValue() == value)
					return false;
		return true;
	}
	
	
	private boolean isFinished() {
		for (Cell[] row : domains)
			for (Cell cell : row)
				if (!cell.isComplete())
					return false;
		return true;
	}
	
	public String toString() {
		int[][] board = new int[9][9];
		for (int x = 0; x < 9; x++)
			for (int y = 0; y < 9; y++)
				board[x][y] = domains[x][y].getValue();
		return ("\n" + SudokuUtil.formatBoard(board));
	}
	
	/* ************************ EMBEDDED CLASSES ************************ */
	
	public class Cell {
		// Represents the cell's domain where the array's index+1 maps to the cell's sudoku value.
		boolean[] domain = { true, true, true, true, true, true, true, true, true };
		
		public Cell(int value) {
			if (value != 0)
				this.setValue(value);
		}
		
		/**
		 * Removes the specified value from this cell's domain. Returns true if this results in the cell
		 * being complete, false otherwise.
		 * @param value
		 * @return A boolean indicating if this cell is now solved.
		 */
		public boolean remove(int value) {
			if (!isComplete()) {
				try {
					domain[value-1] = false;
				} catch (IndexOutOfBoundsException ignore) {}
				if (isComplete()) {
					for (int i = 0; i < domain.length; i++) {
						if (domain[i]) {
							setValue(i+1);
							return true;
						}
					}
				}
			} else
				return true;
			return false;
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
		
		public int getValue() {
			int value = 0;
			for (int i = 0; i < domain.length; i++)
				if (domain[i])
					value = i;
			return value+1;
		}
		
		public boolean isEmpty() {
			boolean empty = true;
			for (boolean valid : domain)
				if (valid)
					empty = false;
			return empty;
		}
		
		public boolean couldBe(int value) {
			return(domain[value-1]);
		}
		
		public void setValue(int value) {
			for (int i = 0; i < domain.length; i++) {
				if (i == (value-1))
					domain[i] = true;
				else
					domain[i] = false;
			}
		}
		
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("{ ");
			for (int i = 0; i < domain.length; i++)
				if (domain[i])
					builder.append(i+1 + " ");
			builder.append("}");
			return builder.toString();
		}
		
	}
	
}
