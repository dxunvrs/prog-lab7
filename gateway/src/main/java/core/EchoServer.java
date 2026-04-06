package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8000);
        System.out.println("Echo сервер запущен на порту 8000");

        while (true) {
            Socket client = serverSocket.accept();
            System.out.println("Сервер подключен: " + client.getRemoteSocketAddress());

            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);

            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Получено: " + line);
                out.println("Echo: " + line);
            }
            client.close();
        }
    }
}
