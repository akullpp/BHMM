package hd.nlp.bayes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of BHMM1 of Goldwater & Griffiths 2007.
 * <p/>
 * Bigram Bayesian Hidden Markov Model with Simulated Annealing and Gibbs sampling for disambiguation with a full
 * lexicon containing every possible tags for each word.
 *
 * @author akullpp@gmail.com
 * @version 1.0
 * @since 19.04.13
 */
public class BHMM {
    private Logger l;
    private Properties p;
    private double alpha;
    private double beta;
    private int iter;
    private String corpus;
    private String lexicon;
    private String gold;
    private String out;
    private ArrayList<Integer> words;
    private ArrayList<Integer> tags;
    private ArrayList<Integer> gtags;
    private HashMap<Integer, ArrayList<Integer>> pWords;
    private HashMap<Integer, ArrayList<Integer>> pTags;
    private int transitions[][];
    private int emissions[][];
    private int nTags;

    /**
     * Constructor.
     *
     * @param l Log.
     * @param p Config.
     */
    public BHMM(Logger l, Properties p) {
        this.l = l;
        this.p = p;

        l.log(Level.FINE, "Initializing Parameters");

        alpha = Double.parseDouble(p.getProperty("alpha"));
        beta = Double.parseDouble(p.getProperty("beta"));
        iter = Integer.parseInt(p.getProperty("iterations"));

        l.log(Level.FINER, String.format("Alpha: %f\tBeta: %f\tIter: %d", alpha, beta, iter));

        corpus = p.getProperty("corpus");
        lexicon = p.getProperty("lexicon");
        gold = p.getProperty("gold");
        out = p.getProperty("out");

        l.log(Level.FINER, String.format("Corpus: %s\tLexicon: %s\tGold: %s", corpus, lexicon, gold));
    }

    /**
     * Computes likelihood of tag sequence.
     *
     * @return Logarithmic likelihood.
     */
    private double computeLikelihood() {
        double p = 0.0;

        for (int i = 1; i < tags.size(); i++) {
            int cTag = tags.get(i);
            int pTag = tags.get(i - 1);
            int w = (cTag != 0) ? pWords.get(cTag).size() : 1;
            double q = 1.0;

            q *= (emissions[cTag][words.get(i)] + beta) /
                    (computeSum(emissions, cTag) + beta * w);

            q *= (transitions[pTag][cTag] + alpha) /
                    (computeSum(transitions, pTag) + alpha * nTags);

            p += Math.log(q);
        }
        return p;
    }

    /**
     * Computes accuracy of tag-ambiguous words.
     *
     * @return Accuracy.
     */
    private double computeAccuracy() {
        int correct = 0;
        int total = 0;

        for (int i = 0; i < tags.size(); i++) {
            if (tags.get(i) != 0) {
                if (pTags.get(words.get(i)).size() > 1) {
                    if (tags.get(i).equals(gtags.get(i))) {
                        correct++;
                    }
                    total++;
                }
            }
        }
        return (total != 0) ? (100 * correct / total) : Double.NaN;
    }

    /**
     * Computes the marginal counts of a cross tab.
     *
     * @param cross Cross tab.
     * @return Marginal counts of column/row.
     */
    private ArrayList<int[]> computeMarginal(int cross[][]) {
        ArrayList<int[]> m = new ArrayList<int[]>();
        int X[] = new int[nTags];
        int Y[] = new int[nTags];

        for (int i = 1; i < nTags; i++) {
            for (int j = 1; j < cross[i].length; j++) {
                X[j] += cross[i][j];
                Y[i] += cross[i][j];
            }
        }
        m.add(X);
        m.add(Y);

        return m;
    }

    /**
     * Computes Entropy between clusters.
     *
     * @param mCounts Sequences of marginal counts
     * @param nTokens Number of tokens.
     * @return Entropy metric.
     */
    private double computeEntropy(ArrayList<int[]> mCounts, double nTokens) {
        double h = 0.0;

        for (int[] marginal : mCounts) {
            double hh = 0.0;

            for (int count : marginal) {
                if (count != 0) {
                    double rCount = count / nTokens;

                    hh -= rCount * Math.log(rCount) / Math.log(2);
                }
            }
            h += hh;
        }
        return h;
    }

