#include <ApplicationMonitor.h>
#include <EEPROM.h>
#include <EnableInterrupt.h>
#include <I2Cdev.h>
#include <MPU6050_6Axis_MotionApps20.h>
#include <PID_v1.h>
#include <Servo.h>
#include <avr/pgmspace.h>
#include <MemoryFree.h>


#if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
    #include <Wire.h>
#endif
#define OUTPUT_READABLE_YAWPITCHROLL

Watchdog::CApplicationMonitor ApplicationMonitor;

Servo m1, m2, m3, m4;

int base_speed = 1330, max_speed = 1900, min_speed = 1000;
int base_speed_old=base_speed;

int m1_speed, m2_speed, m3_speed, m4_speed;

int m1_speed_off = 0, m2_speed_off = 0, m3_speed_off = 0, m4_speed_off = 0;

unsigned long timer = 0, timer2 = 0;

int gyro_ypr[3];
int gyro_ypr_temp[3];
int speed_ypr_raw[3];
int speed_ypr[3];
int desired_ypr[3]={0,0,0};

float serial_ratio = 0, serial_enter, serial_leave, serial_count = 0;
float loop_ratio = 0, loop_enter, loop_leave, loop_count = 0;
float pid_ratio = 0, pid_enter, pid_leave, pid_count = 0;
float motor_ratio = 0, motor_enter, motor_leave, motor_count = 0;
float mpu_ratio = 0, mpu_enter, mpu_leave, mpu_count = 0;
float interpolate_ratio = 0, interpolate_enter, interpolate_leave, interpolate_count = 0;

float prev_ypr_deg[3], prev_ypr[3], sum_ypr_deg[3], sum_ypr[3];
int sum_ypr_int[3], prev_ypr_int[3];

float offset_pitch = 0.00, offset_roll = 0.00, offset_yaw = 0.00;

int kp[3] = {4, 24, 24}, kd[3] = {0, 1, 1}, ki[3] = {0, 0, 0};

int gyro_kp[3] = {1, 1, 1}, gyro_kd[3] = {0, 0, 0}, gyro_ki[3] = {0, 0, 0};
/* int kp[3] = {17190, 16044, 17190}, kd[3] = {1146000, 1146000, 1146000}, ki[3] = {286, 286, 286}; */
/* int gyro_kp[3] = {1000, 1000, 1000}, gyro_kd[3] = {0, 0, 0}, gyro_ki[3] = {0, 0, 0}; */

String in_str, in_key, in_value, in_index, serial_send = "";

bool in_str_arr = false, show_speed = false, show_ypr = false, enable_motors = false,
	enable_pitch = false, enable_yaw = false, enable_roll = false, show_diff = false;

int count_auto_calc=0, count_dmp_calc=0, count_motor = 0, count_mpu = 0, count_ypr = 0, count_serial = 0, count_iter = 0, count_check_overflow = 0;

MPU6050 mpu;
MPU6050 accelgyro;

int ch1=0,ch2=0,ch3=0,ch4=0,ch5=0,ch6=0;
int ch3_old=0;
volatile int count_ch5=0;
volatile int ch1_val=0,ch2_val=0,ch3_val=0,ch4_val=0,ch5_val=0,ch6_val=0;
volatile int ch1_prev=0,ch2_prev=0,ch3_prev=0,ch4_prev=0,ch5_prev=0,ch6_prev=0;
bool ch_changed=false;

const int CH1_PIN=13;
const int CH2_PIN=12;
const int CH3_PIN=11;
const int CH4_PIN=10;
const int CH5_PIN=50;
const int CH6_PIN=52;
const int DMP_INT_PIN=2;

const int CH1_MAX=1808;
const int CH1_MIN=1208;
const int CH2_MAX=1820;
const int CH2_MIN=1216;
const int CH3_MAX=2020;
const int CH3_MIN=1016;
const int CH4_MAX=1808;
const int CH4_MIN=1208;
const int CH5_MAX=2000;
const int CH5_MIN=1000;
const int CH6_MAX=2000;
const int CH6_MIN=1000;

const int CH1_EFFECT=20;
const int CH2_EFFECT=100;
const int CH3_MIN_EFFECT=1400;
const int CH3_MAX_EFFECT=1600;
const int CH4_EFFECT=100;
const int CH5_EFFECT=100;
const int CH6_EFFECT=100;

int take_off_count=0;
int take_down_count=0;
unsigned long take_down_start=0;
const int take_down_cutoff=1450;
const int take_off_gradient=100;
const int take_down_gradient=14;
const int take_down_diff=50;

const int MAX_R_PID_EFFECT=800;
const int MAX_yaw_R_PID_EFFECT=120;
const int MAX_S_PID_EFFECT=1000;
const int MAX_yaw_S_PID_EFFECT=120;
const int MAX_S_PID_I_EFFECT=40;
const int MAX_yaw_S_PID_I_EFFECT=20;
const int MAX_R_PID_I_EFFECT=40;
const int MAX_yaw_R_PID_I_EFFECT=20;


const int RAW_RATIO=16;
const int PID_SAMPLE_TIME=1000;

byte sregRestore;

const int YAW_AVERAGE_COUNT=40;
const unsigned long PROPER_YAW_TIME=20000000UL;
const float YAW_AVERAGE_RETAIN=0.3;

/* #define LED_PIN 13// (Arduino is 13, Teensy is 11, Teensy++ is 6) */
const int GYRO_RATIO=1;
const int YPR_RATIO=128;
const int AUTO_CYCLES=5;
const int MAX_BUFFER=5;
const float Q_RETAIN=0.5;

bool blinkState = false;

