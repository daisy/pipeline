import { ipcMain } from 'electron'

import { registerWindowCreationByIPC } from 'main/factories'
import { IPC } from 'shared/constants'
import { AboutWindow } from '..'

export function registerAboutWindowCreationByIPC() {
    registerWindowCreationByIPC({
        channel: IPC.WINDOWS.ABOUT.CREATE,
        window: AboutWindow,

        callback(window, event) {
            const channel = IPC.WINDOWS.ABOUT.WHEN_CLOSE
            ipcMain.removeHandler(channel)
            window.on(
                'closed',
                () =>
                    event &&
                    event.sender &&
                    event.sender.send(channel, {
                        message: 'About window closed!',
                    })
            )
        },
    })
}
