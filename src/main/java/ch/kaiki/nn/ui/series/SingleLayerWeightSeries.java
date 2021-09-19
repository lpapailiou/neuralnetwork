package ch.kaiki.nn.ui.series;

import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.BaseChart;
import ch.kaiki.nn.ui.NN3DChart;
import ch.kaiki.nn.ui.color.NNColor;
import ch.kaiki.nn.ui.color.NNHeatMap;
import ch.kaiki.nn.ui.util.ChartMode;
import ch.kaiki.nn.ui.seriesobject.Polygon;
import ch.kaiki.nn.ui.seriesobject.SortableSeriesData;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static ch.kaiki.nn.ui.color.NNColor.blend;
import static javafx.scene.paint.Color.TRANSPARENT;

public class SingleLayerWeightSeries extends Series {

    private double[][] seriesData;
    private final NNHeatMap colorMap;
    private final BaseChart chart;
    private final NeuralNetwork neuralNetwork;

    public SingleLayerWeightSeries(BaseChart chart, NeuralNetwork neuralNetwork, NNHeatMap colorMap, int layerIndex) {
        super(null, colorMap.getColors(), ChartMode.MESH_GRID);
        this.chart = chart;
        this.neuralNetwork = neuralNetwork;
        this.colorMap = colorMap;
        seriesData = transpose(neuralNetwork.getWeights().get(layerIndex));
        super.addName("high");
        super.addName("low");
    }