// MPU control/status vars
bool dmp_ready = false;  // set true if DMP init was successful
uint8_t mpu_int_status;   // holds actual interrupt status byte from MPU
uint8_t dev_status;      // return status after each device operation (0 = success, !0 = error)
uint16_t packet_size;    // expected DMP packet size (default is 42 bytes)
uint16_t fifo_count;     // count of all bytes currently in FIFO
uint8_t fifo_buffer[64]; // FIFO storage buffer

// orientation/motion vars
Quaternion q, q_past, q_res;           // [w, x, y, z]         quaternion container

VectorInt16 aa;         // [x, y, z]            accel sensor measurements
VectorInt16 aa_real;     // [x, y, z]            gravity-free accel sensor measurements
VectorInt16 aaW_world;    // [x, y, z]            world-frame accel sensor measurements
VectorFloat gravity;    // [x, y, z]            gravity vector

int rate_ypr[3];
float gyroPitch, gyroRoll;
int32_t prev_gyro_diff[3], sum_gyro[3];
float euler[3];										// [psi, theta, phi]    Euler angle container
float ypr[3], ypr_deg[3], ypr_past[3];				// [yaw, pitch, roll]   yaw/pitch/roll container and gravity vector
int ypr_int_offset[3]={0,-5,0};
int ypr_int[3];
int ypr_int_bound[3];
int gyro_int_bound[3];
int gyro_int[3];
int gyro_int_offset[3]={-21,-8,8};
int gyro_int_raw[3];
float gyro_retain[3]={0.3,0.3,0.3};
int16_t gx, gy, gz;

// packet structure for InvenSense teapot demo
// uint8_t teapotPacket[14] = { '$', 0x02, 0, 0, 0, 0, 0, 0, 0, 0, 0x00, 0x00, '\r', '\n' };

void calc_pid(void);
void motor_control(void);
void get_data(void);
Quaternion mult(Quaternion q, float f);
Quaternion add(Quaternion q, Quaternion s);
Quaternion diff(Quaternion q, Quaternion s);



int ESC_1=4;
int ESC_2=5;
int ESC_3=6;
int ESC_4=7;
int ESC_MIN=1000;

volatile bool mpu_interrupt = false;     // indicates whether MPU interrupt pin has gone high
void dmp_data_ready() {
    mpu_interrupt = true;
}

//Setup functions
void mpu_init();
void xbee_init();
void esc_init();
void rc_init();
void pid_init();
void reset_motor();

//Loop functions
void update_rc();
void update_ypr();
void check_serial();

void ch1_change();
void ch2_change();
void ch3_change();
void ch4_change();
void ch5_change();
void ch6_change();



//Stablize pid
PID s_yaw_pid(&ypr_int[0],&gyro_ypr[0],&desired_ypr[0],kp[0],ki[0],kd[0],REVERSE);
PID s_pitch_pid(&ypr_int[1],&gyro_ypr[1],&desired_ypr[1],kp[1],ki[1],kd[1],REVERSE);
PID s_roll_pid(&ypr_int[2],&gyro_ypr[2],&desired_ypr[2],kp[2],ki[2],kd[2],REVERSE);

//Rate pid
PID r_yaw_pid(&gyro_int[0],&speed_ypr_raw[0],&gyro_ypr_temp[0],gyro_kp[0],gyro_ki[0],gyro_kd[0],REVERSE);
PID r_pitch_pid(&gyro_int[1],&speed_ypr_raw[1],&gyro_ypr_temp[1],gyro_kp[1],gyro_ki[1],gyro_kd[1],REVERSE);
PID r_roll_pid(&gyro_int[2],&speed_ypr_raw[2],&gyro_ypr_temp[2],gyro_kp[2],gyro_ki[2],gyro_kd[2],REVERSE);

//
// Don't edit the output var of the PID it causes problem
//


void setup(){
    ApplicationMonitor.DisableWatchdog();
    Serial.begin(115200);
    while (!Serial); // wait for Leonardo enumeration, others continue immediately
    mpu_init();
    /* xbee_init(); */
    esc_init();
    rc_init();
    pid_init();

	/* mpu_enter = mpu_leave = micros(); */
	/* serial_enter = serial_leave = micros(); */
	/* loop_enter = loop_leave = micros(); */
	/* pid_enter = pid_leave = micros(); */
	/* interpolate_enter = interpolate_leave = micros(); */
    enable_pitch=false;
    enable_roll=true;
    /* ApplicationMonitor.Dump(Serial); */
    ApplicationMonitor.EnableWatchdog(Watchdog::CApplicationMonitor::Timeout_1s);
}



