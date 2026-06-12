/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package loginapp;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;

public class QuickChat {
    private static Scanner sc = new Scanner(System.in);
    
    public static void main(String[] args) {
       
        Message.loadTestMessages();
        
        boolean running = true;
        while (running) {
            System.out.println("\n=== QuickChat Menu ===");
            System.out.println("1) Send Message");
            System.out.println("2) View Sent Messages"); 
            System.out.println("3) Search Messages");
            System.out.println("4) View Stored Messages Options"); // Part 3
            System.out.println("5) Exit");
            System.out.print("Choose: ");
            
            String choice = sc.nextLine();
            
            if (choice.equals("1")) {
                sendMessage();
            } 
            else if (choice.equals("2")) {
                viewMessages();
            } 
            else if (choice.equals("3")) {
                searchMessages();
            } 
            else if (choice.equals("4")) {
                
                System.out.println("\na) Display sender + recipient");
                System.out.println("b) Display longest message");
                System.out.println("c) Search by message ID");
                System.out.println("d) Search by recipient");
                System.out.println("e) Delete by hash");
                System.out.println("f) Display report");
                System.out.print("Choose option: ");
                String sub = sc.nextLine().toLowerCase();
                
                if (sub.equals("a")) Message.displaySenderRecipient();
                else if (sub.equals("b")) Message.displayLongestMessage();
                else if (sub.equals("c")) Message.searchByMessageID();
                else if (sub.equals("d")) Message.searchByRecipient();
                else if (sub.equals("e")) Message.deleteByHash();
                else if (sub.equals("f")) Message.displayReport();
                else System.out.println("Invalid option");
            } 
            else if (choice.equals("5")) {
                System.out.println("Goodbye!");
                running = false;
            } 
            else {
                System.out.println("Invalid choice");
            }
        }
        sc.close();
    }
    
    
    public static void sendMessage() {
        System.out.print("Enter recipient number: ");
        String recipient = sc.nextLine();
        
        System.out.print("Enter message: ");
        String content = sc.nextLine();
        
        Message msg = new Message(recipient, content);
        
        if (!msg.checkRecipientCell()) {
            System.out.println("Error: Recipient must start with +27 and be 12 digits. Example: +27831234567");
            return;
        }
        
        if (!msg.checkMessageLength()) {
            System.out.println("Error: Message too long. Max 250 characters.");
            return;
        }
        
        Message.addMessageToArray(recipient, content);
        System.out.println("Message successfully sent!");
        System.out.println("Message ID: " + msg.getMessageID());
        System.out.println("Hash: " + msg.getHash());
    }
    
    
    public static void viewMessages() {
        System.out.println("\n--- All Stored Messages ---");
        Message.displayReport();
    }
    
    
    public static void searchMessages() {
        Message.loadFromFile();
        System.out.print("Enter keyword to search: ");
        String keyword = sc.nextLine().toLowerCase();
        
        boolean found = false;
        System.out.println("\n--- Search Results ---");
        for (int i = 0; i < Message.storedMessages.length(); i++) {
            JSONObject msg = Message.storedMessages.getJSONObject(i);
            String content = msg.getString("content").toLowerCase();
            if (content.contains(keyword)) {
                System.out.println("Recipient: " + msg.getString("recipient") + 
                                 " | Message: " + msg.getString("content"));
                found = true;
            }
        }
        if (!found) System.out.println("No messages found with keyword: " + keyword);
    }

   
    static class Message {
        private String messageID;
        private String recipient;
        private String content;
        private String hash;
        private static final int MAX_LENGTH = 250;
        
        
        public static JSONArray storedMessages = new JSONArray();
        private static Scanner scanner = new Scanner(System.in);
        
        public Message(String recipient, String content) {
            this.recipient = recipient;
            this.content = content;
            this.messageID = generateMessageID(recipient);
            this.hash = generateMessageHash(messageID, content);
        }
        
        public String getMessageID() { return messageID; }
        public String getHash() { return hash; }
        
        private String generateMessageID(String recipient) {
            Random rand = new Random();
            StringBuilder id = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                id.append(rand.nextInt(10));
            }
            String last3 = recipient.length() >= 3 ? recipient.substring(recipient.length() - 3) : recipient;
            return id.toString() + last3;
        }
        
        private String generateMessageHash(String messageID, String content) {
            String first2ID = messageID.substring(0, 2);
            String first2Content = content.length() >= 2 ? 
                content.substring(0, 2).toUpperCase() : content.toUpperCase();
            return messageID + ":" + first2ID + ":" + first2Content;
        }
        
