package Utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utilities used for connecting with the IO streams and file IO
 */
public class FilesHandler {
    private final Path filesFolder;
    private final Path configsFolder;

    public static void main(String[] args) {
        FilesHandler fh = new FilesHandler("Client/Files", "Client/Configs");
    }

    /**
     * Sets the folder paths for files and configs folder. Relative to the root direct of java files
     *
     * @param filesFolder   string representation of where filesFolder should be.
     * @param configsFolder string representation of where configsFolder should be.
     */
    public FilesHandler(String filesFolder, String configsFolder) {
        this.filesFolder = Paths.get(filesFolder).toAbsolutePath();
        this.configsFolder = Paths.get(configsFolder).toAbsolutePath();

        if (!(createConfigsFolder() && createFilesFolder())) {
            System.out.println("Error locating / Creating folders. Check if you have privileges to write to the disk");
        } else {
            System.out.println("Files and Configs Folders located, ready to go");
        }
    }

    /**
     * Checks if file folder exists, creates the folder if it does not.
     * Used to store files for transfer.
     *
     * @return Returns true if a new folder is created, or folder already exists, false if error during folder creation
     */
    private boolean createFilesFolder() {
        File ff = new File(String.valueOf(filesFolder));
        if (!ff.exists()) {
            // Creates folder if not exist
            return ff.mkdirs();
        }
        return true;
    }


    /**
     * Checks if configs folder exists, creates the folder if it does not.
     * Used to store files for transfer.
     *
     * @return Returns true if a new folder is created, or folder already exists, false if error during folder creation
     */
    private boolean createConfigsFolder() {
        File cf = new File(String.valueOf(configsFolder));
        if (!cf.exists()) {
            // Creates folder if not exist
            return cf.mkdirs();
        }
        return true;
    }

}