void loop(){

    // earlier it was 0x40 but for processing graph I increased it
    // 0x40 is hex of 64 decimal i.e 01000000 and hence the bitwise and works
    if(count_serial & 0x100){
        check_serial();
        count_serial = 0;
        /* Serial.print(ypr_int[1]); */
        /* Serial.print(" "); */
        /* Serial.print(gyro_int[1]); */
        /* Serial.print(" "); */
        /* Serial.print(ypr_int[2]); */
        /* Serial.print(" "); */
        /* Serial.print(gyro_int[2]); */
        /* Serial.print(" "); */
        /* Serial.print(ypr_int[0]); */
        /* Serial.print(" "); */
        /* Serial.print(gyro_int[0]); */
        /* Serial.println(" "); */

        /* Serial.print(gyro_ypr[1]); */
        /* Serial.print(" "); */
        /* Serial.print(gyro_int[1]); */
        /* Serial.print(" "); */
        /* Serial.print(gyro_ypr[2]); */
        /* Serial.print(" "); */
        /* Serial.print(gyro_int[2]); */
        /* Serial.print(" "); */
        /* Serial.print(gyro_ypr[0]); */
        /* Serial.print(" "); */
        /* Serial.print(gyro_int[0]); */
        /* Serial.println(" "); */

        /* Serial.print(speed_ypr[0]); */
        /* Serial.print(" "); */
        /* Serial.print(speed_ypr[1]); */
        /* Serial.print(" "); */
        /* Serial.print(speed_ypr[2]); */
        /* Serial.println(" "); */

        /* Serial.print(m1_speed); */
        /* Serial.print(" "); */
        /* Serial.print(m2_speed); */
        /* Serial.print(" "); */
        /* Serial.print(base_speed); */
        /* Serial.print(m3_speed); */
        /* Serial.print(" "); */
        /* Serial.print(m4_speed); */
        /* Serial.println(" "); */

    }else{
        count_serial=count_serial+1;
    }

    update_rc();
    update_ypr();
    calc_pid();
    motor_control();
    ApplicationMonitor.IAmAlive();

    /* Serial.print(gyro_ypr[0]); */
    /* Serial.print("\t"); */
    /* Serial.print(gyro_ypr[1]); */
    /* Serial.print("\t"); */
    /* Serial.println(gyro_ypr[2]); */
    /* Serial.println(i2c_reset_count); */
    
    /* Serial.print(gx); */
    /* Serial.print(" "); */
    /* Serial.print(gy); */
    /* Serial.print(" "); */
    /* Serial.print(gz); */
    /* Serial.print('\r'); */


}

void pid_init(){
    s_yaw_pid.SetMode(AUTOMATIC);
    s_pitch_pid.SetMode(AUTOMATIC);
    s_roll_pid.SetMode(AUTOMATIC);


    r_yaw_pid.SetMode(AUTOMATIC);
    r_pitch_pid.SetMode(AUTOMATIC);
    r_roll_pid.SetMode(AUTOMATIC);


    s_yaw_pid.SetSampleTime(PID_SAMPLE_TIME);
    s_pitch_pid.SetSampleTime(PID_SAMPLE_TIME);
    s_roll_pid.SetSampleTime(PID_SAMPLE_TIME);

    s_yaw_pid.SetOutputLimits(-MAX_yaw_S_PID_EFFECT,MAX_yaw_S_PID_EFFECT);
    s_pitch_pid.SetOutputLimits(-MAX_S_PID_EFFECT,MAX_S_PID_EFFECT);
    s_roll_pid.SetOutputLimits(-MAX_S_PID_EFFECT,MAX_S_PID_EFFECT);

    s_yaw_pid.SetILimits(-MAX_yaw_S_PID_I_EFFECT,MAX_yaw_S_PID_I_EFFECT);
    s_pitch_pid.SetILimits(-MAX_S_PID_I_EFFECT,MAX_S_PID_I_EFFECT);
    s_roll_pid.SetILimits(-MAX_S_PID_I_EFFECT,MAX_S_PID_I_EFFECT);

    r_yaw_pid.SetSampleTime(PID_SAMPLE_TIME);
    r_pitch_pid.SetSampleTime(PID_SAMPLE_TIME);
    r_roll_pid.SetSampleTime(PID_SAMPLE_TIME);

    r_yaw_pid.SetOutputLimits(-MAX_yaw_R_PID_EFFECT,MAX_yaw_R_PID_EFFECT);
    r_pitch_pid.SetOutputLimits(-MAX_R_PID_EFFECT,MAX_R_PID_EFFECT);
    r_roll_pid.SetOutputLimits(-MAX_R_PID_EFFECT,MAX_R_PID_EFFECT);

    r_yaw_pid.SetILimits(-MAX_yaw_R_PID_I_EFFECT,MAX_yaw_R_PID_I_EFFECT);
    r_pitch_pid.SetILimits(-MAX_R_PID_I_EFFECT,MAX_R_PID_I_EFFECT);
    r_roll_pid.SetILimits(-MAX_R_PID_I_EFFECT,MAX_R_PID_I_EFFECT);

    /* unsigned long yaw_tune_start=micros(); */
    /* while(micros()-yaw_tune_start<PROPER_YAW_TIME) */
        /* update_ypr(); */
    
    /* int desired_yaw; */
    /* int yaw_average_count_copy=YAW_AVERAGE_COUNT; */
    /* while(yaw_average_count_copy>0){ */
        /* update_ypr(); */
        /* desired_yaw=desired_yaw*(1-YAW_AVERAGE_RETAIN)+YAW_AVERAGE_RETAIN*ypr_int[0]; */
        /* yaw_average_count_copy--; */
    /* } */

    /* desired_ypr[0]=desired_yaw; */
}


void ch1_change(){
    if(digitalRead(CH1_PIN) == HIGH){
        ch1_prev = micros();
    }else{
        ch1_val=micros()-ch1_prev;
        ch_changed=true;
    }
}
void ch2_change(){
    if(digitalRead(CH2_PIN) == HIGH){ 
        ch2_prev = micros();
    }else{
        ch2_val=micros()-ch2_prev;
        ch_changed=true;
    }
}
void ch3_change(){
    if(digitalRead(CH3_PIN) == HIGH){ 
        ch3_prev = micros();
    }else{
        ch3_val=micros()-ch3_prev;
        ch_changed=true;
    }
}
void ch4_change(){
    if(digitalRead(CH4_PIN) == HIGH){ 
        ch4_prev = micros();
    }else{
        ch4_val=micros()-ch4_prev;
        ch_changed=true;
    }
}
void ch5_change(){
    if(digitalRead(CH5_PIN) == HIGH){ 
        ch5_prev = micros();
    }else{
        ch5_val=micros()-ch5_prev;
        ch_changed=true;
    }
}
void ch6_change(){
    if(digitalRead(CH6_PIN) == HIGH){ 
        ch6_prev = micros();
    }else{
        ch6_val=micros()-ch6_prev;
        ch_changed=true;
    }
}

