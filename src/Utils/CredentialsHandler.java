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

    private String acct;
    private String pass;
    private LoginState loginState;
    private final Path configFile;
    private final List<List<String>> userInfos;


    /**
     * Constructor needs the path to the config file.
     *
     * @param configFile where the config file for user details is located
     */
    public CredentialsHandler(Path configFile) throws IOException {
        try {
            this.configFile = configFile;
            userInfos = new ArrayList<>();
            loadCredentials();
            loginState = LoginState.INIT;
        } catch (IOException e) {
            throw new IOException("Could not load userInfo.csv for login credentials");
        }
    }


    /**
     * Loads the credentials csv
     *
     * @throws IOException Config file not found or could not read form it
     */
    public void loadCredentials() throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(String.valueOf(configFile)));
        String line;
        while ((line = br.readLine()) != null) {
            String[] values = line.split(",", -1); // -1 limit enables include empty strings
            userInfos.add(Arrays.asList(values));
        }

    }

    /**
     * Finds if the user exists in the config file
     * Updates the state loginState
     *
     * @param user user name
     */
    public void checkUser(String user) {
        for (List<String> userInfo : userInfos) {
            if (userInfo.get(0).equals(user)) {
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

    /**
     * Checks if the account matches the current user's account
     * Updates the state loginState
     *
     * @param acct account name
     */
    public void checkAcct(String acct) {
        if (loginState == LoginState.USER_FOUND) {
            // already found user
            if (this.acct.equals(acct)) {
                loginState = LoginState.ACCT_FOUND;
            }
        } else if (loginState == LoginState.PASS_FOUND) {
            // already matched pass
            if (this.acct.equals(acct)) {
                loginState = LoginState.LOGGED_IN;
            }
        }
    }

    /**
     * Checks if the password matches the current user's password
     * Updates the state loginState
     *
     * @param pass password
     */
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
     * Getter for current login state.
     *
     * @return Current login state
     */
    public LoginState getLoginState() {
        return loginState;
    }
}