    /**
     * Computes Mutual Information between two clusters.
     *
     * @param cross   Cross tab.
     * @param m       Marginal counts.
     * @param nTokens Number of tokens.
     * @return Mutual Information metric.
     */
    private double computeMI(int cross[][], ArrayList<int[]> m, double nTokens) {
        double mi = 0.0;

        for (int i = 0; i < nTags; i++) {
            for (int j = 0; j < nTags; j++) {
                double n = cross[i][j] / nTokens;
                double xRel = m.get(0)[j] / nTokens;
                double yRel = m.get(1)[i] / nTokens;

                if (n != 0 && xRel != 0 && yRel != 0) {
                    mi += n * Math.log(n / (xRel * yRel)) / Math.log(2);
                }
            }
        }
        return mi;
    }

    /**
     * Computes Variation of Information based on Meila 2003/07.
     *
     * @return VI metric.
     */
    private double computeVI() {
        ArrayList<int[]> m;
        double h;
        double mi;
        int cross[][] = new int[nTags][nTags];
        double nTokens = 0.0;

        for (int i = 0; i < tags.size(); i++) {
            cross[gtags.get(i)][tags.get(i)] += 1;

            if (tags.get(i) != 0) {
                nTokens++;
            }
        }
        m = computeMarginal(cross);
        h = computeEntropy(m, nTokens);
        mi = computeMI(cross, m, nTokens);

        return h - 2 * mi;
    }

    /**
     * Changes the count of a sample.
     * <p/>
     * It's either 1 for add or -1 for remove.
     *
     * @param i Position in sequence.
     * @param n Change in count.
     */
    private void changeCount(int i, int n) {
        transitions[tags.get(i - 1)][tags.get(i)] += n;
        transitions[tags.get(i)][tags.get(i + 1)] += n;
        emissions[tags.get(i)][words.get(i)] += n;
    }

    /**
     * Computes the conditional distribution of a tag.
     *
     * @param i    Position in sequence.
     * @param cTag Current tag.
     * @param temp Temperature.
     * @return Probability of <code>cTag</code> in position <code>i</code>.
     */
    private double computeProbability(int i, int cTag, double temp) {
        int pTag = tags.get(i - 1);
        int fTag = tags.get(i + 1);
        int I1 = (cTag == fTag) ? 1 : 0;
        int I2 = (pTag == cTag) ? 1 : 0;

        double p = 1.0;

        p *= (emissions[cTag][words.get(i)] + beta) /
                (computeSum(emissions, cTag) + beta * pWords.get(cTag).size());

        p *= (transitions[pTag][cTag] + alpha) /
                (computeSum(transitions, pTag) + alpha * nTags);

        p *= (transitions[cTag][fTag] + I1 + alpha) /
                (computeSum(transitions, cTag) + I2 + alpha * nTags);

        return Math.pow(p, 1 / temp);
    }

    /**
     * Computes the sum of a tag in a matrix.
     * <p/>
     * Either it computes the sum each time the tag was predecessor in a transition or it computes how many words a tag
     * has emitted.
     *
     * @param tagID  ID of the tag.
     * @param matrix Either emission or transition matrix.
     * @return Sum, see description.
     */
    private int computeSum(int matrix[][], int tagID) {
        int sum = 0;

        for (int i = 0; i < matrix[tagID].length; i++) {
            sum += matrix[tagID][i];
        }
        return sum;
    }

    /**
     * Accepts or rejects a sample tag.
     *
     * @param probs Probabilities of possible tags.
     * @return Index of sampled tag.
     */
    private int sampleTag(double[] probs) {
        int tag = -1;

        for (int i = 1; i < probs.length; i++) {
            probs[i] += probs[i - 1];
        }
        double weight = Math.random() * probs[probs.length - 1];

        for (int i = 0; i < probs.length; i++) {
            if (weight < probs[i]) {
                tag = i;
                break;
            }
        }
        return tag;
    }

