#ifndef OFFSETS_H
#define OFFSETS_H

namespace Offsets {
    enum Offsets {
        //Global
        GWorld = 0x508DEF0,
        GNames = 0x52EBB24,
		PointerSize = 0x4,
		FtransformSpace = 0x30,

		//---------SDK-----------
		//Class: FNameEntry
		FNameEntryToNameString = 0x8,
		//Class: UObject
		UObjectToClassPrivate = 0xC,
		UObjectToFNameindex = 0x10,
		//Class: UStruct
		UStructToSuperStruct = 0x20,

		//---------PUBG UEClasses-----------
		//Class: UWorld
		UWorldToPersistentLevel = 0x20,
		//Class: ULevel
		ULevelToAActors = 0x70,
		//Class: UController
		UControllerToTransformComponent = 0x2d8,
		//Class: UPlayerController
		UPlayerControllerToPlayerCameraManager = 0x324,
		//Class: APlayerCameraManager
		APlayerCameraManagerToCameraCacheEntry = 0x330,
		//Class: USceneComponent
		USceneComponentToComponentToWorld = 0x140,
		//Class: USkeletalMeshComponent
		USkeletalMeshComponentToCachedComponentSpaceTransforms = 0x7b4,
		//Class: AActor
		AActorToRootComponent = 0x138,
		//Class: UCharacter
		UCharacterToMesh = 0x304,
		//Class: UAECharacter
		UAECharacterToPlayerName = 0x5d8,
		UAECharacterToTeamID = 0x600,
		//Class: STExtraCharacter
		STExtraCharacterToHealth = 0x790,
    };
}

#endif
