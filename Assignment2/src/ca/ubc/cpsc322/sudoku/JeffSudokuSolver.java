package ca.ubc.cpsc322.sudoku;

import java.util.ArrayList;
import java.util.List;

import ca.ubc.cpsc322.sudoku.SudokuSolver.Cell;

public class JeffSudokuSolver {
		private Cell[][] cells = new Cell[9][9];
		private List<Arc> TDA = new ArrayList<Arc>();
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
			
//			arcConsistency();
//			fillBoard();
//			checkCompleteness
//			DOMAINSPLIT() ( RECURSIVE)
//			win
				
			
			return board;
		}
				
		private void init(int[][] board) {
			for (int y = 0; y < 9; y++)
				for (int x = 0; x < 9; x++){
					cells[x][y] = new Cell(board[x][y], x, y);
					System.out.println(cells[x][y].toString());
				}
			
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
				if (i != x && cells[i][y].getValue() == value)
					return false;
			// Check that this cell's column does not already contain this value
			for (int i = 0; i < 9; i++)
				if (i != y && cells[x][i].getValue() == value)
					return false;
			// Check that the 3x3 square this cell is located in does not already contain this value
			// Note that we do not have to check the squares in the same row or column as this has already been done
			for (int i = 0; i < 3; i++)
				for (int j = 0; j < 3; j++)
					if (((i != x) || (j != y)) &&
						cells[x/3 + i][y/3 + j].getValue() == value)
						return false;
			return true;
		}
		
		
		private boolean isFinished() {
			for (Cell[] row : cells)
				for (Cell cell : row)
					if (!cell.isComplete())
						return false;
			return true;
		}
		
		public String toString() {
			int[][] board = new int[9][9];
			for (int x = 0; x < 9; x++)
				for (int y = 0; y < 9; y++)
					board[x][y] = cells[x][y].getValue();
			return ("\n" + SudokuUtil.formatBoard(board));
		}
		
		public void createArcs(List<Arc> TDA, int x, int y){
			for(int i = 0; i < 9;i++){
				if(x != i)
					TDA.add(new Arc(cells[x][y], cells[i][x]));
				if(y != i){
					TDA.add(new Arc(cells[x][y], cells[y][i]));
				}
			}
		}

		/* ************************ EMBEDDED CLASSES ************************ */
		
		public class Cell {
			List<Integer> domains = new ArrayList<Integer>();
			int value;
			int[] coord = new int[2];
			
			public Cell(int value, int x, int y) {
				this.value = value;
				coord[0] = x;
				coord[1] = y;
				initDomain();
			}
			
			private void initDomain(){
				if (this.value == 0){
					for(int i = 1; i < 10; i++)
						domains.add(i);
				}else domains.add(value);
			}
			/**
			 * Removes the specified value from this cell's domain. Returns true if this results in the cell
			 * being complete, false otherwise.
			 * @param value
			 * @return A boolean indicating if this cell is now solved.
			 */
			public boolean remove(int value) {
				if (!isComplete()) {
					domains.remove(value);
				} else
					return true;
				return false;
			}
			public boolean isComplete() {
				if(domains.size() == 1) return true;
				else return false;
			}
			
			public void setValue(int value){
				this.value = value;
			}
			
			public List<Integer> getDomain() {
				return domains;
			}
			
			public boolean isEmpty() {
				if(domains.isEmpty()) return true;
				else return false;
			}

			public String toString() {
				StringBuilder builder = new StringBuilder();
				builder.append("{ ");
				for (int i = 0; i < domains.size(); i++)
						builder.append(domains.get(i) + ",");
				builder.append("}");
				return builder.toString();
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