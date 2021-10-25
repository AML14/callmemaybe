package org.memo.translator;

import static org.memo.util.ComplianceChecks.isSpecCompilable;

import org.memo.extractor.DocumentedExecutable;
import org.memo.extractor.ThrowsTag;
import randoop.condition.specification.Guard;
import randoop.condition.specification.ThrowsCondition;

public class ThrowsTranslator {

  public ThrowsCondition translate(ThrowsTag tag, DocumentedExecutable excMember) {
    final String commentTranslation =
        alwaysThrowException(tag.getComment().getText())
            ? "true"
            : BasicTranslator.translate(tag, excMember);

    final Guard guard = new Guard(tag.getComment().getText(), commentTranslation);
    final String exceptionName = tag.getException().getName();

    if (commentTranslation.isEmpty() || !isSpecCompilable(excMember, guard.getConditionSource())) {
      return new ThrowsCondition(
          tag.toString(), new Guard(tag.getComment().getText(), ""), exceptionName);
    }

    return new ThrowsCondition(tag.toString(), guard, exceptionName);
  }

  /**
   * Returns true if an exception is always thrown by the {@code DocumentedExecutable}
   *
   * @param commentText the String comment belonging to the {@code ThrowsTag}
   * @return true if the comment just states "always", false otherwise
   */
  private boolean alwaysThrowException(String commentText) {
    commentText = commentText.replace(".", "");
    if (commentText.equalsIgnoreCase("always")) {
      return true;
    }

    return false;
  }
}
