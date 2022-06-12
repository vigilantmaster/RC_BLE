# RC_BLE  (work in progress)
    Android App that uses bluetooth low energy to control an arduino rc car

## User Stories:

    As a user I would like to connect to the rc vehicle.
    As a user I would like to see if I am connected.
    As a user I would like to connect to a different vehicle.
    As a user I would like to move the vehicle forward.
    As a user I would like to move the vehicle backward.
    As a user I would like to turn the vehicle right.
    As a user I would like to turn the vehicle left.
    As a user I would like to stop the vehicle.
    As a user I would like to turn the vehicle lights on and off.

## Definitions, Use cases, Terms & Definitions
### BLE:
    Bluetooth Low Energy, a subset of the 2.4 GHz Bluetooth wireless technology that specializes in low power and oftentimes infrequent data transmissions for connected devices.
    This is the communication medium that will be used for this project.
### Central/Client:
    A device that scans for and connects to BLE peripherals in order to perform some operation. In the context of app development, this is typically an    Android device.
    (This will be the android device.)
### Peripheral/Server:
    A device that advertises its presence and is connected to by a central in order to accomplish some task. In the context of app development, this is typically a BLE device you’re working with, like a heart rate monitor.
    (This will be the RC car.)
### GATT Service:
    A collection of characteristics (data fields) that describes a feature of a device, e.g. the Device Information service can contain a characteristic representing the serial number of the device, and another characteristic representing the battery level of the device.
    (Drive Service will be used on the device.)
### GATT Characteristic:
    An entity containing meaningful data that can typically be read from or written to, e.g. the Serial Number String characteristic.
    CurrentDriveCommand <- this will hold the drive command character and let the server control the vehicle it's attached to.
### GATT Descriptor:
    A defined attribute that describes the characteristic that it’s attached to, e.g. the Client Characteristic Configuration descriptor shows if the central is currently subscribed to a characteristic’s value change.
    
### Notifications:
    A means for a BLE peripheral to notify the central when a characteristic’s value changes. The central doesn’t need to acknowledge that it’s received the packet.
    . without receipt of sent value
### Indictations:
    Same as an indication, except each data packet is acknowledged by the central. This guarantees their delivery at the cost of throughput.
    . with receipt of sent value
### UUID:
    Universally unique identifier, 128-bit number used to identify services, characteristics and descriptors.

## Connection: 
    I plan to connect to the BLE using Coroutines and KABLE by Juullabs.
## Connection Flow
### 1. Launch coroutine to start scanning
    ScannerFlow - provides stream of advertisements
### 2. Stop Scanning and Connect 
    Filter for the one we want and connect -- use of filter is suggested
    or maybe display them all and let the user decide. - display an activity that displays all ble devices and extract the uuid to connect to.
    displaying all the devices could allow to operate multiple devices and switch between them. Maybe add a tab for each device that is connected.
### 3. MutableStateFlow - Data Binding
    update the UI

## SCANNER OBJECT TEMPLATE:
    val Scanner{
    filters = null
    logging{
        engine = SystemLogEngine
        level = Warnings
        format = Multiline}
    }