void mpu_init(){
    // join I2C bus (I2Cdev library doesn't do this automatically)
    #if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
        Wire.begin(true);
        TWBR = 24; // 400kHz I2C clock (200kHz if CPU is 8MHz)
    #elif I2CDEV_IMPLEMENTATION == I2CDEV_BUILTIN_FASTWIRE
        Fastwire::setup(400, true);
    #endif


    // initialize device
    /* Serial.println(F("Initializing I2C devices...")); */
	/* accelgyro.initialize(); */
	/* accelgyro.setI2CBypassEnabled(true); */
    mpu.initialize();
    mpu.setRate(0);
    mpu.setFullScaleGyroRange(250);

    // verify connection
    /* Serial.println(F("Testing device connections...")); */
    if(!mpu.testConnection())
        Serial.println(F("MPU connection failed"));

    // load and configure the DMP
    /* Serial.println(F("Initializing DMP...")); */
    dev_status = mpu.dmpInitialize();

    // supply your own gyro offsets here, scaled for min sensitivity

    /* mpu.setXGyroOffset(15); */
    /* mpu.setYGyroOffset(-15); */
    /* mpu.setZGyroOffset(-20); */

    /* mpu.setXGyroOffset(220); */
    /* mpu.setYGyroOffset(76); */
    /* mpu.setZGyroOffset(-85); */
    /* mpu.setZAccelOffset(1788); // 1688 factory default for my test chip */

    // make sure it worked (returns 0 if so)
    if (dev_status == 0) {
        // turn on the DMP, now that it's ready
        /* Serial.println(F("Enabling DMP...")); */
        mpu.setDMPEnabled(true);

        // enable Arduino interrupt detection
        /* Serial.println(F("Enabling interrupt detection (Arduino external interrupt 0)...")); */
        enableInterrupt(DMP_INT_PIN, dmp_data_ready, RISING);
        mpu_int_status = mpu.getIntStatus();

        // set our DMP Ready flag so the main loop() function knows it's okay to use it
        /* Serial.println(F("DMP ready! Waiting for first interrupt...")); */
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
		asm volatile ("  jmp 0");
    }

}

void xbee_init(){
}

void esc_init(){
  m1.attach(ESC_1);
  m2.attach(ESC_2);
  m3.attach(ESC_3);
  m4.attach(ESC_4);
  delay(100);
  stop_motors();
}

void stop_motors(){
	m1.writeMicroseconds(ESC_MIN);
	m2.writeMicroseconds(ESC_MIN);
	m3.writeMicroseconds(ESC_MIN);
	m4.writeMicroseconds(ESC_MIN);
}


void rc_init(){
    pinMode(CH1_PIN, INPUT);
    pinMode(CH2_PIN, INPUT);
    pinMode(CH3_PIN, INPUT);
    pinMode(CH4_PIN, INPUT);
    pinMode(CH5_PIN, INPUT);
    pinMode(CH6_PIN, INPUT);

    enableInterrupt(CH1_PIN, ch1_change,CHANGE);
    enableInterrupt(CH2_PIN, ch2_change,CHANGE);
    enableInterrupt(CH3_PIN, ch3_change,CHANGE);
    enableInterrupt(CH4_PIN, ch4_change,CHANGE);
    enableInterrupt(CH5_PIN, ch5_change,CHANGE);
    enableInterrupt(CH6_PIN, ch6_change,CHANGE);
}

void update_ypr(){

	// wait for MPU interrupt or extra packet(s) available
	// reset interrupt flag and get INT_STATUS byte
    if(mpu_interrupt){
        mpu_interrupt = false;
        mpu_int_status = mpu.getIntStatus();

        // get current FIFO count
        fifo_count = mpu.getFIFOCount();
        /* loop_leave=fifo_count; */

        // check for overflow (this should never happen unless our code is too inefficient)
        if ((mpu_int_status & 0x10) || fifo_count == 1024) {
            // reset so we can continue cleanly
            Serial.println(fifo_count);
            mpu.resetFIFO();
            Serial.println(F("FIFO overflow!"));

            // otherwise, check for DMP data ready interrupt (this should happen frequently)
        }else if (mpu_int_status & 0x02) {
            // wait for correct available data length, should be a VERY short wait

            if (fifo_count >= packet_size) {

                // fifo_count = mpu.getFIFOCount();

                q_past = q;

                // read a packet from FIFO
                mpu.getFIFOBytes(fifo_buffer, packet_size);

                // track FIFO count here in case there is > 1 packet available
                // (this lets us immediately read more without waiting for an interrupt)
                fifo_count -= packet_size;
                while(fifo_count>packet_size){
                    mpu.getFIFOBytes(fifo_buffer, packet_size);
                    fifo_count -= packet_size;
                }
                // display Euler angles in degrees
                mpu.dmpGetQuaternion(&q, fifo_buffer);
                // q = add(mult(q, q_retain), mult(q_past, (1 - q_retain)));

                mpu.dmpGetGravity(&gravity, &q);
                mpu.dmpGetYawPitchRoll(ypr, &q, &gravity);
            }
        }
    }
	
    mpu.getRotation(&gx,&gy,&gz);
    gyro_int_raw[0]=gz*GYRO_RATIO+gyro_int_offset[0];
    // x and y are reversed to match pitch and roll.
    // pitch is about y axis and roll about x axis
    gyro_int_raw[1]=gy*GYRO_RATIO+gyro_int_offset[1];
    gyro_int_raw[2]=gx*GYRO_RATIO+gyro_int_offset[2];

    gyro_int[0]=gyro_int[0]*(1-gyro_retain[0])+gyro_retain[0]*gyro_int_raw[0];
    gyro_int[1]=gyro_int[1]*(1-gyro_retain[1])+gyro_retain[1]*gyro_int_raw[1];
    gyro_int[2]=gyro_int[2]*(1-gyro_retain[2])+gyro_retain[2]*gyro_int_raw[2];

    ypr_int[0]=ypr[0]*YPR_RATIO+ypr_int_offset[0];
    ypr_int[1]=ypr[1]*YPR_RATIO+ypr_int_offset[1];
    ypr_int[2]=ypr[2]*YPR_RATIO+ypr_int_offset[2];
}

