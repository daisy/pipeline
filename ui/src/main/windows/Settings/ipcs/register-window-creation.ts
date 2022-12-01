import { ipcMain } from 'electron'

import { registerWindowCreationByIPC } from 'main/factories'
import { IPC } from 'shared/constants'
import { SettingsWindow } from '..'

export function registerSettingsWindowCreationByIPC() {
    registerWindowCreationByIPC({
        channel: IPC.WINDOWS.SETTINGS.CREATE,
        window: SettingsWindow,

        callback(window, event) {
            const channel = IPC.WINDOWS.SETTINGS.WHEN_CLOSE

            ipcMain.removeHandler(channel)

            window.on(
                'closed',
                () =>
                    event &&
                    event.sender &&
                    event.sender.send(channel, {
                        message: 'Settings window closed!',
                    })
            )
        },
    })
}
