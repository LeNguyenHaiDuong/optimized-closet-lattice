# ClosedSet_Lattice: Cải tiến thuật toán khai thác CHUI+

Năm 2023, Ye In Chang và cộng sự đã công bố thuật toán **ClosedSet_Lattice** để khai thác mẫu **CHUI+** trong cơ sở dữ liệu tăng trưởng.

![ClosedSet_Lattice](img/ClosedSet_Lattice.png)

Việc xây dựng cây dựa trên việc xác định mối quan hệ giữa các nút bằng các phép toán (operations) và được chia thành **5 trường hợp chính**:

![Cases](img/Cases.png)

## Cải tiến thuật toán

Repo này thực hiện **tối ưu cấu trúc dữ liệu** và **chiến lược thuật toán** nhằm giảm thời gian chạy (runtime) và mức sử dụng bộ nhớ (memory usage).

| Trước cải tiến | Sau cải tiến |
|---------------|-------------|
| ![Original](img/Original.png) | ![Improved](img/Improved.png) |

## Kết quả thực nghiệm

So sánh hiệu suất của thuật toán cải tiến với bài báo gốc trên hai bộ dữ liệu **foodmart** và **ecommerce**:

![Thống kê kết quả](img/Stats.png)

## Lưu ý

🚀 Repo **không** công khai bản cải tiến của thư viện **BitSet** và những phiên bản thư viện thay thế khác.
