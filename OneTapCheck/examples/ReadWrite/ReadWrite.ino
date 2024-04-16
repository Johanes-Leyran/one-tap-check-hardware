#include <OneTapCheck.h>

static OneTapCheck otc;
static String writeData = "";    
static boolean doWrite = false;  

void setup() {
  otc.setup();

  otc.print(1, " READ WRITE MODE ON ");
  otc.print(2, "    Writing: OFF    ");

}

void loop() {
  otc.renewKey();

  if (!otc.checkForCard()) return;
  if (doWrite) {
    otc.writeDataToCard(writeData);
    doWrite = false;
    writeData = "";
    otc.print(2, "    Writing: OFF    ");


    delay(1500);
  } else {
    otc.sendMessage(otc.readDataFromCard());
    otc.playAlert(true);
    delay(1000);
    otc.playAlert(false);
  }
  otc.stopCardCheck();
}

void serialEvent() {
  const String str = Serial.readString();

  /* Assigning data (Must be 36 characters long!) */
  if (str.startsWith("data ")) {
    String uuid = str.substring(5, 41);

    if (!otc.isUUID(uuid)) {
      otc.sendMessage("Write: Data was not set. " + uuid); 
      return;
    }

    writeData = uuid;
    otc.sendMessage("Write: Data set to " + writeData);
  }
  
  /* Activate writing data */
  else if (str.startsWith("activate")) {
    //if (writeData == "") otc.sendMessage("Write: Data is empty! Assign a value to the data first before you activate writing!"); return;
    if (doWrite) {
      otc.sendMessage("Write: Writing is already activated! Do \"cancel\" to cancel writing!"); 
      return;
    }

    doWrite = true;
    otc.sendMessage("Write: Writing is now activated. The next card detected will be written \"" + writeData + "\"");
    otc.print(2, "    Writing: ON    ");
  }

  /* Cancel writing data */
  else if (str.startsWith("cancel")) {
    if (!doWrite) otc.sendMessage("Write: Writing is already turned off! Do \"activate\" to activate writing!"); return;

    doWrite = false;
    otc.sendMessage("Write: Writing is now cancelled.");
    otc.print(2, "    Writing: OFF    ");
  }
}