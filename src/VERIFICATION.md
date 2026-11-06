# Lần chạy kiểm thử gần nhất

Số liệu lấy từ lần chạy thật bằng `scripts/test.ps1 -TestClass <tên>` từng bộ, không ghi tay.
Sau mỗi lần chạy, `data/` được kiểm tra bằng MD5 và không bị thay đổi.

| Bộ test | Đạt | Lỗi |
| --- | --- | --- |
| `AccountDomainTest` | 22 | 0 |
| `AccountServiceTest` | 20 | 0 |
| `AttendanceIncidentDomainTest` | 5 | 0 |
| `CheckInRulesTest` | 10 | 0 |
| `ConsoleFlowTest` | 11 | 0 |
| `ConsoleUiTest` | 1 | 0 |
| `PaginationTest` | 1 | 0 |
| `PersonCsvTest` | 10 | 0 |
| `PersonDomainTest` | 9 | 0 |
| `RegressionMatrixTest` | 42 | 0 |
| `ReportServiceTest` | 3 | 0 |
| `ReviewFixesTest` | 13 | 0 |
| `RichDomainWorkflowTest` | 3 | 0 |
| `SiteCsvTest` | 9 | 0 |
| `StartupSafetyTest` | 5 | 0 |
| `ZoneDomainTest` | 9 | 0 |
| **Tổng** | **173** | **0** |

Các bộ test cũ của nhánh `backup/before-return-7798417` chưa được chuyển sang đây. Xem `docs/TEST_MIGRATION.md`.
