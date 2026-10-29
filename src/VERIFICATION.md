# Implementation verification

Verified on 29 October 2026 with Java 8. The implementation uses CSV storage
and console/terminal interaction. The CSV preview feature is excluded.

## Automated verification

Run `./scripts/test.ps1` from the project root. The latest complete run passed
**260 checks in 28 suites, with zero failures**. The original workshop matrix
accounts for 25 of these checks. This run includes personnel rendering, private
startup diagnostics, form cancellation, screen transitions, atomic attendance
commits and read-only CSV validation. The normal file-I/O matrix case uses a
generated valid 15-event test ledger; the invalid shipped history is tested for
rejection separately and remains unchanged.

| Suite | Passed |
| --- | ---: |
| AccountAuditTest | 4 |
| AtomicAttendanceTest | 10 |
| AttendanceConcurrencyTest | 3 |
| AttendanceIncidentTest | 14 |
| AttendanceTimestampTest | 8 |
| AuditNotificationTest | 10 |
| AuthenticationConsoleTest | 9 |
| AuthenticationTest | 25 |
| ConsoleScreenTest | 7 |
| ConsoleTest | 7 |
| CsvHelperTest | 4 |
| CsvPersistenceTest | 10 |
| CsvValidationConsoleTest | 4 |
| DateTimeHelperTest | 4 |
| DomainBehaviorTest | 12 |
| HistoricalMatrixTest | 25 |
| IdentifierStaffTest | 9 |
| InputCancellationTest | 12 |
| MigrationTest | 7 |
| ModelValidationTest | 8 |
| PagingTest | 8 |
| PersonnelTest | 9 |
| PersonnelViewTest | 7 |
| ReportArchiveTest | 12 |
| StartupPrivacyTest | 7 |
| VisitorDateTest | 8 |
| WorkflowIntegrationTest | 10 |
| ZoneTest | 7 |
| Total | 260 |

Privacy checks compare anonymous output for healthy and rejected business data,
verify administrator-only diagnostics, exercise personal accounts through hidden
menus and inboxes, and check account switches and fatal credential errors.
Screen checks cover aligned option columns, result acknowledgement, account
boundaries, plain redirected output and EOF while waiting for Enter.
Atomic attendance checks cover invalid first exits, repeated entries/exits,
wrong-zone exits, backwards timestamps, direct repository callers, staged CSV
load rejection, duplicate IDs, failed file replacement, sequence preservation,
restart and competing repository writers. Read-only validation checks confirm
that diagnostics remain administrator-only and cannot edit rejected files.

The Windows UTF-8 launcher also passed a separate smoke check from the `src`
working directory: compilation, `admin` login, home menu, CSV save and clean exit.
An additional forced-screen launcher check passed from `src`, including the
Enter acknowledgement and logout. Both checks used temporary data directories.

Javadoc validation passed. Expanded layout, line lengths and English
description-only source comments were checked for all 66 Java source files.

Form checks cover cancelling string, integer, date and password input; blank menu
navigation; preserving meaningful zero values and optional blanks; escaped command
text; partial personnel/account/contact changes; report filters; read-only validation;
and returning from password entry without spending a failed login attempt.

## Terminal walkthroughs

Process-level walkthroughs include:

1. A linked SITE_USER signs in, records a personal check-in, views personal
   history and occupancy, and exits without seeing another person's records.
2. An administrator opens activity history and the private inbox, then generates
   an inclusive daily report showing the two recorded check-ins.
3. The shipped invalid historical data blocks business activation while allowing
   administrator validation. The first invalid event, ATT6, is reported, and all original business
   CSV bytes remain unchanged after exit.
4. A fresh console records one valid entry, rejects a cross-zone entry and a
   wrong-zone exit, then records a valid exit. The file contains only REC1 IN and
   REC2 OUT at Z1. After restart, another exit is rejected and file bytes remain
   unchanged. This walkthrough used a temporary directory.

The sample's historical anomalies are retained deliberately. Start with a new
data directory for an empty registry, or review historical evidence before
correcting the sample; the application does not invent missing checkout events.

## Earlier report and academic audit checkpoint

The following artifacts document the earlier 213-check implementation checkpoint.
The console and review changes above are verified against the current source;
the academic artifacts and submission archive have not been regenerated for them.

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
Subsequent console privacy, navigation and guided review changes were committed
separately on `codex/console-navigation-data-review`.
Form cancellation was committed on `codex/console-form-cancellation`; atomic
attendance validation and removal of the CSV editor are committed separately
on `codex/atomic-attendance-validation`.
Runtime credentials, activity and notifications remain ignored by Git. Reports
and academic workbooks retain their existing ignored document locations.
Pre-existing unrelated workspace changes were preserved.