    public SingleLayerWeightSeries(BaseChart chart, NeuralNetwork neuralNetwork, NNHeatMap colorMap, int layerIndex, int nodeIndex, int width) {
        super(null, colorMap.getColors(), ChartMode.MESH_GRID);
        this.chart = chart;
        this.neuralNetwork = neuralNetwork;
        this.colorMap = colorMap;
        double[][] weights = neuralNetwork.getWeights().get(layerIndex);
        int columnLength = weights[nodeIndex].length;
        seriesData = new double[columnLength/width][columnLength/(columnLength/width)];
        double[] testcol = new double[]{18.91465270623279, -9.68923646644946, 4.594459811215185, -15.160095187015873, -10.032024873602287, 7.64775672032274, -10.429829842109106, 16.828984584570136, 9.37884189095709, 7.121982601192616, -11.08920907626421, -9.397069019302048, 16.580662330570778, 9.34000903530542, 12.759139499647022, 19.66665048257656, 1.8027540117988041, -7.105279367882756, 0.24980741259770425, 7.352314565916627, -6.764330919304158, -10.749079008224953, 2.5584046322588434, -10.231375284689664, 3.886522812978746, -17.221831909583496, 14.856239771357256, -2.8104001402813403, -9.728136027054742, -3.7204507193995835, 11.736686283776097, -8.02222059228118, 4.137158152329278, 16.64702840289508, -5.7655936295989, 13.02226786252459, 17.11320823733686, 7.426228067976808, 2.3054409292623816, -17.692391471163713, 19.685730875407074, -12.792906281345847, 15.429567968611089, 5.2979477957439105, -19.733371104681716, 14.821335160990444, 10.207971566773303, -1.6009127433991432, 17.714395387861046, -6.4811521908857666, 19.117723672813966, -16.15324145190175, 8.744393725581741, 1.3871570275474692, -12.708716114931805, -19.3562870383838, 7.632789980170239, 8.44208523556582, -10.108020155333445, -0.8237129134425792, -16.125007142285043, 4.514869549418543, -18.493342385095662, 3.219803516724729E-4, 17.073124933554038, 14.684823644641867, 7.103845114796035, 11.432928105109102, 11.018582611266893, 1.8623865636362964, 19.451068077315963, 15.317623851807934, -8.54336782553822, -7.869814534538611, 10.497073610371828, 2.32862827059431, 16.78434503983304, -3.1540030854683043, 15.612586599672914, -4.412976996601686, -16.432640891543315, -19.42799079695987, 5.926749840305524, -17.532038324838425, -8.523087214366143, 18.132558394010033, 2.984423869892931, -14.685575451935813, 19.519303668497468, 16.31165423443555, -18.769465143857985, -8.012265190124166, -9.078251158166717, -5.073618930421223, -0.33773774970370357, 17.883078102632354, -12.246102798477583, -11.444152212039354, 0.1578978958511112, 4.3309009745134714, 11.668345993851041, -5.481826861590046, -15.719550904597066, -10.331116248453501, -13.19971953724909, -13.77135663732843, 1.8111264959611022, 9.257882151223317, -17.736443754549946, 11.729575866155647, -12.133659407218213, 13.548891580093695, -6.488852004276615, 12.996941173706281, -8.707190112004175, 6.726194475449808, 4.392907529327509, 14.509546699004364, 2.807771671352864, -1.5658673047268845, -19.528744434325326, 17.303443636559265, 10.420679909021286, 9.550547295972247, -2.3622264944321496, 9.486831986983756, 13.497774548176839, -16.784023664110002, -2.3419833057933763, 6.16103752561194, 0.03379467347942278, 4.263204988332026, 10.188679766723576, -19.621787647695378, -12.05553674310276, -8.443839369882738, -19.079000882906488, -18.235900841088355, -7.902601939417478, -12.11422772819117, 12.679778212498702, -2.3705009562406403, -3.6827244488886253, 5.3571584147451965, 10.098118563775657, 4.243441706632507, 10.567831771775264, 10.233204976245922, -17.89198856580501, 15.606132040754238, -1.2673641780459568, -20.121634081329447, -5.9572982865857185, -51.039129404848666, -97.24412191327761, -74.38684917480614, -93.30868371760162, -84.9720468960281, -74.44226358638697, -71.86575618670065, -40.056681807929266, -20.72138974528428, -4.567469763053561, 17.362641334407325, 10.139500387831358, -13.58546021184727, -5.200843906592718, 9.310524815232453, 13.743385669918712, -18.27035846044332, -7.441121229686016, 0.41407848968400723, 13.450265305657123, 15.574544583147535, 7.967643966340554, 2.6423441618594876, -1.4159565632229276, -28.79774089890633, -41.27890294066135, -82.26303562985893, -73.89427378937911, -93.26135834369316, -87.46620842561325, -73.50882919029507, -82.68038534165103, -95.42579513489986, -84.7203984460115, -65.77359609850924, -64.92506218397942, -52.30472289413162, 5.424707817936651, -16.375427802316946, -15.206086237011274, 18.937581537482924, -2.2080526131538414, 18.947084074491848, 19.509788162225362, 3.2568847002695986, 13.210701478529526, -10.567969816518865, -4.945324781152003, 11.75248967370955, -12.971122404269773, -10.469434859493415, -9.512963042305339, -68.7404197955644, -75.60305447912023, -61.25115750716344, -85.02761251958876, -61.510960447121576, -90.25891073606124, -89.64746829682123, -67.76756928476028, -84.50106010103775, -57.88344700072664, -73.133561668231, -58.4993032950364, -80.38150011661178, -22.875125771448108, -13.477500480442224, 7.88346381984141, 1.859774950679852, 19.6894475285611, -12.885986029797927, -2.8192881288586347, -18.75446483078674, -6.934634069087043, 18.21422433487273, 13.014430760475316, 5.9584066500737665, -0.9260118205722826, -18.282141509030758, -5.277643532181063, -29.783947461395766, -91.74035230590385, -72.98931518017876, -75.07150876726776, -53.037689287769865, -29.47022683943278, -57.16511647937237, -32.987140709299595, -59.107560285293, -81.36842449124975, -79.0313715708062, -66.02005894260577, -61.87691472734479, -15.082328746837762, 3.0791908673182724, 0.0182016422083162, -6.0297702917382, 5.358073865504457, -15.317793567171696, -11.523715727856647, -11.967430241263447, 11.383874437734224, 11.695198933802333, -8.27775842869415, 10.114925818425318, -17.40433197297998, 14.041876832396841, -16.513473965449677, -16.560656583672227, -2.0378995413998178, 8.247601578497079, -18.889951506103113, 4.794409388510674, 6.673528703968549, -1.599989633969129, 0.021267486386627975, 11.178320054416911, -84.39754735761488, -68.42563722853971, -64.78612688459316, -47.470261942220176, -17.04876582743004, 13.079811360756025, -14.972299664536727, -0.49661756940390983, 5.800790642177844, 14.647791100361196, 7.944680829530197, -11.774214366362045, 14.719255766626377, 9.63804109072616, -18.02649023718991, 6.752533559217737, 15.846031900405581, -16.139454476145257, 15.13476557420495, -6.742219273597351, -5.709104228027224, 0.2857457480106897, 3.169167047121824, -14.840335157451754, -6.067569784716287, -4.59298808432442, 9.34976394050026, -22.374867420503698, -78.7910871952026, -77.37937568406038, -94.68512516074922, -60.0544482410335, 4.459373765488939, 13.05961019255243, 18.819692635619983, -16.07881591123857, 18.823425673990492, 9.392339731603636, -19.613284722604313, -6.900271453650695, 10.020338445644747, -12.772826586676228, -19.079209609027675, -11.67585968143546, -10.316053166130844, -13.335242951355367, -2.5564841508438696, -5.009965117971263, -12.047186378350984, 12.315203210097746, 2.2397103865911467, -10.574308838185543, -15.094561472155187, -19.023269801811345, -9.15395336764917, -56.744341132907394, -85.19313947430697, -58.418234861869216, -84.17272807322973, -4.79049525353, -17.882243634084922, 5.246836785202351, 17.33719599068626, 15.821443648120903, 6.638493898560642, -7.9756779455049, -8.851925442970046, -5.392344280398491, 14.119709044672371, 4.777081241322199, -11.630646188199044, 0.12262426968175609, 12.430053613009747, -15.454120691985342, 12.583848564114875, -5.038019116861042, -1.1742117665167242, -17.049035498453694, 4.483735157863685, -9.319707005961298, -23.36695007015948, -48.887394229397245, -71.1234057130047, -86.40863696399114, -87.1513325420598, -81.54220805330708, -55.79837081626566, -8.627703871489583, 9.0544702633335, -18.721989648211913, -12.701948387662824, 5.966854721064535, 18.508292932881396, 11.679315359835543, -13.824562206657317, -8.268174137542937, -16.583627199186797, 4.051351334225212, -1.3134970569474946, 13.481657012585245, 15.815845926598888, 9.280730247907723, -10.807417717434301, -2.363322501763574, -26.159261372577504, -40.281510964482386, -45.81410516435316, -51.23399183743527, -83.41797264030986, -74.42718809866129, -75.02275906776515, -74.43802305794316, -76.10926545098575, -36.429336875296535, -16.816344795282415, 10.572595759338578, 5.4663397291688565, -5.212465027634028, -5.62900240652783, 4.9606174083293695, -15.073209219194386, 3.455036885186037, 19.18075408693674, -16.73829592782782, 2.9776107591731567, 11.576415470189094, 5.3635327150564, 18.3938193270592, -1.7839018132066287, 11.804508911403836, -29.11824017502905, -79.75490145660054, -72.461104498659, -91.12414148260794, -68.67999175050323, -96.55902033658363, -69.79020828286245, -65.01944514151751, -62.59200571093336, -74.5041543232572, -57.58383803568412, -10.883960485857704, 17.311859808838005, 8.468254903285034, 7.741550869479635, -4.95363526753777, -16.624336401255444, -13.831709361911933, 0.6178182652389296, 5.831521343694163, -5.8218652424057735, -5.212784721593238, -10.583912211746183, 0.9375622927972797, 6.521820358138699, 18.760958330550892, -8.809373201520605, 1.8276616610424319, -17.867916355087882, -71.53334215955655, -64.84090392886831, -67.47281374951207, -58.35683002754176, -57.468109702576655, -86.05057948016392, -58.36572179087969, -92.75254079359182, -89.41328731304054, -72.57976260143246, -16.051249357272084, 9.535445105738056, -2.0204786041993676, 18.046564941692626, -2.8080089036803737, -2.692642476353086, 3.717805601303011, -14.570266089683887, 4.037380711089155, -12.178105139724767, 4.174477057761437, 16.545484943296383, -12.334020467983143, -1.2348890650930007, 15.078685373843259, -1.7186547917173554, 18.835100392113095, -0.0835263383611244, -6.931585588846634, -35.71128841745878, -14.694880238708539, 0.6979267121860833, -22.669053196186574, -14.163278383113326, -9.757315009721575, -36.80888248903192, -74.30743919854127, -70.00966529163472, -4.781239075393188, 4.7637613821011, 19.657819330343564, -5.535440784176795, -12.171762894794796, 0.9539815448675542, 13.58617222661924, -10.947717914927807, 16.667910324046165, -14.575380742419451, 19.004012479280252, 18.94226373132369, 13.423574686698737, -5.849296257164947, -15.469377509895665, 0.06026046600205035, 15.267310405322162, -17.142316807193726, -9.32347950335534, -5.48340793579618, -19.349861091501253, -2.48958524366641, -14.077254092472943, -9.239160184647252, -1.3579079676246433, -5.57509168804377, -62.06470098348996, -88.52833397509319, -23.09914572385857, 1.6827521346017609, -16.018556646415426, 13.654549382647756, -15.539982572518014, -6.042495762446425, 12.569138467381435, 19.43237228765367, -5.209541425131806, 18.0223437345457, -2.9249351870657607, 15.286242784337315, 5.389183263623805, -7.3069911002506895, -9.605757491478348, -7.06518362363903, 13.619765204883631, 12.182277597821253, 6.69210652024089, 2.7674393110886055, -2.3156365493096014, -5.943576059229108, -11.961179889656893, 0.5629457198621248, 1.6976956676325257, -18.735033720433382, -81.32141690785055, -83.80962886140979, -31.701068469518724, 3.8406507462441635, -13.80125514368643, 1.6870679546579144, 2.7632393336924412, 12.050880163291575, -18.790411652455983, -4.2304833299551925, 13.564687816198818, 9.000094447515863, 4.601140611022332, 13.182972373755417, 2.9001285550564515, -13.296354661829774, 0.1832278442063144, 8.857793660486386, -12.148713369454208, -5.658418622561065, -4.630572293535155, 3.233523347132075, -2.2134928640968736, 3.840672513255918, 12.374688540318152, -5.747603634492567, -29.80880699065581, -57.76730972598957, -58.742733377461704, -75.33364644015931, -22.938556628405735, 1.9122306128558941, 16.18371631621348, -1.2509901420396616, -9.861757787824189, 2.867075277116781, 5.897524198101915, -1.4521834864384962, -12.129788046701977, 17.643780567964995, 6.292899841439453, 12.898612404302227, -13.794102586697099, -11.762851222020872, -36.070993279772345, -50.14990200930794, -62.20043605695432, -3.962918475873317, 7.548312291681954, 11.407828051513114, -15.54412841606082, -7.540656892760777, -21.467556661143092, -37.50413940621475, -57.60181373373516, -94.15370714392594, -83.5855983461995, -72.261629017143, -37.90812437939884, 0.6513248544582854, 8.124547498133618, -2.3122659335605857, -11.78920269460196, -1.357404700476724, -17.850918866569533, -4.8711084356908, 2.1029468213804927, -15.349330863865253, 16.138827600150236, -9.880240926527232, -3.782291229446292, -5.461707674272832, -82.22055352350858, -88.10486807339046, -91.04112102825465, -58.90655588332912, -24.2794379636481, -58.40754924878344, -38.627413760015635, -46.80249123075649, -48.83642255960285, -87.71545256781532, -73.88441820949663, -69.2312775445196, -62.03993711965369, -7.430466008664382, -1.9391414004236411, 5.27292935929095, -8.704034486500897, -16.274574553657615, 19.772668873212453, -12.399279007795405, 19.291723938111943, -13.780708273626692, -15.451916792565784, -4.418758856273944, 18.006366510720877, -18.644997827572194, 14.063638779910987, 15.861875580493216, -81.89073900345448, -72.46136699795085, -70.3445450725181, -86.84473337446764, -96.27160654748984, -91.78632225735775, -64.99769251233005, -95.56604393686403, -67.97019330624099, -86.47286154858014, -52.999597027643546, -52.01231658244367, -14.53227115376166, -12.46644209525951, -16.16214278027014, 17.23481498443286, -12.495543120071718, 3.019742939364637, -8.635796909132095, 12.214972042729856, -11.610579904001652, -12.454132462879864, -13.9028014905343, -1.548113295937324, 1.3458702626306094, -5.60520258316901, 6.037381987244226, -6.60096140622075, -0.3296084657327274, -56.41113299669145, -84.26461888888686, -78.80147071312875, -74.97712115814983, -94.09682896267357, -63.36786470575004, -56.39906916151834, -54.94121074321002, -35.24092529578793, 5.522277459067677, 13.96861215196266, 6.457708966447672, 18.46978220529622, -5.269779873427062, -4.308500224398223, 15.453997924980705, -10.281451886321701, 4.209866324775126, 19.3402419337799, -5.496426875481733, 15.638335661940893, 0.4467129419157337, -1.665527389826281, 6.2726422868399965, 4.260555656290735, -9.854608763343382, 18.224935674437802, -9.032413987584496, 0.2702668940509487, -33.908593476762306, -82.95870843059143, -69.2538689650748, -43.04828504922335, -20.813074532571687, -17.99097107064847, -1.645871698047961, 2.253242638630341, -3.1916708160249234, -6.667947752406247, -17.859606518384755, -8.647010255117104, 7.869985555346826, 12.240067160444454, 8.565561064644022, 1.5389413881500829, 2.3686471050101754, 12.144744132940357, 2.361683439990501, 0.09768454374324759, -2.1599632726533247, 6.15343542735055, 16.037645297004623, -9.606291980657915, 4.0071402636818005, -7.0795915010132, 16.566064804040384, -11.322324529559344, -2.893977713976419, -12.509708777144011, 3.9631214488660156, -4.516328213246136, 6.2764619596964994, 17.298453199109893, 11.596532863278636, 12.042180433973646, -12.992262268027067, 19.620881546752642, 3.9905262032861426, -6.52705674960492, 19.16208875129833, -2.157341477518544, 6.575343935782616, 7.669410321740519, 3.4816114566010605, -11.309231903549327, -16.697020119309347, -0.1648767995461231, -15.564176014467414, 14.118318253547256, -7.830961403095148, 11.400242885725303, 14.768691017220883, -17.251558757443405, -3.9531601753189753, 4.012375652258497, 17.751483107285615, 11.00842447623775, -19.78020619642738, 9.332540269532787, 11.616152409019303, 2.253405350779169, 16.432142113955173, -13.810258634516378, 10.098719916055275, 7.765015772053492, -18.005460302838305, -17.222638772125563, -7.89425344395017, -2.9813343411221287, -10.53033754242498, -16.333976507667728, 18.009261723069848, -3.707738062180148, -19.209298234402354, 0.41421355148968836, 8.317832763324766, 7.003644489244993, 11.641268693918, -13.287849110807125, -8.843207405330896, -15.807248066261133, 14.134853721160605, -15.905925407308269, 16.3613832668583, -3.716813076986485, 9.328004146404146, 8.0791579927177, 9.35787513679599, 14.440239640383481, 15.534335850196571, 12.175814544442959, -6.036549473501929, 8.965245019577658, -4.567043580415526, 7.051744214348228, -6.606075341998844, 3.441610658690428, -13.605081287053748, 14.684139706908477, 7.751512663810386, 18.59400413620716, 14.636500533483304, -7.589453165414643, 2.2646727726754943};

        int index = 0;
        for (int j = weights[nodeIndex].length-1; j > 0; j--) {
            if (j%width == 0) {
                index++;
            }
            seriesData[index][j%width] = weights[nodeIndex][j];
            //seriesData[index][j%width] = testcol[j];

        }
        seriesData = transpose(seriesData);
        super.addName("high");
        super.addName("low");
    }


