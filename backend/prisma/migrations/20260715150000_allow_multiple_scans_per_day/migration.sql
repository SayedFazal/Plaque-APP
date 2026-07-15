-- DropIndex
DROP INDEX "Scan_userId_localDate_key";

-- CreateIndex
CREATE INDEX "Scan_userId_localDate_idx" ON "Scan"("userId", "localDate");
