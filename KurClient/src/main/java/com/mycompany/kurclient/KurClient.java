package com.mycompany.kurclient;
// KurClient
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Random;

public class KurClient extends JFrame {
    private static JFrame frame;
    private static JTextField usernameField;
    private static JPasswordField passwordField;
    private static Аccount akk;

    private int score = 0;
    private int colorScore = 0;
    private String colorCheck = "";
    private JLabel scoreLabel = new JLabel("Score: " + score + "/500   " + colorCheck + " blocks: " + (12 - colorScore) + "/12");
    private static final int ROWS = 9;
    private static final int COLUMNS = 9;
    private static final Color[] COLORS = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.PINK, Color.GRAY};
    private static final Color[] COLORS_FREE = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.PINK};
    private static final Color[] COLORS_2 = {Color.MAGENTA, Color.BLUE, Color.CYAN, Color.ORANGE, Color.PINK, Color.DARK_GRAY};
    private static final Color[] COLORS_FREE_2 = {Color.MAGENTA, Color.BLUE, Color.CYAN, Color.ORANGE, Color.PINK};
    private Color actualColor;
    
    private JPanel gamePanel = new JPanel(new GridLayout(ROWS, COLUMNS));
    private JPanel[][] cells = new JPanel[ROWS][COLUMNS];
    private Color[][] board = new Color[ROWS][COLUMNS];
    boolean newClrs = false;

    public static void main(String[] args) {
        frame = new JFrame("Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 150);
        frame.setLayout(new GridLayout(3, 2));

        JLabel usernameLabel = new JLabel("Username:");
        frame.add(usernameLabel);

        usernameField = new JTextField();
        frame.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        frame.add(passwordLabel);

        passwordField = new JPasswordField();
        frame.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                akk.username = usernameField.getText();
                akk.password = new String(passwordField.getPassword());
                if(akk.sendLoginData()){
                    showMainMenu(akk.username);
                    frame.dispose();
                }else{
                    int option = JOptionPane.showConfirmDialog(frame, "Account doesn't exist. Create account?", "Сonfirmation", JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        if(akk.createAccount()){
                            JOptionPane.showMessageDialog(frame, "Account created successfully. Main menu will open.");
                            showMainMenu(akk.username);
                            frame.dispose();
                        }else{
                            JOptionPane.showMessageDialog(frame, "Аn account with the same name already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
        frame.add(loginButton);

        frame.setVisible(true);
    }
    
    public KurClient(String username, boolean newColors) {
        newClrs = newColors;
        setTitle("Three in a Row Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setResizable(false);

        score = 0;
        colorScore = 12;
        
        initializeBoard();

        board = akk.getAccountLevel(COLORS_2, COLORS, ROWS, COLUMNS, newClrs);
        
        if (board[0][0] == null) {
            initializeBoard();
        }    


        updateGamePanel();
       
        add(gamePanel);
       
        
        gamePanel.addMouseListener(new MouseAdapter() {
            int selectedRow = 0, selectedCol = 0, row = 0, col = 0;
            @Override
            public void mousePressed(MouseEvent e){
                selectedRow = e.getY() / (gamePanel.getHeight() / ROWS);
                selectedCol = e.getX() / (gamePanel.getWidth() / COLUMNS);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                row = e.getY() / (gamePanel.getHeight() / ROWS);
                col = e.getX() / (gamePanel.getWidth() / COLUMNS);

                if (Math.abs(row - selectedRow) + Math.abs(col - selectedCol) == 1) {
                    swapCells(selectedRow, selectedCol, row, col);
                    while (checkThreeInARow(false)) {
                        makeCellsFall();
                    }
                    
                    // send score to server
                    if(akk.CheckEnd(score, colorScore)){
                        KurClient.this.dispose(); // Закрываем текущее окно
                        showMainMenu(username);
                    }

                    if(!isMovePossible()){
                        JFrame ggFrame = new JFrame();
                        JOptionPane.showMessageDialog(ggFrame, "Game Over");
                        KurClient.this.dispose(); // Закрываем текущее окно
                        showMainMenu(username);
                    }
                }
                
            }
        });

        setVisible(true);
    }

    private void initializeBoard() {
        Random random = new Random();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                if (newClrs)
                    board[i][j] = COLORS_FREE_2[random.nextInt(COLORS_FREE_2.length)];
                else
                    board[i][j] = COLORS_FREE[random.nextInt(COLORS_FREE.length)];
            }
        }
        int colorChck = random.nextInt(5);
        if (newClrs){
            actualColor = COLORS_FREE_2[colorChck];
            switch (colorChck) {
            case 0:
                colorCheck = "MAGENTA";
                break;
            case 1:
                colorCheck = "BLUE";
                break;
            case 2:
                colorCheck = "CYAN";
                break;
            case 3:
                colorCheck = "ORANGE";
                break;
            case 4:
                colorCheck = "PINK";
                break;
            default:
                colorCheck = "MAGENTA";
                break;
            }
        }
        else{
            actualColor = COLORS_FREE[colorChck];
            switch (colorChck) {
            case 0:
                colorCheck = "RED";
                break;
            case 1:
                colorCheck = "BLUE";
                break;
            case 2:
                colorCheck = "GREEN";
                break;
            case 3:
                colorCheck = "YELLOW";
                break;
            case 4:
                colorCheck = "PINK";
                break;
            default:
                colorCheck = "RED";
                break;
            }
        }
        
        scoreLabel.setText("Score: " + score + "/500   " + colorCheck + " blocks: " + (12 - colorScore) + "/12");
        
        while (checkThreeInARow(false)) {
            makeCellsFall();
        }
    }

    private void updateGamePanel() {
        gamePanel.removeAll();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                JPanel cell = new JPanel();
                cell.setBackground(board[i][j]);
                cells[i][j] = cell;
                gamePanel.add(cell);
            }
        }
        add(scoreLabel, BorderLayout.SOUTH);
        gamePanel.revalidate();
        gamePanel.repaint();
    }

    private boolean isMovePossible() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                for (int k = i-1; k < i+1; k+=2) {
                    if (0<=k && k<=8){
                        if (!board[i][j].equals(Color.GRAY) && !board[k][j].equals(Color.GRAY) && !board[i][j].equals(Color.DARK_GRAY) && !board[k][j].equals(Color.DARK_GRAY)) {
                            Color temp = board[i][j];
                            board[i][j] = board[k][j];
                            board[k][j] = temp;

                            if (checkThreeInARow(true)) {
                                board[k][j] = board[i][j];
                                board[i][j] = temp;  
                                return true;
                            }
                            board[k][j] = board[i][j];
                            board[i][j] = temp;
                        }
                    }
                }
                for (int l = j-1; l < j+1; l+=2) {
                    if (0<=l && l<=8){
                        if (!board[i][j].equals(Color.GRAY) && !board[i][l].equals(Color.GRAY) && !board[i][j].equals(Color.DARK_GRAY) && !board[i][l].equals(Color.DARK_GRAY)) {
                            Color temp = board[i][j];
                            board[i][j] = board[i][l];
                            board[i][l] = temp;

                            if (checkThreeInARow(true)) {
                                board[i][l] = board[i][j];
                                board[i][j] = temp;  
                                return true;
                            }
                            board[i][l] = board[i][j];
                            board[i][j] = temp;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void swapCells(int row1, int col1, int row2, int col2) {
        if (board[row1][col1].equals(Color.GRAY) || board[row2][col2].equals(Color.GRAY) || board[row1][col1].equals(Color.DARK_GRAY) || board[row2][col2].equals(Color.DARK_GRAY)) {
            return; // Do not allow gray cells to be swapped
        }

        Color temp = board[row1][col1];
        board[row1][col1] = board[row2][col2];
        board[row2][col2] = temp;

        int tttt = 0;
        while (checkThreeInARow(false)) {
            tttt = 1;
            makeCellsFall();
        }
        if (tttt == 0){ 
            board[row2][col2] = board[row1][col1];
            board[row1][col1] = temp;   
        }
        
        updateGamePanel();        
        scoreLabel.setText("Score: " + score + "/500   " + colorCheck + " blocks: " + (12 - colorScore) + "/12");
    }

    private boolean checkThreeInARow(boolean justCheck) {
        boolean foundThreeInARow = false;
        boolean[][] toDisappear = new boolean[ROWS][COLUMNS];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS - 2; j++) {
                int count = 1;
                while (j + count < COLUMNS && board[i][j] == board[i][j + count]) {
                    count++;
                }
                if (count >= 3) {
                    if (!board[i][j].equals(Color.GRAY) && !board[i][j].equals(Color.DARK_GRAY)) {
                        for (int k = 0; k < count; k++) {
                            toDisappear[i][j + k] = true;
                        }
                        foundThreeInARow = true;
                    }
                }
                j += count - 1;
            }
        }
        for (int j = 0; j < COLUMNS; j++) {
            for (int i = 0; i < ROWS - 2; i++) {
                int count = 1;
                while (i + count < ROWS && board[i][j] == board[i + count][j]) {
                    count++;
                }
                if (count >= 3) {
                    if (!board[i][j].equals(Color.GRAY) && !board[i][j].equals(Color.DARK_GRAY)) {
                        for (int k = 0; k < count; k++) {
                            toDisappear[i + k][j] = true;
                        }
                        foundThreeInARow = true;
                    }
                }
                i += count - 1;
            }
        }
        if(!justCheck){
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLUMNS; j++) {
                    if (toDisappear[i][j]) {
                        score += 10;
                        if(board[i][j].equals(actualColor)) {
                            if (colorScore > 0) colorScore -=1;
                        }
                        board[i][j] = null;
                    }
                }
            }
        }
        return foundThreeInARow;
    }
    
    private void makeCellsFall() {
        for (int j = 0; j < COLUMNS; j++) {
            for (int i = ROWS - 1; i >= 0; i--) {
                if (board[i][j] == null) {
                    for (int k = i - 1; k >= 0; k--) {
                        if (board[k][j] != null && (!board[k][j].equals(Color.GRAY) && !board[k][j].equals(Color.DARK_GRAY))) {
                            board[i][j] = board[k][j];
                            board[k][j] = null;
                            break;
                        }
                    }
                }
            }
        }

        updateGamePanel();

        fillEmptyCells();
    }

    private void fillEmptyCells() {
        for (int j = 0; j < COLUMNS; j++) {
            for (int i = 0; i < ROWS; i++) {
                if (board[i][j] == null) {
                    if (newClrs)
                        board[i][j] = COLORS_FREE_2[new Random().nextInt(COLORS_FREE_2.length)];
                    else
                        board[i][j] = COLORS_FREE[new Random().nextInt(COLORS_FREE.length)];

                    
                }
            }
        }

        updateGamePanel();
    }
    
    private static void showMainMenu(String username) {
        JFrame mainFrame = new JFrame("Main Menu");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(300, 200);
        mainFrame.setLayout(new GridLayout(2, 1));

        JButton playButton = new JButton("Играть");
        playButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainFrame.dispose();
                boolean newColors = akk.getAccountColors();
                KurClient game = new KurClient(username, newColors);
            }
        });
        mainFrame.add(playButton);

        JButton shopButton = new JButton("Магазин");
        shopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainFrame.dispose();
                showShopWindow(username); // Отправляем информацию о действии "Магазин" на сервер
            }
        });
        mainFrame.add(shopButton);

        mainFrame.setVisible(true);
    }

    private static void showShopWindow(String username) {
        JFrame shopFrame = new JFrame("Shop");
        shopFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        shopFrame.setSize(300, 300);
        shopFrame.setLayout(new GridLayout(3, 1));

        JLabel accountLabel = new JLabel("Счет вашего аккаунта: " + akk.getAccountScore()); // Получаем количество очков от сервера
        shopFrame.add(accountLabel);

        // Добавляем кнопку "Новые цвета"
        JButton newColorsButton = new JButton("Новые цвета");
        newColorsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                akk.BuyColors();
                shopFrame.dispose(); // Закрываем окно счета аккаунта
                showShopWindow(username);
            }
        });
        shopFrame.add(newColorsButton);

        // Добавляем кнопку "Вернуться в главное меню"
        JButton returnButton = new JButton("Вернуться в главное меню");
        returnButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                shopFrame.dispose(); // Закрываем окно счета аккаунта
                showMainMenu(username);
            }
        });
        shopFrame.add(returnButton);

        shopFrame.setVisible(true);
    }
}

