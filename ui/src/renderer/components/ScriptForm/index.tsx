/*
Fill out fields for a new job and submit it
*/
import { jobRequestToXml, jobXmlToJson } from 'renderer/pipelineXmlConverter'
import { JobRequest, JobState, baseurl, ScriptItemBase } from 'shared/types'
import { useState, useEffect } from 'react'
import { useWindowStore } from 'renderer/store'
import {
    findInputType,
    findValue,
    getAllOptional,
    getAllRequired,
    ID,
} from 'renderer/utils/utils'
import { Section } from '../Section'
import { marked } from 'marked'
import { FileOrFolderInput } from '../CustomFields/FileOrFolderInput'
import Markdown from 'react-markdown'
import remarkGfm from 'remark-gfm'

const { App } = window

export function ScriptForm({ job, script, updateJob }) {
    const [submitInProgress, setSubmitInProgress] = useState(false)
    const [error, setError] = useState(false)
    const { pipeline } = useWindowStore()

    const [jobRequest, setJobRequest] = useState<JobRequest>(null)
    useEffect(() => {
        setJobRequest({
            scriptHref: script.href,
            nicename: script.nicename,
            inputs: script.inputs.map((item) => ({
                name: item.name,
                value: null,
                isFile: item.type == 'anyFileURI' || item.type == 'anyDirURI',
            })),
            options: script.options.map((item) => ({
                name: item.name,
                value: item.default ? item.default : null,
                isFile: item.type == 'anyFileURI' || item.type == 'anyDirURI',
            })),
        })
    }, [script])
    let required = getAllRequired(script)
    let optional = getAllOptional(script)

    // take input from the form and add it to the job request
    let saveValueInJobRequest = (value, data) => {
        if (!jobRequest) {
            return
        }
        let inputs = [...jobRequest.inputs]
        let options = [...jobRequest.options]

        // update the array and return a new copy of it
        let updateValue = (value, data, arr) => {
            let arr2 = arr.map((i) =>
                i.name == data.name ? { ...i, value } : i
            )
            return arr2
        }
        if (data.kind == 'input') {
            inputs = updateValue(value, data, inputs)
        } else {
            options = updateValue(value, data, options)
        }
        let newJobRequest = {
            ...jobRequest,
            inputs: [...inputs],
            options: [...options],
        }

        setJobRequest(newJobRequest)
    }

    // submit a job
    let onSubmit = async (e) => {
        e.preventDefault()
        setSubmitInProgress(true)

        let xmlStr = jobRequestToXml(jobRequest)

        // this post request submits the job to the pipeline webservice
        let res = await fetch(`${baseurl(pipeline.runningWebservice)}/jobs`, {
            method: 'POST',
            body: xmlStr,
            mode: 'cors',
        })
        setSubmitInProgress(false)
        if (res.status != 201) {
            setError(true)
        } else {
            let newJobXml = await res.text()
            try {
                let newJobJson = jobXmlToJson(newJobXml)
                let job_ = {
                    ...job,
                    state: JobState.SUBMITTED,
                    jobData: newJobJson,
                    jobRequest,
                    script,
                }
                updateJob(job_)
            } catch (err) {
                setError(true)
            }
        }
    }

    return (
        <>
            <section
                className="header"
                aria-labelledby={`${ID(job.internalId)}-script-hd`}
            >
                <div>
                    <h1 id={`${ID(job.internalId)}-script-hd`}>
                        {script?.nicename}
                    </h1>
                    <p>{script?.description}</p>
                </div>
                <button
                    className="run"
                    type="submit"
                    form={`${ID(job.internalId)}-form`}
                    accessKey="r"
                >
                    Run
                </button>
                {error ? <p>Error</p> : ''}
            </section>

            {!submitInProgress ? (
                <form
                    className="details"
                    onSubmit={onSubmit}
                    id={`${ID(job.internalId)}-form`}
                >
                    <Section
                        className="required-fields"
                        id={`${ID(job.internalId)}-required`}
                        label="Required information"
                    >
                        <ul className="fields">
                            {required.map((item, idx) => (
                                <li key={idx}>
                                    <FormField
                                        item={item}
                                        key={idx}
                                        idprefix={`${ID(
                                            job.internalId
                                        )}-required`}
                                        onChange={saveValueInJobRequest}
                                        initialValue={findValue(
                                            item.name,
                                            item.kind,
                                            jobRequest
                                        )}
                                    />
                                </li>
                            ))}
                        </ul>
                    </Section>
                    {optional.length > 0 ? (
                        <Section
                            className="optional-fields"
                            id={`${ID(job.internalId)}-optional`}
                            label="Options"
                        >
                            <ul className="fields">
                                {optional.map((item, idx) => (
                                    <li key={idx}>
                                        <FormField
                                            item={item}
                                            key={idx}
                                            idprefix={`${ID(
                                                job.internalId
                                            )}-optional`}
                                            onChange={saveValueInJobRequest}
                                            initialValue={findValue(
                                                item.name,
                                                item.kind,
                                                jobRequest
                                            )}
                                        />
                                    </li>
                                ))}
                            </ul>
                        </Section>
                    ) : (
                        ''
                    )}
                </form>
            ) : (
                <>
                    <p>Submitting...</p>
                    {error ? <p>Error</p> : ''}
                </>
            )}
        </>
    )
}

