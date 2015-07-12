arduino quad_summer.ino --upload && picocom /dev/ttyUSB0 -b 9600 -r -l

**********************************************using bootloader optiboot because of bootloader problem
hg clone https://code.google.com/p/optiboot/
if problem with hg export it to github and clone from there
Now place the folder inside bootloaders folder of clone inside arduino-1.6.1/hardware/arduino/avr/bootloaders
We need to make some changes inside the boards.txt of clone
Just before "{name}.bootloader.low_fuses=Some_hex" put "{name}.bootloader.tool=avrdude".
also befoe {name}.upload.protocol=arduino(or something else) put {name}.upload.tool=avrdude
also in {name}.bootloader.file attact "optiboot/" before the file name if not already present

**********************************String library has memory leaks
use char array instead

*****************************
http://www.megunolink.com/how-to-detect-lockups-using-the-arduino-watchdog/
using library from https://github.com/Megunolink/ArduinoCrashMonitor
just put the ApplicationMonitor.h and ApplicationMonitor.cpp files into a folder named ApplicationMonitor
and put that into the library folder


***********************************
THE default twi library is problamatic there are while loops which can run in infinite loop
if there is a problem with the communication
so uning a edited library from (Actualy the library there did not work cause the wire library works with the original twi library so I adapted the original twi library according to the post)
http://forum.arduino.cc/index.php/topic,19624.0.html
http://liken.otsoa.net/pub/ntwi/twi.c
http://liken.otsoa.net/pub/ntwi/twi.c
this adds time out to each while loop
on top of that I am changing twi_init to accept a bool paramater telling should you
enable pullup register too... changing the wire library too to reflect this change in twi_init1

**********************************Dissassembly
avr-objdump -d -S -j .text quad_summer.cpp.elf > Disassembly.txt


**************************************MPU9150
https://github.com/jrowberg/i2cdevlib/tree/master/Arduino/MPU9150
replace prog_uchar with unsigned char in MPU9150_9.....41.h

**************************************IP camera
All features works only in IE7 or IE8...!! In other browsers recording won't work..!!
and nothing works in IE8+
