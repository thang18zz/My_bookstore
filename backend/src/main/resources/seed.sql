USE bookstore;
SET NAMES utf8mb4;

DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM cart_items;
DELETE FROM carts;
DELETE FROM wishlist_items;
DELETE FROM reviews;
DELETE FROM products;
DELETE FROM categories;

INSERT INTO categories (id, name, description, created_at, updated_at) VALUES 
(1, _utf8mb4'Văn học', _utf8mb4'Tiểu thuyết, truyện ngắn, tác phẩm văn học kinh điển', NOW(), NOW()),
(2, _utf8mb4'Kinh tế & Kỹ năng', _utf8mb4'Sách phát triển bản thân, kinh doanh, tài chính', NOW(), NOW()),
(3, _utf8mb4'Khoa học công nghệ', _utf8mb4'Lập trình, trí tuệ nhân tạo, khoa học đời sống', NOW(), NOW()),
(4, _utf8mb4'Tâm lý học', _utf8mb4'Khám phá tâm trí và hành vi con người', NOW(), NOW());

INSERT INTO products (id, name, description, price, stock_quantity, active, image_url, category_id, created_at, updated_at) VALUES 
(1, _utf8mb4'Nhà Giả Kim', _utf8mb4'Cuốn sách nổi tiếng của Paulo Coelho về hành trình theo đuổi ước mơ.', 79000, 50, 1, 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=600&q=80', 1, NOW(), NOW()),
(2, _utf8mb4'Đắc Nhân Tâm', _utf8mb4'Nghệ thuật thu phục lòng người của Dale Carnegie.', 86000, 45, 1, 'https://images.unsplash.com/photo-1543002588-bfa74002ed7e?auto=format&fit=crop&w=600&q=80', 2, NOW(), NOW()),
(3, _utf8mb4'Tư Duy Nhanh Và Chậm', _utf8mb4'Tác phẩm kinh điển của Daniel Kahneman về hệ thống tư duy của con người.', 189000, 30, 1, 'https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=600&q=80', 4, NOW(), NOW()),
(4, _utf8mb4'Clean Code: A Handbook of Agile Software Craftsmanship', _utf8mb4'Cẩm nang viết mã sạch cho lập trình viên chuyên nghiệp của Robert C. Martin.', 350000, 20, 1, 'https://images.unsplash.com/photo-1532012164546-f432f2e3777a?auto=format&fit=crop&w=600&q=80', 3, NOW(), NOW()),
(5, _utf8mb4'Dế Mèn Phiêu Lưu Ký', _utf8mb4'Tác phẩm thiếu nhi kinh điển của nhà văn Tô Hoài.', 45000, 60, 1, 'https://images.unsplash.com/photo-1497633762265-9d179a990aa6?auto=format&fit=crop&w=600&q=80', 1, NOW(), NOW()),
(6, _utf8mb4'Sapiens: Lược Sử Loài Người', _utf8mb4'Hành trình tiến hóa và phát triển của nhân loại qua góc nhìn của Yuval Noah Harari.', 195000, 25, 1, 'https://images.unsplash.com/photo-1476275466078-4007374efbbe?auto=format&fit=crop&w=600&q=80', 3, NOW(), NOW());
