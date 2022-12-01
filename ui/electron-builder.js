const { APP_CONFIG } = require('./app.config')

const { APP_ID, AUTHOR, TITLE, DESCRIPTION, FOLDERS } = APP_CONFIG

const CURRENT_YEAR = new Date().getFullYear()

module.exports = {
    appId: APP_ID,
    productName: TITLE,
    copyright: `Copyright © ${CURRENT_YEAR} — ${AUTHOR.name}`,

    directories: {
        app: FOLDERS.DEV_TEMP_BUILD,
        output: 'dist',
    },

    mac: {
        icon: `${FOLDERS.RESOURCES}/icons/logo.icns`,
        category: 'public.app-category.utilities',
        identity: 'US Fund for DAISY (SAMG8AWD69)',
        hardenedRuntime: true,
    },

    dmg: {
        icon: false,
    },

    linux: {
        category: 'Utilities',
        synopsis: DESCRIPTION,
        target: ['AppImage', 'deb', 'pacman', 'freebsd', 'rpm'],
    },

    win: {
        icon: `${FOLDERS.RESOURCES}/icons/logo_256x256.png`,
        target: ['nsis', 'portable', 'zip'],
    },
    afterSign: 'buildtools/notarize.js',
    asarUnpack: ['resources/daisy-pipeline'],
}
