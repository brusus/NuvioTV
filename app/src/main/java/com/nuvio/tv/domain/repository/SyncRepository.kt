package com.nuvio.tv.domain.repository

// Emptied: dead code. Every method (generateSyncCode/getSyncCode/claimSyncCode/
// unlinkDevice/getLinkedDevices) was reachable only from the manual sync-code
// pairing UI (AccountScreen/SyncCodeGenerateScreen/SyncCodeClaimScreen), which
// was itself dead - superseded by QR sign-in. Left as an empty file rather
// than deleted for this session, per environment restrictions on file
// deletion; safe to delete outright in a normal editor (along with
// data/repository/SyncRepositoryImpl.kt and its RepositoryModule.kt binding).
