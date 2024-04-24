package com.psllab.pallettrackingfixedreader

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.widget.TabHost
import com.seuic.androidreader.bean.SearchParams
import com.seuic.androidreader.bean.TagInfo
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayList
import java.util.LinkedHashMap

class App : Application() {
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit   var mContext:Context
    }

    override fun onCreate() {
        super.onCreate()
        mContext =this
    }
    var Constr_READ = "读"
    var Constr_CONNECT = "连接"
    var Constr_INVENTORY = "盘点"
    var Constr_RWLOP = "读写锁"
    var Constr_set = "设置"
    var Constr_SetFaill = "设置失败："
    var Constr_GetFaill = "获取失败："
    var Constr_SetOk = "设置成功"
    var Constr_unsupport = "不支持"
    var Constr_Putandexit = "再按一次退出程序"
    var Coname = arrayOf(
        "序号", "EPC ID", "次数", "天线",
        "协议", "RSSI", "频率", "RFU", "时间戳", "附加数据 ", "TID", "BID"
    )

    var Constr_stopscan = "请先停止扫描"
    var Constr_hadconnected = "已经连接"
    var Constr_plsetuuid = "请设置好UUID:"
    var Constr_pwderror = "密码错误"
    var Constr_search = "搜索"
    var Constr_stop = "停止"

    var Constr_createreaderok = "读写器创建失败"
    var pdaatpot = arrayOf("一天线", "双天线", "三天线", "四天线", "16天线")

    var spibank = arrayOf("保留区", "EPC区", "TID区", "用户区")
    var spifbank = arrayOf("EPC区", "TID区", "用户区")
    lateinit var regtype: Array<String>
    var spilockbank = arrayOf(
        "访问密码", "销毁密码", "EPCbank",
        "TIDbank", "USERbank"
    )
    var spilocktype = arrayOf("解锁定", "暂时锁定", "永久锁定")
    var Constr_sub3readmem = "读标签"
    var Constr_sub3writemem = "写标签"
    var Constr_sub3lockkill = "锁与销毁"
    var Constr_sub3readfail = "读失败:"
    var Constr_sub3nodata = "无数据"
    var Constr_sub3wrtieok = "写成功"
    var Constr_sub3writefail = "写失败:"
    var Constr_sub3lockok = "锁成功"
    var Constr_sub3lockfail = "锁失败:"
    var Constr_sub3killok = "销毁成功"
    var Constr_sub3killfial = "销毁失败:"

    // String[] spireg={"中国","北美","日本","韩国","欧洲","印度","加拿大","全频段"
    // ,"中国2"};
    var spireg = arrayOf(
        "中国", "北美", "日本", "韩国", "欧洲", "欧洲2",
        "欧洲3", "印度", "加拿大", "全频段", "中国2"
    )
    var spinvmo = arrayOf("普通模式", "高速模式")
    var spitari = arrayOf("25微秒", "12.5微秒", "6.25微秒")
    var spiwmod = arrayOf("字写", "块写")
    var spiqmode = arrayOf("单标签手持机模式", "多标签快速模式", "多标签手持机模式", "多标签智能模式")
    lateinit var gpodemo: Array<String>

    var Auto = "自动"
    var No = "无"
    var Constr_sub4invenpra = "盘点参数"
    var Constr_sub4antpow = "天线功率"
    var Constr_sub4regionfre = "区域频率"
    var Constr_sub4gen2opt = "Gen2项"
    var Constr_sub4invenfil = "盘点过滤"
    var Constr_sub4addidata = "附加数据"
    var Constr_sub4others = "其他参数"
    var Constr_sub4quickly = "快速模式"
    var Constr_sub4setmodefail = "配置模式失败"
    var Constr_sub4setokresettoab = "设置成功，重启读写器生效"
    var Constr_sub4ndsapow = "该设备需要功率一致"
    var Constr_sub4unspreg = "不支持的区域"

    var spiregbs = arrayOf("北美", "中国", "欧频", "中国2")
    var Constr_subblmode = "模式"
    var Constr_subblinven = "盘点"
    var Constr_subblfil = "过滤"
    var Constr_subblfre = "频率"
    var Constr_subblnofre = "没有选择频点"

    var cusreadwrite = arrayOf("读操作", "写操作")
    var cuslockunlock = arrayOf("锁", "解锁")

    var Constr_subcsalterpwd = "改密码"
    var Constr_subcslockwpwd = "带密码锁"
    var Constr_subcslockwoutpwd = "不带密码锁"
    var Constr_subcsplsetimeou = "请设置超时时间"
    var Constr_subcsputcnpwd = "填入当前密码与新密码"
    var Constr_subcsplselreg = "请选择区域"
    var Constr_subcsopfail = "操作失败:"
    var Constr_subcsputcurpwd = "填入当前密码"

    var Constr_subdbdisconnreconn = "已经断开,正在重新连接"
    var Constr_subdbhadconnected = "已经连接"
    var Constr_subdbconnecting = "正在连接......"
    var Constr_subdbrev = "接收"
    var Constr_subdbstop = "停止"
    var action_db_down = ""
    var Constr_subdbdalennot = "数据长度不对"
    var Constr_subdbplpuhexchar = "请输入16进制字符"

    var Constr_subsysaveok = "保存成功"
    var Constr_subsysout = "输入txt或者csv"
    var Constr_subsysreavaid = "重新连接生效"
    var Constr_sub1recfailed = "重新连接失败"
    var Constr_subsysavefailed = "保存失败"
    var Constr_subsysexefin = "执行完毕"
    var Constr_sub1adrno = "地址没有输入"
    var Constr_sub1pdtsl = "请选择平台"
    var Constr_mainpu = "上电："
    var Constr_nostopstreadfailed = "不停顿盘点启动失败"
    var Constr_nostopspreadfailed = "不停顿盘点停止失败"
    var Constr_nostopreadfailed = "开始盘点失败："
    var Constr_connectok = "连接成功"
    var Constr_connectfialed = "连接失败"
    var Constr_disconpowdown = "断开读写器，下电："
    var Constr_ok = "成功:"
    var Constr_failed = "失败:"
    var Constr_excep = "异常:"
    var Constr_setcep = "设置异常:"
    var Constr_getcep = "获取异常:"
    var Constr_killok = "KILL成功"
    var Constr_killfailed = "KILL失败"
    var Constr_psiant = "请选择盘点天线"
    var Constr_selpro = "请选择协议"
    var Constr_setpwd = "设置功率:"
    var Constr_carry_fftable = "频率-反射 图表"
    var Constr_carry_frtable = "频率-回波 图表"
    var Constr_carry_binvpw = "盘点前前向功率:		 "
    var Constr_carry_binvfpw = "盘点前反向功率:		 "
    var Constr_carry_invc = "盘点标签个数:		  "
    var Constr_carry_invtep = "盘点温度:		      "
    var Constr_carry_invpw = "盘点前向功率:		   "
    var Constr_carry_ainvfpw = "盘点后反向功率:		 "
    var Constr_carry_ainvpw = "盘点后前向功率 :		"

    // */

    // */
    /*
	 * 公共变量 public var
	 */
    var TagsMap: Map<String, TagInfo> = LinkedHashMap() // 有序

    // sorted
    var path: String? = null
    var ThreadMODE = 0
    var refreshtime = 1000
    var Mode = 0
    var m: Map<String, String>? = null
    var tabHost: TabHost? = null
    var exittime: Long = 0
    var needreconnect = false
    var haveinit = false
    //    var Mreader: Reader? = null
    var antportc = 0
    var Curepc: String? = null
    var Bank = 0
    var BackResult = 0
    //public deviceVersion dv;

    //public deviceVersion dv;
    var gpodemomode = 0
    var gpodemotime = 20
    var gpodemo1 = false
    var gpodemo2 = false
    var gpodemo3 = false
    var gpodemo4 = false



    var Rparams: ReaderParams? = null
    var Address: String? = null
    var isquicklymode = false
    var issmartmode = false
    var needlisen = false
    var nxpu8 = 0
    var m_BROption = SearchParams()
    var isUniByEmd // 是否附加数据唯一(whether addition data unique)
            = false
    var isUniByAnt // 是否天线号唯一(whether antenna id unique)
            = false

    var stoptimems: Long = 0
    var stopcount = 0
    var isstoptime = false
    var isstopcount = false
    var latetimems: Long = 0
    var islatetime = false
    var Vtestforcount //测试循环次数
            = 0
    var Vtestforsleep //测试循环间隔
            = 0
    var VisTestFor = false //是否循环测试

    var VisStatics = false

    var isEpcup = false
    var isReport_pos = false
    var isReport_rec = false
    var isReport_tep = false
    var isFastID = false
    var isTagfoucs = false

    var file: File? = null
    var fs: FileOutputStream? = null

