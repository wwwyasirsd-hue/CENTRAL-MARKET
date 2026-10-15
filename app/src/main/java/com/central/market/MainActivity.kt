package com.central.market

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.InputType
import android.view.Gravity
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
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
    private val sensitiveTimeout = 3 * 60 * 1000L

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }

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

    // حقل كتابة موحد: يطلب إظهار لوحة المفاتيح عند التركيز.
    private fun newTypingField(): EditText {
        return EditText(this).apply {
            isFocusable = true
            isFocusableInTouchMode = true

            setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    view.postDelayed({
                        if (view.hasFocus()) {
                            val imm = getSystemService(
                                Context.INPUT_METHOD_SERVICE
                            ) as InputMethodManager
                            imm.showSoftInput(
                                view,
                                InputMethodManager.SHOW_IMPLICIT
                            )
                        }
                    }, 180L)
                }
            }
        }
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
            clipToPadding = false

            setOnApplyWindowInsetsListener { view, insets ->
                val topInset =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        insets.getInsets(
                            android.view.WindowInsets.Type.statusBars()
                        ).top
                    } else {
                        @Suppress("DEPRECATION")
                        insets.systemWindowInsetTop
                    }

                val bottomInset =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        insets.getInsets(
                            android.view.WindowInsets.Type.navigationBars()
                        ).bottom
                    } else {
                        @Suppress("DEPRECATION")
                        insets.systemWindowInsetBottom
                    }

                view.setPadding(0, topInset, 0, bottomInset)
                insets
            }

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

        fun navButton(
            label: String,
            color: Int,
            action: () -> Unit
        ): Button {
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

        if (
            title != "البحث" &&
            title != "الدخول" &&
            title != "دخول الحساب" &&
            title != "قفل الجلسة" &&
            title != "القفل الحساس"
        ) {
            addSearchBar()
        }
    }

    private fun addSearchBar() {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 8)
        }

        val input = newTypingField().apply {
            hint = "بحث في الأقسام والمنتجات"
            textSize = 14f
            setSingleLine(true)
            setPadding(12, 8, 12, 8)
            background = roundedBackground(white, 14f)
        }

        row.addView(
            input,
            LinearLayout.LayoutParams(0, 48, 1f)
        )

        val button = Button(this).apply {
            text = "بحث"
            textSize = 13f
            isAllCaps = false
            setTextColor(white)
            background = roundedBackground(blue, 14f)

            setOnClickListener {
                val query = input.text.toString().trim()

                if (query.isBlank()) {
                    Toast.makeText(
                        this@MainActivity,
                        "اكتب كلمة للبحث",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    openPage { showSearch(query) }
                }
            }
        }

        row.addView(
            button,
            LinearLayout.LayoutParams(76, 48).apply {
                setMargins(8, 0, 0, 0)
            }
        )

        content.addView(row)
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

        content.addView(
            card,
            LinearLayout.LayoutParams(-1, -2).apply {
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
            isClickable = action != null
            setOnClickListener { action?.invoke() }
        }

        card.addView(
            TextView(this).apply {
                text = symbol
                textSize = 24f
                gravity = Gravity.CENTER
                setTextColor(gold)
            },
            LinearLayout.LayoutParams(50, 64)
        )

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

        card.addView(
            textBox,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        content.addView(
            card,
            LinearLayout.LayoutParams(-1, -2).apply {
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

        row.addView(
            TextView(this).apply {
                text = title
                textSize = 14f
                setTextColor(darkText)
            },
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        row.addView(TextView(this).apply {
            text = value
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(statusColor)
            gravity = Gravity.END
        })

        content.addView(
            row,
            LinearLayout.LayoutParams(-1, -2).apply {
                setMargins(0, 0, 0, 8)
            }
        )
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
            background = gradientBackground(
                firstColor,
                secondColor,
                24f
            )
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

        content.addView(
            banner,
            LinearLayout.LayoutParams(-1, -2).apply {
                setMargins(0, 8, 0, 14)
            }
        )
    }

    // =========================================================
    // الرئيسية
    // =========================================================

    private fun showHome() {
        pageHistory.clear()
        currentPage = null

        baseLayout(
            "الرئيسية",
            "منصة واحدة .. عالم من الفرص."
        )

        addVisualBanner(
            "◈",
            "CENTRAL MARKET",
            "الأسواق والخدمات والفرص في مكان واحد",
            navy,
            blue
        )

        val online = isOnline()

        addStatus(
            "حالة الاتصال",
            if (online) "متصل" else "غير متصل",
            if (online) green else red
        )

        addSection("المنصة")

        addCard(
            "🛒",
            "الأسواق والمنتجات",
            "المركبات والشاحنات والإلكترونيات وغيرها."
        ) {
            openPage { showProducts() }
        }

        addCard(
            "⚙",
            "الخدمات",
            "الخدمات العامة والذكاء البشري وCTM AI."
        ) {
            openPage { showServices() }
        }

        addCard(
            "📢",
            "الإعلانات",
            "BRONZE وSILVER وGOLD."
        ) {
            openPage { showAds() }
        }

        addCard(
            "🍲",
            "المطبخ",
            "المطاعم والمنتجات الغذائية."
        ) {
            openPage { showKitchen() }
        }

        addCard(
            "🤝",
            "الصدقة والدعم",
            "مبادرات الأيتام والمحتاجين."
        ) {
            openPage { showCharity() }
        }

        addSection("الوصول والحماية")

        addCard(
            "📡",
            "اختبار الاتصال",
            "فحص الشبكة دون تشغيل تتبع الموقع."
        ) {
            openPage { showConnectivity() }
        }

        addCard(
            "👤",
            "الوضع الضيف",
            "تصفح الوظائف العامة."
        ) {
            openPage { showGuestMode() }
        }

        addCard(
            "🔐",
            "الأمان والخصوصية",
            "قواعد حماية الحساب والبيانات."
        ) {
            openPage { showSecurity() }
        }

        addCard(
            "💡",
            "فكرة أو اقتراح",
            "إرسال اقتراح تطويري."
        ) {
            openPage { showIdea() }
        }

        addSection("الحساب")

        addCard(
            "◉",
            "الحساب والدخول",
            "الدخول إلى حسابك وفق الصلاحيات المتاحة."
        ) {
            openPage { showSimulationLogin() }
        }

        if (
            currentRole == UserRole.ADMIN ||
            currentRole == UserRole.PARTNER ||
            currentRole == UserRole.OWNER
        ) {
            addCard(
                "▣",
                "المكتب الإداري",
                "الأدوات المتاحة حسب صلاحية الجلسة."
            ) {
                openPage { showManagerOffice() }
            }
        }

        addInfo(
            "قاعدة الخصوصية",
            "لا يعمل تتبع الموقع كميزة عامة. يقتصر على الخدمات التي تحتاج إليه فعليًا وبما يتفق مع الموافقة والقوانين."
        )
    }

    // =========================================================
    // الحسابات والدخول
    // =========================================================

    private fun showSimulationLogin() {
        baseLayout("الدخول", "الوصول إلى حسابك")

        addInfo(
            "الدخول العام",
            "يمكنك المتابعة كمستخدم عام. الحسابات الإدارية والخاصة لا تُعرض هنا، ولا تُفتح إلا بعد ربط مصادقة موثوقة والتحقق من الصلاحيات."
        )

        addCard(
            "👤",
            "متابعة كمستخدم عام",
            "تصفح الوظائف العامة دون صلاحيات إدارية."
        ) {
            loginSimulation(UserRole.USER, "مستخدم عام")
        }

        addCard(
            "🔐",
            "دخول حساب خاص",
            "يتطلب هذا المسار مصادقة موثوقة غير مفعلة في النسخة المحلية."
        ) {
            Toast.makeText(
                this,
                "الدخول الخاص غير مفعّل بعد؛ لن نستخدم رموزًا ثابتة داخل التطبيق لحماية الحسابات.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showPasswordGate(
        role: UserRole,
        accountName: String
    ) {
        baseLayout("دخول الحساب", accountName)

        addInfo(
            "تنبيه",
            "رموز الدخول هنا تجريبية فقط. لا تستخدم هذه الآلية لحماية بيانات أو أموال حقيقية."
        )

        val input = newTypingField().apply {
            hint = "أدخل رمز المحاكاة"
            textSize = 16f
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_PASSWORD
            setPadding(14, 12, 14, 12)
            background = roundedBackground(white, 14f)
        }

        content.addView(
            input,
            LinearLayout.LayoutParams(-1, 56).apply {
                setMargins(0, 0, 0, 12)
            }
        )

        val login = Button(this).apply {
            text = "دخول"
            isAllCaps = false
            setTextColor(white)
            background = roundedBackground(blue, 16f)

            setOnClickListener {
                loginSimulation(
                    role,
                    accountName,
                    input.text.toString()
                )
            }
        }

        content.addView(
            login,
            LinearLayout.LayoutParams(-1, 54)
        )

        addInfo("الصلاحيات", roleName(role))
    }

    private fun loginSimulation(
        role: UserRole,
        accountName: String,
        password: String = ""
    ) {
        val now = SystemClock.elapsedRealtime()

        if (now < loginBlockedUntil) {
            val remaining =
                ((loginBlockedUntil - now) / 1000L) + 1L

            Toast.makeText(
                this,
                "محاولات الدخول موقوفة مؤقتًا. حاول بعد $remaining ثانية.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val valid = when (role) {
            UserRole.USER -> true
            UserRole.ADMIN,
            UserRole.PARTNER,
            UserRole.OWNER -> false
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

        addCard(
            "◉",
            "بيانات الحساب",
            "عرض حالة الحساب والصلاحيات."
        ) {
            openPage { showAccount() }
        }

        addCard("★", "النقاط", "نظام النقاط.") {
            openPage { showPoints() }
        }

        addCard("♡", "المفضلة", "العناصر المحفوظة.") {
            openPage { showFavorites() }
        }

        addCard(
            "⌕",
            "البحث",
            "البحث داخل أقسام المنصة."
        ) {
            openPage { showSearch() }
        }

        addCard(
            "↪",
            "تسجيل الخروج",
            "إنهاء الجلسة وإعادة التحقق عند الدخول التالي."
        ) {
            logoutSimulation()
        }

        if (
            currentRole == UserRole.ADMIN ||
            currentRole == UserRole.PARTNER ||
            currentRole == UserRole.OWNER
        ) {
            addCard(
                "▣",
                "المكتب الإداري",
                "فتح المكتب وفق الصلاحية."
            ) {
                openPage { showManagerOffice() }
            }
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
        addInfo(
            "الحماية",
            "لا يملك هذا الحساب صلاحية فتح الوظائف الإدارية الحساسة."
        )
    }

    private fun showAdminAccount() {
        baseLayout("حساب الإدارة", currentAccountName)

        addVisualBanner(
            "▣",
            "ADMIN OFFICE",
            "واجهة الإدارة والمراجعة",
            officeDark,
            navy
        )

        addStatus("الدور", "إدارة", blue)
        addStatus("الجلسة", "واجهة محلية", green)

        addInfo(
            "الصلاحيات",
            "الصلاحيات الإنتاجية تحتاج تحققًا من خادم موثوق."
        )
    }

    private fun showPartnerAccount() {
        baseLayout("حساب الشريك", currentAccountName)

        addVisualBanner(
            "🤝",
            "PARTNER OFFICE",
            "واجهة الشريك والأعمال",
            blue,
            navy
        )

        addStatus("الدور", "شريك", blue)

        addInfo(
            "الصلاحيات",
            "واجهة محلية تجريبية لا تمثل إثبات هوية أو صلاحية قانونية."
        )
    }

    private fun showOwnerAccount() {
        baseLayout("حساب المالك", currentAccountName)

        addVisualBanner(
            "★",
            "OWNER OFFICE",
            "الإدارة العليا والمراجعة",
            gold,
            navy
        )

        addStatus("الدور", "مالك", gold)

        addInfo(
            "المالك",
            "بيانات المالك متاحة ضمن الحساب المخوّل فقط."
        )

        addInfo(
            "قاعدة الاعتماد",
            "لا تنفذ هذه المحاكاة عمليات مالية حقيقية أو تغييرات إدارية على خادم."
        )
    }

    
    private fun showProducts() {
        baseLayout("الأسواق والمنتجات", "اختيار الفئة")
        addInfo(
            "الأسواق",
            "منصة متعددة الفئات مع إمكانية تخصيص الحزم حسب الدولة والمنطقة."
        )

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
        addInfo("الفئة المختارة", category)
        addStatus("حالة القسم", "واجهة تجريبية محلية", green)

        addInfo(
            "ملاحظة",
            "هذا القسم يعرض واجهة التطبيق التجريبية. نشر إعلانات حقيقية " +
                "واستقبال الطلبات يتطلبان قاعدة بيانات وخدمة خلفية."
        )

        if (
            category.contains("المركبات") ||
            category.contains("الشاحنات") ||
            category.contains("السفر")
        ) {
            addInfo(
                "الخصوصية والموقع",
                "لا يتم افتراض تتبع موقع المستخدم. أي خدمة تحتاج الموقع " +
                    "يجب أن تطلب الإذن وتوضح الغرض منه."
            )
        }

        addCard(
            "☆",
            "إضافة إلى المفضلة",
            "حفظ اختيارك محليًا في هذه الجلسة."
        ) {
            Toast.makeText(
                this,
                "تم اختيار الفئة للمفضلة محليًا.",
                Toast.LENGTH_SHORT
            ).show()
        }

        addCard(
            "⌕",
            "البحث داخل الفئة",
            "ابحث عن اسم الفئة أو الخدمة المطلوبة."
        ) {
            openPage { showSearch(category) }
        }
    }

    private fun roleName(role: UserRole): String {
        return when (role) {
            UserRole.USER -> "مستخدم"
            UserRole.ADMIN -> "إدارة"
            UserRole.PARTNER -> "شريك"
            UserRole.OWNER -> "مالك"
        }
    }

    private fun showServices() {
        baseLayout("الخدمات العامة", "بوابتك إلى الخدمات العامة الذكية")
        addVisualBanner(
            "الخدمات العامة",
            "الوصول إلى الأقسام والخدمات من مكان واحد"
        )

        addCard(
            "🧠",
            "الذكاء البشري",
            "معلومات ومساعدة لتنظيم الأفكار والمهام."
        ) {
            openPage { showHumanIntelligence() }
        }

        addCard(
            "✦",
            "CTM AI",
            "واجهة المساعد الذكي التجريبية."
        ) {
            openPage { showCtmAi() }
        }

        addCard(
            "🏥",
            "الصحة",
            "معلومات وخدمات صحية عامة."
        ) {
            openPage { showCategory("الصحة") }
        }

        addCard(
            "🎓",
            "التعليم",
            "الخدمات والمعلومات التعليمية."
        ) {
            openPage { showCategory("التعليم") }
        }

        addCard(
            "💡",
            "الكهرباء والمياه",
            "خدمات ومعلومات المرافق العامة."
        ) {
            openPage { showCategory("الكهرباء والمياه") }
        }

        addCard(
            "✈",
            "السفر",
            "معلومات وخدمات السفر."
        ) {
            openPage { showCategory("السفر") }
        }

        addCard(
            "🤲",
            "العمل الخيري",
            "واجهة تجريبية للمبادرات الخيرية."
        ) {
            openPage { showCharity() }
        }

        addCard(
            "🦡",
            "BADGER",
            "الدخول إلى واجهة الوحدة التجريبية."
        ) {
            openPage { showBadger() }
        }

        addCard(
            "✉",
            "الرسائل والمقترحات",
            "إرسال رسالة أو مقترح من خلال الواجهة التجريبية."
        ) {
            openPage { showMessage() }
        }
    }

    private fun showHumanIntelligence() {
        baseLayout("الذكاء البشري", "مساعدة المستخدم")

        addVisualBanner(
            "الذكاء البشري",
            "المعرفة والخبرة واتخاذ القرار بمسؤولية"
        )

        addInfo(
            "عن القسم",
            "قسم تجريبي لتنظيم الأفكار وعرض الإرشادات ومساعدة المستخدم " +
                "على تحديد الخطوات المناسبة."
        )

        addCard(
            "✎",
            "كتابة مقترح",
            "افتح نموذج المقترحات."
        ) {
            openPage { showIdea() }
        }

        addCard(
            "✉",
            "إرسال رسالة",
            "افتح نموذج الرسائل."
        ) {
            openPage { showMessage() }
        }
    }

    private fun showCtmAi() {
        baseLayout("CTM AI", "المساعد الذكي")

        addVisualBanner(
            "CTM AI",
            "مساعد ذكي ضمن منصة CENTRAL MARKET"
        )

        addInfo(
            "حالة المساعد",
            "هذه واجهة تجريبية. لا يوجد اتصال فعلي بخدمة ذكاء اصطناعي " +
                "في هذا الجزء من التطبيق."
        )

        addInfo(
            "ما يمكن إضافته لاحقًا",
            "ربط آمن بخدمة ذكاء اصطناعي، مع حماية بيانات المستخدم " +
                "وعدم إرسال المعلومات الحساسة دون ضرورة وموافقة مناسبة."
        )

        addCard(
            "✎",
            "إرسال فكرة لتطوير المساعد",
            "اكتب اقتراحك."
        ) {
            openPage { showIdea() }
        }
    }

    
    private fun showBadger() {
        baseLayout("BADGER", "الوحدة التجريبية")

        addVisualBanner(
            "BADGER",
            "وحدة الخدمات والأدوات المترابطة"
        )

        addInfo(
            "حالة الوحدة",
            "واجهة تجريبية محلية. تشغيل الخدمات المتصلة بالحسابات " +
                "أو البيانات يتطلب ربطًا خلفيًا آمنًا."
        )

        addCard(
            "▣",
            "لوحة الوحدة",
            "استعراض الأدوات المتاحة."
        ) {
            openPage { showBadgerDashboard() }
        }

        addCard(
            "🔒",
            "المشاركة الاستثمارية",
            "منطقة حساسة تتطلب التحقق من الصلاحيات والجلسة."
        ) {
            openPage { showSensitiveInvestment() }
        }

        addCard(
            "⚙",
            "أدوات الإدارة",
            "متاحة للحسابات المخولة فقط."
        ) {
            if (isPrivileged()) {
                openPage { showManagerTools() }
            } else {
                Toast.makeText(
                    this,
                    "هذه الوظيفة تتطلب صلاحية إدارية.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showBadgerDashboard() {
        baseLayout("لوحة BADGER", "الأدوات والخدمات")

        addInfo(
            "الوحدة",
            "هذه لوحة محاكاة. لا تُنفذ تحويلات مالية أو معاملات " +
                "حقيقية ولا تتصل بخادم خارجي."
        )

        addCard(
            "📄",
            "المستندات",
            "عرض واجهة المستندات."
        ) {
            openPage { showDocuments() }
        }

        addCard(
            "✉",
            "الرسائل",
            "فتح قسم الرسائل."
        ) {
            openPage { showMessage() }
        }

        addCard(
            "🔐",
            "المشاركة الاستثمارية",
            "الانتقال إلى القسم الحساس بعد التحقق."
        ) {
            openPage { showSensitiveInvestment() }
        }

        addCard(
            "↩",
            "العودة إلى الخدمات",
            "عرض أقسام الخدمات العامة."
        ) {
            openPage { showServices() }
        }
    }

    private fun showSensitiveInvestment() {
        if (!isPrivileged()) {
            Toast.makeText(
                this,
                "لا تملك صلاحية الوصول إلى هذه الوظيفة.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (!simulationLoggedIn) {
            showSimulationLogin()
            return
        }

        if (sensitiveLocked) {
            showSensitiveLock()
            return
        }

        handler.removeCallbacks(sensitiveLockRunnable)
        handler.postDelayed(
            sensitiveLockRunnable,
            sensitiveTimeout
        )

        baseLayout("المشاركة الاستثمارية", "وظيفة حساسة")

        addVisualBanner(
            "المشاركة الاستثمارية",
            "واجهة محاكاة وليست نظامًا ماليًا فعليًا"
        )

        addStatus(
            "حالة الجلسة",
            "تم اجتياز فحص الصلاحية المحلي",
            green
        )

        addInfo(
            "حدود النسخة التجريبية",
            "لا يتم إنشاء استثمار حقيقي أو تنفيذ تحويل مالي أو حفظ " +
                "أموال المستخدمين. يجب أن يسبق التشغيل الفعلي تحقق " +
                "من الهوية وصلاحيات الخادم ومراجعة المتطلبات القانونية."
        )

        addInfo(
            "حماية الحساب",
            "يتم قفل القسم الحساس بعد انتهاء المهلة المحددة، ويمكن " +
                "للمستخدم قفله يدويًا."
        )

        addCard(
            "🔒",
            "قفل الوظيفة الحساسة",
            "إغلاق القسم الحساس الآن."
        ) {
            lockSensitiveSection()
        }
    }

    private fun showSensitiveLock() {
        handler.removeCallbacks(sensitiveLockRunnable)
        sensitiveLocked = true

        baseLayout("القفل الحساس", "حماية الوظائف الحساسة")

        addInfo(
            "القسم مقفل",
            "تم قفل الوظيفة الحساسة. أعد التحقق من الجلسة والصلاحيات " +
                "قبل محاولة الوصول مجددًا."
        )

        addCard(
            "🔐",
            "إعادة التحقق",
            "العودة إلى بوابة الحساب التجريبي."
        ) {
            if (simulationLoggedIn && isPrivileged()) {
                sensitiveLocked = false
                openPage { showSensitiveInvestment() }
            } else {
                openPage { showSimulationLogin() }
            }
        }

        addCard(
            "↩",
            "العودة إلى الصفحة الرئيسية",
            "الخروج من القسم الحساس."
        ) {
            sensitiveLocked = true
            goHome()
        }
    }

    private fun lockSensitiveSection() {
        handler.removeCallbacks(sensitiveLockRunnable)
        sensitiveLocked = true
        showSensitiveLock()
    }

    private fun lockAccount() {
        handler.removeCallbacks(globalLockRunnable)
        handler.removeCallbacks(sensitiveLockRunnable)

        if (!simulationLoggedIn) {
            showHome()
            return
        }

        lastActivityTime = System.currentTimeMillis()
        showSessionLock()
    }

    private fun showSessionLock() {
        baseLayout("قفل الجلسة", "حماية الحساب")

        addVisualBanner(
            "الجلسة مقفلة",
            "أعد التحقق للمتابعة في النسخة التجريبية"
        )

        addInfo(
            "سبب القفل",
            "تم إيقاف الوصول إلى صفحات الحساب حتى إعادة التحقق."
        )

        addCard(
            "🔐",
            "إعادة فتح الجلسة التجريبية",
            "العودة إلى بوابة تسجيل الدخول."
        ) {
            openPage { showSimulationLogin() }
        }

        addCard(
            "↩",
            "إنهاء الجلسة",
            "الخروج من الحساب التجريبي."
        ) {
            logoutSimulation()
        }
    }

    private fun clearSession() {
        handler.removeCallbacks(globalLockRunnable)
        handler.removeCallbacks(sensitiveLockRunnable)

        simulationLoggedIn = false
        sensitiveLocked = false
        currentRole = UserRole.USER
        currentAccountName = "زائر"
        failedLoginAttempts = 0
        loginBlockedUntil = 0L
        lastActivityTime = 0L
        lastBackgroundTime = 0L
    }

    private fun logoutSimulation() {
        clearSession()
        pageHistory.clear()
        currentPage = null
        showHome()

        Toast.makeText(
            this,
            "تم إنهاء الجلسة التجريبية.",
            Toast.LENGTH_SHORT
        ).show()
    }

    
    // =========================================================
    // الإعلانات
    // =========================================================

    private fun showAds() {
        baseLayout("الإعلانات", "باقات العرض الإعلاني")

        addVisualBanner(
            "📢",
            "CENTRAL MARKET ADS",
            "خيارات إعلانية تجريبية",
            navy,
            gold
        )

        addCard(
            "🥉",
            "BRONZE",
            "الباقة الأساسية للإعلانات."
        ) {
            openPage { showAdPackage("BRONZE") }
        }

        addCard(
            "🥈",
            "SILVER",
            "الباقة المتوسطة للإعلانات."
        ) {
            openPage { showAdPackage("SILVER") }
        }

        addCard(
            "🥇",
            "GOLD",
            "الباقة المميزة للإعلانات."
        ) {
            openPage { showAdPackage("GOLD") }
        }

        addInfo(
            "تنبيه",
            "الباقات هنا للعرض فقط. لا توجد مدفوعات أو عمليات نشر فعلية."
        )
    }

    private fun showAdPackage(packageName: String) {
        baseLayout("باقة $packageName", "تفاصيل الباقة")

        addStatus("الباقة المختارة", packageName, gold)

        addInfo(
            "الحالة",
            "لم يتم تفعيل الباقة أو تحصيل رسوم. يلزم لاحقًا تحديد الأسعار " +
                "والشروط وربط نظام إعلانات وخادم موثوق."
        )

        addActionButton("إرسال استفسار عن الباقة") {
            openPage {
                showMessage("استفسار عن باقة $packageName")
            }
        }
    }

    // =========================================================
    // المطبخ
    // =========================================================

    private fun showKitchen() {
        baseLayout("المطبخ", "المطاعم والمنتجات الغذائية")

        addVisualBanner(
            "🍲",
            "CENTRAL KITCHEN",
            "واجهة تجريبية للأطعمة والخدمات الغذائية",
            navy,
            gold
        )

        addCard(
            "🍽",
            "المطاعم",
            "استعراض واجهة المطاعم."
        ) {
            openPage { showCategory("المطاعم") }
        }

        addCard(
            "🥬",
            "الخضروات والمنتجات الطازجة",
            "استعراض المنتجات الزراعية."
        ) {
            openPage { showCategory("المنتجات الزراعية") }
        }

        addCard(
            "🐟",
            "الأسماك",
            "استعراض قسم الأسماك."
        ) {
            openPage { showCategory("الأسماك") }
        }

        addCard(
            "✉",
            "استفسار غذائي أو تجاري",
            "إرسال رسالة من النموذج المحلي."
        ) {
            openPage { showMessage("استفسار عن المطبخ") }
        }

        addInfo(
            "حالة الخدمة",
            "لا يوجد طلب طعام أو دفع أو توصيل فعلي في هذه النسخة."
        )
    }

    // =========================================================
    // الصدقة والدعم
    // =========================================================

    private fun showCharity() {
        baseLayout("الصدقة والدعم", "المبادرات الإنسانية")

        addVisualBanner(
            "🤲",
            "العطاء والتكافل",
            "واجهة للمبادرات الخيرية ودعم المحتاجين",
            navy,
            green
        )

        addCard(
            "🧒",
            "دعم الأيتام",
            "واجهة تعريفية لمبادرات رعاية الأيتام."
        ) {
            openPage { showCharityDetails("دعم الأيتام") }
        }

        addCard(
            "🤝",
            "مساعدة المحتاجين",
            "واجهة تعريفية للمبادرات الإنسانية."
        ) {
            openPage { showCharityDetails("مساعدة المحتاجين") }
        }

        addCard(
            "🇸🇩",
            "المبادرات المرتبطة بالسودان",
            "عرض فكرة مبادرات دعم المجتمع السوداني."
        ) {
            openPage { showCharityDetails("المبادرات المرتبطة بالسودان") }
        }

        addInfo(
            "الأمانة المالية",
            "لا يستقبل التطبيق تبرعات مالية حقيقية في هذه النسخة. " +
                "أي جمع أموال مستقبلي يتطلب التحقق من الجهة المستفيدة " +
                "والالتزام بالمتطلبات القانونية."
        )
    }

    private fun showCharityDetails(program: String) {
        baseLayout(program, "تفاصيل المبادرة")

        addInfo(
            "المبادرة",
            program
        )

        addInfo(
            "حالة التنفيذ",
            "صفحة تعريفية تجريبية. لم يُنشأ طلب مساعدة ولم تُستقبل أموال."
        )

        addActionButton("إرسال استفسار") {
            openPage {
                showMessage("استفسار حول مبادرة: $program")
            }
        }
    }

    // =========================================================
    // الاتصال
    // =========================================================

    private fun showConnectivity() {
        baseLayout("اختبار الاتصال", "فحص اتصال الشبكة")

        val online = isOnline()

        addVisualBanner(
            "📡",
            "حالة الشبكة",
            if (online) "يوجد اتصال إنترنت متحقق" else "لا يوجد اتصال إنترنت متحقق",
            navy,
            if (online) green else red
        )

        addStatus(
            "الإنترنت",
            if (online) "متصل" else "غير متصل",
            if (online) green else red
        )

        addInfo(
            "نطاق الاختبار",
            "يفحص هذا الاختبار حالة الاتصال العامة فقط. لا يختبر خادم CENTRAL MARKET " +
                "ولا يثبت أن خدمات الحسابات تعمل."
        )

        addActionButton("إعادة فحص الاتصال") {
            openPage { showConnectivity() }
        }
    }

    // =========================================================
    // وضع الضيف
    // =========================================================

    private fun showGuestMode() {
        baseLayout("الوضع الضيف", "التصفح العام")

        addInfo(
            "صلاحيات الضيف",
            "يمكنك تصفح الأقسام العامة. لا يمنح وضع الضيف أي صلاحيات " +
                "إدارية أو مالية أو وصول إلى حسابات خاصة."
        )

        addCard(
            "🛒",
            "الأسواق",
            "تصفح الفئات العامة."
        ) {
            openPage { showProducts() }
        }

        addCard(
            "⚙",
            "الخدمات العامة",
            "تصفح الخدمات المتاحة."
        ) {
            openPage { showServices() }
        }

        addCard(
            "📢",
            "الإعلانات",
            "تصفح الباقات التجريبية."
        ) {
            openPage { showAds() }
        }

        addCard(
            "👤",
            "الدخول إلى الحساب",
            "الانتقال إلى بوابة الحساب."
        ) {
            openPage { showSimulationLogin() }
        }
    }

    // =========================================================
    // الأمان والخصوصية
    // =========================================================

    private fun showSecurity() {
        baseLayout("الأمان والخصوصية", "معلومات حماية التطبيق")

        addVisualBanner(
            "🔐",
            "الحماية",
            "مبادئ حماية الجلسة والبيانات",
            officeDark,
            navy
        )

        addInfo(
            "حماية لقطات الشاشة",
            "يستخدم التطبيق FLAG_SECURE لمنع لقطات الشاشة والتسجيل " +
                "في النوافذ المحمية على الأنظمة التي تدعم ذلك."
        )

        addInfo(
            "قفل الجلسة",
            "توجد آلية لقفل الجلسة التجريبية بعد انتهاء المهلة المحددة. " +
                "هذا لا يغني عن مصادقة الخادم في الإصدار الفعلي."
        )

        addInfo(
            "البيانات الحساسة",
            "لا تدخل كلمات مرور حقيقية أو معلومات مالية سرية في نماذج المحاكاة."
        )

        addInfo(
            "الموقع",
            "لا يطلب هذا القسم موقعك ولا يشغل تتبع الموقع."
        )

        addInfo(
            "حدود الحماية",
            "الحماية المحلية وحدها لا تكفي لتأمين حسابات المالك أو الشركاء " +
                "أو العمليات المالية الحقيقية."
        )
    }

    // =========================================================
    // المقترحات والرسائل
    // =========================================================

    private fun showIdea() {
        baseLayout("فكرة أو اقتراح", "المساهمة في تطوير المنصة")

        val titleInput = newTypingField().apply {
            hint = "عنوان الفكرة"
            textSize = 15f
            setSingleLine(true)
            setPadding(12, 10, 12, 10)
            background = roundedBackground(white, 14f)
        }

        val detailsInput = newTypingField().apply {
            hint = "اشرح الفكرة أو التحسين المقترح"
            textSize = 15f
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            gravity = Gravity.TOP or Gravity.START
            minLines = 4
            setPadding(12, 12, 12, 12)
            background = roundedBackground(white, 14f)
        }

        content.addView(
            titleInput,
            LinearLayout.LayoutParams(-1, 52).apply {
                setMargins(0, 8, 0, 10)
            }
        )

        content.addView(
            detailsInput,
            LinearLayout.LayoutParams(-1, 130).apply {
                setMargins(0, 0, 0, 12)
            }
        )

        addActionButton("حفظ مسودة الاقتراح") {
            val title = titleInput.text.toString().trim()
            val details = detailsInput.text.toString().trim()

            if (title.isBlank() || details.isBlank()) {
                Toast.makeText(
                    this,
                    "أدخل عنوان الفكرة وتفاصيلها.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "تم التحقق من الحقول. الحفظ الدائم والإرسال إلى الخادم غير مفعّلين.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        addInfo(
            "ملاحظة",
            "النموذج لا يرسل البيانات إلى الإنترنت ولا يحفظها حفظًا دائمًا."
        )
    }

    private fun showMessage(
        initialMessage: String = ""
    ) {
        baseLayout("الرسائل", "نموذج مراسلة تجريبي")

        val subjectInput = newTypingField().apply {
            hint = "موضوع الرسالة"
            setSingleLine(true)
            setPadding(12, 10, 12, 10)
            background = roundedBackground(white, 14f)
        }

        val bodyInput = newTypingField().apply {
            hint = "اكتب رسالتك"
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            gravity = Gravity.TOP or Gravity.START
            minLines = 4
            setPadding(12, 12, 12, 12)
            background = roundedBackground(white, 14f)
            setText(initialMessage)
        }

        content.addView(
            subjectInput,
            LinearLayout.LayoutParams(-1, 52).apply {
                setMargins(0, 8, 0, 10)
            }
        )

        content.addView(
            bodyInput,
            LinearLayout.LayoutParams(-1, 130).apply {
                setMargins(0, 0, 0, 12)
            }
        )

        addActionButton("التحقق من الرسالة") {
            val subject = subjectInput.text.toString().trim()
            val body = bodyInput.text.toString().trim()

            if (subject.isBlank() || body.isBlank()) {
                Toast.makeText(
                    this,
                    "أدخل موضوع الرسالة ونصها.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "الرسالة جاهزة محليًا؛ لم تُرسل إلى أي جهة.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        addInfo(
            "الخصوصية",
            "لا تُرسل الرسالة إلى خادم في هذه النسخة التجريبية."
        )
    }

    
    // =========================================================
    // النقاط والمفضلة
    // =========================================================

    private fun showPoints() {
        baseLayout("النقاط", "نظام النقاط التجريبي")

        addVisualBanner(
            "★",
            "نقاط المستخدم",
            "واجهة أولية لنظام النقاط",
            navy,
            gold
        )

        addStatus("رصيد النقاط", "0", blue)

        addInfo(
            "حالة النظام",
            "لا توجد نقاط مكتسبة أو مكافآت مالية حقيقية. " +
                "احتساب النقاط واسترجاعها يحتاج إلى تخزين موثوق على الخادم."
        )
    }

    private fun showFavorites() {
        baseLayout("المفضلة", "العناصر المحفوظة")

        addInfo(
            "المفضلة",
            "لا توجد عناصر محفوظة بصورة دائمة في هذه النسخة التجريبية."
        )

        addCard(
            "🛒",
            "الأسواق والمنتجات",
            "افتح الفئات لاختيار المنتجات."
        ) {
            openPage { showProducts() }
        }

        addCard(
            "⌕",
            "البحث",
            "ابحث عن قسم أو خدمة."
        ) {
            openPage { showSearch() }
        }

        addInfo(
            "ملاحظة",
            "زر إضافة الفئة إلى المفضلة في النسخة الحالية لا ينشئ سجلًا دائمًا."
        )
    }

    // =========================================================
    // المكتب الإداري
    // =========================================================

    private fun showManagerOffice() {
        if (!isPrivileged()) {
            Toast.makeText(
                this,
                "يتطلب المكتب الإداري حسابًا مخولًا.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        baseLayout("المكتب الإداري", "أدوات الإدارة حسب الصلاحية")

        addVisualBanner(
            "▣",
            "MANAGEMENT OFFICE",
            "واجهة إدارية تجريبية",
            officeDark,
            navy
        )

        addStatus(
            "الدور الحالي",
            roleName(currentRole),
            gold
        )

        addInfo(
            "حدود الصلاحية",
            "الدور الحالي محفوظ محليًا داخل المحاكاة. لا يمثل إثبات هوية " +
                "ولا يمنح صلاحيات على خادم حقيقي."
        )

        addCard(
            "⚙",
            "أدوات الإدارة",
            "عرض أدوات الإدارة التجريبية."
        ) {
            openPage { showManagerTools() }
        }

        addCard(
            "📄",
            "المستندات",
            "عرض واجهة المستندات."
        ) {
            openPage { showDocuments() }
        }

        addCard(
            "🔐",
            "الأمان والخصوصية",
            "مراجعة مبادئ الحماية."
        ) {
            openPage { showSecurity() }
        }

        addCard(
            "✉",
            "الرسائل والمقترحات",
            "فتح نموذج المراسلة."
        ) {
            openPage { showMessage() }
        }
    }

    private fun showManagerTools() {
        if (!isPrivileged()) {
            Toast.makeText(
                this,
                "ليس لديك تصريح للوصول إلى أدوات الإدارة.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        baseLayout("أدوات الإدارة", "واجهة محلية تجريبية")

        addStatus("الدور", roleName(currentRole), gold)

        addCard(
            "📄",
            "المستندات",
            "عرض صفحة المستندات التجريبية."
        ) {
            openPage { showDocuments() }
        }

        addCard(
            "📡",
            "فحص الاتصال",
            "معرفة حالة اتصال الجهاز بالإنترنت."
        ) {
            openPage { showConnectivity() }
        }

        addCard(
            "🔐",
            "التحقق من الجلسة",
            "مراجعة حالة الجلسة الحالية."
        ) {
            openPage { showAdminSessionStatus() }
        }

        addInfo(
            "مهم",
            "لا توجد هنا إدارة مستخدمين فعلية أو صلاحيات محفوظة على خادم. " +
                "يجب أن تُفرض صلاحيات الإدارة الحقيقية من الخادم نفسه."
        )
    }

    private fun showAdminSessionStatus() {
        if (!isPrivileged()) {
            goHome()
            return
        }

        baseLayout("حالة الجلسة", "معلومات محلية")

        addStatus(
            "الجلسة",
            if (simulationLoggedIn) "نشطة" else "غير نشطة",
            if (simulationLoggedIn) green else red
        )

        addStatus(
            "الدور",
            roleName(currentRole),
            gold
        )

        addStatus(
            "القسم الحساس",
            if (sensitiveLocked) "مقفل" else "غير مقفل",
            if (sensitiveLocked) red else green
        )

        addInfo(
            "مصدر الحالة",
            "المعلومات المعروضة تخص جلسة التطبيق الحالية فقط، " +
                "ولا تثبت صحة صلاحيات حساب على خادم."
        )

        addActionButton("قفل القسم الحساس") {
            lockSensitiveSection()
        }

        addActionButton("إنهاء الجلسة") {
            logoutSimulation()
        }
    }

    // =========================================================
    // المستندات
    // =========================================================

    private fun showDocuments() {
        if (!simulationLoggedIn) {
            baseLayout("المستندات", "واجهة عامة")

            addInfo(
                "الوصول",
                "تصفح معلومات المستندات العامة. لا توجد مستندات خاصة محملة."
            )

            addCard(
                "👤",
                "الدخول إلى الحساب",
                "انتقل إلى بوابة الدخول."
            ) {
                openPage { showSimulationLogin() }
            }

            return
        }

        baseLayout("المستندات", "واجهة المستندات التجريبية")

        addVisualBanner(
            "📄",
            "المستندات",
            "عرض أولي لتنظيم الوثائق",
            navy,
            blue
        )

        addInfo(
            "الحالة",
            "لم يتم تحميل مستندات من خادم أو حفظ ملفات خاصة داخل هذه الصفحة."
        )

        addCard(
            "✉",
            "استفسار عن مستند",
            "اكتب استفسارًا في نموذج المراسلة."
        ) {
            openPage { showMessage("استفسار عن المستندات") }
        }

        addInfo(
            "حماية البيانات",
            "قبل إضافة رفع المستندات الفعلية، يجب تحديد صلاحيات الوصول " +
                "والتخزين الآمن وسياسة الاحتفاظ والحذف."
        )
    }

    // =========================================================
    // إنهاء الملف
    // =========================================================

}
