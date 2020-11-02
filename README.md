# nano-rf24-transmit-receive
A project using Arduino Nano and RF24 to implement RF communication

# Wireless Heart Rate Monitoring System
The project consist of pulse sensor connected to arduino board, to calculate the heart rate of a person. It is meant to be wireless heart rate monitoring system, where a person who holds a device can measure his/her heart rate by touch the sensor with a finger, then the device starts sending the signal via RF communication. A single device that acts as a receiver is connected to a PC and then the PC display the heart rate signal of that person via GUI (the program written in Java).

## Illustration of the overall system:
![alt text](https://raw.githubusercontent.com/diannatarahman/nano-rf24-transmit-receive/master/images/Image%201.png)

A single measurement device consists of:
•	Microcontroller Arduino Nano
•	Pulse sensor
•	nRF24L01 transceiver module
•	Battery and switch

The receiver device consists of:
•	Microcontroller Arduino Nano (connected to PC via serial communication)
•	nRF24L01 transceiver module

## A measurement device testing:
![alt text](https://raw.githubusercontent.com/diannatarahman/nano-rf24-transmit-receive/master/images/Image%202.jpg)

## The GUI displays the heart rate signal:
![alt text](https://raw.githubusercontent.com/diannatarahman/nano-rf24-transmit-receive/master/images/Image%203.jpg)
