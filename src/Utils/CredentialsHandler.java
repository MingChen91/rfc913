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
public class CredentialsHandler {
    public enum LoginState {INIT, USER_FOUND, ACCT_FOUND, PASS_FOUND, LOGGED_IN}

    public enum CdirState {INIT, IN_PROGRESS, VERIFIED}

    private String user;
    private String acct;
    private String pass;
    private LoginState loginState;
    private final Path configFile;
    private final List<List<String>> userInfos;


    public CredentialsHandler(Path configFile) {
        this.configFile = configFile;
        userInfos = new ArrayList<>();
        loadCredentials();
        loginState = LoginState.INIT;
    }

    public static void main(String[] args) {
        FilesHandler fh = new FilesHandler("Server/Files", "Server/Configs");
        CredentialsHandler c = new CredentialsHandler(fh.getConfigFilePath("userInfo.csv"));
        c.checkUser("user3");
        c.displayCurrentUser();
        System.out.println("state :" + c.getLoginState());
        c.checkAcct("account3");
        System.out.println("state :" + c.getLoginState());
        c.checkPass("pass3");
        System.out.println("state :" + c.getLoginState());
        c.checkUser("user1");
        System.out.println("state :" + c.getLoginState());
    }

    /**
     * Loads the credentials file into an 2d array list of strings.
     */
    public void loadCredentials() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(String.valueOf(configFile)));
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",", -1); // -1 limit enables include empty strings
                userInfos.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(userInfos);
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
                    loginState = LoginState.LOGGED_IN;
                } else if (this.acct.equals("")) {
                    // no account needed, enter password next
                    loginState = LoginState.ACCT_FOUND;
                } else {
                    // enter account and password next
                    loginState = LoginState.USER_FOUND;
                }
                return;
            }
        }
        loginState = LoginState.INIT;
    }

    public void checkAcct(String acct) {
        if (loginState == LoginState.USER_FOUND) {
            if (this.acct.equals(acct)) {
                loginState = LoginState.ACCT_FOUND;
            }
        } else if (loginState == LoginState.PASS_FOUND) {
            if (this.acct.equals(acct)) {
                loginState = LoginState.LOGGED_IN;
            }
        }
    }

    public void checkPass(String pass) {
        // account is already found, correct password => logs in
        if (loginState == LoginState.ACCT_FOUND) {
            if (this.pass.equals(pass)) {
                loginState = LoginState.LOGGED_IN;
            }
        }
        // haven't specified account but user and password is ok,
        else if (loginState == LoginState.USER_FOUND) {
            if (this.pass.equals(pass)) {
                loginState = LoginState.PASS_FOUND;
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

    /**
     * Getter
     * @return Current login state
     */
    public LoginState getLoginState() {
        return loginState;
    }

}
