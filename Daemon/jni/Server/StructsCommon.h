#ifndef STRUCTSCOMM_H
#define STRUCTSCOMM_H

#include "kmods.h"

using namespace std;

template<class T>
struct TArray {
	kaddr Data;
	int32 Count;

	inline kaddr operator[](int i)
	{
		return Data + (i * sizeof(T));
	};

	T get(int i, bool deref = false) {
		kaddr ptrData = Data + (i * sizeof(T));

		if (deref)
			ptrData = getPtr(ptrData);

		return Read<T>(ptrData);
	}

	void set(T value, int i, bool deref = false) {
		kaddr ptrData = Data + (i * sizeof(T));

		if (deref)
			ptrData = getPtr(ptrData);

		Write<T>(ptrData, value);
	}
};


struct FString {
	kaddr Data;
	int32 Count;

	const char* c_str() {
		char* str = new char[Count];
		for (int i = 0; i < Count; i++) {
			char data = Read<char>(Data + (2 * i));
			str[i] = isascii(data) ? data : '?';
		}
		return (const char*)str;
	}
};

struct Base {
	kaddr ptr;

	Base(kaddr address) {
		if (address) {
			ptr = address;
		}
	}
};

struct Vector3 {
	float X;
	float Y;
	float Z;

	Vector3() {
		this->X = 0;
		this->Y = 0;
		this->Z = 0;
	}

	Vector3(float x, float y, float z) {
		this->X = x;
		this->Y = y;
		this->Z = z;
	}

	Vector3 operator+(const Vector3& v) const {
		return Vector3(X + v.X, Y + v.Y, Z + v.Z);
	}

	Vector3 operator-(const Vector3& v) const {
		return Vector3(X - v.X, Y - v.Y, Z - v.Z);
	}

	static float Dot(Vector3 lhs, Vector3 rhs) {
		return (((lhs.X * rhs.X) + (lhs.Y * rhs.Y)) + (lhs.Z * rhs.Z));
	}

	static float Distance(Vector3 a, Vector3 b) {
		Vector3 vector = Vector3(a.X - b.X, a.Y - b.Y, a.Z - b.Z);
		return sqrt(((vector.X * vector.X) + (vector.Y * vector.Y)) + (vector.Z * vector.Z));
	}
};

struct Vector2 {
	float X;
	float Y;

	Vector2() {
		this->X = 0;
		this->Y = 0;
	}

	Vector2(float x, float y) {
		this->X = x;
		this->Y = y;
	}
};

struct Rect {
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

	bool operator==(const Rect& src) const {
		return (src.x == this->x && src.y == this->y && src.height == this->height &&
			src.width == this->width);
	}

	bool operator!=(const Rect& src) const {
		return (src.x != this->x && src.y != this->y && src.height != this->height &&
			src.width != this->width);
	}
};

struct FMatrix {
	float M[4][4];
};

struct Quat {
	float X;
	float Y;
	float Z;
	float W;
};

struct FTransform {
	Quat Rotation;
	Vector3 Translation;
	Vector3 Scale3D;
};

Vector3 MarixToVector(FMatrix matrix) {
	return Vector3(matrix.M[3][0], matrix.M[3][1], matrix.M[3][2]);
}

FMatrix MatrixMulti(FMatrix m1, FMatrix m2) {
	FMatrix matrix = FMatrix();
	for (int i = 0; i < 4; i++) {
		for (int j = 0; j < 4; j++) {
			for (int k = 0; k < 4; k++) {
				matrix.M[i][j] += m1.M[i][k] * m2.M[k][j];
			}
		}
	}
	return matrix;
}

