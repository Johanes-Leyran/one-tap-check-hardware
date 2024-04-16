#ifndef ONETAPCHECK_H
#define ONETAPCHECK_H

#include <Arduino.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <SPI.h>
#include <MFRC522.h>

class OneTapCheck {
public:
	OneTapCheck();
	void setup();

	void sendMessage(String text);

	void print(int row, String text);
	void clear();
	void clear(int row);

	void renewKey();
	bool checkForCard();
	void stopCardCheck();
	bool isUUID(const String& str);
	String readDataFromCard();
	bool writeDataToCard(String data);

	void playAlert(bool success);


private:
	LiquidCrystal_I2C lcd;
	MFRC522 mfrc522;
	MFRC522::MIFARE_Key key;
};

#endif