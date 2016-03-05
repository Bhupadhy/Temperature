LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := temperature
LOCAL_SRC_FILES := temperature.cpp
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)