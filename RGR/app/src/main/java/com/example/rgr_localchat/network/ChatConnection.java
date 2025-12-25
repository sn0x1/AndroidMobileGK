package com.example.rgr_localchat.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatConnection {

    private Socket socket;
    private ServerSocket serverSocket; // Добавили поле, чтобы можно было закрыть сервер
    private PrintWriter out;
    private BufferedReader in;
    private ConnectionListener listener;
    private boolean isRunning = true;

    private Handler uiHandler = new Handler(Looper.getMainLooper());

    public interface ConnectionListener {
        void onMessageReceived(String message);
        void onConnected();
        void onConnectionError(String error);
        void onStatusUpdate(String status); // Новый метод для обычных статусов
    }

    public ChatConnection(ConnectionListener listener) {
        this.listener = listener;
    }

    // Режим СЕРВЕРА
    public void startServer(int port) {
        isRunning = true;
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                // Используем onStatusUpdate для нормальных сообщений
                uiHandler.post(() -> listener.onStatusUpdate("Server started. Waiting for client..."));

                socket = serverSocket.accept(); // Блокируется, пока кто-то не подключится
                setupStreams();
            } catch (IOException e) {
                // Если мы сами остановили сервер, это не ошибка
                if (isRunning) {
                    postError("Server Error: " + e.getMessage());
                }
            }
        }).start();
    }

    // Режим КЛІЄНТА
    public void connectToServer(String ip, int port) {
        isRunning = true;
        new Thread(() -> {
            try {
                socket = new Socket(ip, port);
                setupStreams();
            } catch (IOException e) {
                postError("Connection failed: " + e.getMessage());
            }
        }).start();
    }

    private void setupStreams() throws IOException {
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        uiHandler.post(() -> listener.onConnected());

        while (isRunning) {
            try {
                String message = in.readLine();
                if (message != null) {
                    uiHandler.post(() -> listener.onMessageReceived(message));
                } else {
                    isRunning = false;
                    postError("Connection closed by other side");
                }
            } catch (IOException e) {
                if (isRunning) postError("Read Error: " + e.getMessage());
            }
        }
    }

    public void sendMessage(String message) {
        new Thread(() -> {
            if (out != null) {
                out.println(message);
            }
        }).start();
    }

    private void postError(String error) {
        uiHandler.post(() -> listener.onConnectionError(error));
        Log.e("ChatConnection", error);
    }

    public void close() {
        isRunning = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
            if (serverSocket != null) serverSocket.close(); // Закрываем серверный сокет тоже
        } catch (IOException e) { e.printStackTrace(); }
    }
}