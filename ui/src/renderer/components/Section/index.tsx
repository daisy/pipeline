/*
Generic section labelled by an h2
*/
export function Section({ label, id, className, children }) {
    return (
        <section className={className} aria-labelledby={id}>
            <h2 id={id}>{label}</h2>
            {children}
        </section>
    )
}
