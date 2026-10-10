package com.central.market

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.InputType
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val navy = Color.rgb(18, 42, 66)
    private val blue = Color.rgb(32, 104, 170)
    private val gold = Color.rgb(205, 157, 45)
    private val green = Color.rgb(45, 145, 85)
    private val red = Color.rgb(180, 55, 55)
    private val white = Color.WHITE
    private val light = Color.rgb(245, 247, 250)
    private val darkText = Color.rgb(25, 35, 45)
    private val gray = Color.rgb(105, 115, 125)
    private val officeDark = Color.rgb(15, 31, 48)
    private val badgerBlack = Color.rgb(20, 23, 27)

    private lateinit var content: LinearLayout
    private val handler = Handler(Looper.getMainLooper())

    private enum class UserRole {
        USER, ADMIN, PARTNER, OWNER
    }

    private var currentRole = UserRole.USER
    private var currentAccountName = "زائر"
    private var simulationLoggedIn = false
    private var sensitiveLocked = false

    private var lastBackgroundTime = 0L
    private var lastActivityTime = 0L
    private var failedLoginAttempts = 0
    private var loginBlockedUntil = 0L
    private var searchTapCount = 0

    private val globalTimeout = 10 * 60 * 1000L
    private val sensitiveTimeout = 5 * 60 * 1000L

    private val pageHistory = ArrayList<() -> Unit>()
    private var currentPage: (() -> Unit)? = null
    private var ignoreHistoryOnce = false

    private val globalLockRunnable = Runnable {
        if (simulationLoggedIn) lockAccount()
    }

    private val sensitiveLockRunnable = Runnable {
        if (simulationLoggedIn) {
            sensitiveLocked = true
            showSensitiveLock()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        showHome()
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (pageHistory.isNotEmpty()) {
            val previous = pageHistory.removeAt(pageHistory.lastIndex)
            ignoreHistoryOnce = true
            previous.invoke()
            ignoreHistoryOnce = false
        } else {
            goHome()
        }
    }

    override fun onPause() {
        super.onPause()

        if (simulationLoggedIn) {
            lastBackgroundTime = SystemClock.elapsedRealtime()
            handler.removeCallbacks(globalLockRunnable)
            handler.postDelayed(globalLockRunnable, globalTimeout)
        }
    }

    override fun onResume() {
        super.onResume()
        handler.removeCallbacks(globalLockRunnable)

        if (simulationLoggedIn && lastBackgroundTime > 0L) {
            val elapsed = SystemClock.elapsedRealtime() - lastBackgroundTime
            if (elapsed >= globalTimeout) lockAccount()
        }

        lastBackgroundTime = 0L
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        registerActivity()
    }

    override fun onDestroy() {
        handler.removeCallbacks(globalLockRunnable)
        handler.removeCallbacks(sensitiveLockRunnable)
        super.onDestroy()
    }

    private fun registerActivity() {
        lastActivityTime = SystemClock.elapsedRealtime()
    }

    private fun openPage(page: () -> Unit) {
        if (!ignoreHistoryOnce) currentPage?.let { pageHistory.add(it) }
        currentPage = page
        page.invoke()
    }

    private fun goHome() {
        pageHistory.clear()
        currentPage = null
        showHome()
    }

    private fun exitPage() {
        if (pageHistory.isNotEmpty()) {
            val previous = pageHistory.removeAt(pageHistory.lastIndex)
            ignoreHistoryOnce = true
            previous.invoke()
            ignoreHistoryOnce = false
        } else {
            goHome()
        }
    }

    private fun baseLayout(title: String, subtitle: String = "") {
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 22)
            
            setBackgroundColor(light)
        }

        val scroll = ScrollView(this).apply {
            setBackgroundColor(light)
            addView(
                content,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }

        setContentView(scroll)
        addTopNavigation(title)
        

        if (subtitle.isNotBlank()) {
            val sub = TextView(this).apply {
                text = subtitle
                textSize = 13f
                setTextColor(gray)
                gravity = Gravity.CENTER
                setPadding(4, 6, 4, 12)
            }
            content.addView(sub)
        }
    }

    private fun addTopNavigation(title: String) {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(6, 6, 6, 6)
            background = roundedBackground(navy, 20f)
        }

        fun navButton(label: String, color: Int, action: () -> Unit): Button {
            return Button(this).apply {
                text = label
                textSize = 23f
                isAllCaps = false
                setTextColor(white)
                gravity = Gravity.CENTER
                background = roundedBackground(color, 16f)
                setOnClickListener { action() }
            }
        }

        val back = navButton("‹", blue) { exitPage() }
        val heading = TextView(this).apply {
            text = title
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
            gravity = Gravity.CENTER
        }
        val home = navButton("⌂", blue) { goHome() }
        val exit = navButton("×", red) { finish() }

        bar.addView(back, LinearLayout.LayoutParams(46, 48))
        bar.addView(
            heading,
            LinearLayout.LayoutParams(0, 48, 1f)
        )
        bar.addView(home, LinearLayout.LayoutParams(46, 48))
        bar.addView(exit, LinearLayout.LayoutParams(46, 48))
        content.addView(bar)

        val brand = TextView(this).apply {
            text = "CENTRAL MARKET"
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(gold)
            gravity = Gravity.CENTER
            setPadding(0, 7, 0, 4)
        }
        content.addView(brand)
    }

    private fun addSection(title: String) {
        content.addView(TextView(this).apply {
            text = title
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(navy)
            setPadding(4, 14, 4, 8)
        })
    }

    private fun addInfo(title: String, body: String) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 14, 16, 14)
            background = cardBackground()
        }

        card.addView(TextView(this).apply {
            text = title
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(navy)
        })

        card.addView(TextView(this).apply {
            text = body
            textSize = 14f
            setTextColor(darkText)
            setPadding(0, 6, 0, 0)
        })

        content.addView(card, LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, 0, 0, 10)
        })
    }

    private fun addCard(
        symbol: String,
        title: String,
        description: String,
        action: (() -> Unit)? = null
    ) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(14, 12, 14, 12)
            background = cardBackground()
            isClickable = action != null
            setOnClickListener { action?.invoke() }
        }

        card.addView(TextView(this).apply {
            text = symbol
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(gold)
        }, LinearLayout.LayoutParams(50, 64))

        val textBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 0, 4, 0)
        }

        textBox.addView(TextView(this).apply {
            text = title
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(navy)
        })

        textBox.addView(TextView(this).apply {
            text = description
            textSize = 13f
            setTextColor(gray)
            setPadding(0, 4, 0, 0)
        })

        card.addView(textBox, LinearLayout.LayoutParams(0, -2, 1f))
        content.addView(card, LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, 0, 0, 10)
        })
    }

    private fun addStatus(
        title: String,
        value: String,
        statusColor: Int = green
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(14, 10, 14, 10)
            background = cardBackground()
        }

        row.addView(TextView(this).apply {
            text = title
            textSize = 14f
            setTextColor(darkText)
        }, LinearLayout.LayoutParams(0, -2, 1f))

        row.addView(TextView(this).apply {
            text = value
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(statusColor)
            gravity = Gravity.END
        })

        content.addView(row, LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, 0, 0, 8)
        })
    }

    private fun addVisualBanner(
        icon: String,
        title: String,
        description: String,
        firstColor: Int,
        secondColor: Int
    ) {
        val banner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(18, 22, 18, 22)
            background = gradientBackground(firstColor, secondColor, 24f)
        }

        banner.addView(TextView(this).apply {
            text = icon
            textSize = 44f
            gravity = Gravity.CENTER
            setTextColor(white)
        })

        banner.addView(TextView(this).apply {
            text = title
            textSize = 23f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(white)
            setPadding(0, 6, 0, 4)
        })

        banner.addView(TextView(this).apply {
            text = description
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(235, 242, 248))
        })

        content.addView(banner, LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, 8, 0, 14)
        })
    }

        // =========================================================
    // الرئيسية
    // =========================================================

    private fun showHome() {
        pageHistory.clear()
        currentPage = null

        baseLayout("CENTRAL MARKET", "منصة واحدة .. عالم من الفرص.")

        addVisualBanner(
            "◈", "CENTRAL MARKET", "المالك ياسر حسن وشركاؤه",
            navy, blue
        )

        val online = isOnline()
        addStatus("حالة الاتصال", if (online) "متصل" else "غير متصل",
            if (online) green else red)

        addSection("المنصة")

        addCard("🛒", "الأسواق والمنتجات",
            "المركبات والشاحنات والإلكترونيات وغيرها.",
            { openPage { showProducts() } })

        addCard("⚙", "الخدمات",
            "الخدمات العامة والذكاء البشري وCTM AI.",
            { openPage { showServices() } })

        addCard("📢", "الإعلانات",
            "BRONZE وSILVER وGOLD.",
            { openPage { showAds() } })

        addCard("🍲", "المطبخ", "المطاعم والمنتجات الغذائية.",
            { openPage { showKitchen() } })

        addCard("🤝", "الصدقة والدعم",
            "مبادرات الأيتام والمحتاجين.",
            { openPage { showCharity() } })

        addSection("الوصول والحماية")

        addCard("📡", "اختبار الاتصال",
            "فحص الشبكة دون تشغيل تتبع الموقع.",
            { openPage { showConnectivity() } })

        addCard("👤", "الوضع الضيف",
            "تصفح الوظائف العامة.",
            { openPage { showGuestMode() } })

        addCard("🔐", "الأمان والخصوصية",
            "قواعد حماية الحساب والبيانات.",
            { openPage { showSecurity() } })

        addCard("💡", "فكرة أو اقتراح",
            "إرسال اقتراح تطويري.",
            { openPage { showIdea() } })

        addSection("الحساب")

        addCard("◉", "الحساب والدخول",
            "المستخدم والإدارة والشريك والمالك.",
            { openPage { showSimulationLogin() } })

        if (currentRole == UserRole.ADMIN ||
            currentRole == UserRole.PARTNER ||
            currentRole == UserRole.OWNER) {
            addCard("▣", "المكتب الإداري",
                "الأدوات المتاحة حسب صلاحية الجلسة.",
                { openPage { showManagerOffice() } })
        }

        addInfo("قاعدة الخصوصية",
            "لا يعمل تتبع الموقع كميزة عامة. يقتصر على الخدمات التي تحتاج إليه فعليًا وبما يتفق مع الموافقة والقوانين.")
    }

    // =========================================================
    // الحسابات والدخول التجريبي
    // =========================================================

    private fun showSimulationLogin() {
        baseLayout("الحسابات", "اختيار نوع الحساب")

        addInfo("نظام الحساب",
            "هذه شاشة محاكاة محلية. لا تمثل مصادقة إنتاجية ولا تنفذ عمليات مالية حقيقية.")

        addCard("👤", "زائر / مستخدم عام",
            "الوظائف العامة.",
            { loginSimulation(UserRole.USER, "زائر") })

        addCard("▣", "حساب الإدارة",
            "أدوات الإدارة التجريبية.",
            { showPasswordGate(UserRole.ADMIN, "حساب الإدارة") })

        addCard("🤝", "حساب الشريك",
            "واجهة الشريك التجريبية.",
            { showPasswordGate(UserRole.PARTNER, "حساب الشريك") })

        addCard("★", "حساب المالك",
            "واجهة المالك التجريبية.",
            { showPasswordGate(UserRole.OWNER, "حساب المالك") })
    }

    private fun showPasswordGate(role: UserRole, accountName: String) {
        baseLayout("دخول الحساب", accountName)

        addInfo("تنبيه",
            "رموز الدخول هنا تجريبية فقط. لا تستخدم هذه الآلية لحماية بيانات أو أموال حقيقية.")

        val input = EditText(this).apply {
            hint = "أدخل رمز المحاكاة"
            textSize = 16f
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_PASSWORD
            setPadding(14, 12, 14, 12)
            background = roundedBackground(white, 14f)
        }

        content.addView(input, LinearLayout.LayoutParams(-1, 56).apply {
            setMargins(0, 0, 0, 12)
        })

        val login = Button(this).apply {
            text = "دخول"
            isAllCaps = false
            setTextColor(white)
            background = roundedBackground(blue, 16f)
            setOnClickListener {
                loginSimulation(role, accountName, input.text.toString())
            }
        }

        content.addView(login, LinearLayout.LayoutParams(-1, 54))
        addInfo("الصلاحيات", roleName(role))
    }

    private fun loginSimulation(
        role: UserRole,
        accountName: String,
        password: String = ""
    ) {
        val now = SystemClock.elapsedRealtime()

        if (now < loginBlockedUntil) {
            val remaining = ((loginBlockedUntil - now) / 1000L) + 1L
            Toast.makeText(
                this,
                "محاولات الدخول موقوفة مؤقتًا. حاول بعد $remaining ثانية.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val valid = when (role) {
            UserRole.USER -> true
            UserRole.ADMIN -> password == "ADMIN-DEMO"
            UserRole.PARTNER -> password == "PARTNER-DEMO"
            UserRole.OWNER -> password == "OWNER-DEMO"
        }

        if (!valid) {
            failedLoginAttempts++

            if (failedLoginAttempts >= 5) {
                loginBlockedUntil = now + 60_000L
                failedLoginAttempts = 0
                Toast.makeText(
                    this,
                    "تم إيقاف محاولات الدخول لمدة دقيقة بسبب المحاولات المتكررة.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "رمز غير صحيح. المحاولة ${failedLoginAttempts} من 5",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return
        }

        failedLoginAttempts = 0
        loginBlockedUntil = 0L
        currentRole = role
        currentAccountName = accountName
        simulationLoggedIn = true
        sensitiveLocked = false
        lastBackgroundTime = 0L

        handler.removeCallbacks(globalLockRunnable)
        handler.removeCallbacks(sensitiveLockRunnable)

        registerActivity()
        showAccountHome()
    }

    private fun showAccountHome() {
        if (!simulationLoggedIn) {
            showSimulationLogin()
            return
        }

        baseLayout("حسابي", currentAccountName)

        addVisualBanner(
            when (currentRole) {
                UserRole.USER -> "👤"
                UserRole.ADMIN -> "▣"
                UserRole.PARTNER -> "🤝"
                UserRole.OWNER -> "★"
            },
            currentAccountName,
            "الدور: ${roleName(currentRole)}",
            when (currentRole) {
                UserRole.USER -> blue
                UserRole.ADMIN -> officeDark
                UserRole.PARTNER -> blue
                UserRole.OWNER -> gold
            },
            navy
        )

        addStatus("حالة الجلسة", "نشطة", green)

        addCard("◉", "بيانات الحساب",
            "عرض حالة الحساب والصلاحيات.",
            { openPage { showAccount() } })

        addCard("★", "النقاط", "نظام النقاط.",
            { openPage { showPoints() } })

        addCard("♡", "المفضلة", "العناصر المحفوظة.",
            { openPage { showFavorites() } })

        addCard("⌕", "البحث", "البحث داخل أقسام المنصة.",
            { openPage { showSearch() } })

        addCard("↪", "تسجيل الخروج",
            "إنهاء الجلسة وإعادة التحقق عند الدخول التالي.",
            { logoutSimulation() })

        if (currentRole == UserRole.ADMIN ||
            currentRole == UserRole.PARTNER ||
            currentRole == UserRole.OWNER) {
            addCard("▣", "المكتب الإداري",
                "فتح المكتب وفق الصلاحية.",
                { openPage { showManagerOffice() } })
        }
    }

    private fun showAccount() {
        when (currentRole) {
            UserRole.USER -> showUserAccount()
            UserRole.ADMIN -> showAdminAccount()
            UserRole.PARTNER -> showPartnerAccount()
            UserRole.OWNER -> showOwnerAccount()
        }
    }

    private fun showUserAccount() {
        baseLayout("حساب المستخدم", currentAccountName)
        addStatus("الدور", "مستخدم عام", blue)
        addInfo("الوصول", "الوظائف العامة المتاحة داخل المنصة.")
        addInfo("الحماية", "لا يملك هذا الحساب صلاحية فتح الوظائف الإدارية الحساسة.")
    }

    private fun showAdminAccount() {
        baseLayout("حساب الإدارة", currentAccountName)
        addVisualBanner("▣", "ADMIN OFFICE",
            "واجهة الإدارة والمراجعة", officeDark, navy)
        addStatus("الدور", "إدارة", blue)
        addStatus("الجلسة", "محمية محليًا", green)
        addInfo("الصلاحيات",
            "واجهة تجريبية للإدارة والتشخيص. الصلاحيات الإنتاجية تحتاج تحققًا من خادم موثوق.")
    }

    private fun showPartnerAccount() {
        baseLayout("حساب الشريك", currentAccountName)
        addVisualBanner("🤝", "PARTNER OFFICE",
            "واجهة الشريك والأعمال", blue, navy)
        addStatus("الدور", "شريك", blue)
        addInfo("الصلاحيات",
            "واجهة محلية تجريبية لا تمثل إثبات هوية أو صلاحية قانونية.")
    }

    private fun showOwnerAccount() {
        baseLayout("حساب المالك", currentAccountName)
        addVisualBanner("★", "OWNER OFFICE",
            "الإدارة العليا والمراجعة", gold, navy)
        addStatus("الدور", "مالك", gold)
        addInfo("المالك", "ياسر حسن وشركاؤه")
        addInfo("قاعدة الاعتماد",
            "لا تنفذ هذه المحاكاة عمليات مالية حقيقية أو تغييرات إدارية على خادم.")
    }

    // =========================================================
    // الأسواق والمنتجات
    // =========================================================

    private fun showProducts() {
        baseLayout("الأسواق والمنتجات", "اختيار الفئة")
        addInfo("الأسواق",
            "منصة متعددة الفئات مع إمكانية تخصيص الحزم حسب الدولة والمنطقة.")

        val categories = listOf(
            "🚚" to ("المركبات والشاحنات" to "مركبات وشاحنات وخدمات مرتبطة بها."),
            "📱" to ("الهواتف والإلكترونيات" to "هواتف وأجهزة إلكترونية."),
            "🍽" to ("المطاعم والتوصيل" to "مطاعم وخدمات توصيل."),
            "🌾" to ("الزراعة والثروة الحيوانية" to "منتجات زراعية وحيوانية."),
            "🐟" to ("الأسماك" to "منتجات وخدمات الأسماك."),
            "🏗" to ("البناء والجملة" to "مواد البناء وتجارة الجملة."),
            "🏥" to ("الصحة" to "معلومات وخدمات صحية عامة."),
            "⚽" to ("الرياضة" to "منتجات وخدمات رياضية."),
            "💡" to ("الكهرباء والمياه" to "احتياجات الكهرباء والمياه."),
            "🎓" to ("التعليم" to "الخدمات والمعلومات التعليمية."),
            "✈" to ("السفر" to "خدمات السفر والتذاكر.")
        )

        categories.forEach { item ->
            addCard(item.first, item.second.first, item.second.second) {
                openPage { showCategory(item.second.first) }
            }
        }
    }

    private fun showCategory(category: String) {
        baseLayout(category, "قسم CENTRAL MARKET")
        addInfo("الفئة", category)
        addStatus("الحالة", "متاح للتصفح", green)

        addInfo("التعاملات",
            "هذه النسخة تعرض واجهة محلية. التعاملات الحقيقية تحتاج خدمات خلفية واعتمادات مناسبة.")

        if (category == "المركبات والشاحنات" || category == "السفر") {
            addInfo("الموقع والتتبع",
                "لا يعمل تتبع الموقع كميزة عامة. يقتصر على خدمة تتطلبه فعليًا وبموافقة ومتطلبات قانونية مناسبة.")
        }

        addCard("♡", "إضافة للمفضلة",
            "حفظ الفئة خلال هذه الجلسة.",
            {
                Toast.makeText(this, "تم اختيار الفئة للمفضلة (محليًا)",
                    Toast.LENGTH_SHORT).show()
            })

        addCard("⌕", "البحث داخل الفئة",
            "فتح البحث.",
            { openPage { showSearch() } })
    }

    private fun roleName(role: UserRole): String = when (role) {
        UserRole.USER -> "مستخدم"
        UserRole.ADMIN -> "إدارة"
        UserRole.PARTNER -> "شريك"
        UserRole.OWNER -> "مالك"
    }

        // =========================================================
    // الخدمات
    // =========================================================

    private fun showServices() {
        baseLayout("الخدمات", "خدمات CENTRAL MARKET")

        addVisualBanner("⚙", "SERVICES",
            "الخدمات والذكاء والخدمات المجتمعية", blue, navy)

        addCard("🧠", "الذكاء البشري",
            "المعرفة والمساعدة البشرية المنظمة.",
            { openPage { showHumanIntelligence() } })

        addCard("◉", "CTM AI",
            "المساعد الرسمي للمنصة ضمن الصلاحيات المعتمدة.",
            { openPage { showCtmAi() } })

        addCard("🏥", "الصحة", "معلومات صحية عامة.",
            { openPage { showCategory("الصحة") } })

        addCard("🎓", "التعليم", "خدمات تعليمية.",
            { openPage { showCategory("التعليم") } })

        addCard("⚡", "الكهرباء والمياه", "الاحتياجات الأساسية.",
            { openPage { showCategory("الكهرباء والمياه") } })

        addCard("✈", "السفر", "خدمات السفر والتذاكر.",
            { openPage { showCategory("السفر") } })

        addCard("🤝", "الدعم المجتمعي", "مبادرات الدعم.",
            { openPage { showCharity() } })

        addCard("🦡", "BADGER",
            "واجهة المنظومة المالية والتحليلات.",
            { openPage { showBadger() } })

        addCard("✉", "مراسلة المنصة",
            "كتابة رسالة محلية.",
            { openPage { showMessage() } })
    }

    private fun showHumanIntelligence() {
        baseLayout("الذكاء البشري", "خدمات المعرفة والمساعدة")

        addVisualBanner("🧠", "HUMAN INTELLIGENCE",
            "المعرفة البشرية والمساعدة المنظمة", navy, blue)

        addInfo("الفكرة",
            "قسم للمساعدة البشرية والمعرفة والخدمات المنظمة داخل المنصة.")

        addInfo("الزكاة البشرية",
            "يمكن تخصيص وظائف إنسانية لحزمة السودان وفق القوانين والجهات المختصة.")

        addInfo("الحزم الدولية",
            "الوظائف الحكومية الخاصة بدولة معينة لا تُفرض على النسخ الخارجية.")

        addStatus("حالة القسم", "واجهة محلية", green)
    }

    private fun showCtmAi() {
        baseLayout("CTM AI", "المساعد الرسمي للمنصة")

        addVisualBanner("◉", "CTM AI",
            "CENTRAL MARKET Intelligence", blue, navy)

        addInfo("وظيفة المساعد",
            "واجهة مخصصة للمساعدة في البحث وقراءة المعلومات المسموح بها.")

        addInfo("الخصوصية",
            "يجب ألا يصل المساعد إلى بيانات أو أقسام لا يملك المستخدم صلاحية الوصول إليها.")

        addInfo("الموافقة",
            "التغييرات الحساسة تخضع للاعتماد المطلوب عند ربط النظام بخدمات إنتاجية.")

        addInfo("التكامل",
            "هذه الصفحة واجهة محلية وليست اتصالًا فعليًا بمحرك ذكاء اصطناعي.")
    }

    // =========================================================
    // BADGER
    // =========================================================

    private fun showBadger() {
        baseLayout("BADGER", "المنظومة المالية المقترحة")

        addVisualBanner("🦡", "BADGER",
            "Banking • Analytics • Deposits • Global",
            badgerBlack, navy)

        addStatus("حالة الحماية", "واجهة محلية", green)

        addCard("▣", "لوحة BADGER",
            "الحساب والتحليلات المتاحة.",
            { openPage { showBadgerDashboard() } })

        addCard("◆", "المشاركة الاستثمارية",
            "وظيفة حساسة تتطلب تحققًا واعتمادات مناسبة.",
            { openPage { showSensitiveInvestment() } })

        addCard("▰", "الحماية الحساسة",
            "عرض حالة القفل الحساس.",
            {
                if (!simulationLoggedIn) {
                    showSimulationLogin()
                } else {
                    lockSensitiveSection()
                }
            })

        addInfo("مبدأ BADGER",
            "منتج مقترح قابل للتطوير للمؤسسات وفق العقود والاعتمادات المناسبة.")

        addInfo("التنفيذ المالي",
            "هذه النسخة لا تنفذ تحويلات أو إيداعات أو استثمارات مالية حقيقية.")
    }

    private fun showBadgerDashboard() {
        baseLayout("BADGER Dashboard", "لوحة المنظومة")

        addVisualBanner("🦡", "BADGER",
            "Financial Technology Interface", badgerBlack, gold)

        addStatus("الجلسة",
            if (simulationLoggedIn) "نشطة" else "زائر",
            if (simulationLoggedIn) green else gray)

        addStatus("القفل الحساس", "5 دقائق", gold)

        addCard("◈", "تحليلات الإيداع",
            "معلومات محاكاة دون تنفيذ مالي.",
            {
                Toast.makeText(this,
                    "التحليلات محاكاة فقط",
                    Toast.LENGTH_SHORT).show()
            })

        addCard("◆", "تحليلات الاستثمار",
            "عرض واجهة الاستثمار الحساسة.",
            { openPage { showSensitiveInvestment() } })

        addCard("▰", "السجلات والعقود",
            "متطلبات توثيق العمولات والعلاقات التجارية.",
            { openPage { showDocuments() } })

        addInfo("الحالة",
            "أي تكامل مالي فعلي يحتاج بنية خلفية آمنة ومؤسسة مخولة واعتمادات مناسبة.")
    }

    // =========================================================
    // الوظائف الحساسة
    // =========================================================

    private fun showSensitiveInvestment() {
        if (!simulationLoggedIn) {
            Toast.makeText(this,
                "يجب الدخول إلى الحساب أولًا",
                Toast.LENGTH_SHORT).show()
            showSimulationLogin()
            return
        }

        if (sensitiveLocked) {
            showSensitiveLock()
            return
        }

        handler.removeCallbacks(sensitiveLockRunnable)
        handler.postDelayed(sensitiveLockRunnable, sensitiveTimeout)

        baseLayout("المشاركة الاستثمارية", "وظيفة حساسة")

        addVisualBanner("◆", "SENSITIVE",
            "حماية الوظائف المالية الحساسة", red, navy)

        addStatus("حماية الشاشة", "مفعلة", green)
        addStatus("القفل التلقائي", "5 دقائق", gold)

        addInfo("الأهلية",
            "أي مشاركة مالية حقيقية تحتاج تحققًا قانونيًا ورسميًا وتفويضات مناسبة.")

        addInfo("الموانع القانونية",
            "لا يجوز تجاوز مانع قانوني موثق. التحقق الحقيقي يحتاج تكاملًا رسميًا معتمدًا.")

        addInfo("حماية الشاشة",
            "تم تفعيل FLAG_SECURE على مستوى النافذة لمنع لقطات الشاشة المعتادة قدر الإمكان.")

        addInfo("التنفيذ",
            "لا توجد عملية استثمار أو تحويل أموال حقيقية في هذه النسخة.")

        addCard("🔒", "إغلاق الوظيفة الحساسة",
            "إغلاق الشاشة الحساسة فورًا.",
            { lockSensitiveSection() })
    }

    private fun showSensitiveLock() {
        handler.removeCallbacks(sensitiveLockRunnable)
        sensitiveLocked = true

        baseLayout("القفل الحساس", "تم تأمين الوظيفة")

        addVisualBanner("🔒", "LOCKED",
            "تم إغلاق القسم الحساس", red, officeDark)

        addInfo("سبب القفل",
            "انتهت مدة الأمان المحددة للوظائف الحساسة أو تم إغلاقها يدويًا.")

        addStatus("الحالة", "مقفلة", red)

        addCard("↻", "إعادة الدخول",
            "العودة إلى الحساب وإعادة فتح الوظيفة بعد التحقق.",
            {
                sensitiveLocked = true
                showAccountHome()
            })

        addCard("⌂", "الرئيسية",
            "العودة إلى الصفحة الرئيسية.",
            {
                sensitiveLocked = true
                goHome()
            })
    }

    private fun lockSensitiveSection() {
        handler.removeCallbacks(sensitiveLockRunnable)
        sensitiveLocked = true
        showSensitiveLock()
    }

    // =========================================================
    // قفل الجلسة وتسجيل الخروج
    // =========================================================

    private fun lockAccount() {
        if (!simulationLoggedIn) return

        handler.removeCallbacks(globalLockRunnable)
        handler.removeCallbacks(sensitiveLockRunnable)

        sensitiveLocked = true
        showSessionLock()
    }

    private fun showSessionLock() {
        baseLayout("قفل الجلسة", "حماية الحساب")

        addVisualBanner("🔒", "SESSION LOCK",
            "تم تأمين الجلسة", red, navy)

        addStatus("الحالة", "مقفلة", red)

        addInfo("السبب",
            "انتهت مدة عدم النشاط في الخلفية أو انتهت المهلة المحددة.")

        addCard("↻", "إعادة الدخول",
            "العودة إلى شاشة الحسابات.",
            {
                clearSession()
                showSimulationLogin()
            })

        addCard("⌂", "الرئيسية",
            "إنهاء الجلسة والعودة للرئيسية.",
            {
                clearSession()
                goHome()
            })
    }

    private fun clearSession() {
        handler.removeCallbacks(globalLockRunnable)
        handler.removeCallbacks(sensitiveLockRunnable)

        simulationLoggedIn = false
        sensitiveLocked = false
        currentRole = UserRole.USER
        currentAccountName = "زائر"
        lastBackgroundTime = 0L
        lastActivityTime = 0L
    }

    private fun logoutSimulation() {
        clearSession()

        Toast.makeText(this,
            "تم تسجيل الخروج. يلزم التحقق مجددًا عند الدخول.",
            Toast.LENGTH_SHORT).show()

        goHome()
    }

        // =========================================================
    // المكتب الإداري
    // =========================================================

    private fun showManagerOffice() {
        if (!simulationLoggedIn ||
            (currentRole != UserRole.ADMIN &&
             currentRole != UserRole.PARTNER &&
             currentRole != UserRole.OWNER)) {
            Toast.makeText(this,
                "هذه المنطقة مخصصة للحسابات المصرح لها",
                Toast.LENGTH_SHORT).show()
            return
        }

        baseLayout("المكتب الإداري", "الوصول حسب الصلاحية")

        addVisualBanner("▣", "MANAGEMENT OFFICE",
            "مكتب الإدارة والمراجعة", officeDark, navy)

        addStatus("الحساب", currentAccountName, blue)
        addStatus("الصلاحية", roleName(currentRole), green)

        addCard("▣", "أدوات الإدارة",
            "المراجعة والإدارة.",
            { openPage { showManagerTools() } })

        addCard("◈", "التشخيص",
            "حالة الاتصال ومكونات التطبيق.",
            { openPage { showDiagnostics() } })

        if (currentRole == UserRole.PARTNER ||
            currentRole == UserRole.OWNER) {
            addCard("🤝", "مكتب الشريك",
                "وظائف الشراكة.",
                { openPage { showPartnerOffice() } })
        }

        if (currentRole == UserRole.OWNER) {
            addCard("★", "مكتب المالك",
                "الموافقات والإدارة العليا.",
                { openPage { showOwnerOffice() } })
        }
    }

    private fun showManagerTools() {
        if (!isPrivileged()) {
            showHome()
            return
        }

        baseLayout("أدوات الإدارة", "Management Tools")

        addVisualBanner("▣", "ADMIN TOOLS",
            "المراجعة والتشخيص", officeDark, blue)

        addCard("◈", "تشخيص النظام",
            "فحص الحالة المحلية.",
            { openPage { showDiagnostics() } })

        addCard("▤", "ملخص النظام",
            "ملخص المكونات والحماية.",
            { openPage { showSystemSummary() } })

        addCard("▰", "الوثائق",
            "الوثائق والاعتمادات.",
            { openPage { showDocuments() } })

        if (currentRole == UserRole.OWNER) {
            addCard("★", "إدارة الشركاء",
                "واجهة إدارة الشركاء.",
                { openPage { showPartnerManagement() } })

            addCard("✓", "اعتماد المالك",
                "مراجعة الوظائف التي تحتاج اعتمادًا.",
                { openPage { showOwnerApproval() } })
        }
    }

    private fun isPrivileged(): Boolean {
        return simulationLoggedIn &&
            (currentRole == UserRole.ADMIN ||
             currentRole == UserRole.PARTNER ||
             currentRole == UserRole.OWNER)
    }

    private fun showDiagnostics() {
        if (!isPrivileged()) {
            showHome()
            return
        }

        baseLayout("التشخيص", "حالة المكونات المحلية")

        val online = isOnline()
        addStatus("الاتصال", if (online) "متصل" else "غير متصل",
            if (online) green else red)

        addStatus("حماية الشاشة", "مفعلة", green)
        addStatus("جلسة المستخدم",
            if (simulationLoggedIn) "نشطة" else "غير مسجلة",
            if (simulationLoggedIn) green else gray)

        addStatus("BADGER", "واجهة محلية", blue)
        addStatus("CTM AI", "واجهة محلية", blue)

        addInfo("ملاحظة",
            "التكاملات الحكومية والمالية الحقيقية تحتاج خدمات خلفية رسمية وتفويضات مناسبة.")
    }

    private fun showSystemSummary() {
        if (!isPrivileged()) {
            showHome()
            return
        }

        baseLayout("ملخص النظام", "حالة CENTRAL MARKET")

        addVisualBanner("◈", "SYSTEM",
            "ملخص الحماية والتكامل", navy, blue)

        addStatus("الحماية الحساسة", "مفعلة كواجهة", green)
        addStatus("حماية الشاشة", "FLAG_SECURE", green)
        addStatus("القفل الحساس", "5 دقائق", gold)
        addStatus("BADGER", "واجهة محلية", blue)
        addStatus("CTM AI", "واجهة محلية", blue)

        addInfo("التكاملات الحكومية",
            "تحتاج مصادر رسمية أو جهات مخولة وتفويضات مناسبة.")

        addInfo("التكاملات المالية",
            "تحتاج مؤسسات مرخصة وخدمات خلفية وعقودًا مناسبة.")

        addInfo("التتبع والموقع",
            "ليس وظيفة عامة. يستخدم فقط عند حاجة خدمة مشروعة إليه.")
    }

    private fun showPartnerOffice() {
        if (!simulationLoggedIn ||
            (currentRole != UserRole.PARTNER &&
             currentRole != UserRole.OWNER)) {
            Toast.makeText(this,
                "الصلاحية غير متاحة",
                Toast.LENGTH_SHORT).show()
            return
        }

        baseLayout("مكتب الشريك", "PARTNER OFFICE")

        addVisualBanner("🤝", "PARTNER",
            "إدارة العلاقة التجارية والشراكات", blue, navy)

        addStatus("الحالة", "واجهة محلية", green)

        addInfo("الوظائف",
            "هذه واجهة عامة. إدارة الشركاء الحقيقيين تحتاج خدمة خلفية وصلاحيات موثقة.")

        addCard("▰", "الوثائق",
            "مراجعة الوثائق المرتبطة بالشراكة.",
            { openPage { showDocuments() } })
    }

    private fun showOwnerOffice() {
        if (!simulationLoggedIn || currentRole != UserRole.OWNER) {
            Toast.makeText(this,
                "هذه الصفحة للمالك فقط",
                Toast.LENGTH_SHORT).show()
            return
        }

        baseLayout("مكتب المالك", "OWNER OFFICE")

        addVisualBanner("★", "OWNER OFFICE",
            "الإدارة العليا والاعتماد", gold, officeDark)

        addStatus("الصلاحية", "مالك تجريبي", gold)
        addInfo("المالك", "ياسر حسن وشركاؤه")

        addCard("✓", "الموافقات",
            "مراجعة الوظائف التي تحتاج اعتمادًا.",
            { openPage { showOwnerApproval() } })

        addCard("🤝", "إدارة الشركاء",
            "مراجعة بنية الشركاء.",
            { openPage { showPartnerManagement() } })

        addCard("▤", "الوثائق",
            "الوثائق والاعتمادات.",
            { openPage { showDocuments() } })

        addCard("◈", "ملخص النظام",
            "مراجعة الحماية والتكامل.",
            { openPage { showSystemSummary() } })
    }

    private fun showPartnerManagement() {
        if (!simulationLoggedIn || currentRole != UserRole.OWNER) {
            showHome()
            return
        }

        baseLayout("إدارة الشركاء", "PARTNER MANAGEMENT")

        addVisualBanner("🤝", "PARTNERS",
            "إدارة الشراكات", blue, navy)

        addInfo("الحالة",
            "هذه واجهة محلية. إضافة أو تعديل شريك حقيقي يحتاج نظام خلفية آمنًا.")

        addStatus("الوصول", "المالك", green)
    }

    private fun showOwnerApproval() {
        if (!simulationLoggedIn || currentRole != UserRole.OWNER) {
            showHome()
            return
        }

        baseLayout("اعتماد المالك", "OWNER APPROVAL")

        addVisualBanner("✓", "APPROVAL",
            "الاعتماد والمراجعة قبل التفعيل", gold, navy)

        addInfo("المبدأ",
            "الوظائف الحساسة لا تُشغّل تلقائيًا لمجرد وجود واجهة.")

        addStatus("حالة الاعتماد", "تحتاج مراجعة", gold)

        addCard("▣", "مراجعة النظام",
            "فتح ملخص النظام.",
            { openPage { showSystemSummary() } })
    }

    private fun showDocuments() {
        baseLayout("الوثائق", "DOCUMENTS")

        addVisualBanner("▤", "DOCUMENTS",
            "الوثائق والعقود والاعتمادات", officeDark, blue)

        addInfo("الوثائق القانونية",
            "الخدمات المنظمة قانونيًا تحتاج وثائق صحيحة ومصادر رسمية.")

        addInfo("العقود",
            "العمولات والشراكات والالتزامات المالية يجب أن تكون موثقة.")

        addInfo("التحقق",
            "التحقق الرسمي من الأهلية يحتاج تكاملًا مع الجهة المخولة.")
    }

    // =========================================================
    // البحث
    // =========================================================

    private fun showSearch() {
        baseLayout("البحث", "البحث داخل المنصة")

        val searchInput = EditText(this).apply {
            hint = "اكتب ما تريد البحث عنه"
            textSize = 16f
            setSingleLine(true)
            setPadding(14, 10, 14, 10)
            background = roundedBackground(white, 14f)
        }

        content.addView(searchInput, LinearLayout.LayoutParams(-1, 56).apply {
            setMargins(0, 0, 0, 10)
        })

        val searchButton = Button(this).apply {
            text = "بحث"
            isAllCaps = false
            setTextColor(white)
            background = roundedBackground(blue, 16f)

            setOnClickListener {
                searchTapCount++

                val query = searchInput.text.toString().trim()
                if (query.isBlank()) {
                    Toast.makeText(this@MainActivity,
                        "اكتب كلمة للبحث",
                        Toast.LENGTH_SHORT).show()
                } else {
                    addInfo("نتيجة البحث",
                        "تم استلام البحث عن: $query\n\nهذه النسخة لا تحتوي على محرك بحث خلفي.")
                }

                if (searchTapCount >= 20) {
                    searchTapCount = 0
                    Toast.makeText(this@MainActivity,
                        "تمت إعادة ضبط عداد ضغطات البحث.",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        content.addView(searchButton, LinearLayout.LayoutParams(-1, 54).apply {
            setMargins(0, 0, 0, 14)
        })

        addCard("🛒", "الأسواق",
            "البحث داخل المنتجات والفئات.",
            { openPage { showProducts() } })

        addCard("⚙", "الخدمات",
            "استعراض الخدمات.",
            { openPage { showServices() } })
    }

    private fun showFavorites() {
        baseLayout("المفضلة", "العناصر المحفوظة")

        addVisualBanner("♡", "FAVORITES",
            "العناصر التي يختارها المستخدم", blue, navy)

        addInfo("الحالة",
            "هذه واجهة للمفضلة. لا يُحفظ المحتوى بعد إغلاق التطبيق في هذه النسخة.")

        addCard("🛒", "الأسواق والمنتجات",
            "العودة إلى المنتجات.",
            { openPage { showProducts() } })
    }

    private fun showPoints() {
        baseLayout("النقاط", "نظام النقاط")

        addVisualBanner("★", "POINTS",
            "نظام النقاط داخل CENTRAL MARKET", gold, navy)

        addStatus("الرصيد الحالي", "0 نقطة", gold)

        addInfo("ملاحظة",
            "هذا رصيد واجهة محلية. نظام المكافآت الفعلي يحتاج قواعد خلفية وشروط استخدام.")
    }

    // =========================================================
    // الإعلانات والمطبخ والدعم
    // =========================================================

    private fun showAds() {
        baseLayout("الإعلانات", "مستويات الإعلان")

        addVisualBanner("📢", "ADVERTISE",
            "مساحات إعلانية منظمة", gold, blue)

        addCard("B", "BRONZE", "المستوى الأساسي.",
            { showAdMessage("BRONZE") })

        addCard("S", "SILVER", "المستوى المتوسط.",
            { showAdMessage("SILVER") })

        addCard("G", "GOLD", "المستوى المميز.",
            { showAdMessage("GOLD") })

        addInfo("الحماية",
            "أي شبكة إعلانية أو دفع حقيقي تحتاج تكاملًا منفصلًا وآمنًا.")
    }

    private fun showAdMessage(level: String) {
        Toast.makeText(this,
            "مستوى الإعلان: $level",
            Toast.LENGTH_SHORT).show()
    }

    private fun showKitchen() {
        baseLayout("المطبخ", "KITCHEN")

        addVisualBanner("🍲", "KITCHEN",
            "المطبخ والخدمات المرتبطة به",
            Color.rgb(130, 75, 35), gold)

        addCard("🍽", "المطاعم",
            "استعراض قسم المطاعم.",
            { openPage { showCategory("المطاعم والتوصيل") } })

        addCard("🛒", "المنتجات الغذائية",
            "استعراض الأسواق الغذائية.",
            { openPage { showCategory("الأسواق الغذائية") } })

        addInfo("التتبع",
            "لا يعمل تتبع الموقع تلقائيًا. إذا تطلبت خدمة توصيل معينة التتبع، يقتصر عليها وبما يراعي الموافقة والقانون.")
    }

    private fun showCharity() {
        baseLayout("الدعم والصدقة", "الأيتام والمحتاجون")

        addVisualBanner("🤝", "CHARITY",
            "الدعم المجتمعي والإنساني", green, navy)

        addInfo("الهدف",
            "واجهة لتنظيم مبادرات الدعم وفق القوانين والجهات المختصة.")

        addInfo("الشفافية",
            "أي أموال حقيقية تحتاج جهة مسؤولة وآليات تحقق وسجلات واضحة.")

        addStatus("الواجهة", "متاحة", green)
    }

    private fun showMessage() {
        baseLayout("المراسلة", "رسالة إلى المنصة")

        val messageInput = EditText(this).apply {
            hint = "اكتب رسالتك"
            textSize = 15f
            gravity = Gravity.TOP
            minLines = 5
            setPadding(14, 12, 14, 12)
            background = roundedBackground(white, 14f)
        }

        content.addView(messageInput, LinearLayout.LayoutParams(-1, 150).apply {
            setMargins(0, 0, 0, 10)
        })

        val save = Button(this).apply {
            text = "حفظ الرسالة محليًا"
            isAllCaps = false
            setTextColor(white)
            background = roundedBackground(blue, 16f)

            setOnClickListener {
                if (messageInput.text.toString().trim().isBlank()) {
                    Toast.makeText(this@MainActivity,
                        "اكتب الرسالة أولًا",
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity,
                        "تم استلام النص داخل الجلسة الحالية فقط",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        content.addView(save, LinearLayout.LayoutParams(-1, 54))
    }

    private fun showIdea() {
        baseLayout("فكرة أو اقتراح", "تطوير CENTRAL MARKET")

        addInfo("شارك فكرتك",
            "هذه الواجهة مخصصة للأفكار والاقتراحات التطويرية.")

        val ideaInput = EditText(this).apply {
            hint = "اكتب الفكرة"
            textSize = 15f
            gravity = Gravity.TOP
            minLines = 5
            setPadding(14, 12, 14, 12)
            background = roundedBackground(white, 14f)
        }

        content.addView(ideaInput, LinearLayout.LayoutParams(-1, 150).apply {
            setMargins(0, 0, 0, 10)
        })

        val save = Button(this).apply {
            text = "حفظ الاقتراح محليًا"
            isAllCaps = false
            setTextColor(white)
            background = roundedBackground(green, 16f)

            setOnClickListener {
                if (ideaInput.text.toString().trim().isBlank()) {
                    Toast.makeText(this@MainActivity,
                        "اكتب الفكرة أولًا",
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity,
                        "تم استلام النص داخل الجلسة الحالية فقط",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        content.addView(save, LinearLayout.LayoutParams(-1, 54))
    }

    // =========================================================
    // الاتصال والضيف والأمان
    // =========================================================

    private fun showConnectivity() {
        baseLayout("اختبار الاتصال", "حالة اتصال الجهاز")

        addVisualBanner("📡", "CONNECTIVITY",
            "فحص الاتصال دون تشغيل تتبع الموقع", blue, navy)

        val online = isOnline()
        addStatus("الاتصال", if (online) "متصل" else "غير متصل",
            if (online) green else red)

        addInfo("الخصوصية",
            "اختبار الاتصال لا يعني تشغيل GPS أو تتبع الموقع.")

        addInfo("التتبع",
            "الموقع ليس ميزة عامة. يستخدم فقط عندما تتطلبه خدمة محددة فعلًا.")

        addCard("↻", "إعادة الفحص",
            "فحص الاتصال مرة أخرى.",
            { openPage { showConnectivity() } })
    }

    private fun isOnline(): Boolean {
        return try {
            val manager = getSystemService(CONNECTIVITY_SERVICE)
                as ConnectivityManager
            val network = manager.activeNetwork ?: return false
            val capabilities = manager.getNetworkCapabilities(network)
                ?: return false

            capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            ) && capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED
            )
        } catch (_: Exception) {
            false
        }
    }

    private fun showOnline() {
        showConnectivity()
    }

    private fun showGuestMode() {
        clearSession()

        baseLayout("الوضع الضيف", "تصفح عام")

        addVisualBanner("👤", "GUEST MODE",
            "الوصول إلى المعلومات العامة", blue, navy)

        addStatus("نوع الوصول", "عام", blue)

        addInfo("الوضع الضيف",
            "يمكن للزائر تصفح الوظائف العامة دون الدخول إلى الوظائف الإدارية الحساسة.")

        addCard("🛒", "الأسواق", "تصفح الفئات.",
            { openPage { showProducts() } })

        addCard("⚙", "الخدمات", "تصفح الخدمات العامة.",
            { openPage { showServices() } })

        addCard("📢", "الإعلانات", "تصفح مستويات الإعلانات.",
            { openPage { showAds() } })

        addCard("🔐", "الأمان والخصوصية",
            "قراءة قواعد الحماية.",
            { openPage { showSecurity() } })

        addCard("⌂", "الرئيسية", "العودة للرئيسية.",
            { goHome() })
    }

    private fun showSecurity() {
        baseLayout("الأمان والخصوصية", "حماية المستخدم والمنصة")

        addVisualBanner("🔐", "SECURITY",
            "الحماية والخصوصية", navy, blue)

        addStatus("حماية الشاشة", "مفعلة", green)
        addStatus("القفل العام", "10 دقائق في الخلفية", gold)
        addStatus("القفل الحساس", "5 دقائق", gold)

        addInfo("لقطات الشاشة",
            "تم استخدام FLAG_SECURE لمنع لقطات الشاشة المعتادة وتسجيل الشاشة قدر الإمكان.")

        addInfo("البيانات",
            "يجب ألا تظهر البيانات الحساسة لمستخدم لا يملك الصلاحية.")

        addInfo("التتبع والموقع",
            "لا يعمل التتبع بشكل عام. أي استخدام يجب أن يكون مرتبطًا بحاجة مشروعة ومحددة.")

        addInfo("التحقق الرسمي",
            "الخدمات التي تحتاج تحققًا قانونيًا يجب أن تستخدم مصادر رسمية أو جهات مخولة.")

        addCard("📜", "القواعد",
            "عرض قواعد المنصة.",
            { openPage { showRules() } })
    }

    private fun showSafety() {
        showSecurity()
    }

    private fun showRules() {
        baseLayout("قواعد المنصة", "CENTRAL MARKET RULES")

        addVisualBanner("◈", "RULES",
            "القواعد الأساسية للتشغيل الآمن", navy, gold)

        addInfo("1 — الخصوصية",
            "لا يتم جمع أو تتبع بيانات المستخدم دون حاجة مشروعة وآلية مناسبة.")

        addInfo("2 — الموقع",
            "الموقع ليس ميزة عامة؛ يستخدم فقط عندما تحتاجه خدمة محددة.")

        addInfo("3 — الخدمات الحساسة",
            "الوظائف المالية والاستثمارية تحتاج حماية وتحققًا قانونيًا مناسبًا.")

        addInfo("4 — الاعتماد",
            "وجود واجهة لا يعني تفعيل التكاملات الحكومية والمالية.")

        addInfo("5 — المالك",
            "الوظائف التي تحتاج اعتمادًا لا تُفعّل تلقائيًا.")

        addInfo("6 — النسخ الدولية",
            "الوظائف الحكومية الخاصة بالدولة تدار ضمن حزمة الدولة المناسبة.")
    }

        // =========================================================
    // أدوات الرسم والخلفيات
    // =========================================================

    private fun roundedBackground(
        color: Int,
        radius: Float
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
        }
    }

    private fun cardBackground(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(white)
            cornerRadius = 18f
            setStroke(1, Color.rgb(225, 230, 235))
        }
    }

    private fun gradientBackground(
        firstColor: Int,
        secondColor: Int,
        radius: Float
    ): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(firstColor, secondColor)
        ).apply {
            cornerRadius = radius
        }
    }

    // =========================================================
    // نهاية MainActivity
    // =========================================================
}
