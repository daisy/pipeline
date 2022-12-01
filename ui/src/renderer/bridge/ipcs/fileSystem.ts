import { ipcRenderer } from 'electron'
import * as events from 'shared/main-renderer-events'

export function pathExists(path) {
    return new Promise<boolean>((resolve, reject) => {
        ipcRenderer.send(events.IPC_EVENT_pathExists, path)
        ipcRenderer.once(events.IPC_EVENT_pathExists, (event, res: boolean) => {
            resolve(res)
        })
    })
}
