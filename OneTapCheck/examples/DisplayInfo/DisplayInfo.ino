/*
 * ONE TAP CHECK: RFID CLASSROOM ATTENDANCE MANAGEMENT SYSTEM FOR SENIOR HIGH SCHOOL STUDENT OF STI COLLEGE MUNOZ-EDSA
 * Submitted to the Faculty of STI  College Munoz-Edsa
 * In Partial Fulfillment of the Requirements for the Senior High School Information Technology in Mobile App and Web Development
 * 
 * Things needed
 *  - (1) Arduino UNO
 *  - (1) 20x4 LCD w/ I2C
 *  - (1) RFID MFRC522 Reader
 *  - (1) Buzzer
 *  - (1) Green LED
 *  - (1) Red LED
 *  - (2) Resistor (for LED)
 * 
 * Connections of pins are below
 *
 * LCD I2C:
 * ----------------
 * GND  -  GND
 * VCC  -  5V
 * SDA  -  A4
 * SCL  -  A5
 *
 * RFID MFRC522:
 * ----------------
 * SDA  -  10
 * SCK  -  13
 * MOSI -  11
 * MISO -  12
 * IRQ  -  not connected
 * GND  -  GND
 * RST  -  9
 * 3.3V -  3.3V
 * 
 * Miscellaneous: (LED must have a resistor in series!)
 * ----------------
 * Green  - 5
 * Red    - 6
 * Buzzer - 7
 */

#include <OneTapCheck.h>

OneTapCheck otc;

String inputString = "";      /* Receiving Input from code */
bool stringComplete = false;  /* Receiving Input from code */


/**
 * Ran once on startup
 */ 
void setup() {
  otc.setup();
  inputString.reserve(200);
  digitalWrite(5, HIGH);
  delay(1000);
  digitalWrite(5, LOW);
}

/**
 * Ran continuously by the arduino
 */
void loop() {
  /* Input from Serial */
  if (stringComplete) {
    processReceivedString(inputString);
    inputString = "";
    stringComplete = false;
  }

  /* RFID code */
  otc.renewKey();

  if (!otc.checkForCard()) return;
  String data = otc.readDataFromCard();
  otc.print(0, data);

  if (data == "02000111222"){
    otc.print(1, "John Doe");
    otc.print(2, "Welcome!");
    otc.playAlert(true);
  } else if (data == "02000654321"){
    otc.print(1, "Juan Dela Cruz");
    otc.print(2, "Welcome!");
    otc.playAlert(true);
  } else {
    otc.print(1, "Unknown");
    otc.print(2, "Access Denied!");
    otc.playAlert(false);
  }

  delay(1500);
  otc.clear();
  otc.stopCardCheck();
}

/**
 * Serial Event method is ran when there is incoming data from code
 * This processes the data into a string, which turns on stringComplete boolean
 * from "[TextHere]" to "TextHere"
 */
void serialEvent() {
  while (Serial.available()) {
    char inChar = (char) Serial.read();

    if (inChar == '[') {
      inputString = ""; 
    } else if (inChar == ']') {
      stringComplete = true;
      break;
    } else {
      inputString += inChar;
    }
  }
}

/**
 * Processes the string received from code.
 * This is where you call methods
 * @param str the string
 */
void processReceivedString(const String& str) {
    if (str.startsWith("lcd ")) {
        String command = str.substring(4);

        /* clear */
        if (command.startsWith("clear")) {
            otc.clear();
        }

        /* if command starts with 0 to 3 (valid row positions) */
        else if (command.startsWith("0 ") || command.startsWith("1 ") || command.startsWith("2 ") || command.startsWith("3 ")) {
            int place = command[0] - '0';
            otc.print(place, "                    ");
            otc.print(place, command.substring(2));
        }
    }
}