/** Shown when the page can't reach the Spring Boot backend, instead of a blank error. */
export function Offline() {
  return (
    <main className="offline">
      <div>
        <h1 className="display">The kitchen is closed.</h1>
        <p>This page couldn&apos;t reach the restaurant API, so there&apos;s no menu to show yet.</p>
        <p>
          Start the backend, then reload:
          <code>cd backend &amp;&amp; mvn spring-boot:run</code>
        </p>
        <a className="btn btn-yellow" href="/">
          Reload
        </a>
      </div>
    </main>
  );
}
