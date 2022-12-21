package ViewController;

import Model.Model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Arrays;

import static IO.IO.getFileAsIOStream;

public class GameWindow extends JPanel implements Runnable {
    Image cursor;
    Model field;
    int colorSchemeIndex = 0;
    int FPS = 100;

    int blockSize = 30;

    int camOffsetX = 25;
    int camOffsetY = 25;
    int camOffsetXShootingMap = 350 + camOffsetX;
    int camOffsetYShootingMap = 25;
    int cursorX;
    int cursorY;
    int scopeCordX = 0;
    int scopeCordY = 0;
    boolean isRunning;
    boolean isMyTurn;
    Thread thread;
    ImageIcon shiBlockIcon;
    ImageIcon fieldBackgroundIcon;
    ImageIcon deleteIcon;
    ImageIcon missedIcon;
    ImageIcon shotIcon;
    ImageIcon rulerIcon;
    Image missed;
    Image shipBlock;
    Image fieldBackground;
    Image delete;
    Image shot;
    Image ruler;

    protected boolean qStatus = false;
    protected boolean enterStatus = false;
    int[] shootCords = new int[2];
    boolean[][] shootMap;

    JLabel instructionText = new JLabel();
    Dimension instructionTextSize = new Dimension(420, 120);
    Font instructionFont = new Font("Courier New", Font.PLAIN, 12);
    String turnInfoMessage = "";

    public GameWindow() throws IOException {
        initTextures();
        this.field = new Model(10);
        shootMap = new boolean[field.getFieldSize()][field.getFieldSize()];
        MyKeyboardListener keyboardListener = new MyKeyboardListener();
        this.addKeyListener(keyboardListener);
        MyMouseListener ml = new MyMouseListener();
        this.setLayout(new BorderLayout());
        this.addMouseListener(ml);
        this.addMouseMotionListener(ml);
        instructionText.setVerticalAlignment(JLabel.CENTER);
        instructionText.setHorizontalAlignment(JLabel.CENTER);
        instructionText.setPreferredSize(instructionTextSize);
        instructionText.setFont(instructionFont);
        this.add(instructionText);
        setFocusable(true);
        start();
    }

    public GameWindow(int colorSchemeIndex) throws IOException {

        this.colorSchemeIndex = colorSchemeIndex;
        initTextures();
        this.field = new Model(10);
        shootMap = new boolean[field.getFieldSize()][field.getFieldSize()];
        MyKeyboardListener keyboardListener = new MyKeyboardListener();
        this.addKeyListener(keyboardListener);
        MyMouseListener ml = new MyMouseListener();
        this.setLayout(new BorderLayout());
        this.addMouseListener(ml);
        this.addMouseMotionListener(ml);
        instructionText.setVerticalAlignment(JLabel.CENTER);
        instructionText.setHorizontalAlignment(JLabel.CENTER);
        instructionText.setPreferredSize(instructionTextSize);
        instructionText.setFont(instructionFont);
        this.add(instructionText);

        setFocusable(true);
        start();
    }

