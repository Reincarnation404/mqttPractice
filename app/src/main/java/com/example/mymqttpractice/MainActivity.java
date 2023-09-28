package com.example.mymqttpractice;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.mymqttpractice.databinding.ActivityMainBinding;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;


//參考網站: https://wildanmsyah.wordpress.com/2017/05/11/mqtt-android-client-tutorial/#client

public class MainActivity extends AppCompatActivity {

    private mqttHelper mqttHelper;

    private wsHelper wsHelper;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //startMqtt();
        startWs();

        binding.btnSub2temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wsHelper.subscribeToTemp();
            }
        });

        binding.btnUnsub2temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mqttHelper.unsubscribeToTemp();
            }
        });

        binding.btnPub2temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = binding.txtTemp.getText().toString().trim();
                wsHelper.publishToTemp(msg);
            }
        });

    }


    //要用這個才會改textView
    private void startMqtt(){
        mqttHelper = new mqttHelper(getApplicationContext());
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                binding.textView.setText(message.toString());
                System.out.println("startMqtt的messageArrived "+message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

    }

    private void startWs(){
        wsHelper = new wsHelper(getApplicationContext());
        wsHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                binding.textView.setText(message.toString());
                System.out.println("startWs的messageArrived "+message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

    }
    


}