    @Override
    public List<Color> getColor() {
        List<Color> colors = colorMap.getColors();
        List<Color> featureLabelColors = new ArrayList<>();
        featureLabelColors.add(colors.get(colors.size()-1));
        featureLabelColors.add(colors.get(0));
        return featureLabelColors;
    }

    @Override
    public void compute() {
        xMin = -0.5;
        xMax = seriesData.length-0.5;
        yMin = -0.5;
        yMax = seriesData[0].length-0.5;

        zMin = Double.MAX_VALUE;
        zMax = Double.MIN_VALUE;

        for (double[] i : seriesData) {
            for (double val : i) {
                if (val < zMin) {
                    zMin = val;
                }
                if (val > zMax) {
                    zMax = val;
                }
            }
        }

        if (colorMap.isScaled()) {
            zMin = colorMap.getMin();
            zMax = colorMap.getMax();
        }
    }



    @Override
    public void render() {
        double[][][][] transformedDataGrid = new double[seriesData.length][seriesData[0].length][4][];
        GraphicsContext context = chart.getContext();
        double zMin = chart.getGlobalMinZ();
        double zMax = chart.getGlobalMaxZ();
        for (int i = 0; i < seriesData.length; i++) {
            for (int j = 0; j < seriesData[0].length; j++) {
                double value = 0;
                double actualValue = 0;
                if (seriesData != null) {
                    value = seriesData[i][j];
                }
                double[] t0 = chart.transform(new double[] {i-0.5, j-0.5, value});
                double[] t1 = chart.transform(new double[] {i-0.5, j+0.5, value});
                double[] t2 = chart.transform(new double[] {i+0.5, j-0.5, value});
                double[] t3 = chart.transform(new double[] {i+0.5, j+0.5, value});
                transformedDataGrid[i][j][0] = new double[]{t0[0], t0[1], t0[3], value};
                transformedDataGrid[i][j][1] = new double[]{t1[0], t1[1], t0[3], value};
                transformedDataGrid[i][j][2] = new double[]{t2[0], t2[1], t2[3], value};
                transformedDataGrid[i][j][3] = new double[]{t3[0], t3[1], t0[3], value};
            }
        }
        List<SortableSeriesData> polygons = new ArrayList<>();

        List<Color> colors = colorMap.getColors();
        double step = Math.abs(zMax -zMin)/(colors.size()-1);
        List<Color> colorList = new ArrayList<>();
        double minOpacity = colorMap.getMinOpacity();
        double maxOpacity = colorMap.getMaxOpacity();
        if (minOpacity == 1 && maxOpacity == 1) {
            colorList.addAll(colors);
        } else {
            if (colors.size() == 1) {
                colorList.add(NNColor.blend(colors.get(0), TRANSPARENT, minOpacity));
                colorList.add(NNColor.blend(colors.get(0), TRANSPARENT, maxOpacity));
            } else {
                double opacityStep = Math.abs(maxOpacity - minOpacity) / (colors.size() - 1);
                double opacity = minOpacity;
                for (Color color : colors) {
                    colorList.add(NNColor.blend(color, TRANSPARENT, opacity));
                    opacity += opacityStep;
                }
            }
        }
        polygons.addAll(getPolygons(context, zMin, zMax, transformedDataGrid, step, colorList));

        Comparator<SortableSeriesData> comparator = (SortableSeriesData::compareTo);
        polygons.sort(chart instanceof NN3DChart ? comparator.reversed() : comparator);
        for (SortableSeriesData p : polygons) {
            p.render();
        }
    }

