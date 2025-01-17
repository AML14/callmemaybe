package org.callmemaybe.extractor;

import java.util.Objects;
import org.callmemaybe.util.Checks;

/**
 * Represents a Javadoc block tag (e.g., {@code @param}, {@code @return}, {@code @thraws}) and its
 * associated text. Each {@code JavadocComment} consists of a {@link JavadocComment.Kind}
 * ({@code @param}, {@code @return}, or {@code @throws}), a comment which is the text introduced by
 * the tag, and a specification (available after the translation of the comment). A specification is
 * the translation of the comment into a Java boolean condition.
 */
public abstract class JavadocComment {

  /** The Javadoc block tags currently supported by CallMeMaybe. */
  public enum Kind {
    FREETEXT, // the main description that precedes tags
    PARAM, // @param
    RETURN, // @return
    THROWS; // @throws and @exception

    @Override
    public String toString() {
      final String label = name();
      switch (label) {
        case "PARAM":
          return "@param";
        case "RETURN":
          return "@return";
        case "THROWS":
          return "@throws";
        case "FREETEXT":
          return "FreeText";
        default:
          throw new IllegalStateException("The value " + label + " has no string representation.");
      }
    }
  }

  /** The kind of this tag (e.g., @throws, @param). */
  private final Kind kind;

  /** The comment of this tag. */
  private CommentContent comment;

  /**
   * Constructs a {@code JavadocComment} of the specific kind, with the given comment.
   *
   * @param kind the comment kind, must not be null
   * @param comment the comment associated with the exception, must not be null
   */
  public JavadocComment(Kind kind, CommentContent comment) {
    Checks.nonNullParameter(kind, "kind");
    Checks.nonNullParameter(comment, "comment");
    this.kind = kind;
    this.comment = comment;
  }

  /**
   * Returns the kind of this tag (e.g., @throws, @param).
   *
   * @return the kind of this tag
   */
  public Kind getKind() {
    return kind;
  }

  /**
   * Returns the comment associated with the exception in this tag.
   *
   * @return the comment associated with the exception in this tag
   */
  public CommentContent getComment() {
    return comment;
  }

  /**
   * Sets the comment for this tag.
   *
   * @param comment the comment for this tag, must not be null
   */
  public void setComment(CommentContent comment) {
    Checks.nonNullParameter(comment, "comment");
    this.comment = comment;
  }

  /**
   * Returns true if this {@code JavadocComment} and the specified object are equal.
   *
   * @param obj the object to test for equality
   * @return true if this object and {@code obj} are equal
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof JavadocComment)) return false;

    JavadocComment that = (JavadocComment) obj;
    return kind.equals(that.kind) && comment.equals(that.comment);
  }

  /**
   * Returns the hash code of this object.
   *
   * @return the hash code of this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(comment, kind);
  }

  /**
   * Returns a string representation of this tag. The returned string is in the format "@Kind
   * COMMENT" where COMMENT is the text of the comment in the tag.
   *
   * @return a string representation of this tag
   */
  @Override
  public String toString() {
    return kind + " " + comment.getText();
  }
}
