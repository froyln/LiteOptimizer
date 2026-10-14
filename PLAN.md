# LiteOptimizer execution plan

Updated 2026-10-13. Optimize Minecraft 1.12.2 with unchanged mechanics, RNG, update order,
and rendered output. Keep Java 8, LiteLoader/Mixin 0.7.5, LGPL-3.0, and no runtime dependencies.
Architecture and invariants: `AGENTS.md`. Candidate inventory: `docs/catalog.md`.
Replanning validation: planning review found no material issues;
`JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build --offline` exited 0 (6 s).

Verification procedures: `docs/methodology.md`. Previous evidence and task history:
`docs/history/plan-through-2026-10-13.md` (read only the relevant section when needed).

## Current position

The mod already has its infrastructure and substantial optimization code. Do not restart
foundation work or plan another wholesale Sodium/Lithium/FerriteCore port.

| area | current evidence | next decision |
|---|---|---|
| Foundation | build, deterministic state harness, benchmark recorder, config switches exist | reuse; repair only a demonstrated blocker |
| Lithium | 0.2/0.5 batches implemented; hopper entity lookup reports isolated B3 MSPT 3.75 → 3.22 ms | seek a new hot path only after profiling the current build |
| Sodium | terrain port implemented; item-frame display lists report B1 18.13 → 10.39 ms and pixel comparison 0 differences | validate current artifact, then target remaining measured cost |
| FerriteCore | blockstate storage reports about 14 MB less old-generation heap; quad dedup about 2.2 MB | preserve; additional memory work requires live retained-heap evidence |
| 0.6 development | settings constrained; frustum gains isolated; its pixel proof unresolved; ordinary TE occlusion redundant; matrix rejected and light-cache tuning deferred | resolve capture proof; retire unsupported occlusion; release gate stays separate |
| Release | partial B1 mod smoke evidence exists; full checklist incomplete; installed mods emit conflict warnings | release remains blocked until required checks pass |

Reported combined culling measurements are not proof of each feature's isolated gain.
The archive/catalog hold exact conditions and limitations. GL43 has known pixel differences
and a poor NVIDIA result: production config now excludes that backend and compact vertices.
There are existing uncommitted source/resource changes: inspect and preserve them before edits.

## Why execution changes

The old version batches mixed implementation, research, release gates, and repeated GUI
attempts. Work continued on compatibility menus after the optimization work had finished.
The catalog's original proposed batches also name optimizations already implemented or rejected.
From now on, the catalog is an inventory, not a requirement to port everything.

Development completion and release approval are separate milestones. Focused compatibility
checks accompany changed hooks; the full installed-mod checklist remains mandatory before
release. A release blocker does not automatically block independent development.

## Completed: artifact and evidence audit

**Status: done** — 2026-10-13. Current ledger: `docs/compat/checklist.md`.
Legacy backend eligibility, combined-gain attribution, and missing artifact identities were
recorded. Offline Java 8 build exited 0. Historical passes were not relabeled as release passes.

## Execution queue

| order | task | deliverable and stop condition |
|---|---|---|
| 1 | current artifact/evidence reconciliation | done: ledger, explicit gaps, build |
| 1a | enforce production parity settings | done: normalized load/save, unsafe controls removed, focused check |
| 2 | select next bottleneck | one focused profile of current optimized build on a ready workload; choose at most one candidate, or report no justified candidate and stop |
| 3 | implement one optimization | minimal mixin/helper/config/catalog change; parity proof, measured gain, focused compatibility, build |
| 4 | assemble release candidate | cumulative regression checks and one scheduled full compatibility session; resolve failures, then release approval |

Repeat tasks 2–3 only when a useful candidate exists. Versions describe delivered behavior;
they do not impose quotas of render/world/memory features. Never tag or publish without authorization.

## Completed: production settings and culling evaluation

- **Parity settings: done.** Unsafe saved flags normalized on load/save; their selectors removed.
  Real-Gson regression passes and fails on pre-fix source. Java 8 build exited 0; runtime full
  compatibility remains owed. Details: `docs/compat/checklist.md`.
- **Entity culling evaluation: done.** No gain at the initial B1 pose with moving entities
  suppressed (3.24→3.31 ms). General parity/gain remain unproven; release-ineligible.
  Evidence: `docs/profiles/entity-culling-B1-2026-10-13/README.md`.
- **TE culling evaluation: done.** Frustum isolated average-frame gains 18.3%/17.8%, exact
  parity unresolved (9 final pixels; earlier unchanged control: 19 pixels). Ordinary TE occlusion
  redundant, no established gain; release-ineligible. No product code changed in evaluation.
  Java 8 build exited 0 in 6 s; restoration/diff checks and evidence review passed.
  Evidence: `docs/profiles/te-culling-B1-2026-10-13/README.md`.

## Completed: conservative 0.6.0-rc2 candidate

**Status: candidate complete; full release validation remains open.** The built jar is installed
in the test instance and matches SHA-256
`a26226f6ec8f96fa9174378feab1efd048cca0e2ae074cd2a3d0f11562e8519b`. LiteLoader startup
records version `0.6.0-rc2`, all options loaded, and a connection to the disposable Carpet server:
`docs/profiles/0.6.0-rc2-2026-10-13/final-startup.log`. The prior runtime-tested payload differs
only in two compiled version-string constants; proof is `version-payload-check.json`.

