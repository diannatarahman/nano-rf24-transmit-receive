#define IBI_MAX 6000000L

volatile unsigned int lastSignals[SMOOTH_FACTOR][SMOOTH_SIZE];
volatile unsigned long rate[IBI_TRACK];
volatile unsigned int lastSmoothSignal;

void interruptSetup(){
  TCCR2A = 0x02;
  TCCR2B = 0x06;
  OCR2A = 0X7C;
  TIMSK2 = 0x02;
  P = analogRead(pulsePin);
  T = P;
  lastSmoothSignal = P;
  for (int i=0; i<SMOOTH_FACTOR; i++) {
    for(int j=0; j<SMOOTH_SIZE; j++){
      lastSignals[i][j] = P;
    }
  }
  sei();     
} 


ISR(TIMER2_COMPA_vect){ //fungsi interrupt, membaca sensor secara periodik
  cli();
  Signal = analogRead(pulsePin);
  if (BPM > 0 || pCounter < IBI_MAX) {
    pCounter += 2;
  }
  hpCounter += 2;
  
  lastSmoothSignal = smoothSignal;

  for (int i=0, s=Signal; i<SMOOTH_FACTOR; i++, s=smoothSignal) {
    word totalSignal = 0;
    for(int j=0; j<SMOOTH_SIZE-1; j++){
      lastSignals[i][j] = lastSignals[i][j+1];
      totalSignal += lastSignals[i][j];
    }
    lastSignals[i][SMOOTH_SIZE-1] = s;
    totalSignal += s;
    smoothSignal = totalSignal/SMOOTH_SIZE;
  }

  if (smoothSignal > lastSmoothSignal) {
    if (!increasing) {
      increasing = true;
      if (hpCounter > 50 && hpCounter <= 500 &&
        P-T > 150 && pCounter > 3*hpCounter) {
          calculateBPM();
          pCounter = 0;
        }
      hpCounter = 0;
      P = smoothSignal;
      T = smoothSignal;
    }
    else {
      if (T > smoothSignal) {
        T = smoothSignal;
      }
      if (P < smoothSignal) {
        P = smoothSignal;
      }
    }
  }
  else if (smoothSignal < lastSmoothSignal) {
    if (increasing) {
      increasing = false;
      hpCounter = 0;
      P = smoothSignal;
      T = smoothSignal;
    }
    else {
      if (T > smoothSignal) {
        T = smoothSignal;
      }
      if (P < smoothSignal) {
        P = smoothSignal;
      }
    }
  }

  if (pCounter > 1000 && pCounter - IBI > 250 && BPM > 0) {
    calculateBPM();
  }
  
  sei();
}

void calculateBPM() {
  IBI = pCounter;

  unsigned long runningTotal = 0;   

  for(int i=0; i<IBI_TRACK-1; i++){
    rate[i] = rate[i+1];
    runningTotal += rate[i];
  }
  rate[IBI_TRACK-1] = IBI;
  
  if (IBI == IBI_MAX) {
    BPM = 0;
    return;
  }
  runningTotal += rate[IBI_TRACK-1];
  BPM = 60000.0F*IBI_TRACK/runningTotal;
}



