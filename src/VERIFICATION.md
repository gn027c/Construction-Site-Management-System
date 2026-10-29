# Implementation verification

Verified on 29 October 2026 with Java 8. The implementation uses CSV storage
and console/terminal interaction. The CSV preview feature is excluded.

## Automated verification

Run `./scripts/test.ps1` from the project root. The last complete run before
form cancellation changes passed
**238 checks in 26 suites, with zero failures**. The original workshop matrix
accounts for 25 of these checks. This run includes personnel rendering, private
startup diagnostics, screen transitions and guided attendance corrections.

| Suite | Passed |
| --- | ---: |
| AccountAuditTest | 4 |
| AttendanceConcurrencyTest | 3 |
| AttendanceIncidentTest | 14 |
| AttendanceReviewConsoleTest | 4 |
| AttendanceTimestampTest | 8 |
| AuditNotificationTest | 10 |
| AuthenticationConsoleTest | 9 |
| AuthenticationTest | 25 |
| ConsoleScreenTest | 7 |
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
| PersonnelViewTest | 7 |
| ReportArchiveTest | 12 |
| StartupPrivacyTest | 7 |
| VisitorDateTest | 8 |
| WorkflowIntegrationTest | 10 |
| ZoneTest | 7 |
| Total | 238 |

Privacy checks compare anonymous output for healthy and rejected business data,
verify administrator-only diagnostics, exercise personal accounts through hidden
menus and inboxes, and check account switches and fatal credential errors.
Screen checks cover aligned option columns, result acknowledgement, account
boundaries, plain redirected output and EOF while waiting for Enter.
Guided review checks cover timeline inspection, retaining unchanged fields,
verified corrections, cancellation, missing record IDs and administrator access.

The Windows UTF-8 launcher also passed a separate smoke check from the `src`
working directory: compilation, `admin` login, home menu, CSV save and clean exit.
An additional forced-screen launcher check passed from `src`, including the
Enter acknowledgement and logout. Both checks used temporary data directories.

Javadoc validation passed. Expanded layout, line lengths and English
description-only source comments were checked for all 66 Java source files.

## Form cancellation follow-up

A focused run after adding cancellation passed **59 checks in 7 suites**:
InputCancellationTest (12), PagingTest (8), AttendanceReviewConsoleTest (4),
ReportArchiveTest (12), ConsoleTest (7), AuthenticationConsoleTest (9) and
PersonnelViewTest (7). No failures occurred. The full suite was not rerun for
this follow-up. Javadoc and readable-layout validation passed for all 67 source files.

The checks cover cancelling string, integer, date and password input; blank menu
navigation; preserving meaningful zero values and optional blanks; escaped command
text; partial personnel/account/contact changes; report filters; review evidence;
and returning from password entry without spending a failed login attempt.
Changes are committed on `codex/console-form-cancellation`.

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
Runtime credentials, activity and notifications remain ignored by Git. Reports
and academic workbooks retain their existing ignored document locations.
Pre-existing unrelated workspace changes were preserved.
