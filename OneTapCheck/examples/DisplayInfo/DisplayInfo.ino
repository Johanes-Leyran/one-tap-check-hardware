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

String writeData = "";    /* Writing data to card */
boolean doWrite = false;  /* Writing data to card */


/**
 * Ran once on startup
 */ 
void setup() {
  otc.setup();
  inputString.reserve(200);
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
  otc.printOnRow(0, data);

  if (data == "02000111222"){
    otc.printOnRow(1, "John Doe");
    otc.printOnRow(2, "Welcome!");
  } else if (data == "02000654321"){
    otc.printOnRow(1, "Juan Dela Cruz");
    otc.printOnRow(2, "Welcome!");
  } else {
    otc.printOnRow(1, "Unknown");
    otc.printOnRow(2, "Access Denied!");
  }

  if (data.startsWith("null")) otc.playAlert(1);
  else otc.playAlert(0);
  
  if (doWrite){
    byte blockData[11];
    for (int i = 0; i < writeData.length() && i < 11; i++){
      blockData[i] = writeData.charAt(i); 
    }
    otc.writeDataToCard(blockData);
    doWrite = false;
  }

  delay(1500);
  otc.clearLCD();
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
            otc.clearLCD();
        }

        /* if command starts with 0 to 3 (valid row positions) */
        else if (command.startsWith("0 ") || command.startsWith("1 ") || command.startsWith("2 ") || command.startsWith("3 ")) {
            int place = command[0] - '0';
            otc.printOnRow(place, "                    ");
            otc.printOnRow(place, command.substring(2));
        }
    }

    if (str.startsWith("write ")) {
        String command = str.substring(6);

        /* Assigning data (Must be 11 characters long!) */
        if (command.startsWith("data ")){
          writeData = command.substring(5);

          if (writeData.length() != 16){
            otc.sendMessage("Write: Data was not set, length of given string should be 11 characters total.");
            return;
          }

          otc.sendMessage("Write: Data set to " + writeData);
        }
        
        /* Activate writing data */
        else if (command.startsWith("activate")){

          if (writeData == ""){
            otc.sendMessage("Write: Data is empty! Assign a value to the data first before you activate writing!");
            return;
          }
          
          if (doWrite){
            otc.sendMessage("Write: Writing is already activated! Do \"write cancel\" to cancel writing!");
            return;
          }

          doWrite = true;
          otc.sendMessage("Write: Writing is now activated. The next card detected will be written \"" + writeData + "\"");
        }

        /* Cancel writing data */
        else if (command.startsWith("cancel")){
          if (!doWrite){
            otc.sendMessage("Write: Writing is already turned off! Do \"write activate\" to activate writing!");
            return;
          }

          doWrite = false;
          otc.sendMessage("Write: Writing is now cancelled.");
        }
    }
}