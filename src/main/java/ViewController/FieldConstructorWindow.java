package ViewController;

import Model.Model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import static IO.IO.getFileAsIOStream;

public class FieldConstructorWindow extends JPanel implements Runnable {
    Image cursor;
    Model field;
    int blockSize = 31;
    int camOffsetX = 20;
    int camOffsetY = 20;
    int cursorX;
    int cursorY;
    boolean chosenAxis;
    int chosenShipSize;
    boolean isRunning;
    Thread thread;
    ImageIcon shiBlockIcon;
    ImageIcon fieldBackgroundIcon;
    ImageIcon fourHorizontalIcon;
    ImageIcon fourVerticalIcon;
    ImageIcon threeHorizontalIcon;
    ImageIcon threeVerticalIcon;
    ImageIcon twoHorizontalIcon;
    ImageIcon twoVerticalIcon;
    ImageIcon oneIcon;
    ImageIcon deleteIcon;
    ImageIcon rulerIcon;
    ImageIcon fourVerticalButtonIcon;
    ImageIcon threeVerticalButtonIcon;
    ImageIcon twoVerticalButtonIcon;
    ImageIcon fourHorizontalButtonIcon;
    ImageIcon threeHorizontalButtonIcon;
    ImageIcon twoHorizontalButtonIcon;
    ImageIcon oneButtonIcon;
    ImageIcon eraseButtonIcon;
    Image shipBlock;
    Image fieldBackground;
    Image fourHorizontal;
    Image fourVertical;
    Image threeHorizontal;
    Image threeVertical;
    Image twoHorizontal;
    Image twoVertical;
    Image one;
    Image delete;
    Image ruler;

    protected boolean spaceStatus = false;
    protected boolean qStatus = false;
    protected boolean enterStatus = false;
    char chosenBlock;
    int chosenBlockIndex = 0;
    int colorSchemeIndex = 0;

    public FieldConstructorWindow() throws IOException {
        initTextures();
        ShipsPanel shipsPanel = new ShipsPanel();
        this.field = new Model(10);
        chosenBlock = Info.blocks[chosenBlockIndex];
        MyKeyboardListener keyboardListener = new MyKeyboardListener();
        this.addKeyListener(keyboardListener);
        MyMouseListener ml = new MyMouseListener();
        this.setLayout(new BorderLayout());
        this.addMouseListener(ml);
        this.addMouseMotionListener(ml);
        this.add(shipsPanel, BorderLayout.EAST);

        setFocusable(true);
        start();
    }

    public FieldConstructorWindow(int colorSchemeIndex) throws IOException {
        this.colorSchemeIndex = colorSchemeIndex;
        initTextures();
        ShipsPanel shipsPanel = new ShipsPanel();
        this.field = new Model(10);
        chosenBlock = Info.blocks[chosenBlockIndex];
        MyKeyboardListener keyboardListener = new MyKeyboardListener();
        this.addKeyListener(keyboardListener);
        MyMouseListener ml = new MyMouseListener();
        this.setLayout(new BorderLayout());
        this.addMouseListener(ml);
        this.addMouseMotionListener(ml);
        this.add(shipsPanel, BorderLayout.EAST);

        setFocusable(true);
        start();
    }

