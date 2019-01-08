package com.example.shubh.bluet;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class bcservice {

    private static final String appName="bluet";

    private static final UUID myuuid= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter m;
    private AcceptThread mat;
    private ConnectThread mct;
    private ConnectedThread mkt;
    private UUID deviceUUID;
    public  BluetoothDevice mmDevice;
    Context mContext;
    ProgressDialog mdialog;


    public bcservice( Context mContext) {
       // this.m = m;
        this.mContext = mContext;
        m=BluetoothAdapter.getDefaultAdapter();
        start();

    }

    private class AcceptThread extends Thread{
        private final BluetoothServerSocket msocket;

        public AcceptThread(){
            BluetoothServerSocket tmp=null;

            try{
                tmp=m.listenUsingInsecureRfcommWithServiceRecord(appName,myuuid);
            }catch(IOException e){

            }
            msocket=tmp;

        }

        public void run(){
            BluetoothSocket socket=null;
            try{
                socket=msocket.accept();
                Log.d(TAG, "run: RFCOM server socket accepted connection.");
            }catch(IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
            }

            if(socket!=null)
            {
                connected(socket,mmDevice);
            }
            Log.i(TAG, "END mAcceptThread ");
        }
        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {
                msocket.close();
            } catch (IOException e) {
                Log.e("c", "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage() );
            }
        }

    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started.");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run() {
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread ");


            try {

                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {

            }

            mmSocket = tmp;

            m.cancelDiscovery();
            try {

                mmSocket.connect();
            } catch (IOException e) {
                try {
                    mmSocket.close();
                    Log.d(TAG, "run: Closed Socket.");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }

            }


            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }





    }
    public synchronized void start() {
        Log.d(TAG, "start");

        if (mct != null) {
            mct.cancel();
            mct = null;
        }
        if (mat == null) {
            mat = new AcceptThread();
            mat.start();
        }
    }

    public void startClient(BluetoothDevice device,UUID uuid)
    {
        mdialog=ProgressDialog.show(mContext,"connecting","wait",true);

        mct=new ConnectThread(device,uuid);
        mct.start();
    }

    private class ConnectedThread extends Thread{
        private final BluetoothSocket msock;
        private final InputStream mis;
        private final OutputStream mos;

        public ConnectedThread(BluetoothSocket socket){
            msock=socket;
            InputStream in=null;
            OutputStream out=null;

           // mdialog.dismiss();
            try{
                mdialog.dismiss();
            }catch (NullPointerException e){
                e.printStackTrace();
            }

            try{


            in=msock.getInputStream();
            out=msock.getOutputStream();}
            catch(IOException e) {
                e.printStackTrace();
               // Log.d(TAG,(String)e);

            }
            mis=in;
            mos=out;


        }

        public void run(){
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = mis.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);

                    Intent i=new Intent("incomingMessage");
                    i.putExtra("theMessage",incomingMessage);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);

                    Toast.makeText(mContext,incomingMessage,Toast.LENGTH_SHORT);
                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage() );
                    break;
                }
            }
        }


        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: outputstream: " + text);
            try {
                mos.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error output stream. " + e.getMessage() );
            }
        }
        public void cancel() {
            try {
                msock.close();
            } catch (IOException e) { }
        }


    }
    private void connected(BluetoothSocket mmSocket,BluetoothDevice mmDevice) {
        mkt = new ConnectedThread(mmSocket);
        mkt.start();
    }

    public void write(byte[] out) {
        ConnectedThread r;
        Log.d(TAG, "write: Write Called.");
        if(mkt!=null)
        mkt.write(out);
    }

}




