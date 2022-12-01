// parse a string of xml and return the first element with the given name
export function parseXml(xmlString, elmName) {
    let doc = new DOMParser().parseFromString(xmlString, 'text/xml')
    if (!doc) {
        throw new Error(`Could not parse XML for ${elmName}`)
    }
    let elm = doc.getElementsByTagName(elmName)
    if (!elm || elm.length == 0) {
        throw new Error(`Element ${elmName} not found`)
    }
    return elm[0]
}
