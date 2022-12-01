import {
    NamedResult,
    Results,
    ResultFile,
    Priority,
    JobStatus,
    JobData,
    MessageLevel,
} from 'shared/types/pipeline'
import { scriptElementToJson } from './scriptToJson'
import { parseXml } from './parser'

function jobXmlToJson(xmlString: string): JobData {
    let jobElm = parseXml(xmlString, 'job')
    return jobElementToJson(jobElm)
}

function jobElementToJson(jobElm: Element): JobData {
    let jobData: JobData = {
        jobId: jobElm.getAttribute('id'),
        priority:
            Priority[jobElm.getAttribute('priority') as keyof typeof Priority],
        status: JobStatus[
            jobElm.getAttribute('status') as keyof typeof JobStatus
        ],
        href: jobElm.getAttribute('href'),
    }

    // TODO is nicename an element or attribute on <job>?
    // not sure at the moment so just check for it in both places
    let nicenameElms = jobElm.getElementsByTagName('nicename')
    if (nicenameElms.length > 0) {
        jobData.nicename = nicenameElms[0].textContent
    } else if (jobElm.hasAttribute('nicename')) {
        jobData.nicename = jobElm.getAttribute('nicename')
    } else {
        jobData.nicename = 'Job'
    }
    let logElms = jobElm.getElementsByTagName('log')
    if (logElms.length > 0) {
        jobData.log = logElms[0].getAttribute('href')
    }
    let resultsElms = jobElm.getElementsByTagName('results')
    if (resultsElms.length > 0) {
        let results: Results = {
            href: resultsElms[0].getAttribute('href'),
            mimeType: resultsElms[0].getAttribute('mime-type'),
            namedResults: [],
        }
        results.namedResults = Array.from(
            resultsElms[0].getElementsByTagName('result')
        )
            // filter out non-direct children
            .filter((resultElm) => resultElm.parentElement == resultsElms[0])
            .map((resultElm): NamedResult => {
                let namedResult: NamedResult = {
                    from: resultElm.getAttribute('from'),
                    href: resultElm.getAttribute('href'),
                    mimeType: resultElm.getAttribute('mime-type'),
                    name: resultElm.getAttribute('name'),
                    nicename: resultElm.getAttribute('nicename'),
                    files: [],
                }
                // the results are structured so that a "result" element is nested inside another "result" element
                // and they have different attributes
                // @ts-ignore
                namedResult.files = Array.from(
                    resultElm.getElementsByTagName('result')
                ).map((resultFileElm) => {
                    let resultFile: ResultFile = {
                        mimeType: resultFileElm.getAttribute('mime-type'),
                        size: resultFileElm.getAttribute('size'),
                    }
                    if (resultFileElm.hasAttribute('file')) {
                        // @ts-ignore
                        resultFile.file = resultFileElm.getAttribute('file')
                    }
                    if (resultFileElm.hasAttribute('href')) {
                        // @ts-ignore
                        resultFile.href = resultFileElm.getAttribute('href')
                    }
                    return resultFile
                })
                return namedResult
            })
        jobData.results = results
    }
    let messagesElms = jobElm.getElementsByTagName('messages')
    if (messagesElms.length > 0) {
        //@ts-ignore
        jobData.messages = Array.from(
            messagesElms[0].getElementsByTagName('message')
        ).map((messageElm) => {
            let timestamp = messageElm.getAttribute('timeStamp')
            try {
                timestamp = new Date(
                    parseInt(messageElm.getAttribute('timeStamp'))
                ).toISOString()
            } catch (err) {
                console.log(err)
            }
            return {
                level: MessageLevel[
                    messageElm.getAttribute(
                        'level'
                    ) as keyof typeof MessageLevel
                ],
                content: messageElm.getAttribute('content'),
                sequence: parseInt(messageElm.getAttribute('sequence')),
                timestamp,
            }
        })
        jobData.progress = parseInt(messagesElms[0].getAttribute('progress'))
    }
    let scriptElms = jobElm.getElementsByTagName('script')
    if (scriptElms.length > 0) {
        jobData.script = scriptElementToJson(scriptElms[0])
    }
    return jobData
}

export { jobXmlToJson, jobElementToJson }
