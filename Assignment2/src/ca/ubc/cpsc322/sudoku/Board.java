package ca.ubc.cpsc322.sudoku;

public class Board {
	// 2D byte array organized as array[row][column]
	private byte[][] array;
	
	public Board() {
		array = new byte[9][9];
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		for (byte[] row : array) {
			for (byte value : row)
				builder.append("[" + value + "] ");
			builder.append("\n");
		}
		return builder.toString();
	}
	
}