        public boolean checkRecipientCell() {
            return recipient.matches("^\\+27\\d{9}$");
        }
        
        public boolean checkMessageLength() {
            return content.length() <= MAX_LENGTH;
        }
        
        public static void loadTestMessages() {
            if (storedMessages.length() == 0) {
                addMessageToArray("0838884567", "It is dinner time !");
                addMessageToArray("0838884567", "Where are you? You are late! I have asked you to be on time.");
                addMessageToArray("+27838884567", "Ok, I am leaving now.");
                addMessageToArray("+27838884567", "Hi MKay.. Can you please send me the report?");
                addMessageToArray("0722263888", "Was the dinner nice?");
            }
        }
        
        public static void addMessageToArray(String recipient, String content) {
            Message msg = new Message(recipient, content);
            JSONObject obj = new JSONObject();
            obj.put("messageID", msg.messageID);
            obj.put("recipient", msg.recipient);
            obj.put("content", msg.content);
            obj.put("hash", msg.hash);
            storedMessages.put(obj);
            saveToFile();
        }
        
        private static void saveToFile() {
            try {
                FileWriter file = new FileWriter("messages.json");
                JSONObject root = new JSONObject();
                root.put("storedMessages", storedMessages);
                file.write(root.toString(4));
                file.close();
            } catch (IOException e) {
                System.out.println("Error saving file: " + e.getMessage());
            }
        }
        
        public static void loadFromFile() {
            try {
                String content = new String(Files.readAllBytes(Paths.get("messages.json")));
                JSONObject root = new JSONObject(content);
                storedMessages = root.getJSONArray("storedMessages");
            } catch (Exception e) {
                storedMessages = new JSONArray();
            }
        }
        
        public static void displaySenderRecipient() {
            loadFromFile();
            System.out.println("\n--- Sender + Recipient ---");
            for (int i = 0; i < storedMessages.length(); i++) {
                JSONObject msg = storedMessages.getJSONObject(i);
                System.out.println("Sender: Me | Recipient: " + msg.getString("recipient"));
            }
        }
        
        public static void displayLongestMessage() {
            loadFromFile();
            String longest = "";
            for (int i = 0; i < storedMessages.length(); i++) {
                JSONObject msg = storedMessages.getJSONObject(i);
                String content = msg.getString("content");
                if (content.length() > longest.length()) {
                    longest = content;
                }
            }
            System.out.println("\n--- Longest Message ---");
            System.out.println(longest);
        }
        
        public static void searchByMessageID() {
            loadFromFile();
            System.out.print("Enter message ID: ");
            String id = scanner.nextLine();
            boolean found = false;
            for (int i = 0; i < storedMessages.length(); i++) {
                JSONObject msg = storedMessages.getJSONObject(i);
                if (msg.getString("messageID").equals(id)) {
                    System.out.println("\n--- Message Found ---");
                    System.out.println("Recipient: " + msg.getString("recipient") + 
                                     " | Message: " + msg.getString("content"));
                    found = true;
                    break;
                }
            }
            if (!found) System.out.println("Message ID not found.");
        }
        
        public static void searchByRecipient() {
            loadFromFile();
            System.out.print("Enter recipient number: ");
            String num = scanner.nextLine();
            System.out.println("\n--- Messages to " + num + " ---");
            boolean found = false;
            for (int i = 0; i < storedMessages.length(); i++) {
                JSONObject msg = storedMessages.getJSONObject(i);
                if (msg.getString("recipient").equals(num)) {
                    System.out.println("Message: " + msg.getString("content"));
                    found = true;
                }
            }
            if (!found) System.out.println("No messages found for this recipient.");
        }
        
        public static void deleteByHash() {
            loadFromFile();
            System.out.print("Enter message hash: ");
            String hash = scanner.nextLine();
            for (int i = 0; i < storedMessages.length(); i++) {
                JSONObject msg = storedMessages.getJSONObject(i);
                if (msg.getString("hash").equals(hash)) {
                    System.out.println("\nMessage: \"" + msg.getString("content") + 
                                     "\" successfully deleted.");
                    storedMessages.remove(i);
                    saveToFile();
                    return;
                }
            }
            System.out.println("Hash not found.");
        }
        
        public static void displayReport() {
            loadFromFile();
            System.out.println("\n--- Full Report ---");
            for (int i = 0; i < storedMessages.length(); i++) {
                JSONObject msg = storedMessages.getJSONObject(i);
                System.out.println("Hash: " + msg.getString("hash") + 
                                 " | Recipient: " + msg.getString("recipient") + 
                                 " | Message: " + msg.getString("content"));
            }
        }
    }
}