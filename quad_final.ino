#include <Servo.h>

Servo m1, m2, m3, m4;

int base_speed = 1330, max_speed = 1800, min_speed = 1100;

int m1_speed, m2_speed, m3_speed, m4_speed;

int m1_speed_off = 0, m2_speed_off = 0, m3_speed_off = 0, m4_speed_off = 0;

unsigned long timer = 0, timer2 = 0;

int speed_ypr[3];

float serial_ratio = 0, serial_enter, serial_leave, serial_count = 0;
float loop_ratio = 0, loop_enter, loop_leave, loop_count = 0;
float pid_ratio = 0, pid_enter, pid_leave, pid_count = 0;
float motor_ratio = 0, motor_enter, motor_leave, motor_count = 0;
float mpu_ratio = 0, mpu_enter, mpu_leave, mpu_count = 0;
float interpolate_ratio = 0, interpolate_enter, interpolate_leave, interpolate_count = 0;

float prev_ypr_deg[3], prev_ypr[3], sum_ypr_deg[3], sum_ypr[3];
int sum_ypr_int[3], prev_ypr_int[3];

float offset_pitch = 0.00, offset_roll = 0.00, offset_yaw = 0.00;

long kp[3] = {17190, 16044, 17190}, kd[3] = {1146000, 1146000, 1146000}, ki[3] = {286, 286, 286};
long gyro_kp[3] = {1000, 1000, 1000}, gyro_kd[3] = {0, 0, 0}, gyro_ki[3] = {0, 0, 0};

String in_str, in_key, in_value, in_index, serial_send = "";

bool in_str_arr = false, show_speed = false, show_ypr = false, enable_motors = false,
	enable_pitch = false, enable_yaw = false, enable_roll = false, show_diff = false;

int count_auto_calc=0, count_dmp_calc=0, count_motor = 0, count_mpu = 0, count_ypr = 0, count_serial = 0, count_iter = 0, count_check_overflow = 0;

// Arduino Wire library is required if I2Cdev I2CDEV_ARDUINO_WIRE implementation
// is used in I2Cdev.h
#include "Wire.h"

// I2Cdev and MPU6050 must be installed as libraries, or else the .cpp/.h files
// for both classes must be in the include path of your project
#include "I2Cdev.h"

#include "MPU6050_6Axis_MotionApps20.h"
//#include "MPU6050.h" // not necessary if using MotionApps include file

// class default I2C address is 0x68
// specific I2C addresses may be passed as a parameter here
// AD0 low = 0x68 (default for SparkFun breakout and InvenSense evaluation board)
// AD0 high = 0x69
MPU6050 mpu;
MPU6050 accelgyro;  

#define LED_PIN 13 // (Arduino is 13, Teensy is 11, Teensy++ is 6)
#define PID 1
#define GYRO_RATIO 76
#define AUTO_CYCLES 5
#define MAX_BUFFER 5
#define Q_RETAIN (float)0.5

bool blinkState = false;

// MPU control/status vars
bool dmp_ready = false;  // set true if DMP init was successful
uint8_t mpu_int_status;   // holds actual interrupt status byte from MPU
uint8_t devStatus;      // return status after each device operation (0 = success, !0 = error)
uint16_t packetSize;    // expected DMP packet size (default is 42 bytes)
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
int ypr_int[3];
int16_t gx, gy, gz;

// packet structure for InvenSense teapot demo
// uint8_t teapotPacket[14] = { '$', 0x02, 0, 0, 0, 0, 0, 0, 0, 0, 0x00, 0x00, '\r', '\n' };

void calc_pid(void);
void motor_control(void);
void get_data(void);
Quaternion mult(Quaternion q, float f);
Quaternion add(Quaternion q, Quaternion s);
Quaternion diff(Quaternion q, Quaternion s);



volatile bool mpu_interrupt = false;     // indicates whether MPU interrupt pin has gone high
void dmpDataReady() {
	mpu_interrupt = true;
}



// ================================================================
// ===                      INITIAL SETUP                       ===
// ================================================================

