#define ROOM_UID "3a8a001e-9c5e-4396-859a-70e2c6bea64a"

#include <OneTapCheck.h>

OneTapCheck otc;

bool hasTapped = false; 
bool hasActivated = false;

/**
 * Ran once on startup
 */ 
void setup() {
  otc.setup();
  delay(1000);
  otc.sendMessage(String("setup ") + ROOM_UID);
}

/**
 * Ran continuously by the arduino
 */
void loop() { 
  if (!hasActivated) return;
  if (hasTapped) return;

  /* RFID code */
  otc.renewKey();

  if (!otc.checkForCard()) return;
  String data = otc.readDataFromCard();

  if (data == "02000111222") {
    otc.print(0, "Pinged!");
    otc.sendMessage(String("tap ") + ROOM_UID + String(" ") + "01001682-06ad-42e9-8cfc-c7ee0b692377");
    hasTapped = true;
  } else if (data == "02000654321"){
    otc.print(0, "Pinged!");
    otc.sendMessage(String("tap ") + ROOM_UID + String(" ") + "573f01cd-d122-4284-b50a-549a98b5d7a2");
    hasTapped = true;
  } else {
    //INSTRUCTIONS FOR DISPLAYING NULL 
    otc.print(1, "Invalid!");
    otc.playAlert(false);
    otc.clear();
  }

  otc.stopCardCheck();
}

  static String incoming = "";
/**
 * Serial Event method is ran when there is incoming data from code
 * This processes the data into a string, which turns on stringComplete boolean
 * from "[TextHere]" to "TextHere"
 */
void serialEvent() {
  while (Serial.available()) {
    char inChar = (char) Serial.read();

    if (inChar == '[') {
      incoming = ""; 
    } else if (inChar == ']') {
      processString(incoming);
      break;
    } else {
      incoming += inChar;
    }
  }
}

void processString(const String& inputString){
  if (inputString.startsWith("lcd ")) {
    String command = inputString.substring(4);
    /* clear */
    if (command.startsWith("clear")) {
        otc.clear();
    }

    /* if command starts with 0 to 3 (valid row positions) */
    else if (command.startsWith("0 ") || command.startsWith("1 ") || command.startsWith("2 ") || command.startsWith("3 ")) {
        int place = command[0] - '0';
        otc.print(place, command.substring(2));
    }
  }
  
  if (inputString.startsWith("activate")) {
    String command = inputString.substring(9);
  
    if (command.startsWith("true")){
      hasActivated = true;
      otc.clear();
    } else if (command.startsWith("false")){
      hasActivated = false;
      otc.clear();
      otc.print(1, "  STI  OneTapCheck  ");
      otc.print(2, "   System Offline   ");
    }
  }

  if (inputString.startsWith("tap ")) {
    otc.sendMessage("test");
    String command = inputString.substring(4);

    int space1 = command.indexOf(' '), space2 = command.indexOf(' ', space1 + 1), space3 = command.indexOf(' ', space2 + 1);
    String status = command.substring(0, space1), line1 = command.substring(space1 + 1, space2), line2 = command.substring(space2 + 1, space3), line3 = command.substring(space3 + 1);

    otc.print(1, line1); otc.print(2, line2); otc.print(3, line3);

    if (status.startsWith("true")) otc.playAlert(true);
    else if (status.startsWith("false")) otc.playAlert(false);

    delay(2000); otc.clear();

    hasTapped = false;
  }
}