# Simple HUD (Fabric, Minecraft 26.2)

Mod client-side sing nampilno **FPS, Koordinat, Hari, Kompas, Ping** ning layar,
kabeh isok di-ON/OFF-no lan diatur posisi + ukurane dhewe.

## Penting dhisik: kenopo iki mesti aman nok server?

Nok `fabric.mod.json` enek baris `"environment": "client"`. Iku tegese
kabeh kode mod iki mung mlaku nok komputer/laptop-mu dhewe, ora enek kode
sing mlebu ning server. Dadi:

- Ora perlu masang mod iki nok server (lan pancen ora isok/ora perlu).
- Isok dinggo nok server vanilla, server modded, realms, LAN, opo wae.
- Server ora isok "mateni" utawa ngontrol mod iki, amarga server babar
  blas ora ngerti mod iki ono.

Iki bedo karo mod sing butuh dipasang bareng nok server (contone mod
sing nambahi block/item anyar) - HUD overlay ngene iki murni tampilan
lokal, dadi paling aman digawe "isok mlaku nok endi wae".

## Cara nganggo (pas wis dadi .jar)

1. Pasang [Fabric Loader](https://fabricmc.net/use/) kanggo Minecraft **26.2**.
2. Copy hasil build (`simplehud-1.0.0.jar` nok folder `build/libs/`) lan
   **Fabric API** (jar-e, download nok Modrinth/CurseForge) ning folder
   `.minecraft/mods/`.
3. Mlebu game, banjur mlebu **Options > Controls > Simple HUD** lan
   bind-ake tombol kanggo "Open HUD Editor" (contone tombol `H` utawa `'`).
4. Nok njero game, pencet tombol mau -> kebuka layar editor:
   - **Drag (klik kiri + geser)** = mindah posisi elemen
   - **Scroll mouse** pas kursor nok duwur elemen = ngganti ukuran
   - **Klik kanan** = ON / OFF elemen kasebut
   - **ESC** utawa tombol rampung = nutup lan otomatis kesimpen
5. Setelan kesimpen ning `.minecraft/config/simplehud.json`, tetep ono
   senajan game ditutup.

## Cara build dhewe (kudu duwe Java 25 + IDE)

Amarga versi Minecraft iki (26.2) anyar banget, aku saranake sampeyan
**miwiti teko template resmi Fabric** ben file `gradlew`/`gradlew.bat`
lan folder `gradle/wrapper` (sing gak tak sertakno nok kene) wis bener:

1. Download/clone template resmi:
   `https://github.com/FabricMC/fabric-example-mod` (pilih branch/tag
   sing cocok karo 26.2, contone `26.1.2` utawa sing luwih anyar).
2. Timpo (ganti) file `build.gradle`, `gradle.properties`,
   `settings.gradle`, `src/main/resources/fabric.mod.json`, lan kabeh
   isi `src/client/java/...` nganggo file-file teko paket iki.
3. Buka project nganggo IntelliJ IDEA (paling gampang, ana dukungan
   Fabric resmi). Tunggu Gradle sync rampung (bakal ndownload
   Minecraft 26.2 + sumber kode-ne, isok suwe pisanan).
4. Jalanno `./gradlew build` (utawa `gradlew.bat build` nok Windows).
   Hasil jar ono nok `build/libs/simplehud-1.0.0.jar`.
5. Kanggo testing langsung: jalanno `./gradlew runClient`.

## Nek pas compile ana error "method/class ora ketemu"

Iki resiko nyata amarga Minecraft 26.2 lagi wae rilis (~2-3 wulan
sakdurunge tanggal riset iki), lan API-ne pancen isih owah-owahan cepet
(pisan gedhe: Mojang mapping dadi standar, HUD API pindah dadi
`HudElementRegistry`, kelas gambar dadi `GuiGraphicsExtractor`). Kode
nok paket iki wis tak cocokno karo dokumentasi resmi Fabric paling
anyar sing tak temokno, tapi nek versi Fabric API/Loader-mu bedo sitik,
mungkin enek jeneng method sing mleset. Cara ndandani:

1. Woco pesen error-e Gradle/IntelliJ - biasane kandha jeneng method
   sing bener sing paling cedhak (IDE kadang kasih "did you mean...").
2. Buka https://docs.fabricmc.net/develop/ (pilih versi 26.2 nok
   dropdown pojok tengen duwur) - kono ana conto kode paling anyar
   kanggo `Rendering in the HUD` lan `Drawing to the GUI`.
3. Nok IntelliJ, isok tak-klik kanan jeneng kelas (contone
   `GuiGraphicsExtractor`) -> "Go to Declaration" kanggo ndelok method
   opo wae sing sakjane kasedhiya.

Bagean sing paling gampang mleset:
- `graphics.pose().pushMatrix()/popMatrix()` - iso wae jenenge dadi
  `.push()/.pop()` nok versi tartamtu.
- `mc.getConnection().getPlayerInfo(uuid)` - jeneng method kanggo njupuk
  data ping isok bedo.

## Struktur file

```
simplehud/
├── build.gradle
├── gradle.properties
├── settings.gradle
└── src/
    ├── main/resources/fabric.mod.json
    └── client/
        ├── java/my/simplehud/client/
        │   ├── SimpleHudClient.java   (entrypoint + render logic)
        │   ├── HudConfig.java         (simpen/moco setelan JSON)
        │   └── HudEditScreen.java     (layar drag/scroll/toggle)
        └── resources/assets/simplehud/lang/en_us.json
```
