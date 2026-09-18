# Git and commit rules

- **No AI attribution anywhere.** No `Co-Authored-By` assistant trailer, no session link,
  no mention of the model or tool in subject or body. `.claude/settings.json` disables the
  automatic trailer; if one appears anyway, strip it before committing.
- **Commit style:** Conventional Commits (`feat:`, `fix:`, `perf:`, `docs:`, `refactor:`), imperative
  subject ≤ 72 chars. An optimization lands as `perf(<area>): <optimization_name>` and its
  body cites the measured gain and the catalog row. Body explains *why*, not *what* the diff shows.
- **One change per commit.** No drive-by reformatting, no unrelated cleanups, no new
  dependencies without asking.
- **Branches:** `perf/<optimization_name>` for optimizations, `docs/`, `build/` otherwise. Solo project —
  committing on `main` is fine when the user says so. Never commit directly
  to `main`/`master` unless the user says so.
- **Never** force-push, rewrite shared history, or delete branches. Pushing at all requires
  an explicit ask (permission rule prompts for it).
- Use `gh` for PRs and issues. PR description: what changed, why, how it was verified.
- Before committing: run `./gradlew build`; stage only the files for this change
  (`git add <paths>`, not `git add -A`).
