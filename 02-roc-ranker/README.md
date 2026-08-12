# 02-roc-ranker

Module ที่ 2 ของ `metastock-roc-ranking` — ดึงราคา DR จาก MetaStock database ตามรายชื่อ,
คำนวณ YTD ROC (รายเดือน) และ Forward-Month ROC, จัดอันดับ (ranking) แล้ว export เป็น CSV

## ลำดับการทำงาน (`Main.java`)

1. โหลดรายชื่อ DR จาก `C:\meta\dr\report\drafl.txt` (comma-separated, 1 บรรทัด)
2. เปิด MetaStock database (`MetaStock.loadMaster()`) แล้วดึงราคาปิดรายวันของแต่ละ DR
   ทั้งหมด เรียงตามวันที่ (`MetaStock.createStockList(symbol)`)
3. คำนวณ ROC 2 ค่า ต่อ DR ต่อเดือน (ตั้งแต่มกราคมถึงเดือนล่าสุดที่มีข้อมูล):
   - **YTD%** = ราคาปิด begin-of-month ÷ ราคาปิด begin-of-year − 1
   - **ForwardMonthROC%** = ราคาปิด next-begin-of-month ÷ ราคาปิด begin-of-month − 1
4. เรียงแต่ละเดือนตาม YTD% มากไปน้อย เก็บ Top 10
5. เขียนผลรวมทุกเดือนลง `C:\meta\dr\report\DRYTD.csv`
6. แสดง Top 10 ของเดือนล่าสุดที่ command prompt แบบจัด column ให้ตรงกัน

## โครงสร้างโปรเจกต์ (Plain Java, ไม่ใช้ Maven)

```
02-roc-ranker/
 ├─ README.md
 └─ src/
     └─ com/metastock/rocranker/
         ├─ Main.java
         ├─ MetaStock.java
         ├─ StockRecord.java
         ├─ StockTimeRecord.java
         ├─ HeaderMetaStock.java
         ├─ DrListLoader.java
         ├─ RocCalculator.java
         ├─ RocReportGenerator.java
         ├─ RocReportRow.java
         ├─ NumberFormatUtil.java
         ├─ CsvReportWriter.java
         └─ ConsoleReportPrinter.java
```

โปรเจกต์นี้ไม่มี external dependency ใช้แค่ JDK มาตรฐาน (`java.time`, `java.io`, `java.nio.file`)
จึงไม่จำเป็นต้องใช้ Maven/Gradle

### เปิดใน IntelliJ IDEA
1. `Open` เลือก folder `02-roc-ranker`
2. คลิกขวาที่ folder `src` → `Mark Directory as` → `Sources Root` (ถ้ายังไม่ถูก mark อัตโนมัติ)
3. คลิกขวาที่ `Main.java` → `Run 'Main.main()'`

### รันจาก command line
```
cd 02-roc-ranker/src
javac com/metastock/rocranker/*.java
java com.metastock.rocranker.Main [metaStockDir] [drListFile] [outputCsv]
```
(ไม่ใส่ argument ก็ได้ จะใช้ default path ตามที่ระบุไว้ใน `Main.java`)

## Logic การหา begin-of-year / begin-of-month (สำคัญ)

ข้อมูลราคาของแต่ละ DR ที่ได้จาก `MetaStock.createStockList()` จะเรียงตามวันที่จากเก่าไปใหม่
เสมออยู่แล้ว โค้ดใน `RocCalculator` จึงหา "ราคาปิดวันแรกของปี/เดือนนั้น" ด้วยการ scan
list ครั้งเดียวหา record แรกที่ตรงปี/เดือนเป้าหมาย ซึ่งครอบคลุมทั้ง 2 กรณีในตัวเดียว:

- **กรณีปกติ**: DR มีข้อมูลอยู่แล้วก่อนหน้าปี/เดือนนั้น → record แรกที่เจอ = วันทำการแรกจริงของปี/เดือน
- **กรณี DR เพิ่งเริ่มเทรดกลางปี/เดือน** (เช่น MICRON01 เริ่ม 9 ก.พ. 2026) → record แรกที่เจอ
  ก็คือ record แรกสุดของ DR นั้นพอดี → ใช้เป็นค่า fallback โดยอัตโนมัติ ไม่ต้องเขียนแยก branch

