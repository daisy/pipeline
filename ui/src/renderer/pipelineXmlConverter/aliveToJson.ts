import { Alive } from 'shared/types'
import { parseXml } from './parser'

function aliveXmlToJson(xmlString: string): Alive {
    try {
        let aliveElm = parseXml(xmlString, 'alive')
        return {
            alive: true,
            localfs: aliveElm[0].getAttribute('localfs') == 'true',
            authentication:
                aliveElm[0].getAttribute('authentication') == 'true',
            version: aliveElm[0].getAttribute('version'),
        }
    } catch (err) {
        return {
            alive: false,
        }
    }
}

export { aliveXmlToJson }
