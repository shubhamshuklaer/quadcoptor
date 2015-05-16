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

**********************************************using bootloader optiboot because of bootloader problem
hg clone https://code.google.com/p/optiboot/
if problem with hg export it to github and clone from there
Now place the folder inside bootloaders folder of clone inside arduino-1.6.1/hardware/arduino/avr/bootloaders
We need to make some changes inside the boards.txt of clone
Just before "{name}.bootloader.low_fuses=Some_hex" put "{name}.bootloader.tool=avrdude".
also befoe {name}.upload.protocol=arduino(or something else) put {name}.upload.tool=avrdude
also in {name}.bootloader.file attact "optiboot/" before the file name if not already present


*********************************************diylc circuit designer
sudo sh -c 'echo "deb http://www.diy-fever.com/diylc_repository binary/">/etc/apt/sources.list.d/diylc.list'
sudo apt-get install diylc

****************************************PID_v1 library
in files PID_v1.h and PID_v1.cpp replace all double with int
except for the ratio variable in SetSampleTime

**********************************String library has memory leaks
use char array instead
