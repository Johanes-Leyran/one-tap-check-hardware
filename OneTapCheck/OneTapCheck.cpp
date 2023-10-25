#include "OneTapCheck.h"

OneTapCheck::OneTapCheck():
    lcd(0x27, 20, 4), 
    mfrc522(10, 9),
    key()
{}

/**
 * Sets up everything needed
 * Execute this in the setup function of Arduino
 */
void OneTapCheck::setup() {
    Serial.begin(9600);

	lcd.init();
	lcd.backlight();
    SPI.begin();
    mfrc522.PCD_Init();

    pinMode(5, OUTPUT); //GREEN
    pinMode(6, OUTPUT); //RED
    pinMode(7, OUTPUT); //BUZZER
}

/**
 * Sends the message to the code and adding starting and end symbols
 * @param text your text
 */
void OneTapCheck::sendMessage(String text) {
    Serial.println("[" + text + "]");
}

/**
 * Print a text on the LCD
 */
void OneTapCheck::printOnRow(int row, String text) {
    if (row > 3 || row < 0) return;
    lcd.setCursor(0, row);
    lcd.print(text);
}

/**
 * Clears the LCD
 */
void OneTapCheck::clearLCD() {
    lcd.clear();
}

/**
 * Clears a specific row
 */
void OneTapCheck::clearLCD(int row) {
    lcd.setCursor(0, row);
    lcd.print("                    ");
}

/**
 * For card scanning
 */
void OneTapCheck::renewKey() {
    for (byte i = 0; i < 6; i++) key.keyByte[i] = 0xFF;
}

/**
 * Sends true if card is detected
 */
bool OneTapCheck::checkForCard() {
    return !(!mfrc522.PICC_IsNewCardPresent() || !mfrc522.PICC_ReadCardSerial());
}

/**
 * Stops card checking and allows the next card to be scanned
 * Note: If card is still on scanner when card checking is stopped, it won't register as a new card until card is put away
 */
void OneTapCheck::stopCardCheck() {
    mfrc522.PICC_HaltA();
    mfrc522.PCD_StopCrypto1();
}

/**
 * Reads the data from card
 * This will only return the data written to card using writeDataToCard()
 * Returns the string from card
 */
String OneTapCheck::readDataFromCard() {

    byte bufferLen = 26;
    byte readBlockData[26];
    byte status = mfrc522.PCD_Authenticate(MFRC522::PICC_CMD_MF_AUTH_KEY_A, 2, &key, &(mfrc522.uid));

    if (status != MFRC522::STATUS_OK) return "null";


    status = mfrc522.MIFARE_Read(2, readBlockData, &bufferLen);
    if (status != MFRC522::STATUS_OK) return "null";


    char str[(sizeof readBlockData) + 1];
    memcpy(str, readBlockData, sizeof readBlockData);
    str[sizeof readBlockData] = 0;

    if (str[0] == '\0') return "null";
    return str;
}

/**
 * Writes the string given to the next card that is detected
 */
void OneTapCheck::writeDataToCard(byte blockData[]) {
    MFRC522::StatusCode status;

    status = mfrc522.PCD_Authenticate(MFRC522::PICC_CMD_MF_AUTH_KEY_A, 2, &key, &(mfrc522.uid));
    if (status != MFRC522::STATUS_OK) {
        sendMessage("Write: Failed to authenticate");
        sendMessage(mfrc522.GetStatusCodeName(status));
        return;
    }

    status = mfrc522.MIFARE_Write(2, blockData, 16);
    if (status != MFRC522::STATUS_OK) {
        sendMessage("Write: Failed to write data");
        sendMessage(mfrc522.GetStatusCodeName(status));
        return;
    } else {
        sendMessage("Write: Data written successfully");
    }
}

/**
 * Lights up the LED and plays a sound on the buzzer appropriately to the severity given
 * 0 = Green + Success sound
 * 1 = Red + Error sound
 * 2 = Blinking Red + Severe Error Sound
 */
void OneTapCheck::playAlert(int severity) {
    if (severity == 0) {
        digitalWrite(5, HIGH);
        tone(7, 784, 250);
        delay(1000);
        digitalWrite(5, LOW);
    }
    
    if (severity == 1) {
        digitalWrite(6, HIGH);
        tone(7, 349, 1000);
        delay(1000);
        digitalWrite(6, LOW);
    }

    if (severity == 2) {
        for (int i = 0; i < 3; i++) {
            digitalWrite(6, HIGH);
            tone(7, 165, 500);
            delay(500);
            digitalWrite(6, LOW);
            delay(500);
        }
    }

}

