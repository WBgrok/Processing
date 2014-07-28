package rita;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import rita.support.*;

/**
 * A set of static properties and utility functions for the package
 */
public class RiTa implements Constants
{ 
  public final static String VERSION = "1.0.43";

  public static final int JAVA = 1, JS = 2, NODEJS = 3;

  public static boolean callbacksDisabled = false;
  
  private static boolean INITD = false;
   
  /** Stops all RiTa output to the console */
  public static boolean SILENT = false;
  
  protected static String[] guesses = { "src/data", "data", "" };
   
  static {
    if (!INITD) RiTa.init();
  }
  
  static void init()
  {
    if (!SILENT && !INITD) {
      INITD = true;
      System.out.println("[INFO] RiTa.version ["+VERSION+"]");
    }
  }
  
  // METHODS ///////////////////////////////////////////////////////////
  public static String stem(String s)                 { return Stemmer.getInstance().stem(s); }
  
  public static String stem(String s, String stemmerType){ return Stemmer.getInstance(stemmerType).stem(s); }

  public static String conjugate(String s, Map args)  { return getConjugator().handleArgs(args).conjugate(s); }
  public static String getPastParticiple(String s)    { return getConjugator().getPastParticiple(s); }
  public static String getPresentParticiple(String s) { return getConjugator().getPresentParticiple(s); }
  
  public static String getPhonemes(String s)          { return getFeature(s, PHONEMES);  }
  public static String getPhonemes(String[] s)        { return getFeature(s, PHONEMES);  }
  public static String getStresses(String s)          { return getFeature(s, STRESSES);  }
  public static String getStresses(String[] s)        { return getFeature(s, STRESSES);  }
  public static String getSyllables(String s)         { return getFeature(s, SYLLABLES); }
  public static String getSyllables(String[] s)       { return getFeature(s, SYLLABLES); }
  
  public static String[] getPosTags(String[] words)     { return PosTagger.getInstance().tag(words); }
  public static String[] getPosTags(String s)           { return PosTagger.getInstance().tag(tokenize(s)); }
  public static String getPosTagsInline(String[] words) { return PosTagger.getInstance().tagInline(words); }
  public static String getPosTagsInline(String s)       { return PosTagger.getInstance().tagInline(s); } 
  
  protected static long millisOffset = System.currentTimeMillis();
  
  protected static Conjugator conjugator;
  protected static Conjugator getConjugator()
  {
    if (conjugator == null)
      conjugator = new Conjugator();
    return conjugator;
  }
 
  public static String[] getPosTags(String[] words, boolean useWordnetTags) { 
    
    return useWordnetTags ? 
        PosTagger.getInstance().tagForWordNet(words) : PosTagger.getInstance().tag(words); 
  }
  
  public static String[] getPosTags(String s, boolean useWordnetTags) { 

    return getPosTags(tokenize(s), useWordnetTags);
  }
  
  public static int env() {
    
    return JAVA;
  }
  
  public static String trim(String s) {
    
    return s.trim(); 
  }

  public static int getWordCount(String s)  { 
    return RiTa.tokenize(s).length;
  }
  
  public static void shuffle(Object[] items)
  { 
    List tmp = new LinkedList();
    for (int i = 0; i < items.length; i++)
      tmp.add(items[i]);
    Collections.shuffle(tmp);
    int idx = 0;
    for (Iterator i = tmp.iterator(); i.hasNext(); idx++)
    {
      items[idx] = i.next();      
    }
  }
  
  /**
   * Packs an array of floats (size 4) representing (a,r,g,b) color values
   * into a single integer
   */
  public static int pack(int a, int r, int g, int b)
  {
     if (a > 255) a = 255; else if (a < 0) a = 0;
     if (r > 255) r = 255; else if (r < 0) r = 0;
     if (g > 255) g = 255; else if (g < 0) g = 0;
     if (b > 255) b = 255; else if (b < 0) b = 0;
     return (a << 24) | (r << 16) | (g << 8) | b;
  }
   
   /**
     * Unpacks a integer into an array of floats (size 4) representing (a,r,g,b)
     * color values
     */
  public static int[] unpack(int pix)
  {
    int a = (pix >> 24) & 0xff;
    int r = (pix >> 16) & 0xff;
    int g = (pix >> 8) & 0xff;
    int b = (pix) & 0xff;
    return new int[] { a, r, g, b };
  }

  public static String untokenize(String[] arr) 
  {
    return untokenize(arr, ' ', true);
  }
  
  public static String untokenize(String[] arr, char delim) 
  {
    return untokenize(arr, delim, true);
  }
  
  public static String untokenize(String[] arr, boolean adjustPunctuationSpacing) 
  {
    return untokenize(arr, ' ', adjustPunctuationSpacing);
  }
  
