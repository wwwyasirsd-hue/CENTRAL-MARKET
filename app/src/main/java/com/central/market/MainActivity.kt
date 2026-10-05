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

        // منع التقاط وتصوير وتسجيل الشاشة.
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
                SystemClock.elapsedRealtime() -
                    outsideAppStartedAt

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

        addInfo(
            "حماية تلقائية",
            "تم قفل الجلسة بعد مرور 10 دقائق خارج التطبيق."
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

        brand.setTypeface(
            null,
            Typeface.BOLD
        )

        brand.gravity = Gravity.CENTER

        header.addView(brand)

        val market = TextView(this)

        market.text = "MARKET"
        market.textSize = 14f
        market.setTextColor(gold2)

        market.setTypeface(
            null,
            Typeface.BOLD
        )

        market.gravity = Gravity.CENTER

        header.addView(market)

        val pageTitle = TextView(this)

        pageTitle.text = title
        pageTitle.textSize = 15f
        pageTitle.setTextColor(white)
        pageTitle.gravity = Gravity.CENTER
        pageTitle.setPadding(0, 6, 0, 0)

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

        text.setTypeface(
            null,
            Typeface.BOLD
        )

        text.setPadding(
            12,
            0,
            8,
            0
        )

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

        heading.setTypeface(
            null,
            Typeface.BOLD
        )

        box.addView(heading)

        val description = TextView(this)

        description.text = body
        description.textSize = 14f
        description.setTextColor(textDark)
        description.setPadding(
            0,
            7,
            0,
            0
        )

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

        button.setTypeface(
            null,
            Typeface.BOLD
        )

        button.gravity =
            Gravity.CENTER_VERTICAL

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

        button.setTypeface(
            null,
            Typeface.BOLD
        )

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

    private fun cardBackground():
        GradientDrawable {

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

    addInfo(
        "المالك",
        "المالك ياسر حسن وشركاؤه"
    )

    addInfo(
        "منصة واحدة .. عالم من الفرص.",
        "منصة متعددة الخدمات والأسواق، مصممة لتكون قابلة للتوسع محليًا وعالميًا."
    )

    addCard(
        "🌐 حالة الاتصال",
        "فحص الاتصال بالإنترنت وقدرات الشبكة"
    ) {
        showOnline()
    }

    addCard(
        "👤 وضع الضيف",
        "استكشاف الخدمات المتاحة دون الدخول إلى حساب"
    ) {
        showGuestMode()
    }

    addSection("الأسواق والخدمات")

    addCard(
        "🛒 المنتجات والأسواق",
        "مركبات، هواتف، إلكترونيات، مطاعم، وخدمات"
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
        "خدمات ومنتجات المطبخ والطلبات"
    ) {
        showKitchen()
    }

    addSection("المجتمع والذكاء")

    addCard(
        "⭐ النقاط",
        "نظام النقاط والمشاركة"
    ) {
        showPoints()
    }

    addCard(
        "🧠 الذكاء البشري",
        "المعرفة والمشاركة البشرية داخل المنصة"
    ) {
        showHumanIntelligence()
    }

    addCard(
        "🤖 CTM AI",
        "المساعد الرسمي للمعلومات المسموح بها داخل التطبيق"
    ) {
        showCtmAi()
    }

    addCard(
        "❤️ الخير والمساندة",
        "دعم الأيتام والمحتاجين والمبادرات الخيرية"
    ) {
        showCharity()
    }

    addSection("الأمان والخصوصية")

    addCard(
        "🛡️ مركز الأمان والخصوصية",
        "الحماية والخصوصية وقواعد استخدام البيانات"
    ) {
        showSafety()
    }

    addCard(
        "🔐 مركز الخصوصية",
        "إدارة مبادئ الخصوصية وحماية المستخدم"
    ) {
        showPrivacyCenter()
    }

    addCard(
        "📋 سياسة البيانات",
        "المعلومات المتعلقة بالبيانات واستخدامها"
    ) {
        showDataPolicy()
    }

    addCard(
        "⚖️ قواعد التطبيق",
        "القواعد العامة والضوابط"
    ) {
        showAppRules()
    }

    addSection("الإدارة والأنظمة الخاصة")

    addCard(
        "🏢 مكتب المدير",
        "أدوات الإدارة والمراجعة الداخلية"
    ) {
        showManagerOffice()
    }

    addCard(
        "🦡 BADGER",
        "نظام مالي مستقل قابل للتقديم للمؤسسات والبنوك"
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
        "البنية والنسخة والمبادئ الأساسية"
    ) {
        showAppInformation()
    }

    addSection("التنقل")

    addNavButton(
        "⌂ الرئيسية"
    ) {
        showHome()
    }

    addNavButton(
        "🔎 البحث"
    ) {
        showSearch()
    }

    addNavButton(
        "⭐ النقاط"
    ) {
        showPoints()
    }

    addNavButton(
        "👤 الحساب / الضيف"
    ) {
        showGuestMode()
    }
}

// =========================
// ONLINE STATUS
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
        manager.getNetworkCapabilities(
            network
        )

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

        addInfo(
            "🟢 متصل",
            "الاتصال متاح حاليًا عبر: $transport"
        )

    } else {

        addInfo(
            "🔴 غير متصل",
            "لا يوجد اتصال شبكة متاح حاليًا."
        )
    }

    addInfo(
        "ملاحظة",
        "يعتمد توفر بعض الخدمات على الاتصال والقدرات المتاحة في الجهاز."
    )

    addNavButton("↩ العودة للرئيسية") {
        showHome()
    }
}

