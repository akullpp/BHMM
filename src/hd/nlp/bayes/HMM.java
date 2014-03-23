package hd.nlp.bayes;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

/**
 * Initializes the HMM datastructures.
 *
 * @author akullpp@gmail.com
 * @version 1.0
 * @since 21.04.13
 */
public class HMM {

    /**
     * Initializes tag sequence.
     * <p/>
     * If a word has more than one possible tag, the tag is randomly choosen out of a uniform distribution.
     *
     * @param words Sequence of word IDs.
     * @param pTags Mapping of words to possible tags.
     * @return Sequence of tag IDs.
     */
    public ArrayList<Integer> initializeTags(ArrayList<Integer> words, HashMap<Integer, ArrayList<Integer>> pTags) {
        ArrayList<Integer> tags = new ArrayList<Integer>();
        Random r = new Random(new Date().getTime());

        for (int wordID : words) {
            if (wordID == 0) {
                tags.add(0);
            } else {
                ArrayList<Integer> pos = pTags.get(wordID);

                if (pos.size() == 1) {
                    tags.add(pos.get(0));
                } else {
                    int i = r.nextInt(pos.size());
                    tags.add(pos.get(i));
                }
            }
        }
        return tags;
    }

    /**
     * Initializes transition matrix.
     * <p/>
     * Format is Count(previous tag | tag).
     *
     * @param nTags Number of tags inclusive boundary.
     * @param tags  Sequence of tag IDs.
     * @return Transition matrix.
     */
    public int[][] initializeTransitions(int nTags, ArrayList<Integer> tags) {
        int transitions[][] = new int[nTags][nTags];

        for (int i = 1; i < tags.size(); i++) {
            int previous = tags.get(i - 1);
            int current = tags.get(i);

            transitions[previous][current] += 1;
        }
        return transitions;
    }

    /**
     * Initializes emission matrix.
     * <p/>
     * Format is Count(tag | word).
     *
     * @param nTags  Number of tags inclusive boundary.
     * @param nWords Number of words inclusive boundary.
     * @param tags   Sequence of tag IDs.
     * @param words  Sequence of word IDs.
     * @return Emission matrix.
     */
    public int[][] initializeEmissions(int nTags, int nWords, ArrayList<Integer> tags, ArrayList<Integer> words) {
        int emissions[][] = new int[nTags][nWords];

        for (int i = 0; i < tags.size(); i++) {
            int currentTag = tags.get(i);
            int currentWord = words.get(i);

            emissions[currentTag][currentWord] += 1;
        }
        return emissions;
    }
}