**GOOG80** ใช้เป็น reference symbol เพียงเพื่อบอกว่า "ปีปัจจุบันคือปีไหน และเดือนล่าสุดที่มีการ
เทรดคือเดือนอะไร" (เพราะมีประวัติยาวและครบที่สุด) → ใช้กำหนดว่า report จะมีกี่ block เดือน
ไม่ได้ใช้ mapping วันที่แบบ 1:1 กับ DR ตัวอื่น

**Next-begin-of-month fallback**: ถ้าเดือนถัดไปยังไม่เริ่มเทรด (กรณีเดือนล่าสุด/current month)
จะ fallback ไปใช้ราคาปิดล่าสุดที่มีอยู่ (`RocCalculator.latest()`) แทน ตามที่ระบุไว้

## Column ของ CSV (`CsvReportWriter`)

```
No, Month, Symbol, BeginOfYearDate, BeginOfYearPrice, YTD(%),
ForwardMonthROC(%), NextBeginOfMonthPrice, BeginOfMonthPrice, LatestPrice
```

- คอลัมน์ราคา (BeginOfYearPrice, NextBeginOfMonthPrice, BeginOfMonthPrice, LatestPrice):
  ปัด 2 ทศนิยม แต่ถ้าลงตัว (เช่น 225.00) จะตัดทศนิยมออกเหลือ `225`
- คอลัมน์ % (YTD, ForwardMonthROC): แสดง 2 ทศนิยมเสมอ (เช่น `12.30`, `-5.00`)

## Class ที่มีในโปรเจกต์นี้

| Class | หน้าที่ |
|---|---|
| `StockRecord`, `StockTimeRecord`, `HeaderMetaStock`, `MetaStock` | คัดลอกจาก module/ไฟล์ที่ให้มา เปลี่ยนแค่ package |
| `DrListLoader` | อ่านไฟล์รายชื่อ DR (comma-separated) |
| `RocCalculator` | หา begin-of-year / begin-of-month price ต่อ symbol |
| `RocReportGenerator` | orchestrate การคำนวณ YTD / ForwardMonthROC + sort + top10 ต่อเดือน |
| `RocReportRow` | bean 1 แถวของ report |
| `NumberFormatUtil` | format ตัวเลขตาม rule ด้านบน |
| `CsvReportWriter` | เขียนไฟล์ CSV |
| `ConsoleReportPrinter` | print ตารางที่ command prompt แบบจัด column |
| `Main` | entry point เชื่อมทุกอย่างเข้าด้วยกัน |

## หมายเหตุ / สมมติฐานที่ต้องตรวจสอบกับข้อมูลจริง

1. **Path**: `MetaStock database dir`, `drafl.txt`, และ `DRYTD.csv` ใช้ default ตาม path ที่ระบุ
   ในโจทย์ (`C:\MetaStockData`, `C:\meta\dr\report\...`) แก้ไขได้ผ่าน command-line argument
   หรือแก้ constant ใน `Main.java`
2. **สูตร YTD/ForwardMonthROC** ตีความเป็น "% การเปลี่ยนแปลง" คือ `(ราคาใหม่/ราคาเก่า − 1) × 100`
   ตามความหมายทั่วไปของ ROC (ถ้าต้องการแค่ผลหาร ไม่ลบ 1 แจ้งได้ แก้ที่ `RocReportGenerator`)
3. **GOOG80** ต้องมีอยู่ใน MetaStock database และมี MASTER/XMASTER entry ที่ `loadMaster()`
   หาเจอ (ทดสอบด้วย `TestMetaStock.java` เดิมได้ก่อนหากไม่แน่ใจ)
4. โปรเจกต์นี้ compile ผ่านแล้วด้วย JDK 21 (`javac`) และมี smoke test ยืนยัน logic
   begin-of-year/month กับ fallback case (MICRON01-style) แล้วว่าให้ผลตรงตามที่ระบุ
