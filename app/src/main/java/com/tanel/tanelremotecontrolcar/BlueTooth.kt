package com.tanel.tanelremotecontrolcar


import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.os.Handler


private const val SCAN_PERIOD: Long = 10000

class DeviceScanActivity(
    bluetoothAdapter: BluetoothAdapter,
    private var handler: Handler
)
{
    private var mScanning: Boolean = false
    private val mScanner = bluetoothAdapter.bluetoothLeScanner
    /*** Scan The BLE Device* Check the available BLE devices in the Surrounding* If the device is Already scanning then stop Scanning
     * * Else start Scanning and check 10 seconds* Send the available devices as a callback to the system
     * * Finish Scanning after 10 Seconds*/
    fun scanLeDevice(enable: Boolean, leScanCallback: ScanCallback) {
        when (enable) {
            true -> {
                // Stops scanning after a pre-defined scan period.
                handler.postDelayed({
                    mScanning = false
                    mScanner.startScan(leScanCallback)
                }, SCAN_PERIOD)
                mScanning = true
                mScanner.startScan(leScanCallback)
            }
            else -> {
                mScanning = false
               mScanner.stopScan(leScanCallback)
            }
        }
    }
}





