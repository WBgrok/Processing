package rita;

import java.io.*;
import java.util.*;

import rita.wordnet.*;
import rita.wordnet.jwnl.*;
import rita.wordnet.jwnl.data.*;
import rita.wordnet.jwnl.data.list.*;
import rita.wordnet.jwnl.data.relationship.*;
import rita.wordnet.jwnl.dictionary.Dictionary;
import rita.wordnet.jwnl.dictionary.FileBackedDictionary;


/**
 * Provides library support for application and applet access to Wordnet.
 * <p>
 * 
 * You can construct this object like so:
 * 
 * <pre>
 * RiWordnet wordnet = new RiWordnet(this);
 * </pre>
 * 
 * If you do not wish to use the embedded data files, but instead prefer a
 * pre-installed <br>
 * (local or remote) Wordnet installation, pass the filepath or URL of the
 * directory in which Wordnet is installed to the constructor as follows:
 * <p>
 * 
 * <pre>
 *   (Note: windows paths require double backslashes as below)
 *   
 *   RiWordnet wordnet = new RiWordnet(this, &quot;c:\\Wordnet3.0\\&quot;);
 * </pre>
 * 
 * Generally three methods are provided for each relation type (e.g.,
 * getHyponyms(String, String), getHyponyms(String, String) and
 * getAllHyponyms(String,String) where the 1st returns hyponyms for a specific
 * sense (as specified by its unique id), the 2nd returns the most common sense,
 * and the 3rd returns all senses for the word/pos pair.
 * <p>
 * You can also retrieve the entire tree of hyponyms (down to the leaves) for a
 * specific sense of the word. (see VariousHyponyms.pde for examples)
 * 
 * <p>
 * Note: Valid wordnet parts-of-speech include (noun="n",verb="v",adj="a", and
 * adverb="r"). <br>
 * These can be specified either as literals or using the String constants:
 * 
 * <pre>
 *    RiWordnet.NOUN
 *    RiWordnet.VERB
 *    RiWordnet.ADJ
 *    RiWordnet.ADV
 * </pre>
 * <p>
 * Note: methods return null either when the query term is not found or there
 * are no entries for the relation type being sought.
 * 
 * <p>
 * For more info on the meaning of various wordnet concepts (synset, sense,
 * hypernym, etc), see: {@link http://wordnet.princeton.edu/gloss}
 * 
 * <p>
 * See the accompanying documentation for license information
 */
public class RiWordnet implements Wordnet
{
  /** String constant for Noun part-of-speech */
  public final static String NOUN = "n";

  /** String constant for Verb part-of-speech */
  public final static String VERB = "v";

  /** String constant for Adjective part-of-speech */
  public final static String ADJ = "a";

  /** String constant for Adverb part-of-speech */
  public final static String ADV = "r";

  /**
   * @invisible debug flag to toggle verbose output
   */
  public static final boolean DBUG = false;

  private static final String ROOT = "entity";

  /** @invisible */
  public static String wordnetHome;

  /** @invisible */
  public static String SLASH;
  
  /** @invisible */
  public Dictionary jwnlDict;
  
  public static RiZipReader zipReader;
  protected WordnetFilters filters;
  protected int maxCharsPerWord = 10;
  protected boolean ignoreCompoundWords = true;
  protected boolean ignoreUpperCaseWords = true;

  static
  {
    SLASH = System.getProperty("file.separator");
  }

  // -------------------- CONSTRUCTORS ----------------------------

  /**
   * Constructs an instance of <code>RiWordnet</code> using the included data
   * files.
   * @invisible
   */
  public RiWordnet()
  {
    this(null);
  }

  /**
   * Constructs an instance of <code>RiWordnet</code> using the Wordnet
   * installation whose location is specified at <code>wordnetInstallDir</code>.
   * 
   * @param wordnetInstallDir
   *          home directory for a pre-installed Wordnet installation.
   */
  public RiWordnet(String wordnetInstallDir)
  {
    this(wordnetInstallDir, getDefaultConfFile());
  }

  private RiWordnet(String wordnetHome, String confFile)
  {
    this.setWordnetHome(wordnetHome);

    if (DBUG)
      System.err.println("RiWordnet.RiWordnet("+ wordnetHome + "," + confFile + ")");

    if (!JWNL.isInitialized())
    {
      try
      {
        initWordnet(confFile);
      }
      catch (Exception e)
      {
        throw new WordnetError("Error loading WordNet with $WORDNET_HOME="
            + wordnetHome + " & CONF_FILE=" + confFile, e);
      }
    }
    
    if (this.jwnlDict == null)
      this.jwnlDict = Dictionary.getInstance();
  }

  private static String getDefaultConfFile()
  {
    // set the locale since the default conf is only English
    
    Locale.setDefault(Locale.ENGLISH);
    
    return DEFAULT_CONF;
  }

  /**
   * for remote creation only
   * 
   * @invisible
   */
  public static RiWordnet createRemote(Map params)
  {
    return new RiWordnet();
  }

  // METHODS =====================================================

  /**
   * Returns an iterator over all words of the specified 'pos'
   */
  public Iterator iterator(String pos)
  {
    return getFilters().lemmaIterator(jwnlDict, convertPos(pos));
  }


  /**
   * Returns up to <code>maxResults</code> full anagram matches for the
   * specified <code>word</code> and <code>pos</code>
   * <p>
   * Example: 'table' returns 'bleat' (but not 'tale').
   * 
   * @param word
   * @param posStr
   * @param maxResults
   */
  public String[] getAnagrams(String word, String posStr, int maxResults)
  {
    return filter(ANAGRAMS, word, convertPos(posStr), maxResults);
  }

  /**
   * Returns all full anagram matches for the specified <code>word</code> and
   * <code>pos</code>
   * <p>
   * Example: 'table' returns 'bleat' (but not 'tale').
   * 
   * @param word
   * @param posStr
   */
  public String[] getAnagrams(String word, String posStr)
  {
    return getAnagrams(word, posStr, Integer.MAX_VALUE);
  }

  /**
   * Returns up to <code>maxResults</code> of the specified <code>pos</code>
   * where each contains the given <code>word</code>
   * <p>
   * Example:
   * 
   * @param word
   * @param posStr
   * @param maxResults
   */
  public String[] getContains(String word, String posStr, int maxResults)
  {
    return filter(CONTAINS, word, convertPos(posStr), maxResults);
  }

  /**
   * Returns all 'contains' matches for the specified <code>word</code> and
   * <code>pos</code>
   * <p>
   * Example: 
   * 
   * @param word
   * @param posStr
   */
  public String[] getContains(String word, String posStr)
  {
    return getContains(word, posStr, Integer.MAX_VALUE);
  }

  /**
   * Returns up to <code>maxResults</code> of the specified <code>pos</code>
   * ending with the given <code>word</code>.
   * <p>
   * Example: 'table' returns 'turntable' & 'uncomfortable'
   * 
   * @param word
   * @param posStr
   * @param maxResults
   */
  public String[] getEndsWith(String word, String posStr, int maxResults)
  {
    return filter(ENDS_WITH, word, convertPos(posStr), maxResults);
  }

  /**
   * Returns up to <code>maxResults</code> of the specified <code>pos</code>
   * ending with the given <code>word</code>.
   * <p>
   * Example: 'table' returns 'turntable' & 'uncomfortable'
   * 
   * @param word
   * @param posStr
   */
  public String[] getEndsWith(String word, String posStr)
  {
    return getEndsWith(word, posStr, Integer.MAX_VALUE);
  }

  /**
   * Returns up to <code>maxResults</code> of the specified <code>pos</code>
   * starting with the given <code>word</code>.
   * <p>
   * Example: 'turn' returns 'turntable'
   * 
   * @param word
   * @param posStr
   * @param maxResults
   */
  public String[] getStartsWith(String word, String posStr, int maxResults)
  {
    return filter(STARTS_WITH, word, convertPos(posStr), maxResults);
  }

  /**
   * Returns up to <code>maxResults</code> of the specified <code>pos</code>
   * starting with the given <code>word</code>.
   * <p>
   * Example: 'turn' returns 'turntable'
   * 
   * @param word
   * @param posStr
   */
  public String[] getStartsWith(String word, String posStr)
  {
    return getStartsWith(word, posStr, Integer.MAX_VALUE);
  }

  /**
   * Returns up to <code>maxResults</code> of the specified <code>pos</code>
   * matching the the given regular expression <code>pattern</code>.
   * <p>
   * 
   * @param pattern
   * @param posStr
   * @param maxResults
   * @see java.util.regex.Pattern
   */
  public String[] getRegexMatch(String pattern, String posStr, int maxResults)
  {
    return filter(REGEX_MATCH, pattern, convertPos(posStr), maxResults);
  }

  /**
   * Returns up to <code>maxResults</code> of the specified <code>pos</code>
   * Example: '.*table' returns 'turntable' & 'uncomfortable'
   * 
   * @param pattern
   * @param posStr
   * @see java.util.regex.Pattern
   */
  public String[] getRegexMatch(String pattern, String posStr)
  {
    return getRegexMatch(pattern, posStr, Integer.MAX_VALUE);
  }

  /**
   * Returns up to <code>maxResults</code> of the specified <code>pos</code>
   * that match the soundex code of the given <code>word</code>.
   * <p>
   * 
   * @param pattern
   * @param posStr
   * @param maxResults
   */
  public String[] getSoundsLike(String pattern, String posStr, int maxResults)
  {
    return filter(SOUNDS_LIKE, pattern, convertPos(posStr), maxResults);
  }

  /**
   * Returns up to <code>maxResults</code> of the specified <code>pos</code>
   * that match the soundex code of the given <code>word</code>.
   * 
   * @param pattern
   * @param posStr
   */
  public String[] getSoundsLike(String pattern, String posStr)
  {
    return getSoundsLike(pattern, posStr, Integer.MAX_VALUE);
  }

  /**
   * Returns up to <code>maxResults</code> of the specified <code>pos</code>
   * matching a wildcard <code>pattern</code>,<br>
   * with * '*' equals any number of characters, <br>
   * and '?' equals any single character.
   * <p>
   * Example: 't?le' returns (tale,tile,tole)<br>
   * Example: 't*le' returns (tatumble, turtle, tussle, etc.)<br>
   * Example: 't?le*' returns (telex, tile,tilefish,tile,talent, tiles, etc.)
   * <br>
   * 
   * @param pattern
   * @param posStr
   * @param maxResults
   */
  public String[] getWildcardMatch(String pattern, String posStr, int maxResults)
  {
    return filter(WILDCARD_MATCH, pattern, convertPos(posStr), maxResults);
  }

  /**
   * Returns up to <code>maxResults</code> of the specified <code>pos</code>
   * matching a wildcard <code>pattern</code>,<br>
   * with '*' representing any number of characters, <br>
   * and '?' equals any single character..
   * <p>
   * Example: 't?le' returns (tale,tile,tole)<br>
   * Example: 't*le' returns (tatumble, turtle, tussle, etc.)<br>
   * Example: 't?le*' returns (telex, tile,tilefish,tile,talent, tiles, etc.)
   * <br>
   * 
   * @param pattern
   * @param posStr
   */
  public String[] getWildcardMatch(String pattern, String posStr)
  {
    return getWildcardMatch(pattern, posStr, Integer.MAX_VALUE);
  }

  /**
   * Return up to <code>maxResults</code> instances of specified
   * <code>posStr</code> matching the filter specified with
   * <code>filterFlag</code>
   * <p>
   * Filter types include:
   * 
   * <pre>
   * RiWordnet.EXACT_MATCH
   *         RiWordnet.ENDS_WITH
   *         RiWordnet.STARTS_WITH
   *         RiWordnet.ANAGRAMS 
   *         RiWordnet.CONTAINS_ALL
   *         RiWordnet.CONTAINS_SOME  
   *         RiWordnet.CONTAINS
   *         RiWordnet.SIMILAR_TO
   *         RiWordnet.SOUNDS_LIKE
   *         RiWordnet.WILDCARD_MATCH
   *         RiWordnet.REGEX_MATCH
   * </pre>
   * 
   * @param filterFlag
   * @param word
   * @param pos
   * @param maxResults
   * @invisible
   */
  public String[] filter(int filterFlag, String word, POS pos, int maxResults)
  {
    return toStrArr(getFilters().filter(filterFlag, word, pos, maxResults));
  }

