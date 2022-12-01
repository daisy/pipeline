import {
    BrowserWindowConstructorOptions,
    IpcMainInvokeEvent,
    BrowserWindow,
} from 'electron'
import { Pipeline2IPCProps } from 'main/factories'
import { Webservice } from './pipeline'

export type BrowserWindowOrNull = Electron.BrowserWindow | null

export interface WindowProps extends BrowserWindowConstructorOptions {
    id: string
}

export interface WindowCreationByIPC {
    channel: string
    window(): BrowserWindowOrNull
    callback(window: BrowserWindow, event: IpcMainInvokeEvent): void
}

export interface ApplicationSettings {
    // Default folder to download the results on the user disk
    downloadFolder?: string
    // Local pipeline server
    // - Run or not a local pipeline server
    runLocalPipeline?: boolean
    // - Local pipeline settings
    localPipelineProps?: Pipeline2IPCProps
    // Remote pipeline settings
    // - Use a remote pipeline instead of the local one
    useRemotePipeline?: boolean
    // - Remote pipeline connection settings
    remotePipelineWebservice?: Webservice
}

export * from './pipeline'
