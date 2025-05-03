#include <LiquidCrystal.h>
#include <SoftwareSerial.h>
#include <Adafruit_NeoPixel.h>


#define PIN 3
#define LED_NUM 100  //LEDの数
Adafruit_NeoPixel ledtape = Adafruit_NeoPixel(LED_NUM, PIN, NEO_GRB + NEO_KHZ800);

// LCD ←→ Arduinoのピンの割り当て
// rs      →   D4
// rw      →   GND
// enable  →   D5
// d4      →   D6
// d5      →   D7
// d6      →   D8
// d7      →   D9

LiquidCrystal lcd(4, 5, 6, 7, 8, 9);
SoftwareSerial Bluetooth(0, 1);  // RX | TX

int LED_PIN_R = 13;  // デジタルピン9番に赤色LEDを割り当てる(変数)
int LED_PIN_G = 12;  // デジタルピン10番に緑色LEDを割り当てる(変数)
int LED_PIN_B = 11;  // デジタルピン11番に青色LEDを割り当てる(変数)
int WAIT = 9;        // 待ち時間の指定
char data = 0;
int color = 0;
int a = 0;

int tapecolor[100][3];

int H;
int R, G, B;
int start = 0;
int start2 = 0;
int count = 0;
int convertedValue;
float convertedValue2;
int ch[5];

void setup() {
  TIMSK0 = 0;

  pinMode(LED_PIN_R, OUTPUT);  // それぞれのピンを出力に設定
  pinMode(LED_PIN_G, OUTPUT);
  pinMode(LED_PIN_B, OUTPUT);

  lcd.begin(16, 2);     // LCDの桁数と行数を指定する(16桁2行)
  lcd.clear();          // LCD画面をクリア
  lcd.setCursor(0, 1);  // カーソルの位置を指定
                        // lcd.print("Arduino Uno");  // 文字の表示

  //Bluetooth.begin(9600); //opens serial port, sets data rate to 9600 bps
  Serial.begin(9600);  //opens serial port, sets data rate to 9600 bps
                       // Serial.println("Connect your device with 1234 as Paring Key\n");


  lcd.setCursor(0, 0);            // カーソルの位置を指定
  lcd.print("Connect your ");     // 文字の表示
  lcd.setCursor(0, 1);            // カーソルの位置を指定
  lcd.print("Device with 1234");  // 文字の表示

  ledtape.begin();
  ledtape.show();



  //色相環
  /*
 for (H=0 ; H<=360 ; H++) { 
   if (H <= 60) { 
   R = 255 ;
   G = H*4.25 ;
   B = 0 ;
 }
 else if (H <= 120) { 
   R = (120-H)*4.25 ;
   G = 255 ;
   B = 0 ;
 }
 else if (H <= 180) { 
   R = 0 ;
   G = 255 ;
   B = (H-120)*4.25 ;
 }
 else if (H <= 240) { 
   R = 0 ;
   G = (240-H)*4.25 ;
   B = 255 ;
 }
 else if (H <= 300) { 
   R = (H-240)*4.25 ;
   G = 0 ;
   B = 255 ;
 }
 else { 
   R = 255 ;
   G = 0 ;
   B = (360-H)*4.25 ;
 }
*/
  /*
 for (H=0 ; H<=360 ; H++) {

          if (H <= 120) {
               R = map(H,0,120,255,0) ;     // 赤LED R←→G
               G = map(H,0,120,0,255) ;     // 緑LED G←→R
               B = 0 ;
          } else if (H <= 240) {

               G = map(H,120,240,255,0) ;   // 緑LED G←→B
               B = map(H,120,240,0,255) ;   // 青LED B←→G
               R = 0 ;
          } else {

               B = map(H,240,360,255,0) ;   // 青LED B←→R
               R = map(H,240,360,0,255) ;   // 青LED R←→B
               G= 0 ;
          }

 tapecolor[int(H*(1/3.6))][0] = R;
 tapecolor[int(H*(1/3.6))][1] = G;
 tapecolor[int(H*(1/3.6))][2] = B;
*/


  //}
}

char in[60];  //文字列格納用
int i = 0;    //文字カウント用



float receive, ACCEL, RAD;

