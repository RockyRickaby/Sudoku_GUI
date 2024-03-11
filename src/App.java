import javax.swing.SwingUtilities;

import game.SudokuGUI;

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