// =========================
// GUEST MODE
// =========================

private fun showGuestMode() {

    baseLayout("وضع الضيف")

    addSection("استكشاف CENTRAL MARKET")

    addInfo(
        "وضع الضيف",
        "يمكن استكشاف أجزاء من المنصة دون تنفيذ عمليات مالية أو الوصول إلى البيانات الخاصة."
    )

    addCard(
        "🛍️ استكشاف المنتجات",
        "عرض المجالات والأسواق"
    ) {
        showProducts()
    }

    addCard(
        "🧰 استكشاف الخدمات",
        "عرض الخدمات العامة"
    ) {
        showServices()
    }

    addCard(
        "📢 الإعلانات",
        "استعراض نظام الإعلانات"
    ) {
        showAds()
    }

    addInfo(
        "الحماية",
        "الوصول إلى الوظائف الحساسة يخضع للصلاحيات والتحقق والضوابط المناسبة."
    )

    addNavButton("↩ العودة للرئيسية") {
        showHome()
    }
}

// =========================
// SAFETY
// =========================

private fun showSafety() {

    baseLayout("الأمان والخصوصية")

    addSection("الحماية")

    addInfo(
        "حماية الشاشة",
        "يستخدم التطبيق حماية FLAG_SECURE لمنع التقاط الشاشة وتسجيلها داخل التطبيق."
    )

    addInfo(
        "قفل الجلسة",
        "يتم قفل الجلسة تلقائيًا بعد مرور 10 دقائق خارج التطبيق."
    )

    addInfo(
        "الخدمات الحساسة",
        "المشاركة الاستثمارية والتمويلية تخضع لضوابط وصول وحماية إضافية."
    )

    addCard(
        "🔐 سياسة الوصول الحساس",
        "عرض ضوابط الخدمات الحساسة"
    ) {
        showSensitiveAccessPolicy()
    }

    addCard(
        "🛡️ قائمة الحماية",
        "مراجعة عناصر الأمان"
    ) {
        showSecurityChecklist()
    }

    addCard(
        "🏛️ مكتب الأمن والمعلومات",
        "المبادئ العامة للمراجعة الأمنية"
    ) {
        showSecurityOffice()
    }

    addNavButton("↩ العودة للرئيسية") {
        showHome()
    }
}

// =========================
// PRODUCTS
// =========================