  /**
   * @invisible Return all instances of specified <code>posStr</code> matching
   *            the filter specified with <code>filterFlag</code>.
   *            <p>
   *            Filter types include:
   * 
   *            <pre>
   * RiWordnet.EXACT_MATCH
   *         RiWordnet.ENDS_WITH
   *         RiWordnet.STARTS_WITH
   *         RiWordnet.ANAGRAMS 
   *         RiWordnet.CONTAINS_ALL
   *         RiWordnet.CONTAINS_SOME  
   *         RiWordnet.CONTAINS
   *         RiWordnet.SIMILAR_TO
   *         RiWordnet.SOUNDS_LIKE
   *         RiWordnet.WILDCARD_MATCH
   *         RiWordnet.REGEX_MATCH
   * </pre>
   * @example SimpleFilterExample.pde
   * @param word
   * @param pos
   * @param filterFlag
   */
  public String[] filter(int filterFlag, String word, POS pos)
  {
    return filter(filterFlag, word, pos, Integer.MAX_VALUE);
  }

  /**
   * Return up to <code>maxResults</code> instances of specified matching ANY of
   * the filters specified with <code>filterFlags</code>.
   * <p>
   * Filter types include:
   * 
   * <pre>
   * RiWordnet.EXACT_MATCH
   *         RiWordnet.ENDS_WITH
   *         RiWordnet.STARTS_WITH
   *         RiWordnet.ANAGRAMS 
   *         RiWordnet.CONTAINS_ALL
   *         RiWordnet.CONTAINS_SOME  
   *         RiWordnet.CONTAINS
   *         RiWordnet.SIMILAR_TO
   *         RiWordnet.SOUNDS_LIKE
   *         RiWordnet.WILDCARD_MATCH
   *         RiWordnet.REGEX_MATCH
   * </pre>
   * 
   * @param filterFlags
   * @param words
   * @param pos
   * @param maxResults
   * @invisible
   */
  public String[] orFilter(int[] filterFlags, String[] words, POS pos, int maxResults)
  {
    return toStrArr(getFilters().orFilter(filterFlags, words, pos, maxResults));
  }

  private WordnetFilters getFilters()
  {
    if (filters == null)
      filters = new WordnetFilters(this);
    return filters;
  }

  /**
   * @invisible Return all instances of specified <code>posStr</code> matching
   *            ANY of the filters specified with <code>filterFlags</code>.
   *            <p>
   *            Filter types include:
   * 
   *            <pre>
   * RiWordnet.EXACT_MATCH
   *         RiWordnet.ENDS_WITH
   *         RiWordnet.STARTS_WITH
   *         RiWordnet.ANAGRAMS 
   *         RiWordnet.CONTAINS_ALL
   *         RiWordnet.CONTAINS_SOME  
   *         RiWordnet.CONTAINS
   *         RiWordnet.SIMILAR_TO
   *         RiWordnet.SOUNDS_LIKE
   *         RiWordnet.WILDCARD_MATCH
   *         RiWordnet.REGEX_MATCH
   * </pre>
   * @example SimpleFilterExample.pde
   * @param word
   * @param pos
   * @param filterFlag
   */
  public String[] orFilter(int[] filterFlag, String[] word, POS pos)
  {
    return orFilter(filterFlag, word, pos, Integer.MAX_VALUE);
  }

  /**
   * Return up to <code>maxResults</code> instances of specified matching ALL of
   * the filters specified with <code>filterFlags</code>.
   * <p>
   * Filter types include:
   * 
   * <pre>
   * RiWordnet.EXACT_MATCH
   *         RiWordnet.ENDS_WITH
   *         RiWordnet.STARTS_WITH
   *         RiWordnet.ANAGRAMS 
   *         RiWordnet.CONTAINS_ALL
   *         RiWordnet.CONTAINS_SOME  
   *         RiWordnet.CONTAINS
   *         RiWordnet.SIMILAR_TO
   *         RiWordnet.SOUNDS_LIKE
   *         RiWordnet.WILDCARD_MATCH
   *         RiWordnet.REGEX_MATCH
   * </pre>
   * 
   * @param filterFlags
   * @param words
   * @param pos
   * @param maxResults
   * @invisible
   */
  private String[] andFilter(int[] filterFlags, String[] words, POS pos, int maxResults)
  {
    return toStrArr(getFilters().andFilter(filterFlags, words, pos, maxResults));
  }

  /**
   * @invisible Return all instances of specified <code>posStr</code> matching
   *            ALL of the filters specified with <code>filterFlags</code>.
   *            <p>
   *            Filter types include:
   * 
   *            <pre>
   * RiWordnet.EXACT_MATCH
   *         RiWordnet.ENDS_WITH
   *         RiWordnet.STARTS_WITH
   *         RiWordnet.ANAGRAMS 
   *         RiWordnet.CONTAINS_ALL
   *         RiWordnet.CONTAINS_SOME  
   *         RiWordnet.CONTAINS
   *         RiWordnet.SIMILAR_TO
   *         RiWordnet.SOUNDS_LIKE
   *         RiWordnet.WILDCARD_MATCH
   *         RiWordnet.REGEX_MATCH
   * </pre>
   * @example SimpleFilterExample.pde
   * @param word
   * @param pos
   * @param filterFlag
   */
  private String[] andFilter(int[] filterFlag, String[] word, POS pos)
  {
    return andFilter(filterFlag, word, pos, Integer.MAX_VALUE);
  }

  // ---------------- end filter methods -------------------

  /**
   * Called by PApplet on shutdown
   * 
   * @invisible
   */
  public void dispose()
  {
    // System.err.println("[INFO] RiTa.Wordnet.dispose()...");
    if (jwnlDict != null)
      jwnlDict.close();
    jwnlDict = null;
    if (zipReader != null)
      zipReader.dispose();
    zipReader = null;
  }

  /**
   * @invisible
   */
  protected void setWordnetHome(String wordnetHome)
  {
    if (wordnetHome != null)
    {
      if (!(wordnetHome.endsWith("/") || wordnetHome.endsWith("\\")))
        wordnetHome += SLASH;
    }
    RiWordnet.wordnetHome = wordnetHome;
    String home = wordnetHome != null ? wordnetHome : "jar:/rita/wordnet/WordNet3.1";
    System.out.println("[INFO] RiTa.Wordnet.HOME=" + home);
  }

  // -------------------------- MAIN METHODS ----------------------------
  private List getSynsetList(int id)
  {
    Synset syns = getSynsetAtId(id);

    // System.out.println("getSynsetList("id+") -> "+syns);

    if (syns == null || syns.getWordsSize() < 1)
      return null;
    List l = new LinkedList();
    addLemmas(syns.getWords(), l);
    return l;
  }

  private Synset getSynsetAtId(int id)
  {
    POS pos = null;
    String idStr = Integer.toString(id);
    int posDigit = Integer.parseInt(idStr.substring(0, 1));
    long offset = Long.parseLong(idStr.substring(1));
    switch (posDigit) {
    case 9:
      pos = POS.NOUN;
      break;
    case 8:
      pos = POS.VERB;
      break;
    case 7:
      pos = POS.ADJECTIVE;
      break;
    case 6:
      pos = POS.ADVERB;
      break;
    }
    try
    {
      return jwnlDict.getSynsetAt(pos, offset);
    }
    catch (JWNLException e)
    {
      throw new WordnetError(e);
    }
  }

  /**
   * Returns an array of unique ids, one for each 'sense' of <code>word</code>
   * with <code>pos</code>, or null if none are found.
   * <p>
   * A Wordnet 'sense' refers to a specific Wordnet meaning and maps 1-1 to the
   * concept of synsets. Each 'sense' of a word exists in a different synset.
   * <p>
   * For more info, see: {@link http://wordnet.princeton.edu/gloss}
   */
  public int[] getSenseIds(String word, String posStr)
  {
    POS pos = convertPos(posStr);
    // System.out.println("getSenseIds()="+posStr+" -> "+pos);
    IndexWord idw = lookupIndexWord(pos, word);
    return getSenseIds(idw);
  }

  /**
   * Returns an array of unique ids, one for each sense of <code>word</code>
   * with <code>pos</code>, or null if none are found.
   */
  public int[] getSenseIds(IndexWord idw)
  {
    int[] result = null;
    try
    {
      int numSenses = idw.getSenseCount();
      if (idw == null || numSenses == 0)
        return null;
      long[] offsets = idw.getSynsetOffsets();
      result = new int[offsets.length];
      for (int i = 0; i < result.length; i++)
        result[i] = toId(idw.getPOS(), offsets[i]);
    }
    catch (Exception e)
    {
      throw new WordnetError(e);
    }
    // System.err.println("ids: "+Util.asList(result));
    return result;
  }

  private int toId(POS wnpos, long offset)
  {
    int posDigit = -1;
    if (wnpos == POS.NOUN)
      posDigit = 9;
    else if (wnpos == POS.VERB)
      posDigit = 8;
    else if (wnpos == POS.ADJECTIVE)
      posDigit = 7;
    else if (wnpos == POS.ADVERB)
      posDigit = 6;
    else
      throw new WordnetError("Invalid POS type: " + wnpos);
    return Integer.parseInt((Integer.toString(posDigit) + offset));
  }

  /**
   * Returns full gloss for 1st sense of 'word' with 'pos' or null if not found
   */
  public String getGloss(String word, String pos)
  {
    Synset synset = getSynsetAtIndex(word, pos, 1);
    return getGloss(synset);
  }

  /**
   * Returns glosses for all senses of 'word' with 'pos', or null if not found
   */
  public String[] getAllGlosses(String word, String pos)
  {
    List glosses = new LinkedList();

    Synset[] synsets = allSynsets(word, pos);
    for (int i = 0; i < synsets.length; i++)
    {
      String gloss = getGloss(synsets[i]);
      if (gloss != null)
        glosses.add(gloss);
    }
    return toStrArr(glosses);
  }

  /**
   * Returns full gloss for word with unique <code>senseId</code>, or null if
   * not found
   */
  public String getGloss(int senseId)
  {
    Synset synset = getSynsetAtId(senseId);
    if (synset == null)
      return null;
    return getGloss(synset);
  }

  /**
   * Returns description for word with unique <code>senseId</code>, or null if
   * not found
   */
  public String getDescription(int senseId)
  {
    String gloss = getGloss(senseId);
    return WordnetUtil.parseDescription(gloss);
  }

  private String getGloss(Synset synset)
  {
    if (synset == null)
      return null;
    return synset.getGloss();
  }

  /**
   * Returns description for <code>word</code> with <code>pos</code> or null if
   * not found
   */
  public String getDescription(String word, String pos)
  {
    String gloss = getGloss(word, pos);
    return WordnetUtil.parseDescription(gloss);
  }

  /**
   * Returns all examples for 1st sense of <code>word</code> with
   * <code>pos</code>, or null if not found
   */
  public String[] getExamples(CharSequence word, CharSequence pos)
  {
    Synset synset = getSynsetAtIndex(word, pos, 1);
    List l = getExamples(synset);
    return toStrArr(l);
  }

  /**
   * Return a random example from the set of examples from all senses of
   * <code>word</code> with <code>pos</code>, assuming they contain
   * <code>word</code>, or else null if not found
   */
  public String getAnyExample(CharSequence word, CharSequence pos)
  {
    String[] all = getAllExamples(word, pos);
    int rand = (int) (Math.random() * all.length);
    return all[rand];
  }

  /**
   * Returns examples for word with unique <code>senseId</code>, or null if not
   * found
   */
  public String[] getExamples(int senseId)
  {
    Synset synset = getSynsetAtId(senseId);
    if (synset == null)
      return null;
    return toStrArr(getExamples(synset));
  }

  /**
   * Returns examples for all senses of <code>word</code> with <code>pos</code>
   * if they contain the <code>word</code>, else null if not found
   */
  public String[] getAllExamples(CharSequence word, CharSequence pos)
  {
    Synset[] syns = allSynsets(word, pos);
    if (syns == null || syns.length < 1)
      return null;
    List l = new LinkedList();
    for (int i = 0; i < syns.length; i++)
    {
      if (syns[i] != null)
      {
        for (int j = 0; j < syns.length; j++)
        {
          List examples = getExamples(syns[i]);
          if (examples == null)
            continue;
          for (Iterator k = examples.iterator(); k.hasNext();)
          {
            String example = (String) k.next();
            // does it contain the word
            if (example.indexOf(word.toString()) < 0)
              continue;
            if (!l.contains(example))
              l.add(example);
          }
        }
      }
    }
    l.remove(word);
    return toStrArr(l);
  }

  private List getExamples(Synset synset)
  {
    String gloss = getGloss(synset);
    return WordnetUtil.parseExamples(gloss);
  }

