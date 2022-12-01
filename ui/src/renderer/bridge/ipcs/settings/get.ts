import { ipcRenderer } from 'electron'

import { IPC } from 'shared/constants'
import { ApplicationSettings } from 'shared/types'

/**
 * Retrieve the settings of the application
 * @returns a promise containing the settings, or null if no settings were saved before
 */
export function getSettings(): Promise<ApplicationSettings | null> {
    const channel = IPC.WINDOWS.SETTINGS.GET
    return ipcRenderer.invoke(channel)
}
