package com.bookstore.common;

import com.bookstore.auth.Role;
import com.bookstore.auth.User;
import com.bookstore.auth.UserRepository;
import com.bookstore.chatbot.FaqEntry;
import com.bookstore.chatbot.FaqRepository;
import com.bookstore.product.Category;
import com.bookstore.product.CategoryRepository;
import com.bookstore.product.Product;
import com.bookstore.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final FaqRepository faqRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initUsers();
        initCategoriesAndProducts();
        initFaqs();
    }

    private void initUsers() {
        if (userRepository.count() == 0) {
            log.info("Khởi tạo dữ liệu người dùng mặc định...");

            // Admin account
            User admin = new User();
            admin.setEmail("admin@bookstore.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("Quản trị viên Hệ thống");
            admin.setPhone("0901234567");
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);

            // Customer account
            User customer = new User();
            customer.setEmail("customer@bookstore.com");
            customer.setPassword(passwordEncoder.encode("customer123"));
            customer.setFullName("Nguyễn Văn Đọc Sách");
            customer.setPhone("0987654321");
            customer.setRole(Role.CUSTOMER);
            customer.setActive(true);
            userRepository.save(customer);

            log.info("Tài khoản mặc định: Admin (admin@bookstore.com / admin123), Customer (customer@bookstore.com / customer123)");
        }
    }

    private void initCategoriesAndProducts() {
        if (categoryRepository.count() == 0) {
            log.info("Khởi tạo danh mục và sách mẫu...");

            Category it = createCategory("Công nghệ thông tin", "Sách về lập trình, trí tuệ nhân tạo, thiết kế hệ thống và khoa học máy tính");
            Category business = createCategory("Kinh tế & Quản trị", "Sách kinh doanh, khởi nghiệp, tài chính đầu tư và quản lý");
            Category literature = createCategory("Văn học & Nghệ thuật", "Tiểu thuyết, truyện ngắn, tác phẩm kinh điển trong nước và quốc tế");
            Category selfHelp = createCategory("Kỹ năng & Phát triển bản thân", "Tư duy, năng suất, thói quen và rèn luyện tâm lý");
            Category science = createCategory("Khoa học & Đời sống", "Khám phá vũ trụ, tự nhiên, sinh học và vật lý ứng dụng");

            // IT books
            createProduct(
                    "Clean Code: A Handbook of Agile Software Craftsmanship",
                    "Cuốn sách kinh điển của Robert C. Martin (Uncle Bob) hướng dẫn nguyên tắc viết code sạch, dễ đọc và dễ bảo trì.",
                    new BigDecimal("350000"),
                    50,
                    "https://images.unsplash.com/photo-1532012164546-f432f2e3777a?auto=format&fit=crop&w=600&q=80",
                    it
            );
            createProduct(
                    "Designing Data-Intensive Applications",
                    "Cẩm nang toàn diện của Martin Kleppmann về kiến trúc dữ liệu phân tán, độ tin cậy, khả năng mở rộng và tính nhất quán.",
                    new BigDecimal("480000"),
                    35,
                    "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=600&q=80",
                    it
            );
            createProduct(
                    "Spring Boot in Action",
                    "Hướng dẫn thực tế xây dựng ứng dụng microservices mạnh mẽ với Spring Boot, Spring Security và RESTful APIs.",
                    new BigDecimal("290000"),
                    40,
                    "https://images.unsplash.com/photo-1516979187457-637abb4f9353?auto=format&fit=crop&w=600&q=80",
                    it
            );

            // Business books
            createProduct(
                    "Từ Tốt Đến Vĩ Đại (Good to Great)",
                    "Nghiên cứu của Jim Collins về cách các công ty bình thường vươn lên thành những tập đoàn vĩ đại trường tồn.",
                    new BigDecimal("185000"),
                    60,
                    "https://images.unsplash.com/photo-1589829085413-56de8ae18c73?auto=format&fit=crop&w=600&q=80",
                    business
            );
            createProduct(
                    "Nhà Đầu Tư Thông Minh (The Intelligent Investor)",
                    "Tác phẩm vĩ đại của Benjamin Graham về triết lý đầu tư giá trị, nguyên tắc phòng ngừa rủi ro trên thị trường tài chính.",
                    new BigDecimal("220000"),
                    25,
                    "https://images.unsplash.com/photo-1592496431122-2349e0fbc666?auto=format&fit=crop&w=600&q=80",
                    business
            );

            // Literature books
            createProduct(
                    "Nhà Giả Kim (The Alchemist)",
                    "Tiểu thuyết nổi tiếng thế giới của Paulo Coelho kể về hành trình theo đuổi ước mơ và lắng nghe trái tim của chàng chăn cừu Santiago.",
                    new BigDecimal("89000"),
                    100,
                    "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?auto=format&fit=crop&w=600&q=80",
                    literature
            );
            createProduct(
                    "Hoàng Tử Bé (Le Petit Prince)",
                    "Tác phẩm văn học bất hủ của Antoine de Saint-Exupéry mang thông điệp nhân văn sâu sắc về tình bạn và cuộc sống.",
                    new BigDecimal("75000"),
                    80,
                    "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=600&q=80",
                    literature
            );

            // Self-help books
            createProduct(
                    "Atomic Habits - Thay Đổi Tí Hon Hiệu Quả Bất Ngờ",
                    "James Clear chia sẻ phương pháp xây dựng thói quen tốt và loại bỏ thói quen xấu dựa trên khoa học hành vi.",
                    new BigDecimal("195000"),
                    75,
                    "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?auto=format&fit=crop&w=600&q=80",
                    selfHelp
            );
            createProduct(
                    "Đắc Nhân Tâm (How to Win Friends and Influence People)",
                    "Cuốn sách gối đầu giường về nghệ thuật giao tiếp, thu phục lòng người và xây dựng mối quan hệ bền vững.",
                    new BigDecimal("98000"),
                    120,
                    "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=600&q=80",
                    selfHelp
            );

            // Science books
            createProduct(
                    "Vũ Trụ Trong Vỏ Hạt Dẻ (The Universe in a Nutshell)",
                    "Stephen Hawking đưa độc giả vào thế giới vật lý lý thuyết, lỗ đen, thời gian đa chiều và nguồn gốc vũ trụ.",
                    new BigDecimal("215000"),
                    30,
                    "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=600&q=80",
                    science
            );

            log.info("Đã khởi tạo xong danh mục và 10 cuốn sách mẫu.");
        }
    }

    private void initFaqs() {
        if (faqRepository.count() == 0) {
            log.info("Khởi tạo dữ liệu câu hỏi thường gặp (FAQ) cho Chatbot...");

            createFaq(
                    "Chính sách giao hàng của nhà sách như thế nào?",
                    "Nhà sách giao hàng toàn quốc từ 2-4 ngày làm việc. Miễn phí vận chuyển cho đơn hàng từ 300.000đ trở lên. Đơn dưới 300.000đ phí ship tiêu chuẩn là 30.000đ.",
                    "giao hàng, ship, vận chuyển, phí ship, thời gian giao"
            );

            createFaq(
                    "Nhà sách hỗ trợ những hình thức thanh toán nào?",
                    "Chúng tôi hỗ trợ thanh toán khi nhận hàng (COD), chuyển khoản ngân hàng và các ví điện tử phổ biến như MoMo, VNPay, ZaloPay.",
                    "thanh toán, trả tiền, cod, chuyển khoản, momo, vnpay"
            );

            createFaq(
                    "Chính sách đổi trả sách được quy định ra sao?",
                    "Quý khách được đổi trả sách trong vòng 7 ngày kể từ khi nhận hàng nếu sách bị lỗi in ấn, rách trang, sai nội dung hoặc hư hỏng do vận chuyển. Sách đổi trả cần giữ nguyên tem mác và tình trạng ban đầu.",
                    "đổi trả, bảo hành, hoàn tiền, sách lỗi, trả hàng"
            );

            createFaq(
                    "Thời gian làm việc và kênh hỗ trợ trực tiếp?",
                    "Đội ngũ chăm sóc khách hàng làm việc từ 8:00 - 21:30 các ngày trong tuần (kể cả Thứ Bảy và Chủ Nhật). Hotline: 1900 6868 hoặc email support@bookstore.com.",
                    "thời gian làm việc, giờ mở cửa, hotline, số điện thoại, liên hệ"
            );

            log.info("Đã khởi tạo xong dữ liệu FAQ.");
        }
    }

    private Category createCategory(String name, String description) {
        Category c = new Category();
        c.setName(name);
        c.setDescription(description);
        return categoryRepository.save(c);
    }

    private Product createProduct(String name, String description, BigDecimal price, int stock, String imageUrl, Category category) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setPrice(price);
        p.setStockQuantity(stock);
        p.setImageUrl(imageUrl);
        p.setCategory(category);
        p.setActive(true);
        return productRepository.save(p);
    }

    private void createFaq(String question, String answer, String keywords) {
        FaqEntry f = new FaqEntry();
        f.setQuestion(question);
        f.setAnswer(answer);
        f.setKeywords(keywords);
        f.setActive(true);
        faqRepository.save(f);
    }
}
