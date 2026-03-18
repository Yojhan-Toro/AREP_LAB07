package Arep.Lab07;

import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HttpServer {

    static Map<String, WebMethod> endPoints = new HashMap<>();
    static String staticFilesFolder = "webroot/public";

    // Thread pool for concurrent request handling
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    // Flag to control the main accept loop
    private static volatile boolean running = true;

    static Map<String, String> MIME_TYPES = new HashMap<>();
    static {
        MIME_TYPES.put("html", "text/html");
        MIME_TYPES.put("css",  "text/css");
        MIME_TYPES.put("js",   "application/javascript");
        MIME_TYPES.put("png",  "image/png");
        MIME_TYPES.put("jpg",  "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
    }

    public static void main(String[] args) throws IOException, URISyntaxException {

        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(35000);
            serverSocket.setReuseAddress(true);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
            return;
        }

        // --- Graceful Shutdown Hook ---
        // Registers a JVM shutdown hook that runs in a separate thread
        // when the JVM receives SIGTERM, SIGINT (Ctrl+C), or System.exit() is called.
        // See: https://www.baeldung.com/jvm-shutdown-hooks
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[Shutdown Hook] Graceful shutdown initiated...");
            running = false;

            // Stop accepting new requests
            try {
                serverSocket.close();
                System.out.println("[Shutdown Hook] Server socket closed.");
            } catch (IOException e) {
                System.err.println("[Shutdown Hook] Error closing server socket: " + e.getMessage());
            }

            // Shutdown thread pool: stop accepting new tasks, wait for running ones to finish
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                    System.err.println("[Shutdown Hook] Thread pool did not terminate in time, forcing shutdown.");
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("[Shutdown Hook] Server shut down gracefully.");
        }, "shutdown-hook-thread"));

        System.out.println("Server started on port 35000. Listening for connections...");

        while (running) {
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                // When the shutdown hook closes the serverSocket, accept() throws here — that's expected.
                if (!running) break;
                System.err.println("Accept failed: " + e.getMessage());
                break;
            }
            // Hand off each accepted connection to the thread pool
            threadPool.submit(new ClientHandler(clientSocket));
        }

        System.out.println("Main accept loop exited.");
    }

    public static void get(String path, WebMethod wm) {
        endPoints.put(path, wm);
    }

    public static void staticfiles(String folder) {
        staticFilesFolder = folder.startsWith("/") ? folder.substring(1) : folder;
    }

    // --- Inner class: handles one client connection in its own thread ---
    static class ClientHandler implements Runnable {

        private final Socket clientSocket;

        ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (
                PrintWriter out    = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in  = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
            ) {
                String inputLine;
                boolean isFirstLine = true;
                String repath      = null;
                String struripath  = null;

                while ((inputLine = in.readLine()) != null) {
                    System.out.println("[Thread " + Thread.currentThread().getName() + "] Received: " + inputLine);

                    if (isFirstLine) {
                        String[] flTokens = inputLine.split(" ");
                        struripath = flTokens[1];
                        URI uripath = new URI(struripath);
                        repath = uripath.getPath();
                        System.out.println("[Thread " + Thread.currentThread().getName() + "] Path: " + repath);
                        isFirstLine = false;
                    }

                    if (!in.ready()) break;
                }

                WebMethod currentWm = endPoints.get(repath);
                String outputLine;

                if (currentWm != null) {
                    HttpRequest req  = new HttpRequest(struripath);
                    HttpResponse res = new HttpResponse();

                    outputLine = "HTTP/1.1 200 OK\r\n"
                            + "Content-Type: text/html\r\n"
                            + "\r\n"
                            + "<!DOCTYPE html>"
                            + "<html><head><meta charset=\"UTF-8\"><title>Response</title></head>"
                            + "<body>"
                            + currentWm.execute(req, res)
                            + "</body></html>";

                } else {
                    String resourcePath = staticFilesFolder + repath;
                    InputStream fileStream = HttpServer.class.getClassLoader()
                            .getResourceAsStream(resourcePath);

                    if (fileStream != null) {
                        String ext = repath.contains(".")
                                ? repath.substring(repath.lastIndexOf('.') + 1) : "";
                        String contentType = MIME_TYPES.getOrDefault(ext, "text/html");
                        byte[] fileBytes = fileStream.readAllBytes();
                        fileStream.close();

                        String headers = "HTTP/1.1 200 OK\r\n"
                                + "Content-Type: " + contentType + "\r\n"
                                + "\r\n";
                        clientSocket.getOutputStream().write(headers.getBytes());
                        clientSocket.getOutputStream().write(fileBytes);
                        return;
                    }

                    outputLine = "HTTP/1.1 404 Not Found\r\n"
                            + "Content-Type: text/html\r\n"
                            + "\r\n"
                            + "<!DOCTYPE html>"
                            + "<html><body><h1>404 - Not Found</h1></body></html>";
                }

                out.println(outputLine);

            } catch (Exception e) {
                System.err.println("[Thread " + Thread.currentThread().getName()
                        + "] Error handling client: " + e.getMessage());
            } finally {
                try { clientSocket.close(); } catch (IOException ignored) {}
            }
        }
    }
}
