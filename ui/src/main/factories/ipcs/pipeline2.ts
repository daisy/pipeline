import { app, ipcMain } from 'electron'
import { resolve, delimiter, relative } from 'path'
import {
    Webservice,
    PipelineStatus,
    PipelineState,
    ApplicationSettings,
} from 'shared/types'
import { ENVIRONMENT, IPC } from 'shared/constants'
import { setTimeout } from 'timers/promises'
import { spawn, ChildProcessWithoutNullStreams } from 'child_process'
import { existsSync, mkdir, mkdirSync } from 'fs'

import { getAvailablePort, Pipeline2Error, walk } from './utils'

import { resolveUnpacked } from 'shared/utils'

import { info, error } from 'electron-log'

import { request as httpRequest } from 'http'
import { pathToFileURL } from 'url'
// NP : for future use if we want to use the app
// to also manage a pipeline behind https
//import { request as httpsRequest } from 'https'

/**
 * Properties for initializing ipc with the daisy pipeline 2
 *
 */
export interface Pipeline2IPCProps {
    /**
     * optional path of the local installation of the pipeline,
     *
     * defaults to the application resources/daisy-pipeline
     */
    localPipelineHome?: string

    appDataFolder?: string

    logsFolder?: string
    /**
     * optional path to the java runtime
     *
     * defaults to the application resource/jre folder
     */
    jrePath?: string

    /**
     * Webservice configuration to use for embedded pipeline,
     *
     * defaults to a localhost managed configuration :
     * ```js
     * {
     *      host: "localhost"
     *      port: 0, // will search for an available port on the current host when calling launch() the first time
     *      path: "/ws"
     * }
     * ```
     *
     */
    webservice?: Webservice

    /**
     *
     */
    onError?: (error: string) => void

    onMessage?: (message: string) => void
}

/**
 * Local DAISY Pipeline 2 management class
 */
export class Pipeline2IPC {
    props: Pipeline2IPCProps
    // Default state
    state: PipelineState
    stateListeners: Map<string, (data: PipelineState) => void> = new Map<
        string,
        (data: PipelineState) => void
    >()
    runStateMonitor: boolean = true

    messages: Array<string>
    messagesListeners: Map<string, (data: string) => void> = new Map<
        string,
        (data: string) => void
    >()

    errors: Array<string>
    errorsListeners: Map<string, (data: string) => void> = new Map<
        string,
        (data: string) => void
    >()

    private instance?: ChildProcessWithoutNullStreams
    /**
     *
     * @param parameters
     */
    constructor(props?: Pipeline2IPCProps) {
        const osAppDataFolder = app.getPath('userData')
        this.props = {
            localPipelineHome:
                (props && props.localPipelineHome) ??
                resolveUnpacked('resources', 'daisy-pipeline'),
            jrePath:
                (props && props.jrePath) ??
                resolveUnpacked('resources', 'daisy-pipeline', 'jre'),
            // Note : [49152 ; 65535] is the range of dynamic port,  0 is reserved for error case
            webservice: (props && props.webservice) ?? {
                host: '127.0.0.1', // Note : localhost resolve as ipv6 ':::' in nodejs, but we need ipv4 for the pipeline
                port: 0,
                path: '/ws',
            },
            appDataFolder:
                (props && props.appDataFolder) ?? app.getPath('userData'),
            logsFolder:
                (props && props.logsFolder) ??
                resolve(app.getPath('userData'), 'pipeline-logs'),
            onError: (props && props.onError) || error,
            onMessage: (props && props.onMessage) || info,
        }
        this.instance = null
        this.errors = []
        this.messages = []
        this.setState({
            status: PipelineStatus.STOPPED,
        })
        this.stateMonitor = this.stateMonitor.bind(this)
    }

    /**
     * Monitor function to watch the webservice state
     */
    async stateMonitor(refreshTimerInMillisecondes = 1000) {
        while (this.runStateMonitor) {
            await setTimeout(refreshTimerInMillisecondes).then(async () => {
                return new Promise<string>((resolve, reject) => {
                    const options = {
                        host: this.props.webservice.host,
                        port: this.props.webservice.port,
                        path: this.props.webservice.path + '/alive',
                    }
                    const callback = (response) => {
                        var data = ''
                        response.on('data', (chunk) => {
                            data += chunk
                        })
                        response.on('end', () => {
                            resolve(data)
                        })
                    }
                    const req = httpRequest(options, callback)
                    req.on('error', (errorVal) => {
                        reject(errorVal)
                    })
                    req.end()
                })
                    .then((value) => {
                        if (this.state.status != PipelineStatus.RUNNING) {
                            this.setState({
                                status: PipelineStatus.RUNNING,
                            })
                        }
                    })
                    .catch(() => {
                        // if pipeline went from running to offline
                        if (this.state.status === PipelineStatus.RUNNING) {
                            this.setState({
                                status: PipelineStatus.STOPPED,
                            })
                        }
                    })
            })
        }
    }