  /**
   * Returns an unordered String[] containing the synset, hyponyms, similars,
   * alsoSees, and coordinate terms (checking each in order), or null if not
   * found.
   */
  public String[] getAllSynonyms(int senseId, int maxResults)
  {
    String[] result = null;
    Set set = new HashSet();

    result = getSynset(senseId);
    this.addSynsetsToSet(result, set);
    // System.err.println("Synsets: "+WordnetUtil.asList(result));

    result = getHyponymTree(senseId);
    this.addSynsetsToSet(result, set);
    // System.err.println("Hypornyms: "+WordnetUtil.asList(result));

    /*
     * result = getHypernyms(senseId); this.addSynsetsToSet(result, set);
     */
    // System.err.println("Hypernyms: "+WordnetUtil.asList(result));

    result = getSimilar(senseId);
    this.addSynsetsToSet(result, set);
    // System.err.println("Similar: "+WordnetUtil.asList(result));

    result = getCoordinates(senseId);
    this.addSynsetsToSet(result, set);
    // System.err.println("Coordinates: "+WordnetUtil.asList(result));

    result = getAlsoSees(senseId);
    this.addSynsetsToSet(result, set);
    // System.err.println("AlsoSees: "+WordnetUtil.asList(result));

    // System.err.println("=======================================");

    return setToStrings(set, maxResults, true);
  }

  public String[] getAllSynonyms(int id)
  {
    return getAllSynonyms(id, Integer.MAX_VALUE);
  }

  /**
   * Returns an unordered String[] containing the synset, hyponyms, similars,
   * alsoSees, and coordinate terms (checking each in order) for all senses of
   * <code>word</code> with <code>pos</code>, or null if not found
   */
  public String[] getSynonyms(String word, String posStr, int maxResults)
  {
    String[] result = null;
    Set set = new HashSet();

    result = getSynset(word, posStr, false);
    this.addSynsetsToSet(result, set);
    // System.err.println("Synsets: "+WordnetUtil.asList(result));
    /*
     * result = getHyponyms(word, posStr); this.addSynsetsToSet(result, set);
     * //System.err.println("Hyponyms: "+WordnetUtil.asList(result));
     */
    result = getHypernyms(word, posStr);
    this.addSynsetsToSet(result, set);
    // System.err.println("Hypernyms: "+WordnetUtil.asList(result));

    result = getSimilar(word, posStr);
    this.addSynsetsToSet(result, set);
    // System.err.println("Similar: "+WordnetUtil.asList(result));

    result = getAlsoSees(word, posStr);
    this.addSynsetsToSet(result, set);
    // System.err.println("AlsoSees: "+WordnetUtil.asList(result));

    result = getCoordinates(word, posStr);
    this.addSynsetsToSet(result, set);
    // System.err.println("Coordinates: "+WordnetUtil.asList(result));

    // System.err.println("=======================================");

    return setToStrings(set, maxResults, true);
  }

  /**
   * Returns an unordered String[] containing the synset, hyponyms, similars,
   * alsoSees, and coordinate terms (checking each in order) for all senses of
   * <code>word</code> with <code>pos</code>, or null if not found
   */
  public String[] getSynonyms(String word, String posStr)
  {
    return getSynonyms(word, posStr, Integer.MAX_VALUE);
  }

  /**
   * Returns an unordered String[] containing the synset, hyponyms, similars,
   * alsoSees, and coordinate terms (checking each in order) for all senses of
   * <code>word</code> with <code>pos</code>, or null if not found
   */
  public String[] getAllSynonyms(String word, String posStr, int maxResults)
  {
    final boolean dbug = false;

    String[] result = null;
    Set set = new HashSet();

    result = getAllSynsets(word, posStr);
    this.addSynsetsToSet(result, set);
    if (dbug)
      System.err.println("Synsets: " + WordnetUtil.asList(result));

    result = getAllHyponyms(word, posStr);
    this.addSynsetsToSet(result, set);
    if (dbug)
      System.err.println("Hyponyms: " + WordnetUtil.asList(result));
    if (dbug)
      System.err.println("Set: " + WordnetUtil.asList(set));

    /*
     * result = getAllHypernyms(word, posStr); this.addSynsetsToSet(result,
     * set); if
     * (dbug)System.err.println("Hypernyms: "+WordnetUtil.asList(result));
     */

    result = getAllSimilar(word, posStr);
    this.addSynsetsToSet(result, set);
    if (dbug)
      System.err.println("Similar: " + WordnetUtil.asList(result));

    result = getAllAlsoSees(word, posStr);
    this.addSynsetsToSet(result, set);
    if (dbug)
      System.err.println("AlsoSees: " + WordnetUtil.asList(result));

    result = getAllCoordinates(word, posStr);
    this.addSynsetsToSet(result, set);
    if (dbug)
      System.err.println("Coordinates: " + WordnetUtil.asList(result));

    // System.err.println("=======================================");
    return setToStrings(set, maxResults, true);
  }

  public String[] getAllSynonyms(String word, String posStr)
  {
    return getAllSynonyms(word, posStr, Integer.MAX_VALUE);
  }

  private void addSynsetsToSet(String[] s, Set set)
  {
    addSynsetsToSet(s, set, Integer.MAX_VALUE);
  }

  private void addSynsetsToSet(String[] s, Set set, int maxResults)
  {
    if (s == null || s.length < 0)
      return;
    for (int i = 0; i < s.length; i++)
    {
      if (s[i].indexOf(SYNSET_DELIM) > 0)
      {
        String[] t = s[i].split(SYNSET_DELIM);
        for (int u = 0; u < t.length; u++)
        {
          set.add(t[u]);
          if (set.size() >= maxResults)
            return;
        }
      }
      else
      {
        set.add(s[i]);
        if (set.size() >= maxResults)
          return;
      }
    }
  }

  private String[] setToStrings(Set set, int maxSize, boolean shuffle)
  {

    if (set == null || set.size() == 0)
      return null;
    List result = new ArrayList(set.size());
    result.addAll(set);
    Collections.shuffle(result);
    int size = Math.min(maxSize, set.size());

    int idx = 0;
    String[] ret = new String[size];
    for (Iterator i = result.iterator(); i.hasNext();)
    {
      ret[idx++] = (String) i.next();
      if (idx == size)
        break;
    }
    return ret;
  }

  /**
   * Returns String[] of Common Parents for 1st senses of words with specified
   * pos' or null if not found
   */
  public String[] getCommonParents(String word1, String word2, String pos)
  {
    List result = getCommonParentList(word1, word2, pos);
    return toStrArr(result);
  }

  /**
   * Returns common parent for words with unique ids <code>id1</code>,
   * <code>id2</code>, or null if either word or no parent is found
   */
  public Synset getCommonParent(int id1, int id2) throws JWNLException
  {
    Synset syn1 = getSynsetAtId(id1);
    if (syn1 == null)
      return null;
    Synset syn2 = getSynsetAtId(id2);
    if (syn2 == null)
      return null;
    RelationshipList list = RelationshipFinder.getInstance().findRelationships(syn1, syn2, PointerType.HYPERNYM);
    AsymmetricRelationship ar = (AsymmetricRelationship) list.get(0); // why 0??
    PointerTargetNodeList nl = ar.getNodeList();
    PointerTargetNode ptn = (PointerTargetNode) nl.get(ar.getCommonParentIndex());
    return ptn.getSynset();
  }

  private List getCommonParentList(String word1, String word2, String pos)
  {
    Synset syn = null;
    try
    {
      POS wnpos = convertPos(pos);
      IndexWord iw1 = lookupIndexWord(wnpos, word1);
      if (iw1 == null)
        return null;
      IndexWord iw2 = lookupIndexWord(wnpos, word2);
      if (iw2 == null)
        return null;
      syn = getCommonParent(iw1, iw2);
      if (syn == null)
        return null;
    }
    catch (JWNLException e)
    {
      throw new WordnetError(this, e);
    }
    List l = new ArrayList();
    addLemmas(syn.getWords(), l);
    return l == null || l.size() < 1 ? null : l;
  }

  private Synset getCommonParent(IndexWord start, IndexWord end) throws JWNLException
  {
    if (start == null || end == null)
      return null;

    RelationshipList list = null;
    try
    {
      list = RelationshipFinder.getInstance().findRelationships(start.getSense(1), end.getSense(1), PointerType.HYPERNYM);
    }
    catch (NullPointerException e)
    {
      // ignore jwnl bug
    }
    if (list == null)
      return null;
    // System.out.println("Hypernym relationship between \"" + start.getLemma()
    // + "\" and \"" + end.getLemma() + "\":");
    AsymmetricRelationship ar = (AsymmetricRelationship) list.get(0);
    PointerTargetNodeList nl = ar.getNodeList();
    // System.out.println("Common Parent Index: "+ar.getCommonParentIndex());
    PointerTargetNode ptn = (PointerTargetNode) nl.get(ar.getCommonParentIndex());
    return ptn.getSynset();
  }

  // SYNSETS

  /**
   * Returns String[] of words in synset for first sense of <code>word</code>
   * with <code>pos</code>, or null if not found.
   * <P>
   * Note: original word is excluded by default.
   * 
   * @see #getSynset(String, String, boolean)
   */
  public String[] getSynset(String word, String pos)
  {
    return getSynset(word, pos, false);
  }

  /**
   * Returns String[] of words in synset for first sense of <code>word</code>
   * with <code>pos</code>, or null if not found.
   */
  public String[] getSynset(String word, String pos, boolean includeOriginal)
  {
    Synset syns = getSynsetAtIndex(word, pos, 1);
    if (syns == null || syns.getWordsSize() < 1)
      return null;
    List l = new LinkedList();
    Word[] words = syns.getWords();
    addLemmas(words, l);
    if (!includeOriginal)
      l.remove(word);

    // System.out.println("RiWordnet.getSynset("+word+","+pos+") -> "+l);

    return toStrArr(l);
  }

  /**
   * Returns String[] of Synsets for unique id <code>id</code> or null if not
   * found.
   */
  public String[] getSynset(int id)
  {
    return toStrArr(getSynsetList(id));
  }

  /**
   * Returns String[] of words in each synset for all senses of
   * <code>word</code> with <code>pos</code>, or null if not found
   */
  public String[] getAllSynsets(String word, String posStr)
  {
    POS pos = convertPos(posStr);
    IndexWord idw = null;
    List result = null;
    try
    {
      idw = lookupIndexWord(pos, word);
      if (idw == null || idw.getSenseCount() < 1)
        return null;
      result = new LinkedList();
      for (int i = 1; i <= idw.getSenseCount(); i++)
      {
        List syns = this.getSynsetAtIndex(idw, i);
        if (syns == null || syns.size() < 1)
          continue;
        for (Iterator j = syns.iterator(); j.hasNext();)
        {
          String lemma = (String) j.next();
          addLemma(lemma, result);
        }
      }
      result.remove(word); // don't include original
      return toStrArr(result);
    }
    catch (JWNLException e)
    {
      throw new WordnetError(e);
    }
  }

  private List getSynsetAtIndex(IndexWord word, int index) throws JWNLException
  {
    if (index < 1)
      throw new IllegalArgumentException("Invalid index: " + index);

    if (word == null || word.getSenseCount() < 1)
      return null;

    List l = new ArrayList();
    addLemmas(word.getSense(index).getWords(), l);
    return l;
  }

  private Synset[] allSynsets(CharSequence word, CharSequence posStr)
  {
    POS pos = convertPos(posStr);
    IndexWord idw = lookupIndexWord(pos, word);
    if (idw == null)
      return null;
    int senseCount = idw.getSenseCount();
    if (senseCount < 1)
      return null;
    Synset[] syns = new Synset[senseCount];
    for (int i = 0; i < syns.length; i++)
    {
      try
      {
        syns[i] = idw.getSense(i + 1);
        if (syns[i] == null)
          System.err.println("[WARN] Wordnet returned null Synset for: " + word + "/" + pos);
      }
      catch (JWNLException e)
      {
        throw new WordnetError(e);
      }
    }
    return syns;
  }

  private Synset getSynsetAtIndex(CharSequence word, CharSequence posStr, int i)
  {
    if (i < 1)
      throw new IllegalArgumentException("Invalid index: " + i);
    POS pos = convertPos(posStr);
    IndexWord idw = lookupIndexWord(pos, word);
    if (idw == null || idw.getSenseCount() < i)
      return null;
    try
    {
      return idw.getSense(i);
    }
    catch (JWNLException e)
    {
      throw new WordnetError(e);
    }
  }

