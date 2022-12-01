import { ipcRenderer } from 'electron'
import * as events from 'shared/main-renderer-events'

export function openInBrowser(payload) {
    ipcRenderer.send(events.IPC_EVENT_openInBrowser, payload)
}
