package ie.suric.suricapp.local

/** Curated navigation over immutable Starter Library articles. Article IDs remain the stable content keys. */
data class RetroLibrarySubcategory(
    val title: String,
    val description: String,
    val articleIds: List<String>
)

data class RetroLibraryCategory(
    val title: String,
    val description: String,
    val subcategories: List<RetroLibrarySubcategory>
)

object RetroLibraryNavigation {
    val categories = listOf(
        RetroLibraryCategory("PlayStation", "Console generations, homebrew and preservation", listOf(
            RetroLibrarySubcategory("PlayStation 1", "PS1 hardware and homebrew", listOf("retro.ps1-homebrew")),
            RetroLibrarySubcategory("PlayStation 2", "PS2 hardware, OPL and storage", listOf("retro.ps2-homebrew", "retro.ps2-loading-options")),
            RetroLibrarySubcategory("PlayStation 3", "HEN, CFW and model checks", listOf("retro.ps3-homebrew", "retro.ps3-cfw-hen-check")),
            RetroLibrarySubcategory("PlayStation 4", "Firmware-aware homebrew", listOf("retro.ps4-homebrew", "retro.ps4-firmware-check")),
            RetroLibrarySubcategory("PlayStation 5", "Current research and limits", listOf("retro.ps5-homebrew", "retro.ps5-research-limits")),
            RetroLibrarySubcategory("Hardware care", "Identification and safe troubleshooting", listOf("retro.console-care"))
        )),
        RetroLibraryCategory("Android", "Platform versions, device changes and development", listOf(
            RetroLibrarySubcategory("Versions and API levels", "37 distinct Android API releases, plus the current overview", StarterLibrary.androidVersionGuideIds() + "retro.android-version-map"),
            RetroLibrarySubcategory("Architecture and development", "System layers, apps and debugging", listOf("retro.android-architecture", "retro.android-apk-anatomy")),
            RetroLibrarySubcategory("Bootloader, root and ROMs", "Device-specific modification basics", listOf("retro.android-boot-chain", "retro.android-root-roms")),
            RetroLibrarySubcategory("Getting started", "Identify your exact phone or tablet", listOf("retro.start-here"))
        )),
        RetroLibraryCategory("Android TV & Google TV", "TV devices, apps, remotes and local media", listOf(
            RetroLibrarySubcategory("Platform and setup", "TV interfaces, apps and developer access", listOf("retro.android-tv")),
            RetroLibrarySubcategory("Streaming between rooms", "Video, interface and controller paths", listOf("retro.whole-home-streaming"))
        )),
        RetroLibraryCategory("NVIDIA Shield", "Shield TV models, display output and media", listOf(
            RetroLibrarySubcategory("Shield TV overview", "Hardware, playback and network storage", listOf("retro.shield-overview")),
            RetroLibrarySubcategory("Display mirroring", "What can and cannot be mirrored", listOf("retro.shield-mirroring"))
        )),
        RetroLibraryCategory("Emulation", "Legal game use, formats and practical setup", listOf(
            RetroLibrarySubcategory("Getting started", "Frontends, files, controls and saves", listOf("retro.emulation")),
            RetroLibrarySubcategory("Console homebrew", "Projects and trusted download sources", listOf("retro.homebrew-catalogues"))
        )),
        RetroLibraryCategory("Homebrew Games", "Homebrew, freeware, demos and preservation", listOf(
            RetroLibrarySubcategory("Finding projects", "Check licences and original releases", listOf("retro.homebrew-catalogues")),
            RetroLibrarySubcategory("Modded Warfare", "Video research with source cross-checks", listOf("retro.modded-warfare"))
        )),
        RetroLibraryCategory("Development & Reverse Engineering", "Build and inspect software responsibly", listOf(
            RetroLibrarySubcategory("Android development", "APK, DEX, signing and debugging", listOf("retro.android-architecture")),
            RetroLibrarySubcategory("Application analysis", "Static and dynamic inspection basics", listOf("retro.reverse-engineering"))
        )),
        RetroLibraryCategory("Networking & Streaming", "Local media, remote play and latency", listOf(
            RetroLibrarySubcategory("Whole-home streaming", "Understand video and controller paths", listOf("retro.whole-home-streaming")),
            RetroLibrarySubcategory("Android TV networking", "Wi-Fi, Ethernet, SMB and DLNA", listOf("retro.android-tv"))
        )),
        RetroLibraryCategory("Tools & Technical Resources", "Official documentation and safe workflows", listOf(
            RetroLibrarySubcategory("Identify before changing", "Record model, software and recovery path", listOf("retro.start-here")),
            RetroLibrarySubcategory("Trusted resources", "Verify project, version and release origin", listOf("retro.homebrew-catalogues", "retro.modded-warfare"))
        ))
    )

    fun articles(subcategory: RetroLibrarySubcategory): List<StarterItem> =
        subcategory.articleIds.mapNotNull(StarterLibrary::byId)
}
