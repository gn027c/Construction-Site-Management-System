# Implementation verification

Verified on 31 October 2026 with Java 8 (OpenJDK 1.8.0_504). The implementation
uses CSV storage and console/terminal interaction.

## Automated verification

Run `./scripts/test.ps1` from the project root. The latest complete run passed
**267 checks in 26 suites, with zero failures**. The original workshop matrix
(`HistoricalMatrixTest`) accounts for 25 of these checks.

| Suite | Passed |
| --- | ---: |
| AttendanceIncidentTest | 14 |
| AttendanceRulesTest | 9 |
| AttendanceTimestampTest | 8 |
| AuthenticationConsoleTest | 10 |
| AuthenticationTest | 27 |
| ConsoleScreenTest | 8 |
| ConsoleTest | 7 |
| CsvHelperTest | 4 |
| CsvPersistenceTest | 10 |
| DateTimeHelperTest | 4 |
| DomainBehaviorTest | 12 |
| DomainViewsTest | 7 |
| HistoricalMatrixTest | 25 |
| IdentifierStaffTest | 11 |
| InputCancellationTest | 12 |
| ModelValidationTest | 8 |
| PagingTest | 9 |
| PersonnelTest | 9 |
| PersonnelViewTest | 7 |
| ReportArchiveTest | 12 |
| SafetyCertificateTest | 5 |
| StaffBusinessValidationTest | 11 |
| StartupPrivacyTest | 6 |
| VisitorDateTest | 11 |
| WorkflowIntegrationTest | 9 |
| ZoneTest | 12 |
| Total | 267 |

What the newer checks cover:

- Attendance rules: invalid first exits, repeated entries and exits, wrong-zone
  exits, backwards timestamps, direct repository callers, staged CSV load
  rejection, duplicate IDs, a failed file write, identifier reuse and restart.
- Safety levels: a level below, equal to and above the zone level; the order of
  denial reasons (allowed list, credential, level); restricted zones rejecting
  `NONE`; visitors refusing a level; unknown levels in CSV; and the sample data
  admitting W1 to Z3 (HIGH) but refusing W1 at Z4 (CRITICAL) by level only.
- Visit dates: check-in on the booked day, refusal on another day or without a
  date, check-out after the date changes, and editing the date from the console.
- Identifiers: the nine-digit limit, leading zeros, and the next identifier per
  prefix, including the clear failure at `999,999,999`.
- Accounts: created and reset passwords must be changed at sign-in, the change
  survives restart, the new password must differ, cancelling signs out, and a
  linked account can change its password while business data is rejected.
- Privacy: anonymous output for healthy and rejected business data, administrator-
  only diagnostics, personal accounts trying hidden menu numbers, and fatal
  credential errors.
- Rejected data: a reload of a still-invalid file keeps data blocked and leaves the
  source bytes unchanged; a corrected file reloads and activates the data.
- Certificates: only numbers ending in `_SAFE` are valid, and a worker may be
  registered without a certificate.

## Terminal walkthrough

On a copy of the shipped `data/` directory, `admin`/`admin` signed in, the home
menu showed options 0-10, and business data loaded without rejection. Through
menu 3, option 1:

| Swipe | Result |
| --- | --- |
| W1 check-in at Z4 | Refused: `Safety level HIGH is below the required CRITICAL.` |
| W1 check-in at Z3 | Recorded as `REC1` in `attendance.csv` |
| V1 check-in at Z1 | Refused: `Visit date is not set; record the visit date before check-in.` |
| W3 check-in at Z4 | Refused: `Safety level MEDIUM is below the required CRITICAL.` |

The shipped `data/` files were not changed by the walkthrough.

## Deliverables

On 31 October 2026 the Workshop 6 and Final reports were rebuilt from the Workshop 5
report (same nine chapters, 40 pages each) and matched to this version: 267 checks in
26 suites, the 25-case test matrix, safety levels, visit dates, the 0-10 home menu and
the removed features. Code listings were taken from `src/`, Figures 3.1-3.6 and 5.1
were redrawn, Figure 6.1 (CSV load and write paths), Figures 7.1-7.4 (console output
of the real runs), Figure 7.5 (results per suite) and Figure 8.1 (problem
decomposition) were added. The AI Audit Logs keep every earlier entry unchanged and
now hold 14 core entries and 3 hallucination cases.
