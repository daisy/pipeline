import ReactDom from 'react-dom/client'
import React from 'react'

import { WindowStoreProvider } from './store'
import { AppRoutes } from './routes'

import './style/style.scss'

ReactDom.createRoot(document.querySelector('app') as HTMLElement).render(
    <React.StrictMode>
        <WindowStoreProvider>
            <AppRoutes />
        </WindowStoreProvider>
    </React.StrictMode>
)
