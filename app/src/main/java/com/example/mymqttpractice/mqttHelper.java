package com.example.mymqttpractice;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class mqttHelper {

    public MqttAndroidClient mqttAndroidClient;

    final String serverUri = "tcp://192.168.100.194:1883";

    final String clientId = MqttClient.generateClientId();  //不能重複

    final String topic = "sensor/temp";   //主題

    final String username = "username";
    final String password = "password";

    public mqttHelper(Context context) {
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
                                        //Extension of MqttCallback to allow new callbacks without breaking the API for existing applications.
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {

            //Called when the connection to the server is completed successfully
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                System.out.println("連線成功");

            }

            //This method is called when the connection to the server is lost.
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("連線lost");
            }

            //This method is called when a message arrives from the server.
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println("收到訊息 主題:"+topic+", 內容:"+message);
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
        mqttAndroidClient.setCallback(mqttCallbackExtended);
    }

    public void connect(){
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();

        //設定是否自動重連
        mqttConnectOptions.setAutomaticReconnect(true);
        //設定是否清空session(設置為false表示server會保留client的連接記錄)
        mqttConnectOptions.setCleanSession(false);
        //server會每隔30s 傳消息給client 判斷是否在線
        mqttConnectOptions.setKeepAliveInterval(30);
        //設定是否重連
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
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
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);

                    System.out.println("連線成功");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println("連線失敗"+exception);
                }
            });
        } catch (MqttException e) {
            System.out.println("連線出錯:"+e);
            throw new RuntimeException(e);
        }
    }

    /**
    * 訂閱主題
     */
    public void subscribeToTemp() {
        try {
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println(topic+"訂閱成功");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println(topic+"訂閱失敗");
                }
            });
        } catch (MqttException e) {
            System.out.println(topic+"訂閱出錯:"+e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 取消訂閱主題
     */
    public void unsubscribeToTemp(){
        try {
            mqttAndroidClient.unsubscribe(topic, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println(topic+"取消訂閱成功");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println(topic+"取消訂閱失敗");
                }
            });
        } catch (MqttException e) {
            System.out.println(topic+"取消訂閱出錯:"+e);
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
            mqttAndroidClient.publish(topic, m, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println(topic+"發布成功");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println(topic+"發布失敗");
                }
            });
        } catch (MqttException e) {
            System.out.println(topic+"發布出錯"+e);
            throw new RuntimeException(e);
        }
    }


}
