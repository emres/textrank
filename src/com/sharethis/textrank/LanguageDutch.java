/*
Copyright (c) 2009, ShareThis, Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.

    * Neither the name of the ShareThis, Inc., nor the names of its
      contributors may be used to endorse or promote products derived
      from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.sharethis.textrank;

import java.io.FileInputStream;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Sequence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.tartarus.snowball.ext.dutchStemmer;


/**
 * Implementation of Dutch-specific tools for natural language processing.
 *
 * @author Emre Sevinc emre.sevinc@gmail.com
 * @author Robert Gibbon
 */
public class LanguageDutch extends LanguageModel {
  private final static Log LOG = LogFactory.getLog(LanguageEnglish.class.getName());

  /**
   * Public definitions.
   */
  public static SentenceModel splitter_nl = null;
  public static TokenizerModel tokenizer_nl = null;
  public static POSModel tagger_nl = null;
  public static dutchStemmer stemmer_nl = null;


  /**
   * Constructor. Not quite a Singleton pattern but close enough
   * given the resources required to be loaded ONCE.
   */
  public LanguageDutch (final String path) throws Exception {
    if (splitter_nl == null) {
      loadResources(path);
    }
  }


  /**
   * Load libraries for OpenNLP for this specific language.
   */
  public void loadResources (final String path) throws Exception {
    splitter_nl = new SentenceModel(new FileInputStream(path + "/opennlp/nl-sent.bin"));
    tokenizer_nl = new TokenizerModel(new FileInputStream(path + "/opennlp/nl-token.bin"));
    tagger_nl = new POSModel((new FileInputStream(path + "/opennlp/nl-pos-maxent.bin")));
    stemmer_nl = new dutchStemmer();
  }


  /**
   * Split sentences within the paragraph text.
   */
  public String[] splitParagraph (final String text) {
    return new SentenceDetectorME(splitter_nl).sentDetect(text);
  }


  /**
   * Tokenize the sentence text into an array of tokens.
   */
  public String[] tokenizeSentence (final String text) {
    final String[] token_list = new TokenizerME(tokenizer_nl).tokenize(text);

    for (int i = 0; i < token_list.length; i++) {
      token_list[i] = token_list[i].replace("\"", "").toLowerCase().trim();
    }

    return token_list;
  }


  /**
   * Run a part-of-speech tagger on the sentence token list.
   */
  public String[] tagTokens (final String[] token_list) {
    final Sequence[] sequences = new POSTaggerME(tagger_nl).topKSequences(token_list);
    final String[] tag_list = new String[token_list.length];

    int i = 0;

    for (Object obj : sequences[0].getOutcomes()) {
        tag_list[i] = (String) obj;
        i++;
    }

    return tag_list;
  }


  /**
   * Prepare a stable key for a graph node (stemmed, lemmatized)
   * from a token.
   */
  public String getNodeKey (final String text, final String pos) throws Exception {
    return pos.substring(0, 1) + stemToken(scrubToken(text)).toLowerCase();
  }


  /**
   * Determine whether the given PoS tag is a noun.
   */
  public boolean isNoun (final String pos) {
    return pos.startsWith("N");
  }


  /**
   * Determine whether the given PoS tag is an adjective.
   */
  public boolean isAdjective (final String pos) {
    return pos.startsWith("Adj");
  }


  /**
   * Perform stemming on the given token.
   */
  public String stemToken (final String token) {
    stemmer_nl.setCurrent(token);
    stemmer_nl.stem();

    return stemmer_nl.getCurrent();
  }
}
