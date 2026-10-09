# PRD – FintechBillingApp

**Product Requirements Document**
Mata Kuliah: Pemrograman Mobile Dasar – Pertemuan 11
Topik: Parsing JSON & Pemodelan Data Berstruktur (Kotlin Data Class DTO, Gson, Nested Object, JSON Array)
Platform: Android (Kotlin, Views/XML) | Package: `com.industri.fintechbilling`
Versi: 1.0 | Status: Siap implementasi

---

## 1. Ringkasan Produk

FintechBillingApp adalah aplikasi Android edukatif yang mensimulasikan tampilan **faktur digital (digital invoice)** pada layanan FinTech / payment gateway. Aplikasi menerima payload JSON transaksi bertingkat, mengurainya (deserialize) ke Kotlin Data Class DTO menggunakan Gson, lalu menampilkannya sebagai rincian faktur yang mudah dibaca.

Aplikasi ini sekaligus menjadi wadah praktik untuk:
- struktur JSON hierarkis (primitif, objek `{}`, array `[]`),
- pemetaan `snake_case` → `camelCase` dengan `@SerializedName`,
- RecyclerView untuk koleksi data,
- defensive programming terhadap nilai `null`.

## 2. Tujuan

| # | Tujuan | Indikator Keberhasilan |
|---|--------|------------------------|
| G1 | Mahasiswa memahami struktur JSON bertingkat | Payload invoice terurai tanpa error |
| G2 | Membuat DTO dengan Kotlin Data Class | 5 data class: `Merchant`, `InvoiceResponse`, `Customer`, `InvoiceItem`, `BillingSummary` |
| G3 | Menerapkan anotasi `@SerializedName` | Semua field snake_case terisi benar (tidak null) |
| G4 | Menampilkan JSON Array ke RecyclerView | 3 item barang tampil sebagai list |
| G5 | Menerapkan null-safety | Aplikasi tidak crash saat voucher tidak ada |

## 3. Ruang Lingkup

**Dalam lingkup (In Scope)**
- Parsing JSON sederhana (Merchant) dari string lokal.
- Parsing JSON invoice 3 lapis dari mock payload lokal.
- Tampilan faktur: header, merchant, customer, daftar barang, rangkuman biaya.
- Tugas Mandiri 1 (voucher nullable) dan Tugas Mandiri 2 (format tanggal).

**Di luar lingkup (Out of Scope)**
- Pemanggilan API sungguhan via Retrofit (JSON disimulasikan secara lokal; dependensi converter sudah disiapkan untuk pengembangan lanjutan).
- Autentikasi, pembayaran riil, penyimpanan database, dan cetak/ekspor faktur.
- Migrasi ke Moshi (hanya dibahas sebagai teori pembanding).

## 4. Persona & User Story

**Persona:** Mahasiswa Sistem Informasi semester awal yang belajar pengembangan Android.

| ID | User Story |
|----|-----------|
| US-01 | Sebagai mahasiswa, saya ingin menekan tombol untuk mem-parse JSON Merchant sehingga saya melihat cara kerja Gson. |
| US-02 | Sebagai pengguna, saya ingin membuka faktur transaksi lengkap agar dapat melihat rincian pembayaran. |
| US-03 | Sebagai pengguna, saya ingin melihat daftar barang beserta qty, harga satuan, dan subtotal. |
| US-04 | Sebagai pengguna, saya ingin melihat subtotal, diskon, PPN 11%, biaya layanan, dan total dibayar. |
| US-05 | Sebagai pengguna, jika tidak ada voucher saya ingin melihat keterangan "Tidak menggunakan promo". |
| US-06 | Sebagai pengguna, saya ingin tanggal transaksi tampil dalam format Indonesia yang ramah dibaca. |

## 5. Kebutuhan Fungsional

### FR-1 Parsing Dasar (Tahap 1)
- FR-1.1 `MainActivity` menyediakan tombol **Parse JSON Merchant** (`btnParseBasic`).
- FR-1.2 Saat ditekan, string JSON Merchant di-deserialize dengan `Gson().fromJson(...)`.
- FR-1.3 Hasil ditampilkan di `tvOutputBasic`: ID Mitra, Nama Toko, Kota, status verifikasi ("RESMI (Ya)" / "Belum").

### FR-2 Pemodelan DTO (Tahap 2)
- FR-2.1 Seluruh DTO memakai `data class` dengan `@SerializedName`.
- FR-2.2 Nilai uang bertipe `Double` (bukan `Int`) untuk mencegah overflow dan kehilangan presisi desimal.
- FR-2.3 `items` dimodelkan sebagai `List<InvoiceItem>`.
- FR-2.4 `isVerified` dan `discountAmount` memiliki nilai default.

