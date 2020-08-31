package Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.text.SimpleDateFormat;


/**
 * Utilities used for connecting with the IO streams and file IO
 */
public class FilesHandler {
    private final Path filesFolder;
    private final Path configsFolder;
    private Path currentPath;

    public static void main(String[] args) {
        FilesHandler fh = new FilesHandler("Server/Files", "Server/Configs");
        //System.out.println(fh.listFiles());
        System.out.println(fh.listFiles("V"));
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
            System.out.println("Files and Configs Folders located");
        }
        currentPath = this.filesFolder;
    }

    /**
     * Gets the userInfo CSV, tries to find the fileName in the configs folder
     *
     * @return path to the user file
     */
    public Path getConfigFilePath(String fileName) {
        return Paths.get(configsFolder.toString(), fileName);
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


    /**
     * List files
     *
     * @param mode
     * @return
     */
    public String listFiles(String mode) {
        // String builder for building the response
        StringBuilder sb = new StringBuilder();

        // The directory we're working in
        File dir = new File(String.valueOf(currentPath)); // no dir , use last active
        File[] files;
        files = dir.listFiles();
        if (files != null) {
            // Display current path
            sb.append(currentPath).append("\r\n");
            if (mode.toUpperCase().equals("V")) {
                // Attributes
                String fileName = "FileName";
                String modifiedTime = "ModifiedTime";
                String size = "Size(B)";
                String owner = "Owner";
                String R = "R";
                String W = "W";
                String X = "X";
                // Date Format
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy kk:mm");
                sb.append(String.format("| %-30.30s | %-20.20s | %-10.10s | %-20.20s | %1s%1s%1s |\r\n", fileName, modifiedTime, size, owner, R, W, X));
                sb.append("-".repeat(99)).append("\r\n");
                //Loop through all the files in the directory listing properties
                for (File file : files) {
                    try {
                        // Fetch attributes
                        BasicFileAttributes attr = Files.readAttributes(Paths.get(String.valueOf(file)), BasicFileAttributes.class);
                        FileOwnerAttributeView attr2 = Files.getFileAttributeView(Paths.get(String.valueOf(file)), FileOwnerAttributeView.class);
                        // Format  attributes into string
                        fileName = file.getName();
                        if (file.isDirectory()) fileName += "/.."; // add /.. if its a folder
                        modifiedTime = df.format(file.lastModified());
                        size = Long.toString(attr.size());
                        owner = attr2.getOwner().getName();
                        R = Files.isReadable(file.toPath()) ? "R" : "-";
                        W = Files.isWritable(file.toPath()) ? "W" : "-";
                        X = Files.isExecutable(file.toPath()) ? "X" : "-";
                        // Append to string
                        sb.append(String.format("| %-30.30s | %-20.20s | %-10.10s | %-20.20s | %1s%1s%1s |\r\n", fileName, modifiedTime, size, owner, R, W, X));
                    } catch (IOException e) {
                        fileName = file.getName();
                        sb.append(String.format("|%-30.30s |%s", fileName, "Access denied\r\n"));
                    }
                }
            } else if (mode.toUpperCase().equals("F")) {
                // Title bar
                sb.append(String.format("| %-30.30s |", "FileName")).append("\r\n");
                sb.append("-".repeat(34)).append("\r\n");
                // Loop through and display file names.
                for (File file : files) {
                    String fileName;
                    fileName = file.getName();
                    // append /.. if its a folder
                    if (file.isDirectory()) fileName += "/..";
                    sb.append(String.format("| %-30.30s |", fileName)).append("\r\n");
                }
            }
            return sb.toString();
        } else {
            return null;
        }

    }


    /**
     * @param mode Mode of list, either F or V
     * @param dir  Attempted to list this dir
     * @return Null if no such dir exists, or
     */
    public String listFiles(String mode, String dir) {
        File file = new File(dir);
        //Checks if it's a valid dir, if so ,changes the dir then lists all the files.
        if (file.isDirectory()) {
            currentPath = file.toPath();
            return listFiles(mode);
        } else {
            return null;
        }
    }
}
