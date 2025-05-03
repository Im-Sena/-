package com.sarmale.arduinobtexample_v3;




import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.sqrt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.opengl.Matrix;



public class MainActivity extends AppCompatActivity implements SensorEventListener{
   // Global variables we will use in the
    private static final String TAG = "FrugalLogs";
    private static final int REQUEST_ENABLE_BT = 1;
    //We will use a Handler to get the BT Connection statys
    public static Handler handler;
    private final static int ERROR_READ = 0; // used in bluetooth handler to identify message update

    //加速度
    private SensorManager m_sensorManager;
    private TextView m_val_x_TextView;
    private TextView m_val_y_TextView;
    private TextView m_val_z_TextView;

    //ジャイロ
    public final static String TAG2 = "SensorTest2";
    protected final static double RAD2DEG = 180/Math.PI;

    SensorManager sensorManager;

    float[] rotationMatrix = new float[9];
    float[] gravity = new float[3];
    float[] geomagnetic = new float[3];
    float[] attitude = new float[3];

    TextView azimuthText;
    TextView pitchText;
    TextView rollText;

//ジャイロ（補正あり）
    private SensorManager sensorManager2;
    private TextView textView;

    TextView yawtext;

//積分

    double[] angularVelocities = new double[39]; // Example angular velocity data
    double initialAngle = 0.0; // Initial angle

    double deltaTime = 0.1; // Time interval between samples

    double integratedAngle = initialAngle;

//姿勢算出洋
    float pitchA = 0; // ピッチ角（ラジアン）
    float yawA = 0;   // ヨー角（ラジアン）
    float rollA = 0;  // ロール角（ラジアン）
    private float[] finalRotationMatrix = new float[9];
    private float[] orientationValues = new float[3]; // ピッチ、ヨー、ロールの順

    TextView sisei;

    BluetoothDevice arduinoBTModule = null;
    UUID arduinoUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //We declare a default UUID to create the global variable
    @SuppressLint("MissingInflatedId")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Intances of BT Manager and BT Adapter needed to work with BT in Android.
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        //Intances of the Android UI elements that will will use during the execution of the APP
        TextView btReadings = findViewById(R.id.btReadings);
        TextView btDevices = findViewById(R.id.btDevices);
        Button connectToDevice = (Button) findViewById(R.id.connectToDevice);
        Button seachDevices = (Button) findViewById(R.id.seachDevices);
        Button clearValues = (Button) findViewById(R.id.refresh);
        Button writeButton = (Button ) findViewById(R.id.writeButton);
        Button Resetyaw = (Button ) findViewById(R.id.resetyaw);
        Button cal = (Button ) findViewById(R.id.Ca);
        Button calR = (Button ) findViewById(R.id.CaR);
        Log.d(TAG, "Begin Execution");

        //TextView myText = findViewById(R.id.my_text);


//kasokudo
        m_sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        m_val_x_TextView = findViewById(R.id.val_x);
        m_val_y_TextView = findViewById(R.id.val_y);
        m_val_z_TextView = findViewById(R.id.val_z);


//jyairo
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        azimuthText = (TextView)findViewById(R.id.azimuth);
        pitchText = (TextView)findViewById(R.id.pitch);
        rollText = (TextView)findViewById(R.id.roll);

//ジャイロ（補正あり）
        // Get an instance of the SensorManager
        sensorManager2 = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Get an instance of the TextView
        textView = findViewById(R.id.text_view);
        yawtext = (TextView)findViewById(R.id.yaw);
//姿勢
        sisei = findViewById(R.id.sisei);

        //Set< BluetoothDevice > devices = mAdapter.getBondedDevices();

