package page.info.edit

import common.CommonStatic
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
import common.util.stage.EStage
import common.util.stage.StageMap
import common.util.stage.StageMap.StageMapInfo
import common.util.unit.UnitLevel
import io.BCPlayer
import page.JL
import page.JTF
import page.JTG
import page.Page
import page.anim.AnimBox
import page.info.edit.ProcTable.AtkProcTable
import page.support.ListJtfPolicy
import page.support.SortTable
import page.view.ViewBox
import page.view.ViewBox.Conf
import page.view.ViewBox.Controller
import page.view.ViewBox.VBExporter
import utilpc.Interpret
import java.awt.event.ActionEvent
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.util.function.Consumer
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JScrollPane
import javax.swing.SwingConstants

internal class AtkEditTable(p: Page?, edit: Boolean, unit: Boolean) : Page(p) {
    private val latk: JL = JL(1, "atk")
    private val lpre: JL = JL(1, "preaa")
    private val lp0: JL = JL(1, "p0")
    private val lp1: JL = JL(1, "p1")
    private val ltp: JL = JL(1, "type")
    private val ldr: JL = JL(1, "dire")
    private val lct: JL = JL(1, "count")
    private val lab: JL = JL(1, "ability")
    private val lmv: JL = JL(1, "move")
    private val fatk: JTF = JTF()
    private val fpre: JTF = JTF()
    private val fp0: JTF = JTF()
    private val fp1: JTF = JTF()
    private val ftp: JTF = JTF()
    private val fdr: JTF = JTF()
    private val fct: JTF = JTF()
    private val fab: JTF = JTF()
    private val fmv: JTF = JTF()
    private val isr: JTG = JTG(1, "isr")
    private val ljp: ListJtfPolicy = ListJtfPolicy()
    private val apt: AtkProcTable
    private val jsp: JScrollPane
    private val editable: Boolean
    private var mul = 0.0
    private val changing = false
    var adm: AtkDataModel? = null
    override fun callBack(o: Any?) {
        front.callBack(o)
    }

    override fun resized(x: Int, y: Int) {
        Page.Companion.set(latk, x, y, 0, 0, 200, 50)
        Page.Companion.set(lpre, x, y, 0, 50, 200, 50)
        Page.Companion.set(lp0, x, y, 0, 100, 200, 50)
        Page.Companion.set(lp1, x, y, 0, 150, 200, 50)
        Page.Companion.set(ltp, x, y, 0, 200, 200, 50)
        Page.Companion.set(ldr, x, y, 0, 250, 200, 50)
        Page.Companion.set(lct, x, y, 0, 300, 200, 50)
        Page.Companion.set(lab, x, y, 0, 350, 200, 50)
        Page.Companion.set(lmv, x, y, 0, 400, 200, 50)
        Page.Companion.set(fatk, x, y, 200, 0, 200, 50)
        Page.Companion.set(fpre, x, y, 200, 50, 200, 50)
        Page.Companion.set(fp0, x, y, 200, 100, 200, 50)
        Page.Companion.set(fp1, x, y, 200, 150, 200, 50)
        Page.Companion.set(ftp, x, y, 200, 200, 200, 50)
        Page.Companion.set(fdr, x, y, 200, 250, 200, 50)
        Page.Companion.set(fct, x, y, 200, 300, 200, 50)
        Page.Companion.set(fab, x, y, 200, 350, 200, 50)
        Page.Companion.set(fmv, x, y, 200, 400, 200, 50)
        Page.Companion.set(isr, x, y, 200, 450, 200, 50)
        apt.setPreferredSize(Page.Companion.size(x, y, 750, 2100).toDimension())
        apt.resized(x, y)
        Page.Companion.set(jsp, x, y, 450, 0, 800, 950)
    }

    fun setData(data: AtkDataModel?, multi: Double) {
        adm = data
        mul = multi
        fatk.setText("" + (adm.atk * mul) as Int)
        fpre.setText("" + adm.pre)
        fp0.setText("" + adm.ld0)
        fp1.setText("" + adm.ld1)
        ftp.setText("" + adm.targ)
        apt.setData(if (adm.ce.common) adm.ce.rep.proc else adm.proc)
        fdr.setText("" + adm.dire)
        fct.setText("" + adm.count)
        fmv.setText("" + adm.move)
        var alt: Int = adm.getAltAbi()
        var i = 0
        var str = "{"
        while (alt > 0) {
            if (alt and 1 == 1) {
                if (str.length > 1) str += ","
                str += i
            }
            alt = alt shr 1
            i++
        }
        fab.setText("$str}")
        isr.setSelected(adm.range)
    }

