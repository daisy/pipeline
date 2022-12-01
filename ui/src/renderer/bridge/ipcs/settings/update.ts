import { ipcRenderer } from 'electron'

import { IPC } from 'shared/constants'
import { ApplicationSettings } from 'shared/types'

/**
 * Send the new application settings to the backend
 */
export function saveSettings(newSettings: ApplicationSettings) {
    const channel = IPC.WINDOWS.SETTINGS.UPDATE
    ipcRenderer.send(channel, newSettings)
}
