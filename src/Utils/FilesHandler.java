package Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;


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
        fh.listFiles();
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
        currentPath = this.filesFolder;
    }

    /**
     * Gets the userInfo CSV, tries to find the fileName in the configs folder
     * @return path to the user file
     */
    public Path getConfigFilePath(String fileName){
        return Paths.get(configsFolder.toString(),fileName);
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


    public String listFiles(){
        String[] files;
        StringBuilder sb = new StringBuilder();

        File dir  = new File(String.valueOf(currentPath));
        files = dir.list();

        assert files != null;
        for(String file : files){
            try {
                BasicFileAttributes attr = Files.readAttributes(Paths.get(String.valueOf(currentPath),file) , BasicFileAttributes.class);
                System.out.println(file);
                System.out.println("lastAccessTime : " + attr.lastAccessTime());
                System.out.println("size:" + attr.size());

                FileOwnerAttributeView attr2 = Files.getFileAttributeView(
                        Paths.get(String.valueOf(currentPath),file),
                        FileOwnerAttributeView.class);

                System.out.println("owner:" + attr2.getOwner());
                
            } catch (IOException e) {
                e.printStackTrace();
            }
            sb.append(file).append("\r\n");
        }
        return sb.toString();
    }


    public String listFiles(String path) {
        return "";
    }

}
