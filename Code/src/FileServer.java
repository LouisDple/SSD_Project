import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FileServer implements FileServerInterface {
    private static final Path ROOT_DIR = Paths.get("server_root");
    private static final int BLOCK_SIZE = 4096;
    private final ExecutorService executorService;

    public FileServer() {
        try {
            Files.createDirectories(ROOT_DIR);
        } catch (IOException e) {
            System.err.println("Error creating root directory: " + e.getMessage());
        }
        this.executorService = Executors.newFixedThreadPool(10);
    }

    @Override
    public String uploadFileBlock(String fileName, byte[] data, int blockNumber) throws RemoteException {
        if (blockNumber < 0) {
            throw new RemoteException("Invalid block number: " + blockNumber);
        }

        Path filePath = ROOT_DIR.resolve(fileName);
        try {
            long fileSize = Files.exists(filePath) ? Files.size(filePath) : 0;
            int maxBlockNumber = (int) Math.ceil((double) fileSize / BLOCK_SIZE);

            // Correction: Allow uploading blocks beyond the current file size to handle new uploads
            if (blockNumber > maxBlockNumber && fileSize > 0) {
                throw new RemoteException("Block number exceeds current file size: " + blockNumber);
            }

            Future<?> future = executorService.submit(() -> {
                try (FileChannel channel = FileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                    channel.position((long) blockNumber * BLOCK_SIZE);
                    channel.write(ByteBuffer.wrap(data));
                    System.out.println("Block " + blockNumber + " uploaded.");
                } catch (IOException e) {
                    System.err.println("Error uploading block: " + e.getMessage());
                }
            });

            future.get(); // Ensure the block is written before returning
            return "Block upload completed: " + blockNumber;
        } catch (IOException | InterruptedException | java.util.concurrent.ExecutionException e) {
            throw new RemoteException("Error uploading block: " + e.getMessage(), e);
        }
    }


    @Override
    public byte[] downloadFileBlock(String fileName, int blockNumber) throws RemoteException {
        if (blockNumber < 0) {
            throw new RemoteException("Invalid block number: " + blockNumber);
        }

        Future<byte[]> future = executorService.submit(() -> {
            Path filePath = ROOT_DIR.resolve(fileName);
            byte[] data = new byte[BLOCK_SIZE];
            try (FileChannel channel = FileChannel.open(filePath, StandardOpenOption.READ)) {
                channel.position((long) blockNumber * BLOCK_SIZE);
                ByteBuffer buffer = ByteBuffer.wrap(data);
                int bytesRead = channel.read(buffer);
                return bytesRead < BLOCK_SIZE ? ByteBuffer.wrap(data, 0, bytesRead).array() : data;
            } catch (IOException e) {
                throw new RemoteException("Error downloading block: " + e.getMessage(), e);
            }
        });

        try {
            return future.get();
        } catch (Exception e) {
            throw new RemoteException("Error retrieving download result: " + e.getMessage(), e);
        }
    }

    @Override
    public int getFileBlockCount(String fileName) throws RemoteException {
        Path filePath = ROOT_DIR.resolve(fileName);
        try {
            long fileSize = Files.size(filePath);
            return (int) Math.ceil((double) fileSize / BLOCK_SIZE);
        } catch (IOException e) {
            throw new RemoteException("Error getting file size: " + e.getMessage(), e);
        }
    }

    @Override
    public String createDirectory(String path) throws RemoteException {
        Path dirPath = ROOT_DIR.resolve(path);
        try {
            Files.createDirectories(dirPath);
            return "Directory created: " + path;
        } catch (IOException e) {
            throw new RemoteException("Error creating directory: " + e.getMessage(), e);
        }
    }

    @Override
    public String delete(String path) throws RemoteException {
        Path targetPath = ROOT_DIR.resolve(path);
        try {
            if (Files.exists(targetPath)) {
                if (Files.isDirectory(targetPath)) {
                    Files.walkFileTree(targetPath, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Files.delete(file);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } else {
                    Files.delete(targetPath);
                }
                return "Deleted: " + path;
            } else {
                return "Path not found: " + path;
            }
        } catch (IOException e) {
            throw new RemoteException("Error deleting: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> downloadDirectoryTree(String path) throws RemoteException {
        List<String> filesAndFolders = new ArrayList<>();
        Path dirPath = ROOT_DIR.resolve(path);
        try {
            Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    filesAndFolders.add(ROOT_DIR.relativize(file).toString());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    filesAndFolders.add(ROOT_DIR.relativize(dir).toString() + "/");
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RemoteException("Error downloading directory tree: " + e.getMessage(), e);
        }
        return filesAndFolders;
    }

    public String uploadDirectoryTree(String localPath, String serverPath) throws RemoteException {
        Path localDir = Paths.get(localPath);
        Path serverDir = ROOT_DIR.resolve(serverPath);

        try {
            if (!Files.exists(serverDir)) {
                Files.createDirectories(serverDir);
            }

            Files.walk(localDir).forEach(path -> {
                Path relativePath = localDir.relativize(path);
                Path targetPath = serverDir.resolve(relativePath);

                try {
                    if (Files.isDirectory(path)) {
                        Files.createDirectories(targetPath);
                    } else {
                        Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    System.err.println("Error uploading: " + path + " - " + e.getMessage());
                }
            });

            return "Directory uploaded successfully.";
        } catch (IOException e) {
            throw new RemoteException("Error uploading directory: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        try {
            int port = 1099;
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
            }

            FileServer server = new FileServer();
            FileServerInterface stub = (FileServerInterface) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.createRegistry(port);
            registry.bind("FileServer", stub);
            System.out.println("File server is ready on port " + port + ".");

            try (Scanner scanner = new Scanner(System.in)) {
                System.out.println("Type 'exit' to shut down the server.");
                while (true) {
                    if (scanner.nextLine().trim().equalsIgnoreCase("exit")) {
                        System.out.println("Shutting down the server...");
                        break;
                    }
                }
            }

            UnicastRemoteObject.unexportObject(server, true);
            System.out.println("Server shut down.");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
