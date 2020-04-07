#ifndef KMODS_H
#define KMODS_H

#include <jni.h>
#include <string>
#include <cstdlib>
#include <unistd.h>
#include <sys/mman.h>
#include <android/log.h>
#include <StructsCommon.h>
#include "Log.h"

bool isESP = false;
//Player
bool isPlayerName = false;
bool isPlayerDist = false;
bool isPlayerHealth = false;
bool isTeamMateShow = false;
bool isPlayerBox = false;
bool isPlayerLine = false;
bool isPlayer360 = false;
float playerTextSize = 4;
//Vehicle
bool isVehicleName = false;
bool isVehicleDist = false;
float vehicleTextSize = 4;
//LootBox
bool isLootBoxName = false;
bool isLootBoxDist = false;
float lootTextSize = 4;

void startDaemon();
int startClient();
bool isConnected();
void stopClient();
bool initServer();
bool stopServer();
Response getData(int screenWidth, int screenHeight);

#endif
