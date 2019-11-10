package com.tanel.tanelremotecontrolcar

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.tanel.tanelremotecontrolcar.MainActivity.BlueToothVars.bluetoothGatt

import kotlinx.android.synthetic.main.activity_control.*
import kotlinx.android.synthetic.main.content_control.*

class ControlActivity : AppCompatActivity(), View.OnTouchListener {
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        var touchCommand = "L"
        //which button was it?
        val buttonItIs = v!!.id
        when (buttonItIs) {
            btnSendB.id -> touchCommand = "V"
            btnSendF.id -> touchCommand = "F"
            btnSendL.id -> touchCommand = "L"
            btnSendR.id -> touchCommand = "R"
            btnStop.id -> touchCommand = "S"
        }
        //handle the command
        //To change body of created functions use File | Settings | File Templates.
        //handle the command
        handleTouch(event!!, touchCommand)

       //v!!.callOnClick()

        return false
    }

    private val stop = "S"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)
        setSupportActionBar(toolbar)
        val blueToothAddress = intent.getStringExtra("BLEid")
        fab.setOnClickListener { view ->
            Snackbar.make(view, "BLEid = $blueToothAddress", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        btnSendL.setOnTouchListener(this)

        btnSendF.setOnTouchListener(this)

        btnSendR.setOnTouchListener(this)

        btnSendB.setOnTouchListener (this)

        btnStop.setOnTouchListener(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    private fun handleTouch(m: MotionEvent, command: String) {
        when (m.actionMasked) {
            MotionEvent.ACTION_CANCEL -> {
                sendGattMessage(stop)
                txtBLEid.text = getString(R.string.Cancel)
            }
            MotionEvent.ACTION_UP -> {
                sendGattMessage(stop)
                txtBLEid.text = getString(R.string.Up)
            }
            MotionEvent.ACTION_BUTTON_RELEASE -> {
                sendGattMessage(stop)
                txtBLEid.text = getString(R.string.ButtonReleased)
            }
            MotionEvent.ACTION_BUTTON_PRESS -> {
                sendGattMessage(command)
                txtBLEid.text = getString(R.string.ButtonPressed)
            }
            MotionEvent.ACTION_DOWN -> {
                sendGattMessage(command)
                txtBLEid.text = getString(R.string.Down)
            }
            MotionEvent.ACTION_OUTSIDE -> {
                sendGattMessage(stop)
                txtBLEid.text = getString(R.string.Outside)
            }
            MotionEvent.ACTION_HOVER_EXIT -> {
                sendGattMessage(stop)
                txtBLEid.text = getString(R.string.HoverExit)
            }
            MotionEvent.ACTION_MOVE -> { //this one is active while pushed down
                sendGattMessage(command)
                txtBLEid.text = getString(R.string.Move)
            }
            else -> {
                //shows up as soon as button released when up not present
                sendGattMessage(command)
                txtBLEid.text = getString(R.string.NothingRegular)
            }
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
                        //send the string as a byte
                        MainActivity.writeSuccess =
                            bluetoothGatt!!.writeCharacteristic(characteristic)

                    }
                }
            }
        }
    }

}
