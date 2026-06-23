package com.vocab.data;

import java.util.List;
import java.util.Map;

public class WordListData {
    public static final int TOTAL_CORPUS_SIZE = 20000;

    public record Band(String name, String label, int rankMin, int rankMax, List<String> words, int sampleCount) {}

    public static final List<Band> BANDS = List.of(
        new Band("1k", "1-1000 最高频", 1, 1000, List.of(
            "the","be","of","and","to","in","that","have","it","for",
            "not","on","with","he","as","you","do","at","this","but",
            "his","by","from","they","we","say","her","she","or","an",
            "will","my","one","all","would","there","their","what","so","up",
            "out","if","about","who","get","which","go","me","when","make",
            "can","like","time","no","just","him","know","take","people","into",
            "year","your","good","some","could","them","see","other","than","then",
            "now","look","only","come","its","over","think","also","back","after",
            "use","two","how","our","work","first","well","way","even","new",
            "want","because","any","these","give","day","most","us","great","big"
        ), 40),
        new Band("2k", "1001-2000", 1001, 2000, List.of(
            "able","accept","across","act","actually","add","address","admit","afraid","afternoon",
            "age","ago","agree","ahead","air","allow","almost","alone","along","already",
            "although","always","among","amount","animal","another","answer","anyone","anything","appear",
            "area","arm","around","arrive","art","ask","away","baby","bad","bag",
            "ball","bank","bar","base","bear","beat","beautiful","became","bed","behind",
            "believe","best","better","bill","bit","black","blood","blow","blue","board",
            "boat","body","book","born","both","box","boy","break","bring","brother",
            "brown","build","building","business","buy","call","car","care","carry","case",
            "catch","cause","center","certain","chance","change","check","child","church","city",
            "class","clean","clear","close","club","cold","college","color","common","company"
        ), 30),
        new Band("3k", "2001-3000", 2001, 3000, List.of(
            "compare","complete","computer","concern","condition","consider","contain","continue","control","cost",
            "country","couple","course","cover","create","culture","cup","current","cut",
            "data","daughter","dead","deal","death","debate","decide","decision","deep","defense",
            "degree","demand","describe","design","despite","detail","determine","develop","difference","different",
            "difficult","dinner","direction","discover","discuss","discussion","disease","doctor","dog","door",
            "down","draw","dream","drive","drop","drug","during","each","early","east",
            "easy","eat","economic","economy","edge","education","effect","effort","eight","either",
            "election","else","employee","energy","enjoy","enough","enter","entire","environment","especially",
            "evening","event","ever","every","everybody","everyone","everything","evidence","exactly","example",
            "executive","exist","expect","experience","expert","explain","eye","face","fact","factor"
        ), 30),
        new Band("4k", "3001-4000", 3001, 4000, List.of(
            "fail","fall","family","far","fast","father","fear","federal","feel","feeling",
            "field","fight","figure","fill","film","final","finally","financial","find","fine",
            "finger","finish","fire","firm","fish","five","floor","fly","focus","follow",
            "food","foot","football","force","foreign","forget","form","former","forward","four",
            "free","friend","front","full","fund","future","game","garden","gas","general",
            "generation","girl","glass","goal","god","gold","gone","government","green","ground",
            "group","grow","growth","guess","gun","guy","hair","half","hand","hang",
            "happen","happy","hard","head","health","hear","heart","heavy","help","here",
            "high","history","hit","hold","home","hope","horse","hospital","hot","hotel",
            "hour","house","huge","human","hundred","husband","idea","identify","image","imagine"
        ), 30),
        new Band("5k", "4001-5000", 4001, 5000, List.of(
            "impact","important","improve","include","increase","indeed","indicate","individual","industry","information",
            "inside","instead","interest","international","interview","investment","involve","issue","item","job",
            "join","keep","key","kid","kill","kind","kitchen","land","language","large",
            "last","late","later","laugh","law","lawyer","lay","lead","leader","learn",
            "least","leave","left","legal","less","let","letter","level","lie","life",
            "light","likely","line","list","listen","little","live","local","long","lose",
            "loss","lot","love","low","machine","magazine","main","maintain","major","majority",
            "man","manage","management","manager","many","market","marriage","material","matter","may",
            "maybe","mean","measure","media","medical","meet","meeting","member","memory","mention",
            "message","method","middle","might","military","million","mind","minute","miss","mission"
        ), 25),
        new Band("6k", "5001-6000", 5001, 6000, List.of(
            "model","modern","moment","money","month","more","morning","mother","mouth","move",
            "movement","movie","much","music","must","myself","name","nation","national","natural",
            "nature","near","nearly","necessary","need","network","never","news","newspaper","next",
            "nice","night","none","nor","north","note","nothing","notice","number","occur",
            "offer","office","officer","official","often","oil","old","once","open","operation",
            "opportunity","option","order","organization","other","outside","own","page","pain","painting",
            "paper","parent","part","participant","particular","particularly","partner","party","pass","past",
            "patient","pattern","pay","peace","people","per","perform","performance","perhaps","period",
            "person","personal","phone","physical","pick","picture","piece","place","plan","plant",
            "play","player","please","point","police","policy","political","politics","poor","popular"
        ), 25),
        new Band("7k", "6001-7000", 6001, 7000, List.of(
            "population","position","positive","possible","power","practice","prepare","present","president","press",
            "pressure","pretty","prevent","price","private","probably","problem","process","produce","product",
            "production","professional","professor","program","project","property","protect","prove","provide","public",
            "pull","purpose","push","put","quality","question","quickly","quite","race","radio",
            "raise","range","rate","rather","reach","read","ready","real","reality","realize",
            "really","reason","receive","recent","recently","recognize","record","red","reduce","reflect",
            "region","relate","relationship","religious","remain","remember","remove","report","represent","require",
            "research","resource","respond","response","rest","result","return","reveal","rich","right",
            "rise","risk","road","rock","role","room","rule","run","safe","same",
            "save","scene","school","science","scientist","score","sea","season","seat","second"
        ), 25),
        new Band("8k", "7001-8000", 7001, 8000, List.of(
            "section","security","seek","seem","senior","sense","series","serious","serve","service",
            "set","seven","several","share","shoot","short","shot","shoulder","show","side",
            "sign","significant","similar","simple","simply","since","sing","single","sister","sit",
            "site","situation","six","size","skill","small","smile","social","society","soldier",
            "somebody","someone","something","sometimes","son","song","soon","sort","sound","south",
            "space","speak","special","specific","speech","spend","sport","spring","staff","stage",
            "stand","standard","star","start","state","statement","station","stay","step","still",
            "stock","stop","store","story","strategy","street","strong","structure","student","study",
            "stuff","style","subject","success","successful","such","suddenly","suffer","suggest","summer",
            "support","sure","surface","system","table","talk","task","tax","teach","teacher"
        ), 25),
        new Band("9k", "8001-9000", 8001, 9000, List.of(
            "team","technology","television","tell","ten","tend","term","test","than","thank",
            "that","their","them","themselves","then","theory","there","these","they","thing",
            "think","third","this","those","though","thought","thousand","threat","three","through",
            "throughout","throw","thus","time","today","together","tomorrow","tonight","too","top",
            "total","tough","toward","town","trade","traditional","training","travel","treat","treatment",
            "tree","trial","trip","trouble","true","truth","try","turn","type","under",
            "understand","unit","until","upon","value","various","very","victim","view","violence",
            "visit","voice","vote","wait","walk","wall","want","war","watch","water",
            "weapon","wear","week","weight","west","western","white","whole","whose","wide",
            "wife","will","win","wind","window","wish","within","without","woman","wonder"
        ), 20),
        new Band("10k", "9001-10000", 9001, 10000, List.of(
            "word","worker","world","worry","worth","would","write","writer","wrong","yard",
            "yeah","year","young","account","achieve","acquire","action","active","activity","actual",
            "adapt","additional","adjust","adopt","advance","advantage","advertise","advice","advocate","affair",
            "affect","afford","agency","agenda","agent","agreement","agriculture","aid","aim","aircraft",
            "album","alcohol","alternative","analysis","analyze","ancient","anger","angle","announce","annual",
            "anxiety","apartment","apparent","appeal","appearance","apple","application","apply","appoint","appreciate",
            "approach","appropriate","approve","argue","argument","arise","arrange","arrest","article","artist",
            "aside","aspect","assess","asset","assign","assist","associate","assume","assure","atmosphere",
            "attach","attempt","attend","attention","attitude","attorney","attract","audience","author","authority",
            "available","average","avoid","award","aware","background","balance","band","baseball","basic"
        ), 20),
        new Band("awl", "学术词汇(AWL)", 10001, 10570, List.of(
            "abandon","abstract","academy","access","accommodate","accompany","accumulate","accurate","achieve","acknowledge",
            "acquire","adapt","adequate","adjacent","adjust","administer","adult","advocate","affect","aggregate",
            "aid","allocate","alter","alternative","ambiguous","amend","analogy","analyse","annual","anticipate",
            "apparent","append","appreciate","approach","appropriate","approximate","arbitrary","area","aspect","assemble",
            "assess","assign","assist","assume","assure","attach","attain","attitude","attribute","author",
            "authority","automate","available","aware","behalf","benefit","bias","bond","brief","bulk",
            "capable","capacity","category","cease","challenge","channel","chapter","chart","chemical","circumstance",
            "cite","civil","clarify","classic","clause","code","coherent","coincide","collapse","colleague",
            "commence","comment","commission","commit","commodity","communicate","community","compatible","compensate","compile",
            "complement","complex","component","compound","comprehensive","comprise","compute","conceive","concentrate","concept"
        ), 20)
    );

    public static String getBandForWord(String word) {
        String w = word.toLowerCase();
        for (Band band : BANDS) {
            if (band.words().contains(w)) return band.name();
        }
        return null;
    }
}
