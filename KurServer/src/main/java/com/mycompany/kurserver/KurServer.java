package com.mycompany.kurserver;
//KurServer
import java.io.*;
import java.net.*;
import java.util.*;

public class KurServer {
    private String fileName = "C:\\Users\\snachi\\Desktop\\java\\Kursach\\user_credentials.ser";
    private String fileName_lvl = "C:\\Users\\snachi\\Desktop\\java\\Kursach\\level.txt";
    private List <User> dataBase = new ArrayList<>();
    public static void main(String[] args){
        new KurServer();
    }
    
    public KurServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(8888);
            System.out.println("Server is running...");
            
            //загрузка из файла
            loadBinaryFromFile();

            while (true) {
                Socket socket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
 
                String action = in.readLine();
                String username = in.readLine();
                String password = in.readLine();
                
                if (checkCredentials(username, password)) {
                    out.println("OK");
                } else if (!action.isEmpty() && action.equals("GET_SCORE")) {
                    System.out.println("No Way");
                    out.println(getAccountScore(username));
                } else if (!action.isEmpty() && action.equals("GET_LVL")) {
                    System.out.println("Way Is No");
                    out.println(getAccountLevel(username));
                } else if (!action.isEmpty() && action.equals("GET_COLORS")) {
                    System.out.println("What is that");
                    out.println(getAccountColors(username));
                } else if (!action.isEmpty() && action.equals("LVL_PASSED")) {
                    System.out.println("Yipeee");
                    updateAccount(username, password, false);
                } else if (!action.isEmpty() && action.equals("BUY")) {
                    System.out.println("SHOPPING");
                    updateAccount(username, password, true);
                } else if (!action.isEmpty() && action.equals("END_CHECK")) {
                    if(Integer.parseInt(username) > 500 && Integer.parseInt(password) == 0) out.println("END");
                    else out.println("NOT_END");
                } else {
                    out.println("FAIL");
                    System.out.println(action); 
                    System.out.println(username);
                    System.out.println(password);
                    if (action.equals("CREATE") && !action.isEmpty()) {
                        if(createAccount(username, password)){
                            out.println("OK");
                        }else{
                            out.println("NO");
                        }
                    } 
                }

                
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkCredentials(String username, String password) {
        for(User user : dataBase){
            if((user.username.equals(username)) && (user.password.equals(password))){
                return true;
            }
        }
        return false;
    }   

    private boolean createAccount(String username, String password) {
        for(User user : dataBase){
            if(user.username.equals(username)){
                return false;
            }
        }
        dataBase.add(new User(username, password, 0, 0, 0));
        saveBinaryToFile();
        return true;
    }
    
    private String getAccountScore(String username) {
        for(User user : dataBase){
            if(user.username.equals(username)){
                return String.valueOf(user.account);
            }
        }
        return "N/A";
    }
    
    private String getAccountLevel(String username) {
        String buff = "";
        for(User user : dataBase){
            if(user.username.equals(username)){
                try (BufferedReader reader = new BufferedReader(new FileReader(fileName_lvl))) {
                    for (int skip = 0; skip < (user.level*10); skip++) {
                        reader.readLine();
                    }
                    for (int i = 0; i < 9; i++) {
                        buff += (reader.readLine() + " + ");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("send: " + buff);
                return buff;
            }
        }
        return "N/A";
    }
    
    private String getAccountColors(String username) {
        for(User user : dataBase){
            if(user.username.equals(username)){
                return String.valueOf(user.account);
            }
        }
        return "0";
    }
    
    private void updateAccount(String username, String score, boolean newColors) {
        for(User user : dataBase){
            if(user.username.equals(username)){
                if(!newColors){
                    user.level ++;
                    user.account += Integer.parseInt(score);
                }
                if (newColors && (user.account >= 1000) && (user.purchase == 0)){
                    user.purchase = 1;
                    user.account -= 1000;
                }
            }
        }
        saveBinaryToFile();
    }

    private void saveBinaryToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(dataBase);
            oos.close();
            System.out.println("Data saved to binary file.");
        } catch (IOException e) {
            System.err.println("Error saving to file: " + e.getMessage());
        }
    }
    
    private void loadBinaryFromFile(){
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            List<User> loadedData = (List<User>) ois.readObject();
            dataBase.addAll(loadedData);
            ois.close();
            System.out.println("Data loaded from binary file.");
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading from file: " + e.getMessage());
        }
    }
    
    public static class User implements Serializable{
        private static final long serialVersionUID = 1L;
        private String username;
        private String password;
        private int account;
        private int level;
        private int purchase;
        
        public User(String username, String password, int account, int level, int purchase){
            this.username = username;
            this.password = password;
            this.account = account;
            this.level = level;
            this.purchase = purchase;
        }
    }
}