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
import android.service.autofill.FillEventHistory
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat




private val BluetoothAdapter.isDisabled: Boolean
    get() = !isEnabled

 class MainActivity : AppCompatActivity() {
    companion object BlueToothVars{
        var blueToothGranted: Boolean = false
        var blueToothAdminGranted: Boolean = false
        var accessFineLocationGranted: Boolean = false
        const val REQUEST_ENABLE_BT = 34
        const val MY_PERMISSIONS_REQUEST_BLUETOOTH = 12
        const val MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN = 23
        const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 45
    }
     private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
         val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
         bluetoothManager.adapter
     }
     private val handler: Handler = Handler()
     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         setContentView(R.layout.activity_main)
         checkPermissions()
         SetupBLE(bluetoothAdapter)
         FindBLEDevices(bluetoothAdapter, handler)

     }

     fun checkPermissions() {
         if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
             != PackageManager.PERMISSION_GRANTED
         ) {
             ActivityCompat.requestPermissions(
                 this,
                 arrayOf(Manifest.permission.BLUETOOTH),
                 MY_PERMISSIONS_REQUEST_BLUETOOTH
             )


         }
         val BluetoothAminPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
             // Here, thisActivity is the current activity
             if (!blueToothAdminGranted) {

                 // Should we show an explanation?
                 if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                         Manifest.permission.BLUETOOTH_ADMIN)) {

                     // Show an explanation to the user *asynchronously* -- don't block
                     // this thread waiting for the user's response! After the user
                     // sees the explanation, try again to request the permission.

                 } else {

                     // No explanation needed, we can request the permission.

                     ActivityCompat.requestPermissions(this,
                         arrayOf(Manifest.permission.BLUETOOTH_ADMIN),
                         MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN)

                     // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                     // app-defined int constant. The callback method gets the
                     // result of the request.
                 }
             } else {
                 // Permission has already been granted
             }


         if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
             != PackageManager.PERMISSION_GRANTED
         ) {
             ActivityCompat.requestPermissions(
                 this,
                 arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                 MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
             )
         }


     }

     override fun onRequestPermissionsResult(
         requestCode: Int,
         permissions: Array<String>, grantResults: IntArray
     ) {
         when (requestCode) {
             MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                 // If request is cancelled, the result arrays are empty.
                 if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                     // permission was granted, yay! Do the
                     // contacts-related task you need to do.
                     Toast.makeText(this, "FINE LOCATION GRANTED", Toast.LENGTH_LONG).show()
                 } else {
                     // permission denied, boo! Disable the
                     // functionality that depends on this permission.
                 }
                 return
             }
             MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN -> {
                 // If request is cancelled, the result arrays are empty.
                 if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                     // permission was granted, yay! Do the
                     // contacts-related task you need to do.

                     Toast.makeText(this, "BLUETOOTH ADMIN GRANTED", Toast.LENGTH_LONG).show()
                 } else {
                     // permission denied, boo! Disable the
                     // functionality that depends on this permission.
                 }
                 return
             }
             MY_PERMISSIONS_REQUEST_BLUETOOTH -> {
                 // If request is cancelled, the result arrays are empty.
                 if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                     // permission was granted, yay! Do the
                     // contacts-related task you need to do.
                     Toast.makeText(this, "BLUETOOTH GRANTED", Toast.LENGTH_LONG).show()
                 } else {
                     // permission denied, boo! Disable the
                     // functionality that depends on this permission.
                 }
                 return
             }

             // Add other 'when' lines to check for other
             // permissions this app might request.
             else -> {
                 // Ignore all other requests.
             }
         }
     }
         fun SetupBLE(bluetoothAdapter: BluetoothAdapter?) {
            val activity = this
             // Ensures Bluetooth is available on the device and it is enabled. If not,
             // displays a dialog requesting user permission to enable Bluetooth.
             bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
                 val blueToothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                 startActivityForResult(activity, blueToothIntent, REQUEST_ENABLE_BT, null)
             }

         }


         fun FindBLEDevices(bluetoothAdapter: BluetoothAdapter?, handler: Handler) {

             if (null != bluetoothAdapter) {
                 checkPermissions()

                 var scanHandler: DeviceScanActivity = DeviceScanActivity(bluetoothAdapter, handler)
                 val myScanCallback: ScanCallback = object : ScanCallback() {
                     override fun onScanResult(callbackType: Int, result: ScanResult) {
                     }

                     override fun onScanFailed(errorCode: Int) {
                     }

                     override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                     }
                 }
                 scanHandler.scanLeDevice(true, myScanCallback)
                 //now connected to device
             }

         }

 }
