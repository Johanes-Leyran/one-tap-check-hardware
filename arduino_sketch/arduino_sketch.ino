String inputString = ""; //Receiving Input
bool stringComplete = false; //Receiving Input   

const int testPinOutput = 10; //Testing
const int testPinInput = 4; //Testing

const int globalDelay = 500; //if you're going to do delay(), put this in

/**
 * Ran once on startup
 */ 
void setup() {
  pinMode(testPinOutput, OUTPUT); //Testing
  pinMode(testPinInput, INPUT); //Testing
  Serial.begin(9600);
  inputString.reserve(200);      
}

/**
 * Ran continuosly by the arduino
 */
void loop() {
  if (stringComplete) {
    processReceivedString(inputString);
    inputString = "";
    stringComplete = false;
  }
  
  //Example of input
  if (digitalRead(testPinInput) == HIGH){
    sendMessage("Pressed");
    delay(globalDelay);
  }
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
  if (str == "open led") {
      digitalWrite(testPinOutput, HIGH); 
  } 
  else if (str == "close led") {;
      digitalWrite(testPinOutput, LOW);
  } 
}

/**
 * Sends the message to the code and adding starting and end symbols
 * @param text your text
 */
void sendMessage(String text){
  Serial.print("[" + text + "]");
}