//    var myhd: Reader.HardwareDetails? = null

    // 将标签关联物品显示(show the relation good)
    var isconngoods = false
    var listName: MutableMap<String, String>? = null
    var issound = true

    var allhtb = intArrayOf(
        915750, 915250, 903250, 926750, 926250,
        904250, 927250, 920250, 919250, 909250, 918750, 917750, 905250,
        904750, 925250, 921750, 914750, 906750, 913750, 922250, 911250,
        911750, 903750, 908750, 905750, 912250, 906250, 917250, 914250,
        907250, 918250, 916250, 910250, 910750, 907750, 924750, 909750,
        919750, 916750, 913250, 923750, 908250, 925750, 912750, 924250,
        921250, 920750, 922750, 902750, 923250
    )
    //    var ei = ErrInfo()
    var qmode = 0

    class ReaderParams {
        // save param
        var opant = 1
        var invpro: MutableList<String>
        var opro: String
        var uants: IntArray
        var readtime: Int
        var sleep: Int
        var checkant: Int
        var rpow: IntArray
        var crpow: Int
        var wpow: IntArray
        var region: Int
        lateinit var frecys: IntArray
        var frelen: Int
        var session: Int
        var qv: Int
        var wmode: Int
        var blf: Int
        var maxlen: Int
        var target: Int
        var gen2code: Int
        var gen2tari: Int
        var fildata: String
        var filadr: Int
        var filbank: Int
        var filisinver: Int
        var filenable: Int
        var emdadr: Int
        var emdbytec: Int
        var emdbank: Int
        var emdenable: Int
        var antq = 0
        var adataq: Int
        var rhssi: Int
        var invw: Int
        var iso6bdeep: Int
        var iso6bdel: Int
        var iso6bblf: Int
        var option: Int

        // other params
        var password: String? = null
        var optime: Int
        fun setdefaulpwval(`val`: Int) {
            crpow = `val`
            rpow = intArrayOf(
                crpow, crpow, crpow, crpow, crpow, crpow, crpow,
                crpow, crpow, crpow, crpow, crpow, crpow, crpow, crpow,
                crpow
            )
            wpow = intArrayOf(
                crpow, crpow, crpow, crpow, crpow, crpow, crpow,
                crpow, crpow, crpow, crpow, crpow, crpow, crpow, crpow,
                crpow
            )
        }

        init {
            invpro = ArrayList()
            invpro.add("GEN2")
            uants = IntArray(1)
            uants[0] = 1
            sleep = 0
            readtime = 50
            optime = 1000
            opro = "GEN2"
            checkant = 1
            crpow = 2000
            // rpow=new
            // int[]{2700,2000,2000,2000,2000,2000,2000,2000,2000,2000,2000,2000,2000,2000,2000,2000};
            rpow = intArrayOf(
                crpow, crpow, crpow, crpow, crpow, crpow, crpow,
                crpow, crpow, crpow, crpow, crpow, crpow, crpow, crpow,
                crpow
            )
            wpow = intArrayOf(
                crpow, crpow, crpow, crpow, crpow, crpow, crpow,
                crpow, crpow, crpow, crpow, crpow, crpow, crpow, crpow,
                crpow
            )
            region = 1
            frelen = 0
            session = 0
            qv = -1
            wmode = 0
            blf = 0
            maxlen = 0
            target = 0
            gen2code = 2
            gen2tari = 0
            fildata = ""
            filadr = 4
            filbank = 1
            filisinver = 0
            filenable = 0
            emdadr = 0
            emdbytec = 0
            emdbank = 1
            emdenable = 0
            adataq = 0
            rhssi = 1
            invw = 0
            iso6bdeep = 0
            iso6bdel = 0
            iso6bblf = 0
            option = 0
        }
    }
}