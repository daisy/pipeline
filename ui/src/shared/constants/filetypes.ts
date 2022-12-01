const mediaTypesFileFilters = {
    '*': {
        name: 'All files',
        extensions: ['*'],
    },
    'application/oebps-package+xml': {
        name: 'Package File',
        extensions: ['opf'],
    },
    'application/x-dtbook+xml': {
        name: 'DTBook',
        extensions: ['xml'],
    },
    'application/epub+zip': {
        name: 'EPUB',
        extensions: ['epub'],
    },
    'application/vnd.pipeline.tts-config+xml': {
        name: 'TTS Config',
        extensions: ['xml'],
    },
    'application/xhtml+xml': {
        name: 'HTML Document',
        extensions: ['html', 'xhtml'],
    },
    'application/xml': {
        name: 'XML Document',
        extensions: ['xml'],
    },
    'application/z3998-auth+xml': {
        name: 'ZedAI Document',
        extensions: ['xml'],
    },
    'text/html': {
        name: '',
        extensions: [''],
    },
    'text/css': {
        name: 'HTML Document',
        extensions: ['html'],
    },
    'application/xslt+xml': {
        name: 'XSLT Document',
        extensions: ['xsl', 'xslt'],
    },
    'text/plain': {
        name: 'Plain text',
        extensions: ['txt'],
    },
    'application/vnd.oasis.opendocument.text-template': {
        name: 'Open Office Template',
        extensions: ['ott'],
    },
}

export { mediaTypesFileFilters }
