#define ROOM_UID "3a8a001e-9c5e-4396-859a-70e2c6bea64a"

#include <OneTapCheck.h>

static OneTapCheck otc;
static bool hasTapped = false; 
static bool hasActivated = false;

/**
 * Ran once on startup
 */ 
void setup() {
  otc.setup();
  delay(1000);
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

  if (!otc.isUUID(data)) {
    //INSTRUCTIONS FOR DISPLAYING NULL/Invalid
    otc.print(3, "Invalid Card!");
    otc.playAlert(false);
    delay(1000);
    otc.clear(3);
  } else {
    otc.sendMessage("tap " + String(ROOM_UID) + " " + String(data));
  }

  otc.stopCardCheck();
}

  
/**
 * Serial Event method is ran when there is incoming data from code
 * This processes the data into a string, which turns on stringComplete boolean
 * from "[TextHere]" to "TextHere"
 */
const String incoming = "";
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

    if (command.startsWith("0 ") || command.startsWith("1 ") || command.startsWith("2 ") || command.startsWith("3 ")) {
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
    const String* command = otc.splitString(inputString.substring(4), '/');

    otc.print(2, command[0]);
    otc.print(3, command[1]);
    otc.playAlert(command[2].startsWith("true"));

    delay(1000);
    otc.clear(2); 
    otc.clear(3); 

    hasTapped = false;
  }

  if (inputString.startsWith("setup")){
    otc.sendMessage(String("setup ") + ROOM_UID);
  }

  if (inputString.startsWith("time ")){
    otc.print(0, inputString.substring(5));
  }
}