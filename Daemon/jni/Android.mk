LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := kdaemon

LOCAL_CPPFLAGS += -pie -fPIE -ffunction-sections -fdata-sections -fvisibility=hidden
LOCAL_LDFLAGS += -pie -fPIE -Wl,--gc-sections
LOCAL_CFLAGS := -Wno-error=format-security -fpermissive
LOCAL_CFLAGS += -ffunction-sections -fdata-sections -fvisibility=hidden
LOCAL_CFLAGS += -fno-rtti -fno-exceptions
LOCAL_CFLAGS += -DNDEBUG

LOCAL_SRC_FILES := Socket/SocketServer.cpp \
				   Server/kmods.cpp \

LOCAL_CPP_INCLUDES += $(LOCAL_PATH)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/Socket
LOCAL_C_INCLUDES += $(LOCAL_PATH)/Server

LOCAL_LDLIBS += -llog

include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)

LOCAL_MODULE := kmods

LOCAL_SRC_FILES := Socket/SocketClient.cpp \
                   Client/kmods.cpp \

LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/Socket
LOCAL_C_INCLUDES += $(LOCAL_PATH)/Client

LOCAL_CPPFLAGS := -ffunction-sections -fdata-sections -fvisibility=hidden
LOCAL_CPPFLAGS += -fno-rtti -fno-exceptions
LOCAL_CPPFLAGS += -DNDEBUG

LOCAL_LDFLAGS += -Wl,--gc-sections
LOCAL_LDFLAGS += -L$(SYSROOT)/usr/lib -lz -llog

include $(BUILD_SHARED_LIBRARY)




