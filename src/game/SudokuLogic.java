package game;

public class SudokuLogic {
    private static final int ROWS_COLS = 27;
    private static final int SMALL_ROWS_COLS = 9;

    private int[][] gameGrid;
    private int[][] smallAnswersGrid;
    private boolean[][] definitiveAnswersGrid;
    private boolean playerIsTakingNotes, disabled;

    public SudokuLogic() {
        playerIsTakingNotes = disabled = false;
        smallAnswersGrid = new int[SMALL_ROWS_COLS][SMALL_ROWS_COLS];
        gameGrid = new int[ROWS_COLS][ROWS_COLS];
        definitiveAnswersGrid = new boolean[ROWS_COLS][ROWS_COLS];
    }

    public boolean disable() {
        return disabled = true;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public boolean toggleNoteTaking() { 
        return playerIsTakingNotes = !playerIsTakingNotes;
    }

    public boolean isTakingNotes() {
        return playerIsTakingNotes;
    }

    public boolean hasDefinitiveAnswerIn(int i, int j) {
        if (i < 0 || i >= ROWS_COLS || j < 0 || j >= ROWS_COLS) {
            return false;
        }
        return definitiveAnswersGrid[i][j];
    }

    // i and j may go all the way up to 26
    public int getValueIn(int i, int j) {
        if (i < 0 || i >= ROWS_COLS || j < 0 || j >= ROWS_COLS) {
            return -1;
        }
        return gameGrid[i][j];
    }

    public boolean put(int val, int i, int j) {
        if (disabled) {
            return false;
        }
        if (!validIndex(i, j) || val < 1 || val > 9) {
            return false;
        }
        if (!playerIsTakingNotes) {
            smallAnswersGrid[i][j] = val;
        }
        return putAnswerOrNote(val, i * 3, j * 3);
    }

    public boolean delete(int i, int j) {
        if (disabled) {
            return false;
        }
        if (!validIndex(i, j) || playerIsTakingNotes) {
            return false;
        }
        smallAnswersGrid[i][j] = 0;
        return deleteAnswer(i * 3, j * 3);
    }

    public boolean deleteNote(int val, int i, int j) {
        if (disabled) {
            return false;
        }
        if (!validIndex(i, j) || !playerIsTakingNotes || val < 1 || val > 9) {
            return false;
        }
        return deleteNoteAt(val, i * 3, j * 3);
    }

    public boolean wrongDigitExistsIn(int i, int j) {
        return wrongDigitInRow(i) || wrongDigitInCol(j) || wrongDigitInGroup(i, j);
    }

    public boolean isValidSudoku() {
        for (int i = 0; i < SMALL_ROWS_COLS; i++) {
            if (wrongDigitInCol(i) || wrongDigitInRow(i)) {
                return false;
            }
        }
        return !wrongDigitInGroups();
    }

    public boolean hasEmptyCells() {
        for (int arr[] : smallAnswersGrid) {
            for (int val : arr) {
                if (val == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean wrongDigitInRow(int i) {
        boolean check[] = new boolean[9];
        for (int j = 0; j < SMALL_ROWS_COLS; j++) {
            if (smallAnswersGrid[i][j] == 0) {
                continue;
            }
            if (check[smallAnswersGrid[i][j] - 1]) {
                return true;
            }
            check[smallAnswersGrid[i][j] - 1] = true;
        }
        return false;
    }

    private boolean wrongDigitInCol(int j) {
        boolean check[] = new boolean[9];
        for (int i = 0; i < SMALL_ROWS_COLS; i++) {
            if (smallAnswersGrid[i][j] == 0) {
                continue;
            }
            if (check[smallAnswersGrid[i][j] - 1]) {
                return true;
            }
            check[smallAnswersGrid[i][j] - 1] = true;
        }
        return false;
    }

    private boolean wrongDigitInGroup(int i, int j) {
        boolean check[] = new boolean[9];
        i = (i / 3) * 3;
        j = (j / 3) * 3;
        for (int k = i; k < i + 3; k++) {
            for (int l = j; l < j + 3; l++) {
                if (smallAnswersGrid[k][l] == 0) {
                    continue;
                }
                if (check[smallAnswersGrid[k][l] - 1]) {
                    return true;
                }
                check[smallAnswersGrid[k][l] - 1] = true;
            }
        }
        return false;
    }

    private boolean wrongDigitInGroups() {
        boolean[] check = new boolean[9];
        for (int i = 0; i < 27; i += 3) {
            for (int j = 0; j < 9; j++) {
                int auxi = 3 * (i / 9) + (j / 3);               //yes
                int auxj = (i % 9) + (j % 3);                   //yes too
                if (smallAnswersGrid[auxi][auxj] == 0) {
                    continue;
                }
                if (check[smallAnswersGrid[auxi][auxj] - 1]) {
                    return true;
                }
                check[smallAnswersGrid[auxi][auxj] - 1] = true;
            }
            for (int k = 0; k < 9; k++) {
                check[k] = false;
            }
        }
        return false;
    }

    private boolean validIndex(int i, int j) {
        return i >= 0 && i < SMALL_ROWS_COLS
            && j >= 0 && j < SMALL_ROWS_COLS;
    }

    private boolean putAnswerOrNote(int val, int i, int j) {
        if (playerIsTakingNotes) {
            if (definitiveAnswersGrid[i][j]) {
                return false;
            }
            gameGrid[i + (val - 1) / 3][j + (val - 1) % 3] = val;
            return true;
        }
        for (int k = i; k < i + 3; k++) {
            for (int l = j; l < j + 3; l++) {
                gameGrid[k][l] = val;
                definitiveAnswersGrid[k][l] = true;
            }
        }
        return true;
    }

    private boolean deleteAnswer(int i, int j) {
        if (playerIsTakingNotes) {
            return false;
        }
        for (int k = i; k < i + 3; k++) {
            for (int l = j; l < j + 3; l++) {
                gameGrid[k][l] = 0;
                definitiveAnswersGrid[k][l] = false;
            }
        }
        return true;
    }

    private boolean deleteNoteAt(int val, int i, int j) {
        int row = i + (val - 1) / 3;
        int col = j + (val - 1) % 3;
        if (!playerIsTakingNotes || definitiveAnswersGrid[row][col]) {
            return false;
        }
        gameGrid[row][col] = 0;
        return true;
    }

    public String smallGridString() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < SMALL_ROWS_COLS; i++) {
            if (i != 0) {
                if (i % 3 == 0) {
                    str.append("\n");
                }
            }
            for (int j = 0; j < SMALL_ROWS_COLS; j++) {
                if (j != 0) {
                    if (j % 3 == 0) {
                        str.append(" ");
                    }
                }
                str.append(smallAnswersGrid[i][j]);
            } 
            str.append("\n");
        }
        return str.toString();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < ROWS_COLS; i++) {
            if (i != 0) {
                if (i % 9 == 0) {
                    str.append("\n\n\n");
                } else if (i % 3 == 0) {
                    str.append("\n");
                }
            }
            for (int j = 0; j < ROWS_COLS; j++) {
                if (j != 0) {
                    if (j % 9 == 0) {
                        str.append("   ");
                    } else if (j % 3 == 0) {
                        str.append(" ");
                    }
                }
                str.append(gameGrid[i][j]);
            } 
            str.append("\n");
        }
        return str.toString();
    }
}
