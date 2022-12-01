import { info } from 'electron-log'
import { readdirSync, statSync } from 'fs'
import { resolve } from 'path'
import { createServer } from 'node:net'
import { setTimeout } from 'timers/promises'

/**
 *
 */
export class Pipeline2Error extends Error {
    name: string

    constructor(name: string, message?: string, options?: ErrorOptions) {
        super(message, options)
        this.name = name
    }
}

/**
 * recursive listing of files in a folder with filtering
 * @param {string} dir the directory to list files from
 * @param {function(string)} filter filter callback, that should return true if a file is matching it
 * @returns {string[]} the list of file path matching the filter
 */
export function walk(
    dir: string,
    filter?: (name: string) => boolean
): string[] {
    let results: string[] = []
    let list = readdirSync(dir)
    list.forEach(function (file) {
        file = resolve(dir, file)
        let stat = statSync(file)
        if (stat && stat.isDirectory()) {
            /* Recurse into a subdirectory */
            results = results.concat(walk(file))
        } else {
            /* Is a file */
            ;(!filter || filter(file)) && results.push(file)
        }
    })
    return results
}

// Dev notes :
// - port seeking in nodejs default to ipv6 ':::' for unset or localhost hostname
// - ipv4 and 6 do not share ports (based on some tests, one app could listen to an ipv4 port while another listen to the same on the ipv6 side)
//
// Some comments on SOF say that hostname resolution is OS dependent, but some clues on github issues for nodejs
// states that the resolution for 'localhost' defaults to ipv6, starting a version of nodejs i can't remember the number

/**
 * seek for an opened port, or return null if none is available
 *
 * @param startPort
 * @param endPort
 * @param host optional hostname or ip adress (default to 127.0.0.1)
 */
export async function getAvailablePort(
    startPort: number,
    endPort: number,
    host: string = '127.0.0.1'
) {
    let server = createServer()
    let portChecked = startPort
    let portOpened = 0

    // Port seeking : if port is in use, retry with a different port
    server.on('error', (err: NodeJS.ErrnoException) => {
        info(`Port ${portChecked.toString()} is not usable : `)
        info(err)
        portChecked += 1
        if (portChecked <= endPort) {
            info(' -> Checking for ' + portChecked.toString())
            server.listen(portChecked, host)
        } else {
            throw new Pipeline2Error(
                'NO_PORT',
                'No port available to host the pipeline webservice'
            )
        }
    })
    // Listening successfully on a port
    server.on('listening', (event) => {
        // close the server if listening a port succesfully
        server.close(() => {
            // select the port when the server is closed
            portOpened = portChecked
        })
        info(portChecked.toString() + ' is available')
    })
    // Start the port seeking
    server.listen(portChecked, host)
    while (portOpened == 0 && portChecked <= endPort) {
        await setTimeout(1000)
    }
    return portOpened
}
