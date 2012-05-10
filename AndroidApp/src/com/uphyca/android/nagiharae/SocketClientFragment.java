package com.uphyca.android.nagiharae;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

public class SocketClientFragment extends Fragment {

    private static final String TAG = "SocketClientFragment";
    static final String SOCKET_IP = "192.168.100.100";
    static final int SOCKET_PORT = 10002;

    Socket conn = null;
    BufferedReader reader;
    OutputStream out;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        close();
    }

    public void sendCommand(String command) throws IOException {
        if (out != null) {
            out.write(command.getBytes());
            out.flush();
        }
    }

    public void connect() {
        
        Thread thread = new Thread() {
            @Override
            public void run() {
                startConnection();
            };
        };
        thread.start();
    }
    
    private void startConnection() {
        try {
            // サーバーへ接続
            conn = new Socket(SOCKET_IP, SOCKET_PORT);

            // メッセージ取得オブジェクトのインスタンス化
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            out = new BufferedOutputStream(conn.getOutputStream());

            // 通信相手から送られるデータの読み込みループを開始する
            Thread thread = new Thread() {
                @Override
                public void run() {
                    for (;;) {
                        try {
                            String message = reader.readLine();
                            if (message != null) {
                                Log.d("MainActivity", message);
                            }
                        } catch (IOException maybeSocketClosed) {
                            break;
                        }
                    }
                };
            };
            thread.start();
            
        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());
        }
    }

    public void close() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, e.toString());
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, e.toString());
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, e.toString());
            }
        }
    }
}