Candidate acceptance passed: Java 8 offline build; focused real-Gson settings and bench-config
checks; B3 cumulative state parity; isolated B1 display-list gain with exact off/on and off/off
pixel captures; and focused full-stack compatibility smoke. The exact conditions and boundaries
are recorded in `docs/profiles/0.6.0-rc2-2026-10-13/README.md` and
`docs/compat/checklist.md`. Original shared mod settings were restored after the final launch.

Do not tag or call this a full release yet. The full compatibility table remains open, and the
malilib direct-reconnect NPE has no matched no-LiteOptimizer comparison. Continue with one grouped
release-check session after preparing the remaining fixtures; if the reconnect issue reproduces
without LiteOptimizer, close it as baseline evidence, otherwise investigate the responsible hook.

## Selecting optimization work

- Start with a current profile, not an upstream feature list. Reuse recent profiles when code,
  workload, hardware, and settings still match. A method name in a sample is a lead, not a proof.
- Ready starting workloads: B1 storage for rendering/heap, B3 hoppers for integrated-server cost.
  Use B4 only for observed entity/spawning cost. B5 requires a running quarry; B6 requires a
  repeatable flight workload. Missing fixtures block those candidates, not the whole project.
- Inspect the top remaining costs in the optimized build. B3 previously sampled
  `WorldSlice.copyBlocks`, flat quad rendering, and AO bounds filling; confirm current importance
  before changing them. AO reuse and chunk buffers are already in the Sodium port.
- Rank supported candidates by expected user-visible gain relative to implementation effort,
  parity risk, invalidation complexity, and foreign mixin collisions. Choose the narrowest win.
- Keep hopper caching, AI caches, tick-scheduler rewrites, light-cache tuning, and global enum
  replacement deferred unless their specific costs are measured. Keep entity matrix, changed
  RNG/update-order algorithms, and pixel-changing transforms rejected.
- No required percentage win is invented here. The improvement must be repeatable beyond the
  observed run noise and worth the added maintenance. An inconclusive result is not a gain.

## Work contract for each candidate

Before coding, write a short task card in this file:

```
Candidate / config key:
Profile evidence and workload:
Vanilla target and foreign hooks:
Parity argument (state, order, RNG, pixels as applicable):
Smallest change / excluded work:
Gain metric and off/on configuration (include dependencies):
Focused compatibility checks:
Acceptance / blocker / stopping condition:
```

Use existing helpers and upstream-compatible code. One candidate per task/commit; a tightly
coupled pair needs an explicit reason and combined attribution. No speculative infrastructure.

Verification order:
1. Small compile/helper check while editing; build once the change is coherent.
2. Relevant parity run; render work also needs a controlled pixel comparison. Screenshots cannot prove parity for entity types they suppress; `--no-entities` retains
   hanging entities, so it can cover item frames/paintings but not moving entities. Keep unchanged float evaluation and sort order.
3. Isolated off/on gain measurement with dependencies held constant where the harness permits.
   Reuse existing runners. A combined benchmark supports a combined claim only.
4. Focused installed-mod smoke for the actual touched hooks, then final offline build if code changed.

Record one concise result with artifact paths, settings, numbers, and limitations. Failed parity
means fix or reject; compatibility failures need investigation; unmeasurable candidates are deferred
or removed. Additional runs require a failure, noise, or changed conditions to justify them.

## Release validation without endless GUI work

- Full `docs/compat/checklist.md` remains a release gate with the installed mod set at B1.
  During development run only affected-hook checks and startup/world join when applicable.
- Reuse passed observations within a release candidate when the artifact, relevant hooks,
  installed mod versions, fixture, and settings are unchanged. Record that identity. Old evidence
  with an unknown artifact is supporting history, not a definitive release pass.
- Group pending checks into one session with prepared schematics, placements, and expected results.
  Use a disposable copy for paste, placement, inventory, lighting changes, and destructive tests;
  preserve original worlds and restore temporary configs. Paste is a required check to prepare,
  not a feature to leave perpetually untested because the original world must be preserved.
- GUI control stays in isolated headless Sway/WayVNC. Never use the owner's desktop/input.
- After two unsuccessful attempts at the same GUI action or 20 minutes without new evidence,
  stop that check, record the exact blocker, and switch to independent work. These are execution
  budgets, not exemptions from acceptance. Do not repeat an unchanged blocker next session.
- Investigate installed-mod conflict warnings against a baseline without LiteOptimizer before
  assigning blame. Baseline conflicts remain visible; do not claim blanket compatibility.
- Server-dependent Carpet checks need a suitable test server/fixture. Record the missing prerequisite
  and schedule the check; menu version `null` is not a server-hook pass.

Release ready means: successful build; each included optimization has parity/gain evidence;
required cumulative regression checks pass; full compatibility checklist passes; no unresolved
release-blocking failure. Development may proceed while a release prerequisite is outstanding.

## Agent session rules

Read this plan, the relevant catalog row, and only necessary code/evidence. Do not reread the
full history or catalog on every “continue”. Continue the named task until its stop condition.
Update its status once; keep at most one task in progress. Put detailed results in the relevant
catalog/profile/compat file, not a growing chronological transcript here.

When blocked, write: exact prerequisite, evidence, next action, and whether independent work
can proceed. A “continue” must produce a new decision, implementation, or proof; it must not
merely repeat the same GUI attempt and append another status paragraph.
