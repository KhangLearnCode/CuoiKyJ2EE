# Field Service Management - Job Module

Module này bổ sung cho project Spring Boot hiện tại một workspace web để demo quản lý công việc ngoài hiện trường, tập trung vào 3 chức năng chính:

1. **Tạo & phân công công việc (Job Dispatch)**
2. **Work Order - cập nhật trạng thái job**
3. **Tìm kiếm & lọc công việc**

Ngoài 3 luồng demo chính, giao diện `/jobs` còn hiển thị các phần hỗ trợ như checklist, upload ảnh, ghi nhận vật tư, chữ ký điện tử, xuất PDF và thông báo.

---

## 1. Công nghệ và màn hình chính

### Màn hình web hiện có

- `/` - trang giới thiệu và kiểm tra nhanh quyền truy cập
- `/login` - đăng nhập
- `/register` - tạo tài khoản
- `/jobs` - workspace thao tác chính

### API liên quan đến 3 demo

- `GET /api/jobs` - tìm kiếm / lọc job
- `POST /api/jobs` - tạo job
- `PUT /api/jobs/{id}` - cập nhật job
- `POST /api/jobs/{id}/assign` - phân công kỹ thuật viên
- `PATCH /api/jobs/{id}/status` - cập nhật trạng thái work order
- `GET /api/users/technicians` - danh sách kỹ thuật viên

---

## 2. Chuẩn bị trước khi chạy demo

### Yêu cầu môi trường

- JDK 17
- Maven Wrapper hoặc Maven cài sẵn
- MySQL đang chạy

### Cấu hình kết nối database

File cấu hình nằm tại:

`src/main/resources/application.properties`

Thông số mặc định:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/nhom13_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=
```

Nếu máy bạn dùng user hoặc password khác, hãy chỉnh lại cho đúng môi trường MySQL.

### Chạy ứng dụng

1. Mở project bằng IntelliJ hoặc IDE tương tự.
2. Đảm bảo Project SDK là **JDK 17**.
3. Chạy class:
   `com.cuoiky.Nhom13.Nhom13Application`
4. Mở trình duyệt và truy cập:
   - `http://localhost:8080/`
   - `http://localhost:8080/login`
   - `http://localhost:8080/register`
   - `http://localhost:8080/jobs`

---

## 3. Tài khoản demo

Khi bảng `jobs` chưa có dữ liệu, hệ thống sẽ seed sẵn tài khoản mẫu:

- `dispatcher / 123456` — tài khoản **Admin/Dispatcher**
- `tech01 / 123456` — tài khoản **Technician**
- `tech02 / 123456` — tài khoản **Technician**

Seed dữ liệu cũng tạo sẵn một số job, vật tư và trạng thái mẫu để bạn nhìn thấy luồng vận hành ngay khi mở `/jobs`.

> Lưu ý: nếu bạn đã có dữ liệu cũ trong database, seed mặc định có thể không chạy lại.

---

## 4. Tổng quan giao diện `/jobs`

Đây là màn hình thao tác chính của demo.

### Cột bên trái

- **Job Form (Admin)**: chỉ hiển thị khi đăng nhập bằng tài khoản Admin
- **Filters**: lọc job theo từ khóa, trạng thái, mức ưu tiên
- **Parts Lookup**: tra cứu vật tư bằng mã/QR/barcode
- **Inventory (Admin)**: chỉ hiển thị cho Admin

### Cột giữa

- Danh sách **Jobs**
- Mỗi card job hiển thị:
  - `jobCode`
  - tiêu đề job
  - khách hàng
  - trạng thái
  - số checklist
  - số ảnh đính kèm

### Cột bên phải

- **Selected Job**: chi tiết job đang được chọn
- Có thể xem:
  - mã job
  - trạng thái hiện tại
  - người được phân công
  - **Assign Technician** (chỉ Admin, chỉ khi job chưa hoàn tất)
  - mô tả
  - checklist
  - vật tư đã dùng
  - ảnh upload
  - chữ ký
  - timeline

