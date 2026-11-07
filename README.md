# 🎬 Seriee

**Pelacak progres tontonan** untuk serial, anime, dan film. Aplikasi Android native, dibuat dengan **Kotlin + Jetpack Compose + Material 3** (tanpa WebView).

![Platform](https://img.shields.io/badge/platform-Android%208.0%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?logo=kotlin&logoColor=white)
![Material 3](https://img.shields.io/badge/UI-Material%203-6750A4)
![Build](https://img.shields.io/badge/build-GitHub%20Actions-2088FF?logo=githubactions&logoColor=white)

---

## ✨ Fitur

### 📈 Tab Progress
- Kartu progres episode dengan persentase.
- Filter: **Semua / Watching / Completed**.
- Tombol **`+`** menambah 1 episode, dan status otomatis menjadi *Completed* saat episode terakhir tercapai.

### 📚 Tab Library
- Pencarian judul dan filter tipe (**Series / Movie**).
- Tampilan **grid** (3 kolom) atau **list**.
- **Pin** item favorit, item yang di-pin tampil paling atas.
- Badge **rating bintang** dan **tag/genre** pada kartu.

### 📝 Kelola Media
- Tambah, ubah, dan hapus media: judul, tipe, status, URL poster, season, episode saat ini, dan total episode.
- Status tersedia: `Watching`, `Completed`, `Plan to Watch`, `Coming Soon`.
- Rating, tag, dan tanggal rilis.

### 🔔 Pengingat Jadwal Tayang
- Atur tanggal & jam rilis (ada preset: Besok, 3 Hari, 1 Minggu, 1 Bulan).
- Hitung mundur tayang (misal `D-3`, "2 hari 5 jam lagi") di kartu dan detail.
- Notifikasi pengingat saat jadwal tiba (alarm tepat waktu, tetap berbunyi saat perangkat idle).
- Tombol uji notifikasi untuk mengecek pengingat.

### ▶️ Tonton
- **Link streaming** dengan preset Netflix, Bilibili, dan YouTube, dibuka di aplikasi atau browser.
- **Video offline**: pilih file dari penyimpanan, lalu buka di **VLC**, **MX Player**, atau pemutar lain.

### 💾 Cadangkan & Pulihkan
- Ekspor seluruh data ke file **JSON**.
- Impor dari file cadangan dengan dua mode:
  - **Gabungkan** — menambah data dari file tanpa menghapus data yang ada (disarankan).
  - **Timpa** — mengganti seluruh data dengan isi file.
- Pengingat aktif dijadwalkan ulang otomatis setelah pemulihan.

### 🎨 Tema
Empat tema: **Dark**, **AMOLED**, **Frost Glass**, dan **Light**. Pilihan tema dan mode tampilan (grid/list) tersimpan di perangkat.

---

## 🛠️ Teknologi

| Komponen | Versi / Library |
|---|---|
| Bahasa | Kotlin |
| UI | Jetpack Compose + Material 3 (Compose BOM `2024.09.00`) |
| Arsitektur | ViewModel (`lifecycle-viewmodel-compose`) |
| Gambar | [Coil](https://coil-kt.github.io/coil/) `2.7.0` |
| Penyimpanan | `SharedPreferences` (data disimpan sebagai JSON, lokal di perangkat) |
| Min SDK / Target SDK | 26 (Android 8.0) / 34 |
| Java / Gradle | 17 / 8.9 |

---

## 📲 Mendapatkan APK (tanpa Android Studio)

1. Push repo ini ke GitHub (branch `main`).
2. Buka tab **Actions → Build APK** (berjalan otomatis saat push, atau klik **Run workflow**).
3. Setelah selesai, unduh artifact **`seriee-debug-apk`** dari halaman run tersebut.
4. Ekstrak dan instal `app-debug.apk` di HP.

> Aktifkan *Install from unknown sources* jika diminta oleh Android.

---

## 🔨 Build Manual

### Android Studio
Buka folder proyek di **Android Studio (Koala atau lebih baru)**, tunggu Gradle sync selesai, lalu tekan **Run**.

Opsional, buat Gradle wrapper sekali saja:
```bash
gradle wrapper --gradle-version 8.9
```

### Terminal
```bash
gradle assembleDebug        # atau ./gradlew assembleDebug jika wrapper sudah dibuat
```
Hasil APK: `app/build/outputs/apk/debug/app-debug.apk`

---

## 📁 Struktur Proyek

```
Seriee/
├── .github/workflows/build.yml      # CI: build APK debug
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/
│       └── java/com/seriee/app/
│           ├── MainActivity.kt
│           ├── SerieeViewModel.kt
│           ├── data/
│           │   ├── Models.kt              # MediaItem, MediaType, WatchStatus, AppTheme
│           │   └── SerieeRepository.kt    # penyimpanan lokal + backup/restore JSON
│           ├── reminder/
│           │   ├── ReminderScheduler.kt   # alarm, notifikasi, hitung mundur
│           │   └── ReminderReceiver.kt
│           └── ui/
│               ├── SerieeApp.kt
│               ├── ProgressScreen.kt
│               ├── LibraryScreen.kt
│               ├── MediaFormDialog.kt
│               ├── DetailDialog.kt
│               ├── BackupRestoreDialog.kt
│               ├── Components.kt
│               ├── Theme.kt
│               └── Actions.kt
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 🔐 Izin Aplikasi

| Izin | Kegunaan |
|---|---|
| `INTERNET` | Memuat poster dan membuka link streaming |
| `POST_NOTIFICATIONS` | Menampilkan notifikasi pengingat tayang |
| `SCHEDULE_EXACT_ALARM` | Menjadwalkan pengingat tepat waktu |
| `VIBRATE`, `WAKE_LOCK` | Getar dan membangunkan perangkat saat pengingat |

---

## 📝 Catatan

- Poster dimuat dari URL via Coil, jadi memerlukan koneksi internet.
- Semua data tersimpan **lokal di perangkat**. Gunakan fitur **Cadangkan & Pulihkan** sebelum ganti HP atau menghapus aplikasi.
- Workflow GitHub Actions menghasilkan **APK debug**. Untuk rilis ke Play Store, diperlukan build release yang ditandatangani (keystore).

---

## 🤝 Kontribusi

Pull request dan saran sangat diterima. Silakan buka *issue* untuk melaporkan bug atau mengusulkan fitur.
