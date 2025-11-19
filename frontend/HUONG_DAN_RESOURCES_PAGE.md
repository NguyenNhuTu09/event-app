# 📚 HƯỚNG DẪN CODE TRANG TÀI NGUYÊN (Resources Page)

## 🎯 Tổng quan

Trang Resources được chia thành 3 sections chính:
1. **Hero Section** - Banner giới thiệu
2. **Categories Section** - 4 danh mục tài nguyên
3. **Documents Section** - Bảng tài liệu tải xuống
4. **Media Gallery** - Thư viện hình ảnh/video với filter

---

## 📖 GIẢI THÍCH CODE CHI TIẾT

### 1. **Import và Setup**

```tsx
import { useLanguage } from '../context/LanguageContext';
import translations from '../translate/translations';
import { gsap } from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';
```

**Giải thích:**
- `useLanguage`: Hook để lấy ngôn ngữ hiện tại (vi/en)
- `translations`: Object chứa tất cả text đã dịch
- `gsap` & `ScrollTrigger`: Thư viện animation

---

### 2. **State Management**

```tsx
const [selectedCategory, setSelectedCategory] = useState('all');
```

**Giải thích:**
- `useState`: Hook React để quản lý state
- `selectedCategory`: Lưu category đang được chọn trong media gallery
- `'all'`: Giá trị mặc định (hiển thị tất cả)

**Cách hoạt động:**
- Khi user click vào filter button → `setSelectedCategory('event')`
- Component re-render → Filter media items theo category

---

### 3. **useRef cho GSAP Animations**

```tsx
const heroRef = useRef(null);
const categoriesRef = useRef(null);
const documentsRef = useRef(null);
const mediaRef = useRef(null);
```

**Giải thích:**
- `useRef`: Tạo reference đến DOM element
- Dùng để GSAP biết element nào cần animate
- `null`: Giá trị ban đầu (sẽ được gán khi render)

**Cách dùng:**
```tsx
<section ref={heroRef}>  {/* Gán ref vào element */}
```

---

### 4. **useEffect cho Animations**

```tsx
useEffect(() => {
    // Code animation
    return () => {
        ScrollTrigger.getAll().forEach((trigger) => trigger.kill());
    };
}, []);
```

**Giải thích:**
- `useEffect`: Chạy sau khi component mount
- `[]`: Dependency array rỗng = chỉ chạy 1 lần khi mount
- `return`: Cleanup function - xóa animations khi component unmount

**GSAP Animation Pattern:**
```tsx
gsap.fromTo(
    element,                    // Element cần animate
    { opacity: 0, y: 50 },     // Trạng thái ban đầu
    { opacity: 1, y: 0, ... }  // Trạng thái cuối
);
```

---

### 5. **Data Arrays**

#### A. Resource Categories
```tsx
const resourceCategories = [
    {
        id: 'guide',
        icon: 'bi-file-text',
        title: t.resourceCategoryGuide,
        description: t.resourceCategoryGuideDesc,
    },
    // ...
];
```

**Giải thích:**
- Mảng chứa 4 category cards
- `t.resourceCategoryGuide`: Lấy text từ translations theo ngôn ngữ
- `icon`: Bootstrap Icons class name

#### B. Documents
```tsx
const documents = [
    {
        name: t.documentVolunteerName,
        description: t.documentVolunteerDesc,
        type: 'PDF',
        downloadLink: '#',
        icon: 'bi-file-earmark-pdf',
    },
    // ...
];
```

**Giải thích:**
- Mảng tài liệu để render vào table
- `downloadLink`: Link tải file (thay `#` bằng link thật)
- `type`: 'PDF' hoặc 'Online'

#### C. Media Items
```tsx
const mediaItems = [
    { 
        id: 1, 
        type: 'image', 
        category: 'event', 
        url: '...', 
        title: '...' 
    },
    // ...
];
```

**Giải thích:**
- `category`: Dùng để filter ('event', 'venue', 'all')
- `type`: 'image' hoặc 'video'
- `url`: Link ảnh/video (thay placeholder bằng ảnh thật)

---

### 6. **Filter Logic**

```tsx
const filteredMedia = selectedCategory === 'all' 
    ? mediaItems 
    : mediaItems.filter(item => item.category === selectedCategory);
```

**Giải thích:**
- **Ternary operator** (`? :`): If-else ngắn gọn
- Nếu `selectedCategory === 'all'` → hiển thị tất cả
- Ngược lại → `filter()` chỉ lấy items có `category` khớp

**Cách hoạt động:**
1. User click "Events" → `setSelectedCategory('event')`
2. `filteredMedia` chỉ còn items có `category: 'event'`
3. Component re-render với danh sách đã filter

---

### 7. **JSX Structure**

#### A. Hero Section
```tsx
<section className="resources-hero" ref={heroRef}>
    <div className="container">
        <h1>{t.resourcesHeroTitle}</h1>
        <p>{t.resourcesHeroDescription}</p>
    </div>
</section>
```

**Giải thích:**
- `className`: CSS class name
- `ref={heroRef}`: Gán ref để GSAP animate
- `{t.resourcesHeroTitle}`: Interpolation - chèn giá trị vào JSX

#### B. Categories Grid
```tsx
<div className="categories-grid">
    {resourceCategories.map((category) => (
        <div key={category.id} className="category-card">
            <h3>{category.title}</h3>
        </div>
    ))}
</div>
```

