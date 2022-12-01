export const IPC = {
    WINDOWS: {
        ABOUT: {
            CREATE: 'windows: create-about-window',
            WHEN_CLOSE: 'windows: when-about-window-close',
        },
        SETTINGS: {
            CREATE: 'windows: create-settings-window',
            GET: 'settings: get',
            UPDATE: 'settings: update',
            CHANGED: 'settings: changed',
            WHEN_CLOSE: 'windows: when-settings-window-close',
        },
    },
    PIPELINE: {
        START: 'pipeline: start',
        STOP: 'pipeline: stop',
        STATE: {
            SEND: 'pipeline: state-send',
            GET: 'pipeline: state-get',
            CHANGED: 'pipeline: state-changed',
        },
        PROPS: {
            GET: 'pipeline: props-get',
        },
        MESSAGES: {
            SEND: 'pipeline: messages-send',
            UPDATE: 'pipeline: messages-update',
            GET: 'pipeline: messages-get',
        },
        ERRORS: {
            SEND: 'pipeline: errors-send',
            UPDATE: 'pipeline: errors-update',
            GET: 'pipeline: errors-get',
        },
    },
    FILE: {
        SAVE: 'file: save',
        UNZIP: 'file: unzip',
        OPEN: 'file: open',
    },
}
