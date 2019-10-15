package com.tanel.rc_car


import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult


const val REQUEST_ENABLE_BT = 345678


private val BluetoothAdapter.isDisabled: Boolean
    get() = !isEnabled

abstract class MainActivity : AppCompatActivity() {

    abstract val handler:Handler
    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val thisActivity: AppCompatActivity = this
        SetupBLE(bluetoothAdapter, thisActivity)
        FindBLEDevices(bluetoothAdapter, handler)
    }
}

fun SetupBLE(bluetoothAdapter: BluetoothAdapter?, returnActivity: AppCompatActivity)
{

    // Ensures Bluetooth is available on the device and it is enabled. If not,
    // displays a dialog requesting user permission to enable Bluetooth.
    bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
        val blueToothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(returnActivity, blueToothIntent, REQUEST_ENABLE_BT, null)
    }

}

fun FindBLEDevices(bluetoothAdapter: BluetoothAdapter?, handler:Handler)
{

    if (null != bluetoothAdapter)
    {
        var scanHandler:DeviceScanActivity = DeviceScanActivity(bluetoothAdapter, handler)
        val myScanCallback: ScanCallback = object:ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
            }

           override fun onScanFailed(errorCode: Int){
           }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            }
        }
        scanHandler.scanLeDevice(true, myScanCallback)
    }


}
