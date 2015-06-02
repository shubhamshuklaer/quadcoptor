#include <Servo.h>

#define MAX_SIGNAL 1900
#define MIN_SIGNAL 1000

Servo m1,m2,m3,m4;

void setup() {
    Serial.begin(115200);

    m1.attach(4);
    m2.attach(5);
    m3.attach(6);
    m4.attach(7);

    Serial.println(F("Press 'h' for high throttle and 'l' for low throttle"));
}

char ch;

void loop() {

    while (!Serial.available());
    ch=Serial.read();

    if(ch=='h'){
        m1.writeMicroseconds(MAX_SIGNAL);
        m2.writeMicroseconds(MAX_SIGNAL);
        m3.writeMicroseconds(MAX_SIGNAL);
        m4.writeMicroseconds(MAX_SIGNAL);
        Serial.println(F("Max"));
    }else if(ch=='l'){
        m1.writeMicroseconds(MIN_SIGNAL);
        m2.writeMicroseconds(MIN_SIGNAL);
        m3.writeMicroseconds(MIN_SIGNAL);
        m4.writeMicroseconds(MIN_SIGNAL);
        Serial.println(F("Min"));
    }

}