  public static Method _findCallback(Object parent, String callbackName)
  {
    try
    {
      return (callbackName == null) ? 
         _findMethod(parent, DEFAULT_CALLBACK, new Class[] { RiTaEvent.class }, false)
         : _findMethod(parent, callbackName, new Class[] {}, false);
    }
    catch (RiTaException e)
    {
      String msg = (callbackName == null) ? 
          DEFAULT_CALLBACK+"(RiTaEvent re);" : callbackName+"();";
      System.err.println("[WARN] Expected callback not found: "
          + shortName(parent)+"."+msg);
      return null;
    }
  }
  
  /**
   * Joins array of word, similar to words.join(delim), but attempts to preserve punctuation position
   * unless the 'adjustPunctuationSpacing' flag is set to false
   * @param  arr the array to join
   * @return  the joined array as a String
   */
  public static String untokenize
    (String[] arr, char delim, boolean adjustPunctuationSpacing) 
  {
    //System.out.println("RiTa.untokenize("+RiTa.asList(arr)+") "+adjustPunctuationSpacing);
    
    if (arr==null || arr.length < 1) return E;
    
    if (adjustPunctuationSpacing) {
      
        String newStr = arr[0] != null ? arr[0] : E;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] != null) {
                if (!arr[i].matches("[,\\.\\;\\:\\?\\!"+ALL_QUOTES+"]+"))
                    newStr += delim;
                newStr += arr[i];
            }
        }
        return newStr.trim();
    }

    return RiTa.join(arr,delim).trim();  
  }
  
  /**
   * Joins Array of String into space-delimited String.
   * @param full - Array of Strings to be joined
   * @return String containing elements of String[] or ""
   */
  public static String join(Object[] full)
  {
    return join(full, SP);
  }
  
  public static String join(List input)
  {    
    return join(input, SP);
  }
  
  public static float elapsed(long start) {
    return ((System.currentTimeMillis()-start)/1000f);
  } 
  
  /**
   * Joins Array of Objects into delimited String.
   * @param full - Array of Strings to be joined
   * @param delim - Delimiter to parse elements in resulting String
   * @return String containing elements of String[] or "" if null
   */
  public static String join(Object[] full, String delim)
  {
    StringBuilder result = new StringBuilder();
    if (full != null) {
      for (int index = 0; index < full.length; index++) {
        if (index == full.length - 1)
          result.append(full[index]);
        else
          result.append(full[index] + delim);
      }
    }
    return result.toString();
  }
  
  /**
   * Concatenates the array 'input' into a single String, spearated by 'delim'
   */
  public static String join(String[] input, char delim)
  {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < input.length; i++) {
      sb.append(input[i]);
      if (i < input.length-1) 
        sb.append(delim);
    }
    return sb.toString();
  }
  
  /**
   * Uses the default WordTokenizer to split the line into words
   * @see RiTokenizer
   */
  public static String[] tokenize(String line) {
    return RiTokenizer.getInstance().tokenize(line);
  }
  
  /**
   * Uses a RegexTokenizer to split the line into words
   * @see RiTokenizer
   */
  public static String[] tokenize(String line, String regex) {
    return (regex == null)  ? tokenize(line) :
      RiTokenizer.getRegexInstance(regex).tokenize(line);
  }
  
  /**
   * Trims punctuation from each side of the <code>token</code> 
   * (does not trim whitespace or internal punctuation).
   */ 
  public static String trimPunctuation(String token)
  {
    if (token == null || token.length()<1) return token;
    
    // Note: needs to handle byte-order marks...
    if (punctPattern == null) 
      punctPattern = Pattern.compile(PUNCT_PATT, Pattern.CASE_INSENSITIVE);
  
    Matcher m = punctPattern.matcher(token);
    boolean match = m.find();
    if (!match || m.groupCount() < 1) {
      System.err.println("[WARN] RiTa.trimPunctuation(): invalid regex state for String "
          + "\n       '" + token + "', perhaps an unexpected byte-order mark?");
      return token;
    }
     
    return m.group(1);
  }  static Pattern punctPattern = null;

  
  
  /**
   * An alternative to {@link String#split(String)} that optionally  
   * returns the delimiters.
   */
  public static String[] split
    (String toSplit, Pattern regexPattern, boolean returnDelims) // NIAPI 
  {
    if (!returnDelims) return regexPattern.split(toSplit);
    
    int index = 0;
    List matchList = new ArrayList();
    Matcher m = regexPattern.matcher(toSplit);
    while (m.find())
    {
      String match = toSplit.subSequence(index, m.start()).toString();
      matchList.add(match);
      matchList.add(toSplit.subSequence(m.start(), m.end()).toString());
      index = m.end();
    }

    if (index == 0) return new String[] { toSplit };

    matchList.add(toSplit.subSequence(index, toSplit.length()).toString());

    int resultSize = matchList.size();
    while (resultSize > 0 && matchList.get(resultSize - 1).equals(""))
      resultSize--;

    return (String[]) matchList.subList(0, resultSize).toArray(new String[resultSize]);
  }
  
  public static String pluralize(String noun) { 
    return Pluralizer.getInstance().pluralize(noun); 
  }

  /**
   * Returns true iff ALL characters in the string are punctuation 
   * @param s String to check
   * @return boolean
   */
  public static boolean isPunctuation(String s)   {
    if (PUNCT == null)
      PUNCT = Pattern.compile(ALL_PUNCT);
    return PUNCT.matcher(s).matches();
  }  protected static Pattern PUNCT = null;
  
  
  public static float distance(float x1, float y1, float x2, float y2) { 
		float dx = x1 - x2;
		float dy = y1 - y2;
		return (float) Math.sqrt(dx * dx + dy * dy);
		
  }  
  
  public static String singularize(String s)          { 
    return Stemmer.getInstance(StemmerType.Pling).stem(s);
  }
  
  public static String stripPunctuation(String phrase) {
    return stripPunctuation(phrase, null);
  }
  
  /**
   * Strips any punctuation characters from the String
   */
  public static String stripPunctuation(String phrase, char[] charsToIgnore)
  {
    if (phrase == null || phrase.length()<1) 
      return "";
    
    StringBuilder sb = new StringBuilder();
    OUTER: for (int i = 0; i < phrase.length(); i++) {
      char c = phrase.charAt(i);
      //System.out.println("char: "+c+" "+Character.valueOf(c));
      if (charsToIgnore != null)  {
        for (int j = 0; j < charsToIgnore.length; j++) {
          if (c == charsToIgnore[j]) {
            sb.append(c);
            continue OUTER;
          }
        }
      }
      if (PUNCT_CHARS.indexOf(c) < 0)
        sb.append(c);
    }
    return sb.toString();
  }
  
  
  /** Delegates to the default sentence-parser to split <code>text</code> into sentences */
  public static String[] splitSentences(String text) {
    return Splitter.getInstance().splitSentences(text);
  }
  
  /**
   * Returns a random element from a Collection (NAPI)
   * @return Object a random item
   */
  public static Object randomItem(Collection c) {
    
    if (c == null || c.isEmpty()) 
      throw new RiTaException("Null passed to randomItem()");
    
    int rand = (int)(Math.random()*c.size());
    Object result = null;
    Iterator it = c.iterator();
    for (int i = 0; i <= rand; i++)
      result = it.next();
    return result;
  }
  
  /**
   * Returns a random element from a List (NAPI)
   * @return Object a random item
   */
  public static Object randomItem(List list)
  { 
    if (list == null || list.size()==0) 
      throw new RiTaException("Null passed to randomItem()");
    int rand = (int)(Math.random()*list.size());
    return list.get(rand);
  }

  
  /**
   * Returns a random element from an array (NAPI)
   * @return Object the random item
   */
  public static Object randomItem(Object[] list)
  { 
    if (list == null || list.length==0) 
      throw new RiTaException("Null passed to randomItem()");
    int rand = (int)(Math.random()*list.length);
    return list[rand];
  }
  
  /** @exclude */
  public static final String upperCaseFirst(String value) {
    return Character.toString(value.charAt(0)).toUpperCase() + value.substring(1);
  }
  
  /**
   * Removes white-space and line breaks from start and end of String
   * @param s String to be chomped
   * @return string without starting or ending white-space or line-breaks
   */
  public static String chomp(String s)
  {
    if (CHOMP == null) 
     CHOMP = Pattern.compile("\\s+$|^\\s+");
    Matcher m = CHOMP.matcher(s);
    return m.replaceAll("");
  } static Pattern CHOMP;
  
  
  /** Returns true if 'input' is an abbreviation */
  public static boolean isAbbreviation(String input)
  {
    return abbreviations.contains(input); // case??
  }
  
  /**
   * Returns true if <code>sentence</code> starts with a question word.
   * * e.g., (is,are,does,who,what,why,where,when,etc.)
   */  
  public static boolean isQuestion(String sentence)
  {

    for (int i = 0; i < QUESTION_STARTS.length; i++)
      if ((sentence.trim().toUpperCase()).startsWith(QUESTION_STARTS[i].toUpperCase()))
        return true;
    return false;
  }
  
  /**
   * Returns true if <code>sentence</code> starts with a w-question word,
   * e.g., (who,what,why,where,when,etc.)
   */  
  public static boolean isW_Question(String sentence)
  {
    for (int i = 0; i < W_QUESTION_STARTS.length; i++)
      if ((sentence.trim().toUpperCase()).startsWith(W_QUESTION_STARTS[i].toUpperCase()))
        return true;
    return false;
  }

  
  /**
   * Returns true if 'currentWord' is the final word of a sentence. <p>
   * This is a simplified version of the OAK/JET sentence splitter method.
   */
  public static boolean isSentenceEnd(String currentWord, String nextWord)
  {
    //System.out.println("RiTa.isSentenceEnd("+currentWord+", "+nextWord+")");
    
    if (currentWord == null) return false;
    
    int cWL = currentWord.length();
    
    // token is a mid-sentence abbreviation (mainly, titles) --> middle of sent
    if (RiTa.isAbbreviation(currentWord))
      return false;
    
    if (cWL > 1 && isIn(currentWord.charAt(0), "`'\"([{<")
        && RiTa.isAbbreviation(currentWord.substring(1)))
      return false;

    if (cWL > 2 && ((currentWord.charAt(0) == '\'' 
      && currentWord.charAt(1) == '\'') || (currentWord.charAt(0) == '`' 
      && currentWord.charAt(1) == '`')) && RiTa.isAbbreviation(currentWord.substring(2)))
    {
      return false;
    }
    
    char currentToken0 = currentWord.charAt(cWL - 1);
    char currentToken1 = (cWL > 1) ? currentWord.charAt(cWL - 2) : ' ';
    char currentToken2 = (cWL > 2) ? currentWord.charAt(cWL - 3) : ' ';
    
    int nTL = nextWord.length();
    char nextToken0 = nextWord.charAt(0);
    char nextToken1 = (nTL > 1) ? nextWord.charAt(1) : ' ';
    char nextToken2 = (nTL > 2) ? nextWord.charAt(2) : ' ';

    // nextToken does not begin with an upper case,
    // [`'"([{<] + upper case, `` + upper case, or < -> middle of sent.
    if (!  (Character.isUpperCase(nextToken0) 
        || (Character.isUpperCase(nextToken1) && isIn(nextToken0, "`'\"([{<"))
        || (Character.isUpperCase(nextToken2) && ((nextToken0 == '`' && nextToken1 == '`') 
        || (nextToken0 == '\'' && nextToken1 == '\'')))
        ||  nextWord.equals("_") || nextToken0 == '<'))
      return false;

    // ends with ?, !, [!?.]["'}>)], or [?!.]'' -> end of sentence
    if (currentToken0 == '?'
        || currentToken0 == '!'
        || (isIn(currentToken1, "?!.") && isIn(currentToken0, "\"'}>)"))
        || (isIn(currentToken2, "?!.") && currentToken1 == '\'' && currentToken0 == '\''))
      return true;
      
    // last char not "." -> middle of sentence
    if (currentToken0 != '.') return false;

    // Note: wont handle Q. / A. at start of sentence, as in a news wire
    //if (startOfSentence && (currentWord.equalsIgnoreCase("Q.") 
      //|| currentWord.equalsIgnoreCase("A.")))return true; 
    
    // single upper-case alpha + "." -> middle of sentence
    if (cWL == 2 && Character.isUpperCase(currentToken1))
      return false;

    // double initial (X.Y.) -> middle of sentence << added for ACE
    if (cWL == 4 && currentToken2 == '.'
        && (Character.isUpperCase(currentToken1) && Character
            .isUpperCase(currentWord.charAt(0))))
      return false;

    // U.S. or U.N. -> middle of sentence
    //if (currentToken.equals("U.S.") || currentToken.equals("U.N."))
      //return false; // dch
      
    //f (Util.isAbbreviation(currentToken)) return false;
    
    // (for XML-marked text) next char is < -> end of sentence
    if (nextToken0 == '<')
      return true;
    
    return true;
  }
  
  
  /**
   * Returns a randomly ordered array
   * of unique integers from 0 to <code>numElements</code> -1.
   * The size of the array will be <code>numElements</code>. 
   */
  public static int[] randomOrdering(int numElements)
  { 
    int[] result = new int[numElements];
    List tmp = new LinkedList();
    for (int i = 0; i < result.length; i++)
      tmp.add(new Integer(i));
    Collections.shuffle(tmp);
    int idx = 0;
    for (Iterator iter = tmp.iterator(); iter.hasNext(); idx++)
      result[idx] = ((Integer)iter.next()).intValue();
    return result;
  }
  
  public static int timer(float period) { // for better error msg
    throw new RiTaException("Missing parent object -- did you mean: RiTa.timer(this, "+period+");"); 
  }
  
  public static int timer(float period, String fun) { return timer(period); } // for better error msg
  
  public static int timer(Object parent, float period) {
    return new RiTimer(parent, period).id();
  }
  
  public static int timer(Object parent, float period, String callbackFunctionName) {
    return new RiTimer(parent, period, callbackFunctionName).id();
  }
  
  public static void stopTimer(int id) { 
    RiTimer rt = RiTimer.findById(id);
    if (rt != null) rt.stop();
  } 
  
  public static void pauseTimer(int id, boolean b) {
    RiTimer rt = RiTimer.findById(id);
    if (rt != null) rt.pause(b);
  }
  
  public static void pauseTimer(int id, float pauseFor) {
    RiTimer rt = RiTimer.findById(id);
    if (rt != null) rt.pauseFor(pauseFor);
  }

  /**
   * Generates random numbers. Each time the <b>random()</b> function is
   * called, it returns an unexpected value within the specified range. If
   * one parameter is passed to the function it will return a <b>float</b>
   * between zero and the value of the <b>high</b> parameter. 
   */
  public static int random(int max) {  return random(0,max); } 

  /**
   * Generates random numbers. Each time the <b>random()</b> function is
   * called, it returns an unexpected value within the specified range. If
   * one parameter is passed to the function it will return a <b>float</b>
   * between zero and the value of the <b>high</b> parameter. 
   */
  public static int random(int low, int high) {
 
    return (int) (low+(random()*(high-low)));
  } 
  

  /**
   * Generates random numbers. Each time the <b>random()</b> function is
   * called, it returns an unexpected value within the specified range. If
   * one parameter is passed to the function it will return a <b>float</b>
   * between zero and the value of the <b>high</b> parameter. 
   */
  public static float random() { 
    if (internalRandom == null) 
      internalRandom = new Random();
    return internalRandom.nextFloat();
  } 
  protected static Random internalRandom;

  /**
   * Generates random numbers. Each time the <b>random()</b> function is
   * called, it returns an unexpected value within the specified range. If
   * one parameter is passed to the function it will return a <b>float</b>
   * between zero and the value of the <b>high</b> parameter. 
   */
  public static float random(float high)
  {
    // avoid an infinite loop
    if (high == 0)
      return 0;

    // internal random number object
    if (internalRandom == null)
      internalRandom = new Random();

    float value = 0;
    do
    {
      value = internalRandom.nextFloat() * high;
    }
    while (value == high);
    return value;
  }


  /**
   * Generates random numbers. Each time the <b>random()</b> function is
   * called, it returns an unexpected value within the specified range. If
   * one parameter is passed to the function it will return a <b>float</b>
   * between zero and the value of the <b>high</b> parameter. The function
   * call <b>random(5)</b> returns values between 0 and 5 (starting at zero,
   * up to but not including 5). If two parameters are passed, it will return
   * a <b>float</b> with a value between the the parameters. The function
   * call <b>random(-5, 10.2)</b> returns values starting at -5 up to (but
   * not including) 10.2. 
   */
  public static float random(float low, float high) {
    if (low >= high) return low;
    float diff = high - low;
    return random(diff) + low;
  }

 /**
   * Sets the seed value for <b>random()</b>. By default, <b>random()</b>
   * produces different results each time the program is run. Set the
   * <b>value</b> parameter to a constant to return the same pseudo-random
   * numbers each time the software is run.
   */
  public static void randomSeed(long seed) {
    // internal random number object
    if (internalRandom == null) internalRandom = new Random();
    internalRandom.setSeed(seed);
  }
  
  private static String escapeHTML(String s) { // problem?
    
    return EntityLookup.getInstance().escape(s);
  }
  
  public static String unescapeHTML(String s) { // TODO: add to reference?
    
    return EntityLookup.getInstance().unescape(s);
  }
  
  /*
   * Only relevant in the javascript version of RiTa
   */
  public static void p5Compatible(boolean b) {/* no-op */}  
  
  //////// HELPERS //////////////////////////////////////////////////////////
  
  /** Converts collection to String array */
  public static String[] strArr(Collection l)
  {
    if (l == null || l.size()==0) return RiTa.EMPTY;
    return (String[])l.toArray(new String[l.size()]);
  }
  
  public static Object invoke(Object callee, String methodName, Class[] argTypes, Object[] args)
  { 
    //System.out.println("INVOKE: "+callee.getClass()+"."+methodName+"(types="+asList(argTypes)+", vals="+asList(args)+")");      
    return _invoke(callee, _findMethod(callee, methodName, argTypes, true), args);
  }

  public static Object invoke(Object callee, String methodName)
  { 
    return invoke(callee, methodName, null, null);    
  }
  
  public static Object invoke(Object callee, String methodName, Object[] args)
  { 
    if (args == null) return invoke(callee, methodName);    
    Class[] argTypes = new Class[args.length];
    for (int i = 0; i < args.length; i++)  {
      argTypes[i] = args[i].getClass();  
      if (argTypes[i]==Integer.class)
        argTypes[i] = Integer.TYPE;
      else if (argTypes[i]==Boolean.class)
        argTypes[i] = Boolean.TYPE;
      else if (argTypes[i]==Float.class)
        argTypes[i] = Float.TYPE;
      else if (argTypes[i]==Double.class)
        argTypes[i] = Double.TYPE;
      else if (argTypes[i]==Character.class)
        argTypes[i] = Character.TYPE; 
    }
    return invoke(callee, methodName, argTypes, args);
  }   
  
  public static Object _invoke(Object callee, Method m, Object[] args)
  {
    try 
    {
      //System.out.println("INVOKE: "+callee+"."+m.getName()+"("+asList(args)+")");
      return m.invoke(callee, args);
    } 
    catch (Throwable e)
    {
      Throwable cause = e.getCause();
      while (cause != null) {
         e = cause;
         cause = e.getCause();
      }
      System.err.println("[WARN] Invoke error on "+RiTa.shortName
        (callee)+"."+m.getName()+"("+asList(args)+")\n  "+_exceptionToString(e));
      throw new RiTaException(e);      
    }
  }
  
  public static Method _findMethod(Object callee, String methodName, Class[] argTypes)
  {
    return _findMethod(callee, methodName, argTypes, false);
  }
  
  /** @exclude */
  public static Method _findMethod(Object callee, String methodName, Class[] argTypes, boolean isPublic)
  {
    //System.err.println("RiTa.findMethod("+callee+"."+methodName+"(), "+isPublic+")");
    
    if (callee == null) 
      throw new RiTaException("Method not found: null."+methodName+"()");    

    Method m = null;

    try
    {
      if (callee instanceof Class) {  // static method
        
        if (isPublic) {
          
          try
          {
            m = ((Class)callee).getMethod(methodName,  argTypes);
          }
          catch (Exception e) { }

        }
        if (m == null) {
          m = ((Class)callee).getDeclaredMethod(methodName,  argTypes);
          m.setAccessible(true);
        }
      }
      else                       // non-static method
      {
        if (isPublic) 
        {
          try
          {
            m = callee.getClass().getMethod(methodName, argTypes);
          }
          catch (Exception e) { }

        }
        if (m == null) {
          m = callee.getClass().getDeclaredMethod(methodName, argTypes);
          m.setAccessible(true);
        }
      }
    }
    catch (SecurityException e)
    {
      throw new RuntimeException(e);
    }
    catch (NoSuchMethodException e)
    {
      throw new RiTaException("Method not found: " +
          callee.getClass().getName()+"."+methodName+"()");      
    }
  
    return m;
  }
  
  /**
   * Returns a String representation of Exception and stacktrace
   *  (only elements with line numbers)
   */  
  public static String _exceptionToString(Throwable e) {
    
    if (e == null) return "null";
    
    StringBuilder s = new StringBuilder(e+"\n");
    StackTraceElement[] stes = e.getStackTrace();
    for (int i = 0; i < stes.length; i++)
    {
      String ste = stes[i].toString();
      if (ste.matches(".*[0-9]+\\)"))
        s.append("    "+ste+'\n');
    }
    return s.toString();
  }  
  
  /** @exclude */
  protected static boolean isIn(char c, String s) {
    
    return s.indexOf(c) >= 0;
  }
  
  /**
   * Concatenates the list 'input' into a single String, separated by 'delim'
   */
  public static String join(List input, String delim)
  {    
    StringBuilder sb = new StringBuilder();
    if (input != null) {
      for (Iterator i = input.iterator(); i.hasNext();) {
        sb.append(i.next());      
        if (i.hasNext())
          sb.append(delim);
      }
    }
    return sb.toString();
  }
  
  /**
   * Opens an InputStream to the specified filename  
   * @exclude 
   */
  public static InputStream _openStream(Class resourceDir, String fileName) 
  {
    return resourceDir.getResourceAsStream(fileName);
  }
      
  /*public static InputStream _openStreamP5(PApplet p, String fileName) 
  {
    return openStreamLocal(fileName);
    //System.err.println("openStream("+p+", "+fileName+")");
    InputStream is = null;
    try
    {
      if (p != null) {
        is =  p.createInput(fileName);
      }
      else 
        is = openStreamLocal(fileName);
      
      if (is == null) throw new RiTaException("null IS");
    }
    catch (RiTaException e) {
      throw new RiTaException("Unable to open stream: "+fileName+" with pApplet="+p);
    }
    return is;//new UnicodeInputStream(is);
  }*/
  
  protected static String[] includedFiles = new String[] { "addenda.txt", "bin.gz" };
  protected static boolean isIncluded(String fname) {
    for (int i = 0; i < includedFiles.length; i++) {
      if (fname.endsWith(includedFiles[i]))
        return true;
    }
    return false;
  }  
  
  public static InputStream openStream(String streamName) // need to handle URLs here..
  {
    //System.out.println("RiTa.openStreamLocal("+streamName+")");

    try // check for url first  (from PApplet)
    {
      URL url = new URL(streamName);
      return url.openStream();
    } catch (MalformedURLException mfue) {
      // not a url, that's fine
    } catch (FileNotFoundException fnfe) {
      // Java 1.5 likes to throw this when URL not available.
      // http://dev.processing.org/bugs/show_bug.cgi?id=403
    } catch (Throwable e) 
    {
      throw new RiTaException("Throwable in openStreamLocal()",e);
    }     
    
    InputStream is = null;
    
    for (int i = 0; i < guesses.length; i++) {
      String guess = streamName;
      if (guesses[i].length() > 0) { 
        if (_isAbsolutePath(guess)) continue;
        guess = guesses[i] + SLASH + guess;
      }
   
      //boolean isDefaultFile = isIncluded(guess);       
      //if (!isDefaultFile && !RiTa.SILENT) 
        //System.out.print("[INFO] Trying "+guess);
      
      try {
        is = new FileInputStream(guess);
        //if (!isDefaultFile&& !RiTa.SILENT) System.out.println("... OK");
      } 
      catch (FileNotFoundException e) {
        //if (!isDefaultFile&& !RiTa.SILENT) System.out.println("... failed");
      }
      if (is != null) break;
    }
    
    if (is == null) // last try with classloader... 
    {
      // Using getClassLoader() prevents java from converting dots
      // to slashes or requiring a slash at the beginning.
      // (a slash as a prefix means that it'll load from the root of
      // the jar, rather than trying to dig into the package location)
      ClassLoader cl = RiTa.class.getClassLoader();

      // by default, data files are exported to the root path of the jar.
      // (not the data folder) so check there first.
      //if (!RiTa.SILENT)System.out.print("[INFO] Trying data/" + streamName+" as resource");
      
      is = cl.getResourceAsStream("data/" + streamName);
      if (is != null) {
        String cn = is.getClass().getName();
        // this is an irritation of sun's java plug-in, which will return
        // a non-null stream for an object that doesn't exist. like all good
        // things, this is probably introduced in java 1.5. awesome!
        // http://dev.processing.org/bugs/show_bug.cgi?id=359
        if (!cn.equals("sun.plugin.cache.EmptyInputStream")) {
          //if (!RiTa.SILENT) System.out.println("... OK");
          return is;
        }
      }
      //if (!RiTa.SILENT)System.out.println("... failed");
    }
    
    if (is == null) 
      throw new RiTaException("Unable to create stream for: "+streamName);
    
    return is;
  } 
  
  public static String[] loadStrings(String fname)
  {    
    return loadStrings(openStream(fname), 100);
  }

  
  protected static String[] loadStrings(InputStream input, int numLines) {
    
    if (input == null) throw new RiTaException("Null input stream!");
    
    try {
      BufferedReader reader = new BufferedReader
         (new InputStreamReader(input, "UTF-8"));

      String lines[] = new String[numLines];
      int lineCount = 0;
      String line = null;
      while ((line = reader.readLine()) != null) {
        if (lineCount == lines.length) {
          String temp[] = new String[lineCount << 1];
          System.arraycopy(lines, 0, temp, 0, lineCount);
          lines = temp;
        }
        lines[lineCount++] = line;
      }
      reader.close();

      if (lineCount == lines.length) {
        return lines;
      }

      // resize array to appropriate amount for these lines
      String output[] = new String[lineCount];
      System.arraycopy(lines, 0, output, 0, lineCount);
      return output;

    }
    catch (IOException e) {
      e.printStackTrace();
    }
    
    return EMPTY;
  } 
  
  /** @exclude */ 
  public static boolean _isAbsolutePath(String fileName) {
    return (fileName.startsWith(SLASH) || 
     fileName.matches("^[A-Za-z]:")); // hmmmmm... 'driveA:\\'?
  }
  
  /**
   * Loads a file or URL by name and reads the 
   * contents into a single String
   * 
   * @return Contents of the file as String
   */
  public static String loadString(String fileName) {

    String[] lines = loadStrings(fileName);
    return RiTa.join(lines,"\n");
  }
  
  // TODO: add a version that takes a callback function (like in RiTimer)
  public static String loadString(Object parent, String fileName)
  {
    if (parent != null && parent instanceof processing.core.PApplet) {
        
      Object result = invoke(parent, "loadBytes", 
            new Class[] { String.class }, new Object[] { fileName });
        
        if (result != null && result instanceof String)
          return (String) result;
    }
    else {
      
      System.err.println("[WARN] Failed calling PApplet.loadBytes...");
    }
    
    
    return loadString(fileName);
  }
  
  /*
   * (Only used as backup method)
   * Loads a File by name and reads the  contents into a single String
   * @return Contents of the file as String

  protected static String loadStringOld(PApplet pApplet, String filename) {
    byte[] bytes = null;
    if (pApplet != null) {
      // ok, we're good w' papplet
      bytes = pApplet.loadBytes(filename);
    }
    else  {// uh-oh, who knows?
      bytes = PApplet.loadBytes(openStreamLocal(filename));
    }    
    
    if (bytes == null)
      throw new RiTaException("The file '"+filename+"' is missing or inaccessible, " +
        "make sure the URL is valid or that the file has been added to your data " +
        "folder and is readable.");  
    
    return new String(bytes);
  }   */
  
  /** Returns a String holding the current working directory */
  public static String cwd() {
    
    String cwd = "unknown";
    try {
      cwd = System.getProperty("user.dir");
    }
    catch (Exception e) {
      System.out.println("[WARN] Unable to determine current directory!");
    }
    return cwd;
  }
  
  /** @exclude */
  public static boolean _lastCharMatches(String string, char[] chars)
  {   
    char c = string.charAt(string.length()-1);   
    for (int i = 0; i < chars.length; i++)
      if (c==chars[i])
        return true;
    return false;
  }
  
  /** 
   * Returns time since 'start' of program in ms
   * @exclude  
   */
  public static int millis() {
    return (int)(System.currentTimeMillis()-millisOffset);
  }  
  
  public static int millis(long startTime)
  {
    return (int)(System.currentTimeMillis()-startTime);
  }
  
  /** @exclude */
  public static List asList(Object[] o)
  {    
    return (o == null) ? new ArrayList() : Arrays.asList(o);
  }
  
  /** @exclude */
  public static List asList(float[] o)
  {    
    return (o == null) ? new ArrayList() : Arrays.asList(o);
  }
  
  /** @exclude */
  public static List asList(int[] o)
  {    
    return (o == null) ? new ArrayList() : Arrays.asList(o);
  }
  
  /** @exclude */
  public static String shortName(Class c)
  {
    String name = c.getName();    
    int idx = name.lastIndexOf(".");
    return name.substring(idx+1);
  }
  
  /** @exclude */
  public static String shortName(Object c)
  {
    return shortName(c.getClass());   
  }
  
  protected static String getFeature(String[] str, String featureName)    {
    
    return getFeature(RiTa.join(str), featureName);
  }
  
  protected static String getFeature(String str, String featureName)    {
    
    RiString riString = new RiString(str);
    String feature = riString.get(featureName);
    return feature == null ? E : feature;
  }

  public static Set abbreviations = new HashSet(64);

  static {
    abbreviations.add("Adm.");
    abbreviations.add("Capt.");
    abbreviations.add("Cmdr.");
    abbreviations.add("Col.");
    abbreviations.add("Dr.");
    abbreviations.add("Gen.");
    abbreviations.add("Gov.");
    abbreviations.add("Lt.");
    abbreviations.add("Maj.");
    abbreviations.add("Messrs.");
    abbreviations.add("Mr.");
    abbreviations.add("Mrs.");
    abbreviations.add("Ms.");
    abbreviations.add("Prof.");
    abbreviations.add("Rep.");
    abbreviations.add("Reps.");
    abbreviations.add("Rev.");
    abbreviations.add("Sen.");
    abbreviations.add("Sens.");
    abbreviations.add("Sgt.");
    abbreviations.add("Sr.");
    abbreviations.add("St.");

    // abbreviated first names
    abbreviations.add("Benj.");
    abbreviations.add("Chas.");
    // abbreviations.add("Alex."); // dch
    
    // abbreviated months
    abbreviations.add("Jan.");
    abbreviations.add("Feb.");
    abbreviations.add("Mar.");
    abbreviations.add("Apr.");
    abbreviations.add("Mar.");
    abbreviations.add("Jun.");
    abbreviations.add("Jul.");
    abbreviations.add("Aug.");
    abbreviations.add("Sept.");
    abbreviations.add("Oct.");
    abbreviations.add("Nov.");
    abbreviations.add("Dec.");

    // other abbreviations
    abbreviations.add("a.k.a.");
    abbreviations.add("c.f.");
    abbreviations.add("i.e.");
    abbreviations.add("e.g.");
    abbreviations.add("vs.");
    abbreviations.add("v.");

    Set tmp = new HashSet(64);
    Iterator it = abbreviations.iterator();
    while (it.hasNext())
      tmp.add(((String) it.next()).toLowerCase());
    abbreviations.addAll(tmp);
  }
  
  public static String stackToString(Throwable aThrowable) {
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);
    aThrowable.printStackTrace(printWriter);
    return result.toString();
  }
  
  /**
   * Whether LTS notifications are output to the console
   */
  public static boolean PRINT_LTS_INFO = false;

  public static void out(Collection l)
  {
    int i = 0;
    if (l == null || l.size() <1) {
      System.out.println("[]");
      return;
    }
    for (Iterator it = l.iterator(); it.hasNext(); i++)
      System.out.println(i + ") '" + it.next()+"'");
  } 
  public static void out(int[] l)
  {
    if (l == null || l.length <1) {
      System.out.println("[]");
      return;
    }
    for (int j = 0; j < l.length; j++)
      System.out.println(j + ") '" + l[j]+"'");
  }
  public static void out(float[] l)
  {
    if (l == null || l.length <1) {
      System.out.println("[]");
      return;
    }
    for (int j = 0; j < l.length; j++)
      System.out.println(j + ") '" + l[j]+"'");
  }
  public static void out(Object[] l)
  {
    if (l == null || l.length <1) {
      System.out.println("[]");
      return;
    }
    for (int j = 0; j < l.length; j++)
      System.out.println(j + ") '" + l[j]+"'");
  }
  public static void out(Map l)
  {
    if (l == null || l.size() <1) {
      System.out.println("[]");
      return;
    }
    for (Iterator it = l.keySet().iterator(); it.hasNext();)
    {
      Object key = it.next();
      Object val = l.get(key);
      System.out.println(key + "='"+val+"'");
    }
  }
  public static void out(Object l)
  {
    System.out.println(l);
  }

  public static void main(String[] args)
  {
    //PApplet.loadBytes("/User/dhowe/Desktop/times-24.json");
    if (1==1) return;
    
    String[] toks = tokenize("The boy, dressed in red, ate an apple.!?");
    toks[0] = "@#$%^#$%^";
    toks[1] = "can't";
    toks[2] = "--";
    toks[3] = "\"'`";
    for (int i = 0; i < toks.length; i++)
    {
      System.out.println(toks[i]+" -> "+isPunctuation(toks[i])+"");
    }
  }


}
