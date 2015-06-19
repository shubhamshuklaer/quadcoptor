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

int m1_speed, m2_speed, m3_speed, m4_speed;

int m1_speed_off = 0, m2_speed_off = 0, m3_speed_off = 0, m4_speed_off = 0;

int gyro_ypr[3];
int speed_ypr[3];
int desired_ypr[3]={0,0,0};

float serial_ratio = 0, serial_enter, serial_leave, serial_count = 0;
float loop_ratio = 0, loop_enter, loop_leave, loop_count = 0;
float pid_ratio = 0, pid_enter, pid_leave, pid_count = 0;
float motor_ratio = 0, motor_enter, motor_leave, motor_count = 0;
float mpu_ratio = 0, mpu_enter, mpu_leave, mpu_count = 0;
float interpolate_ratio = 0, interpolate_enter, interpolate_leave, interpolate_count = 0;

int kp[3] = {4, 24, 24}, kd[3] = {0, 1, 1}, ki[3] = {1, 1, 1};
int iterm_prev;

String in_str, in_key, in_value, in_index, serial_send = "";

bool in_str_arr = false, show_speed = false, show_ypr = false, enable_motors = false,
	enable_pitch = false, enable_yaw = false, enable_roll = false, show_diff = false;

int count_check_serial=0,count_serial=0;

MPU6050 mpu;

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
const int CH3_MIN_CUTOFF=50;

int take_off_count=0;
int take_down_count=0;
unsigned long take_down_start=0;
const int take_down_cutoff=1400;
const int take_off_gradient=100;
const int take_down_gradient=14;
const int take_down_diff=20;

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


void setup(){
    ApplicationMonitor.DisableWatchdog();
    Serial1.begin(9600);
    Serial.begin(9600);
    while (!Serial1); // wait for Leonardo enumeration, others continue immediately
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
    /* ApplicationMonitor.Dump(Serial1); */
    ApplicationMonitor.EnableWatchdog(Watchdog::CApplicationMonitor::Timeout_1s);
}



void loop(){

    // earlier it was 0x40 but for processing graph I increased it
    // 0x40 is hex of 64 decimal i.e 01000000 and hence the bitwise and works
    // 0x100 is 256 in decimal
    if(count_serial & 0x100){
        unsigned long cur_milli=millis();
        count_serial = 0;

        //yaw
        Serial1.print("y ");
        Serial1.print(cur_milli);
        Serial1.print(" ");
        Serial1.println(ypr_int[0]);

        //pitch
        Serial1.print("p ");
        Serial1.print(cur_milli);
        Serial1.print(" ");
        Serial1.println(ypr_int[1]);

        //Roll
        Serial1.print("r ");
        Serial1.print(cur_milli);
        Serial1.print(" ");
        Serial1.println(ypr_int[2]);

        //yaw gyro
        Serial1.print("gy ");
        Serial1.print(cur_milli);
        Serial1.print(" ");
        Serial1.println(gyro_int[0]);

        //pitch gyro
        Serial1.print("gp ");
        Serial1.print(cur_milli);
        Serial1.print(" ");
        Serial1.println(gyro_int[1]);

        //Roll gyro
        Serial1.print("gr ");
        Serial1.print(cur_milli);
        Serial1.print(" ");
        Serial1.println(gyro_int[2]);

        //base speed
        Serial1.print("bs ");
        Serial1.print(cur_milli);
        Serial1.print(" ");
        Serial1.println(base_speed);

        //m1 speed
        Serial1.print("m1 ");
        Serial1.print(cur_milli);
        Serial1.print(" ");
        Serial1.println(m1_speed);

        //m2 speed
        Serial1.print("m2 ");
        Serial1.print(cur_milli);
        Serial1.print(" ");
        Serial1.println(m2_speed);

        //Pid yaw
        Serial1.print("sy ");
        Serial1.print(cur_milli);
        Serial1.print(" ");
        Serial1.println(speed_ypr[0]);

        //Pid pitch
        Serial1.print("sp ");
        Serial1.print(cur_milli);
        Serial1.print(" ");
        Serial1.println(speed_ypr[1]);

        //Pid roll
        Serial1.print("sr ");
        Serial1.print(cur_milli);
        Serial1.print(" ");
        Serial1.println(speed_ypr[2]);
    }else{
        count_serial=count_serial+1;
    }

    if(count_check_serial & 0x40)
        check_serial();
    else
        count_check_serial=count_check_serial+1;


    update_rc();
    update_ypr();
    calc_pid();
    motor_control();
    ApplicationMonitor.IAmAlive();
}

