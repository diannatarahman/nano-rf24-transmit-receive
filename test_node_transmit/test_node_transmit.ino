#define thisTransmitter 3 //nomor urut transmiter ini
#define dataLength (2+sizeof(float)) //ukuran byte data yang dikirimkan melalui komunikasi RF

#include <Arduino.h>
#include <Wire.h>
#include <SPI.h>
#include <nRF24L01.h>
#include <RF24.h>

#define REFRESH_TIME 2 //periode pembacaan nilai adc sensor
#define RF24_CSN 10 //pin yang disambungkan ke pin CSN modul RF24
#define RF24_CE 9 //pin yang disambungkan ke pin CE modul RF24
#define pulsePin 0
#define enableSerial true
#define SMOOTH_SIZE 32
#define SMOOTH_FACTOR 2
#define IBI_TRACK 8

union adc_signal { //tipe data untuk mendefinisikan nilai adc dari sensor
  unsigned int data; //dinyatakan dalam bentuk integer
  byte raw[2]; //dinyatakan dalam bentuk 2 byte
};

union rate_signal { //tipe data untuk mendefinisikan nilai detak jantung
  float data; //dinyatakan dalam bentuk float
  byte raw[sizeof(float)]; //dinyatakan dalam bentuk sejumlah byte
};

const uint64_t RXTXaddress[2] =
{0xF0F0F0F0E1LL /*TX*/, 0xF0F0F0F0D2LL /*RX*/}; //alamat komunikasi RF
uint8_t RXTXchannel = 100; //channel komunikasi RF
unsigned long refreshTimer = millis(); //variabel pencatat waktu
volatile float BPM;
volatile unsigned int Signal;
volatile unsigned int smoothSignal;
volatile unsigned long IBI = 6000000L;
volatile unsigned int P;
volatile unsigned int T;
volatile unsigned long pCounter = 0;
volatile unsigned long hpCounter = 0;
volatile boolean increasing = true;
boolean sendData;
unsigned long sendingTime;

RF24 radio(RF24_CE, RF24_CSN);

void setup(void) { //inisialisasi
  if (enableSerial)
    Serial.begin(115200); //komunikasi serial ke pc
  interruptSetup();
  radio.begin();
  radio.setAutoAck(false);
  radio.setPayloadSize(dataLength + 1); //ditambah 1 untuk header
  radio.setPALevel(RF24_PA_MIN);
  radio.setDataRate(RF24_250KBPS);
  radio.setRetries(0, 0);
  radio.setChannel(RXTXchannel); // set the channel
  radio.openWritingPipe(RXTXaddress[0]);
  radio.openReadingPipe(1, RXTXaddress[1]);
  radio.startListening();
  Serial.print("transmitter ");
  Serial.println(thisTransmitter);
}

void loop(void) {
  if ( radio.available() ) { //sinyal diterima dari receiver
    byte ping[1];
    radio.read( ping, sizeof(ping) ); //baca penanda dari receiver
    if (ping[0] == 0xAA) { //receiver meminta data
      sendData = true;
      sendingTime = millis() + (thisTransmitter % 8); //delay yang berbeda untuk setiap transmiter
    } //data akan dikirimkan setelah delay sebesar sendingTime
  }

  unsigned long now = millis();
  if (sendData && now >= sendingTime) { //saatnya mengirim data
    sendData = false;
    adc_signal as;
    rate_signal rs;
    as.data = smoothSignal; //nilai adc
    rs.data = BPM; //nilai detak jantung
    byte payload[dataLength + 1];
    payload[0] = thisTransmitter; //header
    payload[1] = as.raw[0];
    payload[2] = as.raw[1];
    for (int i=3; i<=dataLength; i++) {
      payload[i] = rs.raw[i-3];
    }
    radio.stopListening(); //mulai pengiriman (berhenti menunggu data)
    radio.write(payload, sizeof(payload)); //kirimkan data
    radio.startListening(); //pengiriman selesai (mulai menunggu data)
  }
  if (now - refreshTimer >= REFRESH_TIME) { //tampilkan data ke pc (jika dibutuhkan)
    refreshTimer = now;
    Serial.print(Signal);
    Serial.print(',');
    Serial.print(smoothSignal);
    Serial.print(',');
    Serial.print(pCounter);
    Serial.print(',');
    Serial.println(BPM);
  }
}
