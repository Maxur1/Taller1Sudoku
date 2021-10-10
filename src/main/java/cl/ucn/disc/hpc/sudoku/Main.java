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
 *
 */
@Slf4j
public class Main {
    //el codigo para revisar si el numero que intentamos usar es valido
    public static boolean isSafe(int[][] board,
                                 int row, int col,
                                 int num)
    {
        // viaja a lo largo de toda la fila
        for (int d = 0; d < board.length; d++)
        {

            // regresa falso si encuentra el numero que intentamos poner
            if (board[row][d] == num) {
                return false;
            }
        }

        // viaja a lo largo de toda la columna
        for (int r = 0; r < board.length; r++)
        {

            // retorna falso si encuentra el numero que intentamos hubicar en la columna
            if (board[r][col] == num)
            {
                return false;
            }
        }

        // revisa si el cuadrado respectivo tiene el numero
        //empiezo consiguiendome las cordenadas del cuadrado usando el largo del sudoku
        int sqrt = (int)Math.sqrt(board.length);
        int boxRowStart = row - row % sqrt;
        int boxColStart = col - col % sqrt;

        for (int r = boxRowStart;
             r < boxRowStart + sqrt; r++)
        {
            for (int d = boxColStart;
                 d < boxColStart + sqrt; d++)
            {
                //otra vez, retorna falso si encuentra el numero
                if (board[r][d] == num)
                {
                    return false;
                }
            }
        }

        // si no encontro el numero significa que es una entrada valida
        return true;
    }

    //resolviendo el sudoku con backtracking
    public static boolean solveSudoku(
            int[][] board, int n)
    {
        int row = -1;
        int col = -1;
        boolean isEmpty = true;
        //reviso toda la matriz para ver si quedan espacios vacios
        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < n; j++)
            {
                if (board[i][j] == 0)
                {
                    row = i;
                    col = j;

                    // el sudoku no esta completado
                    isEmpty = false;
                    break;
                }
            }
            if (!isEmpty) {
                break;
            }
        }

        // si no quedan espacios vacios termino
        if (isEmpty)
        {
            return true;
        }

        // si quedan espacios vacios empiezo a revisarel espacio vacio
        //el siguiente for checkea todos los numeros usables (maximo siendo el largo del sudoku)
        for (int num = 1; num <= n; num++)
        {
            //cada numero se revisa si es que es valido o no dependiendo de los numeros actualmente en el sudoku
            if (isSafe(board, row, col, num))
            {
                board[row][col] = num;
                if (solveSudoku(board, n))
                {
                    // agrego el numero al sudoku en ese lugar (board, n);
                    return true;
                }
                else
                {
                    // si me da problemas vuelvo a vaciar el espacio
                    board[row][col] = 0;
                }
            }
        }
        return false;
    }

    //para facilitarme imprimir el sudoku
    public static void print(
            int[][] board, int N)
    {

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


    // main
    public static void main(String args[]) throws IOException, InterruptedException {

        File sudoku = new File("Sudoku.txt");

        int[][] board = leersudoku(sudoku);
        if(board[0][0] < 0){
            return;
        }
        int N = board.length;

        if(N < 4){
            System.out.print("the number of the sudoku size is not valid");
            System.out.print("it must be a NUMBER bigger than 3 with a square root");
            return;
        }
        if (Math.sqrt(N)% 1 != 0){
            System.out.print("the number of the sudoku size is not valid,");
            System.out.print("it must be a number with a square root");
            return;
        }


        if (solveSudoku(board, N))
        {
            // imprimo solucion
            print(board, N);
        }
        else {
            //no se lleno
            System.out.println("No solution Found");
        }


    }

    //el codigo para leer el archivo texto
    private static int[][] leersudoku(File sudokutext) throws FileNotFoundException {
        Scanner lector = new Scanner(sudokutext);
        String data = lector.nextLine();
        int n;
        try {
            n = Integer.parseInt(data);
        } catch (NumberFormatException e) {
            n = 0;
        }
        int[][] sudoku = new int[n][n];
        try{
            for (int i = 0; i < n; i++) {
                data = lector.nextLine();
                String[] slots = data.split(" ");
                for (int j = 0; j <n ; j++) {
                    sudoku[i][j] = Integer.parseInt(slots[j]);
                }
            }
        } catch (Exception e) {
            System.out.print("there is an error in the sudoku format");
            System.out.print("Basic requirements to check:");
            System.out.print("everything must be numbers");
            System.out.print("the first number must be the length of the sudoku");
            sudoku [0][0] = -2;
        }


        lector.close();


        return sudoku;
    }

}




