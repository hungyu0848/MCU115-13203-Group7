"""Exercise the prepared Kotlin SQL against isolated in-memory SQLite; never opens app data."""
import pathlib
import re
import sqlite3
import unittest

SOURCE = pathlib.Path(__file__).resolve().parents[1] / 'app/src/main/java/com/example/wheretogo/SavedTripSql.kt'
SQL = dict(re.findall(r'const val (\w+) = """(.*?)"""', SOURCE.read_text(encoding='utf-8')))


class SavedTripSqlTest(unittest.TestCase):
    def setUp(self):
        self.db = sqlite3.connect(':memory:')
        self.db.execute('PRAGMA foreign_keys = ON')
        # Account schema copied from Room's existing v1 generated implementation.
        self.db.execute('CREATE TABLE `accounts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, '
                        '`account` TEXT NOT NULL, `salt` BLOB NOT NULL, `passwordHash` BLOB NOT NULL, '
                        '`algorithm` TEXT NOT NULL, `iterations` INTEGER NOT NULL, `keyBits` INTEGER NOT NULL)')
        self.db.execute('CREATE UNIQUE INDEX `index_accounts_account` ON `accounts` (`account`)')
        self.accounts = [(1, 'qa_one', b'salt1', b'hash1', 'algo', 10, 256),
                         (2, 'qa_two', b'salt2', b'hash2', 'algo', 20, 256)]
        self.db.executemany('INSERT INTO accounts VALUES (?, ?, ?, ?, ?, ?, ?)', self.accounts)
        self.db.commit()
        self.db.execute('BEGIN')
        self.db.execute(SQL['CREATE_TABLE'])
        self.db.execute(SQL['CREATE_ACCOUNT_INDEX'])
        self.db.commit()

    def tearDown(self):
        self.db.close()

    def test_migration_preserves_accounts_and_ownership(self):
        self.assertEqual(self.accounts, self.db.execute('SELECT * FROM accounts ORDER BY id').fetchall())
        a = self.db.execute(SQL['INSERT'], (1, '{"stops":["a"]}')).lastrowid
        b = self.db.execute(SQL['INSERT'], (2, '{"stops":["b"]}')).lastrowid
        self.assertEqual((a, 1, '{"stops":["a"]}'), self.db.execute(SQL['READ'], (1, a)).fetchone())
        self.assertIsNone(self.db.execute(SQL['READ'], (2, a)).fetchone())
        self.assertEqual([(b, 2, '{"stops":["b"]}')], self.db.execute(SQL['LIST'], (2,)).fetchall())
        self.assertEqual(0, self.db.execute(SQL['DELETE'], (2, a)).rowcount)
        self.assertEqual(1, self.db.execute(SQL['DELETE'], (1, a)).rowcount)
        self.assertIsNotNone(self.db.execute(SQL['READ'], (2, b)).fetchone())

    def test_foreign_key_rejects_orphans_and_account_deletion(self):
        with self.assertRaises(sqlite3.IntegrityError):
            self.db.execute(SQL['INSERT'], (999, '{}'))
        self.db.execute(SQL['INSERT'], (1, '{}'))
        with self.assertRaises(sqlite3.IntegrityError):
            self.db.execute('DELETE FROM accounts WHERE id = ?', (1,))
        self.assertIsNotNone(self.db.execute('SELECT id FROM accounts WHERE id = 1').fetchone())

    def test_existing_trip_table_is_not_overwritten(self):
        with self.assertRaises(sqlite3.OperationalError):
            self.db.execute(SQL['CREATE_TABLE'])
        self.assertEqual(self.accounts, self.db.execute('SELECT * FROM accounts ORDER BY id').fetchall())

    def test_migration_failure_rolls_back_without_touching_accounts(self):
        db = sqlite3.connect(':memory:')
        try:
            db.execute('CREATE TABLE accounts (id INTEGER PRIMARY KEY, account TEXT NOT NULL)')
            db.execute('INSERT INTO accounts VALUES (1, ?)', ('retained',))
            db.execute('CREATE INDEX index_saved_trips_accountId ON accounts(account)')
            db.commit()
            db.execute('BEGIN')
            db.execute(SQL['CREATE_TABLE'])
            with self.assertRaises(sqlite3.OperationalError):
                db.execute(SQL['CREATE_ACCOUNT_INDEX'])
            db.rollback()
            self.assertEqual([(1, 'retained')], db.execute('SELECT * FROM accounts').fetchall())
            self.assertIsNone(db.execute("SELECT name FROM sqlite_master WHERE name = 'saved_trips'").fetchone())
        finally:
            db.close()


if __name__ == '__main__':
    unittest.main(verbosity=2)
