package Model;

import IO.IO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

    public class FMGSServer implements Runnable {
    String log = "";
    LinkedList<ClientThread> clientThreads = new LinkedList<>();
    HashMap<String, String> loginsAndPasswords;
    Vector<String> usersOnline = new Vector<>();
    Vector<String> usersWaiting = new Vector<>();
    double usersOnlineUpdateDelay = 1000;
    double logUpdateDelay = 10000;
    int port;

    public FMGSServer(int port) {
        this.port = port;
        loginsAndPasswords = new HashMap<>();
    }

    public void run() {
        (new Updater()).start();
        try {
            loginsAndPasswords = IO.fillLoginsAndPasswords();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (loginsAndPasswords.isEmpty()) System.exit(1);
        initLog();
        writeToLog("Server launched!");
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientThread clientThread = new ClientThread(clientSocket);
                clientThread.start();
                clientThreads.add(clientThread);
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
                ioe.printStackTrace();

            }
            //System.out.println("User joined!");
        }

    }

    class Updater extends Thread {
        public void run() {
            double startUsersOnlineUpdate = System.currentTimeMillis();
            double startLogUpdate = System.currentTimeMillis();
            while (true) {
                if (System.currentTimeMillis() - startUsersOnlineUpdate >= usersOnlineUpdateDelay) {
                    updateOnline();
                    //System.out.println(usersOnline.toString());
                    startUsersOnlineUpdate = System.currentTimeMillis();
                }

                if (System.currentTimeMillis() - startLogUpdate >= logUpdateDelay) {
                    try {
                        updateLogFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    startLogUpdate = System.currentTimeMillis();
                }
            }
        }
    }

    class ClientThread extends Thread {
        String nickname;
        Socket socket;
        String clientCommand = null;
        boolean status = true;
        BufferedReader in;
        PrintWriter out;

        ClientThread(Socket s) {
            socket = s;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            writeToLog("New user joined!");
        }

        public void run() {
            clientCommand = null;
            while (true) {
                try {
                    clientCommand = in.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (tryLoggingIn(clientCommand)) {
                    sendLoggedInMessage(nickname);
                    out.println("You successfully logged in!");
                    out.println("Your nickname:");
                    out.println(nickname);
                    usersOnline.add(nickname);
                    writeToLog("User logged in as: " + nickname);

                    break;
                } else {
                    out.println("@LogInError");
                }
            }

            while (status) {
                clientCommand = null;
                try {
                    clientCommand = in.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (clientCommand != null) {
                    getPlayersCheck(clientCommand, this.nickname);
                    newLogRecordCheck(clientCommand);
                    sendUserCheck(clientCommand);
                    waitingCheck(clientCommand);
                    notWaitingAnimoreCheck(clientCommand);
                } else {
                    break;
                }

            }
        }

        public void sendMessageAllThread(String nickname, String message) {
            if (!Objects.equals(this.nickname, nickname)) {
                out.println(nickname + " : " + message);
            }
        }

//        public void sendMessageThread(String nickname, String message) {
//            if (Objects.equals(this.nickname, nickname)) out.println(nickname + " " + message);
//        }

        public void sendMessageThreadNoNick(String nickname, String message) {
            if (Objects.equals(this.nickname, nickname)) out.println(message);
        }

        public boolean byeCheck(String message) {
            if (Objects.equals(message, "@quit")) {
                sendMessageAll(nickname, "Bye everyone!");
                return true;
            } else {
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

        public void changeNickname(char[] sentenceChar, int tagLength) {
            char[] nicknameChar = new char[sentenceChar.length - tagLength - 1];
            int j = 0;
            for (int i = tagLength + 1; i <= sentenceChar.length - 1; i++) {
                nicknameChar[j] = sentenceChar[i];
                j++;
            }

            for (int i = 0; i < nicknameChar.length; i++) {
                if (nicknameChar[i] == '@') {
                    out.println("You are not allowed to use '@' in your nickname! Think of another one.");
                    return;
                }
            }
            this.nickname = new String(nicknameChar);
            out.println("@name " + this.nickname);
        }

        private boolean tryLoggingIn(String command) {
            char[] commandChars = command.toCharArray();
            int counter = 0;

            for (int i = 0; i < commandChars.length; i++) {
                if (commandChars[i] == ' ') counter++;
            }

            if (counter != 1) return false;

            for (int i = 0; i < commandChars.length; i++) {
                if (commandChars[i] == ' ') {
                    commandChars[i] = '\n';
                    break;
                }
            }

            Scanner scanner = new Scanner(new String(commandChars));
            String nickname;
            if (scanner.hasNextLine()) {
                nickname = scanner.nextLine();
            } else return false;
            String password;
            if (scanner.hasNextLine()) {
                password = scanner.nextLine();
            } else return false;

            String realPassword;
            if (loginsAndPasswords.get(nickname) != null) {
                if (checkUserPresence(nickname)) {
                    sendMessageNoNick(this.nickname, "User with this nickname already logged in...");
                    return false;
                }
                realPassword = loginsAndPasswords.get(nickname);
                if (password.equals(realPassword)) {
                    this.nickname = nickname;
                    return true;
                }
            }
            return false;
        }

        private boolean checkUserPresence(String nickname) {
            for (int i = 0; i < usersOnline.size(); i++) {
                if (usersOnline.get(i).equals(nickname)) {
                    return true;
                }
            }
            return false;
        }

        public String getNickname() {
            return this.nickname;
        }
    }


    public void sendMessage(String nickname, String message) {
        for (int i = 0; i < clientThreads.size(); i++) {
            if (clientThreads.get(i).isAlive()) {
//                clientThreads.get(i).sendMessageThread(nickname, message);
                clientThreads.get(i).sendMessageThreadNoNick(nickname, message);
            }
        }
    }

    public void sendMessageAll(String nickname, String message) {
        for (int i = 0; i < clientThreads.size(); i++) {
            if (clientThreads.get(i).isAlive()) {
                clientThreads.get(i).sendMessageAllThread(nickname, message);
            }
        }
    }

    public void sendMessageNoNick(String nickname, String message) {
        for (int i = 0; i < clientThreads.size(); i++) {
            if (clientThreads.get(i).isAlive()) {
                clientThreads.get(i).sendMessageThreadNoNick(nickname, message);
            }
        }
    }

    public boolean notWaitingAnimoreCheck(String clientCommand) {
        String nickName;
        char[] tagOriginal = {'@', 'i', 'm', 'N', 'o', 't', 'W', 'a', 'i', 't', 'i', 'n', 'g', 'A', 'n', 'y', 'm', 'o', 'r', 'e', ' '};
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
                if (scanner.hasNextLine()) nickName = scanner.nextLine();
                else return false;
                usersWaiting.remove(nickName);
                //System.out.println("Removed from waiting list:");
                //System.out.println(usersWaiting);
                //writeToLog(nickName + " found his opponent.");
                return true;
            }
        }
        return false;
    }

    public boolean newLogRecordCheck(String clientCommand) {
        char[] tagOriginal = {'@', 'l', 'o', 'g', 'R', 'e', 'c', 'o', 'r', 'd', ' '};
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
                StringBuilder sb = new StringBuilder();
                while (scanner.hasNextLine()) {
                    sb.append(scanner.nextLine());
                    sb.append(" ");
                }
                writeToLog(sb.toString());
                return true;
            }
        }
        return false;
    }

    public boolean waitingCheck(String clientCommand) {
        String nickName;
        char[] tagOriginal = {'@', 'i', 'm', 'W', 'a', 'i', 't', 'i', 'n', 'g', 'F', 'o', 'r', 'O', 'p', 'p', 'o', 'n', 'e', 'n', 't', ' '};
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
                if (scanner.hasNextLine()) nickName = scanner.nextLine();
                else return false;
                usersWaiting.add(nickName);
                //System.out.println("Added to waiting list:");
                //System.out.println(usersWaiting);
                writeToLog(nickName + " started waiting for opponent!");
                return true;
            }
        }
        return false;
    }

    public boolean sendUserCheck(String clientCommand) {
        char[] tagOriginal = {'@', 's', 'e', 'n', 'd', 'u', 's', 'e', 'r', ' '};
        if (clientCommand.length() > tagOriginal.length) {
            char[] sentenceChar = clientCommand.toCharArray();
            char[] sentenceTag = new char[tagOriginal.length];
            for (int i = 0; i <= sentenceTag.length - 1; i++) {
                sentenceTag[i] = sentenceChar[i];
            }
            int spaceIndex = 0;
            if (Arrays.equals(sentenceTag, tagOriginal)) {
                for (int i = sentenceChar.length - 1; i >= tagOriginal.length; i--) {
                    if (sentenceChar[i] == ' ') spaceIndex = i;
                }
                char[] nicknameChar = new char[sentenceChar.length - tagOriginal.length - (sentenceChar.length - spaceIndex)];
                int j = 0;
                for (int i = tagOriginal.length; i <= spaceIndex - 1; i++) {
                    nicknameChar[j] = sentenceChar[i];
                    j++;
                }
                char[] messageChars = new char[sentenceChar.length - spaceIndex - 1];
                int g = 0;
                for (int i = spaceIndex + 1; i < sentenceChar.length; i++) {
                    messageChars[g] = sentenceChar[i];
                    g++;
                }
                sendMessage(new String(nicknameChar), new String(messageChars));
                return true;
            }
            return false;
        }
        return false;
    }

    public boolean getPlayersCheck(String clientCommand, String nickname) {
        if (clientCommand.equals("@getUsers")) {
            StringBuilder stringBuilder = new StringBuilder();
            //stringBuilder.append('|');
            for (int i = 0; i < usersOnline.size(); i++) {
                if ((!nickname.equals(usersOnline.get(i))) && (usersWaiting.contains(usersOnline.get(i)))) {
                    stringBuilder.append(usersOnline.get(i));
                    stringBuilder.append(' ');
                }
            }
            sendMessageNoNick(nickname, "@usersList " + stringBuilder.toString());
            return true;
        }
        return false;
    }

    private void sendLoggedInMessage(String nickname) {
        sendMessageNoNick(nickname, "@loginResponse " + nickname);
    }

    private void deleteThread(String nickname) {
        for (int i = 0; i < clientThreads.size(); i++) {
            if (clientThreads.get(i) != null) {
                if (clientThreads.get(i).getNickname().equals(nickname)) clientThreads.remove(i);
            }
        }
    }

    private void deleteFromOnline(String nickname) {
        for (int i = 0; i < usersOnline.size(); i++) {
            if (usersOnline.get(i).equals(nickname)) usersOnline.remove(i);
        }
    }

    private void updateOnline() {
        for (int i = 0; i < clientThreads.size(); i++) {
            if (!clientThreads.get(i).isAlive()) {
                String nick = clientThreads.get(i).getNickname();
                deleteFromOnline(nick);
                deleteThread(nick);
                writeToLog(nick + " is offline now.");
            }
        }
    }

    private void initLog() {
        synchronized (log) {
            log = IO.initLog();
        }
    }

    private void updateLogFile() throws IOException {
        synchronized (log) {
            IO.updateLogFile(log);
        }

    }

    private synchronized void writeToLog(String record) {
        synchronized (log) {
            System.out.println(record);
            log = IO.writeToLog(record, log);
        }
    }

}


