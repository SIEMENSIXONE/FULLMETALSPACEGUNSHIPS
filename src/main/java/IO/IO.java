package IO;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Scanner;

public class IO {
    static String usersFilePath = "text/users.txt";
    static String logPath = "./serverLog.txt";

    public static String initLog() {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(logPath))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!stringBuilder.isEmpty()) return stringBuilder.toString();
        else return "";

    }

    public static void updateLogFile(String log) throws IOException {
        String logTmp = log;
        Scanner scanner = new Scanner(logTmp);
        Writer writer = new OutputStreamWriter(new FileOutputStream(logPath));
        while (scanner.hasNextLine()) {
            writer.write(scanner.nextLine());
            writer.write("\n");
        }
        writer.close();

    }

    public static synchronized String writeToLog(String record, String log) {

        LocalDateTime datetime = LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(log);
        stringBuilder.append(datetime);
        stringBuilder.append(" - ");
        stringBuilder.append(record);
        stringBuilder.append('\n');
        return stringBuilder.toString();
    }

//    public static HashMap<String, String> fillLoginsAndPasswords() {
//        HashMap<String, String> loginsAndPasswords = new HashMap<>();
////        try (BufferedReader reader = new BufferedReader(new FileReader(usersFilePath))) {
//        try (BufferedReader reader = new BufferedReader(new FileReader(String.valueOf(usersFilePath)))) {
//            String line = null;
//            while ((line = reader.readLine()) != null) {
//                char[] tmp = line.toCharArray();
//                for (int i = 0; i < tmp.length; i++) {
//                    if (tmp[i] == ' ') tmp[i] = '\n';
//                }
//                line = new String(tmp);
//                Scanner scanner = new Scanner(line);
//                String name;
//                String password;
//                if (scanner.hasNextLine()) name = scanner.nextLine();
//                else return new HashMap<>();
//                if (scanner.hasNextLine()) password = scanner.nextLine();
//                else return new HashMap<>();
//                loginsAndPasswords.put(name, password);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        if (loginsAndPasswords.isEmpty()) return new HashMap<>();
//        return loginsAndPasswords;
//    }

    public static HashMap<String, String> fillLoginsAndPasswords() throws IOException {
        HashMap<String, String> loginsAndPasswords = new HashMap<>();
        InputStream usersFile = getFileAsIOStream(usersFilePath);

        byte[] usersFileBytes = usersFile.readAllBytes();
        //try (BufferedReader reader = new BufferedReader(new FileReader(String.valueOf(usersFilePath)))) {
        Scanner reader = new Scanner(new String(usersFileBytes));
        String line = null;
        while (reader.hasNextLine()) {
            line = reader.nextLine();
            char[] tmp = line.toCharArray();
            for (int i = 0; i < tmp.length; i++) {
                if (tmp[i] == ' ') tmp[i] = '\n';
            }
            line = new String(tmp);
            Scanner scanner = new Scanner(line);
            String name;
            String password;
            if (scanner.hasNextLine()) name = scanner.nextLine();
            else return new HashMap<>();
            if (scanner.hasNextLine()) password = scanner.nextLine();
            else return new HashMap<>();
            loginsAndPasswords.put(name, password);
        }

        System.out.println(loginsAndPasswords.toString());
        if (loginsAndPasswords.isEmpty()) return new HashMap<>();
        return loginsAndPasswords;
    }

    public static InputStream getFileAsIOStream(final String fileName) {
        InputStream ioStream = IO.class
                .getClassLoader()
                .getResourceAsStream(fileName);

        if (ioStream == null) {
            throw new IllegalArgumentException(fileName + " is not found");
        }
        return ioStream;
    }

}
