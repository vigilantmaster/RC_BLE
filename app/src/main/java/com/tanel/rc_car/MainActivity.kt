package com.tanel.rc_car


import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat


const val REQUEST_ENABLE_BT = 345678
const val MY_PERMISSIONS_REQUEST_BLUETOOTH = 123456
const val MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN = 234567
const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 456789

private val BluetoothAdapter.isDisabled: Boolean
    get() = !isEnabled

 class MainActivity : AppCompatActivity() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private val handler:Handler = Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val thisActivity: AppCompatActivity = this
        checkPermissions(thisActivity)
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
fun checkPermissions(thisActivity: AppCompatActivity)
{
    if (ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.BLUETOOTH)
        != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(thisActivity,
            arrayOf(Manifest.permission.BLUETOOTH),
            MY_PERMISSIONS_REQUEST_BLUETOOTH)

    }
    if (ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.BLUETOOTH_ADMIN)
        != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(thisActivity,
            arrayOf(Manifest.permission.BLUETOOTH_ADMIN),
            MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN)
    }
    if (ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(thisActivity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
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
        //now connect to device
    }


}
