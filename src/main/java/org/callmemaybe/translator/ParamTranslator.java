package org.callmemaybe.translator;

import static org.callmemaybe.util.ComplianceChecks.isSpecCompilable;

import org.callmemaybe.extractor.DocumentedExecutable;
import org.callmemaybe.extractor.ParamTag;
import randoop.condition.specification.Guard;
import randoop.condition.specification.Precondition;

public class ParamTranslator {

  public Precondition translate(ParamTag tag, DocumentedExecutable excMember) {
    final String commentTranslation =
        isDescriptiveComment(tag.getComment().getText())
            ? ""
            : BasicTranslator.translate(tag, excMember);

    final Guard guard = new Guard(tag.getComment().getText(), commentTranslation);

    if (commentTranslation.isEmpty() || !isSpecCompilable(excMember, guard.getConditionSource())) {
      return new Precondition(tag.toString(), new Guard(tag.getComment().getText(), ""));
    }

    return new Precondition(tag.toString(), guard);
  }

  private boolean isDescriptiveComment(String text) {
    return text.matches("(.*) is (If|if) (.*)");
  }
}