**Giải thích:**
- `.map()`: Loop qua array, tạo element cho mỗi item
- `key={category.id}`: React cần key để optimize re-render
- `()`: Return JSX (không cần `return` nếu dùng `()`)

#### C. Documents Table
```tsx
<table className="documents-table">
    <thead>
        <tr>
            <th>{t.documentTableName}</th>
        </tr>
    </thead>
    <tbody>
        {documents.map((doc, index) => (
            <tr key={index}>
                <td>{doc.name}</td>
            </tr>
        ))}
    </tbody>
</table>
```

**Giải thích:**
- HTML table structure: `<table>` → `<thead>` → `<tbody>`
- `.map()` trong `<tbody>` để tạo rows động

#### D. Media Gallery với Filter
```tsx
<div className="media-filters">
    <button
        className={`filter-btn ${selectedCategory === 'all' ? 'active' : ''}`}
        onClick={() => setSelectedCategory('all')}
    >
        {t.mediaFilterAll}
    </button>
</div>

<div className="media-gallery">
    {filteredMedia.map((item) => (
        <div className="media-item">
            <img src={item.url} alt={item.title} />
        </div>
    ))}
</div>
```

**Giải thích:**
- **Template literal** (backticks): `${...}` để nối string
- `onClick={() => setSelectedCategory('all')}`: Arrow function handler
- `filteredMedia`: Dùng array đã filter thay vì `mediaItems`

---

## 🎨 CSS EXPLANATIONS

### 1. **Grid Layout**

```css
.categories-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 30px;
}
```

**Giải thích:**
- `display: grid`: CSS Grid layout
- `repeat(auto-fit, minmax(250px, 1fr))`: 
  - Tự động tạo columns
  - Mỗi column tối thiểu 250px
  - Tối đa chia đều không gian (`1fr`)
- `gap: 30px`: Khoảng cách giữa items

### 2. **Masonry Grid cho Gallery**

```css
.media-gallery {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 25px;
}
```

**Giải thích:**
- `auto-fill`: Tự động fill columns
- `minmax(280px, 1fr)`: Mỗi item tối thiểu 280px

### 3. **Hover Effects**

```css
.category-card:hover {
    transform: translateY(-10px);
    box-shadow: 0 15px 40px rgba(0, 123, 255, 0.2);
}
```

**Giải thích:**
- `:hover`: Pseudo-class khi mouse hover
- `transform: translateY(-10px)`: Di chuyển lên 10px
- `box-shadow`: Tạo bóng đổ

### 4. **Responsive Design**

```css
@media (max-width: 768px) {
    .categories-grid {
        grid-template-columns: 1fr;
    }
}
```

**Giải thích:**
- `@media`: Media query
- `max-width: 768px`: Áp dụng khi màn hình ≤ 768px
- `1fr`: 1 column trên mobile

---

## 🚀 CÁCH SỬ DỤNG & TÙY CHỈNH

### 1. **Thay đổi ảnh thật**

```tsx
// Thay placeholder URLs bằng import hoặc link thật
import eventImage1 from '../assets/images/event-2023.jpg';

const mediaItems = [
    { 
        id: 1, 
        type: 'image', 
        category: 'event', 
        url: eventImage1,  // Dùng import
        title: 'Sự kiện 2023' 
    },
];
```

### 2. **Thêm tài liệu mới**

```tsx
const documents = [
    // ... existing documents
    {
        name: 'Tài liệu mới',
        description: 'Mô tả tài liệu',
        type: 'PDF',
        downloadLink: '/documents/new-doc.pdf',
        icon: 'bi-file-earmark-pdf',
    },
];
```

### 3. **Thêm category filter mới**

```tsx
// 1. Thêm vào mediaItems
{ id: 9, type: 'image', category: 'workshop', url: '...', title: '...' }

// 2. Thêm filter button
<button onClick={() => setSelectedCategory('workshop')}>
    Workshop
</button>
```

### 4. **Thêm translations mới**

```jsx
// Trong translations.jsx
vi: {
    // ... existing
    newKey: 'Giá trị tiếng Việt',
},
en: {
    // ... existing
    newKey: 'English value',
}
```

---

## 💡 TIPS & BEST PRACTICES

1. **Luôn dùng key trong .map()**
   ```tsx
   {items.map((item) => (
       <div key={item.id}>  {/* ✅ Đúng */}
   ))}
   ```

2. **Dùng translations thay vì hardcode text**
   ```tsx
   <h1>{t.resourcesHeroTitle}</h1>  {/* ✅ Đúng */}
   <h1>Trung tâm tài nguyên</h1>     {/* ❌ Sai */}
   ```

3. **Cleanup GSAP animations**
   ```tsx
   return () => {
       ScrollTrigger.getAll().forEach(trigger => trigger.kill());
   };
   ```

4. **Responsive first**
   - Luôn test trên mobile
   - Dùng `minmax()` trong Grid
   - Thêm media queries

---

## 🎓 BÀI TẬP THỰC HÀNH

1. Thêm 2 tài liệu mới vào bảng
2. Thêm category "Workshop" vào media filter
3. Thay placeholder images bằng ảnh thật từ assets
4. Thêm animation fade-in cho table rows
5. Tạo modal để xem ảnh lớn khi click vào media item

---

Chúc bạn code vui vẻ! 🎉









