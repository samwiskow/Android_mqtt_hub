package com.epmmu.mqttdemo;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

public class MainActivity extends AppCompatActivity {

    public static final String BROKER_URL = "tcp://iot.eclipse.org:1883";
//    public static final String BROKER_URL = "tcp://broker.mqttdashboard.com:1883";


    String userid = "14042553";  // Alter this to your student id
    //We have to generate a unique Client id.
    String clientId = userid + "-subWHAT";

    // Default sensor to listen for -
    // Change to another if you are broadcasting a different sensor name
    String sensorname = "+";

    String topicname = userid + "/" + sensorname;

    final String TEMP = userid + "/temperature";

    private MqttClient mqttClient;

    Button publishTemperatureBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        publishTemperatureBtn = findViewById(R.id.publishTempBtn);


        publishTemperatureBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                System.out.println("PUBLISHING");
                runOnUiThread(new Runnable() {
                    public void run() {
                        publishTemperature();
                    }
                });
            }
        });


        // start new
        // Create MQTT client and start subscribing to message queue
        try {
            // change from original. Messages in "null" are not stored
            mqttClient = new MqttClient(BROKER_URL, clientId,null);
            mqttClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectionLost(Throwable cause) {
                    //This is called when the connection is lost. We could reconnect here.
                }

                @Override
                public void messageArrived(final String topic, MqttMessage message) throws Exception {
                    System.out.println("DEBUG: Message arrived. Topic: " + topic + "  Message: " + message.toString());
                    // get message data
                    final String messageStr = message.toString();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            System.out.println("Updating UI");

                            TextView tv;
                            // Update UI elements
                            if(topic.contains("brightness")) {
                                tv = findViewById(R.id.brightnessValueTV);
                                tv.setText(messageStr);
                            } else if(topic.contains("temperature")) {
                                tv = findViewById(R.id.tempValueTV);
                                tv.setText(messageStr);
                            } else if(topic.contains("rfid")) {
                                tv = findViewById(R.id.rfidValueTV);
                                tv.setText(messageStr);
                            }

                        }
                    });
                    if ((topicname+"/LWT").equals(topic)) {
                        System.err.println("Sensor gone!");
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //no-op
                }

                @Override
                public void connectComplete(boolean b , String s) {
                    //no-op
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // temp use of ThreadPolicy until use AsyncTask
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        startSubscribing();


    }


    public void startSubscribing() {
        try {
            mqttClient.connect();

            //Subscribe to all subtopics of home
            final String topic = topicname;
            mqttClient.subscribe(topic);

            System.out.println("Subscriber is now listening to "+topic);

        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    public void publishTemperature(){

        try{
            final MqttTopic topic = mqttClient.getTopic(TEMP);
            final int tempValue = 0;
            SeekBar et = findViewById(R.id.tempsb);
            final int temp = et.getProgress();
            final String temp2 = String.valueOf(temp);
            topic.publish(new MqttMessage(temp2.getBytes()));
            System.out.println("Message Published - Topic : " + TEMP + " Message : " + temp);

        } catch (MqttException e){
            e.printStackTrace();
        }
    }


}
