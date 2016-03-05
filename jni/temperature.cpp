//
// Created by Bhavik Upadhyaya on 3/3/16.
//

#include <jni.h>
#include <android/log.h>
using namespace std;

#define  LOG_TAG    "temperaturejni"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


// Temperature conversion calculation functions
float CelsiusToFahrenheit(float temp){
    return (temp * 1.8) + 32;
}

float FahrenheitToCelsius(float temp){
    return (temp - 32) / 1.8;
}

#ifdef __cplusplus
extern "C" {
#endif

// Convert a single Java float temperature from one scale to another by calling the respective
// C++ temperature conversion function.
jfloat Java_com_bhupadhy_temperature_MainActivity_ConvertTemp(JNIEnv* env, jobject obj, jfloat temp, jchar scale){
    jfloat result;
    if(scale == 'C'){
        result = FahrenheitToCelsius(temp);
    } else if(scale == 'F'){
        result = CelsiusToFahrenheit(temp);
    } else{
        //Scale Error
        LOGE("ScaleError: %c is not a recognized scale\n", scale);
    }
    LOGI("Converted %f to %f Scale: %c",temp,result,scale);
    return result;
}

// Convert a Java float array of temperatures of one scale to the other scale by copying
// the elements in the array to a jfloat *ptr which can then be used to call ConvertTemp
// on each of the elements and then converted back into a jfloatArray and returned to the
// caller.
jfloatArray Java_com_bhupadhy_temperature_MainActivity_ConvertListTemps(JNIEnv* env, jobject obj, jfloatArray temps, jchar scale){
    jboolean isCopy;
    jint size = env->GetArrayLength(temps);

    // Gets the body of the primitive float array which will be valid until release is called
    jfloat *ptr = env->GetFloatArrayElements(temps, &isCopy);

    // Loop through array. Convert and update each temp to the new scale passed in
    for(int i = 0; i < size; i++){
        ptr[i] = Java_com_bhupadhy_temperature_MainActivity_ConvertTemp(
                env,
                obj,
                ptr[i],
                scale
        );

    }
    // Inform VM: No longer need access to this ptr(C++) float array
    // When 0 option is selected for third parameter the elements in ptr
    // are copied and written to temps Java float array which will then
    // contain the converted temperatures
    env->ReleaseFloatArrayElements(temps,ptr,0);
    return temps;
}



#ifdef __cplusplus
}
#endif