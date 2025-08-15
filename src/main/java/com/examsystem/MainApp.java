package main.java.com.examsystem;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import main.java.com.examsystem.handler.ExamHandler;
import main.java.com.examsystem.handler.LoginHandler;
import main.java.com.examsystem.handler.SubmitHandler;
import main.java.com.examsystem.utils.DataLoader;
import main.java.com.examsystem.utils.DatabaseInitializer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;


public class MainApp {
    public static void main(String[] args) throws IOException {
        DatabaseInitializer.initializeDatabase();
        DataLoader.saveExamsToDatabase();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        Path filePath = Paths.get("login.html");
        server.createContext("/login", new LoginHandler());
        server.createContext("/exams", new ExamHandler());
        server.createContext("/submit", new SubmitHandler());

        server.createContext("/static", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                Path path = Paths.get("src/main/resources/static",
                        exchange.getRequestURI().getPath().substring(7));
                byte[] response = Files.readAllBytes(path);
                exchange.sendResponseHeaders(200, response.length);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(response);
                outputStream.close();
            }
        });
        server.setExecutor(Executors.newFixedThreadPool(20));
        server.start();
    }
}