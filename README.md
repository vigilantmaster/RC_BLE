# RC_BLE
Android App that uses bluetooth low energy to control an arduino rc car
It works by sending string characters that the arduino micro controller understands.
I have some more code I need to move to this project.
One file I need to move over is the fix I have for the button presses and it's behavior.
This will take me a while since this is a side project for me and it's a toy.
I do see many benefits to this project like being able to use this code to control other devices with custom procedures.


User Stories

As a user I would like to connect to the rc vehicle.
As a user I would like to see if I am connected.
As a user I would like to connect to a different vehicle.
As a user I would like to move the vehicle forward.
As a user I would like to move the vehicle backward.
As a user I would like to turn the vehicle right.
As a user I would like to turn the vehicle left.
As a user I would like to stop the vehicle.
As a user I would like to turn the vehicle lights on and off.


Connecting to the rc vehicle

Connection will be done over BLE using a kotlin built app on android.



Definitions and use cases
Term & Definition
BLE:
    Bluetooth Low Energy, a subset of the 2.4 GHz Bluetooth wireless technology that specializes in low power and oftentimes infrequent data transmissions for connected devices.
    This is the communication medium I am using for this project.
Central/Client:
    A device that scans for and connects to BLE peripherals in order to perform some operation. In the context of app development, this is typically an Android device.
Peripheral/Server:
    A device that advertises its presence and is connected to by a central in order to accomplish some task. In the context of app development, this is typically a BLE device you’re working with, like a heart rate monitor.
GATT Service:
    A collection of characteristics (data fields) that describes a feature of a device, e.g. the Device Information service can contain a characteristic representing the serial number of the device, and another characteristic representing the battery level of the device.
GATT Characteristic:
    An entity containing meaningful data that can typically be read from or written to, e.g. the Serial Number String characteristic.
GATT Descriptor:
    A defined attribute that describes the characteristic that it’s attached to, e.g. the Client Characteristic Configuration descriptor shows if the central is currently subscribed to a characteristic’s value change.
Notifications:
    A means for a BLE peripheral to notify the central when a characteristic’s value changes. The central doesn’t need to acknowledge that it’s received the packet.
Indictations:
    Same as an indication, except each data packet is acknowledged by the central. This guarantees their delivery at the cost of throughput.
UUID:
    Universally unique identifier, 128-bit number used to identify services, characteristics and descriptors.


Connection: I plan to connect to the BLE using Coroutines and KABLE by Juullabs.
1. Launch coroutine to start scanning
   ScannerFlow - provides stream of advertisements
2. Stop Scanning and Connect 
   Filter for the one we want and connect
   or maybe display them all and let the user decide.
3. MutableStateFlow - Data Binding <- update the UI

Connection Types:

1. With Confirmation - might be slow but would allow for a stable connection that will know if the signal did not get received.
   1. When button is pressed down the signal is sent and a confirmation is received. If the confirmation is not received another signal is sent. 
   2. When the button is released a signal to stop is sent with a confirmation and sent again if there is not confirmation.
      This could have an issue if the stop signal is being sent and the user presses another button.  
      The stop signal process should be halted upon another press.

2. Without Confirmation - send signal and not expect a response.  
   This way we assume the signal will make it and don't care if it doesn't.  Could work if a constant signal is sent.
    A.
       1. When button is pressed down the signal is constantly sent and the device receives the signal constantly. - could be time limited
       2. When the button is released constantly send stop signal. - maybe limit the time this happens
    B.  Useful if the device is behaving slow with previous method but not reliable if there is interference or signal loss.
       1. When button is pressed down send signal once and assume it was received.
       2. When button is released send signal to stop and assume it was received.

USER INTERFACE:
    Needs to have buttons or a virtual joystick to control.  
    Methods are dependent on connection type (see connection types for methodology)

References used

https://punchthrough.com/android-ble-guide/
https://zoewave.medium.com/kotlin-beautiful-low-energy-ble-91db3c0ab887