In file arduino-1.6.1/hardware/arduino/avr/libraries/Wire/utility/twi.h changed 
#define TWI_FREQ 			400000L

In file arduino-1.6.1/libraries/I2Cdev/I2Cdev.h
#define TWI_FREQ            400000L


in file arduino-1.6.1/hardware/arduino/avr/cores/arduino/HardwareSerial.h changed
#if (RAMEND < 1000)
#define SERIAL_TX_BUFFER_SIZE 16
#define SERIAL_RX_BUFFER_SIZE 16
#else
#define SERIAL_TX_BUFFER_SIZE 128
#define SERIAL_RX_BUFFER_SIZE 128
#endif


************* NEVER DO THIS *************************************
in file arduino-1.6.1/libraries/MPU6050/ma20.h
#261 -->    0x02,   0x16,   0x02,   0x00, 0x00                // D_0_22 inv_set_fifo_rate
*****************************************************************


arduino quad_final.ino --upload && picocom /dev/ttyUSB0 -b 115200 -r -l 


****************************Not Working****************************
Using the bootloader stk500v2 instead of atmega because of bootloader problem
go to arduino-1.6.1/hardware/arduino/avr/boards.txt and search for Arduino Mega 
now for mega1280 change the bootloader file to correct one
We will have to build the bootloader
sudo apt-get install gcc-avr avr-libc
go into stk500v2 folder and uncomment line 38 saying MCU = atmega128
then do make clean and make


*********************************************diylc circuit designer
sudo sh -c 'echo "deb http://www.diy-fever.com/diylc_repository binary/">/etc/apt/sources.list.d/diylc.list'
sudo apt-get install diylc
