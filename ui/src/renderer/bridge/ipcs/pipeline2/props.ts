import { ipcRenderer } from 'electron'
import { Pipeline2IPCProps } from 'main/factories'

import { IPC } from 'shared/constants'

export function getPipelineProps(): Promise<Pipeline2IPCProps | null> {
    const channel = IPC.PIPELINE.PROPS.GET
    return ipcRenderer.invoke(channel)
}
