#pragma once
#include <unknwn.h>
#include <sstream>
#include <cwctype>
#include <algorithm>
#include <winrt/Windows.Foundation.h>
#include <winrt/Windows.Foundation.Collections.h>
#include <winrt/Windows.Media.SpeechSynthesis.h>
#include <winrt/Windows.Media.Core.h>
#include <winrt/Windows.Media.Playback.h>
#include <winrt/Windows.Storage.Streams.h>
#include <winrt/Windows.Data.Xml.Dom.h>

#include <jni.h>

#include "../Voice.hpp"
#include "../jni_helper.h"
#include "org_daisy_pipeline_tts_onecore_Onecore.h"
#include "org_daisy_pipeline_tts_onecore_OnecoreResult.h"

#include <iostream>


#include <winrt/onecorenative.h>
