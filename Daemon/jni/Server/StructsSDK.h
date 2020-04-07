#ifndef STRUCTSSDK_H
#define STRUCTSSDK_H

#include "StructsCommon.h"
#include "FNames.h"

using namespace std;

//--------------SDK-----------------//

struct UObject : Base {
	kaddr ClassPrivate;
	uint32 FNameID;

	UObject(kaddr address) : Base(address) {
		if (address) {
			ClassPrivate = getPtr(address + Offsets::UObjectToClassPrivate);
			FNameID = Read<uint32>(address + Offsets::UObjectToFNameindex);
		}
	}

	string getName() {
		return GetFNameFromID(FNameID);
	}
};

struct UStruct : UObject {
	kaddr SuperStruct;

	UStruct(kaddr address) : UObject(address) {
		if (address) {
			SuperStruct = getPtr(address + Offsets::UStructToSuperStruct);
		}
	}

	string getClassName() {
		return getName();
	}

	string getClassPath() {
		string classname = getClassName();

		kaddr superclass = SuperStruct;
		while (superclass) {
			UStruct clazz(superclass);

			classname += ".";
			classname += clazz.getClassName();
			
			superclass = clazz.SuperStruct;
		}

		return classname;
	}
};

#endif
