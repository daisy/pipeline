import { ipcRenderer } from 'electron'

import { IPC } from 'shared/constants'

/**
 * Update the settings of the application on the backend
 * @returns a promise containing the settings, or null if no settings were saved before
 */
export function onSettingsChanged(callback) {
    const channel = IPC.WINDOWS.SETTINGS.CHANGED
    return ipcRenderer.on(channel, callback)
}
