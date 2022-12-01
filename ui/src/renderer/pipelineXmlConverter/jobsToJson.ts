import { Job } from 'shared/types'
import { jobElementToJson } from './jobToJson'
import { parseXml } from './parser'

function jobsXmlToJson(xmlString: string): Array<Job> {
    let jobsElm = parseXml(xmlString, 'jobs')
    let jobs = Array.from(jobsElm.getElementsByTagName('job')).map((jobElm) => {
        let job = jobElementToJson(jobElm)
        return job
    })
    return jobs
}

export { jobsXmlToJson }
