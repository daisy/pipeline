import { app, BrowserWindow, ipcMain } from 'electron'
import { ApplicationSettings } from 'shared/types'
import { resolve } from 'path'
import { existsSync, readFileSync, writeFile } from 'fs'
import { resolveUnpacked } from 'shared/utils'
import { IPC } from 'shared/constants'
import { pathToFileURL } from 'url'
import { info } from 'electron-log'

export function registerApplicationSettingsIPC(): ApplicationSettings {
    // Load settings file from current folder
    const settingsFile = resolve(app.getPath('userData'), 'settings.json')
    let settings: ApplicationSettings = {
        // Default folder to download the results on the user disk
        downloadFolder: pathToFileURL(
            resolve(app.getPath('home'), 'Documents', 'DAISY Pipeline results')
        ).href,
        // Local pipeline server
        // - Run or not a local pipeline server
        runLocalPipeline: true,
        // - Local pipeline settings
        localPipelineProps: {
            localPipelineHome: resolveUnpacked('resources', 'daisy-pipeline'),
            jrePath: resolveUnpacked('resources', 'daisy-pipeline', 'jre'),
            // Note : [49152 ; 65535] is the range of dynamic port,  0 is reserved for error case
            webservice: {
                // Note : localhost resolve as ipv6 ':::' in nodejs, but we need ipv4 for the pipeline
                host: '127.0.0.1',
                port: 0,
                path: '/ws',
            },
            appDataFolder: app.getPath('userData'),
            logsFolder: resolve(app.getPath('userData'), 'pipeline-logs'),
        },
        // Remote pipeline settings
        // - Use a remote pipeline instead of the local one
        useRemotePipeline: false,
        // - Remote pipeline connection settings to be defined
        /*remotePipelineWebservice: {
          
        }*/
    }
    // try to load settings file
    try {
        if (existsSync(settingsFile)) {
            settings = JSON.parse(readFileSync(settingsFile, 'utf8'))
        }
    } catch (e) {
        info('Error when trying to parse settings file')
        info(e)
        info('Falling back to default settings')
    }
    // get state from the instance
    ipcMain.handle(IPC.WINDOWS.SETTINGS.GET, (event) => {
        return settings
    })

    ipcMain.on(IPC.WINDOWS.SETTINGS.UPDATE, (event, newSettings) => {
        // Save settings on disk
        writeFile(settingsFile, JSON.stringify(newSettings, null, 4), () => {
            settings = newSettings
            // Send back settings update to all windows
            BrowserWindow.getAllWindows().forEach((window) => {
                window.webContents.send(IPC.WINDOWS.SETTINGS.CHANGED, settings)
            })
        })
    })

    return settings
}
