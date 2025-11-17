import React from 'react';
import './ManageCategories.css';

const ManageCategories = () => {
    // Hardcode categories
    const categories = [
        { id: 1, name: 'Công nghệ', description: 'Các sự kiện về công nghệ', eventCount: 15 },
        { id: 2, name: 'Marketing', description: 'Các sự kiện về marketing', eventCount: 8 },
        { id: 3, name: 'Khởi nghiệp', description: 'Các sự kiện về khởi nghiệp', eventCount: 12 },
        { id: 4, name: 'Giáo dục', description: 'Các sự kiện về giáo dục', eventCount: 20 },
    ];

    return (
        <div className="manage-categories">
            <div className="page-header">
                <div>
                    <h2>Quản lý Danh mục</h2>
                    <p>Quản lý các danh mục sự kiện</p>
                </div>
                <button className="btn-primary">
                    <i className="bi bi-plus-circle"></i>
                    Thêm danh mục
                </button>
            </div>

            <div className="categories-grid">
                {categories.map((category) => (
                    <div key={category.id} className="category-card">
                        <div className="category-header">
                            <h3>{category.name}</h3>
                            <div className="category-actions">
                                <button className="btn-icon" title="Sửa">
                                    <i className="bi bi-pencil"></i>
                                </button>
                                <button className="btn-icon btn-danger" title="Xóa">
                                    <i className="bi bi-trash"></i>
                                </button>
                            </div>
                        </div>
                        <p className="category-description">{category.description}</p>
                        <div className="category-footer">
                            <span className="event-count">
                                <i className="bi bi-calendar-event"></i>
                                {category.eventCount} events
                            </span>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default ManageCategories;