### FR-3 Tampilan Faktur
- FR-3.1 `InvoiceActivity` mem-parse payload mock dan mengikat data ke UI.
- FR-3.2 Header menampilkan nomor invoice, badge status pembayaran, dan tanggal transaksi.
- FR-3.3 Menampilkan nama merchant dan penerima (`nama (telepon)`).
- FR-3.4 Daftar barang ditampilkan lewat `RecyclerView` + `InvoiceItemAdapter`, format baris: `{qty} x @ {harga satuan}` dan subtotal.
- FR-3.5 Rangkuman biaya: subtotal, diskon, PPN 11%, biaya layanan, total dibayar.
- FR-3.6 Semua nominal diformat Rupiah (`Locale("id","ID")`, tanpa desimal).

### FR-4 Tugas Mandiri 1 – Defensive Nullable Handling
- FR-4.1 `BillingSummary` memiliki `voucherCode: String?` (`voucher_code`) dan `voucherDiscountPercent: Int?` (`voucher_discount_percent`), default `null`.
- FR-4.2 Jika `voucherCode` null/kosong → baris diskon **disembunyikan** (`GONE`) dan teks **"Tidak menggunakan promo"** ditampilkan.
- FR-4.3 Jika voucher ada → baris diskon tampil dengan label `Diskon Voucher {kode} ({persen}%)`.
- FR-4.4 Pembacaan `voucherDiscountPercent` memakai fallback (`?: 0`) agar tidak crash.

### FR-5 Tugas Mandiri 2 – Custom Date Formatting
- FR-5.1 Fungsi ekstensi `String.toFriendlyDate()` mengubah `2026-09-23 10:15:00` menjadi **`Rabu, 23 September 2026 - Pukul 10:15 WIB`**.
- FR-5.2 Zona waktu `Asia/Jakarta`, locale `id-ID`.
- FR-5.3 Jika format input tidak valid, fungsi mengembalikan teks asli (tidak crash).

### FR-6 Navigasi Demo
- FR-6.1 `MainActivity` menyediakan dua tombol: **Buka Faktur (Tanpa Voucher)** dan **Buka Faktur (Dengan Voucher)**.
- FR-6.2 Pilihan dikirim ke `InvoiceActivity` melalui Intent extra `EXTRA_WITH_VOUCHER`.

## 6. Spesifikasi Data

### 6.1 Struktur JSON (3 lapis)

```
InvoiceResponse (root)
├── invoice_number, transaction_date, payment_status, payment_method
├── merchant  { merchant_id, merchant_name, city_location, is_verified }
├── customer  { customer_id, full_name, phone }
├── items[]   { item_id, item_name, qty, unit_price, subtotal }
└── summary   { subtotal_amount, discount_amount, tax_ppn_11, service_fee,
                total_paid, voucher_code?, voucher_discount_percent? }
```

### 6.2 Pemetaan DTO

| Kelas | File | Catatan |
|-------|------|---------|
| `Merchant` | `Merchant.kt` | Objek bersarang |
| `InvoiceResponse` | `InvoiceResponse.kt` | Root DTO |
| `Customer` | `InvoiceResponse.kt` | Objek bersarang |
| `InvoiceItem` | `InvoiceResponse.kt` | Elemen JSON Array |
| `BillingSummary` | `InvoiceResponse.kt` | Memuat field voucher nullable |

### 6.3 Data Uji

| Skenario | Subtotal | Diskon | PPN 11% | Biaya Layanan | Total |
|----------|---------:|-------:|--------:|--------------:|------:|
| Tanpa voucher (sesuai modul) | 600.000 | 50.000 | 60.500 | 2.500 | 613.000 |
| Dengan voucher `HEMAT10` (10%) | 600.000 | 60.000 | 59.400 | 2.500 | 601.900 |

Verifikasi: PPN = 11% × (subtotal − diskon); total = subtotal − diskon + PPN + biaya layanan.

## 7. Spesifikasi UI

| Layar | Komponen Utama |
|-------|----------------|
| **MainActivity** | Judul, tombol Parse Merchant, `tvOutputBasic`, dua tombol buka faktur |
| **InvoiceActivity** | `tvInvoiceNo`, `tvStatusBadge`, `tvTransactionDate`, `tvMerchantName`, `tvCustomerName`, `rvInvoiceItems`, baris ringkasan (`tvSubtotalAmt`, `rowDiscount`/`tvDiscountAmt`, `tvPromoInfo`, `tvTaxAmt`, `tvFeeAmt`, `tvTotalPaid`) |
| **item_invoice_product** | `tvItemName`, `tvItemSubtotal`, `tvItemQtyPrice`, garis pemisah |

