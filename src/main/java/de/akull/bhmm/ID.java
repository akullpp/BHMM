package de.akull.bhmm;

import java.util.HashMap;

/**
 * Maps strings to a unique natural number.
 *
 * @author akullpp@gmail.com
 * @version 1.0
 * @since 19.04.13
 */
public class ID {
    /**
     * String -> ID.
     */
    private HashMap<String, Integer> id;
    /**
     * ID -> String, for a better solution check Google's BiMap implementation.
     */
    private HashMap<Integer, String> di;
    private int counter = 1;

    /**
     * Constructor.
     */
    public ID() {
        id = new HashMap<String, Integer>();
        di = new HashMap<Integer, String>();
    }

    /**
     * Assigns a new ID to a string.
     *
     * @param element String.
     * @return Assigned ID.
     */
    public int set(String element) {
        Integer i = id.get(element);

        if (i == null) {
            i = counter;
            id.put(element, i);
            di.put(i, element);
            counter++;
        }
        return i;
    }

    /**
     * Get ID from String.
     *
     * @param element String.
     * @return ID for <code>element</code>.
     */
    public int getID(String element) {
        return id.get(element);
    }

    /**
     * Returns ID -> String mapping.
     *
     * @return Map ID -> String.
     */
    public HashMap<Integer, String> getHashDI() {
        return di;
    }

    /**
     * Get String from ID.
     *
     * @param id ID.
     * @return String for <code>id</code>.
     */
    public String getString(int id) {
        return di.get(id);
    }
}
