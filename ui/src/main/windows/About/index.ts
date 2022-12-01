import { createWindow } from 'main/factories'
import { join } from 'path'
import { APP_CONFIG } from '~/app.config'

export * from './ipcs'

export function AboutWindow() {
    const window = createWindow({
        id: 'about',
        title: `${APP_CONFIG.TITLE} - About`,
        width: 450,
        height: 350,
        resizable: false,
        alwaysOnTop: true,

        webPreferences: {
            preload: join(__dirname, 'bridge.js'),
            nodeIntegration: false,
            contextIsolation: true,
            spellcheck: false,
            sandbox: false,
        },
    })

    return window
}
