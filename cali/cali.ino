#include <Servo.h>

#define MAX_SIGNAL 1900
#define MIN_SIGNAL 1000

Servo m1,m2,m3,m4;

void setup() {
  Serial.begin(115200);
  Serial.println("Program begin...");
  Serial.println("This program will calibrate the ESC.");

  m1.attach(4);
  m2.attach(5);
  m3.attach(6);
  m4.attach(7);

  Serial.println("Now writing maximum output.");
  Serial.println("Turn on power source, then wait 2 seconds and press any key.");
  m1.writeMicroseconds(MAX_SIGNAL);
  m2.writeMicroseconds(MAX_SIGNAL);
  m3.writeMicroseconds(MAX_SIGNAL);
  m4.writeMicroseconds(MAX_SIGNAL);

  // Wait for input
  while (!Serial.available());
  Serial.read();

  // Send min output
  Serial.println("Sending minimum output");
  m1.writeMicroseconds(MIN_SIGNAL);
  m2.writeMicroseconds(MIN_SIGNAL);
  m3.writeMicroseconds(MIN_SIGNAL);
  m4.writeMicroseconds(MIN_SIGNAL);

}

void loop() {  

}
