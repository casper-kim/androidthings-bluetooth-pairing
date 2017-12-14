package com.github.casper_kim.androidthings.bluetoothpairing;

import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;

import com.google.android.things.bluetooth.BluetoothProfile;
import com.google.android.things.bluetooth.BluetoothProfileManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {
    public static final String TAG = "bt-pairing-test";
    BluetoothAdapter mBluetoothAdapter = null;

    private static final Set<String> TARGET_DEVICE_NAME = new HashSet<String>();

    static {
        //todo: add target device name after getting device name with startDiscovery
        TARGET_DEVICE_NAME.add("BT-S15");
    }

    private List<BluetoothDevice> mTargetDevices = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "BT not supported..");
            return;
        }

        if(!mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "BT enable.");
            mBluetoothAdapter.enable();
        }
        else{
            Log.d(TAG, "BT already enabled.");
        }


        checkBluetoothProfile();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mReceiver);
    }

    private void checkBluetoothProfile() {
        BluetoothProfileManager bluetoothProfileManager = new BluetoothProfileManager();
        List<Integer> enabledProfiles = bluetoothProfileManager.getEnabledProfiles();
        boolean isA2DPEnabled = false;
        for (int profile : enabledProfiles) {
            Log.d(TAG, "BT Profile enabled:" + profile);
            if (profile == BluetoothProfile.A2DP) {
                isA2DPEnabled = true;
            }
        }

        if (!isA2DPEnabled) {
            Log.d(TAG, "Enabling A2dp mode.");
            List toEnable = Arrays.asList(BluetoothProfile.A2DP);
            bluetoothProfileManager.enableAndDisableProfiles(toEnable, null);
        } else {
            Log.d(TAG, "A2dp mode already enabled.");
        }
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_A:
                startDiscovery();
                break;
            case KeyEvent.KEYCODE_B:
                stopDiscovery();
                break;
            case KeyEvent.KEYCODE_C:
                createBondDiscoveredDevices();
                break;
            case KeyEvent.KEYCODE_D:
                pairBondedDevices();
                break;
            case KeyEvent.KEYCODE_E:
                unpairBondedDevices();
                break;
        }

        return super.onKeyUp(keyCode, event);
    }


    private void startDiscovery() {
        Log.d(TAG, "startDiscovery");
        if (mBluetoothAdapter != null) {
            mTargetDevices.clear();

            Log.d(TAG, "request BT startDiscovery");
            mBluetoothAdapter.startDiscovery();
        } else {
            Log.d(TAG, "mBluetoothAdapter!=null");
        }
    }

    private void stopDiscovery() {
        Log.d(TAG, "stopDiscovery");
        if (mBluetoothAdapter != null) {
            Log.d(TAG, "cancel BT Discovery");
            mBluetoothAdapter.cancelDiscovery();
        } else {
            Log.d(TAG, "mBluetoothAdapter!=null");
        }
    }

    private void createBondDiscoveredDevices() {
        Log.d(TAG, "createBondDiscoveredDevices");
        if (mTargetDevices.size() <= 0) {
            Log.d(TAG, "no new discovered device");
            return;
        }
        for (BluetoothDevice device : mTargetDevices) {
            Log.d(TAG, "discovered device:" + device.getName());

            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                Log.d(TAG, "already bonded.");
            } else {
                Log.d(TAG, "try to createBond..");
                boolean result = device.createBond();
                Log.d(TAG, "device.createBond() result: " + result);
            }
        }
    }

    private void pairBondedDevices() {
        Log.d(TAG, "pairBondedDevices");
        if (mBluetoothAdapter != null) {
            Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : bondedDevices) {
                Log.d(TAG, "bonded device:" + device.getName());
                if (TARGET_DEVICE_NAME.contains(device.getName())) {
                    pairDevice(device);

                } else {
                    Log.d(TAG, "it is not target device");
                }
            }
        } else {
            Log.d(TAG, "mBluetoothAdapter!=null");
        }
    }

    private void unpairBondedDevices() {
        Log.d(TAG, "unpairBondedDevices");
        if (mBluetoothAdapter != null) {
            Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : bondedDevices) {
                Log.d(TAG, "bonded device:" + device.getName());
                if (TARGET_DEVICE_NAME.contains(device.getName())) {
                    unpairDevice(device);
                } else {
                    Log.d(TAG, "it is not target device");
                }
            }
        } else {
            Log.d(TAG, "mBluetoothAdapter!=null");
        }
    }

    private void pairDevice(final BluetoothDevice device) {
        Log.d(TAG, "pairDevice");

        mBluetoothAdapter.getProfileProxy(this, new android.bluetooth.BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, android.bluetooth.BluetoothProfile proxy) {
                Log.d(TAG, "profile proxy connected..");
                BluetoothA2dp a2dp = (BluetoothA2dp) proxy;

                try {
                    Class clazz = Class.forName("android.bluetooth.BluetoothA2dp");
                    Method method = clazz.getMethod("connect", BluetoothDevice.class);
                    boolean result = (boolean) method.invoke(a2dp, device);
                    Log.d(TAG, "connect result:" + result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {
                Log.d(TAG, "profile proxy disconnected..");
            }
        }, BluetoothProfile.A2DP);
    }

    private void unpairDevice(final BluetoothDevice device) {
        Log.d(TAG, "unPairDevice");
        mBluetoothAdapter.getProfileProxy(this, new android.bluetooth.BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, android.bluetooth.BluetoothProfile proxy) {
                Log.d(TAG, "profile proxy connected..");
                BluetoothA2dp a2dp = (BluetoothA2dp) proxy;

                try {
                    Class clazz = Class.forName("android.bluetooth.BluetoothA2dp");
                    Method method = clazz.getMethod("setPriority", BluetoothDevice.class, int.class);
                    boolean result = (boolean) method.invoke(a2dp, device, 0);
                    Log.d(TAG, "setPriority, result: " + result);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Class clazz = Class.forName("android.bluetooth.BluetoothA2dp");
                    Method method = clazz.getMethod("disconnect", BluetoothDevice.class);
                    boolean result = (boolean) method.invoke(a2dp, device);
                    Log.d(TAG, "disconnect, result: " + result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {
                Log.d(TAG, "profile proxy disconnected..");
            }
        }, BluetoothProfile.A2DP);

        SystemClock.sleep(3000);

        try {
            Class clazz = Class.forName("android.bluetooth.BluetoothDevice");
            Method method = clazz.getMethod("removeBond");
            boolean result = (boolean) method.invoke(device);
            Log.d(TAG, "disconnect result:" + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.d(TAG, "ACTION_FOUND: " + device.getName());
                    if (device.getName() != null && TARGET_DEVICE_NAME.contains(device.getName())) {
                        Log.d(TAG, "target device found..");
                        mTargetDevices.add(device);
                    }
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                    int previousState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);

                    Log.d(TAG, "ACTION_BOND_STATE_CHANGED: state:" + state + ", previous:" + previousState);

                    break;
            }
        }
    };
}
