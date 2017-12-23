// © 2017 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
package com.ibm.icu.impl.number.parse;

import java.util.Iterator;

import com.ibm.icu.impl.TextTrieMap;
import com.ibm.icu.text.UnicodeSet;
import com.ibm.icu.util.Currency;
import com.ibm.icu.util.Currency.CurrencyStringInfo;
import com.ibm.icu.util.ULocale;

/**
 * @author sffc
 *
 */
public class CurrencyTrieMatcher implements NumberParseMatcher {

    private final TextTrieMap<CurrencyStringInfo> longNameTrie;
    private final TextTrieMap<CurrencyStringInfo> symbolTrie;

    public static NumberParseMatcher getInstance(ULocale locale) {
        // TODO: Pre-compute some of the more popular locales?
        return new CurrencyTrieMatcher(locale);
    }

    private CurrencyTrieMatcher(ULocale locale) {
        longNameTrie = Currency.getParsingTrie(locale, Currency.LONG_NAME);
        symbolTrie = Currency.getParsingTrie(locale, Currency.SYMBOL_NAME);
    }

    @Override
    public boolean match(StringSegment segment, ParsedNumber result) {
        if (result.currencyCode != null) {
            return false;
        }

        TextTrieMap.Output trieOutput = new TextTrieMap.Output();
        Iterator<CurrencyStringInfo> values = longNameTrie.get(segment, 0, trieOutput);
        if (values == null) {
            values = symbolTrie.get(segment, 0, trieOutput);
        }
        if (values != null) {
            result.currencyCode = values.next().getISOCode();
            segment.adjustOffset(trieOutput.matchLength);
            result.setCharsConsumed(segment);
        }
        return trieOutput.partialMatch;
    }

    @Override
    public UnicodeSet getLeadChars(boolean ignoreCase) {
        UnicodeSet leadChars = new UnicodeSet();
        longNameTrie.putLeadChars(leadChars);
        symbolTrie.putLeadChars(leadChars);
        return leadChars.freeze();
    }

    @Override
    public void postProcess(ParsedNumber result) {
        // No-op
    }

    @Override
    public String toString() {
        return "<CurrencyTrieMatcher>";
    }
}