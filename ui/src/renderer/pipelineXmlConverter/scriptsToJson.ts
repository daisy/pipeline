import { scriptElementToJson } from './scriptToJson'
import { Script } from 'shared/types'
import { parseXml } from './parser'

function scriptsXmlToJson(xmlString: string): Array<Script> {
    let scriptsElm = parseXml(xmlString, 'scripts')
    let scripts = Array.from(scriptsElm.getElementsByTagName('script')).map(
        (scriptElm) => {
            return scriptElementToJson(scriptElm)
        }
    )
    return scripts
}

export { scriptsXmlToJson }
