package com.example.rgr_localchat;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.rgr_localchat.adapter.ChatAdapter;
import com.example.rgr_localchat.model.ChatMessage;
import com.example.rgr_localchat.network.ChatConnection;

public class MainActivity extends AppCompatActivity implements ChatConnection.ConnectionListener {

    private ChatConnection connection;
    private ChatAdapter adapter;

    private LinearLayout setupLayout, chatLayout;
    private TextView tvStatus, tvMyIP;
    private EditText etTargetIP, etMessage, etNickname;
    private Button btnStartServer;

    private static final int PORT = 8888;
    private boolean isServerRunning = false;

    private String myNickname = "Me";
    private String remoteNickname = "Stranger";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupLayout = findViewById(R.id.setupLayout);
        chatLayout = findViewById(R.id.chatLayout);
        tvStatus = findViewById(R.id.tvStatus);
        tvMyIP = findViewById(R.id.tvMyIP);
        etTargetIP = findViewById(R.id.etTargetIP);
        etMessage = findViewById(R.id.etMessage);
        etNickname = findViewById(R.id.etNickname);

        btnStartServer = findViewById(R.id.btnStartServer);
        Button btnConnect = findViewById(R.id.btnConnect);
        Button btnSend = findViewById(R.id.btnSend);
        Button btnDisconnect = findViewById(R.id.btnDisconnect); // Знаходимо нову кнопку

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter();
        recyclerView.setAdapter(adapter);

        tvMyIP.setText("My IP: " + getMyIpAddress());
        connection = new ChatConnection(this);

        btnStartServer.setOnClickListener(v -> {
            if (!isServerRunning) {
                myNickname = etNickname.getText().toString();
                connection.startServer(PORT);
                btnStartServer.setText("Stop Server");
                isServerRunning = true;
            } else {
                stopConnection(); // Використовуємо спільний метод зупинки
            }
        });

        btnConnect.setOnClickListener(v -> {
            myNickname = etNickname.getText().toString();
            String ip = etTargetIP.getText().toString();
            tvStatus.setText("Status: Connecting...");
            tvStatus.setTextColor(getResources().getColor(android.R.color.black));
            connection.connectToServer(ip, PORT);
        });

        btnSend.setOnClickListener(v -> {
            String msg = etMessage.getText().toString();
            if (!msg.isEmpty()) {
                connection.sendMessage(msg);
                adapter.addMessage(new ChatMessage(msg, true, myNickname));
                etMessage.setText("");
            }
        });

        // ЛОГІКА КНОПКИ ВИХОДУ
        btnDisconnect.setOnClickListener(v -> {
            stopConnection();
        });
    }

    // Допоміжний метод для зупинки всього і скидання інтерфейсу
    private void stopConnection() {
        if (connection != null) connection.close();
        connection = new ChatConnection(this); // Готуємо нове з'єднання

        // Повертаємо інтерфейс
        chatLayout.setVisibility(View.GONE);
        setupLayout.setVisibility(View.VISIBLE);

        // Скидаємо стани
        btnStartServer.setText("Start Server (Wait)");
        tvStatus.setText("Status: Disconnected");
        tvStatus.setTextColor(getResources().getColor(android.R.color.black));
        isServerRunning = false;

        // Очищаємо чат
        adapter.clearMessages();
    }

    private String getMyIpAddress() {
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }

    // --- Методи ConnectionListener ---

    @Override
    public void onConnected() {
        runOnUiThread(() -> {
            setupLayout.setVisibility(View.GONE);
            chatLayout.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Connected!", Toast.LENGTH_SHORT).show();

            connection.sendMessage("NICK:" + myNickname);
            adapter.addMessage(new ChatMessage("Connected! Waiting for user info..."));
        });
    }

    @Override
    public void onMessageReceived(String message) {
        if (message.startsWith("NICK:")) {
            remoteNickname = message.substring(5);
            adapter.addMessage(new ChatMessage(remoteNickname + " joined the chat"));
            playSound();
        } else {
            adapter.addMessage(new ChatMessage(message, false, remoteNickname));
            playSound();
        }
    }

    @Override
    public void onConnectionError(String error) {
        tvStatus.setText("Error: " + error);
        tvStatus.setTextColor(0xFFFF0000);
        if (chatLayout.getVisibility() == View.VISIBLE) {
            adapter.addMessage(new ChatMessage("Connection Error: " + error));
        }
    }

    @Override
    public void onStatusUpdate(String status) {
        tvStatus.setText(status);
        tvStatus.setTextColor(0xFF000000);
    }

    // ЗВУК ВИМКНЕНО ПОВНІСТЮ
    private void playSound() {
        // Залишаємо метод порожнім, щоб вимкнути звук.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connection != null) connection.close();
    }
}