#define numOfTransmitters 4 //jumlah total transmitter
#define dataLength (2+sizeof(float)) //ukuran byte data yang dikirimkan melalui komunikasi RF

#include <Arduino.h>
#include <Wire.h>
#include <SPI.h>
#include <nRF24L01.h>
#include <RF24.h>

#define REFRESH_TIME 10 //periode penerimaan data
#define RF24_CSN 10 //pin yang disambungkan ke pin CSN modul RF24
#define RF24_CE 9 //pin yang disambungkan ke pin CE modul RF24

union adc_signal { //tipe data untuk mendefinisikan nilai adc dari sensor
  unsigned int data; //dinyatakan dalam bentuk integer
  byte raw[2]; //dinyatakan dalam bentuk 2 byte
};

union rate_signal { //tipe data untuk mendefinisikan nilai detak jantung
  float data; //dinyatakan dalam bentuk float
  byte raw[sizeof(float)]; //dinyatakan dalam bentuk sejumlah byte
};

byte data[numOfTransmitters * dataLength]; //seluruh data yang akan dikirim ke pc
const uint64_t RXTXaddress[2] =
{0xF0F0F0F0E1LL /*RX*/, 0xF0F0F0F0D2LL /*TX*/}; //alamat komunikasi RF
uint8_t RXTXchannel = 100; //channel komunikasi RF
unsigned long refreshTimer = millis(); //variabel pencatat waktu
boolean dataAvailable[numOfTransmitters]; //variabel penanda ketersediaan data tiap transmiter

RF24 radio(RF24_CE, RF24_CSN);

void setup(void) { //inisialisasi
  Serial.begin(115200); //komunikasi serial ke pc
  radio.begin();
  radio.setAutoAck(false);
  radio.setPayloadSize(dataLength + 1); //ditambah 1 untuk header
  radio.setPALevel(RF24_PA_MIN);
  radio.setDataRate(RF24_250KBPS);
  radio.setRetries(0, 0);
  radio.setChannel(RXTXchannel);
  radio.openReadingPipe(1, RXTXaddress[0]);
  radio.openWritingPipe(RXTXaddress[1]);
  radio.startListening();
  Serial.println("receiver");
}

void loop(void) {
  if ( radio.available() ) { //sinyal diterima
    byte payload[dataLength + 1];
    /*
     * format data yang diterima:
     * payload 0 = header (nomor urut transmiter)
     * payload 1 dan 2 = adc
     * payload 3 s/d selesai = detak jantung
     */
    radio.read(payload, sizeof(payload)); //baca data
    if (payload[0] <= numOfTransmitters) { //hanya menerima dari transmiter nomor urut 1 s/d numOfTransmitters
      dataAvailable[payload[0] - 1] = true; //menandai bahwa data tersedia
      for (int i = 0; i < dataLength; i++) {
        data[(payload[0] - 1)*dataLength + i] = payload[i + 1];
      } //data diperbarui untuk pengiriman ke pc
    }
  }

  unsigned long timeElapsed = millis() - refreshTimer;
  if (timeElapsed >= REFRESH_TIME) { //delay terpenuhi, saatnya meng-update data
    refreshTimer += timeElapsed; //pencatat waktu diperbarui
    for (int i = 0; ; i++) { //loop pengiriman data tiap transmiter ke pc
      if (dataAvailable[i]) { //data tersedia
        adc_signal as;
        rate_signal rs;
        as.raw[0] = data[i*dataLength];
        as.raw[1] = data[i*dataLength+1];
        for (int j=2; j<dataLength; j++) {
          rs.raw[j-2] = data[i*dataLength+j];
        }
        Serial.print(as.data); //nilai adc
        Serial.print(',');
        Serial.print(rs.data); //nilai detak jantung
      }
      else { //tidak ada data = NaN
        Serial.print("NaN");
        Serial.print(',');
        Serial.print("NaN");
      }
      dataAvailable[i] = false; //reset ketersediaan data untuk penerimaan berikutnya
      if (i == numOfTransmitters - 1) {
        Serial.println();
        break;
      }
      else {
        Serial.print(',');
      }
    }
    byte ping[1];
    ping[0] = 0xAA; //penanda untuk meminta data dari transmiter
    radio.stopListening(); //mulai pengiriman (berhenti menunggu data)
    radio.write(ping, sizeof(ping)); //kirimkan penanda
    radio.startListening(); //pengiriman selesai (mulai menunggu data)
  }
}

