#include "pch.h"

#ifndef SAPI_HELPER_H_
#define SAPI_HELPER_H_



/// <summary>
/// convert a java string to a wstring
/// </summary>
/// <param name="env">java environment</param>
/// <param name="src">java string</param>
/// <param name="dest">UTF-16 encoded string</param>
/// <param name="maxSizeInBytes"></param>
/// <returns>false if the string received has a larger memory size than the maxSizeInBytes value, true if not</returns>
bool convertToUTF16(JNIEnv* env, jstring src, wchar_t* dest, size_t maxSizeInBytes);

/// <summary>
/// Create an empty java array
/// </summary>
/// <param name="env">JNI environment</param>
/// <param name="javaClass">Full class name (with packages, separated by "/") of the corresponding JNIType in java (for example "java/lang/String")</param>
/// <param name="size">Size of the array to allocate (defaults to 0 for empty)</param>
/// <returns></returns>
jobjectArray emptyJavaArray(JNIEnv* env, const char* javaClass, int size = 0);


/// <summary>
/// Convert an collection of items/objects to a java array
/// </summary>
/// <typeparam name="Iterator">Object collection iterator</typeparam>
/// <typeparam name="JavaType">targeted Java (JNI) type for each objects conversion</typeparam>
/// <param name="env">JNI environment</param>
/// <param name="items">iterator of items to be converted</param>
/// <param name="itemConvertor">conversion function that converts the current item of the iterator to a JNIType object</param>
/// <param name="size">number of items to be converted and inserted in the new java array</param>
/// <param name="javaClass">Full class name (with packages, separated by "/") of the corresponding JNIType in java (for example "java/lang/String")</param>
/// <returns></returns>
template<class Iterator, class JNIType>
jobjectArray newJavaArray(JNIEnv* env, Iterator items, std::function<JNIType(Iterator, JNIEnv*)> itemConvertor, size_t size, const char* javaClass) {
	jclass objClass = env->FindClass(javaClass);
	jobjectArray jArray = env->NewObjectArray(static_cast<int>(size), objClass, 0);
	if (size > 0) {
		for (int i = 0; i < static_cast<int>(size); ++items, ++i) {
			const JNIType& r = itemConvertor(items, env);
			env->SetObjectArrayElement(jArray, i, r);
		}
	}
	return jArray;
}

#endif