Palet warna mengikuti modul: teks utama `#0F172A`, teks sekunder `#64748B`, pemisah `#F1F5F9`.

## 8. Kebutuhan Non-Fungsional

| ID | Kebutuhan |
|----|-----------|
| NFR-1 | Min SDK 24 (Android 7.0); berjalan di emulator maupun perangkat fisik. |
| NFR-2 | Tidak crash pada kondisi voucher `null` atau tanggal tidak valid. |
| NFR-3 | Penamaan Kotlin camelCase; konvensi API snake_case dipetakan lewat anotasi. |
| NFR-4 | Build release aman dari obfuscation: tambahkan `-keepclassmembers class com.industri.fintechbilling.** { *; }` pada `proguard-rules.pro`. |
| NFR-5 | Tampilan scrollable agar nyaman di layar kecil. |

## 9. Arsitektur & Teknologi

- **Bahasa:** Kotlin, JDK 17, Android Studio (Iguana atau lebih baru)
- **UI:** XML Views, `RecyclerView` 1.3.2, Material Components
- **Parser:** Gson 2.11.0 (+ `converter-gson` 2.11.0 untuk integrasi Retrofit)
- **Pola:** Activity sederhana + DTO + Adapter (belum memakai ViewModel/Repository karena fokus praktikum pada parsing)

### Struktur Berkas

```
app/src/main/
├── java/com/industri/fintechbilling/
│   ├── MainActivity.kt
│   ├── InvoiceActivity.kt
│   ├── InvoiceItemAdapter.kt
│   ├── Merchant.kt
│   ├── InvoiceResponse.kt
│   └── Extensions.kt
└── res/layout/
    ├── activity_main.xml
    ├── activity_invoice.xml
    └── item_invoice_product.xml
```

## 10. Kriteria Penerimaan (Acceptance Criteria)

| ID | Kriteria | Hasil yang Diharapkan |
|----|----------|-----------------------|
| AC-01 | Tekan "Parse JSON Merchant" | Muncul ID `MCH-99201`, toko "Mega Elektrindo Ritel", kota Bandung, "RESMI (Ya)" |
| AC-02 | Buka faktur tanpa voucher | Nomor `INV-2026-FT9012`, status `PAID_SETTLED`, 3 item tampil di list |
| AC-03 | Rangkuman tanpa voucher | Total Rp613.000; baris diskon tersembunyi; tampil "Tidak menggunakan promo" |
| AC-04 | Buka faktur dengan voucher | Baris "Diskon Voucher HEMAT10 (10%)" tampil, nilai −Rp60.000, total Rp601.900 |
| AC-05 | Tanggal transaksi | Tampil "Rabu, 23 September 2026 - Pukul 10:15 WIB" |
| AC-06 | Input tanggal salah | Teks asli ditampilkan, aplikasi tidak crash |
| AC-07 | Semua field DTO | Tidak ada field `null` akibat salah pemetaan nama |

## 11. Risiko & Mitigasi

| Risiko | Dampak | Mitigasi |
|--------|--------|----------|
| Field null karena nama kunci tidak cocok | Data kosong / crash | Gunakan `@SerializedName` pada setiap field |
| Gson dapat memasukkan null ke properti non-null Kotlin | `NullPointerException` saat runtime | Jadikan field opsional bertipe nullable; pertimbangkan Moshi untuk produksi |
| Obfuscation ProGuard pada build release | Seluruh field menjadi null | Tambahkan aturan keep (NFR-4) |
| Tipe `Int` untuk nilai uang | Overflow / kehilangan desimal | Gunakan `Double` atau `Long` |
| Perbedaan format locale antar versi Android (mis. spasi setelah "Rp") | Tampilan sedikit berbeda | Dapat diterima; tidak memengaruhi fungsi |

## 12. Rencana Pengembangan Lanjutan (Opsional)

1. Mengganti mock JSON dengan pemanggilan **Retrofit** + `GsonConverterFactory`.
2. Migrasi parser ke **Moshi** (KSP) untuk null-safety yang lebih ketat.
3. Menambahkan ViewModel + state loading/error.
4. Menyimpan riwayat faktur ke Room database.

## 13. Pemetaan terhadap Modul

| Bagian Modul | Cakupan di PRD |
|--------------|----------------|
| 3.1 – 3.3 (Tahap 1) | FR-1 |
| 4.1 – 4.2 (JSON & DTO) | FR-2, bagian 6 |
| 4.3 – 4.4 (Layout & Adapter) | FR-3.4, bagian 7 |
| 4.5 (InvoiceActivity) | FR-3 |
| 5 (Troubleshooting) | NFR-4, bagian 11 |
| 6 – Tugas 1 | FR-4 |
| 6 – Tugas 2 | FR-5 |
