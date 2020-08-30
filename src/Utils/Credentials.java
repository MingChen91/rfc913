package Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Used to open the credentials file perform functions related to the login credentials
 */
public class Credentials {
    public enum State {INIT, USER_FOUND, ACCT_FOUND, LOGGED_IN}

    private String user;
    private String acct;
    private String pass;
    private State state;
    private final Path configFile;
    private final List<List<String>> userInfos;


    public Credentials(Path configFile) {
        this.configFile = configFile;
        userInfos = new ArrayList<>();
        loadCredentials();
        state = State.INIT;
    }

    public static void main(String[] args) {
        FilesHandler fh = new FilesHandler("Server/Files", "Server/Configs");
        Credentials c = new Credentials(fh.getConfigFilePath("userInfo.csv"));
        c.checkUser("user3");
        c.displayCurrentUser();
        System.out.println("state :" + c.getState());
        c.checkAcct("account3");
        System.out.println("state :" + c.getState());
        c.checkPass("pass3");
        System.out.println("state :" + c.getState());
        c.checkUser("user1");
        System.out.println("state :" + c.getState());
    }

    /**
     * Loads the credentials file into an 2d array list of strings.
     */
    public void loadCredentials() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(String.valueOf(configFile)));
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",",-1); // -1 limit enables include empty strings
                userInfos.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(userInfos);
    }

    /**
     * Finds if the user exists in the config file
     *
     * @param user user name
     * @return boolean for if the user is found. When found initialise user params in the class
     */
    public void checkUser(String user) {
        for (List<String> userInfo : userInfos) {
            if (userInfo.get(0).equals(user)) {
                this.user = userInfo.get(0);
                this.acct = userInfo.get(1);
                this.pass = userInfo.get(2);
                // No password needed, logged in directly
                if (this.acct.equals("") && this.pass.equals("")) {
                    state = State.LOGGED_IN;
                } else if (this.acct.equals("")) {
                    // no account needed, enter password next
                    state = State.ACCT_FOUND;
                } else {
                    // enter account and password next
                    state = State.USER_FOUND;
                }
                return;
            }
        }
        state = State.INIT;
    }

    public void checkAcct(String acct) {
        if (state == State.USER_FOUND) {
            if (this.acct.equals(acct)) {
                state = State.ACCT_FOUND;
            }
        }
    }

    public void checkPass(String pass) {
        if (state == State.ACCT_FOUND) {
            if (this.pass.equals(pass)) {
                state = State.LOGGED_IN;
            }
        }
    }

    /**
     * displays the current users info
     */
    public void displayCurrentUser() {
        System.out.println(user);
        System.out.println(acct);
        System.out.println(pass);
    }

    public State getState() {
        return state;
    }
}
