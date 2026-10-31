# Source layout and documentation conventions

These conventions apply to the Java code in this source tree. Write source
guides in English.

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

## Comments

Java source files contain no comments or Javadoc. Behaviour, rules and limits
are documented in this guide instead, so keep it up to date when code changes.
Choose names that explain the code without a comment.

## Documentation boundaries

Models own field constraints and entity transitions. Services coordinate lookups
and model operations. Repositories own collections and CSV persistence.
Controllers handle console interaction, and views print navigation and status.

Be precise about existing limits: each CSV file is overwritten directly, the four
business files are saved independently, attendance uses insertion order, the
console is single-user and single-threaded, and spreadsheet exports may prefix
formula-like cells. Update these explanations when the implementation changes.

## Run and verify

The application uses Java 8, CSV storage and a console/terminal interface.
Run from the project root in PowerShell:

```powershell
./scripts/run.ps1
./scripts/test.ps1
./scripts/test.ps1 -TestClass ZoneTest
```

If Windows reports that running scripts is disabled, start the same script with
`powershell -ExecutionPolicy Bypass -File scripts/run.ps1` (or `scripts/test.ps1`).

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
are never overwritten. Only one console should use a data directory at a time,
because nothing prevents two sessions from overwriting each other's files.

## Home menu

| Number | Option | Shown to |
| --- | --- | --- |
| 1 | Personnel | MANAGE_PERSONNEL |
| 2 | Zones and permissions | MANAGE_ZONES |
| 3 | Attendance | any attendance permission |
| 4 | Safety incidents | any incident permission |
| 5 | CSV data and export (reload, save, export) | RELOAD_DATA or EXPORT_REPORTS |
| 6 | Attendance report | EXPORT_REPORTS |
| 7 | Incident report | EXPORT_REPORTS |
| 8 | Manage accounts | ADMIN |
| 9 | Change my password | everyone |
| 10 | Sign out | everyone |
| 0 | Save and exit | everyone |

Hidden numbers are still checked at the service boundary, so typing a number
that is not shown is refused.

## Leaving menus and cancelling input

At navigation menus, Enter or 0 chooses Back; at Home it saves and exits.
Lists also accept Enter, 0 or `/cancel` to return. At an entry field, `/cancel`
abandons the current entry and returns to Home. Numeric selections also accept
Enter to cancel.

Cancellation does not submit partially entered personnel, account, attendance,
or report data. Earlier completed operations remain saved. Empty
optional values retain their documented meanings, including keeping contact
values. Zero still means deactivate, disable or no permissions wherever the
prompt says so.

The command `/cancel` is reserved by console readers. Enter `//cancel` to use the
literal text `/cancel`, including as a password; password whitespace is preserved.
At sign-in, cancelling password entry returns to Username without consuming a
failed login attempt. Enter, 0 or `/cancel` at Username exits. Three wrong
passwords close the sign-in screen.

## People, accounts and permissions

Person subclasses are `Worker`, `Staff`, `Contractor` and `Visitor`. Workers have a
required trade and team; staff have a required position. Both have an optional safety
certificate: a worker waiting for training has none and cannot enter restricted zones.
Staff follow the same check-in, check-out and zone rules as other personnel; their
position alone does not grant application access. Personnel can be deactivated; an inactive person
cannot check in again but can check out of an already-open visit.

Accounts are separate from people. A person can be linked to at most one account.
Menu 8 creates accounts, assigns roles and permissions, changes account status,
resets passwords and links personnel. Adding a worker, staff member or contractor
also creates a linked account named from the person's name and code (for example
`LoiNTW99`) with the initial password `0`; staff whose position mentions safety
become SAFETY_OFFICER, managers and supervisors become SITE_MANAGER, everyone else
SITE_USER. Visitors get no account.

| Account role | Default access |
| --- | --- |
| ADMIN | Everything, including account administration |
| SITE_MANAGER | Personnel and zones, attendance and incident viewing, occupancy, reports, personal attendance |
| SAFETY_OFFICER | Incident reporting, assignment and resolution, occupancy, personal attendance |
| SITE_USER | Personal attendance and personal attendance history |

Personal attendance derives identity from the signed-in account; it never accepts
a substitute person code. Staff accounts without a personnel link can still use
their management operations but must be linked before using personal attendance.
Account administration cannot be granted to non-admin roles.
No account can bypass attendance order, restricted-zone rules or visit dates.

Passwords are stored as plain text in `accounts.csv`, which Git and business
exports exclude; keep the data directory private. Every account created by an
administrator or by personnel provisioning, and every reset password, must be
changed at the next sign-in before the home menu opens. A new password must
differ from the current one. Cancelling the change signs the account out again.

## Canonical identifiers

Stored identifiers use an uppercase prefix and a positive, unpadded number:
`P1`, `W1`, `S2`, `Z10`, `INC101`, `ATT1`, `REC1`. Input such as `w01`, `W001`
or `w-001` resolves to `W1`. Prefix-only values, zero and malformed numbers are
rejected. Natural sorting places `S2` before `S10`. All references, including
whitelists and linked accounts, use the same normalizer.