    /**
     * Gibbs sampling.
     */
    private void sample() {
        int dec = Integer.parseInt(p.getProperty("decrease"));
        double rate = Double.parseDouble(p.getProperty("rate"));
        double temp = Double.parseDouble(p.getProperty("max"));
        double min = Double.parseDouble(p.getProperty("min"));
        int dbg;

        l.log(Level.FINER, String.format("Decrease: %d\tRate: %f\tTemperature: %f\tMinimum: %f", dec, rate, temp, min));

        if ((dbg = Integer.parseInt(p.getProperty("dbg"))) != 0) {
            l.log(Level.FINEST, String.format("Format:\nIteration\tAccuracy\tLikelihood\tVI\tTemperature"));
        }
        for (int itr = 0; itr < iter; itr++) {
            for (int i = 0; i < tags.size(); i++) {
                int wordID = words.get(i);

                if (wordID != 0) {
                    ArrayList<Integer> possibilities = pTags.get(wordID);

                    if (possibilities.size() > 1) {
                        changeCount(i, -1);

                        double probs[] = new double[possibilities.size()];

                        for (int j = 0; j < possibilities.size(); j++) {
                            probs[j] = computeProbability(i, possibilities.get(j), temp);
                        }
                        int tag = possibilities.get(sampleTag(probs));
                        tags.set(i, tag);
                        changeCount(i, 1);
                    }
                }
            }
            double newTemp = temp * rate;

            if (itr % dec == 0 && newTemp >= min) {
                temp = newTemp;
            }
            if (dbg != 0 && itr % dbg == 0 || itr == iter - 1) {
                l.log(Level.FINEST, String.format("\n#%d\t%f\t%f\t%f\t%f",
                        itr + 1, computeAccuracy(), computeLikelihood(), computeVI(), temp));
            }
        }
    }

    /**
     * Coordinates method calls.
     * <p/>
     * Also spams logging messages.
     */
    public void run() {
        l.log(Level.FINE, "Initializing structures");

        IO io = new IO(corpus, lexicon, gold, out);
        HMM hmm = new HMM();
        tags = new ArrayList<Integer>();
        ID wid = new ID();
        ID tid = new ID();
        pWords = new HashMap<Integer, ArrayList<Integer>>();
        pTags = new HashMap<Integer, ArrayList<Integer>>();

        l.log(Level.FINE, String.format("Reading lexicon from %s", lexicon));
        words = io.readCorpus(wid);
        l.log(Level.FINER, String.format("Word IDs: %s", wid.getHashDI()));
        l.log(Level.FINER, String.format("Words: %s", words));

        l.log(Level.FINE, String.format("Reading lexicon from %s", lexicon));
        io.readLexicon(wid, tid, pTags, pWords);
        l.log(Level.FINER, String.format("Tag IDs: %s", tid.getHashDI()));
        l.log(Level.FINER, String.format("Possible words for tags: %s", pWords));
        l.log(Level.FINER, String.format("Possible tags for word: %s", pTags));

        nTags = pWords.size() + 1;
        int nWords = pTags.size() + 1;
        l.log(Level.FINER, String.format("N(words): %d", nWords - 1));
        l.log(Level.FINER, String.format("N(tags): %d", nTags - 1));

        l.log(Level.FINE, String.format("Reading gold standard from %s", gold));
        gtags = io.readGold(tid);
        l.log(Level.FINER, String.format("Gold tags: %s", gtags));

        l.log(Level.FINE, "Initializing tag sequence");
        tags = hmm.initializeTags(words, pTags);
        l.log(Level.FINER, String.format("Tags: %s", tags));

        l.log(Level.FINE, "Initializing transition matrix");
        transitions = hmm.initializeTransitions(nTags, tags);
        l.log(Level.FINER, String.format("Transitions: %s", Arrays.deepToString(transitions)));

        l.log(Level.FINE, "Initializing emission matrix");
        emissions = hmm.initializeEmissions(nTags, nWords, tags, words);
        l.log(Level.FINER, String.format("Emissions: %s", Arrays.deepToString(emissions)));

        l.log(Level.FINE, "Starting Gibbs sampling with annealing");
        sample();
        io.writeSample(words, tags, wid, tid);
    }
}
