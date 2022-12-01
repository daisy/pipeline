/*
Data manager and owner of tab view
*/
import { useState } from 'react'
import { Job, JobStatus, JobState, NamedResult } from 'shared/types/pipeline'
import { useQuery } from '@tanstack/react-query'
import { jobXmlToJson } from 'renderer/pipelineXmlConverter'
import { TabView } from '../TabView'
import { AddJobTab, JobTab } from '../JobTab'
import { JobTabPanel } from '../JobTabPanel'
import { useWindowStore } from 'renderer/store'
import { ipcRenderer } from 'electron'
import { IPC } from 'shared/constants'
import { join } from 'path'

const NEW_JOB = (id) => ({
    internalId: id,
    state: JobState.NEW,
})

const { App } = window

export function MainView() {
    const { settings } = useWindowStore()
    const [jobs, setJobs] = useState(Array<Job>)
    const [nextJobId, setNextJobId] = useState(0)
    const [autoselect, setAutoselect] = useState(false)
    const { isLoading, error, data } = useQuery(
        ['jobsData'],
        async () => {
            let fetchJobData = async (job) => {
                let res = await fetch(job.jobData.href)
                let xmlStr = await res.text()
                if (xmlStr) return jobXmlToJson(xmlStr)
                else return null
            }

            let updatedJobs = await Promise.all(
                jobs.map(async (j) => {
                    // only check submitted jobs (e.g. have a jobData property)
                    // that are either IDLE or RUNNING (e.g. don't recheck ERROR or SUCCESS statuses)
                    if (
                        j.hasOwnProperty('jobData') &&
                        (j.jobData.status == JobStatus.IDLE ||
                            j.jobData.status == JobStatus.RUNNING)
                    ) {
                        let jobData = await fetchJobData(j)
                        if (jobData.status != j.jobData.status) {
                            // download the available results
                            // And change file links
                            if (settings.downloadFolder && jobData.results) {
                                if (jobData.results.namedResults) {
                                    console.log(
                                        'Fetching results ',
                                        jobData.results.namedResults
                                    )
                                    // const result of jobData.results.namedResults
                                    // Note : the data used is the files field and not the 
                                    for (
                                        let i =
                                            jobData.results.namedResults
                                                .length - 1;
                                        i >= 0;
                                        --i
                                    ) {
                                        let namedResult =
                                            jobData.results.namedResults[i]
                                        const newJobURL = new URL(
                                            `${settings.downloadFolder}/${jobData.jobId}/${namedResult.name}`
                                        ).href
                                        // change the target jobData href
                                        jobData.results.namedResults[i].href =
                                            newJobURL
                                        if (
                                            jobData.results.namedResults[i]
                                                .files
                                        ) {
                                            for (
                                                let j =
                                                    jobData.results
                                                        .namedResults[i].files
                                                        .length - 1;
                                                j >= 0;
                                                --j
                                            ) {
                                                let resultFile =
                                                    jobData.results.namedResults[i].files[j]
                                                // Change the file url and keep the original href
                                                const newFileURL = new URL(
                                                    `${
                                                        settings.downloadFolder
                                                    }/${jobData.jobId}/${
                                                        namedResult.name
                                                    }/${resultFile.file
                                                        .split('/')
                                                        .pop()}`
                                                ).href
                                                let fetchedResult = await fetch(
                                                    resultFile.href
                                                )
                                                    .then((response) =>
                                                        response.blob()
                                                    )
                                                    .then((blob) =>
                                                        blob.arrayBuffer()
                                                    )
                                                    .then((buffer) =>
                                                        resultFile.mimeType ===
                                                        'application/zip'
                                                            ? App.unzipFile(
                                                                  buffer,
                                                                  newFileURL
                                                              )
                                                            : App.saveFile(
                                                                  buffer,
                                                                  newFileURL
                                                              )
                                                    )
                                                    .then(() => {
                                                        let newResult =
                                                            Object.assign(
                                                                {},
                                                                resultFile
                                                            )
                                                        newResult.file =
                                                            newFileURL
                                                        return newResult
                                                    })
                                                    .catch((e) => resultFile) // if a problem occured, return the original result
                                                    .finally()
                                                jobData.results.namedResults[i].files[j].file = 
                                                    fetchedResult.file
                                            }
                                        }
                                    }
                                    jobData.results.href = new URL(
                                        `${settings.downloadFolder}/${jobData.jobId}`
                                    ).href
                                }
                            }
                        }
                        j.jobData = jobData
                    }
                    return j
                })
            )
            if (jobs.length == 0) {
                updatedJobs.push(NEW_JOB(`job-${nextJobId}`))
                setNextJobId(nextJobId + 1)
            }
            setJobs([...updatedJobs])
            return updatedJobs
        },
        { refetchInterval: 3000 }
    )

    if (isLoading) {
        return <></>
    }
    if (error instanceof Error) {
        console.log('Error', error)
        return <></>
    }
    if (!data) {
        return <></>
    }

    let addJob = (onItemWasCreated?) => {
        let theNewJob = NEW_JOB(`job-${nextJobId}`)
        setJobs([...jobs, theNewJob])
        setNextJobId(nextJobId + 1)
        if (onItemWasCreated) {
            onItemWasCreated(theNewJob.internalId)
        }
    }

    let removeJob = (jobId) => {
        const jobs_ = jobs.filter((j) => j.internalId !== jobId)
        setJobs(jobs_)
    }

    let updateJob = (job) => {
        let jobId = job.internalId
        let jobs_ = jobs.map((j) => {
            if (j.internalId == jobId) {
                return { ...job }
            } else return j
        })
        setJobs(jobs_)
    }

    return (
        <TabView<Job>
            items={jobs}
            onTabCreate={addJob}
            onTabClose={removeJob}
            ItemTab={JobTab}
            AddItemTab={AddJobTab}
            ItemTabPanel={JobTabPanel}
            updateItem={updateJob}
        />
    )
}
