export function Results({ job }) {
    let handleWebLink = (e) => {
        e.preventDefault()
        App.openInBrowser(e.target.href)
    }

    return (
        <ul aria-live="polite">
            {job.jobData.results?.namedResults.map((item, itemIndex) => (
                <li key={`result-${itemIndex}`}>
                    {item.files.length > 1 ? (
                        <>
                            <span>{item.nicename}</span>
                            <ul>
                                {item.files.map((resultFile, resultIndex) => (
                                    <li
                                        key={`result-${itemIndex}-file-${resultIndex}`}
                                    >
                                        <FileLink fileHref={resultFile.file} />
                                    </li>
                                ))}
                            </ul>
                        </>
                    ) : (
                        <FileLink fileHref={item.files[0]?.file}>
                            {item.nicename}
                        </FileLink>
                    )}
                </li>
            ))}
            {job?.jobData?.log ? (
                <li>
                    <a href={job.jobData.log} onClick={handleWebLink}>
                        Log
                    </a>
                </li>
            ) : (
                ''
            )}
        </ul>
    )
}

const { App } = window

interface FileLinkProps {
    fileHref: string
    children?: React.ReactNode
}

function FileLink({ fileHref, children }: FileLinkProps) {
    let localPath = fileHref
        ? decodeURI(fileHref.replace('file:', '').replace('///', '/'))
        : ''
    let filename = fileHref
        ? fileHref.slice(
              fileHref.lastIndexOf('/'),
              fileHref.length - fileHref.lastIndexOf('/')
          )
        : ''

    let onClick = (e) => {
        e.preventDefault()
        if (localPath) App.showItemInFolder(localPath)
        else App.openInBrowser(fileHref)
    }

    return (
        <a className="filelink" href="#" onClick={onClick}>
            {children ?? filename}
        </a>
    )
}
