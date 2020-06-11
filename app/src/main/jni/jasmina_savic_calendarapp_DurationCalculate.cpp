//
// Created by jasmi on 01-Jun-20.
//

#include "jasmina_savic_calendarapp_DurationCalculate.h"

JNIEXPORT jint JNICALL Java_jasmina_savic_calendarapp_DurationCalculate_durationCalculate
  (JNIEnv *env, jobject obj, jint startH, jint startM, jint endH, jint endM) {
        return (endH * 60 + endM) - (startH * 60 + startM);
  }