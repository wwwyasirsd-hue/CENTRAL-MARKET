package com.central.market

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.text.InputType
import android.view.Gravity
import android.widget.*

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
        header.setPadding(16, 20, 16, 10)
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

    private fun showHome() {
        setContentView(baseLayout())
        content.removeAllViews()

        addSection("مرحبًا بك في CENTRAL MARKET")

        val intro = TextView(this)
        intro.text =
            "منصة واحدة للأسواق والخدمات والتسويق والمركبات والمزيد."
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
            "🛡️ أمان المنطقة",
            "مؤشر معلومات السلامة"
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
            "💡 الذكاء البشري",
            "أفكار وابتكارات ومشاريع"
        ) {
            showHumanIntelligence()
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
            "منظومة الخدمات البنكية المستقبلية"
        ) {
            showBadger()
        }    private fun showOnline() {
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
                getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

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
                    "المرحلة القادمة: ربط البيانات والخدمات بخادم حقيقي."
                status.setTextColor(green)
            } else {
                status.text =
                    "● غير متصل بالإنترنت\n\n" +
                    "يمكنك استخدام الوظائف المحلية."
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
                "وضع البيانات المنخفضة سيتم تطويره في المرحلة التالية"
            )
        }

        addCard(
            "☁️ الخدمات السحابية",
            "قاعدة البيانات والخدمات المتصلة"
        ) {
            showMessage(
                "البنية السحابية سيتم ربطها لاحقًا"
            )
        }

        addCard(
            "📱 تجربة التطبيق",
            "اختبار النسخة على الهاتف"
        ) {
            showMessage(
                "التجربة الحالية تتم من خلال نسخة Android"
            )
        }
    }

    private fun showManagerOffice() {
        setContentView(baseLayout())
        content.removeAllViews()

        addSection("👔 مكتب المدير")

        val name = TextView(this)
        name.text = "ياسر حسن موسى عبدالله\nود الريس"
        name.textSize = 21f
        name.setTypeface(null, Typeface.BOLD)
        name.setTextColor(navy)
        name.gravity = Gravity.CENTER
        name.setPadding(10, 15, 10, 20)
        content.addView(name)

        addCard(
            "📊 لوحة المتابعة",
            "ملخص المشروع والعمليات"
        ) {
            showMessage(
                "لوحة المتابعة قيد التوسعة"
            )
        }

        addCard(
            "📁 الخزانة الإلكترونية",
            "العقود والوثائق والشركاء"
        ) {
            showDocuments()
        }

        addCard(
            "📢 إدارة الإعلانات",
            "BRONZE / SILVER / GOLD"
        ) {
            showAds()
        }

        addCard(
            "💡 الذكاء البشري",
            "الأفكار والمشاريع والفرق"
        ) {
            showHumanIntelligence()
        }

        addCard(
            "🤲 صندوق الدعم",
            "المبادرات والمتابعة"
        ) {
            showCharity()
        }

        addCard(
            "🔔 التنبيهات والتقارير",
            "متابعة التقارير"
        ) {
            showMessage(
                "التقارير قيد التجهيز"
            )
        }

        addCard(
            "🛡️ الأمان والصلاحيات",
            "إدارة الوصول والحماية"
        ) {
            showMessage(
                "نظام الصلاحيات المتقدم قيد التجهيز"
            )
        }

        addCard(
            "🌐 حالة الخدمات",
            "الاتصال والخدمات المتصلة"
        ) {
            showOnline()
        }
    }

    private fun showDocuments() {
        setContentView(baseLayout())
        content.removeAllViews()

        addSection("📁 الخزانة الإلكترونية")

        addCard(
            "📄 العقود",
            "عقود المشروع والشركاء"
        ) {
            showMessage(
                "قسم العقود قيد التجهيز"
            )
        }

        addCard(
            "🤝 الشركاء",
            "بيانات وملفات الشركاء"
        ) {
            showMessage(
                "ملفات الشركاء قيد التجهيز"
            )
        }

        addCard(
            "🧾 المستندات",
            "وثائق المشروع"
        ) {
            showMessage(
                "قسم المستندات قيد التجهيز"
            )
        }

        addCard(
            "🔐 الملفات المحمية",
            "وصول إداري مستقبلي"
        ) {
            showMessage(
                "الحماية المتقدمة ستضاف لاحقًا"
            )
        }
    }

    private fun showBadger() {
        setContentView(baseLayout())
        content.removeAllViews()

        addSection("🦡 BADGER")

        val intro = TextView(this)
        intro.text =
            "منظومة خدمات مالية مستقلة ضمن رؤية المشروع.\n\n" +
            "هذه الصفحة هي البنية الأولية فقط، ولا تنفذ تحويلات مالية حقيقية."

        intro.textSize = 16f
        intro.setTextColor(textDark)
        intro.gravity = Gravity.CENTER
        intro.setPadding(10, 10, 10, 20)
        content.addView(intro)

        addCard(
            "🌍 هوية BADGER",
            "الغرير والكرة الأرضية والهوية الخاصة"
        ) {
            showMessage(
                "هوية BADGER البصرية قيد التطوير"
            )
        }

        addCard(
            "👤 الحساب",
            "ملف المستخدم والخدمات المالية"
        ) {
            showMessage(
                "الحساب المالي سيتم ربطه لاحقًا"
            )
        }

        addCard(
            "💰 الرصيد",
            "عرض الرصيد والخدمات"
        ) {
            showMessage(
                "بيانات الرصيد غير مرتبطة حاليًا"
            )
        }

        addCard(
            "🧾 الإيصالات",
            "إيصالات وتقارير العمليات"
        ) {
            showMessage(
                "نظام الإيصالات قيد التطوير"
            )
        }

        addCard(
            "🚨 التنبيهات والأمان",
            "الحماية ومكافحة الاحتيال"
        ) {
            showMessage(
                "نظام الأمان المتقدم قيد التطوير"
            )
        }

        addCard(
            "📊 التقارير",
            "تقارير شهرية وإدارية"
        ) {
            showMessage(
                "التقارير قيد التجهيز"
            )
        }

        addCard(
            "🔐 الحماية",
            "القفل والصلاحيات"
        ) {
            showMessage(
                "الحماية المتقدمة سيتم تنفيذها قبل أي ربط مالي حقيقي"
            )
        }
    }

    private fun showCategory(
        category: String,
        description: String
    ) {
        setContentView(baseLayout())
        content.removeAllViews()

        addSection(category)

        val info = TextView(this)
        info.text = description
        info.textSize = 16f
        info.setTextColor(muted)
        info.setPadding(5, 5, 5, 15)
        content.addView(info)

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
    }

        addSection("🔮 خدمات قادمة")

        addCard(
            "💳 الدفع الإلكتروني",
            "سيتم ربطه لاحقًا"
        ) {
            showMessage("الدفع الإلكتروني قيد التجهيز")
        }

        addCard(
            "📍 الخرائط والتتبع",
            "الموقع والتتبع"
        ) {
            showMessage("الخرائط والتتبع قيد التجهيز")
        }
    }    private fun showProducts() {
        setContentView(baseLayout())
        content.removeAllViews()

        addSection("📦 المنتجات والخدمات")

        addCard(
            "📱 هاتف ذكي",
            "منتجات إلكترونية وخدمات"
        ) {
            showDetails(
                "هاتف ذكي",
                "منتج تجريبي داخل CENTRAL MARKET"
            )
        }

        addCard(
            "🚗 مركبة",
            "سيارات ومركبات للنقل"
        ) {
            showDetails(
                "مركبة",
                "قسم المركبات قيد التطوير"
            )
        }

        addCard(
            "🍽️ مطعم",
            "مطاعم وتوصيل"
        ) {
            showDetails(
                "مطعم",
                "خدمات المطاعم والتوصيل"
            )
        }

        addCard(
            "🌾 منتجات زراعية",
            "محاصيل ومواشي ومنتجات زراعية"
        ) {
            showDetails(
                "منتجات زراعية",
                "القسم الزراعي قيد التطوير"
            )
        }

        addCard(
            "🏗️ مواد بناء",
            "مواد البناء والجملة"
        ) {
            showDetails(
                "مواد بناء",
                "قسم مواد البناء والجملة"
            )
        }

        addCard(
            "🏥 خدمات صحية",
            "مختبرات وعيادات وصيدليات"
        ) {
            showDetails(
                "خدمات صحية",
                "الخدمات الصحية قيد التطوير"
            )
        }

        addCard(
            "🎓 التعليم",
            "مدارس ودورات وخدمات تعليمية"
        ) {
            showDetails(
                "التعليم",
                "الخدمات التعليمية قيد التطوير"
            )
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
            showMessage("تم حفظ العنصر في المفضلة")
        }

        addCard(
            "📤 مشاركة",
            "مشاركة معلومات العنصر"
        ) {
            showMessage("المشاركة قيد التجهيز")
        }

        addCard(
            "🧾 الرقم المرجعي",
            "رقم خاص بالعملية"
        ) {
            showMessage("سيتم إنشاء الرقم المرجعي عند تنفيذ العملية")
        }

        addCard(
            "💳 الدفع الإلكتروني",
            "سيتم ربطه لاحقًا"
        ) {
            showMessage("الدفع الإلكتروني قيد التجهيز")
        }

        addCard(
            "📍 الخرائط والتتبع",
            "الموقع والتتبع"
        ) {
            showMessage("الخرائط والتتبع قيد التجهيز")
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

        addCard(
            "🥉 BRONZE",
            "الإعلان الأساسي"
        ) {
            showMessage("تم اختيار BRONZE")
        }

        addCard(
            "🥈 SILVER",
            "ظهور أفضل للإعلان"
        ) {
            showMessage("تم اختيار SILVER")
        }

        addCard(
            "🥇 GOLD",
            "ظهور مميز في أعلى النتائج"
        ) {
            showMessage("تم اختيار GOLD")
        }

        addCard(
            "📢 نشر الإعلان",
            "إضافة الإعلان إلى السوق"
        ) {
            if (
                name.text.toString().trim().isEmpty() ||
                description.text.toString().trim().isEmpty()
            ) {
                showMessage("يرجى إدخال اسم المنتج والوصف")
            } else {
                showMessage(
                    "تم تجهيز الإعلان للنشر\n" +
                            "سيتم إنشاء رقم مرجعي خاص به"
                )
            }
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
            val query = search.text.toString().trim()

            if (query.isEmpty()) {
                showMessage("اكتب كلمة للبحث")
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
            showMessage("البحث حسب المدينة قيد التطوير")
        }

        addCard(
            "🏷️ البحث حسب القسم",
            "تصفية النتائج"
        ) {
            showMessage("التصفية حسب القسم قيد التطوير")
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
                "لا توجد عناصر محفوظة حاليًا"
            )
        }

        addCard(
            "🗑️ إدارة المفضلة",
            "حذف العناصر المحفوظة"
        ) {
            showMessage(
                "إدارة المفضلة سيتم ربطها ببيانات المستخدم لاحقًا"
            )
        }
    }    private fun showGuest() {
        setContentView(baseLayout())
        content.removeAllViews()

        addSection("👤 وضع الزائر")

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
            "اختبار الاتصال والخدمات"
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

    private fun showAds() {
        setContentView(baseLayout())
        content.removeAllViews()

        addSection("📢 الإعلانات")

        addCard(
            "🥉 BRONZE",
            "الإعلانات الأساسية"
        ) {
            showMessage("إعلانات BRONZE")
        }

        addCard(
            "🥈 SILVER",
            "ظهور أفضل"
        ) {
            showMessage("إعلانات SILVER")
        }

        addCard(
            "🥇 GOLD",
            "ظهور مميز في أعلى النتائج"
        ) {
            showMessage("إعلانات GOLD")
        }

        addCard(
            "⭐ إعلان مميز",
            "خيار إضافي للظهور"
        ) {
            showMessage("الإعلان المميز قيد التجهيز")
        }
    }  
  private fun showGuest() {        setContentView(baseLayout())
        content.removeAllViews()

        addSection("👤 وضع الزائر")

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
            "اختبار الاتصال والخدمات"
        ) {
            showOnline()
        }

        addCard(
            "🔐 تسجيل الدخول",
            "الدخول إلى الحساب"
        ) {
            showLogin()
        }    }

    private fun showAds() {
        setContentView(baseLayout())
        content.removeAllViews()

        addSection("📢 الإعلانات")
        addCard(
            "🥉 BRONZE",
            "الإعلانات الأساسية"
        ) {
            showMessage("إعلانات BRONZE")
        }

        addCard(
            "🥈 SILVER",
            "ظهور أفضل"
        ) {
            showMessage("إعلانات SILVER")
        }

        addCard(
            "🥇 GOLD",
            "ظهور مميز في أعلى النتائج"
        ) {
            showMessage("إعلانات GOLD")
        }  
        addCard(
            "⭐ إعلان مميز",
            "خيار إضافي للظهور"
        ) {
            showMessage("الإعلان المميز قيد التجهيز")
        }
    }    private fun showPoints() {
        setContentView(baseLayout())
        content.removeAllViews()

        addSection("⭐ النقاط والمكافآت")

        addCard(
            "⭐ نقاطي",
            "رصيد النقاط الحالي"
        ) {
            showMessage("رصيد النقاط: 0")
        }

        addCard(
            "🎁 المكافآت",
            "المكافآت المتاحة"
        ) {
            showMessage("المكافآت قيد التجهيز")
        }        addCard(
            "🏆 مستوى المستخدم",
            "تطور النقاط والمكافآت"
        ) {
            showMessage("مستوى المستخدم قيد التجهيز")
        }

        addCard(
            "📋 سجل النقاط",
            "متابعة عمليات كسب النقاط"
        ) {
            showMessage("سجل النقاط قيد التجهيز")
        }        addCard(
            "🎯 تحديات النقاط",
            "أنشطة للحصول على نقاط"
        ) {
            showMessage("تحديات النقاط قيد التجهيز")
        }

        addCard(
            "🎁 استبدال النقاط",
            "استخدام النقاط في المكافآت"
        ) {
            showMessage("استبدال النقاط قيد التجهيز")
        }

    }    private fun showHumanIntelligence() {
        setContentView(baseLayout())
        content.removeAllViews()

        addSection("🧠 الذكاء البشري")

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
            showMessage("مراجعة الأفكار قيد التجهيز")
        }

        addCard(
            "👥 تكوين الفرق",
            "ربط أصحاب الأفكار والمهارات"
        ) {
            showMessage("تكوين الفرق قيد التجهيز")
        }        addCard(
            "📝 متابعة الفكرة",
            "رقم مرجعي ومراحل التطوير"
        ) {
            showMessage("متابعة الفكرة قيد التجهيز")
        }

        addCard(
            "🤖 مساعدة الذكاء الاصطناعي",
            "إكمال وتحسين معلومات الفكرة"
        ) {
            showMessage("مساعدة الذكاء الاصطناعي قيد التجهيز")
        }

        addCard(
            "🏭 تحويل الفكرة إلى مشروع",
            "استخراج المشاريع والمنتجات المحتملة"
        ) {
            showMessage("تحويل الفكرة إلى مشروع قيد التجهيز")
        }        addCard(
            "🏭 تحويل الفكرة إلى مشروع",
            "استخراج المشاريع والمنتجات المحتملة"
        ) {
            showMessage("تحويل الفكرة إلى مشروع قيد التجهيز")
        }

        addCard(
            "🏅 حالة الفكرة",
            "معرفة مرحلة المراجعة والتطوير"
        ) {
            showMessage("حالة الفكرة قيد التجهيز")
        }        addCard(
            "📊 إحصائيات الأفكار",
            "متابعة عدد الأفكار والمشاريع"
        ) {
            showMessage("إحصائيات الأفكار قيد التجهيز")
        }

        addCard(
            "🔢 الرقم المرجعي",
            "رقم خاص لمتابعة كل فكرة"
        ) {
            showMessage("الرقم المرجعي قيد التجهيز")
        }        addCard(
            "👥 أعضاء الفريق",
            "متابعة المشاركين في المشروع"
        ) {
            showMessage("أعضاء الفريق قيد التجهيز")
        }

        addCard(
            "🚀 مراحل المشروع",
            "من الفكرة إلى التنفيذ"
        ) {
            showMessage("مراحل المشروع قيد التجهيز")
        }        addCard(
            "💰 تمويل المشروع",
            "خيارات دعم وتمويل المشروع"
        ) {
            showMessage("تمويل المشروع قيد التجهيز")
        }

        addCard(
            "📢 نشر المشروع",
            "عرض المشروع بعد اعتماده"
        ) {
            showMessage("نشر المشروع قيد التجهيز")
        }        addCard(
            "⭐ تقييم الفكرة",
            "تقييم مراحل تطوير الفكرة"
        ) {
            showMessage("تقييم الفكرة قيد التجهيز")
        }

        addCard(
            "📚 دليل الابتكار",
            "معلومات تساعد على تطوير الأفكار"
        ) {
            showMessage("دليل الابتكار قيد التجهيز")
        }        addCard(
            "🌍 التعاون",
            "ربط الأفكار بالخبرات والجهات المناسبة"
        ) {
            showMessage("التعاون قيد التجهيز")
        }

        addCard(
            "🔔 إشعارات الفكرة",
            "متابعة آخر التحديثات"
        ) {
            showMessage("إشعارات الفكرة قيد التجهيز")
        }        addCard(
            "🧑‍🔬 الخبراء والمختصون",
            "مراجعة الأفكار من أصحاب الخبرة"
        ) {
            showMessage("الخبراء والمختصون قيد التجهيز")
        }

        addCard(
            "🔄 تحديث الفكرة",
            "إضافة معلومات وتعديلات جديدة"
        ) {
            showMessage("تحديث الفكرة قيد التجهيز")
        }        addCard(
            "✅ اعتماد الفكرة",
            "الانتقال إلى مرحلة المشروع"
        ) {
            showMessage("اعتماد الفكرة قيد التجهيز")
        }

        addCard(
            "📋 شروط المشروع",
            "متطلبات الانتقال إلى التنفيذ"
        ) {
            showMessage("شروط المشروع قيد التجهيز")
        }        addCard(
            "📈 خطة العمل",
            "تنظيم خطوات تنفيذ المشروع"
        ) {
            showMessage("خطة العمل قيد التجهيز")
        }

        addCard(
            "🛠️ أدوات التنفيذ",
            "الأدوات والخدمات اللازمة للمشروع"
        ) {
            showMessage("أدوات التنفيذ قيد التجهيز")
        }        addCard(
            "📅 جدول المشروع",
            "مواعيد ومراحل التنفيذ"
        ) {
            showMessage("جدول المشروع قيد التجهيز")
        }

        addCard(
            "📦 المنتجات الناتجة",
            "عرض المنتجات والخدمات الناتجة عن الفكرة"
        ) {
            showMessage("المنتجات الناتجة قيد التجهيز")
        }        addCard(
            "🏆 إنجازات المشروع",
            "متابعة ما تم إنجازه"
        ) {
            showMessage("إنجازات المشروع قيد التجهيز")
        }

        addCard(
            "📊 تقرير المشروع",
            "ملخص شامل عن حالة المشروع"
        ) {
            showMessage("تقرير المشروع قيد التجهيز")
        }        addCard(
            "🔐 حماية الفكرة",
            "حفظ بيانات الفكرة وخصوصيتها"
        ) {
            showMessage("حماية الفكرة قيد التجهيز")
        }

        addCard(
            "🗂️ أرشيف الأفكار",
            "حفظ الأفكار والمشاريع السابقة"
        ) {
            showMessage("أرشيف الأفكار قيد التجهيز")
        }        addCard(
            "📞 التواصل والدعم",
            "المساعدة والاستفسارات حول الأفكار"
        ) {
            showMessage("التواصل والدعم قيد التجهيز")
        }

        addCard(
            "🏠 العودة إلى الرئيسية",
            "الرجوع إلى الصفحة الرئيسية"
        ) {
            showHome()
        }        addCard(
            "🌟 الأفكار المميزة",
            "عرض الأفكار التي وصلت إلى مراحل متقدمة"
        ) {
            showMessage("الأفكار المميزة قيد التجهيز")
        }

        addCard(
            "📢 فرص التعاون",
            "التعرف على فرص التعاون المتاحة"
        ) {
            showMessage("فرص التعاون قيد التجهيز")
        }        addCard(
            "💡 أفكار المستقبل",
            "أفكار قابلة للتطوير والتوسع"
        ) {
            showMessage("أفكار المستقبل قيد التجهيز")
        }

        addCard(
            "🌐 مشاريع دولية",
            "أفكار ومشاريع قابلة للتوسع خارج السودان"
        ) {
            showMessage("المشاريع الدولية قيد التجهيز")
        }        addCard(
            "🌱 الاستدامة",
            "أفكار تدعم التنمية والاستفادة من الموارد"
        ) {
            showMessage("الاستدامة قيد التجهيز")
        }

        addCard(
            "🤝 الشراكات",
            "ربط المشاريع بالجهات والشركاء"
        ) {
            showMessage("الشراكات قيد التجهيز")
        }        addCard(
            "🧭 خارطة الابتكار",
            "متابعة مسار الأفكار والمشاريع"
        ) {
            showMessage("خارطة الابتكار قيد التجهيز")
        }

        addCard(
            "📌 المشاريع المعتمدة",
            "عرض المشاريع التي تم اعتمادها"
        ) {
            showMessage("المشاريع المعتمدة قيد التجهيز")
        }        addCard(
            "📊 أثر المشروع",
            "متابعة النتائج والفوائد المتوقعة"
        ) {
            showMessage("أثر المشروع قيد التجهيز")
        }

        addCard(
            "🗃️ ملفات المشروع",
            "تنظيم المستندات والمعلومات"
        ) {
            showMessage("ملفات المشروع قيد التجهيز")
        }        addCard(
            "🧪 اختبار الفكرة",
            "تجربة الفكرة قبل التوسع"
        ) {
            showMessage("اختبار الفكرة قيد التجهيز")
        }

        addCard(
            "🔧 تطوير وتحسين",
            "تحسين المشروع بناءً على النتائج"
        ) {
            showMessage("تطوير وتحسين قيد التجهيز")
        }        addCard(
            "📈 قياس النتائج",
            "متابعة تطور المشروع ومؤشراته"
        ) {
            showMessage("قياس النتائج قيد التجهيز")
        }

        addCard(
            "🔔 تنبيهات المشروع",
            "تنبيهات مهمة حول مراحل المشروع"
        ) {
            showMessage("تنبيهات المشروع قيد التجهيز")
        }        addCard(
            "📚 المعرفة والخبرة",
            "مشاركة الخبرات والمعلومات المفيدة"
        ) {
            showMessage("المعرفة والخبرة قيد التجهيز")
        }

        addCard(
            "🌍 التوسع",
            "تطوير الأفكار لتناسب أسواقًا جديدة"
        ) {
            showMessage("التوسع قيد التجهيز")
        }        addCard(
            "🧠 مركز الابتكار",
            "مساحة تجمع الأفكار والخبرات والمشاريع"
        ) {
            showMessage("مركز الابتكار قيد التجهيز")
        }

        addCard(
            "🏁 نهاية المرحلة",
            "الانتقال من الفكرة إلى المشروع"
        ) {
            showMessage("الانتقال إلى مرحلة المشروع قيد التجهيز")
        }        addCard(
            "📝 تقييم التجربة",
            "تسجيل الملاحظات بعد تنفيذ المشروع"
        ) {
            showMessage("تقييم التجربة قيد التجهيز")
        }

        addCard(
            "💬 ملاحظات الفريق",
            "تبادل الملاحظات بين أعضاء المشروع"
        ) {
            showMessage("ملاحظات الفريق قيد التجهيز")
        }        addCard(
            "📋 سجل التطوير",
            "متابعة التغييرات التي تمت على الفكرة"
        ) {
            showMessage("سجل التطوير قيد التجهيز")
        }

        addCard(
            "🔗 ربط الموارد",
            "ربط المشروع بالموارد والخدمات المناسبة"
        ) {
            showMessage("ربط الموارد قيد التجهيز")
        }        addCard(
            "📦 الموارد المتاحة",
            "عرض الموارد والخدمات المرتبطة بالمشروع"
        ) {
            showMessage("الموارد المتاحة قيد التجهيز")
        }

        addCard(
            "📍 موقع المشروع",
            "تحديد موقع المشروع ومجاله"
        ) {
            showMessage("موقع المشروع قيد التجهيز")
        }        addCard(
            "📍 تحديد النطاق",
            "تحديد المدن والمناطق المستهدفة"
        ) {
            showMessage("تحديد النطاق قيد التجهيز")
        }

        addCard(
            "📣 دعوة المشاركين",
            "دعوة أصحاب المهارات للمشاركة"
        ) {
            showMessage("دعوة المشاركين قيد التجهيز")
        }        addCard(
            "👨‍💼 إدارة المشاركين",
            "تنظيم الأدوار والمهام"
        ) {
            showMessage("إدارة المشاركين قيد التجهيز")
        }

        addCard(
            "📊 تقدم الفريق",
            "متابعة تقدم أعضاء الفريق"
        ) {
            showMessage("تقدم الفريق قيد التجهيز")
        }        addCard(
            "🎯 مهام الفريق",
            "توزيع ومتابعة المهام"
        ) {
            showMessage("مهام الفريق قيد التجهيز")
        }

        addCard(
            "⏱️ مواعيد المهام",
            "متابعة المواعيد والإنجاز"
        ) {
            showMessage("مواعيد المهام قيد التجهيز")
        }        addCard(
            "📌 أولوية المهام",
            "ترتيب المهام حسب أهميتها"
        ) {
            showMessage("أولوية المهام قيد التجهيز")
        }

        addCard(
            "🔄 تحديث حالة المهمة",
            "تحديث حالة الإنجاز والمتابعة"
        ) {
            showMessage("تحديث حالة المهمة قيد التجهيز")
        }        addCard(
            "📋 سجل المهام",
            "متابعة تاريخ المهام والتحديثات"
        ) {
            showMessage("سجل المهام قيد التجهيز")
        }

        addCard(
            "🏆 إنجازات الفريق",
            "عرض إنجازات أعضاء الفريق"
        ) {
            showMessage("إنجازات الفريق قيد التجهيز")
        }        addCard(
            "📈 تقييم أداء الفريق",
            "متابعة تقدم الفريق ونتائجه"
        ) {
            showMessage("تقييم أداء الفريق قيد التجهيز")
        }

        addCard(
            "🤝 حل الخلافات",
            "تنظيم الملاحظات والمشكلات داخل الفريق"
        ) {
            showMessage("حل الخلافات قيد التجهيز")
        }        addCard(
            "💬 التواصل داخل الفريق",
            "مشاركة الرسائل والملاحظات"
        ) {
            showMessage("التواصل داخل الفريق قيد التجهيز")
        }

        addCard(
            "📎 مشاركة الملفات",
            "مشاركة ملفات المشروع بين المشاركين"
        ) {
            showMessage("مشاركة الملفات قيد التجهيز")
        }        addCard(
            "📁 مستندات الفريق",
            "تنظيم مستندات وأوراق المشروع"
        ) {
            showMessage("مستندات الفريق قيد التجهيز")
        }

        addCard(
            "🔐 صلاحيات الفريق",
            "تنظيم صلاحيات الوصول للمعلومات"
        ) {
            showMessage("صلاحيات الفريق قيد التجهيز")
        }        addCard(
            "👤 ملف الفريق",
            "معلومات المشاركين وأدوارهم"
        ) {
            showMessage("ملف الفريق قيد التجهيز")
        }

        addCard(
            "🔎 البحث عن مهارات",
            "العثور على المهارات المناسبة للمشروع"
        ) {
            showMessage("البحث عن مهارات قيد التجهيز")
        }        addCard(
            "🧩 مطابقة المهارات",
            "اقتراح أعضاء مناسبين لكل مهمة"
        ) {
            showMessage("مطابقة المهارات قيد التجهيز")
        }

        addCard(
            "🌟 أفضل المساهمين",
            "عرض المشاركين الأكثر مساهمة"
        ) {
            showMessage("أفضل المساهمين قيد التجهيز")
        }        addCard(
            "📣 دعوة خبير",
            "دعوة خبير للمشاركة في المشروع"
        ) {
            showMessage("دعوة الخبير قيد التجهيز")
        }

        addCard(
            "🤝 قبول المشاركة",
            "إدارة طلبات المشاركة في المشروع"
        ) {
            showMessage("قبول المشاركة قيد التجهيز")
        }        addCard(
            "📨 طلبات المشاركة",
            "متابعة طلبات الانضمام إلى المشروع"
        ) {
            showMessage("طلبات المشاركة قيد التجهيز")
        }

        addCard(
            "🚫 إدارة الانسحاب",
            "تنظيم انسحاب المشاركين من المشروع"
        ) {
            showMessage("إدارة الانسحاب قيد التجهيز")
        }        addCard(
            "📌 حالة المشاركة",
            "معرفة حالة طلب المشاركة"
        ) {
            showMessage("حالة المشاركة قيد التجهيز")
        }

        addCard(
            "🔔 إشعار الفريق",
            "إرسال تنبيه لأعضاء الفريق"
        ) {
            showMessage("إشعار الفريق قيد التجهيز")
        }        addCard(
            "👥 قائمة المشاركين",
            "عرض المشاركين في المشروع"
        ) {
            showMessage("قائمة المشاركين قيد التجهيز")
        }

        addCard(
            "🔐 أمان المشاركة",
            "حماية بيانات المشاركين"
        ) {
            showMessage("أمان المشاركة قيد التجهيز")
        }        addCard(
            "📊 مشاركة الفريق",
            "متابعة نشاط المشاركين"
        ) {
            showMessage("مشاركة الفريق قيد التجهيز")
        }

        addCard(
            "🏅 تقدير المساهمين",
            "تسجيل مساهمات أعضاء الفريق"
        ) {
            showMessage("تقدير المساهمين قيد التجهيز")
        }        addCard(
            "📑 سجل المشاركة",
            "حفظ تاريخ مشاركات الفريق"
        ) {
            showMessage("سجل المشاركة قيد التجهيز")
        }

        addCard(
            "🔎 البحث في الفريق",
            "البحث عن مشارك أو مهمة"
        ) {
            showMessage("البحث في الفريق قيد التجهيز")
        }        addCard(
            "📝 طلب تعديل المشاركة",
            "تحديث بيانات المشاركة في المشروع"
        ) {
            showMessage("طلب تعديل المشاركة قيد التجهيز")
        }

        addCard(
            "📋 شروط المشاركة",
            "معرفة متطلبات الانضمام للمشروع"
        ) {
            showMessage("شروط المشاركة قيد التجهيز")
        }

        addCard(
            "🛡️ حماية بيانات الفريق",
            "تنظيم حماية معلومات المشاركين"
        ) {
            showMessage("حماية بيانات الفريق قيد التجهيز")
        }

        addCard(
            "📨 الرسائل المهمة",
            "متابعة الرسائل المرتبطة بالمشروع"
        ) {
            showMessage("الرسائل المهمة قيد التجهيز")
        }        addCard(
            "📅 اجتماعات الفريق",
            "تنظيم مواعيد الاجتماعات"
        ) {
            showMessage("اجتماعات الفريق قيد التجهيز")
        }

        addCard(
            "🗓️ جدول الفريق",
            "تنظيم أعمال ومواعيد المشاركين"
        ) {
            showMessage("جدول الفريق قيد التجهيز")
        }

        addCard(
            "📈 تقرير المشاركة",
            "ملخص نشاط ومساهمات المشاركين"
        ) {
            showMessage("تقرير المشاركة قيد التجهيز")
        }

        addCard(
            "🏁 إكمال المشاركة",
            "إنهاء مرحلة المشاركة في المشروع"
        ) {
            showMessage("إكمال المشاركة قيد التجهيز")
        }        addCard(
            "📊 إحصاءات الفريق",
            "متابعة أرقام ونشاط الفريق"
        ) {
            showMessage("إحصاءات الفريق قيد التجهيز")
        }

        addCard(
            "🎯 أهداف الفريق",
            "تحديد ومتابعة أهداف المشروع"
        ) {
            showMessage("أهداف الفريق قيد التجهيز")
        }

        addCard(
            "📌 نقاط المتابعة",
            "تسجيل أهم نقاط العمل"
        ) {
            showMessage("نقاط المتابعة قيد التجهيز")
        }

        addCard(
            "🔄 مراجعة التقدم",
            "مراجعة مراحل تنفيذ المشروع"
        ) {
            showMessage("مراجعة التقدم قيد التجهيز")
        }        addCard(
            "📂 أرشيف الفريق",
            "حفظ السجلات والملفات السابقة"
        ) {
            showMessage("أرشيف الفريق قيد التجهيز")
        }

        addCard(
            "📑 تقارير الفريق",
            "عرض التقارير الخاصة بالفريق"
        ) {
            showMessage("تقارير الفريق قيد التجهيز")
        }

        addCard(
            "🔔 تنبيهات المهام",
            "متابعة التنبيهات المرتبطة بالمهام"
        ) {
            showMessage("تنبيهات المهام قيد التجهيز")
        }

        addCard(
            "🏢 إدارة المشروع",
            "تنظيم المشروع ومراحله"
        ) {
            showMessage("إدارة المشروع قيد التجهيز")
        }        addCard(
            "📋 سجل قرارات الفريق",
            "حفظ القرارات المهمة للمشروع"
        ) {
            showMessage("سجل قرارات الفريق قيد التجهيز")
        }

        addCard(
            "🧭 مسار الفريق",
            "متابعة رحلة الفريق من البداية"
        ) {
            showMessage("مسار الفريق قيد التجهيز")
        }

        addCard(
            "📢 إعلانات المشروع",
            "عرض الإعلانات المهمة للفريق"
        ) {
            showMessage("إعلانات المشروع قيد التجهيز")
        }

        addCard(
            "📬 صندوق الفريق",
            "متابعة الطلبات والمراسلات"
        ) {
            showMessage("صندوق الفريق قيد التجهيز")
        }        addCard(
            "🤝 الشراكة داخل الفريق",
            "تنظيم التعاون بين المشاركين"
        ) {
            showMessage("الشراكة داخل الفريق قيد التجهيز")
        }

        addCard(
            "📈 نمو الفريق",
            "متابعة تطور الفريق ومهاراته"
        ) {
            showMessage("نمو الفريق قيد التجهيز")
        }

        addCard(
            "🏅 شهادات الإنجاز",
            "توثيق إنجازات المشاركين"
        ) {
            showMessage("شهادات الإنجاز قيد التجهيز")
        }

        addCard(
            "🔚 إنهاء المشروع",
            "إجراءات إغلاق المشروع وأرشفة نتائجه"
        ) {
            showMessage("إنهاء المشروع قيد التجهيز")
        }        addCard(
            "📊 تحليل المشروع",
            "تحليل البيانات والنتائج"
        ) {
            showMessage("تحليل المشروع قيد التجهيز")
        }

        addCard(
            "💡 اقتراحات التحسين",
            "اقتراح أفكار لتطوير المشروع"
        ) {
            showMessage("اقتراحات التحسين قيد التجهيز")
        }

        addCard(
            "🔍 مراجعة الجودة",
            "متابعة جودة العمل والمخرجات"
        ) {
            showMessage("مراجعة الجودة قيد التجهيز")
        }

        addCard(
            "📐 معايير المشروع",
            "تنظيم المعايير المطلوبة للتنفيذ"
        ) {
            showMessage("معايير المشروع قيد التجهيز")
        }        addCard(
            "🌱 التطوير المستمر",
            "تحسين المشروع بعد كل مرحلة"
        ) {
            showMessage("التطوير المستمر قيد التجهيز")
        }

        addCard(
            "📚 مكتبة المعرفة",
            "حفظ المعلومات والخبرات المفيدة"
        ) {
            showMessage("مكتبة المعرفة قيد التجهيز")
        }

        addCard(
            "🗂️ أرشيف المشاريع",
            "حفظ المشاريع السابقة ونتائجها"
        ) {
            showMessage("أرشيف المشاريع قيد التجهيز")
        }

        addCard(
            "🏠 العودة إلى الذكاء البشري",
            "الرجوع إلى مركز الذكاء البشري"
        ) {
            showHumanIntelligence()
        }        addCard(
            "🧠 تحليل الأفكار",
            "دراسة الأفكار واستخراج نقاط القوة"
        ) {
            showMessage("تحليل الأفكار قيد التجهيز")
        }

        addCard(
            "🔗 ربط الأفكار",
            "ربط الأفكار المتشابهة والمكملة"
        ) {
            showMessage("ربط الأفكار قيد التجهيز")
        }

        addCard(
            "♻️ تطوير الأفكار",
            "تحويل الأفكار إلى مقترحات قابلة للتطوير"
        ) {
            showMessage("تطوير الأفكار قيد التجهيز")
        }

        addCard(
            "📝 ملاحظات المراجعة",
            "حفظ ملاحظات المختصين والخبراء"
        ) {
            showMessage("ملاحظات المراجعة قيد التجهيز")
        }        addCard(
            "✅ نتيجة المراجعة",
            "معرفة نتيجة مراجعة الفكرة"
        ) {
            showMessage("نتيجة المراجعة قيد التجهيز")
        }

        addCard(
            "🚀 جاهزية المشروع",
            "معرفة مدى جاهزية الفكرة للتنفيذ"
        ) {
            showMessage("جاهزية المشروع قيد التجهيز")
        }

        addCard(
            "📋 ملف الفكرة",
            "عرض جميع بيانات الفكرة ومراحلها"
        ) {
            showMessage("ملف الفكرة قيد التجهيز")
        }

        addCard(
            "🏠 الرئيسية",
            "العودة إلى الصفحة الرئيسية"
        ) {
            showHome()
        }        addCard(
            "📊 مؤشرات الأداء",
            "متابعة مؤشرات تقدم المشروع"
        ) {
            showMessage("مؤشرات الأداء قيد التجهيز")
        }

        addCard(
            "🎯 أهداف المشروع",
            "تحديد ومتابعة أهداف المشروع"
        ) {
            showMessage("أهداف المشروع قيد التجهيز")
        }

        addCard(
            "📋 خطة التنفيذ",
            "تنظيم مراحل تنفيذ المشروع"
        ) {
            showMessage("خطة التنفيذ قيد التجهيز")
        }

        addCard(
            "⏱️ المواعيد المهمة",
            "متابعة المواعيد والمراحل القادمة"
        ) {
            showMessage("المواعيد المهمة قيد التجهيز")
        }        addCard(
            "💼 نموذج المشروع",
            "تنظيم طريقة عمل المشروع"
        ) {
            showMessage("نموذج المشروع قيد التجهيز")
        }

        addCard(
            "💰 الميزانية",
            "متابعة احتياجات المشروع المالية"
        ) {
            showMessage("الميزانية قيد التجهيز")
        }

        addCard(
            "📦 الموارد",
            "متابعة الموارد المطلوبة والمتاحة"
        ) {
            showMessage("الموارد قيد التجهيز")
        }

        addCard(
            "👥 فريق التنفيذ",
            "تنظيم المشاركين في تنفيذ المشروع"
        ) {
            showMessage("فريق التنفيذ قيد التجهيز")
        }        addCard(
            "🏢 الجهات المرتبطة",
            "تنظيم الجهات والشركاء المرتبطين"
        ) {
            showMessage("الجهات المرتبطة قيد التجهيز")
        }

        addCard(
            "📑 المتطلبات",
            "متابعة متطلبات المشروع"
        ) {
            showMessage("المتطلبات قيد التجهيز")
        }

        addCard(
            "⚖️ المتطلبات القانونية",
            "متابعة الوثائق والمتطلبات القانونية"
        ) {
            showMessage("المتطلبات القانونية قيد التجهيز")
        }

        addCard(
            "🔐 حماية المشروع",
            "حماية معلومات وبيانات المشروع"
        ) {
            showMessage("حماية المشروع قيد التجهيز")
        }        addCard(
            "🗄️ خزانة المشروع",
            "حفظ وتنظيم ملفات ووثائق المشروع"
        ) {
            showMessage("خزانة المشروع قيد التجهيز")
        }

        addCard(
            "📋 سجل المشروع",
            "متابعة مراحل وقرارات وتحديثات المشروع"
        ) {
            showMessage("سجل المشروع قيد التجهيز")
        }

        addCard(
            "👥 فريق العمل",
            "إدارة أعضاء وفرق العمل المرتبطة بالمشروع"
        ) {
            showMessage("فريق العمل قيد التجهيز")
        }

          addCard(
            "🔔 التنبيهات الإدارية",
            "متابعة التنبيهات والمهام المهمة"
        ) {
            showMessage("التنبيهات الإدارية قيد التجهيز")
        }

        addCard(
            "📊 تقارير المشروع",
            "متابعة التقارير والبيانات الإدارية"
        ) {
            showMessage("تقارير المشروع قيد التجهيز")
        }

        addCard(
            "🕓 سجل التحديثات",
            "متابعة آخر التغييرات التي تمت على المشروع"
        ) {
            showMessage("سجل التحديثات قيد التجهيز")
        }

        addCard(
            "🚧 الخدمات المستقبلية",
            "الخدمات والأقسام التي سيتم تطويرها لاحقًا"
        ) {
            showMessage("الخدمات المستقبلية قيد التجهيز")
        }
