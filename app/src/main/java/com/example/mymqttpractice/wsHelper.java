package com.example.mymqttpractice;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;

public class wsHelper {
    MqttAndroidClient mqttClient;
    final String serverUri = "ws://192.168.100.139:8080/webSocket";  //

    final String clientId = MqttClient.generateClientId();  //不能重複

    final String topic = "sensor/temp";   //主題

    final String username = "username";
    final String password = "password";

    public wsHelper(Context context) {

        mqttClient = new MqttAndroidClient(context, serverUri, clientId);

        //Extension of MqttCallback to allow new callbacks without breaking the API for existing applications.
        mqttClient.setCallback(new MqttCallbackExtended() {

            //Called when the connection to the server is completed successfully
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                System.out.println("ws連線成功");

            }

            //This method is called when the connection to the server is lost.
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("ws連線lost");
            }

            //This method is called when a message arrives from the server.
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println("ws收到訊息 主題:"+topic+", 內容:"+message);
            }

            //Called when delivery for a message has been completed, and all acknowledgments have been received.
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("傳送完畢");
            }
        });

        connect();
    }

    public void setCallback(MqttCallbackExtended mqttCallbackExtended){
        mqttClient.setCallback(mqttCallbackExtended);
    }

    public void connect(){
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        String key = generateWebSocketKey();

        //設定是否自動重連
        mqttConnectOptions.setAutomaticReconnect(true);
        //設定是否清空session(設置為false表示server會保留client的連接記錄)
        mqttConnectOptions.setCleanSession(true);
        //server會每隔60s 傳消息給client 判斷是否在線
        mqttConnectOptions.setKeepAliveInterval(60);
        //設定是否重連
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());

        Properties p = new Properties();
        p.setProperty("host", "10.0.2.2:8080");  //模擬器連線server端 10.0.2.2自動轉址
        p.setProperty("upgrade","websocket");   //升級成websocket協議
        p.setProperty("connection","Upgrade");  //使用升級後的方式連線
        p.setProperty("sec-websocket-key", key);
        p.setProperty("sec-websocket-version","13");  //websocket版本
        p.setProperty("Sec-WebSocket-Protocol","mqtt");

        mqttConnectOptions.setCustomWebSocketHeaders(p);
        System.out.println(mqttConnectOptions.getCustomWebSocketHeaders());

        try {

            mqttClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();

                    //設定斷線後是否暫存
                    disconnectedBufferOptions.setBufferEnabled(true);
                    //設定斷線後最多存100條訊息
                    disconnectedBufferOptions.setBufferSize(100);
                    //設定是否刪除舊訊息
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    //設定是否持續暫存(???)
                    disconnectedBufferOptions.setPersistBuffer(false);

                    //設定斷線暫存選項
                    mqttClient.setBufferOpts(disconnectedBufferOptions);

                    System.out.println("ws連線成功");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println("ws連線失敗"+exception);
                }
            });
        } catch (MqttException e) {
            System.out.println("ws連線出錯:"+e.getMessage());
            throw new RuntimeException(e);
        }
    }


    /**
     * 訂閱主題
     */
    public void subscribeToTemp() {
        try {
            mqttClient.subscribe(topic, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println("ws訂閱成功"+message);
                }
            });
        } catch (MqttException e) {
            System.out.println("ws訂閱錯誤"+e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 發布訊息
     *
     * @param message 訊息內容
     */
    public void publishToTemp(String message){
        MqttMessage m = new MqttMessage();
        //string轉byte[]
        m.setPayload(message.getBytes());
        //設定QoS
        m.setQos(0);
        try {
            mqttClient.publish(topic,m);
        } catch (MqttException e) {
            System.out.println("ws發布出錯"+e);
            throw new RuntimeException(e);
        }
    }

    public String generateWebSocketKey() {
        // 生成随机的 16 字节二进制数据
        byte[] keyBytes = new byte[16];
        new SecureRandom().nextBytes(keyBytes);

        // 对二进制数据进行 Base64 编码
        String base64Key = Base64.getEncoder().encodeToString(keyBytes);

        return base64Key;
    }
}