  /**
   * Return the # of senses (polysemy) for a given word/pos. A 'sense' refers to
   * a specific Wordnet meaning and maps 1-1 to the concept of synsets. Each
   * 'sense' of a word exists in a different synset.
   * <p>
   * For more info, see: {@link http://wordnet.princeton.edu/gloss}
   * 
   * @return # of senses or -1 if not found
   */
  public int getSenseCount(String word, String pos)
  {
    int senses = -1;
    try
    {
      IndexWord iw = lookupIndexWord(pos, word);
      if (iw != null)
        senses = iw.getSenseCount();
    }
    catch (WordnetError e)
    {
      System.err.println("[WARN] " + e.getMessage());
    }
    return senses;
  }

  /*
   * private List getAllSynsets(IndexWord word) throws JWNLException { List l =
   * new ArrayList(); Synset[] syns = word.getSenses(); if (syns == null ||
   * syns.length <= 0) return l; for (int k = 0; k < syns.length; k++)
   * addLemmas(syns[k].getWords(), l); return l; }
   */

  // ANTONYMS ------------
  /**
   * Returns String[] of Antonyms for the 1st sense of <code>word</code> with
   * <code>pos</code> or null if not found<br>
   * 
   * Example: 'night' -> 'day', "full", -> "empty"
   */
  public String[] getAntonyms(String word, String pos)
  {
    return getPointerTargetsAtIndex(word, pos, PointerType.ANTONYM, 1);
  }

  /**
   * Returns String[] of Antonyms for the specified id, or null if not found<br>
   * Holds for adjectives only (?)
   */
  public String[] getAntonyms(int id)
  {
    return getPointerTargetsAtId(id, PointerType.ANTONYM);
  }

  /**
   * Returns String[] of Antonyms for the 1st sense of <code>word</code> with
   * <code>pos</code> or null if not found<br>
   * Holds for adjectives only (?)
   */
  public String[] getAllAntonyms(String word, String pos)
  {
    return getAllPointerTargets(word, pos, PointerType.ANTONYM);
  }

  /*
   * Returns String[] of Antonyms for the 1st sense of 'word' with specified pos
   * private List getAntonymsAtIndex(IndexWord idw, int index) throws
   * JWNLException { if (idw == null) return null;
   * 
   * Synset[] synsets = idw.getSenses(); if (synsets == null || synsets.length
   * <= 0) return null;
   * 
   * List l = new ArrayList(); PointerUtils pu = PointerUtils.getInstance();
   * PointerTargetNodeList nodeList = pu.getAntonyms(synsets[index]);
   * getLemmaSet(nodeList, l);
   * 
   * return l == null || l.size() < 1 ? null : l; }
   */
  // HYPERNYMS -- direct
  /**
   * Returns Hypernym String[] for all senses of <code>word</code> with
   * <code>pos</code> or null if not found
   * <p>
   * X is a hyponym of Y if there exists an is-a relationship between X and Y.<br>
   * That is, if X is a subtype of Y. <br>
   * Or, for xample, if X is a species of the genus Y. <br>
   * X is a hypernym of Y is Y is a hyponym of X. <br>
   * Holds between: nouns and nouns & verbs and verbs<br>
   * Examples:
   * <ul>
   * <li>artifact is a hyponym of object
   * <li>object is a hypernym of artifact
   * <li>carrot is a hyponym of herb
   * <li>herb is a hypernym of carrot
   * </ul>
   */
  public String[] getHypernyms(String word, String posStr)
  {
    Synset synset = getSynsetAtIndex(word, posStr, 1);
    PointerTargetNodeList ptnl = null;
    try
    {
      ptnl = PointerUtils.getInstance().getDirectHypernyms(synset);
    }
    catch (NullPointerException e)
    {
      // ignore jwnl bug
      System.err.println("[WARN] JWNL Error: " + word + "/" + posStr);
    }
    catch (JWNLException e)
    {
      throw new WordnetError(e);
    }
    return ptnlToStrings(word, ptnl);
  }

  /**
   * Returns Hypernym String[] for id, or null if not found
   * <p>
   * X is a hyponym of Y if there exists an is-a relationship between X and Y.<br>
   * That is, if X is a subtype of Y. <br>
   * Or, for example, if X is a species of the genus Y. <br>
   * X is a hypernym of Y is Y is a hyponym of X. <br>
   * Holds between: nouns and nouns & verbs and verbs<br>
   * Examples:
   * <ul>
   * <li>artifact is a hyponym of object
   * <li>object is a hypernym of artifact
   * <li>carrot is a hyponym of herb
   * <li>herb is a hypernym of carrot
   * </ul>
   */
  public String[] getHypernyms(int id)
  {
    Synset synset = getSynsetAtId(id);
    PointerTargetNodeList ptnl = null;
    try
    {
      ptnl = PointerUtils.getInstance().getDirectHypernyms(synset);
    }
    catch (NullPointerException e)
    {
      // ignore jwnl bug
    }
    catch (JWNLException e)
    {
      throw new WordnetError(e);
    }
    return ptnlToStrings(null, ptnl);
  }

  /*
   * private List getHypernymList(String word, String posStr) { POS pos =
   * convertPos(posStr); try { IndexWord idw = lookupIndexWord(pos, word); if
   * (idw == null) return null; return this.getHypernyms(idw); } catch
   * (JWNLException e) { throw new CTextError(this, e); }}
   */

  /*
   * Adds the hypernyms for the first sense of IndexWord to List @param word -
   * IndexWord on which to search @param l - List to which we add hypernyms
   */
  /*
   * private List getHypernyms(IndexWord word) throws JWNLException { if (word
   * == null) return null;
   * 
   * Synset[] synsets = word.getSenses(); if (synsets == null || synsets.length
   * == 0) return null;
   * 
   * List l = new ArrayList(); // maintain order? int i = 0; // for (; i <
   * synsets.length; i++) getHypernyms(synsets[i], l); return l == null ||
   * l.size() < 1 ? null : l; }
   */

  /*
   * Adds the hypernyms for this 'synset' to List TODO: redo with a List,
   * checking for dups
   */
  private void getHypernyms(Synset syn, Collection l) throws JWNLException
  {

    PointerTargetNodeList ptnl = null;
    try
    {
      ptnl = PointerUtils.getInstance().getDirectHypernyms(syn);
    }
    catch (NullPointerException e)
    {
      // bug from jwnl, ignore
    }
    getLemmaSet(ptnl, l);
  }

  // HYPERNYMS -- tree

  /**
   * Returns an ordered String[] of hypernym-synsets (each a semi-colon
   * delimited String) up to the root of Wordnet for the 1st sense of the word,
   * or null if not found
   * 
   * @example VariousHypernyms
   */
  public String[] getAllHypernyms(String word, String posStr)
  {
    try
    {
      IndexWord idw = lookupIndexWord(convertPos(posStr), word);
      return toStrArr(this.getAllHypernyms(idw));
    }
    catch (JWNLException e)
    {
      throw new WordnetError(this, e);
    }
  }

  /**
   * Returns an ordered String[] of hypernym-synsets (each a semi-colon
   * delimited String) up to the root of Wordnet for the <code>id</code>, or
   * null if not found
   */
  public String[] getHypernymTree(int id) throws JWNLException
  {
    Synset synset = getSynsetAtId(id);
    if (synset == null)
      return new String[]{ROOT};
    List l = getHypernymTree(synset);
    return toStrArr(l);
  }

  private List getAllHypernyms(IndexWord idw) throws JWNLException
  {
    if (idw == null)
      return null;
    Synset[] synsets = idw.getSenses();
    if (synsets == null || synsets.length <= 0)
      return null;

    int i = 0;
    List result = new LinkedList();
    for (; i < synsets.length; i++)
      getHypernyms(synsets[i], result);
    return result == null || result.size() < 1 ? null : result;
  }

  private List getHypernymTree(Synset synset) throws JWNLException
  {
    // System.err.println("RiWordnet.getHypernymTree("+word+","+synset+")");
    if (synset == null)
      return null;

    PointerTargetTree ptt = null;
    try
    {
      ptt = PointerUtils.getInstance().getHypernymTree(synset);

    }
    catch (NullPointerException e)
    {
      // ignore bad jwnl bug here
    }

    if (ptt == null)
      return null;

    List pointerTargetNodeLists = ptt.toList();
    // System.err.println("#PTNLS -> "+pointerTargetNodeLists.size());
    int count = 0; // why only one item per tree/synset?
    List l = new ArrayList();
    for (Iterator i = pointerTargetNodeLists.iterator(); i.hasNext(); count++)
    {
      PointerTargetNodeList ptnl = (PointerTargetNodeList) i.next();
      List strs = this.getLemmaStrings(ptnl, SYNSET_DELIM, false);
      // System.err.println("  STRS -> "+strs);
      for (Iterator it = strs.iterator(); it.hasNext();)
      {
        String lemma = (String) it.next();
        if (lemma.length() > 0 && !l.contains(lemma))// &&
                                                     // !lemma.equalsIgnoreCase(word))
          l.add(lemma);
      }
    }
    if (l.size() == 1)
      l.remove(0); // ignore the current synset (is this ok??)

    return l == null || l.size() < 1 ? null : l;
  }

  /*
   * // HYPONYMS (direct)
   * 
   * private List getHyponymList(String word, String posStr) { POS pos =
   * convertPos(posStr); try { IndexWord idw = lookupIndexWord(pos, word); if
   * (idw == null) return null; return this.getHyponyms(idw); } catch
   * (JWNLException e) { throw new WordnetError(this, e); } }
   */

  // HYPONYMS (direct)

  /**
   * Returns Hyponym String[] for 1st sense of <code>word</code> with
   * <code>pos</code> or null if not found
   * <p>
   * X is a hyponym of Y if there exists an is-a relationship between X and Y.<br>
   * That is, if X is a subtype of Y. <br>
   * Or, for xample, if X is a species of the genus Y. <br>
   * X is a hypernym of Y is Y is a hyponym of X. <br>
   * Holds between: nouns and nouns & verbs and verbs<br>
   * Examples:
   * <ul>
   * <li>artifact is a hyponym of object
   * <li>object is a hypernym of artifact
   * <li>carrot is a hyponym of herb
   * <li>herb is a hypernym of carrot
   * </ul>
   */
  public String[] getHyponyms(String word, String posStr)
  {
    Synset synset = getSynsetAtIndex(word, posStr, 1);
    // System.out.println("syn="+(synset.toString()));
    PointerTargetNodeList ptnl = null;
    try
    {
      PointerUtils pu = PointerUtils.getInstance();
      ptnl = pu.getDirectHyponyms(synset);

      if (ptnl == null)
        throw new RuntimeException("JWNL ERR: " + word + "/" + posStr);
      // System.out.println(word+"/"+posStr+" -> "+ptnl.size());
    }
    catch (NullPointerException e)
    {
      throw new RuntimeException("JWNL BUG: " + word + "/" + posStr);
    }
    catch (JWNLException e)
    {
      throw new WordnetError(e);
    }
    return ptnlToStrings(word, ptnl);
  }

  /**
   * Returns Hyponym String[] for id, or null if not found
   * <p>
   * X is a hyponym of Y if there exists an is-a relationship between X and Y.<br>
   * That is, if X is a subtype of Y. <br>
   * Or, for xample, if X is a species of the genus Y. <br>
   * X is a hypernym of Y is Y is a hyponym of X. <br>
   * Holds between: nouns and nouns & verbs and verbs<br>
   * Examples:
   * <ul>
   * <li>artifact is a hyponym of object
   * <li>object is a hypernym of artifact
   * <li>carrot is a hyponym of herb
   * <li>herb is a hypernym of carrot
   * </ul>
   */
  public String[] getHyponyms(int id)
  {
    Synset synset = getSynsetAtId(id);
    PointerTargetNodeList ptnl = null;
    try
    {
      ptnl = PointerUtils.getInstance().getDirectHyponyms(synset);
    }
    catch (NullPointerException e)
    {
      // ignore jwnl bug
    }
    catch (JWNLException e)
    {
      throw new WordnetError(e);
    }
    return ptnlToStrings(null, ptnl);
  }

  /*
   * private List getHyponyms(IndexWord word) throws JWNLException { if (word ==
   * null) return null;
   * 
   * Synset[] synsets = word.getSenses(); if (synsets == null || synsets.length
   * <= 0) return null;
   * 
   * List l = new ArrayList(); int i = 0; // for (; i < synsets.length; i++)
   * getHyponyms(synsets[i], l);
   * 
   * return l == null || l.size() < 1 ? null : l; }
   */

