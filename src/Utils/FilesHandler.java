package Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.text.SimpleDateFormat;
import java.util.Arrays;


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
//        System.out.println(fh.listFiles("V"));
//        fh.changeDir("~");
//        System.out.println(fh.getCurrentPath());
//        fh.changeDir("~/Folder1");
//        System.out.println(fh.getCurrentPath());
//        fh.changeDir("/Folder2");
//        System.out.println(fh.getCurrentPath());
//        fh.changeDir("C:\\rfc913\\Server\\Files\\Folder3");
//        System.out.println(fh.getCurrentPath());
        System.out.println(fh.fileExist("cat.png"));
        System.out.println(fh.rename("cat1.png","cat2.png"));
        System.out.println(fh.rename("cat2.png","cat2.png"));
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
        //Checks if it's a valid dir, if so ,changes the dir then lists all the files.
        if (changeDir(dir)) {
            return listFiles(mode);
        } else {
            return null;
        }
    }

    /**
     * @param dir
     * @return
     */
    public boolean changeDir(String dir) {
        // Accounting for different systems
        dir = dir.replace('\\', File.separatorChar);
        dir = dir.replace('/', File.separatorChar);

        if (dir.charAt(0) == '~') {
            if (dir.length() == 1) {
                // go to root folder
                currentPath = filesFolder;
                return true;
            } else if (dir.charAt(1) == File.separatorChar) {
                //relative to root folder
                String newDir = currentPath.toAbsolutePath() + dir.substring(1);
                System.out.println(newDir);
                File f = new File(newDir);
                if (f.isDirectory()) {
                    currentPath = Paths.get(newDir);
                    return true;
                }
            }
        } else if (dir.charAt(0) == File.separatorChar) {
            // Relative to current folder
            String newDir = currentPath.toAbsolutePath() + File.separator + dir;
            File f = new File(newDir);
            if (f.isDirectory()) {
                currentPath = Paths.get(newDir);
                return true;
            }
        } else {
            //Absolute navigation
            File f = new File(dir);
            // Cannot go above root folder
            if (dir.compareTo(String.valueOf(filesFolder)) < 0) {
                return false;
            }
            if (f.isDirectory()) {
                currentPath = Paths.get(dir);
                return true;
            }
        }
        return false;
    }

    public String kill(String fileName) {
        Path fileToKill = new File(currentPath.toAbsolutePath() + File.separator + fileName).toPath();
        try {
            Files.delete(fileToKill);
            return "+" + fileName + " deleted";
        } catch (NoSuchFileException ex) {
            return "-Not deleted because no file exists";
        } catch (IOException e) {
            return "-Not deleted because file is protected";
        }
    }

    public boolean fileExist(String fileName) {
        File file = new File(currentPath.toAbsolutePath() + File.separator + fileName);
        return file.exists();
    }

    public String rename(String oldName, String newName) {
        File oldFile = new File(currentPath.toAbsolutePath() + File.separator + oldName);
        File newFile = new File(currentPath.toAbsolutePath() + File.separator + newName);

        if (oldName.equals(newName)) {
            return "-File wasn't renamed as new name is same as old name";
        }
        if (oldFile.renameTo(newFile)) {
            return String.format("+%s renamed to %s",oldName,newName);
        } else {
            return "-File wasn't renamed because it's protected";
        }

    }

    public Path getCurrentPath() {
        return currentPath;
    }
}
