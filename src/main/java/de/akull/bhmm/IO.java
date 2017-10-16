package de.akull.bhmm;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements various functions to read necessary files.
 *
 * @author akullpp@gmail.com
 * @version 1.0
 * @since 21.04.13
 */
public class IO {
    private final String corpus;
    private final String lexicon;
    private final String gold;
    private final String out;

    /**
     * Constructor.
     *
     * @param corpus  Corpus filename.
     * @param lexicon Lexicon filename.
     * @param gold    Gold standard filename.
     */
    public IO(String corpus, String lexicon, String gold, String out) {
        this.corpus = corpus;
        this.lexicon = lexicon;
        this.gold = gold;
        this.out = out;
    }

    /**
     * Reads the corpus.
     * <p/>
     * Format should be one tokenized sentence per line.
     *
     * @param wid Mapping String (word) -> Integer (id).
     * @return Sequence of word IDs with each sentence seperated by 0-boundaries.
     */
    public ArrayList<Integer> readCorpus(ID wid) {
        ArrayList<Integer> words = null;
        BufferedReader br = null;
        String line;

        try {
            words = new ArrayList<Integer>();
            br = new BufferedReader(new FileReader(corpus));

            while ((line = br.readLine()) != null) {
                words.add(0);

                for (String s : line.split(" ")) {
                    int wordID = wid.set(s);
                    words.add(wordID);
                }
            }
            words.add(0);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return words;
    }

    /**
     * Reads the lexicon.
     * <p/>
     * Format should be the word with its possible tags seperated by a dash.
     * Initializes mappings of possible words for a tag and vice versa.
     *
     * @param wid    Mapping of words to unique IDs.
     * @param tid    Mapping of tags to unique IDs.
     * @param pTags  Possible tags for a word.
     * @param pWords Possible words for a tag.
     */
    public void readLexicon(ID wid,
                            ID tid,
                            HashMap<Integer, ArrayList<Integer>> pTags,
                            HashMap<Integer, ArrayList<Integer>> pWords) {
        String line;
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(lexicon));

            while ((line = br.readLine()) != null) {
                String tmp[] = line.split(" - ");
                int wordID = wid.getID(tmp[0]);
                pTags.put(wordID, new ArrayList<Integer>());

                for (String s : tmp[1].split(" ")) {
                    int tagID = tid.set(s);

                    if (!pWords.containsKey(tagID)) {
                        pWords.put(tagID, new ArrayList<Integer>());
                    }
                    pWords.get(tagID).add(wordID);
                    pTags.get(wordID).add(tagID);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    /**
     * Reads the gold standard.
     * <p/>
     * Format should be word/tag or just the tag in the same position as in the corpus.
     *
     * @param tid Mapping of tags to unique IDs.
     * @return Sequence of correct tags.
     */
    public ArrayList<Integer> readGold(ID tid) {
        ArrayList<Integer> gtags = null;
        BufferedReader br = null;
        String line;

        try {
            gtags = new ArrayList<Integer>();
            br = new BufferedReader(new FileReader(gold));

            while ((line = br.readLine()) != null) {
                gtags.add(0);

                for (String s : line.split(" ")) {
                    Pattern p = Pattern.compile("(.*/)?(.*)");
                    Matcher m = p.matcher(s);

                    if (m.matches()) {
                        gtags.add(tid.getID(m.group(2)));
                    }
                }
            }
            gtags.add(0);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return gtags;
    }

    /**
     * Writes a sample to file.
     * <p/>
     * Format is word/tag with one sentence per line.
     *
     * @param words Sequence of word IDs.
     * @param tags  Sequence of tag IDs.
     * @param wid   Mapping String (word) -> Integer (id).
     * @param tid   Mapping String (tag) -> Integer (id).
     */
    public void writeSample(ArrayList<Integer> words, ArrayList<Integer> tags, ID wid, ID tid) {
        BufferedWriter bw = null;

        try {
            FileWriter fw = new FileWriter(out);
            bw = new BufferedWriter(fw);

            for (int i = 1; i < tags.size(); i++) {
                if (tags.get(i) != 0) {
                    String word = wid.getString(words.get(i));
                    String tag = tid.getString(tags.get(i));
                    bw.write(String.format("%s/%s ", word, tag));
                } else {
                    bw.write("\n");
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