FMatrix TransformToMatrix(FTransform transform) {
	FMatrix matrix;

	matrix.M[3][0] = transform.Translation.X;
	matrix.M[3][1] = transform.Translation.Y;
	matrix.M[3][2] = transform.Translation.Z;

	float x2 = transform.Rotation.X + transform.Rotation.X;
	float y2 = transform.Rotation.Y + transform.Rotation.Y;
	float z2 = transform.Rotation.Z + transform.Rotation.Z;

	float xx2 = transform.Rotation.X * x2;
	float yy2 = transform.Rotation.Y * y2;
	float zz2 = transform.Rotation.Z * z2;

	matrix.M[0][0] = (1.0f - (yy2 + zz2)) * transform.Scale3D.X;
	matrix.M[1][1] = (1.0f - (xx2 + zz2)) * transform.Scale3D.Y;
	matrix.M[2][2] = (1.0f - (xx2 + yy2)) * transform.Scale3D.Z;

	float yz2 = transform.Rotation.Y * z2;
	float wx2 = transform.Rotation.W * x2;
	matrix.M[2][1] = (yz2 - wx2) * transform.Scale3D.Z;
	matrix.M[1][2] = (yz2 + wx2) * transform.Scale3D.Y;

	float xy2 = transform.Rotation.X * y2;
	float wz2 = transform.Rotation.W * z2;
	matrix.M[1][0] = (xy2 - wz2) * transform.Scale3D.Y;
	matrix.M[0][1] = (xy2 + wz2) * transform.Scale3D.X;

	float xz2 = transform.Rotation.X * z2;
	float wy2 = transform.Rotation.W * y2;
	matrix.M[2][0] = (xz2 + wy2) * transform.Scale3D.Z;
	matrix.M[0][2] = (xz2 - wy2) * transform.Scale3D.X;

	matrix.M[0][3] = 0;
	matrix.M[1][3] = 0;
	matrix.M[2][3] = 0;
	matrix.M[3][3] = 1;

	return matrix;
}

struct FRotator {
	float Pitch;
	float Yaw;
	float Roll;
};

FMatrix RotatorToMatrix(FRotator rotation) {
	float radPitch = rotation.Pitch * ((float)M_PI / 180.0f);
	float radYaw = rotation.Yaw * ((float)M_PI / 180.0f);
	float radRoll = rotation.Roll * ((float)M_PI / 180.0f);

	float SP = sinf(radPitch);
	float CP = cosf(radPitch);
	float SY = sinf(radYaw);
	float CY = cosf(radYaw);
	float SR = sinf(radRoll);
	float CR = cosf(radRoll);

	FMatrix matrix;

	matrix.M[0][0] = (CP * CY);
	matrix.M[0][1] = (CP * SY);
	matrix.M[0][2] = (SP);
	matrix.M[0][3] = 0;

	matrix.M[1][0] = (SR * SP * CY - CR * SY);
	matrix.M[1][1] = (SR * SP * SY + CR * CY);
	matrix.M[1][2] = (-SR * CP);
	matrix.M[1][3] = 0;

	matrix.M[2][0] = (-(CR * SP * CY + SR * SY));
	matrix.M[2][1] = (CY * SR - CR * SP * SY);
	matrix.M[2][2] = (CR * CP);
	matrix.M[2][3] = 0;

	matrix.M[3][0] = 0;
	matrix.M[3][1] = 0;
	matrix.M[3][2] = 0;
	matrix.M[3][3] = 1;

	return matrix;
}

struct MinimalViewInfo {
	Vector3 Location;
	Vector3 LocationLocalSpace;
	FRotator Rotation;
	float FOV;
};

struct FCameraCacheEntry {
	float TimeStamp;
	char chunks[0xC];
	MinimalViewInfo POV;
};

Vector2 WorldToScreen(Vector3 worldLocation, MinimalViewInfo camViewInfo, int width, int height) {
	FMatrix tempMatrix = RotatorToMatrix(camViewInfo.Rotation);

	Vector3 vAxisX(tempMatrix.M[0][0], tempMatrix.M[0][1], tempMatrix.M[0][2]);
	Vector3 vAxisY(tempMatrix.M[1][0], tempMatrix.M[1][1], tempMatrix.M[1][2]);
	Vector3 vAxisZ(tempMatrix.M[2][0], tempMatrix.M[2][1], tempMatrix.M[2][2]);

	Vector3 vDelta = worldLocation - camViewInfo.Location;

	Vector3 vTransformed(Vector3::Dot(vDelta, vAxisY), Vector3::Dot(vDelta, vAxisZ), Vector3::Dot(vDelta, vAxisX));

	if (vTransformed.Z < 1.0f) {
		vTransformed.Z = 1.0f;
	}

	float fov = camViewInfo.FOV;
	float screenCenterX = (width / 2.0f);
	float screenCenterY = (height / 2.0f);

	return Vector2(
		(screenCenterX + vTransformed.X * (screenCenterX / tanf(fov * ((float)M_PI / 360.0f))) / vTransformed.Z),
		(screenCenterY - vTransformed.Y * (screenCenterX / tanf(fov * ((float)M_PI / 360.0f))) / vTransformed.Z)
	);
}

#endif
