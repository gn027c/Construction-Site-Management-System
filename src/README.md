# Source layout and documentation conventions

These conventions apply to Java code and developer notes in this source tree.
Keep source notes here and beside the code they explain. Write comments,
Javadoc and source guides in English.

## Java layout

- Use four spaces for each indentation level and eight additional spaces for
  continued expressions or argument lists.
- Put annotations on separate lines immediately above the declaration.
- Put an opening brace at the end of the declaration or control statement and
  a closing brace on its own line. Expand method bodies even for simple getters.
- Always use braces for `if`, `else` and loop bodies. Keep `else if` chains clear.
- Put each statement on a separate line. Expand `switch` branches and
  `try`/`catch`/`finally` bodies instead of combining their statements.
- Aim for at most 120 characters per line. Wrap long parameter lists and calls;
  place their closing parenthesis on a separate line when the list is expanded.
- For a long return call, put each argument on its own line and finish with
  `);`. Keep short return expressions on one line inside a multiline method.
- Keep related fields together and separate methods with a blank line.

```java
@Override
public String toString() {
    return String.format(
            "Zone{zoneId='%s', zoneName='%s'}",
            zoneId,
            zoneName
    );
}

public void setName(String name) {
    if (name == null) {
        throw new IllegalArgumentException("Name must not be null.");
    }
    this.name = name;
}
```

The example illustrates layout only. Actual model validation uses the shared
domain guards and the application's existing exception types.

## Class Javadoc

Start with one sentence describing the class's purpose. Add paragraphs for
important rules, limitations or collaborating classes. Keep the description
focused on the program's behavior.

Exclude member names, student identifiers, work allocation, contribution history
and branch assignments. Avoid workshop-specific instructions, obsolete TODOs
and claims about behavior that the implementation does not provide.

## Method Javadoc

Write a plain description of the method's purpose. Add a short paragraph only
when an important rule, side effect or failure behavior needs explanation.
Keep argument requirements and result meanings in natural sentences when they
are necessary to understand the operation; do not add separate documentation
tags or parameter, return-value and exception sections.

Use `{@code ...}` for identifiers, formats and literal values, and `{@link ...}`
when a related type or method helps explain the description. Explain collection
copies and shared model objects where relevant. Trivial getters do not need
comments that merely repeat their names.

```java
/**
 * Loads CSV into a temporary repository before replacing active data.
 * Invalid data leaves the current collection unchanged.
 */
```

## Inline comments

Explain the reason for a non-obvious decision, such as strict calendar parsing
or best-effort temporary-file cleanup. Keep comments next to the relevant code.
Use Javadoc for caller-facing contracts rather than embedding those contracts
as scattered implementation notes.

## Documentation boundaries

Models own field constraints and entity transitions. Services coordinate lookups
and model operations. Repositories own collections and CSV persistence.
Controllers handle console interaction, and views print navigation and status.

Be precise about existing limits: atomic replacement protects one CSV file,
the four files are saved independently, attendance uses insertion order, and
spreadsheet exports may prefix formula-like cells. Update these explanations
when the corresponding implementation changes.

## Run and verify

The application uses Java 8, CSV storage and a console/terminal interface.
Run from the project root in PowerShell:

```powershell
./scripts/run.ps1
./scripts/test.ps1
```

When the terminal is inside `src`, run `../scripts/run.ps1` instead. The launcher
always compiles from the project root with UTF-8 and writes classes to `bin/app`.
Use `./scripts/run.ps1 -BuildOnly` to compile without starting the application.
Do not use bare `javac Main.java`: Java 8 on Windows may use Cp1252 and reject
UTF-8 source text with an `unmappable character for encoding Cp1252` error.
Changing only the terminal code page does not set the compiler's source encoding.

Interactive ANSI terminals clear the screen when navigating between menus, forms
and list pages. Results remain visible until Enter is pressed. Sign-in and
sign-out request clearing both the visible screen and terminal scrollback.
Screen clearing is presentation only: terminal support varies and captured
output is not erased. Never share an administrator's terminal session or expose
the data directory to accounts that should not read it.

