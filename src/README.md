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

## Sign-in and account administration

Start the application normally and sign in with username `admin` and password
`admin` on a new installation. The default account is created only when
`accounts.csv` does not exist in the selected data directory. Startup preserves
existing credentials and stops if that file is invalid; it never resets an
existing password to the default.

Choose **6 - Account administration** as an administrator to list or create
accounts, assign roles, replace account permissions, enable or disable an
account, or reset a password. Choose **7 - Change my password** to change the
signed-in account's password, or **8 - Sign out** to switch users. Password
creation and reset require confirmation in the console.

Login roles are declared in `Account.Role` and are separate from the personnel
labels in `Role`. Creating a worker, contractor or visitor does not create a
login account. Staff accounts do not automatically receive physical access to
restricted zones.

| Login role | Default access |
| --- | --- |
| `ADMIN` | All application operations and account administration |
| `SITE_MANAGER` | Personnel and zone management, attendance and incident viewing, report exports |
| `SAFETY_OFFICER` | Incident reporting, viewing, assignment and resolution |

Administrators can replace a staff account's permissions with a selected subset
of application operations. Assigning a role restores that role's defaults.
Account administration is reserved for `ADMIN`, and administrator accounts retain
all permissions. At least one enabled administrator must remain. A signed-in
account cannot disable itself.

Menus retain stable option numbers while hiding unavailable operations. Entering
a hidden number still triggers permission validation before the operation runs.
Permission changes apply to subsequent operations, and successful account changes
are saved immediately. A failed save retains the previous in-memory accounts.

The four business CSV files remain independent of `accounts.csv`. Accounts are
excluded from business exports, and the credential file is ignored by Git.
Passwords use PBKDF2-HMAC-SHA256 with 600,000 iterations, a random 16-byte salt
and a 32-byte derived key. This configuration follows the PBKDF2 parameters in
the [OWASP password storage guidance](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html).
The account list never displays stored password hashes. Native consoles hide
password input; embedded terminals and redirected input use the line reader.

Authentication and permissions govern the staff console. Business services and
repositories remain reusable inside the application; the facade and controllers
guard interactive operations. The data-directory lock still limits access to
one cooperating console process at a time.