### Khác biệt theo vai trò

- **Admin/Dispatcher** thấy thêm:
  - Form tạo job
  - Form phân công kỹ thuật viên (trong Selected Job)
  - Quản lý inventory
  - Tất cả job trong hệ thống
- **Technician** thấy:
  - Chỉ job được giao cho mình
  - Form cập nhật trạng thái job (trong Selected Job)
  - Các công cụ thực thi: checklist, vật tư, ảnh, chữ ký

---

## 5. Demo 1 - Tạo & phân công công việc (Job Dispatch)

Mục tiêu của demo này là tạo một job mới từ giao diện web, sau đó quan sát job đó trong danh sách và trạng thái phân công của nó.

### Bước 1: Đăng nhập bằng tài khoản Dispatcher/Admin

1. Mở `http://localhost:8080/login`
2. Đăng nhập bằng:
   - `dispatcher`
   - `123456`
3. Sau khi đăng nhập thành công, vào workspace bằng nút **Open workspace** hoặc truy cập trực tiếp `/jobs`.

### Bước 2: Tạo job mới

1. Ở cột trái, nhìn vào khối **Job Form (Admin)**.
2. Điền đầy đủ thông tin:
   - **Title**: tiêu đề công việc
   - **Customer**: tên khách hàng
   - **Address**: địa chỉ thực hiện
   - **Scheduled Date**: ngày hẹn
   - **Priority**: `LOW`, `MEDIUM`, `HIGH`, `URGENT`
   - **Description**: mô tả thêm nếu cần
3. Bấm **Save Job**.

### Bước 3: Kiểm tra job vừa tạo

1. Job mới sẽ xuất hiện trong danh sách ở cột giữa.
2. Bấm **Open** để mở chi tiết job ở cột phải.
3. Tại khung **Selected Job**, bạn sẽ thấy:
   - `jobCode`
   - trạng thái hiện tại
   - `assignedUsername` nếu đã phân công
   - mô tả job

### Bước 4: Phân công kỹ thuật viên (chỉ Admin)

1. Sau khi tạo job, bấm **Open** để mở chi tiết job ở cột phải.
2. Trong khung **Selected Job**, bạn sẽ thấy khối **Assign Technician** (chỉ hiện với Admin).
3. Chọn kỹ thuật viên từ dropdown.
4. Nhập ghi chú nếu cần (ví dụ: "Urgent - customer VIP").
5. Bấm **Assign Job**.
6. Sau khi assign thành công:
   - Dòng trạng thái sẽ hiển thị tên kỹ thuật viên được giao
   - Trạng thái job tự động chuyển từ `CREATED` sang `ASSIGNED`
   - Timeline bên dưới ghi lại hoạt động phân công

> Lưu ý: Form assign chỉ hiện khi job chưa `COMPLETED` hoặc `CANCELLED`.

### Bước 5: Ý nghĩa nghiệp vụ của demo

Luồng Job Dispatch mô phỏng:

1. Dispatcher tạo job mới
2. Job được đưa vào danh sách chờ xử lý
3. Job được giao cho kỹ thuật viên
4. Trạng thái job chuyển sang `ASSIGNED`

> Nếu bạn muốn demo phân công trực tiếp bằng API thay vì chỉ quan sát trên web, hãy gọi `POST /api/jobs/{id}/assign`.

---

## 6. Demo 2 - Work Order: cập nhật trạng thái job

Mục tiêu của demo này là mô tả vòng đời xử lý work order và cách trạng thái job thay đổi theo quy tắc nghiệp vụ.

### Trạng thái job trong hệ thống

Project hiện có các trạng thái:

- `CREATED`
- `ASSIGNED`
- `IN_PROGRESS`
- `COMPLETED`
- `CANCELLED`

