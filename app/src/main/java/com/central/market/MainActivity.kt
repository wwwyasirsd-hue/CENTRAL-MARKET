package com.central.market

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var content: LinearLayout

    private val navy = Color.rgb(18, 42, 66)
    private val blue = Color.rgb(32, 104, 170)
    private val gold = Color.rgb(205, 157, 45)
    private val green = Color.rgb(38, 130, 85)
    private val red = Color.rgb(180, 65, 65)
    private val background = Color.rgb(246, 249, 252)
    private val white = Color.WHITE
    private val textDark = Color.rgb(30, 43, 55)
    private val muted = Color.rgb(92, 108, 122)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun baseLayout(): LinearLayout {

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(background)

        val header = LinearLayout(this)
        header.orientation = LinearLayout.VERTICAL
        header.gravity = Gravity.CENTER
        header.setPadding(16, 18, 16, 10)
        header.setBackgroundColor(white)

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
        content.orientation = LinearLayout.VERTICAL
        content.setPadding(16, 8, 16, 20)

        val scroll = ScrollView(this)
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
        navigation.orientation = LinearLayout.HORIZONTAL
        navigation.setBackgroundColor(white)

        addNavButton(navigation, "⌂\nالرئيسية") {
            showHome()
        }

        addNavButton(navigation, "⌕\nالبحث") {
            showSearch()
        }

        addNavButton(navigation, "♡\nالمفضلة") {
            showFavorites()
        }

        addNavButton(navigation, "●\nالحساب") {
            showAccount()
        }

        root.addView(navigation)

        return root
    }

    private fun addNavButton(
        parent: LinearLayout,
        title: String,
        action: () -> Unit
    ) {
        val button = Button(this)
        button.text = title
        button.textSize = 10f
        button.setTextColor(navy)
        button.setOnClickListener {
            action()
        }

        parent.addView(
            button,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )
    }

    private fun addSection(title: String) {
        val text = TextView(this)
        text.text = title
        text.textSize = 20f
        text.setTypeface(null, Typeface.BOLD)
        text.setTextColor(navy)
        text.setPadding(4, 18, 4, 9)
        content.addView(text)
    }

    private fun addCard(
        title: String,
        description: String,
        action: () -> Unit
    ) {
        val button = Button(this)
        button.text = "$title\n$description"
        button.textSize = 14f
        button.setTextColor(textDark)
        button.gravity = Gravity.CENTER_VERTICAL
        button.setPadding(14, 18, 14, 18)

        button.background = roundedBackground(
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
                setMargins(0, 5, 0, 5)
            }
        )
    }

    private fun roundedBackground(
        fill: Int,
        stroke: Int,
        radius: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(fill)
            cornerRadius = radius.toFloat()
            setStroke(2, stroke)
        }
    }

    private fun addInfo(textValue: String) {
        val text = TextView(this)
        text.text = textValue
        text.textSize = 16f
        text.setTextColor(textDark)
        text.setPadding(10, 10, 10, 18)
        content.addView(text)
    }

    private fun addInfo(
        title: String,
        description: String
    ) {
        val text = TextView(this)
        text.text = "$title\n$description"
        text.textSize = 16f
        text.setTextColor(textDark)
        text.setPadding(10, 10, 10, 18)
        content.addView(text)
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showHome() {

        setContentView(baseLayout())
        content.removeAllViews()

        addSection("مرحبًا بك في CENTRAL MARKET")

        val intro = TextView(this)
        intro.text =
            "منصة واحدة .. عالم من الفرص.\n\n" +
            "أسواق وخدمات ومركبات وإعلانات ومشاريع وابتكار."
        intro.textSize = 16f
        intro.setTextColor(muted)
        intro.gravity = Gravity.CENTER
        intro.setPadding(5, 5, 5, 18)
        content.addView(intro)

        addSection("🌐 الاتصال والتجربة")

        addCard(
            "🌐 مركز التجربة عبر الإنترنت",
            "فحص اتصال الجهاز والخدمات المتصلة"
        ) {
            showOnline()
        }

        addSection("⚡ الوصول السريع")

        addCard(
            "👤 وضع الزائر",
            "تصفح الخدمات دون تسجيل"
        ) {
            showGuestMode()
        }

        addCard(
            "🛡️ الأمان والخصوصية",
            "معلومات الحماية والصلاحيات"
        ) {
            showSafety()
        }

        addCard(
            "📦 المنتجات والخدمات",
            "استعراض العروض"
        ) {
            showProducts()
        }

        addSection("🏪 الأقسام الرئيسية")

        addCard(
            "🚛 المركبات والشاحنات",
            "مركبات وشاحنات ومعدات"
        ) {
            showCategory(
                "🚛 المركبات والشاحنات",
                "مركبات وشاحنات ومعدات"
            )
        }

        addCard(
            "📱 الهواتف والإلكترونيات",
            "هواتف وأجهزة وإلكترونيات"
        ) {
            showCategory(
                "📱 الهواتف والإلكترونيات",
                "هواتف وأجهزة وإلكترونيات"
            )
        }

        addCard(
            "🍽️ المطاعم والتوصيل",
            "مطاعم وطلبات وتوصيل"
        ) {
            showCategory(
                "🍽️ المطاعم والتوصيل",
                "مطاعم وطلبات وتوصيل"
            )
        }

        addCard(
            "📢 التسويق والإعلانات",
            "تسويق وإعلانات وعروض"
        ) {
            showAds()
        }

        addCard(
            "🛠️ الخدمات",
            "خدمات للأفراد والشركات"
        ) {
            showCategory(
                "🛠️ الخدمات",
                "خدمات متنوعة للأفراد والشركات"
            )
        }

        addCard(
            "🌾 الزراعة والثروة الحيوانية",
            "محاصيل ومواشي ومعدات"
        ) {
            showCategory(
                "🌾 الزراعة والثروة الحيوانية",
                "محاصيل ومواشي ومعدات"
            )
        }

        addCard(
            "🐟 الثروة السمكية",
            "أسماك ومعدات وخدمات"
        ) {
            showCategory(
                "🐟 الثروة السمكية",
                "أسماك ومعدات وخدمات"
            )
        }

        addCard(
            "🏗️ مواد البناء والجملة",
            "مواد البناء وتجارة الجملة"
        ) {
            showCategory(
                "🏗️ مواد البناء والجملة",
                "مواد بناء وتجارة الجملة"
            )
        }

        addCard(
            "🏥 الصحة",
            "عيادات ومختبرات وصيدليات"
        ) {
            showCategory(
                "🏥 الصحة",
                "خدمات صحية"
            )
        }

        addCard(
            "🏋️ الرياضة والملاعب",
            "صالات وملاعب"
        ) {
            showCategory(
                "🏋️ الرياضة والملاعب",
                "خدمات رياضية"
            )
        }

        addCard(
            "⚡ الكهرباء والمياه",
            "خدمات الكهرباء والمياه"
        ) {
            showCategory(
                "⚡ الكهرباء والمياه",
                "الخدمات الأساسية"
            )
        }

        addCard(
            "🏫 التعليم",
            "مدارس وخدمات تعليمية"
        ) {
            showCategory(
                "🏫 التعليم",
                "خدمات تعليمية"
            )
        }

        addCard(
            "✈️ السفر والتذاكر",
            "سفر وحجوزات وتذاكر"
        ) {
            showCategory(
                "✈️ السفر والتذاكر",
                "السفر والحجوزات والتذاكر"
            )
        }

        addCard(
            "⭐ النقاط والمكافآت",
            "نظام النقاط والمكافآت"
        ) {
            showPoints()
        }

        addSection("💡 المشاريع والمجتمع")

        addCard(
            "🧠 الذكاء البشري",
            "أفكار وابتكارات ومشاريع"
        ) {
            showHumanIntelligence()
        }

        addCard(
            "🤖 CTM AI",
            "المساعد الذكي الرسمي للمشروع"
        ) {
            showCtmAi()
        }

        addCard(
            "🤲 صندوق دعم الأيتام والمحتاجين",
            "مبادرات الدعم المجتمعي"
        ) {
            showCharity()
        }

        addCard(
            "🍲 مطبخ الطيبات",
            "وصفات ومعلومات غذائية"
        ) {
            showKitchen()
        }

        addSection("🏢 الإدارة والمنتجات المستقبلية")

        addCard(
            "👔 مكتب المدير",
            "الإدارة والوثائق والتقارير"
        ) {
            showManagerOffice()
        }

        addCard(
            "🦡 BADGER",
            "منظومة مصرفية مستقلة"
        ) {
            showBadger()
        }

        addCard(
            "🛡️ مكتب الأمن والمعلومات",
            "الحماية ومكافحة السرقة والاحتيال"
        ) {
            showSecurityOffice()
        }

        addCard(
            "⚖️ مكتب النائب العام للمشروع",
            "الملفات القانونية والجهات والشراكات"
        ) {
            showAttorneyOffice()
        }

        addCard(
            "💰 النظام المالي الخاص",
            "متابعة الأسهم والمعاملات والدخل"
        ) {
            showPrivateFinancialSystem()
        }
    }

    private fun showOnline() {

        setContentView(baseLayout())
        content.removeAllViews()

        addSection("🌐 مركز التجربة عبر الإنترنت")

        val status = TextView(this)
        status.textSize = 17f
        status.setTextColor(textDark)
        status.setPadding(12, 16, 12, 20)
        content.addView(status)

        fun checkConnection() {

            val manager =
                getSystemService(CONNECTIVITY_SERVICE)
                        as ConnectivityManager

            val network = manager.activeNetwork

            val capabilities =
                manager.getNetworkCapabilities(network)

            val online =
                capabilities?.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                ) == true

            if (online) {

                status.text =
                    "● متصل بالإنترنت\n\n" +
                    "اتصال الإنترنت متاح على الجهاز.\n\n" +
                    "الربط الحقيقي بالخدمات السحابية يتم في مرحلة لاحقة."

                status.setTextColor(green)

            } else {

                status.text =
                    "● غير متصل بالإنترنت\n\n" +
                    "يمكن استخدام الوظائف المحلية المتاحة."

                status.setTextColor(red)
            }
        }

        checkConnection()

        addCard(
            "🔄 فحص الاتصال",
            "تحديث حالة الإنترنت"
        ) {
            checkConnection()
        }

        addCard(
            "📶 وضع البيانات المنخفضة",
            "تقليل استهلاك الإنترنت"
        ) {
            showMessage(
                "وضع البيانات المنخفضة قيد التطوير."
            )
        }

        addCard(
            "☁️ الخدمات السحابية",
            "قاعدة البيانات والخدمات المتصلة"
        ) {
            showMessage(
                "البنية السحابية ستُربط بعد تجهيز الخادم."
            )
        }

        addCard(
            "📱 تجربة التطبيق",
            "اختبار نسخة Android الحالية"
        ) {
            showMessage(
                "هذه نسخة Android المحلية الحالية."
            )
        }

        addCard(
            "🏠 العودة إلى الرئيسية",
            "الرجوع"
        ) {
            showHome()
        }
    }

    private fun showGuestMode() {

        setContentView(baseLayout())
        content.removeAllViews()

        addSection("👤 وضع الزائر")

        addInfo(
            "يمكن للزائر تصفح الأقسام والخدمات العامة " +
            "دون الوصول إلى الوظائف الخاصة."
        )

        addCard(
            "🔎 تصفح السوق",
            "مشاهدة المنتجات والخدمات"
        ) {
            showProducts()
        }

        addCard(
            "📢 مشاهدة الإعلانات",
            "التعرف على العروض"
        ) {
            showAds()
        }

        addCard(
            "🌐 التجربة عبر الإنترنت",
            "اختبار الاتصال"
        ) {
            showOnline()
        }

        addCard(
            "🔐 تسجيل الدخول",
            "الدخول إلى الحساب"
        ) {
            showLogin()
        }
    }

    private fun showSafety() {

        setContentView(baseLayout())
        content.removeAllViews()

        addSection("🛡️ الأمان والخصوصية")

        addInfo(
            "CENTRAL MARKET يميز بين الخدمات العامة " +
            "والأقسام الإدارية والخاصة."
        )

        addCard(
            "🔐 حماية الحساب",
            "إدارة الوصول إلى الحساب"
        ) {
            showMessage("حماية الحساب قيد التطوير.")
        }

        addCard(
            "🛡️ حماية البيانات",
            "تنظيم الوصول إلى المعلومات"
        ) {
            showMessage("حماية البيانات المتقدمة قيد التطوير.")
        }

        addCard(
            "🚨 مكافحة الاحتيال",
            "رصد السلوكيات غير المعتادة"
        ) {
            showMessage("نظام مكافحة الاحتيال قيد التطوير.")
        }

        addCard(
            "🔒 الأقسام الخاصة",
            "الوصول حسب الصلاحيات"
        ) {
            showMessage("الصلاحيات المتقدمة قيد التطوير.")
        }

        addCard(
            "🏠 الرئيسية",
            "العودة"
        ) {
            showHome()
        }
    }

    private fun showCategory(
        category: String,
        description: String
    ) {

        setContentView(baseLayout())
        content.removeAllViews()

        addSection(category)

        addInfo(description)

        addCard(
            "📦 المنتجات والعروض",
            "استعراض العناصر"
        ) {
            showProducts()
        }

        addCard(
            "➕ إضافة عرض",
            "إضافة منتج أو خدمة"
        ) {
            showAddAd()
        }

        addCard(
            "❤️ المفضلة",
            "العناصر المحفوظة"
        ) {
            showFavorites()
        }

        addCard(
            "🔎 البحث داخل القسم",
            "البحث في هذا القسم"
        ) {
            showSearch()
        }

        addSection("🔮 خدمات قادمة")

        addCard(
            "💳 الدفع الإلكتروني",
            "سيتم ربطه لاحقًا"
        ) {
            showMessage("الدفع الإلكتروني قيد التجهيز.")
        }

        addCard(
            "📍 الخرائط والتتبع",
            "الموقع والتتبع"
        ) {
            showMessage("الخرائط والتتبع قيد التجهيز.")
        }

        addCard(
            "🏠 العودة إلى الرئيسية",
            "الرجوع"
        ) {
            showHome()
        }
    }

    private fun showProducts() {

        setContentView(baseLayout())
        content.removeAllViews()

        addSection("📦 المنتجات والخدمات")

        addCard(
            "📱 هاتف ذكي",
            "منتجات إلكترونية وخدمات"
        ) {
            showDetails(
                "هاتف ذكي",
                "منتج تجريبي داخل CENTRAL MARKET."
            )
        }

        addCard(
            "🚗 مركبة",
            "سيارات ومركبات للنقل"
        ) {
            showDetails(
                "مركبة",
                "قسم المركبات قيد التطوير."
            )
        }

        addCard(
            "🚛 شاحنة ومعدات",
            "شاحنات ومعدات ثقيلة"
        ) {
            showDetails(
                "شاحنة ومعدات",
                "قسم الشاحنات والمعدات الثقيلة."
            )
        }

        addCard(
            "🍽️ مطعم",
            "مطاعم وتوصيل"
        ) {
            showDetails(
                "مطعم",
                "خدمات المطاعم والتوصيل."
            )
        }

        addCard(
            "🌾 منتجات زراعية",
            "محاصيل ومواشي ومنتجات زراعية"
        ) {
            showDetails(
                "منتجات زراعية",
                "القسم الزراعي قيد التطوير."
            )
        }

        addCard(
            "🏗️ مواد بناء",
            "مواد البناء والجملة"
        ) {
            showDetails(
                "مواد بناء",
                "قسم مواد البناء والجملة."
            )
        }

        addCard(
            "🏥 خدمات صحية",
            "مختبرات وعيادات وصيدليات"
        ) {
            showDetails(
                "خدمات صحية",
                "الخدمات الصحية قيد التطوير."
            )
        }

        addCard(
            "🎓 التعليم",
            "مدارس ودورات وخدمات تعليمية"
        ) {
            showDetails(
                "التعليم",
                "الخدمات التعليمية قيد التطوير."
            )
        }

        addCard(
            "🏠 الرئيسية",
            "العودة"
        ) {
            showHome()
        }
    }

    private fun showDetails(
        title: String,
        description: String
    ) {

        setContentView(baseLayout())
        content.removeAllViews()

        addSection("📋 تفاصيل")

        val details = TextView(this)

        details.text = """
            $title

            $description

            CENTRAL MARKET
            الرقم المرجعي سيتم إنشاؤه عند تنفيذ العملية.
        """.trimIndent()

        details.textSize = 17f
        details.setTextColor(textDark)
        details.setPadding(12, 15, 12, 20)

        content.addView(details)

        addCard(
            "❤️ إضافة إلى المفضلة",
            "حفظ العنصر"
        ) {
            showMessage("تم حفظ العنصر في المفضلة.")
        }

        addCard(
            "📤 مشاركة",
            "مشاركة معلومات العنصر"
        ) {
            showMessage("المشاركة قيد التجهيز.")
        }

        addCard(
            "🧾 الرقم المرجعي",
            "رقم خاص بالعملية"
        ) {
            showMessage(
                "سيتم إنشاء الرقم المرجعي عند تنفيذ العملية."
            )
        }

        addCard(
            "💳 الدفع الإلكتروني",
            "سيتم ربطه لاحقًا"
        ) {
            showMessage("الدفع الإلكتروني قيد التجهيز.")
        }

        addCard(
            "📍 الخرائط والتتبع",
            "الموقع والتتبع"
        ) {
            showMessage("الخرائط والتتبع قيد التجهيز.")
        }

        addCard(
            "🏠 العودة إلى المنتجات",
            "الرجوع"
        ) {
            showProducts()
        }
    }

    private fun showAddAd() {

        setContentView(baseLayout())
        content.removeAllViews()

        addSection("➕ إضافة إعلان")

        val name = EditText(this)
        name.hint = "اسم المنتج أو الخدمة"
        name.setTextColor(textDark)
        name.setHintTextColor(muted)
        content.addView(name)

        val description = EditText(this)
        description.hint = "وصف المنتج أو الخدمة"
        description.setTextColor(textDark)
        description.setHintTextColor(muted)
        content.addView(description)

        val price = EditText(this)
        price.hint = "السعر"
        price.inputType =
            android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        price.setTextColor(textDark)
        price.setHintTextColor(muted)
        content.addView(price)

        val city = EditText(this)
        city.hint = "المدينة"
        city.setTextColor(textDark)
        city.setHintTextColor(muted)
        content.addView(city)

        addSection("📢 مستوى الإعلان")

        addCard(
            "🥉 BRONZE",
            "الإعلان الأساسي"
        ) {
            showMessage("تم اختيار BRONZE.")
        }

        addCard(
            "🥈 SILVER",
            "ظهور أفضل للإعلان"
        ) {
            showMessage("تم اختيار SILVER.")
        }

        addCard(
            "🥇 GOLD",
            "ظهور مميز في أعلى النتائج"
        ) {
            showMessage("تم اختيار GOLD.")
        }

        addCard(
            "📢 نشر الإعلان",
            "إضافة الإعلان إلى السوق"
        ) {

            val productName =
                name.text.toString().trim()

            val productDescription =
                description.text.toString().trim()

            if (
                productName.isEmpty() ||
                productDescription.isEmpty()
            ) {
                showMessage(
                    "يرجى إدخال اسم المنتج والوصف."
                )
            } else {

                showMessage(
                    "تم تجهيز الإعلان للنشر.\n" +
                            "سيتم إنشاء رقم مرجعي خاص به."
                )
            }
        }

        addCard(
            "🏠 الرئيسية",
            "العودة"
        ) {
            showHome()
        }
    }

    private fun showSearch() {

        setContentView(baseLayout())
        content.removeAllViews()

        addSection("🔎 البحث")

        val search = EditText(this)
        search.hint = "اكتب ما تريد البحث عنه"
        search.setTextColor(textDark)
        search.setHintTextColor(muted)

        content.addView(search)

        addCard(
            "🔍 تنفيذ البحث",
            "البحث داخل CENTRAL MARKET"
        ) {

            val query =
                search.text.toString().trim()

            if (query.isEmpty()) {

                showMessage(
                    "اكتب كلمة للبحث."
                )

            } else {

                showMessage(
                    "نتائج البحث عن:\n$query\n\n" +
                            "سيتم ربط البحث الحقيقي بقاعدة البيانات لاحقًا."
                )
            }
        }

        addCard(
            "📍 البحث حسب المدينة",
            "تحديد النتائج حسب الموقع"
        ) {
            showMessage(
                "البحث حسب المدينة قيد التطوير."
            )
        }

        addCard(
            "🏷️ البحث حسب القسم",
            "تصفية النتائج"
        ) {
            showMessage(
                "التصفية حسب القسم قيد التطوير."
            )
        }

        addCard(
            "🏠 الرئيسية",
            "العودة"
        ) {
            showHome()
        }
    }

       private fun showFavorites() {

        setContentView(baseLayout())
        content.removeAllViews()

        addSection("❤️ المفضلة")

        addCard(
            "📦 العناصر المحفوظة",
            "عرض المنتجات والخدمات التي تم حفظها"
        ) {
            showMessage(
                "لا توجد عناصر محفوظة حاليًا."
            )
        }

        addCard(
            "🗑️ إدارة المفضلة",
            "حذف العناصر المحفوظة"
        ) {
            showMessage(
                "إدارة المفضلة سيتم ربطها ببيانات المستخدم لاحقًا."
            )
        }

        addCard(
            "🏠 الرئيسية",
            "العودة"
        ) {
            showHome()
        }
    }

    private fun showAccount() {

        setContentView(baseLayout())
        content.removeAllViews()

        addSection("● الحساب")

        addInfo(
            "حساب CENTRAL MARKET\n\n" +
                    "الوظائف الخاصة بالحساب ستعمل وفق نظام " +
                    "الصلاحيات والهوية عند ربط الخادم."
        )

        addCard(
            "🔐 تسجيل الدخول",
            "الدخول إلى الحساب"
        ) {
            showLogin()
        }

        addCard(
            "👤 الملف الشخصي",
            "معلومات الحساب"
        ) {
            showMessage(
                "الملف الشخصي قيد التطوير."
            )
        }

        addCard(
            "🔔 التنبيهات",
            "التنبيهات والإشعارات"
        ) {
            showMessage(
                "التنبيهات قيد التطوير."
            )
        }

        addCard(
            "🔒 الخصوصية والصلاحيات",
            "إدارة إعدادات الوصول"
        ) {
            showSafety()
        }

        addCard(
            "🏠 الرئيسية",
            "العودة"
        ) {
            showHome()
        }
    }

    private fun showLogin() {

        setContentView(baseLayout())
        content.removeAllViews()

        addSection("🔐 تسجيل الدخول")

        val phone = EditText(this)
        phone.hint = "رقم الهاتف أو البريد الإلكتروني"
        phone.setTextColor(textDark)
        phone.setHintTextColor(muted)
        content.addView(phone)

        val password = EditText(this)
        password.hint = "كلمة المرور"
        password.inputType =
            android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        password.setTextColor(textDark)
        password.setHintTextColor(muted)
        content.addView(password)

        addCard(
            "🔑 دخول",
            "تسجيل الدخول إلى الحساب"
        ) {

            if (
                phone.text.toString().trim().isEmpty() ||
                password.text.toString().trim().isEmpty()
            ) {

                showMessage(
                    "يرجى إدخال بيانات الدخول."
                )

            } else {

                showMessage(
                    "تسجيل الدخول الحقيقي سيتم ربطه بالخادم الآمن."
                )
            }
        }

        addCard(
            "👤 متابعة كزائر",
            "استخدام الخدمات العامة"
        ) {
            showGuestMode()
        }

        addCard(
            "🏠 الرئيسية",
            "العودة"
        ) {
            showHome()
        }
    }

    private fun showAds() {

        setContentView(baseLayout())
        content.removeAllViews()

        addSection("📢 الإعلانات")

        addCard(
            "🥉 BRONZE",
            "الإعلانات الأساسية"
        ) {
            showMessage("إعلانات BRONZE.")
        }

        addCard(
            "🥈 SILVER",
            "ظهور أفضل"
        ) {
            showMessage("إعلانات SILVER.")
        }

        addCard(
            "🥇 GOLD",
            "ظهور مميز في أعلى النتائج"
        ) {
            showMessage("إعلانات GOLD.")
        }

        addCard(
            "⭐ إعلان مميز",
            "خيار إضافي للظهور"
        ) {
            showMessage(
                "الإعلان المميز قيد التجهيز."
            )
        }

        addCard(
            "➕ إضافة إعلان",
            "إنشاء عرض جديد"
        ) {
            showAddAd()
        }

        addCard(
            "🏠 الرئيسية",
            "العودة"
        ) {
            showHome()
        }
    }

    private fun showPoints() {

        setContentView(baseLayout())
        content.removeAllViews()

        addSection("⭐ النقاط والمكافآت")

        addCard(
            "⭐ نقاطي",
            "رصيد النقاط الحالي"
        ) {
            showMessage(
                "رصيد النقاط: 0"
            )
        }

        addCard(
            "🎁 المكافآت",
            "المكافآت المتاحة"
        ) {
            showMessage(
                "المكافآت قيد التجهيز."
            )
        }

        addCard(
            "🏆 مستوى المستخدم",
            "تطور النقاط والمكافآت"
        ) {
            showMessage(
                "مستوى المستخدم قيد التجهيز."
            )
        }

        addCard(
            "📋 سجل النقاط",
            "متابعة عمليات كسب النقاط"
        ) {
            showMessage(
                "سجل النقاط قيد التجهيز."
            )
        }

        addCard(
            "🎯 تحديات النقاط",
            "أنشطة للحصول على نقاط"
        ) {
            showMessage(
                "تحديات النقاط قيد التجهيز."
            )
        }

        addCard(
            "🎁 استبدال النقاط",
            "استخدام النقاط في المكافآت"
        ) {
            showMessage(
                "استبدال النقاط قيد التجهيز."
            )
        }

        addCard(
            "🏠 الرئيسية",
            "العودة"
        ) {
            showHome()
        }
    }

    private fun showHumanIntelligence() {

        setContentView(baseLayout())
        content.removeAllViews()

        addSection("🧠 الذكاء البشري")

        addInfo(
            "مساحة للأفكار والابتكار والمشاريع والتعاون " +
                    "بين أصحاب المهارات والخبرات."
        )

        addCard(
            "💡 إرسال فكرة",
            "اقتراح فكرة أو مشروع جديد"
        ) {
            showIdea()
        }

        addCard(
            "🔎 مراجعة الأفكار",
            "تنظيم ومراجعة المقترحات"
        ) {
            showMessage(
                "مراجعة الأفكار قيد التجهيز."
            )
        }

        addCard(
            "👥 تكوين الفرق",
            "ربط أصحاب الأفكار والمهارات"
        ) {
            showMessage(
                "تكوين الفرق قيد التجهيز."
            )
        }

        addCard(
            "📝 متابعة الفكرة",
            "رقم مرجعي ومراحل التطوير"
        ) {
            showMessage(
                "متابعة الفكرة قيد التجهيز."
            )
        }

        addCard(
            "🤖 مساعدة CTM AI",
            "تحسين معلومات الفكرة"
        ) {
            showCtmAi()
        }

        addCard(
            "🏭 تحويل الفكرة إلى مشروع",
            "استخراج المشاريع المحتملة"
        ) {
            showMessage(
                "تحويل الفكرة إلى مشروع قيد التجهيز."
            )
        }

        addCard(
            "🏅 حالة الفكرة",
            "معرفة مرحلة المراجعة والتطوير"
        ) {
            showMessage(
                "حالة الفكرة قيد التجهيز."
            )
        }

        addCard(
            "📊 إحصائيات الأفكار",
            "متابعة عدد الأفكار والمشاريع"
        ) {
            showMessage(
                "إحصائيات الأفكار قيد التجهيز."
            )
        }

        addCard(
            "🔢 الرقم المرجعي",
            "رقم خاص لمتابعة كل فكرة"
        ) {
            showMessage(
                "الرقم المرجعي قيد التجهيز."
            )
        }

        addCard(
            "👥 أعضاء الفريق",
            "متابعة المشاركين في المشروع"
        ) {
            showMessage(
                "أعضاء الفريق قيد التجهيز."
            )
        }

        addCard(
            "🚀 مراحل المشروع",
            "من الفكرة إلى التنفيذ"
        ) {
            showMessage(
                "مراحل المشروع قيد التجهيز."
            )
        }

        addCard(
            "💰 تمويل المشروع",
            "خيارات دعم وتمويل المشروع"
        ) {
            showMessage(
                "تمويل المشروع قيد التجهيز."
            )
        }

        addCard(
            "📢 نشر المشروع",
            "عرض المشروع بعد اعتماده"
        ) {
            showMessage(
                "نشر المشروع قيد التجهيز."
            )
        }

        addCard(
            "⭐ تقييم الفكرة",
            "تقييم مراحل تطوير الفكرة"
        ) {
            showMessage(
                "تقييم الفكرة قيد التجهيز."
            )
        }

        addCard(
            "📚 دليل الابتكار",
            "معلومات تساعد على تطوير الأفكار"
        ) {
            showMessage(
                "دليل الابتكار قيد التجهيز."
            )
        }

        addCard(
            "🌍 التعاون",
            "ربط الأفكار بالخبرات والجهات المناسبة"
        ) {
            showMessage(
                "التعاون قيد التجهيز."
            )
        }

        addCard(
            "🔔 إشعارات الفكرة",
            "متابعة آخر التحديثات"
        ) {
            showMessage(
                "إشعارات الفكرة قيد التجهيز."
            )
        }

        addCard(
            "🧑‍🔬 الخبراء والمختصون",
            "مراجعة الأفكار من أصحاب الخبرة"
        ) {
            showMessage(
                "الخبراء والمختصون قيد التجهيز."
            )
        }

        addCard(
            "🔄 تحديث الفكرة",
            "إضافة معلومات وتعديلات جديدة"
        ) {
            showMessage(
                "تحديث الفكرة قيد التجهيز."
            )
        }

        addCard(
            "✅ اعتماد الفكرة",
            "الانتقال إلى مرحلة المشروع"
        ) {
            showMessage(
                "اعتماد الفكرة قيد التجهيز."
            )
        }

        addCard(
            "📋 شروط المشروع",
            "متطلبات الانتقال إلى التنفيذ"
        ) {
            showMessage(
                "شروط المشروع قيد التجهيز."
            )
        }

        addCard(
            "📈 خطة العمل",
            "تنظيم خطوات تنفيذ المشروع"
        ) {
            showMessage(
                "خطة العمل قيد التجهيز."
            )
        }

        addCard(
            "🛠️ أدوات التنفيذ",
            "الأدوات والخدمات اللازمة للمشروع"
        ) {
            showMessage(
                "أدوات التنفيذ قيد التجهيز."
            )
        }

        addCard(
            "📅 جدول المشروع",
            "مواعيد ومراحل التنفيذ"
        ) {
            showMessage(
                "جدول المشروع قيد التجهيز."
            )
        }

        addCard(
            "📦 المنتجات الناتجة",
            "عرض المنتجات والخدمات الناتجة"
        ) {
            showMessage(
                "المنتجات الناتجة قيد التجهيز."
            )
        }

        addCard(
            "🏆 إنجازات المشروع",
            "متابعة ما تم إنجازه"
        ) {
            showMessage(
                "إنجازات المشروع قيد التجهيز."
            )
        }

        addCard(
            "📊 تقرير المشروع",
            "ملخص شامل عن حالة المشروع"
        ) {
            showMessage(
                "تقرير المشروع قيد التجهيز."
            )
        }

        addCard(
            "🔐 حماية الفكرة",
            "حفظ بيانات الفكرة وخصوصيتها"
        ) {
            showMessage(
                "حماية الفكرة قيد التجهيز."
            )
        }

        addCard(
            "🗂️ أرشيف الأفكار",
            "حفظ الأفكار والمشاريع السابقة"
        ) {
            showMessage(
                "أرشيف الأفكار قيد التجهيز."
            )
        }

        addCard(
            "📞 التواصل والدعم",
            "المساعدة والاستفسارات"
        ) {
            showMessage(
                "التواصل والدعم قيد التجهيز."
            )
        }

        addCard(
            "🌟 الأفكار المميزة",
            "أفكار وصلت إلى مراحل متقدمة"
        ) {
            showMessage(
                "الأفكار المميزة قيد التجهيز."
            )
        }

        addCard(
            "📢 فرص التعاون",
            "فرص التعاون المتاحة"
        ) {
            showMessage(
                "فرص التعاون قيد التجهيز."
            )
        }

        addCard(
            "💡 أفكار المستقبل",
            "أفكار قابلة للتطوير والتوسع"
        ) {
            showMessage(
                "أفكار المستقبل قيد التجهيز."
            )
        }

        addCard(
            "🌐 مشاريع دولية",
            "أفكار قابلة للتوسع خارج السودان"
        ) {
            showMessage(
                "المشاريع الدولية قيد التجهيز."
            )
        }

        addCard(
            "🌱 الاستدامة",
            "أفكار تدعم التنمية والاستفادة من الموارد"
        ) {
            showMessage(
                "الاستدامة قيد التجهيز."
            )
        }

        addCard(
            "🤝 الشراكات",
            "ربط المشاريع بالجهات والشركاء"
        ) {
            showMessage(
                "الشراكات قيد التجهيز."
            )
        }

        addCard(
            "🧭 خارطة الابتكار",
            "متابعة مسار الأفكار والمشاريع"
        ) {
            showMessage(
                "خارطة الابتكار قيد التجهيز."
            )
        }

        addCard(
            "📌 المشاريع المعتمدة",
            "عرض المشاريع التي تم اعتمادها"
        ) {
            showMessage(
                "المشاريع المعتمدة قيد التجهيز."
            )
        }

        addCard(
            "📊 أثر المشروع",
            "متابعة النتائج والفوائد"
        ) {
            showMessage(
                "أثر المشروع قيد التجهيز."
            )
        }

        addCard(
            "🗃️ ملفات المشروع",
            "تنظيم المستندات والمعلومات"
        ) {
            showMessage(
                "ملفات المشروع قيد التجهيز."
            )
        }

        addCard(
            "🧪 اختبار الفكرة",
            "تجربة الفكرة قبل التوسع"
        ) {
            showMessage(
                "اختبار الفكرة قيد التجهيز."
            )
        }

        addCard(
            "🔧 تطوير وتحسين",
            "تحسين المشروع بناءً على النتائج"
        ) {
            showMessage(
                "تطوير وتحسين قيد التجهيز."
            )
        }

        addCard(
            "📈 قياس النتائج",
            "متابعة تطور المشروع ومؤشراته"
        ) {
            showMessage(
                "قياس النتائج قيد التجهيز."
            )
        }

        addCard(
            "🔔 تنبيهات المشروع",
            "تنبيهات مهمة حول مراحل المشروع"
        ) {
            showMessage(
                "تنبيهات المشروع قيد التجهيز."
            )
        }

        addCard(
            "📚 المعرفة والخبرة",
            "مشاركة الخبرات والمعلومات المفيدة"
        ) {
            showMessage(
                "المعرفة والخبرة قيد التجهيز."
            )
        }

        addCard(
            "🌍 التوسع",
            "تطوير الأفكار لأسواق جديدة"
        ) {
            showMessage(
                "التوسع قيد التجهيز."
            )
        }

        addCard(
            "🧠 مركز الابتكار",
            "مساحة تجمع الأفكار والخبرات والمشاريع"
        ) {
            showMessage(
                "مركز الابتكار قيد التجهيز."
            )
        }

        addCard(
            "🏠 العودة إلى الرئيسية",
            "الرجوع"
        ) {
            showHome()
        }
    }

    private fun showIdea() {

        setContentView(baseLayout())
        content.removeAllViews()

        addSection("💡 إرسال فكرة")

        val title = EditText(this)
        title.hint = "عنوان الفكرة"
        title.setTextColor(textDark)
        title.setHintTextColor(muted)
        content.addView(title)

        val description = EditText(this)
        description.hint = "شرح الفكرة"
        description.setTextColor(textDark)
        description.setHintTextColor(muted)
        content.addView(description)

        val field = EditText(this)
        field.hint = "المجال"
        field.setTextColor(textDark)
        field.setHintTextColor(muted)
        content.addView(field)

        addCard(
            "💾 حفظ الفكرة",
            "تجهيز الفكرة للمراجعة"
        ) {

            if (
                title.text.toString().trim().isEmpty() ||
                description.text.toString().trim().isEmpty()
            ) {

                showMessage(
                    "يرجى إدخال عنوان الفكرة وشرحها."
                )

            } else {

                showMessage(
                    "تم تجهيز الفكرة للمراجعة.\n" +
                            "سيتم إنشاء الرقم المرجعي عند ربط قاعدة البيانات."
                )
            }
        }

        addCard(
            "🏠 العودة إلى الذكاء البشري",
            "الرجوع"
        ) {
            showHumanIntelligence()
        }
    }

    private fun showManagerOffice() {

        setContentView(baseLayout())
        content.removeAllViews()

        addSection("🏢 مكتب المدير")

        addInfo(
            "المالك",
            "المالك ياسر حسن وشركاؤه"
        )

        addInfo(
            "وظيفة المكتب",
            "إدارة أقسام CENTRAL MARKET ومتابعة التطوير والصلاحيات والإصدارات."
        )

        addCard(
            "📋 إدارة المشروع",
            "متابعة الأقسام والخدمات والإضافات"
        ) {
            showMessage("إدارة المشروع قيد التجهيز.")
        }

        addCard(
            "🤖 مكتب CTM AI",
            "مراجعة الذكاء الاصطناعي قبل تفعيله"
        ) {
            showCtmAi()
        }

        addCard(
            "🛡️ مكتب الأمن والمعلومات",
            "الحماية والرقابة ومكافحة السرقة والاحتيال"
        ) {
            showSecurityOffice()
        }

        addCard(
            "⚖️ مكتب النائب العام للمشروع",
            "الملفات القانونية والاستشارات والصلاحيات"
        ) {
            showAttorneyOffice()
        }

        addCard(
            "💰 النظام المالي الخاص",
            "متابعة الأسهم والمعاملات والدخل المخصص للمالك"
        ) {
            showPrivateFinancialSystem()
        }

        addCard(
            "📄 الوثائق والملفات",
            "تنظيم وثائق المشروع وملفات الإدارة"
        ) {
            showDocuments()
        }

        addCard(
            "📢 الإعلانات",
            "متابعة الإعلانات والخدمات التجارية"
        ) {
            showAds()
        }

        addCard(
            "🧠 الذكاء البشري",
            "الأفكار والخبرات والمشاريع"
        ) {
            showHumanIntelligence()
        }

        addCard(
            "🤲 صندوق دعم الأيتام والمحتاجين",
            "قسم الدعم والمساعدة"
        ) {
            showCharity()
        }

        addCard(
            "🌐 اختبار الاتصال",
            "فحص حالة الاتصال بالإنترنت"
        ) {
            showOnline()
        }

        addCard(
            "🏠 العودة إلى الرئيسية",
            "الرجوع"
        ) {
            showHome()
        }
    }

    private fun showDocuments() {

        setContentView(baseLayout())
        content.removeAllViews()

        addSection("📄 الوثائق والملفات")

        addInfo(
            "حماية الوثائق",
            "الوثائق الإدارية والملفات الخاصة بالمشروع لا تظهر للمستخدم العادي."
        )

        addCard(
            "🗂️ ملفات المشروع",
            "تنظيم ملفات CENTRAL MARKET"
        ) {
            showMessage("قسم ملفات المشروع قيد التجهيز.")
        }

        addCard(
            "🔐 الملفات الخاصة",
            "الوصول حسب الصلاحيات الممنوحة"
        ) {
            showMessage("الملفات الخاصة محمية بالصلاحيات.")
        }

        addCard(
            "🧾 سجل الإصدارات",
            "متابعة نسخ التطبيق والتحديثات"
        ) {
            showMessage("سجل الإصدارات قيد التجهيز.")
        }

        addCard(
            "🛡️ حماية مفاتيح التطبيق",
            "مفاتيح التوقيع والأسرار لا تعرض للمستخدم"
        ) {
            showMessage("حماية مفاتيح التطبيق جزء من نظام الأمان.")
        }

        addCard(
            "🏠 العودة إلى مكتب المدير",
            "الرجوع"
        ) {
            showManagerOffice()
        }
    }

 
    private fun showBadger() {

        setContentView(baseLayout())
        content.removeAllViews()

        addSection("🦡 BADGER")

        addInfo(
            "BADGER",
            "نظام مصرفي متكامل يمكن تطويره وبيعه للمؤسسات والبنوك وفق العقود والموافقات المطلوبة."
        )

        addInfo(
            "الهوية",
            "غرير العسل + الكرة الأرضية، مع هوية إيصالات زرقاء وذهبية وسوداء."
        )

        addCard(
            "🏦 الحسابات والودائع",
            "عرض معلومات الحسابات والودائع بطريقة منظمة"
        ) {
            showMessage("قسم الحسابات والودائع قيد التجهيز.")
        }

        addCard(
            "📊 التحليل المالي",
            "تحليل الإيداعات والاستثمارات والأرباح"
        ) {
            showMessage("التحليل المالي قيد التجهيز.")
        }

        addCard(
            "💵 العمولات",
            "العمولات تحدد بعقد وسجلات واضحة"
        ) {
            showMessage("العمولات لا تنفذ إلا وفق الاتفاقات والصلاحيات المعتمدة.")
        }

        addCard(
            "🧾 الإيصالات",
            "إيصالات واضحة للعمليات"
        ) {
            showMessage("نظام الإيصالات قيد التجهيز.")
        }

        addCard(
            "🛡️ مكافحة الاحتيال",
            "مراقبة العمليات غير المعتادة"
        ) {
            showMessage("نظام مكافحة الاحتيال قيد التجهيز.")
        }

        addCard(
            "🔒 القفل التلقائي",
            "حماية الأقسام المدفوعة والحساسة"
        ) {
            showMessage("سيتم تطبيق القفل التلقائي وفق سياسة الأمان.")
        }

        addCard(
            "✅ تأكيد المالك",
            "لا يتم تنفيذ الإجراءات الحساسة دون التأكيد والصلاحية المطلوبة"
        ) {
            showMessage("التنفيذ الحساس يحتاج إلى الصلاحية والتأكيد المناسبين.")
        }

        addCard(
            "🏠 العودة إلى الرئيسية",
            "الرجوع"
        ) {
            showHome()
        }
    }

    private fun showSecurityOffice() {

        setContentView(baseLayout())
        content.removeAllViews()

        addSection("🛡️ مكتب الأمن والمعلومات")

        addInfo(
            "الهدف",
            "حماية المستخدمين والتطبيق والبيانات ومتابعة محاولات التلاعب والسرقة والاحتيال."
        )

        addCard(
            "🚨 مكافحة السرقة والاحتيال",
            "رصد السلوكيات غير المعتادة وتسجيل الأحداث"
        ) {
            showMessage("قسم مكافحة السرقة والاحتيال قيد التجهيز.")
        }

        addCard(
            "🔐 حماية الدخول",
            "المكاتب الإدارية والأمنية لا تظهر للمستخدم العادي"
        ) {
            showMessage("الوصول للمكاتب الخاصة يعتمد على الصلاحيات.")
        }

        addCard(
            "📝 سجل الحوادث",
            "توثيق الأحداث والإجراءات الأمنية"
        ) {
            showMessage("سجل الحوادث قيد التجهيز.")
        }

        addCard(
            "🤖 مراقبة CTM AI",
            "مراقبة محاولات التلاعب والمخاطر وفق الصلاحيات"
        ) {
            showCtmAi()
        }

        addCard(
            "👤 إجراءات الحسابات",
            "أي إجراء على حساب مستخدم يجب أن يكون وفق سياسة واضحة وتوثيق مناسب"
        ) {
            showMessage("إجراءات الحسابات قيد التجهيز.")
        }

        addCard(
            "🏠 العودة إلى مكتب المدير",
            "الرجوع"
        ) {
            showManagerOffice()
        }
    }

    private fun showAttorneyOffice() {

        setContentView(baseLayout())
        content.removeAllViews()

        addSection("⚖️ مكتب النائب العام للمشروع")

        addInfo(
            "الخصوصية",
            "هذا القسم إداري خاص ولا يظهر للمستخدمين العاديين."
        )

        addInfo(
            "الاختصاص",
            "الاستشارات والملفات القانونية وسجل القضايا والحوادث المتعلقة بالمشروع."
        )

        addCard(
            "📁 القضايا والحوادث",
            "سجل مركزي للأحداث والملفات القانونية"
        ) {
            showMessage("سجل القضايا والحوادث قيد التجهيز.")
        }

        addCard(
            "🏢 الشركات والمستثمرون",
            "تنظيم الاستشارات والاجتماعات والدعوات"
        ) {
            showMessage("قسم الشركات والمستثمرين قيد التجهيز.")
        }

        addCard(
            "📨 الدعوات",
            "دعوات محددة المدة أو لمرة واحدة"
        ) {
            showMessage("نظام الدعوات قيد التجهيز.")
        }

        addCard(
            "🔐 الصلاحيات",
            "لا يتم كشف ملفات المشروع إلا وفق الصلاحية والموافقة المناسبة"
        ) {
            showMessage("نظام الصلاحيات قيد التجهيز.")
        }

        addCard(
            "👔 مكتب المدير",
            "عرض المعلومات المسموح بها للإدارة"
        ) {
            showManagerOffice()
        }

        addCard(
            "🏠 العودة إلى الرئيسية",
            "الرجوع"
        ) {
            showHome()
        }
    }

    private fun showPrivateFinancialSystem() {

        setContentView(baseLayout())
        content.removeAllViews()

        addSection("💰 النظام المالي الخاص")

        addInfo(
            "الوظيفة",
            "قسم خاص يتابع الأسهم والمعاملات والدخل الذي يحدده المالك للمراجعة."
        )

        addInfo(
            "الخصوصية",
            "لا تظهر البيانات المالية الخاصة للمستخدمين أو الشركاء إلا وفق الصلاحيات التي يحددها المالك."
        )

        addCard(
            "📊 الأسهم",
            "متابعة الأسهم التي يحددها المالك"
        ) {
            showMessage("قسم الأسهم قيد التجهيز.")
        }

        addCard(
            "💳 المعاملات",
            "متابعة المعاملات المحددة للمراجعة"
        ) {
            showMessage("قسم المعاملات قيد التجهيز.")
        }

        addCard(
            "💵 الدخل",
            "متابعة الدخل والسجلات الخاصة"
        ) {
            showMessage("قسم الدخل قيد التجهيز.")
        }

        addCard(
            "🤝 صلاحية الشريك",
            "السماح بالاطلاع وفق نافذة محددة يوافق عليها المالك"
        ) {
            showMessage("صلاحيات الشركاء قيد التجهيز.")
        }

        addCard(
            "🏠 العودة إلى مكتب المدير",
            "الرجوع"
        ) {
            showManagerOffice()
        }
    }

        private fun showCtmAi() {

        setContentView(baseLayout())
        content.removeAllViews()

        addSection("🤖 CTM AI")

        addInfo(
            "الاسم الرسمي",
            "CTM AI"
        )

        addInfo(
            "الوظيفة",
            "المساعد الرسمي لمنصة CENTRAL MARKET، ويعمل داخل نطاق المعلومات والخدمات المسموح بها في المنصة."
        )

        addCard(
            "🔎 البحث الذكي",
            "مساعدة المستخدم في الوصول إلى أقسام وخدمات CENTRAL MARKET"
        ) {
            showMessage("البحث الذكي قيد التجهيز.")
        }

        addCard(
            "🎙️ البحث الصوتي",
            "المساعدة في البحث والقراءة داخل المعلومات المسموح بها"
        ) {
            showMessage("البحث الصوتي قيد التجهيز.")
        }

        addCard(
            "🗣️ الكلمات المحلية",
            "تحسين فهم الكلمات المحلية والأخطاء الشائعة"
        ) {
            showMessage("تحسين الكلمات المحلية قيد التجهيز.")
        }

        addCard(
            "🛡️ مراقبة المخاطر",
            "مساعدة الإدارة في اكتشاف الأخطاء والمخاطر وفق الصلاحيات"
        ) {
            showMessage("مراقبة المخاطر قيد التجهيز.")
        }

        addCard(
            "📢 اقتراحات المستخدمين",
            "تنظيم الاقتراحات والتصويت العام على الأفكار"
        ) {
            showMessage("اقتراحات المستخدمين قيد التجهيز.")
        }

        addCard(
            "📄 فحص الوثائق",
            "دعم التحقق من الوثائق وفق النظام والصلاحيات المناسبة"
        ) {
            showMessage("فحص الوثائق قيد التجهيز.")
        }

        addCard(
            "⏸️ إيقاف CTM AI",
            "يمكن تعطيل الخدمة أو إيقافها وفق صلاحيات الإدارة"
        ) {
            showMessage("إدارة حالة CTM AI قيد التجهيز.")
        }

        addCard(
            "ℹ️ نطاق المساعدة",
            "CTM AI هو مساعد CENTRAL MARKET وليس مساعدًا عامًا خارج المنصة."
        ) {
            showMessage("أنا CTM AI، المساعد الرسمي لـ CENTRAL MARKET.")
        }

        addCard(
            "🏠 العودة إلى الرئيسية",
            "الرجوع"
        ) {
            showHome()
        }
    }

    private fun showCharity() {

        setContentView(baseLayout())
        content.removeAllViews()

        addSection("🤲 صندوق دعم الأيتام والمحتاجين")

        addInfo(
            "الهدف",
            "تنظيم مبادرات الدعم والمساعدة بطريقة واضحة ومسؤولة."
        )

        addCard(
            "👶 دعم الأيتام",
            "مبادرات مخصصة لدعم الأيتام"
        ) {
            showMessage("قسم دعم الأيتام قيد التجهيز.")
        }

        addCard(
            "🤝 المساعدات",
            "تنظيم فرص المساعدة والتبرعات وفق القواعد المعتمدة"
        ) {
            showMessage("قسم المساعدات قيد التجهيز.")
        }

        addCard(
            "📋 الحالات",
            "تنظيم بيانات الحالات وفق الخصوصية والصلاحيات"
        ) {
            showMessage("قسم الحالات قيد التجهيز.")
        }

        addCard(
            "🏠 العودة إلى الرئيسية",
            "الرجوع"
        ) {
            showHome()
        }
    }

    private fun showKitchen() {

        setContentView(baseLayout())
        content.removeAllViews()

        addSection("🍲 مطبخ الطيبات")

        addInfo(
            "الهدف",
            "قسم للطعام والمنتجات الغذائية والوصفات والخدمات المرتبطة بها."
        )

        addCard(
            "🍽️ الأطعمة",
            "عرض الأطعمة والمنتجات الغذائية"
        ) {
            showMessage("قسم الأطعمة قيد التجهيز.")
        }

        addCard(
            "👨‍🍳 الوصفات",
            "مشاركة الوصفات والمعلومات الغذائية"
        ) {
            showMessage("قسم الوصفات قيد التجهيز.")
        }

        addCard(
            "🚚 التوصيل",
            "ربط الطعام بخدمات التوصيل المتاحة"
        ) {
            showMessage("خدمة التوصيل قيد التجهيز.")
        }

        addCard(
            "🏪 المطاعم",
            "عرض المطاعم والخدمات الغذائية"
        ) {
            showMessage("قسم المطاعم قيد التجهيز.")
        }

        addCard(
            "🏠 العودة إلى الرئيسية",
            "الرجوع"
        ) {
            showHome()
        }
    }
}
