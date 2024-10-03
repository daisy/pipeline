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
    delete [] dest;
    return res;
}

/// <summary>
/// Create an empty java array
/// </summary>
/// <param name="env">JNI environment</param>
/// <param name="javaClass">Full class name (with packages, separated by "/") of the corresponding JNIType in java (for example "java/lang/String")</param>
/// <param name="size">Size of the array to allocate (defaults to 0 for empty)</param>
/// <returns></returns>
inline jobjectArray emptyJavaArray(JNIEnv* env, const char* javaClass, int size = 0) {
    jclass objClass = env->FindClass(javaClass);
    jobjectArray jArray = env->NewObjectArray(size, objClass, 0);
    return jArray;
}

/// <summary>
/// Helper to throw java IOException from C++
/// </summary>
/// <param name="env">Java calling environment</param>
/// <param name="message"></param>
/// <param name="len"></param>
inline void raiseIOException(JNIEnv* env, const jchar* message, size_t len) {
    jclass exceptionClass = env->FindClass("java/io/IOException");
    jmethodID construtor = env->GetMethodID(exceptionClass, "<init>", "(Ljava/lang/String;)V");
    jstring messageJava = env->NewString(message, (jsize)len);
    jobject except = env->NewObject(exceptionClass, construtor, messageJava);
    env->Throw((jthrowable)except);
}

