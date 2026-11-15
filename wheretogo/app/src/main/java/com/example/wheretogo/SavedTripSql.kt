package com.example.wheretogo

/** Prepared v1 -> v2 SQL; not wired to the production database until migration is verified. */
object SavedTripSql {
    const val CREATE_TABLE = """CREATE TABLE `saved_trips` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `accountId` INTEGER NOT NULL, `snapshotJson` TEXT NOT NULL, FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)"""
    const val CREATE_ACCOUNT_INDEX = """CREATE INDEX `index_saved_trips_accountId` ON `saved_trips` (`accountId`)"""
    const val INSERT = """INSERT INTO `saved_trips` (`accountId`, `snapshotJson`) VALUES (?, ?)"""
    const val READ = """SELECT `id`, `accountId`, `snapshotJson` FROM `saved_trips` WHERE `accountId` = ? AND `id` = ?"""
    const val LIST = """SELECT `id`, `accountId`, `snapshotJson` FROM `saved_trips` WHERE `accountId` = ? ORDER BY `id` DESC"""
    const val DELETE = """DELETE FROM `saved_trips` WHERE `accountId` = ? AND `id` = ?"""
}