void setup() {

	Serial.begin(115200);

	m1.attach(4);            //////pins to which motors are connected
	m2.attach(5);
	m3.attach(6);
	m4.attach(7);

	Serial.write("\nConnect the battery\n");

	//Initial motor speed -> 1000
	m1.writeMicroseconds(1000);
	m2.writeMicroseconds(1000);
	m3.writeMicroseconds(1000);
	m4.writeMicroseconds(1000);


	// join I2C bus (I2Cdev library doesn't do this automatically)
	Wire.begin();

	// TWBR = 12; // set 400kHz mode @ 16MHz CPU or 200kHz mode @ 8MHz CPU

	// initialize serial communication
	// (115200 chosen because it is required for Teapot Demo output, but it's
	// really up to you depending on your project)

	while (!Serial); // wait for Leonardo enumeration, others continue immediately

	// NOTE: 8MHz or slower host processors, like the Teensy @ 3.3v or Ardunio
	// Pro Mini running at 3.3v, cannot handle this baud rate reliably due to
	// the baud timing being too misaligned with processor ticks. You must use
	// 38400 or slower in these cases, or use some kind of external separate
	// crystal solution for the UART timer.

	// initialize device
	Serial.println(F("Initializing I2C devices..."));
	accelgyro.initialize();
	accelgyro.setI2CBypassEnabled(true);
	mpu.initialize();
	mpu.setRate(0);

	// verify connection
	Serial.println(F("Testing device connections..."));
	Serial.println(mpu.testConnection() ? F("MPU6050 connection successful") : F("MPU6050 connection failed"));

	// wait for ready
	Serial.println(F("\nSend any character to begin DMP programming and demo: "));
	while (Serial.available() && Serial.read()); // empty buffer
	while (!Serial.available());                 // wait for data
	while (Serial.available() && Serial.read()); // empty buffer again

	// load and configure the DMP
	Serial.println(F("Initializing DMP..."));
	devStatus = mpu.dmpInitialize();

	// make sure it worked (returns 0 if so)
	if (devStatus == 0) {
		// turn on the DMP, now that it's ready
		Serial.println(F("Enabling DMP..."));
		mpu.setDMPEnabled(true);

		// enable Arduino interrupt detection
		Serial.println(F("Enabling interrupt detection (Arduino external interrupt 0)..."));
		attachInterrupt(0, dmpDataReady, RISING);
		mpu_int_status = mpu.getIntStatus();

		// set our DMP Ready flag so the main loop() function knows it's okay to use it
		Serial.println(F("DMP ready! Waiting for first interrupt..."));
		dmp_ready = true;

		// get expected DMP packet size for later comparison
		packetSize = mpu.dmpGetFIFOPacketSize();
	} else {
		// ERROR!
		// 1 => initial memory load failed
		// 2 => DMP configuration updates failed
		// (if it's going to break, usually the code will be 1)
		Serial.print(F("DMP Initialization failed (code "));
		Serial.print(devStatus);
		Serial.println(F(")"));

		asm volatile ("  jmp 0");  
	}

	// configure LED for output
	pinMode(LED_PIN, OUTPUT);

	mpu_enter = mpu_leave = micros();
	serial_enter = serial_leave = micros();
	loop_enter = loop_leave = micros();
	pid_enter = pid_leave = micros();
	interpolate_enter = interpolate_leave = micros();
	
}


// ================================================================
// ===                    MAIN PROGRAM LOOP                     ===
// ================================================================