  /* Adds the hyponyms for this 'synset' to List */
  private void getHyponyms(Synset syn, Collection l) throws JWNLException
  {
    PointerTargetNodeList ptnl = null;
    try
    {
      PointerUtils pu = PointerUtils.getInstance();
      ptnl = pu.getDirectHyponyms(syn);
    }
    catch (NullPointerException e)
    {
      // bug in jwnl, throws null-pointer instead of returning null or 0-size
      // list
      System.out.println("JWNL BUG: " + e);
      return;
    }
    getLemmaSet(ptnl, l);
  }

  // HYPONYMS (tree)

  /**
   * Returns an unordered String[] of hyponym-synsets (each a colon-delimited
   * String), or null if not found
   * 
   * @example VariousHyponyms
   */
  public String[] getAllHyponyms(String word, String posStr)
  {
    IndexWord idw = lookupIndexWord(convertPos(posStr), word);
    List l = this.getAllHyponyms(idw);
    if (l == null)
      return null;
    l.remove(word);
    return toStrArr(l);
  }

  /*
   * private List getAllHyponyms(IndexWord idw) { int[] ids = getSenseIds(idw);
   * for (int i = 0; i < ids.length; i++) { getHyponyms(ids[i]); } return null;
   * }
   */
  private List getAllHyponyms(IndexWord idw)
  {
    if (idw == null)
      return null;

    // String lemma = idw.getLemma();
    Synset[] synsets = null;
    try
    {
      synsets = idw.getSenses();
    }
    catch (JWNLException e)
    {
      throw new WordnetError(e);
    }
    if (synsets == null || synsets.length <= 0)
      return null;

    List l = new LinkedList();
    for (int i = 0; i < synsets.length; i++)
    {
      try
      {
        // System.err.println(i+") "+synsets[i].getGloss());
        getHyponyms(synsets[i], l);
      }
      catch (JWNLException e)
      {
        e.printStackTrace();
      }
    }
    // for (Iterator i = l.iterator(); i.hasNext();)
    // System.err.println(i.next());

    return l == null || l.size() < 1 ? null : l;
  }

  /**
   * Returns an unordered String[] of hyponym-synsets (each a colon-delimited
   * String) representing all paths to leaves in the ontology (the full hyponym
   * tree), or null if not found
   * <p>
   */
  public String[] getHyponymTree(int id)
  {
    Synset synset = getSynsetAtId(id);
    if (synset == null)
      return null;

    List l = null;
    try
    {
      l = getHyponymTree(synset);
    }
    catch (JWNLException e)
    {
      e.printStackTrace();
    }
    return toStrArr(l);
  }

  /*
   * private List getAllHyponyms(IndexWord idw) { if (idw == null) return null;
   * 
   * String lemma = idw.getLemma(); Synset[] synsets = null; try { synsets =
   * idw.getSenses(); } catch (JWNLException e) { throw new WordnetError(e); }
   * if (synsets == null || synsets.length <= 0) return null;
   * 
   * int i = 0; // ? List l = null; try { l = getHyponymTree(idw.getLemma(),
   * synsets[i]); } catch (JWNLException e) { throw new WordnetError(e); }
   * 
   * return l == null || l.size() < 1 ? null : l; }
   */

  private List getHyponymTree(Synset synset) throws JWNLException
  {
    if (synset == null)
      return null;

    PointerTargetTree ptt = null;
    try
    {
      ptt = PointerUtils.getInstance().getHyponymTree(synset);
    }
    catch (NullPointerException e)
    {
      // ignore bad jwnl bug here
    }
    if (ptt == null)
      return null;

    List pointerTargetNodeLists = ptt.toList();

    // 1 element per unique path to a leaf
    List l = new ArrayList();
    for (Iterator i = pointerTargetNodeLists.iterator(); i.hasNext();)
    {
      PointerTargetNodeList ptnl = (PointerTargetNodeList) i.next();
      List tmp = this.getLemmaStrings(ptnl, SYNSET_DELIM, true);

      // 1 element per synset (comma-delimited)
      for (Iterator it = tmp.iterator(); it.hasNext();)
      {
        String syn = (String) it.next();
        syn = trimFirstandLastChars(syn);
        if (syn.length() < 2)// || syn.equalsIgnoreCase(word)) // not the
                             // original
          continue;
        if (!l.contains(syn)) // no-dups
          l.add(syn);
      }
    }

    // remove all the entries from the current synset (rethink)
    Set syns = new HashSet();
    addLemmas(synset.getWords(), syns);
    OUTER: for (Iterator iter = l.iterator(); iter.hasNext();)
    {
      String syn = (SYNSET_DELIM + (String) iter.next() + SYNSET_DELIM); // yuck
      for (Iterator j = syns.iterator(); j.hasNext();)
      {
        String lemma = (SYNSET_DELIM + j.next() + SYNSET_DELIM);
        if (syn.indexOf(lemma) > -1)
        {
          // System.err.println("removing: "+syn);
          iter.remove();
          continue OUTER;
        }
      }
    }

    return l;
  }

  // -------------------------- AUX METHODS ----------------------------

  public boolean isAdjective(String word)
  {
    return (getPosStr(word).indexOf(Character.toString('a')) > -1);
  }

  public boolean isAdverb(String word)
  {
    return (getPosStr(word).indexOf(Character.toString('r')) > -1);
  }

  public boolean isVerb(String word)
  {
    return (getPosStr(word).indexOf(Character.toString('v')) > -1);
  }

  public boolean isNoun(String word)
  {
    return (getPosStr(word).indexOf(Character.toString('n')) > -1);
  }

  /**
   * Returns an array of all stems, or null if not found
   * 
   * @param query
   * @param pos
   */
  public String[] getStems(String query, CharSequence pos)
  {
    List tmp = getStemList(query, pos);
    return toStrArr(tmp);
  }

  /**
   * Returns stem for <code>pos</code> with <code>pos</code>, or null if not
   * found.
   * 
   * public String getStem(String word, CharSequence pos) { IndexWord iw = null;
   * try { iw = dictionary.getMorphologicalProcessor()
   * .lookupBaseForm(convertPos(pos), word); } catch (JWNLException e) { throw
   * new CTextError(this, e); } return (iw != null) ? iw.getLemma() : null; }
   */

  /**
   * Returns true if 'word' exists with 'pos' and is equal (via String.equals())
   * to any of its stem forms, else false;
   * 
   * @param query
   * @param pos
   */
  public boolean isStem(String word, CharSequence pos)
  {
    String[] stems = getStems(word, pos);
    if (stems == null)
      return false;
    for (int i = 0; i < stems.length; i++)
      if (word.equals(stems[i]))
        return true;
    return false;
  }

  private List getStemList(String query, CharSequence pos)
  {
    try
    {
      return jwnlDict.getMorphologicalProcessor().lookupAllBaseForms(convertPos(pos), query);
    }
    catch (JWNLException e)
    {
      throw new WordnetError(this, e);
    }
  }

  /**
   * Checks the existence of a 'word' in the ontology
   * 
   * @param word
   */
  public boolean exists(String word)
  {
    if (word.indexOf(' ') > -1) return false;
      //throw new WordnetError(this, "expecting word, got phrase: " + word);

    IndexWord[] iw = null;
    try
    {
      if (jwnlDict == null)
      {
        System.err.println("NULL DICT");
        System.exit(1);
      }
      IndexWordSet iws = jwnlDict.lookupAllIndexWords(word);

      if (iws == null || iws.size() < 1)
        return false;

      iw = iws.getIndexWordArray();
    }
    catch (JWNLException e)
    {
      System.err.println("[WARN] " + e.getMessage()); // throw?
    }
    return (iw != null && iw.length > 0);
  }

  /**
   * Check each word in 'words' and removes those that don't exist in the
   * ontology.
   * <p>
   * Note: destructive operation
   * 
   * @invisible
   * 
   * @param words
   */
  public void removeNonExistent(Collection words)
  {
    for (Iterator i = words.iterator(); i.hasNext();)
    {
      String word = (String) i.next();
      if (!exists(word))
        i.remove();
    }
  }

  // -------------------------- PRIVATE METHODS ------------------------------

  private IndexWord lookupIndexWord(String pos, String word)
  {
    return this.lookupIndexWord(convertPos(pos), word);
  }

  private POS convertPos(String pos)
  {
    POS wnPos = WordnetPos.getPos(pos);
    if (wnPos == null)
      throw new WordnetError(this, "Invalid Pos-String: '" + pos + "'");
    return wnPos;
  }

  
  /** @invisible */
  public IndexWord lookupIndexWord(POS pos, CharSequence cs)
  {
    // System.err.println("RiWordnet.lookupIndexWord("+cs+")");
    if (cs == null)
      return null;
    String word = cs.toString().replace('-', '_');
    IndexWord iword = null;
    try
    {
      iword = jwnlDict.lookupIndexWord(pos, word);
    }
    catch (JWNLException e)
    {
      // JWNL bug returns null here, ignore...
    }
    return iword;
  }

  private String toLemmaString(Word[] words, String delim, boolean addStartAndEndDelims)
  {
    if (words == null || words.length == 0)
      return null;
    List dest = new ArrayList();
    addLemmas(words, dest);
    String result = WordnetUtil.join(dest, delim);
    if (addStartAndEndDelims)
      result = delim + result + delim;
    return result;
  }

  private void addLemmas(Word[] words, Collection dest)
  {
    if (words == null || words.length == 0)
      return;
    for (int k = 0; k < words.length; k++)
      addLemma(words[k], dest);
  }

  private void addLemma(Word word, Collection dest)
  {
    this.addLemma(word.getLemma(), dest);
  }

  private void addLemma(String lemma, Collection dest)
  {
    if (ignoreCompoundWords && isCompound(lemma))
      return;
    if (ignoreUpperCaseWords && WordnetUtil.startsWithUppercase(lemma))
      return;
    lemma = cleanLemma(lemma);
    if (!dest.contains(lemma)) // no dups
      dest.add(lemma);
  }

  private void getLemmaSet(PointerTargetNodeList source, Collection dest)
  {
    if (source == null)
      return;

    for (Iterator i = source.iterator(); i.hasNext();)
    {
      PointerTargetNode targetNode = (PointerTargetNode) i.next();
      if (!targetNode.isLexical())
      {
        Synset syn = targetNode.getSynset();
        if (syn != null)
          addLemmas(syn.getWords(), dest);
      }
      else
      {
        addLemma(targetNode.getWord(), dest);
      }
    }
  }

  private List getLemmaStrings(PointerTargetNodeList source, String delim, boolean addStartAndEndDelims)
  {
    List l = new ArrayList();
    for (Iterator i = source.iterator(); i.hasNext();)
    {
      PointerTargetNode targetNode = (PointerTargetNode) i.next();
      if (!targetNode.isLexical())
      {
        Synset syn = targetNode.getSynset();
        if (syn != null)
        {
          String s = toLemmaString(syn.getWords(), delim, addStartAndEndDelims);
          l.add(s);
        }
      }
      else
      { // Never called???
        List dest = new ArrayList();
        addLemma(targetNode.getWord(), dest);
        System.err.println("ILLEGAL CALL TO TARGET: " + targetNode.getWord());
      }
    }
    return l == null || l.size() < 1 ? null : l;
  }

  private static String trimFirstandLastChars(String s)
  {
    if (s.length() < 2)
      throw new IllegalArgumentException("Invalid length String: '" + s + "'");
    return s.substring(1, s.length() - 1);
  }

  private String cleanLemma(String lemma)
  {
    // / TODO!!!
    if (lemma.endsWith(")"))
      lemma = lemma.substring(0, lemma.length() - 3);
    lemma = WordnetUtil.replace(lemma, '_', '-');
    return lemma;
  }

  /*
   * EXPERIMENT!!!!!!!!!!!!!!!!! Adds the hypernyms for all senses of IndexWord
   * to List @param word - IndexWord on which to search @param l - List to which
   * we add hypernyms
   * 
   * private List getDerivedSynset(IndexWord word) throws JWNLException { if
   * (word == null) return null;
   * 
   * Synset[] synsets = word.getSenses(); if (synsets == null || synsets.length
   * <= 0) return null;
   * 
   * List l = new ArrayList(); PointerUtils pu = PointerUtils.getInstance(); for
   * (int i = 0; i < synsets.length; i++) { PointerTargetNodeList nodeList =
   * pu.getDerived(synsets[i]); getLemmaSet(nodeList, l); // redo with a List,
   * checking for dups }
   * 
   * return l; }
   */

  /**
   * Returns an array of all parts-of-speech ordered according to their polysemy
   * count, returning the pos with the most different senses in the first
   * position, etc.
   * 
   * @return String[], one element for each part of speech ("a" = adjective, "n"
   *         = noun, "r" = adverb, "v" = verb), or null if not found.
   */
  public String[] getPos(String word)
  {
    IndexWord[] all = getIndexWords(word);
    if (all == null)
      return null;
    String[] pos = new String[all.length];
    for (int i = 0; i < all.length; i++)
      pos[i] = all[i].getPOS().getKey();
    return pos;
  }

