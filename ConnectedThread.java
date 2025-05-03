package com.sarmale.arduinobtexample_v3;


import static android.os.Looper.loop;
import static com.sarmale.arduinobtexample_v3.MainActivity.aaa;
import static com.sarmale.arduinobtexample_v3.MainActivity.ccc;
import static com.sarmale.arduinobtexample_v3.MainActivity.ddd;
import static com.sarmale.arduinobtexample_v3.MainActivity.eee;
import static com.sarmale.arduinobtexample_v3.MainActivity.yaw;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//Class that given an open BT Socket will
//Open, manage and close the data Stream from the Arduino BT device
public class ConnectedThread extends Thread {

    private static final String TAG = "FrugalLogs";
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private String valueRead;

    public ConnectedThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }
        //Input and Output streams members of the class
        //We wont use the Output stream of this project
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public String getValueRead(){
        return valueRead;
    }

    public void run() {

        byte[] buffer = new byte[1024];
        int bytes = 0; // bytes returned from read()
        int numberOfReadings = 0; //to control the number of readings from the Arduino
        String x = "0";
        // Keep listening to the InputStream until an exception occurs.
        //We just want to get 1 temperature readings from the Arduino



        while (numberOfReadings < 1) {
            try {
               if(!aaa.equals(ddd)) {
                   // String txt = String.valueOf(yaw);
                    mmOutStream.write(ccc.getBytes());//
                    ddd = aaa;
                  // ddd = String.format("%.0f",yaw)
               }

            } catch (IOException e) {
                Log.d(TAG, "Input stream was disconnected", e);
                break;
            }
        }




    }


    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}