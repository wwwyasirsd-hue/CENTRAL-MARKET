package com.central.market

import android.app.Activity
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
import android.view.View
import android.view.ViewGroup
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
    private var sensitiveLocked = true
    private var guestMode = false

    private var lastBackgroundTime = 0L
    private var lastActivityTime = 0L
    private var failedLoginAttempts = 0
    private var loginBlockedUntil = 0L
    private var searchTapCount = 0

    private val globalTimeout = 10 * 60 * 1000L
    private val sensitiveTimeout = 3 * 60 * 1000L

    private val pageHistory = ArrayList<() -> Unit>()
    private var currentPage: (() -> Unit)? = null

    private val globalLockRunnable = Runnable {
        if (simulationLoggedIn) lockAccount()
    }

    private val sensitiveLockRunnable = Runnable {
        if (simulationLoggedIn && !sensitiveLocked) {
            sensitiveLocked = true
            showSensitiveLock()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }

        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        lastActivityTime = SystemClock.elapsedRealtime()
        showSimulationLogin()
    }

        override fun onResume() {
        super.onResume()

        val now = SystemClock.elapsedRealtime()
        val backgroundDuration =
            if (lastBackgroundTime > 0L) {
                now - lastBackgroundTime
            } else {
                0L
            }

        lastBackgroundTime = 0L

        if (
            simulationLoggedIn &&
            backgroundDuration >= globalTimeout
        ) {
            lockAccount()
            return
        }

        if (
            simulationLoggedIn &&
            backgroundDuration >= sensitiveTimeout
        ) {
            sensitiveLocked = true
        }

        lastActivityTime = now
        handler.removeCallbacks(globalLockRunnable)
        handler.removeCallbacks(sensitiveLockRunnable)
        }

    override fun onPause() {
        super.onPause()

        lastBackgroundTime = SystemClock.elapsedRealtime()

        handler.removeCallbacks(globalLockRunnable)
        handler.postDelayed(globalLockRunnable, globalTimeout)

        handler.removeCallbacks(sensitiveLockRunnable)
        handler.postDelayed(sensitiveLockRunnable, sensitiveTimeout)
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (guestMode || !simulationLoggedIn) {
            showSimulationLogin()
            return
        }

        exitPage()
    }

    private fun showSimulationLogin() {
        guestMode = false
        simulationLoggedIn = false
        currentRole = UserRole.USER
        currentAccountName = "زائر"
        sensitiveLocked = true

        pageHistory.clear()
        currentPage = null

        val root = baseLayout(
            "CENTRAL MARKET",
            "بوابتك إلى الخدمات العامة الذكية"
        )

        addSectionTitle(root, "مرحبًا بك")

        addInfo(
            root,
            "نسخة تجريبية. تسجيل الحسابات الخاصة والتحقق الحقيقي " +
                "من الهوية يحتاجان إلى خادم آمن."
        )

        addActionButton(
            root,
            "تصفح المنتجات والأسعار العامة",
            blue
        ) {
            guestMode = true
            simulationLoggedIn = false
            showGuestMode()
        }

        addActionButton(
            root,
            "دخول المستخدم التجريبي",
            gold
        ) {
            loginSimulation(UserRole.USER, "مستخدم تجريبي")
        }

        addActionButton(
            root,
            "دخول الحساب الخاص",
            navy
        ) {
            showMessage(
                "الحسابات الخاصة غير مفعّلة حتى ربط التحقق بخادم آمن."
            )
        }

        addInfo(
            root,
            "التصفح العام لا يمنح صلاحية الشراء أو دخول المكاتب الخاصة."
        )
    }

    private fun loginSimulation(
        role: UserRole,
        accountName: String
    ) {
        if (role != UserRole.USER) {
            showMessage(
                "الحسابات الخاصة تحتاج إلى تحقق آمن."
            )
            return
        }

        guestMode = false
        currentRole = UserRole.USER
        currentAccountName = accountName
        simulationLoggedIn = true
        sensitiveLocked = true

        registerActivity()
        pageHistory.clear()
        currentPage = { showAccountHome() }
        showAccountHome()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()

        lastActivityTime = SystemClock.elapsedRealtime()

        if (simulationLoggedIn) {
            handler.removeCallbacks(globalLockRunnable)
            handler.postDelayed(
                globalLockRunnable,
                globalTimeout
            )
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(globalLockRunnable)
        handler.removeCallbacks(sensitiveLockRunnable)
        super.onDestroy()
    }

    private fun registerActivity() {
        lastActivityTime = SystemClock.elapsedRealtime()
        lastBackgroundTime = 0L

        handler.removeCallbacks(globalLockRunnable)
        handler.removeCallbacks(sensitiveLockRunnable)
    }

    private fun focusAndShowKeyboard(input: EditText) {
        input.requestFocus()

        input.post {
            val manager = getSystemService(
                INPUT_METHOD_SERVICE
            ) as InputMethodManager

            manager.showSoftInput(
                input,
                InputMethodManager.SHOW_IMPLICIT
            )
        }
    }

    private fun openPage(page: () -> Unit) {
        pageHistory.add(currentPage ?: { goHome() })
        currentPage = page
        page()
    }

    private fun goHome() {
        when {
            simulationLoggedIn -> showHome()
            guestMode -> showGuestMode()
            else -> showSimulationLogin()
        }
    }

    private fun exitPage() {
        if (pageHistory.isNotEmpty()) {
            val previous = pageHistory.removeAt(
                pageHistory.lastIndex
            )

            currentPage = previous
            previous()
        } else {
            goHome()
        }
    }

    private fun baseLayout(
        title: String,
        subtitle: String = ""
    ): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 20)
            background = gradientBackground()
            isFocusable = true
            isFocusableInTouchMode = true
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            isClickable = true
            isFocusable = true

            addView(
                root,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scroll.setOnApplyWindowInsetsListener { view, insets ->
                view.setPadding(
                    0,
                    insets.systemWindowInsetTop,
                    0,
                    insets.systemWindowInsetBottom
                )
                insets
            }
        }

        content = root
        setContentView(scroll)

        addTopNavigation(title)

        if (subtitle.isNotBlank()) {
            val sub = TextView(this).apply {
                text = subtitle
                textSize = 14f
                setTextColor(white)
                gravity = Gravity.CENTER
                setPadding(8, 10, 8, 16)
            }

            root.addView(sub)
        }

        return root
    }

        private fun addTopNavigation(title: String) {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(6, 8, 6, 8)
            background = cardBackground()
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(gold)
            gravity = Gravity.CENTER_VERTICAL
        }

        bar.addView(
            titleView,
            LinearLayout.LayoutParams(0, 48, 1f)
        )

        val back = Button(this).apply {
            text = "رجوع"
            isAllCaps = false
            setOnClickListener { exitPage() }
        }
        bar.addView(back)

        val home = Button(this).apply {
            text = "الرئيسية"
            isAllCaps = false
            setOnClickListener { goHome() }
        }
        bar.addView(home)

        content.addView(
            bar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8
            }
        )

        if (
            title != "CENTRAL MARKET" &&
            title != "الدخول" &&
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
            setPadding(4, 4, 4, 10)
        }

        val input = EditText(this).apply {
            hint = "ابحث في السوق"
            textSize = 14f
            setSingleLine(true)
            setTextColor(darkText)
            setHintTextColor(gray)
            inputType = InputType.TYPE_CLASS_TEXT
            background = roundedBackground(white, 12)
            setPadding(12, 8, 12, 8)
        }

        val button = Button(this).apply {
            text = "بحث"
            isAllCaps = false
            setOnClickListener {
                val query = input.text.toString().trim()

                if (query.isNotEmpty()) {
                    openPage { showSearch(query) }
                } else {
                    showMessage("اكتب كلمة للبحث أولًا")
                }
            }
        }

        row.addView(
            input,
            LinearLayout.LayoutParams(0, 48, 1f)
        )
        row.addView(button)

        content.addView(
            row,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun addSectionTitle(
        parent: LinearLayout,
        title: String
    ) {
        val view = TextView(this).apply {
            text = title
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(gold)
            setPadding(4, 18, 4, 8)
        }
        parent.addView(view)
    }

    private fun addInfo(
        parent: LinearLayout,
        message: String
    ) {
        val view = TextView(this).apply {
            text = message
            textSize = 14f
            setTextColor(darkText)
            setPadding(14, 12, 14, 12)
            background = roundedBackground(light, 12)
        }

        parent.addView(
            view,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 10
            }
        )
    }

    private fun addActionButton(
        parent: LinearLayout,
        label: String,
        color: Int,
        action: () -> Unit
    ) {
        val button = Button(this).apply {
            text = label
            setTextColor(white)
            textSize = 15f
            isAllCaps = false
            isClickable = true
            isFocusable = true
            background = roundedBackground(color, 14)
            setOnClickListener { action() }
        }

        parent.addView(
            button,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                54
            ).apply {
                bottomMargin = 10
            }
        )
    }

    private fun addCard(
        parent: LinearLayout,
        title: String,
        description: String,
        action: () -> Unit
    ) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 14, 16, 14)
            background = cardBackground()
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }

        val heading = TextView(this).apply {
            text = title
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(gold)
        }

        val detail = TextView(this).apply {
            text = description
            textSize = 13f
            setTextColor(white)
            setPadding(0, 6, 0, 0)
        }

        card.addView(heading)
        card.addView(detail)

        parent.addView(
            card,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 10
            }
        )
    }

    private fun addStatus(
        parent: LinearLayout,
        title: String,
        value: String
    ) {
        addCard(parent, title, value) {}
    }

    private fun addVisualBanner(
        parent: LinearLayout,
        title: String,
        subtitle: String
    ) {
        val banner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(18, 24, 18, 24)
            background = gradientBackground()
        }

        val heading = TextView(this).apply {
            text = title
            textSize = 25f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(gold)
            gravity = Gravity.CENTER
        }

        val detail = TextView(this).apply {
            text = subtitle
            textSize = 14f
            setTextColor(white)
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 0)
        }

        banner.addView(heading)
        banner.addView(detail)

        parent.addView(
            banner,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 14
            }
        )
    }

        private fun showGuestMode() {
        guestMode = true
        simulationLoggedIn = false
        currentRole = UserRole.USER
        currentAccountName = "زائر"

        val root = baseLayout(
            "التصفح العام",
            "تصفح المنتجات والأسعار — دون شراء"
        )

        addVisualBanner(
            root,
            "CENTRAL MARKET",
            "وضع الزائر"
        )

        addInfo(
            root,
            "يمكنك تصفح الفئات العامة فقط. " +
                "الشراء والمعاملات المالية غير متاحة."
        )

        addCard(
            root,
            "الأسواق والمنتجات",
            "عرض فئات المنتجات"
        ) {
            openPage { showProducts() }
        }

        addActionButton(
            root,
            "العودة إلى تسجيل الدخول",
            gold
        ) {
            showSimulationLogin()
        }
    }

    private fun showSecurity() {
        if (!simulationLoggedIn) {
            showSimulationLogin()
            return
        }

        val root = baseLayout(
            "الأمان",
            "حالة الحماية والجلسة"
        )

        addVisualBanner(
            root,
            "مركز الأمان",
            "حماية الجلسة التجريبية"
        )

        addInfo(
            root,
            "حماية لقطات الشاشة مفعّلة عبر FLAG_SECURE."
        )

        addInfo(
            root,
            "المصادقة الحيوية الفعلية غير مدمجة بعد."
        )

        addInfo(
            root,
            "تُقفل الجلسة وفق مهلة الخمول المحددة."
        )

        addActionButton(
            root,
            "قفل الجلسة الآن",
            red
        ) {
            lockAccount()
        }
    }

    private fun showHome() {
        if (!simulationLoggedIn) {
            if (guestMode) {
                showGuestMode()
            } else {
                showSimulationLogin()
            }
            return
        }

        pageHistory.clear()
        currentPage = { showHome() }

        val root = baseLayout(
            "الرئيسية",
            "بوابتك إلى الخدمات العامة الذكية"
        )

        addVisualBanner(
            root,
            "CENTRAL MARKET",
            "السوق المركزي — خدمات وأسواق ذكية"
        )

        addStatus(
            root,
            "حالة الاتصال",
            if (isOnline()) {
                "متصل بالإنترنت"
            } else {
                "غير متصل — بعض الخدمات محدودة"
            }
        )

        addSectionTitle(root, "الأسواق والخدمات")

        addCard(
            root,
            "الأسواق والمنتجات",
            "تصفح فئات المنتجات المتاحة"
        ) {
            openPage { showProducts() }
        }

        addCard(
            root,
            "الخدمات العامة",
            "خدمات ومعلومات مساعدة"
        ) {
            openPage { showServices() }
        }

        addCard(
            root,
            "البوابة المصرفية التجريبية",
            "واجهة توضيحية — لا توجد معاملات مالية حقيقية"
        ) {
            openPage { showBankingPortal() }
        }

        addCard(
            root,
            "الإعلانات",
            "مساحة إعلانات تجريبية"
        ) {
            openPage { showAds() }
        }

        addCard(
            root,
            "المطبخ",
            "قسم المطبخ وخدمات الطعام"
        ) {
            openPage { showKitchen() }
        }

        addCard(
            root,
            "الصدقات والمساعدات",
            "معلومات توعوية عن العمل الخيري"
        ) {
            openPage { showCharity() }
        }

        addCard(
            root,
            "الاتصال",
            "حالة الشبكة ومعلومات الاتصال"
        ) {
            openPage { showConnectivity() }
        }

        addCard(
            root,
            "الحساب",
            "إدارة الحساب التجريبي"
        ) {
            openPage { showAccountHome() }
        }

        addCard(
            root,
            "الأمان",
            "معلومات الأمان والجلسة"
        ) {
            openPage { showSecurity() }
        }
    }

    private fun showAccountHome() {
        if (!simulationLoggedIn) {
            showSimulationLogin()
            return
        }

        val root = baseLayout(
            "حسابي",
            "مرحبًا، $currentAccountName"
        )

        addVisualBanner(
            root,
            "الحساب التجريبي",
            "الدور: ${roleName(currentRole)}"
        )

        addCard(
            root,
            "بيانات الحساب",
            "عرض بيانات الحساب الحالية"
        ) {
            openPage { showAccount() }
        }

        addCard(
            root,
            "المفضلة",
            "العناصر المحفوظة في هذه الجلسة"
        ) {
            openPage { showFavorites() }
        }

        addCard(
            root,
            "النقاط",
            "عرض نقاط الحساب التجريبي"
        ) {
            openPage { showPoints() }
        }

        if (isPrivileged()) {
            addCard(
                root,
                "المكتب الإداري",
                "الأدوات الإدارية التجريبية"
            ) {
                openPage { showManagerOffice() }
            }
        }

        addActionButton(
            root,
            "تسجيل الخروج",
            red
        ) {
            logoutSimulation()
        }
    }

    private fun showProducts() {
        val root = baseLayout(
            "الأسواق",
            "اختر فئة لتصفح المنتجات"
        )

        val categories = listOf(
            "المركبات والشاحنات",
            "الهواتف والإلكترونيات",
            "المطاعم والتوصيل",
            "الزراعة والثروة الحيوانية",
            "الأسماك",
            "البناء ومواد الجملة",
            "الصحة",
            "الرياضة",
            "الكهرباء والمياه",
            "التعليم",
            "السفر"
        )

        categories.forEach { category ->
            addCard(
                root,
                category,
                "تصفح الفئة — البيانات الفعلية تحتاج إلى مصدر موثوق"
            ) {
                openPage { showCategory(category) }
            }
        }

        addInfo(
            root,
            "هذه نسخة تجريبية. لا تعرض أسعارًا حقيقية مرتبطة " +
                "بقاعدة بيانات، ولا تنفذ عمليات شراء أو دفع."
        )
    }

    private fun showCategory(category: String) {
        val root = baseLayout(
            category,
            "التصفح العام"
        )

        addVisualBanner(
            root,
            category,
            "قسم المنتجات"
        )

        addInfo(
            root,
            "لا توجد منتجات أو أسعار فعلية مرتبطة بقاعدة بيانات " +
                "في هذه النسخة."
        )

        addActionButton(
            root,
            "حفظ في المفضلة التجريبية",
            blue
        ) {
            showMessage(
                "ميزة المفضلة تحتاج إلى تخزين دائم."
            )
        }

        addActionButton(
            root,
            "العودة إلى الأسواق",
            navy
        ) {
            showProducts()
        }

        if (guestMode) {
            addActionButton(
                root,
                "الانتقال إلى تسجيل الدخول",
                gold
            ) {
                showSimulationLogin()
            }
        }
    }

        private fun showServices() {
        if (!simulationLoggedIn) {
            showSimulationLogin()
            return
        }

        val root = baseLayout(
            "الخدمات",
            "الخدمات العامة التجريبية"
        )

        addCard(
            root,
            "الذكاء البشري",
            "المعرفة والخبرة البشرية"
        ) {
            openPage { showHumanIntelligence() }
        }

        addCard(
            root,
            "CTM AI",
            "واجهة مساعد ذكي تجريبية"
        ) {
            openPage { showCtmAi() }
        }

        addCard(root, "الصحة", "معلومات عامة") {
            openPage {
                showMessage(
                    "قسم الصحة تجريبي وغير مرتبط بمقدم خدمة."
                )
            }
        }

        addCard(root, "التعليم", "معلومات تعليمية") {
            openPage { showMessage("قسم التعليم تجريبي.") }
        }

        addCard(root, "المرافق", "الكهرباء والمياه") {
            openPage { showMessage("قسم المرافق تجريبي.") }
        }

        addCard(root, "السفر", "معلومات السفر") {
            openPage { showMessage("قسم السفر تجريبي.") }
        }

        addCard(
            root,
            "العمل الخيري",
            "معلومات عن الصدقات والمساعدات"
        ) {
            openPage { showCharity() }
        }

        addCard(
            root,
            "Pocket",
            "قسم الأدوات الشخصية"
        ) {
            openPage { showBadger() }
        }

        addCard(
            root,
            "الرسائل",
            "واجهة رسائل تجريبية"
        ) {
            openPage {
                showMessage(
                    "الرسائل غير متصلة بخدمة مراسلة فعلية."
                )
            }
        }
    }

    private fun showHumanIntelligence() {
        val root = baseLayout(
            "الذكاء البشري",
            "المعرفة والخبرة البشرية"
        )

        addVisualBanner(
            root,
            "Human Intelligence",
            "الخبرة البشرية والتفكير المسؤول"
        )

        addInfo(
            root,
            "هذا القسم تعريفي ولا يرسل بياناتك إلى خدمة خارجية."
        )
    }

    private fun showCtmAi() {
        val root = baseLayout(
            "CTM AI",
            "واجهة مساعد الذكاء الاصطناعي"
        )

        addVisualBanner(
            root,
            "CTM AI",
            "المساعد الذكي الرسمي للمشروع — واجهة تجريبية"
        )

        addInfo(
            root,
            "هذه الشاشة لا تتصل حاليًا بنموذج ذكاء اصطناعي " +
                "أو خادم خارجي."
        )
    }

    private fun showBadger() {
        val root = baseLayout(
            "Pocket",
            "الأدوات الشخصية"
        )

        addVisualBanner(
            root,
            "Pocket",
            "الهوية الكحلية والذهبية والفضية"
        )

        addCard(
            root,
            "لوحة Pocket",
            "فتح الأدوات التجريبية"
        ) {
            openPage { showBadgerDashboard() }
        }
    }

    private fun showBadgerDashboard() {
        val root = baseLayout(
            "لوحة Pocket",
            "الأدوات المتاحة في النسخة التجريبية"
        )

        addInfo(
            root,
            "الأدوات المصرفية والاستثمارية لا تنفذ معاملات حقيقية."
        )

        addCard(
            root,
            "القسم الحساس",
            "واجهة محمية بقفل الجلسة التجريبي"
        ) {
            openPage { showSensitiveInvestment() }
        }
    }

    private fun showSensitiveInvestment() {
        if (!simulationLoggedIn || sensitiveLocked) {
            showSensitiveLock()
            return
        }

        val root = baseLayout(
            "القسم الحساس",
            "معلومات تجريبية"
        )

        addInfo(
            root,
            "لا تدخل بيانات مصرفية حقيقية. هذا القسم لا يتصل ببنك."
        )

        addActionButton(
            root,
            "قفل القسم",
            red
        ) {
            lockSensitiveSection()
        }
    }

    private fun showSensitiveLock() {
        sensitiveLocked = true

        val root = baseLayout(
            "القفل الحساس",
            "القسم مقفل"
        )

        addVisualBanner(
            root,
            "القسم الحساس",
            "يتطلب إعادة تحقق"
        )

        addInfo(
            root,
            "القفل هنا تجريبي. لا توجد مصادقة حيوية فعلية " +
                "مدمجة في هذا الملف."
        )

        addActionButton(
            root,
            "العودة إلى حسابي",
            blue
        ) {
            if (simulationLoggedIn) {
                showAccountHome()
            } else {
                showSimulationLogin()
            }
        }
    }

    private fun lockSensitiveSection() {
        sensitiveLocked = true
        showSensitiveLock()
    }

    private fun lockAccount() {
        handler.removeCallbacks(globalLockRunnable)
        handler.removeCallbacks(sensitiveLockRunnable)

        if (simulationLoggedIn) {
            simulationLoggedIn = false
            guestMode = false
            sensitiveLocked = true
            currentAccountName = "زائر"
            currentRole = UserRole.USER
            pageHistory.clear()
            currentPage = null
            showSessionLock()
        } else {
            showSimulationLogin()
        }
    }

    private fun showSessionLock() {
        val root = baseLayout(
            "قفل الجلسة",
            "انتهت الجلسة التجريبية"
        )

        addInfo(
            root,
            "سجّل الدخول مرة أخرى لمتابعة استخدام التطبيق."
        )

        addActionButton(
            root,
            "العودة إلى الدخول",
            gold
        ) {
            showSimulationLogin()
        }
    }

    private fun clearSession() {
        handler.removeCallbacks(globalLockRunnable)
        handler.removeCallbacks(sensitiveLockRunnable)

        simulationLoggedIn = false
        guestMode = false
        sensitiveLocked = true
        currentRole = UserRole.USER
        currentAccountName = "زائر"
        pageHistory.clear()
        currentPage = null
    }

    private fun logoutSimulation() {
        clearSession()
        showSimulationLogin()
    }

        private fun showAccount() {
        if (!simulationLoggedIn) {
            showSimulationLogin()
            return
        }

        val root = baseLayout(
            "بيانات الحساب",
            "معلومات الحساب التجريبي"
        )

        addInfo(root, "اسم الحساب: $currentAccountName")
        addInfo(root, "نوع الحساب: ${roleName(currentRole)}")
        addInfo(root, "حالة الجلسة: نشطة تجريبيًا")

        addActionButton(
            root,
            "العودة إلى حسابي",
            blue
        ) {
            showAccountHome()
        }
    }

    private fun showUserAccount() {
        if (!simulationLoggedIn) {
            showSimulationLogin()
            return
        }

        currentRole = UserRole.USER
        showAccount()
    }

    private fun showAdminAccount() {
        showMessage(
            "حساب الإدارة غير مفعّل؛ يلزم تحقق حقيقي من خادم آمن."
        )
    }

    private fun showPartnerAccount() {
        showMessage(
            "حساب الشريك غير مفعّل؛ يلزم تحقق حقيقي من خادم آمن."
        )
    }

    private fun showOwnerAccount() {
        showMessage(
            "حساب المالك غير مفعّل؛ يلزم تحقق حقيقي من خادم آمن."
        )
    }

    private fun roleName(role: UserRole): String {
        return when (role) {
            UserRole.USER -> "مستخدم"
            UserRole.ADMIN -> "مدير النظام"
            UserRole.PARTNER -> "شريك"
            UserRole.OWNER -> "المالك"
        }
    }

    private fun showManagerOffice() {
        if (!isPrivileged()) {
            showMessage("لا تملك صلاحية دخول المكتب الإداري.")
            return
        }

        val root = baseLayout(
            "المكتب الإداري",
            "أدوات إدارية تجريبية"
        )

        addCard(
            root,
            "أدوات الإدارة",
            "إجراءات محاكاة"
        ) {
            openPage { showManagerTools() }
        }

        addCard(
            root,
            "التشخيص",
            "عرض معلومات التطبيق"
        ) {
            openPage { showDiagnostics() }
        }

        addCard(
            root,
            "ملخص النظام",
            "ملخص الحالة الحالية"
        ) {
            openPage { showSystemSummary() }
        }
    }

    private fun showManagerTools() {
        if (!isPrivileged()) {
            showMessage("غير مصرح بالدخول.")
            return
        }

        val root = baseLayout(
            "أدوات الإدارة",
            "نسخة تجريبية"
        )

        addInfo(
            root,
            "لا تنفذ هذه الأدوات تغييرات على خادم أو حسابات حقيقية."
        )

        addActionButton(
            root,
            "عرض التشخيص",
            blue
        ) {
            openPage { showDiagnostics() }
        }

        addActionButton(
            root,
            "عرض ملخص النظام",
            navy
        ) {
            openPage { showSystemSummary() }
        }
    }

    private fun isPrivileged(): Boolean {
        return simulationLoggedIn &&
            (
                currentRole == UserRole.ADMIN ||
                currentRole == UserRole.OWNER ||
                currentRole == UserRole.PARTNER
            )
    }

    private fun showDiagnostics() {
        val root = baseLayout(
            "التشخيص",
            "معلومات تشغيل محلية"
        )

        addInfo(root, "إصدار أندرويد: ${Build.VERSION.RELEASE}")
        addInfo(root, "مستوى API: ${Build.VERSION.SDK_INT}")
        addInfo(
            root,
            "حالة الشبكة: ${if (isOnline()) "متصل" else "غير متصل"}"
        )
        addInfo(root, "الجلسة التجريبية: $simulationLoggedIn")
        addInfo(root, "القسم الحساس مقفل: $sensitiveLocked")
    }

    private fun showSystemSummary() {
        val root = baseLayout(
            "ملخص النظام",
            "حالة التطبيق التجريبي"
        )

        addInfo(root, "اسم التطبيق: CENTRAL MARKET")
        addInfo(root, "الدور الحالي: ${roleName(currentRole)}")
        addInfo(root, "الجلسة نشطة: $simulationLoggedIn")
        addInfo(root, "الوضع العام: $guestMode")
        addInfo(
            root,
            "الاتصال: ${if (isOnline()) "متاح" else "غير متاح"}"
        )
    }

    private fun showPartnerOffice() {
        if (!isPrivileged()) {
            showMessage("لا توجد صلاحية دخول.")
            return
        }

        val root = baseLayout(
            "مكتب الشريك",
            "واجهة تجريبية"
        )

        addInfo(
            root,
            "إدارة الشركاء الفعلية تحتاج إلى صلاحيات خادم موثوقة."
        )
    }

    private fun showOwnerOffice() {
        if (!isPrivileged()) {
            showMessage("لا توجد صلاحية دخول.")
            return
        }

        val root = baseLayout(
            "مكتب المالك",
            "واجهة تجريبية"
        )

        addInfo(
            root,
            "هذه الشاشة ليست نظام صلاحيات إنتاجيًا، " +
                "ولا تثبت ملكية الحساب."
        )
    }

    private fun showPartnerManagement() {
        if (!isPrivileged()) {
            showMessage("لا توجد صلاحية إدارة.")
            return
        }

        val root = baseLayout(
            "إدارة الشركاء",
            "واجهة تجريبية"
        )

        addInfo(
            root,
            "إدارة حسابات الشركاء الحقيقية تحتاج إلى قاعدة بيانات وخادم."
        )
    }

    private fun showOwnerApproval() {
        if (!isPrivileged()) {
            showMessage("لا توجد صلاحية اعتماد.")
            return
        }

        val root = baseLayout(
            "اعتماد المالك",
            "واجهة تجريبية"
        )

        addInfo(
            root,
            "لا يتم اعتماد أي حساب أو تغيير صلاحيات حقيقية من هذه الشاشة."
        )
    }

    private fun showBankingPortal() {
        val root = baseLayout(
            "البوابة المصرفية",
            "واجهة توضيحية فقط"
        )

        addVisualBanner(
            root,
            "BANKING PORTAL",
            "الخدمات المالية التجريبية"
        )

        addInfo(
            root,
            "لا تدخل كلمات مرور مصرفية أو أرقام بطاقات. " +
                "هذه النسخة لا تتصل ببنك ولا تنفذ تحويلات."
        )

        addActionButton(
            root,
            "العودة",
            blue
        ) {
            goHome()
        }
    }

    private fun showDocuments() {
        val root = baseLayout(
            "المستندات",
            "قسم المستندات التجريبي"
        )

        addInfo(
            root,
            "رفع المستندات والتحقق منها يحتاجان إلى خدمة آمنة."
        )
    }

    private fun showSearch() {
        showSearch("")
    }

    private fun showSearch(initialQuery: String) {
        val root = baseLayout(
            "البحث",
            "ابحث في الأقسام المتاحة"
        )

        val input = EditText(this).apply {
            hint = "أدخل عبارة البحث"
            setText(initialQuery)
            setTextColor(darkText)
            setHintTextColor(gray)
            setSingleLine(true)
            inputType = InputType.TYPE_CLASS_TEXT
            background = roundedBackground(white, 12)
            setPadding(12, 10, 12, 10)
        }

        root.addView(
            input,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                52
            )
        )

        addActionButton(
            root,
            "بحث في الفئات",
            blue
        ) {
            val query = input.text.toString().trim()

            if (query.isBlank()) {
                showMessage("أدخل كلمة للبحث")
                return@addActionButton
            }

            val categories = listOf(
                "المركبات والشاحنات",
                "الهواتف والإلكترونيات",
                "المطاعم والتوصيل",
                "الزراعة والثروة الحيوانية",
                "الأسماك",
                "البناء ومواد الجملة",
                "الصحة",
                "الرياضة",
                "الكهرباء والمياه",
                "التعليم",
                "السفر"
            )

            val matches = categories.filter {
                it.contains(query, ignoreCase = true)
            }

            if (matches.isEmpty()) {
                addInfo(
                    root,
                    "لا توجد فئة مطابقة في القائمة التجريبية."
                )
            } else {
                matches.forEach { category ->
                    addCard(root, category, "فتح الفئة") {
                        openPage { showCategory(category) }
                    }
                }
            }
        }

        focusAndShowKeyboard(input)
    }

    private fun showFavorites() {
        val root = baseLayout(
            "المفضلة",
            "العناصر المحفوظة"
        )

        addInfo(
            root,
            "المفضلة غير مرتبطة بتخزين دائم في هذه النسخة."
        )
    }

    private fun showPoints() {
        val root = baseLayout(
            "النقاط",
            "رصيد النقاط التجريبي"
        )

        addInfo(
            root,
            "لا توجد نقاط فعلية أو مكافآت مالية في هذه النسخة."
        )
    }

        private fun showAds() {
        if (!simulationLoggedIn) {
            showSimulationLogin()
            return
        }

        val root = baseLayout(
            "الإعلانات",
            "منصة الإعلانات التجارية"
        )

        addVisualBanner(
            root,
            "CENTRAL ADS",
            "مساحة إعلانية منظمة وقابلة للتطوير"
        )

        addCard(
            root,
            "BRONZE",
            "الفئة الأساسية للإعلانات"
        ) {
            showMessage(
                "فئة BRONZE تجريبية، ولا توجد إعلانات مدفوعة فعليًا."
            )
        }

        addCard(
            root,
            "SILVER",
            "الفئة المتوسطة للإعلانات"
        ) {
            showMessage("فئة SILVER تجريبية.")
        }

        addCard(
            root,
            "GOLD",
            "الفئة المميزة للإعلانات"
        ) {
            showMessage("فئة GOLD تجريبية.")
        }

        addInfo(
            root,
            "لا توجد عمليات دفع أو نشر إعلانات فعلية في هذه النسخة."
        )
    }

    private fun showKitchen() {
        if (!simulationLoggedIn) {
            showSimulationLogin()
            return
        }

        val root = baseLayout(
            "المطبخ",
            "خدمات الطعام والمطبخ"
        )

        addVisualBanner(
            root,
            "CENTRAL KITCHEN",
            "تنظيم خدمات الطعام والطلبات"
        )

        addCard(
            root,
            "قائمة الطعام",
            "عرض الأصناف والأسعار عند ربط قاعدة البيانات"
        ) {
            showMessage(
                "قائمة الطعام الفعلية غير متصلة بقاعدة بيانات."
            )
        }

        addCard(
            root,
            "الطلبات",
            "واجهة تجريبية لمتابعة الطلبات"
        ) {
            showMessage(
                "الطلبات غير متصلة بخدمة تشغيل فعلية."
            )
        }

        addInfo(
            root,
            "هذه الواجهة لا تنفذ طلبات شراء أو دفع حقيقية."
        )
    }

    private fun showCharity() {
        if (!simulationLoggedIn) {
            showSimulationLogin()
            return
        }

        val root = baseLayout(
            "الصدقات والمساعدات",
            "التكافل والعمل الخيري"
        )

        addVisualBanner(
            root,
            "CHARITY",
            "مبادرات خيرية لخدمة المجتمع"
        )

        addCard(
            root,
            "مساعدة المحتاجين",
            "قسم توعوي للمبادرات والمساعدات"
        ) {
            showMessage(
                "تسجيل الحالات والتحقق منها يحتاجان إلى نظام موثوق."
            )
        }

        addCard(
            root,
            "كفالة الأيتام",
            "واجهة تعريفية لمبادرات رعاية الأيتام"
        ) {
            showMessage(
                "لا توجد حاليًا سجلات أو عمليات كفالة فعلية."
            )
        }

        addCard(
            root,
            "الزكاة والصدقات",
            "معلومات تنظيمية وتوعوية"
        ) {
            showMessage(
                "يجب التحقق من الأحكام الشرعية والجهات المستفيدة " +
                    "قبل إطلاق أي خدمة مالية."
            )
        }

        addInfo(
            root,
            "لا يستقبل التطبيق تبرعات أو أموالًا حقيقية في هذه النسخة."
        )
    }

    private fun showConnectivity() {
        val root = baseLayout(
            "الاتصال",
            "حالة الشبكة على الجهاز"
        )

        val connected = isOnline()

        addVisualBanner(
            root,
            "NETWORK STATUS",
            if (connected) {
                "الجهاز متصل بالشبكة"
            } else {
                "لا يوجد اتصال متاح بالإنترنت"
            }
        )

        addInfo(
            root,
            "حالة الاتصال: " +
                if (connected) "متصل" else "غير متصل"
        )

        addInfo(
            root,
            "إصدار أندرويد: ${Build.VERSION.RELEASE}"
        )

        addInfo(
            root,
            "مستوى API: ${Build.VERSION.SDK_INT}"
        )

        addActionButton(
            root,
            "تحديث حالة الاتصال",
            blue
        ) {
            showConnectivity()
        }
    }

    private fun showMessage(message: String) {
        val root = baseLayout(
            "تنبيه",
            "CENTRAL MARKET"
        )

        addVisualBanner(
            root,
            "معلومات",
            "رسالة النظام"
        )

        addInfo(root, message)

        addActionButton(
            root,
            "العودة",
            blue
        ) {
            goHome()
        }
    }

    private fun isOnline(): Boolean {
        return try {
            val manager = getSystemService(
                CONNECTIVITY_SERVICE
            ) as ConnectivityManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = manager.activeNetwork
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
            } else {
                @Suppress("DEPRECATION")
                val networkInfo = manager.activeNetworkInfo

                @Suppress("DEPRECATION")
                networkInfo?.isConnected == true
            }
        } catch (_: SecurityException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    private fun gradientBackground(): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                officeDark,
                navy,
                badgerBlack
            )
        ).apply {
            cornerRadius = 0f
        }
    }

    private fun cardBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 18f
            setColor(officeDark)
            setStroke(1, gold)
        }
    }

    private fun roundedBackground(
        color: Int,
        radius: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius.toFloat()
            setColor(color)
        }
    }
}
