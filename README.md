# StoreNetworkSystem

מערכת ניהול רשת חנויות בגדים – Client Server

## תיאור כללי

הפרויקט מממש מערכת לניהול רשת חנויות בגדים המבוססת על ארכיטקטורת Client-Server.
המערכת מאפשרת עבודה עם מספר סניפים, עובדים, לקוחות ומלאי, תוך סנכרון נתונים בזמן אמת בין העובדים המחוברים.

המערכת רצה דרך הקונסול (Shell) ומדמה תרחיש אמיתי של מערכת ארגונית, כולל אימות משתמשים, הרשאות, מניעת התחברות כפולה, ניהול צ׳אטים ודוחות.

---

## טכנולוגיות ושיטות עבודה

* Java
* Client Server באמצעות Sockets
* OOP מלא: ירושה, פולימורפיזם, ממשקים
* Collections
* Exceptions
* Threads (לשרת וריבוי קליינטים)
* Design Patterns (Singleton, Strategy, Observer)
* עבודה עם JSON
* כתיבת קבצים (דוחות, לוגים)

---

## מבנה תיקיות

```
src/
 ├── client/
 │   ├── ClientMain.java        // צד לקוח – קונסול
 │   ├── ServerConnection.java  // חיבור לשרת
 │   └── ui/                    // לוגיקת ממשק קונסול
 │
 ├── server/
 │   ├── ServerMain.java        // שרת
 │   ├── ClientHandler.java     // Thread לכל משתמש
 │   │
 │   ├── core/                  // לוגיקה עסקית
 │   │   ├── AuthService.java
 │   │   ├── InventoryService.java
 │   │   ├── CustomerService.java
 │   │   ├── EmployeeService.java
 │   │   ├── PurchaseService.java
 │   │   ├── ReportService.java
 │   │   ├── LogService.java
 │   │   └── ServerDataStore.java
 │   │
 │   ├── model/                 // ישויות המערכת
 │   │   ├── employee/
 │   │   ├── customer/
 │   │   └── product/
 │   │
 │   └── chat/                  // מערכת צ׳אט
 │       └── ChatManager.java
 │
 └── common/
     ├── Protocol.java          // פקודות מערכת
     └── Message.java           // פורמט הודעות
```

---

## איך מריצים את המערכת

### קומפילציה

מהתיקייה הראשית של הפרויקט:

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
```

### הרצת השרת

```powershell
java -cp out server.ServerMain
```

### הרצת לקוח

בחלון נוסף:

```powershell
java -cp out client.ClientMain
```

---

## התחברות למערכת

בעת חיבור:

```
Username:
Password:
```

לאחר התחברות מוצלחת יוצג תפריט פקודות בהתאם לתפקיד המשתמש.

---

## פקודות עיקריות במערכת

### מלאי

* `inv`
  מציג את המלאי של הסניף הנוכחי

* `buy`
  קניית מוצר (הגדלת מלאי)

* `sell`
  מכירת מוצר (הפחתת מלאי, עם הגנה מ־Sold Out)

---

### לקוחות

* `cust_add`
  הוספת לקוח חדש (NEW / RETURNING / VIP)

* `cust_list`
  הצגת כל הלקוחות ברשת

* `purchase`
  ביצוע רכישה ללקוח לפי סוגו
  (מימוש פולימורפיזם – כל סוג לקוח מטפל בהנחה אחרת)

---

### עובדים

* `emp_add`
  הוספת עובד חדש (Admin בלבד)

* `emp_list`
  הצגת רשימת עובדים

---

### דוחות

* `report_branch`
  דוח יומי לפי סניף (JSON)

* `report_product`
  דוח לפי מוצר

* `report_category`
  דוח לפי קטגוריה

* `export_word`
  יצוא הדוח האחרון לקובץ Word

---

### צ׳אט בין סניפים

* `chat_request`
  בקשת צ׳אט מול סניף אחר

* `chat_poll`
  בדיקת הודעות נכנסות

* `chat_send`
  שליחת הודעה

* `chat_end`
  סיום צ׳אט

המערכת מונעת:

* התחברות כפולה לאותו משתמש
* השתתפות בצ׳אט ממספר מחשבים
* חיבור למשתמש תפוס

---

### לוגים

* `log_file`
  הצגת קובץ לוג של פעולות המערכת:

  * רישום עובדים
  * רישום לקוחות
  * קניות ומכירות
  * צ׳אטים

---

## אבטחה ובקרות

* אימות משתמשים
* הרשאות לפי תפקיד
* מניעת התחברות כפולה
* טיפול בחריגות (Exceptions)
* סנכרון נתונים בין עובדים בסניף

---

## סיכום

הפרויקט מממש מערכת מלאה לניהול רשת חנויות בגדים, תוך שימוש נכון בעקרונות OOP, Client Server, Threads, Design Patterns וניהול נתונים.
