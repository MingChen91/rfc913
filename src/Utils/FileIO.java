package Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utilities used for connecting with the IO streams and file IO
 */
public class FileIO {
    public static void main(String[] args) {
        System.out.println((createFtpFolder("Client/ftp")));
    }

    /**
     * Checks if a folder exists, creates the folder if it does not. Used to store files for transfer.
     * Creates folders under the root folder of project
     *
     * @param folderName name of the folder
     * @return Returns true if a new folder is created, or folder already exists, false if error during folder creation
     */
    public static boolean createFtpFolder(String folderName) {
        //
        Path rootPath = Paths.get(folderName).toAbsolutePath();
        File folder = new File(String.valueOf(rootPath));
        if (!folder.exists()) {
            // Creates folder if not exist
            return folder.mkdirs();
        }
        return true;
    }
}
