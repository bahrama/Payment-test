#  سامانه مدیریت پرداخت

PayFlow یک پروژه بک‌اند آموزشی با Java و Spring Boot برای مدیریت چرخه عمر
پرداخت‌هاست. این پروژه برای تمرین طراحی REST API، کار با JPA، اعتبارسنجی
ورودی‌ها، مدیریت خطاها، جلوگیری از درخواست تکراری و ثبت تاریخچه وضعیت
پرداخت طراحی شده است.

> **وضعیت فعلی دیتابیس:** پیکربندی موجود از H2 در حافظه استفاده می‌کند.
> حالت `MODE=PostgreSQL` در H2 فقط برخی رفتارهای PostgreSQL را شبیه‌سازی
> می‌کند و جایگزین تست روی PostgreSQL واقعی نیست.

## قابلیت‌ها

-   ایجاد پرداخت با `POST /api/v1/payments`
-   جلوگیری از ایجاد پرداخت تکراری با هدر `Idempotency-Key`
-   رد استفاده مجدد از همان کلید برای محتوای متفاوت
-   پردازش پرداخت با درگاه شبیه‌سازی‌شده
-   لغو پرداخت در وضعیت مجاز
-   دریافت جزئیات یک پرداخت
-   فهرست پرداخت‌ها با صفحه‌بندی و فیلتر وضعیت
-   مشاهده تاریخچه تغییر وضعیت پرداخت
-   اعتبارسنجی DTOها و پارامترهای ورودی
-   مدیریت متمرکز خطاهای API
-   نگهداری تاریخچه وضعیت پرداخت در دیتابیس

## فناوری‌ها

-   Java 17
-   Spring Boot
-   Spring Web MVC / REST
-   Spring Data JPA و Hibernate
-   H2 Database
-   Jakarta Bean Validation
-   Lombok
-   MapStruct
-   Maven

## پیش‌نیازها

قبل از اجرا مطمئن شوید موارد زیر نصب شده‌اند:

1.  **JDK 17 یا بالاتر**
2.  **Maven 3.9 یا نسخه سازگار**
3.  یک IDE مانند IntelliJ IDEA (اختیاری، اما برای اجرای `apitest.http`
    مفید است)

برای بررسی نصب Java و Maven:

``` bash
java -version
mvn -version
```

## دریافت و اجرای پروژه

### ۱. باز کردن پروژه

پروژه را از حالت ZIP خارج کنید و پوشه‌ای را که فایل `pom.xml` در آن قرار
دارد، در IntelliJ IDEA باز کنید. صبر کنید تا Maven وابستگی‌ها را دریافت و
پروژه را همگام‌سازی کند.

### ۲. اجرای برنامه

از ریشه پروژه، یعنی همان پوشه‌ای که `pom.xml` در آن قرار دارد، اجرا کنید:

``` bash
mvn spring-boot:run
```

روش جایگزین در IntelliJ IDEA این است که کلاس زیر را باز و اجرا کنید:

``` text
src/main/java/com/khebre/payment/PaymentApplication.java
```

در صورت اجرای موفق، برنامه به‌طور پیش‌فرض روی پورت `8080` در دسترس خواهد
بود:

``` text
http://localhost:8080
```

### ۳. ساخت فایل JAR

برای اجرای تست‌ها و ساخت خروجی:

``` bash
mvn clean test
mvn clean package
```

سپس فایل JAR تولیدشده را از پوشه `target` اجرا کنید. نام فایل با توجه به
نسخه پروژه ممکن است متفاوت باشد؛ برای نسخه فعلی معمولاً:

``` bash
java -jar target/payment-0.0.1-SNAPSHOT.jar
```

## راه‌اندازی پایگاه داده

### تنظیم فعلی: H2 در حافظه

پیکربندی دیتابیس در فایل زیر قرار دارد:

``` text
src/main/resources/application.properties
```

تنظیمات فعلی:

``` properties
spring.datasource.url=jdbc:h2:mem:payflowdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

در این پیکربندی، **اسکریپت SQL جداگانه‌ای برای ساخت جداول لازم نیست**؛
Hibernate هنگام راه‌اندازی برنامه، ساختار جداول را بر اساس Entityهای JPA
ایجاد یا به‌روزرسانی می‌کند.

### باز کردن کنسول H2

پس از اجرای برنامه، آدرس زیر را در مرورگر باز کنید:

``` text
http://localhost:8080/h2-console
```

مقادیر اتصال:

  فیلد           مقدار
  -------------- -------------------------
  JDBC URL       `jdbc:h2:mem:payflowdb`
  User Name      `sa`
  Password       خالی
  Driver Class   `org.h2.Driver`

روی **Connect** کلیک کنید.

**توجه:** چون دیتابیس از نوع `mem` است، داده‌ها با متوقف‌شدن برنامه از بین
می‌روند. `DB_CLOSE_DELAY=-1` فقط باعث می‌شود دیتابیس در طول عمر همان JVM
پس از بسته‌شدن آخرین اتصال بسته نشود؛ این گزینه داده‌ها را پس از خاموش‌شدن
برنامه دائمی نمی‌کند.

همچنین `MODE=PostgreSQL` به معنای سازگاری کامل با PostgreSQL نیست. پیش
از تحویل نهایی، بهتر است Migrationها و تست‌های یکپارچه روی PostgreSQL
واقعی نیز اجرا شوند.

## APIهای موجود

تمام endpointها زیر مسیر پایه `/api/v1/payments` قرار دارند.

  -------------------------------------------------------------------------------------------------------
  عملیات                  متد HTTP                مسیر
  ----------------------- ----------------------- -------------------------------------------------------
  ایجاد پرداخت            `POST`                  `/api/v1/payments`

  پردازش پرداخت           `POST`                  `/api/v1/payments/{paymentId}/process?merchantId=...`

  لغو پرداخت              `POST`                  `/api/v1/payments/{paymentId}/cancel?merchantId=...`

  دریافت جزئیات پرداخت    `GET`                   `/api/v1/payments/{paymentId}?merchantId=...`

  فهرست و فیلتر پرداخت‌ها  `GET`                   `/api/v1/payments?merchantId=...`

  تاریخچه وضعیت پرداخت    `GET`                   `/api/v1/payments/{paymentId}/history?merchantId=...`
  -------------------------------------------------------------------------------------------------------

### ایجاد پرداخت

نمونه درخواست:

``` http
POST http://localhost:8080/api/v1/payments
Content-Type: application/json
Idempotency-Key: order-1001-request-1

