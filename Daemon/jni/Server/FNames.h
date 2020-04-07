#ifndef FNAMES_H
#define FNAMES_H

#include "kmods.h"

using namespace std;

string getUEString(kaddr address) {
	unsigned int MAX_SIZE = 100;

	string uestring(ReadStr(address, MAX_SIZE));
	uestring.shrink_to_fit();

	return uestring;
}

string GetFNameFromID(uint32_t index) {
	kaddr TNameEntryArray = getPtr(getRealOffset(Offsets::GNames));

	kaddr FNameEntryArr = getPtr(TNameEntryArray + ((index / 0x4000) * Offsets::PointerSize));
	kaddr FNameEntry = getPtr(FNameEntryArr + ((index % 0x4000) * Offsets::PointerSize));

	return getUEString(FNameEntry + Offsets::FNameEntryToNameString);
}

#endif