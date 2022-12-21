package ViewController;

import Model.Model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class FMGSClient implements Runnable {
    int colorSchemeIndex = 0;
    int colorSchemesAvailable = 3;
    double waitingTimeMillis = 1000;
    int lowerBoundary = 0;
    int higherBoundary = 1000;

    int windowWidth = 700;
    int windowHeight = 400;

    String hostName;
    int portNumber;
    BufferedReader in;
    BufferedReader stdIn;
    PrintWriter out;
    Socket socket;
    String nickname;
    int fieldSize = 10;
    boolean loginStatus = false;
    boolean isRunning = true;

    Model myField;
    String opponentNickname = null;
    boolean gotShot = false;
    boolean gotResponse = false;
    boolean fieldStatus = false;
    boolean fieldStatusOpponent = false;
    boolean connectionStatus = false;
    int randNumberOpponent = -1;
    int randNumber = -1;
    boolean gotRandomNumberFromOpponent = false;
    boolean opponentLost = false;
    Vector<String> onlinePlayersList = new Vector<>();
    int onlinePlayersNumber = 0;
    int[] lastShootCords = {-1, -1};
    boolean[][] shootMap;
    int shootCounter = 0;
    boolean optionsFlag = false;


    public FMGSClient(String hostName, int portNumber) {
        this.hostName = hostName;
        this.portNumber = portNumber;
        myField = new Model(fieldSize);
    }

    public void run() {
        try {
            socket = new Socket(hostName, portNumber);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            Listener listenerThread = new Listener();
            Sender senderThread = new Sender();
            listenerThread.start();
            senderThread.start();
            senderThread.join();
            System.exit(0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    class Listener extends Thread {
        public void run() {
            while (true) {
                try {
                    String inString = in.readLine();
                    checkOnlinePlayersList(inString);
                    if (!changeNicknameCheck(inString)) {
                        if (!loginStatus) {
                            logInError(inString);
                            checkLoginResponse(inString);
                        } else if (!connectionStatus) {
                            if (!checkConnectionResponse(inString))
                                checkConnectingNickname(inString);
                        } else {
                            fieldStatusCheck(inString);
                            if (!gotRandomNumberFromOpponent) gotRandomNumberFromOpponent = randCheck(inString);
                            if (!opponentLost) opponentLostCheck(inString);
                            if (responseCheck(inString)) {
                                gotResponse = true;
                            } else if (shotCheck(inString)) {
                                gotShot = true;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Sender extends Thread {
        public void run() {
            logIn();
            while (isRunning) {
                MainGame mainGame = new MainGame();
                mainGame.start();
                try {
                    mainGame.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class MainGame extends Thread {
        public void run() {
            resetGameData();
            sendGetUsers();
            mainMenu();
            if (!optionsFlag) {
                sendGetUsers();
                try {
                    makeHimFillTheField();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                waitForOpponentToFinishHiField();
                coinToss();
                try {
                    gameLoop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            optionsFlag = false;
        }
    }

    public boolean responseCheck(String clientCommand) {
        int x;
        int y;
        char response;
        char[] tmp = (clientCommand).toCharArray();
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i] == ' ') tmp[i] = '\n';
        }
        String tmpString = new String(tmp);
        Scanner scanner = new Scanner(tmpString);
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        } else return false;
        if (scanner.hasNextLine()) {
            String ttmmpp = scanner.nextLine();
            if (!ttmmpp.equals(this.nickname)) return false;
        } else return false;

        if (scanner.hasNextInt()) x = scanner.nextInt();
        else return false;

        if (scanner.hasNextInt()) y = scanner.nextInt();
        else return false;

        response = tmp[tmp.length - 1];

        if ((myField.isCordAppropriate(x)) && (myField.isCordAppropriate(y))) myField.putOpponentField(x, y, response);
        return true;
    }

    public boolean shotCheck(String clientCommand) {
        int x;
        int y;
        char[] tmp = clientCommand.toCharArray();
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i] == ' ') tmp[i] = '\n';
        }
        String tmpString = new String(tmp);
        Scanner scanner = new Scanner(tmpString);
        if (scanner.hasNextLine()) {
            String ttmmpp = scanner.nextLine();
            if (!ttmmpp.equals(nickname)) return false;
        } else return false;
        if (scanner.hasNextInt()) x = scanner.nextInt();
        else return false;
        if (scanner.hasNextInt()) y = scanner.nextInt();
        else return false;
        myField.shoot(x, y);
        for (int i = 0; i < myField.getFieldSize(); i++) {
            for (int j = 0; j < myField.getFieldSize(); j++) {
                sendResponse(i, j, myField.get(i, j));
            }
        }
        return true;
    }

    private void sendResponse(int x, int y, char response) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("@senduser ");
        stringBuilder.append(opponentNickname);
        stringBuilder.append(' ');
        stringBuilder.append("@Response");
        stringBuilder.append(' ');
        stringBuilder.append(opponentNickname);
        stringBuilder.append(' ');
        stringBuilder.append(x);
        stringBuilder.append(' ');
        stringBuilder.append(y);
        stringBuilder.append(' ');
        stringBuilder.append(response);
        out.println(stringBuilder.toString());
    }

    public boolean trySendingShootMessage(int x, int y) {
        if (gotShot) {
            if (!checkShootMap(x, y)) {
                if ((!myField.isCordAppropriate(x)) || (!myField.isCordAppropriate(y))) {
                    System.out.println("Your cords do not suit our game... Try again!");
                    return false;
                } else {
                    gotShot = false;
                    gotResponse = false;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("@senduser ");
                    stringBuilder.append(opponentNickname);
                    stringBuilder.append(' ');
                    stringBuilder.append(opponentNickname);
                    stringBuilder.append(' ');
                    stringBuilder.append(x);
                    stringBuilder.append(' ');
                    stringBuilder.append(y);
                    out.println(stringBuilder.toString());
                    fillShootMap(x, y);
                    System.out.println(stringBuilder.toString());
                    shootCounter++;
                    return true;
                }
            } else {
                return false;
            }
        } else {
            System.out.println("Wait for your turn please!");
            return false;
        }
    }

    public boolean changeNicknameCheck(String clientCommand) {
        char[] tagOriginal = {'@', 'n', 'a', 'm', 'e'};
        if (clientCommand.length() > (tagOriginal.length + 1)) {
            char[] sentenceChar = clientCommand.toCharArray();
            char[] sentenceTag = new char[tagOriginal.length];
            for (int i = 0; i <= sentenceTag.length - 1; i++) {
                sentenceTag[i] = sentenceChar[i];
            }
            if (Arrays.equals(sentenceTag, tagOriginal)) {
                this.changeNickname(sentenceChar, tagOriginal.length);
                return true;
            }
            return false;
        }
        return false;
    }

    public boolean changeNicknameCheckSimple(String clientCommand) {
        char[] tagOriginal = {'@', 'n', 'a', 'm', 'e'};
        if (clientCommand.length() > (tagOriginal.length + 1)) {
            char[] sentenceChar = clientCommand.toCharArray();
            char[] sentenceTag = new char[tagOriginal.length];
            for (int i = 0; i <= sentenceTag.length - 1; i++) {
                sentenceTag[i] = sentenceChar[i];
            }
            return Arrays.equals(sentenceTag, tagOriginal);
        }
        return false;
    }

    public void changeNickname(char[] sentenceChar, int tagLength) {
        char[] nicknameChar = new char[sentenceChar.length - tagLength - 1];
        int j = 0;
        for (int i = tagLength + 1; i <= sentenceChar.length - 1; i++) {
            nicknameChar[j] = sentenceChar[i];
            j++;
        }
        this.nickname = new String(nicknameChar);
        System.out.println("Your nickname is changed successfully!");
    }

    public boolean sendUserCheckSimple(String clientCommand) {
        char[] tagOriginal = {'@', 's', 'e', 'n', 'd', 'u', 's', 'e', 'r', ' '};
        if (clientCommand.length() > tagOriginal.length) {
            char[] sentenceChar = clientCommand.toCharArray();
            char[] sentenceTag = new char[tagOriginal.length];
            for (int i = 0; i <= sentenceTag.length - 1; i++) {
                sentenceTag[i] = sentenceChar[i];
            }
            return Arrays.equals(sentenceTag, tagOriginal);
        }
        return false;
    }

    public void makeHimFillTheField() throws IOException {
        System.out.println("---Fill your field pls:---");
        JFrame frame = new JFrame();
        Container c = frame.getContentPane();
        FieldConstructorWindow panel = new FieldConstructorWindow(colorSchemeIndex);
        c.add(panel);
        JMenuBar menuBar = new JMenuBar();
        JMenuItem Exit = new JMenuItem("Exit");
        Exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                System.exit(0);
            }
        });
        menuBar.add(Exit);
        menuBar.add(Box.createHorizontalGlue());
        frame.setJMenuBar(menuBar);
        frame.setResizable(false);
        frame.setSize(windowWidth, windowHeight);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setVisible(true);

        while (!panel.getField().isFieldReady()) {
        }
        myField = panel.getField();
        JOptionPane.showMessageDialog(null, "Field is ready!");
        frame.dispose();
        fieldStatus = true;
        sendFieldStatus();
    }

    private void sendGetUsers() {
        out.println("@getUsers");
    }

    private void connectToOpponent() {
        sendGetUsers();
        double start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < waitingTimeMillis) {
        }
        sendGetUsers();
        final String[] fromUser = {null};
        String nick = null;

        JFrame frame = new JFrame();
        frame.setSize(windowWidth, windowHeight);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setResizable(false);
        JButton[] buttons;
        Container c = frame.getContentPane();
        c.setLayout(new FlowLayout(FlowLayout.CENTER));
        System.out.println(onlinePlayersNumber);
        buttons = new JButton[onlinePlayersNumber];
        System.out.println(onlinePlayersList.toString());
        for (int i = 0; i < onlinePlayersNumber; i++) {
            buttons[i] = new JButton(onlinePlayersList.get(i));
            buttons[i].setIcon(Info.playerButtonIcon);
            int finalI = i;
            buttons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fromUser[0] = onlinePlayersList.get(finalI);
                    frame.dispose();
                }
            });
            c.add(buttons[i]);
        }

        c.setVisible(true);
        frame.setVisible(true);

        while (fromUser[0] == null) {
            System.out.println("in players list...");
            start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < 200) {
            }
        }

        if (fromUser[0] != null) {
            out.println(fromUser[0]);
            Scanner scanner = new Scanner(fromUser[0]);
            if (scanner.hasNextLine()) nick = scanner.nextLine();
        }
        if (nick != null) {
            if (!nick.equals(nickname)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("@senduser ");
                stringBuilder.append(nick);
                stringBuilder.append(" @connect ");
                stringBuilder.append(nickname);
                out.println(stringBuilder.toString());
                int i = -1;
                start = System.currentTimeMillis();
                while (!connectionStatus) {
                    if (System.currentTimeMillis() - start >= waitingTimeMillis) {
                        System.out.println("Waiting for connection...");
                        start = System.currentTimeMillis();
                    }
                }
                sendStartedGameStatusLog();
                JOptionPane.showMessageDialog(null, "Connection successful! Press OK to start.");


            }
        }
    }

    private void waitForOpponentConnection() {
        sendWaitingNotification();
        JOptionPane.showMessageDialog(null, "Waiting for opponent to connect...");
        double start = System.currentTimeMillis();
        while (!connectionStatus) {
            if (System.currentTimeMillis() - start >= waitingTimeMillis) {
                System.out.println("Waiting for connection...");
                start = System.currentTimeMillis();
            }
        }
        sendFoundOpponentNotification();

        JOptionPane.showMessageDialog(null, "Connection successful! Press OK to start.");
    }

    private boolean checkConnectingNickname(String clientCommand) {
        char[] tagOriginal = {'@', 'c', 'o', 'n', 'n', 'e', 'c', 't', ' '};
        if (clientCommand.length() > tagOriginal.length) {
            char[] sentenceChar = clientCommand.toCharArray();
            char[] sentenceTag = new char[tagOriginal.length];
            for (int i = 0; i <= sentenceTag.length - 1; i++) {
                sentenceTag[i] = sentenceChar[i];
            }
            if (Arrays.equals(sentenceTag, tagOriginal)) {
                char[] tmp = clientCommand.toCharArray();
                for (int i = 0; i < tmp.length; i++) {
                    if (tmp[i] == ' ') tmp[i] = '\n';
                }
                String tmpString = new String(tmp);
                Scanner scanner = new Scanner(tmpString);
                if (scanner.hasNextLine()) scanner.nextLine();
                else return false;
                if (scanner.hasNextLine()) opponentNickname = scanner.nextLine();
                else return false;
                System.out.println("Connection response sent to " + opponentNickname + '.');
                sendConnectionResponse(opponentNickname);
                connectionStatus = true;
                return true;
            }
        }
        return false;
    }

    private boolean randCheck(String clientCommand) {
        char[] tagOriginal = {'@', 'R', 'a', 'n', 'd', ' '};
        if (clientCommand.length() > tagOriginal.length) {
            char[] sentenceChar = clientCommand.toCharArray();
            char[] sentenceTag = new char[tagOriginal.length];
            for (int i = 0; i <= sentenceTag.length - 1; i++) {
                sentenceTag[i] = sentenceChar[i];
            }
            if (Arrays.equals(sentenceTag, tagOriginal)) {
                char[] tmp = clientCommand.toCharArray();
                for (int i = 0; i < tmp.length; i++) {
                    if (tmp[i] == ' ') tmp[i] = '\n';
                }
                String tmpString = new String(tmp);
                Scanner scanner = new Scanner(tmpString);
                if (scanner.hasNextLine()) scanner.nextLine();
                else return false;
                if (scanner.hasNextInt()) {
                    randNumberOpponent = scanner.nextInt();
                    return true;
                } else return false;
            }
        }
        return false;
    }

    private void fieldStatusCheck(String clientCommand) {
        if (clientCommand.equals("@fieldReady")) fieldStatusOpponent = true;
    }

    private void sendConnectionResponse(String opponentNickname) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("@senduser ");
        stringBuilder.append(opponentNickname);
        stringBuilder.append(" @connectionConfirmedFor ");
        stringBuilder.append(nickname);
        connectionStatus = true;
        out.println(stringBuilder.toString());
    }

    private void sendFieldStatus() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("@senduser ");
        stringBuilder.append(opponentNickname);
        stringBuilder.append(" @fieldReady");
        out.println(stringBuilder.toString());
    }

    private void sendLostStatus() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("@senduser ");
        stringBuilder.append(opponentNickname);
        stringBuilder.append(" @imLost");
        out.println(stringBuilder.toString());
    }

    private void opponentLostCheck(String clientCommand) {
        if (clientCommand.equals("@imLost")) opponentLost = true;
    }

    private int sendRandomNumber() {
        int result = -1;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("@senduser ");
        stringBuilder.append(opponentNickname);
        stringBuilder.append(" @Rand ");
        result = getRandomNumber(lowerBoundary, higherBoundary);
        stringBuilder.append(result);
        out.println(stringBuilder.toString());
        return result;
    }

    public int getRandomNumber(int lowerBoundary, int higherBoundary) {
        return (int) (Math.random() * (higherBoundary - lowerBoundary + 1) + lowerBoundary);
    }

    private boolean checkConnectionResponse(String clientCommand) {
        char[] tagOriginal = {'@', 'c', 'o', 'n', 'n', 'e', 'c', 't', 'i', 'o', 'n', 'C', 'o', 'n', 'f', 'i', 'r', 'm', 'e', 'd', 'F', 'o', 'r', ' '};
        if (clientCommand.length() > tagOriginal.length) {
            char[] sentenceChar = clientCommand.toCharArray();
            char[] sentenceTag = new char[tagOriginal.length];
            for (int i = 0; i <= sentenceTag.length - 1; i++) {
                sentenceTag[i] = sentenceChar[i];
            }
            if (Arrays.equals(sentenceTag, tagOriginal)) {
                char[] tmp = clientCommand.toCharArray();
                for (int i = 0; i < tmp.length; i++) {
                    if (tmp[i] == ' ') tmp[i] = '\n';
                }
                String tmpString = new String(tmp);
                Scanner scanner = new Scanner(tmpString);
                if (scanner.hasNextLine()) scanner.nextLine();
                else return false;
                if (scanner.hasNextLine()) {
                    opponentNickname = scanner.nextLine();
                    connectionStatus = true;
                    return true;
                } else return false;
            }
        }
        return false;
    }

    private boolean checkLoginResponse(String clientCommand) {
        char[] tagOriginal = {'@', 'l', 'o', 'g', 'i', 'n', 'R', 'e', 's', 'p', 'o', 'n', 's', 'e', ' '};
        if (clientCommand.length() > tagOriginal.length) {
            char[] sentenceChar = clientCommand.toCharArray();
            char[] sentenceTag = new char[tagOriginal.length];
            for (int i = 0; i <= sentenceTag.length - 1; i++) {
                sentenceTag[i] = sentenceChar[i];
            }
            if (Arrays.equals(sentenceTag, tagOriginal)) {
                char[] tmp = clientCommand.toCharArray();
                for (int i = 0; i < tmp.length; i++) {
                    if (tmp[i] == ' ') tmp[i] = '\n';
                }
                String tmpString = new String(tmp);
                Scanner scanner = new Scanner(tmpString);
                if (scanner.hasNextLine()) scanner.nextLine();
                else return false;
                if (scanner.hasNextLine()) {
                    nickname = scanner.nextLine();
                    loginStatus = true;
                    return true;
                } else return false;
            }
        }
        return false;
    }


    private boolean logInError(String clientCommand) {
        if (clientCommand.equals("@LogInError")) {
            return true;
        }
        return false;
    }

    private void tryLoggingIn() {
        if (loginStatus) return;
        String fromUser = null;
        String nickname = null;
        String password = null;
        if (loginStatus) return;
        nickname = JOptionPane.showInputDialog("Name:");
        password = JOptionPane.showInputDialog("Password:");
        fromUser = nickname + ' ' + password;
        if (fromUser != null) {
            out.println(fromUser);
        } else {
            JOptionPane.showMessageDialog(null, "Hey! Don't leave fields empty! Fill in something!");
        }
    }

    private boolean checkOnlinePlayersList(String clientCommand) {
        char[] tagOriginal = {'@', 'u', 's', 'e', 'r', 's', 'L', 'i', 's', 't', ' '};
        if (clientCommand.length() > tagOriginal.length) {
            char[] sentenceChar = clientCommand.toCharArray();
            char[] sentenceTag = new char[tagOriginal.length];
            for (int i = 0; i <= sentenceTag.length - 1; i++) {
                sentenceTag[i] = sentenceChar[i];
            }
            if (Arrays.equals(sentenceTag, tagOriginal)) {
                char[] tmp = clientCommand.toCharArray();
                for (int i = 0; i < tmp.length; i++) {
                    if (tmp[i] == ' ') tmp[i] = '\n';
                }
                String tmpString = new String(tmp);
                Scanner scanner = new Scanner(tmpString);
                if (scanner.hasNextLine()) scanner.nextLine();
                else return false;
                onlinePlayersList = new Vector<>();
                while (scanner.hasNextLine()) {
                    onlinePlayersList.add(scanner.nextLine());
                }
                onlinePlayersNumber = onlinePlayersList.size();
                return true;
            }
        }
        return false;
    }

    private void gameLoop() throws IOException {
        JFrame frame = new JFrame(nickname);
        Container c = frame.getContentPane();
        GameWindow panel = new GameWindow(colorSchemeIndex);
        panel.updateField(myField);
        c.add(panel);
        JMenuBar menuBar = new JMenuBar();
        JMenuItem Exit = new JMenuItem("Exit");
        Exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                System.exit(0);
            }
        });
        menuBar.add(Exit);
        menuBar.add(Box.createHorizontalGlue());
        frame.setJMenuBar(menuBar);
        frame.setSize(windowWidth, windowHeight);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
        while ((!opponentLost) && (!myField.amILost())) {
            panel.updateField(myField);
            panel.updateTurnInfo(gotShot);
            int[] tmp = panel.getShootCords();
            System.out.println("Shot: " + tmp[0] + " " + tmp[1]);
            trySendingShootMessage(tmp[0], tmp[1]);
            lastShootCords = tmp;
            double start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < waitingTimeMillis) {
            }
        }
        if (opponentLost) {
            sendWonStatusLog();
            JOptionPane.showMessageDialog(null, "You won! Number of shots: " + shootCounter + ". Number of destroyed enemy ships: " + myField.countEnemyDeadShips());
            frame.dispose();
        } else {
            sendLostStatus();
            JOptionPane.showMessageDialog(null, "You lost... Number of shots: " + shootCounter + ". Number of destroyed enemy ships: " + myField.countEnemyDeadShips());
            frame.dispose();
        }
    }

    public void logIn() {
        System.out.println("Login pls:");
        boolean firstTryFlag = true;
        while (!loginStatus) {
            if ((!firstTryFlag) && (!loginStatus))
                JOptionPane.showMessageDialog(null, "Login failed! Check your nickname and password...");
            tryLoggingIn();
            double start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < waitingTimeMillis) {

            }
            firstTryFlag = false;
        }
    }

    public void mainMenu() {
        String[] optionsList = {"connect", "wait", "options", "exit"};
        final int[] selection = {-1};

        final boolean[] flag = {true};
        int buttonWidth = 250;
        int buttonHeight = 25;
        int buttonDistance = 45;
        int distance = 300;
        int titleWidth = 600;
        int titleHeight = 100;
        JFrame frame = new JFrame(nickname);
        frame.setSize(windowWidth, windowHeight);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        JLayeredPane lp = frame.getLayeredPane();
        JLabel gameTitle = new JLabel(Info.mainMenuGameTitle);
        JLabel background = new JLabel(Info.mainMenuBackground);
        background.setBounds(0, 0, windowWidth, windowHeight);
        gameTitle.setBounds(45, 15, titleWidth, titleHeight);
        lp.add(gameTitle, JLayeredPane.POPUP_LAYER);
        lp.add(background, JLayeredPane.DEFAULT_LAYER);

        JButton Connect = new JButton(Info.mainMenuConnectButton);
        Connect.setBorderPainted(false);
        Connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onlinePlayersNumber > 0) {
                    frame.dispose();
                    selection[0] = 0;
                    flag[0] = false;
                } else {
                    JOptionPane.showMessageDialog(null, "Nobody waits for opponent right now... ");

                }
            }
        });
        Connect.setBounds(windowWidth / 2 - buttonWidth / 2, distance - buttonDistance * 3, buttonWidth, buttonHeight);
        lp.add(Connect, JLayeredPane.POPUP_LAYER);

        JButton waitForConnection = new JButton(Info.mainMenuWaitButton);
        waitForConnection.setBorderPainted(false);
        waitForConnection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                selection[0] = 1;
                flag[0] = false;
            }
        });
        waitForConnection.setBounds(windowWidth / 2 - buttonWidth / 2, distance - buttonDistance * 2, buttonWidth, buttonHeight);
        lp.add(waitForConnection, JLayeredPane.POPUP_LAYER);

        JButton options = new JButton(Info.mainMenuOptionsButton);
        options.setBorderPainted(false);
        options.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selection[0] = 2;
                frame.dispose();
            }
        });
        options.setBounds(windowWidth / 2 - buttonWidth / 2, distance - buttonDistance, buttonWidth, buttonHeight);
        lp.add(options, JLayeredPane.POPUP_LAYER);

        JButton exit = new JButton(Info.mainMenuExitButton);
        exit.setBorderPainted(false);
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selection[0] = 3;
                frame.dispose();
            }
        });
        exit.setBounds(windowWidth / 2 - buttonWidth / 2, distance, buttonWidth, buttonHeight);
        lp.add(exit, JLayeredPane.POPUP_LAYER);
        lp.setFocusable(true);
        frame.setVisible(true);
        while (selection[0] == -1) {
            System.out.println("in main menu..." + "color scheme number: " + colorSchemeIndex);
            double start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < 1000) {
            }
            sendGetUsers();
        }
        switch (selection[0]) {
            case 0:
                connectToOpponent();
                break;
            case 1:
                waitForOpponentConnection();
                break;
            case 2:
                options();
                optionsFlag = true;
                break;
            case 3:
                System.exit(0);
                break;
        }
        frame.dispose();
    }

    public void waitForOpponentToFinishHiField() {
        double start = System.currentTimeMillis();
        while ((!fieldStatus) || (!fieldStatusOpponent)) {
            if (System.currentTimeMillis() - start >= waitingTimeMillis) {
                JOptionPane.showMessageDialog(null, "Waiting for opponent to finish his field...");
                start = System.currentTimeMillis();
            }
        }
    }


    public void coinToss() {
        randNumber = sendRandomNumber();
        double start = System.currentTimeMillis();
        while (!gotRandomNumberFromOpponent) {
            if (System.currentTimeMillis() - start >= waitingTimeMillis) {
                System.out.println("Waiting for opponents random number...");
                start = System.currentTimeMillis();
            }
        }

        if (randNumber > randNumberOpponent) {
            gotShot = true;
            gotResponse = true;
            JOptionPane.showMessageDialog(null, "Congratulations! First turn is yours! Your number is " + randNumber + ". Opponents number is: " + randNumberOpponent);
        } else {
            gotShot = false;
            gotResponse = false;
            JOptionPane.showMessageDialog(null, "Bad luck! Your turn is second... Your number is " + randNumber + ". Opponents number is: " + randNumberOpponent);
        }
    }

    public void resetGameData() {
        myField = new Model();

        opponentNickname = null;
        gotShot = false;
        gotResponse = false;
        fieldStatus = false;
        fieldStatusOpponent = false;
        connectionStatus = false;
        randNumberOpponent = -1;
        randNumber = -1;
        gotRandomNumberFromOpponent = false;
        opponentLost = false;
        onlinePlayersList = new Vector<>();
        onlinePlayersNumber = 0;
        lastShootCords = new int[]{-1, -1};
        shootMap = new boolean[myField.getFieldSize()][myField.getFieldSize()];
        shootCounter = 0;
        for (int i = 0; i < shootMap.length; i++) {
            Arrays.fill(shootMap[i], false);
        }
    }

    private boolean checkShootMap(int x, int y) {
        if ((myField.isCordAppropriate(x)) && (myField.isCordAppropriate(y))) {
            return shootMap[y][x];
        }
        return false;
    }

    private void fillShootMap(int x, int y) {
        if ((myField.isCordAppropriate(x)) && (myField.isCordAppropriate(y))) {
            shootMap[y][x] = true;
        }
    }

    private void sendWaitingNotification() {
        StringBuilder sb = new StringBuilder();
        sb.append("@imWaitingForOpponent ");
        sb.append(nickname);
        out.println(sb.toString());
    }

    private void sendFoundOpponentNotification() {
        StringBuilder sb = new StringBuilder();
        sb.append("@imNotWaitingAnymore ");
        sb.append(nickname);
        out.println(sb.toString());
    }

    private void options() {
        final boolean[] choiceFlag = {false};
        JFrame frame = new JFrame("Choose your color theme:");
        frame.setSize(windowWidth, windowHeight);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setResizable(false);
        JButton[] buttons;
        Container c = frame.getContentPane();
        c.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttons = new JButton[colorSchemesAvailable];
        for (int i = 0; i < colorSchemesAvailable; i++) {
            buttons[i] = new JButton(String.valueOf(i + 1));
            //buttons[i].setIcon(Info.playerButtonIcon);
            int finalI = i;
            buttons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    colorSchemeIndex = finalI;
                    choiceFlag[0] = true;
                    optionsFlag = true;
                    frame.dispose();
                }
            });
            c.add(buttons[i]);
        }

        c.setVisible(true);
        frame.setVisible(true);

        while (choiceFlag[0] == false) {
            System.out.println("in settings...");
            double start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < 50) {
            }
        }


    }

    private void sendWonStatusLog() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("@logRecord ");
        stringBuilder.append(nickname + " and " + opponentNickname + " finished their match. The winner is " + nickname);
        out.println(stringBuilder.toString());
    }

    private void sendStartedGameStatusLog() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("@logRecord ");
        stringBuilder.append(nickname + " and " + opponentNickname + " started a match.");
        out.println(stringBuilder.toString());
    }
}
