#include "kmods.h"
#include "Socket/SocketServer.h"
#include "StructsPUBG.h"
#include "FNames.h"

using namespace std;

SocketServer server;

#define maxplayerCount 30
#define maxvehicleCount 20
#define maxitemsCount 20

enum Mode {
	InitMode = 1,
	ESPMode = 2,
	HackMode = 3,
	StopMode = 4,
};

struct Request {
	int Mode;
	int ScreenWidth;
	int ScreenHeight;
};

struct PlayerData {
	string PlayerName;
	int TeamID;
	float Health;
	float Distance;
	Vector2 Location;
	Vector2 HeadLocation;
};

struct VehicleData {
	string Name;
	float Distance;
	Vector2 Location;
};

struct ItemData {
	string Name;
	float Distance;
	Vector2 Location;
};

struct Response {
	bool Success;
	int PlayerCount;
	int VehicleCount;
	int ItemsCount;
	PlayerData Players[maxplayerCount];
	VehicleData Vehicles[maxvehicleCount];
	ItemData Items[maxitemsCount];
};

#define VehicleNames 15
static const char* vehicles[] = {
		"Buggy",
		"Dacia",
		"Tuk",
		"Rony",
		"Mirado",
		"UAZ",
		"MiniBus",
		"PickUp",
		"Scooter",
		"Motorcycle",
		"Snowmobile",
		"MotorcycleCart",
		"PG117",
		"AquaRail",
		"AirDropPlane"
};

string FindVehicleName(string str) {
	for (int i = 0; i < VehicleNames; i++) {
		if (isContain(str, vehicles[i])) {
			return vehicles[i];
		}
	}
	return "Vehicle";
}

void createDataList(Response& response) {
	response.PlayerCount = 0;
	response.VehicleCount = 0;
	response.ItemsCount = 0;

	MinimalViewInfo POV = MinimalViewInfo();

	UWorld uworld(getPtr(getRealOffset(Offsets::GWorld)));
	if (uworld.ptr == 0) { return; }

	ULevel level = uworld.PersistentLevel;
	if (level.ptr == 0) { return; }

	for (int i = 0; i < level.getActorsCount(); i++) {
		AActor actor = level.getActor(i);
		if (actor.ptr == 0) { continue; }

		string acname = actor.getClass().getClassPath();

		/*if (isContain(acname, "PlayerController")) {
			UPlayerController PlayerController(actor.ptr);
			POV = PlayerController.PlayerCameraManager.CameraCacheEntry.POV;
		}*/

		if (isContain(acname, "PlayerCameraManager")) {
			APlayerCameraManager PlayerCameraManager(actor.ptr);
			POV = PlayerCameraManager.CameraCacheEntry.POV;
		}

		Vector3 location = actor.RootComponent.ComponentToWorld.Translation;
		Vector2 wlocation = WorldToScreen(location, POV, SWidth, SHeight);
		float distance = (Vector3::Distance(location, POV.Location) / 100.0f);

		if (isContain(acname, "STExtraPlayerCharacter")) {
			STExtraCharacter player(actor.ptr);

			PlayerData* data = &response.Players[response.PlayerCount];

			//--------------------------------------------------//
			FTransform meshtrans = player.Mesh.ComponentToWorld;
			FMatrix c2wMatrix = TransformToMatrix(meshtrans);

			FTransform headtrans = player.Mesh.getBone(6);
			FMatrix boneMatrix = TransformToMatrix(headtrans);

			Vector3 relLocation = MarixToVector(MatrixMulti(boneMatrix, c2wMatrix));
			Vector2 wrelLocation = WorldToScreen(relLocation, POV, SWidth, SHeight);
			//--------------------------------------------------//

			data->PlayerName = player.PlayerName.c_str();
			data->TeamID = player.TeamID;
			data->Health = player.Health;
			data->Distance = distance;
			data->Location = wlocation;
			data->HeadLocation = wrelLocation;

			response.PlayerCount++;
			if (response.PlayerCount == maxplayerCount) {
				continue;
			}
		}
		else if (isContain(acname, "STExtraVehicleBase") || isContain(acname, "FlightVehicle")) {
			VehicleData* data = &response.Vehicles[response.VehicleCount];

			data->Name = FindVehicleName(acname);
			data->Distance = distance;
			data->Location = wlocation;

			response.VehicleCount++;
			if (response.VehicleCount == maxvehicleCount) {
				continue;
			}
		}
		else if (isContain(acname, "PlayerTombBox")) {
			ItemData* data = &response.Items[response.ItemsCount];

			if (isContain(level.getActor(i-1).getClass().getClassPath(), "AirDropBoxActor")) {
				data->Name = "AirDrop";
			}
			else {
				data->Name = "LootBox";
			}
			data->Distance = distance;
			data->Location = wlocation;

			response.ItemsCount++;
			if (response.ItemsCount == maxitemsCount) {
				continue;
			}
		}
	}
}

int main(int argc, char *argv[]) {
	if (!server.Create()) {
		LOGE("SE:1");
		return -1;
	}

	if (!server.Bind()) {
		LOGE("SE:2");
		return -1;
	}

	if (!server.Listen()) {
		LOGE("SE:3");
		return -1;
	}
	
	if(argc < 2){
		LOGE("SE:4");
		return -1;
	}

	if (server.Accept()) {
		target_pid = atoi(argv[1]);
		if (target_pid == -1) {
			LOGE("Can't find the process\n");
			return -1;
		}

		libbase = get_module_base(lib_name);
		if (libbase == 0) {
			LOGE("Can't find module\n");
			return -1;
		}
		LOGW("Base Address of %s Found At %x\n", lib_name, libbase);

		Request request{};
		while (server.receive((void*)& request) > 0) {
			Response response{};

			if (request.Mode == InitMode) {
				response.Success = (libbase > 0);
			}
			else if (request.Mode == ESPMode) {
				SWidth = request.ScreenWidth;
				SHeight = request.ScreenHeight;
				createDataList(response);
				response.Success = true;
			}
			else if (request.Mode == HackMode) {
				response.Success = true;
			}
			else if (request.Mode == StopMode) {
				response.Success = true;
				server.send((void*)& response, sizeof(response));
				break;
			}

			server.send((void*)& response, sizeof(response));
		}
	}
	
	return 0;
}