## FILTER:
    Filter.Service(uuidFrom("""))  <- can be used for the filter and connect to device right away.

## Connection Types:
### 1. With Confirmation - might be slow but would allow for a stable connection that will know if the signal did not get received.
    . When button is pressed down the signal is sent and a confirmation is received. If the confirmation is not received another signal is sent. 
    . When the button is released a signal to stop is sent with a confirmation and sent again if there is not confirmation.
       . This could have an issue if the stop signal is being sent and the user presses another button.  
       . The stop signal process should be halted upon another press.

### 2. Without Confirmation - send signal and not expect a response.  
    This sends a signal with no confirmation.  Could work if a constant signal is sent.
       . When button is pressed down the signal is constantly sent and the device receives the signal constantly. - could be time limited
       . When the button is released constantly send stop signal. - maybe limit the time this happens
       . Useful if the device is behaving slow with previous method but not reliable if there is interference or signal loss.
       . When button is pressed down send signal once and assume it was received.
       . When button is released send signal to stop and assume it was received.

## USER INTERFACE: - methodology is dependent on Connection type.
    All elements should be labeled
    . Buttons
        . FORWARD BUTTON
        . BACKWARD BUTTON
        . LEFT TURN BUTTON
        . RIGHT TURN BUTTON
        . STOP BUTTON
        . CONNECTION BUTTON - turns into DISCONNECTION BUTTON when connected.
    . JoyStick 
        . Joystick referencing center of screen when not used
        . CONNECTION BUTTON - turns into DISCONNECTION BUTTON when connected.
        . Reference state of travel based on joystick location relative to center of screen.

## References used
    https://punchthrough.com/android-ble-guide/
    https://zoewave.medium.com/kotlin-beautiful-low-energy-ble-91db3c0ab887

## GLOBAL ENUMERATION:
    TYPE_OF_MOVEMENT
    Stop : 0
    Forward: 1
    Backward : 2
    Turn Left : 3
    Turn Right : 4

## FUNCTIONS:
    Bool Movement(typeOfMovement)
    {
        send movement character to server using BLE
    }
# Sample Code Using Kable from Juul Labs
## Kable
    A Kotlin multiplatform library that provides and simple/uniform Bluetooth low energy API
    
### Code
    package com.juul.sensortag.features.scan <-- change name of package here to match 

    import android.app.Application
    import androidx.lifecycle.AndroidViewModel
    import androidx.lifecycle.viewModelScope
    import com.juul.kable.Advertisement
    import com.juul.kable.Scanner
    import com.juul.sensortag.features.scan.ScanStatus.Failed  <-- probably don't need
    import com.juul.sensortag.features.scan.ScanStatus.Started
    import com.juul.sensortag.features.scan.ScanStatus.Stopped
    import kotlinx.coroutines.CancellationException
    import kotlinx.coroutines.CoroutineScope
    import kotlinx.coroutines.Job
    import kotlinx.coroutines.cancelChildren
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.asStateFlow
    import kotlinx.coroutines.flow.catch
    import kotlinx.coroutines.flow.collect
    import kotlinx.coroutines.flow.filter
    import kotlinx.coroutines.flow.onCompletion
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.withTimeoutOrNull
    import java.util.concurrent.TimeUnit

    private val SCAN_DURATION_MILLIS = TimeUnit.SECONDS.toMillis(10)

    sealed class ScanStatus {
        object Stopped : ScanStatus()
        object Started : ScanStatus()
        data class Failed(val message: CharSequence) : ScanStatus()
    }

    class ScanViewModel(application: Application) : AndroidViewModel(application) {

        private val scanner = Scanner()
        private val scanScope = viewModelScope.childScope()
        private val found = hashMapOf<String, Advertisement>()

        private val _scanStatus = MutableStateFlow<ScanStatus>(Stopped)
        val scanStatus = _scanStatus.asStateFlow()

        private val _advertisements = MutableStateFlow<List<Advertisement>>(emptyList())
        val advertisements = _advertisements.asStateFlow()

        fun startScan() {
            if (_scanStatus.value == Started) return // Scan already in progress.
            _scanStatus.value = Started

            scanScope.launch {
                withTimeoutOrNull(SCAN_DURATION_MILLIS) {
                    scanner
                        .advertisements
                        .catch { cause -> _scanStatus.value = Failed(cause.message ?: "Unknown error") }
                        .onCompletion { cause -> if (cause == null) _scanStatus.value = Stopped }
                        .filter { it.isSensorTag } <-- use the uuid of the server to connect and check correct device
                        .collect { advertisement ->
                            found[advertisement.address] = advertisement
                            _advertisements.value = found.values.toList()
                        }
                }
        }
    }

    fun stopScan() {
        scanScope.cancelChildren()
    }
    }

    private val Advertisement.isSensorTag                    <-- don't need this not using sensorTag
        get() = name?.startsWith("SensorTag") == true ||
            name?.startsWith("CC2650 SensorTag") == true

    private fun CoroutineScope.childScope() =
        CoroutineScope(coroutineContext + Job(coroutineContext[Job]))

    private fun CoroutineScope.cancelChildren(
        cause: CancellationException? = null
    ) = coroutineContext[Job]?.cancelChildren(cause)
