# Field Service Management - Job Module

Module nay bo sung cho project Spring Boot hien tai cac chuc nang:

- Tao va cap nhat cong viec
- Phan cong ky thuat vien
- Cap nhat trang thai work order voi rule nghiep vu
- Tim kiem, loc, phan trang va sap xep cong viec
- Timeline lich su assign va doi trang thai

## Tai khoan demo

Khi bang `jobs` chua co du lieu, he thong se seed mau:

- `dispatcher / 123456`
- `tech01 / 123456`
- `tech02 / 123456`

## Chay tren IntelliJ

1. Cai dat JDK 17 va cau hinh Project SDK = `17`
2. Dat MySQL dang chay, tao ket noi theo `src/main/resources/application.properties`
3. Neu can, sua:
   - `spring.datasource.url`
   - `spring.datasource.username`
   - `spring.datasource.password`
4. Mo class `com.cuoiky.Nhom13.Nhom13Application`
5. Run ung dung trong IntelliJ
6. Truy cap:
   - `http://localhost:8080/register`
   - `http://localhost:8080/login`
   - `http://localhost:8080/jobs`

## Luu y moi truong

- Project hien dang khai bao `java.version=17`
- Can `JAVA_HOME` tro den JDK 17 neu chay bang Maven Wrapper
- Neu terminal bao loi `JAVA_HOME`, hay cau hinh lai trong:
  - IntelliJ `File > Project Structure > SDKs`
  - Hoac bien moi truong Windows `JAVA_HOME`

## API chinh

- `GET /api/jobs`
- `POST /api/jobs`
- `PUT /api/jobs/{id}`
- `DELETE /api/jobs/{id}`
- `POST /api/jobs/{id}/assign`
- `PATCH /api/jobs/{id}/status`
- `GET /api/users/technicians`

## Search va sort

`GET /api/jobs?keyword=&status=&priority=&assignedUserId=&page=0&size=10&sortBy=createdAt&sortDir=DESC`

`sortBy` ho tro:

- `createdAt`
- `scheduledDate`
- `priority`
- `status`
- `title`
- `jobCode`
