# Spring Boot + Maven + MySQL + Claude Code, in GitHub Codespaces

A minimal Spring Boot web app, backed by MySQL, configured to run in a GitHub Codespace
with the Claude Code CLI pre-installed, plus a path to a real, always-on public deployment.

## 1. Push this to GitHub

```bash
git init
git add .
git commit -m "Initial Spring Boot + devcontainer scaffold"
git branch -M main
git remote add origin https://github.com/<your-username>/<your-repo>.git
git push -u origin main
```

## 2. Open it in a Codespace

On the repo page: **Code → Codespaces → Create codespace on main**.

The container build uses `.devcontainer/devcontainer.json` + `.devcontainer/docker-compose.yml`,
which together:
- Bring up two containers: `app` (Java 21 + Maven, where you work) and `db` (MySQL 8.4).
- Install Maven via the official `java` devcontainer feature.
- Install the **Claude Code CLI** via Anthropic's official feature (`ghcr.io/anthropics/devcontainer-features/claude-code`).
- Forward ports `8080` (the app) and `3306` (MySQL, if you want to connect a DB client).
- Run `mvn package` automatically after the container builds.

MySQL comes up with a database `demo`, user `demo`, password `demo` (see
`.devcontainer/docker-compose.yml`) — `application.properties` already points at it by default,
so no extra setup is needed inside the Codespace.

Once it's open, in the integrated terminal:

```bash
claude          # sign in and start a Claude Code session
mvn spring-boot:run   # start the dev server
```

Codespaces will pop up a notification with a forwarded URL for port 8080 — that's your
running dev server, viewable at `https://<codespace-name>-8080.app.github.dev`. Try:

```bash
curl -X POST -H "Content-Type: application/json" \
  -d '{"name":"first item","description":"stored in MySQL"}' \
  https://<codespace-name>-8080.app.github.dev/items

curl https://<codespace-name>-8080.app.github.dev/items
```

That round-trip confirms the app is actually reading and writing to MySQL.

## 3. Dev-server access vs. real access

Two different things people mean by "giving others access":

- **Repo access** (so others can see/edit the code): Settings → Collaborators and teams →
  Add people, on your GitHub repo.
- **Website access** (so others can use the running app): a Codespace's forwarded port is
  tied to your session and, by default, private to you — it's meant for previewing your
  own work, not for handing out to other people, and it won't stay up when you're not
  actively working. For a real always-on URL, deploy the built app somewhere durable —
  see below.

## 4. Deploy for an always-on URL

The `Dockerfile` in this repo builds a small runnable image, so any container-friendly host
works. A straightforward option:

**Render** (free/low-cost tier, connects directly to GitHub):
1. Push this repo to GitHub (step 1).
2. In Render: New → Web Service → connect this repo.
3. Render auto-detects the `Dockerfile` and builds/deploys on every push to `main`.
4. You'll get a permanent URL like `https://your-app.onrender.com` — share that with anyone.

Other equally good choices, same Dockerfile: **Railway**, **Fly.io**, or **Google Cloud Run**
(pay-per-request, scales to zero). All of them: connect the GitHub repo, point at the
Dockerfile, deploy, get a stable URL.

`application.properties` already reads the `PORT` env var (falling back to 8080), which is
what most of these platforms require.

### MySQL in production

The local `db` container in Docker Compose is dev-only — it won't exist once you deploy. You
need a real managed MySQL instance and to point the app at it via three environment variables
on your host (Render, Railway, Fly.io, Cloud Run all have an "Environment" settings tab):

```
SPRING_DATASOURCE_URL=jdbc:mysql://<host>:3306/<database>
SPRING_DATASOURCE_USERNAME=<user>
SPRING_DATASOURCE_PASSWORD=<password>
```

Where to get a managed MySQL instance:
- **Railway** and **Render** both offer a MySQL add-on you can provision alongside the app,
  giving you these three values directly.
- **PlanetScale** offers managed MySQL with a generous free tier, usable from any host.
- **AWS RDS** / **Google Cloud SQL** if you're already in that ecosystem.

`spring.jpa.hibernate.ddl-auto=update` (already set) means Hibernate creates/updates tables
automatically from the `@Entity` classes on startup — fine for a demo, but for a real app you'd
typically switch to a proper migration tool (Flyway or Liquibase) once the schema stabilizes.

If you want the URL restricted to specific people rather than public, that needs an auth layer
(e.g. Spring Security with login, or the host's built-in access control — Render and Cloud Run
both support restricting access). Ask if you want that wired in.

## 5. Using Claude Code day-to-day

Inside the Codespace terminal, `claude` drops you into an interactive session scoped to this
repo — useful for things like "add a new REST endpoint," "write tests for X," or "explain this
stack trace." It authenticates via browser login the first time; after that it persists for
the life of the Codespace.
