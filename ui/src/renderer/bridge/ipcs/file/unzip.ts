import { ipcRenderer } from 'electron'

import { IPC } from 'shared/constants'

export function unzipFile(buffer: ArrayBuffer, pathFileURL: string) {
    const channel = IPC.FILE.UNZIP
    return ipcRenderer.invoke(channel, buffer, pathFileURL)
}
