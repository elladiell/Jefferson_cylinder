package com.company;
//по ключу и хэшу передавать цилиндры, генерация по паролю
import java.io.*;
import java.util.*;

public class Main {
    private static int discsCount = 36;
    private static final int ALPHABET_LENGTH = 256; // ASCII codes 32- 126
    private static String fileEncoding = "Cp1251";


    public static String[] generateCylinder(Random random) throws UnsupportedEncodingException {
        String[] discs = new String[discsCount];
        byte[] alphabet = new byte[ALPHABET_LENGTH];
        for (int i = 0; i < alphabet.length; i++) {
            alphabet[i] = (byte) i;// ' ' = 32
        }

        for (int i = 0; i < discs.length; i++) {
            byte [] copy = new byte[ALPHABET_LENGTH];
            System.arraycopy(alphabet,0,copy,0,ALPHABET_LENGTH);
            shuffleArray(copy, random);
            discs[i] = new String(copy, fileEncoding);
        }
        return discs;
    }

    // Implementing Fisher–Yates shuffle
    static void shuffleArray(byte[] ar, Random rnd)
    {
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            byte a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    public static String[] readDiscs(String fileName) throws IOException {
        List<String> discsList = new ArrayList<>();
        try (Reader br = new InputStreamReader(new FileInputStream(fileName), fileEncoding)) {
            int i = 0;
            cycle:
            while(true) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < ALPHABET_LENGTH; j++) {
                    int c = br.read();
                    if (c == -1) break cycle;
                    sb.append((char) c);
                }
                ++i;
                discsList.add(sb.toString());
            }
        }
        discsCount = discsList.size();
        String[] discs = discsList.toArray(new String[]{});
        return discs;
    }


    public static EncryptedMessageWithKey encrypt(String message, String[] discs) {
        List<Integer> discsOrder = generateDiscsOrder(discsCount);
        int shift = new Random().nextInt(ALPHABET_LENGTH);
        StringBuilder encryptedMessageBuider = new StringBuilder();
        for (int i = 0, j = 0; i < message.length(); i++) { //j - disc index
            char c = message.charAt(i);
            int indexInDisc = discs[discsOrder.get(j)].indexOf(c);
            int encryptedIndex = (indexInDisc + shift) % ALPHABET_LENGTH; // зацикливаем массив
            encryptedMessageBuider.append(discs[discsOrder.get(j)].charAt(encryptedIndex));
            j = (j + 1) % discsCount;
        }
        StringBuilder discsOrderStringBuilder = new StringBuilder(0);
        discsOrder.forEach((e) -> {
            discsOrderStringBuilder.append(e);
            discsOrderStringBuilder.append(",");
        });
        return new EncryptedMessageWithKey(encryptedMessageBuider.toString(), discsOrderStringBuilder.toString(), shift);
    }

    static List<Integer> convertDiscOrderStringToList(String discsOrderString) {
        String[] orderStrings = discsOrderString.split(",");
        List<Integer> discsOrder = new ArrayList<>();
        for (int i = 0; i < orderStrings.length; i++) {
            discsOrder.add(Integer.parseInt(orderStrings[i]));
        }
        return discsOrder;
    }

    public static String decrypt(String encrypterMsg, String[] discs, String discsOrderString, int shift) {
        List<Integer> discsOrder = convertDiscOrderStringToList(discsOrderString);
        StringBuilder decryptedMessageBuider = new StringBuilder();
        for (int i = 0, j = 0; i < encrypterMsg.length(); i++) {
            char c = encrypterMsg.charAt(i);
            int indexInDisc = discs[discsOrder.get(j)].indexOf(c);
            int decryptedIndex = indexInDisc - shift; // зацикливаем массив
            if (decryptedIndex < 0) {
                decryptedIndex = ALPHABET_LENGTH + decryptedIndex;
            }
            decryptedMessageBuider.append(discs[discsOrder.get(j)].charAt(decryptedIndex));
            j = (j + 1) % discsCount;
        }
        return decryptedMessageBuider.toString();
    }

    private static List<Integer> generateDiscsOrder(int discsCount) {
        List<Integer> discNos = new ArrayList<>();
        for (int i = 0; i < discsCount; i++) {
            discNos.add(0, i);
        }
        Collections.shuffle(discNos);
        return discNos;
    }

    public static void main(String[] args) throws IOException {

        if (args.length > 1) {
            switch (args[0]) {
                case "--generate-cylinder":
                case "-g": {
                    if (args.length < 2) {
                        printUsage();
                        return;
                    }
                    Random rand = new Random();
                    if (args.length >= 4) {
                        if (args[2].equals("-password")) {
                            rand = new Random(args[3].hashCode());
                        } else {
                            discsCount = Integer.parseInt(args[3]);
                        }
                    }
                    String fileForCylinder = args[1];
                    String[] discs = generateCylinder(rand);
                    writeDiscs(discs, fileForCylinder);
                    break;
                }
                case "-e":
                case "-ef":
                case "--encryptfile":
                case "--encrypt": {
                    if (args.length < 8) {
                        printUsage();
                        return;
                    }
                    String msg = args[1];
                    if(args[0].equals("-ef") || args[0].equals("--encryptfile")){
                        msg = readFromFile(args[1]);
                    }

                    if(msg.isEmpty()){
                        System.out.println("Enter encrypted message:");
                        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                        msg = br.readLine();
                    }
                    String cylinderFile = args[3];
                    String encryptedFile = args[5];
                    String keyFile = args[7];
                    EncryptedMessageWithKey result = encrypt(msg, readDiscs(cylinderFile));
                    System.out.println("Encrypted message: " + result.encryptedMessage);
                    System.out.println("Discs order: " + result.discsOrder);
                    System.out.println("Shift: " + result.shift);
                    writeToFile(result.encryptedMessage, encryptedFile);
                    writeToFile(result.discsOrder + "\n" + result.shift, keyFile);
                    break;
                }
                case "-df":
                case "--decryptfile":
                case "-d":
                case "--decrypt": {
                    if (args.length < 6) {
                        printUsage();
                        return;
                    }
                    String msg = args[1];
                    if(args[0].equals("-df") || args[0].equals("--decryptfile")){
                       msg = readFromFile(args[1]);
                    }

                    if(msg.isEmpty()){
                        System.out.println("Enter encrypted message:");
                        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                        msg = br.readLine();
                    }

                    String [] discs1;
                    if(args[2].equals("-password")){
                        Random rand = new Random(args[3].hashCode());
                        discs1 = generateCylinder(rand);
                    }else{
                        discs1 = readDiscs(args[3]);
                    }

                    String decryptedFile = args[5];
                    String discsOrderString;
                    int shift;
                    if(args.length >= 8){
                        String keyFile = args[7];
                        try(BufferedReader br = new BufferedReader(new FileReader(keyFile))){
                            discsOrderString = br.readLine();
                            shift = Integer.parseInt(br.readLine());
                        }
                    }else {
                        System.out.println("Enter disc order key (comma separated list for zero based indices of discs):");
                        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                        discsOrderString = br.readLine();
                        System.out.println("Enter shift:");
                        shift = Integer.parseInt(br.readLine());
                    }


                    String decrypterMsg = decrypt(msg, discs1, discsOrderString, shift);
                    System.out.println("Decrypted message: " + decrypterMsg);
                    writeToFile(decrypterMsg, decryptedFile);
                    break;
                }
                default:
                    printUsage();
            }
        } else {
            printUsage();
        }
    }

    private static String readFromFile(String fileName) throws IOException {
        StringBuilder msg = new StringBuilder();
        try(InputStreamReader br = new InputStreamReader(new FileInputStream(fileName), fileEncoding)) {
            char [] buf = new char[512];
            int read;
            while((read = br.read(buf))!= -1){
                msg.append(Arrays.copyOfRange(buf, 0, read));
            }
        }
        return msg.toString();
    }


    private static void writeToFile(String msg, String fileName) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), fileEncoding))) {
            bw.write(msg);
        }
    }

    private static void writeDiscs(String[] discs, String fileForCylinder) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileForCylinder), fileEncoding))) {
            for (String disc : discs) {
                bw.write(disc);
            }
        }
    }

    private static void printUsage() {
        System.out.println("Usage: ");
        System.out.println("To generate cylinder: \t java -jar cd.jar [-g|--generate-cylinder] filename [(-discscount N|-password pwd)]");
        System.out.println("To encrypt: java -jar cd.jar [-e|--encrypt] msg -c cylinderFile -o encryptedFile -k keyFile");
        System.out.println("To decrypt: java -jar cd.jar [-d|--decrypt] encryptedMsg (-c cylinderFile|-password pwd) -o decryptedFile");
    }
}

class EncryptedMessageWithKey {

    public String encryptedMessage;//ключ и message были разными в представлянием кода
    public String discsOrder;
    public int shift;

    public EncryptedMessageWithKey(String encryptedMessage, String
            discsOrder, int shift) {
        this.encryptedMessage = encryptedMessage;
        this.discsOrder = discsOrder;
        this.shift = shift;
    }
}