    private void start() {
        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    public void run() {

        long start;
        while ((isRunning)) {
            chosenBlock = Info.blocks[chosenBlockIndex];
            if (chosenBlock == '0') {
                cursor = fourHorizontal;
                chosenAxis = true;
                chosenShipSize = 4;
            }
            if (chosenBlock == '1') {
                cursor = threeHorizontal;
                chosenAxis = true;
                chosenShipSize = 3;
            }
            if (chosenBlock == '2') {
                cursor = twoHorizontal;
                chosenAxis = true;
                chosenShipSize = 2;
            }
            if (chosenBlock == '3') {
                cursor = one;
                chosenAxis = true;
                chosenShipSize = 1;
            }
            if (chosenBlock == '4') {
                cursor = fourVertical;
                chosenAxis = false;
                chosenShipSize = 4;
            }
            if (chosenBlock == '5') {
                cursor = threeVertical;
                chosenAxis = false;
                chosenShipSize = 3;
            }
            if (chosenBlock == '6') {
                cursor = twoVertical;
                chosenAxis = false;
                chosenShipSize = 2;
            }
            if (chosenBlock == field.getEmpty()) {
                cursor = delete;
                chosenShipSize = -1;
            }
            repaint();
            start = System.currentTimeMillis();
            long waitingTime = 0;
            while (waitingTime < 20) {
                repaint();
                waitingTime = System.currentTimeMillis() - start;
            }
            if (field.isFieldReady()) break;
        }
    }

    class MyKeyboardListener extends KeyAdapter {

        public void keyPressed(KeyEvent e) {
            int k = e.getKeyCode();
            if (k == KeyEvent.VK_ENTER) {
                enterStatus = true;
            }
            if (k == KeyEvent.VK_Q) {
                qStatus = true;
                chosenBlockIndex = (chosenBlockIndex + 1) % (Info.blocks.length);
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
                if (field.get(j, i) == '1')
                    g.drawImage(shipBlock, j * blockSize + camOffsetX, i * blockSize + camOffsetY, blockSize, blockSize, null);

                if ((field.get(j, i) == '2') || (field.get(j, i) == '3'))
                    g.drawImage(shipBlock, j * blockSize + camOffsetX, i * blockSize + camOffsetY, blockSize, blockSize, null);

                if ((field.get(j, i) == '4') || (field.get(j, i) == '5') || (field.get(j, i) == '6'))
                    g.drawImage(shipBlock, j * blockSize + camOffsetX, i * blockSize + camOffsetY, blockSize, blockSize, null);

                if ((field.get(j, i) == '7') || (field.get(j, i) == '8') || (field.get(j, i) == '9') || (field.get(j, i) == '0'))
                    g.drawImage(shipBlock, j * blockSize + camOffsetX, i * blockSize + camOffsetY, blockSize, blockSize, null);

                if (field.get(j, i) == field.getEmpty())
                    g.drawImage(fieldBackground, j * blockSize + camOffsetX, i * blockSize + camOffsetY, blockSize, blockSize, null);
            }
        }
        g.drawImage(cursor, cursorX, cursorY, null);
    }

    public void drawBackground(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    public void drawRuler(Graphics g) {
        g.drawImage(ruler, 0, 0, getWidth() / 2, getHeight(), null);
    }

    public Model getField() {
        return field;
    }


    class MyMouseListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            int i = x / blockSize - camOffsetX / blockSize;
            int j = y / blockSize - camOffsetY / blockSize;

            if (chosenShipSize != -1) {
                field.put(i, j, chosenShipSize, chosenAxis);
            } else {
                field.remove(i, j);
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

    public class ShipsPanel extends JPanel {
        public ShipsPanel() {
            JPanel BlockPanel = new JPanel();

            JButton fourVertical = new JButton();
            fourVertical.setIcon(fourVerticalButtonIcon);
            fourVertical.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    chosenBlockIndex = 4;
                }
            });
            JButton fourHorizontal = new JButton();
            fourHorizontal.setIcon(fourHorizontalButtonIcon);
            fourHorizontal.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    chosenBlockIndex = 0;
                }
            });
            JButton threeVertical = new JButton();
            threeVertical.setIcon(threeVerticalButtonIcon);
            threeVertical.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    chosenBlockIndex = 5;
                }
            });
            JButton threeHorizontal = new JButton();
            threeHorizontal.setIcon(threeHorizontalButtonIcon);
            threeHorizontal.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    chosenBlockIndex = 1;
                }
            });
            JButton twoVertical = new JButton();
            twoVertical.setIcon(twoVerticalButtonIcon);
            twoVertical.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    chosenBlockIndex = 6;
                }
            });
            JButton twoHorizontal = new JButton();
            twoHorizontal.setIcon(twoHorizontalButtonIcon);
            twoHorizontal.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    chosenBlockIndex = 2;
                }
            });
            JButton one = new JButton();
            one.setIcon(oneButtonIcon);
            one.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    chosenBlockIndex = 3;
                }
            });
            JButton erase = new JButton();
            erase.setIcon(eraseButtonIcon);
            erase.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    chosenBlockIndex = Info.blocks.length - 1;
                }
            });

            BlockPanel.add(fourVertical);
            BlockPanel.add(fourHorizontal);
            BlockPanel.add(threeVertical);
            BlockPanel.add(threeHorizontal);
            BlockPanel.add(twoVertical);
            BlockPanel.add(twoHorizontal);
            BlockPanel.add(one);
            BlockPanel.add(erase);

            GridLayout gl = new GridLayout(5, 2, 30, 30);
            BlockPanel.setLayout(gl);
            BlockPanel.setFocusable(false);
            add(BlockPanel);
        }
    }

    private void initTextures() throws IOException {
        String path = "textures/skin_" + colorSchemeIndex + '/';

        String shipBlockIconString = path + "shipBlock.png";
        String fieldBackgroundIconString = path + "fieldBackround.png";
        String fourHorizontalIconString = path + "fourHorizontal.png";
        String fourVerticalIconString = path + "fourVertical.png";
        String threeHorizontalIconString = path + "threeHorizontal.png";
        String threeVerticalIconString = path + "threeVertical.png";
        String twoHorizontalIconString = path + "twoHorizontal.png";
        String twoVerticalIconString = path + "twoVertical.png";
        String oneIconString = path + "one.png";
        String deleteIconString = path + "deleteIcon.png";
        String rulerIconString = path + "ruler.png";

        shiBlockIcon = new ImageIcon(getFileAsIOStream(shipBlockIconString).readAllBytes());
        fieldBackgroundIcon = new ImageIcon(getFileAsIOStream(fieldBackgroundIconString).readAllBytes());
        fourHorizontalIcon = new ImageIcon(getFileAsIOStream(fourHorizontalIconString).readAllBytes());
        fourVerticalIcon = new ImageIcon(getFileAsIOStream(fourVerticalIconString).readAllBytes());
        threeHorizontalIcon = new ImageIcon(getFileAsIOStream(threeHorizontalIconString).readAllBytes());
        threeVerticalIcon = new ImageIcon(getFileAsIOStream(threeVerticalIconString).readAllBytes());
        twoHorizontalIcon = new ImageIcon(getFileAsIOStream(twoHorizontalIconString).readAllBytes());
        twoVerticalIcon = new ImageIcon(getFileAsIOStream(twoVerticalIconString).readAllBytes());
        oneIcon = new ImageIcon(getFileAsIOStream(oneIconString).readAllBytes());
        deleteIcon = new ImageIcon(getFileAsIOStream(deleteIconString).readAllBytes());
        rulerIcon = new ImageIcon(getFileAsIOStream(rulerIconString).readAllBytes());


        fourVerticalButtonIcon = Info.fourVButtonIcon;
        threeVerticalButtonIcon = Info.threeVButtonIcon;
        twoVerticalButtonIcon = Info.twoVButtonIcon;
        fourHorizontalButtonIcon = Info.fourHButtonIcon;
        threeHorizontalButtonIcon = Info.threeHButtonIcon;
        twoHorizontalButtonIcon = Info.twoHButtonIcon;
        oneButtonIcon = Info.oneButtonIcon;
        eraseButtonIcon = Info.eraseButtonIcon;
        shipBlock = shiBlockIcon.getImage();
        fieldBackground = fieldBackgroundIcon.getImage();
        fourHorizontal = fourHorizontalIcon.getImage();
        fourVertical = fourVerticalIcon.getImage();
        threeHorizontal = threeHorizontalIcon.getImage();
        threeVertical = threeVerticalIcon.getImage();
        twoHorizontal = twoHorizontalIcon.getImage();
        twoVertical = twoVerticalIcon.getImage();
        one = oneIcon.getImage();
        delete = deleteIcon.getImage();
        ruler = rulerIcon.getImage();
    }
}
