import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.nio.file.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FileServerClient {
    private static final int BLOCK_SIZE = 4096;
    private static final int THREAD_POOL_SIZE = 5;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter the server address (default is 'localhost' if no input): ");
            String serverAddress = scanner.nextLine().trim();
            if (serverAddress.isEmpty()) serverAddress = "localhost";

            Registry registry = LocateRegistry.getRegistry(serverAddress);
            FileServerInterface server = (FileServerInterface) registry.lookup("FileServer");

            runClient(scanner, server);
        } catch (Exception e) {
            System.err.println("Client exception: " + e);
            e.printStackTrace();
        }
    }
    
    private static void runClient(Scanner scanner, FileServerInterface server) throws Exception {
        boolean running = true;
        while (running) {
            displayMenu();
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1 and 8.");
                continue;
            }

            switch (choice) {
                case 1 -> uploadFile(server, scanner);
                case 2 -> downloadFile(server, scanner);
                case 3 -> uploadFolder(server, scanner);
                case 4 -> downloadFolder(server, scanner);
                case 5 -> monitorRootServerFolder(server, scanner);
                case 6 -> createDirectory(server, scanner);
                case 7 -> deletePath(server, scanner);
                case 8 -> {
                    shutdownExecutorService();
                    running = false;
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private static void displayMenu() {
        System.out.println("\nMenu:\n1. Upload file\n2. Download file\n3. Upload folder\n4. Download folder\n5. Monitor folder\n6. Create directory\n7. Delete directory/file\n8. Exit");
        System.out.print("Enter your choice: ");
    }

    private static void shutdownExecutorService() {
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            System.out.println("Shutting down the client...");
            System.exit(0);
        }
    }

    private static void downloadFolder(FileServerInterface server, Scanner scanner) {
        System.out.print("Enter the path of the folder to download from server: ");
        String folderPath = scanner.nextLine();

        try {
            List<String> items = server.downloadDirectoryTree(folderPath);
            Path localBasePath = Paths.get("downloaded_" + folderPath);

            Map<String, Integer> blockCounts = new HashMap<>();
            for (String item : items) {
                if (!item.endsWith("/")) {
                    blockCounts.put(item, server.getFileBlockCount(item));
                }
            }

            for (String item : items) {
                Path localPath = localBasePath.resolve(item);
                if (item.endsWith("/")) {
                    Files.createDirectories(localPath);
                } else {
                    createParentDirectories(localPath);
                    downloadFileChunks(server, item, localPath, blockCounts.get(item));
                }
            }
            System.out.println("Folder download completed.");
        } catch (Exception e) {
            System.err.println("Error downloading folder: " + e.getMessage());
        }
    }

    private static void createParentDirectories(Path localPath) throws IOException {
        if (!Files.exists(localPath.getParent())) {
            Files.createDirectories(localPath.getParent());
        }
    }

    private static void downloadFileChunks(FileServerInterface server, String item, Path localPath, int blockCount) throws IOException {
        try (FileChannel channel = FileChannel.open(localPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            for (int blockNumber = 0; blockNumber < blockCount; blockNumber++) {
                byte[] block = server.downloadFileBlock(item, blockNumber);
                channel.position(blockNumber * BLOCK_SIZE);
                channel.write(ByteBuffer.wrap(block));
            }
        }
    }

    private static void uploadFile(FileServerInterface server, Scanner scanner) throws IOException {
        System.out.print("Enter the path of the file to upload: ");
        String filePath = scanner.nextLine();
        Path path = Paths.get(filePath);

        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            long fileSize = Files.size(path);
            int totalBlocks = (int) Math.ceil((double) fileSize / BLOCK_SIZE);
            byte[] buffer = new byte[BLOCK_SIZE];

            for (int blockNumber = 0; blockNumber < totalBlocks; blockNumber++) {
                int bytesRead = channel.read(ByteBuffer.wrap(buffer));
                byte[] dataToSend = bytesRead < BLOCK_SIZE ? ByteBuffer.wrap(buffer, 0, bytesRead).array() : buffer;
                server.uploadFileBlock(path.getFileName().toString(), dataToSend, blockNumber);
            }
            System.out.println("File upload completed.");
        }
    }

    private static void downloadFile(FileServerInterface server, Scanner scanner) throws IOException {
        System.out.print("Enter the name of the file to download: ");
        String fileName = scanner.nextLine();
        int blockCount = server.getFileBlockCount(fileName);

        try (FileChannel channel = FileChannel.open(Paths.get("downloaded_" + fileName), StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            for (int blockNumber = 0; blockNumber < blockCount; blockNumber++) {
                byte[] block = server.downloadFileBlock(fileName, blockNumber);
                channel.position(blockNumber * BLOCK_SIZE);
                channel.write(ByteBuffer.wrap(block));
            }
            System.out.println("File download completed.");
        }
    }

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
            byte[] buffer = new byte[BLOCK_SIZE];
            int blockNumber = 0;

            while (channel.read(ByteBuffer.wrap(buffer)) > 0) {
                server.uploadFileBlock(relativePath, buffer.clone(), blockNumber);
                blockNumber++;
            }
        }
    }

    private static void createDirectory(FileServerInterface server, Scanner scanner) {
        System.out.print("Enter the directory path to create on the server: ");
        String dirPath = scanner.nextLine();
        try {
            server.createDirectory(dirPath);
            System.out.println("Directory created successfully.");
        } catch (Exception e) {
            System.err.println("Error creating directory: " + e.getMessage());
        }
    }

    private static void deletePath(FileServerInterface server, Scanner scanner) {
        System.out.print("Enter the file or directory path to delete on the server: ");
        String path = scanner.nextLine();
        try {
            server.delete(path);
            System.out.println("Path deleted successfully.");
        } catch (Exception e) {
            System.err.println("Error deleting path: " + e.getMessage());
        }
    }

    private static void monitorRootServerFolder(FileServerInterface server, Scanner scanner) {
        boolean keepMonitoring = true;
        System.out.println("Enter 'exit' to stop monitoring.");

        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            Path rootPath = Paths.get("server_root");

            rootPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            displayFolderContents(rootPath);

            Map<Path, WatchEvent.Kind<?>> eventMap = new ConcurrentHashMap<>();
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

            while (keepMonitoring) {
                if (System.in.available() > 0 && scanner.nextLine().trim().equalsIgnoreCase("exit")) {
                    keepMonitoring = false;
                    break;
                }

                WatchKey key = watchService.poll(5, TimeUnit.SECONDS);
                if (key != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        Path dir = (Path) key.watchable();
                        Path fileName = dir.resolve((Path) event.context());
                        if (dir.equals(rootPath)) {
                            eventMap.put(fileName, event.kind());
                        }
                    }
                    key.reset();

                    scheduler.schedule(() -> {
                        synchronized (eventMap) {
                            eventMap.forEach((fileName, kind) -> System.out.println(kind.name() + ": " + fileName));
                            eventMap.clear();
                        }
                    }, 1000, TimeUnit.MILLISECONDS);
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error monitoring folder: " + e.getMessage());
            Thread.currentThread().interrupt();
        }

        System.out.println("Returning to menu...");
    }


    private static void displayFolderContents(Path path) {
        try {
            System.out.println("\nInitial folder contents:");
            Files.list(path).forEach(p -> System.out.println((Files.isDirectory(p) ? "[DIR] " : "[FILE] ") + p.getFileName()));
        } catch (IOException e) {
            System.err.println("Error displaying folder contents: " + e.getMessage());
        }
    }
    
}
