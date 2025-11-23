# Firmware Storage Directory

Thư mục này lưu trữ các file firmware (.bin) cho OTA updates.

## Cấu trúc thư mục

```
firmware-storage/
├── 1.0.0/
│   └── firmware.bin
├── 1.1.0/
│   └── firmware.bin
└── 2.0.0/
    └── firmware.bin
```

## Lưu ý

- Mỗi version có một thư mục riêng
- File firmware phải có tên `firmware.bin`
- Dung lượng tối đa: 50MB/file
- Chỉ chấp nhận file .bin

## Sử dụng

File này được quản lý tự động bởi `FirmwareStorageService`.
