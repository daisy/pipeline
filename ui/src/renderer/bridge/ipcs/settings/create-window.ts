import { ipcRenderer } from 'electron'

import { IPC } from 'shared/constants'

export function createSettingsWindow() {
    const channel = IPC.WINDOWS.SETTINGS.CREATE

    ipcRenderer.invoke(channel)
}
