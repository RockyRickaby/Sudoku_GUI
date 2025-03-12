package com.mauro.sudoku;

import javax.swing.SwingUtilities;

import com.mauro.sudoku.game.SudokuGUI;

public class App {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SudokuGUI();
            }
        });
    }
}