### Quy tắc chuyển trạng thái

- `CREATED` chỉ được chuyển sang `ASSIGNED` hoặc `CANCELLED`
- `ASSIGNED` chỉ được chuyển sang `IN_PROGRESS` hoặc `CANCELLED`
- `IN_PROGRESS` chỉ được chuyển sang `COMPLETED` hoặc `CANCELLED`
- `COMPLETED` và `CANCELLED` là trạng thái kết thúc, không đổi tiếp

### Cách demo trên giao diện web

1. Đăng nhập bằng tài khoản **tech01** hoặc **tech02**.
2. Mở `/jobs`.
3. Bạn sẽ **chỉ thấy các job được giao cho mình** (không thấy job của technician khác).
4. Chọn một job trong danh sách.
5. Xem khung **Selected Job**, bạn sẽ thấy:
   - Trạng thái hiện tại
   - Khối **Update Job Status** với các nút phù hợp theo quy tắc chuyển trạng thái
6. Thực hiện cập nhật trạng thái:
   - Nếu job đang `ASSIGNED`: hiện nút **Start Work** (→ `IN_PROGRESS`) và **Cancel**
   - Nếu job đang `IN_PROGRESS`: hiện nút **Complete Job** (→ `COMPLETED`) và **Cancel**
   - Nếu job đã `COMPLETED` hoặc `CANCELLED`: không hiện nút nào

### Thao tác trên giao diện

Ví dụ với job đang ở trạng thái `ASSIGNED`:

1. Mở job trong **Selected Job**
2. Cuộn xuống khối **Update Job Status**
3. Thấy dòng "Current: **ASSIGNED**"
4. Bấm **Start Work** → trạng thái chuyển sang `IN_PROGRESS`
5. Sau khi hoàn thành công việc, bấm **Complete Job** → trạng thái chuyển sang `COMPLETED`

### Cách cập nhật trạng thái (kỹ thuật)

Backend hỗ trợ endpoint:

`PATCH /api/jobs/{id}/status`

API này nhận một `status` mới và áp dụng rule chuyển trạng thái ở phía server.

### Gợi ý kịch bản demo

Bạn có thể trình bày luồng như sau:

1. Dispatcher tạo job
2. Dispatcher phân công job cho technician
3. Technician mở job trong `/jobs`
4. Technician thực hiện công việc, ghi nhận checklist / vật tư / ảnh
5. Technician hoặc Admin cập nhật trạng thái lên `IN_PROGRESS`
6. Khi hoàn tất, chuyển sang `COMPLETED`

### Dấu hiệu trên giao diện

Sau khi trạng thái thay đổi:

- card job ở cột giữa sẽ hiển thị trạng thái mới
- khung **Selected Job** cũng cập nhật theo
- timeline bên dưới sẽ ghi lại hoạt động tương ứng

---

## 7. Demo 3 - Tìm kiếm & lọc công việc

Mục tiêu của demo này là giúp người dùng tìm đúng job rất nhanh trong danh sách.

### Vị trí bộ lọc

Bộ lọc nằm ở cột trái, khối **Filters**.

### Các trường lọc hiện có

- **Keyword**: tìm theo từ khóa
- **Status**: lọc theo trạng thái job
- **Priority**: lọc theo mức ưu tiên

### Cách sử dụng

1. Nhập từ khóa vào ô **Keyword**
   - ví dụ: tên khách hàng
   - ví dụ: một phần của tiêu đề job
   - ví dụ: job code nếu bạn nhớ mã
2. Chọn **Status**
   - `CREATED`
   - `ASSIGNED`
   - `IN_PROGRESS`
   - `COMPLETED`
   - `CANCELLED`
3. Chọn **Priority**
   - `LOW`
   - `MEDIUM`
   - `HIGH`
   - `URGENT`
4. Bấm **Load Jobs** để tải danh sách phù hợp.

### Kết quả trên giao diện

