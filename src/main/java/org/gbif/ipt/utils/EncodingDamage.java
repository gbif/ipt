/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;

/**
 * Finds characters in a value that look damaged by an encoding problem.
 */
public final class EncodingDamage {

  // what characters that aren't valid in the encoding are read as
  public static final char REPLACEMENT = '�';

  // UTF-8 read as ISO-8859-1 or windows-1252: the lead byte of a 2-byte sequence (C2, C3) shown as Â or Ã followed by
  // the continuation byte (80-BF) as a Latin-1 character or a windows-1252 one (e.g. Ã¸ for ø, Ã– for Ö), or the
  // 3-byte sequences of punctuation (e.g. â€™ for ’)
  private static final String CP1252_80_9F = "€‚ƒ„…†‡ˆ‰Š‹"
                                             + "ŒŽ‘’“”•–—˜™"
                                             + "š›œžŸ";
  private static final Pattern MOJIBAKE = Pattern.compile(
    "[ÂÃ][\u0080-¿" + CP1252_80_9F + "]|â€[\u0080-¿" + CP1252_80_9F + "]");
  // a letter replaced by a question mark when converting to an encoding that doesn't have it, e.g. Skakbr?t
  private static final Pattern QUESTION_MARK_IN_WORD = Pattern.compile("(?<=\\p{L})\\?(?=\\p{L})");

  private EncodingDamage() {
  }

  /**
   * @param invalid true if the value has characters that weren't valid in the encoding, read as U+FFFD, which are
   *                reported as such rather than as damage
   */
  public static boolean looksDamaged(String value, boolean invalid) {
    if (!invalid && value.indexOf(REPLACEMENT) >= 0) {
      return true;
    }
    if (MOJIBAKE.matcher(value).find()) {
      return true;
    }
    return questionMarksCanBeDamage(value) && QUESTION_MARK_IN_WORD.matcher(value).find();
  }

  /**
   * A quick check of several values at once, e.g. the values of a line, before checking them one by one.
   *
   * @return false if none of the values can look damaged, true if one may
   */
  public static boolean mayLookDamaged(String[] values) {
    // joined with a character that's neither a letter nor part of a damaged character, so no match spans two values
    String text = String.join("\n", values);
    return text.indexOf(REPLACEMENT) >= 0 || MOJIBAKE.matcher(text).find()
           || QUESTION_MARK_IN_WORD.matcher(text).find();
  }

  // URLs have question marks between letters
  private static boolean questionMarksCanBeDamage(String value) {
    return !value.contains("://");
  }

  /**
   * @return the value escaped for HTML, with U+FFFD and the characters that look damaged in &lt;mark&gt;
   */
  public static String markHtml(String value) {
    if (value == null) {
      return "";
    }
    boolean[] marked = new boolean[value.length()];
    for (int i = 0; i < value.length(); i++) {
      marked[i] = value.charAt(i) == REPLACEMENT;
    }
    mark(MOJIBAKE.matcher(value), marked);
    if (questionMarksCanBeDamage(value)) {
      mark(QUESTION_MARK_IN_WORD.matcher(value), marked);
    }

    StringBuilder html = new StringBuilder();
    int start = 0;
    while (start < value.length()) {
      int end = start;
      while (end < value.length() && marked[end] == marked[start]) {
        end++;
      }
      String part = StringEscapeUtils.escapeHtml4(value.substring(start, end));
      html.append(marked[start] ? "<mark>" + part + "</mark>" : part);
      start = end;
    }
    return html.toString();
  }

  private static void mark(Matcher matcher, boolean[] marked) {
    while (matcher.find()) {
      for (int i = matcher.start(); i < matcher.end(); i++) {
        marked[i] = true;
      }
    }
  }
}
