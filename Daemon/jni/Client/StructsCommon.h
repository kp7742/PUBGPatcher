#ifndef STRUCTSCOMM_H
#define STRUCTSCOMM_H

using namespace std;

#define maxplayerCount 30
#define maxvehicleCount 20
#define maxitemsCount 20

class Color {
public:
	float r;
	float g;
	float b;
	float a;

	Color() {
		this->r = 0;
		this->g = 0;
		this->b = 0;
		this->a = 0;
	}

	Color(float r, float g, float b, float a) {
		this->r = r;
		this->g = g;
		this->b = b;
		this->a = a;
	}

	Color(float r, float g, float b) {
		this->r = r;
		this->g = g;
		this->b = b;
		this->a = 255;
	}

	static Color White(){
		return Color(255,255,255);
	}

	static Color Green(){
		return Color(0,255,0);
	}
};

class Vector2 {
public:
	float x;
	float y;

	Vector2() {
		this->x = 0;
		this->y = 0;
	}

	Vector2(float x, float y) {
		this->x = x;
		this->y = y;
	}

	static Vector2 Zero() {
		return Vector2(0.0f, 0.0f);
	}

	bool operator!=(const Vector2 &src) const {
		return (src.x != x) || (src.y != y);
	}

	Vector2 &operator+=(const Vector2 &v) {
		x += v.x;
		y += v.y;
		return *this;
	}

	Vector2 &operator-=(const Vector2 &v) {
		x -= v.x;
		y -= v.y;
		return *this;
	}
};

class Vector3 {
public:
	float x;
	float y;
	float z;

	Vector3() {
		this->x = 0;
		this->y = 0;
		this->z = 0;
	}

	Vector3(float x, float y, float z) {
		this->x = x;
		this->y = y;
		this->z = z;
	}

	static Vector3 Zero() {
		return Vector3(0.0f, 0.0f, 0.0f);
	}
};

class Rect {
public:
	float x;
	float y;
	float width;
	float height;

	Rect() {
		this->x = 0;
		this->y = 0;
		this->width = 0;
		this->height = 0;
	}

	Rect(float x, float y, float width, float height) {
		this->x = x;
		this->y = y;
		this->width = width;
		this->height = height;
	}

	bool operator==(const Rect &src) const {
		return (src.x == this->x && src.y == this->y && src.height == this->height &&
				src.width == this->width);
	}

	bool operator!=(const Rect &src) const {
		return (src.x != this->x && src.y != this->y && src.height != this->height &&
				src.width != this->width);
	}
};

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

#endif
