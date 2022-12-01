import { ipcMain } from 'electron'
import { IPC } from 'shared/constants'
import { writeFile } from 'fs/promises'
import { info } from 'electron-log'
import { dirname, resolve } from 'path'
import { fileURLToPath } from 'url'
import { existsSync, mkdirSync } from 'fs'

import decompress, { File as DecompressedFile } from 'decompress'

export function registerFileIPC() {
    ipcMain.handle(
        IPC.FILE.SAVE,
        async (event, buffer: ArrayBuffer, pathFileURL: string) => {
            const systemPath = fileURLToPath(pathFileURL)
            console.log('saving to ', systemPath)
            if (!existsSync(dirname(systemPath))) {
                mkdirSync(dirname(systemPath), { recursive: true })
            }
            return writeFile(systemPath, Buffer.from(buffer))
                .catch((e) => {
                    info(
                        'An error occured writing file ',
                        pathFileURL,
                        ' : ',
                        e
                    )
                    return false
                })
                .then((v) => {
                    return true
                })
        }
    )

    ipcMain.handle(
        IPC.FILE.UNZIP,
        async (event, buffer: ArrayBuffer, pathFileURL: string) => {
            const systemPath = fileURLToPath(pathFileURL)
            console.log('unzipping to ', systemPath)
            if (!existsSync(systemPath)) {
                mkdirSync(systemPath, { recursive: true })
            }
            return decompress(Buffer.from(buffer), systemPath)
                .then((files: DecompressedFile[]) =>
                    files.map((file) => resolve(systemPath, file.path))
                )
                .catch((error) => {
                    return null
                })
        }
    )

    ipcMain.on(IPC.FILE.OPEN, (event, path) => {
        // Request the opening of a file in the system
    })
}
