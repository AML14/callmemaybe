package org.callmemaybe.extractor;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import java.util.List;
import java.util.Objects;
import org.callmemaybe.util.Checks;

/** Represents a class or interface that is documented with Javadoc comments. */
public final class DocumentedType {

  /** Documented class or interface (or enum, ...). */
  private final Class<?> documentedClass;
  /** Constructors and methods of this documented type. */
  private final List<DocumentedExecutable> documentedExecutables;

  private final List<String> classesInPackage;
  private final NodeList<ImportDeclaration> imports;
  /**
   * Creates a new DocumentedType wrapping the given class and with the given constructors and
   * methods.
   *
   * @param documentedClass the {@code Class} of this documentedClass
   * @param documentedExecutables constructors and methods of {@code documentedClass}
   * @param classesInPackage
   * @throws NullPointerException if either documentedClass or documentedExecutables is null
   */
  DocumentedType(
      Class<?> documentedClass,
      List<DocumentedExecutable> documentedExecutables,
      NodeList<ImportDeclaration> imports,
      List<String> classesInPackage) {
    Checks.nonNullParameter(documentedClass, "documentedClass");
    Checks.nonNullParameter(documentedExecutables, "documentedExecutables");
    this.documentedClass = documentedClass;
    this.documentedExecutables = documentedExecutables;
    this.imports = imports;
    this.classesInPackage = classesInPackage;
  }

  /**
   * Returns the runtime class of the documented type this DocumentedType represents.
   *
   * @return the runtime class of the documented type this DocumentedType represents
   */
  public Class<?> getDocumentedClass() {
    return documentedClass;
  }

  /**
   * Returns constructors and methods of this {@code DocumentedType}.
   *
   * @return constructors and methods of this {@code DocumentedType}
   */
  public List<DocumentedExecutable> getDocumentedExecutables() {
    return documentedExecutables;
  }

  public List<String> getClassesInPackage() {
    return classesInPackage;
  }

  public NodeList<ImportDeclaration> getImports() {
    return imports;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DocumentedType)) {
      return false;
    }
    DocumentedType that = (DocumentedType) o;
    return documentedClass.equals(that.documentedClass)
        && documentedExecutables.equals(that.documentedExecutables);
  }

  @Override
  public int hashCode() {
    return Objects.hash(documentedClass, documentedExecutables);
  }
}
