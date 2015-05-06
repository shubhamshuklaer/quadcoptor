#include "I2Cdev.h"
#include "MPU6050_6Axis_MotionApps20.h"
#include <EEPROM.h>
#include <Servo.h>
#include <avr/pgmspace.h>


#if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
    #include "Wire.h"
#endif
#define OUTPUT_READABLE_YAWPITCHROLL

MPU6050 mpu;

int ESC_1=4;
int ESC_2=5;
int ESC_3=6;
int ESC_4=7;
int ESC_MIN=1000;

// MPU control/status vars
bool dmp_ready = false;  // set true if DMP init was successful
uint16_t fifo_count;     // count of all bytes currently in FIFO
uint16_t packet_size;    // expected DMP packet size (default is 42 bytes)
uint8_t dev_status;      // return status after each device operation (0 = success, !0 = error)
uint8_t fifo_buffer[64]; // FIFO storage buffer
uint8_t mpu_int_status;   // holds actual interrupt status byte from MPU

Servo esc_1,esc_2,esc_3,esc_4;

volatile bool mpu_interrupt = false;     // indicates whether MPU interrupt pin has gone high
void dmp_data_ready() {
    mpu_interrupt = true;
}

//Setup functions
void mpu_init();
void xbee_init();
void esc_init();
void rc_init();
void reset_motor();

//Loop functions
void setup(){
    Serial.begin(115200);
    while (!Serial); // wait for Leonardo enumeration, others continue immediately
    mpu_init();
    xbee_init();
    esc_init();
    rc_init();
}

void loop(){
}


void mpu_init(){
    // join I2C bus (I2Cdev library doesn't do this automatically)
    #if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
        Wire.begin();
        TWBR = 24; // 400kHz I2C clock (200kHz if CPU is 8MHz)
    #elif I2CDEV_IMPLEMENTATION == I2CDEV_BUILTIN_FASTWIRE
        Fastwire::setup(400, true);
    #endif

    // initialize device
    Serial.println(F("Initializing I2C devices..."));
    mpu.initialize();

    // verify connection
    Serial.println(F("Testing device connections..."));
    Serial.println(mpu.testConnection() ? F("MPU6050 connection successful") : F("MPU6050 connection failed"));

    do{
        // load and configure the DMP
        Serial.println(F("Initializing DMP..."));
        dev_status = mpu.dmpInitialize();

        // supply your own gyro offsets here, scaled for min sensitivity
        mpu.setXGyroOffset(220);
        mpu.setYGyroOffset(76);
        mpu.setZGyroOffset(-85);
        mpu.setZAccelOffset(1788); // 1688 factory default for my test chip

        // make sure it worked (returns 0 if so)
        if (dev_status == 0) {
            // turn on the DMP, now that it's ready
            Serial.println(F("Enabling DMP..."));
            mpu.setDMPEnabled(true);

            // enable Arduino interrupt detection
            Serial.println(F("Enabling interrupt detection (Arduino external interrupt 0)..."));
            attachInterrupt(0, dmp_data_ready, RISING);
            mpu_int_status = mpu.getIntStatus();

            // set our DMP Ready flag so the main loop() function knows it's okay to use it
            Serial.println(F("DMP ready! Waiting for first interrupt..."));
            dmp_ready = true;

            // get expected DMP packet size for later comparison
            packet_size = mpu.dmpGetFIFOPacketSize();
        } else {
            // ERROR!
            // 1 = initial memory load failed
            // 2 = DMP configuration updates failed
            // (if it's going to break, usually the code will be 1)
            Serial.print(F("DMP Initialization failed (code "));
            Serial.print(dev_status);
            Serial.println(F(")"));
        }
    }while(dev_status!=0);

}

void xbee_init(){
}

void esc_init(){
  esc_1.attach(ESC_1);
  esc_2.attach(ESC_2);
  esc_3.attach(ESC_3);
  esc_4.attach(ESC_4);
  delay(100);
  reset_motor();
}

void reset_motor(){
	esc_1.writeMicroseconds(ESC_MIN);
	esc_2.writeMicroseconds(ESC_MIN);
	esc_3.writeMicroseconds(ESC_MIN);
	esc_4.writeMicroseconds(ESC_MIN);
}


void rc_init(){
}

void get_ypr(){
}

void update_pid(){
}

void update_escs(){
}

void update_rc(){
}