    /**
     * Change the webservice configuration (stop and restart the server if so)
     * @param webservice
     */
    updateWebservice(webservice: Webservice) {
        this.stop().then(() => {
            this.props.webservice = webservice
            this.launch()
        })
    }

    setState(newState: {
        runningWebservice?: Webservice
        status?: PipelineStatus
    }) {
        this.state = {
            runningWebservice:
                newState.runningWebservice ??
                (this.state && this.state.runningWebservice),
            status:
                newState.status ??
                ((this.state && this.state.status) || PipelineStatus.STOPPED),
        }
        for (const [callerID, callback] of this.stateListeners) {
            callback(this.state)
        }
    }
    pushMessage(message: string) {
        this.messages.push(message)
        if (this.props.onMessage) {
            this.props.onMessage(message)
        }
        this.messagesListeners.forEach((callback) => {
            callback(message)
        })
    }
    pushError(message: string) {
        this.errors.push(message)
        if (this.props.onError) {
            this.props.onError(message)
        }
        this.errorsListeners.forEach((callback) => {
            callback(message)
        })
    }

    /**
     * Launch a local instance of the pipeline using the current webservice settings
     */
    async launch(): Promise<PipelineState> {
        if (!this.instance || this.state.status == PipelineStatus.STOPPED) {
            this.setState({
                status: PipelineStatus.STARTING,
            })
            if (
                this.props.webservice.port !== undefined &&
                this.props.webservice.port === 0
            ) {
                info('Searching for an valid port')
                try {
                    await getAvailablePort(
                        49152,
                        65535,
                        this.props.webservice.host
                    )
                        .then(
                            ((port) => {
                                this.props.webservice.port = port
                                //
                                if (
                                    this.props.jrePath === null ||
                                    !existsSync(this.props.jrePath)
                                ) {
                                    throw new Pipeline2Error(
                                        'NO_JRE',
                                        'No jre found to launch the pipeline'
                                    )
                                }

                                if (
                                    this.props.localPipelineHome === null ||
                                    !existsSync(this.props.jrePath)
                                ) {
                                    throw new Pipeline2Error(
                                        'NO_PIPELINE',
                                        'No pipeline installation found'
                                    )
                                }
                            }).bind(this)
                        )
                        .catch((err) => {
                            // propagate exception for now
                            throw err
                        })
                } catch (error) {
                    this.pushError(error)
                    // no port available, try to use the usual 8181
                    this.props.webservice.port = 8181
                }
            }
            info(
                `Launching pipeline on ${this.props.webservice.host}:${this.props.webservice.port}`
            )
            let ClassFolders = [
                resolve(this.props.localPipelineHome, 'system'),
                resolve(this.props.localPipelineHome, 'modules'),
            ]
            let jarFiles = ClassFolders.reduce(
                (acc: Array<string>, path: string) => {
                    existsSync(path) &&
                        acc.push(
                            ...walk(path, (name) => {
                                return name.endsWith('.jar')
                            })
                        )
                    return acc
                },
                []
            )
            let relativeJarFiles = jarFiles.reduce(
                (acc: Array<string>, path: string) => {
                    let relativeDirPath = relative(
                        this.props.localPipelineHome,
                        path
                    )
                    if (!acc.includes(relativeDirPath)) {
                        acc.push(relativeDirPath)
                    }
                    return acc
                },
                []
            )

            let JavaOptions = [
                '-server',
                '-Dcom.sun.management.jmxremote',
                '--add-opens=java.base/java.security=ALL-UNNAMED',
                '--add-opens=java.base/java.net=ALL-UNNAMED',
                '--add-opens=java.base/java.lang=ALL-UNNAMED',
                '--add-opens=java.base/java.util=ALL-UNNAMED',
                '--add-opens=java.naming/javax.naming.spi=ALL-UNNAMED',
                '--add-opens=java.rmi/sun.rmi.transport.tcp=ALL-UNNAMED',
                '--add-exports=java.base/sun.net.www.protocol.http=ALL-UNNAMED',
                '--add-exports=java.base/sun.net.www.protocol.https=ALL-UNNAMED',
                '--add-exports=java.base/sun.net.www.protocol.jar=ALL-UNNAMED',
                '--add-exports=jdk.xml.dom/org.w3c.dom.html=ALL-UNNAMED',
                '--add-exports=jdk.naming.rmi/com.sun.jndi.url.rmi=ALL-UNNAMED',
            ]

            let SystemProps = [
                '-Dorg.daisy.pipeline.properties="' +
                    resolve(
                        this.props.localPipelineHome,
                        'etc',
                        'pipeline.properties'
                    ) +
                    '"',
                // Logback configuration file
                '-Dlogback.configurationFile=' +
                    pathToFileURL(
                        resolve(
                            this.props.localPipelineHome,
                            'etc',
                            'config-logback.xml'
                        )
                    ).href +
                    '',
                // XMLCalabash base configuration file
                '-Dorg.daisy.pipeline.xproc.configuration="' +
                    resolve(
                        this.props.localPipelineHome,
                        'etc',
                        'config-calabash.xml'
                    ).replaceAll('\\', '/') +
                    '"',
                // Updater configuration
                '-Dorg.daisy.pipeline.updater.bin="' +
                    resolve(
                        this.props.localPipelineHome,
                        'updater',
                        'pipeline-updater'
                    ).replaceAll('\\', '/') +
                    '"',
                '-Dorg.daisy.pipeline.updater.deployPath="' +
                    this.props.localPipelineHome.replaceAll('\\', '/') +
                    '/"',
                '-Dorg.daisy.pipeline.updater.releaseDescriptor="' +
                    resolve(
                        this.props.localPipelineHome,
                        'etc',
                        'releaseDescriptor.xml'
                    ).replaceAll('\\', '/') +
                    '"',
                // Workaround for encoding bugs on Windows
                '-Dfile.encoding=UTF8',
                // to make ${org.daisy.pipeline.data}, ${org.daisy.pipeline.logdir} and ${org.daisy.pipeline.mode}
                // available in config-logback.xml and felix.properties
                // note that config-logback.xml is the only place where ${org.daisy.pipeline.mode} is used
                '-Dorg.daisy.pipeline.data=' + this.props.appDataFolder + '',
                '-Dorg.daisy.pipeline.logdir=' + this.props.logsFolder + '',
                '-Dorg.daisy.pipeline.mode=webservice',
                '-Dorg.daisy.pipeline.ws.localfs=true',
                '-Dorg.daisy.pipeline.ws.authentication=false',
                '-Dorg.daisy.pipeline.ws.host=' + this.props.webservice.host,
                '-Dorg.daisy.pipeline.ws.cors=true',
            ]
            if (this.props.webservice.path) {
                SystemProps.push(
                    '-Dorg.daisy.pipeline.ws.path=' + this.props.webservice.path
                )
            }
            if (this.props.webservice.port) {
                SystemProps.push(
                    '-Dorg.daisy.pipeline.ws.port=' + this.props.webservice.port
                )
            }

            if (
                !existsSync(this.props.appDataFolder) &&
                mkdirSync(this.props.appDataFolder, { recursive: true })
            ) {
                this.pushMessage(`${this.props.appDataFolder} created`)
            } else {
                this.pushMessage(
                    `Using existing ${this.props.appDataFolder} as pipeline data folder`
                )
            }

            if (
                !existsSync(this.props.logsFolder) &&
                mkdirSync(this.props.logsFolder, { recursive: true })
            ) {
                this.pushMessage(`${this.props.logsFolder} created`)
            } else {
                this.pushMessage(
                    `Using existing ${this.props.logsFolder} for pipeline logs`
                )
            }
            // avoid using bat to control the runner ?
            // Spawn pipeline process
            let command = resolve(this.props.jrePath, 'bin', 'java')
            let args = [
                ...JavaOptions,
                ...SystemProps,
                '-classpath',
                `"${delimiter}${relativeJarFiles.join(delimiter)}${delimiter}"`,
                'org.daisy.pipeline.webservice.impl.PipelineWebService',
            ]
            this.pushMessage(
                `Launching the local pipeline with the following command :
${command} ${args.join(' ')}`
            )
            this.instance = spawn(command, args, {
                cwd: this.props.localPipelineHome,
            })
            // NP Replace stdout analysis by webservice monitoring
            this.instance.stdout.on('data', (data) => {
                // Removing logging on nodejs side,
                // as logging is already done in the pipeline side
                //
                // we might read the pipeline logs
                // or check in the API if there is some logs entry point
            })
            this.instance.stderr.on('data', (data) => {
                // keep error logging in case of error raised by the pipeline instance
                // NP : problem found on the pipeline, the webservice messages are also outputed to the err stream
                // this.pushError(`${data.toString()}`)
            })
            this.instance.on('exit', (code, signal) => {
                let message = `Pipeline exiting with code ${code} and signal ${signal}`
                this.setState({
                    status: PipelineStatus.STOPPED,
                })
                this.pushMessage(message)
            })
            this.instance.on('close', (code: number, args: any[]) => {
                let message = `Pipeline closing with code: ${code} args: ${args}`
                this.setState({
                    status: PipelineStatus.STOPPED,
                })
                this.pushMessage(message)
            })
            this.setState({
                status: PipelineStatus.STARTING,
                runningWebservice: this.props.webservice,
            })
        }
        // Launch the async state monitoring loop
        // this.stateMonitor()
        return this.state
    }

