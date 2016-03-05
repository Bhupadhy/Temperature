# Temperature
POC that uses NDK, JNI and SensorManager

Building:
  The app should already be compiled when you download it and can be ran out of the box.
  
  To rebuild this app you must have Android NDK downloaded and placed in your path variables. Then open terminal
  and navigate to the root of the project directory. Enter 'ndk-build' to compile the native C++ code in the jni
  folder. Then copy the generated libraries in the libs folder and place them in /app/src/main/jnilibs.
  
  