private fun showProducts() {

    baseLayout("المنتجات والأسواق")

    addSection("المجالات")

    addCard(
        "🚗 المركبات والشاحنات",
        "بيع وشراء وخدمات مرتبطة بالمركبات"
    ) {
        showCategory("المركبات والشاحنات")
    }

    addCard(
        "📱 الهواتف والإلكترونيات",
        "أجهزة وتقنيات ومنتجات إلكترونية"
    ) {
        showCategory("الهواتف والإلكترونيات")
    }

    addCard(
        "🍽️ المطاعم والتوصيل",
        "مطاعم وطلبات وخدمات توصيل"
    ) {
        showCategory("المطاعم والتوصيل")
    }

    addCard(
        "🌾 الزراعة والثروة الحيوانية",
        "منتجات وخدمات القطاع الزراعي والحيواني"
    ) {
        showCategory("الزراعة والثروة الحيوانية")
    }

    addCard(
        "🐟 الأسماك",
        "منتجات وخدمات مرتبطة بالأسماك"
    ) {
        showCategory("الأسماك")
    }

    addCard(
        "🏗️ البناء والجملة",
        "مواد البناء والبيع بالجملة"
    ) {
        showCategory("البناء والجملة")
    }

    addCard(
        "🏥 الصحة",
        "خدمات ومنتجات صحية ضمن الضوابط"
    ) {
        showCategory("الصحة")
    }

    addCard(
        "⚽ الرياضة",
        "منتجات وخدمات رياضية"
    ) {
        showCategory("الرياضة")
    }

    addCard(
        "💧 الكهرباء والمياه",
        "خدمات ومجالات البنية الأساسية"
    ) {
        showCategory("الكهرباء والمياه")
    }

    addCard(
        "🎓 التعليم",
        "خدمات ومنتجات تعليمية"
    ) {
        showCategory("التعليم")
    }

    addCard(
        "✈️ السفر",
        "السفر والخدمات المرتبطة به"
    ) {
        showCategory("السفر")
    }

    addNavButton("↩ العودة للرئيسية") {
        showHome()
    }
}

// =========================
// CATEGORY
// =========================

private fun showCategory(category: String) {

    baseLayout(category)

    addSection(category)

    addInfo(
        "متاح داخل CENTRAL MARKET",
        "يمكن استخدام هذا القسم لاستعراض المنتجات والخدمات والعروض المرتبطة بالمجال."
    )

    addCard(
        "📋 التفاصيل",
        "عرض معلومات المجال"
    ) {
        showDetails(category)
    }

    addCard(
        "🔎 البحث داخل المجال",
        "البحث عن المنتجات والخدمات"
    ) {
        showSearch()
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
        "التجارة",
        "أي عملية تجارية يجب أن تتم وفق القوانين والأنظمة المعمول بها في المنطقة المعنية."
    )

    addInfo(
        "الشحن والتوصيل",
        "قد تختلف خيارات الشحن والتوصيل حسب البلد والمنطقة وتوفر الخدمة."
    )

    addNavButton("↩ العودة") {
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
        "يمكن استخدام البحث للوصول إلى المنتجات والخدمات والمجالات المتاحة."
    )

    addCard(
        "🚗 المركبات",
        "البحث في المركبات والشاحنات"
    ) {
        showCategory("المركبات والشاحنات")
    }

    addCard(
        "📱 الإلكترونيات",
        "البحث في الهواتف والإلكترونيات"
    ) {
        showCategory("الهواتف والإلكترونيات")
    }

    addCard(
        "🍽️ المطاعم",
        "البحث في المطاعم والتوصيل"
    ) {
        showCategory("المطاعم والتوصيل")
    }

    addCard(
        "🧰 الخدمات",
        "البحث في الخدمات"
    ) {
        showServices()
    }

    addNavButton("↩ العودة للرئيسية") {
        showHome()
    }
}

// =========================
// POINTS
// =========================

private fun showPoints() {

    baseLayout("النقاط")

    addSection("نظام النقاط")

    addInfo(
        "النقاط",
        "نظام للمشاركة والتفاعل داخل المنصة، ويمكن تطويره وفق قواعد المشروع."
    )

    addInfo(
        "المبدأ",
        "النقاط لا تعني تلقائيًا أموالًا أو أرباحًا مالية، وأي نظام مالي يحتاج إلى ضوابط مستقلة."
    )

    addCard(
        "📊 حالة النقاط",
        "عرض المعلومات المتاحة"
    ) {
        showDetails("النقاط")
    }

    addNavButton("↩ العودة للرئيسية") {
        showHome()
    }
}