    /**
     * Stopping the pipeline
     */
    async stop(appIsClosing = false) {
        this.runStateMonitor = false
        if (appIsClosing) {
            this.stateListeners.clear()
            this.messagesListeners.clear()
            this.errorsListeners.clear()
        }
        if (this.instance) {
            info('closing pipeline')
            let finished = false
            finished = this.instance.kill()
            if (!finished) {
                this.instance.kill('SIGKILL')
            }
            this.setState({
                status: PipelineStatus.STOPPED,
            })
            return
        }
    }

    /**
     * Add a listener on state changes
     * @param callerID an id to identify the caller
     * @param callback function to run on new state
     */
    registerStateListener(
        callerID: string,
        callback: (data: PipelineState) => void
    ) {
        this.stateListeners.set(callerID, callback)
    }

    /**
     * Remove a state listener
     * @param callerID the id of the caller which had registered the listener
     */
    removeStateListener(callerID: string) {
        this.stateListeners.delete(callerID)
    }

    /**
     * Add a listener on the messages stack
     * @param callerID the id of the element that register the listener
     * @param callback the function to run when a new message is added on the stack
     */
    registerMessagesListener(
        callerID: string,
        callback: (data: string) => void
    ) {
        this.messagesListeners.set(callerID, callback)
    }

