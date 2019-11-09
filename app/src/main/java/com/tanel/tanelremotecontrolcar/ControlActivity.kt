package com.tanel.tanelremotecontrolcar

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.tanel.tanelremotecontrolcar.MainActivity.BlueToothVars.bluetoothGatt

import kotlinx.android.synthetic.main.activity_control.*
import kotlinx.android.synthetic.main.content_control.*

class ControlActivity : AppCompatActivity() {
    private val stop = "S"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)
        setSupportActionBar(toolbar)
        var blueToothAddress = intent.getStringExtra("BLEid")
        fab.setOnClickListener { view ->
            Snackbar.make(view, "BLEid = $blueToothAddress", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        btnSendL.setOnTouchListener()
        {
            v: View?, event: MotionEvent ->
            val currentAction = "L"
            handleTouch(event, currentAction)
            true
        }
        btnSendF.setOnTouchListener()
        {
                v: View?, event: MotionEvent ->
            val currentAction = "F"
            handleTouch(event, currentAction)
            true
        }
        btnSendR.setOnTouchListener()
        {
                v: View?, event: MotionEvent ->
            val currentAction = "R"
            handleTouch(event, currentAction)
            true
        }
        btnSendB.setOnTouchListener()
        {
                v: View?, event: MotionEvent ->
            val currentAction = "V"
            handleTouch(event, currentAction)
            true
        }
        btnStop.setOnTouchListener()
        {
                v: View?, event: MotionEvent ->
            val currentAction = "S"
            handleTouch(event, currentAction)
            true
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    private fun handleTouch(m: MotionEvent, command: String)
    {

        when (m.actionMasked)
        {

            MotionEvent.ACTION_UP ->
            {
                //shows up as soon as button released
                sendGattMessage(stop)
                txtBLEid.text = "UP"
            }
            MotionEvent.ACTION_MOVE ->
            { //this one is active while pushed down
                txtBLEid.text = "MOVE"
                sendGattMessage(command)
            }
            MotionEvent.ACTION_DOWN -> //button pressed down
            {
                txtBLEid.text = "DOWN"
                sendGattMessage(command)
            }
            else -> txtBLEid.text = "NOTHING"
        }
    }
    private fun sendGattMessage(command: String) {

        // service and characteristic UUID are NOT the same for this project
        for (service in bluetoothGatt!!.services) {
            if (service.uuid == MainActivity.UUID_RC_CAR_SERVICE) {
                for (characteristic in service.characteristics) {
                    if (characteristic.uuid == MainActivity.UUID_RC_CAR_CHARACTERISTIC) {
                        //Okay we found that the service and characteristic exists
                        //now we tell the car to do stuff.
                        //put the command in
                        characteristic.value = command.toByteArray()
                        //works but my car needs this to be constantly send while the button is pushed down
                        // I don't know if android studio lets this happen
                        // an onPushedDown event.
                        MainActivity.writeSuccess =
                            bluetoothGatt!!.writeCharacteristic(characteristic)

                    }
                }
            }
        }
    }
}