- Danh sách job ở cột giữa sẽ thu gọn theo điều kiện lọc
- Số lượng job hiển thị ở badge **Jobs**
- Nếu đang mở một job cũ, màn hình sẽ cố gắng giữ job đó nếu nó vẫn còn trong kết quả lọc

### Mẹo demo nhanh

- Lọc theo `URGENT` để nhấn mạnh job cần xử lý gấp
- Lọc theo `IN_PROGRESS` để xem work order đang chạy
- Lọc theo keyword của khách hàng để minh họa tìm kiếm thực tế

---

## 8. Một số chức năng hỗ trợ trên `/jobs`

Các chức năng này không phải 3 demo chính nhưng rất hữu ích khi trình bày:

- **Checklist**: thêm và cập nhật từng bước công việc
- **Use Part**: ghi nhận vật tư đã dùng cho job
- **Upload Images**: đính kèm ảnh hiện trường
- **Electronic Signature**: lưu chữ ký khách hàng
- **Export PDF**: xuất báo cáo job
- **Notifications**: xem thông báo và đánh dấu đã đọc
- **Export/Import**: xuất danh sách job CSV/XLSX, import job từ CSV
- **Dashboard Stats**: thống kê số lượng theo trạng thái/priority trên landing và workspace
- **Low Stock Alerts**: cảnh báo tồn kho thấp cho admin

---

## 9. Lưu ý khi demo

- Tài khoản **Admin** nhìn thấy nhiều công cụ hơn trên `/jobs`
- Tài khoản **Technician** chỉ thấy phần phù hợp với nghiệp vụ thực hiện công việc
- Nếu không thấy dữ liệu mới, hãy kiểm tra:
  - đã đăng nhập đúng chưa
  - database có đang dùng dữ liệu cũ không
  - `app.seed.enabled=true`
- Nếu MySQL chưa chạy hoặc sai mật khẩu, ứng dụng sẽ không khởi động được

---

## 10. Gợi ý trình bày demo

Nếu bạn cần thuyết minh trước lớp hoặc trong buổi bảo vệ, có thể đi theo thứ tự này:

### Phần 1: Demo với tài khoản Dispatcher

1. Vào `/login`, đăng nhập bằng `dispatcher/123456`
2. Vào `/jobs`
3. **Demo Job Dispatch**:
   - Tạo một job mới bằng form bên trái
   - Chọn job vừa tạo
   - Cuộn xuống form **Assign Technician**
   - Chọn `tech01` và bấm **Assign Job**
   - Quan sát trạng thái chuyển từ `CREATED` → `ASSIGNED`
4. **Demo Search & Filter**:
   - Lọc job theo trạng thái `IN_PROGRESS`
   - Lọc job theo priority `URGENT`
   - Tìm kiếm theo từ khóa (tên khách hàng)
5. Đăng xuất

### Phần 2: Demo với tài khoản Technician

1. Đăng nhập bằng `tech01/123456`
2. Vào `/jobs`
3. Nhấn mạnh: **chỉ thấy job được giao cho tech01** (không thấy job của tech02)
4. **Demo Work Order**:
   - Mở một job đang `ASSIGNED`
   - Cuộn xuống khối **Update Job Status**
   - Bấm **Start Work** → trạng thái chuyển `IN_PROGRESS`
   - (Có thể demo thêm checklist, upload ảnh, ghi nhận vật tư)
   - Bấm **Complete Job** → trạng thái chuyển `COMPLETED`
5. Quan sát timeline để thấy lịch sử thay đổi

### Kết luận

Nhấn mạnh rằng giao diện web đã tích hợp:
- **Job Dispatch** (tạo và phân công) - Admin
- **Work Order** (cập nhật trạng thái theo quy tắc) - Technician
- **Search & Filter** (tìm kiếm nhanh) - Cả hai role
- Và nhiều chức năng hỗ trợ khác trong một workspace duy nhất

