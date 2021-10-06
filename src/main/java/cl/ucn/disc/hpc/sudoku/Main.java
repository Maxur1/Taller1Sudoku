package cl.ucn.disc.hpc.sudoku;

import ch.qos.logback.core.util.ExecutorServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * The main class.
 * @author marcelo Soto Faguett
 * no cree ningun error catcher porque termine muy tarde como para agregarlos y entregar a tiempo
 * si es parte de su evaluacion testear posibles problemas con el .txt le confirmo de adelanto que crasheara
 * el programa, pero mientras se de la informacion correcta funciona perfecto
 */
@Slf4j
public class Main {
    public static boolean isSafe(int[][] board,
                                 int row, int col,
                                 int num)
    {
        // Row has the unique (row-clash)
        for (int d = 0; d < board.length; d++)
        {

            // Check if the number we are trying to
            // place is already present in
            // that row, return false;
            if (board[row][d] == num) {
                return false;
            }
        }

        // Column has the unique numbers (column-clash)
        for (int r = 0; r < board.length; r++)
        {

            // Check if the number
            // we are trying to
            // place is already present in
            // that column, return false;
            if (board[r][col] == num)
            {
                return false;
            }
        }

        // Corresponding square has
        // unique number (box-clash)
        int sqrt = (int)Math.sqrt(board.length);
        int boxRowStart = row - row % sqrt;
        int boxColStart = col - col % sqrt;

        for (int r = boxRowStart;
             r < boxRowStart + sqrt; r++)
        {
            for (int d = boxColStart;
                 d < boxColStart + sqrt; d++)
            {
                if (board[r][d] == num)
                {
                    return false;
                }
            }
        }

        // if there is no clash, it's safe
        return true;
    }

    public static boolean solveSudoku(
            int[][] board, int n)
    {
        int row = -1;
        int col = -1;
        boolean isEmpty = true;
        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < n; j++)
            {
                if (board[i][j] == 0)
                {
                    row = i;
                    col = j;

                    // We still have some remaining
                    // missing values in Sudoku
                    isEmpty = false;
                    break;
                }
            }
            if (!isEmpty) {
                break;
            }
        }

        // No empty space left
        if (isEmpty)
        {
            return true;
        }

        // Else for each-row backtrack
        for (int num = 1; num <= n; num++)
        {
            if (isSafe(board, row, col, num))
            {
                board[row][col] = num;
                if (solveSudoku(board, n))
                {
                    // print(board, n);
                    return true;
                }
                else
                {
                    // replace it
                    board[row][col] = 0;
                }
            }
        }
        return false;
    }

    public static void print(
            int[][] board, int N)
    {

        // We got the answer, just print it
        for (int r = 0; r < N; r++)
        {
            for (int d = 0; d < N; d++)
            {

                if(board[r][d]<10){
                    System.out.print(0);
                }
                System.out.print(board[r][d]);
                System.out.print(" ");
            }
            System.out.print("\n");

            if ((r + 1) % (int)Math.sqrt(N) == 0)
            {
                System.out.print("");
            }
        }
    }


    // Driver Code
    public static void main(String args[]) throws IOException, InterruptedException {

        File sudoku = new File("Sudoku.txt");
        final int maxcores = Runtime.getRuntime().availableProcessors() + 1;
        final int mincores = 1;
        final int k = maxcores;
        final ExecutorService executor = Executors.newFixedThreadPool(k);
        int[][] board = leersudoku(sudoku);
        int N = board.length;

        executor.submit(() -> {
            try {
                Thread.sleep(5);
                log.debug("thread done!{}",Thread.currentThread().getId());
            } catch (InterruptedException ignored){

            }

        });

        if (solveSudoku(board, N))
        {
            // print solution
            print(board, N);
        }
        else {
            System.out.println("No solution");
        }

        executor.shutdown();
        int maxtime = 5;
        if(executor.awaitTermination(maxtime,TimeUnit.MINUTES)){
            log.info("executor ok");
        }else{
            log.warn("problems with executor time");

        }

    }

    private static int[][] leersudoku(File sudokutext) throws FileNotFoundException {
        Scanner lector = new Scanner(sudokutext);
        String data = lector.nextLine();
        int n = Integer.parseInt(data);
        int[][] sudoku = new int[n][n];
        for (int i = 0; i < n; i++) {
            data = lector.nextLine();
            String[] slots = data.split(" ");
            for (int j = 0; j <n ; j++) {
                sudoku[i][j] = Integer.parseInt(slots[j]);
            }
        }

        lector.close();


        return sudoku;
    }

}