void update_rc(){
    if(ch_changed){
        ch_changed=false;
        sregRestore = SREG;
        cli(); // clear the global interrupt enable flag
        ch1=ch1_val;
        ch2=ch2_val;
        ch3=ch3_val;
        ch4=ch4_val;
        ch5=ch5_val;
        ch6=ch6_val;
        SREG = sregRestore; // restore the status register to its previous value

        /* Serial.print("ch1 "); */
        /* Serial.print(ch1); */
        /* Serial.print("\tch2 "); */
        /* Serial.print(ch2); */
        /* Serial.print("\tch3 "); */
        /* Serial.print(ch3); */
        /* Serial.print("\tch4 "); */
        /* Serial.print(ch4); */
        /* Serial.print("\tch5 "); */
        /* Serial.print(ch5); */
        /* Serial.print("\tch6 "); */
        /* Serial.println(ch6); */

        ch1 = (ch1==0)?(CH1_MAX+CH1_MIN)/2:ch1;
        ch2 = (ch2==0)?(CH2_MAX+CH2_MIN)/2:ch2;
        ch3 = (ch3==0)?CH3_MIN_EFFECT:ch3;
        ch4 = (ch4==0)?(CH4_MAX+CH4_MIN)/2:ch4;
        ch5 = (ch5==0)?(CH5_MAX+CH5_MIN)/2:ch5;
        ch6 = (ch6==0)?(CH6_MAX+CH6_MIN)/2:ch6;


        ch1=constrain(ch1,CH1_MIN,CH1_MAX);
        ch2=constrain(ch2,CH2_MIN,CH2_MAX);
        ch3=constrain(ch3,CH3_MIN,CH3_MAX);
        ch4=constrain(ch4,CH4_MIN,CH4_MAX);
        ch5=constrain(ch5,CH5_MIN,CH5_MAX);
        ch6=constrain(ch6,CH6_MIN,CH6_MAX);

        ch1=map(ch1,CH1_MIN,CH1_MAX,-CH1_EFFECT,CH1_EFFECT);
        ch2=map(ch2,CH2_MIN,CH2_MAX,-CH2_EFFECT,CH2_EFFECT);
        ch3=map(ch3,CH3_MIN,CH3_MAX,CH3_MIN_EFFECT,CH3_MAX_EFFECT);
        ch4=map(ch4,CH4_MIN,CH4_MAX,-CH4_EFFECT,CH4_EFFECT);
        ch5=map(ch5,CH5_MIN,CH5_MAX,-CH5_EFFECT,CH5_EFFECT);
        ch6=map(ch6,CH6_MIN,CH6_MAX,-CH6_EFFECT,CH6_EFFECT);


        if(ch6>0){
            if(ch5>CH5_EFFECT/2){
                if(millis()-take_down_start>=take_down_gradient){
                    take_down_start=millis();
                    if(base_speed_old>take_down_cutoff){
                        base_speed_old--;
                    }else{
                        base_speed_old=ESC_MIN;
                        enable_motors=false;
                    }
                    base_speed=base_speed_old;
                }
            }else if(ch5<-CH5_EFFECT/2){
                if(take_off_count>=take_off_gradient){
                    take_off_count=0;
                    if(base_speed<ch3)
                        base_speed++;
                }else{
                    take_off_count++;
                }
                take_down_start=millis();
            }else{
                enable_motors=true;
                take_down_count=0;
                take_off_count=0;
                base_speed=ch3;
                if(base_speed==CH3_MIN_EFFECT || base_speed-base_speed_old<take_down_diff)
                    base_speed_old=base_speed;
                take_down_start=millis();
            }
        }else{
            enable_motors=false;
            if(ch5>CH5_EFFECT/2){
                show_ypr=true;
                show_speed=false;
            }else if(ch5<-CH5_EFFECT/2){
                show_ypr=false;
                show_speed=true;
            }else{
                show_ypr=false;
                show_speed=false;
            }
        }



    }

    /* Serial.print("ch1 "); */
    /* Serial.print(ch1); */
    /* Serial.print("\tch2 "); */
    /* Serial.print(ch2); */
    /* Serial.print("\tch3 "); */
    /* Serial.print(ch3); */
    /* Serial.print("\tch4 "); */
    /* Serial.print(ch4); */
    /* Serial.print("\tch5 "); */
    /* Serial.print(ch5); */
    /* Serial.print("\tch6 "); */
    /* Serial.println(ch6); */
}

inline void interpolate(){
	interpolate_enter = micros();

	ypr[1] += (1 - Q_RETAIN)*(ypr[1]-ypr_past[1]);
	ypr[2] += (1 - Q_RETAIN)*(ypr[2]-ypr_past[2]);
	
	ypr_past[1] = ypr[1];
	ypr_past[2] = ypr[2];

	interpolate_ratio = 0.5*interpolate_ratio + 0.5*(micros() - interpolate_enter)/(micros() - interpolate_leave);
	interpolate_leave = micros();
}

