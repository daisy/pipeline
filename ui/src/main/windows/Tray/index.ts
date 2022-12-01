import {
    app,
    Menu,
    Tray,
    BrowserWindow,
    ipcMain,
    ipcRenderer,
    nativeImage,
} from 'electron'
import { APP_CONFIG } from '~/app.config'
import { resolve } from 'path'
import {
    bindWindowToPipeline,
    makeAppSetup,
    makeAppWithSingleInstanceLock,
    Pipeline2IPC,
} from '../../factories'
import { MainWindow, AboutWindow } from '../../windows'
import { ENVIRONMENT, IPC } from 'shared/constants'
import { PipelineState, PipelineStatus } from 'shared/types'
import { resolveUnpacked } from 'shared/utils'

export class PipelineTray {
    tray: Tray
    mainWindow: BrowserWindow
    pipeline?: Pipeline2IPC = null
    menuBaseTemplate: Array<Electron.MenuItemConstructorOptions> = []
    pipelineMenu: Array<Electron.MenuItemConstructorOptions> = []

    constructor(mainWindow: BrowserWindow, pipeline?: Pipeline2IPC) {
        const icon = nativeImage.createFromPath(
            resolveUnpacked('resources', 'icons', 'logo_32x32.png')
        )
        this.tray = new Tray(icon)
        this.mainWindow = mainWindow
        this.menuBaseTemplate = [
            // Note : uncomment if we want those window
            // {
            //     label: 'About',
            //     click: async (item, window, event) => {
            //         ipcMain.emit(IPC.WINDOWS.ABOUT.CREATE)
            //     },
            // },
            {
                label: 'Settings',
                click: async (item, window, event) => {
                    ipcMain.emit(IPC.WINDOWS.SETTINGS.CREATE)
                },
            },
            {
                label: 'Quit',
                click: (item, window, event) => {
                    BrowserWindow.getAllWindows().forEach((window) =>
                        window.destroy()
                    )
                    pipeline && pipeline.stop(true)
                    app.quit()
                },
            },
        ]

        if (pipeline) {
            this.bindToPipeline(pipeline)
        } else {
            this.pipelineMenu = [
                {
                    label: 'Pipeline could not be launched',
                    enabled: false,
                },
            ]
            this.tray.setToolTip('DAISY Pipeline 2')
            this.tray.setContextMenu(
                Menu.buildFromTemplate([
                    ...this.pipelineMenu,
                    ...this.menuBaseTemplate,
                ])
            )
        }
    }

    /**
     * Bind a pipeline instance to the tray to allow interactions
     * @param pipeline
     */
    bindToPipeline(pipeline: Pipeline2IPC) {
        // setup listeners to update tray based on states
        pipeline.registerStateListener('tray', (newState) => {
            this.updateElectronTray(newState, pipeline)
        })
        this.updateElectronTray(pipeline.state, pipeline)
    }

    /**
     * Update the tray based on a given pipeline state
     * @param newState State to use for tray configuration
     * @param pipeline pipeline instance to be controled by the tray actions
     */
    updateElectronTray(newState: PipelineState, pipeline: Pipeline2IPC) {
        this.pipelineMenu = [
            {
                label: `Pipeline is ${
                    newState.status == PipelineStatus.STARTING ||
                    newState.status == PipelineStatus.RUNNING
                        ? 'running'
                        : 'stopped'
                }`,
                enabled: false,
            },
            {
                label: 'Create a job',
                enabled:
                    newState.status == PipelineStatus.STARTING ||
                    newState.status == PipelineStatus.RUNNING,
                click: async (item, window, event) => {
                    try {
                        this.mainWindow.show()
                    } catch (error) {
                        this.mainWindow = await MainWindow()
                        bindWindowToPipeline(this.mainWindow, pipeline)
                    }
                    // Note : this triggers a refresh
                    // ENVIRONMENT.IS_DEV
                    //     ? this.mainWindow.loadURL(
                    //           `${APP_CONFIG.RENDERER.DEV_SERVER.URL}#/main`
                    //       )
                    //     : this.mainWindow.loadFile('index.html', {
                    //           hash: `/main`,
                    //       })
                },
            },
        ]
        this.tray.setToolTip(
            'DAISY Pipeline 2 is ' +
                (newState.status == PipelineStatus.STARTING ||
                newState.status == PipelineStatus.RUNNING
                    ? 'running'
                    : 'stopped')
        )
        // Update tray
        this.tray.setContextMenu(
            Menu.buildFromTemplate([
                ...this.pipelineMenu,
                ...this.menuBaseTemplate,
            ])
        )
    }
}
