import { ipcRenderer } from 'electron'

import { IPC } from 'shared/constants'

export function stopPipeline() {
    const channel = IPC.PIPELINE.STOP

    ipcRenderer.send(channel)
}