void loop() {
  

  if (Serial.available()) {

    //data = Serial.read();//シリアル通信で受け取ったデータを読み込む
    in[i] = Serial.read();  //文字の読み込み」
    if (in[i] == '/') {     //「/」が読み込まれたなら文字列の最後とみなし
      in[i] = '\0';         //終端文字（「Null」と同様）を挿入
      //Serial.write(in);       //シリアルに文字列を書込
      //Serial.write("\n");     //改行コードを書込
      i = 0;  //カウントを戻す
    } else {
      i++;
    }

    convertedValue = atoi(in);
    convertedValue2 = atof(in);
    receive= convertedValue2;

    //ch[0] = atoi(strtok(in, ","));
    //if(ch[1] == 0 || ch[2] == 0){
    //ch[1] = atoi(strtok(NULL, ","));
    //ch[2] = atoi(strtok(NULL, ","));
    //}
    lcd.clear();
    lcd.setCursor(0, 0);     // カーソルの位置を指定
    lcd.print("Connected");  // 文字の表示
    lcd.setCursor(0, 1);     // カーソルの位置を指定
    lcd.print("Data:");      // 文字の表示
    lcd.setCursor(5, 1);     // カーソルの位置を指定
    lcd.print(in);           // 文字の表示
    //Serial.println(stock);
  }
  /*
 start++;
 count= 0;
 if(start>99) start=0;
 */


  count = 0;

  // start = int((100*convertedValue)/360);
  /*receive = convertedValue2;
  if(convertedValue2<100000) {
    receive = stock;//角度調整は正の値のみ
  }else{
    stock= receive;
  }*/
//  receive = convertedValue2;
  RAD = (receive / 1000) - 100;
  ACCEL = fmod(receive,1000)- 100; 
  //ACCEL /= 10;
  Serial.println(ACCEL);
  //Serial.println(a);
  //test2(RAD,ACCEL);
  
  if(ACCEL < 21.0){//tuning
    taiki(); 
  }else{
  test2(RAD,ACCEL);
  }
  
  ledtape.show();
  
}

void taiki() {
  int count = 0;
  while (count < 100) {
    ledtape.setPixelColor(count, ledtape.Color(20, 20, 20));//tuning
    count++;
  }
}


int HSBtoR(int Rad, float Accel) {

  /* HSVのH値を各ＬＥＤのアナログ出力値(0-255)に変換する処理 */
  if (Rad <= 120) {
    /* Rad値(0-120) 赤-黄-緑     */
    R = map(Rad, 0, 120, 255, 0);  // 赤LED R←→G
  } else if (Rad <= 240) {
    /* Rad値(120-240) 緑-水色-青 */
    R = 0;
  } else {
    /* Rad値(240-360) 青-紫-赤   */
    R = map(Rad, 240, 360, 0, 255);  // 青LED R←→B
  }
  return int(R / Accel);
}

int HSBtoG(int Rad, float Accel) {
  /* HSVのH値を各ＬＥＤのアナログ出力値(0-255)に変換する処理 */
  if (Rad <= 120) {
    /* Rad値(0-120) 赤-黄-緑     */
    G = map(Rad, 0, 120, 0, 255);  // 緑LED G←→R
  } else if (Rad <= 240) {
    /* Rad値(120-240) 緑-水色-青 */
    G = map(Rad, 120, 240, 255, 0);  // 緑LED G←→B
  } else {
    /* Rad値(240-360) 青-紫-赤   */
    G = 0;
  }
  return int(G / Accel);
}
int HSBtoB(int Rad, float Accel) {
  /* HSVのH値を各ＬＥＤのアナログ出力値(0-255)に変換する処理 */
  if (Rad <= 120) {
    /* H値(0-120) 赤-黄-緑     */
    B = 0;
  } else if (Rad <= 240) {
    /* Rad値(120-240) 緑-水色-青 */
    B = map(Rad, 120, 240, 0, 255);  // 青LED B←→G
  } else {
    /* Rad値(240-360) 青-紫-赤   */
    B = map(Rad, 240, 360, 255, 0);  // 青LED B←→R
  }
  return int(B / Accel);
}

void test2(int Rad, float Accel) {
  while (count < 100) {
    if (Rad > 360) Rad -= 360;
    ledtape.setPixelColor(count, ledtape.Color(int(HSBtoR(int(Rad), Accel)), int(HSBtoG(int(Rad), Accel)), int(HSBtoB(int(Rad), Accel))));
    count++;
    Rad += 360 / 100;
  }
}
void test1() {

  while (count < 100) {

    ledtape.setPixelColor(99 - start, ledtape.Color(tapecolor[99 - count][0], tapecolor[99 - count][1], tapecolor[99 - count][2]));
    //delay(10);
    count++;
    start++;
    if (start > 99) {
      start = 0;
    }
  }
}
