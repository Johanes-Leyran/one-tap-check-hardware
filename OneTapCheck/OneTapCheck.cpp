#include "OneTapCheck.h"

OneTapCheck::OneTapCheck():
    lcd(0x27, 20, 4), 
    mfrc522(10, 9),
    key()
{}

/*
 * Sets up everything needed
 * Execute this in the setup function of Arduino
 */
void OneTapCheck::setup() {
    Serial.begin(9600);

	lcd.init();
	lcd.backlight();
    SPI.begin();
    mfrc522.PCD_Init();
    pinMode(8, OUTPUT);
    print(1, "  STI  OneTapCheck  ");
    print(2, "   System Offline   ");
}

/*
 * Sends the message to the code and adding starting and end symbols
 * @param text your text
 */
void OneTapCheck::sendMessage(String text) {
    Serial.println("[" + text + "]");
}

/*
 * Print a text on the LCD
 */
void OneTapCheck::print(int row, String text) {
    if (row > 3 || row < 0) return;
    clear(row);
    lcd.setCursor(0, row);
    lcd.print(text);
}

/*
 * Clears the LCD
 */
void OneTapCheck::clear() {
    lcd.clear();
}

/*
 * Clears a specific row
 */
void OneTapCheck::clear(int row) {
    lcd.setCursor(0, row);
    lcd.print("                    ");
}

/*
 * For card scanning
 */
void OneTapCheck::renewKey() {
    for (byte i = 0; i < 6; i++) key.keyByte[i] = 0xFF;
}

/*
 * Sends true if card is detected
 */
bool OneTapCheck::checkForCard() {
    return !(!mfrc522.PICC_IsNewCardPresent() || !mfrc522.PICC_ReadCardSerial());
}

/*
 * Stops card checking and allows the next card to be scanned
 * Note: If card is still on scanner when card checking is stopped, it won't register as a new card until card is put away
 */
void OneTapCheck::stopCardCheck() {
    mfrc522.PICC_HaltA();
    mfrc522.PCD_StopCrypto1();
}

bool OneTapCheck::isUUID(const String& str) {
    return str.length() == 39;
}

/*
 * Reads the data from card
 * This will only return the data written to card using writeDataToCard()
 * Returns the string from card
 */
String OneTapCheck::readDataFromCard() {
    int blockOne = 1, blockTwo = 5, blockThree = 9, bufferSize = 28;

    byte readOne[bufferSize];
    byte readTwo[bufferSize];
    byte readThree[bufferSize];

    MFRC522::StatusCode status;
    status = mfrc522.PCD_Authenticate(MFRC522::PICC_CMD_MF_AUTH_KEY_A, blockOne, &key, &(mfrc522.uid));
    if (status != MFRC522::STATUS_OK) { sendMessage("1"); return ""; }
    status = mfrc522.MIFARE_Read(blockOne, readOne, (byte) bufferSize);
    if (status != MFRC522::STATUS_OK) { sendMessage(mfrc522.GetStatusCodeName(status)); return ""; }

    status = mfrc522.PCD_Authenticate(MFRC522::PICC_CMD_MF_AUTH_KEY_A, blockTwo, &key, &(mfrc522.uid));
    if (status != MFRC522::STATUS_OK) { sendMessage("3"); return ""; }
    status = mfrc522.MIFARE_Read(blockTwo, readTwo, (byte) bufferSize);
    if (status != MFRC522::STATUS_OK) { sendMessage("4"); return ""; }

    status = mfrc522.PCD_Authenticate(MFRC522::PICC_CMD_MF_AUTH_KEY_A, blockThree, &key, &(mfrc522.uid));
    if (status != MFRC522::STATUS_OK) { sendMessage("5"); return ""; }
    status = mfrc522.MIFARE_Read(blockThree, readThree, (byte) bufferSize);
    if (status != MFRC522::STATUS_OK) { sendMessage("6"); return ""; }

    if (readOne[0] == '\0' || readTwo[0] == '\0' || readThree[0] == '\0') return "";

    String finalString = "";
    for (int i = 0; i < 39; i++) {
        if (i < 16) {
            finalString += (char) readOne[i];
        } else if (i < 32) {
            finalString += (char) readTwo[i - 16];
        } else if (i < 39) {
            finalString += (char) readThree[i - 32];
        }
    }

    return finalString;
}

/*
*Writes the string given to the next card that is detected
*/
bool OneTapCheck::writeDataToCard(String data) {
    int blockOne = 1, blockTwo = 5, blockThree = 9;
    byte dataOne[16];
    byte dataTwo[16];
    byte dataThree[16] = {"                "};

    for (int i = 0; i < data.length(); i++) {
        if (i < 16) {
            dataOne[i] = data.charAt(i);
        }
        else if (i < 32) {
            dataTwo[i - 16] = data.charAt(i);
        }
        else if (i < 39) {
            dataThree[i - 32] = data.charAt(i);
        }
    }

    MFRC522::StatusCode status;
    status = mfrc522.PCD_Authenticate(MFRC522::PICC_CMD_MF_AUTH_KEY_A, blockOne, &key, &(mfrc522.uid));
    if (status != MFRC522::STATUS_OK) { sendMessage("Auth 1 Fail"); return false; }
    status = mfrc522.MIFARE_Write(blockOne, dataOne, 16);
    if (status != MFRC522::STATUS_OK) { sendMessage("Assign 1 Fail"); return false; }

    status = mfrc522.PCD_Authenticate(MFRC522::PICC_CMD_MF_AUTH_KEY_A, blockTwo, &key, &(mfrc522.uid));
    if (status != MFRC522::STATUS_OK) { sendMessage("Auth 2 Fail"); return false; }
    status = mfrc522.MIFARE_Write(blockTwo, dataTwo, 16);
    if (status != MFRC522::STATUS_OK) { sendMessage("Assign 2 Fail"); return false; }

    status = mfrc522.PCD_Authenticate(MFRC522::PICC_CMD_MF_AUTH_KEY_A, blockThree, &key, &(mfrc522.uid));
    if (status != MFRC522::STATUS_OK) { sendMessage("Auth 3 Fail"); return false; }
    status = mfrc522.MIFARE_Write(blockThree, dataThree, 16);
    if (status != MFRC522::STATUS_OK) { sendMessage("Assign 3 Fail"); return false; }

    sendMessage("Write: Successfully written data!");
    return true;
}

/*
 * Lights up the LED and plays a sound on the buzzer depending on the boolean
 * WARNING: Adds delay, use this AFTER important methods
 * true = Green + Success sound
 * false = Red + Error sound
 */
void OneTapCheck::playAlert(bool success) {
    if (success) {
        tone(8, 784, 250);
    }
    else {
        tone(8, 349, 1000);
    }
}