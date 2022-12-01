import { useContext, createContext, useState, useEffect } from 'react'
import {
    PipelineState,
    PipelineStatus,
    Webservice,
    baseurl,
    Script,
    ApplicationSettings,
} from 'shared/types'
import {
    scriptsXmlToJson,
    scriptXmlToJson,
} from 'renderer/pipelineXmlConverter'

export interface WindowStore {
    about: {
        isOpen: boolean
    }
    pipeline: PipelineState
    messages: Array<string>
    errors: Array<string>
    scripts: Array<Script>
    settings: ApplicationSettings
    // react dispatcher
    setPipelineState?: React.Dispatch<React.SetStateAction<PipelineState>>
    setAboutWindowState?: React.Dispatch<
        React.SetStateAction<{ isOpen: boolean }>
    >
    setPipelineErrors?: React.Dispatch<React.SetStateAction<string[]>>
    setPipelineMessages?: React.Dispatch<React.SetStateAction<string[]>>
    setSettings?: React.Dispatch<React.SetStateAction<ApplicationSettings>>
}

const { App } = window

const WindowStoreContext = createContext({
    about: {
        isOpen: false,
    },
    pipeline: {
        status: PipelineStatus.UNKNOWN,
    },
    messages: [],
    errors: [],
    scripts: [],
} as WindowStore)

export function useWindowStore() {
    return useContext(WindowStoreContext)
}

export function WindowStoreProvider({ children }) {
    const [about, setAboutWindowState] = useState({
        isOpen: false,
    })
    const [pipeline, setPipelineState] = useState<PipelineState>({
        status: PipelineStatus.UNKNOWN,
    })
    const [messages, setPipelineMessages] = useState<Array<string>>([])
    const [errors, setPipelineErrors] = useState<Array<string>>([])
    const [scripts, setScripts] = useState<Array<Script>>([])
    const [settings, setSettings] = useState<ApplicationSettings>({
        downloadFolder: '',
    })

    useEffect(() => {
        App.getSettings().then((value) => {
            setSettings(value)
        })
        App.getPipelineState().then((value) => {
            setPipelineState(value)
        })
        App.getPipelineMessages().then((messages) => {
            setPipelineMessages(messages)
        })
        App.getPipelineMessages().then((errors) => {
            setPipelineMessages(errors)
        })
    }, [])

    useEffect(() => {
        const interval = setInterval(async () => {
            if (pipeline.runningWebservice) {
                fetch(`${baseurl(pipeline.runningWebservice)}/alive`)
                    .then((value: Response) => {
                        if (pipeline.status != PipelineStatus.RUNNING) {
                            pipeline.status = PipelineStatus.RUNNING
                            setPipelineState({
                                ...pipeline,
                                status: PipelineStatus.RUNNING,
                            })
                        }
                    })
                    .catch((reason) => {
                        // Change the status to error only if the previous one was the running status
                        // (because the previous could be the starting one, and in this cas i don't want changes
                        // as it may be only the delay of booting up returning an error)
                        if (pipeline.status == PipelineStatus.RUNNING) {
                            pipeline.status = PipelineStatus.STOPPED
                            setPipelineState({
                                ...pipeline,
                                status: PipelineStatus.STOPPED,
                            })
                        }
                    })
            }
        }, 1000)
        if (pipeline.status == PipelineStatus.RUNNING && scripts.length == 0) {
            getScripts(
                `${baseurl(pipeline.runningWebservice)}/scripts`,
                setScripts
            )
        }
    }, [pipeline])
    App.onPipelineStateChanged(async (event, newState) => {
        setPipelineState(newState)
    })
    App.onSettingsChanged(async (event, newSettings) => {
        setSettings(newSettings)
    })

    const sharedStore = {
        about: about,
        pipeline: pipeline,
        messages: messages,
        errors: errors,
        scripts: scripts,
        settings: settings,
        setAboutWindowState,
        setPipelineState,
        setPipelineMessages,
        setPipelineErrors,
        setSettings,
    }

    return (
        <WindowStoreContext.Provider value={sharedStore}>
            {children}
        </WindowStoreContext.Provider>
    )
}

async function getScripts(url, setScriptsFn) {
    let res = await fetch(url)
    let xmlStr = await res.text()

    let scriptsData = scriptsXmlToJson(xmlStr)
    let completeScriptsData = await Promise.all(
        scriptsData.map(async (scriptData) => {
            res = await fetch(scriptData.href)
            xmlStr = await res.text()
            return scriptXmlToJson(xmlStr)
        })
    )
    setScriptsFn(completeScriptsData)
}