// =========================
// HUMAN INTELLIGENCE
// =========================

private fun showHumanIntelligence() {

    baseLayout("الذكاء البشري")

    addSection("الذكاء البشري")

    addInfo(
        "الفكرة",
        "مساحة تعتمد على المعرفة والخبرة والمشاركة البشرية في بناء المعلومات والخدمات."
    )

    addInfo(
        "الزكاة البشرية",
        "مفهوم للمساهمة بالوقت أو المعرفة أو الخبرة لخدمة الآخرين، وفق الضوابط التي يعتمدها المشروع."
    )

    addInfo(
        "المراجعة",
        "المعلومات المهمة تحتاج إلى مراجعة قبل اعتمادها كمعلومة رسمية داخل المنصة."
    )

    addNavButton("↩ العودة للرئيسية") {
        showHome()
    }
}

// =========================
// CTM AI
// =========================

private fun showCtmAi() {

    baseLayout("CTM AI")

    addSection("المساعد الرسمي")

    addInfo(
        "CTM AI",
        "مساعد ذكي رسمي داخل CENTRAL MARKET للمساعدة في الوصول إلى المعلومات المسموح بها داخل التطبيق."
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
        "يمكن إيقاف أو تعطيل وظائف المساعد عند وجود خطأ أو خطر، وفق ضوابط الإدارة والمالك."
    )

    addCard(
        "🔍 معلومات النظام",
        "عرض ملخص بنية CTM AI"
    ) {
        showAboutArchitecture()
    }

    addNavButton("↩ العودة للرئيسية") {
        showHome()
    }
}

// =========================
// CHARITY
// =========================

private fun showCharity() {

    baseLayout("الخير والمساندة")

    addSection("المبادرات الخيرية")

    addInfo(
        "الأيتام والمحتاجون",
        "قسم مخصص لدعم المبادرات الإنسانية ومساعدة الفئات المحتاجة وفق الأنظمة المعمول بها."
    )

    addInfo(
        "الشفافية",
        "أي مساهمة مالية فعلية يجب أن تخضع لإجراءات قانونية ومحاسبية مناسبة."
    )

    addInfo(
        "عدم الادعاء",
        "وجود القسم داخل التطبيق لا يعني وجود جهة خيرية حكومية أو حساب مالي فعلي ما لم يتم تنفيذ واعتماد ذلك رسميًا."
    )

    addNavButton("↩ العودة للرئيسية") {
        showHome()
    }
}

// =========================
// MANAGER OFFICE
// =========================

