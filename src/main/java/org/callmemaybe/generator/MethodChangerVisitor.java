package org.callmemaybe.generator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.callmemaybe.CallMeMaybe;
import org.callmemaybe.conf.Configuration;
import org.callmemaybe.extractor.DocumentedExecutable;
import org.callmemaybe.extractor.DocumentedParameter;
import org.callmemaybe.translator.spec.EquivalenceSpec;
import org.callmemaybe.translator.spec.PostAssertion;
import org.callmemaybe.util.Checks;
import org.callmemaybe.util.ComplianceChecks;
import org.reflections.Reflections;
import randoop.condition.specification.OperationSpecification;
import randoop.condition.specification.Postcondition;
import randoop.condition.specification.Precondition;
import randoop.condition.specification.ThrowsCondition;

// FIXME CURRENTLY DESIGNED TO WORK ON EQ-ONLY

/**
 * Visitor that modifies the aspect template (see method {@code visit}) to generate an aspect
 * (oracle) for a {@code ExecutableMember}.
 */
public class MethodChangerVisitor
    extends ModifierVisitor<Pair<DocumentedExecutable, EquivalenceSpec>> {

  /** Holds CallMeMaybe configuration options. */
  private final Configuration conf = CallMeMaybe.configuration;

  //  /**
  //   * Modifies the methods {@code advice} and {@code getExpectedExceptions}, {@code
  //   * paramTagsSatisfied}, {@code checkResult} of the aspect template, injecting the appropriate
  //   * source code to get an aspect (oracle) for the method arg.
  //   *
  //   * @param methodDeclaration the method declaration of the method to visit
  //   * @param spec the {@code ExecutableMember} for which to generate the aspect (oracle)
  //   * @return the {@code methodDeclaration} modified as and when needed
  //   * @throws NullPointerException if {@code methodDeclaration} or {@code executableMember} is
  // null
  //   */
  //  @Override
  //  public Node visit(
  //      MethodDeclaration methodDeclaration,
  //      Pair<DocumentedExecutable, ? extends OperationSpecification> spec) {
  //    Checks.nonNullParameter(methodDeclaration, "methodDeclaration");
  //    Checks.nonNullParameter(spec, "spec");
  //
  //    DocumentedExecutable documentedExecutable = spec.getKey();
  //    OperationSpecification operationSpec = spec.getValue();
  //
  //    switch (methodDeclaration.getName().asString()) {
  //      case "advice":
  //        adviceChanger(methodDeclaration, documentedExecutable);
  //        break;
  //      case "getExpectedExceptions":
  //        getExpectedExceptionChanger(methodDeclaration, documentedExecutable, operationSpec);
  //        break;
  //      case "paramTagsSatisfied":
  //        paramTagSatisfiedChanger(methodDeclaration, documentedExecutable, operationSpec);
  //        break;
  //      case "checkResult":
  //        checkResultChanger(methodDeclaration, documentedExecutable, operationSpec);
  //        break;
  //      case "equivalenceHolds":
  //        if (operationSpec instanceof EqOperationSpecification) {
  //          equivalenceHoldsChanger(
  //              methodDeclaration, documentedExecutable, (EqOperationSpecification)
  // operationSpec);
  //        }
  //        break;
  //      case "snippetWrapper":
  //        if (operationSpec instanceof EqOperationSpecification) {
  //          snippetWrapperChanger(
  //              methodDeclaration, documentedExecutable, (EqOperationSpecification)
  // operationSpec);
  //        }
  //        break;
  //    }
  //    return methodDeclaration;
  //  }

  /**
   * Modifies the methods {@code advice} and {@code getExpectedExceptions}, {@code
   * paramTagsSatisfied}, {@code checkResult} of the aspect template, injecting the appropriate
   * source code to get an aspect (oracle) for the method arg.
   *
   * @param methodDeclaration the method declaration of the method to visit
   * @param spec the {@code ExecutableMember} for which to generate the aspect (oracle)
   * @return the {@code methodDeclaration} modified as and when needed
   * @throws NullPointerException if {@code methodDeclaration} or {@code executableMember} is null
   */
  @Override
  public Node visit(
      MethodDeclaration methodDeclaration, Pair<DocumentedExecutable, EquivalenceSpec> spec) {
    Checks.nonNullParameter(methodDeclaration, "methodDeclaration");
    Checks.nonNullParameter(spec, "spec");

    DocumentedExecutable documentedExecutable = spec.getKey();
    EquivalenceSpec operationSpec = spec.getValue();

    switch (methodDeclaration.getName().asString()) {
      case "advice":
        try {
          adviceChanger(methodDeclaration, documentedExecutable);
        } catch (IOException e) {
          e.printStackTrace();
        }
        break;
      case "equivalenceHolds":
        equivalenceHoldsChanger(methodDeclaration, documentedExecutable, operationSpec);
        break;
      case "snippetWrapper":
        snippetWrapperChanger(methodDeclaration, documentedExecutable, operationSpec);
        break;
    }
    return methodDeclaration;
  }

  private void equivalenceHoldsChanger(
      MethodDeclaration methodDeclaration,
      DocumentedExecutable executableMember,
      EquivalenceSpec eqSpec) {
    // Replace first parameter name ("result") with specific name from configuration.
    methodDeclaration.getParameter(0).setName(new SimpleName(Configuration.RETURN_VALUE));
    // Replace second parameter name ("target") with specific name from configuration.
    methodDeclaration.getParameter(1).setName(new SimpleName(Configuration.RECEIVER));
    // Replace third parameter name ("clone") with specific name from configuration.
    methodDeclaration.getParameter(2).setName(new SimpleName(Configuration.RECEIVER_CLONE));
    // Check equivalences.

    //    for (EquivalenceSpec eqSpec : spec.getEquivalenceSpecs()) {
    String guard = addCasting(eqSpec.getGuard().getConditionSource(), executableMember, false);
    PostAssertion postAssertion = eqSpec.getPostAssertion();
    String property = addCasting(postAssertion.getAssertionContent(), executableMember, false);
    if (property.isEmpty()) {
      // TODO why does this happen and how can we avoid it
      return;
    }

    ReturnStmt returnResultStmt = new ReturnStmt(new NameExpr("false"));

    String checkThenBlock = "if ((" + property + ")==false) { " + returnResultStmt + " }";

    String thenBlock = "";
    if (!postAssertion.getPreceedingStatements().isEmpty()) {
      LinkedList<String> statements = postAssertion.getBodyContent();

      List<String> restOfStatements;
      // FIXME manage the snippet smartly, PostAssertion should have the dummyMethod filled.
      if (statements.indexOf("//END OF METHOD") != -1) {
        restOfStatements =
            statements.subList(statements.indexOf("//END OF METHOD") + 1, statements.size());
      } else {
        restOfStatements = statements;
      }
      for (String stm : restOfStatements) {
        Statement parse = JavaParser.parseStatement(addCasting(stm, executableMember, true));
        thenBlock += parse.toString();
      }
    }

    IfStmt ifStmt =
        createIfStmt(
            guard, eqSpec.getDescription(), createBlock(thenBlock + "\n" + checkThenBlock));

    methodDeclaration.getBody().ifPresent(body -> body.addStatement(ifStmt));
    //  }

    ReturnStmt finalRetStm = new ReturnStmt(new NameExpr("true"));
    methodDeclaration.getBody().ifPresent(body -> body.addStatement(finalRetStm));
  }

  private void snippetWrapperChanger(
      MethodDeclaration methodDeclaration,
      DocumentedExecutable executableMember,
      EquivalenceSpec eqSpec) {

    // Replace first parameter name ("target") with specific name from configuration.
    methodDeclaration.getParameter(0).setName(new SimpleName(Configuration.RECEIVER_CLONE));

    // Check postconditions.
    //   for (EquivalenceSpec eqSpec : spec.getEquivalenceSpecs()) {
    PostAssertion postAssertion = eqSpec.getPostAssertion();
    if (!postAssertion.getPreceedingStatements().isEmpty()) {
      //        LinkedList<String> statements = postAssertion.getBodyContent();

      // FIXME manage the snippet smartly, PostAssertion should have the dummyMethod filled.
      //        if (statements.indexOf("//END OF METHOD") == -1) {
      //          return;
      //        }

      // FIXME manage the snippet smartly, PostAssertion should have the dummyMethod filled.
      //        List<String> dummyMethod = statements.subList(1, statements.indexOf("//END OF
      // METHOD") - 1);

      LinkedList<String> dummyMethod = postAssertion.getSnippetWrapper().getStatements();
      if (!dummyMethod.isEmpty()) {
        String dummyMethodDeclaration = dummyMethod.pop();
        // TODO could be managed better
        if (dummyMethodDeclaration.contains("public <")) {
          String parametrization =
              dummyMethodDeclaration.substring(
                  dummyMethodDeclaration.indexOf("<") + 1, dummyMethodDeclaration.indexOf(">"));
          String[] allTypeParam = parametrization.split(",");
          for (String p : allTypeParam) {
            methodDeclaration.addTypeParameter(p);
          }
        }

        // TODO could be managed better
        if (dummyMethod.getLast().equals("}")) {
          dummyMethod.removeLast();
        }

        for (String stm : dummyMethod) {
          methodDeclaration
              .getBody()
              .ifPresent(
                  body ->
                      body.addStatement(
                          JavaParser.parseStatement(addCasting(stm, executableMember, true))));
        }
      }
    }
    //    }

    //    String signature = dummyMethod.get(0);
    //    String parameterList = signature.substring(signature.indexOf("("),
    // signature.indexOf(")"));

  }

  private void checkResultChanger(
      MethodDeclaration methodDeclaration,
      DocumentedExecutable executableMember,
      OperationSpecification spec) {
    // Replace first parameter name ("result") with specific name from configuration.
    methodDeclaration.getParameter(0).setName(new SimpleName(Configuration.RETURN_VALUE));
    // Replace second parameter name ("target") with specific name from configuration.
    methodDeclaration.getParameter(1).setName(new SimpleName(Configuration.RECEIVER));
    // Check postconditions.
    for (Postcondition postSpecification : spec.getPostconditions()) {
      String guard =
          addCasting(postSpecification.getGuard().getConditionSource(), executableMember, false);
      String property =
          addCasting(postSpecification.getProperty().getConditionSource(), executableMember, false);
      if (property.isEmpty()) {
        // TODO why does this happen and how can we avoid it
        continue;
      }
      String check = createBlock("if ((" + property + ")==false) { fail(\"Error!\"); }");
      IfStmt ifStmt = createIfStmt(guard, postSpecification.getDescription(), check);
      methodDeclaration.getBody().ifPresent(body -> body.addStatement(ifStmt));
    }
    ReturnStmt returnResultStmt = new ReturnStmt(new NameExpr(Configuration.RETURN_VALUE));
    methodDeclaration.getBody().ifPresent(body -> body.addStatement(returnResultStmt));
  }

  private void paramTagSatisfiedChanger(
      MethodDeclaration methodDeclaration,
      DocumentedExecutable executableMember,
      OperationSpecification specification) {
    // Replace first parameter name ("target") with specific name from configuration.
    methodDeclaration.getParameter(0).setName(new SimpleName(Configuration.RECEIVER));
    boolean returnStmtNeeded = true;
    for (Precondition preSpecification : specification.getPreconditions()) {
      String condition = preSpecification.getGuard().getConditionSource();
      if (condition.isEmpty()) {
        continue; // TODO Does it make sense to have empty guards here? We should avoid that.
      }
      condition = addCasting(condition, executableMember, false);
      String thenBlock = createBlock("return true;");
      String elseBlock = "";
      IfStmt ifStmt =
          createIfStmt(condition, preSpecification.getDescription(), thenBlock, elseBlock);
      methodDeclaration.getBody().ifPresent(body -> body.addStatement(ifStmt));
      returnStmtNeeded = false;
    }
    if (!returnStmtNeeded) {
      ReturnStmt returnFalseStmt = new ReturnStmt(new BooleanLiteralExpr(false));
      methodDeclaration.getBody().ifPresent(body -> body.addStatement(returnFalseStmt));
    } else {
      ReturnStmt returnTrueStmt = new ReturnStmt(new BooleanLiteralExpr(true));
      methodDeclaration.getBody().ifPresent(body -> body.addStatement(returnTrueStmt));
    }
  }

  private void getExpectedExceptionChanger(
      MethodDeclaration methodDeclaration,
      DocumentedExecutable executableMember,
      OperationSpecification operationSpec) {
    // Replace first parameter name ("target") with specific name from configuration.
    methodDeclaration.getParameter(0).setName(new SimpleName(Configuration.RECEIVER));
    for (ThrowsCondition throwsSpecification : operationSpec.getThrowsConditions()) {
      if (throwsSpecification.getGuard().getConditionSource().isEmpty()) {
        continue;
      }
      String condition =
          addCasting(throwsSpecification.getGuard().getConditionSource(), executableMember, false);

      IfStmt ifStmt = new IfStmt();
      Expression conditionExpression;
      conditionExpression = JavaParser.parseExpression(condition);
      ifStmt.setCondition(conditionExpression);
      // Add a try-catch block to prevent runtime error when looking for an exception type
      // that is not on the classpath.
      String addExpectedException =
          "{try{expectedExceptions.add("
              + "Class.forName(\""
              + throwsSpecification.getExceptionTypeName()
              + "\")"
              + ");} catch (ClassNotFoundException e) {"
              + "System.err.println(\"Class not found!\" + e);}}";
      ifStmt.setThenStmt(JavaParser.parseBlock(addExpectedException));

      // Add a try-catch block to avoid NullPointerException to be raised while evaluating a
      // boolean condition generated by CallMeMaybe. For example, suppose that the first argument
      // of a method is null, and that CallMeMaybe generates a condition like
      // args[0].isEmpty()==true. The condition generates a NullPointerException that we want
      // to ignore.
      ClassOrInterfaceType nullPointerException =
          JavaParser.parseClassOrInterfaceType("java.lang.NullPointerException");
      CatchClause catchClause =
          new CatchClause(new Parameter(nullPointerException, "e"), JavaParser.parseBlock("{}"));
      NodeList<CatchClause> catchClauses = new NodeList<>();
      catchClauses.add(catchClause);

      TryStmt nullCheckTryCatch = new TryStmt();
      nullCheckTryCatch.setTryBlock(JavaParser.parseBlock("{" + ifStmt.toString() + "}"));
      nullCheckTryCatch.setCatchClauses(catchClauses);

      // Add comment to if condition. The comment is the original comment in the Java source
      // code that has been translated by CallMeMaybe in the commented boolean condition.
      // CommentContent has to be added here, cause otherwise is ignored by JavaParser.parseBlock.
      final Optional<Statement> ifCondition =
          nullCheckTryCatch
              .getTryBlock()
              .getStatements()
              .stream()
              .filter(stm -> stm instanceof IfStmt)
              .findFirst();
      if (ifCondition.isPresent()) {
        String comment = throwsSpecification.getDescription();
        ifCondition.get().setComment(new LineComment(comment));
      }

      methodDeclaration.getBody().ifPresent(body -> body.addStatement(nullCheckTryCatch));
    }

    methodDeclaration
        .getBody()
        .ifPresent(blockStmt -> blockStmt.addStatement("return expectedExceptions;"));
  }

  private void adviceChanger(
      MethodDeclaration methodDeclaration, DocumentedExecutable executableMember)
      throws IOException {
    StringBuilder pointcut = new StringBuilder();
    if (executableMember.isConstructor()) {
      pointcut.append(
          "execution("
              + getPointcut(executableMember, executableMember.getDeclaringClass().getName())
              + ")");
    } else {
      pointcut
          .append("(call(")
          .append(getPointcut(executableMember, executableMember.getDeclaringClass().getName()))
          .append(")");

      // FIXME "com.google" is just a test for guava
      Reflections reflections = new Reflections("com.google");
      // We have to add more classes that may contain the method call
      Set<Class<?>> subTypes =
          (Set<Class<?>>) reflections.getSubTypesOf(executableMember.getDeclaringClass());
      for (Class st : subTypes) {
        pointcut
            .append(" || ")
            .append("call(")
            .append(getPointcut(executableMember, st.getName()))
            .append(")");
      }
      // TODO print csv for subclasses
      FileWriter csvInterfaces = new FileWriter("interfaces.csv", true);
      csvInterfaces.append(executableMember.getDeclaringClass().getName());
      for (Class st : subTypes) {
        if (!st.getName().contains("$")) {
          csvInterfaces.append(";");
          csvInterfaces.append(st.getName());
        }
      }
      csvInterfaces.append("\n");
      csvInterfaces.close();

      String testClassName = conf.getTestClass();
      if (testClassName != null) {
        pointcut.append(") && within(").append(testClassName).append(")");
      }
    }

    AnnotationExpr annotation =
        new SingleMemberAnnotationExpr(
            new Name("Around"), new StringLiteralExpr(pointcut.toString()));
    NodeList<AnnotationExpr> annotations = methodDeclaration.getAnnotations();
    annotations.add(annotation);
    methodDeclaration.setAnnotations(annotations);
  }

  private static String createBlock(String content) {
    return "{" + content + "}";
  }

  private static IfStmt createIfStmt(String condition, String comment, String thenBlock) {
    return createIfStmt(condition, comment, thenBlock, "");
  }

  private static IfStmt createIfStmt(
      String condition, String comment, String thenBlock, String elseBlock) {
    IfStmt ifStmt = new IfStmt();
    Expression conditionExpression;
    conditionExpression = JavaParser.parseExpression(condition);
    ifStmt.setCondition(conditionExpression);
    try {
      ifStmt.setThenStmt(JavaParser.parseBlock(thenBlock));
    } catch (ParseProblemException e) {
      System.err.println(e.getMessage());
    }
    if (!elseBlock.isEmpty()) {
      ifStmt.setElseStmt(JavaParser.parseBlock(elseBlock));
    }
    ifStmt.setComment(new LineComment(" " + comment));
    return ifStmt;
  }

  /**
   * Generates the AspectJ pointcut definition to be used to match the given {@code
   * ExecutableMember}. A pointcut definition looks like {@code call(void C.foo())}. Given a {@code
   * ExecutableMember} describing the method C.foo(), this method returns the string {@code
   * call(void C.foo())}.
   *
   * @param executable {@code ExecutableMember} for which to generate the pointcut definition
   * @return the pointcut definition matching {@code method}
   */
  private static String getPointcut(DocumentedExecutable executable, String containingClass) {
    StringBuilder pointcut = new StringBuilder();

    if (executable.isConstructor()) { // Constructors
      pointcut.append(containingClass).append(".new(");
    } else { // Regular methods
      String type = executable.getReturnType().getType().getTypeName();
      type = removeParametersFromType(type);
      //      String containingClass = executable.getDeclaringClass().getName();

      pointcut
          .append(type)
          .append(" ")
          .append(containingClass)
          .append(".")
          .append(executable.getName());
    }

    StringJoiner parametersJoiner = new StringJoiner(", ", "(", ")");
    for (DocumentedParameter documentedParameter : executable.getParameters()) {
      parametersJoiner.add(documentedParameter.getTypeName());
    }
    pointcut.append(parametersJoiner.toString());
    return pointcut.toString();
  }

  /**
   * Add the appropriate cast to each mention of a method argument or target in a given Java boolean
   * condition.
   *
   * @param condition the Java boolean condition to which add the casts
   * @param method the method to which the {@code condition} belongs
   * @return the input condition with casted method arguments and target
   * @throws NullPointerException if {@code condition} or {@code method} is null
   */
  private static String addCasting(
      String condition, DocumentedExecutable method, boolean inSnippet) {
    Checks.nonNullParameter(condition, "condition");
    Checks.nonNullParameter(method, "method");

    int index = 0;
    for (DocumentedParameter parameter : method.getParameters()) {
      String type = parameter.getType().getTypeName();
      type = removeParametersFromType(type);
      if (ComplianceChecks.primitiveTypes().contains(type)) {
        type = ComplianceChecks.fromPrimitiveToObject(type);
      } else if (inSnippet && parameter.toString().contains(">")) {
        // TODO could be managed better
        type = parameter.toString().substring(0, parameter.toString().indexOf(">") + 1);
      }
      condition = condition.replace("args[" + index + "]", "((" + type + ") args[" + index + "])");
      index++;
    }

    // Casting of result object in condition.
    String returnType = method.getReturnType().getType().getTypeName();
    returnType = removeParametersFromType(returnType);
    if (ComplianceChecks.isGenericType(returnType)) {
      // FIXME this might be wrong, since the check in ComplianceChecks is naive - improve it
      returnType = "java.lang.Object";
    } else if (ComplianceChecks.primitiveTypes().contains(returnType)) {
      returnType = ComplianceChecks.fromPrimitiveToObject(returnType);
    }
    if (!returnType.equals("void")) {
      condition =
          condition.replace(
              Configuration.RETURN_VALUE,
              "((" + returnType + ")" + Configuration.RETURN_VALUE + ")");
    }

    // Casting of target object in condition.
    String type = method.getDeclaringClass().getName();
    type = removeParametersFromType(type);
    condition =
        condition
            .replace(Configuration.RECEIVER, "((" + type + ") " + Configuration.RECEIVER + ")")
            .replace(
                Configuration.RECEIVER_CLONE,
                "((" + type + ") " + Configuration.RECEIVER_CLONE + ")");

    return condition;
  }

  /**
   * Remove the type parameter(s) from a generic type and returns the resulting string.
   *
   * @param genericType the generic type to be cleaned
   * @return the type without parameter(s)
   */
  @NotNull
  private static String removeParametersFromType(String genericType) {
    int generics = genericType.indexOf("<");
    if (generics != -1) {
      return genericType.substring(0, generics);
    }
    return genericType;
  }
}
