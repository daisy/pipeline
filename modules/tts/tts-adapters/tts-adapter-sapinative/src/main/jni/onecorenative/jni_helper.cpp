#include "pch.h"


bool convertToUTF16(JNIEnv* env, jstring src, wchar_t* dest, size_t maxSizeInBytes) {
	//The JNI functions take as arguments numbers of UTF16 code points,
	//not numbers of UTF16 characters.
	//UTF16 characters may be stored in multiple code points.
	int codePoints = env->GetStringLength(src);
	if ((codePoints + 1) * sizeof(wchar_t) > maxSizeInBytes) {
		return false;
	}
	env->GetStringRegion(src, 0, codePoints, (jchar*)dest);
	dest[codePoints] = L'\0';

	return true;
}


jobjectArray emptyJavaArray(JNIEnv* env, const char* javaClass, int size) {
	jclass objClass = env->FindClass(javaClass);
	jobjectArray jArray = env->NewObjectArray(size, objClass, 0);
	return jArray;
}


