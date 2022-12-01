// various file system utility functions

import { ipcMain } from 'electron'
import fs from 'fs-extra'

// helper functions
const { IPC_EVENT_pathExists } = require('../shared/main-renderer-events')

async function pathExists(path) {
    await fs.access(path, (err) => {
        if (err) {
            return false
        } else {
            return true
        }
    })
}

function setupFileSystemEvents() {
    // comes from the renderer process (ipcRenderer.send())
    ipcMain.on(IPC_EVENT_pathExists, async (event, payload) => {
        let res = await pathExists(payload)
        event.sender.send(IPC_EVENT_pathExists, res)
    })
}
export { setupFileSystemEvents, pathExists }
