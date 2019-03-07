!macro ZIPDLL_EXTRACT SOURCE DESTINATION FILE
  !define "FILE_${FILE}"
  !ifndef FILE_<ALL>
    Push "${FILE}"
  !endif
  IfFileExists "${DESTINATION}" +2
    CreateDirectory "${DESTINATION}"
  Push "${DESTINATION}"
  IfFileExists "${SOURCE}" +2
    SetErrors
  Push "${SOURCE}"
  !ifdef FILE_<ALL>
    ZipDLL::extractall
  !else
    ZipDLL::extractfile
  !endif
  !undef "FILE_${FILE}"
!macroend