private fun showManagerOffice() {

    baseLayout("مكتب المدير")

    addSection("الإدارة")

    addInfo(
        "مكتب المدير",
        "مساحة إدارية للمراجعة والمتابعة وإدارة عناصر المنصة وفق الصلاحيات."
    )

    addCard(
        "🧰 أدوات المدير",
        "الأدوات الإدارية المتاحة"
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
        "معرفة قدرات الجهاز والاتصال"
    ) {
        showDeviceCapabilities()
    }

    addCard(
        "📄 المستندات",
        "معلومات المستندات والسياسات"
    ) {
        showDocuments()
    }

    addCard(
        "⚖️ مكتب النائب العام",
        "عرض معلومات المكتب وصلاحياته"
    ) {
        showAttorneyOffice()
    }

    addNavButton("↩ العودة للرئيسية") {
        showHome()
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
        "التوسع",
        "يمكن إضافة حزم دول ومناطق مختلفة مع الحفاظ على القاعدة العالمية للمشروع."
    )

    addInfo(
        "النسخة",
        "نسخة تطويرية حقيقية من المشروع وليست مجرد واجهة تجريبية منفصلة."
    )

    addNavButton("↩ العودة للرئيسية") {
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
        "لا ينبغي إتاحة البيانات الخاصة إلا للجهات أو المستخدمين الذين يملكون الصلاحية."
    )

    addInfo(
        "الحماية التقنية",
        "يستخدم التطبيق طبقات حماية مناسبة، ومنها منع التقاط الشاشة في المناطق الحساسة."
    )

    addNavButton("↩ العودة للرئيسية") {
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
        "تجمع البيانات وتستخدم فقط وفق الوظيفة والغرض والصلاحيات المسموح بها."
    )

    addInfo(
        "المستخدم",
        "يجب توضيح البيانات المطلوبة وأسباب استخدامها عندما تصبح الوظيفة الفعلية جاهزة."
    )

    addInfo(
        "الأمان",
        "البيانات الحساسة تحتاج إلى حماية إضافية وإجراءات وصول مناسبة."
    )

    addNavButton("↩ العودة للرئيسية") {
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
        "يجب أن تكون المنتجات والخدمات والأنشطة المعروضة متوافقة مع القوانين المحلية المعمول بها."
    )

    addInfo(
        "منع الاحتيال",
        "يحظر استخدام المنصة لخداع المستخدمين أو تقديم معلومات مضللة."
    )

    addInfo(
        "المسؤولية",
        "يجب مراجعة المتطلبات القانونية والتنظيمية قبل إطلاق أي خدمة فعلية."
    )

    addNavButton("↩ العودة للرئيسية") {
        showHome()
    }
}

// =========================
// SENSITIVE ACCESS POLICY
// =========================

private fun showSensitiveAccessPolicy() {

    baseLayout("الخدمات الحساسة")

    addSection("حماية المشاركة الحساسة")

    addInfo(
        "حماية الشاشة",
        "تستخدم الشاشة الحساسة حماية تمنع التقاط الشاشة وتسجيلها قدر الإمكان عبر FLAG_SECURE."
    )

    addInfo(
        "التحقق الرسمي",
        "التحقق من أهلية المشترك يجب أن يتم عبر مصادر وجهات حكومية مخولة وبالإجراءات والتفويضات النظامية اللازمة."
    )

    addInfo(
        "الموانع القانونية",
        "عند تنفيذ الخدمة فعليًا، يجب التحقق من الموانع القانونية الموثقة ذات الصلة وفق القانون والجهة المختصة."
    )

    addInfo(
        "الموافقة",
        "لا يتم تنفيذ مشاركة مالية فعلية من هذه الواجهة دون الإجراءات والموافقات المطلوبة."
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
        "ضوابط المشاركة في المشاريع التمويلية"
    ) {
        showFinancingParticipation()
    }

    addNavButton("↩ العودة للرئيسية") {
        showHome()
    }
}

// =========================
// SECURITY CHECKLIST
// =========================

private fun showSecurityChecklist() {

    baseLayout("قائمة الحماية")

    addSection("مراجعة الأمان")

    addInfo(
        "1 — حماية الشاشة",
        "FLAG_SECURE مفعّل على مستوى Activity."
    )

    addInfo(
        "2 — قفل الجلسة",
        "قفل تلقائي بعد 10 دقائق خارج التطبيق."
    )

    addInfo(
        "3 — الوصول الحساس",
        "المشاركة الاستثمارية والتمويلية لها مسارات حماية مستقلة."
    )

    addInfo(
        "4 — التحقق القانوني",
        "أي تحقق رسمي فعلي يحتاج جهة مخولة وتفويضًا قانونيًا مناسبًا."
    )

    addInfo(
        "5 — العمليات المالية",
        "لا توجد عملية مالية حقيقية بمجرد عرض هذه الواجهات."
    )

    addNavButton("↩ العودة للأمان") {
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
        "الوصول الإداري يخضع للصلاحيات المعتمدة، ولا يعني وجود ربط أمني أو حكومي فعلي."
    )

    addNavButton("↩ الأمان") { showSafety() }
}

// =========================
// SERVICES
// =========================

