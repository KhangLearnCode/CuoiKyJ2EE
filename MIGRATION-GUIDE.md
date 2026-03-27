# HƯỚNG DẪN SỬ DỤNG - FSM Jobs Modular Pages

## 📁 Cấu Trúc Files Mới

```
src/main/resources/templates/
├── common-styles.html          # CSS dùng chung cho tất cả trang
├── common-scripts.html         # JavaScript utilities & API calls
├── jobs-subnav.html           # Sub-navigation bar cho job pages
│
├── jobs-new.html              # 🆕 Trang chính: Danh sách jobs
├── job-details.html           # 🆕 Chi tiết và thực thi job
├── parts-management.html      # 🆕 Quản lý phụ tùng & inventory
├── scanner.html               # 🆕 QR/Barcode scanner
├── notifications.html         # 🆕 Trang thông báo
├── data-tools.html            # 🆕 Import/Export & thống kê
│
└── jobs.html                  # ⚠️ File gốc (vẫn còn, cần rename/xóa)
```

## 🚀 Cách Triển Khai

### Bước 1: Cấu Hình Spring Boot Routes

Bạn cần thêm các routes sau vào Controller:

```java
@Controller
public class JobsPageController {
    
    @GetMapping("/jobs")
    public String jobsList() {
        return "jobs-new";  // Trỏ đến jobs-new.html
    }
    
    @GetMapping("/jobs/details")
    public String jobDetails() {
        return "job-details";
    }
    
    @GetMapping("/jobs/parts-management")
    public String partsManagement() {
        return "parts-management";
    }
    
    @GetMapping("/jobs/scanner")
    public String scanner() {
        return "scanner";
    }
    
    @GetMapping("/jobs/notifications")
    public String notifications() {
        return "notifications";
    }
    
    @GetMapping("/jobs/data-tools")
    public String dataTools() {
        return "data-tools";
    }
}
```

### Bước 2: Xử Lý Include Files

Spring Thymeleaf không hỗ trợ `<!--#include file="..." -->` như SSI. Bạn có 3 lựa chọn:

#### **Lựa chọn 1: Sử dụng Thymeleaf Fragments (Khuyến nghị)**

1. Đổi tên các file common:
   - `common-styles.html` → `common-styles.fragment.html`
   - `common-scripts.html` → `common-scripts.fragment.html`
   - `jobs-subnav.html` → `jobs-subnav.fragment.html`

2. Thêm Thymeleaf fragment tags:

**common-styles.fragment.html:**
```html
<div th:fragment="styles">
  <style>
    /* ... existing CSS ... */
  </style>
</div>
```

**common-scripts.fragment.html:**
```html
<div th:fragment="scripts">
  <script>
    /* ... existing JS ... */
  </script>
</div>
```

3. Thay thế includes trong các trang:
```html
<!-- Thay vì: -->
<!--#include file="common-styles.html" -->

<!-- Dùng: -->
<div th:replace="~{common-styles.fragment :: styles}"></div>
```

#### **Lựa chọn 2: Copy-Paste Trực Tiếp**

Copy nội dung của `common-styles.html`, `common-scripts.html`, `jobs-subnav.html` vào từng trang.

- **Ưu điểm**: Đơn giản, không cần config
- **Nhược điểm**: Khó maintain khi cần update

#### **Lựa chọn 3: External CSS/JS Files**

1. Tạo thư mục `src/main/resources/static/`:
   - `static/css/fsm-common.css`
   - `static/js/fsm-common.js`

2. Move CSS từ `common-styles.html` vào `fsm-common.css`
3. Move JS từ `common-scripts.html` vào `fsm-common.js`

4. Include trong các trang:
```html
<link href="/css/fsm-common.css" rel="stylesheet"/>
<script src="/js/fsm-common.js"></script>
```

### Bước 3: Testing

1. Backup file gốc:
```bash
mv jobs.html jobs-backup-original.html
mv jobs-new.html jobs.html
```

