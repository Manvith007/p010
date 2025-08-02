package util;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for file operations including backup, restore, and I/O
 */
public class FileOperations {
    private static final String BACKUP_DIR = "backups";
    private static final String DATA_DIR = "data";
    
    static {
        createDirectoryIfNotExists(BACKUP_DIR);
        createDirectoryIfNotExists(DATA_DIR);
    }
    
    /**
     * Creates a directory if it doesn't exist
     */
    public static void createDirectoryIfNotExists(String dirPath) {
        try {
            Path path = Paths.get(dirPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            ExceptionLogger.getInstance().logError("Failed to create directory: " + dirPath, e);
        }
    }
    
    /**
     * Writes an object to a file using serialization
     */
    public static void writeObjectToFile(Object obj, String filename) throws IOException {
        String fullPath = DATA_DIR + File.separator + filename;
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fullPath))) {
            oos.writeObject(obj);
        }
    }
    
    /**
     * Reads an object from a file using deserialization
     */
    @SuppressWarnings("unchecked")
    public static <T> T readObjectFromFile(String filename, Class<T> clazz) throws IOException, ClassNotFoundException {
        String fullPath = DATA_DIR + File.separator + filename;
        if (!Files.exists(Paths.get(fullPath))) {
            return null;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fullPath))) {
            return (T) ois.readObject();
        }
    }
    
    /**
     * Writes text to a file
     */
    public static void writeTextToFile(String content, String filename) throws IOException {
        String fullPath = DATA_DIR + File.separator + filename;
        Files.write(Paths.get(fullPath), content.getBytes());
    }
    
    /**
     * Reads text from a file
     */
    public static String readTextFromFile(String filename) throws IOException {
        String fullPath = DATA_DIR + File.separator + filename;
        if (!Files.exists(Paths.get(fullPath))) {
            return null;
        }
        return new String(Files.readAllBytes(Paths.get(fullPath)));
    }
    
    /**
     * Creates a backup of all data files
     */
    public static String createBackup() throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFilename = "backup_" + timestamp + ".zip";
        String backupPath = BACKUP_DIR + File.separator + backupFilename;
        
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(backupPath))) {
            Path dataDir = Paths.get(DATA_DIR);
            if (Files.exists(dataDir)) {
                Files.walk(dataDir)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            String relativePath = dataDir.relativize(file).toString();
                            ZipEntry entry = new ZipEntry(relativePath);
                            zos.putNextEntry(entry);
                            Files.copy(file, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            ExceptionLogger.getInstance().logError("Failed to add file to backup: " + file, e);
                        }
                    });
            }
        }
        
        return backupPath;
    }
    
    /**
     * Restores data from a backup file
     */
    public static void restoreFromBackup(String backupPath) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(backupPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String filePath = DATA_DIR + File.separator + entry.getName();
                Path parentDir = Paths.get(filePath).getParent();
                if (parentDir != null) {
                    Files.createDirectories(parentDir);
                }
                
                Files.copy(zis, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
                zis.closeEntry();
            }
        }
    }
    
    /**
     * Deletes a file
     */
    public static boolean deleteFile(String filename) {
        try {
            String fullPath = DATA_DIR + File.separator + filename;
            return Files.deleteIfExists(Paths.get(fullPath));
        } catch (IOException e) {
            ExceptionLogger.getInstance().logError("Failed to delete file: " + filename, e);
            return false;
        }
    }
    
    /**
     * Checks if a file exists
     */
    public static boolean fileExists(String filename) {
        String fullPath = DATA_DIR + File.separator + filename;
        return Files.exists(Paths.get(fullPath));
    }
    
    /**
     * Gets the size of a file in bytes
     */
    public static long getFileSize(String filename) {
        try {
            String fullPath = DATA_DIR + File.separator + filename;
            return Files.size(Paths.get(fullPath));
        } catch (IOException e) {
            ExceptionLogger.getInstance().logError("Failed to get file size: " + filename, e);
            return -1;
        }
    }
    
    /**
     * Lists all files in the data directory
     */
    public static String[] listDataFiles() {
        try {
            Path dataDir = Paths.get(DATA_DIR);
            if (!Files.exists(dataDir)) {
                return new String[0];
            }
            
            return Files.list(dataDir)
                .filter(Files::isRegularFile)
                .map(path -> path.getFileName().toString())
                .toArray(String[]::new);
        } catch (IOException e) {
            ExceptionLogger.getInstance().logError("Failed to list data files", e);
            return new String[0];
        }
    }
}