# 🚀 HƯỚNG DẪN DEPLOY LÊN VERCEL

## ❌ Lỗi 404 NOT_FOUND - DEPLOYMENT_NOT_FOUND

Lỗi này xảy ra khi Vercel không tìm thấy deployment hoặc cấu hình project không đúng.

## ✅ CÁCH KHẮC PHỤC

### Bước 1: Kiểm tra cấu hình trên Vercel Dashboard

1. Vào https://vercel.com/dashboard
2. Chọn project của bạn
3. Vào **Settings** → **General**

### Bước 2: Cấu hình Project Settings

Đảm bảo các cấu hình sau:

#### **Root Directory:**
```
frontend
```
→ Chọn thư mục `frontend` làm root directory

#### **Build Command:**
```
npm run build
```
→ Không cần `cd frontend` vì đã set root directory là `frontend`

#### **Output Directory:**
```
dist
```
→ Vite build output vào thư mục `dist`

#### **Install Command:**
```
npm install
```
→ Cài đặt dependencies

#### **Framework Preset:**
```
Vite
```
→ Hoặc để **Other** nếu không có Vite

### Bước 3: Kiểm tra file vercel.json

File `vercel.json` ở **root** của project (không phải trong frontend):

```json
{
    "rewrites": [
        {
            "source": "/(.*)",
            "destination": "/index.html"
        }
    ]
}
```

File `frontend/vercel.json` (nếu có):

```json
{
    "rewrites": [
        {
            "source": "/(.*)",
            "destination": "/index.html"
        }
    ]
}
```

### Bước 4: Xóa và Deploy lại

1. Vào **Deployments** tab
2. Xóa tất cả deployments cũ (nếu có lỗi)
3. **Redeploy** hoặc push code mới lên GitHub

### Bước 5: Kiểm tra Build Logs

1. Vào deployment mới nhất
2. Xem **Build Logs** để kiểm tra lỗi
3. Đảm bảo:
   - ✅ `npm install` thành công
   - ✅ `npm run build` thành công
   - ✅ Output directory `dist` được tạo

## 🔧 CẤU HÌNH THỦ CÔNG (Nếu tự động không hoạt động)

### Option 1: Deploy từ thư mục frontend

1. Trên Vercel Dashboard → **Settings** → **General**
2. **Root Directory:** `frontend`
3. **Build Command:** `npm run build`
4. **Output Directory:** `dist`

### Option 2: Deploy từ root với build command

1. **Root Directory:** `.` (root)
2. **Build Command:** `cd frontend && npm install && npm run build`
3. **Output Directory:** `frontend/dist`

## 📝 KIỂM TRA SAU KHI DEPLOY

1. ✅ Build thành công (không có lỗi)
2. ✅ Deployment có URL (ví dụ: `https://your-project.vercel.app`)
3. ✅ Trang web load được (không phải 404)
4. ✅ Routes hoạt động (SPA routing)

## 🐛 TROUBLESHOOTING

### Lỗi: "Build Command failed"
- Kiểm tra `package.json` có script `build` không
- Kiểm tra dependencies có đầy đủ không
- Xem build logs để biết lỗi cụ thể

### Lỗi: "Output Directory not found"
- Đảm bảo `vite.config.js` output vào `dist`
- Kiểm tra `package.json` build script
- Xem build logs xem có tạo folder `dist` không

### Lỗi: "404 NOT_FOUND"
- Kiểm tra `vercel.json` có rewrites đúng không
- Đảm bảo `index.html` có trong output directory
- Kiểm tra URL deployment có đúng không

## 📞 LIÊN HỆ

Nếu vẫn gặp lỗi, kiểm tra:
- Vercel Documentation: https://vercel.com/docs
- Vite Deployment Guide: https://vitejs.dev/guide/static-deploy.html#vercel

---

**Lưu ý:** Sau khi cấu hình xong, push code lên GitHub để Vercel tự động deploy lại.

