# Implementation verification

Verified on 2 November 2026 with Java 8 (OpenJDK 1.8.0_504). The implementation
uses CSV storage and console/terminal interaction.

## Automated verification

Run `./scripts/test.ps1` from the project root. The latest complete run passed
**294 checks in 27 suites, with zero failures**. The original workshop matrix
(`HistoricalMatrixTest`) accounts for 25 of these checks.

| Suite | Passed |
| --- | ---: |
| AttendanceIncidentTest | 14 |
| AttendanceRulesTest | 9 |
| AttendanceTimestampTest | 8 |
| AuthenticationConsoleTest | 10 |
| AuthenticationTest | 27 |
| BusinessCompletionTest | 27 |
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
| Total | 294 |

What the newer checks cover:

- Business completion (`BusinessCompletionTest`): a worker registered without a
  certificate is certified later and then enters a restricted zone (`TC-PERS-07`);
  a rejected profile edit changes nothing; losing a credential returns the level to
  `NONE`; the console renews a contractor contract and clears a certificate with
  `-`; incidents go only to active non-visitor personnel (`TC-INC-05`) and can be
  reassigned until resolved (`TC-INC-06`); zone and incident prompts accept aliases
  such as `z03` and `inc0103`; visitors cannot be granted restricted access;
  granting a listed person reports no change; zone name and level can be edited
  (`TC-ZONE-06`); and a `SITE_USER` records personal attendance from the console,
  which previously failed because the zone check required `MANAGE_ZONES`.
- Accounts and personnel (`BusinessCompletionTest`): a deactivated person cannot
  sign in until reactivated (`TC-AUTH-01`); the first administrator must choose a
  new password (`TC-AUTH-02`); an administrator deletes an unused person together
  with the linked account (`TC-PERS-08`); a worker becomes staff with the same code,
  history and a required account role (`TC-PERS-09`); a visitor hired from the
  console gets an account with the chosen role (`TC-PERS-10`); and a staff position
  change requires a role, while a manager keeps the old position.
- Incidents and reports (`BusinessCompletionTest`): a safety officer reports an
  incident with its zone and is recorded as reporter (`TC-INC-07`); resolving needs
  a note that survives reload (`TC-INC-08`); the old 9-column `incidents.csv` is
  rejected; and working hours pair check-ins with check-outs (`TC-REP-01`: on the
  sample day W1 has 3 sessions and 8.67 hours, C1 one open session).
- Permission review (`BusinessCompletionTest`): an account holding only one permission
  completes the menu option that permission shows, for zone grants, staff check-out,
  occupancy, and reporting, assigning and resolving incidents (`TC-AUTH-03`); a site
  manager provisions only SITE_USER accounts (`TC-AUTH-04`); accounts cannot be
  created for or linked to visitors (`TC-AUTH-05`); an invalid report filter keeps the
  report screen; and occupancy rows keep a name containing `|` intact.

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

On a copy of the shipped `data/` directory (2 November 2026), `admin`/`admin` signed
in, was asked for a new password, the home menu showed options 0-10, and business
data loaded without rejection. Through menu 3, option 1:

| Swipe | Result |
| --- | --- |
| W1 check-in at Z4 | Refused: `Safety level HIGH is below the required CRITICAL.` |
| W1 check-in at Z3 | Recorded as `REC1` in `attendance.csv` |
| V1 check-in at Z1 | Refused: `Visit is booked for 12/10/2026, not 02/11/2026.` |
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

On 3 November 2026 the Final report was rebuilt on the PRO192 template (Arial, header
and footer, lists of figures, tables and listings) after the lecturer's review, and
matched to main @ a14915f: 294 checks in 27 suites and a 40-case test matrix (the 25
workshop cases plus 15 from `BusinessCompletionTest`). New content: a use case
diagram and requirement traceability (Chapter 2), an overall model, a package table
and the real `tree /F /A src` output for the seven packages (Section 5.1), a two-part
UML class diagram (Section 5.2) and new console runs (Chapter 7). The Final AI Audit
Log gained entry 015 and a sheet listing the 40 test cases with their results.

A second walkthrough on a fresh copy of `data/` (2 November 2026), after W4 was
deactivated through menu 1:

| Action | Result |
| --- | --- |
| Menu 2, grant `z03` to `w03` | `Access list updated: W3 granted Z3.` |
| Menu 2, grant Z3 to V1 | Refused: `Visitors cannot enter restricted zones, so they cannot be granted access.` |
| Menu 2, grant Z3 to W1 | `W1 is already on the allowed list of Z3.` |
| Menu 4, assign `inc0103` to inactive W4, then to V1 | Refused; neither appears in the assignee list |
| Menu 4, assign `inc0103` to `w2`, then reassign to W1 | `INC103` assigned to W2, then to W1 |
| Menu 1, edit W4: trade, team, certificate `CERT_W4_SAFE`, level 3 | Saved as `Steel Fixer`, `Team C`, `HIGH` |
| Menu 2 option 5, edit `z04` to level 3 | Z4 renamed, required level `HIGH` |