void pid_init(){
    iterm_prev=millis();

    unsigned long yaw_tune_start=micros();
    while(micros()-yaw_tune_start<PROPER_YAW_TIME)
        update_ypr();
    
    int desired_yaw;
    int yaw_average_count_copy=YAW_AVERAGE_COUNT;
    while(yaw_average_count_copy>0){
        update_ypr();
        desired_yaw=desired_yaw*(1-YAW_AVERAGE_RETAIN)+YAW_AVERAGE_RETAIN*ypr_int[0];
        yaw_average_count_copy--;
    }

    desired_ypr[0]=desired_yaw;
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
    mpu.initialize();
    mpu.setRate(0);
    mpu.setFullScaleGyroRange(250);

    // verify connection
    if(!mpu.testConnection())
        Serial1.println(F("MPU connection failed"));

    // load and configure the DMP
    dev_status = mpu.dmpInitialize();

    // make sure it worked (returns 0 if so)
    if (dev_status == 0) {
        // turn on the DMP, now that it's ready
        /* Serial1.println(F("Enabling DMP...")); */
        mpu.setDMPEnabled(true);

        // enable Arduino interrupt detection
        enableInterrupt(DMP_INT_PIN, dmp_data_ready, RISING);
        mpu_int_status = mpu.getIntStatus();

        // set our DMP Ready flag so the main loop() function knows it's okay to use it
        dmp_ready = true;

        // get expected DMP packet size for later comparison
        packet_size = mpu.dmpGetFIFOPacketSize();
    } else {
        // ERROR!
        // 1 = initial memory load failed
        // 2 = DMP configuration updates failed
        // (if it's going to break, usually the code will be 1)
        Serial1.print(F("DMP Initialization failed (code "));
        Serial1.print(dev_status);
        Serial1.println(F(")"));
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
            Serial1.println(fifo_count);
            mpu.resetFIFO();
            Serial1.println(F("FIFO overflow!"));

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
                    if(base_speed>take_down_cutoff){
                        base_speed--;
                    }else{
                        base_speed=ESC_MIN;
                        enable_motors=false;
                    }
                }
            }else if(ch5<-CH5_EFFECT/2){
                enable_pitch=false;
            }else{
                enable_motors=true;
                enable_pitch=true;
                take_down_count=0;
                take_off_count=0;
                base_speed=ch3;
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

}

int MY_RATIO=16;
int ypr_yaw_bound=10;
int ypr_bound=33;
int gyro_yaw_bound=10;
int gyro_bound=800;
int pr_bound=800;
int y_bound=10;
int ipr_bound=200;
int iy_bound=10;
int num_millis_iterm=50;
int iterm[3]={0,0,0};

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

    if(millis()-iterm_prev>num_millis_iterm){
        iterm[1]+=kp[1]*ypr_int_bound[1];
        iterm[2]+=kp[2]*ypr_int_bound[2];
        iterm[1]=constrain(iterm[1],-ipr_bound,ipr_bound);
        iterm[2]=constrain(iterm[2],-ipr_bound,ipr_bound);
        iterm_prev=millis();
    }


    speed_ypr[0]=-kp[0]*ypr_int_bound[0]+kd[0]*gyro_int_bound[0]+iterm[0];
    speed_ypr[1]=kp[1]*ypr_int_bound[1]+kd[1]*gyro_int_bound[1]+iterm[1];
    speed_ypr[2]=-kp[2]*ypr_int_bound[2]+kd[2]*gyro_int_bound[2]+iterm[2];

    /* speed_ypr[0]=constrain(speed_ypr[0],-y_bound,y_bound); */
    /* speed_ypr[1]=constrain(speed_ypr[1],-pr_bound,pr_bound); */
    /* speed_ypr[2]=constrain(speed_ypr[2],-pr_bound,pr_bound); */

    speed_ypr[1]/=MY_RATIO;
    speed_ypr[2]/=MY_RATIO;
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
	serial_send = "";

	if (Serial1.available() > 0){
		// read the value
		char ch = Serial1.read();

		if(ch != ';' && ch != '='){
			// Add the character to the in_str
			in_str += ch;
		}else if(ch == '='){
			in_key = in_str;
			in_str = "";
		}else if(ch == ';'){
			in_value = in_str;
			// print the incoming string
			serial_send += "Key: ";
			serial_send += in_key;
			serial_send += "\t\t";
			serial_send += "Value: ";
			serial_send += in_value;
			Serial.println(serial_send);//to terminal
			serial_send = "";

			// Convert the string to an integer
			int val = in_value.toInt();

            if(in_key=="rat"){
                MY_RATIO=val;
            }else if(in_key=="prb"){
                ypr_bound=val;
            }else if(in_key=="gprb"){
                gyro_bound=val;
            }else if(in_key=="iprb"){
                ipr_bound=val;
            }else if(in_key=="yprb"){
                ypr_yaw_bound=val;
            }else if(in_key=="ygprb"){
                gyro_yaw_bound=val;
            }else if(in_key=="yiprb"){
                iy_bound=val;
            }else if(in_key=="miterm"){
                num_millis_iterm=val;
            }else if(in_key=="sb"){
                pr_bound=val;
            }else if(in_key == "kd"){
                kd[1] = kd[2] = val;
			}else if(in_key == "kp"){
                kp[1] = kp[2] = val;
			}else if(in_key == "ki"){
                ki[1] = ki[2] = val;
            }else if(in_key == "ykd"){
                kd[0]= val;
			}else if(in_key == "ykp"){
                kp[0]= val;
			}else if(in_key == "yki"){
                ki[0] = val;
			}else{
				serial_send += "Error with the input ";
				serial_send += in_key;
                //It will print on terminal
				Serial.println(serial_send);
			}
			in_key = in_value = in_str = in_index = "";
		}
	}
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

