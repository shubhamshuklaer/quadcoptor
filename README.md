In file arduino-1.6.1/hardware/arduino/avr/libraries/Wire/utility/twi.h changed 
#define TWI_FREQ 490000L


in file
arduino-1.6.1/hardware/arduino/avr/cores/arduino/HardwareSerial.h changed
#if (RAMEND < 1000)
#define SERIAL_TX_BUFFER_SIZE 16
#define SERIAL_RX_BUFFER_SIZE 16
#else
#define SERIAL_TX_BUFFER_SIZE 128
#define SERIAL_RX_BUFFER_SIZE 128
#endif



arduino quad_final.ino --upload && picocom /dev/ttyUSB0 -b 115200 -r -l 
