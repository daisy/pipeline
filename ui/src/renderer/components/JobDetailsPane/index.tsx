/*
Details of a submitted job
*/
import { JobStatus } from '/shared/types'
import { Messages } from './Messages'
import { Settings } from './Settings'
import { Results } from './Results'
import { Section } from '../Section'

import { ID } from '../../utils/utils'

const { App } = window

const readableStatus = {
    IDLE: 'Waiting',
    RUNNING: 'Running',
    ERROR: 'Error',
    SUCCESS: 'Completed',
    FAIL: 'Error',
}

export function JobDetailsPane({ job }) {
    return (
        <>
            <section
                className="header"
                aria-labelledby={`${ID(job.internalId)}-hd`}
            >
                <div>
                    <h1 id={`${ID(job.internalId)}-hd`}>
                        {job.jobData.nicename}
                    </h1>
                    <p>{job.script.description}</p>
                    <p
                        aria-live="polite"
                        className={`status ${readableStatus[
                            job.jobData.status
                        ].toLowerCase()}`}
                    >
                        Job status: {readableStatus[job.jobData.status]} {job.jobData.progress ? `(${job.jobData.progress * 100}%)` : '' }
                    </p>
                </div>
                {job.jobData.status == JobStatus.SUCCESS ||
                job.jobData.status == JobStatus.FAIL ? (
                    <JobResults
                        jobId={job.jobData.jobId}
                        results={job.jobData.results}
                    />
                ) : (
                    ''
                )}
            </section>
            <div className="details">
                <Section
                    label="Settings"
                    className="job-settings"
                    id={`${ID(job.internalId)}-job-settings`}
                >
                    <Settings job={job} />
                </Section>
                <Section
                    label="Messages"
                    className="job-messages"
                    id={`${ID(job.internalId)}-job-messages`}
                >
                    <Messages job={job} />
                </Section>
                <Section
                    label="Results"
                    className="job-results"
                    id={`${ID(job.internalId)}-job-results`}
                >
                    <Results job={job} />
                </Section>
            </div>
        </>
    )
}

function JobResults({ jobId, results }) {
    // this is a hack!
    // get the first file and use its path to figure out what is probably the output folder for the job
    let file = ''
    if (results?.namedResults.length > 0) {
        if (results.namedResults[0].files.length > 0) {
            file = results.namedResults[0].files[0].file
            let idx = file.indexOf(jobId)
            if (idx != -1) {
                file = file.slice(0, idx + jobId.length) + '/'
                file = file.replace('file:', '').replace('///', '/')
                file = decodeURI(file)
            }
        }
    }

    if (file != '') {
        return (
            <button
                className="jobResults"
                onClick={(e) => App.showItemInFolder(file)}
            >
                Show results folder
            </button>
        )
    } else {
        return <p className="jobResults">Results unavailable</p>
    }
}