  /**
   * @return String from ("a" = adjective, "n" = noun, "r" = adverb, "v" =
   *         verb), or null if not found.
   */
  public String getPos(int id)
  {
    Synset synsets = getSynsetAtId(id);
    if (synsets == null)
      return null;
    return synsets.getPOS().getKey();
  }

  /**
   * @invisible
   * Returns a String of characters, 1 for each part of speech: ("a" =
   * adjective, "n" = noun, "r" = adverb, "v" = verb) or an empty String if not
   * found.
   * <p>
   */
  public String getPosStr(String word)
  {
    String pos = QQ;
    IndexWord[] all = getIndexWords(word);
    if (all == null)
      return pos;
    for (int i = 0; i < all.length; i++)
      pos += all[i].getPOS().getKey();
    return pos;
  }

  /**
   * Finds the most-common part-of-speech for the word, according to its
   * polysemy count, returning the pos for the version of the word with the most
   * different senses.
   * 
   * @return single-char String for the most common part of speech ("a" =
   *         adjective, "n" = noun, "r" = adverb, "v" = verb), or null if not
   *         found.
   */
  public String getBestPos(String word)
  {
    IndexWord[] all = getIndexWords(word);
    if (all == null || all.length < 1)
      return null;
    POS p = all[0].getPOS();
    if (p == POS.NOUN)
      return NOUN;
    if (p == POS.VERB)
      return VERB;
    if (p == POS.ADVERB)
      return ADV;
    if (p == POS.ADJECTIVE)
      return ADJ;
    throw new WordnetError("no pos for word: " + word);
  }

  private IndexWord[] getIndexWords(CharSequence word)
  {
    // IndexWord[] all = null;
    List list = new ArrayList();
    for (Iterator itr = POS.getAllPOS().iterator(); itr.hasNext();)
    {
      IndexWord current = lookupIndexWord((POS) itr.next(), word.toString());
      if (current != null)
      {
        int polysemy = current.getSenseCount();
        list.add(new ComparableIndexWord(current, polysemy));
      }
    }
    int idx = 0;
    Collections.sort(list);
    IndexWord[] iws = new IndexWord[list.size()];
    for (Iterator i = list.iterator(); i.hasNext();)
    {
      ComparableIndexWord ciw = (ComparableIndexWord) i.next();
      iws[idx++] = ciw.iw;
    }
    return iws;
  }

  class ComparableIndexWord implements Comparable
  {
    IndexWord iw;
    int polysemy = -1;

    public ComparableIndexWord(IndexWord current, int polysemy)
    {
      this.iw = current;
      this.polysemy = polysemy;
    }

    public String toString()
    {
      return iw.toString() + "polysemy=" + polysemy;
    }

    public int compareTo(Object arg0)
    {
      ComparableIndexWord ciw = (ComparableIndexWord) arg0;
      if (ciw.polysemy == polysemy)
        return 0;
      return (ciw.polysemy > polysemy) ? 1 : -1;
    }
  }

  /**
   * @param confFile
   *          wordnet xml-based configuration file full path.
   * @throws FileNotFoundException
   */
  private void initWordnet(String confFile) throws JWNLException
  {
    if (DBUG)
      System.err.println("[INFO] Initializing WordNet: conf='" + confFile + "'");
    
    try
    {
      InputStream is = WordnetUtil.getResourceStream(WordnetUtil.class, confFile);
      
      if (DBUG)
        System.err.println("[INFO] Initializing WordNet: stream='" + is + "'");
      
      JWNL.initialize(is);
    }
    catch (RuntimeException e)
    {
      System.err.println(e.getMessage());
      throw e;
    }
  }

  /**
   * @param query
   * @invisible
   * @param l
   */
  public static String[] toStrArr(List l)
  {
    if (l == null || l.size() == 0)
      return null;
    return (String[]) l.toArray(new String[l.size()]);
  }

  /**
   * @invisible
   * @param l
   */
  /* public */String[] ptnlToStrings(String query, PointerTargetNodeList ptnl)
  {
    if (ptnl == null || ptnl.size() == 0)
      return null;
    List l = new LinkedList();
    getLemmaSet(ptnl, l);
    // ??? (remove this? what if we dont know the original?)
    if (query != null)
      l.remove(query); // remove original
    return toStrArr(l);
  }

  // RANDOM METHODS ==============================================

  /**
   * Returns a random example from a random word w' <code>pos</code>
   * 
   * @return random example
   */
  public String getRandomExample(CharSequence pos)
  {
    return getRandomExamples(pos, 1)[0];
  }

  /**
   * Returns <code>numExamples</code> random examples from random words w'
   * <code>pos</code>
   * 
   * @return random examples
   */
  public String[] getRandomExamples(CharSequence pos, int numExamples)
  {
    int idx = 0;
    String[] result = new String[numExamples];
    WHILE: while (true)
    {
      try
      {
        IndexWord iw = null;
        while (iw == null || !ignoreCompoundWords && WordnetUtil.contains(iw.getLemma(), " "))
          iw = jwnlDict.getRandomIndexWord(convertPos(pos));

        Synset syn = iw.getSenses()[0];
        List l = getExamples(syn);
        if (l == null || l.size() < 1)
          continue;
        for (Iterator i = l.iterator(); i.hasNext();)
        {
          String example = (String) i.next();
          if (example != null)
          {
            result[idx++] = example;
            break;
          }
        }
        if (idx == result.length)
          break WHILE;
      }
      catch (JWNLException e)
      {
        System.err.println("WARN] Unexpected Exception: " + e.getMessage());
      }
    }
    return result;
  }

  /**
   * Returns <code>count</code> random words w' <code>pos</code>
   * 
   * @return String[] of random words
   */
  public String[] getRandomWords(CharSequence pos, int count)
  {
    String[] result = new String[count];
    for (int i = 0; i < result.length; i++)
      result[i] = getRandomWord(pos, true, maxCharsPerWord);
    return result;
  }

  /**
   * Returns a random stem with <code>pos</code> and a max length of
   * <code>this.maxCharsPerWord</code>.
   * 
   * @return random word
   */
  public String getRandomWord(CharSequence pos)
  {
    return this.getRandomWord(pos, true, maxCharsPerWord);
  }

  /**
   * Returns a random word with <code>pos</code> and a maximum of
   * <code>maxChars</code>.
   * 
   * @return a random word or null if none is found
   */
  public String getRandomWord(CharSequence pos, boolean stemsOnly, int maxChars)
  {
    IndexWord iw = null;
    POS wnPos = convertPos(pos);
    while (true)
    {
      try
      {
        FileBackedDictionary d;
        iw = jwnlDict.getRandomIndexWord(wnPos);
      }
      catch (JWNLRuntimeException e)
      {
        // System.err.println("[WARN] "+e.getMessage());
        // if (e != null &&
        // e.getMessage().trim().startsWith("Illegal tokenizer state"))
        // System.out.println("\n[WARN] "+e.getMessage());
        continue;
      }
      catch (JWNLException e)
      {
        throw new WordnetError(e);
      }
      String word = iw.getLemma();
      if (ignoreCompoundWords && isCompound(word))
        continue;
      if (word.length() > maxChars)
        continue;
      if (!stemsOnly || isStem(word, pos))
        return iw.getLemma();
    }
  }

  /**
   * Returns true if the word is considered compound (contains either a space,
   * dash,or underscore), else false
   */
  public static boolean isCompound(String word)
  {
    return word.indexOf(' ') > 0 || word.indexOf('-') > 0 || word.indexOf('_') > 0;
  }

  /** @invisible */
  public Dictionary getDictionary()
  {
    return jwnlDict;
  }

  /**
   * Prints the full hyponym tree to System.out (primarily for debugging).
   * 
   * @param senseId
   * @invisible
   */
  public void printHyponymTree(int senseId)
  {
    try
    {
      dumpHyponymTree(System.out, getSynsetAtId(senseId));
    }
    catch (JWNLException e)
    {
      throw new WordnetError(e);
    }
  }

  void dumpHyponymTree(String word, String pos) throws JWNLException
  {
    dumpHyponymTree(System.err, word, pos);
  }

  public void dumpHyponymTree(PrintStream ps, String word, String pos) throws JWNLException
  {
    IndexWord iw = lookupIndexWord(pos, word);
    Synset syn = iw.getSense(1);
    this.dumpHyponymTree(ps, syn);
  }

  void dumpHyponymTree(PrintStream ps, Synset syn) throws JWNLException
  {
    PointerTargetTree hyponyms = null;
    try
    {
      hyponyms = PointerUtils.getInstance().getHyponymTree(syn);
    }
    catch (NullPointerException e)
    {
      // ignore jwnl bug
    }
    if (hyponyms == null)
      return;
    Set syns = new HashSet();
    addLemmas(syn.getWords(), syns);
    ps.println("\nHyponyms of synset" + syns + ":\n-------------------------------------------");

    hyponyms.print(ps);
    ps.println();
  }

  /**
   * Prints the full hypernym tree to System.out (primarily for debugging).
   * 
   * @param senseId
   */
  public void printHypernymTree(int senseId)
  {
    try
    {
      Synset s = getSynsetAtId(senseId);
      // System.out.println("Syn: "+s);
      dumpHypernymTree(System.out, s);
    }
    catch (JWNLException e)
    {
      throw new WordnetError(e);
    }
  }

  void printHypernymTree(String word, String pos) throws JWNLException
  {
    dumpHypernymTree(System.err, word, pos);
  }

  /** @invisible */
  public void dumpHypernymTree(PrintStream ps, String word, String pos) throws JWNLException
  {
    // Get all the hyponyms (children) of the first sense of <var>word</var>
    IndexWord iw = lookupIndexWord(pos, word);
    Synset syn = iw.getSense(1);
    dumpHypernymTree(ps, syn);
  }

  void dumpHypernymTree(PrintStream ps, Synset syn) throws JWNLException
  {
    PointerTargetTree hypernyms = null;
    try
    {
      hypernyms = PointerUtils.getInstance().getHypernymTree(syn);
    }
    catch (StackOverflowError e)
    {
      PointerUtils.getInstance().setOverflowError(true);
      hypernyms = PointerUtils.getInstance().getHypernymTree(syn);
    }
    catch (NullPointerException e)
    {
      // ignore jwnl bug
    }
    if (hypernyms == null)
      return;
    Set syns = new HashSet();
    addLemmas(syn.getWords(), syns);
    ps.println("\nHypernyms of synset" + syns + ":\n-------------------------------------------");
    hypernyms.print(ps);
    ps.println();
  }

  /**
   * Returns the min distance between any two senses for the 2 words in the
   * wordnet tree (result normalized to 0-1) with specified pos, or 1.0 if
   * either is not found.
   * <P>
   * The algorithm procedes as follows:
   * <ol>
   * <li>locate node <code>cp</code>, the common parent of the two lemmas, if
   * one exists, by checking each sense of each lemma; if one is not found,
   * return 1.0
   * <li>calculate <code>minDistToCommonParent</code>, the shortest path from
   * either lemma to cp
   * <li>calculate <code>distFromCommonParentToRoot</code>, the length of the
   * path from cp to the root of ontology
   * <li>calculate and return the <code>normalizedDistToCommonParent</code> as:
   * <br>
   * <code>(minDistToCommonParent / (distFromCommonParentToRoot + minDistToCommonParent))</code>
   * <ol>
   */
  public float getDistance(String lemma1, String lemma2, String pos)
  {
    if (lemma1 == null || lemma1.contains(" "))
      return -1;
    if (lemma2 == null || lemma2.contains(" "))
      return -1;
    
    IndexWordSet WORDSET1, WORDSET2;
    IndexWord WORD1, WORD2;

    float d = 1.0f;
    float smallestD = 1.0f;
    POS p = convertPos(pos);

    if (lemma1.equals(lemma2))
    {
      smallestD = 0.0f;
    }
    else
    {
      try
      {
        // get complete definition for each word (all POS, all senses)
        WORDSET1 = this.jwnlDict.lookupAllIndexWords(lemma1);
        WORDSET2 = this.jwnlDict.lookupAllIndexWords(lemma2);

        // for each POS listed in wordTypes...
        // for (int i = 0; i < wordTypes.length; i++)
        // p = wordTypes[i];

        if (WORDSET1.isValidPOS(p) && WORDSET2.isValidPOS(p))
        {
          WORD1 = WORDSET1.getIndexWord(p);
          WORD2 = WORDSET2.getIndexWord(p);

          // get distance between words based on this POS
          try
          {
            d = getWordDistance(WORD1, WORD2);
          }
          catch (NullPointerException e)
          {
            // ignore jwnl bug
          }
          if (d < smallestD)
          {
            smallestD = d;
          }
        }

      }
      catch (JWNLException e)
      {
        System.err.println("[WARN] Error obtaining distance: " + e);
        return 1.0f;
      }
    }

    return smallestD;
  }

