import { resolve } from 'path'

const __baseUnpackagedPath = __dirname.endsWith('.asar')
    ? __dirname + '.unpacked'
    : __dirname

const resolveUnpacked = (...path: string[]) => {
    return resolve(__baseUnpackagedPath, ...path)
}

export { resolveUnpacked }
