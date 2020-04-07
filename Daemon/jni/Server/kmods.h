#ifndef KMODS_H
#define KMODS_H

#include <cstdio>
#include <cstdlib>
#include <cstddef>
#include <dirent.h>
#include <unistd.h>
#include <cmath>
#include <ctime>
#include <string>
#include <vector>
#include <list>
#include <stack>
#include <iostream>
#include <iomanip>
#include <fstream>

#include "Log.h"
#include "Offsets.h"
#include "Process.h"
#include "Memory.h"

static const char* game_version = "0.13.0";
static const char* process_name = "com.tencent.ig";
static const char* lib_name = "libUE4.so";

int SWidth = 1920;
int SHeight = 1080;

bool isStartWith(string str, const char* check) {
	return (str.rfind(check, 0) == 0);
}

bool isEqual(char* s1, const char* s2) {
	return (strcmp(s1, s2) == 0);
}

bool isEqual(string s1, const char* check) {
	string s2(check);
	return (s1 == s2);
}

bool isEqual(string s1, string s2) {
	return (s1 == s2);
}

bool isContain(string str, const char* check) {
	size_t found = str.find(check);
	return (found != string::npos);
}

#endif
