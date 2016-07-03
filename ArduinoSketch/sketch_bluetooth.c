/**
 * Autor: Pedro Simão
 * Sketch de exemplo para conexão Bluetooth com Android | GDG Cabreúva | Junho de 2016
 * Referencias: https://www.youtube.com/watch?v=ZejQOX69K5M e https://www.youtube.com/watch?v=sXs7S048eIo
 */

/** PINs **/
const int pinLED = 13;
const int pinHCSR04Trig = 8;
const int pinHCSR04Echo = 9;

/** HCSR04 **/
long duration;
int distance;

/** Bluetooth **/
int state = 0;
int flag = 1;   // 1 - Printar status do LED; 0 - Não printar.

void setup() {
  // Modo em que os PINs irão operar
  pinMode(pinLED, OUTPUT);
  pinMode(pinHCSR04Trig, OUTPUT);
  pinMode(pinHCSR04Echo, INPUT);
  digitalWrite(pinLED, LOW);  // Inicia com o LED desligado
  Serial.begin(9600);         // Inicia leitura serial com 9600
}

void loop() {
  if(Serial.available() > 0){   // Caso a porta serial esteja disponível
    state = Serial.read();      // Atribui valor lido a variável state
    flag = 1;
  }
  switch(state){
    case '0':
      turnLedOff();
      break;
    case '1':
      turnLedOn();
      break;
    case '2':
      readHCSR04();
      break;      
  }
  state = 0;
}

void turnLedOn(){
  digitalWrite(pinLED, HIGH);
  if(flag == 1){
    Serial.println("LED On");
    flag = 0;
  }
}

void turnLedOff(){
  digitalWrite(pinLED, LOW);
  if(flag == 1){
    Serial.println("LED Off");
    flag = 0;
  }
}

void readHCSR04(){
  if(flag == 1){
    // Desliga Trigger para garantir que está limpo (2 microsegundos são o suficiente)
    digitalWrite(pinHCSR04Trig, LOW);
    delayMicroseconds(2);
    // Envia onda sonora pelo trigger e aguarda 10 microsegundos
    digitalWrite(pinHCSR04Trig, HIGH);
    delayMicroseconds(10);
    digitalWrite(pinHCSR04Trig, LOW);
    // Pega quantidade de pulsos que o HCSR04 armazenou enquanto esperava pelo retorno da onda sonora
    duration = pulseIn(pinHCSR04Echo, HIGH);
    // Calcula distancia de acordo com pulsos armazenados relacionado a velocidade do som
    distance = duration * 0.034 / 2;
    Serial.print("Distance: ");
    Serial.print(distance);
    Serial.println("cm");
    flag = 0;
  }
}
