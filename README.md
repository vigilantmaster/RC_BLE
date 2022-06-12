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
    This is the communication medium I am using for this project.
### Central/Client:
    A device that scans for and connects to BLE peripherals in order to perform some operation. In the context of app development, this is typically an Android device.
### Peripheral/Server:
    A device that advertises its presence and is connected to by a central in order to accomplish some task. In the context of app development, this is typically a BLE device you’re working with, like a heart rate monitor.
### GATT Service:
    A collection of characteristics (data fields) that describes a feature of a device, e.g. the Device Information service can contain a characteristic representing the serial number of the device, and another characteristic representing the battery level of the device.
### GATT Characteristic:
    An entity containing meaningful data that can typically be read from or written to, e.g. the Serial Number String characteristic.
### GATT Descriptor:
    A defined attribute that describes the characteristic that it’s attached to, e.g. the Client Characteristic Configuration descriptor shows if the central is currently subscribed to a characteristic’s value change.
### Notifications:
    A means for a BLE peripheral to notify the central when a characteristic’s value changes. The central doesn’t need to acknowledge that it’s received the packet.
### Indictations:
    Same as an indication, except each data packet is acknowledged by the central. This guarantees their delivery at the cost of throughput.
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

GLOBAL ENUMERATION:
TYPE_OF_MOVEMENT
Stop : 0
Forward: 1
Backward : 2
Turn Left : 3
Turn Right : 4

FUNCTIONS:

Bool Movement(typeOfMovement)
{
    send movement character to server using BLE
}
