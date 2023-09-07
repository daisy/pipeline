#include "Voice.hpp"

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
/// convert a java string to a wstring
/// </summary>
/// <param name="env">java environment</param>
/// <param name="src">java string</param>
/// <param name="dest">UTF-16 encoded string</param>
/// <param name="maxSizeInBytes"></param>
/// <returns>false if the string received has a larger memory size than the maxSizeInBytes value, true if not</returns>
inline std::wstring jstringToWstring(JNIEnv* env, jstring src) {
    //The JNI functions take as arguments numbers of UTF16 code points,
    //not numbers of UTF16 characters.
    //UTF16 characters may be stored in multiple code points.
    int codePoints = env->GetStringLength(src);
    if (codePoints == 0) return std::wstring();

    wchar_t* dest = new wchar_t[sizeof(wchar_t) + codePoints * sizeof(wchar_t)];
    env->GetStringRegion(src, 0, codePoints, (jchar*)dest);
    dest[codePoints] = L'\0';
    std::wstring res = std::wstring(dest);
    delete dest;
    return res;
}

/// <summary>
/// Create an empty java array
/// </summary>
/// <param name="env">JNI environment</param>
/// <param name="javaClass">Full class name (with packages, separated by "/") of the corresponding JNIType in java (for example "java/lang/String")</param>
/// <param name="size">Size of the array to allocate (defaults to 0 for empty)</param>
/// <returns></returns>
jobjectArray emptyJavaArray(JNIEnv* env, const char* javaClass, int size = 0);


inline void raiseIOException(JNIEnv* env, const jchar* message, size_t len) {
    jclass exceptionClass = env->FindClass("java/io/IOException");
    jmethodID construtor = env->GetMethodID(exceptionClass, "<init>", "(Ljava/lang/String;)V");
    jstring messageJava = env->NewString(message, len);
    jobject except = env->NewObject(exceptionClass, construtor, messageJava);
    env->Throw((jthrowable)except);
}

inline void raiseException(JNIEnv* env, int errorCode, std::wstring message) {
    std::wostringstream excep;
    excep << L"Error code (0x" << std::hex << errorCode << L") raised when trying to speak with OneCore or SAPI" << std::endl;
    excep << message << std::endl;
    // Use exception instead of return result to get error code in java
    raiseIOException(env, (const jchar*)excep.str().c_str(), excep.str().size());
}


/// <summary>
/// Convert an collection of items/objects to a java array
/// </summary>
/// <typeparam name="Iterator">Object collection iterator</typeparam>
/// <typeparam name="Convertor">conversion class that implements a static method ::convert(Iterator items, JNIEnv* env) that converts the current item of the iterator to a Java (JNI) typed object</typeparam>
/// <param name="env">JNI environment</param>
/// <param name="items">iterator of items to be converted</param>
/// <param name="itemConvertor">conversion class that implements a ::to( that converts the current item of the iterator to a Java (JNI) typed object</param>
/// <param name="size">number of items to be converted and inserted in the new java array</param>
/// <param name="javaClass">Full class name (with packages, separated by "/") of the corresponding JNIType in java (for example "java/lang/String")</param>
/// <returns></returns>
template<class Iterator, class Convertor>
jobjectArray newJavaArray(JNIEnv* env, Iterator items, size_t size, const char* javaClass) {
	jclass objClass = env->FindClass(javaClass);
	jobjectArray jArray = env->NewObjectArray(static_cast<int>(size), objClass, 0);
	if (size > 0) {
		for (int i = 0; i < static_cast<int>(size); ++items, ++i) {
			const auto& r = Convertor::convert(items, env);
			env->SetObjectArrayElement(jArray, i, r);
		}
	}
	return jArray;
}


template<class Iterator>
struct StringToJString {
    static jstring convert(const Iterator& it, JNIEnv* env) {
        const wchar_t* str = it->c_str();
        return env->NewString((const jchar*)str, static_cast<jsize>(std::wcslen(str)));
    }
};

inline jobject getStaticEnumValue(JNIEnv* env, jclass enumClass, const char* sig, const char* enumValueName) {
    return env->GetStaticObjectField(enumClass, env->GetStaticFieldID(enumClass, enumValueName, sig));
}

