//
// Created by Bhavik Upadhyaya on 3/3/16.
//

#include <jni.h>
#include <android/log.h>
using namespace std;

#define  LOG_TAG    "temperaturejni"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

float CelciusToFahrenheit(float temp){
    return (temp * 1.8) + 32;
}

float FahrenheitToCelcius(float temp){
    return (temp - 32) / 1.8;
}

#ifdef __cplusplus
extern "C" {
#endif

jstring Java_com_bhupadhy_temperature_MainActivity_Hello(JNIEnv* env, jobject obj){
    return env->NewStringUTF("Its me!!!!!");
}

jfloat Java_com_bhupadhy_temperature_MainActivity_ConvertTemp(JNIEnv* env, jobject obj, jfloat temp, jchar scale){
    LOGI("ConvertTemp: Temperature: %f Scale: %c",temp,scale);
    jfloat result;
    if(scale == 'C'){
        result = FahrenheitToCelcius(temp);
    } else if(scale == 'F'){
        result = CelciusToFahrenheit(temp);
    } else{
        //Scale Error
        LOGE("ScaleError: %c is not a recognized scale\n", scale);
    }
    return result;
}

jfloatArray Java_com_bhupadhy_temperature_MainActivity_ConvertListTemps(JNIEnv* env, jobject obj, jfloatArray temps, jchar scale){
//    jclass arrayClass = env->GetObjectClass(temps);
//    jmethodID sizeMid = env->GetMethodID(arrayClass, "size", "()I");
//    jmethodID getMid = env->GetMethodID(arrayClass, "get", "(I)Ljava/lang/Object;");
//    jmethodID addMid = env->GetMethodID(arrayClass, "add", "(Ljava/lang/Object;)Z");

    jclass floatClass = env->FindClass("java/lang/Float");
    jmethodID floatValueMid = env->GetMethodID(floatClass, "floatValue", "()F");
    jboolean isCopy;
    jvalue arg;
    jint size = env->GetArrayLength(temps);//env->CallIntMethodA(temps, sizeMid, &arg);
    jfloat *ptr = env->GetFloatArrayElements(temps, &isCopy);
    //float* cppArray = new float[size];
    //jobject obj = env->NewObject(arrayClass, env->GetMethodID(arrayClass, "<init>", "()V"));
    for(int i = 0; i < size; i++){
        arg.i = i;
//        jobject element = env->CallObjectMethodA(temps, getMid, &arg);
        ptr[i] = Java_com_bhupadhy_temperature_MainActivity_ConvertTemp(
                env,
                obj,
                ptr[i],
                scale
        );
        //env->CallBooleanMethod(temps,addMid,cppArray[i]);
        // Cant have unlimited active local refs
        //env->DeleteLocalRef(element);
    }


    // Create/Populate an Arraylist to pass back
    //jobject nArrayList = env->NewGlobalRef(temps);
    //jmethodID clearMid = env->GetMethodID(arrayClass, "clear", "()V");
    //jvalue arg1;
    //env->CallVoidMethod(temps,clearMid,&arg1);

    //jfloatArray *jbuf = env->GetFloatArrayElements(cppArray,0);
    //env->CallBooleanMethod(nArrayList,addMid,jbuf);
//    for(int i = 0; i < size; i++){
//        //arg1.i = i;
//        env->CallBooleanMethod(nArrayList,addMid,cppArray[i]);
//    }
    env->ReleaseFloatArrayElements(temps,ptr,0);
    return temps;
}



#ifdef __cplusplus
}
#endif