// create a form element for the item
// item.type can be:
// anyFileURI, anyDirURI, xsd:string, xsd:dateTime, xsd:boolean, xsd:integer, xsd:float, xsd:double, xsd:decimal
// item.mediaType is a file type e.g. application/x-dtbook+xml
function FormField({
    item,
    idprefix,
    onChange,
    initialValue,
}: {
    item: ScriptItemBase
    idprefix: string
    onChange: (string, ScriptItemBase) => void // function to set the value in a parent-level collection.
    initialValue: any // the initialValue
}) {
    let inputType = findInputType(item.type)
    const [value, setValue] = useState(initialValue)
    let controlId = `${idprefix}-${item.name}`

    let onFileFolderChange = (filename, data) => {
        console.log('onFileFolderChange', filename)
        onChange(filename, data)
    }
    let onInputChange = (e, data) => {
        let newValue =
            e.target.getAttribute('type') == 'checkbox'
                ? e.target.checked
                : e.target.value
        setValue(newValue)
        onChange(newValue, data)
    }
    let dialogOpts =
        item.type == 'anyFileURI'
            ? ['openFile']
            : item.type == 'anyDirURI'
            ? ['openDirectory']
            : ['openFile', 'openDirectory']

    let externalLinkClick = (e) => {
        e.preventDefault()
        App.openInBrowser(e.target.href)
    }

    return (
        <div className="form-field">
            <label htmlFor={controlId}>{item.nicename}</label>
            <span className="description">
                <Markdown
                    remarkPlugins={[remarkGfm]}
                    components={{
                        a: (props) => {
                            return (
                                <a
                                    href={props.href}
                                    onClick={externalLinkClick}
                                >
                                    {props.children}
                                </a>
                            )
                        },
                    }}
                >
                    {item.desc}
                </Markdown>
            </span>

            {inputType == 'file' ? ( // 'item' may be an input or an option
                <FileOrFolderInput
                    type="open"
                    dialogProperties={dialogOpts}
                    elemId={controlId}
                    mediaType={item.mediaType}
                    name={item.name}
                    onChange={(filename) => onFileFolderChange(filename, item)}
                    useSystemPath={false}
                    buttonLabel="Browse"
                    required={item.required}
                    initialValue={initialValue}
                />
            ) : inputType == 'checkbox' ? ( // 'item' is an option
                <input
                    type={inputType}
                    required={item.required}
                    onChange={(e) => onInputChange(e, item)}
                    id={controlId}
                    checked={value === 'true' || value === true}
                ></input>
            ) : (
                // 'item' is an option
                <input
                    type={inputType}
                    required={item.required}
                    // @ts-ignore
                    value={initialValue ?? ''}
                    id={controlId}
                    onChange={(e) => onInputChange(e, item)}
                ></input>
            )}
        </div>
    )
}
