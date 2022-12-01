/*
Select a script and submit a new job
*/
import { useState } from 'react'
import { ScriptForm } from '../ScriptForm'
import { useWindowStore } from 'renderer/store'
import { ID } from 'renderer/utils/utils'

export function NewJobPane({ job, updateJob }) {
    const [selectedScript, setSelectedScript] = useState(null)
    const { scripts } = useWindowStore()

    let onSelectChange = (e) => {
        let selection = scripts.find((script) => script.id == e.target.value)
        setSelectedScript(selection)
    }

    let job_ = { ...job }
    return (
        <>
            <section
                className="select-script"
                aria-labelledby={`${ID(job.internalId)}-select-script}`}
            >
                <label
                    id={`${ID(job.internalId)}-select-script}`}
                    htmlFor="script"
                >
                    Select a script:
                </label>
                <select id="script" onChange={(e) => onSelectChange(e)}>
                    <option value={null}>None</option>
                    {scripts
                        .sort((a, b) => (a.nicename > b.nicename ? 1 : -1))
                        .map((script, idx) => (
                            <option key={idx} value={script.id}>
                                {script.nicename}
                            </option>
                        ))}
                </select>
            </section>
            {selectedScript != null ? (
                <ScriptForm
                    job={job_}
                    script={selectedScript}
                    updateJob={updateJob}
                />
            ) : (
                ''
            )}
        </>
    )
}
