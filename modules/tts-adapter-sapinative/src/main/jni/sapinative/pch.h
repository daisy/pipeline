// pch.h : Il s'agit d'un fichier d'en-tête précompilé.
// Les fichiers listés ci-dessous sont compilés une seule fois, ce qui améliore les performances de génération des futures builds.
// Cela affecte également les performances d'IntelliSense, notamment la complétion du code et de nombreuses fonctionnalités de navigation du code.
// Toutefois, les fichiers listés ici sont TOUS recompilés si l'un d'entre eux est mis à jour entre les builds.
// N'ajoutez pas de fichiers fréquemment mis à jour ici, car cela annule les gains de performance.

#ifndef PCH_H
#define PCH_H

#include <cwctype>
#include <algorithm>
#include <stack>
// ajouter les en-têtes à précompiler ici
#include "framework.h"
#include "queue_stream.h"

#include "org_daisy_pipeline_tts_sapinative_SAPI.h"
#include "org_daisy_pipeline_tts_sapinative_SAPIResult.h"

// shared includes
#include "../jni_helper.h"
#include "../Voice.hpp"

#endif //PCH_H
