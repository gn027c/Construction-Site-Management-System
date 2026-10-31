# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

SmartSite (PRO192, Group 2, Topic 7) is a Java 8 console application for a construction site:
attendance by code, safety incidents, personnel/zone management, role-based accounts. Storage is
CSV files only; there is no Maven/Gradle and no external dependency. The repository has three
things to keep in sync: the **code**, the **Project Report** and the **AI Audit Log**.

## Commands (PowerShell, from the repository root)

```powershell
./scripts/run.ps1                          # compile src/ to bin/app (UTF-8) and start the console
./scripts/run.ps1 -BuildOnly               # compile only
./scripts/run.ps1 -DataDirectory D:/Tmp/X  # run against another data directory (new dir = empty registry)
./scripts/run.ps1 -ScreenMode never        # no ANSI screen clearing (auto|always|never)
./scripts/test.ps1                         # compile src/ + tests/ to bin/csv-tests, run every *Test class
./scripts/test.ps1 -TestClass ZoneTest     # run one suite (comma-separate for several)
```

- Never compile with bare `javac Main.java` or from inside `src/`: Java 8 on Windows defaults to
  Cp1252 and rejects UTF-8 sources, and package classes fail with `NoClassDefFoundError: wrong name`.
  Without the scripts: `javac -encoding UTF-8 -d bin/app <all src/**/*.java>` then
  `java -Dfile.encoding=UTF-8 -Dsmartsite.dataDir=data -cp bin/app Main`.
- Tests are plain `main` classes using `tests/support/Checks.java` (`test`, `equal`, `require`,
  `throwsType`); each prints `PASS`/`FAIL` lines and a `RESULT n passed, m failed` line, and exits
  non-zero on failure. There is no JUnit. Test IDs such as `TC-ZONE-01` are the ones cited in the
  report's test matrix. `src/VERIFICATION.md` records the latest full run - update it when counts change.
- Console flows can be driven with piped input for traces: `printf '...\n' | java -cp bin/app Main`.
  A fresh data directory creates account `admin` / password `admin`.
- One-off CSV schema migration: `java -Dfile.encoding=UTF-8 -cp bin/app util.CsvMigration <oldDir> <newDir>`.

## Architecture

Layered flow: **Main -> controller -> service -> repository -> CSV** (`util.CsvHelper`).

- `Main` only signs in, shows `MenuView.showMainMenu` and dispatches menu numbers to
  `SiteManagerController` (facade). The facade owns the four business services (personnel, zone,
  attendance, incident), the per-module controllers and `AuthenticationService` (which in turn owns
  `ActivityLogService` and `NotificationService`), and does load/save/reload of all data.
- **Responsibilities are strict** (see `src/README.md`, "Documentation boundaries"):
  models own field constraints and state transitions (`AttendanceRecord.assertCanFollow`,
  `Incident.assignTo/resolve`, polymorphic `Zone.checkAccess` / `RestrictedZone.checkAccess`);
  services look things up and coordinate but never use `Scanner`/`System.out`; repositories own the
  collections and CSV I/O; controllers/views do all console interaction (`InputHelper`, `ConsoleTheme`,
  `ConsoleScreen`, `ListView` for 10-row paging/filtering).
- Hierarchies: `Person` -> `Worker`, `Staff`, `Contractor`, `Visitor`; `Zone` -> `RestrictedZone`.
  Accounts (`Account`, `Role`, `Permission`) are separate from people; one person links to at most one
  account. All business exceptions extend `SiteManagementException` (a `RuntimeException`).
- Identifiers are canonicalised by `util.IdentifierHelper`: uppercase prefix + unpadded number
  (`W01`, `w-001` -> `W1`). Always resolve references through it.
- Attendance write path: validate against the person's previous event -> write temp file -> atomic
  replace of `attendance.csv` -> publish in memory, all under one repository lock. Atomicity is per
  file only; the four business CSVs are saved independently. `util.DataDirectoryLock` stops two
  console sessions using one data directory.
- Invalid CSV on startup blocks business operations rather than being repaired; admin menu 11 is
  read-only validation. The shipped `data/` sample intentionally contains historical inconsistencies -
  do not "fix" it automatically. Tests that need files use temp directories or copies of `data/`.
- CSV schemas, role permissions, menu numbering, cancellation (`/cancel`) and password hashing
  (PBKDF2) are documented in `src/README.md`; design rationale in `docs/DESIGN_DECISIONS.md`,
  `docs/MODEL_DESIGN.md`, `docs/WS6.md`. Update those files when behaviour they describe changes.

## Code conventions

`src/README.md` is the style guide: English comments/Javadoc, 4-space indent, braces always, one
statement per line, <=120 chars. Javadoc is prose only (no `@param`/`@return` sections) and must not
contain member names, student IDs, workshop notes or claims the code does not implement.

## Deliverables: Project Report and AI Audit Log

Both live outside git: `Project Report/` and `Ai Audit Log/` at the main checkout root are symlinks
into OneDrive and are git-ignored, as are `Task/`, `Workshop/`, `.agents/` and `scratch/`. They are
not visible from a worktree - use the main checkout path.

| Piece | Files |
| --- | --- |
| Workshop report | `Project Report/WorkshopN_Group2_Topic7.docx` + `.pdf` (N = 1..6) |
| Final report | `Project Report/PRO192_Final_Report_Group2_SmartSite.docx` + `.pdf` |
| Audit log | `Ai Audit Log/AI_AuditLog_Workshop_N_Group2_SE211059.xlsx`, `AI_AuditLog_Final_Group2_SE211059.xlsx` |
| Requirements / hand-outs | `Task/WorkshopN/requirement.md` (lecturer's text - never edit), `task.md` (member code tasks), `agent_action_plan.md` (agent's report/log/packaging plan) |
| Guidelines | `Ai Audit Log/README_AI_AuditLog_Guide.md`, `Project Report/PRO192_Report_Template.docx` |

For any report / audit-log / hand-out / "nộp bài" work, use the project skill
`smartsite-workshop-deliverables` (`.claude/skills/` in the main checkout). Its non-negotiables:

- Every claim, class name, listing and test result in a report or log must match the code. Run the
  real tests before writing PASS; build code listings from source, never by hand; run its
  `scripts/sweep_phantoms.py` to catch names that do not exist in `src/`.
- Build log N from log N-1 (not from the Final log, which once cited non-existent classes) and
  report N from report N-1, checking that figures/images survived.
- Report language: academic English. Audit log language: Vietnamese.
- Team process (`.agents/rules/workshop_transition_protocol.md`): members write 100% of the code
  (25% each, on their own `khanghnh-*`, `tuantna-*`, `thientt-*`, `loint-*` branches); the agent
  writes 100% of reports, logs and packaging. When handing out a workshop, add `// TODO (WS<N> ...)`
  markers only - do not pre-implement member code.

## Git

- Do not add `Co-Authored-By` or any AI attribution to commits or PRs; the owner rewrites history to
  remove it.
- `main` lags behind the `codex/*` feature branches; check which branch holds the current code
  (`git for-each-ref --sort=-committerdate refs/heads`) before comparing reports against it.