void loop(){
	loop_enter = micros();

	// go via sensor once in every 8 iterations
	if((count_iter & 0x08)|| mpu_interrupt){
		count_iter = 0;
		count_check_overflow++;

		mpu_enter = micros();


		if(count_check_overflow & 0x40){
			count_check_overflow = 0;

			// reset interrupt flag and get INT_STATUS byte
			
			mpu_int_status = mpu.getIntStatus();

			// check for overflow (code is too inefficient) || actual limit ==> 1024
			if (mpu_int_status & 0x10) {
				// reset so we can continue cleanly
				mpu.resetFIFO();
				serial_send = "overflow:\t";
				Serial.println(serial_send);
			}else if(mpu.getFIFOCount() > 256){
				Serial.println("Slow !!");
			}
		}

		// otherwise, check for DMP data ready interrupt
		if (mpu_interrupt){
			mpu_interrupt = false;

			count_auto_calc--;
			// read a packet from FIFO
			mpu.getFIFOBytes(fifo_buffer, packetSize);
			
			// display Euler angles in degrees
			mpu.dmpGetQuaternion(&q, fifo_buffer);
			mpu.dmpGetGravity(&gravity, &q);
			mpu.dmpGetYawPitchRoll(ypr, &q, &gravity);
			
		}else{
			count_auto_calc++;
			interpolate();
		}

		if(count_auto_calc > 100 || count_auto_calc < -100){
			Serial.println(count_auto_calc);
			count_auto_calc = 0;
		}

		mpu_ratio = 0.5*mpu_ratio + 0.5*(micros() - mpu_enter)/(micros() - mpu_leave);
		mpu_leave = micros();

	}else{
		// interpolate past ypr values to assume newer
		interpolate();
		count_iter++;	
	}
	
	// accelgyro.getRotation(&gx,&gy,&gz);

	count_serial++;
	if(count_serial & 0x40){
		check_serial();
		count_serial = 0;
	}

	loop_ratio = 0.5*loop_ratio + 0.5*(micros() - loop_enter)/(micros() - loop_leave);
	loop_leave = micros();

	calc_pid();
	motor_control();

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

inline void calc_pid()
{
	pid_enter = micros();
	
	//==============================		YPR 		==============================
	
	ypr_int[1] = ypr[1] * 128;
	ypr_int[2] = ypr[2] * 128;
	
	sum_ypr_int[1] += ypr_int[1];
	sum_ypr_int[2] += ypr_int[2];

	sum_ypr_int[1] = constrain(sum_ypr_int[1],-256,256);
	sum_ypr_int[2] = constrain(sum_ypr_int[2],-256,256);

	speed_ypr[1] = kp[1]*ypr_int[1] + kd[1]*(ypr_int[1]-prev_ypr_int[1]) + ki[1]*sum_ypr_int[1];
	speed_ypr[2] = kp[2]*ypr_int[2] + kd[2]*(ypr_int[2]-prev_ypr_int[2]) + ki[2]*sum_ypr_int[2];
	
	prev_ypr_int[0] = ypr_int[0];                                  
	prev_ypr_int[1] = ypr_int[1]; 
	prev_ypr_int[2] = ypr_int[2];  
	
	

	//==============================		RATE 		==============================

	// gyroPitch = gx*GYRO_RATIO;
	// gyroRoll = gy*GYRO_RATIO;

	// sum_gyro[1] = sum_gyro[1] + (speed_ypr[1]-gyroPitch);
	// sum_gyro[2] = sum_gyro[2] + (speed_ypr[2]-gyroRoll);

	// sum_gyro[1] = constrain(sum_gyro[1],-2000000,2000000);
	// sum_gyro[2] = constrain(sum_gyro[2],-2000000,2000000);

	// rate_ypr[1] = gyro_kp[1]*(speed_ypr[1] - gyroPitch) + gyro_kd[1]*((speed_ypr[1]-gyroPitch)-prev_gyro_diff[1]) + gyro_ki[1]*sum_gyro[1]; 
	// rate_ypr[2] = gyro_kp[2]*(speed_ypr[2] - gyroRoll) + gyro_kd[2]*((speed_ypr[2]+gyroRoll)-prev_gyro_diff[2]) + gyro_ki[2]*sum_gyro[2]; 

	// prev_gyro_diff[1] = (speed_ypr[1] - gyroPitch);                                  
	// prev_gyro_diff[2] = (speed_ypr[2] - gyroRoll); 
	
	
	pid_ratio = 0.5*pid_ratio + 0.5*(micros() - pid_enter)/(micros() - pid_leave);
	pid_leave = micros();
}

inline void motor_control()
{
	motor_enter = micros();

	// based on pitch
	m1_speed = base_speed - (speed_ypr[1] >> 7) + m1_speed_off;
	m3_speed = base_speed + (speed_ypr[1] >> 7) + m3_speed_off;

	// based on roll
	m2_speed = base_speed + (speed_ypr[2] >> 7) + m2_speed_off;
	m4_speed = base_speed - (speed_ypr[2] >> 7) + m4_speed_off;
	// Serial.println(rate_ypr[2]);
	// Serial.println(rate_ypr[1]);
	

	// // based on pitch
	// m1_speed = base_speed - rate_ypr[1]/10000 + m1_speed_off;
	// m3_speed = base_speed + rate_ypr[1]/10000 + m3_speed_off ;

	// // based on roll
	// m2_speed = base_speed + rate_ypr[2]/10000 + m2_speed_off ;
	// m4_speed = base_speed - rate_ypr[2]/10000 + m4_speed_off ;

	 // m1_speed = base_speed - speed_ypr[1] + speed_ypr[0]; 
	 // m2_speed = base_speed + speed_ypr[2] - speed_ypr[0];
	 // m3_speed = base_speed + speed_ypr[1] + speed_ypr[0];
	 // m4_speed = base_speed - speed_ypr[2] - speed_ypr[0];

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

	if(show_ypr){
		count_ypr++;
		if(count_ypr & 0x10){
			count_ypr = 0;
			
			serial_send += (ypr[1] * 180/M_PI) - offset_pitch;
			serial_send += "\t";                         
			serial_send += (ypr[2] * 180/M_PI) - offset_roll;
			serial_send += "\t||\t";                          //	
			serial_send += sum_ypr_deg[1];
			serial_send += "\t";                       
			serial_send += sum_ypr_deg[2];
			Serial.println(serial_send);
		}
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

		}
	}
	serial_ratio = 0.5*serial_ratio + 0.5*(micros() - serial_enter)/(micros() - serial_leave);
	serial_leave = micros();
}

void stop_motors(){
	m1.writeMicroseconds(1000);
	m3.writeMicroseconds(1000);
	m2.writeMicroseconds(1000);
	m4.writeMicroseconds(1000);
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