    /**
     * Remove a listener on the messages stack
     * @param callerID the id of the caller which had registered the listener
     */
    removeMessageListener(callerID: string) {
        this.messagesListeners.delete(callerID)
    }

    /**
     * Add a listener on the error messages stack
     * @param callerID the id of the element that register the listener
     * @param callback the function to run when a new error message is added on the stack
     */
    registerErrorsListener(callerID: string, callback: (data: string) => void) {
        this.errorsListeners.set(callerID, callback)
    }

    /**
     * Remove a listener on the error messages stack
     * @param callerID the id of the caller which had registered the listener
     */
    removeErrorsListener(callerID: string) {
        this.errorsListeners.delete(callerID)
    }
}

/**
 * Register the management of a local pipeline instance to IPC for communication with selected windows
 * @returns the managed instance for supplemental bindings
 */
export function registerPipeline2ToIPC(
    settings?: ApplicationSettings
): Pipeline2IPC {
    // Instance managed through IPC calls within the app
    let pipeline2instance = new Pipeline2IPC(
        settings ? settings.localPipelineProps : undefined
    )
    // Update the instance if the settings are being updated
    ipcMain.on(
        IPC.WINDOWS.SETTINGS.UPDATE,
        (event, newSettings: ApplicationSettings) => {
            info('pipeline has received settings update')
            // Check if pipeline should be deactivated
            if (
                newSettings.runLocalPipeline == false &&
                pipeline2instance.state.status != PipelineStatus.STOPPED
            ) {
                pipeline2instance.stop()
            } else {
                // TODO: restart the pipeline with updated settings if those have changed
                // pipeline2instance.stop().then(() => {
                //     if (newSettings.localPipelineProps) {
                //         pipeline2instance.props = {
                //             ...pipeline2instance.props,
                //             ...newSettings.localPipelineProps,
                //         }
                //     }
                //     pipeline2instance.launch()
                // })
            }
        }
    )
    // start the pipeline runner.
    ipcMain.on(IPC.PIPELINE.START, async (event, webserviceProps) => {
        // New settings requested with an existing instance :
        // Destroy the instance if new settings are requested
        if (webserviceProps) {
            pipeline2instance.updateWebservice(webserviceProps)
        }
        pipeline2instance.launch()
    })

    // Stop the pipeline instance
    ipcMain.on(IPC.PIPELINE.STOP, (event) => pipeline2instance.stop())

    // get state from the instance
    ipcMain.handle(IPC.PIPELINE.STATE.GET, (event) => {
        return pipeline2instance.state || null
    })

    // get properties of the instance
    ipcMain.handle(IPC.PIPELINE.PROPS.GET, (event) => {
        return pipeline2instance.props || null
    })

    // get messages from the instance
    ipcMain.handle(IPC.PIPELINE.MESSAGES.GET, (event) => {
        return pipeline2instance.messages || null
    })
    // get errors from the instance
    ipcMain.handle(IPC.PIPELINE.ERRORS.GET, (event) => {
        return pipeline2instance.errors || null
    })

    // Launch the pipeline if requested in the settings
    if (!settings || (settings && settings.runLocalPipeline)) {
        pipeline2instance.launch()
    }

    return pipeline2instance
}
