package cl.ucn.disc.hpc.sudoku;

import ch.qos.logback.core.util.ExecutorServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
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
        final int maxCores = Runtime.getRuntime().availableProcessors() + 1;
        final int minCores = 1;

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
        //creo un sudoku clon para guardar copia
        int[][] board2 = new int[N][N];
        clonarsudoku(board,board2);
        final int Repeticiones = 5;
        for (int n = minCores; n <= maxCores; n++) {


            List<Long> times = new ArrayList<>();
            for (int m = 1; m <= Repeticiones; m++) {
                //reseteo el sudoku para que se pueda re-completar
                clonarsudoku(board2,board);
                long time = sudokuMultipleCores(n, board);
                times.add(time);

            }

            long min = Collections.min(times);
            long max = Collections.max(times);

            //Erase two non-characteristic values
            times.remove(min);
            times.remove(max);


            //Get the average with stream magic!
            double average = times.stream().mapToLong((x) -> x).average().getAsDouble();
            log.info("Average time with {} cores: {} nano sec. Max time: {} nano sec. Min time: {} nano sec.", n, average, max, min);

            //Print the board with the solution
            print(board,N);
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

    private static void clonarsudoku(int[][] sudoku1,int[][]sudoku2){
        for (int i = 0; i < sudoku1.length; i++) {
            for (int j = 0; j < sudoku1.length; j++) {
                sudoku2[i][j] = sudoku1[i][j];
            }

        }
        return;
    }
    private static long sudokuMultipleCores(int cores, int[][] board1) throws InterruptedException {

        final ExecutorService executorService = Executors.newFixedThreadPool(cores);


        StopWatch sw = StopWatch.createStarted();
        executorService.submit(() -> {
            if (solveSudoku(board1, board1.length))
            {
                // imprimo solucion
                System.out.println("");
                print(board1, board1.length);
            }
            else {
                //no se lleno
                System.out.println("No solution Found");
            }


        });
        executorService.shutdown();
        long time = sw.getTime(TimeUnit.NANOSECONDS);
        int maxTime = 5;

        if (executorService.awaitTermination(maxTime, TimeUnit.MINUTES)) {
            //log.info("Founded a solution! with a time of {} ms:",time);
        } else {
            log.warn("The executor didn't finish in {} minutes", maxTime);
        }
        return time;


    }

}