    private fun ini() {
        set(latk)
        set(lpre)
        set(lp0)
        set(lp1)
        set(ltp)
        set(ldr)
        set(lct)
        set(lab)
        set(lmv)
        set(fatk)
        set(fpre)
        set(fp0)
        set(fp1)
        set(ftp)
        set(fdr)
        set(fct)
        set(fab)
        set(fmv)
        add(isr)
        ftp.setToolTipText(
                "<html>" + "+1 for normal attack<br>" + "+2 to attack kb<br>" + "+4 to attack underground<br>"
                        + "+8 to attack corpse<br>" + "+16 to attack soul<br>" + "+32 to attack ghost</html>")
        fdr.setToolTipText("direction, 1 means attack enemies, 0 means not an attack, -1 means assist allies")
        fpre.setToolTipText(
                "<html>use 0 for random attack attaching to previous one.<br>pre=0 for first attack will invalidate it</html>")
        var ttt = ("<html>enter ID of abilities separated by comma or space.<br>" + "it changes the ability state"
                + "(has to hot has, not has to has)<br>"
                + "it won't change back until you make another attack to change it<br>")
        for (i in Interpret.SABIS.indices) ttt += i.toString() + ": " + Interpret.SABIS.get(i) + "<br>"
        fab.setToolTipText("$ttt</html>")
        add(jsp)
        isr.setEnabled(editable)
        jsp.getVerticalScrollBar().setUnitIncrement(10)
        focusTraversalPolicy = ljp
        isFocusCycleRoot = true
        isr.setLnr(Consumer { x: ActionEvent? -> adm.range = isr.isSelected() })
    }

    private fun input(jtf: JTF, text: String) {
        if (text.length > 0) {
            if (jtf === fab) {
                val ent: IntArray = CommonStatic.parseIntsN(text)
                var ans = 0
                for (i in ent) if (i >= 0 && i < Interpret.ABIS.size) ans = if (ans == -1) 1 shl i else ans or (1 shl i)
                adm.alt = ans
            }
            var v: Int = CommonStatic.parseIntN(text)
            if (jtf === fatk) {
                v /= mul.toInt()
                adm.atk = v
            }
            if (jtf === fpre) {
                if (v < 0) v = 1
                adm.pre = v
            }
            if (jtf === fp0) {
                adm.ld0 = v
                if (adm.ld0 != 0 || adm.ld1 != 0) if (adm.ld1 <= v) adm.ld1 = v + 1
            }
            if (jtf === fp1) {
                adm.ld1 = v
                if (adm.ld0 != 0 || adm.ld1 != 0) if (adm.ld0 >= v) adm.ld0 = v - 1
            }
            if (jtf === ftp) {
                if (v < 1) v = 1
                adm.targ = v
            }
            if (jtf === fdr) {
                if (v < -1) v = -1
                if (v > 1) v = 1
                adm.dire = v
            }
            if (jtf === fct) {
                if (v < 0) v = -1
                adm.count = v
            }
            if (jtf === fmv) adm.move = v
        }
        callBack(null)
    }

    private fun set(jl: JLabel) {
        jl.horizontalAlignment = SwingConstants.CENTER
        jl.border = BorderFactory.createEtchedBorder()
        add(jl)
    }

    private fun set(jtf: JTF) {
        jtf.setEditable(editable)
        add(jtf)
        ljp.add(jtf)
        jtf.addFocusListener(object : FocusAdapter() {
            override fun focusLost(fe: FocusEvent?) {
                if (changing) return
                input(jtf, jtf.getText())
                callBack(null)
            }
        })
    }

    companion object {
        private const val serialVersionUID = 1L
    }

    init {
        apt = AtkProcTable(this, edit, unit)
        jsp = JScrollPane(apt)
        editable = edit
        ini()
    }
}