  // get distance between words that are the same POS
  private float getWordDistance(IndexWord start, IndexWord end) 
    throws JWNLException, NullPointerException // on                                                                                                        // bug
  {
    RelationshipList relList;
    AsymmetricRelationship rel;
    int cpIndex, relLength, depth, depthRootCp, depthCpLeaf;
    float distance, newDistance;
    PointerTargetNode cpNode;
    Synset cpSynset;
    List cpHypListList;
    distance = 1.0f;

    int senseCount1 = start.getSenseCount();
    int senseCount2 = end.getSenseCount();

    // for each pairing of word senses...
    for (int i = 1; i <= senseCount1; i++)
    {
      for (int j = 1; j <= senseCount2; j++)
      {
        // get list of relationships between words (usually only one)
        try
        {
          //System.out.println(i+","+j+": "+start.getSense(i)+","+end.getSense(j));
          relList = RelationshipFinder.getInstance().findRelationships
            (start.getSense(i), end.getSense(j), PointerType.HYPERNYM);
        }
        catch (Exception e)
        {
          //System.out.println("RiWordnet.getWordDistance().exception="+e.getMessage());
          continue;
        }

        // calculate distance for each one
        for (Iterator relListItr = relList.iterator(); relListItr.hasNext();)
        {
          rel = (AsymmetricRelationship) relListItr.next();
          cpIndex = rel.getCommonParentIndex();
          relLength = rel.getDepth();

          // distance between items going through the CP
          // (depth of furthest word from CP)
          depthCpLeaf = Math.max(relLength - cpIndex, cpIndex);

          // get the CPI node
          cpNode = (PointerTargetNode) rel.getNodeList().get(cpIndex);
          // get the synset of the CPI node
          cpSynset = cpNode.getSynset();
          // get all the hypernyms of the CPI synset
          // returns a list of hypernym chains.
          // probably always one chain, but better to be safe...
          cpHypListList = (PointerUtils.getInstance().getHypernymTree(cpSynset)).toList();

          // System.out.println("PARENT: "+cpSynset);

          // get shortest depth from root to CP
          depthRootCp = -1;
          for (Iterator cpHypListListItr = cpHypListList.iterator(); cpHypListListItr.hasNext();)
          {
            depth = ((List) cpHypListListItr.next()).size();
            if (depthRootCp == -1)
            {
              depthRootCp = depth;
            }
            else
            {
              if (depth < depthRootCp)
              {

                depthRootCp = depth;
              }
            }
          }

          // normalize the distance
          newDistance = (float) depthCpLeaf / (depthRootCp + depthCpLeaf);
          if (newDistance < distance)
          {
            distance = newDistance;
          }
        }
      }
    }
    return distance;
  }

  /**
   * Returns array of whole-to-part relationships for 1st sense of word/pos, or
   * null if not found<br>
   * X is a meronym of Y if Y has X as a part.<br>
   * X is a holonym of Y if X has Y as a part. That is, if Y is a meronym of X. <br>
   * Holds between: Nouns and nouns<br>
   * Returns part,member, and substance meronyms<br>
   * Example: arm -> [wrist, carpus, wrist-joint, radiocarpal-joint...]
   * 
   * @param query
   * @param pos
   */
  public String[] getMeronyms(String query, String pos)
  {
    try
    {
      Synset synset = getSynsetAtIndex(query, pos, 1);
      if (synset == null)
        return null;
      PointerTargetNodeList ptnl = null;
      try
      {
        ptnl = PointerUtils.getInstance().getMeronyms(synset);
      }
      catch (NullPointerException e)
      {
        // ignore jwnl bug
        // throw new WordnetError(e);
      }
      return ptnlToStrings(query, ptnl);
    }

    catch (JWNLException e)
    {
      throw new WordnetError(this, e);
    }
  }

  /**
   * Returns array of whole-to-part relationships for id, or null if not found<br>
   * X is a meronym of Y if Y has X as a part.<br>
   * X is a holonym of Y if X has Y as a part. That is, if Y is a meronym of X. <br>
   * Holds between: Nouns and nouns<br>
   * Returns part,member, and substance meronyms<br>
   * Example: arm -> [wrist, carpus, wrist-joint, radiocarpal-joint...]
   */
  public String[] getMeronyms(int id)
  {
    try
    {
      Synset synset = getSynsetAtId(id);
      if (synset == null)
        return null;
      PointerTargetNodeList ptnl = null;
      try
      {
        ptnl = PointerUtils.getInstance().getMeronyms(synset);
      }
      catch (NullPointerException e)
      {
        // ignore jwnl bug
        // throw new WordnetError(e);
      }
      return ptnlToStrings(null, ptnl);
    }

    catch (JWNLException e)
    {
      throw new WordnetError(this, e);
    }
  }

  /**
   * Returns array of whole-to-part relationships for all senses of word/pos, or
   * null if not found<br>
   * X is a meronym of Y if Y has X as a part.<br>
   * X is a holonym of Y if X has Y as a part. That is, if Y is a meronym of X. <br>
   * Holds between: Nouns and nouns<br>
   * Returns part,member, and substance meronyms<br>
   * Example: arm -> [wrist, carpus, wrist-joint, radiocarpal-joint...]
   * 
   * @param query
   * @param pos
   */
  public String[] getAllMeronyms(String query, String pos)
  {
    try
    {
      Synset[] synsets = allSynsets(query, pos);
      if (synsets == null)
        return null;
      List l = new LinkedList();
      for (int i = 0; i < synsets.length; i++)
      {
        if (synsets[i] == null)
          continue;
        PointerTargetNodeList ptnl = null;
        try
        {
          ptnl = PointerUtils.getInstance().getMeronyms(synsets[i]);
        }
        catch (NullPointerException e)
        {
          // ignore jwnl bug
          // throw new WordnetError(e);
        }
        getLemmaSet(ptnl, l);
      }
      l.remove(query); // skip original
      return toStrArr(l);
    }
    catch (JWNLException e)
    {
      throw new WordnetError(this, e);
    }
  }

  /**
   * Returns part-to-whole relationships for 1st sense of word/pos, or none if
   * not found<br>
   * X is a meronym of Y if Y has X as a part.<br>
   * X is a holonym of Y if X has Y as a part. That is, if Y is a meronym of X. <br>
   * Holds between: nouns and nouns<br>
   * Returns part, member, and substance holonyms<br>
   * Example: arm -> [body, physical-structure, man, human...]
   * 
   * @param query
   * @param pos
   */
  public String[] getHolonyms(String query, String pos)
  {
    PointerTargetNodeList ptnl = null;
    try
    {
      Synset synset = getSynsetAtIndex(query, pos, 1);
      if (synset == null)
        return null;
      ptnl = PointerUtils.getInstance().getHolonyms(synset);
    }
    catch (NullPointerException e)
    {
      // ignore jwnl bug
    }
    catch (JWNLException e)
    {
      throw new WordnetError(this, e);
    }
    // returns part,member, and substance holonyms
    return ptnlToStrings(query, ptnl);
  }

  /**
   * Returns part-to-whole relationships for 1st sense of word/pos, or none if
   * not found<br>
   * X is a meronym of Y if Y has X as a part.<br>
   * X is a holonym of Y if X has Y as a part. That is, if Y is a meronym of X. <br>
   * Holds between: nouns and nouns<br>
   * Returns part, member, and substance holonyms<br>
   * Example: arm -> [body, physical-structure, man, human...]
   * 
   * @param query
   * @param pos
   */
  public String[] getHolonyms(int id)
  {
    PointerTargetNodeList ptnl = null;
    try
    {
      Synset synset = getSynsetAtId(id);
      if (synset == null)
        return null;
      ptnl = PointerUtils.getInstance().getHolonyms(synset);
    }
    catch (NullPointerException e)
    {
      // ignore jwnl bug
    }
    catch (JWNLException e)
    {
      throw new WordnetError(this, e);
    }
    return ptnlToStrings(null, ptnl);
  }

  /**
   * Returns part-to-whole relationships for all sense of word/pos, or none if
   * not found<br>
   * X is a meronym of Y if Y has X as a part.<br>
   * X is a holonym of Y if X has Y as a part. That is, if Y is a meronym of X. <br>
   * Holds between: nouns and nouns<br>
   * Returns part, member, and substance holonyms<br>
   * Example: arm -> [body, physical-structure, man, human...]
   * 
   * @param query
   * @param pos
   */
  public String[] getAllHolonyms(String query, String pos)
  {
    try
    {
      Synset[] synsets = allSynsets(query, pos);
      if (synsets == null)
        return null;
      List l = new LinkedList();
      for (int i = 0; i < synsets.length; i++)
      {
        if (synsets[i] == null)
          continue;
        PointerTargetNodeList ptnl = null;
        try
        {
          ptnl = PointerUtils.getInstance().getHolonyms(synsets[i]);
        }
        catch (NullPointerException e)
        {
          // jwnl bug
        }
        getLemmaSet(ptnl, l);
      }
      l.remove(query); // skip original
      return toStrArr(l);
    }
    catch (JWNLException e)
    {
      throw new WordnetError(this, e);
    }
  }

  /**
   * Returns coordinate terms for 1st sense of word/pos, or null if not found<br>
   * X is a coordinate term of Y if there exists a term Z which is the hypernym
   * of both X and Y.<br>
   * Examples:
   * <ul>
   * <li>blackbird and robin are coordinate terms (since they are both a kind of
   * thrush)
   * <li>gun and bow are coordinate terms (since they are both weapons)
   * <li>fork and spoon are coordinate terms (since they are both cutlery, or
   * eating utensils)
   * <li>hat and helmet are coordinate terms (since they are both a kind of
   * headgear or headdress)
   * </ul>
   * Example: arm -> [hind-limb, forelimb, flipper, leg, crus, thigh, arm...]<br>
   * Holds btwn nouns/nouns and verbs/verbs
   * 
   * @param query
   * @param pos
   */
  public String[] getCoordinates(String query, String pos)
  {
    String[] result = null;
    try
    {
      Synset synset = getSynsetAtIndex(query, pos, 1);
      if (synset == null)
        return null;
      PointerTargetNodeList ptnl = PointerUtils.getInstance().getCoordinateTerms(synset);
      if (ptnl != null)
        result = ptnlToStrings(query, ptnl);
    }
    catch (NullPointerException e)
    {
      // ignore jwnl bug here
    }
    catch (JWNLException e)
    {
      throw new WordnetError(this, e);
    }
    return result;
  }

  /**
   * Returns String[] of Coordinates for the specified id, or null if not found<br>
   */
  public String[] getCoordinates(int id)
  {
    String[] result = null;
    try
    {
      Synset synset = getSynsetAtId(id);
      if (synset == null)
        return null;
      PointerTargetNodeList ptnl = PointerUtils.getInstance().getCoordinateTerms(synset);
      if (ptnl != null)
        result = ptnlToStrings(null, ptnl);
    }
    catch (NullPointerException e)
    {
      // ignore jwnl bug here
    }
    catch (JWNLException e)
    {
      throw new WordnetError(this, e);
    }
    return result;
  }

  /**
   * Returns coordinate terms for all sense of word/pos, or null if not found<br>
   * X is a coordinate term of Y if there exists a term Z which is the hypernym
   * of both X and Y.<br>
   * Examples:
   * <ul>
   * <li>blackbird and robin are coordinate terms (since they are both a kind of
   * thrush)
   * <li>gun and bow are coordinate terms (since they are both weapons)
   * <li>fork and spoon are coordinate terms (since they are both cutlery, or
   * eating utensils)
   * <li>hat and helmet are coordinate terms (since they are both a kind of
   * headgear or headdress)
   * </ul>
   * Example: arm -> [hind-limb, forelimb, flipper, leg, crus, thigh, arm...]<br>
   * Holds btwn nouns/nouns and verbs/verbs
   * 
   * @param query
   * @param pos
   */
  public String[] getAllCoordinates(String query, String pos)
  {
    try
    {
      Synset[] synsets = allSynsets(query, pos);
      if (synsets == null)
        return null;
      List l = new LinkedList();
      for (int i = 0; i < synsets.length; i++)
      {
        if (synsets[i] == null)
          continue;
        PointerTargetNodeList ptnl = null;
        try
        {
          ptnl = PointerUtils.getInstance().getCoordinateTerms(synsets[i]);
        }
        catch (NullPointerException e)
        {
          // ignore jwnl bug
        }
        getLemmaSet(ptnl, l);
      }
      l.remove(query); // skip original
      return toStrArr(l);
    }
    catch (JWNLException e)
    {
      throw new WordnetError(this, e);
    }
  }

