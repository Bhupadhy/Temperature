//
// Created by Bhavik Upadhyaya on 3/3/16.
//

#include "temperature.h"

jstring Java_com_bhupadhy_temperature_MainActivity_hello(JNIEnv* env, jobject obj){
    return (*env)->NewStringUTF(env,"Its me");
}