int MY_RATIO=16;
int ypr_yaw_bound=10;
int ypr_bound=35;
int gyro_yaw_bound=10;
int gyro_bound=800;
int pr_bound=800;
int y_bound=10;

inline void calc_pid(){

    /* gyro_int[0]/=3; */
    /* gyro_int[1]/=3; */
    /* gyro_int[2]/=3; */

    ypr_int_bound[0]=constrain(ypr_int[0],-ypr_yaw_bound,ypr_yaw_bound);
    ypr_int_bound[1]=constrain(ypr_int[1],-ypr_bound,ypr_bound);
    ypr_int_bound[2]=constrain(ypr_int[2],-ypr_bound,ypr_bound);

    gyro_int_bound[0]=constrain(gyro_int[0],-gyro_yaw_bound,gyro_yaw_bound);
    gyro_int_bound[1]=constrain(gyro_int[1],-gyro_bound,gyro_bound);
    gyro_int_bound[2]=constrain(gyro_int[2],-gyro_bound,gyro_bound);

    speed_ypr[0]=-kp[0]*ypr_int[0]+kd[0]*gyro_int[0];
    speed_ypr[1]=kp[1]*ypr_int[1]+kd[1]*gyro_int[1];
    speed_ypr[2]=-kp[2]*ypr_int[2]+kd[2]*gyro_int[2];

    speed_ypr[0]=constrain(speed_ypr[0],-y_bound,y_bound);
    speed_ypr[1]=constrain(speed_ypr[1],-pr_bound,pr_bound);
    speed_ypr[2]=constrain(speed_ypr[2],-pr_bound,pr_bound);

    speed_ypr[0]=0;
    speed_ypr[1]/=MY_RATIO;
    speed_ypr[2]/=MY_RATIO;

    /* s_yaw_pid.Compute(); */
    /* s_pitch_pid.Compute(); */
    /* s_roll_pid.Compute(); */

    /* gyro_ypr_temp[0]=gyro_ypr[0]; */
    /* gyro_ypr_temp[1]=gyro_ypr[1]; */
    /* gyro_ypr_temp[2]=gyro_ypr[2]; */

    // Rate pid on yaw axis not necessary
    /* r_yaw_pid.Compute(); */
    /* r_pitch_pid.Compute(); */
    /* r_roll_pid.Compute(); */

    /* speed_ypr[0]=0; */
    /* speed_ypr[1]=gyro_ypr[1]/MY_RATIO; */
    /* speed_ypr[2]=gyro_ypr[2]/MY_RATIO; */

    /* speed_ypr[0]=speed_ypr_raw[0]; */
    /* speed_ypr[1]=speed_ypr_raw[1]/2; */
    /* speed_ypr[2]=speed_ypr_raw[2]/2; */

    /* speed_ypr[0]=gyro_ypr[0]; */
    /* speed_ypr[1]=gyro_ypr[1]; */
    /* speed_ypr[2]=gyro_ypr[2]; */
    /* if(ret_val) */
        /* Serial.println("Hello"); */
    /* else */
        /* Serial.println("hoooo"); */
}


inline void motor_control()
{
	motor_enter = micros();

	// based on pitch
	m1_speed = base_speed -ch2 -ch1 - speed_ypr[0] - speed_ypr[1]+ m1_speed_off;
	m3_speed = base_speed +ch2 -ch1 -speed_ypr[0] + speed_ypr[1]+ m3_speed_off;

	// based on roll
	m2_speed = base_speed +ch4 +ch1 +speed_ypr[0] - speed_ypr[2]+ m2_speed_off;
	m4_speed = base_speed -ch4 +ch1 +speed_ypr[0] + speed_ypr[2]+ m4_speed_off;
	
	//constrain to to the pulse width limit we can give to the motor
	m1_speed = constrain(m1_speed, min_speed, max_speed);
	m2_speed = constrain(m2_speed, min_speed, max_speed);
	m3_speed = constrain(m3_speed, min_speed, max_speed);
	m4_speed = constrain(m4_speed, min_speed, max_speed);



	if(enable_motors && enable_pitch){
		m1.writeMicroseconds(m1_speed);
		m3.writeMicroseconds(m3_speed);
	}else{
		m1.writeMicroseconds(1000);
		m3.writeMicroseconds(1000);
	}
	
	if(enable_motors && enable_roll){
		m4.writeMicroseconds(m4_speed);
		m2.writeMicroseconds(m2_speed);
	}else{
		m4.writeMicroseconds(1000);
		m2.writeMicroseconds(1000);
	}

	motor_ratio = 0.5*motor_ratio + 0.5*(micros() - motor_enter)/(micros() - motor_leave);
	motor_leave = micros();

}


