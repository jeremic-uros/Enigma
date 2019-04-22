package com.jeremic.uros.enigma.view;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.jeremic.uros.enigma.R;
import com.jeremic.uros.enigma.bluetooth.BluetoothHandler;

import java.util.HashMap;
import java.util.Set;

public class PairedDevicesDialogFragment extends DialogFragment {
    private HashMap<String,BluetoothDevice> devicesMap;
    private BluetoothHandler bluetoothHandler;
    public static final int CALLBACK_NUMBER = 123;
    private boolean searching;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        devicesMap = new HashMap<>();
        bluetoothHandler = new BluetoothHandler(getActivity());
        searching = false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.paired_devices_fragment,container,false);

        view.findViewById(R.id.searchDevicesButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSearchPressed((Button) view);
            }
        });

        LinearLayout paired_devices = view.findViewById(R.id.pairedDevices);
        Set<BluetoothDevice> devices = bluetoothHandler.getPairedDevices();
        if(devices.size() > 0 ){
            for(BluetoothDevice device : devices) {
                devicesMap.put(device.getName(),device);
                Button pairedDevice = new Button(getActivity());
                pairedDevice.setText(device.getName());
                pairedDevice.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT ));
                pairedDevice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onDeviceSelect(view);
                    }
                });
                paired_devices.addView(pairedDevice);
            }
        }
        return view;
    }

    private boolean checkPremission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int check = getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if( check != PackageManager.PERMISSION_GRANTED ) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},CALLBACK_NUMBER);
                return false;
            }
            else
                return true;
        }
        else return true;
    }



    public void onSearchPressed(Button btn){
        if(checkPremission()) {
            if (searching) {
                bluetoothHandler.cancelDiscovery();
                btn.setText(getString(R.string.searchButton));
                searching = false;
            } else {
                bluetoothHandler.startDiscovery();
                btn.setText(getString(R.string.stopSearchButton));
                searching = true;
            }
        }
    }

    // Called by a broadcast receiver when a bluetooth device has been found nearby
    public void updateDeviceList(BluetoothDevice bluetoothDevice) {
        if( devicesMap != null) {
            devicesMap.put(bluetoothDevice.getName(),bluetoothDevice);
            LinearLayout paired_devices = getView().findViewById(R.id.otherDevices);
            Button otherDeviceButton = new Button(getActivity());
            otherDeviceButton.setText(bluetoothDevice.getName());
            otherDeviceButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT ));
            otherDeviceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onDeviceSelect(view);
                }
            });
            paired_devices.addView(otherDeviceButton);
        }
    }

    //When a device from list has been selected
    public void onDeviceSelect(View view) {
        BluetoothDevice bluetoothDevice = devicesMap.get(((Button)view).getText());
        bluetoothHandler.connectToDevice(bluetoothDevice);
        dismiss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        bluetoothHandler.cancelDiscovery();
    }
}
