import { useEffect, useState } from 'react'
import { useWindowStore } from 'renderer/store'
import { ApplicationSettings } from 'shared/types'
import { FileOrFolderInput } from '../CustomFields/FileOrFolderInput'

const { App } = window // The "App" comes from the bridge

export function SettingsForm() {
    // Current registered settings
    const { settings } = useWindowStore()
    // Copy settings in new settings
    const [newSettings, setNewSettings] = useState<ApplicationSettings>({
        ...settings,
    })
    const [saved, setSaved] = useState(true)
    useEffect(() => {
        setNewSettings({
            ...settings,
        })
    }, [settings])
    // Changed folder
    const resultsFolderChanged = (filename) => {
        setNewSettings({
            ...newSettings,
            downloadFolder: filename,
        })
        setSaved(false)
    }

    // send back the settings for being save on disk
    const handleSave = () => {
        App.saveSettings(newSettings)
        setSaved(true)
    }
    return (
        <form className="settings-form">
            <div>
                <div className="form-field">
                    <label htmlFor="resultsFolder">
                        Default results folder
                    </label>
                    <span className="description">
                        A folder where all jobs will be automatically downloaded
                    </span>
                    <FileOrFolderInput
                        type="open"
                        dialogProperties={['openDirectory']}
                        elemId="resultsFolder"
                        mediaType={['']}
                        name={'Results folder'}
                        onChange={resultsFolderChanged}
                        useSystemPath={false}
                        initialValue={newSettings.downloadFolder}
                        buttonLabel="Browse"
                    />
                </div>
                {/* insert local pipeline settings form part here */}
                {/* insert remote pipeline settings form part here */}
            </div>
            <div className="save-settings">
                {' '}
                <button
                    id="save-settings"
                    type="submit"
                    onClick={handleSave}
                    className="save-button"
                    // disabled={
                    //     JSON.stringify({ ...settings }) !=
                    //     JSON.stringify({ ...newSettings })
                    // }
                >
                    Save
                </button>
                {saved ? (
                    <span className="confirm-save" aria-live="polite">
                        Saved
                    </span>
                ) : (
                    ''
                )}
            </div>
        </form>
    )
}
