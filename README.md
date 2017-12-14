# Bluetooth Pairing Util for Android Things

This util helps the android things connect to bluetooth speaker. It contains BT discovering, bonding, pairing funtions.

It is tested with Android Things Preview 6, Raspberry Pi 3, Bluetooth Speaker (BT-S15).



## Configurations

Add your target device name to the TARGET_DEVICE_NAME list in the MainActivity. The device name can be shown below discovery list.

```java
    static {
        //todo: add target device name after getting device name with startDiscovery
        TARGET_DEVICE_NAME.add("BT-S15");
    }
```



## How to use it

### Discovery

Press A key or send key event with adb command to start BT discovering and find target devices.

```
casper-pc$ adb shell input keyevent KEYCODE_A
```

```
rpi3$ input keyevent KEYCODE_A
```



You can check the device name on the adb logs. Add the name to the TARGET_DEVICE_NAME list for pairing. After adding the target device name, the device information will be found while discovering.

```bash
bluetoothpairing D/bt-pairing-test: startDiscovery
bluetoothpairing D/bt-pairing-test: request BT startDiscovery
bluetoothpairing D/bt-pairing-test: ACTION_FOUND: null
bluetoothpairing D/bt-pairing-test: ACTION_FOUND: LC-Speaker-T5
bluetoothpairing D/bt-pairing-test: ACTION_FOUND: BT-S15  #in TARGET_DEVICE_NAME list
bluetoothpairing D/bt-pairing-test: target device found.. #in TARGET_DEVICE_NAME list
```



### Stop Discovery

Press B key or send KEYCODE_B event with adb command to stop BT discovering.

```
bluetoothpairing D/bt-pairing-test: stopDiscovery
bluetoothpairing D/bt-pairing-test: cancel BT Discovery
```



### Create Bond

Press C key or send KEYCODE_C event with adb command to createBond with found devices.

```
bluetoothpairing I/BluetoothDevice: createBondDiscoveredDevices
bluetoothpairing I/BluetoothDevice: discovered device: BT-S15
bluetoothpairing I/BluetoothDevice: createBond() for device 12:00:00:00:01:CC called by pid: 1701 tid: 1701
bluetoothpairing D/bt-pairing-test: device.createBond() result: true

bluetoothpairing D/bt-pairing-test: ACTION_BOND_STATE_CHANGED: state:10, previous:12
```



### Pairing

Press D key or send KEYCODE_D event with adb command to pair the bonded/target devices.

```
bluetoothpairing D/bt-pairing-test: pairBondedDevices
bluetoothpairing D/bt-pairing-test: bonded device:BT-S15
bluetoothpairing D/bt-pairing-test: pairDevice
bluetoothpairing D/BluetoothA2dp: Proxy object connected
bluetoothpairing D/bt-pairing-test: profile proxy connected..
bluetoothpairing D/BluetoothA2dp: connect(12:00:00:00:01:CC)
bluetoothpairing D/bt-pairing-test: connect result:true
```



## Known Issues

Press E key or send KEYCODE_E to unpair the target devices but it is connected again automatically..

```
bluetoothpairing D/bt-pairing-test: unpairBondedDevices
bluetoothpairing D/bt-pairing-test: bonded device:BT-S15
bluetoothpairing D/bt-pairing-test: unPairDevice
bluetoothpairing I/BluetoothDevice: removeBond() for device 12:00:00:00:01:CC called by pid: 1701 tid: 1701
bluetoothpairing D/bt-pairing-test: disconnect result:true

bluetoothpairing D/bt-pairing-test: ACTION_BOND_STATE_CHANGED: state:10, previous:12
```

