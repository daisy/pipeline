import {
    app,
    BrowserWindow,
    ipcMain,
    Menu,
    MenuItemConstructorOptions,
    shell,
} from 'electron'

import { error } from 'electron-log'

import {
    bindWindowToPipeline,
    makeAppSetup,
    makeAppWithSingleInstanceLock,
    Pipeline2IPC,
    registerApplicationSettingsIPC,
    registerPipeline2ToIPC,
} from './factories'

import {
    MainWindow,
    PipelineTray,
    registerAboutWindowCreationByIPC,
    registerSettingsWindowCreationByIPC,
} from './windows'

import { setupFileDialogEvents } from './fileDialogs'
import { ENVIRONMENT, IPC } from 'shared/constants'
import { setupShowInFolderEvents } from './folder'
import { registerFileIPC } from './factories/ipcs/file'
import { setupFileSystemEvents } from './fileSystem'
import { setupOpenInBrowserEvents } from './browser'
import { APP_CONFIG } from '~/app.config'

makeAppWithSingleInstanceLock(async () => {
    await app.whenReady()
    // Windows
    let mainWindow = await makeAppSetup(MainWindow)
    registerSettingsWindowCreationByIPC()
    registerAboutWindowCreationByIPC()
    registerFileIPC()
    // Settings
    let settings = registerApplicationSettingsIPC()

    // Pipeline instance creation with IPC communication registering
    const pipelineInstance = registerPipeline2ToIPC(settings)
    bindWindowToPipeline(mainWindow, pipelineInstance)

    let tray: PipelineTray = null
    try {
        tray = new PipelineTray(mainWindow, pipelineInstance)
    } catch (err) {
        error(err)
        // quit app for now but we might need to think for a better handling for the user
        app.quit()
    }
    setupFileDialogEvents()
    setupShowInFolderEvents()
    setupOpenInBrowserEvents()
    setupFileSystemEvents()

    const isMac = process.platform === 'darwin'

    // Template taken from electron documentation
    // To be completed
    // @ts-ignore
    const template: MenuItemConstructorOptions = [
        // { role: 'appMenu' }
        ...(isMac
            ? [
                  {
                      label: app.name,
                      submenu: [
                          { role: 'services' },
                          { type: 'separator' },
                          { role: 'hide' },
                          { role: 'hideOthers' },
                          { role: 'unhide' },
                          { type: 'separator' },
                          { role: 'quit' },
                      ],
                  },
              ]
            : []),
        // { role: 'fileMenu' }
        {
            label: 'File',
            submenu: [
                {
                    label: 'Create a new job',
                    click: async () => {
                        try {
                            mainWindow.show()
                        } catch (error) {
                            mainWindow = await MainWindow()
                            bindWindowToPipeline(mainWindow, pipelineInstance)
                        }
                    },
                },
                {
                    label: 'Settings',
                    click: async () => {
                        // Open the settings window
                        ipcMain.emit(IPC.WINDOWS.SETTINGS.CREATE)
                    },
                },
                { type: 'separator' },
                isMac ? { role: 'close' } : { role: 'quit' },
            ],
        },
        {
            label: 'Edit',
            submenu: [{ role: 'copy' }, { role: 'paste' }],
        },
        {
            label: 'View',
            submenu: [
                { role: 'resetZoom' },
                { role: 'zoomIn' },
                { role: 'zoomOut' },
            ],
        },
        {
            role: 'help',
            submenu: [
                {
                    label: 'Learn more',
                    click: async () => {
                        await shell.openExternal(
                            'https://daisy.github.io/pipeline/'
                        )
                    },
                },
                {
                    label: 'User guide',
                    click: async () => {
                        await shell.openExternal(
                            'https://daisy.github.io/pipeline/Get-Help/'
                        )
                    },
                },
            ],
        },
    ]

    // @ts-ignore
    const menu = Menu.buildFromTemplate(template)
    Menu.setApplicationMenu(menu)
})
