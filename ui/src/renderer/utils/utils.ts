import { JobRequest, Script } from 'shared/types'

// make an HTML-friendly ID string
export let ID = (id) => `z-${id}`

export function getAllRequired(script: Script) {
    return script
        ? [
              ...script.inputs.filter((i) => i.required),
              ...script.options.filter((i) => i.required),
          ]
        : []
}

export function getAllOptional(script: Script) {
    return script
        ? [
              ...script.inputs.filter((i) => !i.required),
              ...script.options.filter((i) => !i.required),
          ]
        : []
}

export function findValue(name: string, kind: string, jobRequest: JobRequest) {
    if (!jobRequest) return ''
    let arr = kind == 'input' ? jobRequest.inputs : jobRequest.options
    let item = arr.find((i) => i.name == name)
    if (!item) return ''
    let value =
        // @ts-ignore
        item.value === true
            ? 'true'
            : // @ts-ignore
            item.value === false
            ? 'false'
            : item.value
    return value
}

export function findInputType(type) {
    let inputType = 'text'
    if (type == 'anyFileURI') {
        inputType = 'file'
    } else if (type == 'anyDirURI') {
        inputType = 'file'
    } else if (type == 'xsd:dateTime' || type == 'datetime') {
        inputType = 'datetime-local'
    } else if (type == 'xsd:boolean' || type == 'boolean') {
        inputType = 'checkbox'
    } else if (type == 'xsd:string' || type == 'string') {
        inputType = 'text'
    } else if (
        [
            'xsd:integer',
            'xsd:float',
            'xsd:double',
            'xsd:decimal',
            'xs:integer',
            'integer',
            'number',
        ].includes(type)
    ) {
        inputType = 'number'
    } else {
        inputType = 'text'
    }
    return inputType
}
