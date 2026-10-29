# Implementation verification

Verified on 29 October 2026 with Java 8. The implementation uses CSV storage
and console/terminal interaction. The CSV preview feature is excluded.

## Automated verification

Run `./scripts/test.ps1` from the project root. The initial completed run passed
**213 checks in 22 suites, with zero failures**. The original workshop matrix
accounts for 25 of these checks. A subsequent focused run passed all 10 workflow
integration checks and all 4 account audit checks after audit-target normalization.

| Suite | Passed |
| --- | ---: |
| AccountAuditTest | 4 |
| AttendanceConcurrencyTest | 3 |
| AttendanceIncidentTest | 14 |
| AttendanceTimestampTest | 8 |
| AuditNotificationTest | 10 |
| AuthenticationConsoleTest | 9 |
| AuthenticationTest | 25 |
| ConsoleTest | 7 |
| CsvHelperTest | 4 |
| CsvPersistenceTest | 10 |
| DateTimeHelperTest | 4 |
| DomainBehaviorTest | 12 |
| HistoricalMatrixTest | 25 |
| IdentifierStaffTest | 9 |
| MigrationTest | 7 |
| ModelValidationTest | 8 |
| PagingTest | 8 |
| PersonnelTest | 9 |
| ReportArchiveTest | 12 |
| VisitorDateTest | 8 |
| WorkflowIntegrationTest | 10 |
| ZoneTest | 7 |
| Total | 213 |

After moving personnel rendering out of the models, a focused run passed
**33 checks with zero failures**: `PersonnelViewTest` (7), `PersonnelTest` (9),
`CsvPersistenceTest` (10) and `ConsoleTest` (7). This covers all four personnel
types, missing optional fields, subtype search, identifier aliases, active and
inactive filters, contact edits, persistence, export and console termination.
The full suite was not rerun for this view change.

The Windows UTF-8 launcher also passed a separate smoke check from the `src`
working directory: compilation, `admin` login, home menu, CSV save and clean exit.
The smoke check used a temporary data directory.

Javadoc validation passed. Expanded layout, line lengths and English
description-only source comments were checked for the original 63 Java source files.

## Terminal walkthroughs

Three additional process-level walkthroughs passed:

1. A linked SITE_USER signs in, records a personal check-in, views personal
   history and occupancy, and exits without seeing another person's records.
2. An administrator opens activity history and the private inbox, then generates
   an inclusive daily report showing the two recorded check-ins.
3. The shipped invalid historical data blocks business activation while allowing
   administrator review. ATT6 and ATT8 are reported, and all original business
   CSV bytes remain unchanged after exit.

The sample's historical anomalies are retained deliberately. Start with a new
data directory for an empty registry, or review historical evidence before
correcting the sample; the application does not invent missing checkout events.

## Report and academic audit verification

Workshop 6 and Final Word reports and their PDFs were updated in their existing
Project Report locations. Each PDF has 17 pages, including current models, CSV
schemas, role boundaries, the 22-suite result table and all 25 original acceptance
scenarios. All distinct rendered pages were inspected. The packaged renderer
could not find LibreOffice; hidden Microsoft Word export and Poppler rendering
were used to verify the actual document layout.

Both corresponding AI Audit Log workbooks were updated in their existing
locations. Their four sheets were inspected and rendered. Each contains 16 core
entries and 6 documented correction cases. Formula caches, summary counts,
continuous entry rows, evidence paths and wrapped Human Delta cells were checked.
The summary identifies the total historical prompt count as unverified; the
legacy estimate of 75 is retained as an estimate and is not used to assert a
verified selection ratio. Academic audit workbooks are distinct from the
application's runtime activity log.

## Change boundaries

Code was committed on `codex/complete-console-plan` in separate domain,
identifier, authentication/audit, console workflow and CSV migration commits.
Runtime credentials, activity and notifications remain ignored by Git. Reports
and academic workbooks retain their existing ignored document locations.
Pre-existing unrelated workspace changes were preserved.
