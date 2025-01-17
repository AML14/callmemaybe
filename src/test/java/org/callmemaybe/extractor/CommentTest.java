package org.callmemaybe.extractor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;
import org.junit.Test;

public class CommentTest {

  @Test
  public void testComment() {
    String multipleTags =
        "This comment contains a {@code codeElement}, another {@code one}, and a last {@code codeElement}";
    String simpleTag = "This comment contains a {@code codeElement}";
    String complexTag = "This comment contains a {@code complex codeElement}";
    String specialCharTag = "This comment contains {@code specialCharacters[]}";
    String expressionTag = "This comment contains an expression: {@code i<0}";
    String regularHTMLTags = "This comment contains <b>bold text</b>";
    String selfClosingHTMLTags = "This comment contains a break <br/>";
    String mixedHTMLTags = "This comment contains <b>bold text</b> and a break <br/>";

    CommentContent simpleComment = new CommentContent(simpleTag);
    List<Integer> codeWordOccurrences = simpleComment.getWordsMarkedAsCode().get("codeElement");
    assertThat(codeWordOccurrences, not(is(nullValue())));
    assertThat(codeWordOccurrences.size(), is(1));
    assertThat(codeWordOccurrences.get(0), is(0));

    CommentContent multiTagComment = new CommentContent(multipleTags);
    codeWordOccurrences = multiTagComment.getWordsMarkedAsCode().get("codeElement");
    // codeElement is tagged as code twice (both the first and second occurrence) in
    // multiTagComment.
    // Thus we expect to find the values 0 and 1 (first and second occurrence).
    assertThat(codeWordOccurrences, not(is(nullValue())));
    assertThat(codeWordOccurrences.size(), is(2));
    assertThat(codeWordOccurrences.get(0), is(0));
    assertThat(codeWordOccurrences.get(1), is(1));

    codeWordOccurrences = multiTagComment.getWordsMarkedAsCode().get("one");
    assertThat(codeWordOccurrences, not(is(nullValue())));
    assertThat(codeWordOccurrences.size(), is(1));
    assertThat(codeWordOccurrences.get(0), is(0));

    CommentContent complexComment = new CommentContent(complexTag);
    codeWordOccurrences = complexComment.getWordsMarkedAsCode().get("complex");
    assertThat(codeWordOccurrences, not(is(nullValue())));
    assertThat(codeWordOccurrences.size(), is(1));
    assertThat(codeWordOccurrences.get(0), is(0));

    codeWordOccurrences = complexComment.getWordsMarkedAsCode().get("codeElement");
    assertThat(codeWordOccurrences, not(is(nullValue())));
    assertThat(codeWordOccurrences.size(), is(1));
    assertThat(codeWordOccurrences.get(0), is(0));

    CommentContent specialCharComment = new CommentContent(specialCharTag);
    codeWordOccurrences = specialCharComment.getWordsMarkedAsCode().get("specialCharacters[]");
    assertThat(codeWordOccurrences, not(is(nullValue())));
    assertThat(codeWordOccurrences.size(), is(1));
    assertThat(codeWordOccurrences.get(0), is(0));

    // From expression such as i<0, we do not retain signs and numbers
    CommentContent exprComment = new CommentContent(expressionTag);
    assertThat(exprComment.getWordsMarkedAsCode().size(), is(1));
    codeWordOccurrences = exprComment.getWordsMarkedAsCode().get("i");
    assertThat(codeWordOccurrences, not(is(nullValue())));

    CommentContent regularHTMLComment = new CommentContent(regularHTMLTags);
    assertThat(regularHTMLComment.getText(), is("This comment contains bold text"));

    CommentContent scHTMLComment = new CommentContent(selfClosingHTMLTags);
    assertThat(scHTMLComment.getText(), is("This comment contains a break"));

    CommentContent mixedHTMLComment = new CommentContent(mixedHTMLTags);
    assertThat(mixedHTMLComment.getText(), is("This comment contains bold text and a break"));
  }
}
