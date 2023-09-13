package tools;

import org.mini.layout.UITemplate;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class GenMapJson {
    String[] mapNames = {
            "城阳谷林一"
            , "姬水部落"
            , "百鬼井一"
            , "孤月岛一"
            , "云中仙城"
            , "天籁岛一"
            , "昌意城"
            , "常羊山一"
            , "玉竹林一"
            , "迷幻云阶一"
            , "昆吾草原"
            , "雷泽一"
            , "姚墟残骸一"
            , "姚墟城堡一"
            , "断骨石窟一"
            , "血池崖一"
            , "紫骨妖城"
            , "万妖冢一"
            , "影牙都市"
            , "妖魂沼泽一"
            , "噬血堂一"
            , "不周山麓一"
            , "轮回森林"
            , "猩红海岛一"
            , "猩红海岸一"
            , "地狱一"
            , "禁锢云海一"
            , "枭山一"
            , "死或生一"
            , "鬼魂牢"
            , "迷幻梦境"
            , "妖魂神庙"
            , "九爪龙洞"
            , "魔炎龙穴"
            , "禁锢天牢"
            , "台前深渊"
            , "神农秘境"
            , "鬼魂牢二层"
            , "九爪龙洞二层"
            , "禁锢天牢二层"
            , "禁锢天牢三层"
            , "魔炎龙穴二层"
            , "魔炎龙穴三层"
            , "台前深渊二层"
            , "台前深渊三层"
            , "神农秘境二层"
            , "神农秘境三层"
            , "城阳谷林二"
            , "血池崖二"
            , "断骨石窟二"
            , "孤月岛二"
            , "鬼魂牢三层"
            , "妖魂神庙二层"
            , "迷幻梦境二层"
            , "九爪龙洞三层"
            , "百鬼井二"
            , "百鬼井三"
            , "天籁岛二"
            , "天籁岛三"
            , "常羊山二"
            , "常羊山三"
            , "迷幻云阶二"
            , "玉竹林二"
            , "雷泽二"
            , "雷泽三"
            , "姚墟残骸二"
            , "姚墟城堡二"
            , "姚墟城堡三"
            , "姚墟城堡四"
            , "姚墟城堡五"
            , "姚墟城堡六"
            , "禁锢云海二"
            , "禁锢云海三"
            , "枭山二"
            , "枭山三"
            , "死或生二"
            , "死或生三"
            , "噬血堂二"
            , "妖魂沼泽二"
            , "妖魂沼泽三"
            , "猩红海岛二"
            , "猩红海岸二"
            , "地狱二"
            , "地狱三"
            , "地狱四"
            , "地狱五"
            , "地狱六"
            , "不周山麓二"
            , "万妖冢二"
            , "万妖冢三"
            , "噩梦谷一"
            , "噩梦谷二"
            , "噩梦谷三"
            , "炼狱魔都"
            , "百鬼魔域一"
            , "百鬼魔域二"
            , "天籁深渊一"
            , "天籁深渊二"
            , "噩梦血池一"
            , "噩梦血池二"
            , "万妖墓穴一"
            , "万妖墓穴二"
            , "无底监牢"
            , "天地心"
            , "清凉世界一"
            , "清凉世界二"
            , "紫竹坟一"
            , "紫竹坟二"
            , "天河界一"
            , "天河界二"
            , "厄雷池一"
            , "厄雷池二"
            , "地狱炎城一"
            , "地狱炎城二"
            , "风云顶一"
            , "风云顶二"
            , "怒炎堡垒一"
            , "怒炎堡垒二"
            , "暴雷峡谷一"
            , "暴雷峡谷二"
            , "竞技场"
            , "竞技场"
            , "竞技场"
            , "天人准备区"
            , "修罗准备区"
            , "寄情福地"
            , "梦萦洞天"
            , "寄情福地"
            , "梦萦洞天"
            , "氏族领地"
            , "氏族领地"
            , "氏族战场"
            , "炎池一"
            , "炎池二"
            , "齐风顶一"
            , "齐风顶二"
            , "火鬼腐宅一"
            , "火鬼腐宅二"
            , "混沌之心一"
            , "混沌之心二"
            , "混沌之心三"
            , "混沌之心四"
            , "火鬼腐宅三"
            , "火鬼腐宅四"
            , "冤魂水屋一"
            , "冤魂水屋二"
            , "冤魂水屋三"
            , "冤魂水屋四"
            , "禁断天涯一"
            , "禁断天涯二"
            , "禁断天涯三"
            , "禁断天涯四"
            , "怒风台"
            , "归去来"
            , "血逍遥"
            , "乾坤角"
            , "十里津"
            , "夺魄牌"
            , "月如刀"
            , "齐天堡"
            , "青玄神殿"
            , "九幽冥地"
            , "九黎土丘一"
            , "九黎土丘二"
            , "九黎土丘三"
            , "残溶洞一层"
            , "残溶洞二层"
            , "堕落之路一"
            , "堕落之路二"
            , "堕落之路三"
            , "炼狱之海一层"
            , "炼狱之海二层"
            , "流波山一"
            , "流波山二"
            , "流波山三"
            , "颛顼之墟一层"
            , "颛顼之墟二层"
            , "涿鹿荒野一"
            , "涿鹿荒野二"
            , "涿鹿荒野三"
            , "遗忘坟场一层"
            , "遗忘坟场二层"
            , "伊洛平原一"
            , "伊洛平原二"
            , "伊洛平原三"
            , "虚空神界一层"
            , "虚空神界二层"
            , "兰陵幽墓一层"
            , "兰陵幽墓二层"
            , "灵台虚界一"
            , "灵台虚界二"
            , "灵台虚界三"
            , "灵台虚界四"
            , "紫杨竹林一"
            , "紫杨竹林二"
            , "紫杨竹林三"
            , "紫杨竹林四"
            , "幻天镜一"
            , "幻天镜二"
            , "蛮荒涿一"
            , "蛮荒涿二"
            , "农场一"
            , "农场二"
            , "农场三"
            , "农场一"
            , "农场二"
            , "农场三"
            , "族地镇"
            , "始祖园"
            , "族地镇"
            , "始祖园"
            , "蛮荒涿一"
            , "蛮荒涿二"
            , "移动营业厅"
            , "移动营业厅"
            , "移动营业厅"
            , "移动营业厅"
            , "幽魂宅一"
            , "幽魂宅二"
            , "幽魂宅三"
            , "幽魂宅四"
            , "梦魇乡一"
            , "梦魇乡二"
            , "梦魇乡三"
            , "梦魇乡四"
            , "死气洞穴一"
            , "死气洞穴二"
            , "死气洞穴三"
            , "死气洞穴四"
            , "荒坟草场一"
            , "荒坟草场二"
            , "荒坟草场三"
            , "荒坟草场四"
            , "无梦仙乡一"
            , "无梦仙乡二"
            , "无梦仙乡三"
            , "无梦仙乡四"
            , "百花谷一"
            , "百花谷二"
            , "无情海岸一"
            , "无情海岸二"
            , "小池镇"
            , "鬼阴山山脚"
            , "鬼阴山山腰"
            , "鬼阴山山顶"
            , "幽魂沼泽一"
            , "幽魂沼泽二"
            , "黑岩洞一"
            , "黑岩洞二"
            , "血色古堡"
            , "万魔古窟一"
            , "万魔古窟二"
            , "万魔古窟三"
            , "万魔古窟四"
            , "火鬼腐宅五"
            , "火鬼腐宅六"
            , "冤魂水屋五"
            , "冤魂水屋六"
            , "幻愿界一"
            , "幻愿界二"
            , "幻愿界三"
            , "幻愿界四"
            , "神水府邸一"
            , "神水府邸二"
            , "神水府邸三"
            , "神水府邸四"
    };

    int[][] substitue = {
            {126, 125}
            , {127, 125}
            , {128, 125}
            , {144, 136}
            , {145, 137}
            , {146, 142}
            , {147, 143}
            , {148, 138}
            , {149, 139}
            , {150, 140}
            , {151, 141}
            , {193, 189}
            , {194, 190}
            , {195, 191}
            , {196, 192}
            , {204, 201}
            , {205, 202}
            , {206, 203}
            , {209, 207}
            , {210, 208}
            , {211, 200}
            , {212, 199}
            , {214, 213}
            , {215, 213}
            , {216, 213}
            , {221, 217}
            , {222, 218}
            , {223, 219}
            , {224, 220}
            , {225, 217}
            , {226, 218}
            , {227, 219}
            , {228, 220}
            , {229, 217}
            , {230, 218}
            , {231, 219}
            , {232, 220}
            , {233, 217}
            , {234, 218}
            , {235, 219}
            , {236, 220}
            , {256, 254}
            , {257, 255}
            , {258, 189}
            , {259, 190}
            , {260, 191}
            , {261, 192}
            , {262, 189}
            , {263, 190}
            , {264, 191}
            , {265, 192}
    };
    String jsonPath = "./src/main/resource/res/map/";
    String hightMapPath = "./src/main/resource/res/textures/terrain/";

    String template = "{\n" +
            "  \"desc\": \"{NAME},{COLS}x{ROWS},\",\n" +
            "  \"name\": \"{NAME}\",\n" +
            "  \"cols\": {COLS},\n" +
            "  \"rows\": {ROWS},\n" +
            "  \"deftex\": \"textures/floor/floor_grass_0\",\n" +
            "  \"rtex\": \"textures/floor/floor_mud_0\",\n" +
            "  \"gtex\": \"textures/floor/floor_stone_1\",\n" +
            "  \"btex\": \"textures/floor/floor_fens_0\",\n" +
            "  \"blendMap\": \"textures/terrain/b_{MAPID}\",\n" +
            "  \"highMap\": \"textures/terrain/h_{MAPID}\",\n" +
            "  \"waterTileSize\": 30,\n" +
            "  \"waters\": [\n" +
            "  ]\n" +
            "}";

    public static void main(String[] args) {
        new GenMapJson().gen();
    }

    int getSubstitue(int id) {
        for (int i = 0; i < substitue.length; i++) {
            if (substitue[i][0] == id) {
                return substitue[i][1];
            }
        }
        return id;
    }

    void gen() {

        MediaTracker tracker = new MediaTracker(new Frame());
        StringBuilder sb = new StringBuilder();
        StringBuilder sb1 = new StringBuilder();
        for (int i = 1000; i < mapNames.length; i++) {//改初值生成文件
            try {
                int substitue = getSubstitue(i);
                sb.setLength(0);
                sb1.setLength(0);
                sb.append(i);
                sb1.append(substitue);
                if (sb.length() < 2) {
                    sb.insert(0, "00");
                    sb1.insert(0, "00");
                } else if (sb.length() < 3) {
                    sb.insert(0, "0");
                    sb1.insert(0, "0");
                }
                String pngName = "h_" + sb1 + ".png";
                Image img = Toolkit.getDefaultToolkit().createImage(hightMapPath + pngName);
                tracker.addImage(img, 0);
                tracker.waitForAll();
                System.out.println("map =" + sb.toString() + "  " + img.getWidth(null) + ", " + img.getHeight(null));

                UITemplate tmp = new UITemplate(template);
                UITemplate.getVarMap().put("NAME", mapNames[i]);
                UITemplate.getVarMap().put("COLS", img.getWidth(null));
                UITemplate.getVarMap().put("ROWS", img.getHeight(null));
                UITemplate.getVarMap().put("MAPID", sb1.toString());

                File file = new File(jsonPath + "map_" + sb + ".json");
                FileOutputStream fos = new FileOutputStream(file);
                PrintStream ps = new PrintStream(fos);
                ps.print(tmp.parse());
                ps.flush();
                fos.flush();
                tracker.removeImage(img);
                ps.close();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
