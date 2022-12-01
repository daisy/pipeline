import { ipcRenderer } from 'electron'
import * as events from 'shared/main-renderer-events'

export function showItemInFolder(payload) {
    return new Promise((resolve, reject) => {
        ipcRenderer.send(events.IPC_EVENT_showItemInFolder, payload)
    })
}
