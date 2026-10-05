package com.central.market

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.text.InputType
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class MainActivity : Activity() {

    private val navy = Color.rgb(18, 42, 66)
    private val blue = Color.rgb(32, 104, 170)
    private val gold = Color.rgb(205, 157, 45)
    private val green = Color.rgb(45, 150, 95)
    private val red = Color.rgb(190, 65, 65)
    private val white = Color.WHITE
    private val light = Color.rgb(245, 247, 250)

    private lateinit var content: LinearLayout

    private val handler = Handler(Looper.getMainLooper())

    private enum class UserRole {
        USER, ADMIN, PARTNER, OWNER
    }

    private var currentRole = UserRole.USER
    private var currentAccount = "زائر"
    private var simulationLoggedIn = false

    private var lastBackgroundTime = 0L
    private val sessionTimeout = 10 * 60 * 1000L
    private val sensitiveTimeout = 3 * 60 * 1000L

    private var sensitiveLocked = false
    private var sensitiveRunnable: Runnable? = null

    private var sessionRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        showHome()
    }

    override fun onPause() {
        super.onPause()

        lastBackgroundTime = SystemClock.elapsedRealtime()

        sessionRunnable?.let { handler.removeCallbacks(it) }

        sessionRunnable = Runnable {
            if (SystemClock.elapsedRealtime() - lastBackgroundTime >= sessionTimeout) {
                lockAccount()
            }
        }

        handler.postDelayed(sessionRunnable!!, sessionTimeout)
    }

    override fun onResume() {
        super.onResume()

        sessionRunnable?.let { handler.removeCallbacks(it) }

        if (lastBackgroundTime > 0L &&
            SystemClock.elapsedRealtime() - lastBackgroundTime >= sessionTimeout
        ) {
            lockAccount()
        }

        lastBackgroundTime = 0L
    }

    private fun baseLayout(title: String): ScrollView {
        val scroll = ScrollView(this)
        scroll.setBackgroundColor(light)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(16, 12, 16, 24)

        val top = LinearLayout(this)
        top.orientation = LinearLayout.HORIZONTAL
        top.gravity = Gravity.CENTER_VERTICAL
        top.setPadding(4, 4, 4, 10)

        val back = addNavButton("↩️")
        back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val titleView = TextView(this)
        titleView.text = title
        titleView.setTextColor(navy)
        titleView.textSize = 18f
        titleView.setTypeface(null, Typeface.BOLD)
        titleView.gravity = Gravity.CENTER
        top.addView(
            back,
            LinearLayout.LayoutParams(52, 52)
        )

        top.addView(
            titleView,
            LinearLayout.LayoutParams(0, 56, 1f)
        )

        val home = addNavButton("🏠")
        home.setOnClickListener { showHome() }

        top.addView(
            home,
            LinearLayout.LayoutParams(52, 52)
        )

        val exit = addNavButton("🚪")
        exit.setOnClickListener { finish() }

        top.addView(
            exit,
            LinearLayout.LayoutParams(52, 52)
        )

        root.addView(top)

        val brand = TextView(this)
        brand.text = "CENTRAL MARKET"
        brand.setTextColor(blue)
        brand.textSize = 25f
        brand.setTypeface(null, Typeface.BOLD)
        brand.gravity = Gravity.CENTER
        brand.setPadding(0, 4, 0, 14)
        root.addView(brand)

        val subtitle = TextView(this)
        subtitle.text = title
        subtitle.setTextColor(Color.DKGRAY)
        subtitle.textSize = 13f
        subtitle.gravity = Gravity.CENTER
        subtitle.setPadding(0, 0, 0, 12)
        root.addView(subtitle)

        content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.setPadding(0, 4, 0, 8)
        root.addView(
            content,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        scroll.addView(root)
        return scroll
    }

    private fun addSection(text: String) {
        val title = TextView(this)
        title.text = text
        title.setTextColor(navy)
        title.textSize = 20f
        title.setTypeface(null, Typeface.BOLD)
        title.setPadding(4, 14, 4, 8)
        content.addView(title)
    }

    private fun addInfo(title: String, body: String) {
        val box = TextView(this)
        box.text = "$title\n$body"
        box.setTextColor(navy)
        box.textSize = 14f
        box.setPadding(16, 14, 16, 14)
        box.background = cardBackground()
        content.addView(
            box,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 6, 0, 6)
            }
        )
    }

    private fun addCard(
        symbol: String,
        title: String,
        description: String,
        action: () -> Unit
    ) {
        val button = Button(this)
        button.text = "$symbol  $title\n$description"
        button.textSize = 14f
        button.setTextColor(navy)
        button.gravity = Gravity.CENTER_VERTICAL
        button.isAllCaps = false
        button.setPadding(18, 14, 18, 14)
        button.background = cardBackground()
        button.setOnClickListener { action() }

        content.addView(
            button,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 6, 0, 6)
            }
        )
    }

    private fun addStatus(text: String, positive: Boolean) {
        val status = TextView(this)
        status.text = text
        status.textSize = 14f
        status.setTypeface(null, Typeface.BOLD)
        status.setTextColor(if (positive) green else red)
        status.setPadding(12, 10, 12, 10)
        content.addView(status)
    }

    private fun addNavButton(symbol: String): Button {
        val button = Button(this)
        button.text = symbol
        button.textSize = 18f
        button.isAllCaps = false
        button.setTextColor(navy)
        button.background = roundedBackground(white, gold, 2f, 16f)
        return button
    }

    private fun roundedBackground(
        fill: Int,
        stroke: Int,
        strokeWidth: Float,
        radius: Float
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(fill)
            setStroke(strokeWidth.toInt(), stroke)
            cornerRadius = radius
        }
    }

    private fun cardBackground(): GradientDrawable {
        return roundedBackground(white, Color.LTGRAY, 1f, 18f)
    }

    private fun gradientBackground(): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(navy, blue)
        ).apply {
            cornerRadius = 22f
        }
    }

    private fun showScreen(view: ScrollView) {
        setContentView(view)
    }

    private fun isOnline(): Boolean {
        val manager = getSystemService(CONNECTIVITY_SERVICE)
            as ConnectivityManager

        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network)
            ?: return false

        return capabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET
        )
    }

    private fun isPrivileged(): Boolean {
        return currentRole == UserRole.ADMIN ||
                currentRole == UserRole.PARTNER ||
                currentRole == UserRole.OWNER
    }

    private fun roleName(): String {
        return when (currentRole) {
            UserRole.USER -> "مستخدم"
            UserRole.ADMIN -> "إدارة"
            UserRole.PARTNER -> "شريك"
            UserRole.OWNER -> "مالك"
        }
    }

    package com.central.market

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.text.InputType

