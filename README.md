Sudoku game made in Java :D

![Screenshot showing how the game looks](/looks.png)

You may use the number keys on your keyboard to fill in the cells.

## Building and running

Run these on the command line:

```console
mvn package
java -jar ./target/SudokuGUI-<current version>.jar
```

Alternatively, if the second command doesn't work, try this:

```console
java -cp ./target/SudokuGUI-<current version>.jar com.mauro.sudoku.App
```

Double clicking the JAR file should also work.

The current version can be found within the `<version>` tag in the POM file.