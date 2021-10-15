#include <CurieBLE.h>

/*-----( Declare objects )-----*/


const int speedPin1 = 6;   //connect to ENA pin green
const int fwdPin1 = 7;   //connect to logic pin 7 in1 blue
const int revPin1 = 8;   //connect to logic pin 8 in2 yellow
const int speedPin2 = 5;  //connect to second enB pin red
const int fwdPin2 = 3;  // connect to logic pin 3 in3 black
const int revPin2 = 4;  // connect to logic pin 4 in4 purple
float motor1Speed = 0;  //keep track of motor 1 speed okay for low level programming
float motor2Speed = 0;  //keep track of motor 2 speed okay for low level programming
const int ledPin = 13; // set ledPin to use
bool motorStopped = false;

BLEService RC_CAR_RECIEVER("19B10000-E8F2-537E-4F6C-D104768A1214"); // create bluetooth service

// create switch characteristic and allow remote device to read and write
BLECharCharacteristic switchChar("19B10001-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite);

void setup() {

  ////////////////////////
  // initialize outputs //
  ////////////////////////
  
  Serial.begin(9600);
  pinMode(LED_BUILTIN, OUTPUT);
  pinMode(ledPin, OUTPUT); // use the LED on pin 13 as an output
  //set all the pins as outputs
  // motor 1
  pinMode(speedPin1, OUTPUT);
  pinMode(fwdPin1, OUTPUT);
  pinMode(revPin1, OUTPUT);
  analogWrite(speedPin1, 0);
  analogWrite(speedPin2, 0);
  // motor 2
  pinMode(speedPin2, OUTPUT);
  pinMode(fwdPin2, OUTPUT);
  pinMode(revPin2, OUTPUT);
  // pinMode(LED_PIN , OUTPUT);
  digitalWrite(fwdPin2, HIGH);
  digitalWrite(fwdPin1, HIGH);
  digitalWrite(revPin1, LOW);
  digitalWrite(revPin2, LOW);

  ////////////////////////
  // BLE  initialization//
  ////////////////////////
  BLE.begin();

  // set the local name peripheral advertises
  BLE.setLocalName("TanelsRemoteCar");
  // set the UUID for the service this peripheral advertises
  BLE.setAdvertisedService(RC_CAR_RECIEVER);

  // add the characteristic to the service
  RC_CAR_RECIEVER.addCharacteristic(switchChar);

  // add service
  BLE.addService(RC_CAR_RECIEVER);

  // assign event handlers for connected, disconnected to peripheral
  BLE.setEventHandler(BLEConnected, blePeripheralConnectHandler);
  BLE.setEventHandler(BLEDisconnected, blePeripheralDisconnectHandler);

  // assign event handlers for characteristic
  switchChar.setEventHandler(BLEWritten, switchCharacteristicWritten);
  // set an initial value for the characteristic
  switchChar.setValue('S');

  // start advertising
  BLE.advertise();

  Serial.println(("Bluetooth device active, waiting for connections..."));
}

//set motor1 to go forward
void motor1FWD()
{
  digitalWrite(fwdPin1, HIGH);
  digitalWrite(revPin1, LOW);
}

//set motor2 to go forward
void motor2FWD()
{
  digitalWrite(fwdPin2, HIGH);
  digitalWrite(revPin2, LOW);
}

//set motor1 to go in reverse
void motor1REV()
{
  digitalWrite(fwdPin1, LOW);
  digitalWrite(revPin1, HIGH);
}

//set motor1 to go in reverse
void motor2REV()
{
  digitalWrite(fwdPin2, LOW);
  digitalWrite(revPin2, HIGH);
}

//stop the motor
void motorStop()
{
  analogWrite(speedPin1, 0);
  analogWrite(speedPin2, 0);
  if (motorStopped == false)
  {
    motorStopped = true;
    digitalWrite(LED_BUILTIN, LOW);
    //   motorHalted = 700;
     //Serial.println("Motor Stopped");
  }
  motor1Speed = 0;
  motor2Speed = 0;
}

//if motor stop is activated and motor is halted this will start
void motorStart()
{
  motorSetSpeed1(255);
  motorSetSpeed2(255);
  motorStopped = false;
  //  motorHalted = 600;
  digitalWrite(LED_BUILTIN, HIGH);
  //Serial.println("Motor Reset");
}

//set the motor speed
void motorSetSpeed1(long speed)
{
  analogWrite(speedPin1, speed);
}
void motorSetSpeed2(long speed)
{
  analogWrite(speedPin2, speed);
}

//slow the motor
void motorSlow(float speedReduction)
{
  if (motor1Speed > 0 && motor2Speed > 0)
  {
    motor1Speed = motor1Speed - speedReduction;
    motor2Speed = motor2Speed - speedReduction;
    Serial.println("reduced speed");
  }
}

///////////////
// MAIN LOOP //
///////////////

void loop() {
  // poll for BLE events
  BLE.poll();
}


//////////////////////////////////
// Bluetooth Connection Handlers//
//////////////////////////////////

void blePeripheralConnectHandler(BLEDevice central) {
  // central connected event handler
  Serial.print("Connected event, central: ");
  Serial.println(central.address());
}

void blePeripheralDisconnectHandler(BLEDevice central) {
  // central disconnected event handler
  Serial.print("Disconnected event, central: ");
  Serial.println(central.address());
}

void switchCharacteristicWritten(BLEDevice central, BLECharacteristic characteristic) {
  // central wrote new value to characteristic, update LED
  //Serial.print("Characteristic event, written: ");

  if (switchChar.value() == 'L') { // turn left
    motorStart();
    Serial.print(" L\n");
    motor1REV();
    motor2FWD();
  }
  if (switchChar.value() == 'R') {  // turn right
    motorStart();
    Serial.print(" R\n");
    motor2REV();
    motor1FWD();
  }
  if (switchChar.value() == 'V') { // reverse
    motorStart();
    Serial.print(" V\n");
    motor1REV();
    motor2REV();
  }
  if (switchChar.value() == 'F') { // forward
    motorStart();
    Serial.print(" F\n");
    motor1FWD();
    motor2FWD();
  }
  if (switchChar.value() == 'S') { // stop
    Serial.print(" S\n");
    motorStop();
  }
}

/*
  Copyright (c) 2016 Intel Corporation. All rights reserved.

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-
  1301 USA
*/