/// <summary>
/// Raise an IOException exception with a given error code and message
/// </summary>
/// <param name="env"></param>
/// <param name="errorCode"></param>
/// <param name="message"></param>
inline void raiseException(JNIEnv* env, int errorCode, std::wstring message) {
    std::wostringstream excep;
    excep << L"Error code (0x" << std::hex << errorCode << L") raised when trying to speak with SAPI" << std::endl;
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

/// <summary>
/// Struct to convert a string array item (referenced by an iterator) to a java string
/// </summary>
/// <typeparam name="Iterator">The string array iterator</typeparam>
template<class Iterator>
struct StringToJString {
    static jstring convert(const Iterator& it, JNIEnv* env) {
        const wchar_t* str = it->c_str();
        return env->NewString((const jchar*)str, static_cast<jsize>(std::wcslen(str)));
    }
};

/// <summary>
/// Helper to retrieve a specific field of an enum stored in a class
/// </summary>
/// <param name="env">Java calling environment</param>
/// <param name="enumClass">Class of the enum to retrieve the field from</param>
/// <param name="sig">Signature of the field searched in the enum</param>
/// <param name="enumValueName">Name of the field searched in the enum</param>
/// <returns>The corresponding field in a java object</returns>
inline jobject getStaticEnumValue(JNIEnv* env, jclass enumClass, const char* sig, const char* enumValueName) {
    return env->GetStaticObjectField(enumClass, env->GetStaticFieldID(enumClass, enumValueName, sig));
}

/// <summary>
/// Create a java Voices array from an existing Voices list
/// </summary>
/// <typeparam name="T">the raw data type used to identify voices</typeparam>
/// <param name="env">the Java calling environment</param>
/// <param name="list">the list of voices</param>
/// <param name="engineName">the current engine name used to identify the voices</param>
/// <returns></returns>
template<typename T >
jobjectArray VoicesListToPipelineVoicesArray(
    JNIEnv* env,
    std::list<Voice<T>> &list,
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

    jclass localeClass = env->FindClass("java/util/Locale");
    if (localeClass == NULL) {
        const wchar_t* str = L"VoiceInfo class not found in runtime";
        raiseIOException(env, (const jchar*)str, static_cast<jsize>(std::wcslen(str)));
        return NULL;
    }
    jmethodID forLanguageTagID = env->GetStaticMethodID(localeClass, "forLanguageTag", "(Ljava/lang/String;)Ljava/util/Locale;"); // TBD
    if (forLanguageTagID == NULL) {
        const wchar_t* str = L"forLanguageTag method not found in runtime";
        raiseIOException(env, (const jchar*)str, static_cast<jsize>(std::wcslen(str)));
        return NULL;
    }

    jclass genderClass = env->FindClass("org/daisy/pipeline/tts/VoiceInfo$Gender");
    const char* genderClassSig = "Lorg/daisy/pipeline/tts/VoiceInfo$Gender;";
    
    size_t size = list.size();
    jobjectArray voicesArray = env->NewObjectArray(static_cast<int>(size), voiceClass, 0);
    if (size > 0) {
        auto items = list.begin();
        for (int i = 0; i < static_cast<int>(size); ++items, ++i) {
            std::wstring currentGender =  items->gender;
            std::transform(currentGender.begin(), currentGender.end(), currentGender.begin(), ::towlower);

            std::wstring currentAge = items->age;
            std::transform(currentAge.begin(), currentAge.end(), currentAge.begin(), ::towlower);


            jobject selected = getStaticEnumValue(env, genderClass, genderClassSig, "FEMALE_ADULT");
            if (currentGender.compare(L"male") == 0) {
                if (currentAge.compare(L"child") == 0) {
                    selected = getStaticEnumValue(env, genderClass, genderClassSig, "MALE_CHILD");
                }
                else if (currentAge.compare(L"elderly") == 0) {
                    selected = getStaticEnumValue(env, genderClass, genderClassSig, "MALE_ELDERY");
                }
                else {
                    selected = getStaticEnumValue(env, genderClass, genderClassSig, "MALE_ADULT");
                }
            }
            else {
                if (currentAge.compare(L"child") == 0) {
                    selected = getStaticEnumValue(env, genderClass, genderClassSig, "FEMALE_CHILD");
                }
                else if (currentAge.compare(L"elderly") == 0) {
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
                    env->NewString((const jchar*)items->name.c_str(), static_cast<jsize>(std::wcslen(items->name.c_str()))),
                    env->CallStaticObjectMethod(
                        localeClass,
                        forLanguageTagID,
                        env->NewString((const jchar*)items->language.c_str(), static_cast<jsize>(std::wcslen(items->language.c_str())))
                    ),
                    selected
                )
            );
        }

    }

    return voicesArray;
}

/// <summary>
/// Create and initialize a NativeSynthesisResult java object
/// </summary>
/// <typeparam name="StringArray">The type of array used to store marks name</typeparam>
/// <typeparam name="Iterator">The iterator type of the marks name array</typeparam>
/// <param name="env">Java calling environment</param>
/// <param name="dataSize">Memory size of the speech data bytes array</param>
/// <param name="dataArray">The speech data bytes array</param>
/// <param name="marksNames">The marks names array</param>
/// <param name="marksPositionsArray">The marks positions array</param>
/// <returns></returns>
template<class StringArray, class Iterator>
jobject newSynthesisResult(JNIEnv* env,
    size_t dataSize, 
    const uint8_t* dataArray,
    StringArray& marksNames,
    const int64_t* marksPositionsArray
) {
    // Stream data to java byte array
    jbyteArray data = env->NewByteArray((jsize)dataSize);
    if (dataSize > 0) {
        env->SetByteArrayRegion(data, 0, (jsize)dataSize, (const jbyte*)dataArray);
    }

    size_t marksSize = marksNames.size();
    jlongArray positions = env->NewLongArray((jsize)marksSize);
    if (marksSize > 0) {
        env->SetLongArrayRegion(positions, 0, (jsize)marksSize, marksPositionsArray);
    }

    jobjectArray names = newJavaArray<Iterator,StringToJString<Iterator>>(
        env,
        marksNames.begin(),
        marksSize,
        "java/lang/String"
    );

    jclass resClass = env->FindClass("org/daisy/pipeline/tts/sapinative/NativeSynthesisResult");
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

inline jobject getLogger(JNIEnv* env, std::wstring name) {
    jclass loggerFactory = env->FindClass("org/slf4j/LoggerFactory");
    jmethodID getLogger = env->GetStaticMethodID(loggerFactory, "getLogger", "(Ljava/lang/String;)Lorg/slf4j/Logger;");
    return env->CallStaticObjectMethod(
        loggerFactory,
        getLogger,
        env->NewString((const jchar*)name.c_str(), static_cast<jsize>(std::wcslen(name.c_str())))
    );
}

inline void log(JNIEnv* env, jclass loggerClass, jobject logger, const char* method, std::wstring msg) {
    env->CallObjectMethod( 
        logger,
        env->GetMethodID(loggerClass, method, "(Ljava/lang/String;)V"),
        env->NewString((const jchar*)msg.c_str(), static_cast<jsize>(std::wcslen(msg.c_str())))
    );
}

#endif