inline void check_serial(){
    serial_enter = micros();
	// serial_count++;
	serial_send = "";

    /* Serial.println(loop_leave); */

    

	if(show_ypr){

        Serial.print("y: ");
        Serial.print(ypr_int[0]);
        Serial.print(" p: ");
        Serial.print(ypr_int[1]);
        Serial.print(" r: ");
        Serial.print(ypr_int[2]);
        Serial.print(" gy: ");
        Serial.print(gyro_int[0]);
        Serial.print(" gp: ");
        Serial.print(gyro_int[1]);
        Serial.print(" gr: ");
        Serial.print(gyro_int[2]);
        Serial.print("\t\t");

        /* Serial.print("p-y: "); */
        /* Serial.print(gyro_ypr[0]); */
        /* Serial.print(" p-p: "); */
        /* Serial.print(gyro_ypr[1]); */
        /* Serial.print(" p-r: "); */
        /* Serial.print(gyro_ypr[2]); */
        Serial.print(" p-gy: ");
        Serial.print(speed_ypr[0]);
        Serial.print(" p-gp: ");
        Serial.print(speed_ypr[1]);
        Serial.print(" p-gr: ");
        Serial.print(speed_ypr[2]);
        /* Serial.println(""); */
        Serial.print("\t\t");

        Serial.print("m1: ");
        Serial.print(m1_speed);
        Serial.print(" m2: ");
        Serial.print(m2_speed);
        Serial.print(" m3: ");
        Serial.print(m3_speed);
        Serial.print(" m4: ");
        Serial.print(m4_speed);
        Serial.print(" bs: ");
        Serial.print(base_speed);
        Serial.println(" ");
		/* count_ypr++; */
		/* if(count_ypr & 0x10){ */
			/* count_ypr = 0; */
			
			/* serial_send += (ypr[1] * 180/M_PI) - offset_pitch; */
			/* serial_send += "\t";                          */
			/* serial_send += (ypr[2] * 180/M_PI) - offset_roll; */
			/* serial_send += "\t||\t";                          //	 */
			/* serial_send += sum_ypr_deg[1]; */
			/* serial_send += "\t";                        */
			/* serial_send += sum_ypr_deg[2]; */
			/* Serial.println(serial_send); */
		/* } */
	}

	if(show_diff){
		serial_send += "diff_pitch: ";
		serial_send += speed_ypr[1];
		serial_send += "\tdiff_roll: ";
		serial_send += speed_ypr[2];
		serial_send += "gyro_pitch: ";
		serial_send += gx;
		serial_send += "\tgyro_roll: ";
		serial_send += gy;
		serial_send += "\tgyro_yaw: ";
		serial_send += gz;
		Serial.println(serial_send);


	}

	if(show_speed){
		serial_send += "speed: ";
		serial_send += m1_speed;
		serial_send += "\t";
		serial_send += m2_speed;
		serial_send += "\t";
		serial_send += m3_speed;
		serial_send += "\t";
		serial_send += m4_speed;
		Serial.println(serial_send);
	}

	if (Serial.available() > 0){
		// read the value
		char ch = Serial.read();
		// serial_send += ch;

		if(ch != ';' && ch != '=' && ch != ' ' && ch != 'l') {
			// Add the character to the in_str
			in_str += ch;
		}else if(ch == '='){
			in_key = in_str;
			in_str = "";
		}else if(ch == ' '){
			enable_motors = !enable_motors; 
		}else if(ch == 'l'){
			in_key = in_value = in_str = in_index = "";
			serial_send += "speed: ";
			serial_send += base_speed;
			serial_send += "\t\t";
			serial_send += m1_speed;
			serial_send += "\t";
			serial_send += m2_speed;
			serial_send += "\t";
			serial_send += m3_speed;
			serial_send += "\t";
			serial_send += m4_speed;
			// serial_send += "gyro: ";
			// serial_send += gx;
			// serial_send += "\t";
			// serial_send += gy;
			// serial_send += "\t";
			// serial_send += gz;

			serial_send += "serial ratio: \t";
			serial_send += serial_ratio;
			serial_send += "loop ratio: \t";
			serial_send += loop_ratio;
			serial_send += "pid ratio: \t";
			serial_send += pid_ratio;
			serial_send += "mpu ratio: \t";
			serial_send += mpu_ratio;
			serial_send += "motor ratio: \t";
			serial_send += motor_ratio;
			serial_send += "interpolate ratio: \t";
			serial_send += interpolate_ratio;
			

			
			// serial_send += "\tserial count: ";
			// serial_send += serial_count;
			Serial.println(serial_send);

		}else if(ch == ';'){
			in_value = in_str;



			// print the incoming string
			serial_send += "Key: ";
			serial_send += in_key;
			serial_send += "\t\t";
			serial_send += "Value: ";
			serial_send += in_value;
			Serial.println(serial_send);
			serial_send = "";

			// Convert the string to an integer
			float val = in_value.toFloat();

			if(in_key == "kd"){
				if(val < 1000 && val >= 0){
					kd[1] = kd[2] = val;
				}
			}else if(in_key == "kp"){
				if(val < 1000 && val >= 0){
					kp[1] = kp[2] = val;
				}
			}else if(in_key == "ki"){
				if(val < 1000 && val >= 0){
					ki[1] = ki[2] = val;
				}
			}else if(in_value == "k0"){
				serial_send += kp[0];
				serial_send += "\t";
				serial_send += ki[0];
				serial_send += "\t";
				serial_send += kd[2];
				serial_send += "\t";
				serial_send += offset_yaw;
				Serial.println(serial_send);
			}else if(in_value == "k1"){
				serial_send += kp[1];
				serial_send += "\t";
				serial_send += ki[1];
				serial_send += "\t";
				serial_send += kd[1];
				serial_send += "\t";
				serial_send += offset_pitch;
				Serial.println(serial_send);
			}else if(in_value == "k2"){
				serial_send += kp[2];
				serial_send += "\t";
				serial_send += ki[2];
				serial_send += "\t";
				serial_send += kd[2];
				serial_send += "\t";
				serial_send += offset_roll;
				Serial.println(serial_send);
			}else if(in_value == "gk0"){
				serial_send += gyro_kp[0];
				serial_send += "\t";
				serial_send += gyro_ki[0];
				serial_send += "\t";
				serial_send += gyro_kd[0];
				Serial.println(serial_send);
			}else if(in_value == "gk1"){
				serial_send += gyro_kp[1];
				serial_send += "\t";
				serial_send += gyro_ki[1];
				serial_send += "\t";
				serial_send += gyro_kd[1];
				Serial.println(serial_send);
			}else if(in_value == "gk2"){
				serial_send += gyro_kp[2];
				serial_send += "\t";
				serial_send += gyro_ki[2];
				serial_send += "\t";
				serial_send += gyro_kd[2];
				Serial.println(serial_send);
			}else if(in_key == "kp1"){
				if(val < 1000 && val >= 0){
					kp[1] = val;
				}
			}else if(in_key == "ki1"){
				if(val < 1000 && val >= 0){
					ki[1] = val;
				}
			}else if(in_key == "kd1"){
				if(val < 1000 && val >= 0){
					kd[1] = val;
				}
			}else if(in_key == "kp2"){
				if(val < 1000 && val >= 0){
					kp[2] = val;
				}
			}else if(in_key == "ki2"){
				if(val < 1000 && val >= 0){
					ki[2] = val;
				}
			}else if(in_key == "kd2"){
				if(val < 1000 && val >= 0){
					kd[2] = val;
				}
			}else if(in_key == "gkp"){
				if(val < 1000 && val >= 0){
					gyro_kp[1] = val;
					gyro_kp[2] = val;
				}
			}else if(in_key == "gkd"){
				if(val < 1000 && val >= 0){
					gyro_kd[1] = val;
					gyro_kd[2] = val;
				}
			}else if(in_key == "gki"){
				if(val < 1000 && val >= 0){
					gyro_ki[1] = val;
					gyro_ki[2] = val;
				}
			}else if(in_key == "gkp1"){
				if(val < 1000 && val >= 0){
					gyro_kp[1] = val;
				}
			}else if(in_key == "gki1"){
				if(val < 1000 && val >= 0){
					gyro_ki[1] = val;
				}
			}else if(in_key == "gkd1"){
				if(val < 1000 && val >= 0){
					gyro_kd[1] = val;
				}
			}else if(in_key == "gkp2"){
				if(val < 1000 && val >= 0){
					gyro_kp[2] = val;
				}
			}else if(in_key == "gki2"){
				if(val < 1000 && val >= 0){
					gyro_ki[2] = val;
				}
			}else if(in_key == "gkd2"){
				if(val < 1000 && val >= 0){
					gyro_kd[2] = val;
				}
			}else if(in_key == "or"){
				if(val < 1000 && val >= 0){
					offset_roll = val;
				}
			}else if(in_key == "op"){
				if(val < 1000 && val >= 0){
					offset_pitch = val;
				}
			}else if(in_key == "o1"){
				if(val < 600 && val >= 0){
					m1_speed_off = val;
				}
			}else if(in_key == "o2"){
				if(val < 600 && val >= 0){
					m2_speed_off = val;
				}
			}else if(in_key == "o3"){
				if(val < 600 && val >= 0){
					m3_speed_off = val;
				}
			}else if(in_key == "o4"){
				if(val < 600 && val >= 0){
					m4_speed_off = val;
				}
			}else if(in_key == "qr"){
				if(val <= 1 && val >= 0){
					#define Q_RETAIN val;
				}
			}else if(in_key == "bs"){
				if(val < 2000 && val >= 0){
					base_speed = (int)val;
					serial_send += "Speed changed to: ";
					serial_send += base_speed;
					Serial.println(serial_send);
				}
			}else if(in_value == "s"){
				show_speed = !show_speed;
			}else if(in_value == "ypr"){
				show_ypr = !show_ypr;
			}else if(in_value == "diff"){
				show_diff = !show_diff;
			}else if(in_value == "m"){
				enable_motors = !enable_motors;
			}else if(in_value == "p"){
				enable_pitch = !enable_pitch;
			}else if(in_value == "r"){
				enable_roll = !enable_roll;
			}else if(in_value == "y"){
				enable_yaw = !enable_yaw;
			}else{
				serial_send += "Error with the input ";
				serial_send += in_key;
				Serial.println(serial_send);
			}
			in_key = in_value = in_str = in_index = "";


            s_yaw_pid.SetTunings(kp[0],ki[0],kd[0]);
            s_pitch_pid.SetTunings(kp[1],ki[1],kd[1]);
            s_roll_pid.SetTunings(kp[2],ki[2],kd[2]);

            r_yaw_pid.SetTunings(gyro_kp[0],gyro_ki[0],gyro_kd[0]);
            r_pitch_pid.SetTunings(gyro_kp[1],gyro_ki[1],gyro_kd[1]);
            r_roll_pid.SetTunings(gyro_kp[2],gyro_ki[2],gyro_kd[2]);
		}
	}
	serial_ratio = 0.5*serial_ratio + 0.5*(micros() - serial_enter)/(micros() - serial_leave);
	serial_leave = micros();
}


Quaternion mult(Quaternion q, float f){
	q.w *= f;
	q.x *= f;
	q.y *= f;
	q.z *= f;

	return q;
}


Quaternion add(Quaternion q, Quaternion s){
	Quaternion t;
	t.w = q.w + s.w;
	t.x = q.x + s.x;
	t.y = q.y + s.y;
	t.z = q.z + s.z;
	
	return t;
}

Quaternion diff(Quaternion q, Quaternion s){
	Quaternion t;
	t.w = q.w - s.w;
	t.x = q.x - s.x;
	t.y = q.y - s.y;
	t.z = q.z - s.z;
	
	return t;
}



								/* 
								     up [*] -ve pitch

								     M 1

								     ||
								     ||
								  sparkfun
								     ||
down [X] +ve roll    M 2 ============||============  M 4     up [*] -ve roll
								     ||
								     ||
								     ||
								     ||

								     M 3

								   down [X] +ve pitch



                               _______\     
                                      /
                               (  yaw  )     clockwise +ve
                               /______
                               \


   roll values


   1500  27.00  25.00  5.00

 */