    private void start() {
        for (int i = 0; i < shootMap.length; i++) {
            Arrays.fill(shootMap[i], false);
        }

        Arrays.fill(shootCords, -1);

        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    public void run() {

        long start;
        while ((isRunning)) {
            if (isMyTurn) {
                turnInfoMessage = "Its my turn!";
            } else {
                turnInfoMessage = "Its not my turn...";
            }
            repaint();
            start = System.currentTimeMillis();
            long waitingTime = 0;
            while (waitingTime < 1000 / FPS) {
                repaint();
                waitingTime = System.currentTimeMillis() - start;
            }
        }
    }

    class MyKeyboardListener extends KeyAdapter {

        public void keyPressed(KeyEvent e) {
            int k = e.getKeyCode();
            if (k == KeyEvent.VK_ENTER) {
                enterStatus = true;
                if (isMyTurn) {
                    if (!checkShootMap(scopeCordX, scopeCordY)) {
                        shootCords[0] = scopeCordX;
                        shootCords[1] = scopeCordY;
                        fillShootMap(scopeCordX, scopeCordY);
                        System.out.println("Enter: " + shootCords[0] + ' ' + shootCords[1]);
                    }
                }
            }
        }

        public void keyReleased(KeyEvent e) {
            int k = e.getKeyCode();
            if (k == KeyEvent.VK_ENTER) enterStatus = false;
            if (k == KeyEvent.VK_Q) qStatus = false;
        }

        public void keyTyped(KeyEvent e) {
        }

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawBackground(g);
        drawRuler(g);
        for (int i = 0; i < field.getFieldSize(); i++) {
            for (int j = 0; j < field.getFieldSize(); j++) {
                if ((field.get(j, i) == '1') || (field.get(j, i) == '2') || (field.get(j, i) == '3') || (field.get(j, i) == '4') || (field.get(j, i) == '5') || (field.get(j, i) == '6') || (field.get(j, i) == '7') || (field.get(j, i) == '8') || (field.get(j, i) == '9') || (field.get(j, i) == '0'))
                    g.drawImage(shipBlock, j * blockSize + camOffsetX, i * blockSize + camOffsetY, blockSize, blockSize, null);

                if ((field.get(j, i) == 'A') || (field.get(j, i) == 'B') || (field.get(j, i) == 'C') || (field.get(j, i) == 'D') || (field.get(j, i) == 'E') || (field.get(j, i) == 'F') || (field.get(j, i) == 'G') || (field.get(j, i) == 'H') || (field.get(j, i) == 'I') || (field.get(j, i) == 'J'))
                    g.drawImage(shot, j * blockSize + camOffsetX, i * blockSize + camOffsetY, blockSize, blockSize, null);

                if (field.get(j, i) == field.getMissed())
                    g.drawImage(missed, j * blockSize + camOffsetX, i * blockSize + camOffsetY, blockSize, blockSize, null);

                if (field.get(j, i) == field.getDestroyed())
                    g.drawImage(delete, j * blockSize + camOffsetX, i * blockSize + camOffsetY, blockSize, blockSize, null);


                if (field.get(j, i) == field.getEmpty())
                    g.drawImage(fieldBackground, j * blockSize + camOffsetX, i * blockSize + camOffsetY, blockSize, blockSize, null);

                if (field.getOpponentField(j, i) == field.getDestroyed())
                    g.drawImage(delete, j * blockSize + camOffsetXShootingMap, i * blockSize + camOffsetYShootingMap, blockSize, blockSize, null);
                else if (field.getOpponentField(j, i) == field.getMissed())
                    g.drawImage(missed, j * blockSize + camOffsetXShootingMap, i * blockSize + camOffsetYShootingMap, blockSize, blockSize, null);
                else if ((field.getOpponentField(j, i) == 'A') || (field.getOpponentField(j, i) == 'B') || (field.getOpponentField(j, i) == 'C') || (field.getOpponentField(j, i) == 'D') || (field.getOpponentField(j, i) == 'E') || (field.getOpponentField(j, i) == 'F') || (field.getOpponentField(j, i) == 'G') || (field.getOpponentField(j, i) == 'H') || (field.getOpponentField(j, i) == 'I') || (field.getOpponentField(j, i) == 'J'))
                    g.drawImage(shot, j * blockSize + camOffsetXShootingMap, i * blockSize + camOffsetYShootingMap, blockSize, blockSize, null);
                else
                    g.drawImage(fieldBackground, j * blockSize + camOffsetXShootingMap, i * blockSize + camOffsetYShootingMap, blockSize, blockSize, null);

            }

        }
        if (isMyTurn) {
            g.setColor(Color.RED);
            g.drawRect(scopeCordX * blockSize + camOffsetXShootingMap, scopeCordY * blockSize + camOffsetYShootingMap, blockSize, blockSize);
        }
        g.drawImage(cursor, cursorX, cursorY, null);
    }

    public void drawBackground(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    public void drawRuler(Graphics g) {
        g.drawImage(ruler, 0, 0, getWidth() / 2, getHeight(), null);
        g.drawImage(ruler, getWidth() / 2, 0, getWidth() / 2, getHeight(), null);
    }

    public int[] getShootCords() {
        return shootCords;
    }

    public void updateTurnInfo(boolean isMyTurn) {
        this.isMyTurn = isMyTurn;
    }

    public void setOpponentsField(char[][] opponentsField) {
        this.field.setOpponentField(opponentsField);
    }

    public void updateField(Model field) {
        this.field = field;
    }

    class MyMouseListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            int i = x / blockSize - camOffsetXShootingMap / blockSize;
            int j = y / blockSize - camOffsetYShootingMap / blockSize;

            if (isMyTurn) {
                if ((field.isCordAppropriate(i)) && (field.isCordAppropriate(j))) {
                    if (!checkShootMap(i, j)) {
                        scopeCordX = i;
                        scopeCordY = j;
                    }
                }
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            cursorX = x;
            cursorY = y;
        }

    }

    private boolean checkShootMap(int x, int y) {
        return shootMap[y][x];
    }

    private void fillShootMap(int x, int y) {
        shootMap[y][x] = true;
    }

    private void initTextures() throws IOException {
        String path = "textures/skin_" + colorSchemeIndex + '/';
        String shipBlockIconString = path + "shipBlock.png";
        String fieldBackgroundIconString = path + "fieldBackround.png";
        String deleteIconString = path + "deleteIcon.png";
        String rulerIconString = path + "ruler.png";
        String missedIconString = path + "missed.png";
        String shotIconString = path + "shot.png";

        shiBlockIcon = new ImageIcon(getFileAsIOStream(shipBlockIconString).readAllBytes());
        fieldBackgroundIcon = new ImageIcon(getFileAsIOStream(fieldBackgroundIconString).readAllBytes());
        deleteIcon = new ImageIcon(getFileAsIOStream(deleteIconString).readAllBytes());
        missedIcon = new ImageIcon(getFileAsIOStream(missedIconString).readAllBytes());
        shotIcon = new ImageIcon(getFileAsIOStream(shotIconString).readAllBytes());
        rulerIcon = new ImageIcon(getFileAsIOStream(rulerIconString).readAllBytes());

        shipBlock = shiBlockIcon.getImage();
        fieldBackground = fieldBackgroundIcon.getImage();
        delete = deleteIcon.getImage();
        ruler = rulerIcon.getImage();
        missed = missedIcon.getImage();
        shot = shotIcon.getImage();

    }
}