private fun showServices() {
    baseLayout("الخدمات")
    addSection("الخدمات والمجالات")

    listOf(
        "🚚 النقل والشحن",
        "🔧 الخدمات المهنية",
        "🌾 الزراعة والثروة الحيوانية",
        "🏗️ البناء والجملة",
        "🏥 الصحة",
        "⚡ الكهرباء والمياه",
        "🎓 التعليم",
        "✈️ السفر"
    ).forEach { item ->
        addCard(item, "عرض المجال والخدمات المتاحة") {
            showDetails(item)
        }
    }

    addNavButton("↩ الرئيسية") { showHome() }
}

// =========================
// ADS
// =========================

private fun showAds() {
    baseLayout("الإعلانات")
    addSection("نظام الإعلانات")

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

    addCard("➕ إضافة إعلان", "إدخال معلومات الإعلان") {
        showAddAd()
    }

    addNavButton("↩ الرئيسية") { showHome() }
}

private fun showAddAd() {
    baseLayout("إضافة إعلان")
    addSection("بيانات الإعلان")

    addInfo(
        "الإعلان",
        "هذه الواجهة تمثل مسار إضافة الإعلان. التنفيذ التجاري الفعلي يحتاج نظام حسابات ومراجعة وصلاحيات."
    )

    addInfo(
        "المستويات",
        "BRONZE / SILVER / GOLD"
    )

    addNavButton("↩ الإعلانات") { showAds() }
}

// =========================
// KITCHEN
// =========================

private fun showKitchen() {
    baseLayout("المطبخ")
    addSection("المطبخ والطلبات")

    addInfo(
        "الخدمات",
        "قسم للمنتجات والخدمات والطلبات المرتبطة بالمطبخ."
    )

    addInfo(
        "التوصيل",
        "تختلف إمكانية التوصيل حسب المنطقة ومقدم الخدمة."
    )

    addNavButton("↩ الرئيسية") { showHome() }
}

// =========================
// BADGER
// =========================

private fun showBadger() {
    baseLayout("BADGER")
    addSection("BADGER")

    addInfo(
        "الهوية",
        "منتج مالي مستقل بهوية تعتمد على الغرير والعالم، ومصمم ليكون قابلًا للتقديم للمؤسسات والبنوك."
    )

    addInfo(
        "الإيصالات",
        "تصور الهوية يتضمن الإيصالات الزرقاء والذهبية والسوداء."
    )

    addInfo(
        "الأمان",
        "الأقسام المدفوعة والحساسة تخضع لقفل تلقائي وضوابط وصول."
    )

    addInfo(
        "التنفيذ المالي",
        "لا يتم تنفيذ عملية مالية حقيقية من هذه الواجهة دون نظام مالي مرخص وموافقات وإجراءات فعلية."
    )

    addCard("🏦 مكتب BADGER", "البنية والمبادئ") {
        showBadgerOffice()
    }

    addCard("💼 المشاركة الاستثمارية", "ضوابط المشاركة") {
        showInvestmentParticipation()
    }

    addCard("🏗️ المشاركة التمويلية", "ضوابط المشاريع") {
        showFinancingParticipation()
    }

    addNavButton("↩ الرئيسية") { showHome() }
}

// =========================
// ATTORNEY OFFICE
// =========================

private fun showAttorneyOffice() {
    baseLayout("مكتب النائب العام")
    addSection("المكتب القانوني")

    addInfo(
        "الدور",
        "واجهة معلوماتية لمبادئ المتابعة القانونية داخل بنية المشروع."
    )

    addInfo(
        "الصلاحيات",
        "وجود المكتب في التطبيق لا يعني وجود تكامل حكومي فعلي أو صلاحية حكومية."
    )

    addInfo(
        "الإدارة",
        "يمكن للمدير الاطلاع على المعلومات المسموح بها دون تعديل الصلاحيات القانونية."
    )

    addNavButton("↩ مكتب المدير") { showManagerOffice() }
}

// =========================
// PRIVATE FINANCIAL SYSTEM
// =========================

