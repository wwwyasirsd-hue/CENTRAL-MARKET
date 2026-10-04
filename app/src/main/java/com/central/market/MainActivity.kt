package com.central.market

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.os.Build
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    // =========================
    // الألوان الأساسية
    // =========================

    private val navy = Color.rgb(18, 42, 66)
    private val blue = Color.rgb(32, 104, 170)
    private val gold = Color.rgb(205, 157, 45)
    private val green = Color.rgb(38, 130, 85)
    private val red = Color.rgb(180, 65, 65)

    private val background = Color.rgb(246, 249, 252)
    private val white = Color.WHITE
    private val textDark = Color.rgb(30, 43, 55)
    private val muted = Color.rgb(92, 108, 122)

    // =========================
    // الواجهة
    // =========================

    private lateinit var content: LinearLayout

    // =========================
    // حماية التطبيق
    // =========================

    private val idleHandler = Handler(Looper.getMainLooper())

    private val outsideAppTimeout = 10 * 60 * 1000L

    private var outsideAppStartedAt = 0L
    private var sessionLocked = false

    private val outsideAppLockRunnable = Runnable {
        lockSession()
    }

    // =========================
    // بداية التطبيق
    // =========================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // حماية الشاشة على مستوى التطبيق بالكامل
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        showHome()
    }

        // =========================
    // دورة حياة التطبيق والحماية
    // =========================

    override fun onStart() {
        super.onStart()

        idleHandler.removeCallbacks(outsideAppLockRunnable)

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

        idleHandler.removeCallbacks(outsideAppLockRunnable)

        idleHandler.postDelayed(
            outsideAppLockRunnable,
            outsideAppTimeout
        )
    }

    override fun onDestroy() {
        idleHandler.removeCallbacks(
            outsideAppLockRunnable
        )
        super.onDestroy()
    }

    private fun lockSession() {
        if (sessionLocked) {
            return
        }

        sessionLocked = true

        idleHandler.removeCallbacks(
            outsideAppLockRunnable
        )

        showSessionLocked()
    }

    private fun unlockSession() {
        sessionLocked = false
        outsideAppStartedAt = 0L

        showHome()
    }

    // =========================
    // شاشة قفل الجلسة
    // =========================

    private fun showSessionLocked() {
        val root = LinearLayout(this)

        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setPadding(28, 28, 28, 28)
        root.setBackgroundColor(background)

        val icon = TextView(this)

        icon.text = "🔒"
        icon.textSize = 48f
        icon.gravity = Gravity.CENTER

        root.addView(
            icon,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val title = TextView(this)

        title.text = "الجلسة مقفلة"
        title.textSize = 25f
        title.setTypeface(null, Typeface.BOLD)
        title.setTextColor(navy)
        title.gravity = Gravity.CENTER
        title.setPadding(0, 18, 0, 12)

        root.addView(title)

        val message = TextView(this)

        message.text =
            "تم قفل الجلسة بعد مرور 10 دقائق " +
            "خارج التطبيق.\n\n" +
            "حماية الشاشة مفعلة على مستوى التطبيق."

        message.textSize = 16f
        message.setTextColor(textDark)
        message.gravity = Gravity.CENTER
        message.setPadding(10, 10, 10, 24)

        root.addView(message)

        val continueButton = Button(this)

        continueButton.text = "🔐 متابعة الجلسة"
        continueButton.textSize = 16f
        continueButton.setTextColor(white)
        continueButton.background =
            roundedBackground(
                blue,
                blue,
                18
            )

        continueButton.setOnClickListener {
            unlockSession()
        }

        root.addView(
            continueButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        )

        val homeButton = Button(this)

        homeButton.text = "🏠 العودة إلى الرئيسية"
        homeButton.textSize = 15f
        homeButton.setTextColor(navy)
        homeButton.background =
            roundedBackground(
                white,
                gold,
                18
            )

        homeButton.setOnClickListener {
            sessionLocked = false
            outsideAppStartedAt = 0L
            showHome()
        }

        root.addView(
            homeButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        )

        setContentView(root)
    }

    // =========================
    // التخطيط الأساسي
    // =========================

    private fun baseLayout(): LinearLayout {

        val root = LinearLayout(this)

        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(background)

        val header = LinearLayout(this)

        header.orientation = LinearLayout.VERTICAL
        header.gravity = Gravity.CENTER
        header.setPadding(16, 18, 16, 12)

        val headerBackground =
            GradientDrawable().apply {
                setColor(white)
                cornerRadius = 0f
            }

        header.background = headerBackground

        val logo = TextView(this)

        logo.text = "CENTRAL"
        logo.textSize = 27f
        logo.setTypeface(null, Typeface.BOLD)
        logo.setTextColor(navy)
        logo.gravity = Gravity.CENTER

        val market = TextView(this)

        market.text = "MARKET"
        market.textSize = 13f
        market.setTypeface(null, Typeface.BOLD)
        market.setTextColor(blue)
        market.gravity = Gravity.CENTER

        val line = TextView(this)

        line.text = "━━━━━━━━━━━━"
        line.textSize = 10f
        line.setTextColor(gold)
        line.gravity = Gravity.CENTER

        header.addView(logo)
        header.addView(market)
        header.addView(line)

        root.addView(header)

        content = LinearLayout(this)

        content.orientation =
            LinearLayout.VERTICAL

        content.setPadding(
            16,
            10,
            16,
            24
        )

        val scroll = ScrollView(this)

        scroll.isFillViewport = true
        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        val navigation = LinearLayout(this)

        navigation.orientation =
            LinearLayout.HORIZONTAL

        navigation.setPadding(
            4,
            4,
            4,
            4
        )

        navigation.setBackgroundColor(white)

        addNavButton(
            navigation,
            "⌂\nالرئيسية"
        ) {
            showHome()
        }

        addNavButton(
            navigation,
            "⌕\nالبحث"
        ) {
            showSearch()
        }

        addNavButton(
            navigation,
            "♡\nالمفضلة"
        ) {
            showFavorites()
        }

        addNavButton(
            navigation,
            "●\nالحساب"
        ) {
            showAccount()
        }

        root.addView(navigation)

        return root
    }

    // =========================
    // أزرار التنقل
    // =========================

    private fun addNavButton(
        parent: LinearLayout,
        title: String,
        action: () -> Unit
    ) {

        val button = Button(this)

        button.text = title
        button.textSize = 10f
        button.setTextColor(navy)

        button.background =
            roundedBackground(
                white,
                gold,
                12
            )

        button.setOnClickListener {
            action()
        }

        parent.addView(
            button,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(2, 2, 2, 2)
            }
        )
    }

    // =========================
    // العناوين
    // =========================

    private fun addSection(title: String) {

        val text = TextView(this)

        text.text = title
        text.textSize = 20f
        text.setTypeface(null, Typeface.BOLD)
        text.setTextColor(navy)

        text.setPadding(
            4,
            18,
            4,
            9
        )

        content.addView(text)
    }

    // =========================
    // البطاقات
    // =========================

    private fun addCard(
        title: String,
        description: String,
        action: () -> Unit
    ) {

        val button = Button(this)

        button.text =
            "$title\n$description"

        button.textSize = 14f
        button.setTextColor(textDark)
        button.gravity =
            Gravity.CENTER_VERTICAL

        button.setPadding(
            16,
            18,
            16,
            18
        )

        button.background =
            roundedBackground(
                white,
                blue,
                18
            )

        button.setOnClickListener {
            action()
        }

        content.addView(
            button,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(
                    0,
                    5,
                    0,
                    5
                )
            }
        )
    }

    // =========================
    // خلفية البطاقات
    // =========================

    private fun roundedBackground(
        fill: Int,
        stroke: Int,
        radius: Int
    ): GradientDrawable {

        return GradientDrawable().apply {

            setColor(fill)

            cornerRadius =
                radius.toFloat()

            setStroke(
                2,
                stroke
            )
        }
    }

    // =========================
    // معلومات نصية
    // =========================

    private fun addInfo(
        textValue: String
    ) {

        val text = TextView(this)

        text.text = textValue
        text.textSize = 16f
        text.setTextColor(textDark)

        text.setPadding(
            10,
            10,
            10,
            18
        )

        content.addView(text)
    }

    private fun addInfo(
        title: String,
        description: String
    ) {

        val text = TextView(this)

        text.text =
            "$title\n$description"

        text.textSize = 16f
        text.setTextColor(textDark)

        text.setPadding(
            10,
            10,
            10,
            18
        )

        content.addView(text)
    }

    // =========================
    // الرسائل
    // =========================

    private fun showMessage(
        message: String
    ) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

        // =========================
    // الرئيسية
    // =========================

    private fun showHome() {

        val root = baseLayout()

        setContentView(root)

        addSection("🌍 CENTRAL MARKET")

        addInfo(
            "منصة واحدة .. عالم من الفرص.",
            "منظومة تجارية وخدمية قابلة للتوسع محليًا وعالميًا."
        )

        // بطاقة الحالة العامة
        addCard(
            "📶 حالة الاتصال",
            "فحص الاتصال وقدرات الشبكة والجهاز"
        ) {
            showOnline()
        }

        // الوضع الضيف
        addCard(
            "👤 وضع الضيف",
            "تصفح الخدمات العامة بدون الدخول إلى الحساب"
        ) {
            showGuestMode()
        }

        // الأمان والخصوصية
        addCard(
            "🔒 الأمان والخصوصية",
            "حماية التطبيق والبيانات والصلاحيات"
        ) {
            showSafety()
        }

        addSection("🛍️ الأسواق والخدمات")

        addCard(
            "🚗 المركبات والشاحنات",
            "بيع وشراء وخدمات المركبات"
        ) {
            showCategory(
                "🚗 المركبات والشاحنات"
            )
        }

        addCard(
            "📱 الهواتف والإلكترونيات",
            "أجهزة وتقنيات وإكسسوارات"
        ) {
            showCategory(
                "📱 الهواتف والإلكترونيات"
            )
        }

        addCard(
            "🍽️ المطاعم والتوصيل",
            "مطاعم وطلبات وخدمات توصيل"
        ) {
            showCategory(
                "🍽️ المطاعم والتوصيل"
            )
        }

        addCard(
            "📢 الإعلانات",
            "إعلانات BRONZE وSILVER وGOLD"
        ) {
            showAds()
        }

        addCard(
            "🛠️ الخدمات",
            "خدمات مهنية وتجارية متنوعة"
        ) {
            showCategory(
                "🛠️ الخدمات"
            )
        }

        addSection("🌾 القطاعات الأساسية")

        addCard(
            "🌾 الزراعة والثروة الحيوانية",
            "منتجات وخدمات القطاع الزراعي والحيواني"
        ) {
            showCategory(
                "🌾 الزراعة والثروة الحيوانية"
            )
        }

        addCard(
            "🐟 الأسماك",
            "بيع وخدمات ومنتجات الأسماك"
        ) {
            showCategory(
                "🐟 الأسماك"
            )
        }

        addCard(
            "🏗️ البناء والجملة",
            "مواد البناء وتجارة الجملة"
        ) {
            showCategory(
                "🏗️ البناء والجملة"
            )
        }

        addCard(
            "🏥 الصحة",
            "خدمات ومعلومات صحية عامة"
        ) {
            showCategory(
                "🏥 الصحة"
            )
        }

        addCard(
            "⚽ الرياضة",
            "رياضة ومرافق وخدمات رياضية"
        ) {
            showCategory(
                "⚽ الرياضة"
            )
        }

        addCard(
            "💡 الكهرباء والمياه",
            "خدمات واحتياجات الكهرباء والمياه"
        ) {
            showCategory(
                "💡 الكهرباء والمياه"
            )
        }

        addCard(
            "🎓 التعليم",
            "تعليم وتدريب ومصادر تعليمية"
        ) {
            showCategory(
                "🎓 التعليم"
            )
        }

        addCard(
            "✈️ السفر",
            "سفر وحجوزات وخدمات مرتبطة"
        ) {
            showCategory(
                "✈️ السفر"
            )
        }

        addCard(
            "⭐ النقاط",
            "نظام النقاط والمكافآت"
        ) {
            showPoints()
        }

        addSection("🧠 المشاريع والمنظومة")

        addCard(
            "🧠 Human Superintelligence",
            "الذكاء البشري والمشاريع والأفكار"
        ) {
            showHumanIntelligence()
        }

        addCard(
            "🤖 CTM AI",
            "المساعد الذكي الرسمي للمنصة"
        ) {
            showCtmAi()
        }

        addCard(
            "❤️ العمل الخيري",
            "دعم الأيتام والمحتاجين والمبادرات"
        ) {
            showCharity()
        }

        addCard(
            "🍳 المطبخ",
            "محتوى وخدمات ومنتجات المطبخ"
        ) {
            showKitchen()
        }

        addCard(
            "🏢 مكتب الإدارة",
            "إدارة المنصة وفق الصلاحيات"
        ) {
            showManagerOffice()
        }

        addCard(
            "🦡 BADGER",
            "منظومة مالية مستقلة قابلة للتكامل البنكي"
        ) {
            showBadger()
        }

        addCard(
            "🛡️ مكتب الأمن والمعلومات",
            "الأمان ومتابعة المخاطر والحوادث"
        ) {
            showSecurityOffice()
        }

        addCard(
            "⚖️ المكتب القانوني",
            "الصلاحيات والمراجعات والإجراءات القانونية"
        ) {
            showAttorneyOffice()
        }

        addCard(
            "💰 النظام المالي الخاص",
            "إدارة المعلومات المالية والصلاحيات"
        ) {
            showPrivateFinancialSystem()
        }

        addSection("⚖️ الخدمات الحساسة")

        addCard(
            "📈 المشاركة الاستثمارية",
            "طلب المشاركة وفق الأهلية والإجراءات النظامية"
        ) {
            showInvestmentParticipation()
        }

        addCard(
            "🏗️ المشاركة في المشاريع التمويلية",
            "طلبات التمويل والمشاركة وفق الضوابط"
        ) {
            showFinancingParticipation()
        }

        addCard(
            "🪪 التحقق من الأهلية",
            "التحقق الرسمي عند توفر التكامل والتفويض"
        ) {
            showEligibilityVerification()
        }

        addCard(
            "🔐 سياسة الوصول الحساس",
            "قواعد الحماية والصلاحيات للخدمات الحساسة"
        ) {
            showSensitiveAccessPolicy()
        }

        addSection("⚙️ حالة النظام")

        addCard(
            "📋 قواعد التطبيق",
            "قواعد الاستخدام والحماية والصلاحيات"
        ) {
            showAppRules()
        }

        addCard(
            "🔎 تشخيص الجهاز والاتصال",
            "معلومات تشغيلية تساعد على توافق التطبيق"
        ) {
            showDiagnostics()
        }

        addCard(
            "📊 حالة المشروع",
            "معلومات عامة عن حالة المنصة"
        ) {
            showProjectStatus()
        }

        addInfo(
            "المالك",
            "المالك ياسر حسن وشركاؤه"
        )
    }

    // =========================
    // الاتصال والشبكة
    // =========================

    private fun showOnline() {

        val root = baseLayout()

        setContentView(root)

        addSection(
            "📶 الاتصال وقدرات الشبكة"
        )

        val manager =
            getSystemService(
                CONNECTIVITY_SERVICE
            ) as ConnectivityManager

        val network =
            manager.activeNetwork

        val capabilities =
            network?.let {
                manager.getNetworkCapabilities(it)
            }

        val connected =
            capabilities != null

        if (connected) {

            addInfo(
                "الحالة",
                "🟢 الاتصال متاح"
            )

            val wifi =
                capabilities?.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                ) == true

            val mobile =
                capabilities?.hasTransport(
                    NetworkCapabilities.TRANSPORT_CELLULAR
                ) == true

            val ethernet =
                capabilities?.hasTransport(
                    NetworkCapabilities.TRANSPORT_ETHERNET
                ) == true

            val connectionType =
                when {
                    wifi -> "Wi-Fi"
                    mobile -> "شبكة الهاتف"
                    ethernet -> "Ethernet"
                    else -> "اتصال آخر"
                }

            addInfo(
                "نوع الاتصال",
                connectionType
            )

            val internet =
                capabilities?.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                ) == true

            val validated =
                capabilities?.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED
                ) == true

            addInfo(
                "الإنترنت",
                if (internet)
                    "متاح"
                else
                    "غير مؤكد"
            )

            addInfo(
                "الوصول الفعلي للإنترنت",
                if (validated)
                    "متاح"
                else
                    "غير مؤكد"
            )

        } else {

            addInfo(
                "الحالة",
                "🔴 لا يوجد اتصال حاليًا"
            )

            addInfo(
                "وضع العمل",
                "يمكن استخدام الوظائف المحلية التي لا تحتاج إلى اتصال."
            )
        }

        addSection(
            "📡 الاستخدام الذكي للبيانات"
        )

        addInfo(
            "الوضع منخفض البيانات",
            "تجنب تحميل محتوى ثقيل عند ضعف الاتصال، " +
                    "مع إبقاء الوظائف الأساسية متاحة قدر الإمكان."
        )

        addInfo(
            "الخدمات السحابية",
            "لا يتم اعتبار أي خدمة خارجية متصلة فعليًا " +
                    "إلا بعد وجود التكامل الرسمي."
        )

        addCard(
            "📱 تشخيص الجهاز",
            "عرض معلومات التشغيل والتوافق"
        ) {
            showDiagnostics()
        }

        addCard(
            "🏠 العودة للرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

    // =========================
    // وضع الضيف
    // =========================

    private fun showGuestMode() {

        val root = baseLayout()

        setContentView(root)

        addSection(
            "👤 وضع الضيف"
        )

        addInfo(
            "الوصول العام",
            "يمكن للزائر استعراض الأقسام العامة " +
                    "دون الوصول إلى البيانات الخاصة."
        )

        addInfo(
            "الخصوصية",
            "الوظائف التي تتطلب حسابًا أو صلاحية " +
                    "لا تُتاح من وضع الضيف."
        )

        addCard(
            "🛍️ استعراض المنتجات",
            "الانتقال إلى الأسواق العامة"
        ) {
            showProducts()
        }

        addCard(
            "🔎 البحث",
            "البحث في المحتوى المتاح للضيف"
        ) {
            showSearch()
        }

        addCard(
            "🔐 تسجيل الدخول",
            "الوصول إلى وظائف الحساب"
        ) {
            showLogin()
        }

        addCard(
            "🏠 العودة للرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }
    
        // =========================
    // الأمان والخصوصية
    // =========================

    private fun showSafety() {

        val root = baseLayout()

        setContentView(root)

        addSection(
            "🔒 الأمان والخصوصية"
        )

        addInfo(
            "حماية الشاشة",
            "الحماية مفعلة على مستوى التطبيق بالكامل، " +
                    "ولا يسمح التطبيق بالتقاط الشاشة أو تسجيلها."
        )

        addInfo(
            "قفل الجلسة",
            "عند خروج التطبيق من الواجهة يبدأ احتساب " +
                    "10 دقائق، وبعدها يتم قفل الجلسة."
        )

        addInfo(
            "البيانات",
            "تتم حماية المعلومات وفق الصلاحيات والوظائف " +
                    "المعتمدة في النظام."
        )

        addInfo(
            "الصلاحيات",
            "الخدمات الحساسة تحتاج إلى صلاحيات حقيقية " +
                    "عند ربط النظام بالخدمات الخلفية."
        )

        addCard(
            "🛡️ مكتب الأمن والمعلومات",
            "إدارة الأمان والمخاطر"
        ) {
            showSecurityOffice()
        }

        addCard(
            "📋 قائمة الأمان",
            "مراجعة عناصر الحماية"
        ) {
            showSecurityChecklist()
        }

        addCard(
            "🔐 سياسة الوصول",
            "الخدمات الحساسة والصلاحيات"
        ) {
            showSensitiveAccessPolicy()
        }

        addCard(
            "🏠 الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

    // =========================
    // المنتجات
    // =========================

    private fun showProducts() {

        val root = baseLayout()

        setContentView(root)

        addSection(
            "🛍️ المنتجات والأسواق"
        )

        addInfo(
            "الأسواق",
            "تصفح المنتجات والخدمات حسب القسم."
        )

        addCard(
            "🚗 المركبات والشاحنات",
            "مركبات وشاحنات وملحقاتها"
        ) {
            showCategory(
                "🚗 المركبات والشاحنات"
            )
        }

        addCard(
            "📱 الهواتف والإلكترونيات",
            "هواتف وأجهزة وإلكترونيات"
        ) {
            showCategory(
                "📱 الهواتف والإلكترونيات"
            )
        }

        addCard(
            "🍽️ المطاعم والتوصيل",
            "مطاعم وطلبات وخدمات توصيل"
        ) {
            showCategory(
                "🍽️ المطاعم والتوصيل"
            )
        }

        addCard(
            "📢 الإعلانات",
            "BRONZE / SILVER / GOLD"
        ) {
            showAds()
        }

        addCard(
            "🛠️ الخدمات",
            "خدمات مهنية وتجارية"
        ) {
            showCategory(
                "🛠️ الخدمات"
            )
        }

        addCard(
            "🌾 الزراعة والثروة الحيوانية",
            "منتجات وخدمات القطاع"
        ) {
            showCategory(
                "🌾 الزراعة والثروة الحيوانية"
            )
        }

        addCard(
            "🐟 الأسماك",
            "منتجات وخدمات الأسماك"
        ) {
            showCategory(
                "🐟 الأسماك"
            )
        }

            addCard(
        "🏗️ البناء والجملة",
        "مواد البناء وتجارة الجملة"
    ) {
        showCategory(
            "🏗️ البناء والجملة"
        )
    }

    addCard(
        "🏥 الصحة",
        "خدمات ومنتجات مرتبطة بالصحة"
    ) {
        showCategory(
            "🏥 الصحة"
        )
    }

    addCard(
        "⚽ الرياضة",
        "خدمات ومرافق رياضية"
    ) {
        showCategory(
            "⚽ الرياضة"
        )
    }

    addCard(
        "💡 الكهرباء والمياه",
        "خدمات واحتياجات الكهرباء والمياه"
    ) {
        showCategory(
            "💡 الكهرباء والمياه"
        )
    }

    addCard(
        "🎓 التعليم",
        "تعليم وتدريب ومصادر تعليمية"
    ) {
        showCategory(
            "🎓 التعليم"
        )
    }

    addCard(
        "✈️ السفر",
        "سفر وخدمات مرتبطة"
    ) {
        showCategory(
            "✈️ السفر"
        )
    }

    addCard(
        "⭐ النقاط",
        "النقاط والمكافآت"
    ) {
        showPoints()
    }

    addCard(
        "🏠 الرئيسية",
        "العودة إلى الصفحة الرئيسية"
    ) {
        showHome()
    }
}

    // =========================
    // القسم العام
    // =========================

    private fun showCategory(
        category: String
    ) {

        val root = baseLayout()

        setContentView(root)

        addSection(category)

        addInfo(
            "القسم",
            category
        )

        addInfo(
            "حالة القسم",
            "يمكن تجهيز هذا القسم لعرض المنتجات " +
                    "والخدمات عند توفر مصدر البيانات."
        )

        addCard(
            "📦 عرض التفاصيل",
            "استعراض محتوى القسم"
        ) {
            showDetails(category)
        }

        addCard(
            "➕ إضافة إعلان",
            "إضافة إعلان وفق الصلاحيات"
        ) {
            showAddAd()
        }

        addCard(
            "🔎 البحث",
            "البحث داخل القسم"
        ) {
            showSearch()
        }

        addCard(
            "📢 الإعلانات",
            "مشاهدة الإعلانات"
        ) {
            showAds()
        }

        addCard(
            "🏠 الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

    // =========================
    // تفاصيل القسم
    // =========================

    private fun showDetails(
        category: String
    ) {

        val root = baseLayout()

        setContentView(root)

        addSection(
            "📦 تفاصيل القسم"
        )

        addInfo(
            "القسم",
            category
        )

        addInfo(
            "البيانات",
            "تظهر المنتجات والخدمات الفعلية " +
                    "عند توفر مصدر البيانات المعتمد."
        )

        addInfo(
            "التجارة المحلية",
            "تدعم المنصة الخدمات التجارية المحلية " +
                    "وفق الأنظمة المعمول بها."
        )

        addInfo(
            "التجارة العالمية",
            "يمكن إضافة معلومات الشحن والجمارك " +
                    "والتقديرات عند توفر التكامل المناسب."
        )

        addCard(
            "📢 إضافة إعلان",
            "إنشاء إعلان"
        ) {
            showAddAd()
        }

        addCard(
            "🔎 البحث",
            "البحث داخل القسم"
        ) {
            showSearch()
        }

        addCard(
            "↩️ العودة للقسم",
            "العودة إلى القسم السابق"
        ) {
            showCategory(category)
        }
    }

        private fun showFavorites() {
        baseLayout("المفضلة")

        addSection("⭐ المفضلة")
        addInfo(
            "العناصر المحفوظة",
            "هنا تظهر المنتجات والخدمات التي تحفظها للرجوع إليها لاحقًا."
        )

        addCard(
            "🛍️ المنتجات المحفوظة",
            "الوصول السريع إلى العناصر المفضلة"
        ) {
            showProducts()
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
        }

            private fun showAccount() {
        baseLayout("الحساب")

        addSection("👤 الحساب")
        addInfo(
            "إدارة الحساب",
            "الوصول إلى بيانات المستخدم وإعدادات الخصوصية والأمان."
        )

        addCard(
            "🔐 تسجيل الدخول",
            "الدخول إلى الحساب"
        ) {
            showLogin()
        }

        addCard(
            "🛡️ الخصوصية والأمان",
            "مراجعة إعدادات الحماية"
        ) {
            showSafety()
        }

        addCard(
            "⭐ النقاط",
            "عرض النقاط والمزايا"
        ) {
            showPoints()
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showLogin() {
        baseLayout("تسجيل الدخول")

        addSection("🔐 تسجيل الدخول")
        addInfo(
            "الوصول الآمن",
            "هذه الواجهة مخصصة للدخول إلى الحساب."
        )

        val name = EditText(this)
        name.hint = "اسم المستخدم"
        content.addView(name)

        val loginButton = Button(this)
        loginButton.text = "دخول"
        loginButton.setOnClickListener {
            showMessage("تم إرسال طلب الدخول للمراجعة.")
        }
        content.addView(loginButton)

        addCard(
            "↩️ العودة",
            "العودة إلى الحساب"
        ) {
            showAccount()
        }
    }

    private fun showAds() {
        baseLayout("الإعلانات")

        addSection("📢 الإعلانات")
        addInfo(
            "مستويات الإعلان",
            "اختر مستوى الإعلان المناسب وفق ضوابط المنصة."
        )

        addCard("🥉 BRONZE", "إعلان أساسي") {
            showMessage("تم اختيار مستوى BRONZE.")
        }

        addCard("🥈 SILVER", "إعلان متقدم") {
            showMessage("تم اختيار مستوى SILVER.")
        }

        addCard("🥇 GOLD", "إعلان مميز") {
            showMessage("تم اختيار مستوى GOLD.")
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

        private fun showPoints() {
        baseLayout("النقاط")

        addSection("⭐ النقاط والمزايا")
        addInfo(
            "نظام النقاط",
            "متابعة النقاط والمزايا المتاحة للمستخدم."
        )

        addCard(
            "📊 رصيد النقاط",
            "عرض الرصيد الحالي"
        ) {
            showMessage("رصيد النقاط سيظهر هنا.")
        }

        addCard(
            "🎁 المزايا",
            "المزايا المرتبطة بالنقاط"
        ) {
            showMessage("المزايا متاحة وفق نظام المنصة.")
        }

        addCard(
            "📋 سجل النقاط",
            "مراجعة العمليات السابقة"
        ) {
            showMessage("سجل النقاط محفوظ داخل الحساب.")
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showHumanIntelligence() {
        baseLayout("الذكاء البشري")

        addSection("🧠 الذكاء البشري")
        addInfo(
            "منظومة الأفكار والخبرات",
            "استقبال الأفكار وتنظيم الخبرات وتحويل المقترحات إلى مشاريع قابلة للدراسة."
        )

        addCard(
            "💡 تقديم فكرة",
            "إرسال فكرة جديدة"
        ) {
            showIdea()
        }

        addCard(
            "📋 مراجعة الأفكار",
            "تنظيم ومتابعة الأفكار"
        ) {
            showMessage("تتم مراجعة الأفكار وفق نظام المنصة.")
        }

        addCard(
            "🤝 تكوين فريق",
            "ربط أصحاب الأفكار والخبرات"
        ) {
            showMessage("سيتم تنظيم الفرق وفق الصلاحيات.")
        }
    }

        private fun showIdea() {
        baseLayout("تقديم فكرة")

        addSection("💡 تقديم فكرة جديدة")
        addInfo(
            "منظومة الأفكار",
            "أدخل فكرة أو مقترحًا يمكن دراسته وتطويره داخل المنصة."
        )

        val ideaInput = EditText(this)
        ideaInput.hint = "اكتب الفكرة هنا"
        ideaInput.minLines = 4
        content.addView(ideaInput)

        val submitButton = Button(this)
        submitButton.text = "إرسال الفكرة"
        submitButton.setOnClickListener {
            if (ideaInput.text.toString().trim().isEmpty()) {
                showMessage("يرجى كتابة الفكرة أولًا.")
            } else {
                showMessage("تم تسجيل الفكرة للمراجعة.")
            }
        }
        content.addView(submitButton)

        addCard(
            "📊 حالة الفكرة",
            "متابعة مراحل دراسة الفكرة"
        ) {
            showMessage("حالة الفكرة تظهر بعد تسجيلها.")
        }

        addCard(
            "🧠 الخبراء",
            "الاستفادة من الخبرات المتخصصة"
        ) {
            showMessage("سيتم ربط الخبرات وفق صلاحيات المنصة.")
        }

        addCard(
            "🤝 التعاون",
            "إمكانية التعاون حول الأفكار"
        ) {
            showMessage("التعاون يخضع لقواعد المنصة.")
        }

        addCard(
            "↩️ الذكاء البشري",
            "العودة إلى منظومة الذكاء البشري"
        ) {
            showHumanIntelligence()
        }
    }

    private fun showManagerOffice() {
        baseLayout("مكتب المدير")

        addSection("🏢 مكتب المدير")
        addInfo(
            "إدارة المنصة",
            "مساحة إدارية لمتابعة المشاريع والأقسام والصلاحيات."
        )

        addCard(
            "👤 المالك",
            "المالك ياسر حسن وشركاؤه"
        ) {
            showOwnerApproval()
        }

        addCard(
            "📁 إدارة المشاريع",
            "متابعة المشاريع والمهام"
        ) {
            showProjectStatus()
        }

        addCard(
            "🧠 الذكاء البشري",
            "متابعة الأفكار والمشاريع"
        ) {
            showHumanIntelligence()
        }

        addCard(
            "🤖 CTM AI",
            "المساعد الذكي الرسمي للمنصة"
        ) {
            showCtmAi()
        }

        addCard(
            "🛡️ مكتب الأمن والمعلومات",
            "الأمان ومتابعة الحوادث"
        ) {
            showSecurityOffice()
        }

        addCard(
            "⚖️ المكتب القانوني",
            "المتابعة القانونية والصلاحيات"
        ) {
            showAttorneyOffice()
        }

        addCard(
            "💰 النظام المالي الخاص",
            "إدارة المعلومات المالية"
        ) {
            showPrivateFinancialSystem()
        }

        addCard(
            "📢 الإعلانات",
            "إدارة مستويات الإعلانات"
        ) {
            showAds()
        }

        addCard(
            "📄 الوثائق",
            "إدارة الوثائق والسجلات"
        ) {
            showDocuments()
        }

        addCard(
            "💙 العمل الخيري",
            "متابعة مبادرات الدعم"
        ) {
            showCharity()
        }

            private fun showDocuments() {
        baseLayout("الوثائق")

        addSection("📄 الوثائق والسجلات")
        addInfo(
            "إدارة الوثائق",
            "تنظيم الوثائق والمعلومات المرتبطة بأعمال المنصة."
        )

        addCard("📁 وثائق المشاريع", "عرض وثائق المشاريع") {
            showMessage("وثائق المشاريع تخضع للصلاحيات.")
        }

        addCard("📝 السجلات", "متابعة السجلات الإدارية") {
            showMessage("السجلات الإدارية محمية.")
        }

        addCard("🔐 الوثائق الحساسة", "حماية المعلومات الحساسة") {
            showSensitiveAccessPolicy()
        }

        addCard("↩️ مكتب المدير", "العودة إلى مكتب المدير") {
            showManagerOffice()
        }
    }

    private fun showBadger() {
        baseLayout("BADGER")

        addSection("🦡 BADGER 🌍")
        addInfo(
            "النظام المالي",
            "منظومة مالية مستقلة مخصصة لإدارة المعلومات والتحليلات المالية وفق الصلاحيات."
        )

        addCard(
            "👤 الحسابات",
            "إدارة الحسابات والملفات المالية"
        ) {
            showMessage("إدارة الحسابات تخضع للتحقق والصلاحيات.")
        }

        addCard(
            "💰 الودائع",
            "متابعة بيانات الودائع"
        ) {
            showMessage("بيانات الودائع للعرض والتحليل فقط.")
        }

        addCard(
            "📊 التحليل المالي",
            "تحليل المعلومات والمؤشرات"
        ) {
            showMessage("التحليل المالي لا ينفذ معاملات مالية.")
        }

        addCard(
            "🧾 العمولات",
            "متابعة العمولات المسجلة"
        ) {
            showMessage("العمولات يجب أن تكون موثقة تعاقديًا.")
        }

        addCard(
            "🧾 الإيصالات",
            "إدارة الإيصالات"
        ) {
            showMessage("الإيصالات تخضع للسجل والصلاحيات.")
        }

        addCard(
            "🛡️ مكافحة الاحتيال",
            "مراقبة المخاطر والعمليات غير المعتادة"
        ) {
            showSecurityOffice()
        }

        addCard(
            "📈 المشاركة الاستثمارية",
            "الوصول إلى متطلبات المشاركة"
        ) {
            showInvestmentParticipation()
        }

        addCard(
            "🏗️ المشاركة التمويلية",
            "الوصول إلى متطلبات التمويل"
        ) {
            showFinancingParticipation()
        }

        addCard(
            "⚖️ التحقق من الأهلية",
            "التحقق القانوني عبر المسارات الرسمية"
        ) {
            showEligibilityVerification()
        }

        addCard(
            "🔒 الحماية",
            "حماية المعلومات والشاشة"
        ) {
            showSensitiveAccessPolicy()
        }

        addCard(
            "👑 موافقة المالك",
            "لا تنفيذ مالي دون الموافقة المطلوبة"
        ) {
            showOwnerApproval()
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showSecurityOffice() {
        baseLayout("مكتب الأمن")

        addSection("🛡️ مكتب الأمن والمعلومات")
        addInfo(
            "الحماية والمراقبة",
            "متابعة أمن الحسابات والمعلومات والعمليات الحساسة."
        )

        addCard(
            "🔐 حماية الشاشة",
            "الحماية مفعلة على مستوى التطبيق بالكامل"
        ) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            showMessage("حماية الشاشة مفعلة.")
        }

        addCard(
            "⏱️ قفل خارج التطبيق",
            "السكون بعد 10 دقائق خارج التطبيق"
        ) {
            showMessage("إعداد القفل الخارجي مضبوط على 10 دقائق.")
        }

        addCard(
            "🚨 مكافحة الاحتيال",
            "مراقبة مؤشرات الاحتيال والمخاطر"
        ) {
            showMessage("المراقبة الأمنية مفعلة.")
        }

        addCard(
            "👤 صلاحيات الوصول",
            "إدارة الوصول حسب الصلاحية"
        ) {
            showMessage("الوصول يعتمد على الصلاحيات المعتمدة.")
        }

        addCard(
            "🤖 مراقبة CTM AI",
            "متابعة المخاطر والأخطاء"
        ) {
            showCtmAi()
        }

        addCard(
            "⚖️ التحقق القانوني",
            "التحقق عبر الجهات والمسارات الرسمية"
        ) {
            showEligibilityVerification()
        }

        addCard(
            "📋 قائمة الأمان",
            "مراجعة متطلبات الحماية"
        ) {
            showSecurityChecklist()
        }

        addCard(
            "↩️ مكتب المدير",
            "العودة إلى مكتب المدير"
        ) {
            showManagerOffice()
        }
    }

    private fun showAttorneyOffice() {
        baseLayout("المكتب القانوني")

        addSection("⚖️ المكتب القانوني")
        addInfo(
            "المتابعة القانونية",
            "إدارة المعلومات القانونية وفق الصلاحيات والأنظمة المعتمدة."
        )

        addCard(
            "📋 الحالات والحوادث",
            "متابعة الحالات المسجلة"
        ) {
            showMessage("الحالات القانونية تحتاج صلاحية معتمدة.")
        }

        addCard(
            "🏢 الشركات والمستثمرون",
            "تنظيم البيانات القانونية"
        ) {
            showMessage("البيانات القانونية تخضع للتحقق.")
        }

        addCard(
            "📨 الدعوات",
            "إدارة الدعوات والإجراءات"
        ) {
            showMessage("الدعوات تخضع للصلاحيات.")
        }

        addCard(
            "🔐 الصلاحيات",
            "مراجعة صلاحيات الوصول"
        ) {
            showMessage("لا يتم منح الصلاحيات من هذه الواجهة العامة.")
        }

        addCard(
            "⚖️ الأهلية",
            "متطلبات التحقق القانوني"
        ) {
            showEligibilityRequirements()
        }

        addCard(
            "🏢 مكتب المدير",
            "الارتباط الإداري"
        ) {
            showManagerOffice()
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showPrivateFinancialSystem() {
        baseLayout("النظام المالي الخاص")

        addSection("💼 النظام المالي الخاص")
        addInfo(
            "المعلومات المالية",
            "عرض وتنظيم البيانات المالية دون تنفيذ معاملات مالية حقيقية من التطبيق المحلي."
        )

        addCard(
            "📈 الاستثمارات",
            "متابعة معلومات الاستثمار"
        ) {
            showInvestmentParticipation()
        }

        addCard(
            "💳 المعاملات",
            "عرض بيانات المعاملات"
        ) {
            showMessage("المعاملات المالية الحقيقية تحتاج نظامًا مصرحًا ومتكاملًا.")
        }

        addCard(
            "💵 الدخل",
            "تنظيم بيانات الدخل"
        ) {
            showMessage("بيانات الدخل مخصصة للعرض والتنظيم.")
        }

        addCard(
            "🤝 صلاحيات الشركاء",
            "إدارة الوصول حسب الصلاحيات"
        ) {
            showMessage("صلاحيات الشركاء تحتاج تحققًا معتمدًا.")
        }

        addCard(
            "🏗️ التمويل",
            "المشاركة في المشاريع التمويلية"
        ) {
            showFinancingParticipation()
        }

        addCard(
            "🛡️ الحماية",
            "حماية المعلومات المالية"
        ) {
            showSensitiveAccessPolicy()
        }

        addCard(
            "👑 موافقة المالك",
            "الموافقة المطلوبة قبل التنفيذ"
        ) {
            showOwnerApproval()
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

        private fun showCtmAi() {
        baseLayout("CTM AI")

        addSection("🤖 CTM AI")
        addInfo(
            "المساعد الذكي الرسمي",
            "مساعد ذكي داخل المنصة للبحث والقراءة والمساعدة في المعلومات المسموح بها."
        )

        addCard(
            "🔎 البحث الذكي",
            "البحث داخل معلومات المنصة"
        ) {
            showMessage("البحث الذكي يعمل ضمن نطاق المعلومات المسموح بها.")
        }

        addCard(
            "🎙️ البحث الصوتي",
            "استخدام الصوت للبحث"
        ) {
            showMessage("البحث الصوتي يقرأ ويبحث في المعلومات المسموح بها.")
        }

        addCard(
            "🌍 الكلمات المحلية",
            "فهم المصطلحات المحلية"
        ) {
            showMessage("سيتم دعم المصطلحات المحلية ضمن حزمة اللغة والمنطقة.")
        }

        addCard(
            "📄 فحص الوثائق",
            "مساعدة في تنظيم الوثائق"
        ) {
            showDocuments()
        }

        addCard(
            "🛡️ مراقبة المخاطر",
            "متابعة الأخطاء والمخاطر"
        ) {
            showSecurityOffice()
        }

        addCard(
            "💡 اقتراحات المستخدم",
            "إرسال مقترح لتطوير المساعد"
        ) {
            showIdea()
        }

        addCard(
            "⏸️ حالة المساعد",
            "إمكانية الإيقاف أو التفعيل وفق الصلاحية"
        ) {
            showMessage("إدارة حالة CTM AI تخضع للصلاحيات المعتمدة.")
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showCharity() {
        baseLayout("العمل الخيري")

        addSection("💙 العمل الخيري")
        addInfo(
            "دعم المحتاجين",
            "تنظيم المبادرات والمساهمات الخيرية بطريقة واضحة ومسؤولة."
        )

        addCard(
            "🤲 الأيتام",
            "دعم برامج الأيتام"
        ) {
            showMessage("برامج الدعم تخضع للتوثيق والضوابط.")
        }

        addCard(
            "❤️ المحتاجون",
            "مبادرات مساعدة المحتاجين"
        ) {
            showMessage("يتم تنظيم الدعم وفق البيانات والصلاحيات.")
        }

        addCard(
            "📋 المبادرات",
            "عرض المبادرات الخيرية"
        ) {
            showMessage("سيتم تنظيم المبادرات داخل سجل مخصص.")
        }

        addCard(
            "🔐 الحماية",
            "حماية بيانات المستفيدين"
        ) {
            showSensitiveAccessPolicy()
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showKitchen() {
        baseLayout("المطبخ")

        addSection("🍳 المطبخ")
        addInfo(
            "خدمات ومنتجات المطبخ",
            "قسم خاص بالمنتجات والخدمات المرتبطة بالطعام والمطابخ."
        )

        addCard(
            "🍲 المنتجات",
            "عرض منتجات المطبخ"
        ) {
            showCategory("🍳 المطبخ")
        }

        addCard(
            "🚚 التوصيل",
            "خدمات التوصيل"
        ) {
            showMessage("خدمات التوصيل تعتمد على المنطقة والتوفر.")
        }

        addCard(
            "🏪 الموردون",
            "التعامل مع الموردين"
        ) {
            showMessage("بيانات الموردين تخضع للتحقق.")
        }

        addCard(
            "📍 المنطقة",
            "تحديد الخدمات المتاحة"
        ) {
            showOnline()
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showEligibilityVerification() {
        baseLayout("التحقق من الأهلية")

        addSection("⚖️ التحقق الرسمي من الأهلية")
        addInfo(
            "التحقق القانوني",
            "أي تحقق قانوني رسمي يجب أن يتم عبر جهة حكومية أو مصدر مخول وبالإجراءات والتفويضات النظامية."
        )

        addCard(
            "📋 متطلبات التحقق",
            "عرض المتطلبات الأساسية"
        ) {
            showEligibilityRequirements()
        }

        addCard(
            "🏛️ الجهات الرسمية",
            "مصادر حكومية مخولة فقط"
        ) {
            showMessage("لا يتم الوصول إلى قواعد بيانات قانونية غير مصرح بها.")
        }

        addCard(
            "🔐 التفويض",
            "التحقق من وجود التفويض النظامي"
        ) {
            showMessage("أي تحقق رسمي يحتاج التفويض والإجراء القانوني المناسب.")
        }

        addCard(
            "📄 النتائج الموثقة",
            "التعامل مع النتائج الرسمية"
        ) {
            showMessage("النتائج الرسمية تحفظ وفق الصلاحيات والأنظمة.")
        }

        addCard(
            "🛡️ حماية البيانات",
            "حماية المعلومات القانونية"
        ) {
            showSensitiveAccessPolicy()
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showEligibilityRequirements() {
        baseLayout("متطلبات الأهلية")

        addSection("📋 متطلبات التحقق")
        addInfo(
            "ملاحظة قانونية",
            "هذه النسخة المحلية لا تحتوي على اتصال حكومي فعلي. أي تكامل مستقبلي يجب أن يكون رسميًا ومصرحًا به."
        )

        addCard(
            "🪪 بيانات الهوية",
            "تقديم البيانات المطلوبة بالطريقة النظامية"
        ) {
            showMessage("تُطلب البيانات فقط عند وجود أساس قانوني واضح.")
        }

        addCard(
            "🏛️ المصدر الرسمي",
            "الاعتماد على جهة مخولة"
        ) {
            showMessage("المصدر الرسمي يجب أن يكون معتمدًا ومصرحًا به.")
        }

        addCard(
            "⚖️ الموانع القانونية",
            "التحقق من الموانع الموثقة عند السماح بذلك"
        ) {
            showMessage("لا يتم استنتاج أو اختلاق أي مانع قانوني.")
        }

        addCard(
            "🔐 الموافقة والتفويض",
            "تطبيق الإجراءات النظامية"
        ) {
            showMessage("الموافقة والتفويض يحددان حسب النظام المختص.")
        }

        addCard(
            "🛡️ حماية الشاشة",
            "حماية بيانات التحقق"
        ) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            showMessage("حماية الشاشة مفعلة.")
        }

        addCard(
            "↩️ التحقق من الأهلية",
            "العودة إلى صفحة التحقق"
        ) {
            showEligibilityVerification()
        }
    }

    private fun showInvestmentParticipation() {
        baseLayout("المشاركة الاستثمارية")

        addSection("📈 المشاركة في الاستثمار")
        addInfo(
            "خدمة حساسة",
            "المشاركة الاستثمارية تخضع للتحقق والأهلية والموافقة النظامية."
        )

        addCard(
            "📋 المتطلبات",
            "مراجعة شروط المشاركة"
        ) {
            showInvestmentRequirements()
        }

        addCard(
            "⚖️ الأهلية",
            "التحقق الرسمي من الأهلية"
        ) {
            showEligibilityVerification()
        }

        addCard(
            "🔒 الحماية",
            "حماية بيانات المشاركة"
        ) {
            showSensitiveAccessPolicy()
        }

        addCard(
            "👑 الموافقة",
            "الموافقة المطلوبة قبل أي تنفيذ"
        ) {
            showOwnerApproval()
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showInvestmentRequirements() {
        baseLayout("متطلبات الاستثمار")

        addSection("📋 متطلبات المشاركة الاستثمارية")
        addInfo(
            "قبل المشاركة",
            "يجب مراجعة الشروط والأهلية والمخاطر والمعلومات الرسمية قبل أي قرار."
        )

        addCard(
            "⚖️ التحقق القانوني",
            "مراجعة الأهلية عبر المسار الرسمي"
        ) {
            showEligibilityVerification()
        }

        addCard(
            "📊 المعلومات",
            "مراجعة بيانات المشروع"
        ) {
            showMessage("بيانات المشروع يجب أن تكون موثقة ومحدثة.")
        }

        addCard(
            "🛡️ الحماية",
            "حماية الشاشة والبيانات"
        ) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            showMessage("حماية الشاشة مفعلة.")
        }

        addCard(
            "👑 الموافقة",
            "لا تنفيذ مالي تلقائي"
        ) {
            showOwnerApproval()
        }

        addCard(
            "↩️ العودة",
            "العودة إلى المشاركة الاستثمارية"
        ) {
            showInvestmentParticipation()
        }
    }

        private fun showFinancingParticipation() {
        baseLayout("المشاركة التمويلية")

        addSection("🏗️ المشاركة في المشاريع التمويلية")
        addInfo(
            "خدمة حساسة",
            "المشاركة التمويلية تحتاج مراجعة المشروع والأهلية والمخاطر والموافقات النظامية."
        )

        addCard(
            "📋 المتطلبات",
            "مراجعة شروط المشاركة"
        ) {
            showFinancingRequirements()
        }

        addCard(
            "⚖️ التحقق من الأهلية",
            "التحقق عبر المسارات الرسمية"
        ) {
            showEligibilityVerification()
        }

        addCard(
            "📊 معلومات المشروع",
            "مراجعة بيانات المشروع"
        ) {
            showMessage("بيانات المشروع يجب أن تكون موثقة ومحدثة.")
        }

        addCard(
            "🔒 حماية المعلومات",
            "حماية الشاشة والبيانات الحساسة"
        ) {
            showSensitiveAccessPolicy()
        }

        addCard(
            "👑 موافقة المالك",
            "لا تنفيذ مالي تلقائي"
        ) {
            showOwnerApproval()
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showFinancingRequirements() {
        baseLayout("متطلبات التمويل")

        addSection("📋 متطلبات المشاركة التمويلية")
        addInfo(
            "قبل المشاركة",
            "يجب مراجعة المشروع والبيانات والشروط والأهلية قبل أي قرار."
        )

        addCard(
            "🏗️ المشروع",
            "مراجعة تفاصيل المشروع"
        ) {
            showMessage("تفاصيل المشروع يجب أن تكون موثقة.")
        }

        addCard(
            "📊 البيانات المالية",
            "مراجعة المعلومات المالية"
        ) {
            showMessage("المعلومات المالية تعرض وفق الصلاحيات.")
        }

        addCard(
            "⚖️ الأهلية القانونية",
            "التحقق من الأهلية بالطريقة الرسمية"
        ) {
            showEligibilityVerification()
        }

        addCard(
            "🔐 التفويض",
            "التأكد من الصلاحيات المطلوبة"
        ) {
            showMessage("أي تحقق رسمي يحتاج تفويضًا مناسبًا.")
        }

        addCard(
            "🛡️ الحماية",
            "حماية بيانات المشاركة"
        ) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            showMessage("حماية الشاشة مفعلة.")
        }

        addCard(
            "↩️ العودة",
            "العودة إلى المشاركة التمويلية"
        ) {
            showFinancingParticipation()
        }
    }

    private fun showSensitiveAccessPolicy() {
        baseLayout("سياسة الوصول الحساس")

        addSection("🔒 سياسة الخدمات الحساسة")
        addInfo(
            "حماية شاملة",
            "حماية التطبيق مفعلة على مستوى التطبيق بالكامل، وليست مقتصرة على شاشة واحدة."
        )

        addCard(
            "📵 منع التقاط الشاشة",
            "حماية محتوى التطبيق من الالتقاط"
        ) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            showMessage("حماية الشاشة مفعلة.")
        }

        addCard(
            "🎥 منع تسجيل الشاشة",
            "حماية المحتوى من تسجيل الشاشة"
        ) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            showMessage("حماية تسجيل الشاشة مفعلة.")
        }

        addCard(
            "⏱️ السكون خارج التطبيق",
            "القفل بعد 10 دقائق خارج التطبيق"
        ) {
            showMessage("مدة السكون خارج التطبيق: 10 دقائق.")
        }

        addCard(
            "⚖️ التحقق الرسمي",
            "التحقق القانوني عبر الجهات المخولة"
        ) {
            showEligibilityVerification()
        }

        addCard(
            "💰 الاستثمار",
            "متطلبات المشاركة الاستثمارية"
        ) {
            showInvestmentParticipation()
        }

        addCard(
            "🏗️ التمويل",
            "متطلبات المشاركة التمويلية"
        ) {
            showFinancingParticipation()
        }

        addCard(
            "👑 الموافقة",
            "الموافقات المطلوبة قبل التنفيذ"
        ) {
            showOwnerApproval()
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showSecurityChecklist() {
        baseLayout("قائمة الأمان")

        addSection("🛡️ قائمة مراجعة الأمان")

        addInfo(
            "الحماية العامة",
            "حماية الشاشة مفعلة على مستوى التطبيق بالكامل."
        )

        addCard(
            "✅ حماية الشاشة",
            "FLAG_SECURE مفعلة"
        ) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            showMessage("الحماية مفعلة.")
        }

        addCard(
            "✅ قفل خارج التطبيق",
            "السكون بعد 10 دقائق"
        ) {
            showMessage("إعداد السكون: 10 دقائق.")
        }

        addCard(
            "✅ حماية البيانات",
            "تقليل عرض البيانات الحساسة"
        ) {
            showMessage("البيانات الحساسة تخضع للصلاحيات.")
        }

        addCard(
            "✅ التحقق الرسمي",
            "الاعتماد على المصادر الحكومية المخولة"
        ) {
            showEligibilityVerification()
        }

        addCard(
            "✅ مكافحة الاحتيال",
            "متابعة مؤشرات المخاطر"
        ) {
            showSecurityOffice()
        }

        addCard(
            "↩️ مكتب الأمن",
            "العودة إلى مكتب الأمن"
        ) {
            showSecurityOffice()
        }
    }

    private fun showOwnerApproval() {
        baseLayout("موافقة المالك")

        addSection("👑 موافقة المالك")
        addInfo(
            "صلاحية إدارية",
            "العمليات الحساسة والقرارات المالية لا تنفذ تلقائيًا من هذه النسخة."
        )

        addCard(
            "💰 العمليات المالية",
            "لا تنفيذ مالي دون الموافقة المطلوبة"
        ) {
            showMessage("لا يتم تنفيذ عملية مالية حقيقية من هذه الواجهة.")
        }

        addCard(
            "🏗️ المشاريع",
            "مراجعة المشاريع الحساسة"
        ) {
            showProjectStatus()
        }

        addCard(
            "🛡️ الأمن",
            "مراجعة القرارات الأمنية"
        ) {
            showSecurityOffice()
        }

        addCard(
            "⚖️ القانون",
            "مراجعة المسائل القانونية"
        ) {
            showAttorneyOffice()
        }

        addCard(
            "🤖 CTM AI",
            "إدارة حالة المساعد الذكي"
        ) {
            showCtmAi()
        }

        addCard(
            "↩️ مكتب المدير",
            "العودة إلى مكتب المدير"
        ) {
            showManagerOffice()
        }
    }

    private fun showProjectStatus() {
        baseLayout("حالة المشروع")

        addSection("📊 حالة المشروع")
        addInfo(
            "المتابعة",
            "عرض حالة الأقسام والمشاريع والتطوير."
        )

        addCard(
            "🧠 الأفكار",
            "متابعة الأفكار والمقترحات"
        ) {
            showHumanIntelligence()
        }

        addCard(
            "🏗️ المشاريع التمويلية",
            "متابعة المشاريع التمويلية"
        ) {
            showFinancingParticipation()
        }

        addCard(
            "📈 المشاريع الاستثمارية",
            "متابعة المشاريع الاستثمارية"
        ) {
            showInvestmentParticipation()
        }

        addCard(
            "🛡️ الأمن",
            "حالة منظومة الأمان"
        ) {
            showSecurityOffice()
        }

        addCard(
            "🤖 CTM AI",
            "حالة المساعد الذكي"
        ) {
            showCtmAi()
        }

        addCard(
            "🌐 الاتصال",
            "فحص الاتصال والخدمات"
        ) {
            showOnline()
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showAppRules() {
        baseLayout("قواعد التطبيق")

        addSection("📜 قواعد المنصة")
        addInfo(
            "الاستخدام المسؤول",
            "يجب استخدام المنصة وفق القوانين والأنظمة والصلاحيات المعتمدة."
        )

        addCard(
            "🔐 الخصوصية",
            "احترام وحماية بيانات المستخدمين"
        ) {
            showSafety()
        }

        addCard(
            "⚖️ القانون",
            "الالتزام بالقواعد النظامية"
        ) {
            showAttorneyOffice()
        }

        addCard(
            "🛡️ الأمان",
            "حماية الحسابات والمعلومات"
        ) {
            showSecurityOffice()
        }

        addCard(
            "💰 الخدمات المالية",
            "لا تنفيذ مالي تلقائي"
        ) {
            showPrivateFinancialSystem()
        }

        addCard(
            "🌍 الاستخدام العالمي",
            "تختلف القواعد حسب الدولة والمنطقة"
        ) {
            showMessage("تطبيق القواعد يعتمد على البلد والحزمة المفعلة.")
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

        private fun showDiagnostics() {
        baseLayout("التشخيص")

        addSection("🔧 تشخيص الجهاز والتطبيق")
        addInfo(
            "الفحص التلقائي",
            "عرض معلومات أساسية عن الاتصال وقدرات الجهاز دون جمع بيانات غير ضرورية."
        )

        val connectivityManager =
            getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val capabilities =
            connectivityManager.getNetworkCapabilities(network)

        val connected =
            capabilities != null &&
            capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            )

        val validated =
            capabilities != null &&
            capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED
            )

        addInfo(
            "الاتصال",
            if (connected) "متصل" else "غير متصل"
        )

        addInfo(
            "الإنترنت",
            if (validated) "متاح ومتحقق" else "غير متحقق"
        )

        val networkType = when {
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

        addInfo(
            "نوع الاتصال",
            networkType
        )

        addInfo(
            "إصدار Android",
            Build.VERSION.RELEASE
        )

        addInfo(
            "SDK",
            Build.VERSION.SDK_INT.toString()
        )

        addInfo(
            "الجهاز",
            "${Build.MANUFACTURER} ${Build.MODEL}"
        )

        addCard(
            "🌐 اختبار الاتصال",
            "إعادة فحص حالة الشبكة"
        ) {
            showOnline()
        }

        addCard(
            "📱 قدرات الجهاز",
            "عرض معلومات توافق الجهاز"
        ) {
            showDeviceCapabilities()
        }

        addCard(
            "📊 حالة التطبيق",
            "مراجعة حالة الحماية"
        ) {
            showProjectStatus()
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showDeviceCapabilities() {
        baseLayout("قدرات الجهاز")

        addSection("📱 قدرات الجهاز والتوافق")
        addInfo(
            "التوافق",
            "يتم تصميم التطبيق ليعمل على مجموعة واسعة من أجهزة Android المتوافقة."
        )

        addInfo(
            "Android",
            "الإصدار ${Build.VERSION.RELEASE}"
        )

        addInfo(
            "SDK",
            Build.VERSION.SDK_INT.toString()
        )

        addInfo(
            "الشركة",
            Build.MANUFACTURER
        )

        addInfo(
            "الطراز",
            Build.MODEL
        )

        val connectivityManager =
            getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val capabilities =
            connectivityManager.getNetworkCapabilities(network)

        val hasWifi =
            capabilities?.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI
            ) == true

        val hasMobile =
            capabilities?.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR
            ) == true

        addCard(
            "📶 Wi-Fi",
            if (hasWifi) "متاح حاليًا" else "غير متاح حاليًا"
        ) {
            showMessage(
                if (hasWifi) "اتصال Wi-Fi متاح." else "لا يوجد Wi-Fi متاح حاليًا."
            )
        }

        addCard(
            "📡 بيانات الهاتف",
            if (hasMobile) "متاحة حاليًا" else "غير متاحة حاليًا"
        ) {
            showMessage(
                if (hasMobile) "بيانات الهاتف متاحة." else "بيانات الهاتف غير متاحة."
            )
        }

        addCard(
            "💾 وضع البيانات",
            "دعم الاتصال المحدود وتقليل الاستخدام غير الضروري"
        ) {
            showMessage("التطبيق مصمم لتقليل العمليات غير الضرورية.")
        }

        addCard(
            "🧩 التوافق",
            "مراجعة توافق التطبيق مع الجهاز"
        ) {
            showMessage("تم تسجيل معلومات الجهاز للتشخيص المحلي.")
        }

        addCard(
            "↩️ التشخيص",
            "العودة إلى صفحة التشخيص"
        ) {
            showDiagnostics()
        }
    }

    private fun showDeveloperNotice() {
        baseLayout("ملاحظات التطوير")

        addSection("🧑‍💻 ملاحظات التطوير")
        addInfo(
            "نسخة التطوير",
            "هذه الواجهة تساعد على متابعة حالة التطبيق أثناء التطوير والاختبار."
        )

        addCard(
            "🔧 التشخيص",
            "فحص الجهاز والاتصال"
        ) {
            showDiagnostics()
        }

        addCard(
            "🛡️ الأمان",
            "مراجعة حماية التطبيق"
        ) {
            showSecurityChecklist()
        }

        addCard(
            "🌐 الاتصال",
            "فحص الاتصال بالإنترنت"
        ) {
            showOnline()
        }

        addCard(
            "📊 حالة المشروع",
            "عرض حالة الأقسام"
        ) {
            showProjectStatus()
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showSearch() {
        baseLayout("البحث")

        addSection("🔎 البحث")
        addInfo(
            "البحث داخل المنصة",
            "اكتب كلمة للبحث عن قسم أو خدمة أو منتج."
        )

        val searchInput = EditText(this)
        searchInput.hint = "اكتب كلمة البحث"
        content.addView(searchInput)

        val searchButton = Button(this)
        searchButton.text = "بحث"
        searchButton.setOnClickListener {
            val query = searchInput.text.toString().trim()

            if (query.isEmpty()) {
                showMessage("اكتب كلمة البحث أولًا.")
            } else {
                showMessage(
                    "تم استلام البحث: $query"
                )
            }
        }

        content.addView(searchButton)

        addCard(
            "🛍️ المنتجات",
            "الانتقال إلى الأقسام والمنتجات"
        ) {
            showProducts()
        }

        addCard(
            "🤖 CTM AI",
            "البحث الذكي والمساعدة"
        ) {
            showCtmAi()
        }

        addCard(
            "📢 الإعلانات",
            "عرض خدمات الإعلانات"
        ) {
            showAds()
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showAccessDenied() {
        baseLayout("الوصول")

        addSection("🔐 الوصول غير متاح")
        addInfo(
            "الصلاحية",
            "هذه الوظيفة تحتاج إلى صلاحية أو تحقق مناسب قبل الوصول إليها."
        )

        addCard(
            "🛡️ الأمان",
            "مراجعة إعدادات الحماية"
        ) {
            showSafety()
        }

        addCard(
            "👑 موافقة المالك",
            "مراجعة الصلاحيات الإدارية"
        ) {
            showOwnerApproval()
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

    private fun clearSensitiveView() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )
        showHome()
    }

    private fun requireSensitiveConfirmation(
        title: String,
        description: String
    ) {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )

        addSection(title)
        addInfo(
            "🔒 تأكيد الحماية",
            description
        )
    }

    private fun enableSensitiveScreenProtection() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    private fun showSensitiveHeader(
        title: String,
        description: String
    ) {
        enableSensitiveScreenProtection()

        addSection(title)

        addInfo(
            "🔒 شاشة محمية",
            description
        )
    }

        private fun showServices() {
        baseLayout("الخدمات")

        addSection("🧰 الخدمات")
        addInfo(
            "خدمات المنصة",
            "مجموعة من الخدمات العامة والمهنية المتاحة حسب البلد والمنطقة."
        )

        addCard(
            "🚚 النقل والتوصيل",
            "خدمات النقل والتوصيل"
        ) {
            showMessage("الخدمة تعتمد على المنطقة والتوفر.")
        }

        addCard(
            "🏗️ البناء",
            "مواد البناء والخدمات المرتبطة بها"
        ) {
            showCategory("🏗️ البناء والجملة")
        }

        addCard(
            "🌾 الزراعة والمواشي",
            "منتجات وخدمات الزراعة والثروة الحيوانية"
        ) {
            showCategory("🌾 الزراعة والمواشي")
        }

        addCard(
            "🐟 الأسماك",
            "منتجات وخدمات الأسماك"
        ) {
            showCategory("🐟 الأسماك")
        }

        addCard(
            "🏥 الصحة",
            "خدمات ومعلومات صحية عامة"
        ) {
            showCategory("🏥 الصحة")
        }

        addCard(
            "🎓 التعليم",
            "الخدمات والموارد التعليمية"
        ) {
            showCategory("🎓 التعليم")
        }

        addCard(
            "✈️ السفر",
            "السفر والتنقل والحجوزات"
        ) {
            showCategory("✈️ السفر")
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showAgriculture() {
        baseLayout("الزراعة والمواشي")

        addSection("🌾 الزراعة والمواشي")
        addInfo(
            "القطاع الزراعي",
            "منتجات وخدمات الزراعة والمواشي والثروة الحيوانية."
        )

        addCard(
            "🌱 الزراعة",
            "منتجات ومستلزمات زراعية"
        ) {
            showMessage("سيتم تنظيم المنتجات الزراعية حسب المنطقة.")
        }

        addCard(
            "🐄 المواشي",
            "المواشي والثروة الحيوانية"
        ) {
            showMessage("بيانات المواشي تخضع للتوثيق المناسب.")
        }

        addCard(
            "🚜 المعدات",
            "معدات وأدوات زراعية"
        ) {
            showMessage("عرض المعدات حسب المنطقة والتوفر.")
        }

        addCard(
            "📦 البيع والشراء",
            "التجارة المحلية للمنتجات"
        ) {
            showMessage("المعاملات تخضع لقواعد التجارة المحلية.")
        }

        addCard(
            "↩️ المنتجات",
            "العودة إلى المنتجات"
        ) {
            showProducts()
        }
    }

    private fun showHealth() {
        baseLayout("الصحة")

        addSection("🏥 الصحة")
        addInfo(
            "الخدمات الصحية",
            "قسم للمعلومات والخدمات الصحية العامة."
        )

        addCard(
            "🏥 الخدمات",
            "الوصول إلى الخدمات الصحية"
        ) {
            showMessage("الخدمات تعتمد على المنطقة والتوفر.")
        }

        addCard(
            "💊 المنتجات",
            "المنتجات الصحية المسموح بعرضها"
        ) {
            showMessage("عرض المنتجات وفق القوانين المحلية.")
        }

        addCard(
            "📍 المنطقة",
            "معرفة الخدمات المتاحة"
        ) {
            showOnline()
        }

        addCard(
            "🔐 الخصوصية",
            "حماية المعلومات الشخصية"
        ) {
            showSafety()
        }

        addCard(
            "↩️ المنتجات",
            "العودة إلى المنتجات"
        ) {
            showProducts()
        }
    }

    private fun showSports() {
        baseLayout("الرياضة")

        addSection("⚽ الرياضة")
        addInfo(
            "الرياضة والأنشطة",
            "خدمات ومنتجات ومعلومات رياضية."
        )

        addCard(
            "⚽ المنتجات الرياضية",
            "عرض المنتجات والمستلزمات"
        ) {
            showMessage("سيتم تنظيم المنتجات الرياضية.")
        }

        addCard(
            "🏟️ الأنشطة",
            "الأنشطة والفعاليات الرياضية"
        ) {
            showMessage("الفعاليات تعتمد على المنطقة.")
        }

        addCard(
            "👥 الفرق",
            "تنظيم الفرق والمجموعات"
        ) {
            showMessage("يمكن تنظيم الفرق وفق الصلاحيات.")
        }

        addCard(
            "↩️ المنتجات",
            "العودة إلى المنتجات"
        ) {
            showProducts()
        }
    }

    private fun showEducation() {
        baseLayout("التعليم")

        addSection("🎓 التعليم")
        addInfo(
            "الخدمات التعليمية",
            "موارد وخدمات تعليمية للطلاب والمؤسسات."
        )

        addCard(
            "📚 الموارد",
            "الوصول إلى الموارد التعليمية"
        ) {
            showMessage("الموارد التعليمية تنظم حسب المجال.")
        }

        addCard(
            "🏫 المؤسسات",
            "معلومات المؤسسات التعليمية"
        ) {
            showMessage("بيانات المؤسسات تحتاج مصدرًا موثوقًا.")
        }

        addCard(
            "👨‍🏫 الخبراء",
            "الاستفادة من الخبرات التعليمية"
        ) {
            showHumanIntelligence()
        }

        addCard(
            "↩️ المنتجات",
            "العودة إلى المنتجات"
        ) {
            showProducts()
        }
    }

    private fun showTravel() {
        baseLayout("السفر")

        addSection("✈️ السفر")
        addInfo(
            "السفر والتنقل",
            "خدمات ومعلومات السفر وفق البلد والمنطقة."
        )

        addCard(
            "✈️ الرحلات",
            "معلومات الرحلات"
        ) {
            showMessage("معلومات الرحلات تحتاج مصدرًا محدثًا.")
        }

        addCard(
            "🚌 النقل",
            "خيارات النقل والتنقل"
        ) {
            showMessage("خيارات النقل تختلف حسب المنطقة.")
        }

        addCard(
            "🛂 المتطلبات",
            "معلومات عامة عن متطلبات السفر"
        ) {
            showMessage("يجب الرجوع إلى الجهات الرسمية للحصول على المتطلبات النهائية.")
        }

        addCard(
            "🌍 المناطق",
            "القواعد تختلف حسب البلد"
        ) {
            showMessage("القواعد المحلية تختلف حسب الدولة والمنطقة.")
        }

        addCard(
            "↩️ المنتجات",
            "العودة إلى المنتجات"
        ) {
            showProducts()
        }
    }

    private fun showElectricityWater() {
        baseLayout("الكهرباء والمياه")

        addSection("⚡ الكهرباء والمياه")
        addInfo(
            "الخدمات الأساسية",
            "الوصول إلى المعلومات والخدمات المرتبطة بالكهرباء والمياه."
        )

        addCard(
            "⚡ الكهرباء",
            "خدمات ومستلزمات الكهرباء"
        ) {
            showMessage("الخدمات تعتمد على المنطقة.")
        }

        addCard(
            "💧 المياه",
            "خدمات ومستلزمات المياه"
        ) {
            showMessage("الخدمات تعتمد على المنطقة.")
        }

        addCard(
            "🔧 الصيانة",
            "خدمات الصيانة"
        ) {
            showMessage("سيتم تنظيم مقدمي الخدمات وفق المنطقة.")
        }

        addCard(
            "↩️ المنتجات",
            "العودة إلى المنتجات"
        ) {
            showProducts()
        }
    }

    private fun showVehicles() {
        baseLayout("المركبات")

        addSection("🚗 المركبات والشاحنات")
        addInfo(
            "المركبات",
            "عرض المركبات والشاحنات والخدمات المرتبطة بها."
        )

        addCard(
            "🚗 السيارات",
            "السيارات والمركبات"
        ) {
            showMessage("عرض المركبات حسب المنطقة.")
        }

        addCard(
            "🚚 الشاحنات",
            "الشاحنات ومعدات النقل"
        ) {
            showMessage("عرض الشاحنات حسب المنطقة.")
        }

        addCard(
            "🔧 قطع الغيار",
            "قطع الغيار والخدمات"
        ) {
            showMessage("سيتم تنظيم قطع الغيار حسب النوع.")
        }

        addCard(
            "🛠️ الصيانة",
            "خدمات الصيانة"
        ) {
            showMessage("خدمات الصيانة تعتمد على المنطقة.")
        }

        addCard(
            "↩️ المنتجات",
            "العودة إلى المنتجات"
        ) {
            showProducts()
        }
    }

    private fun showElectronics() {
        baseLayout("الإلكترونيات")

        addSection("📱 الهواتف والإلكترونيات")
        addInfo(
            "الإلكترونيات",
            "الهواتف والأجهزة والإكسسوارات والخدمات المرتبطة بها."
        )

        addCard(
            "📱 الهواتف",
            "الهواتف والأجهزة المحمولة"
        ) {
            showMessage("عرض الأجهزة حسب المنطقة والتوفر.")
        }

        addCard(
            "💻 الأجهزة",
            "الأجهزة الإلكترونية"
        ) {
            showMessage("عرض الأجهزة الإلكترونية.")
        }

        addCard(
            "🎧 الإكسسوارات",
            "الإكسسوارات الإلكترونية"
        ) {
            showMessage("عرض الإكسسوارات.")
        }

        addCard(
            "🔧 الصيانة",
            "خدمات صيانة الأجهزة"
        ) {
            showMessage("خدمات الصيانة تعتمد على المنطقة.")
        }

        addCard(
            "↩️ المنتجات",
            "العودة إلى المنتجات"
        ) {
            showProducts()
        }
    }

    private fun showRestaurants() {
        baseLayout("المطاعم والتوصيل")

        addSection("🍽️ المطاعم والتوصيل")
        addInfo(
            "الطعام والتوصيل",
            "المطاعم وخدمات الطعام والتوصيل حسب المنطقة."
        )

        addCard(
            "🍽️ المطاعم",
            "استكشاف المطاعم"
        ) {
            showMessage("المطاعم تعتمد على المنطقة والتوفر.")
        }

        addCard(
            "🚚 التوصيل",
            "خدمات توصيل الطعام"
        ) {
            showMessage("التوصيل يعتمد على المنطقة.")
        }

        addCard(
            "🍳 المطبخ",
            "منتجات وخدمات المطبخ"
        ) {
            showKitchen()
        }

        addCard(
            "↩️ المنتجات",
            "العودة إلى المنتجات"
        ) {
            showProducts()
        }
    }

        private fun showBuilding() {
        baseLayout("البناء والجملة")

        addSection("🏗️ البناء والجملة")
        addInfo(
            "مواد البناء والتجارة",
            "منتجات وخدمات البناء وتجارة الجملة حسب المنطقة."
        )

        addCard(
            "🧱 مواد البناء",
            "عرض مواد البناء"
        ) {
            showMessage("مواد البناء تعرض حسب المنطقة والتوفر.")
        }

        addCard(
            "🏪 تجارة الجملة",
            "التعاملات التجارية بالجملة"
        ) {
            showMessage("تجارة الجملة تخضع للقواعد المحلية.")
        }

        addCard(
            "🚚 النقل",
            "خدمات نقل المواد"
        ) {
            showMessage("خدمات النقل تعتمد على المنطقة.")
        }

        addCard(
            "👷 الخدمات",
            "الخدمات المهنية في البناء"
        ) {
            showServices()
        }

        addCard(
            "↩️ المنتجات",
            "العودة إلى المنتجات"
        ) {
            showProducts()
        }
    }

    private fun showFish() {
        baseLayout("الأسماك")

        addSection("🐟 الأسماك")
        addInfo(
            "منتجات وخدمات الأسماك",
            "عرض المنتجات والخدمات المرتبطة بالأسماك حسب المنطقة."
        )

        addCard(
            "🐟 المنتجات",
            "الأسماك والمنتجات البحرية"
        ) {
            showMessage("المنتجات تعتمد على التوفر والمنطقة.")
        }

        addCard(
            "🚚 التوصيل",
            "خدمات النقل والتوصيل"
        ) {
            showMessage("التوصيل يعتمد على المنطقة.")
        }

        addCard(
            "🏪 الموردون",
            "بيانات الموردين"
        ) {
            showMessage("الموردون يخضعون للتحقق المناسب.")
        }

        addCard(
            "↩️ المنتجات",
            "العودة إلى المنتجات"
        ) {
            showProducts()
        }
    }

    private fun showWholesale() {
        baseLayout("تجارة الجملة")

        addSection("🏪 تجارة الجملة")
        addInfo(
            "التجارة",
            "خدمات ومنتجات تجارة الجملة والتعامل بين الموردين والعملاء."
        )

        addCard(
            "📦 المنتجات",
            "عرض المنتجات بالجملة"
        ) {
            showProducts()
        }

        addCard(
            "🤝 الموردون",
            "تنظيم بيانات الموردين"
        ) {
            showMessage("بيانات الموردين تخضع للتحقق.")
        }

        addCard(
            "🚚 الشحن",
            "خدمات النقل والشحن"
        ) {
            showMessage("تكلفة الشحن تعتمد على المنطقة والمسافة.")
        }

        addCard(
            "📋 القواعد",
            "القواعد التجارية المحلية"
        ) {
            showAppRules()
        }

        addCard(
            "↩️ البناء",
            "العودة إلى قسم البناء والجملة"
        ) {
            showBuilding()
        }
    }

    private fun showFavoritesEmpty() {
        baseLayout("المفضلة")

        addSection("⭐ المفضلة")
        addInfo(
            "المفضلة",
            "احفظ المنتجات والخدمات المهمة للوصول إليها بسرعة."
        )

        addCard(
            "🛍️ استكشاف المنتجات",
            "إضافة عناصر إلى المفضلة"
        ) {
            showProducts()
        }

        addCard(
            "🔎 البحث",
            "البحث عن منتج أو خدمة"
        ) {
            showSearch()
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showAccountSettings() {
        baseLayout("إعدادات الحساب")

        addSection("⚙️ إعدادات الحساب")
        addInfo(
            "إدارة الحساب",
            "إعدادات عامة للحساب والخصوصية والحماية."
        )

        addCard(
            "🛡️ الخصوصية",
            "إعدادات الخصوصية والأمان"
        ) {
            showSafety()
        }

        addCard(
            "🔐 الحماية",
            "مراجعة حماية التطبيق"
        ) {
            showSecurityChecklist()
        }

        addCard(
            "👤 الحساب",
            "العودة إلى الحساب"
        ) {
            showAccount()
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showNetworkStatus() {
        baseLayout("حالة الاتصال")

        addSection("🌐 حالة الاتصال")

        val manager =
            getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = manager.activeNetwork
        val capabilities =
            manager.getNetworkCapabilities(network)

        val online =
            capabilities != null &&
            capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            )

        val validated =
            capabilities != null &&
            capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED
            )

        addInfo(
            "الحالة",
            if (online) "متصل" else "غير متصل"
        )

        addInfo(
            "الإنترنت",
            if (validated) "متاح" else "غير متحقق"
        )

        addCard(
            "📶 فحص الشبكة",
            "إعادة فحص الاتصال"
        ) {
            showOnline()
        }

        addCard(
            "🔧 التشخيص",
            "عرض تفاصيل الجهاز والاتصال"
        ) {
            showDiagnostics()
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showOwnerPanel() {
        baseLayout("إدارة المالك")

        addSection("👑 إدارة المالك")
        addInfo(
            "المالك",
            "المالك ياسر حسن وشركاؤه"
        )

        addCard(
            "📊 حالة المشروع",
            "متابعة حالة المشروع"
        ) {
            showProjectStatus()
        }

        addCard(
            "🧠 الذكاء البشري",
            "متابعة الأفكار والمشاريع"
        ) {
            showHumanIntelligence()
        }

        addCard(
            "🤖 CTM AI",
            "إدارة المساعد الذكي"
        ) {
            showCtmAi()
        }

        addCard(
            "🛡️ الأمن",
            "مراجعة الحماية"
        ) {
            showSecurityOffice()
        }

        addCard(
            "⚖️ القانون",
            "المتابعة القانونية"
        ) {
            showAttorneyOffice()
        }

        addCard(
            "💼 النظام المالي",
            "المعلومات المالية الخاصة"
        ) {
            showPrivateFinancialSystem()
        }

        addCard(
            "🦡 BADGER",
            "النظام المالي المستقل"
        ) {
            showBadger()
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showGlobalPackages() {
        baseLayout("الحزم والمناطق")

        addSection("🌍 الحزم العالمية")
        addInfo(
            "النظام العالمي",
            "تصميم المنصة يسمح بتفعيل خصائص مختلفة حسب الدولة والمنطقة."
        )

        addCard(
            "🇸🇩 حزمة السودان",
            "خصائص وخدمات السودان عند تفعيلها رسميًا"
        ) {
            showMessage("الحزمة الإقليمية تخضع للإعداد والصلاحيات.")
        }

        addCard(
            "🌍 النسخة العالمية",
            "الخدمات العامة المشتركة"
        ) {
            showMessage("النسخة العالمية لا تفترض خصائص دولة محددة.")
        }

        addCard(
            "📍 المنطقة",
            "تطبيق القواعد حسب المنطقة"
        ) {
            showMessage("القواعد والخدمات تختلف حسب الدولة والمنطقة.")
        }

        addCard(
            "🚚 الشحن والجمارك",
            "تقديرات الشحن والقواعد"
        ) {
            showMessage("التقديرات تعتمد على الدولة والمسار.")
        }

        addCard(
            "🎫 السفر",
            "معلومات السفر حسب البلد"
        ) {
            showTravel()
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showAppInformation() {
        baseLayout("معلومات التطبيق")

        addSection("ℹ️ معلومات التطبيق")
        addInfo(
            "CENTRAL MARKET",
            "منصة متعددة الخدمات والأسواق، قابلة للتوسع حسب الدولة والمنطقة."
        )

        addInfo(
            "الحماية",
            "حماية الشاشة مفعلة على مستوى التطبيق."
        )

        addInfo(
            "السكون الخارجي",
            "10 دقائق خارج التطبيق."
        )

        addInfo(
            "الاتصال",
            "يتم اكتشاف حالة الاتصال تلقائيًا."
        )

        addInfo(
            "التوافق",
            "مصمم للعمل على أجهزة Android المتوافقة."
        )

        addCard(
            "🔧 التشخيص",
            "فحص الجهاز والاتصال"
        ) {
            showDiagnostics()
        }

        addCard(
            "📜 القواعد",
            "مراجعة قواعد الاستخدام"
        ) {
            showAppRules()
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }

        private fun showManagerDocuments() {
        baseLayout("وثائق الإدارة")

        addSection("📂 وثائق الإدارة")
        addInfo(
            "السجلات الإدارية",
            "تنظيم الوثائق المتعلقة بإدارة المشروع والأقسام."
        )

        addCard(
            "📄 وثائق المشاريع",
            "متابعة وثائق المشاريع"
        ) {
            showDocuments()
        }

        addCard(
            "⚖️ الوثائق القانونية",
            "المعلومات القانونية المصرح بها"
        ) {
            showAttorneyOffice()
        }

        addCard(
            "🛡️ السجلات الأمنية",
            "معلومات الحماية والحوادث"
        ) {
            showSecurityOffice()
        }

        addCard(
            "💼 السجلات المالية",
            "المعلومات المالية المصرح بها"
        ) {
            showPrivateFinancialSystem()
        }

        addCard(
            "🔐 الوثائق الحساسة",
            "حماية المعلومات الحساسة"
        ) {
            showSensitiveAccessPolicy()
        }

        addCard(
            "↩️ مكتب المدير",
            "العودة إلى مكتب المدير"
        ) {
            showManagerOffice()
        }
    }

    private fun showHumanProjects() {
        baseLayout("مشاريع الذكاء البشري")

        addSection("🧠 المشاريع والأفكار")
        addInfo(
            "تطوير الأفكار",
            "تنظيم الأفكار ومتابعتها وتحويل المناسب منها إلى مشاريع قابلة للدراسة."
        )

        addCard(
            "💡 الأفكار الجديدة",
            "إضافة فكرة جديدة"
        ) {
            showIdea()
        }

        addCard(
            "📊 التقييم",
            "مراجعة الأفكار والمشاريع"
        ) {
            showMessage("التقييم يتم وفق معايير المشروع والصلاحيات.")
        }

        addCard(
            "🤝 الفرق",
            "تكوين فرق العمل"
        ) {
            showMessage("تكوين الفرق يعتمد على الخبرات والصلاحيات.")
        }

        addCard(
            "📅 خطة العمل",
            "تنظيم مراحل المشروع"
        ) {
            showMessage("يمكن تنظيم مراحل المشروع حسب نوعه.")
        }

        addCard(
            "🏆 الإنجازات",
            "متابعة الإنجازات"
        ) {
            showMessage("سيتم حفظ الإنجازات ضمن سجل المشروع.")
        }

        addCard(
            "🌍 المشاريع الدولية",
            "تطوير المشاريع القابلة للتوسع"
        ) {
            showGlobalPackages()
        }

        addCard(
            "↩️ الذكاء البشري",
            "العودة إلى المنظومة"
        ) {
            showHumanIntelligence()
        }
    }

    private fun showCtmAiSettings() {
        baseLayout("إعدادات CTM AI")

        addSection("🤖 إعدادات CTM AI")
        addInfo(
            "المساعد الرسمي",
            "CTM AI يعمل ضمن نطاق المعلومات والوظائف المسموح بها."
        )

        addCard(
            "🔎 البحث",
            "البحث الذكي"
        ) {
            showCtmAi()
        }

        addCard(
            "🎙️ الصوت",
            "البحث والقراءة الصوتية"
        ) {
            showMessage("الوظائف الصوتية تخضع لقدرات الجهاز والصلاحيات.")
        }

        addCard(
            "🛡️ مراقبة المخاطر",
            "متابعة الأخطاء والمخاطر"
        ) {
            showSecurityOffice()
        }

        addCard(
            "⏸️ الإيقاف",
            "إدارة حالة المساعد"
        ) {
            showMessage("إيقاف أو تفعيل CTM AI يحتاج الصلاحية المناسبة.")
        }

        addCard(
            "↩️ CTM AI",
            "العودة إلى المساعد"
        ) {
            showCtmAi()
        }
    }

    private fun showBadgerSecurity() {
        baseLayout("أمان BADGER")

        addSection("🦡🛡️ أمان BADGER")
        addInfo(
            "حماية النظام المالي",
            "حماية المعلومات المالية ومنع الوصول غير المصرح به."
        )

        addCard(
            "🔒 حماية الشاشة",
            "منع التقاط وتسجيل الشاشة"
        ) {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SECURE
            )
            showMessage("حماية الشاشة مفعلة.")
        }

        addCard(
            "⏱️ السكون",
            "قفل الجلسة بعد 10 دقائق خارج التطبيق"
        ) {
            showMessage("السكون الخارجي مضبوط على 10 دقائق.")
        }

        addCard(
            "🚨 مكافحة الاحتيال",
            "متابعة مؤشرات المخاطر"
        ) {
            showSecurityOffice()
        }

        addCard(
            "⚖️ الأهلية",
            "التحقق الرسمي عند الحاجة"
        ) {
            showEligibilityVerification()
        }

        addCard(
            "👑 الموافقة",
            "الموافقة المطلوبة للعمليات الحساسة"
        ) {
            showOwnerApproval()
        }

        addCard(
            "↩️ BADGER",
            "العودة إلى BADGER"
        ) {
            showBadger()
        }
    }

    private fun showInvestmentSafety() {
        baseLayout("أمان الاستثمار")

        addSection("📈🛡️ أمان المشاركة الاستثمارية")
        addInfo(
            "خدمة حساسة",
            "لا يتم تنفيذ أي عملية مالية حقيقية تلقائيًا من هذه الواجهة."
        )

        addCard(
            "📵 حماية الشاشة",
            "الحماية الشاملة مفعلة"
        ) {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SECURE
            )
            showMessage("حماية الشاشة مفعلة.")
        }

        addCard(
            "⚖️ الأهلية",
            "التحقق عبر المسار الرسمي"
        ) {
            showEligibilityVerification()
        }

        addCard(
            "📋 الشروط",
            "مراجعة متطلبات الاستثمار"
        ) {
            showInvestmentRequirements()
        }

        addCard(
            "👑 الموافقة",
            "مراجعة الموافقة المطلوبة"
        ) {
            showOwnerApproval()
        }

        addCard(
            "↩️ الاستثمار",
            "العودة إلى المشاركة الاستثمارية"
        ) {
            showInvestmentParticipation()
        }
    }

    private fun showFinancingSafety() {
        baseLayout("أمان التمويل")

        addSection("🏗️🛡️ أمان المشاركة التمويلية")
        addInfo(
            "خدمة حساسة",
            "حماية المعلومات شرط أساسي قبل أي مشاركة أو إجراء."
        )

        addCard(
            "📵 حماية الشاشة",
            "منع التقاط الشاشة وتسجيلها"
        ) {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SECURE
            )
            showMessage("حماية الشاشة مفعلة.")
        }

        addCard(
            "⚖️ الأهلية",
            "التحقق الرسمي"
        ) {
            showEligibilityVerification()
        }

        addCard(
            "📋 الشروط",
            "مراجعة متطلبات التمويل"
        ) {
            showFinancingRequirements()
        }

        addCard(
            "👑 الموافقة",
            "الموافقة قبل أي إجراء حساس"
        ) {
            showOwnerApproval()
        }

        addCard(
            "↩️ التمويل",
            "العودة إلى المشاركة التمويلية"
        ) {
            showFinancingParticipation()
        }
    }

    private fun showRegionalRules() {
        baseLayout("قواعد المنطقة")

        addSection("🌍 قواعد الدولة والمنطقة")
        addInfo(
            "القواعد المحلية",
            "الخدمات والمعاملات تختلف حسب الدولة والمنطقة والقوانين المطبقة."
        )

        addCard(
            "📍 المنطقة",
            "تحديد الخدمات المناسبة"
        ) {
            showDiagnostics()
        }

        addCard(
            "🚚 الشحن والجمارك",
            "معلومات عامة عن الشحن"
        ) {
            showMessage("التقديرات تعتمد على الدولة والمسار.")
        }

        addCard(
            "🎫 السفر",
            "قواعد السفر حسب البلد"
        ) {
            showTravel()
        }

        addCard(
            "⚖️ المتطلبات القانونية",
            "مراجعة القواعد الرسمية"
        ) {
            showAttorneyOffice()
        }

        addCard(
            "🌍 الحزم",
            "الحزم الإقليمية والعالمية"
        ) {
            showGlobalPackages()
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showConnectionHelp() {
        baseLayout("مساعدة الاتصال")

        addSection("📶 مساعدة الاتصال")
        addInfo(
            "الاتصال بالإنترنت",
            "إذا كان الاتصال ضعيفًا، يتم تقليل العمليات غير الضرورية قدر الإمكان."
        )

        addCard(
            "🔄 إعادة الفحص",
            "فحص الاتصال مرة أخرى"
        ) {
            showOnline()
        }

        addCard(
            "📱 بيانات الهاتف",
            "مراجعة نوع الاتصال"
        ) {
            showNetworkStatus()
        }

        addCard(
            "🔧 التشخيص",
            "فحص الجهاز والشبكة"
        ) {
            showDiagnostics()
        }

        addCard(
            "↩️ الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

        private fun showHelpCenter() {
        baseLayout("مركز المساعدة")

        addSection(
            "🆘 مركز المساعدة",
            "إرشادات مختصرة لاستخدام المنصة بأمان وسهولة"
        )

        addCard(
            "📶 الاتصال",
            "حلول مشاكل الاتصال والعمل مع الشبكات الضعيفة"
        ) {
            showConnectionHelp()
        }

        addCard(
            "🔐 الأمان والخصوصية",
            "مراجعة حماية التطبيق والبيانات"
        ) {
            showSafety()
        }

        addCard(
            "📱 تشخيص الجهاز",
            "فحص قدرات الجهاز والاتصال"
        ) {
            showDiagnostics()
        }

        addCard(
            "👤 وضع الضيف",
            "الوصول إلى الخدمات العامة دون فتح الأقسام الخاصة"
        ) {
            showGuestMode()
        }

        addCard(
            "🏠 الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showPrivacyCenter() {
        baseLayout("مركز الخصوصية")

        showSensitiveHeader(
            "🔐 مركز الخصوصية",
            "إدارة مبادئ الخصوصية والحماية داخل المنصة"
        )

        addInfo(
            "حماية الشاشة",
            "حماية التطبيق من التقاط الشاشة أو التسجيل باستخدام FLAG_SECURE."
        )

        addInfo(
            "حماية التطبيق كاملة",
            "الحماية مطبقة على مستوى التطبيق وليست مقتصرة على شاشة واحدة."
        )

        addInfo(
            "السكون خارج التطبيق",
            "بعد مرور 10 دقائق خارج التطبيق يتم قفل الجلسة تلقائيًا."
        )

        addInfo(
            "البيانات",
            "لا ينبغي جمع أو مشاركة أي بيانات إلا للغرض المعلن وبالحد الأدنى اللازم."
        )

        addInfo(
            "الصلاحيات",
            "أي صلاحية يجب أن تكون مرتبطة بوظيفة واضحة ومعلنة للمستخدم."
        )

        addCard(
            "🛡️ قائمة الأمان",
            "مراجعة ضوابط الأمان الحالية"
        ) {
            showSecurityChecklist()
        }

        addCard(
            "↩️ العودة",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showDataPolicy() {
        baseLayout("سياسة البيانات")

        addSection(
            "📄 سياسة البيانات",
            "مبادئ عامة للتعامل مع معلومات المستخدمين"
        )

        addInfo(
            "تقليل البيانات",
            "يتم تصميم الخدمات بحيث لا تعتمد على بيانات غير ضرورية."
        )

        addInfo(
            "الشفافية",
            "يجب توضيح سبب طلب البيانات وطريقة استخدامها قبل الاعتماد عليها."
        )

        addInfo(
            "الحماية",
            "المعلومات الحساسة تحتاج إلى حماية تقنية وإدارية مناسبة."
        )

        addInfo(
            "الوصول",
            "الوصول إلى البيانات الخاصة يجب أن يكون وفق صلاحيات محددة."
        )

        addInfo(
            "الاحتفاظ",
            "لا ينبغي الاحتفاظ بالبيانات لفترة أطول من الحاجة النظامية لها."
        )

        addInfo(
            "المراجعة",
            "تخضع سياسات البيانات للمراجعة قبل إطلاق الخدمات الحساسة."
        )

        addCard(
            "🔐 مركز الخصوصية",
            "عرض ضوابط الخصوصية"
        ) {
            showPrivacyCenter()
        }

        addCard(
            "🏠 الرئيسية",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showShipping() {
        baseLayout("الشحن")

        addSection(
            "🚚 الشحن والخدمات اللوجستية",
            "معلومات عامة عن نقل المنتجات والطلبات"
        )

        addInfo(
            "الشحن المحلي",
            "يمكن عرض خيارات النقل المحلية وفق المنطقة والمنتج."
        )

        addInfo(
            "الشحن الدولي",
            "الطلبات الدولية تحتاج إلى مراعاة بلد المصدر وبلد الوصول."
        )

        addInfo(
            "التكلفة",
            "تقدير التكلفة يعتمد على الوزن والحجم والمسافة وطريقة النقل."
        )

        addInfo(
            "التتبع",
            "يمكن دعم التتبع عندما تتوفر خدمة تتبع موثوقة من شركة النقل."
        )

        addInfo(
            "الجمارك",
            "الشحن الدولي قد يخضع لإجراءات ورسوم جمركية وفق القوانين المعمول بها."
        )

        addCard(
            "🧾 الجمارك",
            "معلومات عامة عن الإجراءات والرسوم"
        ) {
            showCustoms()
        }

        addCard(
            "🏠 الرئيسية",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showCustoms() {
        baseLayout("الجمارك")

        addSection(
            "🧾 الجمارك والاستيراد",
            "معلومات إرشادية وليست بديلاً عن الجهات الرسمية"
        )

        addInfo(
            "التصنيف",
            "قد تختلف المتطلبات حسب نوع المنتج وتصنيفه القانوني."
        )

        addInfo(
            "الرسوم",
            "الرسوم والضرائب تختلف حسب البلد والمنتج وقيمة الشحنة."
        )

        addInfo(
            "المستندات",
            "قد تحتاج بعض الشحنات إلى فواتير أو مستندات منشأ أو تصاريح."
        )

        addInfo(
            "التحقق",
            "يجب الاعتماد على المصادر والجهات الرسمية عند اتخاذ قرار استيراد."
        )

        addInfo(
            "تنبيه",
            "المنصة لا تعتبر هذا القسم تصريحًا جمركيًا أو موافقة حكومية."
        )

        addCard(
            "🚚 الشحن",
            "العودة إلى خدمات الشحن"
        ) {
            showShipping()
        }

        addCard(
            "🏠 الرئيسية",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showInternationalProjects() {
        baseLayout("المشاريع الدولية")

        addSection(
            "🌍 المشاريع الدولية",
            "تصور للتعاون التجاري والمشاريع العابرة للحدود"
        )

        addInfo(
            "الأسواق",
            "يمكن تنظيم الفرص حسب الدولة والمنطقة والقطاع."
        )

        addInfo(
            "القوانين",
            "كل مشروع دولي يجب أن يراعي قوانين بلد التشغيل والتجارة."
        )

        addInfo(
            "الشحن",
            "تقديرات النقل والجمارك تعتمد على البيانات الفعلية المتاحة."
        )

        addInfo(
            "التحقق",
            "المشاريع الحساسة لا تنتقل إلى مرحلة المشاركة دون استكمال متطلبات التحقق."
        )

        addInfo(
            "الاعتماد",
            "الموافقات الداخلية لا تستبدل التراخيص أو الموافقات الرسمية المطلوبة."
        )

        addCard(
            "📦 الشحن",
            "الخدمات اللوجستية"
        ) {
            showShipping()
        }

        addCard(
            "📋 قواعد المناطق",
            "مراجعة قواعد المناطق والدول"
        ) {
            showRegionalRules()
        }

        addCard(
            "🏠 الرئيسية",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showSustainability() {
        baseLayout("الاستدامة")

        addSection(
            "🌱 الاستدامة",
            "تشجيع المشاريع والخدمات ذات الأثر الإيجابي"
        )

        addInfo(
            "الطاقة",
            "تشجيع الحلول التي تساعد على الاستخدام المسؤول للطاقة."
        )

        addInfo(
            "المياه",
            "دعم التوعية بحماية المياه وتحسين استخدامها."
        )

        addInfo(
            "الزراعة",
            "تشجيع المشاريع الزراعية والإنتاج المحلي."
        )

        addInfo(
            "المجتمع",
            "ربط المشاريع ذات الأثر الاجتماعي بالمجتمعات المستفيدة."
        )

        addInfo(
            "المسؤولية",
            "لا يعني عرض المشروع أن المنصة تضمن نجاحه أو عوائده."
        )

        addCard(
            "🌾 الزراعة والثروة الحيوانية",
            "العودة إلى قطاع الزراعة"
        ) {
            showAgriculture()
        }

        addCard(
            "💧 الكهرباء والمياه",
            "الخدمات المرتبطة بالطاقة والمياه"
        ) {
            showElectricityWater()
        }

        addCard(
            "🏠 الرئيسية",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showInnovationCenter() {
        baseLayout("مركز الابتكار")

        addSection(
            "💡 مركز الابتكار",
            "أفكار ومشاريع تقنية وتجارية قابلة للدراسة"
        )

        addInfo(
            "الأفكار",
            "يمكن تسجيل الأفكار ومراجعتها قبل اعتمادها ضمن المشروع."
        )

        addInfo(
            "الذكاء الاصطناعي",
            "يمكن استخدام CTM AI ضمن الحدود والوظائف التي يعتمدها المشروع."
        )

        addInfo(
            "الذكاء البشري",
            "الخبرة البشرية والمراجعة المتخصصة تظل جزءًا أساسيًا من القرارات المهمة."
        )

        addInfo(
            "السلامة",
            "أي ميزة جديدة تمر بمراجعة أمنية وتقنية قبل الإطلاق."
        )

        addInfo(
            "التجربة",
            "المزايا الجديدة لا تمنح صلاحيات مالية أو قانونية تلقائيًا."
        )

        addCard(
            "🤖 CTM AI",
            "إعدادات ووظائف المساعد الرسمي"
        ) {
            showCtmAi()
        }

        addCard(
            "🧠 الذكاء البشري",
            "المشاريع والخبرات البشرية"
        ) {
            showHumanIntelligence()
        }

        addCard(
            "💭 فكرة أو اقتراح",
            "عرض مساحة الأفكار"
        ) {
            showIdea()
        }

        addCard(
            "🏠 الرئيسية",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showCooperation() {
        baseLayout("التعاون والشراكات")

        addSection(
            "🤝 التعاون والشراكات",
            "تنظيم فرص التعاون ضمن ضوابط واضحة"
        )

        addInfo(
            "الشراكات",
            "تخضع الشراكات المقترحة للمراجعة القانونية والإدارية المناسبة."
        )

        addInfo(
            "الأطراف",
            "يجب تحديد الأطراف والصلاحيات والمسؤوليات بشكل واضح."
        )

        addInfo(
            "العقود",
            "العمولات والالتزامات يجب أن تكون موثقة في عقود واضحة."
        )

        addInfo(
            "المخاطر",
            "تتم مراجعة المخاطر قبل اعتماد المشاريع الحساسة."
        )

        addInfo(
            "الاعتماد",
            "عرض فرصة أو شراكة داخل التطبيق لا يعني اعتمادها تلقائيًا."
        )

        addCard(
            "📁 المشاريع البشرية",
            "مشاريع وخبرات المجتمع"
        ) {
            showHumanProjects()
        }

        addCard(
            "🏢 مكتب المدير",
            "إدارة ومراجعة المشاريع"
        ) {
            showManagerOffice()
        }

        addCard(
            "🏠 الرئيسية",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showExperts() {
        baseLayout("الخبراء")

        addSection(
            "👥 الخبراء والمختصون",
            "تنظيم المعرفة والخبرات لدعم المستخدمين والمشاريع"
        )

        addInfo(
            "التخصصات",
            "يمكن تصنيف الخبرات حسب المجال والقطاع والمنطقة."
        )

        addInfo(
            "التحقق",
            "الصفة المهنية أو الترخيص الرسمي يجب التحقق منه عبر القنوات المناسبة."
        )

        addInfo(
            "المسؤولية",
            "عرض ملف خبير لا يعني ضمان جودة أي استشارة يقدمها."
        )

        addInfo(
            "الخصوصية",
            "يجب عدم نشر بيانات شخصية غير لازمة."
        )

        addCard(
            "🧠 الذكاء البشري",
            "الوصول إلى منظومة الخبرات"
        ) {
            showHumanIntelligence()
        }

        addCard(
            "🤝 التعاون",
            "الشراكات والمشاريع"
        ) {
            showCooperation()
        }

        addCard(
            "🏠 الرئيسية",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }

        private fun showNotifications() {
        baseLayout("الإشعارات")

        addSection(
            "🔔 الإشعارات",
            "تنبيهات المنصة والتنبيهات المهمة للمستخدم"
        )

        addInfo(
            "تنبيهات النظام",
            "تظهر هنا التنبيهات المتعلقة بالخدمات والتحديثات المهمة."
        )

        addInfo(
            "تنبيهات الأمان",
            "قد تظهر تنبيهات عند وجود إجراء أمني يحتاج إلى مراجعة."
        )

        addInfo(
            "تنبيهات المشاريع",
            "المشاريع والمشاركات الحساسة تخضع لإشعارات واضحة قبل أي خطوة مهمة."
        )

        addInfo(
            "الخصوصية",
            "لا ينبغي أن تحتوي الإشعارات على معلومات حساسة غير ضرورية."
        )

        addCard(
            "🔐 مركز الخصوصية",
            "مراجعة إعدادات الخصوصية"
        ) {
            showPrivacyCenter()
        }

        addCard(
            "🛡️ الأمان",
            "مراجعة حماية التطبيق"
        ) {
            showSafety()
        }

        addCard(
            "🏠 الرئيسية",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showArchive() {
        baseLayout("الأرشيف")

        addSection(
            "🗄️ أرشيف المشروع",
            "مساحة تنظيمية لحفظ القرارات والمعلومات المهمة"
        )

        addInfo(
            "قرارات المشروع",
            "يتم الاحتفاظ بالقرارات المهمة قبل تنفيذ التغييرات الأساسية."
        )

        addInfo(
            "الإصدارات",
            "يجب تسجيل الإصدارات والتغييرات المهمة بصورة منظمة."
        )

        addInfo(
            "المراجعات",
            "نتائج المراجعة التقنية والأمنية تساعد على تتبع تطور المشروع."
        )

        addInfo(
            "الملكية",
            "المعلومات الخاصة بإدارة المشروع لا تمنح صلاحيات للمستخدمين العاديين."
        )

        addInfo(
            "النسخ الاحتياطية",
            "النسخ الاحتياطية الفعلية تحتاج إلى نظام تخزين آمن خارج واجهة التطبيق."
        )

        addCard(
            "📊 حالة المشروع",
            "عرض حالة النسخة الحالية"
        ) {
            showProjectStatus()
        }

        addCard(
            "📄 المستندات",
            "إدارة المستندات"
        ) {
            showDocuments()
        }

        addCard(
            "🏠 الرئيسية",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showMarketplaceRules() {
        baseLayout("قواعد السوق")

        addSection(
            "🛒 قواعد السوق",
            "قواعد عامة للتجارة والعروض داخل المنصة"
        )

        addInfo(
            "السلع",
            "يجب أن تكون المنتجات والخدمات المعروضة قانونية ومسموحًا بتداولها."
        )

        addInfo(
            "الإعلانات",
            "الإعلان لا يعني أن المنصة تضمن جودة المنتج أو صحة جميع ادعاءاته."
        )

        addInfo(
            "الأسعار",
            "يجب توضيح السعر والعملة وأي رسوم إضافية قبل إتمام المعاملة."
        )

        addInfo(
            "المعاملات",
            "المعاملات المالية الحقيقية تحتاج إلى مزود دفع أو نظام مالي مرخص ومتكامل."
        )

        addInfo(
            "النزاعات",
            "تحتاج النزاعات التجارية إلى آلية واضحة للتواصل والمراجعة."
        )

        addCard(
            "📢 الإعلانات",
            "مستويات الإعلانات"
        ) {
            showAds()
        }

        addCard(
            "📦 المنتجات",
            "استعراض الأقسام"
        ) {
            showProducts()
        }

        addCard(
            "🏠 الرئيسية",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showLocalGlobalTransactions() {
        baseLayout("المعاملات المحلية والدولية")

        addSection(
            "🌍 المعاملات",
            "تنظيم التعاملات المحلية والعابرة للحدود"
        )

        addInfo(
            "محلية",
            "المعاملات المحلية تراعي الدولة والمنطقة والعملة والأنظمة المحلية."
        )

        addInfo(
            "دولية",
            "المعاملات الدولية تحتاج إلى مراعاة قوانين الأطراف والشحن والجمارك."
        )

        addInfo(
            "العملة",
            "عرض العملة لا يعني تنفيذ تحويل مالي فعلي."
        )

        addInfo(
            "الرسوم",
            "يجب توضيح الرسوم والتكاليف قبل إتمام أي معاملة فعلية."
        )

        addInfo(
            "التحقق",
            "الخدمات الحساسة لا تنتقل إلى التنفيذ قبل استكمال التحقق المطلوب."
        )

        addCard(
            "🚚 الشحن",
            "الشحن والخدمات اللوجستية"
        ) {
            showShipping()
        }

        addCard(
            "🧾 الجمارك",
            "إرشادات الجمارك"
        ) {
            showCustoms()
        }

        addCard(
            "📋 قواعد المناطق",
            "القواعد الإقليمية"
        ) {
            showRegionalRules()
        }

        addCard(
            "🏠 الرئيسية",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showCommunitySupport() {
        baseLayout("دعم المجتمع")

        addSection(
            "❤️ دعم المجتمع",
            "مبادرات اجتماعية وخيرية منظمة"
        )

        addInfo(
            "المساعدات",
            "يمكن تنظيم المبادرات الموجهة للأسر والأفراد المحتاجين."
        )

        addInfo(
            "الأيتام",
            "تحتاج برامج دعم الأيتام إلى ضوابط حماية وخصوصية ومراجعة مناسبة."
        )

        addInfo(
            "الشفافية",
            "يجب توثيق مسار الدعم والجهات المسؤولة عنه بصورة واضحة."
        )

        addInfo(
            "الخصوصية",
            "لا ينبغي نشر بيانات المستفيدين الحساسة بشكل علني."
        )

        addInfo(
            "الاعتماد",
            "عرض مبادرة لا يعني أن المنصة تضمن الجهة أو النتائج دون تحقق."
        )

        addCard(
            "🤲 الأعمال الخيرية",
            "العودة إلى قسم الخير والدعم"
        ) {
            showCharity()
        }

        addCard(
            "🧠 المشاريع البشرية",
            "المبادرات والمشاريع المجتمعية"
        ) {
            showHumanProjects()
        }

        addCard(
            "🏠 الرئيسية",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showAppServices() {
        baseLayout("الخدمات العامة")

        addSection(
            "🧰 خدمات المنصة",
            "مجموعة الخدمات الأساسية المتاحة داخل التطبيق"
        )

        addCard(
            "🛍️ السوق",
            "المنتجات والأقسام التجارية"
        ) {
            showProducts()
        }

        addCard(
            "🧑‍💼 الخدمات",
            "الخدمات العامة والمهنية"
        ) {
            showServices()
        }

        addCard(
            "🚚 الشحن",
            "الخدمات اللوجستية"
        ) {
            showShipping()
        }

        addCard(
            "🌍 المعاملات",
            "محلية ودولية"
        ) {
            showLocalGlobalTransactions()
        }

        addCard(
            "🤝 التعاون",
            "الشراكات والمشاريع"
        ) {
            showCooperation()
        }

        addCard(
            "🆘 المساعدة",
            "مركز المساعدة"
        ) {
            showHelpCenter()
        }

        addCard(
            "🏠 الرئيسية",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showPlatformOverview() {
        baseLayout("نظرة عامة")

        addSection(
            "🌐 المنصة",
            "نظرة عامة على بنية CENTRAL MARKET ووظائفها"
        )

        addInfo(
            "السوق",
            "منظومة لعرض المنتجات والخدمات والفرص التجارية."
        )

        addInfo(
            "الخدمات",
            "أقسام متعددة يمكن تنظيمها حسب البلد والمنطقة."
        )

        addInfo(
            "الذكاء",
            "CTM AI والذكاء البشري يعملان ضمن الحدود المعتمدة للمنصة."
        )

        addInfo(
            "الأمان",
            "حماية التطبيق مفعلة على مستوى التطبيق بالكامل."
        )

        addInfo(
            "التمويل",
            "الأقسام المالية الحساسة تحتاج إلى تحقق واعتماد مناسبين."
        )

        addInfo(
            "الدول",
            "المنصة مصممة بفكرة الحزم المحلية والعالمية."
        )

        addCard(
            "🌍 الحزم العالمية",
            "عرض الحزم والدول"
        ) {
            showGlobalPackages()
        }

        addCard(
            "📋 قواعد المناطق",
            "القواعد الإقليمية"
        ) {
            showRegionalRules()
        }

        addCard(
            "🏠 الرئيسية",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showUpdates() {
        baseLayout("التحديثات")

        addSection(
            "🔄 تحديثات المنصة",
            "معلومات عامة عن تطوير التطبيق وإصداراته"
        )

        addInfo(
            "التحديثات",
            "أي تحديث مهم يجب أن يمر بالمراجعة والاختبار قبل اعتماده."
        )

        addInfo(
            "الأمان",
            "التحديثات الأمنية لها أولوية عند وجود مشكلة مؤثرة."
        )

        addInfo(
            "التوافق",
            "يجب مراعاة الأجهزة القديمة والاتصالات الضعيفة قدر الإمكان."
        )

        addInfo(
            "البيانات",
            "يفضل أن تكون التحديثات خفيفة لتقليل استهلاك الإنترنت."
        )

        addInfo(
            "الاختبار",
            "يتم اختبار النسخة قبل اعتمادها للإصدار."
        )

        addCard(
            "📊 حالة المشروع",
            "عرض الحالة الحالية"
        ) {
            showProjectStatus()
        }

        addCard(
            "🧪 التشخيص",
            "فحص الجهاز والاتصال"
        ) {
            showDiagnostics()
        }

        addCard(
            "🏠 الرئيسية",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showAccessibility() {
        baseLayout("سهولة الوصول")

        addSection(
            "♿ سهولة الوصول",
            "تصميم الخدمات بصورة أسهل لمختلف المستخدمين"
        )

        addInfo(
            "الوضوح",
            "استخدام عناوين واضحة وأزرار مباشرة قدر الإمكان."
        )

        addInfo(
            "النص",
            "الحفاظ على نصوص قابلة للقراءة على الشاشات المختلفة."
        )

        addInfo(
            "الأداء",
            "تقليل العناصر الثقيلة لدعم الأجهزة والاتصالات الضعيفة."
        )

        addInfo(
            "التنقل",
            "توفير مسارات واضحة للعودة إلى الأقسام السابقة."
        )

        addInfo(
            "اللغات",
            "يمكن توسيع دعم اللغات ضمن الإصدارات والحزم المستقبلية."
        )

        addCard(
            "📱 قدرات الجهاز",
            "فحص قدرات الجهاز الحالية"
        ) {
            showDeviceCapabilities()
        }

        addCard(
            "🆘 المساعدة",
            "مركز المساعدة"
        ) {
            showHelpCenter()
        }

        addCard(
            "🏠 الرئيسية",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showAboutPlatform() {
        baseLayout("عن المنصة")

        addSection(
            "ℹ️ عن المنصة",
            "معلومات تعريفية عن المشروع"
        )

        addInfo(
            "الاسم",
            "CENTRAL MARKET"
        )

        addInfo(
            "الملكية",
            "المالك ياسر حسن وشركاؤه"
        )

        addInfo(
            "الهدف",
            "إنشاء منصة متعددة الخدمات والأسواق قابلة للتوسع حسب الدول والمناطق."
        )

        addInfo(
            "التصميم",
            "واجهة بسيطة وخفيفة وقابلة للاستخدام على أجهزة متعددة."
        )

        addInfo(
            "الأمان",
            "الحماية العامة مفعلة على مستوى التطبيق."
        )

        addInfo(
            "التمويل",
            "الخدمات المالية الحساسة تحتاج إلى أنظمة تحقق واعتماد فعلية قبل التشغيل الإنتاجي."
        )

        addCard(
            "📄 معلومات التطبيق",
            "التفاصيل الفنية والإصدار"
        ) {
            showAppInformation()
        }

        addCard(
            "📊 حالة المشروع",
            "عرض حالة المشروع"
        ) {
            showProjectStatus()
        }

        addCard(
            "🏠 الرئيسية",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }

    private fun showFinalReview() {
        baseLayout("المراجعة")

        addSection(
            "🔎 المراجعة النهائية",
            "نقطة مراجعة قبل اعتماد أي نسخة جديدة"
        )

        addInfo(
            "الكود",
            "يجب مراجعة الدوال والاستدعاءات والأقواس قبل البناء."
        )

        addInfo(
            "الأمان",
            "حماية التطبيق العامة لا ينبغي تعطيلها من أي شاشة."
        )

        addInfo(
            "البيانات",
            "الخدمات الحساسة تحتاج إلى تحقق فعلي قبل التشغيل الإنتاجي."
        )

        addInfo(
            "المالية",
            "لا يتم تنفيذ معاملات مالية حقيقية من هذه الواجهة وحدها."
        )

        addInfo(
            "الحالة",
            "هذه الشاشة تنظيمية ولا تعتبر بديلًا عن الاختبارات الفعلية."
        )

        addCard(
            "🛡️ قائمة الأمان",
            "مراجعة عناصر الحماية"
        ) {
            showSecurityChecklist()
        }

        addCard(
            "🧪 التشخيص",
            "فحص الجهاز والاتصال"
        ) {
            showDiagnostics()
        }

        addCard(
            "📊 حالة المشروع",
            "عرض حالة المشروع"
        ) {
            showProjectStatus()
        }

        addCard(
            "🏠 الرئيسية",
            "العودة إلى الرئيسية"
        ) {
            showHome()
        }
    }
