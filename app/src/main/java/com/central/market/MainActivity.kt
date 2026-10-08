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

    // =========================================================
    // CENTRAL MARKET — الهوية البصرية
    // =========================================================

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

    // =========================================================
    // الأدوار
    // =========================================================

    private enum class UserRole {
        USER,
        ADMIN,
        PARTNER,
        OWNER
    }

    private var currentRole = UserRole.USER
    private var currentAccountName = "زائر"
    private var simulationLoggedIn = false

    // =========================================================
    // الجلسات والحماية
    // =========================================================

    private var lastBackgroundTime = 0L

    private val globalTimeout = 10 * 60 * 1000L
    private val sensitiveTimeout = 3 * 60 * 1000L

    private var sensitiveLocked = false

    private val globalLockRunnable = Runnable {
        if (simulationLoggedIn) {
            lockAccount()
        }
    }

    private val sensitiveLockRunnable = Runnable {
        sensitiveLocked = true
        showSensitiveLock()
    }

    // =========================================================
    // نظام الرجوع والتنقل
    // =========================================================

    private val pageHistory = ArrayList<() -> Unit>()
    private var currentPage: (() -> Unit)? = null
    private var ignoreHistoryOnce = false

    // =========================================================
    // Activity
    // =========================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // منع لقطات الشاشة والتسجيل من داخل التطبيق.
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
            showHome()
        }
    }

    override fun onPause() {
        super.onPause()

        if (simulationLoggedIn) {
            lastBackgroundTime = SystemClock.elapsedRealtime()

            handler.removeCallbacks(globalLockRunnable)
            handler.postDelayed(
                globalLockRunnable,
                globalTimeout
            )
        }
    }

    override fun onResume() {
        super.onResume()

        handler.removeCallbacks(globalLockRunnable)

        if (
            simulationLoggedIn &&
            lastBackgroundTime > 0L
        ) {
            val elapsed =
                SystemClock.elapsedRealtime() - lastBackgroundTime

            if (elapsed >= globalTimeout) {
                lockAccount()
            }
        }

        lastBackgroundTime = 0L
    }

    override fun onDestroy() {
        handler.removeCallbacks(globalLockRunnable)
        handler.removeCallbacks(sensitiveLockRunnable)
        super.onDestroy()
    }

    // =========================================================
    // محرك الصفحات
    // =========================================================

    private fun openPage(page: () -> Unit) {

        if (!ignoreHistoryOnce) {
            currentPage?.let {
                pageHistory.add(it)
            }
        }

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
            val previous =
                pageHistory.removeAt(pageHistory.lastIndex)

            ignoreHistoryOnce = true
            previous.invoke()
            ignoreHistoryOnce = false
        } else {
            goHome()
        }
    }

    // =========================================================
    // التخطيط الأساسي
    // =========================================================

    private fun baseLayout(
        title: String,
        subtitle: String = ""
    ) {
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 22)
            setBackgroundColor(light)
        }

        val scroll = ScrollView(this).apply {
            setBackgroundColor(light)
            addView(content)
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

            content.addView(
                sub,
                LinearLayout.LayoutParams(
                    -1,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }
    }

    // =========================================================
    // الشريط العلوي — الرجوع / الرئيسية / الخروج
    // =========================================================

    private fun addTopNavigation(title: String) {

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(6, 6, 6, 6)
            background = roundedBackground(navy, 20f)
        }

        val back = Button(this).apply {
            text = "‹"
            textSize = 30f
            isAllCaps = false
            setTextColor(white)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 4)
            background = roundedBackground(blue, 16f)

            setOnClickListener {
                exitPage()
            }
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
            gravity = Gravity.CENTER
        }

        val home = Button(this).apply {
            text = "⌂"
            textSize = 25f
            isAllCaps = false
            setTextColor(white)
            gravity = Gravity.CENTER
            background = roundedBackground(blue, 16f)

            setOnClickListener {
                goHome()
            }
        }

        val exit = Button(this).apply {
            text = "×"
            textSize = 27f
            isAllCaps = false
            setTextColor(white)
            gravity = Gravity.CENTER
            background = roundedBackground(red, 16f)

            setOnClickListener {
                finish()
            }
        }

        bar.addView(
            back,
            LinearLayout.LayoutParams(50, 50)
        )

        bar.addView(
            titleView,
            LinearLayout.LayoutParams(
                0,
                50,
                1f
            )
        )

        bar.addView(
            home,
            LinearLayout.LayoutParams(50, 50)
        )

        bar.addView(
            exit,
            LinearLayout.LayoutParams(50, 50)
        )

        content.addView(
            bar,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

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

    // =========================================================
    // البطاقات والعناوين
    // =========================================================

    private fun addSection(title: String) {
        val view = TextView(this).apply {
            text = title
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(navy)
            setPadding(4, 14, 4, 8)
        }

        content.addView(view)
    }

    private fun addInfo(
        title: String,
        body: String
    ) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 14, 16, 14)
            background = cardBackground()
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(navy)
        }

        val bodyView = TextView(this).apply {
            text = body
            textSize = 14f
            setTextColor(darkText)
            setPadding(0, 6, 0, 0)
        }

        card.addView(titleView)
        card.addView(bodyView)

        content.addView(
            card,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 10)
            }
        )
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

            setOnClickListener {
                action?.invoke()
            }
        }

        val icon = TextView(this).apply {
            text = symbol
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(gold)
        }

        val textBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 0, 4, 0)
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(navy)
        }

        val descriptionView = TextView(this).apply {
            text = description
            textSize = 13f
            setTextColor(gray)
            setPadding(0, 4, 0, 0)
        }

        textBox.addView(titleView)
        textBox.addView(descriptionView)

        card.addView(
            icon,
            LinearLayout.LayoutParams(50, 64)
        )

        card.addView(
            textBox,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        content.addView(
            card,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 10)
            }
        )
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

        val label = TextView(this).apply {
            text = title
            textSize = 14f
            setTextColor(darkText)
        }

        val valueView = TextView(this).apply {
            text = value
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(statusColor)
            gravity = Gravity.END
        }

        row.addView(
            label,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        row.addView(
            valueView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        content.addView(
            row,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 8)
            }
        )
    }

    // =========================================================
    // البطل البصري للمكاتب و BADGER
    // =========================================================

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
            background = gradientBackground(
                firstColor,
                secondColor,
                24f
            )
        }

        val iconView = TextView(this).apply {
            text = icon
            textSize = 44f
            gravity = Gravity.CENTER
            setTextColor(white)
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 23f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(white)
            setPadding(0, 6, 0, 4)
        }

        val descView = TextView(this).apply {
            text = description
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(235, 242, 248))
        }

        banner.addView(iconView)
        banner.addView(titleView)
        banner.addView(descView)

        content.addView(
            banner,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 14)
            }
        )
    }

        // =========================================================
    // الصفحة الرئيسية
    // =========================================================

    private fun showHome() {
        pageHistory.clear()
        currentPage = null

        baseLayout(
            "CENTRAL MARKET",
            "منصة واحدة .. عالم من الفرص."
        )

        addVisualBanner(
            "◈",
            "CENTRAL MARKET",
            "المالك ياسر حسن وشركاؤه",
            navy,
            blue
        )

        addStatus(
            "حالة الاتصال",
            if (isOnline()) "متصل" else "غير متصل",
            if (isOnline()) green else red
        )

        addSection("المنصة")

        addCard(
            "🛒",
            "الأسواق والمنتجات",
            "منتجات وفئات متعددة داخل المنصة.",
            { openPage { showProducts() } }
        )

        addCard(
            "⚙",
            "الخدمات",
            "الخدمات العامة والذكاء البشري وCTM AI.",
            { openPage { showServices() } }
        )

        addCard(
            "📢",
            "الإعلانات",
            "مساحات إعلانية بمستويات BRONZE وSILVER وGOLD.",
            { openPage { showAds() } }
        )

        addCard(
            "🍲",
            "المطبخ",
            "قسم المطبخ والخدمات المرتبطة به.",
            { openPage { showKitchen() } }
        )

        addCard(
            "🤝",
            "الصدقة والدعم",
            "قسم دعم الأيتام والمحتاجين.",
            { openPage { showCharity() } }
        )

        addSection("الوصول والحماية")

        addCard(
            "📡",
            "اختبار الاتصال",
            "فحص حالة الاتصال بالجهاز دون تشغيل تتبع الموقع.",
            { openPage { showConnectivity() } }
        )

        addCard(
            "👤",
            "الوضع الضيف",
            "تصفح المعلومات العامة دون الدخول إلى الحسابات الحساسة.",
            { openPage { showGuestMode() } }
        )

        addCard(
            "🔐",
            "الأمان والخصوصية",
            "قواعد حماية الحساب والبيانات والوظائف الحساسة.",
            { openPage { showSecurity() } }
        )

        addCard(
            "💡",
            "فكرة أو اقتراح",
            "مساحة لإرسال فكرة تطويرية للمنصة.",
            { openPage { showIdea() } }
        )

        addSection("الحساب")

        addCard(
            "◉",
            "الحساب والدخول",
            "المستخدم والإدارة والشريك والمالك.",
            { openPage { showSimulationLogin() } }
        )

        if (
            currentRole == UserRole.ADMIN ||
            currentRole == UserRole.PARTNER ||
            currentRole == UserRole.OWNER
        ) {
            addCard(
                "▣",
                "المكتب الإداري",
                "الوصول إلى الأدوات حسب صلاحية الحساب.",
                { openPage { showManagerOffice() } }
            )
        }

        addInfo(
            "قاعدة الخصوصية",
            "لا يتم تشغيل تتبع الموقع كميزة عامة. عند وجود خدمة تتطلب التتبع فعليًا، يجب أن يكون التفعيل مرتبطًا بالحاجة المشروعة والموافقة والمتطلبات القانونية."
        )
    }

    // =========================================================
    // تسجيل الدخول بالمحاكاة
    // =========================================================

    private fun showSimulationLogin() {
        baseLayout(
            "الحسابات",
            "اختيار نوع الحساب"
        )

        addInfo(
            "نظام الحساب",
            "هذه الشاشة تستخدم بيانات محاكاة داخل التطبيق. لا توجد هنا عملية مالية حقيقية ولا تنفيذ مالي حقيقي."
        )

        addCard(
            "👤",
            "زائر / مستخدم عام",
            "الدخول إلى الوظائف العامة.",
            {
                loginSimulation(
                    UserRole.USER,
                    "زائر"
                )
            }
        )

        addCard(
            "▣",
            "حساب الإدارة",
            "وظائف الإدارة والتشخيص وفق الصلاحية.",
            {
                showPasswordGate(
                    UserRole.ADMIN,
                    "حساب الإدارة"
                )
            }
        )

        addCard(
            "🤝",
            "حساب الشريك",
            "وظائف الشريك والمكتب المرتبط به.",
            {
                showPasswordGate(
                    UserRole.PARTNER,
                    "حساب الشريك"
                )
            }
        )

        addCard(
            "★",
            "حساب المالك",
            "وظائف المالك والموافقات الحساسة.",
            {
                showPasswordGate(
                    UserRole.OWNER,
                    "حساب المالك"
                )
            }
        )
    }

    // =========================================================
    // بوابة كلمة المرور
    // =========================================================

    private fun showPasswordGate(
        role: UserRole,
        accountName: String
    ) {
        baseLayout(
            "دخول الحساب",
            accountName
        )

        addInfo(
            "تنبيه",
            "هذه بيانات محاكاة داخل التطبيق وليست بيانات اعتماد لخدمة خارجية."
        )

        val input = EditText(this).apply {
            hint = "أدخل رمز المحاكاة"
            textSize = 16f
            inputType =
                InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_PASSWORD

            setPadding(14, 12, 14, 12)
            background = roundedBackground(
                white,
                14f
            )
        }

        content.addView(
            input,
            LinearLayout.LayoutParams(
                -1,
                56
            ).apply {
                setMargins(0, 0, 0, 12)
            }
        )

        val loginButton = Button(this).apply {
            text = "دخول"
            isAllCaps = false
            textSize = 16f
            setTextColor(white)
            background = roundedBackground(
                blue,
                16f
            )

            setOnClickListener {
                loginSimulation(
                    role,
                    accountName,
                    input.text.toString()
                )
            }
        }

        content.addView(
            loginButton,
            LinearLayout.LayoutParams(
                -1,
                54
            ).apply {
                setMargins(0, 0, 0, 10)
            }
        )

        addInfo(
            "الصلاحيات",
            when (role) {
                UserRole.USER ->
                    "الوصول إلى الوظائف العامة."

                UserRole.ADMIN ->
                    "وظائف الإدارة والتشخيص والمراجعة."

                UserRole.PARTNER ->
                    "وظائف الشريك والمكتب المرتبط به."

                UserRole.OWNER ->
                    "وظائف المالك والموافقات والإعدادات الحساسة."
            }
        )
    }

    // =========================================================
    // تسجيل الدخول
    // =========================================================

    private fun loginSimulation(
        role: UserRole,
        accountName: String,
        password: String = ""
    ) {
        val valid = when (role) {
            UserRole.USER -> true
            UserRole.ADMIN -> password == "ADMIN-DEMO"
            UserRole.PARTNER -> password == "PARTNER-DEMO"
            UserRole.OWNER -> password == "OWNER-DEMO"
        }

        if (!valid) {
            Toast.makeText(
                this,
                "رمز المحاكاة غير صحيح",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        currentRole = role
        currentAccountName = accountName
        simulationLoggedIn = true
        sensitiveLocked = false

        lastBackgroundTime = 0L
        registerActivity()

        showAccountHome()
    }

    // =========================================================
    // الصفحة الرئيسية للحساب
    // =========================================================

    private fun showAccountHome() {
        baseLayout(
            "حسابي",
            currentAccountName
        )

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

        addStatus(
            "حالة الجلسة",
            "نشطة",
            green
        )

        addCard(
            "◉",
            "بيانات الحساب",
            "عرض حالة الحساب والصلاحيات.",
            {
                openPage {
                    showAccount()
                }
            }
        )

        addCard(
            "★",
            "النقاط",
            "عرض نظام النقاط.",
            {
                openPage {
                    showPoints()
                }
            }
        )

        addCard(
            "♡",
            "المفضلة",
            "عرض عناصر المفضلة.",
            {
                openPage {
                    showFavorites()
                }
            }
        )

        addCard(
            "⌕",
            "البحث",
            "البحث داخل أقسام المنصة.",
            {
                openPage {
                    showSearch()
                }
            }
        )

        addCard(
            "↪",
            "تسجيل الخروج",
            "إنهاء جلسة الحساب الحالية.",
            {
                logoutSimulation()
            }
        )

        if (
            currentRole == UserRole.ADMIN ||
            currentRole == UserRole.PARTNER ||
            currentRole == UserRole.OWNER
        ) {
            addCard(
                "▣",
                "المكتب الإداري",
                "فتح المكتب وفق الصلاحية.",
                {
                    openPage {
                        showManagerOffice()
                    }
                }
            )
        }
    }

    // =========================================================
    // الحساب العام
    // =========================================================

    private fun showAccount() {
        when (currentRole) {
            UserRole.USER -> showUserAccount()
            UserRole.ADMIN -> showAdminAccount()
            UserRole.PARTNER -> showPartnerAccount()
            UserRole.OWNER -> showOwnerAccount()
        }
    }

    private fun showUserAccount() {
        baseLayout(
            "حساب المستخدم",
            currentAccountName
        )

        addStatus(
            "الدور",
            "مستخدم عام",
            blue
        )

        addInfo(
            "الوصول",
            "يمكن للحساب استخدام الوظائف العامة المتاحة داخل المنصة."
        )

        addInfo(
            "الحماية",
            "الوظائف الحساسة لا تُفتح تلقائيًا لهذا الحساب."
        )
    }

    private fun showAdminAccount() {
        baseLayout(
            "حساب الإدارة",
            currentAccountName
        )

        addVisualBanner(
            "▣",
            "ADMIN OFFICE",
            "واجهة الإدارة والمراجعة",
            officeDark,
            navy
        )

        addStatus(
            "الدور",
            "إدارة",
            blue
        )

        addStatus(
            "الجلسة",
            "محمية",
            green
        )

        addInfo(
            "الصلاحيات",
            "الوصول إلى أدوات الإدارة والتشخيص والمراجعة وفق حدود الصلاحية."
        )
    }

    private fun showPartnerAccount() {
        baseLayout(
            "حساب الشريك",
            currentAccountName
        )

        addVisualBanner(
            "🤝",
            "PARTNER OFFICE",
            "واجهة الشريك والأعمال المرتبطة به",
            blue,
            navy
        )

        addStatus(
            "الدور",
            "شريك",
            blue
        )

        addInfo(
            "الصلاحيات",
            "الوصول إلى الوظائف الخاصة بالشريك وفق الصلاحيات الممنوحة."
        )
    }

    private fun showOwnerAccount() {
        baseLayout(
            "حساب المالك",
            currentAccountName
        )

        addVisualBanner(
            "★",
            "OWNER OFFICE",
            "إدارة واعتماد ومراجعة المنصة",
            gold,
            navy
        )

        addStatus(
            "الدور",
            "مالك",
            gold
        )

        addInfo(
            "المالك",
            "ياسر حسن وشركاؤه"
        )

        addInfo(
            "قاعدة الاعتماد",
            "العمليات الحساسة والقرارات المالية الحقيقية لا تُنفذ من هذه المحاكاة."
        )
    }

    // =========================================================
    // المنتجات والأسواق
    // =========================================================

    private fun showProducts() {
        baseLayout(
            "الأسواق والمنتجات",
            "اختيار الفئة"
        )

        addInfo(
            "الأسواق",
            "منصة متعددة الفئات، مع إمكانية تخصيص الحزم حسب الدولة والمنطقة."
        )

        addCard(
            "🚗",
            "المركبات والشاحنات",
            "مركبات وشاحنات وخدمات مرتبطة بها.",
            { openPage { showCategory("المركبات والشاحنات") } }
        )

        addCard(
            "📱",
            "الهواتف والإلكترونيات",
            "هواتف وأجهزة وإلكترونيات.",
            { openPage { showCategory("الهواتف والإلكترونيات") } }
        )

        addCard(
            "🍽",
            "المطاعم والتوصيل",
            "مطاعم وخدمات توصيل.",
            { openPage { showCategory("المطاعم والتوصيل") } }
        )

        addCard(
            "🌾",
            "الزراعة والثروة الحيوانية",
            "منتجات وخدمات زراعية وحيوانية.",
            { openPage { showCategory("الزراعة والثروة الحيوانية") } }
        )

        addCard(
            "🐟",
            "الأسماك",
            "منتجات وخدمات مرتبطة بالأسماك.",
            { openPage { showCategory("الأسماك") } }
        )

        addCard(
            "🏗",
            "البناء والجملة",
            "مواد بناء وتجارة الجملة.",
            { openPage { showCategory("البناء والجملة") } }
        )

        addCard(
            "🏥",
            "الصحة",
            "خدمات ومعلومات صحية عامة.",
            { openPage { showCategory("الصحة") } }
        )

        addCard(
            "⚽",
            "الرياضة",
            "منتجات وخدمات رياضية.",
            { openPage { showCategory("الرياضة") } }
        )

        addCard(
            "💡",
            "الكهرباء والمياه",
            "خدمات واحتياجات الكهرباء والمياه.",
            { openPage { showCategory("الكهرباء والمياه") } }
        )

        addCard(
            "🎓",
            "التعليم",
            "خدمات ومعلومات تعليمية.",
            { openPage { showCategory("التعليم") } }
        )

        addCard(
            "✈",
            "السفر",
            "خدمات السفر والتذاكر والمعلومات المرتبطة بها.",
            { openPage { showCategory("السفر") } }
        )
    }

    private fun showCategory(category: String) {
        baseLayout(
            category,
            "قسم CENTRAL MARKET"
        )

        addInfo(
            "الفئة",
            category
        )

        addStatus(
            "الحالة",
            "متاح للتصفح",
            green
        )

        addInfo(
            "التعاملات",
            "أي تعامل حقيقي يحتاج إلى خدمات خلفية واعتمادات مناسبة. هذه النسخة الحالية تعرض الهيكل والواجهات فقط."
        )

        if (
            category == "المركبات والشاحنات" ||
            category == "السفر"
        ) {
            addInfo(
                "الموقع والتتبع",
                "لا يتم تشغيل تتبع الموقع كميزة عامة. عند الحاجة الفعلية لخدمة نقل أو خدمة تتطلب التتبع، يجب أن يكون التفعيل محددًا لهذه الخدمة فقط وبموافقة ومتطلبات قانونية مناسبة."
            )
        }

        addCard(
            "♡",
            "إضافة للمفضلة",
            "حفظ الفئة ضمن المفضلة.",
            {
                Toast.makeText(
                    this,
                    "تمت إضافة الفئة إلى المفضلة",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        addCard(
            "⌕",
            "البحث داخل الفئة",
            "فتح البحث.",
            {
                openPage {
                    showSearch()
                }
            }
        )
    }

    // =========================================================
    // أدوات مساعدة
    // =========================================================

    private fun roleName(role: UserRole): String {
        return when (role) {
            UserRole.USER -> "مستخدم"
            UserRole.ADMIN -> "إدارة"
            UserRole.PARTNER -> "شريك"
            UserRole.OWNER -> "مالك"
        }
    }

    private fun registerActivity() {
        lastActivityTime = SystemClock.elapsedRealtime()
    }

    private var lastActivityTime = 0L

    override fun onUserInteraction() {
        super.onUserInteraction()
        registerActivity()
    }

        // =========================================================
    // الخدمات
    // =========================================================

    private fun showServices() {
        baseLayout(
            "الخدمات",
            "خدمات CENTRAL MARKET"
        )

        addVisualBanner(
            "⚙",
            "SERVICES",
            "الخدمات والذكاء والخدمات المجتمعية",
            blue,
            navy
        )

        addCard(
            "🧠",
            "الذكاء البشري",
            "منظومة مساعدة بشرية وخدمات معرفية.",
            { openPage { showHumanIntelligence() } }
        )

        addCard(
            "◉",
            "CTM AI",
            "المساعد الرسمي للمنصة ضمن الصلاحيات المعتمدة.",
            { openPage { showCtmAi() } }
        )

        addCard(
            "🏥",
            "الصحة",
            "معلومات وخدمات عامة مرتبطة بالصحة.",
            { openPage { showCategory("الصحة") } }
        )

        addCard(
            "🎓",
            "التعليم",
            "خدمات ومعلومات تعليمية.",
            { openPage { showCategory("التعليم") } }
        )

        addCard(
            "⚡",
            "الكهرباء والمياه",
            "خدمات واحتياجات أساسية.",
            { openPage { showCategory("الكهرباء والمياه") } }
        )

        addCard(
            "✈",
            "السفر",
            "خدمات السفر والتذاكر والمعلومات.",
            { openPage { showCategory("السفر") } }
        )

        addCard(
            "🤝",
            "الدعم المجتمعي",
            "المساهمة في دعم الأيتام والمحتاجين.",
            { openPage { showCharity() } }
        )
    }

    // =========================================================
    // الذكاء البشري
    // =========================================================

    private fun showHumanIntelligence() {
        baseLayout(
            "الذكاء البشري",
            "خدمات المعرفة والمساعدة"
        )

        addVisualBanner(
            "🧠",
            "HUMAN INTELLIGENCE",
            "المعرفة البشرية والمساعدة المنظمة",
            navy,
            blue
        )

        addInfo(
            "الفكرة",
            "قسم مخصص للمساعدة البشرية والمعرفة والخدمات المنظمة داخل المنصة."
        )

        addInfo(
            "الزكاة البشرية",
            "يمكن تخصيص حزمة السودان للوظائف المجتمعية والإنسانية وفق القوانين والجهات المختصة."
        )

        addInfo(
            "الحزم الدولية",
            "الوظائف الحكومية أو المحلية الخاصة بدولة معينة لا تُفرض على النسخ الخارجية، بل تُدار ضمن حزمة الدولة المناسبة."
        )

        addStatus(
            "حالة القسم",
            "جاهز للواجهة",
            green
        )
    }

    // =========================================================
    // CTM AI
    // =========================================================

    private fun showCtmAi() {
        baseLayout(
            "CTM AI",
            "المساعد الرسمي للمنصة"
        )

        addVisualBanner(
            "◉",
            "CTM AI",
            "CENTRAL MARKET Intelligence",
            blue,
            navy
        )

        addInfo(
            "وظيفة المساعد",
            "يساعد المستخدم في البحث والقراءة والوصول إلى المعلومات المسموح بها داخل التطبيق."
        )

        addInfo(
            "الخصوصية",
            "لا يُفترض أن يفتح المساعد بيانات أو أقسامًا لا يملك المستخدم صلاحية الوصول إليها."
        )

        addInfo(
            "الموافقة",
            "التغييرات الحساسة أو تشغيل الوظائف المتقدمة يخضعان لاعتماد المالك عندما تكون هذه الصلاحية مطلوبة."
        )

        addInfo(
            "الصوت",
            "يمكن دعم البحث أو قراءة المعلومات المسموح بها صوتيًا عند توفر التكامل المناسب."
        )

        addStatus(
            "الحالة الحالية",
            "واجهة محلية",
            green
        )
    }

    // =========================================================
    // BADGER
    // =========================================================

    private fun showBadger() {
        baseLayout(
            "BADGER",
            "المنظومة المالية المقترحة"
        )

        addVisualBanner(
            "🦡",
            "BADGER",
            "Banking • Analytics • Deposits • Global",
            badgerBlack,
            navy
        )

        addStatus(
            "حالة الحماية",
            "مفعلة",
            green
        )

        addCard(
            "▣",
            "لوحة BADGER",
            "الحساب والتحليلات والوظائف المتاحة.",
            {
                openPage {
                    showBadgerDashboard()
                }
            }
        )

        addCard(
            "◆",
            "المشاركة الاستثمارية",
            "وظيفة حساسة تتطلب حماية وأهلية واعتمادات مناسبة.",
            {
                openPage {
                    showSensitiveInvestment()
                }
            }
        )

        addCard(
            "▰",
            "الحماية الحساسة",
            "قواعد حماية الوظائف المالية الحساسة.",
            {
                openPage {
                    showSensitiveLock()
                }
            }
        )

        addInfo(
            "مبدأ BADGER",
            "BADGER مصمم كمنتج منفصل يمكن تطويره وطرحه للمؤسسات المالية وفق العقود والاعتمادات المناسبة."
        )

        addInfo(
            "التنفيذ المالي",
            "هذه النسخة لا تنفذ تحويلات أو إيداعات أو استثمارات مالية حقيقية."
        )
    }

    // =========================================================
    // لوحة BADGER
    // =========================================================

    private fun showBadgerDashboard() {
        baseLayout(
            "BADGER Dashboard",
            "لوحة المنظومة"
        )

        addVisualBanner(
            "🦡",
            "BADGER",
            "Financial Technology Interface",
            badgerBlack,
            gold
        )

        addStatus(
            "الجلسة",
            if (simulationLoggedIn) "نشطة" else "زائر",
            if (simulationLoggedIn) green else gray
        )

        addStatus(
            "القفل التلقائي",
            "3 دقائق للوظائف الحساسة",
            gold
        )

        addCard(
            "◈",
            "تحليلات الإيداع",
            "عرض هيكل تحليلي فقط دون تنفيذ مالي.",
            {
                Toast.makeText(
                    this,
                    "التحليلات الحالية معلومات محاكاة فقط",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        addCard(
            "◆",
            "تحليلات الاستثمار",
            "عرض هيكل المشاركة الاستثمارية الحساسة.",
            {
                openPage {
                    showSensitiveInvestment()
                }
            }
        )

        addCard(
            "▰",
            "السجلات والعقود",
            "العمولات والعلاقات التجارية يجب أن تكون موثقة تعاقديًا.",
            {
                openPage {
                    showDocuments()
                }
            }
        )

        addInfo(
            "الهوية البصرية",
            "تستخدم واجهة BADGER خلفية داكنة مع رموز هندسية وهوية النحلة/الغرير والعالم، مع إبقاء المعلومات واضحة ومقروءة."
        )
    }

    // =========================================================
    // الوظائف الحساسة
    // =========================================================

    private fun showSensitiveInvestment() {

        if (!simulationLoggedIn) {
            Toast.makeText(
                this,
                "يجب الدخول إلى الحساب أولًا",
                Toast.LENGTH_SHORT
            ).show()

            showSimulationLogin()
            return
        }

        sensitiveLocked = false

        handler.removeCallbacks(sensitiveLockRunnable)

        handler.postDelayed(
            sensitiveLockRunnable,
            sensitiveTimeout
        )

        baseLayout(
            "المشاركة الاستثمارية",
            "وظيفة حساسة"
        )

        addVisualBanner(
            "◆",
            "SENSITIVE",
            "حماية الوظائف المالية الحساسة",
            red,
            navy
        )

        addStatus(
            "حماية الشاشة",
            "مفعلة",
            green
        )

        addStatus(
            "القفل التلقائي",
            "3 دقائق",
            gold
        )

        addInfo(
            "الأهلية",
            "أي مشاركة مالية حقيقية يجب أن تعتمد على تحقق قانوني ورسمي مناسب، ومصادر حكومية أو جهات مخولة، والتفويضات المطلوبة."
        )

        addInfo(
            "الموانع القانونية",
            "لا يتم تجاوز أي مانع قانوني موثق. التحقق الحقيقي يجب أن يتم عبر تكامل رسمي معتمد عند بناء الخدمة الخلفية."
        )

        addInfo(
            "حماية الشاشة",
            "تم تفعيل FLAG_SECURE على التطبيق لمنع لقطات الشاشة وتسجيل الشاشة قدر الإمكان داخل النظام."
        )

        addInfo(
            "التنفيذ",
            "لا توجد عملية استثمار أو تحويل أموال حقيقية في هذه النسخة."
        )

        addCard(
            "🔒",
            "إغلاق الوظيفة الحساسة",
            "إغلاق الشاشة الحساسة فورًا.",
            {
                lockSensitiveSection()
            }
        )
    }

    private fun showSensitiveLock() {
        handler.removeCallbacks(sensitiveLockRunnable)

        baseLayout(
            "القفل الحساس",
            "تم تأمين الوظيفة"
        )

        addVisualBanner(
            "🔒",
            "LOCKED",
            "تم إغلاق القسم الحساس تلقائيًا",
            red,
            officeDark
        )

        addInfo(
            "سبب القفل",
            "انتهت مدة الأمان المحددة للوظائف الحساسة."
        )

        addStatus(
            "الحالة",
            "مقفلة",
            red
        )

        addCard(
            "↻",
            "إعادة الدخول",
            "العودة إلى الحساب وإعادة فتح القسم عند توفر الصلاحية.",
            {
                sensitiveLocked = false
                showAccountHome()
            }
        )
    }

    private fun lockSensitiveSection() {
        sensitiveLocked = true
        handler.removeCallbacks(sensitiveLockRunnable)
        showSensitiveLock()
    }

    // =========================================================
    // أدوات الإدارة
    // =========================================================

    private fun showManagerOffice() {

        if (
            currentRole != UserRole.ADMIN &&
            currentRole != UserRole.PARTNER &&
            currentRole != UserRole.OWNER
        ) {
            Toast.makeText(
                this,
                "هذه المنطقة مخصصة للحسابات المصرح لها",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        baseLayout(
            "المكتب الإداري",
            "الوصول حسب الصلاحية"
        )

        addVisualBanner(
            "▣",
            "MANAGEMENT OFFICE",
            "مكتب الإدارة والمراجعة",
            officeDark,
            navy
        )

        addStatus(
            "الحساب",
            currentAccountName,
            blue
        )

        addStatus(
            "الصلاحية",
            roleName(currentRole),
            green
        )

        addCard(
            "▣",
            "أدوات الإدارة",
            "الأدوات العامة للمراجعة والإدارة.",
            {
                openPage {
                    showManagerTools()
                }
            }
        )

        addCard(
            "◈",
            "التشخيص",
            "حالة الاتصال وبعض مكونات التطبيق.",
            {
                openPage {
                    showDiagnostics()
                }
            }
        )

        if (
            currentRole == UserRole.PARTNER ||
            currentRole == UserRole.OWNER
        ) {
            addCard(
                "🤝",
                "مكتب الشريك",
                "إدارة وظائف الشريك.",
                {
                    openPage {
                        showPartnerOffice()
                    }
                }
            )
        }

        if (currentRole == UserRole.OWNER) {
            addCard(
                "★",
                "مكتب المالك",
                "الموافقات والإدارة العليا.",
                {
                    openPage {
                        showOwnerOffice()
                    }
                }
            )
        }
    }

    private fun showManagerTools() {
        baseLayout(
            "أدوات الإدارة",
            "Management Tools"
        )

        addVisualBanner(
            "▣",
            "ADMIN TOOLS",
            "المراجعة والتشخيص",
            officeDark,
            blue
        )

        addCard(
            "◈",
            "تشخيص النظام",
            "فحص الحالة المحلية للتطبيق.",
            {
                openPage {
                    showDiagnostics()
                }
            }
        )

        addCard(
            "▤",
            "ملخص النظام",
            "ملخص المكونات والحماية.",
            {
                openPage {
                    showSystemSummary()
                }
            }
        )

        addCard(
            "▰",
            "الوثائق",
            "سجل الوثائق والاعتمادات المطلوبة.",
            {
                openPage {
                    showDocuments()
                }
            }
        )

        if (currentRole == UserRole.OWNER) {
            addCard(
                "★",
                "إدارة الشركاء",
                "إدارة بنية الشركاء.",
                {
                    openPage {
                        showPartnerManagement()
                    }
                }
            )

            addCard(
                "✓",
                "اعتماد المالك",
                "مراجعة الوظائف التي تحتاج اعتمادًا.",
                {
                    openPage {
                        showOwnerApproval()
                    }
                }
            )
        }
    }

    private fun showDiagnostics() {
        baseLayout(
            "التشخيص",
            "حالة المكونات المحلية"
        )

        val online = isOnline()

        addStatus(
            "الاتصال",
            if (online) "متصل" else "غير متصل",
            if (online) green else red
        )

        addStatus(
            "حماية الشاشة",
            "مفعلة",
            green
        )

        addStatus(
            "جلسة المستخدم",
            if (simulationLoggedIn) "نشطة" else "غير مسجلة",
            if (simulationLoggedIn) green else gray
        )

        addStatus(
            "BADGER",
            "واجهة محلية",
            blue
        )

        addStatus(
            "CTM AI",
            "واجهة محلية",
            blue
        )

        addInfo(
            "ملاحظة",
            "التكاملات الحكومية والمالية الحقيقية تحتاج خدمات خلفية رسمية وتصاريح مناسبة قبل تشغيلها في الإنتاج."
        )
    }

    private fun showPartnerOffice() {
        if (
            currentRole != UserRole.PARTNER &&
            currentRole != UserRole.OWNER
        ) {
            Toast.makeText(
                this,
                "الصلاحية غير متاحة",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        baseLayout(
            "مكتب الشريك",
            "PARTNER OFFICE"
        )

        addVisualBanner(
            "🤝",
            "PARTNER",
            "إدارة العلاقة التجارية والشراكات",
            blue,
            navy
        )

        addStatus(
            "الحالة",
            "مصرح",
            green
        )

        addInfo(
            "الوظائف",
            "تعرض هذه النسخة الهيكل العام للمكتب. التنفيذ الحقيقي يعتمد على الخدمات الخلفية والعقود والصلاحيات."
        )

        addCard(
            "▰",
            "الوثائق",
            "مراجعة الوثائق المرتبطة بالشراكة.",
            {
                openPage {
                    showDocuments()
                }
            }
        )
    }

        // =========================================================
    // ملخص النظام
    // =========================================================

    private fun showSystemSummary() {
        baseLayout(
            "ملخص النظام",
            "حالة CENTRAL MARKET"
        )

        addVisualBanner(
            "◈",
            "SYSTEM",
            "ملخص الحماية والتكامل",
            navy,
            blue
        )

        addStatus(
            "الحماية الحساسة",
            "مفعلة",
            green
        )

        addStatus(
            "حماية الشاشة",
            "FLAG_SECURE",
            green
        )

        addStatus(
            "القفل الحساس",
            "3 دقائق",
            gold
        )

        addStatus(
            "BADGER",
            "مفعل كواجهة",
            blue
        )

        addStatus(
            "CTM AI",
            "مفعل كواجهة",
            blue
        )

        addInfo(
            "التكاملات الحكومية",
            "تحتاج مصادر حكومية أو جهات مخولة وتفويضات مناسبة قبل أي تحقق قانوني حقيقي."
        )

        addInfo(
            "التكاملات المالية",
            "تحتاج مؤسسات مالية مرخصة وخدمات خلفية وعقودًا مناسبة قبل أي تنفيذ حقيقي."
        )

        addInfo(
            "التتبع والموقع",
            "ليس وظيفة عامة. يتم تفعيله فقط عند حاجة خدمة مشروعة تتطلبه، مع احترام الخصوصية والقوانين."
        )
    }

    // =========================================================
    // مكتب المالك
    // =========================================================

    private fun showOwnerOffice() {
        if (currentRole != UserRole.OWNER) {
            Toast.makeText(
                this,
                "هذه الصفحة للمالك فقط",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        baseLayout(
            "مكتب المالك",
            "OWNER OFFICE"
        )

        addVisualBanner(
            "★",
            "OWNER OFFICE",
            "الإدارة العليا والاعتماد",
            gold,
            officeDark
        )

        addStatus(
            "الصلاحية",
            "مالك",
            gold
        )

        addInfo(
            "المالك",
            "ياسر حسن وشركاؤه"
        )

        addCard(
            "✓",
            "الموافقات",
            "مراجعة الوظائف التي تحتاج اعتماد المالك.",
            {
                openPage {
                    showOwnerApproval()
                }
            }
        )

        addCard(
            "🤝",
            "إدارة الشركاء",
            "مراجعة وإدارة بنية الشركاء.",
            {
                openPage {
                    showPartnerManagement()
                }
            }
        )

        addCard(
            "▤",
            "الوثائق",
            "الوثائق والاعتمادات.",
            {
                openPage {
                    showDocuments()
                }
            }
        )

        addCard(
            "◈",
            "ملخص النظام",
            "مراجعة حالة التكامل والحماية.",
            {
                openPage {
                    showSystemSummary()
                }
            }
        )
    }

    private fun showPartnerManagement() {
        if (currentRole != UserRole.OWNER) {
            Toast.makeText(
                this,
                "الصلاحية غير متاحة",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        baseLayout(
            "إدارة الشركاء",
            "PARTNER MANAGEMENT"
        )

        addVisualBanner(
            "🤝",
            "PARTNERS",
            "إدارة الشراكات",
            blue,
            navy
        )

        addInfo(
            "الحالة",
            "هذه الواجهة مخصصة لبنية الإدارة. إضافة أو تعديل شريك حقيقي يحتاج نظام خلفية آمنًا وصلاحيات موثقة."
        )

        addStatus(
            "الوصول",
            "المالك",
            green
        )
    }

    private fun showOwnerApproval() {
        if (currentRole != UserRole.OWNER) {
            Toast.makeText(
                this,
                "هذه الصفحة للمالك فقط",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        baseLayout(
            "اعتماد المالك",
            "OWNER APPROVAL"
        )

        addVisualBanner(
            "✓",
            "APPROVAL",
            "الاعتماد والمراجعة قبل التفعيل",
            gold,
            navy
        )

        addInfo(
            "المبدأ",
            "الوظائف الحساسة أو المؤسسات والتكاملات التي تتطلب اعتمادًا لا يتم تشغيلها تلقائيًا."
        )

        addStatus(
            "حالة الاعتماد",
            "تحتاج مراجعة",
            gold
        )

        addCard(
            "▣",
            "مراجعة النظام",
            "فتح ملخص النظام.",
            {
                openPage {
                    showSystemSummary()
                }
            }
        )
    }

    private fun showDocuments() {
        baseLayout(
            "الوثائق",
            "DOCUMENTS"
        )

        addVisualBanner(
            "▤",
            "DOCUMENTS",
            "الوثائق والعقود والاعتمادات",
            officeDark,
            blue
        )

        addInfo(
            "الوثائق القانونية",
            "الوظائف المنظمة قانونيًا يجب أن تعتمد على وثائق صحيحة ومصادر رسمية عند تشغيل الخدمة الفعلية."
        )

        addInfo(
            "العقود",
            "العمولات والشراكات والالتزامات المالية يجب أن تكون موثقة تعاقديًا."
        )

        addInfo(
            "التحقق",
            "أي تحقق رسمي من الأهلية أو الموانع القانونية يحتاج تكاملًا مع الجهة المخولة."
        )
    }

    // =========================================================
    // البحث
    // =========================================================

    private fun showSearch() {
        baseLayout(
            "البحث",
            "البحث داخل المنصة"
        )

        val searchInput = EditText(this).apply {
            hint = "اكتب ما تريد البحث عنه"
            textSize = 16f
            setSingleLine(true)
            setPadding(14, 10, 14, 10)
            background = roundedBackground(
                white,
                14f
            )
        }

        content.addView(
            searchInput,
            LinearLayout.LayoutParams(
                -1,
                56
            ).apply {
                setMargins(0, 0, 0, 10)
            }
        )

        val button = Button(this).apply {
            text = "بحث"
            isAllCaps = false
            setTextColor(white)
            background = roundedBackground(
                blue,
                16f
            )

            setOnClickListener {
                val query =
                    searchInput.text.toString().trim()

                if (query.isBlank()) {
                    Toast.makeText(
                        this@MainActivity,
                        "اكتب كلمة للبحث",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    addInfo(
                        "نتيجة البحث",
                        "تم استلام البحث عن: $query\n\nهذه النسخة تعرض واجهة البحث، بينما محرك البحث الفعلي يحتاج قاعدة بيانات أو خدمة خلفية."
                    )
                }
            }
        }

        content.addView(
            button,
            LinearLayout.LayoutParams(
                -1,
                54
            ).apply {
                setMargins(0, 0, 0, 14)
            }
        )

        addCard(
            "🛒",
            "الأسواق",
            "البحث داخل المنتجات والفئات.",
            {
                openPage {
                    showProducts()
                }
            }
        )

        addCard(
            "⚙",
            "الخدمات",
            "البحث داخل الخدمات.",
            {
                openPage {
                    showServices()
                }
            }
        )
    }

    // =========================================================
    // المفضلة
    // =========================================================

    private fun showFavorites() {
        baseLayout(
            "المفضلة",
            "العناصر المحفوظة"
        )

        addVisualBanner(
            "♡",
            "FAVORITES",
            "العناصر التي يختارها المستخدم",
            blue,
            navy
        )

        addInfo(
            "الحالة",
            "هذه النسخة تحتوي على واجهة المفضلة. التخزين الدائم يحتاج قاعدة بيانات محلية أو خدمة خلفية."
        )

        addCard(
            "🛒",
            "الأسواق والمنتجات",
            "العودة إلى المنتجات لإضافة العناصر.",
            {
                openPage {
                    showProducts()
                }
            }
        )
    }

    // =========================================================
    // النقاط
    // =========================================================

    private fun showPoints() {
        baseLayout(
            "النقاط",
            "نظام النقاط"
        )

        addVisualBanner(
            "★",
            "POINTS",
            "نظام النقاط داخل CENTRAL MARKET",
            gold,
            navy
        )

        addStatus(
            "الرصيد الحالي",
            "0 نقطة",
            gold
        )

        addInfo(
            "ملاحظة",
            "النقاط الحالية واجهة محلية. أي نظام مكافآت حقيقي يحتاج قواعد خلفية واضحة وشروط استخدام."
        )
    }

    // =========================================================
    // الإعلانات
    // =========================================================

    private fun showAds() {
        baseLayout(
            "الإعلانات",
            "مستويات الإعلان"
        )

        addVisualBanner(
            "📢",
            "ADVERTISE",
            "مساحات إعلانية منظمة",
            gold,
            blue
        )

        addCard(
            "B",
            "BRONZE",
            "المستوى الأساسي للإعلان.",
            {
                showAdMessage("BRONZE")
            }
        )

        addCard(
            "S",
            "SILVER",
            "المستوى المتوسط للإعلان.",
            {
                showAdMessage("SILVER")
            }
        )

        addCard(
            "G",
            "GOLD",
            "المستوى المميز للإعلان.",
            {
                showAdMessage("GOLD")
            }
        )

        addInfo(
            "الحماية",
            "أي شبكة إعلانية أو دفع حقيقي تحتاج تكاملًا منفصلًا وآمنًا."
        )
    }

    private fun showAdMessage(level: String) {
        Toast.makeText(
            this,
            "مستوى الإعلان: $level",
            Toast.LENGTH_SHORT
        ).show()
    }

    // =========================================================
    // المطبخ
    // =========================================================

    private fun showKitchen() {
        baseLayout(
            "المطبخ",
            "KITCHEN"
        )

        addVisualBanner(
            "🍲",
            "KITCHEN",
            "المطبخ والخدمات المرتبطة به",
            Color.rgb(130, 75, 35),
            gold
        )

        addCard(
            "🍽",
            "المطاعم",
            "استعراض قسم المطاعم.",
            {
                openPage {
                    showCategory("المطاعم والتوصيل")
                }
            }
        )

        addCard(
            "🛒",
            "المنتجات الغذائية",
            "استعراض الأسواق المرتبطة بالغذاء.",
            {
                openPage {
                    showCategory("الأسواق الغذائية")
                }
            }
        )

        addInfo(
            "التتبع",
            "لا يتم تشغيل تتبع الموقع تلقائيًا. إذا تطلبت خدمة توصيل معينة التتبع فعليًا، يكون مرتبطًا بالخدمة فقط وبالموافقة والمتطلبات القانونية."
        )
    }

    // =========================================================
    // الصدقة والدعم
    // =========================================================

    private fun showCharity() {
        baseLayout(
            "الدعم والصدقة",
            "الأيتام والمحتاجون"
        )

        addVisualBanner(
            "🤝",
            "CHARITY",
            "الدعم المجتمعي والإنساني",
            green,
            navy
        )

        addInfo(
            "الهدف",
            "واجهة لتنظيم مبادرات دعم الأيتام والمحتاجين وفق القوانين والجهات المختصة."
        )

        addInfo(
            "الشفافية",
            "أي أموال حقيقية تحتاج جهة مسؤولة وآليات تحقق وسجلات واضحة قبل التشغيل."
        )

        addStatus(
            "الواجهة",
            "متاحة",
            green
        )
    }

    // =========================================================
    // الرسائل والاقتراحات
    // =========================================================

    private fun showMessage() {
        baseLayout(
            "المراسلة",
            "رسالة إلى المنصة"
        )

        val messageInput = EditText(this).apply {
            hint = "اكتب رسالتك"
            textSize = 15f
            gravity = Gravity.TOP
            minLines = 5
            setPadding(14, 12, 14, 12)
            background = roundedBackground(
                white,
                14f
            )
        }

        content.addView(
            messageInput,
            LinearLayout.LayoutParams(
                -1,
                150
            ).apply {
                setMargins(0, 0, 0, 10)
            }
        )

        val send = Button(this).apply {
            text = "حفظ الرسالة"
            isAllCaps = false
            setTextColor(white)
            background = roundedBackground(
                blue,
                16f
            )

            setOnClickListener {
                if (
                    messageInput.text
                        .toString()
                        .trim()
                        .isBlank()
                ) {
                    Toast.makeText(
                        this@MainActivity,
                        "اكتب الرسالة أولًا",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "تم حفظ الرسالة محليًا في هذه النسخة",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        content.addView(
            send,
            LinearLayout.LayoutParams(
                -1,
                54
            )
        )
    }

    private fun showIdea() {
        baseLayout(
            "فكرة أو اقتراح",
            "تطوير CENTRAL MARKET"
        )

        addInfo(
            "شارك فكرتك",
            "هذه الواجهة مخصصة للأفكار والاقتراحات التطويرية."
        )

        val ideaInput = EditText(this).apply {
            hint = "اكتب الفكرة"
            textSize = 15f
            gravity = Gravity.TOP
            minLines = 5
            setPadding(14, 12, 14, 12)
            background = roundedBackground(
                white,
                14f
            )
        }

        content.addView(
            ideaInput,
            LinearLayout.LayoutParams(
                -1,
                150
            ).apply {
                setMargins(0, 0, 0, 10)
            }
        )

        val save = Button(this).apply {
            text = "حفظ الاقتراح"
            isAllCaps = false
            setTextColor(white)
            background = roundedBackground(
                green,
                16f
            )

            setOnClickListener {
                if (
                    ideaInput.text
                        .toString()
                        .trim()
                        .isBlank()
                ) {
                    Toast.makeText(
                        this@MainActivity,
                        "اكتب الفكرة أولًا",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "تم حفظ الاقتراح محليًا في هذه النسخة",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        content.addView(
            save,
            LinearLayout.LayoutParams(
                -1,
                54
            )
        )
    }

        // =========================================================
    // الاتصال
    // =========================================================

    private fun showConnectivity() {
        baseLayout(
            "اختبار الاتصال",
            "حالة اتصال الجهاز"
        )

        addVisualBanner(
            "📡",
            "CONNECTIVITY",
            "فحص الاتصال دون تشغيل تتبع الموقع",
            blue,
            navy
        )

        val online = isOnline()

        addStatus(
            "الاتصال",
            if (online) "متصل" else "غير متصل",
            if (online) green else red
        )

        addInfo(
            "الخصوصية",
            "اختبار الاتصال لا يعني تشغيل GPS أو تتبع موقع المستخدم."
        )

        addInfo(
            "التتبع",
            "الموقع لا يُستخدم كميزة عامة. يتم اللجوء إليه فقط عندما تتطلب خدمة محددة ذلك فعليًا، مع الالتزام بالخصوصية والموافقة والقوانين."
        )

        addCard(
            "↻",
            "إعادة الفحص",
            "فحص حالة الاتصال مرة أخرى.",
            {
                openPage {
                    showConnectivity()
                }
            }
        )
    }

    private fun showOnline() {
        showConnectivity()
    }

    private fun isOnline(): Boolean {
        return try {
            val manager =
                getSystemService(
                    CONNECTIVITY_SERVICE
                ) as ConnectivityManager

            val network =
                manager.activeNetwork
                    ?: return false

            val capabilities =
                manager.getNetworkCapabilities(network)
                    ?: return false

            capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            ) &&
                capabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED
                )
        } catch (
            _: SecurityException
        ) {
            false
        } catch (
            _: Exception
        ) {
            false
        }
    }

    // =========================================================
    // وضع الضيف
    // =========================================================

    private fun showGuestMode() {
        currentRole = UserRole.USER
        currentAccountName = "زائر"
        simulationLoggedIn = false

        baseLayout(
            "الوضع الضيف",
            "تصفح عام"
        )

        addVisualBanner(
            "👤",
            "GUEST MODE",
            "الوصول إلى المعلومات العامة",
            blue,
            navy
        )

        addStatus(
            "نوع الوصول",
            "عام",
            blue
        )

        addInfo(
            "الوضع الضيف",
            "يمكن للزائر تصفح الوظائف العامة دون الدخول إلى الحسابات الإدارية أو الوظائف الحساسة."
        )

        addCard(
            "🛒",
            "الأسواق",
            "تصفح الفئات.",
            {
                openPage {
                    showProducts()
                }
            }
        )

        addCard(
            "⚙",
            "الخدمات",
            "تصفح الخدمات العامة.",
            {
                openPage {
                    showServices()
                }
            }
        )

        addCard(
            "📢",
            "الإعلانات",
            "تصفح مستويات الإعلانات.",
            {
                openPage {
                    showAds()
                }
            }
        )

        addCard(
            "🔐",
            "الأمان والخصوصية",
            "قراءة قواعد الحماية.",
            {
                openPage {
                    showSecurity()
                }
            }
        )

        addCard(
            "⌂",
            "الرئيسية",
            "العودة إلى الصفحة الرئيسية.",
            {
                goHome()
            }
        )
    }

    // =========================================================
    // الأمان
    // =========================================================

    private fun showSecurity() {
        baseLayout(
            "الأمان والخصوصية",
            "حماية المستخدم والمنصة"
        )

        addVisualBanner(
            "🔐",
            "SECURITY",
            "الحماية والخصوصية",
            navy,
            blue
        )

        addStatus(
            "حماية الشاشة",
            "مفعلة",
            green
        )

        addStatus(
            "القفل العام",
            "10 دقائق",
            gold
        )

        addStatus(
            "القفل الحساس",
            "3 دقائق",
            gold
        )

        addInfo(
            "لقطات الشاشة",
            "تم استخدام FLAG_SECURE لمنع لقطات الشاشة وتسجيل الشاشة قدر الإمكان داخل التطبيق."
        )

        addInfo(
            "البيانات",
            "يجب عدم إظهار بيانات حساسة لمستخدم لا يملك الصلاحية."
        )

        addInfo(
            "التتبع والموقع",
            "لا يتم تشغيل الموقع أو التتبع بشكل عام. أي استخدام يجب أن يكون مرتبطًا بحاجة مشروعة ومحددة، مع احترام القوانين المحلية والدولية."
        )

        addInfo(
            "التحقق الرسمي",
            "الخدمات التي تحتاج تحققًا قانونيًا حقيقيًا يجب أن تستخدم مصادر حكومية أو جهات مخولة وآليات تفويض مناسبة."
        )

        addCard(
            "📜",
            "القواعد",
            "عرض قواعد المنصة.",
            {
                openPage {
                    showRules()
                }
            }
        )
    }

    private fun showSafety() {
        showSecurity()
    }

    // =========================================================
    // القواعد
    // =========================================================

    private fun showRules() {
        baseLayout(
            "قواعد المنصة",
            "CENTRAL MARKET RULES"
        )

        addVisualBanner(
            "◈",
            "RULES",
            "القواعد الأساسية للتشغيل الآمن",
            navy,
            gold
        )

        addInfo(
            "1 — الخصوصية",
            "لا يتم جمع أو تتبع بيانات المستخدم دون حاجة مشروعة وآلية مناسبة وموافقة عندما تكون مطلوبة."
        )

        addInfo(
            "2 — الموقع",
            "الموقع ليس ميزة عامة. يستخدم فقط عندما تحتاج خدمة محددة إليه فعليًا."
        )

        addInfo(
            "3 — الخدمات الحساسة",
            "الوظائف المالية والاستثمارية الحساسة تحتاج حماية إضافية والتحقق القانوني المناسب."
        )

        addInfo(
            "4 — الاعتماد",
            "التكاملات الحكومية والمالية الحقيقية لا تُعتبر مفعلة بمجرد وجود واجهة داخل التطبيق."
        )

        addInfo(
            "5 — المالك",
            "الوظائف التي تتطلب اعتمادًا إداريًا أو من المالك لا تُفعّل تلقائيًا."
        )

        addInfo(
            "6 — النسخ الدولية",
            "الوظائف الحكومية الخاصة بالدولة تُدار ضمن حزمة الدولة ولا تُفرض على النسخ الخارجية."
        )
    }

    // =========================================================
    // الجلسة
    // =========================================================

    private fun lockAccount() {
        if (!simulationLoggedIn) {
            return
        }

        handler.removeCallbacks(
            globalLockRunnable
        )

        handler.removeCallbacks(
            sensitiveLockRunnable
        )

        sensitiveLocked = true
        showSessionLock()
    }

    private fun showSessionLock() {
        baseLayout(
            "قفل الجلسة",
            "حماية الحساب"
        )

        addVisualBanner(
            "🔒",
            "SESSION LOCK",
            "تم تأمين الجلسة تلقائيًا",
            red,
            navy
        )

        addStatus(
            "الحالة",
            "مقفلة",
            red
        )

        addInfo(
            "السبب",
            "انتهت مدة عدم النشاط أو خرج التطبيق إلى الخلفية لمدة تجاوزت الحد المسموح."
        )

        addCard(
            "↻",
            "إعادة الدخول",
            "العودة إلى شاشة الحسابات.",
            {
                simulationLoggedIn = false
                sensitiveLocked = false
                showSimulationLogin()
            }
        )

        addCard(
            "⌂",
            "الرئيسية",
            "العودة إلى الصفحة الرئيسية.",
            {
                simulationLoggedIn = false
                sensitiveLocked = false
                goHome()
            }
        )
    }

    private fun logoutSimulation() {
        handler.removeCallbacks(
            globalLockRunnable
        )

        handler.removeCallbacks(
            sensitiveLockRunnable
        )

        simulationLoggedIn = false
        sensitiveLocked = false
        currentRole = UserRole.USER
        currentAccountName = "زائر"
        lastBackgroundTime = 0L

        Toast.makeText(
            this,
            "تم تسجيل الخروج",
            Toast.LENGTH_SHORT
        ).show()

        goHome()
    }

    // =========================================================
    // الرسم والخلفيات
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
            setStroke(
                1,
                Color.rgb(225, 230, 235)
            )
        }
    }

    private fun gradientBackground(
        firstColor: Int,
        secondColor: Int,
        radius: Float
    ): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                firstColor,
                secondColor
            )
        ).apply {
            cornerRadius = radius
        }
    }

    // =========================================================
    // نهاية MainActivity
    // =========================================================
}
