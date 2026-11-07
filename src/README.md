# SmartSite source guide

Java 8 console application. Flow: `Main -> controller -> service -> repository -> CSV`.

## Responsibilities

- **Models** own field rules and state changes: `Person`, `Worker`, `Contractor`, `Visitor`, `Zone`,
  `RestrictedZone`, `AttendanceRecord`, `Incident`, `Account`.
- **Services** look things up and coordinate. They never use `Scanner` or `System.out`.
- **Repositories** own the collections and CSV files.
- Services use the CSV repositories directly. CSV is the only persistence format in this version.
- **Controllers and views** do all console interaction and check permissions before each menu action.

## Roles

A person is a `Worker`, `Contractor` or `Visitor`. There is no management subclass: management capability is
the `Account.Role` of the linked account, and `Person.position` is the job title.

| Role | Default permissions |
| --- | --- |
| `ADMIN` | All |
| `SITE_MANAGER` | `MANAGE_PERSONNEL`, `MANAGE_ZONES`, `VIEW_ATTENDANCE`, `VIEW_INCIDENTS`, `VIEW_OCCUPANCY`, `EXPORT_REPORTS`, own attendance |
| `SAFETY_OFFICER` | `REPORT_INCIDENTS`, `VIEW_INCIDENTS`, `ASSIGN_INCIDENTS`, `RESOLVE_INCIDENTS`, `VIEW_OCCUPANCY`, own attendance |
| `SITE_USER` | Own attendance only; must be linked to a person |

- Changing a position on a person with an account needs an explicit role in the same action.
- `SITE_MANAGER` and `SAFETY_OFFICER` linked to a person need a position.
- An account cannot change its own role or disable itself. Changing a role resets permissions to the defaults.

## Rules

- Restricted-zone entry, checked in this order: on the allowed list, valid credential (`_SAFE` certificate,
  `CTR-` contract, never for visitors), safety level covers the zone (`NONE < LOW < MEDIUM < HIGH < CRITICAL`).
- A level above `NONE` needs a valid credential; losing the credential returns the level to `NONE`.
- Check-in needs an active person; a visitor also needs the booked date. Check-out is always allowed so nobody
  stays recorded inside a zone.
- Attendance alternates `CHECK_IN` and `CHECK_OUT`; a check-out must match the zone of the check-in.
- Incidents move `OPEN -> ASSIGNED -> RESOLVED`. The handler can change until the incident is resolved. Only an
  active non-visitor can be a handler.
- A person with attendance records, a restricted-zone listing or an assigned incident cannot be deleted. Deleting
  a person also deletes the linked account, which needs `MANAGE_ACCOUNTS`.
- Sign-in checks the account is enabled, the password, then the linked person. Every failure shows the same
  message, and three failures close the sign-in screen.

## CSV files

All files are UTF-8 with BOM in `data/` (`-Dsmartsite.dataDir` selects another directory). A wrong header, wrong
column count or invalid value rejects the whole file and names the line. Rejected data blocks business menus and
saving until menu 5 -> 1 reloads a corrected file. A missing file starts empty.

| File | Columns |
| --- | --- |
| `persons.csv` | `type,id,code,name,phoneNumber,position,active,safetyLevel,extra1,extra2,extra3` |
| `zones.csv` | `type,zoneId,zoneName,requiredSafetyLevel,allowedPersonCodes` (`;` between codes) |
| `attendance.csv` | `recordId,personCode,zoneId,timestamp,checkType` (`yyyy-MM-dd HH:mm:ss`) |
| `incidents.csv` | `incidentId,title,description,severity,status,assignedTo,reportedAt` |
| `accounts.csv` | `username,password,role,enabled,permissions,personCode,mustChangePassword` |

`extra1..3` by type: `WORKER` trade, certificate, team; `CONTRACTOR` contract, company, empty; `VISITOR` purpose,
host, visit date (`dd/MM/yyyy`, may be empty). `accounts.csv` needs one enabled `ADMIN`; when the file is missing
the application creates `admin` / `admin`, which must change its password. Passwords are plain text by
requirement. Existing six-column `incidents.csv` files remain readable. New saves add the `reportedAt` column so
weekly and monthly incident reports can filter newly created incidents. Account changes are written immediately;
other data is saved on sign-out, exit and menu 5 -> 2.

## Menus

Home: 1 Person, 2 Zone, 3 Attendance, 4 Incident, 5 Data, 6 Accounts, 7 Change password, 8 Sign out, 9 Reports, 0 Exit.

Zone management supports add, list, update, delete, grant and revoke. A zone with attendance history is protected from deletion.

## Style

4-space indent, braces always, one statement per line, at most 120 characters, no comments or Javadoc.

Model class member order:
1. Static constants (`static final`)
2. Nested types (enums)
3. Instance fields
4. Constructors
5. Public static utility methods
6. Pure state queries and getters (`getX`, `isX`)
7. Business methods (state mutations, domain checks, abstract hooks)
8. Private and protected helper methods
9. Serialization and representation (`displayDetails`, `toCsvLine`, `toString`)

## Reports

The Reports menu requires EXPORT_REPORTS. It displays current-week, current-month or custom-period attendance and incident data, shows current occupancy by zone, and exports UTF-8 BOM CSV files suitable for Excel. Legacy incidents without a timestamp are shown separately and exported to `incident-undated.csv`.