private fun showPrivateFinancialSystem() {
    baseLayout("النظام المالي الخاص")
    addSection("النظام المالي")

    addInfo(
        "المبدأ",
        "نظام خاص مستقل عن العرض العام، ويحتاج إلى تصميم قانوني وتقني ومالي منفصل قبل أي تشغيل فعلي."
    )

    addInfo(
        "التنفيذ",
        "لا توجد حركة أموال حقيقية بمجرد وجود هذه الواجهة."
    )

    addCard("💼 الاستثمار", "المشاركة الاستثمارية") {
        showInvestmentParticipation()
    }

    addCard("🏗️ التمويل", "المشاركة في المشاريع") {
        showFinancingParticipation()
    }

    addNavButton("↩ الرئيسية") { showHome() }
}

// =========================
// INVESTMENT
// =========================

private fun showInvestmentParticipation() {
    baseLayout("المشاركة الاستثمارية")
    addSection("حماية المشاركة")

    addInfo(
        "الحماية",
        "الشاشة الحساسة تستخدم حماية FLAG_SECURE لمنع التقاط الشاشة وتسجيلها."
    )

    addInfo(
        "الأهلية",
        "يجب التحقق من الأهلية عبر الجهات والمصادر الحكومية المخولة وبالتفويضات والإجراءات النظامية."
    )

    addInfo(
        "الموافقة",
        "لا يتم تنفيذ مشاركة مالية فعلية من هذه الواجهة."
    )

    addCard("📋 متطلبات الاستثمار", "عرض المتطلبات") {
        showInvestmentRequirements()
    }

    addCard("🛡️ التحقق الرسمي", "عرض مسار التحقق") {
        showEligibilityVerification()
    }

    addCard("👑 موافقة المالك", "ضوابط الاعتماد") {
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

    addInfo(
        "الحماية",
        "المعلومات الحساسة محمية، ولا يسمح بالعمليات المالية الفعلية من الواجهة وحدها."
    )

    addInfo(
        "الأهلية",
        "تتطلب المشاركة الفعلية إجراءات تحقق قانونية وتنظيمية مناسبة."
    )

    addCard("📋 متطلبات التمويل", "عرض المتطلبات") {
        showFinancingRequirements()
    }

    addCard("🛡️ التحقق", "التحقق من الأهلية") {
        showEligibilityVerification()
    }

    addNavButton("↩ الخدمات الحساسة") {
        showSensitiveAccessPolicy()
    }
}

// =========================
// ELIGIBILITY
// =========================

private fun showEligibilityVerification() {
    baseLayout("التحقق من الأهلية")
    addSection("التحقق الرسمي")

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

    addNavButton("↩ الخدمات الحساسة") {
        showSensitiveAccessPolicy()
    }
}

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

    addNavButton("↩ التحقق") {
        showEligibilityVerification()
    }
}

private fun showInvestmentRequirements() {
    baseLayout("متطلبات الاستثمار")
    addSection("المشاركة الاستثمارية")

    addInfo(
        "المتطلبات",
        "الهوية، الأهلية، الإفصاحات، الموافقات، والضوابط القانونية والتنظيمية المطلوبة للخدمة الفعلية."
    )

    addNavButton("↩ الاستثمار") {
        showInvestmentParticipation()
    }
}

