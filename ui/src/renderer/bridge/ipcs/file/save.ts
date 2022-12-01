import { ipcRenderer } from 'electron'

import { IPC } from 'shared/constants'

export function saveFile(buffer: ArrayBuffer, pathFileURL: string) {
    const channel = IPC.FILE.SAVE
    return ipcRenderer.invoke(channel, buffer, pathFileURL)
}