        //Using a handler to update the interface in case of an error connecting to the BT device
        //My idea is to show handler vs RxAndroid
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {

                    case ERROR_READ:

                       String arduinoMsg = msg.obj.toString(); // Read message from Arduino
                        btReadings.setText(arduinoMsg);

                        break;
                }
            }
        };







        // Set a listener event on a button to clear the texts
        clearValues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //btDevices.setText("");
                //btReadings.setText("");
                aaa = "1";
                Snackbar.make(view, "Send:"+aaa, Snackbar.LENGTH_SHORT).show();
            }
        });

        // Create an Observable from RxAndroid
        //The code will be executed when an Observer subscribes to the the Observable
        final Observable<String> connectToBTObservable = Observable.create(emitter -> {
            Log.d(TAG, "Calling connectThread class");
            //Call the constructor of the ConnectThread class
            //Passing the Arguments: an Object that represents the BT device,
            // the UUID and then the handler to update the UI
            ConnectThread connectThread = new ConnectThread(arduinoBTModule, arduinoUUID, handler);
            connectThread.run();
            //Check if Socket connected
            if (connectThread.getMmSocket().isConnected()) {
                Log.d(TAG, "Calling ConnectedThread class");
                //The pass the Open socket as arguments to call the constructor of ConnectedThread
                ConnectedThread connectedThread = new ConnectedThread(connectThread.getMmSocket());
                connectedThread.run();
                if(connectedThread.getValueRead()!=null)
                {
                    // If we have read a value from the Arduino
                    // we call the onNext() function
                    //This value will be observed by the observer
                    emitter.onNext(connectedThread.getValueRead());
                }
                //We just want to stream 1 value, so we close the BT stream
                connectedThread.cancel();
            }
           // SystemClock.sleep(5000); // simulate delay
            //Then we close the socket connection
            connectThread.cancel();
            //We could Override the onComplete function
            emitter.onComplete();

        });

        connectToDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (arduinoBTModule != null) {

                    //We subscribe to the observable until the onComplete() is called
                    //We also define control the thread management with
                    // subscribeOn:  the thread in which you want to execute the action
                    // observeOn: the thread in which you want to get the response
                    connectToBTObservable.
                            observeOn(AndroidSchedulers.mainThread()).
                            subscribeOn(Schedulers.io()).
                            subscribe(valueRead -> {
                                //valueRead returned by the onNext() from the Observable
                                //btReadings.setText(valueRead);
                                //btReadings.setText(okay);

                                //We just scratched the surface with RxAndroid
                            });

                }
            }
        });

        seachDevices.setOnClickListener(new View.OnClickListener() {
            //Display all the linked BT Devices
            @Override
            public void onClick(View view) {
                //Check if the phone supports BT
                if (bluetoothAdapter == null) {
                    // Device doesn't support Bluetooth
                    Log.d(TAG, "Device doesn't support Bluetooth");
                } else {
                    Log.d(TAG, "Device support Bluetooth");
                    //Check BT enabled. If disabled, we ask the user to enable BT
                    if (!bluetoothAdapter.isEnabled()) {
                        Log.d(TAG, "Bluetooth is disabled");
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            Log.d(TAG, "We don't BT Permissions");
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                            Log.d(TAG, "Bluetooth is enabled now");
                        } else {
                            Log.d(TAG, "We have BT Permissions");
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                            Log.d(TAG, "Bluetooth is enabled now");
                        }

                    } else {
                        Log.d(TAG, "Bluetooth is enabled");
                    }
                    String btDevicesString="";
                    Set < BluetoothDevice > pairedDevices = bluetoothAdapter.getBondedDevices();

                    if (pairedDevices.size() > 0) {
                        // There are paired devices. Get the name and address of each paired device.
                        for (BluetoothDevice device: pairedDevices) {
                            String deviceName = device.getName();
                            String deviceHardwareAddress = device.getAddress(); // MAC address
                            Log.d(TAG, "deviceName:" + deviceName);
                            Log.d(TAG, "deviceHardwareAddress:" + deviceHardwareAddress);
                            //We append all devices to a String that we will display in the UI
                            btDevicesString=btDevicesString+deviceName+" || "+deviceHardwareAddress+"\n";
                            //If we find the HC 06 device (the Arduino BT module)
                            //We assign the device value to the Global variable BluetoothDevice
                            //We enable the button "Connect to HC 05 device"
                            if (deviceName.equals("HC-06")) {
                                Log.d(TAG, "HC-06 found");
                                arduinoUUID = device.getUuids()[0].getUuid();
                                arduinoBTModule = device;
                                //HC -05 Found, enabling the button to read results
                                connectToDevice.setEnabled(true);
                            }
                            //btDevices.setText(btDevicesString);
                            //<!--Linked Bluetooth devices:-->
                        }
                    }
                }
                Log.d(TAG, "Button Pressed");
            }
        });

        writeButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
                // 接続中のみ書込みを行う
            aaa = "0";
            Snackbar.make(view, "Send:"+aaa, Snackbar.LENGTH_SHORT).show();

                   /* try {
                        Snackbar.make(view, "Send1", Snackbar.LENGTH_SHORT).show();
                       tmpOut.write("1".getBytes());

                    } catch (IOException e) {

                    }*/

            }

        });

        Resetyaw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 接続中のみ書込みを行う
                bbb  = 1;
                Snackbar.make(view, "reseted yaw"+yaw, Snackbar.LENGTH_SHORT).show();


            }

        });

        cal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pitchC = (int)pitch;
                rollC = (int)roll;
                yawC = (int)yaw;

                Snackbar.make(view, "calibrated", Snackbar.LENGTH_SHORT).show();


            }

        });

        calR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pitchC = 0;
                rollC = 0;
                yawC = 0;

                Snackbar.make(view, "calibrated clear", Snackbar.LENGTH_SHORT).show();


            }

        });

    }

    public static String aaa = "1";
    public static String ccc = "";
    public static String ddd = "";

    public static String  eee = "";
    public static String fff = "";

    public static int bbb = 0;

    public static int pitchC = 0;
    public static int rollC = 0;
    public static int yawC = 0;

    public static double pitch = 0;
    public static double roll = 0;
    public static double yaw = 0;


    public static double attitu = 0;

    public static double AX = 0;
    public static double AY = 0;
    public static double AZ = 0;


    @Override protected void onResume() {
        super.onResume();
        // Event Listener登録
        Sensor accel = m_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        m_sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);

        //J
        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);

        //J2
        // Listenerの登録
        Sensor gyro = sensorManager2.getDefaultSensor(
                Sensor.TYPE_GYROSCOPE_UNCALIBRATED);

        if(gyro != null){
            sensorManager2.registerListener(this,
                    gyro, SensorManager.SENSOR_DELAY_UI);
        }
        else{
            String ns = "No Support";
            textView.setText(ns);
        }

    }

    @Override protected void onPause() {
        super.onPause();
        // Event Listener登録解除
        m_sensorManager.unregisterListener(this);
        //j
        sensorManager.unregisterListener(this);
        //j2
        sensorManager2.unregisterListener(this);
    }

    @Override public void onSensorChanged(SensorEvent event) {


        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            m_val_x_TextView.setText(String.format("%.3f", event.values[0]));//x
            m_val_y_TextView.setText(String.format("%.3f", event.values[1]));//y
            m_val_z_TextView.setText(String.format("%.3f", event.values[2]));//z

            AX = event.values[0]*10;
            AY = event.values[1]*10;
            AZ = sqrt((AX*AX)+(AY*AY));
            if(AZ<0) AZ*= -1;
            if(AZ>90) AZ=90;
            AZ+=100;

        }
        //j
        switch(event.sensor.getType()){
            case Sensor.TYPE_MAGNETIC_FIELD:
                geomagnetic = event.values.clone();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                gravity = event.values.clone();
                break;
        }

        if(geomagnetic != null && gravity != null){

            SensorManager.getRotationMatrix(
                    rotationMatrix, null,
                    gravity, geomagnetic);

            SensorManager.getOrientation(
                    rotationMatrix,
                    attitude);

            azimuthText.setText(Integer.toString(
                    (int)(attitude[0] * RAD2DEG)));
            pitchText.setText(Integer.toString(
                    (int)(attitude[1] * RAD2DEG)));
            rollText.setText(Integer.toString(
                    (int)(attitude[2] * RAD2DEG)));

            pitch = (int)(attitude[1] * RAD2DEG);
            roll = (int)(attitude[2] * RAD2DEG);
        }

        //j2
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {
            float sensorX =0;
            float sensorY=0;
            float sensorZ=0;
            float sensorEDX=0;
            float sensorEDY=0;
            float sensorEDZ=0;
            for(int i =0; i<angularVelocities.length; i++) {
                if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {
                     sensorX = event.values[0];
                     sensorY = event.values[1];
                     sensorZ = event.values[2];
                     sensorEDX = event.values[3];
                     sensorEDY = event.values[4];
                     sensorEDZ = event.values[5];
                    angularVelocities[i] = sensorZ - sensorEDZ;
                }
            }

            double xxx = 0;

            for (int i = 0; i < angularVelocities.length - 1; i++) {
                double averageAngularVelocity = (angularVelocities[i] + angularVelocities[i + 1]) / 2;
                double deltaAngle = averageAngularVelocity * deltaTime;
                integratedAngle += deltaAngle;
                xxx += deltaAngle;
            }

            if(xxx >360 || xxx <-360){
                integratedAngle = 0;
                xxx = 0;
            }



            if(bbb == 0) {
                if(integratedAngle>179){
                    integratedAngle = -180;
                }else if(integratedAngle < -179){
                    integratedAngle = 180;
                }

                yaw = integratedAngle;

            }else{
                integratedAngle = 0;
                bbb = 0;
            }


            if(yaw > 179){
                xxx = -2*integratedAngle;
            }else if(yaw < -179){
                xxx = 2*integratedAngle;
            }

            String strTmp = "Gyroscope\n"
                    + " X: " + sensorX + "\n"
                    + " Y: " + sensorY + "\n"
                    + " Z: " + sensorZ + "\n\n"

                    + " ドリフト予測\n"
                    + " driftX: " + sensorEDX + "\n"
                    + " driftY: " + sensorEDY + "\n"
                    + " driftZ: " + sensorEDZ + "\n" ;

            textView.setText(strTmp);
            yawtext.setText(String.format("%.0f",yaw ));
        }



            double qqq = 0;//arctan
            qqq = atan2(pitch-pitchC,roll-rollC);




            attitu = qqq*(180/Math.PI)+280;



            aaa = String.format("%.0f", attitu);
            eee = String.format("%.0f", AZ);
            fff = String.format("%.4f", AY);
            ccc = aaa+eee+"/";
            //fff = String.format("%.03d.0f", attitu);
            //ccc = fff+"/";
           // sisei.setText( String.format("%.0f",attitu));
            sisei.setText( String.format("%.4f", AZ));



/*
        //orientationValues[0] = pitchA;
        //orientationValues[1] = yawA;
        //orientationValues[2] = rollA;

        System.arraycopy(rotationMatrix, 0, finalRotationMatrix, 0, 9);
        SensorManager.getOrientation(finalRotationMatrix, orientationValues);

        // ピッチ、ヨー、ロールの角度を取得
        float calculatedPitch = orientationValues[1]; // ピッチ角（ラジアン）
        float calculatedYaw = orientationValues[0];   // ヨー角（ラジアン）
        float calculatedRoll = orientationValues[2];  // ロール角（ラジアン）

        String siseitext = "姿勢\n"
                + " P: " + calculatedPitch*(180/Math.PI) + "\n"
                + " Y: " + calculatedYaw*(180/Math.PI) + "\n"
                + " R: " + calculatedRoll*(180/Math.PI)  + "\n\n";

        sisei.setText(siseitext);
*/


    }




    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}