private fun showFinancingRequirements() {
    baseLayout("متطلبات التمويل")
    addSection("المشاركة التمويلية")

    addInfo(
        "المتطلبات",
        "الهوية، الأهلية، بيانات المشروع، التحقق، الإفصاحات، والموافقات اللازمة قبل أي تنفيذ فعلي."
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
    addSection("الموافقة")

    addInfo(
        "مبدأ الاعتماد",
        "التغييرات الحساسة والعمليات الخاصة تخضع لمراجعة واعتماد المالك وفق الصلاحيات المحددة."
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
// PROJECT STATUS
// =========================

private fun showProjectStatus() {
    baseLayout("حالة المشروع")
    addSection("CENTRAL MARKET")

    addInfo(
        "الحالة",
        "نسخة تطوير حقيقية قيد البناء والمراجعة."
    )

    addInfo(
        "الهدف",
        "الوصول إلى APK قابل للبناء والاختبار مع الحفاظ على بنية المشروع ومواصفاته."
    )

    addNavButton("↩ الرئيسية") { showHome() }
}

// =========================
// DIAGNOSTICS
// =========================

private fun showDiagnostics() {
    baseLayout("التشخيص")
    addSection("تشخيص التطبيق")

    addInfo(
        "الواجهة",
        "واجهة التطبيق تعمل ضمن Activity واحدة وبناء برمجي مباشر."
    )

    addInfo(
        "الحماية",
        "FLAG_SECURE وقفل الجلسة مفعّلان في هذه النسخة."
    )

    addInfo(
        "الاتصال",
        "يمكن فحص الشبكة من قسم حالة الاتصال."
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
        getSystemService(CONNECTIVITY_SERVICE)
            as ConnectivityManager

    val network =
        manager.activeNetwork

    val capabilities =
        manager.getNetworkCapabilities(network)

    addInfo(
        "الشبكة",
        if (capabilities != null)
            "يوجد اتصال شبكة متاح."
        else
            "لا يوجد اتصال شبكة حاليًا."
    )

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

    addNavButton("↩ مكتب المدير") {
        showManagerOffice()
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
        "التنبيه الإقليمي",
        "يمكن لاحقًا إضافة تنبيهات حسب الدولة والمنطقة عند توفر البيانات الموثوقة."
    )

    addNavButton("↩ الخدمات") { showServices() }
}

// =========================
// COUNTRY PACKAGE
// =========================

private fun showCountryPackage() {
    baseLayout("حزمة الدولة")
    addSection("البنية العالمية")

    addInfo(
        "Global First",
        "القاعدة الأساسية عالمية، ويمكن إضافة حزم خاصة بكل دولة."
    )

    addInfo(
        "حزمة السودان",
        "يمكن أن تتضمن الوظائف والمؤسسات والمعلومات الخاصة بالسودان عند اعتمادها وتنفيذها."
    )

    addInfo(
        "الدول الأخرى",
        "تظهر فقط الوظائف المناسبة للدولة والمنطقة والقانون المحلي."
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
        "القاعدة",
        "القوانين والقيود والرسوم والشحن والتجارة تختلف حسب الدولة والمنطقة."
    )

    addInfo(
        "التنبيهات",
        "يمكن للنظام عرض تنبيه عندما تتطلب الخدمة مراجعة قواعد محلية."
    )

    addNavButton("↩ النظام") {
        showSystemSummary()
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
        "نظام مستقل يمكن تطويره كمنتج تقني ومالي منفصل وتقديمه للمؤسسات والبنوك."
    )

    addInfo(
        "العمولات",
        "العمولات والشروط يجب أن تكون موثقة تعاقديًا عند وجود تشغيل فعلي."
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

    addNavButton("↩ النظام") {
        showSystemSummary()
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

    addNavButton("↩ أدوات المدير") {
        showManagerTools()
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

// =========================
// MANAGER TOOLS
// =========================

private fun showManagerTools() {
    baseLayout("أدوات المدير")
    addSection("الإدارة")

    addCard("🔍 التشخيص", "حالة التطبيق") {
        showDiagnostics()
    }

    addCard("📱 قدرات الجهاز", "الجهاز والاتصال") {
        showDeviceCapabilities()
    }

    addCard("📄 المستندات", "الوثائق والسياسات") {
        showDocuments()
    }

    addCard("🌍 حزمة الدولة", "إعدادات الدولة") {
        showCountryPackage()
    }

    addCard("⚖️ القواعد الإقليمية", "القوانين حسب المنطقة") {
        showRegionalRules()
    }

    addCard("👑 تحكم المالك", "الصلاحيات والاعتماد") {
        showOwnerControls()
    }

    addNavButton("↩ مكتب المدير") {
        showManagerOffice()
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

    addInfo(
        "الأمان",
        "حماية الشاشة، قفل الجلسة، وضوابط إضافية للخدمات الحساسة."
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
        "هذه واجهة تطبيق حقيقية قابلة للبناء والاختبار، ولا تدعي تنفيذ تكاملات حكومية أو مالية غير موجودة."
    )

    addNavButton("↩ الرئيسية") {
        showHome()
    }
}
