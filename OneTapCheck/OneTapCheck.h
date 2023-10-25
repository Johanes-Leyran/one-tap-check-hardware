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

	void printOnRow(int row, String text);
	void clearLCD();
	void clearLCD(int row);

	void renewKey();
	bool checkForCard();
	void stopCardCheck();
	String readDataFromCard();
	void writeDataToCard(byte blockData[]);

	void playAlert(int severity);


private:
	LiquidCrystal_I2C lcd;
	MFRC522 mfrc522;
	MFRC522::MIFARE_Key key;
};

#endif