    private List<Polygon> getPolygons(GraphicsContext context, double zMin, double zMax, double[][][][] grid, double step, List<Color> colors) {
        //System.out.println(zMin + " " + zMax);
        List<Polygon> polygons = new ArrayList<>();
        double range = Math.abs(zMax-zMin);
        double pos = 0.3;
        double neg = -pos;
        for (int i = 0; i < seriesData.length; i++) {
            for (int j = 0; j < seriesData[0].length; j++) {
                double[] a = grid[i][j][0];
                double[] b = grid[i][j][1];
                double[] c = grid[i][j][3];
                double[] d = grid[i][j][2];

                /*
                    d   c
                    a   b
                 */
                double[] xEs = {a[0] <= c[0] ? a[0] + neg : a[0] + pos,
                        b[0] >= d[0] ? b[0] + pos : b[0] + neg,
                        c[0] >= a[0] ? c[0] + pos : c[0] + neg,
                        d[0] <= b[0] ? d[0] + neg : d[0] + pos};
                double[] ys =  {a[1] >= c[1] ? a[1] + pos : a[1] + neg,
                        b[1] >= d[1] ? b[1] + pos : b[1] + neg,
                        c[1] <= a[1] ? c[1] + neg : c[1] + pos,
                        d[1] <= b[1] ? d[1] + neg : d[1] + pos};


                double zSum = (a[3] + b[3] + c[3] + d[3]) / 4;


                if (zSum < zMin || zSum > zMax) {
                     continue;        // TODO: check if really helpful
                }

                double sort = (a[2] + b[2] + c[2] + d[2]) / 4;

                Color color;
                if (colors.size() > 2) {
                    int stepIndex = 0;
                    double value = zMin;
                    for (int k = 0; k < colors.size() - 1; k++) {
                        value += step;
                        if (zSum <= value || k == colors.size() - 2) {
                            stepIndex = k;
                            break;
                        }
                    }

                    double ratio = 1 / step * Math.abs(value - zSum);
                    if (zSum > zMax) {
                        ratio = 0;
                    }
                    color = blend(colors.get(stepIndex), colors.get(stepIndex+1), ratio);
                } else {
                    //System.out.println((zSum-zMin)/range);
                    color = blend(colors.get(1), colors.get(0), (zSum-zMin)/range); // TODO: ratio not working!!!
                }
                double zVal = chart instanceof NN3DChart ? sort : zSum;
                double polygonLabel = seriesData[i][j];
                //polygons.add(new Polygon(context, xEs, ys, zVal, color, polygonLabel, true));
                polygons.add(new Polygon(context, xEs, ys, zVal, color));
            }
        }
        return polygons;
    }
    private double[][] transpose(double[][] m) {
        double[][] tmp = new double[m[0].length][m.length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                tmp[j][i] = m[i][j];
            }
        }
        return tmp;
    }
}