template<typename T >
jobjectArray VoicesMapToPipelineVoicesArray(
    JNIEnv* env,
    std::map<std::pair<std::wstring, std::wstring>, Voice<T>> &map,
    const wchar_t* engineName
) {
    jclass voiceClass = env->FindClass("org/daisy/pipeline/tts/Voice");
    if (voiceClass == NULL) {
        const wchar_t* str = L"Voice class not found in runtime";
        raiseIOException(env, (const jchar*)str, static_cast<jsize>(std::wcslen(str)));
        return NULL;
    }
    jmethodID voiceConstructor = env->GetMethodID(voiceClass, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/util/Locale;Lorg/daisy/pipeline/tts/VoiceInfo$Gender;)V"); // TBD
    if (voiceConstructor == NULL) {
        const wchar_t* str = L"Voice constructor not found in runtime";
        raiseIOException(env, (const jchar*)str, static_cast<jsize>(std::wcslen(str)));
        return NULL;
    }

    jclass voiceInfoClass = env->FindClass("org/daisy/pipeline/tts/VoiceInfo");
    if (voiceInfoClass == NULL) {
        const wchar_t* str = L"VoiceInfo class not found in runtime";
        raiseIOException(env, (const jchar*)str, static_cast<jsize>(std::wcslen(str)));
        return NULL;
    }
    jmethodID tagToLocaleID = env->GetStaticMethodID(voiceInfoClass, "tagToLocale", "(Ljava/lang/String;)Ljava/util/Locale;"); // TBD
    if (tagToLocaleID == NULL) {
        const wchar_t* str = L"tagToLocale method not found in runtime";
        raiseIOException(env, (const jchar*)str, static_cast<jsize>(std::wcslen(str)));
        return NULL;
    }

    jclass genderClass = env->FindClass("org/daisy/pipeline/tts/VoiceInfo$Gender");
    const char* genderClassSig = "Lorg/daisy/pipeline/tts/VoiceInfo$Gender;";
    
    size_t size = map.size();
    jobjectArray voicesArray = env->NewObjectArray(static_cast<int>(size), voiceClass, 0);
    if (size > 0) {
        auto items = map.begin();
        for (int i = 0; i < static_cast<int>(size); ++items, ++i) {
            std::wstring currentGender =  items->second.gender;
            std::transform(currentGender.begin(), currentGender.end(), currentGender.begin(), ::towlower);

            std::wstring currentAge = items->second.age;
            std::transform(currentAge.begin(), currentAge.end(), currentAge.begin(), ::towlower);


            jobject selected = getStaticEnumValue(env, genderClass, genderClassSig, "FEMALE_ADULT");
            if (currentGender.compare(L"male") == 0) {
                if (currentAge.compare(L"child")) {
                    selected = getStaticEnumValue(env, genderClass, genderClassSig, "MALE_CHILD");
                }
                else if (currentAge.compare(L"elderly")) {
                    selected = getStaticEnumValue(env, genderClass, genderClassSig, "MALE_ELDERY");
                }
                else {
                    selected = getStaticEnumValue(env, genderClass, genderClassSig, "MALE_ADULT");
                }
            }
            else {
                if (currentAge.compare(L"child")) {
                    selected = getStaticEnumValue(env, genderClass, genderClassSig, "FEMALE_CHILD");
                }
                else if (currentAge.compare(L"elderly")) {
                    selected = getStaticEnumValue(env, genderClass, genderClassSig, "FEMALE_ELDERY");
                }
                else {
                    selected = getStaticEnumValue(env, genderClass, genderClassSig, "FEMALE_ADULT");
                }
            }


            env->SetObjectArrayElement(
                voicesArray,
                i,
                env->NewObject(
                    voiceClass,
                    voiceConstructor,
                    env->NewString((const jchar*)engineName, static_cast<jsize>(std::wcslen(engineName))),
                    env->NewString((const jchar*)items->second.name.c_str(), static_cast<jsize>(std::wcslen(items->second.name.c_str()))),
                    env->CallStaticObjectMethod(
                        voiceInfoClass,
                        tagToLocaleID,
                        env->NewString((const jchar*)items->second.language.c_str(), static_cast<jsize>(std::wcslen(items->second.language.c_str())))
                    ),
                    selected
                )
            );
        }

    }

    return voicesArray;
}


template<class StringArray, class Iterator>
jobject newSynthesisResult(JNIEnv* env,
    size_t dataSize, 
    const uint8_t* dataArray,
    StringArray& marksNames,
    const int64_t* marksPositionsArray
) {
    // Stream data to java byte array
    jbyteArray data = env->NewByteArray(dataSize);
    if (dataSize > 0) {
        env->SetByteArrayRegion(data, 0, dataSize, (const jbyte*)dataArray);
    }

    size_t marksSize = marksNames.size();
    jlongArray positions = env->NewLongArray(marksSize);
    if (marksSize > 0) {
        env->SetLongArrayRegion(positions, 0, marksSize, marksPositionsArray);
    }

    jobjectArray names = newJavaArray<Iterator,StringToJString<Iterator>>(
        env,
        marksNames.begin(),
        marksSize,
        "java/lang/String"
    );

    jclass resClass = env->FindClass("org/daisy/pipeline/tts/onecore/NativeSynthesisResult");
    if (resClass == NULL) {
        raiseException(env, -1, L"Class NativeSynthesisResult was not found in runtime");
        return NULL;
    }
    jmethodID constructor = env->GetMethodID(resClass, "<init>", "([B[Ljava/lang/String;[J)V");
    if (constructor == NULL) {
        raiseException(env, -1, L"NativeSynthesisResult constructor was not found in runtime");
        return NULL;
    }

    return env->NewObject(resClass, constructor, data, names, positions);
}

#endif