package game;

import java.util.Random;

public class SudokuGenerator {
    private SudokuGenerator() {
    }

    private static boolean checkedPutIn(int[][] grid, int row, int col, int c) {
        for (int k = 0; k < 9; k++) {
            if (grid[row][k] == c) {
                return false;
            }
            if (grid[k][col] == c) {
                return false;
            }
            if (grid[3 * (row / 3) + k / 3][3 * (col / 3) + k % 3] == c) {
                return false;
            }
        }
        grid[row][col] = c;
        return true;
    }

    private static void fillRegion(int[][] grid, int k, int l) {      
        Random r = new Random();
        for (int i = k; i < 3 + k; i++) {
            for (int j = l; j < 3 + k; j++) {
                int aux = 0;
                do {
                    aux = (int) (r.nextInt(9) + 1);
                }
                while (!checkedPutIn(grid, i, j, aux));
            }
        }
    }   

    private static boolean fill(int[][] grid) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (grid[i][j] != 0) {
                    continue;
                }
                for (int c = 1; c <= 9; c++) {
                    if (!checkedPutIn(grid, i, j, c)) {
                        continue;
                    }
                    if (fill(grid)) {
                        return true;
                    }
                    grid[i][j] = 0;
                }
                return false;
            }
        }
        return true;
    }

    private static void clearNCells(int[][] grid) {
        Random r = new Random();
        int i, j;
        int n = r.nextInt(60 - 32) + 32;
        for (int k = 0; k < n; k++) {
            do {
                i = r.nextInt(9);
                j = r.nextInt(9);
            } while(grid[i][j] == 0);
            grid[i][j] = 0;
        }
     }

    private static void generateSudoku(int[][] grid) {
        for (int i = 0; i < 9; i += 3) {
            fillRegion(grid, i, i);
        }
        fill(grid);
        clearNCells(grid);
    }
    
    public static SudokuLogic generateSudoku() {
        int[][] grid = new int[9][9];
        generateSudoku(grid);
        SudokuLogic sudoku = new SudokuLogic();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                sudoku.put(grid[i][j], i, j);
            }
        } 
        return sudoku;
    }

}
