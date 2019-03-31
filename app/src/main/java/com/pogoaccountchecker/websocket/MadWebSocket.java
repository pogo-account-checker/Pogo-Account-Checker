package com.pogoaccountchecker.websocket;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MadWebSocket {
    private WebSocketFactory mFactory;
    private WebSocket mWebSocket;
    private String mWebSocketUri;
    private OnWebSocketEventListener mCallback;
    private final String LOG_TAG = getClass().getSimpleName();

    public MadWebSocket(String webSocketUri) {
        mFactory = new WebSocketFactory();
        mWebSocket = null;
        mWebSocketUri = webSocketUri;
    }

    public interface OnWebSocketEventListener {
        void onConnected();
        void onNotConnected();
        void onDisconnected(boolean closedByServer);
        void onMessageReceived(String message);
    }

    public void start(OnWebSocketEventListener listener) {
        mCallback = listener;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mWebSocket = mFactory.createSocket(mWebSocketUri);
                    mWebSocket.addListener(new WebSocketAdapter() {
                        @Override
                        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
                            Log.i(LOG_TAG, "Connected to WebSocket.");
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                public void run() {
                                    mCallback.onConnected();
                                }
                            });
                            Log.i(LOG_TAG, "Connected to WebSocket.");
                        }

                        @Override
                        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, final boolean closedByServer) {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                public void run() {
                                    mCallback.onDisconnected(closedByServer);
                                }
                            });
                            Log.i(LOG_TAG, "WebSocket connection closed.");
                        }

                        @Override
                        public void onTextMessage(WebSocket websocket, final String message) {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                public void run() {
                                    mCallback.onMessageReceived(message);
                                }
                            });
                            Log.i(LOG_TAG, "Received message from WebSocket: " + message);
                        }

                        @Override
                        public void handleCallbackError(WebSocket websocket, Throwable cause) {
                            Log.i(LOG_TAG, "Error!");
                        }
                    });
                    mWebSocket.connect();
                } catch (IOException | WebSocketException e) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            mCallback.onNotConnected();
                        }
                    });
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendMessage(String message) {
        mWebSocket.sendText(message);
    }
}