class MainActivity : Activity() {

    // =========================================================
    // CENTRAL MARKET — Core configuration
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

    private lateinit var content: LinearLayout

    private val handler = Handler(Looper.getMainLooper())

    // =========================================================
    // Roles and session
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
    // Page navigation
    // =========================================================

    private val pageHistory = ArrayList<() -> Unit>()

    private var currentPage: (() -> Unit)? = null
    private var ignoreHistoryOnce = false

    // =========================================================
    // Activity lifecycle
    // =========================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // حماية أساسية للتطبيق والشاشات الحساسة
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        showHome()
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
            val elapsed =
                SystemClock.elapsedRealtime() - lastBackgroundTime

            if (elapsed >= globalTimeout) {
                lockAccount()
            }
        }

        lastBackgroundTime = 0L
    }

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

    // =========================================================
    // Navigation engine
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
            val previous = pageHistory.removeAt(pageHistory.lastIndex)

            ignoreHistoryOnce = true
            previous.invoke()
            ignoreHistoryOnce = false
        } else {
            goHome()
        }
    }

    // =========================================================
    // Base screen
    // =========================================================

    private fun baseLayout(
        title: String,
        subtitle: String = ""
    ) {
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 12, 18, 20)
            setBackgroundColor(light)
        }

        val scroll = ScrollView(this).apply {
            addView(content)
        }

        setContentView(scroll)

        addTopNavigation(title)

        if (subtitle.isNotBlank()) {
            val sub = TextView(this).apply {
                text = subtitle
                textSize = 13f
                setTextColor(gray)
                setPadding(4, 2, 4, 12)
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
    // Top navigation
    // =========================================================

    private fun addTopNavigation(title: String) {

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(4, 4, 4, 10)
            background = roundedBackground(navy, 18)
        }

        val back = Button(this).apply {
            text = "←"
            textSize = 20f
            setTextColor(white)
            background = roundedBackground(blue, 14)

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
            textSize = 20f
            setTextColor(white)
            background = roundedBackground(blue, 14)

            setOnClickListener {
                goHome()
            }
        }

        val exit = Button(this).apply {
            text = "×"
            textSize = 20f
            setTextColor(white)
            background = roundedBackground(red, 14)

            setOnClickListener {
                exitPage()
            }
        }

        bar.addView(
            back,
            LinearLayout.LayoutParams(52, 48)
        )

        bar.addView(
            titleView,
            LinearLayout.LayoutParams(
                0,
                48,
                1f
            )
        )

        bar.addView(
            home,
            LinearLayout.LayoutParams(52, 48)
        )

        bar.addView(
            exit,
            LinearLayout.LayoutParams(52, 48)
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
    // Common UI helpers
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
            textSize = 25f
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
            LinearLayout.LayoutParams(48, 64)
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

        private fun showPasswordGate(
        role: UserRole,
        accountName: String
    ) {
        baseLayout(
            "تسجيل الدخول",
            "تحقق كلمة المرور"
        )

        addInfo(
            "الحساب",
            "$accountName — ${role.name}"
        )

        val password = EditText(this).apply {
            hint = "كلمة المرور"
            inputType =
                InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_PASSWORD
            setPadding(14, 12, 14, 12)
            background = cardBackground()
        }

        content.addView(
            password,
            LinearLayout.LayoutParams(
                -1,
                56
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        )

        addInfo(
            "بيانات المحاكاة",
            when (role) {
                UserRole.ADMIN -> "ADMIN-DEMO"
                UserRole.PARTNER -> "PARTNER-DEMO"
                UserRole.OWNER -> "OWNER-DEMO"
                UserRole.USER -> "دخول عام"
            }
        )

        addCard(
            "✓",
            "تحقق ودخول",
            "التحقق من كلمة المرور المحاكاة."
        ) {
            val entered = password.text.toString()

            val correct = when (role) {
                UserRole.ADMIN -> "ADMIN-DEMO"
                UserRole.PARTNER -> "PARTNER-DEMO"
                UserRole.OWNER -> "OWNER-DEMO"
                UserRole.USER -> ""
            }

            if (entered == correct) {
                loginSimulation(role, accountName)
            } else {
                Toast.makeText(
                    this,
                    "كلمة المرور غير صحيحة",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        addCard(
            "←",
            "العودة",
            "العودة إلى اختيار الحساب."
        ) {
            openPage { showSimulationLogin() }
        }
    }

    private fun loginSimulation(
        role: UserRole,
        accountName: String
    ) {
        currentRole = role
        currentAccountName = accountName
        simulationLoggedIn = true
        sensitiveLocked = false

        handler.removeCallbacks(globalLockRunnable)
        handler.removeCallbacks(sensitiveLockRunnable)

        showAccountHome()
    }

    // =========================================================
    // Account home
    // =========================================================

    private fun showAccountHome() {
        openPage {
            baseLayout(
                "الحساب",
                "الدور الحالي: ${roleName()}"
            )

            addInfo(
                "الحساب الحالي",
                "$currentAccountName\nالدور: ${roleName()}"
            )

            when (currentRole) {
                UserRole.USER -> showUserAccount()
                UserRole.ADMIN -> showAdminAccount()
                UserRole.PARTNER -> showPartnerAccount()
                UserRole.OWNER -> showOwnerAccount()
            }

            addSection("إدارة الجلسة")

            addCard(
                "🔒",
                "قفل الحساب",
                "إغلاق الجلسة مؤقتًا وإخفاء محتوى الحساب."
            ) {
                lockAccount()
            }

            addCard(
                "⇥",
                "تسجيل الخروج",
                "إنهاء الحساب الحالي والعودة إلى الدخول."
            ) {
                logoutSimulation()
            }
        }
    }

    private fun showUserAccount() {
        addSection("حساب المستخدم")

        addCard(
            "★",
            "النقاط",
            "عرض نظام النقاط والمزايا."
        ) {
            openPage { showPoints() }
        }

        addCard(
            "♡",
            "المفضلة",
            "العناصر المحفوظة للمستخدم."
        ) {
            openPage { showFavorites() }
        }

        addCard(
            "▣",
            "المستندات",
            "منطقة معلومات المستندات."
        ) {
            openPage { showDocuments() }
        }
    }

    private fun showAdminAccount() {
        addSection("مكتب الإدارة")

        addCard(
            "A",
            "أدوات الإدارة",
            "أدوات الإدارة والصلاحيات المسموحة."
        ) {
            openPage { showManagerTools() }
        }

        addCard(
            "⌁",
            "التشخيص",
            "فحص حالة التطبيق والمكونات."
        ) {
            openPage { showDiagnostics() }
        }

        addCard(
            "▣",
            "المستندات",
            "المستندات الإدارية المتاحة."
        ) {
            openPage { showDocuments() }
        }
    }

    private fun showPartnerAccount() {
        addSection("مكتب الشريك")

        addCard(
            "P",
            "مكتب الشريك",
            "بيانات الشراكة والصلاحيات."
        ) {
            openPage { showPartnerOffice() }
        }

        addCard(
            "◎",
            "إدارة الشراكات",
            "عرض أدوات إدارة الشركاء."
        ) {
            openPage { showPartnerManagement() }
        }

        addCard(
            "Σ",
            "ملخص النظام",
            "عرض ملخص تشغيلي غير مالي."
        ) {
            openPage { showSystemSummary() }
        }
    }

    private fun showOwnerAccount() {
        addSection("مكتب المالك")

        addCard(
            "O",
            "مكتب المالك",
            "إدارة ومراجعة إعدادات المنصة."
        ) {
            openPage { showOwnerOffice() }
        }

        addCard(
            "✓",
            "اعتمادات المالك",
            "مراجعة الاعتمادات التي تتطلب موافقة المالك."
        ) {
            openPage { showOwnerApproval() }
        }

        addCard(
            "Σ",
            "ملخص النظام",
            "ملخص شامل لحالة المكونات."
        ) {
            openPage { showSystemSummary() }
        }

        addCard(
            "⌁",
            "التشخيص",
            "فحص حالة التطبيق."
        ) {
            openPage { showDiagnostics() }
        }
    }

    // =========================================================
    // Products
    // =========================================================

    private fun showProducts() {
        baseLayout(
            "الأسواق والمنتجات",
            "تصنيفات CENTRAL MARKET"
        )

        val categories = listOf(
            "السيارات والشاحنات",
            "الهواتف والإلكترونيات",
            "المطاعم والتوصيل",
            "الزراعة والثروة الحيوانية",
            "الأسماك",
            "البناء والجملة",
            "الصحة",
            "الرياضة",
            "الكهرباء والمياه",
            "التعليم",
            "السفر"
        )

        categories.forEach { name ->
            addCard(
                "◆",
                name,
                "فتح القسم ومعلوماته."
            ) {
                openPage { showCategory(name) }
            }
        }
    }

    private fun showCategory(name: String) {
        baseLayout(
            name,
            "قسم من أقسام السوق"
        )

        addInfo(
            "القسم",
            name
        )

        addInfo(
            "حالة القسم",
            "متاح للعرض والتنظيم داخل النسخة الحالية."
        )

        addInfo(
            "المعاملات",
            "لا يتم تنفيذ معاملات مالية حقيقية من هذه النسخة."
        )

        addCard(
            "⌕",
            "البحث داخل القسم",
            "البحث عن العناصر المتاحة."
        ) {
            openPage { showSearch() }
        }

        addCard(
            "♡",
            "إضافة للمفضلة",
            "حفظ القسم ضمن المفضلة."
        ) {
            Toast.makeText(
                this,
                "تمت إضافة القسم للمفضلة",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // =========================================================
    // Services
    // =========================================================

    private fun showServices() {
        baseLayout(
            "الخدمات",
            "خدمات المنصة"
        )

        addCard(
            "◆",
            "الخدمات العامة",
            "الخدمات والمعلومات العامة."
        ) {
            openPage { showSearch() }
        }

        addCard(
            "K",
            "المطبخ",
            "خدمات ومعلومات المطبخ."
        ) {
            openPage { showKitchen() }
        }

        addCard(
            "♥",
            "الدعم المجتمعي",
            "معلومات الدعم والمساعدة المجتمعية."
        ) {
            openPage { showCharity() }
        }

        addCard(
            "◎",
            "الذكاء البشري",
            "المعرفة والخبرات البشرية."
        ) {
            openPage { showHumanIntelligence() }
        }

        addCard(
            "AI",
            "CTM AI",
            "مساعد معلوماتي للتطبيق."
        ) {
            openPage { showCtmAi() }
        }

        addCard(
            "B",
            "BADGER",
            "منظومة معلومات مالية محمية."
        ) {
            openPage { showBadger() }
        }
    }

    private fun showHumanIntelligence() {
        baseLayout(
            "الذكاء البشري",
            "المعرفة والخبرة المنظمة"
        )

        addInfo(
            "الهدف",
            "تنظيم المعرفة والخبرات البشرية بطريقة مفيدة وقابلة للمراجعة."
        )

        addInfo(
            "الزكاة البشرية",
            "مفهوم لدعم المجتمع بالمعرفة والخبرة والوقت وفق الضوابط المناسبة."
        )

        addInfo(
            "الحدود",
            "المعلومات لا تُعد بديلًا عن الجهات المختصة أو الاستشارة المهنية."
        )
    }

    private fun showCtmAi() {
        baseLayout(
            "CTM AI",
            "المساعد الرسمي للمعلومات"
        )

        addInfo(
            "وظيفة المساعد",
            "قراءة والبحث في المعلومات المسموح بها داخل التطبيق."
        )

        addInfo(
            "التحكم",
            "يمكن إيقاف المساعد أو تعطيله عند ظهور خطأ أو خطر."
        )

        addInfo(
            "الحدود",
            "لا ينفذ معاملات مالية حقيقية ولا يتجاوز صلاحيات المستخدم."
        )
    }

        // =========================================================
    // BADGER
    // =========================================================

    private fun showBadger() {
        baseLayout(
            "BADGER",
            "منظومة مالية معلوماتية مستقلة"
        )

        addInfo(
            "الحالة",
            "نسخة محاكاة معلوماتية. لا توجد معاملات مالية حقيقية."
        )

        addStatus(
            "الحماية",
            "مفعلة",
            green
        )

        addCard(
            "B",
            "لوحة BADGER",
            "عرض المؤشرات والمعلومات المحاكاة."
        ) {
            openPage { showBadgerDashboard() }
        }

        addCard(
            "▲",
            "المشاركة الحساسة",
            "معلومات المشاركة الاستثمارية والتمويلية."
        ) {
            openPage { showSensitiveInvestment() }
        }

        addInfo(
            "القفل التلقائي",
            "الأقسام الحساسة والمدفوعة تستخدم مهلة حماية قصيرة."
        )
    }

    private fun showBadgerDashboard() {
        baseLayout(
            "لوحة BADGER",
            "معلومات محاكاة"
        )

        addStatus(
            "الحساب",
            if (simulationLoggedIn) "مسجل" else "غير مسجل",
            if (simulationLoggedIn) green else red
        )

        addInfo(
            "التحليلات",
            "يمكن عرض مؤشرات الإيداع والاستثمار بصورة معلوماتية فقط."
        )

        addInfo(
            "العمولات",
            "يجب أن تكون أي عمولات مستقبلية موثقة تعاقديًا وبصورة قانونية."
        )

        addInfo(
            "التنفيذ المالي",
            "لا يتم تنفيذ تحويل أو استثمار أو سحب حقيقي من هذه النسخة."
        )
    }

    private fun showSensitiveInvestment() {
        if (!simulationLoggedIn) {
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
            "الخدمة الحساسة",
            "حماية إضافية للمشاركة الاستثمارية والتمويلية"
        )

        addStatus(
            "حماية الشاشة",
            "مفعلة",
            green
        )

        addStatus(
            "مهلة القفل",
            "3 دقائق",
            green
        )

        addInfo(
            "التحقق الرسمي",
            "عند تنفيذ النظام الحقيقي يجب التحقق من أهلية المشترك " +
                    "عبر الجهات الحكومية المخولة وبالطرق والتفويضات القانونية المناسبة."
        )

        addInfo(
            "الموانع القانونية",
            "يجب التعامل مع أي مانع قانوني موثق وفق القوانين والجهات المختصة، " +
                    "دون الاعتماد على بيانات غير رسمية."
        )

        addInfo(
            "حماية البيانات",
            "البيانات الحساسة لا ينبغي تخزينها بصورة مكشوفة داخل APK، " +
                    "بل خلف نظام خادم وصلاحيات آمنة في النظام الحقيقي."
        )

        addInfo(
            "تنبيه",
            "هذه الشاشة معلوماتية في النسخة الحالية ولا تنفذ مشاركة مالية حقيقية."
        )

        addCard(
            "🔒",
            "قفل الشاشة الحساسة",
            "إغلاق الوصول إلى هذه الشاشة فورًا."
        ) {
            sensitiveLocked = true
            handler.removeCallbacks(sensitiveLockRunnable)
            showSensitiveLock()
        }
    }

    private fun showSensitiveLock() {
        handler.removeCallbacks(sensitiveLockRunnable)

        baseLayout(
            "الشاشة الحساسة مقفلة",
            "تحتاج إلى إعادة التحقق"
        )

        addStatus(
            "الحالة",
            "مقفلة",
            red
        )

        addInfo(
            "السبب",
            "تم قفل الشاشة للحماية بعد انتهاء المهلة أو بطلب المستخدم."
        )

        addCard(
            "↻",
            "إعادة التحقق",
            "العودة إلى بوابة الحساب."
        ) {
            showSimulationLogin()
        }

        addCard(
            "⌂",
            "الرئيسية",
            "العودة إلى الصفحة الرئيسية."
        ) {
            goHome()
        }
    }

    // =========================================================
    // Manager / administration offices
    // =========================================================

    private fun showManagerTools() {
        baseLayout(
            "أدوات الإدارة",
            "صلاحيات الإدارة"
        )

        addInfo(
            "الدور",
            "هذه المنطقة مخصصة للإدارة المخولة."
        )

        addStatus(
            "الحماية الحساسة",
            "مفعلة",
            green
        )

        addCard(
            "⌁",
            "التشخيص",
            "فحص حالة التطبيق والمكونات."
        ) {
            openPage { showDiagnostics() }
        }

        addCard(
            "Σ",
            "ملخص النظام",
            "عرض حالة المكونات الرئيسية."
        ) {
            openPage { showSystemSummary() }
        }
    }

    private fun showDiagnostics() {
        baseLayout(
            "التشخيص",
            "فحص داخلي غير تنفيذي"
        )

        addStatus(
            "التطبيق",
            "يعمل",
            green
        )

        addStatus(
            "الشبكة",
            if (isOnline()) "متاحة" else "غير متاحة",
            if (isOnline()) green else red
        )

        addStatus(
            "حماية الشاشة",
            "FLAG_SECURE",
            green
        )

        addStatus(
            "الجلسة",
            if (simulationLoggedIn) "نشطة" else "غير نشطة",
            if (simulationLoggedIn) green else red
        )

        addInfo(
            "ملاحظة",
            "التشخيص الحالي لا يتصل بأنظمة حكومية أو مالية حقيقية."
        )
    }

    private fun showPartnerOffice() {
        baseLayout(
            "مكتب الشريك",
            "صلاحيات الشريك"
        )

        addInfo(
            "الوصول",
            "هذه المنطقة متاحة لدور الشريك المصرح له فقط."
        )

        addInfo(
            "المشاركة",
            "يمكن مستقبلًا ربط بيانات الشراكة بنظام خادم آمن."
        )

        addCard(
            "Σ",
            "ملخص النظام",
            "عرض المعلومات التشغيلية."
        ) {
            openPage { showSystemSummary() }
        }
    }

    private fun showSystemSummary() {
        baseLayout(
            "ملخص النظام",
            "حالة المكونات الرئيسية"
        )

        addStatus(
            "الاتصال",
            if (isOnline()) "متصل" else "غير متصل",
            if (isOnline()) green else red
        )

        addStatus(
            "الجلسة",
            if (simulationLoggedIn) "نشطة" else "مغلقة",
            if (simulationLoggedIn) green else red
        )

        addStatus(
            "الحماية",
            "مفعلة",
            green
        )

        addStatus(
            "التكاملات الحكومية",
            "غير مفعلة في هذه النسخة",
            red
        )

        addStatus(
            "التنفيذ المالي",
            "غير متاح",
            red
        )

        addInfo(
            "حالة الحماية الحساسة",
            "حماية الشاشة والقفل الزمني مفعّلان. " +
                    "التحقق الحكومي والمالي الحقيقي يتطلبان تكاملًا رسميًا وصلاحيات موثقة."
        )
    }

    private fun showOwnerOffice() {
        baseLayout(
            "مكتب المالك",
            "إدارة واعتماد المنصة"
        )

        addInfo(
            "صلاحية المالك",
            "صلاحيات أعلى مستوى داخل النموذج الحالي."
        )

        addCard(
            "✓",
            "اعتمادات المالك",
            "مراجعة الإجراءات التي تحتاج اعتمادًا."
        ) {
            openPage { showOwnerApproval() }
        }

        addCard(
            "Σ",
            "ملخص النظام",
            "حالة النظام والتكاملات."
        ) {
            openPage { showSystemSummary() }
        }
    }

    private fun showPartnerManagement() {
        baseLayout(
            "إدارة الشراكات",
            "معلومات الشركاء"
        )

        addInfo(
            "الإدارة",
            "هذه الصفحة مخصصة لإدارة بيانات الشراكة وفق الصلاحيات."
        )

        addInfo(
            "البيانات",
            "البيانات الحساسة يجب أن تحفظ في خادم آمن، لا داخل APK."
        )

        addStatus(
            "التنفيذ المالي",
            "غير متاح",
            red
        )
    }

    private fun showOwnerApproval() {
        baseLayout(
            "اعتمادات المالك",
            "مراجعة الإجراءات"
        )

        addInfo(
            "القاعدة",
            "الإجراءات الحساسة أو التغييرات الجوهرية تحتاج اعتماد المالك."
        )

        addInfo(
            "التحقق",
            "أي تكامل حكومي أو مالي حقيقي يحتاج إجراءات رسمية وتفويضات مناسبة."
        )

        addStatus(
            "حالة الاعتماد",
            "لا توجد عملية تنفيذ حقيقية",
            green
        )
    }

    private fun showDocuments() {
        baseLayout(
            "المستندات",
            "منطقة المستندات والمعلومات"
        )

        addInfo(
            "المستندات",
            "هذه النسخة تعرض هيكل منطقة المستندات فقط."
        )

        addInfo(
            "الخصوصية",
            "المستندات الحساسة يجب أن تكون محمية بصلاحيات مناسبة عند تنفيذ النظام الحقيقي."
        )

        addStatus(
            "التخزين المحلي الحساس",
            "غير مستخدم للتنفيذ المالي",
            green
        )
    }

    // =========================================================
    // Search / favorites / points
    // =========================================================

    private fun showSearch() {
        baseLayout(
            "البحث",
            "البحث داخل المنصة"
        )

        val search = EditText(this).apply {
            hint = "اكتب كلمة البحث"
            setSingleLine(true)
            setPadding(14, 12, 14, 12)
            background = cardBackground()
        }

        content.addView(
            search,
            LinearLayout.LayoutParams(
                -1,
                56
            ).apply {
                setMargins(0, 6, 0, 10)
            }
        )

        addCard(
            "⌕",
            "تنفيذ البحث",
            "البحث المعلوماتي داخل النسخة الحالية."
        ) {
            Toast.makeText(
                this,
                "تم تجهيز البحث: ${search.text}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showFavorites() {
        baseLayout(
            "المفضلة",
            "العناصر المحفوظة"
        )

        addInfo(
            "المفضلة",
            "يمكن إضافة الأقسام والعناصر المفضلة من صفحات السوق."
        )
    }

    private fun showPoints() {
        baseLayout(
            "النقاط",
            "نظام النقاط"
        )

        addInfo(
            "الرصيد المعلوماتي",
            "0 نقطة في النسخة الحالية."
        )

        addInfo(
            "الغرض",
            "يمكن استخدام النقاط مستقبلًا ضمن نظام مزايا واضح وموثق."
        )
    }

    // =========================================================
    // Kitchen / charity / connectivity
    // =========================================================

    private fun showKitchen() {
        baseLayout(
            "المطبخ",
            "خدمات ومعلومات المطبخ"
        )

        addInfo(
            "المطبخ",
            "قسم مستقل داخل الخدمات."
        )

        addInfo(
            "الخدمات",
            "يمكن تطويره لاحقًا ليضم المنتجات والطلبات والمعلومات المناسبة."
        )
    }

    private fun showCharity() {
        baseLayout(
            "الدعم المجتمعي",
            "الأيتام والمحتاجون"
        )

        addInfo(
            "الهدف",
            "تنظيم مبادرات الدعم المجتمعي بصورة موثقة."
        )

        addInfo(
            "الضوابط",
            "أي تبرعات أو أموال حقيقية تحتاج نظامًا قانونيًا ومحاسبيًا مناسبًا."
        )
    }

    private fun showConnectivity() {
        baseLayout(
            "اختبار الاتصال",
            "حالة اتصال الجهاز"
        )

        val online = isOnline()

        addStatus(
            "حالة الشبكة",
            if (online) "متصل" else "غير متصل",
            if (online) green else red
        )

        addInfo(
            "الملاحظة",
            if (online) {
                "الجهاز لديه اتصال بالشبكة حاليًا."
            } else {
                "لا يوجد اتصال متاح حاليًا."
            }
        )
    }

    // =========================================================
    // Security / privacy / rules
    // =========================================================

    private fun showSecurity() {
        baseLayout(
            "الأمان",
            "حماية المستخدم والتطبيق"
        )

        addStatus(
            "حماية الشاشة",
            "FLAG_SECURE مفعلة",
            green
        )

        addStatus(
            "قفل الجلسة",
            "10 دقائق",
            green
        )

        addStatus(
            "الأقسام الحساسة",
            "3 دقائق",
            green
        )

        addInfo(
            "المبدأ",
            "الصلاحيات تختلف حسب الدور، والمناطق الإدارية لا تظهر للمستخدم العادي."
        )

        addCard(
            "▣",
            "الخصوصية",
            "عرض مبادئ الخصوصية."
        ) {
            openPage { showPrivacy() }
        }

        addCard(
            "✓",
            "القواعد",
            "عرض قواعد استخدام المنصة."
        ) {
            openPage { showRules() }
        }
    }

    private fun showPrivacy() {
        baseLayout(
            "الخصوصية",
            "حماية البيانات"
        )

        addInfo(
            "المبدأ",
            "يجب تقليل البيانات المطلوبة واستخدامها للغرض المعلن فقط."
        )

        addInfo(
            "البيانات الحساسة",
            "لا ينبغي وضع البيانات الحساسة أو أسرار الحسابات داخل APK بصورة مكشوفة."
        )

        addInfo(
            "النظام الحقيقي",
            "يحتاج خادمًا آمنًا، صلاحيات، تسجيل عمليات، وحماية مناسبة للبيانات."
        )
    }

    private fun showRules() {
        baseLayout(
            "قواعد المنصة",
            "الاستخدام المسؤول"
        )

        addInfo(
            "1",
            "لا تستخدم المنصة في أي نشاط غير قانوني."
        )

        addInfo(
            "2",
            "المعلومات المالية في هذه النسخة محاكاة ولا تمثل تنفيذًا ماليًا حقيقيًا."
        )

        addInfo(
            "3",
            "أي تحقق حكومي مستقبلي يجب أن يتم عبر جهة مخولة وبإجراءات قانونية."
        )

        addInfo(
            "4",
            "صلاحيات الإدارة والشريك والمالك لا تمنح للمستخدم العادي."
        )
    }

    // =========================================================
    // Session control
    // =========================================================

    private fun lockAccount() {
        handler.removeCallbacks(globalLockRunnable)
        handler.removeCallbacks(sensitiveLockRunnable)

        simulationLoggedIn = false
        sensitiveLocked = true

        showSessionLock()
    }

    private fun showSessionLock() {
        baseLayout(
            "الجلسة مقفلة",
            "تحتاج إلى تسجيل الدخول من جديد"
        )

        addStatus(
            "الحالة",
            "مقفلة",
            red
        )

        addInfo(
            "السبب",
            "انتهت مهلة الجلسة أو تم قفل الحساب يدويًا."
        )

        addCard(
            "↻",
            "تسجيل الدخول",
            "إعادة فتح الحساب."
        ) {
            showSimulationLogin()
        }

        addCard(
            "⌂",
            "الرئيسية",
            "العودة إلى الصفحة الرئيسية."
        ) {
            goHome()
        }
    }

    private fun logoutSimulation() {
        handler.removeCallbacks(globalLockRunnable)
        handler.removeCallbacks(sensitiveLockRunnable)

        simulationLoggedIn = false
        sensitiveLocked = false
        currentRole = UserRole.USER
        currentAccountName = "زائر"

        pageHistory.clear()
        currentPage = null

        showSimulationLogin()
    }

    // =========================================================
    // Activity cleanup
    // =========================================================

    override fun onDestroy() {
        handler.removeCallbacks(globalLockRunnable)
        handler.removeCallbacks(sensitiveLockRunnable)
        super.onDestroy()
    }

    // =========================================================
    // Drawable helpers
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
            setStroke(1, Color.rgb(220, 225, 230))
            cornerRadius = 20f
        }
    }
}
