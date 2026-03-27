# HƯỚNG DẪN DEMO HỆ THỐNG FSM CHI TIẾT, DỄ HIỂU

---

## 1. GIỚI THIỆU NHANH
- **FSM** là hệ thống quản lý kỹ thuật viên ngoài hiện trường.
- Demo gồm các chức năng: Đăng ký/đăng nhập, phân quyền, tạo & phân công công việc, checklist, quản lý vật tư, upload ảnh, ký nhận, xuất PDF, thông báo, quét QR/barcode.
- Giao diện web chia vai trò rõ ràng, thao tác trực quan.

---

## 2. CHUẨN BỊ MÔI TRƯỜNG
1. **Cài đặt:**
   - JDK 17
   - MySQL (tạo sẵn database hoặc để hệ thống tự tạo)
   - Mở project bằng IntelliJ (chọn đúng JDK 17)
2. **Cấu hình database:**
   - File: `src/main/resources/application.properties`
   - Sửa lại user/password nếu khác mặc định.
3. **Chạy ứng dụng:**
   - Chạy class `com.cuoiky.Nhom13.Nhom13Application`
   - Truy cập: `http://localhost:8080/`

---

## 3. TÀI KHOẢN MẪU (DÙNG NGAY)
- **dispatcher / 123456** (Quản lý, phân công)
- **tech01 / 123456** (Kỹ thuật viên)
- **tech02 / 123456** (Kỹ thuật viên)

---

## 4. LUỒNG DEMO TỪNG VAI TRÒ

### A. Thành — Đăng ký, đăng nhập, phân quyền
1. **Đăng ký:**
   - Vào `/register`, điền email, password, chọn vai trò.
   - Đăng ký xong sẽ chuyển sang trang đăng nhập.
2. **Đăng nhập:**
   - Vào `/login`, nhập email và password.
   - Đăng nhập thành công sẽ vào trang chính.
3. **Phân quyền:**
   - Sau khi đăng nhập, giao diện sẽ tự động hiển thị đúng chức năng theo vai trò.
   - Có thể đăng xuất ở góc trên phải.

### B. Khang — Tạo & phân công công việc, cập nhật trạng thái
1. **Tạo job:**
   - Đăng nhập bằng dispatcher/admin.
   - Vào `/jobs`, nhìn bên trái có form tạo job.
   - Điền thông tin, bấm **Save Job**.
2. **Phân công kỹ thuật viên:**
   - Chọn job vừa tạo ở danh sách giữa.
   - Bên phải có mục **Assign Technician**.
   - Chọn kỹ thuật viên, bấm **Assign Job**.
3. **Cập nhật trạng thái (Work Order):**
   - Đăng nhập bằng tech01/tech02.
   - Vào `/jobs`, chỉ thấy job được giao.
   - Chọn job, bấm **Start Work** để bắt đầu, **Complete Job** khi xong.
   - Trạng thái sẽ tự động cập nhật, timeline ghi lại lịch sử.

### C. Nhiên — Checklist, quản lý vật tư, sử dụng vật tư
1. **Checklist:**
   - Trong chi tiết job, có phần checklist.
   - Tick/check các mục, bấm lưu.
2. **Quản lý vật tư:**
   - Đăng nhập bằng admin/inventory_manager.
   - Vào phần inventory, thêm/sửa/xóa vật tư, điều chỉnh số lượng.
   - Nếu tồn kho thấp sẽ có cảnh báo.
3. **Ghi nhận sử dụng vật tư:**
   - Khi hoàn thành job, chọn vật tư đã dùng.
   - Số lượng tồn kho tự động trừ.

### D. Bình — Upload ảnh, ký nhận, xuất PDF
1. **Upload ảnh:**
   - Trong chi tiết job, có nút upload ảnh.
   - Ảnh sẽ hiển thị ngay sau khi upload.
2. **Ký nhận:**
   - Có vùng ký nhận (canvas), ký xong bấm lưu.
3. **Xuất PDF:**
   - Khi job hoàn thành, bấm **Export PDF** để tải báo cáo.

### E. Tín — UI tổng thể, thông báo, quét QR/barcode, tìm kiếm
1. **UI tổng thể:**
   - Giao diện chia 3 cột: trái (form/filter), giữa (danh sách job), phải (chi tiết job).
2. **Thông báo:**
   - Khi có job mới hoặc hoàn thành, sẽ có thông báo trên UI/email.
3. **Quét QR/barcode:**
   - Dùng webcam quét mã vật tư, thông tin sẽ hiện ngay.
4. **Tìm kiếm & lọc:**
   - Dùng bộ lọc bên trái để tìm nhanh job theo trạng thái, người thực hiện, ngày, ưu tiên, từ khóa.

---

## 5. MẸO DEMO NHANH
- Đăng nhập bằng tài khoản mẫu để tiết kiệm thời gian.
- Tạo job mới, phân công, chuyển trạng thái, thử checklist, upload ảnh, ký nhận, xuất PDF, quét QR.
- Nếu không thấy dữ liệu mới, kiểm tra lại đăng nhập hoặc seed database.
- Đảm bảo MySQL đang chạy.

---

## 6. TỔNG KẾT
- Demo nên đi theo thứ tự: Đăng ký/đăng nhập → Tạo job → Phân công → Thực hiện checklist/vật tư → Upload ảnh/ký nhận → Xuất PDF → Tìm kiếm/lọc/QR.
- Mỗi thành viên trình bày đúng phần mình làm, nhấn mạnh sự liên kết giữa các module.

---



