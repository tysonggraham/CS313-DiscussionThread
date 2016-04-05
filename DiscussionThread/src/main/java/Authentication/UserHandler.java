/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Authentication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author student
 */
public class UserHandler implements Runnable {
    volatile File _userFile;
    volatile Boolean _stop;

    /**
     * Constructor
     * @param file
     */
    public UserHandler(File file) {
        _userFile = file;
        _stop = false;
    }

    /**
     * GET USERS
     * Returns the list of existing users or null if there's no user.
     * @return List<User>:  List of users
     *                      null if there's no user
     * @throws IOException 
     */
    synchronized public List<User> getUsers() throws IOException {
        List<User> users = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(_userFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] user = line.split(",");
                users.add(new User(user[0], user[1]));
            }
        } catch (FileNotFoundException e) {
            return null;
        }
        return users;
    }

    /**
     * SAVE USER
     * Save a new user in the file.
     * @param username
     * @param password
     * @return int: 1 if success
     *              0 if user already exists
     *              -1 if fails writing into file
     *              -2 if fails reading the file
     */
    synchronized public int saveUser(String username, String password) {
        // First check if user exists.
        int num_user = checkUsername(username);
        int result = 0;
        // Then create the new user and save its credentials.
        switch (num_user) {
            case 1:
                return 0;
            case 0:
            case -1:
            case -2:
                try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(_userFile, true)))) {
                    out.println(username + "," + password);
                    result = 1;
                } catch (IOException e) {
                    result = -1;
                }
            default:
                break;
        }
        return result;
    }

    /**
     * CHECK CREDENTIALS
     * Checks if credentials are valid.
     * @param username
     * @param password
     * @return int: 1 if valid
     *              0 if invalid credentials
     *              -1 if there is no user
     *              -2 if failed reading the user file
     */
    synchronized public int checkCredentials(String username, String password) {
        List<User> users = new ArrayList<>();
        int result = 0;
        try {
            users = getUsers();
            if (users == null) {
                return -1;
            } else {
                for(User user : users){
                    if (username.equals(user.getUsername()) && password.equals(user.getPassword())) {
                        return 1;
                    }
                }
            }
        } catch (IOException e) {
            return -2;
        }
        return result;
    }

    /**
     * CHECK USERNAME
     * Checks if a given username exists
     * @param username
     * @return int: 1 if exists
     *              0 if doesn't exist
     *              -1 if there's no user
     *              -2 if failed reading the user file
     */
    synchronized public int checkUsername(String username) {
        List<User> users = new ArrayList<>();
        try {
            int exist = 0;
            users = getUsers();
            if (users == null) {
                return -1;
            } else {
                String line;
                for(User user : users){
                    if (username.equals(user.getUsername())) {
                        return 1;
                    }
                }
            }
            return exist;
        } catch (IOException e) {
            return -2;
        }
    }

    @Override
    public void run() {
        while (!_stop) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // This will never happen.
            }
        }
    }

    public void stop() {
        _stop = true;
    }
}