{
  "merchantId": "merchant-001",
  "merchantReference": "ORDER-1001",
  "amount": 500000,
  "currency": "IRR"
}
```

قواعد ورودی فعلی:

-   `merchantId` و `merchantReference` نباید خالی باشند و حداکثر ۱۰۰
    کاراکتر دارند.
-   `amount` الزامی، بزرگ‌تر از صفر و حداکثر با دو رقم اعشار است.
-   `currency` باید از سه حرف بزرگ انگلیسی تشکیل شود؛ برای مثال `IRR` یا
    `USD`.
-   هدر `Idempotency-Key` الزامی است و حداکثر ۱۰۰ کاراکتر دارد.

در صورت ایجاد پرداخت جدید، پاسخ با وضعیت `201 Created` برمی‌گردد. اگر
همان پذیرنده همان کلید Idempotency را با همان محتوای درخواست تکرار کند،
پرداخت قبلی برگردانده می‌شود. استفاده از همان کلید برای محتوای متفاوت،
خطای تعارض ایجاد می‌کند.

### پردازش پرداخت

بعد از دریافت `id` از پاسخ ایجاد پرداخت، آن را در مسیر قرار دهید:

``` http
POST http://localhost:8080/api/v1/payments/{paymentId}/process?merchantId=merchant-001
```

درگاه این پروژه شبیه‌سازی‌شده است و درخواست واقعی شبکه یا بانک ارسال
نمی‌کند. برای شبیه‌سازی نتیجه ناموفق، مقدار `merchantReference` را با
`FAIL-` شروع کنید؛ برای مثال `FAIL-ORDER-1002`. سایر مقادیر مرجع، نتیجه
موفق شبیه‌سازی‌شده ایجاد می‌کنند.

### لغو پرداخت

``` http
POST http://localhost:8080/api/v1/payments/{paymentId}/cancel?merchantId=merchant-001
```

در طراحی فعلی فقط پرداختی که در وضعیت `PENDING` است قابل لغو است. تلاش
برای لغو پرداختی که در وضعیت دیگری قرار دارد، خطای تعارض وضعیت ایجاد
می‌کند.

### فهرست پرداخت‌ها

``` http
GET http://localhost:8080/api/v1/payments?merchantId=merchant-001
```

فیلتر وضعیت:

``` http
GET http://localhost:8080/api/v1/payments?merchantId=merchant-001&status=SUCCESS
```

صفحه‌بندی:

``` http
GET http://localhost:8080/api/v1/payments?merchantId=merchant-001&page=0&size=10&sort=createdAt,desc
```

شماره صفحه از `0` شروع می‌شود. اندازه پیش‌فرض صفحه `20` رکورد است.

### دریافت تاریخچه

``` http
GET http://localhost:8080/api/v1/payments/{paymentId}/history?merchantId=merchant-001
```

وضعیت‌های تعریف‌شده برای پرداخت:

-   `PENDING`
-   `PROCESSING`
-   `SUCCESS`
-   `FAILED`
-   `CANCELLED`

## اجرای درخواست‌ها با IntelliJ HTTP Client

فایل `apitest.http` در ریشه پروژه برای ارسال درخواست‌های نمونه قرار دارد.

1.  برنامه Spring Boot را اجرا کنید.
2.  فایل `apitest.http` را در IntelliJ باز کنید.
3.  روی آیکون اجرای کنار درخواست موردنظر کلیک کنید.
4.  در صورت نیاز، شناسه پرداخت را از پاسخ درخواست ایجاد پرداخت بردارید و
    در درخواست‌های بعدی استفاده کنید.

## ساختار کلی سورس

``` text
src/main/java/com/khebre/payment/
├── controller/   # دریافت درخواست‌های HTTP و ساخت پاسخ
├── dao/          # Spring Data JPA repositories
├── dto/          # DTOهای ورودی و خروجی
├── exception/    # Exceptionها و مدیریت مرکزی خطاها
├── model/        # Entityها و وضعیت پرداخت
└── service/      # منطق کسب‌وکار و درگاه شبیه‌سازی‌شده

src/main/resources/
├── application.properties
└── ValidationMessages.properties
```
