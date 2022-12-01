import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MainView } from 'renderer/components'
import { useWindowStore } from 'renderer/store'
import { PipelineStatus } from 'shared/types/pipeline'

const queryClient = new QueryClient()

export function MainScreen() {
    const { pipeline } = useWindowStore()
    return (
        <QueryClientProvider client={queryClient}>
            <>
                <main>
                    {pipeline.status == PipelineStatus.RUNNING ? (
                        <MainView />
                    ) : (
                        <p>Starting the DAISY Pipeline...</p>
                    )}
                </main>
            </>
        </QueryClientProvider>
    )
}
