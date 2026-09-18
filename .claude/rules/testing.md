---
paths:
  - "**/*.test.*"
  - "**/*.spec.*"
  - "**/*_test.*"
  - "**/test_*.*"
  - "tests/**"
  - "test/**"
  - "__tests__/**"
---
# Testing rules

- Test runner and commands: see `AGENTS.md` → Commands. Run the smallest check that proves
  the change (single file / single test) while iterating; the full `./gradlew build`
  before finishing.
- Bug fix = a failing test first that reproduces the symptom, then the fix. Name the test
  after the behavior, not the ticket.
- Test behavior through public interfaces. Avoid mocks for code you own; mock only I/O
  and third-party boundaries.
- There is no JUnit. Pure-Java helpers (no `net.minecraft`/LWJGL imports) get a `main` with
  `assert`s, run as `javac -d /tmp/x Foo.java && java -ea -cp /tmp/x Foo`. Anything touching
  MC is verified in-game: parity harness + benchmark per `docs/methodology.md`, numbers pasted
  into the PLAN.md task and `docs/catalog.md`.
- A parity failure is a bug in the optimization, never a reason to relax the hash comparison.
- Never delete or skip a failing test to get green. Report it and stop.
