import { ipcRenderer } from 'electron'

import { IPC } from 'shared/constants'

export function onPipelineError(callback) {
    const channel = IPC.PIPELINE.ERRORS.UPDATE

    return ipcRenderer.on(channel, callback)
}

export function getPipelineErrors(): Promise<Array<string> | null> {
    const channel = IPC.PIPELINE.ERRORS.GET

    return ipcRenderer.invoke(channel)
}
