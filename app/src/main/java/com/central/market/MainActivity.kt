package com.central.market

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    // =========================
    // CENTRAL MARKET IDENTITY
    // =========================

    private val navy = Color.rgb(12, 31, 52)
    private val navy2 = Color.rgb(18, 42, 66)
    private val blue = Color.rgb(32, 104, 170)
    private val blue2 = Color.rgb(54, 137, 205)
    private val gold = Color.rgb(205, 157, 45)
    private val gold2 = Color.rgb(235, 194, 86)
    private val green = Color.rgb(38, 130, 85)
    private val red = Color.rgb(180, 65, 65)
    private val orange = Color.rgb(205, 125, 40)
    private val background = Color.rgb(242, 246, 250)
    private val white = Color.WHITE
    private val textDark = Color.rgb(28, 42, 55)
    private val muted = Color.rgb(92, 108, 122)
    private val border = Color.rgb(220, 228, 236)

    private lateinit var content: LinearLayout

    // =========================
    // SESSION PROTECTION
    // =========================

    private val handler = Handler(Looper.getMainLooper())
    private val outsideAppTimeout = 10 * 60 * 1000L

    private var outsideAppStartedAt = 0L
    private var sessionLocked = false

    private val lockRunnable = Runnable {
        lockSession()
    }

    // =========================
    // ACTIVITY
    // =========================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )

        showHome()
    }

    override fun onStart() {
        super.onStart()

        handler.removeCallbacks(lockRunnable)

        if (outsideAppStartedAt > 0L) {
            val elapsed =
                SystemClock.elapsedRealtime() - outsideAppStartedAt

            if (elapsed >= outsideAppTimeout) {
                lockSession()
            }

            outsideAppStartedAt = 0L
        }
    }

    override fun onStop() {
        super.onStop()

        outsideAppStartedAt =
            SystemClock.elapsedRealtime()

        handler.removeCallbacks(lockRunnable)

        handler.postDelayed(
            lockRunnable,
            outsideAppTimeout
        )
    }

    override fun onDestroy() {
        handler.removeCallbacks(lockRunnable)
        super.onDestroy()
    }

    // =========================
    // SESSION LOCK
    // =========================

    private fun lockSession() {
        if (sessionLocked) return

        sessionLocked = true
        handler.removeCallbacks(lockRunnable)

        baseLayout("حماية الجلسة")

        addSection("الجلسة مقفلة")

        addStatusCard(
            "🔒 حماية تلقائية",
            "تم قفل الجلسة بعد مرور 10 دقائق خارج التطبيق.",
            red
        )

        addInfo(
            "سبب القفل",
            "هذه طبقة حماية محلية لتقليل خطر الوصول غير المصرح به عند ترك التطبيق."
        )

        addCard(
            "🔓 فتح الجلسة",
            "العودة إلى CENTRAL MARKET"
        ) {
            sessionLocked = false
            outsideAppStartedAt = 0L
            showHome()
        }
    }

    // =========================
    // MAIN LAYOUT
    // =========================

    private fun baseLayout(
        title: String = "CENTRAL MARKET"
    ): LinearLayout {

        val root = LinearLayout(this)

        root.orientation =
            LinearLayout.VERTICAL

        root.setBackgroundColor(background)

        val header = LinearLayout(this)

        header.orientation =
            LinearLayout.VERTICAL

        header.gravity =
            Gravity.CENTER

        header.setPadding(
            20,
            22,
            20,
            20
        )

        header.background =
            gradientBackground(
                navy,
                blue,
                24f
            )

        val brand = TextView(this)

        brand.text = "CENTRAL"
        brand.textSize = 27f
        brand.setTextColor(white)
        brand.setTypeface(null, Typeface.BOLD)
        brand.gravity = Gravity.CENTER

        header.addView(brand)

        val market = TextView(this)

        market.text = "MARKET"
        market.textSize = 14f
        market.setTextColor(gold2)
        market.setTypeface(null, Typeface.BOLD)
        market.gravity = Gravity.CENTER

        header.addView(market)

        val pageTitle = TextView(this)

        pageTitle.text = title
        pageTitle.textSize = 15f
        pageTitle.setTextColor(white)
        pageTitle.gravity = Gravity.CENTER
        pageTitle.setPadding(0, 7, 0, 0)

        header.addView(pageTitle)

        root.addView(
            header,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val scroll = ScrollView(this)

        scroll.setBackgroundColor(background)

        content = LinearLayout(this)

        content.orientation =
            LinearLayout.VERTICAL

        content.setPadding(
            14,
            16,
            14,
            28
        )

        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        setContentView(root)

        return content
    }

    // =========================
    // SECTION
    // =========================

    private fun addSection(title: String) {

        val box = LinearLayout(this)

        box.gravity =
            Gravity.CENTER_VERTICAL

        box.setPadding(
            14,
            10,
            14,
            10
        )

        box.background =
            roundedBackground(
                Color.rgb(232, 239, 246),
                14f
            )

        val line = View(this)

        line.setBackgroundColor(gold)

        box.addView(
            line,
            LinearLayout.LayoutParams(
                4,
                32
            )
        )

        val text = TextView(this)

        text.text = title
        text.textSize = 18f
        text.setTextColor(navy)
        text.setTypeface(null, Typeface.BOLD)
        text.setPadding(12, 0, 8, 0)

        box.addView(
            text,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        content.addView(
            box,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8
                bottomMargin = 12
            }
        )
    }

    // =========================
    // INFORMATION CARD
    // =========================

    private fun addInfo(
        title: String,
        body: String
    ) {

        val box = LinearLayout(this)

        box.orientation =
            LinearLayout.VERTICAL

        box.setPadding(
            18,
            16,
            18,
            16
        )

        box.background = cardBackground()

        val heading = TextView(this)

        heading.text = title
        heading.textSize = 15f
        heading.setTextColor(navy)
        heading.setTypeface(null, Typeface.BOLD)

        box.addView(heading)

        val description = TextView(this)

        description.text = body
        description.textSize = 14f
        description.setTextColor(textDark)
        description.setPadding(0, 7, 0, 0)

        box.addView(description)

        content.addView(
            box,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 10
            }
        )
    }

    // =========================
    // STATUS CARD
    // =========================

    private fun addStatusCard(
        title: String,
        body: String,
        statusColor: Int
    ) {

        val box = LinearLayout(this)

        box.orientation =
            LinearLayout.VERTICAL

        box.setPadding(
            18,
            16,
            18,
            16
        )

        box.background =
            GradientDrawable().apply {
                setColor(white)
                cornerRadius = 18f
                setStroke(3, statusColor)
            }

        val heading = TextView(this)

        heading.text = title
        heading.textSize = 16f
        heading.setTextColor(statusColor)
        heading.setTypeface(null, Typeface.BOLD)

        box.addView(heading)

        val text = TextView(this)

        text.text = body
        text.textSize = 14f
        text.setTextColor(textDark)
        text.setPadding(0, 7, 0, 0)

        box.addView(text)

        content.addView(
            box,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
            }
        )
    }

    // =========================
    // ACTION CARD
    // =========================

    private fun addCard(
        title: String,
        description: String,
        action: () -> Unit
    ) {

        val button = Button(this)

        button.text =
            "$title\n$description"

        button.textSize = 15f
        button.setTextColor(white)
        button.setTypeface(null, Typeface.BOLD)
        button.gravity = Gravity.CENTER_VERTICAL

        button.setPadding(
            18,
            15,
            18,
            15
        )

        button.background =
            gradientBackground(
                blue,
                blue2,
                18f
            )

        button.elevation = 5f

        button.setOnClickListener {
            action()
        }

        content.addView(
            button,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 11
            }
        )
    }

    // =========================
    // NAVIGATION BUTTON
    // =========================

    private fun addNavButton(
        title: String,
        action: () -> Unit
    ) {

        val button = Button(this)

        button.text = title
        button.textSize = 14f
        button.setTextColor(navy)
        button.setTypeface(null, Typeface.BOLD)

        button.setPadding(
            14,
            12,
            14,
            12
        )

        button.background =
            roundedBackground(
                white,
                16f
            )

        button.elevation = 2f

        button.setOnClickListener {
            action()
        }

        content.addView(
            button,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8
            }
        )
    }

    // =========================
    // DRAWABLE HELPERS
    // =========================

    private fun gradientBackground(
        startColor: Int,
        endColor: Int,
        radius: Float
    ): GradientDrawable {

        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                startColor,
                endColor
            )
        ).apply {
            cornerRadius = radius
        }
    }

    private fun cardBackground(): GradientDrawable {

        return GradientDrawable().apply {
            setColor(white)
            cornerRadius = 18f
            setStroke(1, border)
        }
    }

    private fun roundedBackground(
        color: Int,
        radius: Float
    ): GradientDrawable {

        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
        }
    }

    // =========================
    // HOME
    // =========================

    private fun showHome() {

        baseLayout("منصة واحدة .. عالم من الفرص.")

        addSection("CENTRAL MARKET")

        addStatusCard(
            "● المنصة الأساسية",
            "أسواق وخدمات ومعلومات قابلة للتوسع محليًا وعالميًا.",
            green
        )

        addInfo(
            "المالك",
            "المالك ياسر حسن وشركاؤه"
        )

        addInfo(
            "الرؤية",
            "منصة واحدة تجمع الفرص والخدمات والمعلومات ضمن بنية قابلة للتوسع."
        )

        addSection("الاتصال والوصول")

        addCard(
            "🌐 حالة الاتصال",
            "فحص الاتصال بالإنترنت وقدرات الشبكة"
        ) {
            showOnline()
        }

        addCard(
            "👤 وضع الضيف",
            "استكشاف الخدمات دون الدخول إلى بيانات خاصة"
        ) {
            showGuestMode()
        }

        addSection("الأسواق والخدمات")

        addCard(
            "🛒 المنتجات والأسواق",
            "مركبات، هواتف، إلكترونيات، مطاعم، وأسواق متعددة"
        ) {
            showProducts()
        }

        addCard(
            "🧰 الخدمات",
            "الخدمات العامة والمهنية والمجالات المختلفة"
        ) {
            showServices()
        }

        addCard(
            "📢 الإعلانات",
            "BRONZE • SILVER • GOLD"
        ) {
            showAds()
        }

        addCard(
            "🍳 المطبخ",
            "منتجات وخدمات وطلبات المطبخ"
        ) {
            showKitchen()
        }

        addSection("المجتمع والذكاء")

        addCard(
            "⭐ النقاط",
            "نظام المشاركة والتفاعل"
        ) {
            showPoints()
        }

        addCard(
            "🧠 الذكاء البشري",
            "المعرفة والخبرة والمشاركة البشرية"
        ) {
            showHumanIntelligence()
        }

        addCard(
            "🤖 CTM AI",
            "المساعد الرسمي للمعلومات المسموح بها"
        ) {
            showCtmAi()
        }

        addCard(
            "❤️ الخير والمساندة",
            "دعم الأيتام والمحتاجين والمبادرات"
        ) {
            showCharity()
        }

        addSection("الأمان والخصوصية")

        addCard(
            "🛡️ مركز الأمان",
            "حماية الجلسة والشاشة والخدمات الحساسة"
        ) {
            showSafety()
        }

        addCard(
            "🔐 مركز الخصوصية",
            "مبادئ الخصوصية وحماية المستخدم"
        ) {
            showPrivacyCenter()
        }

        addCard(
            "📋 سياسة البيانات",
            "قواعد البيانات واستخدامها"
        ) {
            showDataPolicy()
        }

        addCard(
            "⚖️ قواعد التطبيق",
            "القواعد العامة والضوابط"
        ) {
            showAppRules()
        }

        addSection("الأنظمة الخاصة")

        addCard(
            "🏢 مكتب المدير",
            "الإدارة والمراجعة الداخلية"
        ) {
            showManagerOffice()
        }

        addCard(
            "🦡 BADGER",
            "منتج مالي مستقل قابل للتطوير"
        ) {
            showBadger()
        }

        addCard(
            "🔒 الأنظمة الحساسة",
            "حماية المشاركة الاستثمارية والتمويلية"
        ) {
            showSensitiveAccessPolicy()
        }

        addCard(
            "ℹ️ معلومات التطبيق",
            "البنية والنسخة والمبادئ"
        ) {
            showAppInformation()
        }

        addSection("التنقل")

        addNavButton("⌂ الرئيسية") {
            showHome()
        }

        addNavButton("🔎 البحث") {
            showSearch()
        }

        addNavButton("⭐ النقاط") {
            showPoints()
        }

        addNavButton("👤 الحساب / الضيف") {
            showGuestMode()
        }
    }

    // =========================
    // ONLINE
    // =========================

    private fun showOnline() {

        baseLayout("حالة الاتصال")

        addSection("الاتصال بالإنترنت")

        val manager =
            getSystemService(
                CONNECTIVITY_SERVICE
            ) as ConnectivityManager

        val network =
            manager.activeNetwork

        val capabilities =
            manager.getNetworkCapabilities(network)

        val connected =
            capabilities != null

        val transport =
            when {
                capabilities?.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                ) == true -> "Wi-Fi"

                capabilities?.hasTransport(
                    NetworkCapabilities.TRANSPORT_CELLULAR
                ) == true -> "بيانات الهاتف"

                capabilities?.hasTransport(
                    NetworkCapabilities.TRANSPORT_ETHERNET
                ) == true -> "Ethernet"

                else -> "غير معروف"
            }

        if (connected) {
            addStatusCard(
                "🟢 متصل",
                "الاتصال متاح حاليًا عبر: $transport",
                green
            )
        } else {
            addStatusCard(
                "🔴 غير متصل",
                "لا يوجد اتصال شبكة متاح حاليًا.",
                red
            )
        }

        addInfo(
            "قدرات الشبكة",
            "يتم اكتشاف حالة الشبكة من الجهاز دون افتراض سرعة أو جودة غير معروفة."
        )

        addNavButton("↩ العودة للرئيسية") {
            showHome()
        }
    }

    // =========================
    // GUEST
    // =========================

    private fun showGuestMode() {

        baseLayout("وضع الضيف")

        addSection("استكشاف المنصة")

        addStatusCard(
            "👤 وضع الضيف",
            "يمكن استكشاف أجزاء من المنصة دون الوصول إلى البيانات الخاصة.",
            blue
        )

        addInfo(
            "الحماية",
            "لا يعني وضع الضيف امتلاك صلاحية لتنفيذ عمليات مالية أو الوصول إلى الأنظمة الحساسة."
        )

        addCard(
            "🛒 المنتجات",
            "استكشاف الأسواق"
        ) {
            showProducts()
        }

        addCard(
            "🧰 الخدمات",
            "استكشاف الخدمات"
        ) {
            showServices()
        }

        addCard(
            "📢 الإعلانات",
            "استعراض نظام الإعلانات"
        ) {
            showAds()
        }

        addNavButton("↩ العودة للرئيسية") {
            showHome()
        }
    }

        // =========================
    // SAFETY
    // =========================

    private fun showSafety() {

        baseLayout("الأمان والخصوصية")

        addSection("الحماية الأساسية")

        addStatusCard(
            "🔒 حماية الشاشة",
            "FLAG_SECURE مفعّل على مستوى Activity لمنع التقاط الشاشة وتسجيلها عبر آليات Android المدعومة.",
            green
        )

        addStatusCard(
            "⏱️ قفل الجلسة",
            "يتم قفل الجلسة تلقائيًا بعد 10 دقائق خارج التطبيق.",
            green
        )

        addInfo(
            "الخدمات الحساسة",
            "المشاركة الاستثمارية والتمويلية لها ضوابط وصول وحماية إضافية."
        )

        addCard(
            "🔐 سياسة الوصول الحساس",
            "الحماية والتحقق والأهلية"
        ) {
            showSensitiveAccessPolicy()
        }

        addCard(
            "🛡️ قائمة الحماية",
            "مراجعة طبقات الأمان"
        ) {
            showSecurityChecklist()
        }

        addCard(
            "🏛️ مكتب الأمن والمعلومات",
            "مبادئ المراجعة الأمنية"
        ) {
            showSecurityOffice()
        }

        addNavButton("↩ الرئيسية") {
            showHome()
        }
    }

    // =========================
    // PRODUCTS
    // =========================

    private fun showProducts() {

        baseLayout("المنتجات والأسواق")

        addSection("المجالات")

        val categories = listOf(
            "🚗 المركبات والشاحنات" to "المركبات والخدمات المرتبطة بها",
            "📱 الهواتف والإلكترونيات" to "الأجهزة والتقنيات",
            "🍽️ المطاعم والتوصيل" to "المطاعم والطلبات",
            "🌾 الزراعة والثروة الحيوانية" to "القطاع الزراعي والحيواني",
            "🐟 الأسماك" to "منتجات وخدمات الأسماك",
            "🏗️ البناء والجملة" to "مواد البناء والبيع بالجملة",
            "🏥 الصحة" to "الخدمات والمنتجات الصحية ضمن الضوابط",
            "⚽ الرياضة" to "المنتجات والخدمات الرياضية",
            "💧 الكهرباء والمياه" to "مجالات البنية الأساسية",
            "🎓 التعليم" to "الخدمات والمنتجات التعليمية",
            "✈️ السفر" to "السفر والخدمات المرتبطة به"
        )

        categories.forEach { item ->
            addCard(
                item.first,
                item.second
            ) {
                showCategory(
                    item.first.replace(
                        Regex("^[^ ]+ "),
                        ""
                    )
                )
            }
        }

        addNavButton("↩ الرئيسية") {
            showHome()
        }
    }

    // =========================
    // CATEGORY
    // =========================

    private fun showCategory(category: String) {

        baseLayout(category)

        addSection(category)

        addStatusCard(
            "🛒 المجال",
            "المجال متاح ضمن البنية العامة للمنصة.",
            blue
        )

        addInfo(
            "المنتجات والخدمات",
            "يمكن لاحقًا ربط هذا المجال بقاعدة المنتجات والخدمات والعروض عند اكتمال طبقة البيانات."
        )

        addInfo(
            "التجارة",
            "أي عملية تجارية يجب أن تتوافق مع القوانين والأنظمة المعمول بها في المنطقة المعنية."
        )

        addCard(
            "📋 التفاصيل",
            "معلومات المجال"
        ) {
            showDetails(category)
        }

        addCard(
            "🔎 البحث",
            "البحث داخل المجالات"
        ) {
            showSearch()
        }

        addCard(
            "🚚 الشحن والجمارك",
            "المعلومات الإقليمية والتجارية"
        ) {
            showCustoms()
        }

        addNavButton("↩ المنتجات والأسواق") {
            showProducts()
        }
    }

    // =========================
    // DETAILS
    // =========================

    private fun showDetails(title: String) {

        baseLayout("تفاصيل")

        addSection(title)

        addInfo(
            "المعلومات",
            "هذا القسم مخصص لعرض التفاصيل والمعلومات المتعلقة بالمجال المختار."
        )

        addInfo(
            "الشحن والتوصيل",
            "تختلف خيارات الشحن والتوصيل حسب البلد والمنطقة ومقدم الخدمة."
        )

        addInfo(
            "الرسوم والجمارك",
            "أي تقديرات فعلية يجب أن تعتمد على بيانات موثوقة وقواعد الدولة المعنية."
        )

        addInfo(
            "الاعتماد",
            "المحتوى المعروض هنا لا يمثل موافقة حكومية أو قانونية تلقائية."
        )

        addNavButton("↩ المنتجات والأسواق") {
            showProducts()
        }
    }

    // =========================
    // SEARCH
    // =========================

    private fun showSearch() {

        baseLayout("البحث")

        addSection("البحث في CENTRAL MARKET")

        addInfo(
            "البحث",
            "واجهة البحث الأساسية. يمكن توسيعها لاحقًا بقاعدة بيانات وفلاتر متقدمة."
        )

        addCard(
            "🚗 المركبات",
            "المركبات والشاحنات"
        ) {
            showCategory("المركبات والشاحنات")
        }

        addCard(
            "📱 الإلكترونيات",
            "الهواتف والإلكترونيات"
        ) {
            showCategory("الهواتف والإلكترونيات")
        }

        addCard(
            "🍽️ المطاعم",
            "المطاعم والتوصيل"
        ) {
            showCategory("المطاعم والتوصيل")
        }

        addCard(
            "🧰 الخدمات",
            "الخدمات العامة"
        ) {
            showServices()
        }

        addNavButton("↩ الرئيسية") {
            showHome()
        }
    }

    // =========================
    // POINTS
    // =========================

    private fun showPoints() {

        baseLayout("النقاط")

        addSection("نظام النقاط")

        addStatusCard(
            "⭐ المشاركة",
            "نظام قابل للتطوير للمشاركة والتفاعل داخل المنصة.",
            gold
        )

        addInfo(
            "المبدأ",
            "النقاط لا تعني تلقائيًا أموالًا أو أرباحًا مالية."
        )

        addInfo(
            "الضوابط",
            "أي تحويل للنقاط إلى قيمة مالية فعلية يحتاج نظامًا مستقلًا وضوابط قانونية ومحاسبية."
        )

        addCard(
            "📊 تفاصيل النظام",
            "المعلومات المتاحة حاليًا"
        ) {
            showDetails("نظام النقاط")
        }

        addNavButton("↩ الرئيسية") {
            showHome()
        }
    }

    // =========================
    // HUMAN INTELLIGENCE
    // =========================

    private fun showHumanIntelligence() {

        baseLayout("الذكاء البشري")

        addSection("الذكاء البشري")

        addStatusCard(
            "🧠 معرفة + خبرة + مشاركة",
            "مساحة تعتمد على المعرفة والخبرة والمشاركة البشرية.",
            blue
        )

        addInfo(
            "الزكاة البشرية",
            "مفهوم للمساهمة بالوقت أو المعرفة أو الخبرة لخدمة الآخرين وفق الضوابط التي يعتمدها المشروع."
        )

        addInfo(
            "المراجعة",
            "المعلومات المهمة تحتاج إلى مراجعة قبل اعتمادها كمعلومة رسمية."
        )

        addInfo(
            "المسؤولية",
            "المشاركة البشرية لا تعني تلقائيًا اعتماد المعلومة أو صحتها."
        )

        addNavButton("↩ الرئيسية") {
            showHome()
        }
    }

    // =========================
    // CTM AI
    // =========================

    private fun showCtmAi() {

        baseLayout("CTM AI")

        addSection("المساعد الرسمي")

        addStatusCard(
            "🤖 CTM AI",
            "مساعد ذكي رسمي للمعلومات المسموح بها داخل التطبيق.",
            blue
        )

        addInfo(
            "الصلاحيات",
            "لا يملك المساعد صلاحية تنفيذ عمليات مالية أو تغيير إعدادات حساسة دون الصلاحيات والموافقات المطلوبة."
        )

        addInfo(
            "الصوت",
            "يمكن دعم البحث والقراءة الصوتية للمعلومات المسموح بها وفق إمكانات الجهاز."
        )

        addInfo(
            "المراقبة",
            "يمكن إيقاف أو تعطيل الوظائف عند وجود خطأ أو خطر وفق ضوابط الإدارة والمالك."
        )

        addCard(
            "🏗️ بنية CTM AI",
            "ملخص المعمارية"
        ) {
            showAboutArchitecture()
        }

        addNavButton("↩ الرئيسية") {
            showHome()
        }
    }

    // =========================
    // CHARITY
    // =========================

    private fun showCharity() {

        baseLayout("الخير والمساندة")

        addSection("المبادرات الإنسانية")

        addStatusCard(
            "❤️ الخير والمساندة",
            "قسم مخصص للمبادرات الإنسانية ودعم المحتاجين.",
            green
        )

        addInfo(
            "الأيتام والمحتاجون",
            "يمكن أن يدعم المشروع مبادرات إنسانية وفق الأنظمة والإجراءات المعمول بها."
        )

        addInfo(
            "الشفافية",
            "أي مساهمة مالية فعلية تحتاج إجراءات قانونية ومحاسبية مناسبة."
        )

        addInfo(
            "ملاحظة",
            "وجود القسم داخل التطبيق لا يعني وجود جهة خيرية حكومية أو حساب مالي فعلي ما لم يتم اعتماد ذلك رسميًا."
        )

        addNavButton("↩ الرئيسية") {
            showHome()
        }
    }

    // =========================
    // SERVICES
    // =========================

    private fun showServices() {

        baseLayout("الخدمات")

        addSection("الخدمات والمجالات")

        val services = listOf(
            "🚚 النقل والشحن",
            "🔧 الخدمات المهنية",
            "🌾 الزراعة والثروة الحيوانية",
            "🏗️ البناء والجملة",
            "🏥 الصحة",
            "⚡ الكهرباء والمياه",
            "🎓 التعليم",
            "✈️ السفر"
        )

        services.forEach { item ->
            addCard(
                item,
                "عرض المجال والخدمات المتاحة"
            ) {
                showDetails(item)
            }
        }

        addNavButton("↩ الرئيسية") {
            showHome()
        }
    }

    // =========================
    // ADS
    // =========================

    private fun showAds() {

        baseLayout("الإعلانات")

        addSection("نظام الإعلانات")

        addStatusCard(
            "📢 مستويات الإعلان",
            "BRONZE • SILVER • GOLD",
            gold
        )

        addInfo(
            "BRONZE",
            "مستوى إعلاني أساسي."
        )

        addInfo(
            "SILVER",
            "مستوى إعلاني موسع."
        )

        addInfo(
            "GOLD",
            "مستوى إعلاني مميز."
        )

        addCard(
            "➕ إضافة إعلان",
            "إدخال معلومات الإعلان"
        ) {
            showAddAd()
        }

        addNavButton("↩ الرئيسية") {
            showHome()
        }
    }

    private fun showAddAd() {

        baseLayout("إضافة إعلان")

        addSection("بيانات الإعلان")

        addInfo(
            "الإعلان",
            "هذه واجهة لمسار إضافة الإعلان. التنفيذ التجاري الفعلي يحتاج حسابات ومراجعة وصلاحيات."
        )

        addInfo(
            "المستويات",
            "BRONZE / SILVER / GOLD"
        )

        addInfo(
            "المراجعة",
            "يجب مراجعة الإعلان قبل نشره وفق قواعد المنصة."
        )

        addNavButton("↩ الإعلانات") {
            showAds()
        }
    }

    // =========================
    // KITCHEN
    // =========================

    private fun showKitchen() {

        baseLayout("المطبخ")

        addSection("المطبخ والطلبات")

        addStatusCard(
            "🍳 المطبخ",
            "قسم للمنتجات والخدمات والطلبات المرتبطة بالمطبخ.",
            orange
        )

        addInfo(
            "الخدمات",
            "يمكن ربط القسم بالمطاعم والطلبات والتوصيل حسب المنطقة."
        )

        addInfo(
            "التوصيل",
            "تختلف إمكانية التوصيل حسب المنطقة ومقدم الخدمة."
        )

        addNavButton("↩ الرئيسية") {
            showHome()
        }
    }

    // =========================
    // PRIVACY CENTER
    // =========================

    private fun showPrivacyCenter() {

        baseLayout("مركز الخصوصية")

        addSection("الخصوصية")

        addInfo(
            "حماية المعلومات",
            "يجب التعامل مع المعلومات وفق الغرض المعلن والصلاحيات المناسبة."
        )

        addInfo(
            "الوصول",
            "لا ينبغي إتاحة البيانات الخاصة إلا للمستخدم أو الجهة التي تملك الصلاحية."
        )

        addInfo(
            "الحماية التقنية",
            "يستخدم التطبيق طبقات حماية مناسبة، ومنها FLAG_SECURE."
        )

        addNavButton("↩ الرئيسية") {
            showHome()
        }
    }

    // =========================
    // DATA POLICY
    // =========================

    private fun showDataPolicy() {

        baseLayout("سياسة البيانات")

        addSection("البيانات")

        addInfo(
            "المبدأ",
            "تجمع البيانات وتستخدم وفق الوظيفة والغرض والصلاحيات المسموح بها."
        )

        addInfo(
            "المستخدم",
            "يجب توضيح البيانات المطلوبة وأسباب استخدامها عندما تصبح الوظيفة الفعلية جاهزة."
        )

        addInfo(
            "الأمان",
            "البيانات الحساسة تحتاج حماية إضافية وإجراءات وصول مناسبة."
        )

        addNavButton("↩ الرئيسية") {
            showHome()
        }
    }

    // =========================
    // APP RULES
    // =========================

    private fun showAppRules() {

        baseLayout("قواعد التطبيق")

        addSection("القواعد العامة")

        addInfo(
            "التجارة القانونية",
            "يجب أن تكون المنتجات والخدمات والأنشطة متوافقة مع القوانين المحلية."
        )

        addInfo(
            "منع الاحتيال",
            "يحظر استخدام المنصة لخداع المستخدمين أو تقديم معلومات مضللة."
        )

        addInfo(
            "المسؤولية",
            "يجب مراجعة المتطلبات القانونية والتنظيمية قبل إطلاق أي خدمة فعلية."
        )

        addNavButton("↩ الرئيسية") {
            showHome()
        }
    }

        // =========================
    // SENSITIVE ACCESS POLICY
    // =========================

    private fun showSensitiveAccessPolicy() {

        baseLayout("الخدمات الحساسة")

        addSection("حماية المشاركة الحساسة")

        addStatusCard(
            "🔒 حماية الشاشة",
            "FLAG_SECURE مفعّل على Activity لمنع التقاط الشاشة وتسجيلها عبر آليات Android المدعومة.",
            green
        )

        addStatusCard(
            "🏛️ التحقق الرسمي",
            "التكامل الحكومي غير متصل فعليًا في هذه النسخة. التنفيذ الفعلي يتطلب جهة مخولة وتفويضًا نظاميًا.",
            orange
        )

        addInfo(
            "الموانع القانونية",
            "عند تنفيذ الخدمة فعليًا، يجب التحقق من الموانع القانونية الموثقة ذات الصلة وفق القانون والجهة المختصة."
        )

        addInfo(
            "الموافقة",
            "لا يتم تنفيذ مشاركة مالية فعلية من هذه الواجهة وحدها."
        )

        addCard(
            "📋 متطلبات الأهلية",
            "المتطلبات العامة للتحقق"
        ) {
            showEligibilityRequirements()
        }

        addCard(
            "💼 المشاركة الاستثمارية",
            "ضوابط المشاركة الاستثمارية"
        ) {
            showInvestmentParticipation()
        }

        addCard(
            "🏗️ المشاركة التمويلية",
            "ضوابط المشاركة في المشاريع"
        ) {
            showFinancingParticipation()
        }

        addNavButton("↩ الرئيسية") {
            showHome()
        }
    }

    // =========================
    // SECURITY CHECKLIST
    // =========================

    private fun showSecurityChecklist() {

        baseLayout("قائمة الحماية")

        addSection("مراجعة الأمان")

        addStatusCard(
            "1 — حماية الشاشة",
            "FLAG_SECURE مفعّل على مستوى Activity.",
            green
        )

        addStatusCard(
            "2 — قفل الجلسة",
            "قفل تلقائي بعد 10 دقائق خارج التطبيق.",
            green
        )

        addStatusCard(
            "3 — الوصول الحساس",
            "مسارات مستقلة للمشاركة الاستثمارية والتمويلية.",
            green
        )

        addStatusCard(
            "4 — التحقق القانوني",
            "أي تحقق رسمي فعلي يحتاج جهة مخولة وتفويضًا قانونيًا مناسبًا.",
            orange
        )

        addStatusCard(
            "5 — العمليات المالية",
            "لا توجد حركة أموال حقيقية بمجرد عرض هذه الواجهات.",
            blue
        )

        addNavButton("↩ الأمان") {
            showSafety()
        }
    }

    // =========================
    // SECURITY OFFICE
    // =========================

    private fun showSecurityOffice() {

        baseLayout("مكتب الأمن والمعلومات")

        addSection("الأمن والمعلومات")

        addInfo(
            "الدور",
            "مراجعة مبادئ حماية المعلومات والوصول والأنظمة الحساسة."
        )

        addInfo(
            "الصلاحيات",
            "الوصول الإداري يخضع للصلاحيات المعتمدة."
        )

        addInfo(
            "التكامل الحكومي",
            "لا يوجد في هذه النسخة ربط فعلي بقواعد بيانات حكومية أو أمنية."
        )

        addNavButton("↩ الأمان") {
            showSafety()
        }
    }

    // =========================
    // BADGER
    // =========================

    private fun showBadger() {

        baseLayout("BADGER")

        addSection("BADGER")

        addStatusCard(
            "🦡 BADGER",
            "منتج مالي مستقل قابل للتطوير والتقديم للمؤسسات والبنوك.",
            blue
        )

        addInfo(
            "الهوية",
            "تصور الهوية يعتمد على الغرير والعالم مع طابع مؤسسي."
        )

        addInfo(
            "الإيصالات",
            "تصور الهوية يتضمن الإيصالات الزرقاء والذهبية والسوداء."
        )

        addInfo(
            "القفل",
            "الأقسام الحساسة والمدفوعة تحتاج إلى قفل أمني مناسب."
        )

        addInfo(
            "التنفيذ المالي",
            "لا يتم تنفيذ عملية مالية حقيقية من هذه الواجهة دون نظام مالي مرخص وموافقات وإجراءات فعلية."
        )

        addCard(
            "🏦 مكتب BADGER",
            "البنية والمبادئ"
        ) {
            showBadgerOffice()
        }

        addCard(
            "💼 المشاركة الاستثمارية",
            "ضوابط المشاركة"
        ) {
            showInvestmentParticipation()
        }

        addCard(
            "🏗️ المشاركة التمويلية",
            "ضوابط المشاريع"
        ) {
            showFinancingParticipation()
        }

        addCard(
            "💳 النظام المالي الخاص",
            "البنية المالية الداخلية"
        ) {
            showPrivateFinancialSystem()
        }

        addNavButton("↩ الرئيسية") {
            showHome()
        }
    }

    // =========================
    // BADGER OFFICE
    // =========================

    private fun showBadgerOffice() {

        baseLayout("مكتب BADGER")

        addSection("BADGER")

        addInfo(
            "المنتج",
            "نظام مستقل يمكن تطويره كمنتج تقني ومالي منفصل."
        )

        addInfo(
            "المؤسسات والبنوك",
            "التقديم الفعلي يحتاج عقودًا ومتطلبات قانونية وتقنية خاصة بالجهة المستلمة."
        )

        addInfo(
            "العمولات",
            "العمولات والشروط يجب أن تكون موثقة تعاقديًا عند التشغيل الفعلي."
        )

        addInfo(
            "الحسابات المحاكاة",
            "يمكن استخدام حسابات محاكاة لعرض وظائف النظام دون اعتبارها حسابات مالية حقيقية."
        )

        addInfo(
            "القفل",
            "الأقسام الحساسة والمدفوعة تخضع لقفل أمني مناسب."
        )

        addNavButton("↩ BADGER") {
            showBadger()
        }
    }

    // =========================
    // PRIVATE FINANCIAL SYSTEM
    // =========================

    private fun showPrivateFinancialSystem() {

        baseLayout("النظام المالي الخاص")

        addSection("النظام المالي")

        addStatusCard(
            "💳 نظام مستقل",
            "هذه واجهة بنيوية وليست نظامًا مصرفيًا أو ماليًا متصلًا فعليًا.",
            blue
        )

        addInfo(
            "المبدأ",
            "يحتاج النظام المالي المستقل إلى تصميم قانوني وتقني ومالي منفصل قبل التشغيل."
        )

        addInfo(
            "التنفيذ",
            "لا توجد حركة أموال حقيقية بمجرد وجود هذه الواجهة."
        )

        addCard(
            "💼 الاستثمار",
            "المشاركة الاستثمارية"
        ) {
            showInvestmentParticipation()
        }

        addCard(
            "🏗️ التمويل",
            "المشاركة في المشاريع"
        ) {
            showFinancingParticipation()
        }

        addNavButton("↩ BADGER") {
            showBadger()
        }
    }

    // =========================
    // INVESTMENT
    // =========================

    private fun showInvestmentParticipation() {

        baseLayout("المشاركة الاستثمارية")

        addSection("حماية المشاركة")

        addStatusCard(
            "🔒 شاشة حساسة",
            "الحماية من التقاط الشاشة مفعلة عبر FLAG_SECURE على Activity.",
            green
        )

        addStatusCard(
            "🏛️ التحقق الرسمي",
            "لا يوجد ربط حكومي فعلي في هذه النسخة.",
            orange
        )

        addInfo(
            "الأهلية",
            "يجب التحقق من الأهلية عبر الجهات والمصادر الحكومية المخولة وبالتفويضات والإجراءات النظامية."
        )

        addInfo(
            "الموانع القانونية",
            "عند التشغيل الفعلي، يجب التحقق من الموانع القانونية الموثقة ذات الصلة وفق القانون والجهة المختصة."
        )

        addInfo(
            "الموافقة",
            "لا يتم تنفيذ مشاركة مالية فعلية من هذه الواجهة."
        )

        addCard(
            "📋 متطلبات الاستثمار",
            "عرض المتطلبات"
        ) {
            showInvestmentRequirements()
        }

        addCard(
            "🛡️ التحقق الرسمي",
            "عرض مسار التحقق"
        ) {
            showEligibilityVerification()
        }

        addCard(
            "👑 موافقة المالك",
            "ضوابط الاعتماد"
        ) {
            showOwnerApproval()
        }

        addNavButton("↩ الخدمات الحساسة") {
            showSensitiveAccessPolicy()
        }
    }

    // =========================
    // FINANCING
    // =========================

    private fun showFinancingParticipation() {

        baseLayout("المشاركة التمويلية")

        addSection("المشاريع التمويلية")

        addStatusCard(
            "🔒 حماية المشاركة",
            "المعلومات الحساسة محمية، ولا تسمح الواجهة وحدها بعملية مالية فعلية.",
            green
        )

        addStatusCard(
            "🏛️ التحقق",
            "التنفيذ الرسمي يحتاج مصادر وجهات مخولة وإجراءات قانونية.",
            orange
        )

        addInfo(
            "الأهلية",
            "تتطلب المشاركة الفعلية إجراءات تحقق قانونية وتنظيمية مناسبة."
        )

        addInfo(
            "الموافقة",
            "لا يتم تنفيذ التمويل أو تحويل الأموال من هذه الواجهة."
        )

        addCard(
            "📋 متطلبات التمويل",
            "عرض المتطلبات"
        ) {
            showFinancingRequirements()
        }

        addCard(
            "🛡️ التحقق",
            "التحقق من الأهلية"
        ) {
            showEligibilityVerification()
        }

        addNavButton("↩ الخدمات الحساسة") {
            showSensitiveAccessPolicy()
        }
    }

    // =========================
    // ELIGIBILITY VERIFICATION
    // =========================

    private fun showEligibilityVerification() {

        baseLayout("التحقق من الأهلية")

        addSection("التحقق الرسمي")

        addStatusCard(
            "🏛️ حالة التكامل الحكومي",
            "غير متصل فعليًا في هذه النسخة.",
            orange
        )

        addInfo(
            "المصدر",
            "أي تحقق رسمي يجب أن يتم عبر مصدر حكومي مخول أو جهة معتمدة قانونيًا."
        )

        addInfo(
            "التفويض",
            "لا يجوز افتراض وجود صلاحية للوصول إلى قواعد بيانات حكومية دون تفويض وتنفيذ رسمي."
        )

        addInfo(
            "الخصوصية",
            "تستخدم المعلومات فقط ضمن الغرض القانوني المحدد وبالقدر اللازم."
        )

        addInfo(
            "الموانع",
            "أي مانع قانوني يجب أن يكون موثقًا ويُراجع وفق القانون والجهة المختصة."
        )

        addNavButton("↩ الخدمات الحساسة") {
            showSensitiveAccessPolicy()
        }
    }

    // =========================
    // ELIGIBILITY REQUIREMENTS
    // =========================

    private fun showEligibilityRequirements() {

        baseLayout("متطلبات الأهلية")

        addSection("المتطلبات")

        addInfo(
            "1",
            "هوية وبيانات صحيحة وفق متطلبات الخدمة الفعلية."
        )

        addInfo(
            "2",
            "موافقة المستخدم والإجراءات القانونية اللازمة."
        )

        addInfo(
            "3",
            "تحقق رسمي من المصادر المخولة عند الحاجة."
        )

        addInfo(
            "4",
            "مراجعة الموانع القانونية الموثقة وفق القانون والجهة المختصة."
        )

        addInfo(
            "5",
            "حماية البيانات وعدم استخدامها خارج الغرض المحدد."
        )

        addNavButton("↩ التحقق") {
            showEligibilityVerification()
        }
    }

    // =========================
    // INVESTMENT REQUIREMENTS
    // =========================

    private fun showInvestmentRequirements() {

        baseLayout("متطلبات الاستثمار")

        addSection("المشاركة الاستثمارية")

        addInfo(
            "المتطلبات",
            "الهوية، الأهلية، الإفصاحات، الموافقات، والضوابط القانونية والتنظيمية المطلوبة للخدمة الفعلية."
        )

        addInfo(
            "التحقق",
            "التحقق الرسمي لا ينفذ من هذه الواجهة حاليًا."
        )

        addNavButton("↩ الاستثمار") {
            showInvestmentParticipation()
        }
    }

    // =========================
    // FINANCING REQUIREMENTS
    // =========================

    private fun showFinancingRequirements() {

        baseLayout("متطلبات التمويل")

        addSection("المشاركة التمويلية")

        addInfo(
            "المتطلبات",
            "الهوية، الأهلية، بيانات المشروع، التحقق، الإفصاحات، والموافقات اللازمة."
        )

        addInfo(
            "التنفيذ",
            "لا يوجد تنفيذ مالي حقيقي في هذه النسخة."
        )

        addNavButton("↩ التمويل") {
            showFinancingParticipation()
        }
    }

        // =========================
    // OWNER APPROVAL
    // =========================

    private fun showOwnerApproval() {

        baseLayout("اعتماد المالك")

        addSection("الموافقة والاعتماد")

        addStatusCard(
            "👑 اعتماد المالك",
            "التغييرات الحساسة والإصدارات المهمة تحتاج مراجعة واعتمادًا قبل الاستبدال.",
            gold
        )

        addInfo(
            "مبدأ الاعتماد",
            "لا يعني وجود هذه الشاشة أن أي عملية مالية أو قانونية قد تمت الموافقة عليها فعليًا."
        )

        addInfo(
            "الاختبار",
            "يجب اختبار النسخة قبل اعتمادها كنسخة تشغيلية."
        )

        addInfo(
            "لا تنفيذ مالي",
            "هذه الواجهة لا تنفذ تحويلات أو استثمارات مالية حقيقية."
        )

        addNavButton("↩ الاستثمار") {
            showInvestmentParticipation()
        }
    }

    // =========================
    // ATTORNEY OFFICE
    // =========================

    private fun showAttorneyOffice() {

        baseLayout("مكتب النائب العام")

        addSection("المكتب القانوني")

        addStatusCard(
            "⚖️ مكتب معلوماتي",
            "وجود هذه الواجهة لا يعني وجود تكامل حكومي أو صلاحية حكومية فعلية.",
            orange
        )

        addInfo(
            "الدور",
            "واجهة معلوماتية لمبادئ المتابعة القانونية داخل بنية المشروع."
        )

        addInfo(
            "الصلاحيات",
            "لا توجد في هذه النسخة صلاحية حكومية فعلية أو وصول إلى قواعد بيانات رسمية."
        )

        addInfo(
            "الإدارة",
            "يمكن للمدير الاطلاع على المعلومات المسموح بها دون تعديل الصلاحيات القانونية."
        )

        addNavButton("↩ مكتب المدير") {
            showManagerOffice()
        }
    }

    // =========================
    // MANAGER OFFICE
    // =========================

    private fun showManagerOffice() {

        baseLayout("مكتب المدير")

        addSection("الإدارة")

        addStatusCard(
            "🏢 مكتب المدير",
            "مساحة إدارية للمراجعة والمتابعة وفق الصلاحيات.",
            blue
        )

        addCard(
            "🧰 أدوات المدير",
            "الأدوات الإدارية"
        ) {
            showManagerTools()
        }

        addCard(
            "🔍 التشخيص",
            "فحص حالة التطبيق"
        ) {
            showDiagnostics()
        }

        addCard(
            "📱 قدرات الجهاز",
            "الجهاز والاتصال"
        ) {
            showDeviceCapabilities()
        }

        addCard(
            "📄 المستندات",
            "الوثائق والسياسات"
        ) {
            showDocuments()
        }

        addCard(
            "⚖️ مكتب النائب العام",
            "المعلومات والصلاحيات"
        ) {
            showAttorneyOffice()
        }

        addNavButton("↩ الرئيسية") {
            showHome()
        }
    }

    // =========================
    // MANAGER TOOLS
    // =========================

    private fun showManagerTools() {

        baseLayout("أدوات المدير")

        addSection("الإدارة والمراجعة")

        addStatusCard(
            "🔐 حالة الحماية الحساسة",
            "حماية الشاشة وقفل الجلسة مفعّلان، أما التحقق الحكومي والمالي الفعلي فيحتاج تكاملات رسمية لاحقة.",
            green
        )

        addCard(
            "🔍 التشخيص",
            "حالة التطبيق"
        ) {
            showDiagnostics()
        }

        addCard(
            "📱 قدرات الجهاز",
            "الجهاز والاتصال"
        ) {
            showDeviceCapabilities()
        }

        addCard(
            "📄 المستندات",
            "الوثائق والسياسات"
        ) {
            showDocuments()
        }

        addCard(
            "🌍 حزمة الدولة",
            "البنية العالمية وحزم الدول"
        ) {
            showCountryPackage()
        }

        addCard(
            "⚖️ القواعد الإقليمية",
            "القوانين حسب المنطقة"
        ) {
            showRegionalRules()
        }

        addCard(
            "🏛️ حالة التكاملات",
            "الحكومي والمالي والاعتماد"
        ) {
            showSystemSummary()
        }

        addCard(
            "👑 تحكم المالك",
            "الصلاحيات والاعتماد"
        ) {
            showOwnerControls()
        }

        addNavButton("↩ مكتب المدير") {
            showManagerOffice()
        }
    }

    // =========================
    // OWNER CONTROLS
    // =========================

    private fun showOwnerControls() {

        baseLayout("تحكم المالك")

        addSection("صلاحيات المالك")

        addInfo(
            "المبدأ",
            "التغييرات الحساسة والإصدارات المهمة تحتاج مراجعة واعتمادًا قبل الاستبدال."
        )

        addInfo(
            "الاختبار",
            "يجب اختبار النسخة قبل اعتمادها كنسخة تشغيلية."
        )

        addInfo(
            "الحماية",
            "لا تظهر أدوات المالك للمستخدم العادي ضمن مسار الاستخدام العام."
        )

        addStatusCard(
            "🔒 العمليات الحساسة",
            "لا يتم تنفيذ عملية مالية حقيقية من هذه الواجهة.",
            green
        )

        addNavButton("↩ أدوات المدير") {
            showManagerTools()
        }
    }

    // =========================
    // DIAGNOSTICS
    // =========================

    private fun showDiagnostics() {

        baseLayout("التشخيص")

        addSection("تشخيص التطبيق")

        addStatusCard(
            "🟢 الواجهة",
            "Activity واحدة مع بناء برمجي مباشر.",
            green
        )

        addStatusCard(
            "🔒 الحماية",
            "FLAG_SECURE وقفل الجلسة مفعّلان.",
            green
        )

        addStatusCard(
            "🌐 الاتصال",
            "يمكن فحص الشبكة من قسم حالة الاتصال.",
            blue
        )

        addInfo(
            "الحالة",
            "هذه النسخة مصممة لتكون قابلة للبناء والاختبار قبل إضافة التكاملات الخارجية."
        )

        addNavButton("↩ مكتب المدير") {
            showManagerOffice()
        }
    }

    // =========================
    // DEVICE CAPABILITIES
    // =========================

    private fun showDeviceCapabilities() {

        baseLayout("قدرات الجهاز")

        addSection("اكتشاف الجهاز")

        val manager =
            getSystemService(
                CONNECTIVITY_SERVICE
            ) as ConnectivityManager

        val network =
            manager.activeNetwork

        val capabilities =
            manager.getNetworkCapabilities(network)

        if (capabilities != null) {

            addStatusCard(
                "🟢 الشبكة",
                "يوجد اتصال شبكة متاح حاليًا.",
                green
            )

            val type =
                when {
                    capabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_WIFI
                    ) -> "Wi-Fi"

                    capabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_CELLULAR
                    ) -> "بيانات الهاتف"

                    capabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_ETHERNET
                    ) -> "Ethernet"

                    else -> "شبكة أخرى"
                }

            addInfo(
                "نوع الاتصال",
                type
            )

        } else {

            addStatusCard(
                "🔴 الشبكة",
                "لا يوجد اتصال شبكة حاليًا.",
                red
            )
        }

        addInfo(
            "التوافق",
            "يتم تصميم الخدمات بحيث تتكيف مع قدرات الجهاز والاتصال المتاح."
        )

        addNavButton("↩ مكتب المدير") {
            showManagerOffice()
        }
    }

    // =========================
    // DOCUMENTS
    // =========================

    private fun showDocuments() {

        baseLayout("المستندات")

        addSection("المستندات والسياسات")

        addInfo(
            "الوثائق",
            "مكان مخصص لعرض الوثائق والسياسات المعتمدة عند ربط النظام بالمحتوى الفعلي."
        )

        addInfo(
            "المراجعة",
            "المستندات القانونية والتنظيمية الفعلية يجب اعتمادها من الجهات المختصة."
        )

        addInfo(
            "الإصدار",
            "أي وثيقة رسمية تحتاج إدارة إصدارات وصلاحيات مناسبة."
        )

        addNavButton("↩ مكتب المدير") {
            showManagerOffice()
        }
    }

    // =========================
    // COUNTRY PACKAGE
    // =========================

    private fun showCountryPackage() {

        baseLayout("حزمة الدولة")

        addSection("Global First")

        addStatusCard(
            "🌍 قاعدة عالمية",
            "القاعدة الأساسية للمشروع عالمية وقابلة لإضافة حزم الدول.",
            blue
        )

        addInfo(
            "حزمة السودان",
            "يمكن أن تتضمن الوظائف والمؤسسات والمعلومات الخاصة بالسودان عند اعتمادها وتنفيذها."
        )

        addInfo(
            "الدول الأخرى",
            "تظهر الوظائف المناسبة للدولة والمنطقة والقانون المحلي."
        )

        addInfo(
            "الفروع التجارية",
            "يمكن بناء فروع قانونية وتجارية خاصة بالأسواق المختلفة وفق الأنظمة المحلية."
        )

        addNavButton("↩ النظام") {
            showSystemSummary()
        }
    }

    // =========================
    // REGIONAL RULES
    // =========================

    private fun showRegionalRules() {

        baseLayout("القواعد الإقليمية")

        addSection("المنطقة والدولة")

        addInfo(
            "القوانين",
            "القوانين والقيود والرسوم والشحن والتجارة تختلف حسب الدولة والمنطقة."
        )

        addInfo(
            "التنبيهات",
            "يمكن للنظام عرض تنبيه عندما تتطلب الخدمة مراجعة قواعد محلية."
        )

        addInfo(
            "المعلومات",
            "لا ينبغي اعتماد معلومة قانونية أو تنظيمية قبل التحقق من مصدر موثوق."
        )

        addCard(
            "🚚 الشحن والجمارك",
            "عرض مبادئ التجارة الدولية"
        ) {
            showCustoms()
        }

        addNavButton("↩ النظام") {
            showSystemSummary()
        }
    }

    // =========================
    // CUSTOMS
    // =========================

    private fun showCustoms() {

        baseLayout("الشحن والجمارك")

        addSection("التجارة الدولية")

        addInfo(
            "الجمارك",
            "تقديرات الشحن والجمارك تعتمد على البلد والمنطقة ونوع المنتج والأنظمة السارية."
        )

        addInfo(
            "الشحن",
            "خيارات الشحن تختلف حسب الدولة ومقدم الخدمة والمسار."
        )

        addInfo(
            "التنبيه الإقليمي",
            "يمكن لاحقًا إضافة تنبيهات حسب الدولة والمنطقة عند توفر البيانات الموثوقة."
        )

        addNavButton("↩ الخدمات") {
            showServices()
        }
    }

    // =========================
    // ARCHITECTURE
    // =========================

    private fun showAboutArchitecture() {

        baseLayout("بنية النظام")

        addSection("المعمارية")

        addInfo(
            "القاعدة",
            "CENTRAL MARKET قاعدة عالمية قابلة لإضافة حزم دول ومناطق."
        )

        addInfo(
            "الأمان",
            "الوظائف الحساسة منفصلة منطقيًا عن العرض العام."
        )

        addInfo(
            "التوسع",
            "يمكن إضافة خدمات ومؤسسات ومكاتب جديدة دون تغيير هوية المنصة الأساسية."
        )

        addInfo(
            "CTM AI",
            "المساعد الرسمي مصمم للمعلومات المسموح بها ولا ينفذ عمليات حساسة دون صلاحية."
        )

        addNavButton("↩ الرئيسية") {
            showHome()
        }
    }

    // =========================
    // PROJECT STATUS
    // =========================

    private fun showProjectStatus() {

        baseLayout("حالة المشروع")

        addSection("CENTRAL MARKET")

        addStatusCard(
            "🔧 قيد التطوير والمراجعة",
            "نسخة حقيقية من المشروع قيد البناء والاختبار.",
            blue
        )

        addInfo(
            "الهدف",
            "الوصول إلى APK قابل للبناء والاختبار مع الحفاظ على بنية المشروع ومواصفاته."
        )

        addInfo(
            "التكاملات",
            "التكاملات الحكومية والمالية الفعلية ليست متصلة في هذه النسخة."
        )

        addNavButton("↩ الرئيسية") {
            showHome()
        }
    }

    // =========================
    // SYSTEM SUMMARY
    // =========================

    private fun showSystemSummary() {

        baseLayout("ملخص النظام")

        addSection("CENTRAL MARKET")

        addInfo(
            "المنصة",
            "أسواق وخدمات وذكاء بشري وCTM AI ومكاتب إدارية وأنظمة خاصة."
        )

        addStatusCard(
            "🔒 الحماية",
            "حماية الشاشة وقفل الجلسة وضوابط إضافية للخدمات الحساسة.",
            green
        )

        addStatusCard(
            "🏛️ التكامل الحكومي",
            "غير متصل فعليًا. يحتاج مصدرًا رسميًا وتفويضًا وإجراءات قانونية.",
            orange
        )

        addStatusCard(
            "💳 التكامل المالي",
            "غير متصل فعليًا. الواجهات الحالية لا تنفذ حركة أموال.",
            orange
        )

        addStatusCard(
            "👑 الاعتماد",
            "التغييرات الحساسة تحتاج مراجعة واعتماد المالك قبل الاستبدال.",
            gold
        )

        addInfo(
            "BADGER",
            "منتج مستقل قابل للتطوير والتقديم للمؤسسات والبنوك."
        )

        addInfo(
            "الدول",
            "قاعدة عالمية مع حزم خاصة بالدول والمناطق."
        )

        addInfo(
            "الوضع الحالي",
            "هذه واجهة تطبيق حقيقية قابلة للبناء والاختبار، ولا تدعي وجود تكاملات حكومية أو مالية غير موجودة."
        )

        addNavButton("↩ أدوات المدير") {
            showManagerTools()
        }
    }

    // =========================
    // APP INFORMATION
    // =========================

    private fun showAppInformation() {

        baseLayout("معلومات التطبيق")

        addSection("CENTRAL MARKET")

        addInfo(
            "الهوية",
            "CENTRAL MARKET منصة متعددة المجالات قابلة للتوسع محليًا وعالميًا."
        )

        addInfo(
            "المالك",
            "المالك ياسر حسن وشركاؤه"
        )

        addInfo(
            "البنية",
            "التطبيق مصمم ليعمل على أجهزة Android المتوافقة مع الحد الأدنى المحدد للمشروع."
        )

        addInfo(
            "الحماية",
            "يتضمن حماية الشاشة وقفل الجلسة ومسارات حماية للخدمات الحساسة."
        )

        addInfo(
            "التوسع",
            "يمكن إضافة حزم دول ومناطق مختلفة مع الحفاظ على القاعدة العالمية للمشروع."
        )

        addInfo(
            "النسخة",
            "نسخة تطويرية حقيقية من المشروع وليست مجرد واجهة منفصلة."
        )

        addNavButton("↩ الرئيسية") {
            showHome()
        }
    }

    // =========================
    // HOME NAVIGATION
    // =========================

    private fun showHomeNavigation() {

        baseLayout("التنقل")

        addSection("أقسام التطبيق")

        addNavButton("⌂ الرئيسية") {
            showHome()
        }

        addNavButton("🔎 البحث") {
            showSearch()
        }

        addNavButton("⭐ النقاط") {
            showPoints()
        }

        addNavButton("🛒 المنتجات") {
            showProducts()
        }

        addNavButton("🧰 الخدمات") {
            showServices()
        }

        addNavButton("👤 وضع الضيف") {
            showGuestMode()
        }
    }
}
