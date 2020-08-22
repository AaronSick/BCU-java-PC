package page.view

import common.io.assets.Admin
import common.io.assets.Admin.StaticPermitted
import common.io.assets.AssetLoader
import common.io.assets.AssetLoader.AssetHeader
import common.io.assets.AssetLoader.AssetHeader.AssetEntry
import common.io.json.JsonEncoder
import common.io.json.Test
import common.io.json.Test.JsonTest_0.JsonD
import common.io.json.Test.JsonTest_2
import common.pack.Source.AnimLoader
import common.pack.Source.ResourceLocation
import common.pack.Source.SourceAnimLoader
import common.pack.Source.SourceAnimSaver
import common.pack.Source.Workspace
import common.pack.Source.ZipSource
import common.pack.UserProfile
import common.system.Node
import common.util.anim.AnimU.UType
import common.util.stage.EStage
import common.util.stage.StageMap
import common.util.stage.StageMap.StageMapInfo
import common.util.unit.Form
import common.util.unit.Unit
import common.util.unit.UnitLevel
import io.BCPlayer
import page.JBTN
import page.JL
import page.Page
import page.anim.AnimBox
import page.info.UnitInfoPage
import page.support.ListJtfPolicy
import page.support.SortTable
import page.support.UnitLCR
import page.view.ViewBox
import page.view.ViewBox.Conf
import page.view.ViewBox.Controller
import page.view.ViewBox.VBExporter
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.*
import javax.swing.JList
import javax.swing.JScrollPane
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class UnitViewPage(p: Page?, private val pac: String) : AbViewPage(p) {
    private val jlu: JList<Unit> = JList<Unit>()
    private val jspu: JScrollPane = JScrollPane(jlu)
    private val jlf: JList<Form> = JList<Form>()
    private val jspf: JScrollPane = JScrollPane(jlf)
    private val stat: JBTN = JBTN(0, "stat")

    constructor(p: Page?, u: Unit) : this(p, u.getID().pack) {
        jlu.setSelectedValue(u, true)
    }

    protected override fun resized(x: Int, y: Int) {
        super.resized(x, y)
        Page.Companion.set(jspu, x, y, 50, 100, 300, 1100)
        Page.Companion.set(jspf, x, y, 400, 100, 300, 400)
        Page.Companion.set(stat, x, y, 400, 1000, 300, 50)
    }

    protected override fun updateChoice() {
        val f: Form = jlf.getSelectedValue() ?: return
        setAnim<UType>(f.anim)
    }

    private fun addListeners() {
        jlu.addListSelectionListener(object : ListSelectionListener {
            override fun valueChanged(arg0: ListSelectionEvent) {
                if (arg0.getValueIsAdjusting()) return
                val u: Unit = jlu.getSelectedValue() ?: return
                var ind: Int = jlf.getSelectedIndex()
                if (ind == -1) ind = 0
                jlf.setListData(u.forms)
                jlf.setSelectedIndex(if (ind < u.forms.size) ind else 0)
            }
        })
        jlf.addListSelectionListener(object : ListSelectionListener {
            override fun valueChanged(arg0: ListSelectionEvent) {
                if (arg0.getValueIsAdjusting()) return
                updateChoice()
            }
        })
        stat.addActionListener(object : ActionListener {
            override fun actionPerformed(e: ActionEvent?) {
                val u: Unit = jlu.getSelectedValue() ?: return
                val n: Node<Unit> = Node.Companion.getList<Unit>(UserProfile.Companion.getAll<Unit>(pac, Unit::class.java), u)
                changePanel(UnitInfoPage(getThis(), n))
            }
        })
    }

    private fun ini() {
        preini()
        add(jspu)
        add(jspf)
        add(stat)
        jlu.setCellRenderer(UnitLCR())
        jlf.setCellRenderer(UnitLCR())
        addListeners()
    }

    companion object {
        private const val serialVersionUID = 2010L
    }

    init {
        jlu.setListData(Vector<Unit>(UserProfile.Companion.getAll<Unit>(pac, Unit::class.java)))
        ini()
        resized()
    }
}