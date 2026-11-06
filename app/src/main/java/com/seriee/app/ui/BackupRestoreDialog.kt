package com.seriee.app.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seriee.app.SerieeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupRestoreDialog(
    vm: SerieeViewModel,
    onDismiss: () -> Unit
) {
    val p = LocalPalette.current
    val context = LocalContext.current
    var replaceMode by remember { mutableStateOf(false) }

    // Export launcher
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            vm.exportBackup(context, uri).onSuccess { count ->
                Toast.makeText(context, "✅ Berhasil mencadangkan $count series ke file JSON!", Toast.LENGTH_LONG).show()
                onDismiss()
            }.onFailure { err ->
                Toast.makeText(context, "Gagal mencadangkan: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Import launcher
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            vm.importBackup(context, uri, replace = replaceMode).onSuccess { count ->
                Toast.makeText(context, "✅ Berhasil memulihkan $count series dari file cadangan!", Toast.LENGTH_LONG).show()
                onDismiss()
            }.onFailure { err ->
                Toast.makeText(context, "Gagal memulihkan: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = p.surface,
            border = BorderStroke(1.dp, p.border)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Storage,
                        contentDescription = null,
                        tint = SerieColors.Blue400,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Cadangkan & Pulihkan",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = p.text,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Tutup", tint = p.muted)
                    }
                }

                // Info banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(p.chip)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📦 Data Saat Ini: ${vm.items.size} series tersimpan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = p.text
                    )
                }

                // Section 1: Backup / Export
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(p.card)
                        .border(1.dp, p.border, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.FileDownload,
                            contentDescription = null,
                            tint = SerieColors.Blue400,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Cadangkan ke File JSON",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = p.text
                        )
                    }

                    Text(
                        "Simpan seluruh daftar series, episode, poster cover, dan jadwal tayang ke file JSON di penyimpanan internal perangkat Anda.",
                        fontSize = 12.sp,
                        color = p.muted,
                        lineHeight = 17.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SerieColors.Blue600)
                            .clickable {
                                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                exportLauncher.launch("seriee_backup_$timeStamp.json")
                            }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.FileDownload,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Simpan File Backup (.json)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Section 2: Restore / Import
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(p.card)
                        .border(1.dp, p.border, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.FileUpload,
                            contentDescription = null,
                            tint = SerieColors.Emerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Pulihkan dari File JSON",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = p.text
                        )
                    }

                    Text(
                        "Impor kembali file cadangan JSON dari memori HP. Anda dapat menggabungkan dengan data yang ada atau menimpanya.",
                        fontSize = 12.sp,
                        color = p.muted,
                        lineHeight = 17.sp
                    )

                    // Mode Selection: Merge vs Replace
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(p.chip)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Metode Pemulihan:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = p.text
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { replaceMode = false },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (!replaceMode) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (!replaceMode) SerieColors.Blue400 else p.muted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    "Gabungkan Data (Direkomendasikan)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = p.text
                                )
                                Text(
                                    "Menambah series dari file tanpa menghapus yang sudah ada",
                                    fontSize = 10.sp,
                                    color = p.muted
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { replaceMode = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (replaceMode) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (replaceMode) SerieColors.Orange else p.muted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    "Timpa / Ganti Seluruh Data",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = p.text
                                )
                                Text(
                                    "Semua data sekarang akan diganti dengan isi file cadangan",
                                    fontSize = 10.sp,
                                    color = p.muted
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SerieColors.Emerald)
                            .clickable {
                                importLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                            }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.FileUpload,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Pilih File Cadangan (.json)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
