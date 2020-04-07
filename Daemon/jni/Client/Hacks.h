#ifndef HACKS_H
#define HACKS_H

#include "kmods.h"

bool isValidPlayer(PlayerData data){
    return (data.Location != Vector2::Zero() && data.HeadLocation != Vector2::Zero() && data.PlayerName.size() > 0);
}

bool isValidItem(VehicleData data){
    return (data.Location != Vector2::Zero() && data.Name.size() > 0);
}

bool isValidItem(ItemData data){
    return (data.Location != Vector2::Zero() && data.Name.size() > 0);
}

int isOutsideSafezone(Vector2 pos, Vector2 screen) {
    Vector2 mSafezoneTopLeft(screen.x * 0.04f, screen.y * 0.04f);
    Vector2 mSafezoneBottomRight(screen.x * 0.96f, screen.y * 0.96f);

    int result = 0;
    if (pos.y < mSafezoneTopLeft.y) {
        result |= 1;
    }
    if (pos.x > mSafezoneBottomRight.x) {
        result |= 2;
    }
    if (pos.y > mSafezoneBottomRight.y) {
        result |= 4;
    }
    if (pos.x < mSafezoneTopLeft.x) {
        result |= 8;
    }
    return result;
}

Vector2 pushToScreenBorder(Vector2 Pos, Vector2 screen, int borders, int offset) {
    int x = (int)Pos.x;
    int y = (int)Pos.y;
    if ((borders & 1) == 1) {
        y = 0 - offset;
    }
    if ((borders & 2) == 2) {
        x = (int)screen.x + offset;
    }
    if ((borders & 4) == 4) {
        y = (int)screen.y + offset;
    }
    if ((borders & 8) == 8) {
        x = 0 - offset;
    }
    return Vector2(x, y);
}

void DrawESP(ESP esp, int screenWidth, int screenHeight) {
    if(isESP){
        esp.DrawCrosshair(Color(0, 0, 0, 255), Vector2(screenWidth / 2, screenHeight / 2), 42);

        Vector2 screen(screenWidth, screenHeight);
        float mScale = screenHeight / (float) 1080;

        Response response = getData(screenWidth, screenHeight);
        if(response.Success){
            int count = response.PlayerCount;
            if(count > 0){
                PlayerData localPlayer = response.Players[0];

                for(int i=1; i < count; i++){
                    PlayerData player = response.Players[i];
                    if(!isValidPlayer(player)){ continue; }

                    bool isTeamMate = player.TeamID == localPlayer.TeamID;
                    if(isTeamMate && !isTeamMateShow){ continue; }

                    Vector2 location = player.Location;

                    std::string dist;
                    dist += "[ ";
                    dist += std::to_string((int) player.Distance);
                    dist += "M";
                    dist += " ]";

                    int borders = isOutsideSafezone(location, screen);
                    if(isPlayer360 && borders != 0){
                        Vector2 hintDotRenderPos = pushToScreenBorder(location, screen, borders, (int)((mScale * 100) / 3));
                        Vector2 hintTextRenderPos = pushToScreenBorder(location, screen, borders, -(int)((mScale * 36)));
                        esp.DrawFilledCircle((isTeamMate ? Color(0,255,0,128) : Color(255,0,0,128)), hintDotRenderPos, (mScale * 100));
                        esp.DrawText(Color::White(), dist.c_str(), hintTextRenderPos, playerTextSize);
                    }
                    else {
                        Vector2 End = player.HeadLocation;
                        float boxHeight = (screenWidth / player.Distance);
                        float boxWidth = (screenHeight / player.Distance);
                        Rect PlayerRect(End.x - (boxWidth / 2), End.y, boxWidth, boxHeight);

                        if(isPlayerLine){
                            esp.DrawLine(Color::White(), 1, Vector2((screenWidth / 2), 0), player.HeadLocation);
                        }
                        if(isPlayerBox){
                            esp.DrawBox(Color::White(), 1, PlayerRect);
                        }

                        if(isPlayerName) {
                            esp.DrawText((isTeamMate ? Color::Green() : Color::White()),
                                         player.PlayerName.c_str(),
                                         Vector2(PlayerRect.x + (PlayerRect.width / 2), PlayerRect.y - 12.0f),
                                         playerTextSize);
                        }
                        if(isPlayerDist) {
                            esp.DrawText(Color::White(), dist.c_str(),
                                         Vector2(PlayerRect.x + (PlayerRect.width / 2),
                                                 PlayerRect.y + PlayerRect.height +
                                                 12.5f), playerTextSize);
                        }
                        if(isPlayerHealth) {
                            esp.DrawHorizontalHealthBar(
                                    Vector2(PlayerRect.x - (35 * mScale), PlayerRect.y - (isPlayerName ? 22.0f : 12.0f)),
                                    (80 * mScale),
                                    100, player.Health);
                        }
                    }
                }
            }

            count = response.VehicleCount;
            if(count > 0){
                for(int i=0; i < count; i++){
                    VehicleData vehicle = response.Vehicles[i];
                    if(!isValidItem(vehicle)){ continue;}

                    Vector2 location = vehicle.Location;

                    std::string dist;
                    dist += "[ ";
                    dist += std::to_string((int) vehicle.Distance);
                    dist += "M";
                    dist += " ]";

                    if(isVehicleName) {
                        esp.DrawText(Color::White(), vehicle.Name.c_str(), Vector2(
                                location.x, location.y - (30 * mScale)), vehicleTextSize);
                    }
                    if(isVehicleDist) {
                        esp.DrawText(Color::White(), dist.c_str(), Vector2(
                                location.x, location.y - (10 * mScale)), vehicleTextSize);
                    }
                }
            }

            count = response.ItemsCount;
            if(count > 0){
                for(int i=0; i < count; i++){
                    ItemData item = response.Items[i];
                    if(!isValidItem(item)){ continue;}

                    Vector2 location = item.Location;

                    std::string dist;
                    dist += "[ ";
                    dist += std::to_string((int) item.Distance);
                    dist += "M";
                    dist += " ]";

                    if(isLootBoxName) {
                        esp.DrawText(Color::White(), item.Name.c_str(), Vector2(
                                location.x, location.y - (30 * mScale)), lootTextSize);
                    }
                    if(isLootBoxDist) {
                        esp.DrawText(Color::White(), dist.c_str(), Vector2(
                                location.x, location.y - (10 * mScale)), lootTextSize);
                    }
                }
            }
        }
    }
}

#endif //HACKS_H
