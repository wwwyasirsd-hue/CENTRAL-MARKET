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

    private val navy = Color.rgb(18, 42, 66)
    private val blue = Color.rgb(32, 104, 170)
    private val gold = Color.rgb(205, 157, 45)
    private val green = Color.rgb(38, 130, 85)
    private val red = Color.rgb(180, 65, 65)

    private val background = Color.rgb(246, 249, 252)
    private val white = Color.WHITE
    private val textDark = Color.rgb(30, 43, 55)
    private val muted = Color.rgb(92, 108, 122)

    private lateinit var content: LinearLayout

    private val handler = Handler(Looper.getMainLooper())
    private val outsideAppTimeout = 10 * 60 * 1000L

    private var outsideAppStartedAt = 0L
    private var sessionLocked = false

    private val lockRunnable = Runnable {
        lockSession()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // حماية شاملة للتطبيق من تصوير وتسجيل الشاشة.
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

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
        handler.postDelayed(lockRunnable, outsideAppTimeout)
    }

    override fun onDestroy() {
        handler.removeCallbacks(lockRunnable)
        super.onDestroy()
    }

    private fun lockSession() {
        if (sessionLocked) return

        sessionLocked = true
        handler.removeCallbacks(lockRunnable)

        baseLayout("التطبيق مقفل")
        addSection("حماية الجلسة")

        addInfo(
            "تم قفل الجلسة",
            "مرّت 10 دقائق أثناء وجود التطبيق خارج الشاشة."
        )

        addCard(
            "🔓 فتح الجلسة",
            "العودة إلى التطبيق"
        ) {
            sessionLocked = false
            outsideAppStartedAt = 0L
            showHome()
        }
    }

    private fun baseLayout(title: String = "CENTRAL MARKET"): LinearLayout {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(background)

        val header = TextView(this)
        header.text = title
        header.setTextColor(white)
        header.textSize = 22f
        header.setTypeface(null, Typeface.BOLD)
        header.gravity = Gravity.CENTER
        header.setPadding(16, 24, 16, 24)
        header.background = roundedBackground(navy, 0f)

        root.addView(
            header,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val scroll = ScrollView(this)

        content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.setPadding(16, 16, 16, 24)

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

    private fun addSection(title: String) {
        val text = TextView(this)
        text.text = title
        text.textSize = 19f
        text.setTextColor(navy)
        text.setTypeface(null, Typeface.BOLD)
        text.setPadding(4, 18, 4, 10)

        content.addView(text)
    }

    private fun addInfo(title: String, body: String) {
        val box = TextView(this)
        box.text = "$title\n$body"
        box.textSize = 15f
        box.setTextColor(textDark)
        box.setPadding(18, 16, 18, 16)
        box.background = roundedBackground(white, 18f)

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

    private fun addCard(
        title: String,
        description: String,
        action: () -> Unit
    ) {
        val button = Button(this)
        button.text = "$title\n$description"
        button.textSize = 15f
        button.setTextColor(white)
        button.setPadding(14, 14, 14, 14)
        button.background = roundedBackground(blue, 18f)
        button.setOnClickListener {
            action()
        }

        content.addView(
            button,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 10
            }
        )
    }

    private fun addNavButton(
        title: String,
        action: () -> Unit
    ) {
        val button = Button(this)
        button.text = title
        button.setTextColor(navy)
        button.setOnClickListener {
            action()
        }

        content.addView(
            button,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
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

    private fun showHome() {
        baseLayout("CENTRAL MARKET")

        addSection("منصة واحدة .. عالم من الفرص.")

        addInfo(
            "المنصة",
            "منصة منظمة للتجارة والخدمات والمعلومات والمشاريع."
        )

        addCard(
            "🌐 الاتصال",
            "فحص حالة الإنترنت وقدرات الاتصال"
        ) {
            showOnline()
        }

        addCard(
            "👤 وضع الضيف",
            "الدخول إلى الخدمات العامة"
        ) {
            showGuestMode()
        }

        addCard(
            "🛡️ الأمان والخصوصية",
            "الحماية والخصوصية وقواعد الاستخدام"
        ) {
            showSafety()
        }

        addCard(
            "🛍️ المنتجات والأقسام",
            "استعراض الأسواق والقطاعات"
        ) {
            showProducts()
        }

        addCard(
            "🔎 البحث",
            "البحث داخل محتوى المنصة"
        ) {
            showSearch()
        }

        addCard(
            "⭐ النقاط",
            "النقاط والمشاركة المجتمعية"
        ) {
            showPoints()
        }

        addCard(
            "🧠 الذكاء البشري",
            "المعرفة والخبرات والمبادرات"
        ) {
            showHumanIntelligence()
        }

        addCard(
            "🤖 CTM AI",
            "المساعد الرسمي للمنصة"
        ) {
            showCtmAi()
        }

        addCard(
            "❤️ الدعم المجتمعي",
            "المساعدة والمبادرات الخيرية"
        ) {
            showCharity()
        }

        addCard(
            "🏢 إدارة المنصة",
            "المكاتب والإدارة والحماية"
        ) {
            showManagerOffice()
        }

        addInfo(
            "المالك",
            "المالك ياسر حسن وشركاؤه"
        )

        addInfo(
            "الحماية",
            "حماية الشاشة مفعلة على مستوى التطبيق بالكامل."
        )

        addNavButton("ℹ️ معلومات التطبيق") {
            showAppInformation()
        }
    }

    private fun showOnline() {
        baseLayout("حالة الاتصال")

        val manager =
            getSystemService(CONNECTIVITY_SERVICE)
                    as ConnectivityManager

        val network =
            manager.activeNetwork

        val capabilities =
            manager.getNetworkCapabilities(network)

        val online = capabilities != null

        addSection("تشخيص الاتصال")

        addInfo(
            "الحالة",
            if (online) "متصل بالإنترنت" else "غير متصل"
        )

        if (capabilities != null) {
            val wifi =
                capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                )

            val mobile =
                capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_CELLULAR
                )

            val ethernet =
                capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_ETHERNET
                )

            addInfo(
                "نوع الاتصال",
                when {
                    wifi -> "Wi-Fi"
                    mobile -> "شبكة الهاتف"
                    ethernet -> "Ethernet"
                    else -> "اتصال آخر"
                }
            )

            addInfo(
                "الإنترنت",
                if (
                    capabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_INTERNET
                    )
                ) {
                    "متاح"
                } else {
                    "غير مؤكد"
                }
            )
        }

        addCard(
            "🔄 فحص مرة أخرى",
            "تحديث حالة الاتصال"
        ) {
            showOnline()
        }

        addNavButton("↩️ الرئيسية") {
            showHome()
        }
    }

        private fun showGuestMode() {
        baseLayout("وضع الضيف")

        addSection("الدخول العام")

        addInfo(
            "وضع الضيف",
            "يمكن تصفح الخدمات والمعلومات العامة دون فتح الأقسام الخاصة."
        )

        addCard(
            "🛍️ المنتجات",
            "استعراض الأقسام العامة"
        ) {
            showProducts()
        }

        addCard(
            "🔎 البحث",
            "البحث في المعلومات المتاحة"
        ) {
            showSearch()
        }

        addCard(
            "🛡️ الخصوصية",
            "الاطلاع على قواعد حماية البيانات"
        ) {
            showPrivacyCenter()
        }

        addCard(
            "📋 قواعد الاستخدام",
            "القواعد العامة للمنصة"
        ) {
            showAppRules()
        }

        addNavButton("↩️ الرئيسية") {
            showHome()
        }
    }

    private fun showSafety() {
        baseLayout("الأمان والخصوصية")

        addSection("الحماية الشاملة")

        addInfo(
            "حماية الشاشة",
            "يتم منع التقاط الشاشة وتسجيلها على مستوى التطبيق."
        )

        addInfo(
            "قفل خارج التطبيق",
            "بعد مرور 10 دقائق خارج التطبيق يتم قفل الجلسة."
        )

        addInfo(
            "البيانات",
            "يجب التعامل مع البيانات الحساسة وفق الصلاحيات والقوانين المعمول بها."
        )

        addCard(
            "🔐 سياسة الوصول الحساس",
            "قواعد الوصول إلى الخدمات الحساسة"
        ) {
            showSensitiveAccessPolicy()
        }

        addCard(
            "✅ قائمة الأمان",
            "مراجعة متطلبات الحماية"
        ) {
            showSecurityChecklist()
        }

        addCard(
            "🛡️ مركز الخصوصية",
            "إدارة وفهم الخصوصية"
        ) {
            showPrivacyCenter()
        }

        addCard(
            "📄 سياسة البيانات",
            "مبادئ التعامل مع البيانات"
        ) {
            showDataPolicy()
        }

        addCard(
            "🏢 مكتب الأمن والمعلومات",
            "قسم الأمن والمعلومات"
        ) {
            showSecurityOffice()
        }

        addNavButton("↩️ الرئيسية") {
            showHome()
        }
    }

    private fun showProducts() {
        baseLayout("المنتجات والأقسام")

        addSection("الأسواق والقطاعات")

        addCard(
            "🚗 المركبات والشاحنات",
            "بيع وشراء وخدمات مرتبطة بالمركبات"
        ) {
            showCategory("المركبات والشاحنات")
        }

        addCard(
            "📱 الهواتف والإلكترونيات",
            "الأجهزة والمنتجات الإلكترونية"
        ) {
            showCategory("الهواتف والإلكترونيات")
        }

        addCard(
            "🍽️ المطاعم والتوصيل",
            "المطاعم وخدمات التوصيل"
        ) {
            showCategory("المطاعم والتوصيل")
        }

        addCard(
            "🌾 الزراعة والثروة الحيوانية",
            "المنتجات والخدمات الزراعية"
        ) {
            showCategory("الزراعة والثروة الحيوانية")
        }

        addCard(
            "🐟 الأسماك",
            "المنتجات والخدمات المرتبطة بالأسماك"
        ) {
            showCategory("الأسماك")
        }

        addCard(
            "🏗️ البناء",
            "مواد البناء والخدمات المرتبطة بها"
        ) {
            showCategory("البناء")
        }

        addCard(
            "📦 الجملة",
            "التجارة بالجملة والتوريد"
        ) {
            showCategory("الجملة")
        }

        addCard(
            "🏥 الصحة",
            "الخدمات والمعلومات الصحية العامة"
        ) {
            showCategory("الصحة")
        }

        addCard(
            "⚽ الرياضة",
            "المنتجات والخدمات الرياضية"
        ) {
            showCategory("الرياضة")
        }

        addCard(
            "⚡ الكهرباء والمياه",
            "الخدمات والاحتياجات المتعلقة بالطاقة والمياه"
        ) {
            showCategory("الكهرباء والمياه")
        }

        addCard(
            "🎓 التعليم",
            "الخدمات والفرص التعليمية"
        ) {
            showCategory("التعليم")
        }

        addCard(
            "✈️ السفر",
            "السفر والخدمات المرتبطة به"
        ) {
            showCategory("السفر")
        }

        addCard(
            "🛠️ الخدمات",
            "الخدمات المهنية والعامة"
        ) {
            showServices()
        }

        addCard(
            "📢 الإعلانات",
            "الإعلانات التجارية ومستوياتها"
        ) {
            showAds()
        }

        addNavButton("↩️ الرئيسية") {
            showHome()
        }
    }

    private fun showCategory(category: String) {
        baseLayout(category)

        addSection("القسم")

        addInfo(
            category,
            "يمكن هنا عرض المنتجات والخدمات والإعلانات المتعلقة بهذا القسم."
        )

        addCard(
            "📋 تفاصيل القسم",
            "عرض المعلومات والخدمات المتاحة"
        ) {
            showDetails(category)
        }

        addCard(
            "📢 إضافة إعلان",
            "إنشاء إعلان وفق قواعد المنصة"
        ) {
            showAddAd()
        }

        addCard(
            "🔎 البحث",
            "البحث داخل هذا القسم"
        ) {
            showSearch()
        }

        addNavButton("↩️ المنتجات والأقسام") {
            showProducts()
        }

        addNavButton("🏠 الرئيسية") {
            showHome()
        }
    }

    private fun showDetails(category: String) {
        baseLayout("تفاصيل القسم")

        addSection(category)

        addInfo(
            "المحتوى",
            "هذا القسم مخصص لعرض تفاصيل المنتجات والخدمات والعروض المتاحة."
        )

        addInfo(
            "المعاملات",
            "أي عملية تجارية يجب أن تلتزم بالقوانين والأنظمة المعمول بها."
        )

        addInfo(
            "الحماية",
            "لا تُعرض البيانات الحساسة إلا وفق الصلاحيات المناسبة."
        )

        addCard(
            "📦 الخدمات",
            "عرض الخدمات المتاحة"
        ) {
            showServices()
        }

        addCard(
            "📢 الإعلانات",
            "عرض الإعلانات التجارية"
        ) {
            showAds()
        }

        addCard(
            "🔎 البحث",
            "البحث في محتوى المنصة"
        ) {
            showSearch()
        }

        addNavButton("↩️ العودة للقسم") {
            showCategory(category)
        }

        addNavButton("🏠 الرئيسية") {
            showHome()
        }
    }

        private fun showSearch() {
        baseLayout("البحث")

        addSection("البحث داخل المنصة")

        addInfo(
            "البحث",
            "ابحث عن المنتجات والخدمات والمعلومات المتاحة داخل المنصة."
        )

        addCard(
            "🚗 المركبات والشاحنات",
            "الوصول إلى قسم المركبات"
        ) {
            showCategory("المركبات والشاحنات")
        }

        addCard(
            "📱 الهواتف والإلكترونيات",
            "الوصول إلى قسم الإلكترونيات"
        ) {
            showCategory("الهواتف والإلكترونيات")
        }

        addCard(
            "🍽️ المطاعم والتوصيل",
            "الوصول إلى المطاعم والتوصيل"
        ) {
            showCategory("المطاعم والتوصيل")
        }

        addCard(
            "🛠️ الخدمات",
            "البحث في الخدمات"
        ) {
            showServices()
        }

        addCard(
            "📢 الإعلانات",
            "البحث في الإعلانات"
        ) {
            showAds()
        }

        addNavButton("↩️ الرئيسية") {
            showHome()
        }
    }

    private fun showPoints() {
        baseLayout("النقاط")

        addSection("النقاط والمشاركة")

        addInfo(
            "النقاط",
            "نظام النقاط مخصص للمشاركة والأنشطة المسموح بها داخل المنصة."
        )

        addInfo(
            "الاحتساب",
            "يتم احتساب النقاط وفق قواعد المنصة المعتمدة."
        )

        addCard(
            "📋 حالة النقاط",
            "عرض المعلومات الحالية للنقاط"
        ) {
            addInfo(
                "الحالة",
                "لا توجد بيانات نقاط فعلية مرتبطة بحساب المستخدم حاليًا."
            )
        }

        addCard(
            "🧠 الذكاء البشري",
            "المشاركة بالمعرفة والخبرات"
        ) {
            showHumanIntelligence()
        }

        addNavButton("↩️ الرئيسية") {
            showHome()
        }
    }

    private fun showHumanIntelligence() {
        baseLayout("الذكاء البشري")

        addSection("الذكاء البشري")

        addInfo(
            "الفكرة",
            "مساحة للاستفادة من الخبرات والمعارف البشرية والمبادرات المجتمعية."
        )

        addInfo(
            "المشاركة",
            "تخضع المشاركات للقواعد والصلاحيات والمراجعة المناسبة."
        )

        addCard(
            "🤝 المبادرات",
            "المشاركة في المبادرات المجتمعية"
        ) {
            showCharity()
        }

        addCard(
            "📄 المستندات",
            "المعلومات والوثائق المسموح بها"
        ) {
            showDocuments()
        }

        addNavButton("↩️ الرئيسية") {
            showHome()
        }
    }

    private fun showCtmAi() {
        baseLayout("CTM AI")

        addSection("المساعد الرسمي")

        addInfo(
            "CTM AI",
            "المساعد الرسمي للمنصة، ويعمل ضمن المعلومات والخدمات المسموح بها."
        )

        addInfo(
            "الصلاحيات",
            "الوصول إلى المعلومات يكون وفق الصلاحيات المعتمدة من المنصة."
        )

        addInfo(
            "الصوت",
            "يمكن دعم البحث أو قراءة المعلومات المسموح بها صوتيًا عند توفر الميزة."
        )

        addInfo(
            "الإدارة",
            "يمكن إيقاف أو تعديل الخدمة وفق اعتماد إدارة المنصة."
        )

        addCard(
            "🔍 البحث",
            "العودة إلى البحث داخل المنصة"
        ) {
            showSearch()
        }

        addNavButton("↩️ الرئيسية") {
            showHome()
        }
    }

    private fun showCharity() {
        baseLayout("الدعم المجتمعي")

        addSection("المساعدة والمبادرات")

        addInfo(
            "الدعم المجتمعي",
            "قسم مخصص للمبادرات الخيرية ودعم الأيتام والمحتاجين وفق الأنظمة المعمول بها."
        )

        addInfo(
            "الزكاة البشرية",
            "يمكن أن تشمل النسخة المخصصة للسودان خدمات ومبادرات مرتبطة بالزكاة البشرية وفق الإطار النظامي."
        )

        addCard(
            "🍽️ المطبخ",
            "المبادرات والخدمات المتعلقة بالمطبخ"
        ) {
            showKitchen()
        }

        addCard(
            "📋 المستندات",
            "المعلومات والوثائق الخاصة بالمبادرات"
        ) {
            showDocuments()
        }

        addNavButton("↩️ الرئيسية") {
            showHome()
        }
    }

    private fun showManagerOffice() {
        baseLayout("إدارة المنصة")

        addSection("مكاتب الإدارة")

        addInfo(
            "مكتب المدير",
            "قسم إداري مخصص لإدارة المنصة ومتابعة الأقسام والاعتمادات."
        )

        addInfo(
            "الصلاحيات",
            "الوصول إلى وظائف الإدارة يكون حسب مستوى الصلاحية المعتمد."
        )

        addCard(
            "🛡️ مكتب الأمن والمعلومات",
            "الأمن وحماية المعلومات"
        ) {
            showSecurityOffice()
        }

        addCard(
            "⚖️ مكتب النائب العام",
            "معلومات المكتب والصلاحيات"
        ) {
            showAttorneyOffice()
        }

        addCard(
            "💼 النظام المالي الخاص",
            "إدارة المعلومات المالية المسموح بها"
        ) {
            showPrivateFinancialSystem()
        }

        addCard(
            "🤖 CTM AI",
            "إدارة المساعد الرسمي"
        ) {
            showCtmAi()
        }

        addNavButton("↩️ الرئيسية") {
            showHome()
        }
    }

    private fun showAppInformation() {
        baseLayout("معلومات التطبيق")

        addSection("CENTRAL MARKET")

        addInfo(
            "المنصة",
            "منصة متعددة الأقسام للتجارة والخدمات والمعلومات والمشاريع."
        )

        addInfo(
            "المالك",
            "المالك ياسر حسن وشركاؤه"
        )

        addInfo(
            "الحماية",
            "حماية الشاشة مفعلة على مستوى التطبيق بالكامل."
        )

        addInfo(
            "قفل الجلسة",
            "عند بقاء التطبيق خارج الشاشة لمدة 10 دقائق يتم قفل الجلسة."
        )

        addInfo(
            "التوافق",
            "يتم الاعتماد على قدرات الجهاز والاتصال المتاحين عند الحاجة."
        )

        addNavButton("↩️ الرئيسية") {
            showHome()
        }
    }

    private fun showPrivacyCenter() {
        baseLayout("مركز الخصوصية")

        addSection("الخصوصية")

        addInfo(
            "حماية البيانات",
            "يجب استخدام البيانات وفق الغرض المسموح والصلاحيات المعتمدة."
        )

        addInfo(
            "الخدمات الحساسة",
            "تطبق عليها ضوابط وصول وحماية إضافية وفق طبيعة الخدمة."
        )

        addCard(
            "📄 سياسة البيانات",
            "قراءة مبادئ التعامل مع البيانات"
        ) {
            showDataPolicy()
        }

        addCard(
            "🛡️ قواعد الأمان",
            "مراجعة ضوابط الأمان"
        ) {
            showSecurityChecklist()
        }

        addNavButton("↩️ الرئيسية") {
            showHome()
        }
    }

        private fun showDataPolicy() {
        baseLayout("سياسة البيانات")

        addSection("البيانات والخصوصية")

        addInfo(
            "المبدأ",
            "تتعامل المنصة مع البيانات وفق الغرض المسموح والصلاحيات والقوانين المعمول بها."
        )

        addInfo(
            "البيانات الحساسة",
            "لا يتم عرض أو استخدام البيانات الحساسة إلا عند وجود صلاحية وسبب مشروع."
        )

        addInfo(
            "الحماية",
            "حماية الشاشة مفعلة على مستوى التطبيق بالكامل."
        )

        addInfo(
            "الجلسة",
            "إذا بقي التطبيق خارج الشاشة لمدة 10 دقائق يتم قفل الجلسة."
        )

        addNavButton("↩️ مركز الخصوصية") {
            showPrivacyCenter()
        }

        addNavButton("🏠 الرئيسية") {
            showHome()
        }
    }

    private fun showAppRules() {
        baseLayout("قواعد الاستخدام")

        addSection("القواعد العامة")

        addInfo(
            "الاستخدام القانوني",
            "يجب استخدام المنصة في الأنشطة المسموح بها ووفق القوانين والأنظمة."
        )

        addInfo(
            "التجارة",
            "يجب أن تكون الإعلانات والمعاملات التجارية واضحة ومشروعة."
        )

        addInfo(
            "المحتوى",
            "يُمنع نشر معلومات أو محتوى مخالف للقوانين أو حقوق الآخرين."
        )

        addInfo(
            "الحسابات",
            "تخضع الحسابات والصلاحيات لمستوى الوصول المعتمد."
        )

        addInfo(
            "الخدمات الحساسة",
            "قد تتطلب بعض الخدمات تحققًا إضافيًا قبل السماح بالوصول."
        )

        addNavButton("↩️ الأمان والخصوصية") {
            showSafety()
        }

        addNavButton("🏠 الرئيسية") {
            showHome()
        }
    }

    private fun showSensitiveAccessPolicy() {
        baseLayout("الوصول الحساس")

        addSection("الخدمات الحساسة")

        addInfo(
            "الحماية",
            "الحماية مطبقة على مستوى التطبيق بالكامل، ولا تقتصر على شاشة معينة."
        )

        addInfo(
            "التقاط الشاشة",
            "يتم منع التقاط الشاشة وتسجيلها داخل التطبيق."
        )

        addInfo(
            "التحقق",
            "الخدمات الحساسة قد تتطلب تحققًا رسميًا من أهلية المستخدم قبل الوصول."
        )

        addInfo(
            "المصادر الرسمية",
            "يجب أن يتم التحقق عبر الجهات أو المصادر الحكومية المخولة وبالطرق والتفويضات النظامية المناسبة."
        )

        addInfo(
            "المشاركة المالية",
            "المشاركة في الاستثمار أو المشاريع التمويلية لا تُفتح إلا بعد استيفاء المتطلبات المعتمدة."
        )

        addInfo(
            "التنفيذ المالي",
            "لا يتم تنفيذ أي عملية مالية فعلية دون اعتماد وتأكيد الصلاحية المطلوبة."
        )

        addCard(
            "✅ التحقق من الأهلية",
            "متطلبات التحقق الرسمي"
        ) {
            showEligibilityVerification()
        }

        addCard(
            "💰 المشاركة الاستثمارية",
            "متطلبات المشاركة في الاستثمار"
        ) {
            showInvestmentParticipation()
        }

        addCard(
            "🏗️ المشاركة التمويلية",
            "متطلبات المشاركة في المشاريع التمويلية"
        ) {
            showFinancingParticipation()
        }

        addNavButton("↩️ الأمان والخصوصية") {
            showSafety()
        }
    }

    private fun showSecurityChecklist() {
        baseLayout("قائمة الأمان")

        addSection("فحص الحماية")

        addInfo(
            "حماية الشاشة",
            "مفعلة على مستوى التطبيق."
        )

        addInfo(
            "قفل خارج التطبيق",
            "يبدأ بعد 10 دقائق من بقاء التطبيق خارج الشاشة."
        )

        addInfo(
            "الخدمات الحساسة",
            "تخضع للتحقق والصلاحيات المناسبة."
        )

        addInfo(
            "التنفيذ المالي",
            "لا يتم التنفيذ المالي الفعلي دون الاعتماد المطلوب."
        )

        addInfo(
            "الاتصال",
            "يتم فحص حالة الاتصال وقدرات الشبكة عند الحاجة."
        )

        addCard(
            "📱 قدرات الجهاز",
            "فحص قدرات الجهاز المتاحة"
        ) {
            showDeviceCapabilities()
        }

        addCard(
            "🔍 التشخيص",
            "عرض معلومات التشخيص المتاحة"
        ) {
            showDiagnostics()
        }

        addNavButton("↩️ الأمان والخصوصية") {
            showSafety()
        }
    }

    private fun showSecurityOffice() {
        baseLayout("مكتب الأمن والمعلومات")

        addSection("الأمن والمعلومات")

        addInfo(
            "المهمة",
            "متابعة حماية المعلومات والصلاحيات ومخاطر الاستخدام داخل المنصة."
        )

        addInfo(
            "الوصول",
            "هذا القسم مخصص للصلاحيات الإدارية المعتمدة وليس للاستخدام العام."
        )

        addCard(
            "🛡️ قائمة الأمان",
            "مراجعة عناصر الحماية"
        ) {
            showSecurityChecklist()
        }

        addCard(
            "🔐 سياسة الوصول الحساس",
            "مراجعة الخدمات الحساسة"
        ) {
            showSensitiveAccessPolicy()
        }

        addCard(
            "🔍 التشخيص",
            "عرض حالة الجهاز والاتصال"
        ) {
            showDiagnostics()
        }

        addCard(
            "📄 المستندات",
            "المستندات المسموح بالوصول إليها"
        ) {
            showDocuments()
        }

        addNavButton("↩️ إدارة المنصة") {
            showManagerOffice()
        }
    }

    private fun showServices() {
        baseLayout("الخدمات")

        addSection("الخدمات العامة")

        addInfo(
            "الخدمات",
            "قسم يجمع الخدمات المهنية والعامة المتاحة عبر المنصة."
        )

        addCard(
            "🏥 الصحة",
            "خدمات ومعلومات صحية عامة"
        ) {
            showCategory("الصحة")
        }

        addCard(
            "🎓 التعليم",
            "خدمات وفرص تعليمية"
        ) {
            showCategory("التعليم")
        }

        addCard(
            "✈️ السفر",
            "خدمات السفر والمعلومات المرتبطة به"
        ) {
            showCategory("السفر")
        }

        addCard(
            "⚡ الكهرباء والمياه",
            "الخدمات المتعلقة بالطاقة والمياه"
        ) {
            showCategory("الكهرباء والمياه")
        }

        addCard(
            "🏗️ البناء",
            "الخدمات ومواد البناء"
        ) {
            showCategory("البناء")
        }

        addNavButton("↩️ المنتجات والأقسام") {
            showProducts()
        }

        addNavButton("🏠 الرئيسية") {
            showHome()
        }
    }

        private fun showAds() {
        baseLayout("الإعلانات")

        addSection("مستويات الإعلانات")

        addInfo(
            "الإعلانات",
            "نظام إعلانات منظم داخل المنصة وفق مستوى الإعلان والصلاحيات."
        )

        addCard(
            "🥉 BRONZE",
            "المستوى الأساسي للإعلان"
        ) {
            showAddAd()
        }

        addCard(
            "🥈 SILVER",
            "مستوى إعلان موسع"
        ) {
            showAddAd()
        }

        addCard(
            "🥇 GOLD",
            "مستوى إعلان متقدم"
        ) {
            showAddAd()
        }

        addCard(
            "➕ إضافة إعلان",
            "إنشاء إعلان جديد"
        ) {
            showAddAd()
        }

        addNavButton("↩️ المنتجات والأقسام") {
            showProducts()
        }

        addNavButton("🏠 الرئيسية") {
            showHome()
        }
    }

    private fun showAddAd() {
        baseLayout("إضافة إعلان")

        addSection("إعلان جديد")

        addInfo(
            "المتطلبات",
            "يجب أن تكون معلومات الإعلان واضحة وصحيحة ومتوافقة مع قواعد المنصة."
        )

        addInfo(
            "المراجعة",
            "قد يخضع الإعلان للمراجعة قبل ظهوره للمستخدمين."
        )

        addInfo(
            "المستويات",
            "يمكن اختيار مستوى الإعلان وفق الخدمات المتاحة والصلاحيات المعتمدة."
        )

        addCard(
            "🥉 BRONZE",
            "اختيار المستوى الأساسي"
        ) {
            showOwnerApproval()
        }

        addCard(
            "🥈 SILVER",
            "اختيار المستوى الموسع"
        ) {
            showOwnerApproval()
        }

        addCard(
            "🥇 GOLD",
            "اختيار المستوى المتقدم"
        ) {
            showOwnerApproval()
        }

        addNavButton("↩️ الإعلانات") {
            showAds()
        }
    }

    private fun showKitchen() {
        baseLayout("المطبخ")

        addSection("المطبخ والخدمات الغذائية")

        addInfo(
            "المطبخ",
            "قسم للخدمات والمنتجات والمبادرات المتعلقة بالمطبخ والطعام."
        )

        addCard(
            "🍽️ المطاعم",
            "الوصول إلى المطاعم والتوصيل"
        ) {
            showCategory("المطاعم والتوصيل")
        }

        addCard(
            "❤️ المبادرات",
            "المبادرات المجتمعية المرتبطة بالطعام"
        ) {
            showCharity()
        }

        addNavButton("↩️ الدعم المجتمعي") {
            showCharity()
        }
    }

    private fun showBadger() {
        baseLayout("BADGER")

        addSection("BADGER")

        addInfo(
            "المنتج",
            "نظام مالي مستقل مخصص للتطوير والتقديم للمؤسسات المالية وفق المتطلبات النظامية."
        )

        addInfo(
            "الهوية",
            "هوية BADGER مرتبطة بالغُرَيْر والعالم، مع تصميمات إيصالات باللون الأزرق والذهبي والأسود."
        )

        addInfo(
            "العمولات",
            "تُسجل العمولات وفق العقود والاتفاقيات المعتمدة."
        )

        addInfo(
            "الأمان",
            "تطبق حماية الشاشة وقواعد الجلسة والحماية الشاملة للتطبيق."
        )

        addCard(
            "💰 التحليلات المالية",
            "عرض التحليلات والمعلومات المسموح بها"
        ) {
            showPrivateFinancialSystem()
        }

        addCard(
            "💼 المشاركة الاستثمارية",
            "متطلبات المشاركة"
        ) {
            showInvestmentParticipation()
        }

        addCard(
            "🏗️ المشاريع التمويلية",
            "متطلبات المشاركة في المشاريع"
        ) {
            showFinancingParticipation()
        }

        addCard(
            "🛡️ الأمان",
            "قواعد حماية BADGER"
        ) {
            showSensitiveAccessPolicy()
        }

        addNavButton("↩️ إدارة المنصة") {
            showManagerOffice()
        }
    }

    private fun showAttorneyOffice() {
        baseLayout("مكتب النائب العام")

        addSection("مكتب النائب العام")

        addInfo(
            "الوصول",
            "قسم إداري خاص يخضع للصلاحيات والأنظمة المعمول بها."
        )

        addInfo(
            "الدور",
            "يمكن عرض المعلومات المصرح بها دون منح صلاحيات غير معتمدة."
        )

        addInfo(
            "الإدارة",
            "لا يتم تعديل أو إصدار بيانات رسمية من داخل التطبيق دون تفويض قانوني مناسب."
        )

        addCard(
            "📄 المستندات",
            "عرض المستندات المسموح بها"
        ) {
            showDocuments()
        }

        addCard(
            "✅ متطلبات الأهلية",
            "معلومات التحقق الرسمي"
        ) {
            showEligibilityVerification()
        }

        addNavButton("↩️ إدارة المنصة") {
            showManagerOffice()
        }
    }

    private fun showPrivateFinancialSystem() {
        baseLayout("النظام المالي الخاص")

        addSection("المعلومات المالية")

        addInfo(
            "النظام",
            "قسم خاص لعرض وإدارة المعلومات المالية المسموح بها وفق الصلاحيات."
        )

        addInfo(
            "التنفيذ",
            "لا يتم تنفيذ تحويل أو استثمار أو عملية مالية فعلية من هذه الواجهة دون الاعتماد والتفويض المطلوب."
        )

        addInfo(
            "الأمان",
            "الخدمات المالية الحساسة تخضع للحماية الشاملة والتحقق المناسب."
        )

        addCard(
            "💰 الاستثمار",
            "المشاركة في الاستثمارات"
        ) {
            showInvestmentParticipation()
        }

        addCard(
            "🏗️ التمويل",
            "المشاركة في المشاريع التمويلية"
        ) {
            showFinancingParticipation()
        }

        addCard(
            "🛡️ التحقق",
            "التحقق من الأهلية"
        ) {
            showEligibilityVerification()
        }

        addCard(
            "📊 حالة المشروع",
            "عرض حالة المشروع"
        ) {
            showProjectStatus()
        }

        addNavButton("↩️ BADGER") {
            showBadger()
        }
    }

    private fun showInvestmentParticipation() {
        baseLayout("المشاركة الاستثمارية")

        addSection("الاستثمار")

        addInfo(
            "قبل المشاركة",
            "يجب استيفاء متطلبات الأهلية والهوية والتفويضات المطلوبة وفق النظام المعمول به."
        )

        addInfo(
            "التحقق الرسمي",
            "يتم التحقق من الأهلية عبر الجهات أو المصادر الحكومية المخولة وبالطرق النظامية."
        )

        addInfo(
            "الموانع القانونية",
            "تُراجع الموانع القانونية الموثقة ذات الصلة بالمشاركة وفق الصلاحيات والأساس القانوني."
        )

        addInfo(
            "التنفيذ",
            "هذه الشاشة لا تنفذ عملية استثمار مالية فعلية."
        )

        addCard(
            "✅ متطلبات الاستثمار",
            "عرض المتطلبات"
        ) {
            showInvestmentRequirements()
        }

        addCard(
            "🛡️ التحقق من الأهلية",
            "بدء مسار التحقق النظامي"
        ) {
            showEligibilityVerification()
        }

        addCard(
            "📋 اعتماد المالك",
            "مراجعة الاعتماد المطلوب"
        ) {
            showOwnerApproval()
        }

        addNavButton("↩️ النظام المالي") {
            showPrivateFinancialSystem()
        }
    }

        private fun showFinancingParticipation() {
        baseLayout("المشاركة التمويلية")

        addSection("المشاريع التمويلية")

        addInfo(
            "قبل المشاركة",
            "يجب استيفاء متطلبات الأهلية والهوية والتفويضات المطلوبة."
        )

        addInfo(
            "التحقق الرسمي",
            "يتم التحقق عبر الجهات أو المصادر الحكومية المخولة وبالطرق النظامية."
        )

        addInfo(
            "الموانع القانونية",
            "تتم مراجعة الموانع القانونية الموثقة ذات الصلة وفق الصلاحيات والأساس القانوني."
        )

        addInfo(
            "التنفيذ",
            "هذه الشاشة لا تنفذ تمويلًا ماليًا فعليًا."
        )

        addCard(
            "📋 متطلبات التمويل",
            "عرض متطلبات المشاركة"
        ) {
            showFinancingRequirements()
        }

        addCard(
            "✅ التحقق من الأهلية",
            "مسار التحقق النظامي"
        ) {
            showEligibilityVerification()
        }

        addCard(
            "📋 اعتماد المالك",
            "مراجعة الاعتماد المطلوب"
        ) {
            showOwnerApproval()
        }

        addNavButton("↩️ النظام المالي") {
            showPrivateFinancialSystem()
        }
    }

    private fun showEligibilityVerification() {
        baseLayout("التحقق من الأهلية")

        addSection("التحقق الرسمي")

        addInfo(
            "الغرض",
            "التحقق من أهلية المستخدم للخدمات الحساسة وفق المتطلبات النظامية."
        )

        addInfo(
            "المصدر",
            "يجب أن يتم التحقق من خلال جهة حكومية أو مصدر رسمي مخول."
        )

        addInfo(
            "التفويض",
            "لا يجوز الوصول إلى بيانات حكومية أو خاصة دون التفويض والسلطة النظامية المطلوبة."
        )

        addInfo(
            "الموانع",
            "يمكن التحقق من وجود موانع قانونية موثقة ذات صلة بالخدمة، وفق ما يسمح به القانون."
        )

        addInfo(
            "الخصوصية",
            "لا يتم افتراض وجود مانع قانوني ولا يتم استخدام بيانات غير مصرح بالوصول إليها."
        )

        addCard(
            "📋 متطلبات الأهلية",
            "عرض المتطلبات العامة"
        ) {
            showEligibilityRequirements()
        }

        addCard(
            "💰 متطلبات الاستثمار",
            "متطلبات المشاركة الاستثمارية"
        ) {
            showInvestmentRequirements()
        }

        addCard(
            "🏗️ متطلبات التمويل",
            "متطلبات المشاركة التمويلية"
        ) {
            showFinancingRequirements()
        }

        addNavButton("↩️ الأمان") {
            showSensitiveAccessPolicy()
        }
    }

    private fun showEligibilityRequirements() {
        baseLayout("متطلبات الأهلية")

        addSection("المتطلبات")

        addInfo(
            "الهوية",
            "يجب استخدام وسيلة تحقق هوية معتمدة عندما تتطلب الخدمة ذلك."
        )

        addInfo(
            "المصدر الرسمي",
            "يجب أن يكون مصدر التحقق جهة رسمية مخولة."
        )

        addInfo(
            "الموافقة",
            "يجب الحصول على الموافقات والتفويضات المطلوبة قبل الوصول إلى البيانات المقيدة."
        )

        addInfo(
            "النتيجة",
            "تُستخدم نتيجة التحقق فقط للغرض النظامي المحدد."
        )

        addInfo(
            "المراجعة",
            "أي نتيجة غير واضحة تحتاج إلى مراجعة الجهة المختصة، ولا تُعامل كتأكيد تلقائي."
        )

        addNavButton("↩️ التحقق من الأهلية") {
            showEligibilityVerification()
        }
    }

    private fun showInvestmentRequirements() {
        baseLayout("متطلبات الاستثمار")

        addSection("المشاركة الاستثمارية")

        addInfo(
            "الأهلية",
            "استيفاء شروط الأهلية المعتمدة للخدمة."
        )

        addInfo(
            "التحقق",
            "إكمال التحقق الرسمي المطلوب قبل المشاركة."
        )

        addInfo(
            "الموافقة",
            "الحصول على الموافقات والاعتمادات المطلوبة."
        )

        addInfo(
            "المخاطر",
            "يجب عرض معلومات المخاطر والشروط قبل أي قرار استثماري."
        )

        addInfo(
            "التنفيذ",
            "لا تنفذ هذه الواجهة أي عملية مالية فعلية."
        )

        addNavButton("↩️ المشاركة الاستثمارية") {
            showInvestmentParticipation()
        }
    }

    private fun showFinancingRequirements() {
        baseLayout("متطلبات التمويل")

        addSection("المشاركة في المشاريع التمويلية")

        addInfo(
            "الأهلية",
            "استيفاء شروط الأهلية المعتمدة."
        )

        addInfo(
            "التحقق",
            "إكمال التحقق الرسمي المطلوب."
        )

        addInfo(
            "الموافقة",
            "الحصول على الاعتمادات والتفويضات المطلوبة."
        )

        addInfo(
            "الشروط",
            "يجب عرض شروط المشروع والتكاليف والمخاطر والالتزامات بوضوح."
        )

        addInfo(
            "التنفيذ",
            "لا تنفذ هذه الشاشة عملية تمويل فعلية."
        )

        addNavButton("↩️ المشاركة التمويلية") {
            showFinancingParticipation()
        }
    }

    private fun showOwnerApproval() {
        baseLayout("اعتماد المالك")

        addSection("المراجعة والاعتماد")

        addInfo(
            "الاعتماد",
            "بعض الإجراءات الحساسة تحتاج إلى مراجعة واعتماد صاحب الصلاحية."
        )

        addInfo(
            "التنفيذ المالي",
            "لا يتم تنفيذ أي عملية مالية فعلية تلقائيًا."
        )

        addInfo(
            "الحماية",
            "يجب الحفاظ على حماية البيانات والشاشة أثناء مراجعة المعلومات الحساسة."
        )

        addCard(
            "📊 حالة المشروع",
            "عرض حالة الطلب أو المشروع"
        ) {
            showProjectStatus()
        }

        addNavButton("↩️ إدارة المنصة") {
            showManagerOffice()
        }
    }

        private fun showProjectStatus() {
        baseLayout("حالة المشروع")

        addSection("المتابعة")

        addInfo(
            "الحالة",
            "لا توجد عملية مالية فعلية مرتبطة بهذه الواجهة."
        )

        addInfo(
            "المراجعة",
            "يمكن استخدام هذا القسم لمتابعة حالة الطلب أو المشروع وفق الصلاحيات."
        )

        addInfo(
            "الاعتماد",
            "أي خطوة حساسة تحتاج إلى الاعتماد المطلوب قبل التنفيذ."
        )

        addCard(
            "📋 المستندات",
            "عرض المستندات المرتبطة بالمشروع"
        ) {
            showDocuments()
        }

        addCard(
            "🔍 التشخيص",
            "عرض حالة الجهاز والاتصال"
        ) {
            showDiagnostics()
        }

        addNavButton("↩️ النظام المالي") {
            showPrivateFinancialSystem()
        }
    }

    private fun showDiagnostics() {
        baseLayout("التشخيص")

        addSection("تشخيص التطبيق والجهاز")

        addInfo(
            "التطبيق",
            "يعمل الفحص من داخل التطبيق لعرض المعلومات المتاحة عن الاتصال وقدرات الجهاز."
        )

        addCard(
            "🌐 الاتصال",
            "فحص حالة الشبكة"
        ) {
            showOnline()
        }

        addCard(
            "📱 قدرات الجهاز",
            "فحص قدرات الجهاز المتاحة"
        ) {
            showDeviceCapabilities()
        }

        addCard(
            "📊 حالة المشروع",
            "عرض حالة المشروع"
        ) {
            showProjectStatus()
        }

        addNavButton("↩️ قائمة الأمان") {
            showSecurityChecklist()
        }
    }

    private fun showDeviceCapabilities() {
        baseLayout("قدرات الجهاز")

        addSection("الفحص التلقائي")

        val manager =
            getSystemService(CONNECTIVITY_SERVICE)
                    as ConnectivityManager

        val network =
            manager.activeNetwork

        val capabilities =
            manager.getNetworkCapabilities(network)

        val hasNetwork =
            capabilities != null

        addInfo(
            "الاتصال",
            if (hasNetwork) {
                "يوجد اتصال شبكي متاح."
            } else {
                "لا يوجد اتصال شبكي حاليًا."
            }
        )

        if (capabilities != null) {
            val wifi =
                capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                )

            val mobile =
                capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_CELLULAR
                )

            val validated =
                capabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED
                )

            addInfo(
                "Wi-Fi",
                if (wifi) "متاح" else "غير مستخدم حاليًا"
            )

            addInfo(
                "شبكة الهاتف",
                if (mobile) "متاحة" else "غير مستخدمة حاليًا"
            )

            addInfo(
                "التحقق من الإنترنت",
                if (validated) "الاتصال بالإنترنت مؤكد" else "غير مؤكد"
            )
        }

        addInfo(
            "التوافق",
            "تتكيف الخدمات مع قدرات الجهاز والاتصال المتاحين بدل افتراض توفر كل الإمكانات."
        )

        addNavButton("↩️ التشخيص") {
            showDiagnostics()
        }
    }

    private fun showDocuments() {
        baseLayout("المستندات")

        addSection("المستندات والمعلومات")

        addInfo(
            "الوصول",
            "يتم عرض المستندات وفق الصلاحيات ومستوى الوصول المعتمد."
        )

        addInfo(
            "المستندات الحساسة",
            "تخضع المستندات الحساسة للحماية والضوابط القانونية المناسبة."
        )

        addInfo(
            "الخصوصية",
            "لا يتم عرض بيانات أو مستندات مقيدة دون الصلاحية اللازمة."
        )

        addCard(
            "🛡️ الأمان",
            "مراجعة حماية المعلومات"
        ) {
            showSecurityChecklist()
        }

        addNavButton("↩️ الرئيسية") {
            showHome()
        }
    }

    private fun showCustoms() {
        baseLayout("الجمارك والشحن")

        addSection("الشحن والجمارك")

        addInfo(
            "الشحن",
            "يمكن عرض تقديرات الشحن وفق المسار والبلد والبيانات المتاحة."
        )

        addInfo(
            "الجمارك",
            "أي تقدير للرسوم الجمركية هو تقدير إرشادي ويجب الرجوع إلى الجهات الرسمية."
        )

        addInfo(
            "القواعد الإقليمية",
            "قد تختلف متطلبات الشحن والاستيراد والتصدير حسب الدولة والمنطقة."
        )

        addInfo(
            "المعاملات",
            "يجب الالتزام بالقوانين والأنظمة المحلية والدولية ذات الصلة."
        )

        addNavButton("↩️ المنتجات والأقسام") {
            showProducts()
        }
    }

        private fun showCountryPackage() {
        baseLayout("حزمة الدولة")

        addSection("النظام العالمي")

        addInfo(
            "الأساس",
            "المنصة مصممة كأساس عالمي يمكن أن يعمل مع حزم خاصة بالدول والمناطق."
        )

        addInfo(
            "السودان",
            "يمكن أن تتضمن حزمة السودان الخدمات والجهات والميزات الخاصة بها وفق القوانين المحلية."
        )

        addInfo(
            "الدول الأخرى",
            "يتم تفعيل أو تعطيل الميزات الخاصة بالدولة حسب الحزمة المعتمدة."
        )

        addInfo(
            "المعاملات الدولية",
            "تختلف القواعد والرسوم والشحن والجمارك حسب الدولة والمسار."
        )

        addCard(
            "📦 الشحن والجمارك",
            "المعلومات الإرشادية للشحن والجمارك"
        ) {
            showCustoms()
        }

        addCard(
            "🌐 الاتصال",
            "فحص الاتصال الحالي"
        ) {
            showOnline()
        }

        addNavButton("↩️ الرئيسية") {
            showHome()
        }
    }

    private fun showRegionalRules() {
        baseLayout("القواعد الإقليمية")

        addSection("القواعد حسب المنطقة")

        addInfo(
            "الاختلافات",
            "قد تختلف القوانين والرسوم ومتطلبات التجارة والخدمات من منطقة إلى أخرى."
        )

        addInfo(
            "التنبيه",
            "يجب عرض تنبيه مناسب عندما تختلف متطلبات الخدمة حسب الدولة أو المنطقة."
        )

        addInfo(
            "المصدر",
            "المعلومات القانونية والتنظيمية النهائية يجب أن تعتمد على المصادر الرسمية المختصة."
        )

        addCard(
            "📦 الشحن والجمارك",
            "عرض المعلومات الإرشادية"
        ) {
            showCustoms()
        }

        addNavButton("↩️ الرئيسية") {
            showHome()
        }
    }

    private fun showBadgerOffice() {
        baseLayout("BADGER")

        addSection("إدارة BADGER")

        addInfo(
            "المنتج",
            "BADGER منتج مالي مستقل مخصص للتطوير المؤسسي والتقديم للجهات المالية وفق المتطلبات النظامية."
        )

        addInfo(
            "الحماية",
            "تطبق الحماية الشاملة للتطبيق وقواعد قفل الجلسة."
        )

        addCard(
            "💰 النظام المالي",
            "المعلومات المالية المسموح بها"
        ) {
            showPrivateFinancialSystem()
        }

        addCard(
            "🛡️ حماية الخدمات الحساسة",
            "قواعد الوصول والحماية"
        ) {
            showSensitiveAccessPolicy()
        }

        addNavButton("↩️ إدارة المنصة") {
            showManagerOffice()
        }
    }

    private fun showAboutArchitecture() {
        baseLayout("هيكل المنصة")

        addSection("التنظيم")

        addInfo(
            "التجارة",
            "المنتجات والإعلانات والخدمات التجارية ضمن أقسام منظمة."
        )

        addInfo(
            "الخدمات",
            "الخدمات العامة والمهنية والمعلوماتية حسب الدولة والمنطقة."
        )

        addInfo(
            "الأمان",
            "الحماية مطبقة على مستوى التطبيق بالكامل."
        )

        addInfo(
            "الذكاء",
            "يشمل المشروع الذكاء البشري وCTM AI وفق الصلاحيات المعتمدة."
        )

        addInfo(
            "BADGER",
            "منتج مستقل ضمن منظومة المشروع، مع ضوابط خاصة للخدمات المالية."
        )

        addNavButton("↩️ معلومات التطبيق") {
            showAppInformation()
        }
    }

    private fun showOwnerControls() {
        baseLayout("صلاحيات المالك")

        addSection("الاعتماد والإدارة")

        addInfo(
            "المالك",
            "المالك ياسر حسن وشركاؤه"
        )

        addInfo(
            "الاعتماد",
            "التغييرات الحساسة والأقسام الخاصة تخضع للاعتماد والصلاحيات المناسبة."
        )

        addInfo(
            "الاختبار",
            "يتم اعتماد النسخ والتغييرات قبل استبدال النسخة المستخدمة."
        )

        addInfo(
            "المالية",
            "لا يتم تنفيذ العمليات المالية الفعلية من هذه الواجهة."
        )

        addCard(
            "📊 حالة المشروع",
            "متابعة حالة المشروع"
        ) {
            showProjectStatus()
        }

        addCard(
            "🛡️ الأمن",
            "مراجعة الأمان"
        ) {
            showSecurityChecklist()
        }

        addNavButton("↩️ إدارة المنصة") {
            showManagerOffice()
        }
    }

    private fun showHomeNavigation() {
        baseLayout("التنقل")

        addSection("أقسام المنصة")

        addCard(
            "🏠 الرئيسية",
            "الصفحة الرئيسية"
        ) {
            showHome()
        }

        addCard(
            "🔎 البحث",
            "البحث داخل المنصة"
        ) {
            showSearch()
        }

        addCard(
            "⭐ النقاط",
            "النقاط والمشاركة"
        ) {
            showPoints()
        }

        addCard(
            "👤 الضيف",
            "وضع الضيف"
        ) {
            showGuestMode()
        }

        addCard(
            "🛡️ الأمان",
            "الأمان والخصوصية"
        ) {
            showSafety()
        }

        addNavButton("🏠 الرئيسية") {
            showHome()
        }
    }

    private fun showManagerTools() {
        baseLayout("أدوات الإدارة")

        addSection("الأدوات")

        addCard(
            "🛡️ الأمن والمعلومات",
            "إدارة ومراجعة الأمان"
        ) {
            showSecurityOffice()
        }

        addCard(
            "⚖️ مكتب النائب العام",
            "المعلومات والصلاحيات"
        ) {
            showAttorneyOffice()
        }

        addCard(
            "🦡 BADGER",
            "إدارة منتج BADGER"
        ) {
            showBadgerOffice()
        }

        addCard(
            "📋 اعتماد المالك",
            "الاعتمادات المطلوبة"
        ) {
            showOwnerApproval()
        }

        addCard(
            "🔍 التشخيص",
            "فحص التطبيق والجهاز"
        ) {
            showDiagnostics()
        }

        addNavButton("↩️ إدارة المنصة") {
            showManagerOffice()
        }
    }

    private fun showSystemSummary() {
        baseLayout("ملخص النظام")

        addSection("CENTRAL MARKET")

        addInfo(
            "التجارة والخدمات",
            "منتجات وخدمات وإعلانات وأقسام متعددة."
        )

        addInfo(
            "الذكاء",
            "الذكاء البشري وCTM AI."
        )

        addInfo(
            "المجتمع",
            "الدعم المجتمعي والمبادرات."
        )

        addInfo(
            "BADGER",
            "منظومة مالية مستقلة بضوابط خاصة."
        )

        addInfo(
            "الأمان",
            "حماية شاشة شاملة وقفل الجلسة بعد 10 دقائق خارج التطبيق."
        )

        addInfo(
            "الأهلية",
            "الخدمات الحساسة يمكن أن تتطلب تحققًا رسميًا وتفويضًا نظاميًا."
        )

        addNavButton("↩️ الرئيسية") {
            showHome()
        }
    }
}
