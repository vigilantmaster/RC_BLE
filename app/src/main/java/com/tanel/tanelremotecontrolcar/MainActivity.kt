package com.tanel.tanelremotecontrolcar

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import kotlinx.android.synthetic.main.activity_main.*
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.widget.ArrayAdapter
import android.widget.RadioButton


class MainActivity : AppCompatActivity() {
    companion object BlueToothVars {

        private val BluetoothAdapter.isDisabled: Boolean
            get() = !isEnabled
        const val REQUEST_ENABLE_BT = 34
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
        btnPermissions.setOnClickListener {
            checkAndRequestPermissions()
        }
        btnSetupBLE.setOnClickListener {
            SetupBLE(bluetoothAdapter)
        }
        btnFindDevices.setOnClickListener {
            FindBLEDevices(bluetoothAdapter, handler)
        }
    }


    private fun checkAndRequestPermissions(): Boolean {

        val permissionLocation =
            PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {


            // You can show your dialog message here but instead I am
            // showing the grant permission dialog box
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )


        } else {

            //Requesting permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
        return false
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(this, "FINE LOCATION GRANTED", Toast.LENGTH_LONG).show()
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
            ActivityCompat.startActivityForResult(
                activity,
                blueToothIntent,
                REQUEST_ENABLE_BT,
                null
            )
        }

    }


    fun FindBLEDevices(bluetoothAdapter: BluetoothAdapter?, handler: Handler) {

        if (null != bluetoothAdapter) {

            var bleList: ArrayList<ScanResult> = ArrayList()
            val scanHandler = DeviceScanActivity(bluetoothAdapter, handler)
            val myScanCallback: ScanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    if (result !in bleList) {
                        //if we haven't seen it then it should be added
                        bleList.add(result)
                        val radioButton = RadioButton(this@MainActivity)
                        radioButton.text = (result.device.address + result.device.name)
                        radioBLEGroup.addView(radioButton)
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                }

                override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                    var resultIterator = results!!.iterator()
                    //want to remove previous radio buttons since we don't want duplication
                    radioBLEGroup.removeAllViews()
                    for (a_result in resultIterator)
                    {
                        //check if we already saw this device
                        if (a_result !in bleList) {
                            bleList.add(a_result)
                        }
                        for(option in bleList) {
                            val radioButton = RadioButton(this@MainActivity)
                            radioButton.text = option.scanRecord?.deviceName
                            radioBLEGroup.addView(radioButton)
                        }
                    }
                }
            }
            scanHandler.scanLeDevice(true, myScanCallback)



        }

    }
}
