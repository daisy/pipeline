#pragma once

#define WIN32_LEAN_AND_MEAN             // Exclure les en-têtes Windows rarement utilisés
// Fichiers d'en-tête Windows
#define _ATL_APARTMENT_THREADED

#include <atlbase.h>
////You may derive a class from CComModule and use it if you want to override something,
////but do not change the name of _Module
extern CComModule _Module;
#include <atlcom.h>

#include <map>
#include <string> //must come first because it uses variables called "__in" overridden after
#include <vector>
#include <functional>


//#import "libid:C866CA3A-32F7-11D2-9602-00C04F8EE628" named_guids rename_namespace("SAPI") //v5.4
#include <sapi.h>
#include <sperror.h>

#include <Shlwapi.h>

#include <jni.h>

