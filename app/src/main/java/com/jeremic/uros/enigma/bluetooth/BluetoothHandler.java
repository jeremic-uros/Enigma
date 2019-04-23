package com.jeremic.uros.enigma.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

// Encapsulates bluetooth manipulation
public class BluetoothHandler {
    public static final int REQUEST_ENABLE_BT = 7777;
    private static final String uniqueID = "9429d3d0-a7a9-4649-bec1-b3f649ae73b1";
    private BluetoothAdapter bluetoothAdapter;
    private Activity context;
    private boolean serverRunning;
    private AcceptThread bluetoothServer;



    public BluetoothHandler(Activity context) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.context = context;
        if(bluetoothAdapter ==  null) {
            // No bluetooth on phone..
            // TO:DO add display message
        }
        serverRunning = false;
    }


    public void turnBluetoothOn() {
        if(!bluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public boolean isEnabled() { return bluetoothAdapter.isEnabled();}

    // Enable so others can see your device
    public void setDiscoverability() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
        context.startActivity(discoverableIntent);
    }

    public Set<BluetoothDevice> getPairedDevices(){
        return bluetoothAdapter.getBondedDevices();
    }

    public boolean startDiscovery() {
        if(!bluetoothAdapter.isDiscovering()) {
            return bluetoothAdapter.startDiscovery();
        }
        return false;
    }

    public boolean cancelDiscovery() {
        return bluetoothAdapter.cancelDiscovery();

    }

    public void startBluetoothServer() {
        if( serverRunning) {
            Toast.makeText(context,"Ćeka se drugi uredjaj", Toast.LENGTH_SHORT).show();
        }
        else {
            bluetoothServer = new AcceptThread();
            bluetoothServer.start();
            serverRunning = true;
        }

    }

    public void stopBluetoothServer() {
        if(bluetoothServer != null) {
            bluetoothServer.cancel();
        }
    }

    public void connectToDevice(BluetoothDevice bluetoothDevice){
        bluetoothAdapter.cancelDiscovery();
        new ConnectThread(bluetoothDevice).start();
    }
    //Called by acceptThread
    private void manageConnection(final BluetoothSocket bluetoothSocket){

            new Thread() {
                public void run() {
                    try {
                        ((BluetoothCommunication) context).communicate(bluetoothSocket);
                    }catch (ClassCastException c) {
                        Log.e("BAD_CAST","The caller activity does not implement BluetoothCommunication...");
                    } catch (IOException e) {
                        e.printStackTrace(); // should not happen
                    }
                }
            }.start();
    }

    //Activity using BluetoothHandler must implement this inteface
    public interface BluetoothCommunication {
        void communicate(final BluetoothSocket bluetoothSocket) throws IOException;
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket bluetoothServerSocket;

        public AcceptThread() {
            // Using a temporary object to initiate bluetoothServerSocket because it is final
            BluetoothServerSocket tmp  = null;
            try{
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("server",UUID.fromString(uniqueID));
            }catch (IOException e){
                Log.e("Socket", "Socket's listen() method failed", e);
            }
            bluetoothServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = bluetoothServerSocket.accept();

                    if (socket != null) {
                        // A connection was accepted. Perform work associated with
                        // the connection in a separate thread.
                        manageConnection(socket);
                        bluetoothServerSocket.close();
                        serverRunning = false;
                        break;
                    }
                }
                catch (IOException e) {
                    Log.e("SOCKET", "Socket's accept() method failed", e);
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                bluetoothServerSocket.close();
            } catch (IOException e) {
                Log.e("Socket", "Could not close the connect socket", e);
            }
        }
    }

    private class ConnectThread extends  Thread {
        private final BluetoothSocket bluetoothSocket;
        private final BluetoothDevice bluetoothDevice;

        public ConnectThread(BluetoothDevice bluetoothDevice){
            this.bluetoothDevice = bluetoothDevice;
            // Using a temporary object to initiate bluetoothServerSocket because it is final
            BluetoothSocket tmp = null;


            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(uniqueID));
            } catch (IOException e) {
                Log.e("Socket", "Socket's create() method failed", e);
            }
            bluetoothSocket = tmp;
        }

        public void run() {
            // Stop discovery process so it doesn't slow down connection
            cancelDiscovery();
            try {
                //Connect to remote device, blocks until connected or exception
                bluetoothSocket.connect();
            } catch (IOException e) {
                Log.e("Socket", "Could not close the client socket");
                return;
            }
            //Device has connection succeeded
            manageConnection(bluetoothSocket);
        }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e("Socket", "Could not close the client socket", e);
            }
        }

    }
}