Redirected input/output stays sequential without extra acknowledgement prompts.
Use `./scripts/run.ps1 -ScreenMode never` to disable clearing, or `-ScreenMode always`
for a terminal that supports ANSI but is not detected automatically. The matching
Java option is `-Dsmartsite.screen=auto|always|never`.

Select a different directory with `./scripts/run.ps1 -DataDirectory D:/SmartSiteData`.
The first installation creates `admin` with password `admin`; existing credentials
are never overwritten. Change the password through menu 7. Menu 8 signs out.

## People, accounts and permissions

Person subclasses are `Worker`, `Staff`, `Contractor` and `Visitor`. Staff have a
required position and an optional safety certificate. They follow the same
check-in, check-out and zone rules as other personnel. Their position alone does
not grant application access. Personnel can be deactivated; an inactive person
cannot check in again but can check out of an already-open visit.

Accounts are separate from people. A person can be linked to at most one account.
Admin menu 6 creates accounts, assigns roles and permissions, changes account
status, resets passwords and links personnel. The creation flow requests the
personnel code before the password for non-admin roles.

| Account role | Default access |
| --- | --- |
| ADMIN | Application and account administration, logs and data review |
| SITE_MANAGER | Personal attendance, personnel/zones, occupancy, reports |
| SAFETY_OFFICER | Personal attendance, occupancy, incident handling |
| SITE_USER | Personal attendance and personal attendance history |

Personal attendance derives identity from the signed-in account; it never accepts
a substitute person code. Staff accounts without a personnel link can still use
their management operations but must be linked before using personal attendance.
Administrator-only log and review permissions cannot be granted to staff roles.
No account can bypass attendance order or restricted-zone credentials.

## Canonical identifiers

Stored identifiers use an uppercase prefix and a positive, unpadded number:
`P1`, `W1`, `S2`, `Z10`, `INC101`, `ATT1`, `REC1`. Input such as `w01`, `W001`
or `w-001` resolves to `W1`. Prefix-only values, zero and malformed numbers are
rejected. Natural sorting places `S2` before `S10`. All references, including
whitelists and linked accounts, use the same normalizer.

Phone numbers, passwords, usernames, certificate numbers, contract numbers and
free text do not use business identifier normalization. Account usernames retain
their own case-insensitive rules. Migration stops on identifier collisions and
never silently merges records.

## CSV schemas and migration

| File | Columns |
| --- | --- |
| persons.csv | type,id,code,name,phoneNumber,role,extra1,extra2,extra3,active |
| zones.csv | type,zoneId,zoneName,requiredSafetyLevel,allowedPersonCodes |
| attendance.csv | recordId,personCode,zoneId,timestamp,checkType |
| incidents.csv | incidentId,title,description,severity,status,assignedTo,reportedAt,resolvedAt,archived |
| accounts.csv | username,passwordHash,role,enabled,permissions,personCode |
| activity_log.csv | logId,operationId,timestamp,actorUsername,actorRole,action,targetType,targetId,status,reason |
| notifications.csv | notificationId,recipientUsername,operationId,timestamp,message,read |

Worker extras are certificate/trade/team; Staff extras are certificate/position/
empty; Contractor extras are contract/company/empty; Visitor extras are purpose/
host/date. Dates use `dd/MM/yyyy`; timestamps use `yyyy-MM-dd HH:mm:ss`.
Unknown historical incident timestamps remain empty. Migration does not invent
dates or fix unknown attendance history.

Convert an older CSV directory once into a new destination:

```powershell
java '-Dfile.encoding=UTF-8' -cp bin/app util.CsvMigration D:/OldData D:/ConvertedData
```

The converter preserves source files, credentials and free-text fields, checks
collisions/references before writing, and refuses an existing destination. It
converts old personnel, incident and account schemas; runtime loaders use the new
schemas. Existing activity and notification files are not copied by migration.
Each business CSV replacement is atomic; the collection is not a multi-file
transaction. No file-based application snapshot or state store is used.

