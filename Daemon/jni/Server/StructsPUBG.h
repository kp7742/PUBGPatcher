#ifndef STRUCTSPUBG_H
#define STRUCTSPUBG_H

#include "StructsSDK.h"
#include "StructsCommon.h"

using namespace std;

//--------------UEClasses-----------------//

struct UEObject : UObject {
	UStruct clazz;

	UEObject(kaddr address) : UObject(address), clazz(ClassPrivate) {}

	UStruct getClass() {
		return clazz;
	}
};

struct USceneComponent : UEObject {
	FTransform ComponentToWorld;

	USceneComponent(kaddr address) : UEObject(address) {
		if (address) {
			ComponentToWorld = Read<FTransform>(address + Offsets::USceneComponentToComponentToWorld);
		}
	}
};

struct APlayerCameraManager : UEObject {
	FCameraCacheEntry CameraCacheEntry;

	APlayerCameraManager(kaddr address) : UEObject(address) {
		if (address) {
			CameraCacheEntry = Read<FCameraCacheEntry>(address + Offsets::APlayerCameraManagerToCameraCacheEntry);
		}
	}
};

struct AController : UEObject {
	USceneComponent TransformComponent;

	AController(kaddr address) : UEObject(address), TransformComponent(address + Offsets::UControllerToTransformComponent) {}
};

struct UPlayerController : AController {
	APlayerCameraManager PlayerCameraManager;

	UPlayerController(kaddr address) : AController(address),
		PlayerCameraManager(getPtr(address + Offsets::UPlayerControllerToPlayerCameraManager)){}
};

struct AActor : UEObject {
	USceneComponent RootComponent;

	AActor(kaddr address) : UEObject(address), RootComponent(getPtr(address + Offsets::AActorToRootComponent)) {}
};

struct SkeletalMeshComponent : USceneComponent {
	TArray<kaddr> CachedComponentSpaceTransforms;

	SkeletalMeshComponent(kaddr address) : USceneComponent(address) {
		if (address) {
			CachedComponentSpaceTransforms = Read<TArray<kaddr>>(address + Offsets::USkeletalMeshComponentToCachedComponentSpaceTransforms);
		}
	}

	FTransform getBone(int i) {
		return Read<FTransform>(CachedComponentSpaceTransforms.Data + (i * Offsets::FtransformSpace));
	}
};

struct Character : AActor {
	SkeletalMeshComponent Mesh;

	Character(kaddr address) : AActor(address), Mesh(getPtr(address + Offsets::UCharacterToMesh)) {}
};

struct UAECharacter : Character {
	FString PlayerName;
	int TeamID;

	UAECharacter(kaddr address) : Character(address) {
		if (address) {
			PlayerName = Read<FString>(address + Offsets::UAECharacterToPlayerName);
			TeamID = Read<int>(address + Offsets::UAECharacterToTeamID);
		}
	}
};

struct STExtraCharacter : UAECharacter {
	float Health;

	STExtraCharacter(kaddr address) : UAECharacter(address) {
		if (address) {
			Health = Read<float>(address + Offsets::STExtraCharacterToHealth);
		}
	}
};

struct ULevel : UEObject {
	TArray<kaddr> AActorList;

	ULevel(kaddr address) : UEObject(address) {
		if (address) {
			AActorList = Read<TArray<kaddr>>(address + Offsets::ULevelToAActors);
		}
	}

	int getActorsCount() {
		return AActorList.Count;
	}

	AActor getActor(int i) {
		return AActor(AActorList.get(i));
	}
};

struct UWorld : UEObject {
	ULevel PersistentLevel;

	UWorld(kaddr address) : UEObject(address),
		PersistentLevel(getPtr(address + Offsets::UWorldToPersistentLevel)){}
};

#endif
