package game;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class SudokuGUI extends JFrame {
    private static final String[] NUMBERS = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private static final Color CURSOR_COLORS[] = {Color.PINK, Color.GREEN.darker()};

    private final Rectangle2D.Double[][] GRID_CELLS = new Rectangle2D.Double[9][9];
    private final Line2D.Double[] GRID_LINES = new Line2D.Double[4];
    private final boolean[][] WRONG_DIGITS = new boolean[9][9];

    private final Rectangle2D.Double CURSOR = new Rectangle2D.Double();
    private final BasicStroke CELLS_STROKE = new BasicStroke(.125F),
                                     GRID_STROKE = new BasicStroke(3);


    // private static final Color DARKER_MAGENTA = Color.MAGENTA.darker();

    private HashMap<String, BufferedImage> gridImages, smallerImages;
    private List<Path> imagePaths;
    private HashMap<String, BufferedImage> imageMap;

    private double gridScaleX, gridScaleY;
    private double subscaleX, subscaleY;
    private double cursorX, cursorY;

    private SudokuLogic sudoku;
    private int cursorColorIdx;
    private int numberToInsert;
    private int insertX, insertY;
    private boolean loadingImagesFirstTime;

    public SudokuGUI() {
        loadingImagesFirstTime = true;

        cursorX = cursorY = -1;
        subscaleX = subscaleY = cursorX = cursorY = 0;
        gridScaleX = gridScaleY = 54;
        insertX = insertY = 0;
        cursorColorIdx = 0;

        GRID_LINES[0] = new Line2D.Double();
        GRID_LINES[1] = new Line2D.Double();
        GRID_LINES[2] = new Line2D.Double();
        GRID_LINES[3] = new Line2D.Double();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                GRID_CELLS[i][j] = new Rectangle2D.Double();
            }
        }

        sudoku = SudokuGenerator.generateSudoku();
        numberToInsert = -1;
        imagePaths = null;
        imageMap = new HashMap<>();
        gridImages = new HashMap<>();
        smallerImages = new HashMap<>();

        setBackground(Color.BLACK);
        setTitle("Sudoku");
        add(mainPanel());
        setJMenuBar(menuBar());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(598, 713);
        setLocationRelativeTo(null);
        setVisible(true);
        System.out.println(getContentPane().getSize());
    }

    private void loadImages() {
        try (Stream<Path> imagesStream = Files.walk(Path.of("src/game/images"))) {
            imagePaths = imagesStream.skip(1).collect(Collectors.toList());
            reloadImages();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void reloadImages() {
        gridImages.clear();
        smallerImages.clear();
        imagePaths.forEach(this::loadImage);
    }

    private void loadImage(Path p) {
        String filePath = p.subpath(p.getNameCount() - 1, p.getNameCount()).toString();
        filePath = filePath.substring(0, filePath.lastIndexOf("."));
        try {
            BufferedImage loadedImage = null;
            if (!imageMap.containsKey(filePath)) { // to reduce IO operations
                loadedImage = ImageIO.read(p.toFile());
                imageMap.put(filePath, loadedImage);
            }
            loadedImage = imageMap.get(filePath);
            
            BufferedImage resizedImage = new BufferedImage((int) gridScaleX, (int) gridScaleY, loadedImage.getType());
            Graphics2D resizer = resizedImage.createGraphics();
            resizer.drawImage(loadedImage, 0, 0, (int) gridScaleX, (int) gridScaleY, null);
            resizer.dispose();

            gridImages.put(filePath, resizedImage);

            resizedImage = new BufferedImage((int) subscaleX, (int) subscaleY, loadedImage.getType());
            resizer = resizedImage.createGraphics();
            resizer.drawImage(loadedImage, 0, 0, (int) subscaleX, (int) subscaleY, null);
            resizer.dispose();

            smallerImages.put(filePath, resizedImage);
        } catch(IOException e) {
            e.printStackTrace();
        } 
    }

    private JMenuBar menuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem item = new JMenuItem("Exit");
        item.addActionListener(e -> System.exit(0));
        menu.add(item);
        menuBar.add(menu);

        menu = new JMenu("Game");

        item = new JMenuItem("Restart");
        item.addActionListener(e -> {
            sudoku = SudokuGenerator.generateSudoku();
            repaint();
        });
        menu.add(item);
        menuBar.add(menu);
        return menuBar;
    }

    private JPanel mainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.BLACK);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridheight = gc.gridwidth = GridBagConstraints.RELATIVE;
        // gc.weightx = .1;
        gc.weighty = .75;
        gc.fill = GridBagConstraints.BOTH;
        gc.anchor = GridBagConstraints.CENTER;
        panel.add(sudokuPanel(), gc);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.gridwidth = 1;
        gc.weightx = .0625;
        gc.weighty = .125;
        gc.fill = GridBagConstraints.BOTH;
        gc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonsPanel(), gc);
        return panel;
        // return sudokuPanel();
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 9));
        JButton button = null;
        for (int i = 0; i < NUMBERS.length; i++) {
            button = new JButton(NUMBERS[i]);
            int insert = i + 1;
            button.addActionListener(e -> {
                numberToInsert = insert;
                sudoku.put(insert, insertY, insertX);
                repaint();
            });
            panel.add(button);
        }
        button = new JButton("Take Note");
        button.addActionListener(e -> {
            if (sudoku.toggleNoteTaking()) {
                cursorColorIdx = 1;
            } else {
                cursorColorIdx = 0;
            }
            repaint();
        });
        panel.add(button);

        button = new JButton("Delete");
        button.addActionListener(e -> {
            if (sudoku.isTakingNotes()) {
                sudoku.deleteNote(numberToInsert, insertY, insertX);
            } else {
                sudoku.delete(insertY, insertX);
            }
            repaint();
        });
        panel.add(button);
        
        button = new JButton("Check");
        button.addActionListener(e -> {
            if (!sudoku.hasEmptyCells() && sudoku.isValidSudoku()) {
                JOptionPane.showInternalMessageDialog(null, "Congratulations!", getTitle(), JOptionPane.INFORMATION_MESSAGE);
                sudoku.disable();
            } else {
                JOptionPane.showInternalMessageDialog(null, "Hmm... something's not right *thinks*", getTitle(), JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(button);
        return panel;
    }

    private JPanel sudokuPanel() {
        JPanel panel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                double width = getWidth();
                double height = getHeight();

                gridScaleX =  width / 9;
                gridScaleY = height / 9;
                subscaleX = width / 27;
                subscaleY = height / 27;
                
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        int val = sudoku.getValueIn(i * 3, j * 3);
                        if (!sudoku.hasDefinitiveAnswerIn(i * 3, j * 3)) {
                            continue;
                        }
                        g2.drawImage(gridImages.get(NUMBERS[val - 1]), null, (int) (j * gridScaleX), (int) (i * gridScaleY));
                    }
                }

                for (int i = 0; i < 27; i++) {
                    for (int j = 0; j < 27; j++) {
                        int val = sudoku.getValueIn(i, j);
                        if (sudoku.hasDefinitiveAnswerIn(i, j) || val == 0) {
                            continue;
                        }
                        g2.drawImage(smallerImages.get(NUMBERS[val - 1]), null, (int) (j * subscaleX), (int) (i * subscaleY));
                    }
                }

                g2.setColor(Color.WHITE);
                g2.setStroke(CELLS_STROKE);

                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        GRID_CELLS[i][j].setRect(i * gridScaleX, j * gridScaleY, gridScaleX, gridScaleY);
                        g2.draw(GRID_CELLS[i][j]);
                    }
                }

                g2.setColor(Color.GRAY);
                g2.setStroke(GRID_STROKE);

                GRID_LINES[0].setLine(gridScaleX * 3, 0, gridScaleX * 3, gridScaleY * 9);
                GRID_LINES[1].setLine(gridScaleX * 6, 0, gridScaleX * 6, gridScaleY * 9);
                GRID_LINES[2].setLine(0, gridScaleY * 3, gridScaleX * 9, gridScaleY * 3);
                GRID_LINES[3].setLine(0, gridScaleY * 6, gridScaleX * 9, gridScaleY * 6);

                g2.draw(GRID_LINES[0]);
                g2.draw(GRID_LINES[1]);
                g2.draw(GRID_LINES[2]);
                g2.draw(GRID_LINES[3]);

                g2.setColor(CURSOR_COLORS[cursorColorIdx]);
                cursorX = insertX * gridScaleX;
                cursorY = insertY * gridScaleY;

                CURSOR.setFrame(cursorX, cursorY, gridScaleX, gridScaleY);
                g2.draw(CURSOR);
            }
        };
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                insertX = (e.getX() / (int) gridScaleX);
                insertY = (e.getY() / (int) gridScaleY);

                repaint();
                panel.grabFocus();
            }  
        });
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keycode = e.getKeyCode();
                switch(keycode) {
                    case KeyEvent.VK_1: numberToInsert = 1; break;
                    case KeyEvent.VK_2: numberToInsert = 2; break;
                    case KeyEvent.VK_3: numberToInsert = 3; break;
                    case KeyEvent.VK_4: numberToInsert = 4; break;
                    case KeyEvent.VK_5: numberToInsert = 5; break;
                    case KeyEvent.VK_6: numberToInsert = 6; break;
                    case KeyEvent.VK_7: numberToInsert = 7; break;
                    case KeyEvent.VK_8: numberToInsert = 8; break;
                    case KeyEvent.VK_9: numberToInsert = 9; break;
                    default: e.consume();
                }
                if (keycode == KeyEvent.VK_DELETE) {
                    if (sudoku.isTakingNotes()) {
                        sudoku.deleteNote(numberToInsert, insertY, insertX);
                    } else {
                        sudoku.delete(insertY, insertX);
                    }
                } else {
                    sudoku.put(numberToInsert, insertY, insertX);
                }
                repaint();
                panel.grabFocus();
            }
        });
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (loadingImagesFirstTime) {
                    loadImages();
                    loadingImagesFirstTime = false;
                }
                reloadImages();
                panel.grabFocus();
            }
        });
        panel.setPreferredSize(new Dimension(9 * (int) gridScaleX, 9 * (int) gridScaleY));
        return panel;
    }
}