## Attendance, occupancy and data review

Attendance menu 3 retains staff entry/history at options 1/2, and adds personal
entry/history at 3/4 and occupancy at 5/6. Staff entry on behalf of others needs
its separate permission. Failed swipes are logged with `FAILED` but are not added
to attendance.csv. A recorded check-in at one zone must be checked out there
before entering another. Concurrent submissions are serialized. If attendance
storage fails, the staged swipe and sequence are rolled back so retry is safe.

Occupancy is derived from the last successful swipe for each person, including
inactive people who have not checked out. Global occupancy requires its own
permission; personal occupancy includes only the linked person.

The scanner validates row formats, references and complete swipe order. A failed
reload preserves active data. Invalid startup data keeps business operations
blocked while an administrator can sign in and use menu 11. The included sample
history deliberately contains invalid historical swipes; verify the evidence
before correcting them. Menu 11 lists row/record/person issues and accepts an
explicitly confirmed correction with reason and evidence. Original attendance
values and the verified correction are retained in the administrator log. The
whole history must pass validation before corrected data becomes active. Detailed
startup diagnostics appear only after administrator sign-in; anonymous sessions
receive no CSV health information. Other accounts receive a generic service
availability message. A mismatch indicates inconsistent data, not proven misconduct.

Use the administrator review workflow:

1. Open menu 11, then option 1 to find the record and personnel codes.
2. Use option 3 and enter that personnel code to inspect their events in CSV order.
3. Verify the actual events against a reliable register or the data owner.
4. Use option 2 and enter the record ID. The original record is displayed first.
   Leave zone/time blank, or choose action 0, to retain the current value.
5. Enter the verified reason and evidence/reference, inspect the proposed values,
   then answer `yes` to save. Answering anything else preserves the record.
6. Scan again if other inconsistencies remain. Missing or duplicate events require
   reconciliation with the data owner; editing a row cannot recover an unknown event.

For example, a verified checkout entered with the wrong zone can be corrected
to the preceding check-in's zone while retaining its time and action. This is
appropriate only when the supporting register confirms the zone. The sample's
ATT6/ATT8 issues alone do not establish which timestamps or events are missing.

The scanner does not infer race conditions or manufacture missing checkout records.

## Activity history and notifications

Activity history records logins, account operations, protected business actions,
reads/search/navigation, exports, denied operations and verified corrections.
Statuses are SUCCESS, FAILED and CANCELLED. Menu 9 is restricted to ADMIN at the
service boundary. Passwords, salts and credential hashes are excluded. Reading
logs does not cause recursive logging.

Menu 10 exposes only the current account's inbox and its own read markers. Failed
attendance notifies the actor with a generic failed-operation message and enabled
administrators with diagnostic details, with an operation identifier when logging
succeeds. Account changes also notify the affected user.
A log or notification storage failure does not reverse successfully persisted
business data; only the administrator sees storage diagnostics. If storage is
unavailable, the application does not claim that the missing record was saved.
Credentials, runtime activity and notifications are excluded from Git and
business report exports. Passwords use PBKDF2-HMAC-SHA256 with 600,000 iterations
and independent salts, following the [OWASP password storage guidance](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html).

## Search, archive and reports

Lists show ten rows per page. Use N/P for next/previous, a page number to jump,
F to filter, R to clear filters and 0 to return. Filter terms combine with AND;
identifiers accept canonical aliases. Status words such as active/inactive and
success/failed are matched as whole words. Authorization limits the collection
before searching or paging.

Incident menu 5 archives resolved incidents instead of deleting history; option
6 includes archived records. Archived incidents remain in reports. Resolving an
incident stamps its resolution time; reporting stamps its creation time.

Menu 12 generates attendance and incident summaries by day, Monday-based week or
calendar month, with inclusive from/to dates. Attendance reports can filter person
and zone; incident reports include unknown historical dates in a separate row.
Exports contain the filtered report and do not include accounts or logs. Menu 5
still saves/reloads/exports business CSV data according to account permissions.
