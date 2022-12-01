// job settings, not application settings
import { findValue, getAllOptional, getAllRequired } from 'renderer/utils/utils'

export function Settings({ job }) {
    let required = getAllRequired(job.script)
    let optional = getAllOptional(job.script)

    return (
        <ul>
            {required.map((item, idx) => (
                <li key={idx}>
                    <span>{item.nicename}: </span>
                    <span>
                        {findValue(item.name, item.kind, job.jobRequest)}
                    </span>
                </li>
            ))}
            {optional.map((item, idx) => (
                <li key={idx}>
                    <span>{item.nicename}: </span>
                    <span>
                        {findValue(item.name, item.kind, job.jobRequest)}
                    </span>
                </li>
            ))}
        </ul>
    )
}
