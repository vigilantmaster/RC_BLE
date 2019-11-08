package com.tanel.tanelremotecontrolcar

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.RadioButton
import androidx.core.view.get
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    companion object BlueToothVars {
        //BLE lookup variables
        var bleList: ArrayList<ScanResult> = ArrayList()
        var bleDeviceAddressList: ArrayList<String> = ArrayList()
        private val BluetoothAdapter.isDisabled: Boolean
            get() = !isEnabled
        const val REQUEST_ENABLE_BT = 34
        const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 45
        //connection variables
        const val TAG = "bluetooth"
        var bluetoothGatt: BluetoothGatt? = null
        var bluetoothDevice: BluetoothDevice? = null
        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTING = 1
        private const val STATE_CONNECTED = 2
        internal var writeSuccess = false
        var testBool = false
        const val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        const val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
        const val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"
        internal val UUID_RC_CAR_SERVICE: UUID = UUID.fromString("19B10000-E8F2-537E-4F6C-D104768A1214")
        internal val UUID_RC_CAR_CHARACTERISTIC: UUID = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1214")


    }

    public val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private val handler: Handler = Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        SetupBLE(bluetoothAdapter)
        btnPermissions.setOnClickListener {
            checkAndRequestPermissions()
        }
        btnSetupBLE.setOnClickListener {
            //check if there is a device selected
            if (radioBLEGroup.checkedRadioButtonId != -1) {
                connectToBle()
                startControlActivity()

            } else {
                Toast.makeText(this, "PLEASE MAKE A SELECTION", Toast.LENGTH_LONG).show()
            }

        }
        btnFindDevices.setOnClickListener {
            findBLEDevices(bluetoothAdapter, handler)
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
                if ((PermissionChecker.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PermissionChecker.PERMISSION_GRANTED)
                ) {
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
    private fun startControlActivity()
    {
        val intent = Intent(this, ControlActivity::class.java)
        // To pass any data to next activity
        intent.putExtra("BLEid", bluetoothGatt!!.device.address)
        intent.putExtra("bluetoothDevice", bluetoothDevice)
        // start your next activity
        startActivity(intent)
    }
    private fun connectToBle() {

        var connectionState = STATE_DISCONNECTED
        //using the index given gives me the next radio button so subtract one
        val radioButtonCheck: RadioButton =
            radioBLEGroup[radioBLEGroup.checkedRadioButtonId - 1] as RadioButton
        //use the radio button to get the values extracted with scan
        bluetoothDevice =
            bluetoothAdapter?.getRemoteDevice(bleList[radioBLEGroup.checkedRadioButtonId - 1].device.address)
        val gattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(
                gatt: BluetoothGatt,
                status: Int,
                newState: Int
            ) {
                val intentAction: String
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        intentAction = ACTION_GATT_CONNECTED
                        connectionState = STATE_CONNECTED
                        broadcastUpdate(intentAction)
                        Log.i(TAG, "Connected to GATT server.")
                        //discover its services
                        gatt.discoverServices()
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        intentAction = ACTION_GATT_DISCONNECTED
                        connectionState = STATE_DISCONNECTED
                        Log.i(TAG, "Disconnected from GATT server.")
                        broadcastUpdate(intentAction)
                    }
                }
            }
            override fun onServicesDiscovered(gat: BluetoothGatt, status: Int)
            {
                // when service is discovered
                // service and characteristic UUID are NOT the same for this project
                for (service in gat.services)
                {
                    if(service.uuid == UUID_RC_CAR_SERVICE)
                    {
                        for (characteristic in service.characteristics)
                        {
                            if(characteristic.uuid == UUID_RC_CAR_CHARACTERISTIC)
                            {
                                //Okay we found that the service and characteristic exists
                                //now we tell the car to do stuff.
                                //for default i'll tell it to spin in circles by adding the value "L" or "R"
                                characteristic.value = "L".toByteArray()
                                //works but my car needs this to be constantly send while the button is pushed down
                                // I don't know if android studio lets this happen
                                // an onPushedDown event.
                                writeSuccess = gat.writeCharacteristic(characteristic)
                                if (writeSuccess)
                                {
                                    //this says write was successful
                                    testBool = writeSuccess
                                }
                            }
                        }
                    }
                }
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                super.onCharacteristicWrite(gatt, characteristic, status)
                Toast.makeText(
                    this@MainActivity,
                    "OnCharacteristicWrite value " + characteristic!!.value,
                    Toast.LENGTH_LONG
                ).show()
            }



        }
        bluetoothGatt = bluetoothDevice?.connectGatt(this, false, gattCallback)
        bluetoothGatt!!.connect()
        //now we are setup and connected
        //bluetoothGatt?.discoverServices()



    }


    private fun findBLEDevices(bluetoothAdapter: BluetoothAdapter?, handler: Handler) {

        if (null != bluetoothAdapter) {
            radioBLEGroup.removeAllViewsInLayout()
            val scanHandler = DeviceScanActivity(bluetoothAdapter, handler)
            val myScanCallback: ScanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    // return if already found
                    if (result.device.address in bleDeviceAddressList) return
                    //if we haven't seen it then it should be added
                    bleDeviceAddressList.add(result.device.address)
                    bleList.add(result)
                    val radioButton = RadioButton(this@MainActivity)
                    radioButton.text = (result.device.address + result.device.name)
                    radioBLEGroup.addView(radioButton)
                }

                override fun onScanFailed(errorCode: Int) {
                }

                override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                    val resultIterator = results!!.iterator()
                    //want to remove previous radio buttons since we don't want duplication
                    radioBLEGroup.removeAllViewsInLayout()
                    resultIterator.forEach { a_result ->
                        //check if we already saw this device
                        if (a_result in bleList) {
                            return
                        } else {
                            bleList.add(a_result)
                        }
                        // bleList.forEach { option ->
                        //    val radioButton = RadioButton(this@MainActivity)
                        //    radioButton.text = option.scanRecord?.deviceName
                        //    radioBLEGroup.addView(radioButton)
                        //  }
                    }
                }
            }
            scanHandler.scanLeDevice(true, myScanCallback)


        }

    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }



}