2. Khởi động ứng dụng và test các routes:
   - `http://localhost:8080/jobs` - Danh sách jobs
   - `http://localhost:8080/jobs/details` - Chi tiết job
   - `http://localhost:8080/jobs/parts-management` - Quản lý parts
   - `http://localhost:8080/jobs/scanner` - QR Scanner
   - `http://localhost:8080/jobs/notifications` - Thông báo
   - `http://localhost:8080/jobs/data-tools` - Data tools (admin only)

## 🎯 Chức Năng Từng Trang

### 1. **jobs-new.html** (Trang Chính)
- ✅ Danh sách jobs với filters
- ✅ Tạo/Sửa job (admin)
- ✅ Thống kê workspace
- ✅ Sub-navbar điều hướng
- ✅ Link đến job details

### 2. **job-details.html**
- ✅ Thông tin job đầy đủ
- ✅ Assign technician (admin)
- ✅ Update status (technician)
- ✅ Checklist management
- ✅ Parts usage recording
- ✅ Upload images
- ✅ Electronic signature
- ✅ Activity timeline
- ✅ Export PDF

### 3. **parts-management.html**
- ✅ Create/Edit parts (admin)
- ✅ Parts list với search
- ✅ Stock management (+10/-10)
- ✅ QR code generation
- ✅ Low stock warnings

### 4. **scanner.html**
- ✅ Camera-based QR scanner
- ✅ Manual barcode lookup
- ✅ Search parts by name
- ✅ View part details
- ✅ Quick actions (use in job, edit)

### 5. **notifications.html**
- ✅ Notifications list
- ✅ Filter by status & type
- ✅ Mark as read / clear
- ✅ Navigate to related jobs/parts
- ✅ Real-time updates (WebSocket)

### 6. **data-tools.html** (Admin Only)
- ✅ Export jobs (CSV/Excel)
- ✅ Import jobs from CSV
- ✅ Statistics dashboard
- ✅ Filter export data

## ⚠️ Lưu Ý Quan Trọng

1. **Authentication**: Tất cả trang đều check `user` trong localStorage. Nếu không có sẽ redirect về `/login`

2. **Role-Based Access**:
   - Admin: Truy cập tất cả trang + create/edit
   - Technician: Chỉ xem và update jobs được assign

3. **WebSocket**: Cần WebSocket endpoint `/ws` đang hoạt động cho real-time notifications

4. **API Endpoints**: Đảm bảo các endpoints sau đang hoạt động:
   - `/api/jobs`, `/api/jobs/{id}`, `/api/jobs/stats`
   - `/api/parts`, `/api/parts/{id}/stock`
   - `/api/notifications`, `/api/notifications/unread-count`
   - `/api/users/technicians`

5. **File Uploads**: Cấu hình Spring Boot cho multipart file uploads:
```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=20MB
```

## 📝 Migration Plan (Nếu Có Lỗi)

Nếu gặp vấn đề, bạn có thể:

1. **Rollback**: Rename `jobs-backup-original.html` về `jobs.html`
2. **Gradual Migration**: Giữ cả 2 versions, test từng page một
3. **Fix Includes**: Chuyển sang Thymeleaf fragments hoặc external CSS/JS

## 🎨 Customization

Để thay đổi giao diện:
- Edit `common-styles.html` cho CSS variables (colors, spacing)
- Edit `jobs-subnav.html` để thêm/bớt menu items
- Edit `common-scripts.html` để thay đổi global behaviors

## 📞 Troubleshooting

**Lỗi 404**: Check Spring Boot routes mapping
**CSS không load**: Verify Thymeleaf fragments hoặc static resources
**JS errors**: Check console, verify API endpoints
**Auth lỗi**: Check localStorage có `user` object không

---

**✅ Tất cả 9 todos đã hoàn thành!**

Bạn đã có một hệ thống modular, dễ maintain hơn nhiều so với file monolithic ban đầu.
