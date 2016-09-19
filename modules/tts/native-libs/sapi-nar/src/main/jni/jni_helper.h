#ifndef SAPI_HELPER_H_
#define SAPI_HELPER_H_

//dest is an UTF-16 encoded string 
bool convertToUTF16(JNIEnv* env, jstring src, wchar_t* dest, int maxSizeInBytes){
	//The JNI functions take as arguments numbers of UTF16 code points,
	//not numbers of UTF16 characters.
	//UTF16 characters may be stored in multiple code points.
	int codePoints = env->GetStringLength(src);
    if ((codePoints+1)*sizeof(wchar_t) > maxSizeInBytes){ 
		return false;
	}
	env->GetStringRegion(src, 0, codePoints, (jchar*) dest);
	dest[codePoints] = L'\0';
	
	return true;
}

template<class CollectionAccessor, class Result, class Iterator>
jobjectArray newJavaArray(JNIEnv* env, Iterator it, int size, const char* javaClass){
	jclass objClass = env->FindClass(javaClass);
	jobjectArray jArray = env->NewObjectArray(size, objClass, 0);
	
	for (int i = 0; i < size; ++it, ++i){
		const Result& r = CollectionAccessor::get(it, env);
		env->SetObjectArrayElement(jArray, i, r);
	}

	return jArray;
}

#endif