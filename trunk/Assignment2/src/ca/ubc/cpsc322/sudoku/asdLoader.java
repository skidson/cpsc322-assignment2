package ca.ubc.cpsc322.sudoku;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Sudoku Solver with Arc Consistency and Domain Splitting
 * @author James Li
 * 
 * http://www.function13.net
 *
 */
public class asdLoader
{
	static final int BOARD_SIZE = 9;
	static int[][] board;
	static asd solver;
	
	public static void main (String[] args)
	{
		if (args.length < 1)
		{
			System.out.println("Usage:");
			System.out.println("    SudokuSolver (filename)");
			System.exit(1);
		}
		
		try
		{
			board = makeBoard(args[0]);
		}
		catch (IOException e)
		{
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
		printBoard(board);
		
		System.out.println("Solving...\n");
		
		solver = new asd();
		
		try
		{
			board = solver.solve(board);
			printBoard(board);
			System.out.println("Done! :)");

		}
		catch (Exception e)
		{
			System.out.println("Warning: " + e.getMessage());
		}

		
	}
	
	/**
	 * Reads in a file and creates a 2d array
	 * @param filename The file name of the sudoku board
	 */
	public static int[][] makeBoard(String filename) throws IOException
	{
		int[][] board = new int[BOARD_SIZE][BOARD_SIZE];
		String[] rowStr;
		
		BufferedReader reader = new BufferedReader( new FileReader(filename));

		for (int row = 0; row < BOARD_SIZE ; row++)
		{
			String str = reader.readLine();
			rowStr = str.split(",\\s*");
			for (int i = 0 ; i < rowStr.length ; i++)
			{
				board[row][i] = Integer.parseInt(rowStr[i]);
			}
		}
		
		return board;
	}
	
	/**
	 * Prints the board to the screen in a pretty format.
	 * @param board
	 */
	public static void printBoard(int[][] board)
	{
		
		for (int i = 0 ; i < BOARD_SIZE ; i++)
		{
			if (i % 3 == 0)
			{
				System.out.println("+-------+-------+-------+");
			}
			
			for (int j = 0 ; j < BOARD_SIZE ; j++)
			{
				if (j % 3 == 0)
				{
					System.out.print("| ");
				}
				System.out.print(board[i][j] + " ");
			}
			System.out.println("|");
		}
		
		System.out.println("+-------+-------+-------+\n");
	}
}

