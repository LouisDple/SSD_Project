import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.nio.file.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// This class serves as the client-side application that interacts with the remote FileServer.
public class FileServerClient {
    private static final int BLOCK_SIZE = 4096; // Block size for file chunks during upload/download.
    private static final ExecutorService executorService = Executors.newFixedThreadPool(5); // Thread pool for concurrent tasks.

    // Main method to start the client application and handle user input.
    public static void main(String[] args) {
        try {
            // Connect to the RMI registry on localhost.
            Registry registry = LocateRegistry.getRegistry("localhost");
            FileServerInterface server = (FileServerInterface) registry.lookup("FileServer");
            Scanner scanner = new Scanner(System.in);

            boolean running = true;
            // Display the menu and handle user choices.
            while (running) {
                System.out.println("\nMenu:\n1. Upload file\n2. Download file\n3. Upload folder\n4. Download folder\n5. Monitor folder\n6. Create directory\n7. Delete directory/file\n8. Exit");
                System.out.print("Enter your choice: ");
                int choice = Integer.parseInt(scanner.nextLine()); // Use nextLine() consistently

                switch (choice) {
                    case 1:
                        uploadFile(server, scanner);
                        break;
                    case 2:
                        downloadFile(server, scanner);
                        break;
                    case 3:
                        uploadFolder(server, scanner);
                        break;
                    case 4:
                        downloadFolder(server, scanner);
                        break;
                    case 5:
                        System.out.print("Enter the path of the folder to monitor: ");
                        String folderToMonitor = scanner.nextLine();
                        displayFolderContents(folderToMonitor);
                        monitorFolder(Paths.get(folderToMonitor), server);
                        break;
                    case 6:
                        createDirectory(server, scanner);
                        break;
                    case 7:
                        deletePath(server, scanner);
                        break;
                    case 8:
                        System.out.println("Exiting...");
                        executorService.shutdown();
                        executorService.awaitTermination(5, TimeUnit.SECONDS);
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice!");
                }
            }
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

 // Method to display the contents of a folder before monitoring it.
    private static void displayFolderContents(String folderPath) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(folderPath))) {
            System.out.println("Contents of folder: " + folderPath);
            for (Path entry : stream) {
                System.out.println(" - " + (Files.isDirectory(entry) ? "[DIR] " : "[FILE] ") + entry.getFileName());
                
            }
        } catch (IOException e) {
            System.err.println("Error reading folder contents: " + e.getMessage());
        }
    }

    // Method to upload a file to the server in chunks.
    private static void uploadFile(FileServerInterface server, Scanner scanner) throws IOException {
        System.out.print("Enter the path of the file to upload: ");
        String filePath = scanner.nextLine();
        Path path = Paths.get(filePath);

        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            long fileSize = Files.size(path);
            int totalBlocks = (int) Math.ceil((double) fileSize / BLOCK_SIZE);
            int blockNumber = 0;
            byte[] buffer = new byte[BLOCK_SIZE];

            while (blockNumber < totalBlocks) {
                int bytesRead = channel.read(ByteBuffer.wrap(buffer));
                byte[] dataToSend = (bytesRead < BLOCK_SIZE) ? ByteBuffer.wrap(buffer, 0, bytesRead).array() : buffer;

                System.out.println(server.uploadFileBlock(path.getFileName().toString(), dataToSend, blockNumber));
                blockNumber++;
                System.out.println("Progress: " + (blockNumber * 100 / totalBlocks) + "%");
            }
            System.out.println("File upload completed.");
        }
    }

    // Method to download a file from the server in chunks.
    private static void downloadFile(FileServerInterface server, Scanner scanner) throws IOException {
        System.out.print("Enter the name of the file to download: ");
        String fileName = scanner.nextLine();
        int blockCount = server.getFileBlockCount(fileName);
        byte[] buffer = new byte[BLOCK_SIZE];

        try (FileChannel channel = FileChannel.open(Paths.get("downloaded_" + fileName), StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            for (int blockNumber = 0; blockNumber < blockCount; blockNumber++) {
                byte[] block = server.downloadFileBlock(fileName, blockNumber);
                channel.position(blockNumber * BLOCK_SIZE);
                channel.write(ByteBuffer.wrap(block));
                System.out.println("Downloaded block " + blockNumber);
            }
            System.out.println("File download completed.");
        }
    }

    // Method to upload a folder and its contents to the server.
   /* private static void uploadFolder(FileServerInterface server, Scanner scanner) {
        System.out.print("Enter the path of the folder to upload: ");
        String folderPath = scanner.nextLine();
        Path path = Paths.get(folderPath);

        try {
            Files.walk(path).forEach(filePath -> {
                if (Files.isDirectory(filePath)) {
                    try {
                        server.createDirectory(path.relativize(filePath).toString());
                    } catch (Exception e) {
                        System.err.println("Error creating directory on server: " + e.getMessage());
                    }
                } else {
                    try (FileChannel channel = FileChannel.open(filePath, StandardOpenOption.READ)) {
                        long fileSize = Files.size(filePath);
                        byte[] buffer = new byte[BLOCK_SIZE];

                        for (int blockNumber = 0; blockNumber < (int) Math.ceil((double) fileSize / BLOCK_SIZE); blockNumber++) {
                            int bytesRead = channel.read(ByteBuffer.wrap(buffer));
                            byte[] dataToSend = (bytesRead < BLOCK_SIZE) ? ByteBuffer.wrap(buffer, 0, bytesRead).array() : buffer;
                            server.uploadFileBlock(filePath.getFileName().toString(), dataToSend, blockNumber);
                        }
                    } catch (IOException e) {
                        System.err.println("Error uploading file: " + e.getMessage());
                    }
                }
            });
            System.out.println("Folder upload completed.");
        } catch (IOException e) {
            System.err.println("Error walking folder: " + e.getMessage());
        }
    }*/
    
    private static void uploadFolder(FileServerInterface server, Scanner scanner) {
        System.out.print("Enter the path of the folder to upload: ");
        String folderPath = scanner.nextLine();
        Path path = Paths.get(folderPath);

        try {
            Files.walk(path).forEach(filePath -> {
                try {
                    if (Files.isDirectory(filePath)) {
                        server.createDirectory(path.relativize(filePath).toString());
                    } else {
                        uploadFileChunked(server, path.relativize(filePath).toString(), filePath);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing path: " + filePath + " - " + e.getMessage());
                }
            });
            System.out.println("Folder upload completed.");
        } catch (IOException e) {
            System.err.println("Error walking folder: " + e.getMessage());
        }
    }
    
    private static void uploadFileChunked(FileServerInterface server, String relativePath, Path filePath) throws IOException {
        try (FileChannel channel = FileChannel.open(filePath, StandardOpenOption.READ)) {
            long fileSize = Files.size(filePath);
            byte[] buffer = new byte[BLOCK_SIZE];
            int blockNumber = 0;

            while (channel.read(ByteBuffer.wrap(buffer)) > 0) {
                byte[] dataToSend = buffer.clone();
                server.uploadFileBlock(relativePath, dataToSend, blockNumber);
                blockNumber++;
            }
        }
    }
    
    // Method to download a folder and its contents from the server.
    private static void downloadFolder(FileServerInterface server, Scanner scanner) {
        System.out.print("Enter the path of the folder to download from server: ");
        String folderPath = scanner.nextLine();

        try {
            List<String> filesAndFolders = server.downloadDirectoryTree(folderPath);
            Path localPath = Paths.get("downloaded_" + folderPath);

            for (String item : filesAndFolders) {
                Path itemPath = localPath.resolve(item);
                if (item.endsWith("/")) {
                    Files.createDirectories(itemPath);
                } else {
                    Path parentDir = itemPath.getParent();
                    if (parentDir != null && !Files.exists(parentDir)) {
                        Files.createDirectories(parentDir);
                    }
                    byte[] fileData = server.downloadFileBlock(item, 0);
                    Files.write(itemPath, fileData);
                }
            }
            System.out.println("Folder download completed.");
        } catch (Exception e) {
            System.err.println("Error downloading folder: " + e.getMessage());
        }
    }

    // Method to create a directory on the server.
    private static void createDirectory(FileServerInterface server, Scanner scanner) {
        System.out.print("Enter the directory path to create on the server: ");
        String dirPath = scanner.nextLine();
        try {
            System.out.println(server.createDirectory(dirPath));
        } catch (Exception e) {
            System.err.println("Error creating directory: " + e.getMessage());
        }
    }

    // Method to delete a file or directory on the server.
    private static void deletePath(FileServerInterface server, Scanner scanner) {
        System.out.print("Enter the file or directory path to delete on the server: ");
        String path = scanner.nextLine();
        try {
            Path deletePath = Paths.get(path);
            if (Files.isDirectory(deletePath)) {
                System.out.println(server.delete(path));
            } else if (Files.isRegularFile(deletePath)) {
                System.out.println(server.delete(path));
            } else {
                System.out.println("Invalid path: " + path);
            }
        } catch (Exception e) {
            System.err.println("Error deleting path: " + e.getMessage());
        }
    }

    // Method to monitor a local folder for changes and sync them with the server.
    private static void monitorFolder(Path folderToMonitor, FileServerInterface server) {
        executorService.execute(() -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                folderToMonitor.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

                while (true) {
                    WatchKey key = watchService.poll(5, TimeUnit.SECONDS);
                    if (key == null) continue;

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        Path fileName = (Path) event.context();

                        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            System.out.println("File created: " + fileName);
                        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            System.out.println("File modified: " + fileName);
                        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            System.out.println("File deleted: " + fileName);
                        }
                    }

                    // Display the current contents of the folder after each change.
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderToMonitor)) {
                        System.out.println("Current folder contents:");
                        for (Path entry : stream) {
                            System.out.println(" - " + entry.getFileName());
                        }
                    } catch (IOException e) {
                        System.err.println("Error reading folder contents: " + e.getMessage());
                    }

                    key.reset();
                }
            } catch (Exception e) {
                System.err.println("Error monitoring folder: " + e.getMessage());
            }
        });
    }
}