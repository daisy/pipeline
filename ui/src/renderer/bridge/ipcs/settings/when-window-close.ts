import { ipcRenderer } from 'electron'

import { IPC } from 'shared/constants'

export function whenSettingsWindowClose(fn: (...args: any[]) => void) {
    const channel = IPC.WINDOWS.SETTINGS.WHEN_CLOSE

    ipcRenderer.on(channel, (_, ...args) => {
        fn(...args)
    })
}
