## Liquibase Developer Workflow (Maven)

### Goals
- One-line commands via Maven (no manual CLI flags)
- Credentials come from Spring `application-<profile>.properties`
- No-drop policy; raw SQL preferred for views/functions

### Maven commands
- Validate changelog
  - QA: `mvn initialize -Pqa liquibase:validate`
  - PROD: `mvn initialize -Pprod liquibase:validate`

- Preview SQL (no execution)
  - QA: `mvn initialize -Pprod liquibase:status -Dliquibase.verbose=true`
  - PROD: `mvn initialize -Pprod liquibase:status -Dliquibase.verbose=true`

- Apply updates
  - QA: `mvn initialize -Pqa liquibase:update`
  - PROD: `mvn initialize -Pprod liquibase:update`

- Mark baseline executed (one-time per environment)
  - QA: `mvn initialize -Pqa liquibase:changelogSync`
  - PROD: `mvn initialize -Pprod liquibase:changelogSync`

- Show history
  - QA: `mvn initialize -Pqa liquibase:history`

### Profiles and properties
- Profiles: `local` (default), `qa`, `prod`
- Each profile loads `src/main/resources/application-<profile>.properties` and maps:
  - `spring.datasource.url` → Liquibase `url`
  - `spring.datasource.username` → Liquibase `username`
  - `spring.datasource.password` → Liquibase `password`
- ChangeLog resolution pinned to `src/main/resources/db/changelog` and `db.changelog-master.xml`.

### File locations
- Master: `src/main/resources/db/changelog/db.changelog-master.xml`
- Baseline: `src/main/resources/db/changelog/baseline/`
- Changes: `src/main/resources/db/changelog/changes/`

### Standards
- Place new changes in `changes/` (no date folders required). Keep preConditions; if destructive, separate approval.
- Views/functions: prefer raw SQL in XML with `<![CDATA[...]]>` and `CREATE OR REPLACE` forms.
- Add `logicalFilePath` if path identity needs to be stable across envs.

### Rollback quick refs
- Tag before deploy: `mvn initialize -Pprod liquibase:tag -Dliquibase.tag=prod-pre-release`
- Roll back to tag: `mvn initialize -Pprod liquibase:rollback -Dliquibase.tag=prod-pre-release`
- Roll back last N: `mvn -Pqa liquibase:rollbackCount -Dliquibase.rollbackCount=1`
- One changeset (if supported): `mvn -Pqa liquibase:rollbackOneChangeSet -Dliquibase.changesetId=... -Dliquibase.changesetAuthor=... -Dliquibase.changesetPath=...`

### Flow diagram

```mermaid
flowchart TD
  Dev[Dev adds XML in changes/] --> Master[Master includes file]
  Master --> CIValidate[mvn -Pqa liquibase:validate]
  CIValidate --> CIUpdateSQL[mvn -Pqa liquibase:updateSQL (artifact)]
  CIUpdateSQL --> Approve[Manual approval]
  Approve --> CIUpdateQA[mvn -Pqa liquibase:update]
  CIUpdateQA --> Tag[Tag release]
  Tag --> ProdUpdateSQL[mvn -Pprod liquibase:updateSQL (artifact)]
  ProdUpdateSQL --> ApproveProd[Manual approval + Backup]
  ApproveProd --> ProdUpdate[mvn -Pprod liquibase:update]
```

### Notes
- Keep `application-qa.properties` and `application-prod.properties` in CI secret store or override via environment variables for credentials.
- Ensure migrator user has CREATE/ALTER and access to Liquibase tables.