class Аccount{
    
    static String username;
    static String password;
    
    static boolean CheckEnd(int score, int colorScore){
        try{
            Socket socket = new Socket("localhost", 8888);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("END_CHECK");
            out.println(score);
            out.println(colorScore);
            String action = in.readLine();

            in.close();
            out.close();
            socket.close();

            if (!action.isEmpty() && action.equals("END")){
                try{
                    socket = new Socket("localhost", 8888);
                    out = new PrintWriter(socket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out.println("LVL_PASSED");
                    out.println(username);
                    out.println(score);

                    in.close();
                    out.close();
                    socket.close();
                    return true;
                } catch (IOException eee) {
                    eee.printStackTrace();
                }
            }
        } catch (IOException ee) {
            ee.printStackTrace();
        }
        return false;
    }
    
    static void BuyColors(){
        try {
            Socket socket = new Socket("localhost", 8888);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("BUY");
            out.println(username);
            out.println(username);
            socket.close();

        } catch (IOException ee) {
            ee.printStackTrace();
        }
    }
    
    static Color[][] getAccountLevel(Color[] COLORS_2, Color[] COLORS, int ROWS, int COLUMNS, boolean newClrs) {
        Color[][] board = new Color[ROWS][COLUMNS];
        try {
            Socket socket = new Socket("localhost", 8888);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("GET_LVL");
            out.println(username);
            out.println(username);
            
            String lvl = in.readLine();
            
            String[] tokens = lvl.split(" \\+ ");
            for (int i = 0; i < ROWS; i++) {
                String[] token = tokens[i].split(" ");
                for (int j = 0; j < COLUMNS; j++) {
                    int colorNumber = Integer.parseInt(token[j]);
                    if (newClrs)
                        board[i][j] = COLORS_2[colorNumber - 1];
                    else
                        board[i][j] = COLORS[colorNumber - 1];
                }
            }
            
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return board;
    }
    
    static boolean getAccountColors() {
        try {
            Socket socket = new Socket("localhost", 8888);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("GET_COLORS");// Отправляем запрос на получение счета
            out.println(username);
            out.println(username);
            String colors = in.readLine(); // Получаем количество очков от сервера
            out.close();
            socket.close();
            
            if(colors.equals("1")){
                return true;
            }
            else 
                return false;
            
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    static String getAccountScore() {
        try {
            Socket socket = new Socket("localhost", 8888);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("GET_SCORE");// Отправляем запрос на получение счета
            out.println(username);
            out.println(username);
            String scoree = in.readLine(); // Получаем количество очков от сервера

            out.close();
            socket.close();

            return scoree;
        } catch (IOException e) {
            e.printStackTrace();
            return "N/A";
        }
    }
        
    static boolean createAccount() {
        try {
            Socket socket = new Socket("localhost", 8888);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("CREATE");
            out.println(username);
            out.println(password);
            String response = in.readLine();
            response = in.readLine();
            
            out.close();
            socket.close();
            switch (response) {
                case "OK" -> {
                    return true;
                }
                case "NO" -> {
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    static boolean sendLoginData() {
        try {
            Socket socket = new Socket("localhost", 8888);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Login");
            out.println(username);
            out.println(password);
            String response = in.readLine();
            
            out.close();
            socket.close();
            
            switch (response) {
                case "OK" -> {
                    return true;
                }
                case "FAIL" -> {
                    return false;
                }
            }
            
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}