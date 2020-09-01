package Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.text.SimpleDateFormat;


/**
 * Used for navigating Directories for both server and client
 */
public class FilesHandler {
    private final Path filesPath;
    private final Path configsPath;
    private Path currentPath;


    /**
     * Sets the folder paths for files and configs folder. Relative to the root direct of java files
     *
     * @param filesFolder string representation of where files folder should be.
     * @param configsPath string representation of where configs folder should be.
     */
    public FilesHandler(String filesFolder, String configsPath) {
        this.filesPath = Paths.get(filesFolder).toAbsolutePath();
        this.configsPath = Paths.get(configsPath).toAbsolutePath();

        if (!(createConfigsFolder() && createFilesFolder())) {
            System.out.println("Error locating / Creating folders. Check if you have privileges to write to the disk");
        }
        currentPath = this.filesPath;
    }

    /**
     * Gets the userInfo CSV, tries to find the fileName in the configs folder
     *
     * @return path to the user file
     */
    public Path getConfigFilePath(String fileName) {
        return Paths.get(configsPath.toString(), fileName);
    }


    /**
     * Checks if file folder exists, creates the folder if it does not.
     * Used to store files for transfer.
     *
     * @return Returns true if a new folder is created, or folder already exists, false if error during folder creation
     */
    private boolean createFilesFolder() {
        File ff = new File(String.valueOf(filesPath));
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
        File cf = new File(String.valueOf(configsPath));
        if (!cf.exists()) {
            // Creates folder if not exist
            return cf.mkdirs();
        }
        return true;
    }


    /**
     * List files in current directory
     *
     * @param mode V for verbose, F for simple Files
     * @return Ascii table of files in the directory.
     */
    public String listFiles(String mode) throws FileNotFoundException {
        // String builder for building the response
        StringBuilder sb = new StringBuilder();

        // The directory we're working in
        File dir = new File(String.valueOf(currentPath)); // no dir , use last active
        File[] files;
        files = dir.listFiles();
        if (files == null) {
            throw new FileNotFoundException("-Directory invalid");
        }
        // Display current path
        sb.append("+").append(currentPath).append("\r\n");
        // Verbose Mode
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
        }
        // Standard Mode
        else if (mode.toUpperCase().equals("F")) {
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
    }


    /**
     * Overload version of List files, also tries to change directories first.
     *
     * @param mode Mode of list, either F or V
     * @param dir  Attempted to list this dir
     * @return Null if no such dir exists, or
     */
    public String listFiles(String mode, String dir) throws FileNotFoundException {
        //Checks if it's a valid dir, if so ,changes the dir then lists all the files.
        if (changeDir(dir)) {
            return listFiles(mode);
        }
        // Invalid dir
        return "-Directory invalid";
    }

    /**
     * Checks the current path by navigating folders.
     * todo do i want to restrict folder access?
     *
     * @param dir string representation of folder to change to
     * @return true if changed.
     */
    public boolean changeDir(String dir) {
        // Accounting for different systems
        dir = dir.replace('\\', File.separatorChar);
        dir = dir.replace('/', File.separatorChar);
        //
        String newDir;
        File f;

        if ((dir.charAt(0) == '~') && (dir.length() == 1)) {
            // go to default folder
            currentPath = filesPath;
            return true;
        } else if (dir.equals("..")) {
            // Go up a level
            currentPath = currentPath.getParent();
            return true;
        } else if (dir.charAt(0) == '@') {
            // absolute path
            newDir = dir.substring(1);
            f = new File(newDir);
            if (f.isDirectory()) {
                currentPath = Paths.get(newDir);
                return true;
            }
        } else {
            // Relative to current folder
            newDir = currentPath.toAbsolutePath() + File.separator + dir;
            f = new File(newDir);
            if (f.isDirectory()) {
                currentPath = Paths.get(newDir);
                return true;
            }
        }
        return false;
    }

    /**
     * Deletes a file in the current folder
     *
     * @param fileName file to delete
     * @return Message about whether deleted successfully
     */
    public String delete(String fileName) {
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

    /**
     * Used to rename a file
     *
     * @param oldName old name of file
     * @param newName new name of file
     * @return Message about if rename was sucessful
     */
    public String rename(String oldName, String newName) {
        File oldFile = new File(currentPath.toAbsolutePath() + File.separator + oldName);
        File newFile = new File(currentPath.toAbsolutePath() + File.separator + newName);

        if (oldName.equals(newName)) {
            return "-File wasn't renamed as new name is same as old name";
        }
        if (oldFile.renameTo(newFile)) {
            return String.format("+%s renamed to %s", oldName, newName);
        } else {
            return "-File wasn't renamed because it's protected";
        }
    }


    /**
     * Checks if a file exists
     *
     * @param fileName String of file name
     * @return True if file exists.
     */
    public boolean fileExist(String fileName) {
        File file = new File(currentPath.toAbsolutePath() + File.separator + fileName);
        return file.exists();
    }

    /**
     * Generates the "File" class file in the base files folder or current dir.
     *
     * @param fileName   name of file
     * @param currentDir Use current dir or base files dir
     * @return File object
     */
    public File generateFile(String fileName, boolean currentDir) {

        return currentDir?
                new File(currentPath.toAbsolutePath() + File.separator + fileName)
                : new File(filesPath.toAbsolutePath() + File.separator + fileName);
    }


    /**
     * Checks if theres enough space to the file in current path
     *
     * @param fileSize file size to fit in current path
     * @return True if can fit, False if can't fit or can't get information on usable space
     */
    public boolean enoughFreeSpace(long fileSize,boolean currentDir) {
        try {
            long freeSpace = currentDir?
                    Files.getFileStore(currentPath).getUsableSpace():
                    Files.getFileStore(filesPath).getUsableSpace();

            return (freeSpace > fileSize);
        } catch (IOException e) {
            // Can't get usable space,
            return false;
        }

    }

    /**
     * Gets the current path
     *
     * @return Current path
     */
    public Path getCurrentPath() {
        return currentPath;
    }
}

