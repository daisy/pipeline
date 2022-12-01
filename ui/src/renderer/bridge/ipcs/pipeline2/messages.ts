import { ipcRenderer } from 'electron'

import { IPC } from 'shared/constants'

export function onPipelineMessage(callback) {
    const channel = IPC.PIPELINE.MESSAGES.UPDATE

    return ipcRenderer.on(channel, callback)
}

export function getPipelineMessages(): Promise<Array<string> | null> {
    const channel = IPC.PIPELINE.MESSAGES.GET

    return ipcRenderer.invoke(channel)
}