The number is limited to nine digits (`999,999,999`), which fits in an `int`.
That is far more than ten years of use needs: 2,000 people swiping four times a
day for ten years create about 29 million attendance records. New personnel codes,
zone IDs, incident IDs (starting at `INC101`) and attendance IDs (`REC1`, `REC2`,
...) follow the highest existing number for their prefix and fail with a clear
message at the limit. Internal person IDs are picked at random from `P10000` to
`P99999` so they do not reveal registration order; the range holds 90,000 people.

Phone numbers, passwords, usernames, certificate numbers, contract numbers and
free text do not use business identifier normalization. Account usernames retain
their own case-insensitive rules.

## CSV schemas

| File | Columns |
| --- | --- |
| persons.csv | type,id,code,name,phoneNumber,role,extra1,extra2,extra3,active,safetyLevel |
| zones.csv | type,zoneId,zoneName,requiredSafetyLevel,allowedPersonCodes |
| attendance.csv | recordId,personCode,zoneId,timestamp,checkType |
| incidents.csv | incidentId,title,description,severity,status,assignedTo,reportedAt,resolvedAt,archived |
| accounts.csv | username,password,role,enabled,permissions,personCode,mustChangePassword |

Worker extras are certificate/trade/team; Staff extras are certificate/position/
empty; Contractor extras are contract/company/empty; Visitor extras are purpose/
host/visit date. Dates use `dd/MM/yyyy`; timestamps use `yyyy-MM-dd HH:mm:ss`.
Unknown historical incident timestamps and unknown visit dates remain empty; the
loader does not invent values. Files with a different header or column count are
rejected rather than converted.

## Safety levels and restricted zones

Safety levels are ordered `NONE < LOW < MEDIUM < HIGH < CRITICAL`. A regular zone
admits anyone. A restricted zone requires `LOW`, `MEDIUM`, `HIGH` or `CRITICAL`
and admits a person only when all three rules hold, checked in this order:

1. The person's code is on the zone's allowed list (menu 2, grant/revoke).
2. The person has a valid credential for their type: a worker or staff
   certificate ending in `_SAFE`, or a contractor contract starting with `CTR-`.
   Visitors never have one.
3. The person's safety level is at least the zone's level; a higher level also
   covers lower zones.

A denied check-in names the first rule that failed, for example
`Safety level HIGH is below the required CRITICAL.` New personnel start at `NONE`;
the add and edit forms ask for the level only when the credential is valid.
Visitors are always `NONE`.

## Attendance, visit dates and rejected data

The attendance menu (3) offers staff entry and history at options 1/2, personal
entry and history at 3/4, and occupancy at 5/6. Staff entry on behalf of others
needs its separate permission.

A first event must be CHECK_IN. After an entry, only CHECK_OUT at the same zone
is valid. After an exit, only CHECK_IN is valid. Event time cannot precede that
person's previous event. Canonical aliases resolve to the same person before
these rules are checked. References, active status, visit dates and zone rules
are checked by the service; the model owns the transition rule and the repository
also enforces it for direct appends and CSV loads.

A visitor may check in only on the booked visit date. A visitor without a date
must have one recorded first (menu 1, view and update personnel). Check-out is
always allowed so nobody is held inside.

Saving a swipe validates it against the previous event, writes the whole history
to `attendance.csv`, and only then adds the event to memory. A validation or write
failure leaves the in-memory history unchanged and does not use up an identifier.
If the program stops in the middle of writing, the file may be incomplete; the
next start then rejects it instead of guessing the missing rows.

Attendance fields cannot be edited after construction. CSV loading validates the
whole staged history before replacing active records, stopping at the first
invalid row. Because every load and every new swipe is checked, the history in
memory is always valid, and writing `attendance.csv` checks it once more.

Occupancy is derived from the last successful swipe for each person, including
inactive people who have not checked out. Global occupancy requires its own
permission; personal occupancy includes only the linked person.

Invalid startup data leaves business operations blocked; nothing is repaired or
guessed. After signing in, an administrator sees the first rejected file and row
(for example `Invalid attendance.csv: row 7 / ATT6: ...`); other accounts receive a
generic availability message. Correct the source file outside the application using
verified information, then reload it with menu 5, option 1. A reload that still
fails keeps the data blocked, and a failed reload of active data retains it.
To work with an empty registry independently of the sample, use
`./scripts/run.ps1 -DataDirectory D:/SmartSiteData` with a new directory. A CSV
inconsistency is not proof of misconduct.

## Search, archive and reports

Lists show ten rows per page. Use N/P for next/previous, a page number to jump,
F to filter, R to clear filters, D to open a record and 0 to return. Filter terms
combine with AND; identifiers accept canonical aliases. Status words such as
active/inactive are matched as whole words. Authorization limits the collection
before searching or paging.

Incident menu option 5 archives resolved incidents instead of deleting history;
option 6 includes archived records. Archived incidents remain in reports. Resolving
an incident stamps its resolution time; reporting stamps its creation time.

Menus 6 and 7 generate attendance and incident summaries by day, Monday-based week
or calendar month, with inclusive from/to dates. Attendance reports can filter
person and zone; incident reports include unknown historical dates in a separate
row. Exports contain the filtered report and do not include accounts. Menu 5
reloads, saves and exports business CSV data according to account permissions.