  /**
   * Returns verb group for 1st sense of verb or null if not found<br>
   * Example: live -> [dwell, inhabit]<br>
   * Holds for verbs
   * 
   * @param query
   * @param pos
   */
  public String[] getVerbGroup(String query, String pos)
  {
    PointerTargetNodeList ptnl = null;
    try
    {
      Synset synset = getSynsetAtIndex(query, pos, 1);
      if (synset == null)
        return null;
      try
      {
        ptnl = PointerUtils.getInstance().getVerbGroup(synset);
      }
      catch (NullPointerException e)
      {
        // ignore jwnl bug
      }
      if (ptnl == null)
        return null;

    }
    catch (JWNLException e)
    {
      throw new WordnetError(this, e);
    }
    return ptnlToStrings(query, ptnl);
  }

  /**
   * Returns verb group for id, or null if not found<br>
   * Example: live -> [dwell, inhabit]<br>
   * Holds for verbs
   */
  public String[] getVerbGroup(int id)
  {
    PointerTargetNodeList ptnl = null;
    try
    {
      Synset synset = getSynsetAtId(id);
      if (synset == null)
        return null;
      try
      {
        ptnl = PointerUtils.getInstance().getVerbGroup(synset);
      }
      catch (NullPointerException e)
      {
        // ignore jwnl bug
      }
      if (ptnl == null)
        return null;

    }
    catch (JWNLException e)
    {
      throw new WordnetError(this, e);
    }
    return ptnlToStrings(null, ptnl);
  }

  /**
   * Returns verb group for all senses of verb or null if not found<br>
   * Example: live -> [dwell, inhabit]<br>
   * Holds for verbs
   * 
   * @param query
   * @param pos
   */
  public String[] getAllVerbGroups(String query, String pos)
  {
    try
    {
      Synset[] synsets = allSynsets(query, pos);
      if (synsets == null)
        return null;
      List l = new LinkedList();
      for (int i = 0; i < synsets.length; i++)
      {
        if (synsets[i] == null)
          continue;
        PointerTargetNodeList ptnl = null;
        try
        {
          ptnl = PointerUtils.getInstance().getVerbGroup(synsets[i]);
        }
        catch (NullPointerException e)
        {
          // ignore jwnl bug
        }
        getLemmaSet(ptnl, l);
      }
      l.remove(query); // skip original
      return toStrArr(l);
    }
    catch (JWNLException e)
    {
      throw new WordnetError(this, e);
    }
  }

  /**
   * Returns derived terms for 1st sense of word/pos or null if not found<br>
   * Holds for adverbs <br>
   * Example: happily -> [jubilant, blithe, gay, mirthful, merry, happy]
   * 
   * @param query
   * @param pos
   */
  public String[] getDerivedTerms(String query, String pos)
  {
    return getPointerTargetsAtIndex(query, pos, PointerType.DERIVED, 1);
  }

  /**
   * Returns derived terms for the id, or null if not found<br>
   * Holds for adverbs <br>
   * Example: happily -> [jubilant, blithe, gay, mirthful, merry, happy]
   */
  public String[] getDerivedTerms(int id)
  {
    return getPointerTargetsAtId(id, PointerType.DERIVED);
  }

  /**
   * Returns derived terms forall senses of word/pos or null if not found<br>
   * Holds for adverbs <br>
   * Example: happily -> [jubilant, blithe, gay, mirthful, merry, happy]
   * 
   * @param query
   * @param pos
   */
  public String[] getAllDerivedTerms(String query, String pos)
  {
    return getAllPointerTargets(query, pos, PointerType.DERIVED);
  }

  /**
   * Returns also-see terms for 1st sense of word/pos or null if not found<br>
   * Holds for nouns (?) & adjectives<br>
   * Example: happy -> [cheerful, elated, euphoric, felicitous, joyful,
   * joyous...]
   * 
   * @param query
   * @param pos
   */
  public String[] getAlsoSees(String query, String pos)
  {
    return getPointerTargetsAtIndex(query, pos, PointerType.SEE_ALSO, 1);
  }

  /**
   * Returns also-see terms for seseId or null if not found<br>
   * Holds for nouns (?) & adjectives<br>
   * Example: happy -> [cheerful, elated, euphoric, felicitous, joyful,
   * joyous...]
   * 
   * @param senseId
   */
  public String[] getAlsoSees(int senseId)
  {
    return getPointerTargetsAtId(senseId, PointerType.SEE_ALSO);
  }

  /**
   * Returns also-see terms for all senses ofword/pos or null if not found<br>
   * Holds for nouns (?) & adjectives<br>
   * Example: happy -> [cheerful, elated, euphoric, felicitous, joyful,
   * joyous...]
   * 
   * @param query
   * @param pos
   */
  public String[] getAllAlsoSees(String query, String pos)
  {
    return getAllPointerTargets(query, pos, PointerType.SEE_ALSO);
  }

  /**
   * Returns attribute terms for word/pos or null if not found<br>
   * Holds for nouns & adjectives<br>
   * Example: happiness(n) -> [happy, unhappy]<br>
   * happy(a) -> [happiness, felicity]<br>
   * 
   * @param query
   * @param pos
   *          // seems to be same as Nominalizations??? public String[]
   *          getAttributeTerms(String query, String pos) { return
   *          getPointerTargets(query, pos, PointerType.ATTRIBUTE); }
   */

  /**
   * Returns nominalized terms for 1st sense of word/pos or null if not found<br>
   * Refers to the use of a verb or an adjective as a noun. Holds for nouns,
   * verbs & adjecstives(?)<br>
   * Example: happiness(n) -> [happy, unhappy]<br>
   * happy(a) -> [happiness, felicity]<br>
   * 
   * @param query
   * @param pos
   */
  public String[] getNominalizations(String query, String pos)
  {
    return getPointerTargetsAtIndex(query, pos, PointerType.NOMINALIZATION, 1);
  }

  /*
   * Returns attribute terms for word/pos or null if not found<br> Holds for
   * nouns & adjectives<br> Example: happiness(n) -> [happy, unhappy]<br>
   * happy(a) -> [happiness, felicity]<br>
   * 
   * @param query
   * 
   * @param pos // seems to be same as Nominalizations??? public String[]
   * getAttributeTerms(String query, String pos) { return
   * getPointerTargets(query, pos, PointerType.ATTRIBUTE); }
   */

  /**
   * Returns nominalized terms for id, or null if not found<br>
   * Refers to the use of a verb or an adjective as a noun. Holds for nouns,
   * verbs & adjecstives(?)<br>
   * Example: happiness(n) -> [happy, unhappy]<br>
   * happy(a) -> [happiness, felicity]<br>
   */
  public String[] getNominalizations(int id)
  {
    return getPointerTargetsAtId(id, PointerType.NOMINALIZATION);
  }

  /**
   * Returns nominalized terms for all sense of word/pos or null if not found<br>
   * Refers to the use of a verb or an adjective as a noun. Holds for nouns,
   * verbs & adjecstives(?)<br>
   * Example: happiness(n) -> [happy, unhappy]<br>
   * happy(a) -> [happiness, felicity]<br>
   * 
   * @param query
   * @param pos
   */
  public String[] getAllNominalizations(String query, String pos)
  {
    return getAllPointerTargets(query, pos, PointerType.NOMINALIZATION);
  }

  /**
   * Returns similar-to list for first sense of word/pos or null if not found<br>
   * Holds for adjectives<br>
   * Example:<br>
   * happy(a) -> [blessed, blissful, bright, golden, halcyon, prosperous...]<br>
   * 
   * @param query
   * @param pos
   */
  public String[] getSimilar(String query, String pos)
  {
    return getPointerTargetsAtIndex(query, pos, PointerType.SIMILAR_TO, 1);
  }

  /**
   * Returns similar-to list for id, or null if not found<br>
   * Holds for adjectives<br>
   * Example:<br>
   * happy(a) -> [blessed, blissful, bright, golden, halcyon, prosperous...]<br>
   */
  public String[] getSimilar(int id)
  {
    return getPointerTargetsAtId(id, PointerType.SIMILAR_TO);
  }

  /**
   * Returns similar-to list for all sense of word/pos or null if not found<br>
   * Holds for adjectives<br>
   * Example:<br>
   * happy(a) -> [blessed, blissful, bright, golden, halcyon, prosperous...]<br>
   * 
   * @param query
   * @param pos
   */
  public String[] getAllSimilar(String query, String pos)
  {
    return getAllPointerTargets(query, pos, PointerType.SIMILAR_TO);
  }

  // PRIVATES --------------------------------------------------------

  private POS convertPos(CharSequence pos)
  {
    if (pos == null)
      pos = "";
    return convertPos(pos.toString());
  }

  /**
   * Returns cause terms for word/pos or null if not found<br>
   * Holds for verbs<br>
   * Example:<br>
   * 
   * @param query
   * @param pos
   *          public String[] getCauseTerms(String query, String pos) { return
   *          getPointerTargets(query, pos, PointerType.SIMILAR_TO); }
   */

  /* Get all the pointer targets of <var>synset</var> of type <var>type</var>. */
  private PointerTargetNodeList getPointerTargets(Synset synset, PointerType type) throws JWNLException
  {
    if (synset == null)
      return null;

    // System.err.println("RiWordnet.getPointerTargets("+synset+", "+type+")");
    PointerTarget[] pta;
    try
    {
      pta = synset.getTargets(type);
      if (pta == null || pta.length == 0)
        return null;
    }
    catch (NullPointerException e) // a JWNL bug
    {
      // throw new CTextException(this,e);
      return null;
    }

    return new PointerTargetNodeList(pta);
  }

  public boolean isIgnoringCompoundWords()
  {
    return this.ignoreCompoundWords;
  }

  public void ignoreCompoundWords(boolean ignoreCompoundWords)
  {
    this.ignoreCompoundWords = ignoreCompoundWords;
  }

  public boolean isIgnoringUpperCaseWords()
  {
    return this.ignoreUpperCaseWords;
  }

  public void ignoreUpperCaseWords(boolean ignoreUpperCaseWords)
  {
    this.ignoreUpperCaseWords = ignoreUpperCaseWords;
  }

  private String[] getAllPointerTargets(String word, String pos, PointerType type)
  {
    Synset[] syns = allSynsets(word, pos);
    if (syns == null || syns.length < 1)
      return null;
    List result = new LinkedList();
    for (int i = 0; i < syns.length; i++)
    {
      try
      {
        PointerTargetNodeList ptnl = getPointerTargets(syns[i], type);
        String[] targets = ptnlToStrings(word, ptnl);
        if (targets == null)
          continue;
        for (int j = 0; j < targets.length; j++)
        {
          if (targets[j] != null)
            addLemma(targets[j], result);
        }
      }
      catch (JWNLException e)
      {
        throw new WordnetError(e);
      }
    }
    result.remove(word); // skip the original
    return toStrArr(result);
  }

  /*
   * Get a String[] from the pointer targets of <var>synset</var> of type
   * <var>type</var>.
   */
  private String[] getPointerTargetsAtIndex(String word, String pos, PointerType type, int index)
  {
    try
    {
      Synset synset = getSynsetAtIndex(word, pos, index);
      if (synset == null)
        return null;
      return ptnlToStrings(word, getPointerTargets(synset, type));
    }
    catch (JWNLException e)
    {
      throw new WordnetError(this, e);
    }
  }

  /*
   * Get a String[] from the pointer <var>id</var> of type <var>type</var>.
   */
  private String[] getPointerTargetsAtId(int id, PointerType type)
  {
    Synset synset = getSynsetAtId(id);
    if (synset == null)
      return null;
    try
    {
      return ptnlToStrings(null, getPointerTargets(synset, type));
    }
    catch (JWNLException e)
    {
      throw new WordnetError(e);
    }
  }
  
  public static void main(String[] args)
  {

    String result = "No WordNet in JS";
    if (RiTa.env() == RiTa.JAVA)
    {

      RiWordnet w = new RiWordnet("/WordNet-3.1");
      String test = "night";
      String[] s = w.getAntonyms(test, "n");
      result = test + " != " + s[0];
    }
    System.out.println(result);
  }
  
}